package com.example.kasperchat_test.fragment

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
import com.example.kasperchat_test.R
import com.example.kasperchat_test.background_services.NotificationCheckService
import com.example.kasperchat_test.databinding.FragmentLoginBinding
import kotlinx.coroutines.runBlocking


class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var socketManager: SocketManager
    private lateinit var serviceIntent:Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        socketManager = (requireActivity().application as SocketManagerInterface).socketManager
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding){
            buttonLogin.setOnClickListener {
                serviceIntent = Intent(context, NotificationCheckService::class.java)
                requireContext().startService(serviceIntent)
                runBlocking {
                    if(socketManager.connect()){
                        Log.i("Socket","Successful connection")
                    }else{
                        Log.i("Socket","Failed connection")
                    }
                }
               /* Thread{
                    val inputStream = socket.getInputStream()
                    var id = 0
                    id = inputStream.read()
                    Log.i("Input stream", id.toString())
                    if (id == 0)
                        return@Thread
                    outputStream.write("ok".toByteArray())
                    var tempString = inputStream.read().toString()
                    Log.i("Input stream", tempString)


                }.start()*/
                it.findNavController().navigate(R.id.action_loginFragment_to_chatsListFragment)
            }
            linkRegister.setOnClickListener {
                it.findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
            }
        }
    }

}