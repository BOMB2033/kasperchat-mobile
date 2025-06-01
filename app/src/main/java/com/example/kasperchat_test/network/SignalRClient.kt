package com.example.kasperchat_test.network

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.example.kasperchat_test.model.Message
import io.reactivex.rxjava3.core.Single
import java.util.Date

object SignalRClient {
    private lateinit var hubConnection: HubConnection
    private const val HUB_URL = "http://185.130.224.155:5012/chathub"
    private lateinit var appContext: Context
    private var currentToken: String? = null

    private const val AUTH_PREFS_NAME = "auth_prefs"
    private const val AUTH_TOKEN_KEY = "auth_token"
    private const val TOKEN_EXPIRY_KEY = "token_expiry"

    fun initialize(context: Context, token: String? = null, expiry: Long? = null) {
        appContext = context.applicationContext
        currentToken = token

        if (token != null && expiry != null) {
            saveToken(token, expiry)
        }

        if (currentToken == null) {
            val savedTokenData = loadToken()
            currentToken = savedTokenData.first
            if (savedTokenData.second != null && savedTokenData.second!! < System.currentTimeMillis()) {
                Log.w("SignalRClient", "Saved token is expired")
                currentToken = null
                clearToken()
            }
        }

        hubConnection = HubConnectionBuilder
            .create(HUB_URL)
            .withAccessTokenProvider(currentToken?.let { Single.just(it) })
            .build()

        hubConnection.on("UpdateMessages", { chatId: String ->
            Log.d("SignalRClient", "Received update notification for chatId: $chatId")
            onMessagesUpdate?.invoke(chatId.toIntOrNull() ?: 0)
        }, String::class.java)

        hubConnection.onClosed { exception ->
            if (exception != null) {
                Log.e("SignalRClient", "Connection closed with error: ${exception.message}")
            } else {
                Log.d("SignalRClient", "Connection closed normally")
            }
            startConnection()
        }
    }

    private var onMessagesUpdate: ((Int) -> Unit)? = null

    fun setOnMessagesUpdateListener(listener: (Int) -> Unit) {
        onMessagesUpdate = listener
    }

    @SuppressLint("CheckResult")
    fun updateToken(token: String, expiry: Long) {
        currentToken = token
        saveToken(token, expiry)
        if (::hubConnection.isInitialized) {
            hubConnection.stop().subscribe({
                hubConnection = HubConnectionBuilder
                    .create(HUB_URL)
                    .withAccessTokenProvider(Single.just(token))
                    .build()
                startConnection()
            }, { error ->
                Log.e("SignalRClient", "Error stopping hub for token update: ${error.message}")
            })
        }
    }

    @SuppressLint("CheckResult")
    fun startConnection() {
        if (!::hubConnection.isInitialized || hubConnection.connectionState == HubConnectionState.DISCONNECTED) {
            if (currentToken == null) {
                Log.w("SignalRClient", "No valid token available, cannot start connection")
                return
            }
            hubConnection.start().subscribe({
                Log.d("SignalRClient", "Connected to SignalR hub")
            }, { error ->
                Log.e("SignalRClient", "Error connecting to SignalR hub: ${error.message}")
                Thread.sleep(5000)
                startConnection()
            })
        }
    }

    @SuppressLint("CheckResult")
    fun stopConnection() {
        if (::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.stop().subscribe({
                Log.d("SignalRClient", "Disconnected from SignalR hub")
            }, { error ->
                Log.e("SignalRClient", "Error disconnecting from SignalR hub: ${error.message}")
            })
        }
    }

    fun isConnected(): Boolean {
        return ::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED
    }

    @SuppressLint("CheckResult")
    fun joinChatGroup(chatId: String) {
        if (::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.invoke("AddToGroup", chatId).subscribe({
                Log.d("SignalRClient", "Joined group: $chatId")
            }, { error ->
                Log.e("SignalRClient", "Error joining group $chatId: ${error.message}")
            })
        } else {
            Log.w("SignalRClient", "Cannot join group $chatId: SignalR not connected")
        }
    }

    @SuppressLint("CheckResult")
    fun leaveChatGroup(chatId: String) {
        if (::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.invoke("RemoveFromGroup", chatId).subscribe({
                Log.d("SignalRClient", "Left group: $chatId")
            }, { error ->
                Log.e("SignalRClient", "Error leaving group $chatId: ${error.message}")
            })
        }
    }

    @SuppressLint("CheckResult")
    fun sendMessage(chatId: String, userId: String, message: String) {
        if (::hubConnection.isInitialized && hubConnection.connectionState == HubConnectionState.CONNECTED) {
            hubConnection.invoke("SendMessage", chatId, userId, message).subscribe({
                Log.d("SignalRClient", "Message sent to group $chatId")
            }, { error ->
                Log.e("SignalRClient", "Error sending message to $chatId: ${error.message}")
            })
        } else {
            Log.w("SignalRClient", "Cannot send message to $chatId: SignalR not connected")
        }
    }

    private fun saveToken(token: String, expiry: Long) {
        val sharedPreferences = appContext.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString(AUTH_TOKEN_KEY, token)
            putLong(TOKEN_EXPIRY_KEY, expiry)
            apply()
        }
        Log.i("SignalRClient", "Token saved with expiry: $expiry")
    }

    private fun loadToken(): Pair<String?, Long?> {
        val sharedPreferences = appContext.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        val token = sharedPreferences.getString(AUTH_TOKEN_KEY, null)
        val expiry = if (sharedPreferences.contains(TOKEN_EXPIRY_KEY)) {
            sharedPreferences.getLong(TOKEN_EXPIRY_KEY, 0)
        } else null
        return Pair(token, expiry)
    }

    private fun clearToken() {
        val sharedPreferences = appContext.getSharedPreferences(AUTH_PREFS_NAME, Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove(AUTH_TOKEN_KEY)
            remove(TOKEN_EXPIRY_KEY)
            apply()
        }
        Log.i("SignalRClient", "Token cleared")
    }
}