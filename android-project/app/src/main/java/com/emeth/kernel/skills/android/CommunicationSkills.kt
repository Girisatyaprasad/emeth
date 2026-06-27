package com.emeth.kernel.skills.android

import android.content.ClipData
import android.content.ContentUris
import android.content.Context
import android.content.Intent as AndroidIntent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult

class CallContactSkill(private val context: Context) : Skill {
    override val id = "android.call"
    override val name = "Call Contact"
    override val description = "Opens dialer to call"

    override fun canHandle(intent: Intent) = intent == Intent.CALL_CONTACT

    override fun execute(request: SkillRequest): SkillResult {
        val contact = request.command.contactName
        val number = contact?.let { ContactPhoneResolver.find(context, it) }
        if (contact != null && number == null) {
            return ContactPhoneResolver.failure(context, contact)
        }
        val i = AndroidIntent(AndroidIntent.ACTION_DIAL).apply {
            if (number != null) data = Uri.parse("tel:${Uri.encode(number)}")
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success(if (contact == null) "Opened dialer." else "Opened dialer for $contact.")
    }
}

class SmsContactSkill(private val context: Context) : Skill {
    override val id = "android.sms"
    override val name = "SMS Contact"
    override val description = "Opens SMS app to send message"

    override fun canHandle(intent: Intent) = intent == Intent.SMS_CONTACT

    override fun execute(request: SkillRequest): SkillResult {
        val contact = request.command.contactName
        val number = contact?.let { ContactPhoneResolver.find(context, it) }
        if (contact != null && number == null) {
            return ContactPhoneResolver.failure(context, contact)
        }
        val i = AndroidIntent(AndroidIntent.ACTION_VIEW).apply {
            data = Uri.parse("sms:${number.orEmpty()}")
            request.command.message?.let { putExtra("sms_body", it) }
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success(if (contact == null) "Opened SMS." else "Prepared SMS to $contact.")
    }
}

class WhatsAppSkill(private val context: Context) : Skill {
    override val id = "android.whatsapp.open"
    override val name = "Open WhatsApp"
    override val description = "Opens WhatsApp"

    override fun canHandle(intent: Intent) = intent == Intent.OPEN_WHATSAPP ||
        intent == Intent.OPEN_WHATSAPP_CHATS ||
        intent == Intent.OPEN_WHATSAPP_UPDATES ||
        intent == Intent.OPEN_WHATSAPP_COMMUNITIES ||
        intent == Intent.OPEN_WHATSAPP_CALLS ||
        intent == Intent.OPEN_WHATSAPP_SETTINGS ||
        intent == Intent.SEND_WHATSAPP ||
        intent == Intent.SEND_WHATSAPP_STATUS ||
        intent == Intent.MARK_WHATSAPP_READ ||
        intent == Intent.MUTE_WHATSAPP_CHAT

    override fun execute(request: SkillRequest): SkillResult {
        return when (request.command.intentType) {
            Intent.SEND_WHATSAPP -> sendMessage(request)
            Intent.SEND_WHATSAPP_STATUS -> sendStatus(request)
            Intent.MARK_WHATSAPP_READ -> unsupportedChatStateAction("mark WhatsApp chats as read")
            Intent.MUTE_WHATSAPP_CHAT -> unsupportedChatStateAction("list or mute WhatsApp chats")
            Intent.OPEN_WHATSAPP_CHATS -> openSection("Chats")
            Intent.OPEN_WHATSAPP_UPDATES -> openSection("Updates", "Status")
            Intent.OPEN_WHATSAPP_COMMUNITIES -> openSection("Communities")
            Intent.OPEN_WHATSAPP_CALLS -> openSection("Calls")
            Intent.OPEN_WHATSAPP_SETTINGS -> openSection("Settings")
            else -> openWhatsApp()
        }
    }

    private fun openWhatsApp(): SkillResult {
        val launch = whatsappLaunchIntent()
            ?: return SkillResult.Failure("WhatsApp is not installed or Android is blocking its launch activity.")
        return try {
            context.startActivity(launch)
            SkillResult.Success("Opened WhatsApp.")
        } catch (error: Exception) {
            SkillResult.Failure("Android found WhatsApp but could not open it: ${error.message}", error)
        }
    }

    private fun openSection(primaryLabel: String, fallbackLabel: String? = null): SkillResult {
        val launch = whatsappLaunchIntent()
            ?: return SkillResult.Failure("WhatsApp is not installed or Android is blocking its launch activity.")
        context.startActivity(launch)
        Thread.sleep(900)
        val tapped = com.emeth.kernel.access.EmethAccessibilityService.tapText(primaryLabel) ||
            (fallbackLabel != null && com.emeth.kernel.access.EmethAccessibilityService.tapText(fallbackLabel))
        return if (tapped) {
            SkillResult.Success("Opened WhatsApp $primaryLabel.")
        } else {
            SkillResult.Partial(
                "Opened WhatsApp. Enable Accessibility access so Emeth can select $primaryLabel inside WhatsApp."
            )
        }
    }

    private fun whatsappLaunchIntent(): AndroidIntent? {
        val packageManager = context.packageManager
        for (packageName in listOf("com.whatsapp", "com.whatsapp.w4b")) {
            packageManager.getLaunchIntentForPackage(packageName)?.let {
                return it.apply { flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK }
            }
            val uriIntent = AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("whatsapp://send")).apply {
                setPackage(packageName)
                flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            }
            if (uriIntent.resolveActivity(packageManager) != null) return uriIntent
        }
        return packageManager.queryIntentActivities(
            AndroidIntent(AndroidIntent.ACTION_MAIN).addCategory(AndroidIntent.CATEGORY_LAUNCHER),
            0
        ).firstOrNull {
            val label = it.loadLabel(packageManager).toString()
            label.contains("whatsapp", ignoreCase = true)
        }?.activityInfo?.let { info ->
            AndroidIntent(AndroidIntent.ACTION_MAIN)
                .addCategory(AndroidIntent.CATEGORY_LAUNCHER)
                .setClassName(info.packageName, info.name)
                .addFlags(AndroidIntent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun sendMessage(request: SkillRequest): SkillResult {
        val rawText = request.command.rawText.lowercase()
        val message = request.command.message ?: extractMessageBeforeRecipient(rawText)
        if (message.isNullOrBlank()) {
            return SkillResult.Partial("What message should I send on WhatsApp?")
        }

        val typedPhone = Regex("\\+?[0-9][0-9\\s-]{7,}").find(rawText)?.value?.filter { it.isDigit() }
        val contact = request.command.contactName
        val phone = typedPhone ?: contact?.let { ContactPhoneResolver.find(context, it) }
        if (contact != null && phone == null) {
            return ContactPhoneResolver.failure(context, contact)
        }
        val intent = if (!phone.isNullOrBlank()) {
            val waNumber = phone.filter { it.isDigit() }
            AndroidIntent(
                AndroidIntent.ACTION_VIEW,
                Uri.parse("https://wa.me/$waNumber?text=${Uri.encode(message)}")
            ).apply {
                setPackage(installedWhatsAppPackage())
            }
        } else {
            AndroidIntent(AndroidIntent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(AndroidIntent.EXTRA_TEXT, message)
                setPackage(installedWhatsAppPackage())
            }
        }.apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
        if (!phone.isNullOrBlank()) {
            if (com.emeth.kernel.access.EmethAccessibilityService.waitForAndClick("Send", 5000)) {
                return SkillResult.Success("Sent WhatsApp message to ${contact ?: phone}.")
            }
            return SkillResult.Partial(
                "Prepared the WhatsApp message to ${contact ?: phone}. Enable Accessibility control in Access so Emeth can press Send."
            )
        }
        return SkillResult.Success("Opened WhatsApp with the message ready.")
    }

    private fun sendStatus(request: SkillRequest): SkillResult {
        val latestVideo = latestDownloadedVideo()
            ?: return SkillResult.Partial("I couldn't find a downloaded video to send to WhatsApp status.")

        if (request.command.actionMode == null) {
            return SkillResult.Partial(
                "I found the latest downloaded video. Choose headless or two_step; with app intents, two_step is the supported status method.",
                latestVideo
            )
        }

        if (request.command.actionMode == "headless") {
            return SkillResult.Partial(
                "WhatsApp status cannot be posted headlessly through public Android app intents. I can ready the latest video at the pre-send screen.",
                latestVideo
            )
        }

        val uri = Uri.parse(latestVideo.uri)
        val intent = AndroidIntent(AndroidIntent.ACTION_SEND).apply {
            type = latestVideo.mimeType ?: "video/*"
            putExtra(AndroidIntent.EXTRA_STREAM, uri)
            clipData = ClipData.newUri(context.contentResolver, latestVideo.displayName, uri)
            addFlags(AndroidIntent.FLAG_GRANT_READ_URI_PERMISSION)
            flags = flags or AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            setPackage(installedWhatsAppPackage())
        }

        context.startActivity(intent)
        return SkillResult.Success("Prepared latest downloaded video for WhatsApp status.")
    }

    private fun installedWhatsAppPackage(): String {
        return listOf("com.whatsapp", "com.whatsapp.w4b")
            .firstOrNull { context.packageManager.getLaunchIntentForPackage(it) != null }
            ?: "com.whatsapp"
    }

    private fun unsupportedChatStateAction(action: String): SkillResult {
        return SkillResult.Partial(
            "I can recognize this request, but Android app intents do not expose WhatsApp chat lists, unread state, or mute controls. I can open WhatsApp so you can finish $action there.",
            mapOf("supportedFallback" to "open_whatsapp")
        )
    }

    private fun latestDownloadedVideo(): SmartFileCandidate? {
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection = buildList {
            add(MediaStore.Video.Media._ID)
            add(MediaStore.Video.Media.DISPLAY_NAME)
            add(MediaStore.Video.Media.SIZE)
            add(MediaStore.Video.Media.DATE_MODIFIED)
            add(MediaStore.Video.Media.MIME_TYPE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                add(MediaStore.Video.Media.RELATIVE_PATH)
            }
        }.toTypedArray()
        val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) "${MediaStore.Video.Media.RELATIVE_PATH} LIKE ?" else null
        val args = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) arrayOf("Download%") else null
        val sort = "${MediaStore.Video.Media.DATE_ADDED} DESC"

        context.contentResolver.query(collection, projection, selection, args, sort)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID))
                val uri = ContentUris.withAppendedId(collection, id)
                val pathIndex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
                } else {
                    -1
                }
                return SmartFileCandidate(
                    displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)) ?: "latest downloaded video",
                    uri = uri.toString(),
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE)),
                    relativePath = if (pathIndex >= 0) cursor.getString(pathIndex) else null,
                    sizeBytes = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)),
                    modifiedAtSeconds = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)),
                    score = 100
                )
            }
        }

        return null
    }

    private fun extractMessageBeforeRecipient(rawText: String): String? {
        val match = Regex("(?:send|text|message)\\s+(.+?)\\s+to\\s+").find(rawText)
        return match?.groupValues?.getOrNull(1)?.trim()
    }
}

private object ContactPhoneResolver {
    fun find(context: Context, name: String): String? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY
        )
        val wanted = normalizeName(name)
        var bestNumber: String? = null
        var bestScore = -1

        // Strategy 1: SQL LIKE match
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} LIKE ?"
        val args = arrayOf("%$name%")
        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                selection,
                args,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY} ASC"
            )?.use { cursor ->
                while (cursor.moveToNext()) {
                    val displayName = cursor.getString(2).orEmpty()
                    val candidate = cursor.getString(1) ?: cursor.getString(0) ?: continue
                    val normalizedDisplay = normalizeName(displayName)
                    val score = when {
                        normalizedDisplay == wanted -> 100
                        normalizedDisplay.startsWith(wanted) -> 80
                        normalizedDisplay.contains(wanted) -> 60
                        else -> 20
                    }
                    if (score > bestScore) {
                        bestScore = score
                        bestNumber = candidate.filter { it.isDigit() || it == '+' }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore
        }

        // Strategy 2: Fallback scan across all contacts if Strategy 1 fails
        if (bestNumber == null) {
            try {
                context.contentResolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    while (cursor.moveToNext()) {
                        val displayName = cursor.getString(2).orEmpty()
                        val candidate = cursor.getString(1) ?: cursor.getString(0) ?: continue
                        val normalizedDisplay = normalizeName(displayName)
                        val score = when {
                            normalizedDisplay == wanted -> 100
                            normalizedDisplay.startsWith(wanted) -> 80
                            normalizedDisplay.contains(wanted) -> 60
                            else -> -1
                        }
                        if (score > bestScore) {
                            bestScore = score
                            bestNumber = candidate.filter { it.isDigit() || it == '+' }
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        return bestNumber
    }

    fun failure(context: Context, name: String): SkillResult {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        return if (granted) {
            SkillResult.Failure("I couldn't find a phone number for $name.")
        } else {
            SkillResult.Failure("Contacts permission is required. Open the Access tab and allow Contacts.")
        }
    }

    private fun normalizeName(value: String): String {
        return value.lowercase()
            .replace(Regex("[^a-z0-9]"), "")
    }
}

class ContactsSearchSkill(private val context: Context) : Skill {
    override val id = "android.contacts.search"
    override val name = "Search Contacts"
    override val description = "Searches for a contact"

    override fun canHandle(intent: Intent) = intent == Intent.FIND_CONTACT

    override fun execute(request: SkillRequest): SkillResult {
        val i = AndroidIntent(AndroidIntent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success("Opened Contacts Picker")
    }
}
