package com.example.kasperchat_test.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kasperchat_test.R


private const val NICKNAME = "nickname"

class ListChatsFragment : Fragment() {
    private var nickname: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            nickname = it.getString(NICKNAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_chats, container, false)
    }

    companion object {
        @JvmStatic
        fun setNickname(nickname: String) : Bundle = Bundle().apply {
            putString(NICKNAME, nickname)
        }

    }
}