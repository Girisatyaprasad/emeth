package com.emeth.kernel.nlu

object PhraseExpander {
    private val mappings = mapOf(
        // YouTube last played
        "play last played video on youtube" to "canonical_youtube_last_played",
        "play last played youtube video" to "canonical_youtube_last_played",
        "continue my last youtube video" to "canonical_youtube_last_played",
        "resume previous youtube video" to "canonical_youtube_last_played",
        "open last video i watched" to "canonical_youtube_last_played",
        "continue youtube" to "canonical_youtube_last_played",
        "resume youtube" to "canonical_youtube_last_played",
        "open the last video i watched" to "canonical_youtube_last_played",

        // YouTube general
        "play youtube shorts" to "canonical_open_shorts",
        "show shorts" to "canonical_open_shorts",
        "search shorts for" to "canonical_search_shorts",
        "open youtube shorts" to "canonical_open_shorts",
        "play shorts" to "canonical_open_shorts",
        "open shorts" to "canonical_open_shorts",
        "open youtube history" to "canonical_open_history",
        "show youtube history" to "canonical_open_history",
        "go to youtube history" to "canonical_open_history",
        "open watch later" to "canonical_open_watch_later",
        "show my watch later on youtube" to "canonical_open_watch_later",
        "open liked videos on youtube" to "canonical_open_liked_videos",
        "open your videos on youtube" to "canonical_open_your_videos",
        "open youtube playlists" to "canonical_open_playlists",
        "open youtube subscriptions" to "canonical_open_subscriptions",
        "replay last youtube video" to "canonical_youtube_last_played",
        "upload video to youtube" to "canonical_upload_video",
        "create youtube short" to "canonical_create_short",
        "make a short" to "canonical_create_short",

        // Steps
        "how many steps today" to "canonical_steps",
        "steps today" to "canonical_steps",
        "step count" to "canonical_steps",
        "how far have i walked" to "canonical_steps",
        "how much did i walk" to "canonical_steps",
        "walking today" to "canonical_steps",

        // Daily alarm
        "everyday" to "canonical_repeat_all",
        "every day" to "canonical_repeat_all",
        "daily" to "canonical_repeat_all",
        "recurring" to "canonical_repeat_all",
        "repeat every day" to "canonical_repeat_all",
        "weekdays" to "canonical_repeat_weekdays",
        "monday to friday" to "canonical_repeat_weekdays",
        "weekends" to "canonical_repeat_weekends",

        // YouTube search
        "search youtube for" to "canonical_youtube_search",
        "find" to "canonical_search",
        "on youtube" to "canonical_target_youtube",
        "look up" to "canonical_search",
        "in youtube" to "canonical_target_youtube",

        // Settings
        "open settings" to "canonical_settings",
        "go to settings" to "canonical_settings",
        "show settings" to "canonical_settings",
        "take me to settings" to "canonical_settings",

        // Battery
        "battery" to "canonical_battery",
        "battery percentage" to "canonical_battery",
        "battery level" to "canonical_battery",
        "how much battery" to "canonical_battery",
        "how much charge" to "canonical_battery",
        "charge level" to "canonical_battery",
        "charge left" to "canonical_battery",

        // Storage
        "storage" to "canonical_storage",
        "space left" to "canonical_storage",
        "free space" to "canonical_storage",
        "how much space" to "canonical_storage",
        
        // Numbers
        "8k" to "8000",
        "10k" to "10000"
    )

    fun expand(normalizedText: String): String {
        var expanded = normalizedText
        val sortedKeys = mappings.keys.sortedByDescending { it.length }
        for (key in sortedKeys) {
            val replacement = mappings[key]!!
            expanded = expanded.replace(Regex("\\b$key\\b"), replacement)
        }
        return expanded
    }
}
