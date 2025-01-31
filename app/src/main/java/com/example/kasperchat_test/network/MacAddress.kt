package com.example.kasperchat_test.network

import java.net.NetworkInterface

// Вспомогательная функция для попытки получить MAC-адрес из NetworkInterface
private fun getMacAddressFromNetworkInterface(): String? {
    try {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.name.contains("wlan")) { // Или "eth0" для Ethernet
                val macBytes = networkInterface.hardwareAddress
                if (macBytes != null) {
                    return bytesToHex(macBytes)
                }
            }
        }
    } catch (e: Exception) {
        // Обработка ошибок
    }
    return null
}

// Вспомогательная функция для преобразования байтов в шестнадцатеричную строку
private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = CharArray(bytes.size * 2)
    for (j in bytes.indices) {
        val v = bytes[j].toInt() and 0xFF
        hexChars[j * 2] = HEX_ARRAY[v ushr 4]
        hexChars[j * 2 + 1] = HEX_ARRAY[v and 0x0F]
    }
    return String(hexChars)
}

private val HEX_ARRAY = "0123456789ABCDEF".toCharArray()