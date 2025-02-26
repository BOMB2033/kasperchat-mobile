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
import com.example.kasperchat_test.ui.adapter.message.MessageItem
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MessagesAdapter(private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>() {

    private var messageItems: List<MessageItem> = emptyList()
    private var previousMessageItem: MessageItem? = null

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
        holder.bind(currentItem, previousMessageItem, onItemClickListener)
        previousMessageItem = currentItem
    }

    override fun getItemCount(): Int = messageItems.size

    interface OnItemClickListener {
        fun onItemClick(chatItem: MessageItem)
    }

    class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(messageItem: MessageItem, previousMessageItem: MessageItem?, clickListener: OnItemClickListener) {
            with(binding) {
                messageText.text = messageItem.text

                messageText.post {
                    linearLayoutMessageTime.orientation =
                        if (messageText.lineCount == 1) LinearLayout.HORIZONTAL else LinearLayout.VERTICAL
                }
                timestamp.text = formatTimestamp(messageItem.timestamp)
                root.setOnClickListener {
                    clickListener.onItemClick(messageItem)
                }
                val isMyMessage = messageItem.authorId == (itemView.context.applicationContext as SocketManagerInterface).socketManager.myUserId


                val isSameAuthorAndWithinMinute = previousMessageItem != null &&
                        previousMessageItem.authorId == messageItem.authorId &&
                        messageItem.timestamp - previousMessageItem.timestamp < 60000

                root.gravity = if (isMyMessage) Gravity.END else Gravity.START

                messageContainer.background = if (isSameAuthorAndWithinMinute)
                     ContextCompat.getDrawable(root.context, R.drawable.item_message_shape_without_tail)
                 else
                    ContextCompat.getDrawable(root.context, R.drawable.item_message_shape)

                avatar.visibility = if (isSameAuthorAndWithinMinute || isMyMessage) View.GONE else View.VISIBLE

                val layoutParams = root.layoutParams as ViewGroup.MarginLayoutParams

                layoutParams.topMargin = if (isSameAuthorAndWithinMinute)
                     root.context.resources.getDimensionPixelSize(R.dimen.message_margin_small)
                 else
                    root.context.resources.getDimensionPixelSize(R.dimen.message_margin_large)

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