package com.emeth.kernel.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.emeth.kernel.access.EmethAccessibilityService
import com.emeth.kernel.notifications.EmethNotificationListenerService

data class PermissionStatus(
    val id: String,
    val label: String,
    val enabled: Boolean,
    val unlocks: String,
    val setup: PermissionSetup,
    val permissions: List<String> = emptyList()
)

enum class PermissionSetup {
    ACCESSIBILITY,
    NOTIFICATION_ACCESS,
    RUNTIME_PERMISSION,
    APP_SETTINGS
}

object PermissionCockpit {
    fun current(context: Context): List<PermissionStatus> {
        return listOf(
            PermissionStatus(
                id = "accessibility",
                label = "Accessibility control",
                enabled = EmethAccessibilityService.isEnabled(),
                unlocks = "Screen reading, tapping, typing, back/home, recipes.",
                setup = PermissionSetup.ACCESSIBILITY
            ),
            PermissionStatus(
                id = "notification_access",
                label = "Notification access",
                enabled = EmethNotificationListenerService.isEnabled(context),
                unlocks = "Read active notifications and use exposed actions like WhatsApp Mark as read.",
                setup = PermissionSetup.NOTIFICATION_ACCESS
            ),
            PermissionStatus(
                id = "post_notifications",
                label = "Post notifications",
                enabled = Build.VERSION.SDK_INT < 33 || has(context, Manifest.permission.POST_NOTIFICATIONS),
                unlocks = "Show watcher alerts and assistant notifications.",
                setup = PermissionSetup.RUNTIME_PERMISSION,
                permissions = if (Build.VERSION.SDK_INT >= 33) listOf(Manifest.permission.POST_NOTIFICATIONS) else emptyList()
            ),
            PermissionStatus(
                id = "media",
                label = "Media files",
                enabled = mediaGranted(context),
                unlocks = "Find downloads, videos, images, audio, and status-share candidates.",
                setup = PermissionSetup.RUNTIME_PERMISSION,
                permissions = mediaPermissions()
            ),
            PermissionStatus(
                id = "contacts",
                label = "Contacts",
                enabled = has(context, Manifest.permission.READ_CONTACTS),
                unlocks = "Find people for calls, SMS, and communication prep.",
                setup = PermissionSetup.RUNTIME_PERMISSION,
                permissions = listOf(Manifest.permission.READ_CONTACTS)
            ),
            PermissionStatus(
                id = "calendar",
                label = "Calendar",
                enabled = has(context, Manifest.permission.READ_CALENDAR) && has(context, Manifest.permission.WRITE_CALENDAR),
                unlocks = "Read and create calendar events.",
                setup = PermissionSetup.RUNTIME_PERMISSION,
                permissions = listOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
            ),
            PermissionStatus(
                id = "activity",
                label = "Activity recognition",
                enabled = Build.VERSION.SDK_INT < 29 || has(context, Manifest.permission.ACTIVITY_RECOGNITION),
                unlocks = "Step count and step-based automations.",
                setup = PermissionSetup.RUNTIME_PERMISSION,
                permissions = if (Build.VERSION.SDK_INT >= 29) listOf(Manifest.permission.ACTIVITY_RECOGNITION) else emptyList()
            ),
            PermissionStatus(
                id = "phone_sms",
                label = "Phone and SMS",
                enabled = has(context, Manifest.permission.CALL_PHONE) && has(context, Manifest.permission.SEND_SMS),
                unlocks = "Trusted call/SMS workflows where Android allows it.",
                setup = PermissionSetup.RUNTIME_PERMISSION,
                permissions = listOf(Manifest.permission.CALL_PHONE, Manifest.permission.SEND_SMS)
            )
        )
    }

    fun openSetup(context: Context, setup: PermissionSetup) {
        val intent = when (setup) {
            PermissionSetup.ACCESSIBILITY -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            PermissionSetup.NOTIFICATION_ACCESS -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            PermissionSetup.RUNTIME_PERMISSION,
            PermissionSetup.APP_SETTINGS -> Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}")
            )
        }.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun appSettings(context: Context) {
        openSetup(context, PermissionSetup.APP_SETTINGS)
    }

    private fun mediaGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            has(context, Manifest.permission.READ_MEDIA_IMAGES) ||
                has(context, Manifest.permission.READ_MEDIA_VIDEO) ||
                has(context, Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            has(context, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun mediaPermissions(): List<String> {
        return if (Build.VERSION.SDK_INT >= 33) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun has(context: Context, permission: String): Boolean {
        return Build.VERSION.SDK_INT < 23 ||
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
}
