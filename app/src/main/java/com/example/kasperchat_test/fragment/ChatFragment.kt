package com.example.kasperchat_test.fragment

import com.example.kasperchat_test.ui.adapter.message.MessagesAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.databinding.FragmentChatBinding
import com.example.kasperchat_test.fillMessageItemsWithTestData
import com.example.kasperchat_test.ui.adapter.message.MessageItem

private const val ARG_CHAT_ID = "chatId"

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private var chatId: String? = null
    private lateinit var messagesAdapter: MessagesAdapter
    private val messagesItems = mutableListOf<MessageItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chatId = arguments?.getString(ARG_CHAT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            rvMessages.layoutManager = LinearLayoutManager(requireContext())

            fillMessageItemsWithTestData(messagesItems)
            messagesAdapter = MessagesAdapter(object : MessagesAdapter.OnItemClickListener{
                override fun onItemClick(chatItem: MessageItem) {

                }

            })
            messagesAdapter.submitList(messagesItems)
            rvMessages.adapter = messagesAdapter
        }
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