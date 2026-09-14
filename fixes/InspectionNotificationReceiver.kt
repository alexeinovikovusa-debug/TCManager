package com.example.tcmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class InspectionNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val complexName = intent.getStringExtra(EXTRA_COMPLEX_NAME) ?: return
        val date = intent.getStringExtra(EXTRA_DATE) ?: return
        val manager = context.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Проверки",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Предстоящая проверка")
            .setContentText("Комплексная проверка ${date.substring(0, 5)} $complexName")
            .setAutoCancel(true)
            .build()
        manager.notify(complexName.hashCode(), notification)
    }

    companion object {
        const val EXTRA_COMPLEX_NAME = "complex_name"
        const val EXTRA_DATE = "inspection_date"
        private const val CHANNEL_ID = "inspection_notifications"
    }
}
