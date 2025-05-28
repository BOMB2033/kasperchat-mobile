package com.example.kasperchat_test.fragment

import android.content.Context
import com.example.kasperchat_test.network.SocketManager
import com.example.kasperchat_test.network.SocketManagerInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentLoginBinding
import com.example.kasperchat_test.model.LoginResponse
import com.example.kasperchat_test.model.Message
import com.example.kasperchat_test.model.User
import com.example.kasperchat_test.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var socketManager: SocketManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View{
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Проверяем, есть ли сохранённый токен
        val sharedPreferences = requireContext().getSharedPreferences("KasperChatPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("auth_token", null)

        if (token != null) {
            RetrofitClient.apiService.getMessages().enqueue(object : Callback<List<Message>> {
                override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                    if (response.isSuccessful) {
                        findNavController().navigate(R.id.action_loginFragment_to_chatsListFragment)
                    } else {
                        RetrofitClient.clearToken() // Токен недействителен, очищаем
                    }
                }

                override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                    Log.e("LoginFragment", "Error: ${t.message}")
                    RetrofitClient.clearToken() // Очищаем токен при ошибке
                }
            })
        }

        // Если токена нет, показываем экран логина и обрабатываем вход
        with(binding) {
            buttonLogin.setOnClickListener {
                // Пример авторизации
                val user = User(editTextLogin.text.toString(), editTextPassword.text.toString())
                RetrofitClient.apiService.login(user).enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        if (response.isSuccessful) {
                            val loginResponse = response.body()
                            loginResponse?.token?.let { token ->
                                RetrofitClient.saveToken(token) // Сохраняем токен
                                Log.d("LoginFragment", "Login successful, token saved: $token")
                                // Переходим на ChatsListFragment после успешного логина
                                it.findNavController().navigate(R.id.action_loginFragment_to_chatsListFragment)
                            }
                        } else {
                            Log.e("LoginFragment", "Login failed: ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        Log.e("LoginFragment", "Error: ${t.message}")
                    }
                })
            }

            linkRegister.setOnClickListener {
                it.findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
            }
        }
    }

}