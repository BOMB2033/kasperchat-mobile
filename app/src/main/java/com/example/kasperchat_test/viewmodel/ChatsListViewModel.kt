package com.example.kasperchat_test.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.api.ApiService
import com.example.kasperchat_test.model.CreateChatRequest
import com.example.kasperchat_test.model.DisplayableChatItem
import com.example.kasperchat_test.signalr.ChatEvent
import com.example.kasperchat_test.signalr.SignalRManager
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject
import kotlin.collections.map

@HiltViewModel
class ChatsListViewModel @Inject constructor(
    private val apiService: ApiService,
    private val signalRManager: SignalRManager,
    private val gson: Gson
) : ViewModel() {
    private val _chats = MutableLiveData<List<DisplayableChatItem>>()
    val chats: LiveData<List<DisplayableChatItem>> = _chats

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _chatCreationSuccessEvent = MutableLiveData<Boolean>()
    val chatCreationSuccessEvent: LiveData<Boolean> = _chatCreationSuccessEvent

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {
        // Подписка на события SignalR
        viewModelScope.launch {
            signalRManager.messages.collect { messages ->
                // Обновляем список чатов при новых сообщениях
                fetchChats()
            }
        }

        viewModelScope.launch {
            signalRManager.chatEvents.collect { event ->
                if (event != null) {
                    when (event) {
                        is ChatEvent.ChatCreated -> fetchChats()
                        is ChatEvent.ChatUpdated -> fetchChats()
                        is ChatEvent.MemberAdded -> fetchChats()
                    }
                }
            }
        }

        // Первоначальная загрузка чатов
        fetchChats()
    }

    fun fetchChats() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            try {
                val response = apiService.getChats()
                if (response.isSuccessful && response.body() != null) {
                    val chats = response.body()!!.map { chat ->
                        DisplayableChatItem(
                            chat = chat,
                            linkedUserProfiles = emptyList(), // Заполнить, если нужно
                            lastMessageText = null, // Заполнить на основе данных
                            lastMessageTimestamp = null,
                            unreadMessagesCount = 0
                        )
                    }
                    _chats.postValue(chats)
                    _error.postValue(null)
                } else {
                    val errorMessage = "Ошибка загрузки чатов: ${response.code()} - ${response.message()}"
                    _error.postValue(errorMessage)
                    Log.e("ChatsListViewModel", errorMessage)
                }
            } catch (e: IOException) {
                _error.postValue("Сетевая ошибка. Проверьте подключение.")
                Log.e("ChatsListViewModel", "Network error", e)
            } catch (e: Exception) {
                _error.postValue("Произошла ошибка при загрузке чатов.")
                Log.e("ChatsListViewModel", "Unexpected error", e)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun createChat(name: String, isGroup: Boolean, avatarUrl: String? = null) {
        _isLoading.postValue(true)
        _error.postValue(null)
        _chatCreationSuccessEvent.postValue(false)
        viewModelScope.launch {
            try {
                val request = CreateChatRequest(name, isGroup, avatarUrl)
                val response = apiService.createChat(request)
                if (response.isSuccessful && response.body() != null) {
                    fetchChats() // Обновляем список после создания
                    _error.postValue(null)
                } else {
                    val errorMessage = "Ошибка создания чата: ${response.code()} - ${response.message()}"
                    _error.postValue(errorMessage)
                    Log.e("ChatsListViewModel", errorMessage)
                }
            } catch (e: IOException) {
                _error.postValue("Сетевая ошибка. Проверьте подключение.")
                Log.e("ChatsListViewModel", "Network error", e)
            } catch (e: Exception) {
                _error.postValue("Произошла ошибка при создании чата.")
                Log.e("ChatsListViewModel", "Unexpected error", e)
            } finally {
                _isLoading.postValue(false)
                _chatCreationSuccessEvent.postValue(true)
            }
        }
    }
}