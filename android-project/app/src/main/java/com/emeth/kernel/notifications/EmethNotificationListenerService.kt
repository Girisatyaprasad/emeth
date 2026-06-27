package com.emeth.kernel.notifications

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.app.NotificationManagerCompat
import java.util.concurrent.ConcurrentHashMap

data class ObservedNotification(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val text: String?,
    val subText: String?,
    val postTime: Long,
    val actions: List<String>
)

data class NotificationActionResult(
    val sentCount: Int,
    val matchedNotifications: Int,
    val errors: List<String>
)

class EmethNotificationListenerService : NotificationListenerService() {
    override fun onListenerConnected() {
        super.onListenerConnected()
        refresh(activeNotifications.orEmpty().toList())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return
        active[sbn.key] = toObservedNotification(this, sbn)
        live[sbn.key] = sbn
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        active.remove(sbn.key)
        live.remove(sbn.key)
    }

    private fun refresh(notifications: List<StatusBarNotification>) {
        active.clear()
        live.clear()
        notifications.forEach { sbn ->
            active[sbn.key] = toObservedNotification(this, sbn)
            live[sbn.key] = sbn
        }
    }

    companion object {
        private val active = ConcurrentHashMap<String, ObservedNotification>()
        private val live = ConcurrentHashMap<String, StatusBarNotification>()

        fun isEnabled(context: Context): Boolean {
            return NotificationManagerCompat.getEnabledListenerPackages(context)
                .contains(context.packageName)
        }

        fun activeNotifications(): List<ObservedNotification> {
            return active.values.sortedByDescending { it.postTime }
        }

        fun sendWhatsAppMarkReadActions(): NotificationActionResult {
            val errors = mutableListOf<String>()
            var matched = 0
            var sent = 0

            live.values
                .filter { it.packageName == "com.whatsapp" || it.packageName == "com.whatsapp.w4b" }
                .forEach { sbn ->
                    val actions = sbn.notification.actions.orEmpty()
                    val readActions = actions.filter { action ->
                        val title = action.title?.toString()?.lowercase().orEmpty()
                        title.contains("mark as read") || title == "read" || title.contains("read")
                    }
                    if (readActions.isNotEmpty()) matched += 1
                    readActions.forEach { action ->
                        try {
                            action.actionIntent?.send()
                            sent += 1
                        } catch (ex: PendingIntent.CanceledException) {
                            errors.add("Canceled action on ${sbn.packageName}: ${ex.message.orEmpty()}")
                        } catch (ex: RuntimeException) {
                            errors.add("Failed action on ${sbn.packageName}: ${ex.message.orEmpty()}")
                        }
                    }
                }

            return NotificationActionResult(sent, matched, errors)
        }

        private fun toObservedNotification(
            context: Context,
            sbn: StatusBarNotification
        ): ObservedNotification {
            val notification = sbn.notification
            val extras = notification.extras
            return ObservedNotification(
                key = sbn.key,
                packageName = sbn.packageName,
                appName = appName(context, sbn.packageName),
                title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString(),
                text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString(),
                subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString(),
                postTime = sbn.postTime,
                actions = notification.actions.orEmpty()
                    .mapNotNull { it.title?.toString() }
                    .filter { it.isNotBlank() }
            )
        }

        private fun appName(context: Context, packageName: String): String {
            return try {
                val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
                context.packageManager.getApplicationLabel(appInfo).toString()
            } catch (_: Exception) {
                packageName
            }
        }
    }
}
