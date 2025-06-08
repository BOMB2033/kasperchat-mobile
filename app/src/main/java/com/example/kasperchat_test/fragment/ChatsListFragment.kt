package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatsListBinding
import com.example.kasperchat_test.model.DisplayableChatItem
import com.example.kasperchat_test.ui.adapter.chatelement.ChatAdapter
import com.example.kasperchat_test.viewmodel.ChatsListViewModel
import com.example.kasperchat_test.viewmodel.LoginViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatsListFragment : Fragment() {

    private var _binding: FragmentChatsListBinding? = null
    private val binding get() = _binding!!

    private val chatsViewModel: ChatsListViewModel by viewModels()
    private val loginViewModel: LoginViewModel by viewModels()

    private lateinit var chatAdapter: ChatAdapter

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
        chatsViewModel.fetchChats() // Загружаем чаты при старте
    }

    private fun setupUI() {
        with(binding) {
            // Инициализация UI
            progressBarChats.isVisible = false
            textViewErrorChats.isVisible = false
            swipeRefreshLayoutChats.isEnabled = true

            // Кнопка создания чата
            addChannelButton.setOnClickListener {
                val action = ChatsListFragmentDirections.actionChatsListFragmentToCreateChatFragment()
                findNavController().navigate(action)
            }

            // Кнопка настроек
            optionsButton.setOnClickListener {
                findNavController().navigate(R.id.action_chatsListFragment_to_preferensFragment)
            }

            // Обновление списка по свайпу
            swipeRefreshLayoutChats.setOnRefreshListener {
                chatsViewModel.fetchChats()
            }
        }
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(
            clickListener = object : ChatAdapter.OnItemClickListener {
                override fun onItemClick(chatItem: DisplayableChatItem) {
                    chatItem.chat.id?.let { chatId ->
                        val action = ChatsListFragmentDirections.actionChatsListFragmentToChatFragment(chatId)
                        findNavController().navigate(action)
                    }
                }
            }
        )
        binding.rvChats.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter
        }
    }

    private fun observeViewModel() {
        // Наблюдение за списком чатов
        chatsViewModel.chats.observe(viewLifecycleOwner) { chats ->
            binding.swipeRefreshLayoutChats.isRefreshing = false
            binding.rvChats.isVisible = chats.isNotEmpty()
            binding.textViewErrorChats.isVisible = chats.isEmpty()

            if (chats.isEmpty()) {
                binding.textViewErrorChats.text = getString(R.string.no_chats_found)
            } else {
                chatAdapter.submitList(chats)
            }
        }

        // Наблюдение за состоянием загрузки
        chatsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarChats.isVisible = isLoading
            binding.swipeRefreshLayoutChats.isEnabled = !isLoading
        }

        // Наблюдение за ошибками
        chatsViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.textViewErrorChats.isVisible = true
                binding.textViewErrorChats.text = it
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()

                if (it.contains("401", ignoreCase = true) || it.contains("Не авторизован", ignoreCase = true)) {
                    handleUnauthorizedError()
                }
            }
        }
    }

    private fun handleUnauthorizedError() {
        Toast.makeText(context, R.string.session_expired, Toast.LENGTH_LONG).show()
        loginViewModel.clearAuthToken()
        if (isAdded && findNavController().currentDestination?.id == R.id.chatsListFragment) {
            findNavController().navigate(R.id.action_chatsListFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}