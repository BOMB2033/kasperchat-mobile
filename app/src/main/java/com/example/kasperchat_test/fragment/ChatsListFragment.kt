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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatsListBinding
import com.example.kasperchat_test.model.Chat // Ваша модель данных
import com.example.kasperchat_test.ui.adapter.chatelement.ChatAdapter
import com.example.kasperchat_test.viewmodel.ChatsResult
import com.example.kasperchat_test.viewmodel.ChatsViewModel
import com.example.kasperchat_test.viewmodel.LinksResult
import com.example.kasperchat_test.viewmodel.LoginViewModel // Для выхода из системы
import com.example.kasperchat_test.viewmodel.UsersProfileResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChatsListFragment : Fragment() {
    private var _binding: FragmentChatsListBinding? = null
    private val binding get() = _binding!!

    private val chatsViewModel: ChatsViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels() // Для возможности разлогиниться

    private lateinit var chatAdapter: ChatAdapter
    // private val chatItems = mutableListOf<ChatItem>() // Старый список, будем использовать данные из ViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupUI()
        observeViewModel()

        // Загружаем список чатов при создании View
        chatsViewModel.fetchLinks()
        chatsViewModel.fetchUserProfiles()
        chatsViewModel.fetchChats()
    }

    private fun setupUI() {
        with(binding) {
            // Скрываем progressBar и текст ошибки по умолчанию
            progressBarChats.isVisible = false
            textViewErrorChats.isVisible = false

            // Кнопки навигации (остаются как были, если нужны)
            addChannelButton.setOnClickListener {
                // findNavController().navigate(R.id.action_chatsListFragment_to_options_chat_group)
                // TODO: Реализовать или обновить навигацию для создания чата/канала
                Toast.makeText(context, "Add channel/group clicked", Toast.LENGTH_SHORT).show()
            }

            optionsButton.setOnClickListener {
                // findNavController().navigate(R.id.action_chatsListFragment_to_preferensFragment)
                // TODO: Реализовать или обновить навигацию к настройкам
                // Пример: меню с опцией "Выйти"
                showLogoutOption() // Пример
            }

            // Обновление свайпом (если есть SwipeRefreshLayout)
            swipeRefreshLayoutChats.setOnRefreshListener {
                chatsViewModel.fetchUserProfiles()
                chatsViewModel.fetchChats()
            }
        }
    }

    private fun setupRecyclerView() {
        // Инициализация адаптера
        chatAdapter = ChatAdapter(object : ChatAdapter.OnItemClickListener {
            override fun onItemClick(chatItem: Chat) { // ChatItem нужно будет создать из Chats
                // Переход к конкретному чату
                val action = ChatsListFragmentDirections.actionChatsListFragmentToChatFragment(chatItem.id)
                findNavController().navigate(action)
            }
        })

        binding.rvChats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun observeViewModel() {
        chatsViewModel.linksResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is LinksResult.Loading -> {
                    Log.d("ChatsListFragment", "Загрузка связей чата пользователя")
                }

                is LinksResult.Success -> {
                    Log.i(
                        "ChatsListFragment",
                        "Связи загружены успешно. Размер списка: ${result.links.size}"
                    )
                    chatAdapter.submitLinksList(result.links)
                }

                is LinksResult.Error -> {
                    // Ошибка при загрузке пользователей
                    Log.e("ChatsListFragment", "Ошибка загрузки связей: ${result.message}")
                    binding.textViewErrorChats.text = result.message
                    binding.rvChats.isVisible = false
                    // Если ошибка "Unauthorized", возможно, нужно разлогинить пользователя
                    if (result.message.contains("Unauthorized", ignoreCase = true) ||
                        result.message.contains("401", ignoreCase = true)) {
                        handleUnauthorizedError()
                    }
                }
            }
        })
        chatsViewModel.usersProfileResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is UsersProfileResult.Loading -> {
                    Log.d("ChatsListFragment", "Загрузка пользователей для чатов")
                }

                is UsersProfileResult.Success -> {
                    Log.i(
                        "ChatsListFragment",
                        "Пользователи загружены успешно. Размер списка: ${result.userProfiles.size}"
                    )
                    chatAdapter.submitUserProfilesList(result.userProfiles)
                }

                is UsersProfileResult.Error -> {
                    // Ошибка при загрузке пользователей
                    Log.e("ChatsListFragment", "Ошибка загрузки пользователей: ${result.message}")
                    binding.textViewErrorChats.text = result.message
                    binding.rvChats.isVisible = false
                    // Если ошибка "Unauthorized", возможно, нужно разлогинить пользователя
                    if (result.message.contains("Unauthorized", ignoreCase = true) ||
                        result.message.contains("401", ignoreCase = true)) {
                        handleUnauthorizedError()
                    }
                }
            }
        })
        chatsViewModel.chatsResult.observe(viewLifecycleOwner, Observer { result ->
            binding.swipeRefreshLayoutChats.isRefreshing = false // Остановить анимацию свайпа
            binding.progressBarChats.isVisible = result is ChatsResult.Loading
            binding.textViewErrorChats.isVisible = result is ChatsResult.Error

            when (result) {
                is ChatsResult.Loading -> {
                    Log.d("ChatsListFragment", "Загрузка чатов")
                    binding.rvChats.isVisible = false // Скрываем список во время загрузки
                }
                is ChatsResult.Success -> {
                    Log.i("ChatsListFragment", "Чаты загружены успешно. Размер списка: ${result.chats.size}")
                    binding.rvChats.isVisible = true
                    if (result.chats.isEmpty()) {
                        binding.textViewErrorChats.isVisible = true
                        binding.textViewErrorChats.text = getString(R.string.no_chats_found) // Добавьте эту строку в strings.xml
                    } else {
                        // Преобразуем List<Chats> в List<ChatItem> для адаптера
                        val chatList = result.chats.map { chat ->
                            //Загружаем пользователей для каждого чата
                            // TODO Добавить загрузку пользователей для каждого чата


                            val calendar = Calendar.getInstance()
                            calendar.timeInMillis = chat.createdAt.time

                            val todayCalendar = Calendar.getInstance()

                            val formattedTimestamp = if (calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                                calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)) {
                                // Сегодняшний день - выводим время
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
                            } else {
                                // Другой день - выводим дату
                                SimpleDateFormat("dd.MM", Locale.getDefault()).format(calendar.time)
                            }

                            Chat(
                                id = chat.id,
                                chatName = chat.chatName,
                                createdAt = chat.createdAt,
                            )
                        }
                        chatAdapter.submitChatList(chatList) // Используйте submitList, если ваш адаптер поддерживает ListAdapter/AsyncListDiffer

                    }

                }
                is ChatsResult.Error -> {
                    Log.e("ChatsListFragment", "Ошибка загрузки чатов: ${result.message}")
                    binding.textViewErrorChats.text = result.message
                    binding.rvChats.isVisible = false
                    // Если ошибка "Unauthorized", возможно, нужно разлогинить пользователя
                    if (result.message.contains("Unauthorized", ignoreCase = true) ||
                        result.message.contains("401", ignoreCase = true)) {
                        handleUnauthorizedError()
                    }
                }
            }
        })
    }

    private fun handleUnauthorizedError() {
        Toast.makeText(context, "Сессия истекла. Пожалуйста, войдите снова.", Toast.LENGTH_LONG).show()
        loginViewModel.clearAuthToken() // Очищаем токен
        // Переход на экран логина
        // Убедимся, что мы все еще в этом фрагменте
        if (isAdded && findNavController().currentDestination?.id == R.id.chatsListFragment) {
            findNavController().navigate(R.id.action_chatsListFragment_to_loginFragment) // Убедитесь, что этот action существует
        }
    }

    private fun showLogoutOption() {
        // Лучше использовать AlertDialog или PopupMenu // TODO прислушаться к комментарию
        findNavController().navigate(R.id.action_chatsListFragment_to_preferensFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем ссылку на binding
    }
}