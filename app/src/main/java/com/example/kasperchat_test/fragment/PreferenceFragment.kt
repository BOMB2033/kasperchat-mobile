package com.example.kasperchat_test.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentPreferenceBinding
import com.example.kasperchat_test.network.RetrofitClient


class PreferenceFragment : Fragment() {
    private lateinit var binding: FragmentPreferenceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPreferenceBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            buttonLogoutAccount.setOnClickListener {
                RetrofitClient.clearToken() // Очищаем токен при ошибке
                it.findNavController().navigate(R.id.loginFragment) // TODO Сделать запрет возвращение по стаку
            }
        }
    }

}