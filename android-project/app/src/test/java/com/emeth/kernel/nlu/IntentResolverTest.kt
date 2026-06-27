package com.emeth.kernel.nlu

import com.emeth.kernel.intents.Intent
import com.emeth.kernel.intents.IntentResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IntentResolverTest {
    private val resolver = IntentResolver()

    private fun assertIntent(input: String, expected: Intent) {
        val parsed = resolver.resolve(input)
        assertEquals("Failed on: $input", expected, parsed.intentType)
    }

    @Test
    fun testYouTubeOpen() {
        val tests = listOf(
            "Open YouTube",
            "Launch YouTube",
            "Start YouTube",
            "Can you open YouTube",
            "I want YouTube",
            "Take me to YouTube",
            "Play YouTube"
        )
        for (t in tests) assertIntent(t, Intent.OPEN_YOUTUBE)
    }

    @Test
    fun testSettingsOpen() {
        val tests = listOf(
            "Open settings",
            "Go to settings",
            "Show settings",
            "Take me to settings"
        )
        for (t in tests) assertIntent(t, Intent.OPEN_SETTINGS)
    }

    @Test
    fun testBattery() {
        val tests = listOf(
            "Battery",
            "Battery percentage",
            "How much battery do I have",
            "What's my battery level",
            "How much charge left"
        )
        for (t in tests) assertIntent(t, Intent.CHECK_BATTERY)
    }

    @Test
    fun testSteps() {
        val tests = listOf(
            "How many steps today",
            "Steps today",
            "How far have I walked today",
            "What is my step count",
            "How much did I walk today"
        )
        for (t in tests) assertIntent(t, Intent.CHECK_STEPS)
    }

    @Test
    fun testAlarms() {
        val tests = listOf(
            "Set alarm 5:30am everyday",
            "Wake me up every day at 5:30",
            "I want a daily alarm at 5:30am",
            "Create recurring alarm for 5:30",
            "Set alarm weekdays at 6am",
            "Wake me up Monday to Friday at 6"
        )
        for (t in tests) {
            val parsed = resolver.resolve(t)
            assertEquals("Failed on: $t", Intent.SET_ALARM, parsed.intentType)
            assertEquals("Missing repeat on: $t", true, parsed.repeatDays != null)
        }
    }

    @Test
    fun testYouTubeLastPlayed() {
        val tests = listOf(
            "Play last played YouTube video",
            "Continue my last YouTube video",
            "Resume previous YouTube video",
            "Open the last video I watched",
            "Continue YouTube",
            "Resume YouTube"
        )
        for (t in tests) assertIntent(t, Intent.YOUTUBE_LAST_PLAYED)
    }

    @Test
    fun testYouTubeSearch() {
        val tests = listOf(
            "Search YouTube for Ronaldo",
            "Find Ronaldo on YouTube",
            "Look up Ronaldo in YouTube",
            "YouTube Ronaldo"
        )
        for (t in tests) {
            val parsed = resolver.resolve(t)
            assertEquals("Failed on: $t", Intent.SEARCH_YOUTUBE, parsed.intentType)
            assertEquals("Missing query on: $t", "ronaldo", parsed.query?.lowercase())
        }
    }

    @Test
    fun testWatchers() {
        val tests = listOf(
            "Remind me when I hit 8000 steps",
            "Tell me when I reach 8k steps",
            "Notify me at 10000 steps",
            "Ring when I hit 8000 steps"
        )
        for (t in tests) {
            val parsed = resolver.resolve(t)
            assertEquals("Failed on: $t", Intent.CREATE_WATCHER, parsed.intentType)
            assertEquals("Missing threshold on: $t", true, parsed.thresholdValue != null)
        }
    }

    @Test
    fun testTimeAndBatteryAutomations() {
        val timed = resolver.resolve("say good morning at 6am")
        assertEquals(Intent.CREATE_WATCHER, timed.intentType)
        assertEquals("TIME_OF_DAY", timed.conditionType)
        assertEquals(360f, timed.thresholdValue)
        assertEquals("good morning", timed.message)

        val battery = resolver.resolve("notify me when battery drops to 20%")
        assertEquals(Intent.CREATE_WATCHER, battery.intentType)
        assertEquals("BATTERY_LEVEL", battery.conditionType)
        assertEquals(20f, battery.thresholdValue)
        assertEquals("LESS_THAN_EQUAL", battery.conditionOp)

        val highBattery = resolver.resolve("notify me when battery hit 95%")
        assertEquals(Intent.CREATE_WATCHER, highBattery.intentType)
        assertEquals("BATTERY_LEVEL", highBattery.conditionType)
        assertEquals(95f, highBattery.thresholdValue)
        assertEquals("GREATER_THAN_EQUAL", highBattery.conditionOp)
    }

    @Test
    fun testYouTubeOpenShorts() {
        val tests = listOf(
            "open shorts",
            "play shorts",
            "open youtube shorts"
        )
        for (t in tests) assertIntent(t, Intent.OPEN_SHORTS)
    }

    @Test
    fun testYouTubeDeepLinks() {
        assertIntent("open youtube history", Intent.OPEN_HISTORY)
        assertIntent("show youtube history", Intent.OPEN_HISTORY)
        assertIntent("open watch later", Intent.OPEN_WATCH_LATER)
        assertIntent("show my watch later on youtube", Intent.OPEN_WATCH_LATER)
        assertIntent("open liked videos on youtube", Intent.OPEN_LIKED_VIDEOS)
        assertIntent("open your videos on youtube", Intent.OPEN_YOUR_VIDEOS)
        assertIntent("open youtube playlists", Intent.OPEN_PLAYLISTS)
        assertIntent("open youtube subscriptions", Intent.OPEN_SUBSCRIPTIONS)
    }

    @Test
    fun testYouTubeCreation() {
        assertIntent("upload video to youtube", Intent.UPLOAD_VIDEO)
        assertIntent("create youtube short", Intent.CREATE_SHORT)
        assertIntent("make a short", Intent.CREATE_SHORT)
    }

    @Test
    fun testSmartFileManagement() {
        val resume = resolver.resolve("fetch resume")
        assertEquals(Intent.FIND_FILE, resume.intentType)
        assertEquals("resume", resume.fileQuery)

        assertIntent("delete duplicate files smartly", Intent.FIND_DUPLICATE_FILES)
        assertIntent("move file latest resume to documents", Intent.MOVE_FILE)
    }

    @Test
    fun testWhatsAppManagement() {
        assertIntent("send hello to whatsapp", Intent.SEND_WHATSAPP)
        assertIntent("send latest downloaded video to whatsapp status", Intent.SEND_WHATSAPP_STATUS)
        assertIntent("mark all unread whatsapp messages as read", Intent.MARK_WHATSAPP_READ)
        assertIntent("mute warriors whatsapp group", Intent.MUTE_WHATSAPP_CHAT)

        val parsed = resolver.resolve("send message \"I am on my way\" to whatsapp")
        assertEquals(Intent.SEND_WHATSAPP, parsed.intentType)
        assertEquals("I am on my way".lowercase(), parsed.message?.lowercase())
    }

    @Test
    fun testCurrentWhatsAppSections() {
        assertIntent("open whatsapp chats", Intent.OPEN_WHATSAPP_CHATS)
        assertIntent("open whatsapp updates", Intent.OPEN_WHATSAPP_UPDATES)
        assertIntent("open whatsapp communities", Intent.OPEN_WHATSAPP_COMMUNITIES)
        assertIntent("open whatsapp calls", Intent.OPEN_WHATSAPP_CALLS)
        assertIntent("open whatsapp settings", Intent.OPEN_WHATSAPP_SETTINGS)
    }

    @Test
    fun testExtendedAndroid16SettingsPhrases() {
        val commands = mapOf(
            "open vpn settings" to Intent.OPEN_SETTINGS,
            "open nfc settings" to Intent.OPEN_SETTINGS,
            "open default apps settings" to Intent.OPEN_SETTINGS_APPS,
            "open developer options settings" to Intent.OPEN_SETTINGS,
            "open about phone settings" to Intent.OPEN_SETTINGS,
            "open privacy settings" to Intent.OPEN_SETTINGS_SECURITY,
            "open data usage settings" to Intent.OPEN_SETTINGS,
            "open notification access settings" to Intent.OPEN_NOTIFICATION_ACCESS_SETUP,
            "open install unknown apps settings" to Intent.OPEN_SETTINGS_APPS,
            "open do not disturb settings" to Intent.OPEN_SETTINGS
        )
        commands.forEach { (command, expected) -> assertIntent(command, expected) }
    }

    @Test
    fun testAndroidNoteContract() {
        val note = resolver.resolve("create note buy milk and bread")
        assertEquals(Intent.CREATE_NOTE, note.intentType)
        assertTrue(note.confidence >= 0.75f)

        assertIntent("take a note saying call mom", Intent.CREATE_NOTE)
        assertIntent("note remember passport", Intent.CREATE_NOTE)
    }

    @Test
    fun testWhatsNextGuidance() {
        assertIntent("what's next", Intent.WHAT_IS_NEXT)
        assertIntent("next step", Intent.WHAT_IS_NEXT)
        assertIntent("build next skill", Intent.WHAT_IS_NEXT)
        assertIntent("what should we build next", Intent.WHAT_IS_NEXT)
    }

    @Test
    fun testCapabilityGuidance() {
        assertIntent("what can you do headlessly", Intent.QUERY_CAPABILITIES)
        assertIntent("show capabilities", Intent.QUERY_CAPABILITIES)
        assertIntent("list commands", Intent.QUERY_CAPABILITIES)
        assertIntent("permission status", Intent.QUERY_PERMISSION_STATUS)
        assertIntent("what access is enabled", Intent.QUERY_PERMISSION_STATUS)
    }

    @Test
    fun testPhoneControlIntents() {
        assertIntent("go home", Intent.PHONE_HOME)
        assertIntent("go back", Intent.PHONE_BACK)
        assertIntent("open notifications", Intent.OPEN_NOTIFICATION_SHADE)
        assertIntent("open quick settings", Intent.OPEN_QUICK_SETTINGS)
        assertIntent("enable full access", Intent.OPEN_ACCESSIBILITY_SETUP)
        assertIntent("read notifications", Intent.READ_NOTIFICATIONS)
        assertIntent("enable notification access", Intent.OPEN_NOTIFICATION_ACCESS_SETUP)

        val tap = resolver.resolve("tap Allow")
        assertEquals(Intent.TAP_TEXT, tap.intentType)
        assertEquals("allow", tap.uiTarget)

        val tapIndex = resolver.resolve("tap 3")
        assertEquals(Intent.TAP_INDEX, tapIndex.intentType)
        assertEquals(3, tapIndex.uiIndex)

        val type = resolver.resolve("type hello there")
        assertEquals(Intent.TYPE_TEXT, type.intentType)
        assertEquals("hello there", type.uiTarget)

        assertIntent("scroll down", Intent.SCROLL_DOWN)
        assertIntent("scroll up", Intent.SCROLL_UP)
        assertIntent("what's on screen", Intent.READ_SCREEN)
        assertIntent("list buttons", Intent.READ_SCREEN)
    }

    @Test
    fun testWebSearchIntent() {
        val parsed = resolver.resolve("search weather in Delhi")
        assertEquals(Intent.SEARCH_WEB, parsed.intentType)
        assertEquals("weather in delhi", parsed.query)
    }

    @Test
    fun testPhoneRecipeIntents() {
        val inspect = resolver.resolve("inspect whatsapp")
        assertEquals(Intent.RUN_PHONE_RECIPE, inspect.intentType)
        assertEquals("open_and_read", inspect.recipeName)

        val search = resolver.resolve("search weather in Delhi then read screen")
        assertEquals(Intent.RUN_PHONE_RECIPE, search.intentType)
        assertEquals("search_and_read", search.recipeName)

        val whatsapp = resolver.resolve("prepare whatsapp message hello there")
        assertEquals(Intent.RUN_PHONE_RECIPE, whatsapp.intentType)
        assertEquals("prepare_whatsapp_message", whatsapp.recipeName)
    }

    @Test
    fun testRecipeMemoryIntents() {
        assertIntent("last screen", Intent.QUERY_LAST_SCREEN)
        assertIntent("previous screen snapshot", Intent.QUERY_LAST_SCREEN)
        assertIntent("last recipe", Intent.QUERY_LAST_RECIPE)
        assertIntent("last phone task", Intent.QUERY_LAST_RECIPE)
    }

    @Test
    fun testConfidenceReflectsMatchStrength() {
        val strong = resolver.resolve("send latest downloaded video to whatsapp status")
        assertEquals(Intent.SEND_WHATSAPP_STATUS, strong.intentType)
        assertTrue("Expected high confidence, got ${strong.confidence}", strong.confidence >= 0.84f)
        assertTrue(strong.matchReasons.isNotEmpty())

        val weak = resolver.resolve("youtube")
        assertEquals(Intent.OPEN_YOUTUBE, weak.intentType)
        assertTrue("Expected weak confidence, got ${weak.confidence}", weak.confidence < 0.45f)

        val unknown = resolver.resolve("purple banana orbit")
        assertEquals(Intent.UNKNOWN, unknown.intentType)
        assertEquals(0f, unknown.confidence)
    }

    @Test
    fun testCommonCommandsAreExecutableConfidence() {
        val commands = listOf(
            "open youtube" to Intent.OPEN_YOUTUBE,
            "open settings" to Intent.OPEN_SETTINGS,
            "battery percentage" to Intent.CHECK_BATTERY,
            "turn on flashlight" to Intent.TOGGLE_FLASHLIGHT,
            "mute phone" to Intent.MUTE_VOLUME,
            "show volume" to Intent.SET_VOLUME,
            "adjust brightness" to Intent.SET_BRIGHTNESS,
            "read clipboard" to Intent.READ_CLIPBOARD,
            "check ram" to Intent.CHECK_RAM,
            "call Satya" to Intent.CALL_CONTACT,
            "open whatsapp" to Intent.OPEN_WHATSAPP,
            "set a timer" to Intent.SET_TIMER
        )

        for ((input, expected) in commands) {
            val parsed = resolver.resolve(input)
            assertEquals("Failed on: $input", expected, parsed.intentType)
            assertTrue("Planner would reject '$input' at ${parsed.confidence}", parsed.confidence >= 0.75f)
        }
    }

    @Test
    fun testContactExtraction() {
        val call = resolver.resolve("call Satya")
        assertEquals("satya", call.contactName)

        val whatsapp = resolver.resolve("send hello to Satya on whatsapp")
        assertEquals(Intent.SEND_WHATSAPP, whatsapp.intentType)
        assertEquals("satya", whatsapp.contactName)
    }

    @Test
    fun testTimerDurationExtraction() {
        val minutes = resolver.resolve("set a timer for 12 minutes")
        assertEquals(Intent.SET_TIMER, minutes.intentType)
        assertEquals(720, minutes.durationSeconds)

        val seconds = resolver.resolve("timer for 30 seconds")
        assertEquals(30, seconds.durationSeconds)

        val hours = resolver.resolve("set timer for 2 hours")
        assertEquals(7200, hours.durationSeconds)
    }

    @Test
    fun testAndroidNativeAndOemAppLaunchMap() {
        val appNames = listOf(
            "phone",
            "contacts",
            "messages",
            "email",
            "camera",
            "gallery",
            "files",
            "downloads",
            "clock",
            "calculator",
            "maps",
            "music",
            "calendar",
            "browser",
            "notes",
            "weather",
            "recorder"
        )

        for (appName in appNames) {
            val parsed = resolver.resolve("open $appName")
            assertTrue("Failed native app: $appName", parsed.intentType != Intent.UNKNOWN)
            if (parsed.intentType == Intent.OPEN_APP) {
                assertEquals(appName, parsed.targetApp)
            }
            assertTrue("Low confidence for $appName", parsed.confidence >= 0.75f)
        }

        val show = resolver.resolve("show the calculator app")
        assertEquals(Intent.OPEN_APP, show.intentType)
        assertEquals("calculator", show.targetApp)
    }

    @Test
    fun testAndroidSettingsIntentMap() {
        val commands = mapOf(
            "open wifi settings" to Intent.OPEN_SETTINGS_WIFI,
            "open bluetooth settings" to Intent.OPEN_SETTINGS_BLUETOOTH,
            "open display settings" to Intent.OPEN_SETTINGS_DISPLAY,
            "open sound settings" to Intent.OPEN_SETTINGS_SOUND,
            "open accessibility settings" to Intent.OPEN_SETTINGS_ACCESSIBILITY,
            "open security settings" to Intent.OPEN_SETTINGS_SECURITY,
            "open apps settings" to Intent.OPEN_SETTINGS_APPS,
            "open battery settings" to Intent.OPEN_SETTINGS_BATTERY,
            "open storage settings" to Intent.OPEN_SETTINGS_STORAGE,
            "open location settings" to Intent.OPEN_SETTINGS_LOCATION,
            "open date and time settings" to Intent.OPEN_SETTINGS_DATE_TIME
        )
        for ((command, expected) in commands) {
            assertIntent(command, expected)
        }
    }
}
