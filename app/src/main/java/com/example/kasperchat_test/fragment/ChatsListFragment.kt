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
            chatItems.add(ChatItem("Иосиф Виссарионович", "Я подключился, ща разберусь в управлении", "12:00"))
            chatItems.add(ChatItem("Мария", "Я родила!", "12:00"))
            chatItems.add(ChatItem("Малыш", "Я выпал, толстяк скоро за мной прилетит", "19:00"))
            chatItems.add(ChatItem("Дарт Вейдер", "Люк, я твой отец! (И у меня проблемы с дыханием)", "19:00"))
            chatItems.add(ChatItem("Терминатор", "Hasta la vista, baby! (И да, я вернусь)", "00:00"))
            chatItems.add(ChatItem("Нео", "Я знаю кунг-фу! (И где тут выход из Матрицы?)", "10:00"))
            chatItems.add(ChatItem("Гарри Поттер", "Авада Кедавра! (Ой, не туда)", "15:00"))
            chatItems.add(ChatItem("Шерлок Холмс", "Элементарно, Ватсон! (Но где мой чай?)", "09:00"))
            chatItems.add(ChatItem("Доктор Кто", "Времени мало, а планет много! (И где моя отвертка?)", "11:11"))
            chatItems.add(ChatItem("Джон Сноу", "Ты ничего не знаешь, Джон Сноу! (А я знаю, что зима близко)", "13:00"))
            chatItems.add(ChatItem("Король Джулиан", "Я люблю двигать телом! (И у меня есть корона)", "17:00"))
            chatItems.add(ChatItem("Баба Яга", "Опять избушка на курьих ножках забарахлила! Где мой ступа-то?", "10:00"))
            chatItems.add(ChatItem("Илья Муромец", "Сижу на печи, жду, когда тридцать лет пройдет...", "09:00"))
            chatItems.add(ChatItem("Кот Баюн", "Сказки сказывать - не мешки ворочать. Но я постараюсь...", "14:00"))
            chatItems.add(ChatItem("Змей Горыныч", "Опять все три головы спорят, куда лететь...", "16:00"))
            chatItems.add(ChatItem("Василиса Прекрасная", "Ищу лягушачью кожу, кто видел?", "11:00"))
            chatItems.add(ChatItem("Емеля", "По щучьему велению, по моему хотению, пусть чат заработает!", "12:00"))
            chatItems.add(ChatItem("Богатырь", "Есть ли тут супостаты, с кем силушкой померяться?", "18:00"))
            chatItems.add(ChatItem("Снегурочка", "Дедушка Мороз опять где-то застрял, а подарки сами себя не разнесут!", "15:00"))
            chatItems.add(ChatItem("Александр Сергеевич", "Что-то муза меня сегодня не посещает... Может, чаю?", "19:00"))
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