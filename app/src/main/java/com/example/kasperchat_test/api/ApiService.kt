package com.example.kasperchat_test.api

import com.example.kasperchat_test.model.LoginResponse
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.model.User
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("api/auth/login")
    fun login(@Body user: User): Call<LoginResponse>

    @GET("api/messages")
    fun getMessages(): Call<List<Message>>

    @POST("api/messages")
    fun sendMessage(@Body message: Message): Call<Message>
}