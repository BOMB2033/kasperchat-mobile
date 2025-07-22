package com.fvk_solutions.kasperchat.di

import android.content.Context
import android.content.SharedPreferences
import com.fvk_solutions.kasperchat.api.ApiService
import com.fvk_solutions.kasperchat.api.RetrofitClient
import com.fvk_solutions.kasperchat.signalr.SignalRManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()

    @Provides
    @Singleton
    fun provideApiService(gson: Gson): ApiService {
        // Поскольку RetrofitClient - это объект-компаньон, мы можем использовать его напрямую
        // Но для корректной работы с Hilt, возвращаем apiService
        return RetrofitClient.apiService
    }

    @Provides
    @Singleton
    fun provideSignalRManager(): SignalRManager = SignalRManager()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
}