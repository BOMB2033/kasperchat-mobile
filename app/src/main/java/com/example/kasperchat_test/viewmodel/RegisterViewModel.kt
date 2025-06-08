package com.example.kasperchat_test.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.api.ApiService
import com.example.kasperchat_test.api.RetrofitClient
import com.example.kasperchat_test.model.RegisterRequest
import com.example.kasperchat_test.model.UserProfile
import com.example.kasperchat_test.signalr.SignalRManager
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import androidx.core.content.edit

sealed class RegisterResult {
    data class Success(val token: String, val userId: String, val userProfile: UserProfile?) : RegisterResult()
    data class Error(val message: String, val code: Int? = null) : RegisterResult()
    data object Loading : RegisterResult()
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    application: Application,
    private val apiService: ApiService,
    private val signalRManager: SignalRManager,
    private val gson: Gson
) : AndroidViewModel(application) {

    private val _registerResult = MutableLiveData<RegisterResult>()
    val registerResult: LiveData<RegisterResult> = _registerResult

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private var isFetchingProfile = false

    private companion object {
        const val AUTH_PREFS_NAME = "auth_prefs"
        const val AUTH_TOKEN_KEY = "auth_token"
        const val TOKEN_EXPIRY_KEY = "token_expiry"
        const val USER_PROFILE_KEY = "user_profile"
    }

    fun register(fullName: String, login: String, password: String, email: String? = null) {
        _registerResult.postValue(RegisterResult.Loading)
        viewModelScope.launch {
            try {
                val request = RegisterRequest(
                    username = login.trim(),
                    password = password,
                    fullName = fullName.trim(),
                    email = email?.trim()
                )
                val response = apiService.registerUser(request)

                if (response.isSuccessful && response.body() != null) {
                    val registerResponse = response.body()!!
                    val token = registerResponse.token
                    val userId = registerResponse.userId
                    // Предполагаем, что токен действителен 12 часов, как на сервере
                    val expiry = System.currentTimeMillis() + 12 * 60 * 60 * 1000
                    saveAuthToken(token, expiry)
                    RetrofitClient.setToken(token)
                    signalRManager.startConnection()
                    Log.i("RegisterViewModel", "Регистрация успешна: userId=$userId")

                    if (!isFetchingProfile) {
                        fetchCurrentUserProfile { fetchedProfile ->
                            _registerResult.postValue(RegisterResult.Success(token, userId, fetchedProfile))
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = when (response.code()) {
                        400 -> "Некорректные данные. Проверьте логин, пароль, имя или email. $errorBody"
                        409 -> "Пользователь с таким логином уже существует. $errorBody"
                        else -> "Ошибка регистрации: ${response.code()} - ${response.message()}. $errorBody"
                    }
                    Log.e("RegisterViewModel", errorMessage)
                    _registerResult.postValue(RegisterResult.Error(errorMessage, response.code()))
                }
            } catch (e: IOException) {
                Log.e("RegisterViewModel", "Сетевая ошибка при регистрации", e)
                _registerResult.postValue(RegisterResult.Error("Сетевая ошибка. Проверьте подключение.", null))
            } catch (e: JsonSyntaxException) {
                Log.e("RegisterViewModel", "Ошибка парсинга ответа сервера", e)
                _registerResult.postValue(RegisterResult.Error("Ошибка обработки данных сервера.", null))
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Непредвиденная ошибка при регистрации", e)
                _registerResult.postValue(RegisterResult.Error("Произошла ошибка. Попробуйте снова.", null))
            }
        }
    }

    fun fetchCurrentUserProfile(onResult: ((UserProfile?) -> Unit)? = null) {
        if (getAuthToken() == null || isFetchingProfile) {
            Log.w("RegisterViewModel", "Cannot fetch profile: token is null or fetch in progress")
            onResult?.invoke(null)
            return
        }
        isFetchingProfile = true
        viewModelScope.launch {
            try {
                Log.d("RegisterViewModel", "Fetching current user profile...")
                val response = apiService.getCurrentUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    saveUserProfile(user)
                    _userProfile.postValue(user)
                    Log.i("RegisterViewModel", "User profile fetched successfully: $user")
                    onResult?.invoke(user)
                } else {
                    Log.e("RegisterViewModel", "Failed to fetch user profile: ${response.code()} - ${response.message()}")
                    if (response.code() == 401) {
                        clearAuthToken()
                        _userProfile.postValue(null)
                    }
                    onResult?.invoke(null)
                }
            } catch (e: IOException) {
                Log.e("RegisterViewModel", "Network error fetching user profile", e)
                onResult?.invoke(null)
            } catch (e: Exception) {
                Log.e("RegisterViewModel", "Unexpected error fetching user profile", e)
                onResult?.invoke(null)
            } finally {
                isFetchingProfile = false
            }
        }
    }

    fun validateInput(fullName: String, login: String, password: String, email: String? = null): String? {
        return when {
            login.trim().isEmpty() -> "Логин не может быть пустым."
            login.trim().length < 3 -> "Логин должен содержать минимум 3 символа."
            login.trim().length > 50 -> "Логин не должен превышать 50 символов."
            password.isEmpty() -> "Пароль не может быть пустым."
            password.length < 6 -> "Пароль должен содержать минимум 6 символов."
            password.length > 100 -> "Пароль не должен превышать 100 символов."
            fullName.trim().isEmpty() -> "Имя не может быть пустым."
            fullName.trim().length < 2 -> "Имя должно содержать минимум 2 символа."
            fullName.trim().length > 100 -> "Имя не должен превышать 100 символов."
            email != null && email.trim().isNotEmpty() && !email.trim().matches(Regex("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) ->
                "Неверный формат email."
            email != null && email.trim().length > 100 -> "Email не должен превышать 100 символов."
            else -> null
        }
    }

    private fun saveAuthToken(token: String, expiry: Long) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString(AUTH_TOKEN_KEY, token)
            putLong(TOKEN_EXPIRY_KEY, expiry)
        }
        Log.i("RegisterViewModel", "Auth token saved with expiry: $expiry")
    }

    private fun getAuthToken(): String? {
        val (token, expiry) = loadToken()
        return if (expiry != null && expiry > System.currentTimeMillis()) token else null
    }

    private fun loadToken(): Pair<String?, Long?> {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
        val expiry = if (sharedPreferences.contains(TOKEN_EXPIRY_KEY)) {
            sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
        } else null
        return Pair(token, expiry)
    }

    private fun saveUserProfile(userProfile: UserProfile) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val userProfileJson = gson.toJson(userProfile)
            sharedPreferences.edit {
                putString(USER_PROFILE_KEY, userProfileJson)
            }
            Log.i("RegisterViewModel", "User profile saved to SharedPreferences: $userProfileJson")
        } catch (e: Exception) {
            Log.e("RegisterViewModel", "Error serializing user profile to JSON", e)
        }
    }

    private fun clearAuthToken() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            remove(AUTH_TOKEN_KEY)
            remove(TOKEN_EXPIRY_KEY)
            remove(USER_PROFILE_KEY)
        }
        _userProfile.postValue(null)
        signalRManager.stopConnection()
        Log.i("RegisterViewModel", "Auth token and user profile cleared.")
    }
}