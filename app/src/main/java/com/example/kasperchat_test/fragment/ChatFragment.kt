package com.example.kasperchat_test.fragment

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatBinding
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.ui.adapter.message.MessageListAdapter
import com.example.kasperchat_test.viewmodel.ChatViewModel
import com.example.kasperchat_test.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private val args: ChatFragmentArgs by navArgs()
    private val chatViewModel: ChatViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var messageAdapter: MessageListAdapter
    private var currentUserId: String? = null

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

        setupRecyclerView()
        setupUI(chatId)
        observeViewModel(chatId)

        // Подключение к чату и загрузка данных
        chatViewModel.joinChat(chatId)
    }

    private fun setupUI(chatId: String) {
        with(binding) {
            // Инициализация UI
            progressBarMessages.isVisible = false
            textViewErrorMessages.isVisible = false
            swipeRefreshLayoutMessages.isEnabled = true

            // Установка заголовка по умолчанию
            toolbarInclude.chatTitle.text = getString(R.string.loading)

            // Кнопка выхода
            toolbarInclude.buttonExit.setOnClickListener {
                chatViewModel.leaveChat(chatId)
                findNavController().popBackStack()
            }

            // Кнопка настроек чата
            toolbarInclude.buttonOptions.setOnClickListener {
                val action = ChatFragmentDirections.actionChatFragmentToOptionsChatGroup(chatId)
                findNavController().navigate(action)
            }

            // Отправка сообщения
            buttonSendMessage.setOnClickListener {
                hideKeyboard()
                val messageText = editTextMessage.text.toString().trim()
                if (messageText.isNotEmpty()) {
                    if (currentUserId != null) {
                        chatViewModel.sendMessage(chatId, messageText)
                        editTextMessage.text?.clear()
                    } else {
                        Toast.makeText(context, R.string.user_not_authenticated, Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Обновление по свайпу
            swipeRefreshLayoutMessages.setOnRefreshListener {
                chatViewModel.fetchMessages(chatId)
                chatViewModel.fetchChatData(chatId)
            }
        }
    }

    private fun setupRecyclerView() {
        messageAdapter = MessageListAdapter(
            chatId = args.chatId,
            currentUserId = currentUserId ?: "",
            onItemClickListener = object : MessageListAdapter.OnItemClickListener {
                override fun onItemClick(message: Message) {
                    // Обработка клика на сообщение (например, открыть детали)
                }
            },
            onItemLongClickListener = object : MessageListAdapter.OnItemLongClickListener {
                override fun onEditMessage(position: Int, message: Message) {
                    // Обработка редактирования сообщения (добавить серверный эндпоинт)
                }
                override fun onDeleteMessage(position: Int) {
                    // Обработка удаления сообщения (добавить серверный эндпоинт)
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

    private fun observeViewModel(chatId: String) {
        // Наблюдение за текущим пользователем
        loginViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                currentUserId = profile.id
                Log.d("ChatFragment", "Current User ID updated: $currentUserId")
                if (::messageAdapter.isInitialized) {
                    messageAdapter.setCurrentUserId(currentUserId ?: "")
                }
            } else {
                currentUserId = null
                Log.w("ChatFragment", "User profile is null")
                handleAuthOrAccessError()
            }
        }

        // Наблюдение за данными чата
        chatViewModel.chat.observe(viewLifecycleOwner) { chat ->
            if (chat != null) {
                val chatName = chat.name.takeIf { it.isNotBlank() } ?: getString(R.string.default_chat_name, chatId)
                binding.toolbarInclude.chatTitle.text = chatName
                Log.d("ChatFragment", "Chat title set to: $chatName")
            } else {
                binding.toolbarInclude.chatTitle.text = getString(R.string.default_chat_name, chatId)
                Log.w("ChatFragment", "Chat data is null")
            }
        }

        // Наблюдение за сообщениями
        chatViewModel.messages.observe(viewLifecycleOwner) { messages ->
            binding.swipeRefreshLayoutMessages.isRefreshing = false
            binding.rvMessages.isVisible = messages.isNotEmpty()
            binding.textViewErrorMessages.isVisible = messages.isEmpty()

            if (messages.isEmpty()) {
                binding.textViewErrorMessages.text = getString(R.string.no_messages_in_chat)
            } else {
                messageAdapter.submitList(messages.sortedBy { it.timestamp })
                binding.rvMessages.scrollToPosition(messages.size - 1)
            }
        }

        // Наблюдение за состоянием загрузки
        chatViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarMessages.isVisible = isLoading
            binding.swipeRefreshLayoutMessages.isEnabled = !isLoading
        }

        // Наблюдение за ошибками
        chatViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.textViewErrorMessages.isVisible = true
                binding.textViewErrorMessages.text = it
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()

                if (it.contains("401", ignoreCase = true) || it.contains("403", ignoreCase = true) ||
                    it.contains("Не авторизован", ignoreCase = true) || it.contains("Доступ запрещен", ignoreCase = true)
                ) {
                    handleAuthOrAccessError()
                }
            }
        }
    }

    private fun handleAuthOrAccessError() {
        Toast.makeText(context, R.string.session_expired, Toast.LENGTH_LONG).show()
        loginViewModel.clearAuthToken()
        chatViewModel.leaveChat(args.chatId)
        if (isAdded && findNavController().currentDestination?.id == R.id.chatFragment) {
            findNavController().popBackStack()
        }
    }

    private fun hideKeyboard() {
        requireActivity().currentFocus?.let { view ->
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        chatViewModel.leaveChat(args.chatId)
        super.onDestroyView()
        _binding = null
    }
}