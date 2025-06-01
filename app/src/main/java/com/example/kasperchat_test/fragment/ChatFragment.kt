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
import androidx.navigation.fragment.navArgs // Для получения аргументов навигации
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatBinding // Убедитесь, что у вас есть этот layout
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.ui.adapter.message.MessageListAdapter
import com.example.kasperchat_test.viewmodel.MessageViewModel
import com.example.kasperchat_test.viewmodel.MessagesResult
import com.example.kasperchat_test.viewmodel.LoginViewModel // Для обработки разлогина

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    // Получаем аргументы навигации (chatId)
    private val args: ChatFragmentArgs by navArgs()

    // ViewModel для сообщений
    private val messageViewModel: MessageViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels() // Для обработки разлогина

     private lateinit var messageAdapter: MessageListAdapter // Объявите ваш адаптер сообщений
     private var currentUserId: Int = -1 // Замените на реальный ID текущего пользователя (можно получить из LoginViewModel или SharedPreferences)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginViewModel.currentUserIdLiveData.observe(viewLifecycleOwner) { userId ->
            if (userId != null) {
                currentUserId = userId
                Log.d("ChatFragment", "Current User ID updated: $currentUserId")
                // Здесь можно обновить адаптер, если он зависит от ID пользователя
                 if (::messageAdapter.isInitialized) {
                     messageAdapter.setCurrentUserId(currentUserId)
                 }
            } else {
                currentUserId = -1 // Пользователь не определен или вышел
                Log.d("ChatFragment", "Current User ID is null.")
                // Возможно, здесь нужно обработать ситуацию, когда ID пользователя становится null
                // (например, если произошел автоматический выход из-за истечения токена)
            }
        }

        val chatId = args.chatId // Получаем chatId из аргументов
        Log.d("ChatFragment", "Displaying chat for chatId: $chatId")

        // Устанавливаем заголовок (если есть Toolbar в layout'е)
         binding.toolbarInclude.chatTitle.text = "Chat $chatId" // Пример
         binding.toolbarInclude.buttonExit.setOnClickListener { findNavController().popBackStack() }

        setupRecyclerView()
        setupUI(chatId)
        observeViewModel(chatId)

        // Можно вызвать fetchMessages здесь, если getMessagesForChat не делает это автоматически при первом запросе
         messageViewModel.fetchMessages(chatId) // Уже вызывается в getMessagesForChat, если LiveData для chatId еще нет
    }

    private fun setupUI(chatId: Int) {
        with(binding) {
            progressBarMessages.isVisible = false
            textViewErrorMessages.isVisible = false

            // Кнопка отправки сообщения (пример)
            buttonSendMessage.setOnClickListener {
                val messageText = editTextMessage.text.toString().trim()
                if (messageText.isNotEmpty()) {
                     messageViewModel.sendMessage(chatId, messageText, currentUserId)
                    editTextMessage.text.clear()
                }
            }

            // Обновление свайпом
            swipeRefreshLayoutMessages.setOnRefreshListener {
                messageViewModel.fetchMessages(chatId)
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
                     // Обработка долгого нажатия на редактирование сообщения
                 }
                 override fun onDeleteMessage(position: Int) {
                     // Обработка долгого нажатия на удаление сообщения
                 }
             }
         )
         binding.rvMessages.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true // Новые сообщения будут добавляться снизу и прокручиваться к ним
            }
            adapter = messageAdapter
         }
    }

    private fun observeViewModel(chatId: Int) {
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
                        binding.textViewErrorMessages.text = getString(R.string.no_messages_in_chat) // Добавьте в strings.xml
                    } else {
                         messageAdapter.submitList(result.messages)
                         binding.rvMessages.scrollToPosition(result.messages.size - 1) // Прокрутка к последнему сообщению

                    }
                }
                is MessagesResult.Error -> {
                    Log.e("ChatFragment", "Error loading messages for chatId $chatId: ${result.message}")
                    binding.textViewErrorMessages.text = result.message
                    binding.rvMessages.isVisible = false
                    if (result.message.contains("Не авторизован", ignoreCase = true) ||
                        result.message.contains("Доступ к этому чату запрещен", ignoreCase = true) ||
                        result.message.contains("401") || result.message.contains("403")) {
                        handleAuthOrAccessError()
                    }
                }
            }
        })
    }

    private fun handleAuthOrAccessError() {
        // Если ошибка авторизации или доступа, обычно возвращаемся назад или на экран логина
        Toast.makeText(context, "Access denied or session expired.", Toast.LENGTH_LONG).show()
        loginViewModel.clearAuthToken() // На всякий случай, если это 401
        // Безопасный переход назад
        if (isAdded && findNavController().currentDestination?.id == R.id.chatFragment) {
            findNavController().popBackStack() // Или на экран логина, если это 401
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}