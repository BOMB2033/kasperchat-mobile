package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels // Для делегата viewModels
import androidx.lifecycle.Observer // Для наблюдения за LiveData
import androidx.navigation.fragment.findNavController // Улучшенный способ получения NavController
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentLoginBinding
import com.example.kasperchat_test.viewmodel.LoginResult // Импортируем наш sealed class
import com.example.kasperchat_test.viewmodel.LoginViewModel // Импортируем нашу ViewModel
import kotlin.text.trim

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null // Сделаем nullable для безопасного обращения в onDestroyView
    private val binding get() = _binding!! // Для удобства доступа

    // Инициализируем ViewModel с помощью делегата
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Попытка автоматического входа, если токен существует
        // Настоящая валидация токена должна происходить на сервере при первом запросе
        // Здесь мы просто проверяем его наличие для быстрого входа
        val existingToken = loginViewModel.getAuthToken()
        if (!existingToken.isNullOrEmpty()) {
            // В идеале, здесь можно было бы сделать тестовый запрос к защищенному эндпоинту
            // чтобы убедиться, что токен все еще валиден на сервере.
            // Для упрощения, мы просто переходим дальше, если токен есть.
            Log.i("LoginFragment", "Token found, attempting auto-login.")
            navigateToChatsList()
        }
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        with(binding) {
            // Скрываем progressBar по умолчанию
            progressBar.isVisible = false

            buttonLogin.setOnClickListener {
                val email = editTextLogin.text.toString().trim()
                val password = editTextPassword.text.toString().trim()

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                // Запускаем процесс логина через ViewModel
                loginViewModel.loginUser(email, password)
            }

            linkRegister.setOnClickListener {
                findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
            }
        }
    }

    private fun observeViewModel() {
        loginViewModel.loginResult.observe(viewLifecycleOwner, Observer { result ->
            when (result) {
                is LoginResult.Loading -> {
                    binding.progressBar.isVisible = true
                    binding.buttonLogin.isEnabled = false // Блокируем кнопку во время загрузки
                    Log.d("LoginFragment", "Login loading...")
                }
                is LoginResult.Success -> {
                    binding.progressBar.isVisible = false
                    binding.buttonLogin.isEnabled = true
                    Log.i("LoginFragment", "Login successful. Token: ${result.token}")
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    navigateToChatsList()
                }
                is LoginResult.Error -> {
                    binding.progressBar.isVisible = false
                    binding.buttonLogin.isEnabled = true
                    Log.e("LoginFragment", "Login error: ${result.message}")
                    Toast.makeText(context, "Login Failed: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun navigateToChatsList() {
        // Убедимся, что мы все еще в этом фрагменте, чтобы избежать крэшей при быстрой навигации
        if (isAdded && findNavController().currentDestination?.id == R.id.loginFragment) {
            findNavController().navigate(R.id.action_loginFragment_to_chatsListFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем ссылку на binding, чтобы избежать утечек памяти
    }
}