package com.example.kasperchat_test.api

import com.example.kasperchat_test.model.Chat
import com.example.kasperchat_test.model.ChatData
import com.example.kasperchat_test.model.Link
import com.example.kasperchat_test.model.LoginRequest
import com.example.kasperchat_test.model.LoginResponse
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>
    @GET("api/auth/me")
    suspend fun getCurrentUserProfile(): Response<UserProfile>

    @GET("api/chats")
    suspend fun getChats(): Response<List<Chat>>

    @GET("api/chats/{chatId}/messages")
    suspend fun getMessagesByChatId(
        @Path("chatId") chatId: Int
    ): Response<List<Message>>

    @GET("api/chats/users")
    suspend fun getUsers(): Response<List<UserProfile>>

    @GET("api/chats/links")
    suspend fun getLinks(): Response<List<Link>>

    @POST("api/chats/{chatId}/messages")
    suspend fun sendMessageToChat(
        @Path("chatId") chatId: Int,
        @Body content: String
     ): Response<Message>

    @GET("/api/chats/{chatId}")
    suspend fun getChatById(@Path("chatId") chatId: Int): Response<ChatData>
}



