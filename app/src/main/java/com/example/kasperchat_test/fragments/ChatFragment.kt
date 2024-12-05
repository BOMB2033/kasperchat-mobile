package com.example.kasperchat_test.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentChatBinding

private const val ARG_NICKNAME = "nickName"

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding
    private var nickname: String? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            nickname = if(it.getString(ARG_NICKNAME).isNullOrBlank()) "anonym" else it.getString(ARG_NICKNAME)
        }
        binding.textViewUsername.text = nickname
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun setNickName(nickName: String) : Bundle = Bundle().apply {
                    putString(ARG_NICKNAME, nickName)
                }

    }
}