package com.example.kasperchat_test
import android.app.Application
import com.example.kasperchat_test.signalr.SignalRManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import android.util.Log
import com.example.kasperchat_test.api.RetrofitClient
@HiltAndroidApp
class KasperChatApp : Application() {
    @Inject
    lateinit var signalRManager: SignalRManager

    override fun onCreate() {
        super.onCreate()
        // Инициализация логирования (например, Timber или Log)
        Log.d("KasperChatApp", "Application initialized")

        // Инициализация SignalR, если токен уже сохранён
        val token = getAuthToken()
        if (token != null) {
            RetrofitClient.setToken(token)
            signalRManager.startConnection()
        }
    }

    private fun getAuthToken(): String? {
        val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)
        val expiry = sharedPreferences.getLong("token_expiry", 0)
        return if (expiry > System.currentTimeMillis()) token else null
    }
}