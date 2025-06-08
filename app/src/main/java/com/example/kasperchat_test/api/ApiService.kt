package com.example.kasperchat_test.api
import com.example.kasperchat_test.model.Chat
import com.example.kasperchat_test.model.ChatMember
import com.example.kasperchat_test.model.CreateChatRequest
import com.example.kasperchat_test.model.CreateMessageRequest
import com.example.kasperchat_test.model.LoginRequest
import com.example.kasperchat_test.model.LoginResponse
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.model.RegisterRequest
import com.example.kasperchat_test.model.RegisterResponse
import com.example.kasperchat_test.model.UserProfile
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
interface ApiService {
    @POST("api/auth/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
    @POST("api/auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUserProfile(): Response<UserProfile>

    @GET("api/chats")
    suspend fun getChats(): Response<List<Chat>>

    @GET("api/chats/{chatId}/messages")
    suspend fun getMessagesByChatId(
        @Path("chatId") chatId: String,
        @Query("offset") offset: Int = 0,
        @Query("limit") limit: Int = 20
    ): Response<List<Message>>

    @GET("api/users")
    suspend fun getUsers(): Response<List<UserProfile>>

    @GET("api/chats/{chatId}/members")
    suspend fun getChatMembers(@Path("chatId") chatId: String): Response<List<ChatMember>>

    @POST("api/chats/{chatId}/messages")
    suspend fun sendMessageToChat(
        @Path("chatId") chatId: String,
        @Body request: CreateMessageRequest
    ): Response<Message>

    @GET("api/chats/{chatId}")
    suspend fun getChatById(@Path("chatId") chatId: String): Response<Chat>

    @POST("api/chats")
    suspend fun createChat(@Body request: CreateChatRequest): Response<Chat>

    @PUT("api/chats/{chatId}")
    suspend fun updateChat(
        @Path("chatId") chatId: String,
        @Body chat: Chat
    ): Response<Chat>

    @POST("api/chats/{chatId}/members")
    suspend fun addChatMember(
        @Path("chatId") chatId: String,
        @Body userId: String
    ): Response<ChatMember>
}