package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.kasperchat_test.R
import com.example.kasperchat_test.databinding.FragmentLoginBinding
import com.example.kasperchat_test.network.SocketManager
import com.example.kasperchat_test.network.SocketManagerInterface
import kotlinx.coroutines.runBlocking

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var socketManager: SocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        socketManager = (requireActivity() as SocketManagerInterface).socketManager
        runBlocking {
            socketManager.connect()
        }
        with(binding){
            buttonLogin.setOnClickListener {
                runBlocking {

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
            /*textViewRegistration.setOnClickListener {
                it.findNavController().navigate(R.id.action_loginFragment_to_registrationFragment)
            }*/
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}