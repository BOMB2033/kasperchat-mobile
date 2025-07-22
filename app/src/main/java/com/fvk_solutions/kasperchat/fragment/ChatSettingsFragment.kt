package com.fvk_solutions.kasperchat.fragment

import android.content.ContentResolver
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import coil.load
import com.fvk_solutions.kasperchat.R
import com.fvk_solutions.kasperchat.databinding.FragmentChatSettingsBinding
import com.fvk_solutions.kasperchat.repository.UserRepository
import com.fvk_solutions.kasperchat.ui.adapter.ChatMembersAdapter
import com.fvk_solutions.kasperchat.ui.adapter.UserSearchAdapter
import com.fvk_solutions.kasperchat.viewmodel.ChatSettingsResult
import com.fvk_solutions.kasperchat.viewmodel.ChatSettingsViewModel
import com.fvk_solutions.kasperchat.viewmodel.UserProfileViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@AndroidEntryPoint
class ChatSettingsFragment : Fragment() {


    private var _binding: FragmentChatSettingsBinding? = null
    private val binding get() = _binding!!

    private val args: ChatSettingsFragmentArgs by navArgs()
    private val chatViewModel: ChatSettingsViewModel by viewModels()

    private val userProfileViewModel: UserProfileViewModel by activityViewModels()

    @Inject
    lateinit var userRepository: UserRepository // Внедряем репозиторий

    private lateinit var searchAdapter: UserSearchAdapter
    private lateinit var membersAdapter: ChatMembersAdapter
    private var isCurrentUserCreator: Boolean? = null// Флаг для хранения статуса


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupUI()
        observeViewModel()

        chatViewModel.fetchChatDetails(args.chatId)
        chatViewModel.fetchChatMembers(args.chatId)

        // Запрашиваем профиль, если он еще не загружен
        if (userProfileViewModel.userProfile.value == null) {
            userProfileViewModel.fetchCurrentUserProfile()
        }
    }
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            handleImageSelection(it)
        }
    }
    private fun handleImageSelection(uri: Uri) {
        val file = getFileFromUri(uri)
        file?.let {
            chatViewModel.uploadAndSaveChatAvatar(args.chatId, it)
        } ?: Toast.makeText(context, "Не удалось обработать выбранный файл", Toast.LENGTH_SHORT).show()
    }
    // Вспомогательная функция для копирования файла из Uri
    private fun getFileFromUri(uri: Uri): File? {
        val context = requireContext()
        val contentResolver: ContentResolver = context.contentResolver
        val fileName = getFileName(contentResolver, uri) ?: "temp_chat_avatar.jpg"
        val tempFile = File(context.cacheDir, fileName)
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return tempFile
        } catch (e: Exception) {
            Log.e("ChatSettingsFragment", "Failed to copy file from Uri", e)
            return null
        }
    }

    // Вспомогательная функция для получения имени файла
    private fun getFileName(resolver: ContentResolver, uri: Uri): String? {
        resolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    return cursor.getString(nameIndex)
                }
            }
        }
        return null
    }

    private fun setupRecyclerViews() {
        searchAdapter = UserSearchAdapter { user ->
            chatViewModel.addUserToChat(args.chatId, user)
            binding.editTextSearchUser.text?.clear()
            binding.groupAddMembers.isVisible = false // Скрываем поиск после добавления
        }
        binding.recyclerViewSearchResults.adapter = searchAdapter
        binding.recyclerViewSearchResults.layoutManager = LinearLayoutManager(context)

        membersAdapter = ChatMembersAdapter { user ->
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_deletion_title)
                .setMessage(getString(R.string.confirm_remove_user_message, user.fullName))
                .setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton(R.string.delete) { _, _ ->
                    chatViewModel.removeUserFromChat(args.chatId, user)
                }
                .show()
        }
        binding.recyclerViewMembers.adapter = membersAdapter
        binding.recyclerViewMembers.layoutManager = LinearLayoutManager(context)
    }
    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonAddMembers.setOnClickListener {
            // Показываем/скрываем блок поиска
            binding.groupAddMembers.isVisible = !binding.groupAddMembers.isVisible
        }

        binding.buttonSaveChanges.setOnClickListener {
            val newName = binding.editTextChatName.text.toString().trim()
            if (newName.isEmpty()) {
                binding.textInputLayoutChatName.error = getString(R.string.error_field_cant_be_empty)
                return@setOnClickListener
            }
            binding.textInputLayoutChatName.error = null
            // Теперь передаем только имя
            chatViewModel.updateChat(args.chatId, newName)
        }

        binding.fabEditAvatar.setOnClickListener {
            // Запускаем выбор изображения из галереи
            pickImageLauncher.launch("image/*")
        }


        binding.editTextSearchUser.doAfterTextChanged { text ->
            chatViewModel.searchUsers(text.toString(), args.chatId)
        }
        binding.buttonDelete.setOnClickListener {
            // Диалог подтверждения удаления чата
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.delete_chat))
                .setMessage(R.string.confirm_delete_chat_message) // Добавьте эту строку в strings.xml
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete) { _, _ ->
                    chatViewModel.deleteChat(args.chatId)
                }
                .show()
        }

        binding.buttonLeaveChat.setOnClickListener {
            val currentUserId = userRepository.userProfile.value?.id
            if (currentUserId != null) {
                // Диалог подтверждения выхода из чата
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.leave_chat))
                    .setMessage(R.string.confirm_leave_chat_message) // Добавьте эту строку
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.leave) { _, _ -> // Добавьте строку "leave"
                        chatViewModel.leaveChat(args.chatId, currentUserId)
                    }
                    .show()
            } else {
                Toast.makeText(context, "Не удалось определить пользователя", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUiForRole(isCreator: Boolean) {
        // Если новое значение не отличается от старого, ничего не делаем
        if (isCurrentUserCreator == isCreator) return

        isCurrentUserCreator = isCreator
        val currentUserId = userRepository.userProfile.value?.id

        // Управление элементами для создателя
        binding.buttonSaveChanges.isVisible = isCreator
        binding.buttonDelete.isVisible = isCreator
        binding.fabEditAvatar.isVisible = isCreator
        binding.textInputLayoutChatName.isEnabled = isCreator
        binding.buttonAddMembers.isVisible = isCreator
        if (!isCreator) {
            // Если не создатель, всегда скрываем блок поиска
            binding.groupAddMembers.isVisible = false
        }

        // Управление элементами для обычного участника
        binding.buttonLeaveChat.isVisible = !isCreator

        // Обновляем адаптер участников
        membersAdapter.setCreatorMode(isCreator, currentUserId)

        // После обновления видимости кнопок, нужно обновить их доступность
        // в соответствии с текущим состоянием загрузки
        val isLoading = chatViewModel.settingsResult.value is ChatSettingsResult.Loading
        setControlsEnabled(!isLoading)
    }



    private fun observeViewModel() {
        userProfileViewModel.userProfile.observe(viewLifecycleOwner) { userProfile ->
            // Когда профиль пользователя загружен, проверяем снова
            if (userProfile != null && chatViewModel.chat.value != null) {
                val chat = chatViewModel.chat.value!!
                updateUiForRole(chat.creatorId == userProfile.id)
            }
        }


        chatViewModel.chat.observe(viewLifecycleOwner) { chat ->
            chat?.let {
                binding.editTextChatName.setText(it.name)
                binding.imageViewChatAvatar.load(it.avatarUrl) {
                    placeholder(R.drawable.ic_avatar)
                    error(R.drawable.ic_avatar)
                }

                // Когда данные о чате загружены, проверяем, есть ли уже профиль
                userProfileViewModel.userProfile.value?.let { userProfile ->
                    updateUiForRole(it.creatorId == userProfile.id)
                }
            }
        }

        chatViewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }

        // Наблюдаем за новой LiveData для всех операций
        chatViewModel.settingsResult.observe(viewLifecycleOwner) { result ->
            // Управляем видимостью ProgressBar и доступностью кнопок
            val isLoading = result is ChatSettingsResult.Loading
            binding.progressBar.isVisible = isLoading
            setControlsEnabled(!isLoading)

            when (result) {
                is ChatSettingsResult.Success -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                    chatViewModel.onResultHandled() // Сбрасываем состояние
                }

                is ChatSettingsResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    chatViewModel.onResultHandled()
                }

                is ChatSettingsResult.NavigationBack -> {
                    // Возвращаемся на предыдущий экран после удаления или выхода
                    findNavController().popBackStack()
                    chatViewModel.onResultHandled()
                }

                is ChatSettingsResult.Loading -> { /* UI уже обновлен */
                }

                is ChatSettingsResult.Idle -> { /* Ничего не делаем */
                }
            }
        }
        chatViewModel.searchResults.observe(viewLifecycleOwner) { users ->
            binding.recyclerViewSearchResults.isVisible = users.isNotEmpty()
            searchAdapter.submitList(users)
        }

        chatViewModel.members.observe(viewLifecycleOwner) { members ->
            binding.textViewMembersTitle.text = getString(R.string.members_count, members.size)
            membersAdapter.submitList(members)
        }
    }
    private fun setControlsEnabled(isEnabled: Boolean) {
        val creatorRole =
            isCurrentUserCreator == true // Если роль не определена, считаем что не создатель

        Log.d("ChatSettingsFragment", "setControlsEnabled: isEnabled=$isEnabled, isCreator=$creatorRole")

        // Управление доступностью элементов
        binding.editTextChatName.isEnabled = isEnabled && creatorRole
        binding.buttonSaveChanges.isEnabled = isEnabled && creatorRole
        binding.buttonDelete.isEnabled = isEnabled && creatorRole
        binding.buttonLeaveChat.isEnabled = isEnabled && !creatorRole
        binding.buttonAddMembers.isEnabled = isEnabled && creatorRole
        binding.fabEditAvatar.isEnabled = isEnabled && creatorRole
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}