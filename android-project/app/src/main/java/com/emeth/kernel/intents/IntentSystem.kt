package com.emeth.kernel.intents

import java.util.Calendar

enum class Intent {
    OPEN_APP,
    CLOSE_APP,
    
    // System
    TOGGLE_FLASHLIGHT,
    TOGGLE_BLUETOOTH,
    TOGGLE_WIFI,
    TOGGLE_HOTSPOT,
    SET_VOLUME,
    MUTE_VOLUME,
    SET_BRIGHTNESS,
    READ_CLIPBOARD,
    SET_CLIPBOARD,
    CHECK_BATTERY,
    CHECK_STORAGE,
    CHECK_RAM,
    PHONE_BACK,
    PHONE_HOME,
    OPEN_NOTIFICATION_SHADE,
    OPEN_QUICK_SETTINGS,
    OPEN_ACCESSIBILITY_SETUP,
    TAP_TEXT,
    TAP_INDEX,
    TYPE_TEXT,
    SCROLL_UP,
    SCROLL_DOWN,
    READ_SCREEN,
    RUN_PHONE_RECIPE,
    
    // Communication
    CALL_CONTACT,
    SMS_CONTACT,
    OPEN_WHATSAPP,
    OPEN_WHATSAPP_CHATS,
    OPEN_WHATSAPP_UPDATES,
    OPEN_WHATSAPP_COMMUNITIES,
    OPEN_WHATSAPP_CALLS,
    OPEN_WHATSAPP_SETTINGS,
    SEND_WHATSAPP,
    SEND_WHATSAPP_STATUS,
    MARK_WHATSAPP_READ,
    MUTE_WHATSAPP_CHAT,
    FIND_CONTACT,

    // Files
    FIND_FILE,
    DELETE_FILE,
    MOVE_FILE,
    FIND_DUPLICATE_FILES,
    
    // Media / Camera
    TAKE_PHOTO,
    RECORD_VIDEO,
    RECORD_VOICE,
    OPEN_YOUTUBE,
    SEARCH_YOUTUBE,
    YOUTUBE_LAST_PLAYED,
    SEARCH_YOUTUBE_SHORTS,
    OPEN_SHORTS,
    OPEN_HISTORY,
    OPEN_WATCH_LATER,
    OPEN_LIKED_VIDEOS,
    OPEN_YOU_PAGE,
    OPEN_PLAYLISTS,
    OPEN_YOUR_VIDEOS,
    OPEN_SUBSCRIPTIONS,
    PLAY_VIDEO_URL,
    PLAY_LAST_SHORT,
    UPLOAD_VIDEO,
    CREATE_SHORT,
    OPEN_YOUTUBE_SETTINGS,
    OPEN_YOUTUBE_TRENDING,
    OPEN_YOUTUBE_MUSIC,
    OPEN_YOUTUBE_GAMING,
    OPEN_YOUTUBE_MOVIES,
    OPEN_YOUTUBE_DOWNLOADS,
    OPEN_YOUTUBE_NOTIFICATIONS,
    YOUTUBE_LIKE_VIDEO,
    YOUTUBE_SUBSCRIBE,
    YOUTUBE_COMMENT,
    
    // Watchers
    CREATE_WATCHER,
    
    // Productivity
    QUERY_CAPABILITIES,
    QUERY_PERMISSION_STATUS,
    EXECUTE_ANDROID_ACTION,
    WHAT_IS_NEXT,
    OPEN_CALENDAR,
    ADD_CALENDAR_EVENT,
    SET_ALARM,
    SET_TIMER,
    START_STOPWATCH,
    CREATE_NOTE,
    SEARCH_NOTES,
    CREATE_REMINDER,
    READ_REMINDERS,
    
    // Web
    OPEN_BROWSER,
    SEARCH_WEB,
    
    // Settings pages
    OPEN_SETTINGS,
    OPEN_SETTINGS_WIFI,
    OPEN_SETTINGS_BLUETOOTH,
    OPEN_SETTINGS_DISPLAY,
    OPEN_SETTINGS_SOUND,
    OPEN_SETTINGS_ACCESSIBILITY,
    OPEN_SETTINGS_SECURITY,
    OPEN_SETTINGS_APPS,
    OPEN_SETTINGS_BATTERY,
    OPEN_SETTINGS_STORAGE,
    OPEN_SETTINGS_LOCATION,
    OPEN_SETTINGS_DATE_TIME,
    
    // Notifications
    READ_NOTIFICATIONS,
    OPEN_NOTIFICATION_ACCESS_SETUP,
    
    // Health
    CHECK_STEPS,
    
    // Weather
    CHECK_WEATHER,

    // Memory
    QUERY_MEMORY_TODAY,
    QUERY_MEMORY_APPS_TODAY,
    QUERY_MEMORY_YESTERDAY,
    QUERY_MEMORY_SEARCH,
    QUERY_LAST_SCREEN,
    QUERY_LAST_RECIPE,

    UNKNOWN
}

class IntentResolver {
    fun resolve(input: String): ParsedCommand {
        val normalized = com.emeth.kernel.nlu.TextNormalizer.normalize(input)
        val expanded = com.emeth.kernel.nlu.PhraseExpander.expand(normalized)
        val entities = com.emeth.kernel.nlu.EntityExtractor.extract(expanded)
        val match = com.emeth.kernel.nlu.SemanticIntentMatcher.matchWithDetails(expanded, entities)

        var targetApp = entities.targetApp
        var intentType = match.intent
        var confidence = match.confidence
        var matchReasons = match.reasons

        // Extract an app/capability name for generic native and installed-app launches.
        if (intentType == Intent.UNKNOWN || intentType == Intent.OPEN_APP) {
            val appMatch = Regex("^(?:please\\s+)?(?:open|launch|start|show|use|go to)\\s+(?:the\\s+)?(.+?)(?:\\s+app)?$")
                .find(expanded)
            if (appMatch != null) {
                targetApp = appMatch.groupValues[1].trim()
                intentType = Intent.OPEN_APP
                confidence = 0.82f
                matchReasons = listOf("native or installed app launch phrase")
            }
        }

        return ParsedCommand(
            intentType = intentType,
            rawText = input,
            targetApp = targetApp,
            query = entities.query,
            contactName = entities.contactName,
            message = entities.message,
            fileQuery = entities.fileQuery,
            actionMode = entities.actionMode,
            uiTarget = entities.uiTarget,
            uiIndex = entities.uiIndex,
            recipeName = entities.recipeName,
            timeHour = entities.timeHour,
            timeMinute = entities.timeMinute,
            durationSeconds = entities.durationSeconds,
            repeatDays = entities.repeatDays,
            url = entities.url,
            thresholdValue = entities.thresholdValue,
            conditionType = entities.conditionType,
            conditionOp = entities.conditionOp,
            confidence = confidence,
            matchReasons = matchReasons
        )
    }
}
