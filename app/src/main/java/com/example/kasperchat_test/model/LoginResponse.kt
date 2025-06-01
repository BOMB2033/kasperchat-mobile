package com.example.kasperchat_test.model // Или ваш пакет для моделей

import java.util.Date

data class LoginResponse(
    val token: String,
    val expiresAt: Date // Gson сможет распарсить стандартный формат даты из JSON
)