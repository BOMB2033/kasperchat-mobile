package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.kasperchat_test.databinding.FragmentRegistrationBinding
import com.example.kasperchat_test.viewmodel.RegisterResult
import com.example.kasperchat_test.viewmodel.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegistrationFragment : Fragment() {

    private var _binding: FragmentRegistrationBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        with(binding) {
            // Скрываем прогресс-бар по умолчанию
            progressBar.isVisible = false

            // Кнопка регистрации
            buttonRegister.setOnClickListener {
                val login = editRegLogin.text.toString()
                val password = editRegPassword.text.toString()
                val fullName = editTextFullName.text.toString()
                val email = editTextEmail.text.toString().takeIf { it.isNotBlank() } // Поле email опционально

                val errorMessage = registerViewModel.validateInput(fullName, login, password, email)
                if (errorMessage != null) {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                } else {
                    registerViewModel.register(fullName, login, password, email)
                }
            }

            // Переход на экран логина
            linkSingIn.setOnClickListener {
                findNavController().navigate(
                    RegistrationFragmentDirections.actionRegistrationFragmentToLoginFragment()
                )
            }
        }
    }

    private fun observeViewModel() {
        registerViewModel.registerResult.observe(viewLifecycleOwner) { result ->
            with(binding) {
                // Управляем видимостью прогресс-бара и доступностью UI
                progressBar.isVisible = result is RegisterResult.Loading
                buttonRegister.isEnabled = result !is RegisterResult.Loading
                editRegLogin.isEnabled = result !is RegisterResult.Loading
                editRegPassword.isEnabled = result !is RegisterResult.Loading
                editTextFullName.isEnabled = result !is RegisterResult.Loading
                editTextEmail.isEnabled = result !is RegisterResult.Loading
                linkSingIn.isEnabled = result !is RegisterResult.Loading

                when (result) {
                    is RegisterResult.Loading -> {
                        // Можно добавить дополнительный индикатор загрузки, если нужно
                    }
                    is RegisterResult.Success -> {
                        // Очищаем поля ввода
                        editRegLogin.text?.clear()
                        editRegPassword.text?.clear()
                        editTextFullName.text?.clear()
                        editTextEmail.text?.clear()

                        Toast.makeText(
                            context,
                            "Регистрация успешна! Войдите в аккаунт.",
                            Toast.LENGTH_LONG
                        ).show()
                        findNavController().navigate(
                            RegistrationFragmentDirections.actionRegistrationFragmentToLoginFragment()
                        )
                    }
                    is RegisterResult.Error -> {
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Наблюдение за профилем пользователя (для отладки или UI)
        registerViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                // Можно обновить UI, если профиль нужен сразу (например, отобразить имя)
                // Log.d("RegistrationFragment", "User profile loaded: ${profile.fullName}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}