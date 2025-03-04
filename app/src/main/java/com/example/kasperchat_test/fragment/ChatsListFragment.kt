package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatsListBinding
import com.example.kasperchat_test.ui.adapter.chatelement.ChatAdapter
import com.example.kasperchat_test.ui.adapter.chatelement.ChatItem
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException


class ChatsListFragment : Fragment() {
    private lateinit var binding: FragmentChatsListBinding
    private lateinit var chatAdapter: ChatAdapter
    private val chatItems = mutableListOf<ChatItem>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsListBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            rvChats.layoutManager = LinearLayoutManager(requireContext())
            // Инициализация данных
            chatItems.add(ChatItem("0000001",
                "Я подключился, ща разберусь в управлении",
                stringToTimestamp("27.10.2023 10:30"),
                "Fedor Kasper",
                1
                ))
            chatItems.add(ChatItem("0000002",
                "Привет как дела?",
                stringToTimestamp("13.12.2024 14:33"),
                "Yozhik Ron",
                4
            ))
addChannelButton.setOnClickListener {
    it.findNavController().navigate(R.id.action_chatsListFragment_to_options_chat_group)
}

optionsButton.setOnClickListener {
    it.findNavController().navigate(R.id.action_chatsListFragment_to_preferensFragment)
}
            chatAdapter = ChatAdapter(object : ChatAdapter.OnItemClickListener{
                override fun onItemClick(chatItem: ChatItem) {
                    val action = ChatsListFragmentDirections.actionChatsListFragmentToChatFragment(chatItem.chatId)
                    view.findNavController().navigate(action)
                }

            })
            chatAdapter.submitList(chatItems)
            binding.rvChats.adapter = chatAdapter
        }
    }

    private fun stringToTimestamp(dateTimeString: String, pattern: String = "dd.MM.yyyy HH:mm"): Long {
        return try {
            val formatter = DateTimeFormatter.ofPattern(pattern)
            val localDateTime = LocalDateTime.parse(dateTimeString, formatter)
            val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
            zonedDateTime.toInstant().toEpochMilli()
        } catch (e: DateTimeParseException) {
            println("Error parsing date and time: ${e.message}")
            System.currentTimeMillis()
        }
    }
}