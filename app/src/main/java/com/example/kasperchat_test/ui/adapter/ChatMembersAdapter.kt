package com.example.kasperchat_test.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.ItemMemberBinding
import com.example.kasperchat_test.model.UserProfile

class ChatMembersAdapter(
    private val onRemoveClick: (UserProfile) -> Unit
) : ListAdapter<UserProfile, ChatMembersAdapter.MemberViewHolder>(MemberDiffCallback()) {

    private var isCreatorMode = false
    private var currentUserId: String? = null

    // Метод для установки режима (создатель или нет)
    @SuppressLint("NotifyDataSetChanged")
    fun setCreatorMode(isCreator: Boolean, userId: String?) {
        isCreatorMode = isCreator
        currentUserId = userId
        notifyDataSetChanged() // Перерисовать список с новыми правами
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        // Передаем флаг в ViewHolder
        return MemberViewHolder(binding, onRemoveClick)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        // Передаем флаг и ID текущего юзера в bind
        holder.bind(getItem(position), isCreatorMode, currentUserId)
    }

    class MemberViewHolder(
        private val binding: ItemMemberBinding,
        private val onRemoveClick: (UserProfile) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        // Bind теперь принимает дополнительные параметры
        fun bind(user: UserProfile, isCreatorMode: Boolean, currentUserId: String?) {
            binding.textViewFullName.text = user.fullName ?: ""
            binding.textViewLogin.text = user.login
            binding.imageViewAvatar.load(user.avatarUrl) {
                placeholder(R.drawable.ic_avatar)
                error(R.drawable.ic_avatar)
            }

            // Кнопка удаления видна, если:
            // 1. Текущий пользователь - создатель чата.
            // 2. Элемент списка - не сам создатель.
            val canBeRemoved = isCreatorMode && user.id != currentUserId
            binding.buttonRemove.isVisible = canBeRemoved

            binding.buttonRemove.setOnClickListener {
                onRemoveClick(user)
            }
        }
    }

    class MemberDiffCallback : DiffUtil.ItemCallback<UserProfile>() {
        override fun areItemsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserProfile, newItem: UserProfile): Boolean {
            return oldItem == newItem
        }
    }
}