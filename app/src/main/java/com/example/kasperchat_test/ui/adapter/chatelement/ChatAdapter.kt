package com.example.kasperchat_test.ui.adapter.chatelement

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kasperchat_test.databinding.ItemChatBinding
import com.example.kasperchat_test.model.DisplayableChatItem

class ChatAdapter(
    private val clickListener: OnItemClickListener
) : ListAdapter<DisplayableChatItem, ChatAdapter.ViewHolder>(ChatDiffCallback()) {

    interface OnItemClickListener {
        fun onItemClick(chatItem: DisplayableChatItem)
    }

    class ViewHolder(val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatItem = getItem(position)
        with(holder.binding) {
            textViewChatName.text = chatItem.chat.name
            lastMessageTime.text = chatItem.lastMessageText ?: "Нет сообщений"
            root.setOnClickListener { clickListener.onItemClick(chatItem) }
        }
    }
}

class ChatDiffCallback : DiffUtil.ItemCallback<DisplayableChatItem>() {
    override fun areItemsTheSame(oldItem: DisplayableChatItem, newItem: DisplayableChatItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DisplayableChatItem, newItem: DisplayableChatItem): Boolean {
        return oldItem == newItem
    }
}