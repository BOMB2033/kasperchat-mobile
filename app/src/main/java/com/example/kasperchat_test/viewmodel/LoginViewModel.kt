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
import com.example.kasperchat_test.model.LoginRequest
import com.example.kasperchat_test.model.UserProfile
import com.example.kasperchat_test.signalr.SignalRManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import androidx.core.content.edit
import androidx.lifecycle.asLiveData
import com.example.kasperchat_test.repository.UserRepository

sealed class LoginResult {
    data class Success(val token: String, val userProfile: UserProfile?) : LoginResult()
    data class Error(val message: String, val code: Int? = null) : LoginResult()
    data object Loading : LoginResult()
}
@HiltViewModel
class LoginViewModel @Inject constructor(
    application: Application,
    private val apiService: ApiService,
    private val signalRManager: SignalRManager,
    private val userRepository: UserRepository, // Внедряем репозиторий
    // gson и SharedPreferences больше не нужны напрямую
    //  private val sessionManager: SessionManager // Предполагается, что у вас есть SessionManager для токена
) : AndroidViewModel(application) {
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _userProfile = MutableLiveData<UserProfile?>()
    val userProfile: LiveData<UserProfile?> = userRepository.userProfile.asLiveData()

    private var isFetchingProfile = false

    private companion object {
        const val AUTH_PREFS_NAME = "auth_prefs"
        const val AUTH_TOKEN_KEY = "auth_token"
        const val TOKEN_EXPIRY_KEY = "token_expiry"
        const val USER_PROFILE_KEY = "user_profile"
    }

    init {
        val tokenData = loadToken()
        if (tokenData.first != null && tokenData.second != null && tokenData.second!! > System.currentTimeMillis()) {
            Log.i("LoginViewModel", "Valid token found, initializing SignalR")
            signalRManager.startConnection()
            if (_userProfile.value == null && !isFetchingProfile) {
                fetchCurrentUserProfile()
            }
        } else if (tokenData.first != null) {
            Log.w("LoginViewModel", "Expired token found, clearing")
            clearAuthToken()
        }
    }

    fun loginUser(username: String, password: String) {
        _loginResult.postValue(LoginResult.Loading)
        viewModelScope.launch {
            try {
                val loginRequest = LoginRequest(username, password)
                val response = apiService.loginUser(loginRequest)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    // Сохраняем токен через SessionManager или прямо здесь
                    // sessionManager.saveToken(loginResponse.token) // Предполагаемый метод
                    RetrofitClient.setToken(loginResponse.token) // Ваш текущий быстрый способ
                    saveToken(loginResponse.token)


                    signalRManager.startConnection()

                    // Запрашиваем профиль через репозиторий
                    viewModelScope.launch {
                        val profileResult = userRepository.fetchCurrentUserProfile()
                        profileResult.onSuccess { fetchedProfile ->
                            _loginResult.postValue(LoginResult.Success(loginResponse.token, fetchedProfile))
                        }.onFailure {
                            // Профиль не загрузился, но логин успешен
                            _loginResult.postValue(LoginResult.Success(loginResponse.token, null))
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = when (response.code()) {
                        400 -> "Неверный запрос: Логин и пароль обязательны. $errorBody"
                        401 -> "Неверные учетные данные. $errorBody"
                        else -> "Ошибка входа: ${response.code()} - ${response.message()}. $errorBody"
                    }
                    Log.e("LoginViewModel", errorMessage)
                    _loginResult.postValue(LoginResult.Error(errorMessage, response.code()))
                }
            } catch (e: IOException) {
                Log.e("LoginViewModel", "Сетевая ошибка во время входа", e)
                _loginResult.postValue(LoginResult.Error("Сетевая ошибка. Проверьте подключение.", null))
            } catch (e: Exception) {
                Log.e("LoginViewModel", "Непредвиденная ошибка во время входа", e)
                _loginResult.postValue(LoginResult.Error("Произошла ошибка.", null))
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
                    userRepository.saveProfile(user)
                    _userProfile.postValue(user)
                    Log.i("LoginViewModel", "User profile fetched successfully: $user")
                    onResult?.invoke(user)
                } else {
                    Log.e("LoginViewModel", "Failed to fetch user profile: ${response.code()} - ${response.message()}")
                    if (response.code() == 401) {
                        clearAuthToken()
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
            } finally {
                isFetchingProfile = false
            }
        }
    }

    fun getAuthToken(): String? {
        val (token, expiry) = loadToken()
        return if (expiry != null && expiry > System.currentTimeMillis()) token else null
    }

    fun clearAuthToken() {
        //sessionManager.clearToken() // Предполагаемый метод
        RetrofitClient.setToken(null) // Очищаем токен
        userRepository.clearProfile() // Очищаем профиль через репозиторий
        // Удаление токена из памяти SharedPreferences
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit {
            remove(AUTH_TOKEN_KEY)
            remove(TOKEN_EXPIRY_KEY)
        }
        signalRManager.stopConnection()
        Log.i("LoginViewModel", "Auth token and user profile cleared.")
    }

    private fun loadToken(): Pair<String?, Long?> {

        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
        val expiry = if (sharedPreferences.contains(TOKEN_EXPIRY_KEY)) {
            sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
        } else null
        return Pair(token, expiry)
    }
    private fun saveToken(token: String){
        val sharedPreferences = getApplication<Application>().getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        val expiry = System.currentTimeMillis() + 3600000 // Токен действителен 1 час
        sharedPreferences.edit {
            putString(AUTH_TOKEN_KEY, token)
            putLong(TOKEN_EXPIRY_KEY, expiry)
        }
        Log.i("LoginViewModel", "Token saved with expiry: $expiry")
    }
}