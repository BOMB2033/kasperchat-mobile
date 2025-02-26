package com.example.kasperchat_test.ui.adapter.message

data class MessageItem(
    val messageId: String,
    val text: String,
    val timestamp: Long,
    val authorId: String,
    val isRead: Boolean = false,
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val imageUrl: String? = null,
    val replyToMessageId: String? = null,
    val type: MessageType = MessageType.TEXT
)

enum class MessageType {
    TEXT,
    IMAGE,
    VIDEO
}
