package com.emeth.kernel.nlu

import com.emeth.kernel.intents.Intent

data class IntentMatch(
    val intent: Intent,
    val confidence: Float,
    val score: Float,
    val reasons: List<String>
)

object SemanticIntentMatcher {
    fun match(expandedText: String, entities: ExtractedEntities): Intent {
        return matchWithDetails(expandedText, entities).intent
    }

    fun matchWithDetails(expandedText: String, entities: ExtractedEntities): IntentMatch {
        val scores = mutableMapOf<Intent, Float>()
        val reasons = mutableMapOf<Intent, MutableList<String>>()

        fun score(intent: Intent, weight: Float, reason: String) {
            scores[intent] = (scores[intent] ?: 0f) + weight
            reasons.getOrPut(intent) { mutableListOf() }.add(reason)
        }

        // --- Exact Canonical Matches (Highest Priority) ---
        if (expandedText.contains("canonical_youtube_last_played")) {
            score(Intent.YOUTUBE_LAST_PLAYED, 5.0f, "canonical YouTube last played phrase")
        }
        if (expandedText.contains("canonical_search_shorts")) {
            score(Intent.SEARCH_YOUTUBE_SHORTS, 5.0f, "canonical YouTube Shorts search phrase")
        }
        if (expandedText.contains("canonical_open_shorts")) {
            score(Intent.OPEN_SHORTS, 5.0f, "canonical open Shorts phrase")
        }
        if (expandedText.contains("canonical_open_history")) {
            score(Intent.OPEN_HISTORY, 5.0f, "canonical open YouTube history phrase")
        }
        if (expandedText.contains("canonical_open_watch_later")) {
            score(Intent.OPEN_WATCH_LATER, 5.0f, "canonical open Watch Later phrase")
        }
        if (expandedText.contains("canonical_open_liked_videos")) {
            score(Intent.OPEN_LIKED_VIDEOS, 5.0f, "canonical open liked videos phrase")
        }
        if (expandedText.contains("canonical_open_your_videos")) {
            score(Intent.OPEN_YOUR_VIDEOS, 5.0f, "canonical open your videos phrase")
        }
        if (expandedText.contains("canonical_open_playlists")) {
            score(Intent.OPEN_PLAYLISTS, 5.0f, "canonical open playlists phrase")
        }
        if (expandedText.contains("canonical_open_subscriptions")) {
            score(Intent.OPEN_SUBSCRIPTIONS, 5.0f, "canonical open subscriptions phrase")
        }
        if (expandedText.contains("canonical_upload_video")) {
            score(Intent.UPLOAD_VIDEO, 5.0f, "canonical upload video phrase")
        }
        if (expandedText.contains("canonical_create_short") || (expandedText.contains("create") && expandedText.contains("short"))) {
            score(Intent.CREATE_SHORT, 5.0f, "create Short phrase")
        }
        
        // Flexible keyword matching for YouTube
        if ((expandedText.contains("play") || expandedText.contains("resume") || expandedText.contains("continue") || expandedText.contains("open")) && 
            (expandedText.contains("last") || expandedText.contains("previous"))) {
            score(Intent.YOUTUBE_LAST_PLAYED, 4.0f, "last or previous video keywords")
        }
        if (expandedText.contains("short") && !expandedText.contains("create") && !expandedText.contains("search")) {
            score(Intent.OPEN_SHORTS, 4.0f, "Shorts keyword")
        }
        if (expandedText.contains("history")) {
            score(Intent.OPEN_HISTORY, 4.0f, "history keyword")
        }
        if (expandedText.contains("watch later")) {
            score(Intent.OPEN_WATCH_LATER, 4.0f, "watch later keyword")
        }
        if (expandedText.contains("liked")) {
            score(Intent.OPEN_LIKED_VIDEOS, 4.0f, "liked videos keyword")
        }
        if (expandedText.contains("playlist")) {
            score(Intent.OPEN_PLAYLISTS, 4.0f, "playlist keyword")
        }
        if (expandedText.contains("subscription") || expandedText.contains("sub feed")) {
            score(Intent.OPEN_SUBSCRIPTIONS, 4.0f, "subscription keyword")
        }
        if (expandedText.contains("trending") || expandedText.contains("explore")) {
            score(Intent.OPEN_YOUTUBE_TRENDING, 4.0f, "YouTube trending keyword")
        }
        if (expandedText.contains("music")) {
            score(Intent.OPEN_YOUTUBE_MUSIC, 4.0f, "music keyword")
        }
        if (expandedText.contains("gaming")) {
            score(Intent.OPEN_YOUTUBE_GAMING, 4.0f, "gaming keyword")
        }
        if (expandedText.contains("download")) {
            score(Intent.OPEN_YOUTUBE_DOWNLOADS, 4.0f, "download keyword")
        }
        if (expandedText.contains("notification")) {
            score(Intent.OPEN_YOUTUBE_NOTIFICATIONS, 4.0f, "notification keyword")
        }
        if (expandedText.contains("movies") || expandedText.contains("store")) {
            score(Intent.OPEN_YOUTUBE_MOVIES, 4.0f, "movies or store keyword")
        }
        if (expandedText.contains("like") && expandedText.contains("video")) {
            score(Intent.YOUTUBE_LIKE_VIDEO, 4.0f, "like video phrase")
        }
        if (expandedText.contains("subscribe")) {
            score(Intent.YOUTUBE_SUBSCRIBE, 4.0f, "subscribe keyword")
        }
        if (expandedText.contains("comment")) {
            score(Intent.YOUTUBE_COMMENT, 4.0f, "comment keyword")
        }
        if (entities.url != null && expandedText.contains("play")) {
            score(Intent.PLAY_VIDEO_URL, 5.0f, "play plus URL")
        }

        // Files
        if (expandedText.contains("duplicate") && (expandedText.contains("file") || expandedText.contains("delete") || expandedText.contains("remove"))) {
            score(Intent.FIND_DUPLICATE_FILES, 5.0f, "duplicate file phrase")
        }
        if (entities.targetApp != "YouTube" &&
            (expandedText.contains("find") || expandedText.contains("fetch") || expandedText.contains("locate") || expandedText.contains("search") || expandedText.contains("canonical_search")) &&
            (expandedText.contains("file") || expandedText.contains("resume") || expandedText.contains("download") || entities.fileQuery != null)
        ) {
            score(Intent.FIND_FILE, 4.5f, "file search phrase")
        }
        if ((expandedText.contains("delete") || expandedText.contains("remove")) && (expandedText.contains("file") || entities.fileQuery != null)) {
            score(Intent.DELETE_FILE, 4.5f, "file delete phrase")
        }
        if (expandedText.contains("move") && (expandedText.contains("file") || entities.fileQuery != null)) {
            score(Intent.MOVE_FILE, 4.5f, "file move phrase")
        }

        // WhatsApp management through Android app intents only.
        if (expandedText.contains("whatsapp") && expandedText.contains("settings")) {
            score(Intent.OPEN_WHATSAPP_SETTINGS, 6.0f, "WhatsApp settings section")
        }
        if (expandedText.contains("whatsapp") && expandedText.contains("communit")) {
            score(Intent.OPEN_WHATSAPP_COMMUNITIES, 6.0f, "WhatsApp communities section")
        }
        if (expandedText.contains("whatsapp") && expandedText.contains("update")) {
            score(Intent.OPEN_WHATSAPP_UPDATES, 6.0f, "WhatsApp updates section")
        }
        if (expandedText.contains("whatsapp") && expandedText.contains("call")) {
            score(Intent.OPEN_WHATSAPP_CALLS, 6.0f, "WhatsApp calls section")
        }
        if (expandedText.contains("whatsapp") && expandedText.contains("chat") &&
            (expandedText.contains("open") || expandedText.contains("show"))
        ) {
            score(Intent.OPEN_WHATSAPP_CHATS, 6.0f, "WhatsApp chats section")
        }
        if (expandedText.contains("whatsapp") && expandedText.contains("status") && expandedText.contains("video")) {
            score(Intent.SEND_WHATSAPP_STATUS, 5.0f, "WhatsApp status video phrase")
        }
        if (expandedText.contains("whatsapp") && (expandedText.contains("send") || expandedText.contains("message") || expandedText.contains("text"))) {
            score(Intent.SEND_WHATSAPP, 5.0f, "WhatsApp send/message phrase")
        }
        if (expandedText.contains("whatsapp") && expandedText.contains("mark") && expandedText.contains("read")) {
            score(Intent.MARK_WHATSAPP_READ, 6.0f, "WhatsApp mark read phrase")
        }
        if (expandedText.contains("mute") && (expandedText.contains("whatsapp") || expandedText.contains("chat") || expandedText.contains("group"))) {
            score(Intent.MUTE_WHATSAPP_CHAT, 5.0f, "WhatsApp mute chat phrase")
        }

        // --- Medium Priority (Specific Actions) ---
        if (expandedText.contains("youtube") &&
            (expandedText.contains("open") || expandedText.contains("launch") ||
                expandedText.contains("start") || expandedText.contains("take me to") ||
                expandedText.contains("play youtube") || expandedText.contains("want youtube"))
        ) {
            score(Intent.OPEN_YOUTUBE, 4.5f, "explicit YouTube launch phrase")
        }
        if (expandedText.contains("canonical_youtube_search") || (expandedText.contains("canonical_search") && expandedText.contains("canonical_target_youtube")) || (entities.query != null && entities.targetApp == "YouTube")) {
            score(Intent.SEARCH_YOUTUBE, 3.0f, "YouTube search target and query")
        }
        if (expandedText.contains("settings") && (expandedText.contains("wifi") || expandedText.contains("wi fi"))) {
            score(Intent.OPEN_SETTINGS_WIFI, 6.0f, "Wi-Fi settings phrase")
        }
        if (expandedText.contains("settings") && expandedText.contains("bluetooth")) {
            score(Intent.OPEN_SETTINGS_BLUETOOTH, 6.0f, "Bluetooth settings phrase")
        }
        if (expandedText.contains("settings") && (expandedText.contains("display") || expandedText.contains("screen"))) {
            score(Intent.OPEN_SETTINGS_DISPLAY, 6.0f, "display settings phrase")
        }
        if (expandedText.contains("settings") && (expandedText.contains("sound") || expandedText.contains("volume"))) {
            score(Intent.OPEN_SETTINGS_SOUND, 6.0f, "sound settings phrase")
        }
        if (expandedText.contains("settings") && expandedText.contains("accessibility")) {
            score(Intent.OPEN_SETTINGS_ACCESSIBILITY, 6.0f, "accessibility settings phrase")
        }
        if (expandedText.contains("settings") && (expandedText.contains("security") || expandedText.contains("privacy"))) {
            score(Intent.OPEN_SETTINGS_SECURITY, 6.0f, "security settings phrase")
        }
        if (expandedText.contains("settings") && (expandedText.contains("apps") || expandedText.contains("applications"))) {
            score(Intent.OPEN_SETTINGS_APPS, 6.0f, "apps settings phrase")
        }
        if (expandedText.contains("settings") && expandedText.contains("battery")) {
            score(Intent.OPEN_SETTINGS_BATTERY, 6.0f, "battery settings phrase")
        }
        if (expandedText.contains("settings") && expandedText.contains("storage")) {
            score(Intent.OPEN_SETTINGS_STORAGE, 6.0f, "storage settings phrase")
        }
        if (expandedText.contains("settings") && expandedText.contains("location")) {
            score(Intent.OPEN_SETTINGS_LOCATION, 6.0f, "location settings phrase")
        }
        if (expandedText.contains("settings") &&
            (expandedText.contains("date") || expandedText.contains("time"))
        ) {
            score(Intent.OPEN_SETTINGS_DATE_TIME, 6.0f, "date/time settings phrase")
        }
        if (expandedText.contains("canonical_settings") || expandedText.contains("settings")) {
            score(Intent.OPEN_SETTINGS, 4.0f, "settings keyword")
        }
        if (expandedText.contains("canonical_battery")) {
            score(Intent.CHECK_BATTERY, 4.0f, "battery keyword")
        }
        if (expandedText.contains("canonical_steps")) {
            score(Intent.CHECK_STEPS, 4.0f, "steps keyword")
        }
        if (expandedText.contains("canonical_storage")) {
            score(Intent.CHECK_STORAGE, 4.0f, "storage keyword")
        }
        if (expandedText.contains("flashlight") || expandedText.contains("torch")) {
            score(Intent.TOGGLE_FLASHLIGHT, 4.5f, "flashlight control phrase")
        }
        if (expandedText.contains("mute") && !expandedText.contains("whatsapp") &&
            !expandedText.contains("chat") && !expandedText.contains("group")
        ) {
            score(Intent.MUTE_VOLUME, 4.5f, "device mute phrase")
        }
        if (expandedText.contains("volume") || expandedText.contains("sound level")) {
            score(Intent.SET_VOLUME, 4.0f, "volume control phrase")
        }
        if (expandedText.contains("brightness")) {
            score(Intent.SET_BRIGHTNESS, 4.0f, "brightness control phrase")
        }
        if (expandedText.contains("clipboard") && (expandedText.contains("read") ||
                expandedText.contains("show") || expandedText.contains("what")))
        {
            score(Intent.READ_CLIPBOARD, 4.5f, "clipboard read phrase")
        }
        if (expandedText.contains("ram") || expandedText.contains("memory available")) {
            score(Intent.CHECK_RAM, 4.0f, "RAM status phrase")
        }
        if (expandedText.startsWith("call ") || expandedText.startsWith("dial ")) {
            score(Intent.CALL_CONTACT, 5.0f, "call contact phrase")
        }
        if ((expandedText.startsWith("send sms") || expandedText.startsWith("text ")) &&
            !expandedText.contains("whatsapp")
        ) {
            score(Intent.SMS_CONTACT, 5.0f, "SMS contact phrase")
        }
        if ((expandedText.contains("open") || expandedText.contains("launch")) &&
            expandedText.contains("whatsapp")
        ) {
            score(Intent.OPEN_WHATSAPP, 5.0f, "explicit WhatsApp launch phrase")
        }
        if (expandedText.contains("timer")) {
            score(Intent.SET_TIMER, 4.5f, "timer phrase")
        }
        if (expandedText.contains("stopwatch")) {
            score(Intent.START_STOPWATCH, 4.5f, "stopwatch phrase")
        }
        if (expandedText.contains("calendar") && !expandedText.contains("add") &&
            !expandedText.contains("create")
        ) {
            score(Intent.OPEN_CALENDAR, 4.0f, "calendar launch phrase")
        }
        if (expandedText == "back" || expandedText.contains("go back") || expandedText.contains("press back")) {
            score(Intent.PHONE_BACK, 5.0f, "phone back control phrase")
        }
        if (expandedText == "home" || expandedText.contains("go home") || expandedText.contains("press home")) {
            score(Intent.PHONE_HOME, 5.0f, "phone home control phrase")
        }
        if (expandedText.contains("notification shade") || expandedText.contains("open notifications") || expandedText.contains("show notifications")) {
            score(Intent.OPEN_NOTIFICATION_SHADE, 5.0f, "notification shade control phrase")
        }
        if ((expandedText.contains("read") || expandedText.contains("list") || expandedText.contains("what")) &&
            (expandedText.contains("notification") || expandedText.contains("notifications"))
        ) {
            score(Intent.READ_NOTIFICATIONS, 6.0f, "notification reading phrase")
        }
        if ((expandedText.contains("enable") || expandedText.contains("open")) &&
            expandedText.contains("notification") &&
            expandedText.contains("access")
        ) {
            score(Intent.OPEN_NOTIFICATION_ACCESS_SETUP, 6.0f, "notification access setup phrase")
        }
        if (expandedText.contains("quick settings") || expandedText.contains("control center")) {
            score(Intent.OPEN_QUICK_SETTINGS, 5.0f, "quick settings control phrase")
        }
        if ((expandedText.contains("enable") || expandedText.contains("open")) &&
            (expandedText.contains("accessibility") || expandedText.contains("full access") || expandedText.contains("phone access"))
        ) {
            score(Intent.OPEN_ACCESSIBILITY_SETUP, 5.0f, "accessibility setup phrase")
        }
        if (expandedText.startsWith("tap ") || expandedText.startsWith("click ") || expandedText.startsWith("press ") || expandedText.startsWith("select ")) {
            if (entities.uiIndex != null) {
                score(Intent.TAP_INDEX, 5.5f, "tap visible index phrase")
            } else {
                score(Intent.TAP_TEXT, 5.0f, "tap visible text phrase")
            }
        }
        if (expandedText.startsWith("type ") || expandedText.startsWith("enter ") || expandedText.startsWith("write ")) {
            score(Intent.TYPE_TEXT, 5.0f, "type text phrase")
        }
        if (expandedText.contains("scroll down") || expandedText.contains("swipe up")) {
            score(Intent.SCROLL_DOWN, 5.0f, "scroll down phrase")
        }
        if (expandedText.contains("scroll up") || expandedText.contains("swipe down")) {
            score(Intent.SCROLL_UP, 5.0f, "scroll up phrase")
        }
        if (expandedText.contains("what's on screen") ||
            expandedText.contains("what is on screen") ||
            expandedText.contains("read screen") ||
            expandedText.contains("describe screen") ||
            expandedText.contains("screen snapshot") ||
            expandedText.contains("list buttons")
        ) {
            score(Intent.READ_SCREEN, 5.0f, "screen snapshot phrase")
        }
        if (entities.recipeName != null ||
            expandedText.startsWith("inspect ") ||
            expandedText.startsWith("run recipe ") ||
            expandedText.contains("then read screen") ||
            expandedText.contains("and read screen")
        ) {
            score(Intent.RUN_PHONE_RECIPE, 6.0f, "phone recipe phrase")
        }
        if (expandedText.contains("alarm") || expandedText.contains("wake me")) {
            score(Intent.SET_ALARM, 2.0f, "alarm keyword")
            if (entities.repeatDays != null) score(Intent.SET_ALARM, 0.5f, "repeat days extracted")
            if (entities.timeHour != null) score(Intent.SET_ALARM, 0.5f, "time extracted")
        }
        if (expandedText.contains("remind") ||
            expandedText.contains("notify") ||
            expandedText.contains("tell") ||
            expandedText.contains("say") ||
            expandedText.contains("ring") ||
            entities.conditionType != null
        ) {
            score(Intent.CREATE_WATCHER, 2.0f, "watcher reminder keyword")
            if (entities.conditionType != null) score(Intent.CREATE_WATCHER, 1.5f, "condition type extracted")
            if (entities.thresholdValue != null) score(Intent.CREATE_WATCHER, 1.0f, "threshold extracted")
        }
        if (expandedText.contains("whats next") ||
            expandedText.contains("what's next") ||
            expandedText.contains("what is next") ||
            expandedText.contains("next step") ||
            expandedText.contains("what should") && expandedText.contains("next") ||
            expandedText.contains("build next") ||
            expandedText.contains("next skill")
        ) {
            score(Intent.WHAT_IS_NEXT, 4.0f, "next step guidance phrase")
        }
        if (expandedText.contains("capability") ||
            expandedText.contains("capabilities") ||
            expandedText.contains("what can you do") ||
            expandedText.contains("what do you do") ||
            expandedText.contains("commands") ||
            expandedText.contains("headless") && expandedText.contains("do")
        ) {
            score(Intent.QUERY_CAPABILITIES, 4.5f, "capability query phrase")
        }
        if (expandedText.contains("permission") ||
            expandedText.contains("permissions") ||
            expandedText.contains("access status") ||
            expandedText.contains("what access") ||
            expandedText.contains("enabled access")
        ) {
            score(Intent.QUERY_PERMISSION_STATUS, 5.0f, "permission status phrase")
        }
        if (expandedText.contains("last screen") || expandedText.contains("previous screen snapshot")) {
            score(Intent.QUERY_LAST_SCREEN, 6.0f, "last screen memory phrase")
        }
        if (expandedText.contains("last recipe") || expandedText.contains("previous recipe") || expandedText.contains("last phone task")) {
            score(Intent.QUERY_LAST_RECIPE, 6.0f, "last recipe memory phrase")
        }

        // --- Low Priority (Generic App Open) ---
        if (Regex("^(?:please\\s+)?(?:open|launch|start|show|use|go to)\\s+").containsMatchIn(expandedText)) {
            score(Intent.OPEN_APP, 3.5f, "generic installed or native app launch phrase")
        }
        if (expandedText.contains("youtube")) {
            score(Intent.OPEN_YOUTUBE, 1.0f, "YouTube keyword")
        }
        if (expandedText.contains("browser") || expandedText.contains("internet")) {
            score(Intent.OPEN_BROWSER, 1.0f, "browser or internet keyword")
        }
        if ((expandedText.startsWith("search ") || expandedText.startsWith("google ") || expandedText.contains("web search")) &&
            entities.targetApp != "YouTube" &&
            entities.fileQuery == null
        ) {
            score(Intent.SEARCH_WEB, 4.0f, "web search phrase")
        }
        if (expandedText.contains("camera") || expandedText.contains("photo")) {
            score(Intent.TAKE_PHOTO, 1.0f, "camera or photo keyword")
        }
        if (expandedText.contains("video") && expandedText.contains("record")) {
            score(Intent.RECORD_VIDEO, 1.0f, "record video phrase")
        }
        if (expandedText.contains("voice") && expandedText.contains("record")) {
            score(Intent.RECORD_VOICE, 1.0f, "record voice phrase")
        }

        val best = scores.maxByOrNull { it.value }
        if (best == null) {
            return IntentMatch(Intent.UNKNOWN, 0f, 0f, emptyList())
        }

        val runnerUpScore = scores
            .filterKeys { it != best.key }
            .values
            .maxOrNull() ?: 0f
        val margin = best.value - runnerUpScore
        val confidence = confidenceFor(best.value, margin)

        return IntentMatch(
            intent = best.key,
            confidence = confidence,
            score = best.value,
            reasons = reasons[best.key].orEmpty()
        )
    }

    private fun confidenceFor(score: Float, margin: Float): Float {
        val base = when {
            score >= 5.0f -> 0.95f
            score >= 4.0f -> 0.86f
            score >= 3.0f -> 0.78f
            score >= 2.0f -> 0.62f
            else -> 0.35f
        }
        val marginAdjustment = when {
            margin >= 2.0f -> 0.04f
            margin >= 1.0f -> 0.01f
            margin <= 0f -> -0.10f
            else -> -0.04f
        }
        return (base + marginAdjustment).coerceIn(0f, 0.99f)
    }
}
