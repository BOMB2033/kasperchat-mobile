package com.example.kasperchat_test.ui.adapter.chatelement

data class ChatItem(
    val chatId: String, // Уникальный идентификатор чата
    val lastMessage: String, // Текст последнего сообщения
    val lastMessageTimestamp: Long, // Время последнего сообщения (Unix timestamp)
    val otherUserName: String, // Имя собеседника
    val unreadMessagesCount: Int = 0, // Количество непрочитанных сообщений
    val otherUserAvatarUrl: String? = null // Ссылка на аватар собеседника (опционально)
) : DiffItem, DiffContent {
    override fun getItemId(): Any = chatId
    override fun getContentHash(): Int =
        chatId.hashCode() + lastMessage.hashCode() + lastMessageTimestamp.hashCode()
}

interface DiffContent {

    fun getContentHash(): Int
}

interface DiffItem {

    fun getItemId(): Any
}
