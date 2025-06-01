package com.example.kasperchat_test.network

import android.content.Context
import android.util.Log
import okhttp3.Interceptor

// Класс AuthInterceptor
class AuthInterceptor(private val application: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = getTokenFromStorage()
        val requestBuilder = chain.request().newBuilder()
        token?.let {
            Log.d("AuthInterceptor", "Adding token to request: Bearer $it")
            requestBuilder.addHeader("Authorization", "Bearer $it")
        }
        return chain.proceed(requestBuilder.build())
    }

    private fun getTokenFromStorage(): String? {
        // Используем те же ключи, что и в LoginViewModel
        val sharedPreferences = application.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("auth_token", null)
    }
}