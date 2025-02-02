package com.example.kasperchat_test.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kasperchat_test.database.MyApplication
import com.example.kasperchat_test.database.User
import com.example.kasperchat_test.databinding.ActivityMainBinding
import com.example.kasperchat_test.network.SocketManager
import com.example.kasperchat_test.network.SocketManagerInterface

class MainActivity : AppCompatActivity(),SocketManagerInterface {
    private lateinit var binding: ActivityMainBinding
    override lateinit var socketManager:SocketManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        socketManager  = SocketManager(applicationContext)

        // Получение экземпляра DAO
        val userDao = (application as MyApplication).database.userDao()

// Вставка данных
        val user = User(firstName= "John", lastName = "Doe")
        userDao.insertAll(user)

// Получение всех пользователей
        val users = userDao.getAll()

// Поиск пользователя по имени
        val foundUser = userDao.findByName("John", "Doe")

// Обновление пользователя
        val updatedUser = User(id = foundUser.id, firstName = "Updated John", lastName = "Updated Doe")
        userDao.update(updatedUser)

// Удаление пользователя
        userDao.delete(updatedUser)

    }
}