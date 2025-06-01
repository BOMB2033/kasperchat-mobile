package com.example.kasperchat_test.viewmodel // Создайте этот пакет, если его нет

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.model.Chat
import com.example.kasperchat_test.model.Link
import com.example.kasperchat_test.model.UserProfile
import com.example.kasperchat_test.network.RetrofitClient
import com.google.gson.JsonSyntaxException // Импорт для отлова ошибки парсинга JSON
import kotlinx.coroutines.launch
import java.io.IOException

// Запечатанный класс для представления результата загрузки чатов
sealed class ChatsResult {
    data class Success(val chats: List<Chat>) : ChatsResult() // Используем модель Chats
    data class Error(val message: String) : ChatsResult()
    data object Loading : ChatsResult()
}

sealed class UsersProfileResult {
    data class Success(val userProfiles: List<UserProfile>) : UsersProfileResult()
    data class Error(val message: String) : UsersProfileResult()
    data object Loading : UsersProfileResult()
}

sealed class LinksResult {
    data class Success(val links: List<Link>) : LinksResult()
    data class Error(val message: String) : LinksResult()
    data object Loading : LinksResult()
}

class ChatsViewModel(application: Application) : AndroidViewModel(application) {

    private val _chatsResult = MutableLiveData<ChatsResult>()
    val chatsResult: LiveData<ChatsResult> = _chatsResult

    private val _usersProfileResult = MutableLiveData<UsersProfileResult>()
    val usersProfileResult: LiveData<UsersProfileResult> = _usersProfileResult

    private val _linksResult = MutableLiveData<LinksResult>()
    val linksResult: LiveData<LinksResult> = _linksResult

    private val apiService = RetrofitClient.instance
    private val loginViewModel = LoginViewModel(application) // Для получения токена

    fun fetchChats() {
        _chatsResult.postValue(ChatsResult.Loading) // Устанавливаем состояние загрузки
        viewModelScope.launch {
            val token = loginViewModel.getAuthToken() // Получаем сохраненный токен
            if (token == null) {
                _chatsResult.postValue(ChatsResult.Error("Пользователь не аутентифицирован"))
                Log.e("ChatsViewModel", "Токен отсутствует. Невозможно загрузить чаты.")
                return@launch
            }

            try {
                val response = apiService.getChats()

                if (response.isSuccessful && response.body() != null) {
                    _chatsResult.postValue(ChatsResult.Success(response.body()!!))
                    Log.i("ChatsViewModel", "Чаты успешно загружены: ${response.body()}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Не удалось загрузить чаты: ${response.code()} - ${response.message()}. Тело ошибки: $errorBody"
                    Log.e("ChatsViewModel", errorMessage)
                    if (response.code() == 401) {
                        errorMessage = "Не авторизован. Пожалуйста, войдите снова."
                        _chatsResult.postValue(ChatsResult.Error(errorMessage))
                        loginViewModel.clearAuthToken()
                    } else {
                        _chatsResult.postValue(ChatsResult.Error(errorMessage))
                    }
                }

            } catch (e: IOException) {
                Log.e("ChatsViewModel", "Сетевая ошибка при загрузке чатов", e)
                _chatsResult.postValue(ChatsResult.Error("Сетевая ошибка. Проверьте ваше подключение."))
            } catch (e: JsonSyntaxException) { // Отдельно ловим ошибку парсинга JSON
                Log.e("ChatsViewModel", "Ошибка парсинга JSON при загрузке чатов", e)
                _chatsResult.postValue(ChatsResult.Error("Ошибка обработки данных от сервера."))
            }
            catch (e: Exception) {
                Log.e("ChatsViewModel", "Непредвиденная ошибка при загрузке чатов", e)
                _chatsResult.postValue(ChatsResult.Error("Произошла непредвиденная ошибка."))
            }
        }
    }

    fun fetchUserProfiles() {
        _usersProfileResult.postValue(UsersProfileResult.Loading) // Устанавливаем состояние загрузки
        viewModelScope
        viewModelScope.launch {
            val token = loginViewModel.getAuthToken() // Получаем сохраненный токен
            if (token == null) {
                _usersProfileResult.postValue(UsersProfileResult.Error("Пользователь не аутентифицирован"))
                Log.e("ChatsViewModel", "Токен отсутствует. Невозможно загрузить пользователей.")
                return@launch
            }
            try {
                val response = apiService.getUsers()

                if (response.isSuccessful && response.body() != null) {
                    _usersProfileResult.postValue(UsersProfileResult.Success(response.body()!!))
                    Log.i("ChatsViewModel", "Пользователи успешно загружены: ${response.body()}")
                }else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Не удалось загрузить пользователей: ${response.code()} - ${response.message()}. Тело ошибки: $errorBody"
                    Log.e("ChatsViewModel", errorMessage)
                    if (response.code() == 401) {
                        errorMessage = "Не авторизован. Пожалуйста, войдите снова."
                        _usersProfileResult.postValue(UsersProfileResult.Error(errorMessage))
                        loginViewModel.clearAuthToken()
                    } else {
                        _usersProfileResult.postValue(UsersProfileResult.Error(errorMessage))
                    }
                }

            } catch (e: IOException) {
                Log.e("ChatsViewModel", "Сетевая ошибка при загрузке пользователей", e)
                _usersProfileResult.postValue(UsersProfileResult.Error("Сетевая ошибка. Проверьте ваше подключение."))
            } catch (e: JsonSyntaxException) { // Отдельно ловим ошибку парсинга JSON
                Log.e("ChatsViewModel", "Ошибка парсинга JSON при загрузке пользователей", e)
                _usersProfileResult.postValue(UsersProfileResult.Error("Ошибка обработки данных от сервера."))
            }
            catch (e: Exception) {
                Log.e("ChatsViewModel", "Непредвиденная ошибка при загрузке пользователей", e)
                _usersProfileResult.postValue(UsersProfileResult.Error("Произошла непредвиденная ошибка."))
            }
        }
    }

    fun fetchLinks() {
        _linksResult.postValue(LinksResult.Loading) // Устанавливаем состояние загрузки
        viewModelScope
        viewModelScope.launch {
            val token = loginViewModel.getAuthToken() // Получаем сохраненный токен
            if (token == null) {
                _linksResult.postValue(LinksResult.Error("Пользователь не аутентифицирован"))
                Log.e("ChatsViewModel", "Токен отсутствует. Невозможно загрузить связи.")
                return@launch
            }
            try {
                val response = apiService.getLinks()

                if (response.isSuccessful && response.body() != null) {
                    _linksResult.postValue(LinksResult.Success(response.body()!!))
                    Log.i("ChatsViewModel", "Связи успешно загружены: ${response.body()}")
                }else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Не удалось загрузить связи: ${response.code()} - ${response.message()}. Тело ошибки: $errorBody"
                    Log.e("ChatsViewModel", errorMessage)
                    if (response.code() == 401) {
                        errorMessage = "Не авторизован. Пожалуйста, войдите снова."
                        _linksResult.postValue(LinksResult.Error(errorMessage))
                        loginViewModel.clearAuthToken()
                    } else {
                        _linksResult.postValue(LinksResult.Error(errorMessage))
                    }
                }

            } catch (e: IOException) {
                Log.e("ChatsViewModel", "Сетевая ошибка при загрузке пользователей", e)
                _linksResult.postValue(LinksResult.Error("Сетевая ошибка. Проверьте ваше подключение."))
            } catch (e: JsonSyntaxException) { // Отдельно ловим ошибку парсинга JSON
                Log.e("ChatsViewModel", "Ошибка парсинга JSON при загрузке пользователей", e)
                _linksResult.postValue(LinksResult.Error("Ошибка обработки данных от сервера."))
            }
            catch (e: Exception) {
                Log.e("ChatsViewModel", "Непредвиденная ошибка при загрузке пользователей", e)
                _linksResult.postValue(LinksResult.Error("Произошла непредвиденная ошибка."))
            }
        }
    }
}