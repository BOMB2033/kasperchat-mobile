package com.example.kasperchat_test.background_services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import android.app.AlarmManager
import android.os.Message
import android.os.SystemClock
import com.example.kasperchat_test.R
import com.example.kasperchat_test.activity.MainActivity
import kotlinx.coroutines.runBlocking

class NotificationCheckService : Service() {
    private val CHANNEL_ID = "KasperChatNotificationCheck"
    private val NOTIFICATION_ID = 1
    private val ALARM_REQUEST_CODE = 100
    private val CHECK_INTERVAL = 3 * 100L  // 1 minutes in milliseconds


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d("NotificationCheck", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification("Служба запущена"))
        scheduleNextCheck()
        checkNotifications()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notification Check Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(message: String): Notification {
        val intent =
            Intent(this, MainActivity::class.java) // Replace MainActivity with your main activity
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Ожидание уведомлений")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_avatar) // Replace with your icon
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun scheduleNextCheck() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationCheckService::class.java)
        val pendingIntent = PendingIntent.getService(
            this,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val triggerAtMillis = SystemClock.elapsedRealtime() + CHECK_INTERVAL
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtMillis, pendingIntent)
    }

    private fun checkNotifications() {
        Log.d("NotificationCheck", "Checking for notifications...")
        runBlocking {

        }
    }
    private fun updateNotification(text: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        val notification = createUpdatedNotification(text)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    private fun createUpdatedNotification(text: String): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Проверка уведомлений")
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_avatar)
            .setContentIntent(pendingIntent)
            .build()
    }
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("NotificationCheck", "Service destroyed")
    }
}