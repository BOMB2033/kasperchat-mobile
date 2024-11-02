package com.example.kasperchat_test

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.kasperchat_test.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding

    private val items = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding){
            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, items)
            listViewMessages.adapter = adapter
            buttonSend.setOnClickListener {
                val text = editTextMessage.text.toString()
                if (text.isNotEmpty()) {
                    items.add(text)
                    adapter.notifyDataSetChanged()
                    editTextMessage.text.clear()
                }
            }

        }
    }
}