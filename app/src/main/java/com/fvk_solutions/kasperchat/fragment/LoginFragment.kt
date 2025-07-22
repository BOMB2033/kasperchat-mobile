package com.fvk_solutions.kasperchat.fragment
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.fvk_solutions.kasperchat.R
import com.fvk_solutions.kasperchat.databinding.FragmentLoginBinding
import com.fvk_solutions.kasperchat.viewmodel.LoginResult
import com.fvk_solutions.kasperchat.viewmodel.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        // Попытка автоматического входа, если токен существует и валиден
        val existingToken = loginViewModel.getAuthToken()
        if (!existingToken.isNullOrEmpty()) {
            Log.i("LoginFragment", "Valid token found, attempting auto-login")
            navigateToChatsList()
        }
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

            // Кнопка входа
            buttonLogin.setOnClickListener {
                // Скрываем клавиатуру
                hideKeyboard()

                val username = editTextLogin.text.toString().trim()
                val password = editTextPassword.text.toString().trim()

                // Валидация на стороне UI
                editTextLoginLayout.error = when {
                    username.isEmpty() -> getString(R.string.error_username_empty)
                    username.length < 3 -> getString(R.string.error_username_too_short)
                    username.length > 50 -> getString(R.string.error_username_too_long)
                    else -> null
                }
                editTextPasswordLayout.error = when {
                    password.isEmpty() -> getString(R.string.error_password_empty)
                    password.length < 6 -> getString(R.string.error_password_too_short)
                    password.length > 100 -> getString(R.string.error_password_too_long)
                    else -> null
                }

                if (editTextLoginLayout.error == null && editTextPasswordLayout.error == null) {
                    loginViewModel.loginUser(username, password)
                } else {
                    Toast.makeText(context, R.string.error_fix_fields, Toast.LENGTH_LONG).show()
                }
            }

            // Переход на экран регистрации
            linkRegister.setOnClickListener {
                findNavController().navigate(
                    R.id.action_loginFragment_to_registrationFragment
                )
            }
        }
    }

    private fun observeViewModel() {
        loginViewModel.loginResult.observe(viewLifecycleOwner) { result ->
            with(binding) {
                // Управляем видимостью прогресс-бара и доступностью UI
                progressBar.isVisible = result is LoginResult.Loading
                buttonLogin.isEnabled = result !is LoginResult.Loading
                editTextLoginLayout.isEnabled = result !is LoginResult.Loading
                editTextPasswordLayout.isEnabled = result !is LoginResult.Loading
                linkRegister.isEnabled = result !is LoginResult.Loading

                when (result) {
                    is LoginResult.Loading -> {
                        Log.d("LoginFragment", "Login loading...")
                    }
                    is LoginResult.Success -> {
                        // Очищаем поля ввода
                        editTextLogin.text?.clear()
                        editTextPassword.text?.clear()
                        editTextLoginLayout.error = null
                        editTextPasswordLayout.error = null

                        Log.i("LoginFragment", "Login successful. Token: ${result.token}")
                        Toast.makeText(context, R.string.login_success, Toast.LENGTH_SHORT).show()
                        navigateToChatsList()
                    }
                    is LoginResult.Error -> {
                        editTextLoginLayout.error = null
                        editTextPasswordLayout.error = null
                        Log.e("LoginFragment", "Login error: ${result.message}")
                        Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Наблюдение за профилем пользователя (для отладки или UI)
        loginViewModel.userProfile.observe(viewLifecycleOwner) { profile ->
            if (profile != null) {
                Log.d("LoginFragment", "User profile loaded: ${profile.login}")
            }
        }
    }

    private fun navigateToChatsList() {
        if (isAdded && findNavController().currentDestination?.id == R.id.loginFragment) {
            findNavController().navigate(R.id.action_loginFragment_to_chatsListFragment)
        }
    }

    private fun hideKeyboard() {
        requireActivity().currentFocus?.let { view ->
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}