package com.example.kasperchat_test.network // Или ваш пакет для сети

import android.content.Context
import com.example.kasperchat_test.api.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // ЗАМЕНИТЕ НА ВАШ URL!
    // Например: "http://10.0.2.2:5012/" для локального сервера на эмуляторе Android Studio
    // или "http://192.168.X.X:5012/" если тестируете на реальном устройстве в той же Wi-Fi сети
    // или "http://185.130.224.155:5012/" для полноценного сервера
    private const val BASE_URL = "http://192.168.0.4:5012/" // Пример, используйте ваш актуальный URL

    // Application context нужно будет передать при первой инициализации
    private lateinit var appContext: Context // Используем lateinit и Context

    fun initialize(context: Context) {
        // Убедимся, что инициализация происходит только один раз и с applicationContext
        if (!this::appContext.isInitialized) {
            appContext = context.applicationContext // Важно использовать applicationContext
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Ленивая инициализация OkHttpClient с AuthInterceptor
    private val okHttpClient: OkHttpClient by lazy {
        if (!this::appContext.isInitialized) {
            throw IllegalStateException("RetrofitClient must be initialized with ApplicationContext before use.")
        }
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(appContext)) // Добавляем наш AuthInterceptor
            .build()
    }

    val instance: ApiService by lazy {
        if (!this::appContext.isInitialized) {
            throw IllegalStateException("RetrofitClient must be initialized with ApplicationContext before use.")
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Используем OkHttpClient с интерсепторами
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(ApiService::class.java)
    }
}