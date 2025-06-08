package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentOptionsChatGroupBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatSettingsFragment : Fragment() {

    private var _binding: FragmentOptionsChatGroupBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptionsChatGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            buttonAddMembers.setOnClickListener {
                findNavController().navigate(R.id.action_options_chat_group_to_add_members)
            }
            buttonRemoveMembers.setOnClickListener {
                findNavController().navigate(R.id.action_options_chat_group_to_remove_members)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}