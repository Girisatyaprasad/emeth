package com.emeth.kernel.skills.android

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.MediaStore
import java.util.Locale

data class NativeIntentTarget(
    val label: String,
    val intent: Intent
)

object AndroidNativeIntentMap {
    fun resolve(context: Context, rawTarget: String): NativeIntentTarget? {
        val target = normalize(rawTarget)
        val result = when (target) {
            "phone", "dialer", "calls" -> NativeIntentTarget(
                "Phone",
                Intent(Intent.ACTION_DIAL, Uri.parse("tel:"))
            )
            "contacts", "contact", "address book" -> NativeIntentTarget(
                "Contacts",
                Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
            )
            "messages", "message", "messaging", "sms" -> NativeIntentTarget(
                "Messages",
                Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"))
            )
            "email", "mail", "gmail" -> NativeIntentTarget(
                "Email",
                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
            )
            "camera" -> NativeIntentTarget(
                "Camera",
                Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            )
            "gallery", "photos", "photo gallery" -> NativeIntentTarget(
                "Gallery",
                Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            )
            "files", "file manager", "documents", "document manager" -> NativeIntentTarget(
                "Files",
                Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
            )
            "downloads", "download manager" -> NativeIntentTarget(
                "Downloads",
                Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            )
            "clock", "alarms", "alarm clock" -> NativeIntentTarget(
                "Clock",
                Intent(AlarmClock.ACTION_SHOW_ALARMS)
            )
            "maps", "map", "navigation" -> NativeIntentTarget(
                "Maps",
                Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0"))
            )
            "music", "music player", "audio player" -> NativeIntentTarget(
                "Music",
                Intent(
                    Intent.ACTION_VIEW,
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                ).apply { type = "audio/*" }
            )
            "calendar" -> NativeIntentTarget(
                "Calendar",
                Intent(
                    Intent.ACTION_VIEW,
                    CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
                )
            )
            "browser", "web browser", "internet" -> NativeIntentTarget(
                "Browser",
                Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))
            )
            else -> null
        } ?: return null

        result.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return result.takeIf { it.intent.resolveActivity(context.packageManager) != null }
    }

    fun normalize(rawTarget: String): String {
        return rawTarget
            .lowercase(Locale.ROOT)
            .removeSuffix(" app")
            .removePrefix("the ")
            .trim()
            .replace(Regex("\\s+"), " ")
    }
}
