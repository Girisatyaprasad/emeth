package com.emeth.kernel.capabilities

import com.emeth.kernel.intents.Intent

enum class ExecutionMode {
    HEADLESS,
    APP_INTENT,
    CONFIRMATION_REQUIRED,
    ACCESSIBILITY_REQUIRED,
    UNSUPPORTED
}

enum class RiskLevel {
    LOW,
    MEDIUM,
    HIGH
}

data class Capability(
    val intents: Set<Intent>,
    val label: String,
    val executionMode: ExecutionMode,
    val riskLevel: RiskLevel,
    val requirement: String? = null,
    val limit: String? = null
)

object CapabilityRegistry {
    val all: List<Capability> = listOf(
        Capability(
            intents = setOf(Intent.CHECK_BATTERY, Intent.CHECK_STORAGE, Intent.CHECK_RAM, Intent.READ_CLIPBOARD, Intent.CHECK_STEPS),
            label = "Read device state",
            executionMode = ExecutionMode.HEADLESS,
            riskLevel = RiskLevel.LOW,
            requirement = "Relevant Android runtime permission when required",
            limit = "Step count depends on sensor availability and Activity Recognition permission."
        ),
        Capability(
            intents = setOf(Intent.FIND_FILE, Intent.FIND_DUPLICATE_FILES),
            label = "Find files and duplicate groups",
            executionMode = ExecutionMode.HEADLESS,
            riskLevel = RiskLevel.LOW,
            requirement = "Media read permissions",
            limit = "Search uses Android MediaStore visibility, not every private app folder."
        ),
        Capability(
            intents = setOf(Intent.DELETE_FILE, Intent.MOVE_FILE),
            label = "Delete or move files",
            executionMode = ExecutionMode.CONFIRMATION_REQUIRED,
            riskLevel = RiskLevel.HIGH,
            requirement = "Media permissions plus exact user selection",
            limit = "Emeth should list candidates first and request Android/user confirmation before destructive changes."
        ),
        Capability(
            intents = setOf(Intent.TOGGLE_FLASHLIGHT, Intent.MUTE_VOLUME, Intent.SET_VOLUME),
            label = "Basic device controls",
            executionMode = ExecutionMode.HEADLESS,
            riskLevel = RiskLevel.MEDIUM,
            requirement = "Camera or audio system access depending on action",
            limit = "Some OEM settings may still show system UI."
        ),
        Capability(
            intents = setOf(
                Intent.PHONE_BACK,
                Intent.PHONE_HOME,
                Intent.OPEN_NOTIFICATION_SHADE,
                Intent.OPEN_QUICK_SETTINGS,
                Intent.TAP_TEXT,
                Intent.TAP_INDEX,
                Intent.TYPE_TEXT,
                Intent.SCROLL_UP,
                Intent.SCROLL_DOWN,
                Intent.READ_SCREEN
            ),
            label = "Global phone UI control",
            executionMode = ExecutionMode.ACCESSIBILITY_REQUIRED,
            riskLevel = RiskLevel.HIGH,
            requirement = "Explicitly enabled Emeth Accessibility service",
            limit = "This is the control bridge for operating Android UI. It must be granted by the user in system settings."
        ),
        Capability(
            intents = setOf(Intent.RUN_PHONE_RECIPE),
            label = "Phone task recipes",
            executionMode = ExecutionMode.ACCESSIBILITY_REQUIRED,
            riskLevel = RiskLevel.HIGH,
            requirement = "Emeth Accessibility service plus app intents",
            limit = "Recipes are deterministic chains. A future model can choose recipes, but the recipe runner itself stays local."
        ),
        Capability(
            intents = setOf(Intent.OPEN_ACCESSIBILITY_SETUP),
            label = "Enable Emeth phone access",
            executionMode = ExecutionMode.APP_INTENT,
            riskLevel = RiskLevel.MEDIUM,
            limit = "Opens Android Accessibility settings; the user must enable Emeth Access manually."
        ),
        Capability(
            intents = setOf(Intent.READ_NOTIFICATIONS, Intent.MARK_WHATSAPP_READ),
            label = "Notification awareness and exposed notification actions",
            executionMode = ExecutionMode.HEADLESS,
            riskLevel = RiskLevel.HIGH,
            requirement = "Explicitly enabled Emeth Notification Access service",
            limit = "Can read active notifications and invoke actions that notifications expose, such as WhatsApp Mark as read when present."
        ),
        Capability(
            intents = setOf(Intent.OPEN_NOTIFICATION_ACCESS_SETUP),
            label = "Enable Emeth notification access",
            executionMode = ExecutionMode.APP_INTENT,
            riskLevel = RiskLevel.MEDIUM,
            limit = "Opens Android Notification Access settings; the user must enable Emeth manually."
        ),
        Capability(
            intents = setOf(
                Intent.OPEN_APP,
                Intent.OPEN_BROWSER,
                Intent.SEARCH_WEB,
                Intent.OPEN_SETTINGS,
                Intent.OPEN_SETTINGS_WIFI,
                Intent.OPEN_SETTINGS_BLUETOOTH,
                Intent.OPEN_SETTINGS_DISPLAY,
                Intent.OPEN_SETTINGS_SOUND,
                Intent.OPEN_SETTINGS_ACCESSIBILITY,
                Intent.OPEN_SETTINGS_SECURITY,
                Intent.OPEN_SETTINGS_APPS,
                Intent.OPEN_SETTINGS_BATTERY,
                Intent.OPEN_SETTINGS_STORAGE,
                Intent.OPEN_SETTINGS_LOCATION,
                Intent.OPEN_SETTINGS_DATE_TIME
            ),
            label = "Open apps, browser, web search, and settings",
            executionMode = ExecutionMode.APP_INTENT,
            riskLevel = RiskLevel.LOW,
            limit = "These actions visibly open another app or Android settings screen."
        ),
        Capability(
            intents = setOf(
                Intent.OPEN_YOUTUBE,
                Intent.SEARCH_YOUTUBE,
                Intent.SEARCH_YOUTUBE_SHORTS,
                Intent.OPEN_WATCH_LATER,
                Intent.OPEN_LIKED_VIDEOS,
                Intent.OPEN_YOUR_VIDEOS,
                Intent.PLAY_VIDEO_URL,
                Intent.UPLOAD_VIDEO,
                Intent.OPEN_YOUTUBE_MUSIC,
                Intent.YOUTUBE_LAST_PLAYED
            ),
            label = "YouTube app-intent actions",
            executionMode = ExecutionMode.APP_INTENT,
            riskLevel = RiskLevel.LOW,
            requirement = "YouTube app or browser fallback",
            limit = "Unsupported YouTube sections may fall back or fail depending on installed app support."
        ),
        Capability(
            intents = setOf(Intent.YOUTUBE_LIKE_VIDEO, Intent.YOUTUBE_SUBSCRIBE, Intent.YOUTUBE_COMMENT, Intent.CREATE_SHORT),
            label = "YouTube interactive actions",
            executionMode = ExecutionMode.ACCESSIBILITY_REQUIRED,
            riskLevel = RiskLevel.HIGH,
            requirement = "Explicitly enabled Accessibility service",
            limit = "Public app intents do not expose these as headless actions."
        ),
        Capability(
            intents = setOf(Intent.CALL_CONTACT, Intent.SMS_CONTACT, Intent.FIND_CONTACT),
            label = "Contacts, calls, and SMS prep",
            executionMode = ExecutionMode.APP_INTENT,
            riskLevel = RiskLevel.HIGH,
            requirement = "Contacts/SMS/phone permissions depending on action",
            limit = "Calls and SMS should remain confirmation-first unless a trusted automation rule exists."
        ),
        Capability(
            intents = setOf(Intent.OPEN_WHATSAPP, Intent.SEND_WHATSAPP, Intent.SEND_WHATSAPP_STATUS),
            label = "WhatsApp prepare and share",
            executionMode = ExecutionMode.CONFIRMATION_REQUIRED,
            riskLevel = RiskLevel.HIGH,
            requirement = "WhatsApp installed and contact/media visibility",
            limit = "Public Android app intents can prepare messages or shares, but cannot guarantee silent send or status post."
        ),
        Capability(
            intents = setOf(Intent.MUTE_WHATSAPP_CHAT),
            label = "WhatsApp chat state management",
            executionMode = ExecutionMode.UNSUPPORTED,
            riskLevel = RiskLevel.HIGH,
            requirement = "Accessibility service recipe for a future implementation",
            limit = "WhatsApp does not expose public app intents for chat lists or mute controls."
        ),
        Capability(
            intents = setOf(Intent.SET_ALARM, Intent.SET_TIMER, Intent.START_STOPWATCH, Intent.OPEN_CALENDAR, Intent.ADD_CALENDAR_EVENT),
            label = "Clock and calendar surfaces",
            executionMode = ExecutionMode.APP_INTENT,
            riskLevel = RiskLevel.MEDIUM,
            requirement = "Clock/calendar providers",
            limit = "Some actions open a confirmation or edit screen."
        ),
        Capability(
            intents = setOf(Intent.CREATE_WATCHER),
            label = "Background watchers",
            executionMode = ExecutionMode.HEADLESS,
            riskLevel = RiskLevel.MEDIUM,
            requirement = "WorkManager and relevant sensor/notification permissions",
            limit = "Current watcher conditions are limited; richer triggers need new evaluators."
        ),
        Capability(
            intents = setOf(Intent.TAKE_PHOTO, Intent.RECORD_VIDEO, Intent.RECORD_VOICE),
            label = "Camera and recorder launch",
            executionMode = ExecutionMode.APP_INTENT,
            riskLevel = RiskLevel.MEDIUM,
            requirement = "Camera/audio app and permissions",
            limit = "Recording still happens in the target app UI."
        ),
        Capability(
            intents = setOf(Intent.QUERY_MEMORY_TODAY, Intent.QUERY_MEMORY_APPS_TODAY, Intent.QUERY_MEMORY_YESTERDAY, Intent.QUERY_MEMORY_SEARCH),
            label = "Memory recall",
            executionMode = ExecutionMode.HEADLESS,
            riskLevel = RiskLevel.LOW,
            limit = "Current memory is command/app usage, not full personal context."
        )
    )

    fun forIntent(intent: Intent): Capability? {
        return all.firstOrNull { intent in it.intents }
    }

    fun byMode(mode: ExecutionMode): List<Capability> {
        return all.filter { it.executionMode == mode }
    }

    fun summary(): String {
        val counts = ExecutionMode.entries.associateWith { mode -> byMode(mode).size }
        return "Capability ledger: ${counts[ExecutionMode.HEADLESS]} headless, " +
            "${counts[ExecutionMode.APP_INTENT]} app-intent, " +
            "${counts[ExecutionMode.CONFIRMATION_REQUIRED]} confirmation-required, " +
            "${counts[ExecutionMode.ACCESSIBILITY_REQUIRED]} accessibility-required, " +
            "${counts[ExecutionMode.UNSUPPORTED]} unsupported groups."
    }
}
