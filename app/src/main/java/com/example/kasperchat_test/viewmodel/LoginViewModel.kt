package com.example.kasperchat_test.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.model.LoginRequest
import com.example.kasperchat_test.model.UserProfile
import com.example.kasperchat_test.network.RetrofitClient // Импортируем наш клиент
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import java.io.IOException // Для обработки сетевых ошибок

sealed class LoginResult {
    data class Success(val token: String, val userProfile: UserProfile?) : LoginResult() // Добавляем userProfile
    data class Error(val message: String) : LoginResult()
    data object Loading : LoginResult()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    // LiveData для хранения профиля пользователя
    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val apiService = RetrofitClient.instance
    private val gson = Gson() // Для сериализации/десериализации UserProfile

    private companion object {
        const val AUTH_PREFS_NAME = "auth_prefs"
        const val AUTH_TOKEN_KEY = "auth_token"
        const val USER_PROFILE_KEY = "user_profile" // Ключ для сохранения профиля
    }
    init {
        // При инициализации ViewModel пытаемся загрузить сохраненный профиль и токен
        loadUserProfileFromPrefs()
        if (getAuthToken() != null && _userProfile.value == null) {
            // Если есть токен, но нет профиля в LiveData (например, после перезапуска приложения),
            // попробуем загрузить его с сервера.
            fetchCurrentUserProfile()
        }
    }
    fun loginUser(email: String, pass: String) {
        _loginResult.postValue(LoginResult.Loading)
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(username = email, password = pass)
                val response = apiService.loginUser(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    val token = loginResponse.token
                    saveAuthToken(token)
                    Log.i("LoginViewModel", "Login successful. Token: $token")

                    // После успешного логина и сохранения токена, запрашиваем профиль пользователя
                    val profileResponse = apiService.getCurrentUserProfile()
                    if (profileResponse.isSuccessful && profileResponse.body() != null) {
                        val user = profileResponse.body()!!
                        saveUserProfile(user) // Сохраняем профиль
                        _userProfile.postValue(user)
                        _loginResult.postValue(LoginResult.Success(token, user))
                        Log.i("LoginViewModel", "User profile fetched and saved: $user")
                    } else {
                        Log.e("LoginViewModel", "Failed to fetch user profile after login: ${profileResponse.code()} - ${profileResponse.message()}")
                        // Логин успешен, но профиль не получен. Можно решить, как обрабатывать:
                        // 1. Считать логин успешным без профиля (текущая реализация _loginResult)
                        // 2. Считать это ошибкой логина.
                        _loginResult.postValue(LoginResult.Success(token, null)) // Успех, но профиль не получен
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = when (response.code()) {
                        400 -> "Неверный запрос: Имя пользователя и пароль обязательны. $errorBody"
                        401 -> "Не авторизован: Неверные учетные данные. $errorBody"
                        else -> "Ошибка входа: ${response.code()} - ${response.message()}. $errorBody"
                    }
                    Log.e("LoginViewModel", errorMessage)
                    _loginResult.postValue(LoginResult.Error(errorMessage))
                }
            } catch (e: IOException) {
                Log.e("LoginViewModel", "Сетевая ошибка во время входа или получения профиля", e)
                _loginResult.postValue(LoginResult.Error("Сетевая ошибка. Проверьте ваше подключение."))
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Непредвиденная ошибка во время входа или получения профиля", e)
                _loginResult.postValue(LoginResult.Error("Произошла непредвиденная ошибка."))
            }
        }
    }
    fun fetchCurrentUserProfile(onResult: ((UserProfile?) -> Unit)? = null) {
        if (getAuthToken() == null) {
            Log.w("LoginViewModel", "Cannot fetch profile, token is null.")
            onResult?.invoke(null)
            return
        }
        viewModelScope.launch {
            try {
                Log.d("LoginViewModel", "Fetching current user profile...")
                val response = apiService.getCurrentUserProfile()
                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    saveUserProfile(user)
                    _userProfile.postValue(user)
                    Log.i("LoginViewModel", "User profile fetched successfully: $user")
                    onResult?.invoke(user)
                } else {
                    Log.e("LoginViewModel", "Failed to fetch user profile: ${response.code()} - ${response.message()}")
                    // Если не удалось получить профиль (например, токен истек), можно очистить старый
                    if (response.code() == 401) {
                        clearAuthToken() // Очищаем токен и профиль
                        clearUserProfile()
                        _userProfile.postValue(null)
                    }
                    onResult?.invoke(null)
                }
            } catch (e: IOException) {
                Log.e("LoginViewModel", "Network error fetching user profile", e)
                onResult?.invoke(null)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Unexpected error fetching user profile", e)
                onResult?.invoke(null)
            }
        }
    }
    private fun saveAuthToken(token: String) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(AUTH_TOKEN_KEY, token)
            apply()
        }
        Log.i("LoginViewModel", "Auth token saved.")
    }

    fun getAuthToken(): String? {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        return sharedPreferences.getString(AUTH_TOKEN_KEY, null)
    }

    fun clearAuthToken() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(AUTH_TOKEN_KEY)
            apply()
        }
        clearUserProfile() // Также очищаем профиль пользователя при выходе
        _userProfile.postValue(null) // Обновляем LiveData
        Log.i("LoginViewModel", "Auth token and user profile cleared.")
    }
    private fun saveUserProfile(userProfile: UserProfile) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        try {
            val userProfileJson = gson.toJson(userProfile)
            with(sharedPreferences.edit()) {
                putString(USER_PROFILE_KEY, userProfileJson)
                apply()
            }
            Log.i("LoginViewModel", "User profile saved to SharedPreferences: $userProfileJson")
        } catch (e: Exception) {
            // Это может произойти, если объект UserProfile не может быть сериализован Gson,
            // хотя это маловероятно для простых data-классов.
            Log.e("LoginViewModel", "Error serializing user profile to JSON", e)
        }
    }

    private fun loadUserProfileFromPrefs() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        val userProfileJson = sharedPreferences.getString(USER_PROFILE_KEY, null)
        if (userProfileJson != null) {
            try {
                val userProfile = gson.fromJson(userProfileJson, UserProfile::class.java)
                _userProfile.postValue(userProfile) // Используем postValue, так как init может вызываться не из главного потока
                Log.i("LoginViewModel", "User profile loaded from SharedPreferences: $userProfile")
            } catch (e: JsonSyntaxException) {
                Log.e("LoginViewModel", "Error parsing UserProfile JSON from SharedPreferences. Clearing corrupted profile.", e)
                // Если данные повреждены, лучше их очистить
                clearUserProfile()
                _userProfile.postValue(null)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Unexpected error loading user profile from SharedPreferences", e)
                _userProfile.postValue(null)
            }
        } else {
            _userProfile.postValue(null) // Если профиля нет в SharedPreferences
            Log.i("LoginViewModel", "No user profile found in SharedPreferences.")
        }
    }

    private fun clearUserProfile() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(USER_PROFILE_KEY)
            apply()
        }
        // _userProfile.postValue(null) // Обновление LiveData уже происходит в clearAuthToken или при загрузке, если профиля нет
        Log.i("LoginViewModel", "User profile cleared from SharedPreferences.")
    }

    // Дополнительная публичная LiveData для удобного доступа к ID пользователя из других частей приложения
    // (если UserProfile содержит поле id)
    val currentUserIdLiveData: LiveData<Int?> = MutableLiveData<Int?>().apply {
        // Наблюдаем за изменениями в _userProfile и обновляем currentUserIdLiveData
        _userProfile.observeForever { profile ->
            this.value = profile?.id
        }
    }
    val userLiveData: LiveData<UserProfile?> get() = _userProfile
}