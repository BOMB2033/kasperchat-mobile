package com.example.kasperchat_test.model

import java.util.Date

data class Chat(
    val id: Int,
    var chatName: String,
    val createdAt: Date // Gson по умолчанию должен справиться с форматом даты из ASP.NET Core
)