package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatBinding
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.network.SignalRClient
import com.example.kasperchat_test.ui.adapter.message.MessageListAdapter
import com.example.kasperchat_test.viewmodel.ChatDataResult
import com.example.kasperchat_test.viewmodel.LoginViewModel
import com.example.kasperchat_test.viewmodel.MessageViewModel
import com.example.kasperchat_test.viewmodel.MessagesResult
import com.microsoft.signalr.HubConnectionState

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val args: ChatFragmentArgs by navArgs()
    private val messageViewModel: MessageViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var messageAdapter: MessageListAdapter
    private var currentUserId: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val chatId = args.chatId
        Log.d("ChatFragment", "Displaying chat for chatId: $chatId")

        // Наблюдение за ID текущего пользователя
        loginViewModel.currentUserIdLiveData.observe(viewLifecycleOwner) { userId ->
            if (userId != null) {
                currentUserId = userId
                Log.d("ChatFragment", "Current User ID updated: $currentUserId")
                if (::messageAdapter.isInitialized) {
                    messageAdapter.setCurrentUserId(currentUserId)
                }
            } else {
                currentUserId = -1
                Log.d("ChatFragment", "Current User ID is null.")
                handleAuthOrAccessError()
            }
        }

        // Инициализация SignalR
        val token = loginViewModel.getAuthToken()
        if (token != null) {
            SignalRClient.initialize(requireContext(), token)
            SignalRClient.startConnection()
            waitForSignalRConnection(chatId)
        } else {
            Log.w("ChatFragment", "No valid token available for SignalR")
            Toast.makeText(context, "Real-time messaging unavailable", Toast.LENGTH_SHORT).show()
        }

        // Подписка на уведомления об обновлении сообщений
        SignalRClient.setOnMessagesUpdateListener { notifiedChatId ->
            if (notifiedChatId == chatId) {
                activity?.runOnUiThread {
                    Log.d("ChatFragment", "Received update notification for chatId: $chatId")
                    messageViewModel.fetchMessages(chatId)
                }
            }
        }

        // Наблюдение за данными чата
        messageViewModel.getChatData(chatId).observe(viewLifecycleOwner) { result ->
            when (result) {
                is ChatDataResult.Success -> {
                    val chatName = result.chat.name?.takeIf { it.isNotBlank() } ?: "Chat $chatId"
                    binding.toolbarInclude.chatTitle.text = chatName
                    Log.d("ChatFragment", "Chat title set to: $chatName")
                }
                is ChatDataResult.Error -> {
                    binding.toolbarInclude.chatTitle.text = "Chat $chatId"
                    Log.e("ChatFragment", "Error loading chat data: ${result.message}")
                }
                is ChatDataResult.Loading -> {
                    binding.toolbarInclude.chatTitle.text = "Loading..."
                }
            }
        }

        // Загрузка данных чата
        messageViewModel.fetchChatData(chatId)

        // Настройка UI
        binding.toolbarInclude.buttonExit.setOnClickListener {
            SignalRClient.leaveChatGroup(chatId.toString())
            findNavController().popBackStack()
        }

        setupRecyclerView()
        setupUI(chatId)
        observeMessages(chatId)

        // Начальная загрузка сообщений
        messageViewModel.fetchMessages(chatId)
    }

    private fun waitForSignalRConnection(chatId: Int) {
        if (SignalRClient.isConnected()) {
            SignalRClient.joinChatGroup(chatId.toString())
        } else {
            Thread {
                var attempts = 0
                val maxAttempts = 10 // 5 секунд
                while (attempts < maxAttempts && !SignalRClient.isConnected()) {
                    Thread.sleep(500)
                    attempts++
                }
                if (SignalRClient.isConnected()) {
                    SignalRClient.joinChatGroup(chatId.toString())
                } else {
                    Log.e("ChatFragment", "Failed to connect to SignalR after $maxAttempts attempts")
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Failed to connect to real-time messaging", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    private fun setupUI(chatId: Int) {
        with(binding) {
            progressBarMessages.isVisible = false
            textViewErrorMessages.isVisible = false

            buttonSendMessage.setOnClickListener {
                val messageText = editTextMessage.text.toString().trim()
                if (messageText.isNotEmpty()) {
                    if (currentUserId != -1) {
                        if (SignalRClient.isConnected()) {
                            SignalRClient.sendMessage(chatId.toString(), currentUserId.toString(), messageText)
                            editTextMessage.text.clear()
                        } else {
                            Toast.makeText(context, "Cannot send message: no real-time connection", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            swipeRefreshLayoutMessages.setOnRefreshListener {
                messageViewModel.fetchMessages(chatId)
                messageViewModel.fetchChatData(chatId) // Обновляем данные чата при свайпе
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageListAdapter(
            chatId = args.chatId,
            currentUserId,
            onItemClickListener = object : MessageListAdapter.OnItemClickListener {
                override fun onItemClick(chatItem: Message) {
                    // Обработка клика на сообщение
                }
            },
            onItemLongClickListener = object : MessageListAdapter.OnItemLongClickListener {
                override fun onEditMessage(position: Int, message: Message) {
                    // Обработка редактирования сообщения
                }
                override fun onDeleteMessage(position: Int) {
                    // Обработка удаления сообщения
                }
            }
        )
        binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun observeMessages(chatId: Int) {
        messageViewModel.getMessagesForChat(chatId).observe(viewLifecycleOwner, Observer { result ->
            binding.swipeRefreshLayoutMessages.isRefreshing = false
            binding.progressBarMessages.isVisible = result is MessagesResult.Loading
            binding.textViewErrorMessages.isVisible = result is MessagesResult.Error

            when (result) {
                is MessagesResult.Loading -> {
                    Log.d("ChatFragment", "Loading messages for chatId $chatId...")
                    binding.rvMessages.isVisible = false
                }
                is MessagesResult.Success -> {
                    Log.i("ChatFragment", "Messages loaded for chatId $chatId: ${result.messages.size} items")
                    binding.rvMessages.isVisible = true
                    if (result.messages.isEmpty()) {
                        binding.textViewErrorMessages.isVisible = true
                        binding.textViewErrorMessages.text = getString(R.string.no_messages_in_chat)
                    } else {
                        messageAdapter.submitList(result.messages.sortedBy { it.timestamp })
                        binding.rvMessages.scrollToPosition(result.messages.size - 1)
                    }
                }
                is MessagesResult.Error -> {
                    Log.e("ChatFragment", "Error loading messages for chatId $chatId: ${result.message}")
                    binding.textViewErrorMessages.text = result.message
                    binding.rvMessages.isVisible = false
                    if (result.message.contains("Не авторизован", ignoreCase = true) ||
                        result.message.contains("Доступ к этому чату запрещен", ignoreCase = true) ||
                        result.message.contains("401") || result.message.contains("403")
                    ) {
                        handleAuthOrAccessError()
                    }
                }
            }
        })
    }

    private fun handleAuthOrAccessError() {
        Toast.makeText(context, "Access denied or session expired.", Toast.LENGTH_LONG).show()
        loginViewModel.clearAuthToken()
        SignalRClient.stopConnection()
        if (isAdded && findNavController().currentDestination?.id == R.id.chatFragment) {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        SignalRClient.leaveChatGroup(args.chatId.toString())
        super.onDestroyView()
        _binding = null
    }
}