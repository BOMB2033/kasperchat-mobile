package com.example.kasperchat_test.network

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.PrintWriter
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.util.Locale

interface SocketManagerInterface {
    val socketManager: SocketManager
}

class SocketManager(
    private var _endPoint: SocketAddress
) {
    constructor(host: String, port: Int) : this(
        InetSocketAddress(
            InetAddress.getByName(host),
            port
        )
    )

    constructor(host: String) : this(host, 8888)
    constructor() : this("192.168.0.36")

    private var socket: Socket? = null

    public var myUserId: String = "null"
    suspend fun connect(endPoint: SocketAddress = _endPoint): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (socket?.isConnected == true && isSocketAlive()) {
                    Log.i("Socket", "Socket is already connected and alive!")
                    return@withContext true
                }
                socket = Socket()
                Log.i("Socket", "Start connect to server")
                socket?.connect(endPoint, 3000)
                if (socket?.isConnected == true && isSocketAlive()) {
                    Log.i("Socket", "Socket is connected and alive")
                    return@withContext true
                } else {
                    Log.e("Socket", "Socket connection failed or is not alive")
                    disconnect()
                    return@withContext false
                }
            } catch (ex: Exception) {
                Log.e("Socket-Exception", "Error: ${ex.message}")
                disconnect()
                false
            }
        }
    }
    private suspend fun isSocketAlive(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                // Send a test message
                val testMessage = "ping"
                send(testMessage)

                // Wait for a response (e.g., "pong")
                val response = read().trim()
                Log.i("Socket", "Received response: $response")
                // Check if the response is correct
                if (response == "pong") {
                    Log.i("Socket", "Socket is alive")
                    return@withContext true
                } else {
                    Log.e("Socket", "Socket is not alive, received: $response")
                    return@withContext false
                }
            } catch (e: Exception) {
                Log.e("Socket-Exception", "Error checking socket aliveness: ${e.message}")
                return@withContext false
            }
        }
    }

    private suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            socket?.close()
            socket = null
        }
        Log.i("Socket", "Socket is closed")
    }


    @SuppressLint("HardwareIds")
    fun getMac(context: Context): String {
        val manager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = manager.connectionInfo
        return info.macAddress.toUpperCase(Locale.ROOT)
    }

    suspend fun login(context: Context) {
        Log.i("Socket", "Start login")
        sendMacAddress(context)
        val id = read()
        Log.i("Socket", "Received id: $id")
        send("ok")
    }

    private suspend fun sendMacAddress(context: Context) {
        val macAddress = getMac(context)
        Log.i("Socket", "Start send mac address: $macAddress")
        send(macAddress)
    }

    private suspend fun send(data: String) {
        withContext(Dispatchers.IO) {
            Log.d("Socket-Send", "Send message $data")
            try {
                val writer = PrintWriter(socket?.getOutputStream()!!, true)
                writer.println(data)
            } catch (e: IOException) {
                Log.e("Socket-Exception", "Error sending data: ${e.message}")
                disconnect()
            }
        }
    }

    private suspend fun read(): String {
        return withContext(Dispatchers.IO) {
            try {
                socket?.getInputStream()?.bufferedReader()?.use { reader ->
                    val line = reader.readLine()
                    if (line != null) {
                        Log.i("Socket", "Received: $line")
                        return@withContext line
                    } else {
                        Log.e("Socket", "No data received")
                        return@withContext ""
                    }
                } ?: ""
            } catch (e: IOException) {
                Log.e("Socket-Exception", "Error reading data: ${e.message}")
                disconnect()
                ""
            }
        }
    }
}