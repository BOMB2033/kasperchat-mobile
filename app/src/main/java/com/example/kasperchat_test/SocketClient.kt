package com.example.kasperchat_test

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.PrintWriter
import java.net.Socket

class SocketClient(private val host: String, private val port: Int) {

    private var socket: Socket? = null

    fun connect() {

            try {
                Log.i("Socket", "Начало иницилизаци")
                socket = Socket(host, port)
                if (socket != null) Log.i("Socket", "Socket иницилизирован") else  Log.e("Socket", "Не получилось инициализировать Socket")

               // out = socket!!.getOutputStream()
            } catch (e: Exception) {
                Log.i("Socket", "Ошибка при подключении: \n" + e.message.toString())
                e.printStackTrace()
            }
    }

    fun send(message:String){
        Thread {
            Log.i("Socket-send", "Отправка сообщения " + message)
            if (socket == null) {
                Log.e("Socket-send", "Socket не иницилизован! Сообщение не отправлено")
                return@Thread
            }
            if (socket!!.isClosed) {
                Log.e("Socket-send", "Socket закрыт")
                return@Thread
            }
            var out = socket!!.getOutputStream()
            out.write(message.toByteArray())
            out.flush()
        }.start()

    }

    fun read(): String? {
        Log.i("Socket-read","Чтение начато")
        if(socket == null) {
            Log.e("Socket-read","Socket не иницилизован!")
            return null
        }
        if(socket!!.isClosed) {
            Log.e("Socket-read","Socket закрыт!")
            return null
        }
        Log.i("Socket-read","Socket открыт")

        var reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
        Log.i("Socket-read","Получен стрим чтения")
        val message: String? = reader.readLine()
        Log.i("Socket-read","Считано:\n" + message)

        return message
    }

    fun close() {
        try {
            //inReader?.close()
            //out?.close()
            socket?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}