package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentOptionsChatGroupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatSettingsFragment : Fragment() {

    private var _binding: FragmentOptionsChatGroupBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null
    private var chatId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Проверяем, есть ли аргументы, и получаем их, если есть
        if (arguments != null) {
            chatId = requireArguments().getString(ARG_CHAT_ID)
            param1 = requireArguments().getString(ARG_PARAM1)
            param2 = requireArguments().getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptionsChatGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            buttonAddMembers.setOnClickListener {
                findNavController().navigate(R.id.action_options_chat_group_to_add_members)
            }
            buttonRemoveMembers.setOnClickListener {
                findNavController().navigate(R.id.action_options_chat_group_to_remove_members)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_CHAT_ID = "chat_id"
        private const val ARG_PARAM1 = "param1"
        private const val ARG_PARAM2 = "param2"

        @JvmStatic
        fun newInstance(chatId: String?, param1: String?, param2: String?): ChatSettingsFragment {
            val fragment = ChatSettingsFragment()
            val args = Bundle().apply {
                putString(ARG_CHAT_ID, chatId)
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
            fragment.arguments = args
            return fragment
        }
    }
}