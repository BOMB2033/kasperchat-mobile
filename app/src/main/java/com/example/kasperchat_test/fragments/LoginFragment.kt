package com.example.kasperchat_test.fragments

import android.os.Bundle
import android.telecom.CallEndpoint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentLoginBinding
import java.net.Socket
import java.net.SocketAddress

class LoginFragment : Fragment() {
    private lateinit var binding:FragmentLoginBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            progressLayout.visibility = View.GONE

            registrationButton.setOnClickListener {

                it.findNavController().navigate(
                    R.id.action_loginFragment_to_registrationFragment,
                    RegistrationFragment.setEmail(editTextEmail.text.toString()))
            }

            confirmButton.setOnClickListener {
                val client = Socket(editTextIp.text.toString(),8500)

            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

}
