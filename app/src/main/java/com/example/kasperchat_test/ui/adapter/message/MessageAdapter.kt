package com.example.kasperchat_test.ui.adapter.message

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.ItemMessageBinding
import com.example.kasperchat_test.network.SocketManagerInterface
import com.example.kasperchat_test.ui.adapter.ItemDiffCallback
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

enum class TypeBorderMessage{
    Lonely,
    First,
    Midl,
    Last
}

class MessagesAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messageItems: List<MessageItem> = emptyList()
    private var previousMessageItem: MessageItem? = null
    private var nextMessageItem: MessageItem? = null

    fun submitList(newItems: List<MessageItem>) {
        val diffCallback = ItemDiffCallback(messageItems, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messageItems = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMessageBinding.inflate(inflater, parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentItem = messageItems[position]

        nextMessageItem =
            if (position < messageItems.size - 1)
                if (messageItems[position + 1].authorId == currentItem.authorId)
                    if (messageItems[position + 1].timestamp - currentItem.timestamp < 60000)
                        messageItems[position + 1]
                    else
                        null
                else
                    null
            else
                null

        previousMessageItem =
            if (position != 0)
                if (messageItems[position - 1].authorId == currentItem.authorId)
                    if (currentItem.timestamp - messageItems[position - 1].timestamp < 60000)
                        messageItems[position - 1]
                    else
                        null
                else
                    null
            else
                null

        holder.bind(currentItem, previousMessageItem, nextMessageItem, onItemClickListener)
    }

    override fun getItemCount(): Int = messageItems.size

    interface OnItemClickListener {
        fun onItemClick(chatItem: MessageItem)
    }

    class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(currentMessageItem: MessageItem, previousMessageItem: MessageItem?, nextMessageItem: MessageItem?, clickListener: OnItemClickListener) {
            with(binding) {
                messageText.text = currentMessageItem.text
                messageText.post {
                    linearLayoutMessageTime.orientation =
                        if (messageText.lineCount == 1) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                }
                timestamp.text = formatTimestamp(currentMessageItem.timestamp)
                root.setOnClickListener {
                    clickListener.onItemClick(currentMessageItem)
                }

                val isMyMessage = currentMessageItem.authorId == (itemView.context.applicationContext as SocketManagerInterface).socketManager.myUserId
                root.gravity = if (isMyMessage) Gravity.END else Gravity.START

                val typeBorderMessage=  when{
                    previousMessageItem == null && nextMessageItem != null -> TypeBorderMessage.First
                    previousMessageItem != null && nextMessageItem != null -> TypeBorderMessage.Midl
                    previousMessageItem != null && nextMessageItem == null -> TypeBorderMessage.Last
                    else -> TypeBorderMessage.Lonely
                }
                avatar.visibility = if (typeBorderMessage == TypeBorderMessage.Lonely  || typeBorderMessage == TypeBorderMessage.First  || isMyMessage) View.GONE else View.VISIBLE

                messageContainer.background = ContextCompat.getDrawable(root.context, when(typeBorderMessage) {
                    TypeBorderMessage.Lonely -> R.drawable.item_message_shape_without_tail // TODO Сюда установить картинку одиночного сообщения
                    TypeBorderMessage.First -> R.drawable.item_message_shape_my_without_tail // TODO Сюда установить картинку первого сообщения
                    TypeBorderMessage.Midl -> R.drawable.item_message_shape_my_without_tail // TODO Сюда установить картинку среднего сообщения
                    TypeBorderMessage.Last -> R.drawable.item_message_shape_my_without_tail // TODO Сюда установить картинку последнего сообщения
                })

                val marginLayoutParams = messageContainer.layoutParams as ViewGroup.MarginLayoutParams
                marginLayoutParams.marginStart =
                    when(typeBorderMessage){
                        TypeBorderMessage.Lonely -> root.context.resources.getDimensionPixelSize(R.dimen.message_no_indent)
                        TypeBorderMessage.First -> root.context.resources.getDimensionPixelSize(R.dimen.message_no_indent)
                        TypeBorderMessage.Midl -> root.context.resources.getDimensionPixelSize(R.dimen.message_indent)
                        TypeBorderMessage.Last -> root.context.resources.getDimensionPixelSize(R.dimen.message_indent)
                    }
                messageContainer.layoutParams = marginLayoutParams

                val layoutParams = root.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.topMargin = when(typeBorderMessage){
                    TypeBorderMessage.Lonely -> R.dimen.message_margin_large
                    TypeBorderMessage.First -> R.dimen.message_margin_large
                    TypeBorderMessage.Midl -> R.dimen.message_margin_small
                    TypeBorderMessage.Last -> R.dimen.message_margin_small
                }
                root.layoutParams = layoutParams
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
}