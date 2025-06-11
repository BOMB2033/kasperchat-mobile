package com.example.kasperchat_test.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class UpdateUserProfileRequest(
    val fullName: String?,
    val bio: String?,
    val avatarUrl: String?
)

data class Chat(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String,
    @SerializedName("isGroup")
    val isGroup: Boolean,
    @SerializedName("creatorId")
    val creatorId: String,
    @SerializedName("isMuted")
    val isMuted: Boolean,
    @SerializedName("notificationSound")
    val notificationSound: String? = null,
    @SerializedName("isEncrypted")
    val isEncrypted: Boolean = true,
    @SerializedName("messageRetention")
    val messageRetention: String? = null,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("backgroundUrl")
    val backgroundUrl: String? = null,
    @SerializedName("bubbleColor")
    val bubbleColor: String? = null,
    @SerializedName("createdAt")
    val createdAt: Date,
    @SerializedName("lastMessage")
    val lastMessage: Message? = null,
    @SerializedName("authorNameLastMessage")
    val authorNameLastMessage: String? = null,
    @SerializedName("countUnreadMessages")
    val countUnreadMessages: Int? = null,
)

data class CreateChatRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("isGroup")
    val isGroup: Boolean,
    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,
    @SerializedName("backgroundUrl")
    val backgroundUrl: String? = null,
    @SerializedName("bubbleColor")
    val bubbleColor: String? = null
)

data class ChatMember(
    @SerializedName("chatId")
    val chatId: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("joinedAt")
    val joinedAt: Date
)

data class Message(
    @SerializedName("id")
    val id: String,
    @SerializedName("chatId")
    val chatId: String,
    @SerializedName("authorId")
    val authorId: String,
    @SerializedName("content")
    val content: String?,
    @SerializedName("messageType")
    val messageType: String,
    @SerializedName("timestamp")
    val timestamp: Date,
    @SerializedName("isRead")
    val isRead: Boolean,
    @SerializedName("isEdited")
    val isEdited: Boolean,
    @SerializedName("isDeleted")
    val isDeleted: Boolean,
    @SerializedName("fileId")
    val fileId: String? = null,
    @SerializedName("replyToMessageId")
    val replyToMessageId: String? = null,
    @SerializedName("forwardedFromMessageId")
    val forwardedFromMessageId: String? = null
)

data class CreateMessageRequest(
    @SerializedName("content")
    val content: String,
    @SerializedName("messageType")
    val messageType: String = "Text",
    @SerializedName("fileId")
    val fileId: String? = null,
    @SerializedName("replyToMessageId")
    val replyToMessageId: String? = null,
    @SerializedName("forwardedFromMessageId")
    val forwardedFromMessageId: String? = null
)

data class UserProfile(
    @SerializedName("id")
    val id: String,
    @SerializedName("fullName")
    val fullName: String?,
    @SerializedName("login")
    val login: String,
    @SerializedName("bio")
    val bio: String?,
    @SerializedName("avatarUrl")
    val avatarUrl: String?,
    @SerializedName("lastOnline")
    val lastOnline: Date?,
    @SerializedName("isOnline")
    val isOnline: Boolean
)

data class LoginRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("expiresAt")
    val expiresAt: Date,
    @SerializedName("userId")
    val userId: String
)

data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("fullName")
    val fullName: String,
    @SerializedName("email")
    val email: String? = null
)

data class RegisterResponse(
    @SerializedName("token")
    val token: String,
    @SerializedName("userId")
    val userId: String
)

data class DisplayableChatItem(
    val chat: Chat,
    val linkedUserProfiles: List<UserProfile>,
    val lastMessageText: String? = null,
    var authorName: String? = null,
    val lastMessageTimestamp: Date? = null,
    val unreadMessagesCount: Int = 0
) {
    val id: String = chat.id ?: ""
}

data class UpdateChatRequest(
    val name: String,
    val isGroup: Boolean,
    val avatarUrl: String?,
    val backgroundUrl: String?,
    val bubbleColor: String?
)

data class UploadResponse(
    @SerializedName("url") // Имя поля должно совпадать с JSON-ответом сервера ("Url")
    val url: String
)