package com.example.kasperchat_test // Ваш корневой пакет

import android.app.Application
import com.example.kasperchat_test.network.RetrofitClient

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Инициализация RetrofitClient с applicationContext
        RetrofitClient.initialize(this)
    }
}