package com.example.kasperchat_test.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.ItemChatBinding

class ChatAdapter(
    private var chatItems: List<ChatItem>,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(chatItem: ChatItem)
    }class ChatViewHolder(private val binding: ItemChatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(chatItem: ChatItem, clickListener: OnItemClickListener) {
            binding.tvName.text = chatItem.senderName
            binding.tvLastMessage.text = chatItem.message
            binding.tvTime.text = chatItem.time
            binding.root.setOnClickListener { clickListener.onItemClick(chatItem) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemChatBinding.inflate(inflater, parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val currentItem = chatItems[position]
        holder.bind(currentItem, onItemClickListener)
    }

    override fun getItemCount(): Int = chatItems.size

    fun updateData(newChatItems: List<ChatItem>) {
        val diffResult = DiffUtil.calculateDiff(ChatDiffCallback(chatItems, newChatItems))
        chatItems = newChatItems
        diffResult.dispatchUpdatesTo(this)
    }
}
