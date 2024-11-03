package com.example.kasperchat_test

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kasperchat_test.databinding.ActivityMainBinding
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding

    private val items = mutableListOf<String>()

    private var id = 0;
    private var name = "PhoneClient";

    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        Log.i("Testing-m", "1")

        lifecycleScope.launch {
            val mainHandler = Handler(Looper.getMainLooper())

            Thread {
                var socketClient = SocketClient("192.168.0.20", 8888)
                socketClient.connect()
                var isId = true
                while (true) {
                    Log.i("Socket-Activity", "Чтение буфера")
                    var data = socketClient.read()
                    Log.i("Socket-Activity", "Считан буфер")
                    if (data != null) {
                        if (isId) {
                            isId = false
                            id = data.toInt()
                            mainHandler.post {
                                // Здесь вы можете обновить UI
                                with(binding) {
                                    buttonSend.text = data
                                    progressBar.visibility = View.GONE
                                    buttonSend.setOnClickListener {
                                        val text = editTextMessage.text.toString()
                                        if (text.isNotEmpty()) {
                                            items.add(text)
                                            var jsonMessage = JsonMessage()
                                            jsonMessage.id = id;
                                            jsonMessage.name = name
                                            jsonMessage.message = text
                                            jsonMessage.dateTime = SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(Date())
                                            Gson().toJson(jsonMessage)
                                            socketClient.send(Gson().toJson(jsonMessage))
                                            adapter.notifyDataSetChanged()
                                            editTextMessage.text.clear()
                                        }
                                    }
                                }
                            }
                            socketClient.send(name)
                        } else {
                            mainHandler.post {
                                var dataMessage = Gson().fromJson(data,JsonMessage::class.java)
                                items.add(dataMessage.dateTime+" "+dataMessage.name + " " + dataMessage.message)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }else{
                        Log.i("Socket-Activity", "Буфер пуст")
                    }
                    Thread.sleep(100)
                }
            }.start()

        }



        Log.i("Testing-m", "3")

        with(binding){

            adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, items)
            listViewMessages.adapter = adapter
            items.add(toStringDate(Date()))
            adapter.notifyDataSetChanged()

        }
    }

    private fun toStringDate(date: Date):String{
        var time = date.day.toString()
        return SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(date)
    }
}