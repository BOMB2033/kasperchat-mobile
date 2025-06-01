package com.example.kasperchat_test.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.model.ChatData
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.network.RetrofitClient
import kotlinx.coroutines.launch
import java.io.IOException

sealed class MessagesResult {
    data class Success(val messages: List<Message>) : MessagesResult()
    data class Error(val message: String) : MessagesResult()
    data object Loading : MessagesResult()
}

sealed class ChatDataResult {
    data class Success(val chat: ChatData) : ChatDataResult()
    data class Error(val message: String) : ChatDataResult()
    data object Loading : ChatDataResult()
}

class MessageViewModel : ViewModel() {

    private val apiService = RetrofitClient.instance
    private val messagesLiveData = mutableMapOf<Int, MutableLiveData<MessagesResult>>()
    private val chatDataLiveData = mutableMapOf<Int, MutableLiveData<ChatDataResult>>()

    fun getMessagesForChat(chatId: Int): LiveData<MessagesResult> {
        return messagesLiveData.getOrPut(chatId) { MutableLiveData() }
    }

    fun getChatData(chatId: Int): LiveData<ChatDataResult> {
        return chatDataLiveData.getOrPut(chatId) { MutableLiveData() }
    }

    fun fetchMessages(chatId: Int) {
        val liveData = messagesLiveData.getOrPut(chatId) { MutableLiveData() }
        liveData.postValue(MessagesResult.Loading)
        viewModelScope.launch {
            try {
                val response = apiService.getMessagesByChatId(chatId)
                if (response.isSuccessful && response.body() != null) {
                    val messages = response.body()!!
                    liveData.postValue(MessagesResult.Success(messages))
                    Log.i("MessageViewModel", "Fetched ${messages.size} messages for chatId $chatId")
                } else {
                    val errorMessage = "Error: ${response.code()} - ${response.message()}"
                    liveData.postValue(MessagesResult.Error(errorMessage))
                    Log.e("MessageViewModel", errorMessage)
                }
            } catch (e: IOException) {
                val errorMessage = "Network error fetching messages"
                liveData.postValue(MessagesResult.Error(errorMessage))
                Log.e("MessageViewModel", errorMessage, e)
            } catch (e: Exception) {
                val errorMessage = "Unexpected error fetching messages"
                liveData.postValue(MessagesResult.Error(errorMessage))
                Log.e("MessageViewModel", errorMessage, e)
            }
        }
    }

    fun fetchChatData(chatId: Int) {
        val liveData = chatDataLiveData.getOrPut(chatId) { MutableLiveData() }
        liveData.postValue(ChatDataResult.Loading)
        viewModelScope.launch {
            try {
                val response = apiService.getChatById(chatId)
                if (response.isSuccessful && response.body() != null) {
                    val chat = response.body()!!
                    liveData.postValue(ChatDataResult.Success(chat))
                    Log.i("MessageViewModel", "Fetched chat data for chatId $chatId: ${chat.name}")
                } else {
                    val errorMessage = "Error: ${response.code()} - ${response.message()}"
                    liveData.postValue(ChatDataResult.Error(errorMessage))
                    Log.e("MessageViewModel", errorMessage)
                }
            } catch (e: IOException) {
                val errorMessage = "Network error fetching chat data"
                liveData.postValue(ChatDataResult.Error(errorMessage))
                Log.e("MessageViewModel", errorMessage, e)
            } catch (e: Exception) {
                val errorMessage = "Unexpected error fetching chat data"
                liveData.postValue(ChatDataResult.Error(errorMessage))
                Log.e("MessageViewModel", errorMessage, e)
            }
        }
    }
}