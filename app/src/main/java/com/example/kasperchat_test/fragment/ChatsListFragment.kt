package com.example.kasperchat_test.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasperchat_test.databinding.FragmentChatsListBinding
import com.example.kasperchat_test.ui.adapter.ChatAdapter
import com.example.kasperchat_test.ui.adapter.ChatItem

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ChatsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ChatsListFragment : Fragment(),ChatAdapter.OnItemClickListener {
    private lateinit var binding: FragmentChatsListBinding
    private lateinit var chatAdapter: ChatAdapter
    private val chatItems = mutableListOf<ChatItem>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatsListBinding.inflate(inflater,container,false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding){
            binding.rvChats.layoutManager = LinearLayoutManager(requireContext())
            // Инициализация данных
            chatItems.add(ChatItem("Иосиф Виссарионович", "Я подключился, ща разберусь в управлении", "12:00 22 июня 1941"))
            chatItems.add(ChatItem("Мария", "Я родила!", "12:00 01.01.0001"))
            chatItems.add(ChatItem("Малыш", "Я выпал, толстяк скоро за мной прилетит", "6 августа 1945"))

            chatAdapter = ChatAdapter(chatItems, object : ChatAdapter.OnItemClickListener{
                override fun onItemClick(chatItem: ChatItem) {
                    
                }

            })
            binding.rvChats.adapter = chatAdapter
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ChatsListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ChatsListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemClick(chatItem: ChatItem) {
        TODO("Not yet implemented")
    }
}