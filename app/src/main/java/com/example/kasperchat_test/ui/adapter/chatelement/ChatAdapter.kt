package com.example.kasperchat_test.ui.adapter.chatelement

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kasperchat_test.databinding.ItemChatBinding
import com.example.kasperchat_test.ui.adapter.ItemDiffCallback
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ChatAdapter(private val clickListener: OnItemClickListener) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    private var chatList: List<ChatItem> = emptyList()

    fun submitList(newList: List<ChatItem>) {
        val diffCallback = ItemDiffCallback(chatList, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        chatList = newList
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(chatList[position], clickListener)
    }

    override fun getItemCount(): Int = chatList.size

    class ChatViewHolder(private val binding: ItemChatBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(chatItem: ChatItem, clickListener: OnItemClickListener) {
            with(binding) {
                otherUserName.text = chatItem.otherUserName
                lastMessage.text = chatItem.lastMessage
                lastMessageTime.text = formatTimestamp(chatItem.lastMessageTimestamp)
                unreadMessagesCount.text = chatItem.unreadMessagesCount.toString()
                if (chatItem.otherUserAvatarUrl != null) {
                    //TODO Загрузка аватара с помощью Glide или другой библиотеки
                }
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
        fun onItemClick(chatItem: ChatItem)
    }
}