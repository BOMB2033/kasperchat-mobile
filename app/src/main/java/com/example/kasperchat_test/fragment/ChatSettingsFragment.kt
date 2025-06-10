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
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatSettingsBinding
import com.example.kasperchat_test.ui.adapter.UserSearchAdapter
import com.example.kasperchat_test.viewmodel.ChatSettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.widget.doAfterTextChanged

@AndroidEntryPoint
class ChatSettingsFragment : Fragment() {

    private var _binding: FragmentChatSettingsBinding? = null
    private val binding get() = _binding!!

    private val args: ChatSettingsFragmentArgs by navArgs()
    private val chatViewModel: ChatSettingsViewModel by viewModels()

    private lateinit var searchAdapter: UserSearchAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews() // Новый метод
        setupUI()
        observeViewModel()

        chatViewModel.fetchChatDetails(args.chatId)
        // viewModel.fetchChatMembers(args.chatId) // TODO Вызываем загрузку участников
    }
    private fun setupRecyclerViews() {
        searchAdapter = UserSearchAdapter { user ->
            // Клик по кнопке "добавить"
            chatViewModel.addUserToChat(args.chatId, user)
            binding.editTextSearchUser.text?.clear() // Очищаем поле поиска
        }
        binding.recyclerViewSearchResults.adapter = searchAdapter

        // TODO Инициализация адаптера для участников
        // membersAdapter = ChatMembersAdapter()
        // binding.recyclerViewMembers.adapter = membersAdapter
    }
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonSaveChanges.setOnClickListener {
            val newName = binding.editTextChatName.text.toString().trim()
            if (newName.isEmpty()) {
                binding.textInputLayoutChatName.error = getString(R.string.error_field_cant_be_empty)
                return@setOnClickListener
            }
            binding.textInputLayoutChatName.error = null

            // TODO: Реализовать логику выбора и загрузки нового аватара.
            // Пока будем передавать текущий URL.
            val currentAvatarUrl = chatViewModel.chat.value?.avatarUrl

            chatViewModel.updateChat(args.chatId, newName, currentAvatarUrl)
        }

        binding.fabEditAvatar.setOnClickListener {
            // TODO: Открыть галерею или камеру для выбора нового изображения
            Toast.makeText(context, "Функция смены аватара в разработке", Toast.LENGTH_SHORT).show()
        }

        // Добавляем слушатель для поля поиска
        binding.editTextSearchUser.doAfterTextChanged { text ->
            chatViewModel.searchUsers(text.toString(), args.chatId)
        }
    }

    private fun observeViewModel() {
        chatViewModel.chat.observe(viewLifecycleOwner) { chat ->
            chat?.let {
                binding.editTextChatName.setText(it.name)
                // Используйте библиотеку для загрузки изображений, например, Coil
                binding.imageViewChatAvatar.load(it.avatarUrl) {
                    placeholder(R.drawable.ic_avatar)
                    error(R.drawable.ic_avatar)
                }
            }
        }

        chatViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.buttonSaveChanges.isEnabled = !isLoading
        }

        chatViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        chatViewModel.updateSuccessEvent.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { // Обрабатываем событие только один раз
                Toast.makeText(context, R.string.save_success, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack() // Возвращаемся на предыдущий экран
            }
        }

        chatViewModel.searchResults.observe(viewLifecycleOwner) { users ->
            binding.recyclerViewSearchResults.isVisible = users.isNotEmpty()
            searchAdapter.submitList(users)
        }

        // TODO Наблюдатель для списка участников
        // viewModel.members.observe(viewLifecycleOwner) { members ->
        //     membersAdapter.submitList(members)
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}