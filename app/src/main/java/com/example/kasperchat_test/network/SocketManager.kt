package com.example.kasperchat_test.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.util.Locale

interface SocketManagerInterface{
    val socketManager: SocketManager
}
class SocketManager(private val context:Context,private var endPoint:SocketAddress) {
    constructor(context:Context,host:String, port:Int) : this(context,InetSocketAddress(InetAddress.getByName(host),port))
    constructor(context:Context,host: String) : this( context,host,8888)
    constructor(context:Context) : this(context,"192.168.161.188")

//    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob()) // SupervisorJob для обработки ошибок в корутинах
    private lateinit var socket:Socket

    suspend fun connect():Boolean{
        withContext(Dispatchers.IO) {
            try{
                Log.i("Socket", "Start connect to server")
                socket = Socket()
                socket.connect(endPoint)
                while (!socket.isConnected)
                    return@withContext

            }catch (ex:Exception){
                Log.e("Socket", "Error: ${ex.message}")
                socket.close()
                Log.i("Socket", "Socked is closed")

            } finally {
            }
        }
        return socket.isConnected
    }

    @SuppressLint("HardwareIds")
    fun getMac(context: Context): String {
        val manager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = manager.connectionInfo
        return info.macAddress.toUpperCase(Locale.ROOT)
    }

    suspend fun login(){
        Log.i("Socket", "Start send mac address")
        send(getMac(context))
        Log.i("Socket", "Read id from server")
        read()

        send("ok")
    }

    suspend fun send(data: String){
        withContext(Dispatchers.IO) {
            Log.i("Socket", "Send message $data")
            socket.outputStream.write(data.toByteArray())
        }
    }

    suspend fun read():String{
            val inputStream = withContext(Dispatchers.IO) {
                socket.getInputStream()
            }
            val buffer = ByteArray(1024)
            val bytesRead = withContext(Dispatchers.IO) {
                inputStream.read(buffer)
            }
            if (bytesRead > 0) {
                Log.i("Socket","Received: ${String(buffer, 0, bytesRead)}")
            }

        return String(buffer, 0, bytesRead)
    }
}
/*
class SocketConnectionManager {


    suspend fun connectAndSendMessage(host: String, port: Int, message: String) {
        withContext(Dispatchers.IO) { // Все операции с сокетом внутри IO Dispatcher
            try {
                val socket = Socket(host, port)
                println("Connected to $host:$port")

                // Отправка сообщения в том же потоке (Dispatchers.IO)


                // Чтение ответа (если необходимо) тоже в том же потоке
                val inputStream = socket.getInputStream()
                val buffer = ByteArray(1024)
                val bytesRead = inputStream.read(buffer)
                if (bytesRead > 0) {
                    println("Received: ${String(buffer, 0, bytesRead)}")
                }

            } catch (e: Exception) {
                println("Error: ${e.message}")
            } finally {
                // Закрытие сокета важно делать всегда
                socket?.close()
            }
        }
    }


    fun cancel() {
        scope.cancel()
    }
}*/