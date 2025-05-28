package com.example.kasperchat_test.model

import java.util.Date

data class Message(
    val id: Int,
    val sender: String,
    val content: String,
    val timestamp: Date
)