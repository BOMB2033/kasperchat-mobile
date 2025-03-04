package com.example.kasperchat_test.ui.adapter.message

import android.content.res.ColorStateList
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
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

enum class TypeBorderMessage {
    Lonely,
    First,
    Midl,
    Last
}

class MessagesAdapter(
    private val onItemClickListener: OnItemClickListener,
    private val onItemLongClickListener: OnItemLongClickListener
) : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messageItems: MutableList<MessageItem> = mutableListOf() // MutableList для изменения списка
    private var previousMessageItem: MessageItem? = null
    private var nextMessageItem: MessageItem? = null

    // Обновление списка сообщений с использованием DiffUtil
    fun submitList(newItems: List<MessageItem>) {
        val diffCallback = ItemDiffCallback(messageItems, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messageItems.clear()
        messageItems.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    // Метод для удаления сообщения
    fun removeItem(position: Int) {
        messageItems.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, messageItems.size) // Обновляем диапазон после удаления
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMessageBinding.inflate(inflater, parent, false)
        return MessageViewHolder(binding, onItemClickListener, onItemLongClickListener)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentItem = messageItems[position]

        nextMessageItem = if (position < messageItems.size - 1) {
            if (messageItems[position + 1].authorId == currentItem.authorId &&
                messageItems[position + 1].timestamp - currentItem.timestamp < 60000) {
                messageItems[position + 1]
            } else null
        } else null

        previousMessageItem = if (position != 0) {
            if (messageItems[position - 1].authorId == currentItem.authorId &&
                currentItem.timestamp - messageItems[position - 1].timestamp < 60000) {
                messageItems[position - 1]
            } else null
        } else null

        holder.bind(currentItem, previousMessageItem, nextMessageItem)
    }

    override fun getItemCount(): Int = messageItems.size

    // Интерфейс для кликов
    interface OnItemClickListener {
        fun onItemClick(chatItem: MessageItem)
    }

    // Интерфейс для долгого нажатия
    interface OnItemLongClickListener {
        fun onEditMessage(position: Int, message: MessageItem)
        fun onDeleteMessage(position: Int)
    }

    class MessageViewHolder(
        private val binding: ItemMessageBinding,
        private val clickListener: OnItemClickListener,
        private val longClickListener: OnItemLongClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(currentMessageItem: MessageItem, previousMessageItem: MessageItem?, nextMessageItem: MessageItem?) {
            with(binding) {
                messageText.text = currentMessageItem.text
                messageText.post {
                    linearLayoutMessageTime.orientation =
                        if (messageText.lineCount == 1) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                }
                timestamp.text = formatTimestamp(currentMessageItem.timestamp)

                // Обработка короткого клика
                root.setOnClickListener {
                    clickListener.onItemClick(currentMessageItem)
                }

                // Обработка долгого нажатия
                root.setOnLongClickListener {
                    showPopupMenu(it, adapterPosition, currentMessageItem)
                    true
                }

                val isMyMessage = currentMessageItem.authorId == (itemView.context.applicationContext as SocketManagerInterface).socketManager.myUserId
                root.gravity = if (isMyMessage) Gravity.END else Gravity.START

                val typeBorderMessage = when {
                    previousMessageItem == null && nextMessageItem != null -> TypeBorderMessage.First
                    previousMessageItem != null && nextMessageItem != null -> TypeBorderMessage.Midl
                    previousMessageItem != null && nextMessageItem == null -> TypeBorderMessage.Last
                    else -> TypeBorderMessage.Lonely
                }
                avatar.visibility = if (typeBorderMessage == TypeBorderMessage.Midl || typeBorderMessage == TypeBorderMessage.Last || isMyMessage) View.GONE else View.VISIBLE

                val color = root.context.getColor(if (isMyMessage) R.color.color_item_message_send else R.color.color_item_message_incoming)
                messageContainer.backgroundTintList = ColorStateList.valueOf(color)

                val timestampColor = root.context.getColor(if (isMyMessage) R.color.color_timestamp_send else R.color.color_timestamp_incoming)
                timestamp.setTextColor(timestampColor)

                messageContainer.setBackgroundResource(when (typeBorderMessage) {
                    TypeBorderMessage.Lonely ->
                        if (!isMyMessage) R.drawable.item_message_shape_lonely else R.drawable.item_message_shape_my_lonely
                    TypeBorderMessage.First ->
                        if (!isMyMessage) R.drawable.item_message_shape_first else R.drawable.item_message_shape_my_first
                    TypeBorderMessage.Midl ->
                        if (!isMyMessage) R.drawable.item_message_shape_midl else R.drawable.item_message_shape_my_midl
                    TypeBorderMessage.Last ->
                        if (!isMyMessage) R.drawable.item_message_shape_last else R.drawable.item_message_shape_my_last
                })

                val marginLayoutParams = messageContainer.layoutParams as ViewGroup.MarginLayoutParams
                marginLayoutParams.marginStart = when (typeBorderMessage) {
                    TypeBorderMessage.Lonely -> root.context.resources.getDimensionPixelSize(R.dimen.message_no_indent)
                    TypeBorderMessage.First -> root.context.resources.getDimensionPixelSize(R.dimen.message_no_indent)
                    TypeBorderMessage.Midl -> root.context.resources.getDimensionPixelSize(R.dimen.message_indent)
                    TypeBorderMessage.Last -> root.context.resources.getDimensionPixelSize(R.dimen.message_indent)
                }
                messageContainer.layoutParams = marginLayoutParams

                val marginRootLayoutParams = mainContainer.layoutParams as ViewGroup.MarginLayoutParams
                marginRootLayoutParams.setMargins(0, when (typeBorderMessage) {
                    TypeBorderMessage.Lonely -> root.context.resources.getDimensionPixelSize(R.dimen.message_margin_large)
                    TypeBorderMessage.First -> root.context.resources.getDimensionPixelSize(R.dimen.message_margin_large)
                    TypeBorderMessage.Midl -> root.context.resources.getDimensionPixelSize(R.dimen.message_margin_small)
                    TypeBorderMessage.Last -> root.context.resources.getDimensionPixelSize(R.dimen.message_margin_small)
                }, 0, 0)
                mainContainer.layoutParams = marginRootLayoutParams

                Log.d("MessageViewHolder", "$isMyMessage $typeBorderMessage => ${currentMessageItem.text}")
            }
        }

        // Метод для показа всплывающего меню
        private fun showPopupMenu(view: View, position: Int, message: MessageItem) {
            val popupMenu = PopupMenu(view.context, view)
            popupMenu.menuInflater.inflate(R.menu.message_options_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_edit -> {
                        longClickListener.onEditMessage(position, message)
                        true
                    }
                    R.id.action_delete -> {
                        longClickListener.onDeleteMessage(position)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
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