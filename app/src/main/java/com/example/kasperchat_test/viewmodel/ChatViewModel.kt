package com.example.kasperchat_test.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.api.ApiService
import com.example.kasperchat_test.model.Chat
import com.example.kasperchat_test.model.ChatMember
import com.example.kasperchat_test.model.CreateMessageRequest
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.model.UserProfile
import com.example.kasperchat_test.repository.UserRepository
import com.example.kasperchat_test.signalr.SignalRManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val apiService: ApiService,
    private val signalRManager: SignalRManager,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _chat = MutableLiveData<Chat?>()
    val chat: LiveData<Chat?> = _chat

    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> = _messages

    private val _chatMembers = MutableLiveData<List<UserProfile>>()
    val chatMembers: LiveData<List<UserProfile>> = _chatMembers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        // Подписываемся на новые сообщения ОДИН РАЗ при создании ViewModel
        viewModelScope.launch {
            signalRManager.newMessageFlow.collect { newMessage ->
                val currentChatId = _chat.value?.id
                if (newMessage.chatId.toString() == currentChatId) {
                    val currentList = _messages.value ?: emptyList()
                    if (!currentList.any { it.id == newMessage.id }) {
                        _messages.postValue(currentList + newMessage)

                        markMessagesAsRead(newMessage.chatId)
                    }
                }
            }
        }
    }

    fun joinChat(chatId: String) {
        // Устанавливаем ID текущего чата
        // Это нужно, чтобы `collect` в `init` знал, какие сообщения добавлять
        fetchChatData(chatId) // fetchChatData установит _chat.value

        // Подключаемся к группе SignalR
        signalRManager.joinChat(chatId)

        // Загружаем начальную историю сообщений
        fetchMessages(chatId)

        fetchChatMembers(chatId) // Это можно оставить как есть
    }

    fun fetchChatData(chatId: String) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val response = apiService.getChatById(chatId)
                if (response.isSuccessful && response.body() != null) {
                    _chat.postValue(response.body())
                    _error.postValue(null)
                } else {
                    _error.postValue("Ошибка загрузки данных чата: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Ошибка загрузки данных чата: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchMessages(chatId: String, offset: Int = 0, limit: Int = 50) {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val response = apiService.getMessagesByChatId(chatId, offset, limit)
                if (response.isSuccessful && response.body() != null) {
                    _messages.postValue(response.body()!!)
                    markMessagesAsRead(chatId)
                    _error.postValue(null)
                } else {
                    _error.postValue("Ошибка загрузки сообщений: ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Ошибка загрузки сообщений: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun fetchChatMembers(chatId: String) {
        viewModelScope.launch {
            try {
                // Используем ту же эффективную стратегию, что и в ChatSettingsViewModel
                val usersResponse = apiService.getUsers()
                val membersResponse = apiService.getChatMembers(chatId)

                if (usersResponse.isSuccessful && membersResponse.isSuccessful) {
                    val allUsers = usersResponse.body() ?: emptyList()
                    val memberIds = membersResponse.body()?.map { it.userId }?.toSet() ?: emptySet()

                    val chatMembersProfiles = allUsers.filter { user ->
                        memberIds.contains(user.id)
                    }
                    _chatMembers.postValue(chatMembersProfiles) // Отправляем список профилей
                } else {
                    Log.e("ChatViewModel", "Error fetching members: membersResponse code ${membersResponse.code()}, usersResponse code ${usersResponse.code()}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error fetching members: ${e.message}")
            }
        }
    }
    private fun markMessagesAsRead(chatId: String) {
        viewModelScope.launch {
            try {
                // Просто отправляем запрос, сервер сам разберется
                val response = apiService.markMessagesAsRead(chatId)
                if (response.isSuccessful) {
                    Log.d("ChatViewModel", "Messages in chat $chatId marked as read.")
                    // Локально обновляем статус isRead для всех входящих сообщений,
                    // чтобы UI обновился мгновенно, не дожидаясь следующей загрузки с сервера.
                    updateIncomingMessagesAsReadLocally()
                } else {
                    Log.e("ChatViewModel", "Failed to mark messages as read. Code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error marking messages as read", e)
            }
        }
    }
    private fun updateIncomingMessagesAsReadLocally() {
        // 1. Получаем ID текущего пользователя из репозитория
        val currentUserId = userRepository.userProfile.value?.id
        if (currentUserId == null) {
            Log.w("ChatViewModel", "Cannot update read status locally: currentUserId is null.")
            return
        }

        val currentMessages = _messages.value
        if (currentMessages.isNullOrEmpty()) return

        // 2. Обновляем только входящие (чужие) сообщения, которые еще не помечены как прочитанные
        val updatedMessages = currentMessages.map { message ->
            // Обновляем, только если это чужое сообщение и оно еще не прочитано
            if (message.authorId != currentUserId && !message.isRead) {
                message.copy(isRead = true)
            } else {
                message // Возвращаем без изменений
            }
        }

        // 3. Отправляем обновленный список в LiveData, только если были изменения
        if (currentMessages != updatedMessages) {
            _messages.postValue(updatedMessages)
            Log.d("ChatViewModel", "Updated incoming messages' read status locally.")
        }
    }
    fun sendMessage(chatId: String, content: String, messageType: String = "Text") {
        viewModelScope.launch {
            try {
                val request = CreateMessageRequest(content, messageType)
                val response = apiService.sendMessageToChat(chatId, request)
                if (response.isSuccessful) {
                    // УСПЕХ! Ничего не делаем. Ждем сообщения от SignalR.
                    _error.postValue(null)
                } else {
                    _error.postValue("Ошибка отправки сообщения: ${response.code()} ${response.message()}")
                }
            } catch (e: Exception) {
                _error.postValue("Ошибка отправки сообщения: ${e.message}")
            }
        }
    }

    fun leaveChat(chatId: String) {
        // Здесь не нужно останавливать соединение, если пользователь может вернуться
        // в другой чат. Лучше делать leaveChatGroup.
         signalRManager.leaveChat(chatId) // Вам нужно будет добавить этот метод в SignalRManager
        // stopConnection() лучше вызывать при логауте.
    }

    override fun onCleared() {
        // Можно отписаться от группы при уничтожении ViewModel
        // _chat.value?.id?.let { signalRManager.leaveChat(it) }
        super.onCleared()
    }
}