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
import com.example.kasperchat_test.network.RetrofitClient
import com.example.kasperchat_test.network.SignalRClient
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import java.io.IOException

sealed class LoginResult {
    data class Success(val token: String, val userProfile: UserProfile?) : LoginResult()
    data class Error(val message: String) : LoginResult()
    data object Loading : LoginResult()
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = _userProfile

    private val apiService = RetrofitClient.instance
    private val gson = Gson()

    private var isFetchingProfile = false // Флаг для предотвращения повторных вызовов

    private companion object {
        const val AUTH_PREFS_NAME = "auth_prefs"
        const val AUTH_TOKEN_KEY = "auth_token"
        const val TOKEN_EXPIRY_KEY = "token_expiry"
        const val USER_PROFILE_KEY = "user_profile"
    }

    init {
        loadUserProfileFromPrefs()
        val tokenData = loadToken()
        if (tokenData.first != null && tokenData.second != null && tokenData.second!! > System.currentTimeMillis()) {
            Log.i("LoginViewModel", "Valid token found, initializing SignalR")
            SignalRClient.initialize(getApplication(), tokenData.first, tokenData.second)
            SignalRClient.startConnection()
            if (_userProfile.value == null && !isFetchingProfile) {
                fetchCurrentUserProfile()
            }
        } else if (tokenData.first != null) {
            Log.w("LoginViewModel", "Expired token found, clearing")
            clearAuthToken()
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
                    val expiry = loginResponse.expiresAt.time
                    saveAuthToken(token, expiry)
                    SignalRClient.updateToken(token, expiry)
                    SignalRClient.startConnection()
                    Log.i("LoginViewModel", "Login successful. Token: $token, Expiry: $expiry")

                    if (!isFetchingProfile) {
                        fetchCurrentUserProfile()
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
                Log.e("LoginViewModel", "Сетевая ошибка во время входа", e)
                _loginResult.postValue(LoginResult.Error("Сетевая ошибка. Проверьте ваше подключение."))
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Непредвиденная ошибка во время входа", e)
                _loginResult.postValue(LoginResult.Error("Произошла непредвиденная ошибка."))
            }
        }
    }

    fun fetchCurrentUserProfile(onResult: ((UserProfile?) -> Unit)? = null) {
        if (getAuthToken() == null || isFetchingProfile) {
            Log.w("LoginViewModel", "Cannot fetch profile: token is null or fetch in progress")
            onResult?.invoke(null)
            return
        }
        isFetchingProfile = true
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
                    if (response.code() == 401) {
                        clearAuthToken()
                        clearUserProfile()
                        _userProfile.postValue(null)
                        SignalRClient.stopConnection()
                    }
                    onResult?.invoke(null)
                }
            } catch (e: IOException) {
                Log.e("LoginViewModel", "Network error fetching user profile", e)
                onResult?.invoke(null)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Unexpected error fetching user profile", e)
                onResult?.invoke(null)
            } finally {
                isFetchingProfile = false
            }
        }
    }

    private fun saveAuthToken(token: String, expiry: Long) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(AUTH_TOKEN_KEY, token)
            putLong(TOKEN_EXPIRY_KEY, expiry)
            apply()
        }
        Log.i("LoginViewModel", "Auth token saved with expiry: $expiry")
    }

    fun getAuthToken(): String? {
        val (token, expiry) = loadToken()
        return if (expiry != null && expiry > System.currentTimeMillis()) token else null
    }

    fun clearAuthToken() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(AUTH_TOKEN_KEY)
            remove(TOKEN_EXPIRY_KEY)
            apply()
        }
        clearUserProfile()
        _userProfile.postValue(null)
        SignalRClient.stopConnection()
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
            Log.e("LoginViewModel", "Error serializing user profile to JSON", e)
        }
    }

    private fun loadUserProfileFromPrefs() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        val userProfileJson = sharedPreferences.getString(USER_PROFILE_KEY, null)
        if (userProfileJson != null) {
            try {
                val userProfile = gson.fromJson(userProfileJson, UserProfile::class.java)
                _userProfile.postValue(userProfile)
                Log.i("LoginViewModel", "User profile loaded from SharedPreferences: $userProfile")
            } catch (e: JsonSyntaxException) {
                Log.e("LoginViewModel", "Error parsing UserProfile JSON from SharedPreferences. Clearing corrupted profile.", e)
                clearUserProfile()
                _userProfile.postValue(null)
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Unexpected error loading user profile from SharedPreferences", e)
                _userProfile.postValue(null)
            }
        } else {
            _userProfile.postValue(null)
            Log.i("LoginViewModel", "No user profile found in SharedPreferences.")
        }
    }

    private fun loadToken(): Pair<String?, Long?> {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
        val expiry = if (sharedPreferences.contains(TOKEN_EXPIRY_KEY)) {
            sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
        } else null
        return Pair(token, expiry)
    }

    private fun clearUserProfile() {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(USER_PROFILE_KEY)
            apply()
        }
        Log.i("LoginViewModel", "User profile cleared from SharedPreferences.")
    }

    val currentUserIdLiveData: LiveData<Int?> = MutableLiveData<Int?>().apply {
        _userProfile.observeForever { profile ->
            this.value = profile?.id
        }
    }

    val userLiveData: LiveData<UserProfile?> get() = _userProfile
}