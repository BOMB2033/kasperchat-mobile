package com.example.kasperchat_test.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.api.ApiService
import com.example.kasperchat_test.model.Chat
import com.example.kasperchat_test.model.UpdateChatRequest
import com.example.kasperchat_test.model.UserProfile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel для экрана настроек чата.
 * Управляет загрузкой данных чата и их обновлением.
 */
@HiltViewModel
class ChatSettingsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    // --- LiveData для состояния UI ---

    // Хранит текущие данные о чате.
    private val _chat = MutableLiveData<Chat?>()
    val chat: LiveData<Chat?> = _chat

    // Управляет видимостью ProgressBar.
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Хранит текст ошибки для отображения в Toast/Snackbar.
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Событие, сигнализирующее об успешном обновлении.
    // Фрагмент должен наблюдать за ним, чтобы выполнить навигацию назад.
    private val _updateSuccessEvent = MutableLiveData<Event<Unit>>()
    val updateSuccessEvent: LiveData<Event<Unit>> = _updateSuccessEvent

    // Новые LiveData для поиска и участников
    private val _searchResults = MutableLiveData<List<UserProfile>>()
    val searchResults: LiveData<List<UserProfile>> = _searchResults

    private val _members = MutableLiveData<List<UserProfile>>()
    val members: LiveData<List<UserProfile>> = _members

    /**
     * Загружает детальную информацию о чате с сервера.
     * @param chatId ID чата для загрузки.
     */
    fun fetchChatDetails(chatId: String) {
        // Показываем индикатор загрузки
        _isLoading.value = true

        viewModelScope.launch {
            try {
                val response = apiService.getChatById(chatId)
                if (response.isSuccessful && response.body() != null) {
                    // Успешно загрузили, обновляем LiveData
                    _chat.postValue(response.body())
                } else {
                    // Сервер вернул ошибку
                    _error.postValue("Ошибка загрузки данных чата: ${response.code()}")
                }
            } catch (e: Exception) {
                // Ошибка сети или другая непредвиденная проблема
                _error.postValue("Сетевая ошибка: ${e.message}")
            } finally {
                // В любом случае убираем индикатор загрузки
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Обновляет данные чата на сервере.
     * @param chatId ID чата для обновления.
     * @param newName Новое название чата.
     * @param newAvatarUrl Новый URL аватара (может быть null).
     */
    fun updateChat(chatId: String, newName: String, newAvatarUrl: String?) {
        // Получаем текущее состояние чата, чтобы не потерять другие поля
        val currentChat = _chat.value
        if (currentChat == null) {
            _error.value = "Данные чата еще не загружены. Попробуйте снова."
            return
        }

        _isLoading.value = true

        // Создаем DTO для запроса на обновление
        val request = UpdateChatRequest(
            name = newName,
            isGroup = currentChat.isGroup, // Отправляем текущее значение, т.к. не меняем его
            avatarUrl = newAvatarUrl,
            backgroundUrl = currentChat.backgroundUrl, // Сохраняем старые значения
            bubbleColor = currentChat.bubbleColor
        )

        viewModelScope.launch {
            try {
                val response = apiService.updateChat(chatId, request)
                if (response.isSuccessful) {
                    // Успех! Отправляем событие для навигации
                    _updateSuccessEvent.postValue(Event(Unit))
                } else {
                    // Ошибка сервера
                    _error.postValue("Ошибка сохранения: ${response.code()}")
                }
            } catch (e: Exception) {
                // Ошибка сети
                _error.postValue("Сетевая ошибка: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchChatMembers(chatId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getChatMembers(chatId) // Предполагаем, что этот метод возвращает List<ChatMember>
                if(response.isSuccessful && response.body() != null) {
                    // TODO: Вам нужно будет получить полные профили пользователей по их ID,
                    // либо изменить API, чтобы оно сразу возвращало UserProfile.
                    // Пока оставим пустым для простоты.
                }
            } catch (e: Exception) {
                _error.postValue("Ошибка загрузки участников: ${e.message}")
            }
        }
    }

    fun searchUsers(query: String, chatId: String) {
        if (query.length < 2) {
            _searchResults.postValue(emptyList())
            return
        }

        viewModelScope.launch {
            try {
                val response = apiService.searchUsers(query, chatId)
                if (response.isSuccessful) {
                    _searchResults.postValue(response.body() ?: emptyList())
                }
            } catch (e: Exception) {
                // Можно не показывать ошибку, а просто оставить список пустым
            }
        }
    }

    fun addUserToChat(chatId: String, userToAdd: UserProfile) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.addChatMember(chatId, userToAdd.id)
                if (response.isSuccessful) {
                    // Успешно добавили. Теперь нужно обновить список участников и очистить поиск
                    _error.postValue(userToAdd.fullName + " добавлен в чат") // Используем error для уведомления
                    _searchResults.postValue(emptyList()) // Очищаем результаты поиска
                    fetchChatMembers(chatId) // Обновляем список участников
                } else {
                    _error.postValue("Ошибка добавления пользователя: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Сетевая ошибка: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

}

/**
 * Класс-обертка для событий, которые должны обрабатываться только один раз,
 * например, навигация или показ Toast/Snackbar.
 */
open class Event<out T>(private val content: T) {
    var hasBeenHandled = false
        private set

    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    fun peekContent(): T = content
}