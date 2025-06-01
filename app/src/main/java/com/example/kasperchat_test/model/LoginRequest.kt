package com.example.kasperchat_test.model

data class LoginRequest(
    val username: String, // Должно совпадать с "Username" в LoginData на сервере
    val password: String  // Должно совпадать с "Password" в LoginData на сервере
)