package com.example.kasperchat_test // Ваш корневой пакет

import android.app.Application
import com.example.kasperchat_test.network.RetrofitClient
import com.example.kasperchat_test.network.SignalRClient

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.initialize(this)
        SignalRClient.initialize(this, null) // Токен можно обновить после логина
    }
}