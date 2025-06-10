package com.example.kasperchat_test.ui.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.ItemUserSearchBinding
import com.example.kasperchat_test.model.UserProfile

class UserSearchAdapter(
    private val onAddClick: (UserProfile) -> Unit
) : RecyclerView.Adapter<UserSearchAdapter.UserViewHolder>() {

    private var users: List<UserProfile> = emptyList()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newUsers: List<UserProfile>) {
        users = newUsers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserSearchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding, onAddClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    class UserViewHolder(
        private val binding: ItemUserSearchBinding,
        private val onAddClick: (UserProfile) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(user: UserProfile) {
            binding.textViewFullName.text = user.fullName
            binding.textViewLogin.text = "@${user.login}"
            binding.imageViewAvatar.load(user.avatarUrl) {
                placeholder(R.drawable.ic_avatar)
                error(R.drawable.ic_avatar)
            }
            binding.buttonAddUser.setOnClickListener { onAddClick(user) }
        }
    }
}