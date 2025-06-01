package com.example.kasperchat_test.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentPreferenceBinding
import com.example.kasperchat_test.network.RetrofitClient
import com.example.kasperchat_test.viewmodel.LoginViewModel


class PreferenceFragment : Fragment() {
    private lateinit var binding: FragmentPreferenceBinding
    private val loginViewModel: LoginViewModel by activityViewModels()

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
                loginViewModel.clearAuthToken()
                it.findNavController().navigate(R.id.loginFragment, null, NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true) // Очищает весь стек до начального фрагмента
                    .build())
            }
        }
    }

}