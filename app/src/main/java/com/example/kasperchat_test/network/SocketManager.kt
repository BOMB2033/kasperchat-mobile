package com.example.kasperchat_test.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.Locale

interface SocketManagerInterface{
    val socketManager: SocketManager
}
class SocketManager(private val context:Context,private var _endPoint:SocketAddress) {
    constructor(context:Context,host:String, port:Int) : this(context,InetSocketAddress(InetAddress.getByName(host),port))
    constructor(context:Context,host: String) : this( context,host,8888)
    constructor(context:Context) : this(context,"192.168.56.1")

//    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob()) // SupervisorJob для обработки ошибок в корутинах
    private lateinit var socket: Socket

    init {
        socket = Socket()
    }
    suspend fun connect(endPoint:SocketAddress = _endPoint):Boolean{
        withContext(Dispatchers.IO) {
            try{
                if (socket.isConnected) {
                    Log.i("Socket", "Socket is already connected!")
               }else{
                    socket = Socket()
                    Log.i("Socket", "Start connect to server")
                    socket.connect(endPoint,3000)
                }

            }catch (ex:Exception){
                Log.e("Socket-Exception", "Error: ${ex.message}")
                disconnect()

            } finally {
            }
        }
        return isConnectionAlive(socket)
    }
    private suspend fun isConnectionAlive(socket: Socket): Boolean =withContext(Dispatchers.IO) {
        if (!socket.isConnected || socket.isClosed) {
            return@withContext false
        }

        try {
            // Attempt to send a small amount of data (e.g., a newline character)
            val writer = PrintWriter(socket.getOutputStream(), true)
            writer.print("\n")
            writer.flush()

            // Set a timeout for reading data (e.g., 1 second)
            socket.soTimeout = 1000

            // Attempt to read data from the server
            val inputStream: InputStream = socket.getInputStream()
            val bytesRead = inputStream.read()

            // If we get here without an exception, it means we received data (or at least didn't time out)
            Log.e("Socket-Exception","inputStream.read() bytesRead != -1")
            return@withContext bytesRead != -1
        } catch (e: SocketTimeoutException) {
            // Timeout while reading data means the server is likely down
            Log.e("Socket-Exception", "Connection timed out: ${e.message}")
            return@withContext false
        } catch (e: IOException) {
            // An exception indicates a problem with the connection
            Log.e("Socket-Exception", "Connection is broken: ${e.message}")
            return@withContext false
        } catch (e: SocketException) {
            Log.e("Socket-Exception", "Connection is broken: ${e.message}")
            return@withContext false
        } finally {
            // Reset the timeout to 0 (infinite) after the check
            socket.soTimeout = 0
        }
    }
    private suspend fun disconnect(){
        withContext(Dispatchers.IO) {
            socket.close()
        }
        Log.i("Socket", "Socked is closed")
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
            socket.outputStream?.write(data.toByteArray())
        }
    }

    suspend fun read():String{
            val inputStream = withContext(Dispatchers.IO) {
                socket.getInputStream()
            }
            val buffer = ByteArray(1024)
            val bytesRead = withContext(Dispatchers.IO) {
                inputStream!!.read(buffer)
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