package com.example.kasperchat_test.ui.adapter.chatelement

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kasperchat_test.databinding.ItemChatBinding
import com.example.kasperchat_test.model.Chat
import com.example.kasperchat_test.model.Link
import com.example.kasperchat_test.model.UserProfile
import com.example.kasperchat_test.ui.adapter.ItemDiffCallback
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatAdapter(private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var chatList: List<Chat> = emptyList()
    private var links: List<Link> = emptyList()
    private var userProfiles: List<UserProfile> = emptyList()

    fun submitChatList(chatList: List<Chat>) {
        val diffCallbackChat = ItemDiffCallback(this.chatList, chatList)
        val diffResultChat = DiffUtil.calculateDiff(diffCallbackChat)
        this.chatList = chatList
        diffResultChat.dispatchUpdatesTo(this)
    }
    fun submitLinksList(links: List<Link>) {
        val diffCallback = ItemDiffCallback(this.links, links)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.links = links
        diffResult.dispatchUpdatesTo(this)
    }
    fun submitUserProfilesList(userProfiles: List<UserProfile>) {
        val diffCallback = ItemDiffCallback(this.userProfiles, userProfiles)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.userProfiles = userProfiles
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentChat = chatList[position]
        val linkedUserIds = links.filter { it.chatId == currentChat.id }.map { it.userId }
        val linkedUserProfiles = userProfiles.filter { userProfile ->
            linkedUserIds.contains(userProfile.id)
        }

        holder.bind(currentChat, linkedUserProfiles, clickListener)
    }

    override fun getItemCount(): Int = chatList.size

    class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(chatItem: Chat, linkedUserProfiles: List<UserProfile>, clickListener: OnItemClickListener) {
            with(binding) {
                textViewChatName.text = if (linkedUserProfiles.size == 1) linkedUserProfiles.first().fullName else "Групповой чат"
                textViewLastMessage.text = "Последние сообщение" //TODO Реализовать получения последнего сообщения
                lastMessageTime.text = "12:00" //TODO Реализовать получение времени последнего сообщения
                unreadMessagesCount.text = "0" //TODO Реализовать подсчет непрочитанных сообщений
                /*if (chatItem.image != null) {
                    //TODO Загрузка аватара с помощью Glide или другой библиотеки
                }*/
                linearLayout.setOnClickListener {
                    Log.d("TEST", "CLICK!!")
                    clickListener.onItemClick(chatItem)
                }

            }
        }
        private fun formatTimestamp(timestamp: Long): String {
            val instant = Instant.ofEpochMilli(timestamp)
            val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())
            val now = LocalDateTime.now()

            return if (localDateTime.toLocalDate() == now.toLocalDate()) {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                localDateTime.format(formatter)
            } else {
                val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
                localDateTime.format(formatter)
            }
        }
    }
    interface OnItemClickListener {
        fun onItemClick(chatItem: Chat)
    }
}