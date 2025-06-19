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
import com.example.kasperchat_test.model.UpdateChatRequest
import com.example.kasperchat_test.model.UpdateUserProfileRequest
import com.example.kasperchat_test.model.UploadResponse
import com.example.kasperchat_test.model.UserProfile
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    suspend fun registerUser(@Body registerRequest: RegisterRequest): Response<RegisterResponse>
    @POST("api/auth/login")
    suspend fun loginUser(@Body loginRequest: LoginRequest): Response<LoginResponse>

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

    @GET("api/chats/ChatMembers/{chatId}")
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
        @Body chat: UpdateChatRequest
    ): Response<Chat>

    @POST("api/chats/ChatMembers/{chatId}")
    suspend fun addChatMember(
        @Path("chatId") chatId: String,
        @Body userId: String
    ): Response<ChatMember>

    // Путь изменен с /api/auth/me на /api/users/me
    @GET("api/users/me")
    suspend fun getCurrentUserProfile(): Response<UserProfile>

    // НОВЫЙ МЕТОД
    @PUT("api/users/me")
    suspend fun updateUserProfile(@Body request: UpdateUserProfileRequest): Response<UserProfile>

    /**
     * Выполняет поиск пользователей для добавления в чат.
     * Отправляет GET-запрос на эндпоинт: /api/users/search
     *
     * @param query Текст для поиска (логин или полное имя пользователя).
     * @param chatId ID чата, в который планируется добавление (для исключения текущих участников).
     * @return Ответ сервера, содержащий список найденных пользователей в виде List<UserProfile>.
     */
    @GET("api/users/search")
    suspend fun searchUsers(
        @Query("query") query: String,
        @Query("chatId") chatId: String
    ): Response<List<UserProfile>>

    @DELETE("api/chats/ChatMembers/{chatId}/{userId}") // Используем HTTP DELETE
    suspend fun removeChatMember(
        @Path("chatId") chatId: String,
        @Path("userId") userId: String
    ): Response<Unit> // Ответ может быть пустым (204 No Content)

    /**
     * Отмечает все сообщения в чате как прочитанные для текущего пользователя.
     * Отправляет POST-запрос на эндпоинт: /api/chats/{chatId}/read
     */
    @POST("api/chats/{chatId}/messages/read")
    suspend fun markMessagesAsRead(@Path("chatId") chatId: String): Response<Unit>

    @DELETE("api/chats/{chatId}")
    suspend fun deleteChat(@Path("chatId") chatId: String): Response<Unit>

    @Multipart
    @POST("api/file/upload")
    suspend fun uploadFile(@Part file: MultipartBody.Part): Response<UploadResponse>

    @POST("api/users/fcm-token")
    suspend fun updateFcmToken(@Body token: String): Response<Unit>


}