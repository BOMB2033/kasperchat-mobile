package com.example.kasperchat_test

import com.example.kasperchat_test.network.SocketManager
import com.example.kasperchat_test.network.SocketManagerInterface
import android.app.Application
import androidx.room.Room
import com.example.kasperchat_test.database.AppDatabase

class MyApplication : Application(), SocketManagerInterface {
    lateinit var database: AppDatabase
    override lateinit var socketManager: SocketManager
    override fun onCreate() {
        super.onCreate()
        socketManager = SocketManager()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "my-database"
        ).build()
    }
}