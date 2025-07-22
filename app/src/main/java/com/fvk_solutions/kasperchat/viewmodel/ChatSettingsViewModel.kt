package com.fvk_solutions.kasperchat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fvk_solutions.kasperchat.api.ApiService
import com.fvk_solutions.kasperchat.model.Chat
import com.fvk_solutions.kasperchat.model.UpdateChatRequest
import com.fvk_solutions.kasperchat.model.UserProfile
import com.fvk_solutions.kasperchat.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

// Sealed class для управления всеми результатами на этом экране
sealed class ChatSettingsResult {
    data class Success(val message: String) : ChatSettingsResult()
    data class Error(val message: String) : ChatSettingsResult()
    data object Loading : ChatSettingsResult()
    data object Idle : ChatSettingsResult()
    data object NavigationBack : ChatSettingsResult() // Для удаления или выхода
}


/**
 * ViewModel для экрана настроек чата.
 * Управляет загрузкой данных чата и их обновлением.
 */
@HiltViewModel
class ChatSettingsViewModel @Inject constructor(
    private val apiService: ApiService,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _chat = MutableLiveData<Chat?>()
    val chat: LiveData<Chat?> = _chat

    private val _error = MutableLiveData<String?>() // Оставляем для простых уведомлений
    val error: LiveData<String?> = _error

    // Новая LiveData для управления состоянием UI
    private val _settingsResult = MutableLiveData<ChatSettingsResult>(ChatSettingsResult.Idle)
    val settingsResult: LiveData<ChatSettingsResult> = _settingsResult

    // LiveData для поиска и участников (остаются без изменений)
    private val _searchResults = MutableLiveData<List<UserProfile>>()
    val searchResults: LiveData<List<UserProfile>> = _searchResults

    private val _members = MutableLiveData<List<UserProfile>>()
    val members: LiveData<List<UserProfile>> = _members

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading


    /**
     * Загружает детальную информацию о чате с сервера.
     * @param chatId ID чата для загрузки.
     */
    fun fetchChatDetails(chatId: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getChatById(chatId)
                if (response.isSuccessful && response.body() != null) {
                    _chat.postValue(response.body())
                } else {
                    _error.postValue("Ошибка загрузки данных чата: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Сетевая ошибка: ${e.message}")
            }
        }
    }

    /**
     * Обновляет данные чата на сервере.
     * @param chatId ID чата для обновления.
     * @param newName Новое название чата.
     * @param newAvatarUrl Новый URL аватара (может быть null).
     */
    fun updateChat(chatId: String, newName: String) {
        val currentChat = _chat.value ?: run {
            _settingsResult.value = ChatSettingsResult.Error("Данные чата не загружены.")
            return
        }

        _settingsResult.value = ChatSettingsResult.Loading

        val request = UpdateChatRequest(
            name = newName,
            isGroup = currentChat.isGroup,
            avatarUrl = currentChat.avatarUrl, // Сохраняем текущий аватар
            backgroundUrl = currentChat.backgroundUrl,
            bubbleColor = currentChat.bubbleColor
        )

        viewModelScope.launch {
            try {
                val response = apiService.updateChat(chatId, request)
                if (response.isSuccessful) {
                    // Обновляем локальные данные и сообщаем об успехе
                    fetchChatDetails(chatId) // Перезагружаем для свежести
                    _settingsResult.postValue(ChatSettingsResult.Success("Название чата обновлено"))
                } else {
                    _settingsResult.postValue(ChatSettingsResult.Error("Ошибка сохранения: ${response.code()}"))
                }
            } catch (e: Exception) {
                _settingsResult.postValue(ChatSettingsResult.Error("Сетевая ошибка: ${e.message}"))
            }
        }
    }

    fun uploadAndSaveChatAvatar(chatId: String, file: File) {
        _settingsResult.value = ChatSettingsResult.Loading
        viewModelScope.launch {
            // Шаг 1: Загрузить файл
            userRepository.uploadAvatar(file).onSuccess { newAvatarUrl ->
                // Шаг 2: Обновить чат с новым URL
                val currentChat = _chat.value ?: run {
                    _settingsResult.postValue(ChatSettingsResult.Error("Данные чата не загружены."))
                    return@onSuccess
                }
                val request = UpdateChatRequest(
                    name = currentChat.name,
                    isGroup = currentChat.isGroup,
                    avatarUrl = newAvatarUrl, // Используем новый URL
                    backgroundUrl = currentChat.backgroundUrl,
                    bubbleColor = currentChat.bubbleColor
                )

                try {
                    val response = apiService.updateChat(chatId, request)
                    if (response.isSuccessful) {
                        fetchChatDetails(chatId) // Обновляем данные чата
                        _settingsResult.postValue(ChatSettingsResult.Success("Аватар чата обновлен"))
                    } else {
                        _settingsResult.postValue(ChatSettingsResult.Error("Ошибка обновления аватара: ${response.code()}"))
                    }
                } catch (e: Exception) {
                    _settingsResult.postValue(ChatSettingsResult.Error("Сетевая ошибка: ${e.message}"))
                }

            }.onFailure { exception ->
                _settingsResult.postValue(ChatSettingsResult.Error("Ошибка загрузки файла: ${exception.message}"))
            }
        }
    }

    fun onResultHandled() {
        _settingsResult.value = ChatSettingsResult.Idle
    }


    /**
     * Загружает участников чата.
     * Стратегия:
     * 1. Загружаем полный список пользователей (из общих чатов).
     * 2. Загружаем список ID участников текущего чата.
     * 3. Фильтруем полный список пользователей, оставляя только участников.
     */
    fun fetchChatMembers(chatId: String) {
        viewModelScope.launch {
            try {
                // Шаг 1: Загружаем всех доступных пользователей
                val usersResponse = apiService.getUsers()
                // Шаг 2: Загружаем ID участников
                val membersResponse = apiService.getChatMembers(chatId)

                if (usersResponse.isSuccessful && membersResponse.isSuccessful) {
                    val allUsers = usersResponse.body() ?: emptyList()
                    val memberIds = membersResponse.body()?.map { it.userId }?.toSet() ?: emptySet()

                    // Шаг 3: Фильтруем
                    val chatMembersProfiles = allUsers.filter { user ->
                        memberIds.contains(user.id)
                    }
                    _members.postValue(chatMembersProfiles)
                } else {
                    _error.postValue("Ошибка загрузки участников: ${membersResponse.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Сетевая ошибка при загрузке участников: ${e.message}")
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

    /**
     * Удаляет пользователя из чата.
     * @param chatId ID чата.
     * @param userToRemove Пользователь для удаления.
     */
    fun removeUserFromChat(chatId: String, userToRemove: UserProfile) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Вам понадобится эндпоинт для удаления. Предположим, он такой:
                // DELETE /api/chats/{chatId}/members/{userId}
                val response = apiService.removeChatMember(chatId, userToRemove.id) // <--- НУЖНО ДОБАВИТЬ В ApiService
                if (response.isSuccessful) {
                    _error.postValue("${userToRemove.fullName} удален из чата")
                    fetchChatMembers(chatId) // Обновляем список
                } else {
                    _error.postValue("Ошибка удаления: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Сетевая ошибка: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun deleteChat(chatId: String) {
        _settingsResult.value = ChatSettingsResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.deleteChat(chatId)
                if (response.isSuccessful) {
                    _settingsResult.postValue(ChatSettingsResult.NavigationBack)
                } else {
                    _settingsResult.postValue(ChatSettingsResult.Error("Ошибка удаления: ${response.code()}"))
                }
            }catch (e: Exception) {
                _settingsResult.postValue(ChatSettingsResult.Error("Сетевая ошибка: ${e.message}"))
            }
        }
    }

    /**
     * Позволяет текущему пользователю выйти из чата.
     * @param chatId ID чата.
     * @param userId ID пользователя, который выходит.
     */
    fun leaveChat(chatId: String, userId: String) {
        _settingsResult.value = ChatSettingsResult.Loading
        viewModelScope.launch {
            try {
                val response = apiService.removeChatMember(chatId, userId)
                if (response.isSuccessful) {
                    _settingsResult.postValue(ChatSettingsResult.NavigationBack)
                } else {
                    _settingsResult.postValue(ChatSettingsResult.Error("Не удалось покинуть чат: ${response.code()}"))
                }
            } catch (e: Exception) {
                _settingsResult.postValue(ChatSettingsResult.Error("Сетевая ошибка: ${e.message}"))
            }
        }
    }
}