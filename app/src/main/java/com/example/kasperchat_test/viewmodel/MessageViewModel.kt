package com.example.kasperchat_test.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.network.RetrofitClient
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.launch
import java.io.IOException

// Sealed class для представления результата загрузки сообщений
sealed class MessagesResult {
    data class Success(val messages: List<Message>) : MessagesResult()
    data class Error(val message: String) : MessagesResult()
    data object Loading : MessagesResult()
}
sealed class SendMessageResult {
    data class Success(val sentMessage: Message) : SendMessageResult()
    data class Error(val errorMessage: String) : SendMessageResult()
    data object Sending : SendMessageResult()
}
class MessageViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {

    // Используем Map для хранения сообщений по chatId, чтобы не смешивать их
    // Ключ - chatId, значение - LiveData с результатом для этого чата
    private val _messagesByChat = mutableMapOf<Int, MutableLiveData<MessagesResult>>()

    // Экспонируем LiveData для конкретного chatId, который будет запрашивать ChatFragment
    fun getMessagesForChat(chatId: Int): LiveData<MessagesResult> {
        return _messagesByChat.getOrPut(chatId) {
            MutableLiveData<MessagesResult>().also {
                // Если для этого chatId еще нет данных, или мы хотим обновить, загружаем их
                // Можно добавить логику, чтобы не загружать каждый раз, если данные уже есть и свежие
                fetchMessages(chatId)
            }
        }
    }
    private val _sendMessageResult = MutableLiveData<SendMessageResult>()
    val sendMessageResult: LiveData<SendMessageResult> = _sendMessageResult

    private val apiService = RetrofitClient.instance
    private val loginViewModel = LoginViewModel(application) // Для обработки ошибок авторизации

    // Метод для загрузки или обновления сообщений для конкретного чата
    fun fetchMessages(chatId: Int) {
        val liveData = _messagesByChat.getOrPut(chatId) { MutableLiveData() }
        liveData.postValue(MessagesResult.Loading)

        viewModelScope.launch {
            val token = loginViewModel.getAuthToken()
            if (token == null) {
                liveData.postValue(MessagesResult.Error("Пользователь не аутентифицирован"))
                Log.e("MessageViewModel", "Токен отсутствует для chatId: $chatId")
                return@launch
            }

            try {
                Log.d("MessageViewModel", "Загрузка сообщений для chatId: $chatId")
                val response = apiService.getMessagesByChatId(chatId)

                if (response.isSuccessful && response.body() != null) {
                    liveData.postValue(MessagesResult.Success(response.body()!!))
                    Log.i("MessageViewModel", "Сообщения для chatId $chatId успешно загружены: ${response.body()!!.size} сообщений")
                } else {
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Не удалось загрузить сообщения для chatId $chatId: ${response.code()} - ${response.message()}. Тело ошибки: $errorBody"
                    Log.e("MessageViewModel", errorMessage)
                    if (response.code() == 401) {
                        errorMessage = "Не авторизован. Пожалуйста, войдите снова."
                        loginViewModel.clearAuthToken() // Разлогиниваем
                    } else if (response.code() == 403) { // Forbid - пользователь не состоит в чате
                        errorMessage = "Доступ к этому чату запрещен."
                    }
                    liveData.postValue(MessagesResult.Error(errorMessage))
                }
            } catch (e: IOException) {
                Log.e("MessageViewModel", "Сетевая ошибка при загрузке сообщений для chatId $chatId", e)
                liveData.postValue(MessagesResult.Error("Сетевая ошибка. Проверьте ваше подключение."))
            } catch (e: JsonSyntaxException) {
                Log.e("MessageViewModel", "Ошибка парсинга JSON для chatId $chatId", e)
                liveData.postValue(MessagesResult.Error("Ошибка обработки данных от сервера."))
            }
            catch (e: Exception) {
                Log.e("MessageViewModel", "Непредвиденная ошибка при загрузке сообщений для chatId $chatId", e)
                liveData.postValue(MessagesResult.Error("Произошла непредвиденная ошибка."))
            }
        }
    }

    fun sendMessage(chatId: Int, messageText: String, currentUserId: Int /* пока не используется в запросе, но может быть полезен для UI */) {
        _sendMessageResult.postValue(SendMessageResult.Sending) // Устанавливаем состояние "Отправка"
        viewModelScope.launch {
            val token = loginViewModel.getAuthToken()
            if (token == null) {
                _sendMessageResult.postValue(SendMessageResult.Error("Пользователь не аутентифицирован для отправки."))
                Log.e("MessageViewModel", "Токен отсутствует. Невозможно отправить сообщение.")
                return@launch
            }

            // Проверка на пустое сообщение перед отправкой на сервер
            if (messageText.isBlank()) {
                _sendMessageResult.postValue(SendMessageResult.Error("Сообщение не может быть пустым."))
                Log.w("MessageViewModel", "Попытка отправить пустое сообщение в chatId: $chatId")
                return@launch
            }

            try {
                Log.d("MessageViewModel", "Отправка сообщения в chatId: $chatId, текст: \"$messageText\"")

                // Используем вариант с передачей строки напрямую, Retrofit/Gson обернет ее в JSON-строку.
                // Если сервер ожидает "сырой" текст, нужно будет использовать RequestBody.
                val response = apiService.sendMessageToChat(chatId, messageText)

                if (response.isSuccessful && response.body() != null) {
                    val sentMessage = response.body()!!
                    _sendMessageResult.postValue(SendMessageResult.Success(sentMessage))
                    Log.i("MessageViewModel", "Сообщение успешно отправлено: $sentMessage")

                    // Оптимистичное обновление UI: добавляем отправленное сообщение в существующий список.
                    // Это улучшает UX, так как пользователь сразу видит свое сообщение.
                    val currentMessagesLiveData = _messagesByChat[chatId]
                    val currentResult = currentMessagesLiveData?.value
                    if (currentResult is MessagesResult.Success) {
                        val updatedMessages = currentResult.messages.toMutableList()
                        updatedMessages.add(sentMessage)
                        currentMessagesLiveData.postValue(MessagesResult.Success(updatedMessages))
                    } else {
                        // Если по какой-то причине текущего списка нет (например, первая загрузка провалилась),
                        // можно инициировать полную перезагрузку сообщений.
                        // Однако, если отправка успешна, а загрузка провалилась, это странная ситуация.
                        // Возможно, лучше просто дождаться следующего pull-to-refresh от пользователя.
                        // Для простоты, если списка нет, можно его запросить.
                        Log.w("MessageViewModel", "Список сообщений для chatId $chatId не был в состоянии Success при добавлении отправленного сообщения. Запрашиваем полный список.")
                        fetchMessages(chatId)
                    }
                } else {
                    // Обработка ошибок от сервера
                    val errorBody = response.errorBody()?.string()
                    var errorMessage = "Не удалось отправить сообщение: ${response.code()} - ${response.message()}."
                    if (!errorBody.isNullOrBlank()) {
                        errorMessage += " Тело ошибки: $errorBody"
                    }
                    Log.e("MessageViewModel", errorMessage)

                    when (response.code()) {
                        401 -> {
                            errorMessage = "Не авторизован для отправки. Пожалуйста, войдите снова."
                            loginViewModel.clearAuthToken() // Разлогиниваем
                        }
                        403 -> {
                            errorMessage = "Доступ к отправке в этот чат запрещен."
                        }
                        400 -> {
                            // Можно добавить более специфичную обработку ошибок 400,
                            // если сервер возвращает структурированные ошибки в теле.
                            errorMessage = "Неверный запрос для отправки сообщения. Проверьте данные."
                        }
                    }
                    _sendMessageResult.postValue(SendMessageResult.Error(errorMessage))
                }
            } catch (e: IOException) {
                Log.e("MessageViewModel", "Сетевая ошибка при отправке сообщения для chatId $chatId", e)
                _sendMessageResult.postValue(SendMessageResult.Error("Сетевая ошибка при отправке. Проверьте подключение и попробуйте снова."))
            } catch (e: JsonSyntaxException) {
                Log.e("MessageViewModel", "Ошибка парсинга JSON при отправке/получении ответа на сообщение для chatId $chatId", e)
                _sendMessageResult.postValue(SendMessageResult.Error("Ошибка обработки ответа от сервера после отправки сообщения."))
            } catch (e: Exception) {
                Log.e("MessageViewModel", "Непредвиденная ошибка при отправке сообщения для chatId $chatId", e)
                _sendMessageResult.postValue(SendMessageResult.Error("Произошла непредвиденная ошибка при отправке сообщения."))
            }
        }
    }
}