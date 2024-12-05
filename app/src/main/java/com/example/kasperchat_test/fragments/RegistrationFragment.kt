package com.example.kasperchat_test.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentRegistrationBinding

private const val ARG_EMAIL = "email"

class RegistrationFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationBinding
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            email = it.getString(ARG_EMAIL)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            if (email.isNullOrBlank().not())
                editTextEmail.setText(email)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    companion object {
        @JvmStatic
        fun setEmail(email: String) : Bundle = Bundle().apply {
            putString(ARG_EMAIL, email)
        }

    }
}