package com.example.kasperchat_test.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatsListBinding
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.ui.adapter.chatelement.ChatAdapter
import com.example.kasperchat_test.ui.adapter.chatelement.ChatItem
import com.example.kasperchat_test.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class ChatsListFragment : Fragment() {
    private lateinit var binding: FragmentChatsListBinding
    private lateinit var chatAdapter: ChatAdapter
    private val chatItems = mutableListOf<ChatItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            rvChats.layoutManager = LinearLayoutManager(requireContext())

            // Инициализация адаптера
            chatAdapter = ChatAdapter(object : ChatAdapter.OnItemClickListener {
                override fun onItemClick(chatItem: ChatItem) {
                    val action = ChatsListFragmentDirections.actionChatsListFragmentToChatFragment(chatItem.chatId)
                    view.findNavController().navigate(action)
                }
            })

            binding.rvChats.adapter = chatAdapter

            // Кнопки навигации
            addChannelButton.setOnClickListener {
                it.findNavController().navigate(R.id.action_chatsListFragment_to_options_chat_group)
            }

            optionsButton.setOnClickListener {
                it.findNavController().navigate(R.id.action_chatsListFragment_to_preferensFragment)
            }

            // Загрузка сообщений с сервера
            loadMessages()
        }
    }

    private fun loadMessages() {
        val token = requireContext().getSharedPreferences("KasperChatPrefs", Context.MODE_PRIVATE)
            .getString("auth_token", null)

        if (token == null) {
            // Если токена нет, перенаправляем на логин
            findNavController().navigate(R.id.loginFragment) // TODO Сделать запрет возвращение по стаку
            return
        }

        RetrofitClient.apiService.getMessages().enqueue(object : Callback<List<Message>> {
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                if (response.isSuccessful) {
                    val messages = response.body() ?: emptyList()
                    chatItems.clear()
                    messages.forEach { serverMessage ->
                        // Преобразуем серверные данные в ChatItem
                        val chatItem = ChatItem(
                            chatId = serverMessage.id.toString(), // id из Message в chatId
                            lastMessage = serverMessage.content, // content в lastMessage
                            lastMessageTimestamp = serverMessage.timestamp.time, // timestamp в миллисекундах
                            otherUserName = serverMessage.sender, // sender в otherUserName
                            unreadMessagesCount = 0, // Пока 0, можно запросить с сервера, если доступно
                            otherUserAvatarUrl = null // Пока нет аватара, оставляем null
                        )
                        chatItems.add(chatItem)
                    }
                    chatAdapter.submitList(chatItems.toList()) // Обновляем адаптер
                    Toast.makeText(requireContext(), "Чаты загружены", Toast.LENGTH_SHORT).show()
                } else if (response.code() == 401) {
                    // Токен недействителен, очищаем и перенаправляем на логин
                    RetrofitClient.clearToken()
                    findNavController().navigate(R.id.loginFragment) // TODO Сделать запрет возвращение по стаку
                    Toast.makeText(requireContext(), "Ваша сессия истекла. Авторизуйтесь заново.", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("ChatsListFragment", "Failed to get messages: ${response.message()}")
                    Toast.makeText(requireContext(), "Не удалось загрузить чаты", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                Log.e("ChatsListFragment", "Error: ${t.message}")
            }
        })
    }

    private fun stringToTimestamp(dateTimeString: String, pattern: String = "dd.MM.yyyy HH:mm"): Long {
        return try {
            val formatter = DateTimeFormatter.ofPattern(pattern)
            val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
            val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
            zonedDateTime.toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            println("Error parsing date and time: ${e.message}")
            System.currentTimeMillis()
        }
    }
}