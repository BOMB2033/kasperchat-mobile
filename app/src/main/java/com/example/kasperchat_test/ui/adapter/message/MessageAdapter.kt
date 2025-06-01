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
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.ui.adapter.ItemDiffCallback
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

enum class TypeBorderMessage {
    Lonely,
    First,
    Midl,
    Last
}

class MessageListAdapter(val chatId: Int,
                         private var currentUserId: Int,
                         private val onItemClickListener: OnItemClickListener,
                         private val onItemLongClickListener: OnItemLongClickListener
) : RecyclerView.Adapter<MessageListAdapter.MessageViewHolder>() {

    private var messages: MutableList<Message> = mutableListOf() // MutableList для изменения списка
    private var previousMessage: Message? = null
    private var nextMessage: Message? = null

    // Обновление списка сообщений с использованием DiffUtil
    fun submitList(newItems: List<Message>) {
        val diffCallback = ItemDiffCallback(messages, newItems)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        messages.clear()
        messages.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }

    // Метод для удаления сообщения
    fun removeItem(position: Int) {
        messages.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, messages.size) // Обновляем диапазон после удаления
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemMessageBinding.inflate(inflater, parent, false)
        return MessageViewHolder(binding, currentUserId, onItemClickListener, onItemLongClickListener)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val currentItem = messages[position]

        nextMessage = if (position < messages.size - 1) {
            if (messages[position + 1].authorId == currentItem.authorId &&
                messages[position + 1].timestamp - currentItem.timestamp < 60000) {
                messages[position + 1]
            } else null
        } else null

        previousMessage = if (position != 0) {
            if (messages[position - 1].authorId == currentItem.authorId &&
                currentItem.timestamp - messages[position - 1].timestamp < 60000) {
                messages[position - 1]
            } else null
        } else null

        holder.bind(currentItem, previousMessage, nextMessage)
    }

    override fun getItemCount(): Int = messages.size
    fun setCurrentUserId(currentUserId: Int) {
        this.currentUserId = currentUserId
    }

    // Интерфейс для кликов
    interface OnItemClickListener {
        fun onItemClick(chatItem: Message)
    }

    // Интерфейс для долгого нажатия
    interface OnItemLongClickListener {
        fun onEditMessage(position: Int, message: Message)
        fun onDeleteMessage(position: Int)
    }

    class MessageViewHolder(
        private val binding: ItemMessageBinding,
        private val currentUserId: Int,
        private val clickListener: OnItemClickListener,
        private val longClickListener: OnItemLongClickListener
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(currentMessage: Message, previousMessage: Message?, nextMessage: Message?) {
            with(binding) {
                messageText.text = currentMessage.content
                messageText.post {
                    linearLayoutMessageTime.orientation =
                        if (messageText.lineCount == 1) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                }
                timestamp.text = formatTimestamp(currentMessage.timestamp)

                // Обработка короткого клика
                root.setOnClickListener {
                    clickListener.onItemClick(currentMessage)
                }

                // Обработка долгого нажатия 
                root.setOnLongClickListener {
                    showPopupMenu(it, adapterPosition, currentMessage)
                    true
                }


                val isMyMessage = currentMessage.authorId == currentUserId
                root.gravity = if (isMyMessage) Gravity.END else Gravity.START

                val typeBorderMessage = when {
                    previousMessage == null && nextMessage != null -> TypeBorderMessage.First
                    previousMessage != null && nextMessage != null -> TypeBorderMessage.Midl
                    previousMessage != null && nextMessage == null -> TypeBorderMessage.Last
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

                Log.d("MessageViewHolder", "$isMyMessage $typeBorderMessage => ${currentMessage.content}")
            }
        }

        // Метод для показа всплывающего меню
        private fun showPopupMenu(view: View, position: Int, message: Message) {
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

        private fun formatTimestamp(timestamp: Date): String {
            val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp.time), ZoneId.systemDefault())
            val now = LocalDateTime.now()

            return if (localDateTime.toLocalDate() == now.toLocalDate()) {
                val formatter = DateTimeFormatter.ofPattern("HH:mm")
                localDateTime.format(formatter)
            } else {
                val formatter = DateTimeFormatter.ofPattern("dd.MM")
                localDateTime.format(formatter)
            }
        }
    }
}

/**
 * Сравнивает две даты.
 *
 * @param other другая дата для сравнения.
 * @return отрицательное значение, если эта дата раньше другой,
 * положительное значение, если эта дата позже другой,
 * и ноль, если даты равны.
 */
private operator fun Date.compareTo(other: Date): Int {
    return this.time.compareTo(other.time)
}

private operator fun Date.minus(timestamp: Date): Long {
    return this.time - timestamp.time
}
