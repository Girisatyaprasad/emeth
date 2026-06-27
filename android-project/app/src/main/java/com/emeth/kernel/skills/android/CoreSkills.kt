package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent as AndroidIntent
import android.net.Uri
import android.provider.ContactsContract
import android.os.BatteryManager
import android.provider.Settings
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.notifications.EmethNotificationListenerService
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import kotlinx.coroutines.launch
import java.util.Locale

class OpenAppSkill(private val context: Context) : Skill {
    override val id = "android.app.open"
    override val name = "Open App"
    override val description = "Opens a generic application if resolved"

    override fun canHandle(intent: Intent) = intent == Intent.OPEN_APP

    override fun execute(request: SkillRequest): SkillResult {
        val target = request.command.targetApp?.trim().orEmpty()
        if (target.isBlank()) {
            return SkillResult.Partial("Which app should I open?")
        }

        val packageManager = context.packageManager
        val launchIntent = packageManager.getInstalledApplications(0)
            .asSequence()
            .mapNotNull { appInfo ->
                val label = packageManager.getApplicationLabel(appInfo).toString()
                val score = appMatchScore(target, label, appInfo.packageName)
                if (score > 0) Triple(appInfo.packageName, label, score) else null
            }
            .sortedByDescending { it.third }
            .firstOrNull()
            ?.let { (packageName, label, _) ->
                val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
                } ?: return@let null
                Triple(intent, label, packageName)
            }

        if (launchIntent == null) {
            return SkillResult.Failure("I couldn't find an installed app named $target.")
        }

        context.startActivity(launchIntent.first)
        val memoryStore = com.emeth.kernel.memory.MemoryStore(context)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            memoryStore.store(
                com.emeth.kernel.memory.MemoryType.APP_USAGE,
                launchIntent.second.lowercase(Locale.ROOT),
                "{\"package\":\"${launchIntent.third}\", \"action\":\"open\"}"
            )
        }
        return SkillResult.Success("Opened ${launchIntent.second}.")
    }

    private fun appMatchScore(query: String, label: String, packageName: String): Int {
        val q = query.lowercase(Locale.ROOT)
        val l = label.lowercase(Locale.ROOT)
        val p = packageName.lowercase(Locale.ROOT)
        return when {
            l == q -> 100
            l.startsWith(q) -> 80
            l.contains(q) -> 60
            p.contains(q.replace(" ", "")) -> 30
            else -> 0
        }
    }
}

class NotificationSkill(private val context: Context) : Skill {
    override val id = "android.notifications"
    override val name = "Notifications"
    override val description = "Reads active notifications and invokes exposed notification actions"

    override fun canHandle(intent: Intent) = intent == Intent.READ_NOTIFICATIONS ||
        intent == Intent.MARK_WHATSAPP_READ ||
        intent == Intent.OPEN_NOTIFICATION_ACCESS_SETUP

    override fun execute(request: SkillRequest): SkillResult {
        if (request.command.intentType == Intent.OPEN_NOTIFICATION_ACCESS_SETUP) {
            openNotificationAccessSettings()
            return SkillResult.Partial("Opened Notification Access settings. Enable Emeth there so I can read active notifications headlessly.")
        }

        if (!EmethNotificationListenerService.isEnabled(context)) {
            openNotificationAccessSettings()
            return SkillResult.Partial(
                "Notification Access is not enabled for Emeth yet. I opened the setting; enable Emeth to let me read notifications and use exposed actions like WhatsApp Mark as read.",
                mapOf("requiredAccess" to "notification_listener")
            )
        }

        return when (request.command.intentType) {
            Intent.MARK_WHATSAPP_READ -> markWhatsAppRead()
            else -> readNotifications()
        }
    }

    private fun readNotifications(): SkillResult {
        val notifications = EmethNotificationListenerService.activeNotifications()
        if (notifications.isEmpty()) {
            return SkillResult.Success("No active notifications.")
        }

        val summary = notifications
            .take(10)
            .mapIndexed { index, notification ->
                val title = notification.title?.takeIf { it.isNotBlank() } ?: notification.appName
                val text = notification.text?.takeIf { it.isNotBlank() }
                val actionSummary = if (notification.actions.isNotEmpty()) {
                    " Actions: ${notification.actions.joinToString(", ")}."
                } else {
                    ""
                }
                "${index + 1}. ${notification.appName}: $title${text?.let { " - $it" }.orEmpty()}.$actionSummary"
            }
            .joinToString("\n")

        return SkillResult.Success("Active notifications:\n$summary", notifications.take(10))
    }

    private fun markWhatsAppRead(): SkillResult {
        val result = EmethNotificationListenerService.sendWhatsAppMarkReadActions()
        if (result.sentCount > 0) {
            return SkillResult.Success(
                "Marked WhatsApp notification${if (result.sentCount == 1) "" else "s"} as read using Android notification actions.",
                result
            )
        }

        val whatsappNotifications = EmethNotificationListenerService.activeNotifications()
            .filter { it.packageName == "com.whatsapp" || it.packageName == "com.whatsapp.w4b" }

        if (whatsappNotifications.isEmpty()) {
            return SkillResult.Partial("I did not find active WhatsApp notifications to mark as read.")
        }

        return SkillResult.Partial(
            "I found WhatsApp notifications, but none exposed a Mark as read action right now.",
            whatsappNotifications
        )
    }

    private fun openNotificationAccessSettings() {
        val intent = AndroidIntent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }
}

class ContactsSkill(private val context: Context) : Skill {
    override val id = "android.contacts"
    override val name = "Contacts"
    override val description = "Finds contacts"

    override fun canHandle(intent: Intent) = intent == Intent.FIND_CONTACT

    override fun execute(request: SkillRequest): SkillResult {
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts.DISPLAY_NAME),
            "${ContactsContract.Contacts.DISPLAY_NAME} LIKE ?",
            arrayOf("%mom%"),
            null
        )
        val names = mutableListOf<String>()
        cursor?.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                names.add(name)
            }
        }
        return SkillResult.Success("Found contacts: ${names.joinToString()}", names)
    }
}

class BrowserSkill(private val context: Context) : Skill {
    override val id = "android.browser"
    override val name = "Browser"
    override val description = "Opens the browser"

    override fun canHandle(intent: Intent) = intent == Intent.OPEN_BROWSER

    override fun execute(request: SkillRequest): SkillResult {
        val i = AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success("Opened Browser")
    }
}

class BatterySkill(private val context: Context) : Skill {
    override val id = "android.battery"
    override val name = "Battery"
    override val description = "Checks battery level"

    override fun canHandle(intent: Intent) = intent == Intent.CHECK_BATTERY

    override fun execute(request: SkillRequest): SkillResult {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        return SkillResult.Success("Battery: $batteryLevel%", batteryLevel)
    }
}
