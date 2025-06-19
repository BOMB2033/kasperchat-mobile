package com.example.kasperchat_test
import android.app.Application
import com.example.kasperchat_test.signalr.SignalRManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.example.kasperchat_test.api.RetrofitClient
import com.google.firebase.messaging.FirebaseMessaging
import androidx.core.content.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@HiltAndroidApp
class KasperChatApp : Application() {
    @Inject
    lateinit var signalRManager: SignalRManager

    override fun onCreate() {
        super.onCreate()
        // Инициализация логирования (например, Timber или Log)
        Log.d("KasperChatApp", "Application initialized")
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token: $token")
                // Сохраняем токен и отправляем на сервер
                val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                sharedPreferences.edit { putString("fcm_token", token) }
                sendFcmTokenToServer(token)
            } else {
                Log.e("FCM", "Failed to get FCM token", task.exception)
            }
        }
        // Инициализация SignalR, если токен уже сохранён
        val token = getAuthToken()
        if (token != null) {
            RetrofitClient.setToken(token)
            signalRManager.startConnection()
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES) //TODO: Убрать после реализации светлого дизайна
    }

    private fun sendFcmTokenToServer(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                RetrofitClient.apiService.updateFcmToken(token)
                Log.d("FCM", "FCM Token sent to server")
            } catch (e: Exception) {
                Log.e("FCM", "Failed to send FCM token", e)
            }
        }
    }

    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val expiry = sharedPreferences.getLong("token_expiry", 0)
        return if (expiry > System.currentTimeMillis()) token else null
    }
}