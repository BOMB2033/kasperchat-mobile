package com.example.kasperchat_test.ui.adapter.chatelement

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.ItemChatBinding
import com.example.kasperchat_test.model.DisplayableChatItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatItem = getItem(position)
        with(holder.binding) {
            textViewChatName.text = chatItem.chat.name
            if (chatItem.lastMessageText == null) {
                textViewLastMessage.text = "Нет сообщений"
                lastMessageTime.visibility = View.GONE
            } else {
                if (chatItem.authorName == null)
                    textViewLastMessage.text = "Вы: " + chatItem.lastMessageText
                else
                    if (chatItem.chat.isGroup) {
                        textViewLastMessage.text = chatItem.authorName + ": " + chatItem.lastMessageText
                    }else {
                        textViewLastMessage.text = chatItem.lastMessageText
                    }
                lastMessageTime.text = formatTimestamp(chatItem.lastMessageTimestamp)
                lastMessageTime.visibility = View.VISIBLE
            }

            if (chatItem.unreadMessagesCount == 0) {
                unreadMessagesCount.visibility = View.INVISIBLE
            } else {
                unreadMessagesCount.visibility = View.VISIBLE
                unreadMessagesCount.text = chatItem.unreadMessagesCount.toString()
            }
            if (chatItem.chat.isGroup)
                statusImageView.visibility = View.INVISIBLE
            else
                statusImageView.visibility = View.VISIBLE

            if (chatItem.chat.avatarUrl != null) {
                avatarImageView.load(chatItem.chat.avatarUrl) {
                    placeholder(R.drawable.ic_avatar)
                    error(R.drawable.ic_avatar)
                }
            }

            root.setOnClickListener { clickListener.onItemClick(chatItem) }
        }
    }

    private fun formatTimestamp(timestamp: Date?): String {
        if (timestamp == null) return ""

        val messageDate = timestamp
        val currentDate = Date()

        val calendarMessage = Calendar.getInstance().apply { time = messageDate }
        val calendarCurrent = Calendar.getInstance().apply { time = currentDate }

        val diffInMillis = currentDate.time - messageDate.time
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)

        return when {
            // Сегодня
            calendarMessage.get(Calendar.YEAR) == calendarCurrent.get(Calendar.YEAR) &&
                    calendarMessage.get(Calendar.DAY_OF_YEAR) == calendarCurrent.get(Calendar.DAY_OF_YEAR) -> {
                SimpleDateFormat("HH:mm", Locale.getDefault()).format(messageDate)
            }
            // Вчера
            calendarMessage.get(Calendar.YEAR) == calendarCurrent.get(Calendar.YEAR) &&
                    calendarMessage.get(Calendar.DAY_OF_YEAR) == calendarCurrent.get(Calendar.DAY_OF_YEAR) - 1 -> {
                "Вчера"
            }
            // Раньше недели
            diffInDays < 7 -> SimpleDateFormat("EEEE", Locale("ru")).format(messageDate) // День недели на русском
            // Больше недели
            else -> SimpleDateFormat("dd MMM", Locale("ru")).format(messageDate) // Число и месяц на русском
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