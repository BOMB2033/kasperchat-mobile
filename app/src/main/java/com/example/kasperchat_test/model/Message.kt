package com.example.kasperchat_test.model
import java.util.Date

data class Message(
    val id: Int,
    val chatId: Int,
    val authorId: Int,
    val content: String,
    val timestamp: Date,
    val isRead: Boolean = false,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val fileId: Int? = null,
    val replyToMessageId: Int? = null,
)
