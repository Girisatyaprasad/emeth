package com.emeth.kernel.watchers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

class ActionDispatcher(private val context: Context) {
    
    private val channelId = "emeth_watchers_channel"
    private val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Emeth Watchers",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for triggered watchers"
            }
            nm.createNotificationChannel(channel)
        }
    }

    fun dispatch(action: WatcherAction) {
        if (action.type == "local_notification") {
            val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Emeth Watcher Triggered")
                .setContentText(action.payload)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            
            nm.notify(System.currentTimeMillis().toInt(), builder.build())
        }
    }
}
