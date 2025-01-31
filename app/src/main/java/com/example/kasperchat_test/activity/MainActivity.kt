package com.example.kasperchat_test.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kasperchat_test.databinding.ActivityMainBinding
import com.example.kasperchat_test.network.SocketManager
import com.example.kasperchat_test.network.SocketManagerInterface

class MainActivity : AppCompatActivity(),SocketManagerInterface {
    private lateinit var binding: ActivityMainBinding
    override val socketManager:SocketManager = SocketManager(applicationContext)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}