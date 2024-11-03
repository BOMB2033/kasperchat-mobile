package com.example.kasperchat_test
import android.os.Handler
import android.os.Looper

class BufferChecker(private val buffer: String?, private val onDataReceived: (String) -> Unit) {

    private val handler = Handler(Looper.getMainLooper())
    private var isRunning = false

    fun startChecking() {
        isRunning = true
        checkBuffer()
    }

    fun stopChecking() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun checkBuffer() {
        if (isRunning) {
            if (buffer?.isNotEmpty() == true) {
                // Получаем данные из буфера
                val data = buffer
                // Вызываем функцию в основном потоке
                onDataReceived(data)
            }
            // Повторяем проверку через 100 миллисекунд
            handler.postDelayed({ checkBuffer() }, 100)
        }
    }
}