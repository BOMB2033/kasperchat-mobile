package com.example.kasperchat_test.signalr

import android.annotation.SuppressLint
import android.util.Log
import com.example.kasperchat_test.BuildConfig
import com.example.kasperchat_test.api.RetrofitClient
import com.example.kasperchat_test.model.Chat
import com.example.kasperchat_test.model.ChatMember
import com.example.kasperchat_test.model.Message
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

// Определяем sealed class для событий чата
sealed class ChatEvent {
    data class ChatCreated(val chat: Chat) : ChatEvent()
    data class ChatUpdated(val chat: Chat) : ChatEvent()
    data class MemberAdded(val member: ChatMember) : ChatEvent()
}

@Singleton // Убедимся, что Hilt создает только один экземпляр
class SignalRManager @Inject constructor() { // Используем @Inject для Hilt

    // HubConnection создается один раз, но не запускается сразу
    private val hubConnection: HubConnection = HubConnectionBuilder.create("http://${BuildConfig.SERVER_IP}:${BuildConfig.SERVER_PORT}/chatHub")
        // withAccessTokenProvider с Single.defer - это ПРАВИЛЬНЫЙ подход!
        // Он будет запрашивать актуальный токен при каждом подключении.
        .withAccessTokenProvider(Single.defer { Single.just(RetrofitClient.getToken() ?: "") })
        .build()

    // ... ваши StateFlow остаются без изменений ...
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> get() = _messages

    private val _chatMembers = MutableStateFlow<List<ChatMember>>(emptyList())
    val chatMembers: StateFlow<List<ChatMember>> get() = _chatMembers //TODO Реализовать или удалить

    private val _chatEvents = MutableStateFlow<ChatEvent?>(null)
    val chatEvents: StateFlow<ChatEvent?> get() = _chatEvents

    // Предоставляем Flow для новых сообщений
    val newMessageFlow: Flow<Message> = callbackFlow {
        hubConnection.on("NewMessage", { message: Message ->
            Log.d("SignalR", "New message received via Flow: ${message.content}")
            trySend(message) // Отправляем только одно новое сообщение в Flow
        }, Message::class.java)

        awaitClose {
            hubConnection.remove("NewMessage")
            Log.d("SignalR", "NewMessage handler removed.")
        }
    }

    init {
        // Регистрация обработчиков событий
        hubConnection.on("NewMessage", { message: Message ->
            _messages.value = _messages.value + message
            Log.d("SignalR", "New message received: ${message.content}")
        }, Message::class.java)

        hubConnection.on("ChatCreated", { chat: Chat ->
            _chatEvents.value = ChatEvent.ChatCreated(chat)
            Log.d("SignalR", "Chat created: ${chat.id}")
        }, Chat::class.java)

        // ... другие обработчики ...

        // Добавим логирование состояния подключения для отладки
        hubConnection.onClosed { exception ->
            Log.e("SignalR", "Connection closed", exception)
        }
    }

    @SuppressLint("CheckResult")
    fun startConnection() {
        // Проверяем, что соединение еще не установлено или не устанавливается
        if (hubConnection.connectionState == HubConnectionState.DISCONNECTED) {
            try {
                Log.d("SignalR", "Starting connection...")
                hubConnection.start().subscribe(
                    { Log.i("SignalR", "Connection started successfully.") },
                    { error -> Log.e("SignalR", "Connection failed to start.", error) }
                )
            } catch (e: Exception) {
                Log.e("SignalR", "Exception while starting connection", e)
            }
        } else {
            Log.w("SignalR", "Connection is already ${hubConnection.connectionState}. Cannot start again.")
        }
    }

    @SuppressLint("CheckResult")
    fun joinChat(chatId: String) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.invoke("JoinChatGroup", chatId)
                .subscribe(
                    { Log.d("SignalR", "Successfully invoked JoinChatGroup for: $chatId") },
                    { error -> Log.e("SignalR", "Failed to invoke JoinChatGroup", error) }
                )
        } else {
            Log.w("SignalR", "Cannot join chat. Connection is not established. State: ${hubConnection.connectionState}")
        }
    }

    fun stopConnection() {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.stop()
            Log.d("SignalR", "Connection stopped.")
        }
    }

    @SuppressLint("CheckResult")
    fun leaveChat(chatId: String) {
        if (hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.invoke("LeaveChatGroup", chatId)
                .subscribe(
                    { Log.d("SignalR", "Successfully invoked LeaveChatGroup for: $chatId") },
                    { error -> Log.e("SignalR", "Failed to invoke LeaveChatGroup", error) }
                )
        }else{
            Log.w("SignalR", "Cannot leave chat. Connection is not established. State: ${hubConnection.connectionState}")
        }
    }
}