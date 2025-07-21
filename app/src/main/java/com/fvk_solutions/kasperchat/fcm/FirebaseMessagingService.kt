package com.fvk_solutions.kasperchat.fcm

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.fvk_solutions.kasperchat.api.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.fvk_solutions.kasperchat.activity.MainActivity
import java.util.Random
import androidx.core.content.edit

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var apiService: ApiService

    private companion object {
        private const val TAG = "FCM"
        private const val CHANNEL_ID = "chat_notifications"
        private const val CHANNEL_NAME = "Chat Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "Received new FCM token: $token")
        sendTokenToServer(token)
    }

    private fun sendTokenToServer(token: String) {
        showNotification("FCM Token", token, null)
        Log.d(TAG, "Attempting to save FCM token to SharedPreferences")
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit { putString("fcm_token", token) }
        Log.i(TAG, "FCM token saved to SharedPreferences successfully")

        Log.d(TAG, "Sending FCM token to server")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.updateFcmToken(token)
                if (response.isSuccessful) {
                    Log.i(TAG, "FCM token sent to server successfully, response code: ${response.code()}")
                } else {
                    Log.e(TAG, "Failed to send FCM token to server, response code: ${response.code()}, error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception while sending FCM token to server", e)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.i(TAG, "Received FCM message: data=${message.data}, notification=${message.notification}")

        // Логируем содержимое data payload
        val title = message.data["title"] ?: "New Message"
        val body = message.data["body"] ?: "You have a new message"
        val chatId = message.data["chatId"]
        Log.d(TAG, "Parsed notification data: title=$title, body=$body, chatId=$chatId")

        // Показываем уведомление
        showNotification(title, body, chatId)
    }

    private fun showNotification(title: String, body: String, chatId: String?) {
        Log.d(TAG, "Creating notification for chatId=$chatId, title=$title, body=$body")
       // val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationManager = NotificationManagerCompat.from(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        /*// Создаем канал уведомлений (для Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "Creating notification channel: id=$CHANNEL_ID, name=$CHANNEL_NAME")
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new chat messages"
            }
            notificationManager.createNotificationChannel(channel)
            Log.i(TAG, "Notification channel created successfully")
        }*/

        // Интент для открытия чата при клике на уведомление
        Log.d(TAG, "Creating intent for MainActivity with chatId=$chatId")
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("chatId", chatId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        Log.d(TAG, "PendingIntent created for notification")

        // Создаем уведомление
        Log.d(TAG, "Building notification with title=$title, body=$body")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Замените на ваш значок
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Показываем уведомление
        val notificationId = Random().nextInt()
        Log.i(TAG, "Showing notification with id=$notificationId")
        try {
            notificationManager.notify(notificationId, notification)
            Log.i(TAG, "Notification displayed successfully")
        } catch (e: SecurityException) {
            Log.e(TAG, "Failed to display notification due to missing permissions", e)
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error while displaying notification", e)
        }
    }
}