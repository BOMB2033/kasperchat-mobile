package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kasperchat_test.databinding.FragmentCreateChatBinding
import com.example.kasperchat_test.viewmodel.ChatsListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CreateChatFragment : Fragment() {

    private var _binding: FragmentCreateChatBinding? = null
    private val binding get() = _binding!!
    private val chatsListViewModel: ChatsListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonCreateChat.setOnClickListener {
            // Логика создания чата
            val chatName = binding.editTextChatName.text.toString()
            chatsListViewModel.createChat(chatName, true)
            observeViewModel()
        }
    }

    private fun observeViewModel() {
        // Наблюдаем за состоянием загрузки
        chatsListViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // Опционально: показать индикатор загрузки
                 binding.progressBar.visibility = View.VISIBLE
                 binding.buttonCreateChat.isEnabled = false // Блокируем кнопку на время загрузки
            } else {
                // Опционально: скрыть индикатор загрузки
                 binding.progressBar.visibility = View.GONE
                 binding.buttonCreateChat.isEnabled = true
            }
        }

        chatsListViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
            }
        }

        chatsListViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (!isLoading && chatsListViewModel.error.value == null
                && chatsListViewModel.chatCreationSuccessEvent.value == true ) {
                findNavController().popBackStack()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}