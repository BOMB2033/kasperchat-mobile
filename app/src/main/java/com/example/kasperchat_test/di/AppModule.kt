package com.example.kasperchat_test.di
import android.content.Context
import android.content.SharedPreferences
import com.example.kasperchat_test.api.ApiService
import com.example.kasperchat_test.api.RetrofitClient
import com.example.kasperchat_test.signalr.SignalRManager
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
    fun provideApiService(): ApiService = RetrofitClient.apiService

    @Provides
    @Singleton
    fun provideSignalRManager(): SignalRManager = SignalRManager()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        .create()

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences =
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
}