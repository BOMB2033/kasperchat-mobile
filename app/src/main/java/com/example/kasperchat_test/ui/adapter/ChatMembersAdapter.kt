package com.example.kasperchat_test.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MemberViewHolder(binding, onRemoveClick)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MemberViewHolder(
        private val binding: ItemMemberBinding,
        private val onRemoveClick: (UserProfile) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserProfile) {
            binding.textViewFullName.text = user.fullName ?: ""
            binding.textViewLogin.text = user.login
            binding.imageViewAvatar.load(user.avatarUrl) {
                placeholder(R.drawable.ic_avatar)
                error(R.drawable.ic_avatar)
            }
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