package com.example.kasperchat_test.model
import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id") // Убедитесь, что имена совпадают с JSON от сервера
    val id: Int,
    @SerializedName("fullName")
    val fullName: String?, // Может быть null, если не всегда есть
    @SerializedName("login") // Обычно это username или email
    val login: String,
    @SerializedName("avatar") // Avatar приходит как Base64 строка, может быть null
    val avatar: String?
)