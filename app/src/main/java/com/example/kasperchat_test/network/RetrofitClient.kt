package com.example.kasperchat_test.network

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.example.kasperchat_test.api.ApiService
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "http://185.130.224.155:5012" // Замени на HTTPS, когда настроишь
    private const val PREFS_NAME = "KasperChatPrefs"
    private const val TOKEN_KEY = "auth_token"

    private val sharedPreferences: SharedPreferences by lazy {
        MyApplication.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val token = sharedPreferences.getString(TOKEN_KEY, null)
                val requestBuilder = original.newBuilder()
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer $token")
                }
                // Создаём новый запрос, сохраняя метод и тело оригинального запроса
                val newRequest: Request = requestBuilder.build()
                chain.proceed(newRequest)
            }
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Функция для сохранения токена
    fun saveToken(token: String) {
        sharedPreferences.edit().putString(TOKEN_KEY, token).apply()
    }

    // Функция для удаления токена (например, при выходе)
    fun clearToken() {
        sharedPreferences.edit().remove(TOKEN_KEY).apply()
    }
}

// Класс для доступа к контексту приложения
@SuppressLint("StaticFieldLeak")
object MyApplication {
    lateinit var context: Context
    fun initialize(context: Context) {
        MyApplication.context = context.applicationContext
    }
}
