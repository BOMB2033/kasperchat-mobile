package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatBinding
import com.example.kasperchat_test.fillMessageItemsWithTestData
import com.example.kasperchat_test.ui.adapter.message.MessageItem
import com.example.kasperchat_test.ui.adapter.message.MessagesAdapter

private const val ARG_CHAT_ID = "chatId"

class ChatFragment : Fragment(), MessagesAdapter.OnItemClickListener, MessagesAdapter.OnItemLongClickListener {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private var chatId: String? = null
    private lateinit var messagesAdapter: MessagesAdapter
    private val messagesItems = mutableListOf<MessageItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatId = requireArguments().getString(ARG_CHAT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            rvMessages.layoutManager = LinearLayoutManager(requireContext())

            fillMessageItemsWithTestData(messagesItems)
            messagesAdapter = MessagesAdapter(this@ChatFragment, this@ChatFragment)
            messagesAdapter.submitList(messagesItems)
            rvMessages.adapter = messagesAdapter

            toolbarInclude.buttonExit.setOnClickListener {
                findNavController().navigate(R.id.action_chatFragment_to_chatsListFragment)
            }
            toolbarInclude.buttonOptions.setOnClickListener {
                findNavController().navigate(R.id.action_chatFragment_to_options_chat_group)

            }

        }
    }

    override fun onItemClick(chatItem: MessageItem) {
        // Логика клика
    }

    override fun onEditMessage(position: Int, message: MessageItem) {
        // Логика редактирования
    }

    override fun onDeleteMessage(position: Int) {
        messagesAdapter.removeItem(position)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(chatId: String) =
            ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CHAT_ID, chatId)
                }
            }
    }
}