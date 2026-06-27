package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent as AndroidIntent
import android.net.Uri
import android.provider.MediaStore
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import kotlinx.coroutines.launch

class YouTubeSkill(private val context: Context) : Skill {
    override val id = "android.youtube"
    override val name = "YouTube"
    override val description = "YouTube Deep Links"

    override fun canHandle(intent: Intent): Boolean {
        return intent in listOf(
            Intent.OPEN_YOUTUBE,
            Intent.SEARCH_YOUTUBE,
            Intent.SEARCH_YOUTUBE_SHORTS,
            Intent.OPEN_SHORTS,
            Intent.OPEN_HISTORY,
            Intent.OPEN_WATCH_LATER,
            Intent.OPEN_LIKED_VIDEOS,
            Intent.OPEN_YOU_PAGE,
            Intent.OPEN_PLAYLISTS,
            Intent.OPEN_YOUR_VIDEOS,
            Intent.OPEN_SUBSCRIPTIONS,
            Intent.PLAY_VIDEO_URL,
            Intent.UPLOAD_VIDEO,
            Intent.CREATE_SHORT,
            Intent.OPEN_YOUTUBE_SETTINGS,
            Intent.OPEN_YOUTUBE_TRENDING,
            Intent.OPEN_YOUTUBE_MUSIC,
            Intent.OPEN_YOUTUBE_GAMING,
            Intent.OPEN_YOUTUBE_MOVIES,
            Intent.OPEN_YOUTUBE_DOWNLOADS,
            Intent.OPEN_YOUTUBE_NOTIFICATIONS,
            Intent.YOUTUBE_LIKE_VIDEO,
            Intent.YOUTUBE_SUBSCRIBE,
            Intent.YOUTUBE_COMMENT
        )
    }

    override fun execute(request: SkillRequest): SkillResult {
        if (request.command.intentType in listOf(
                Intent.CREATE_SHORT, 
                Intent.YOUTUBE_LIKE_VIDEO,
                Intent.YOUTUBE_SUBSCRIBE,
                Intent.YOUTUBE_COMMENT
            )) {
            return SkillResult.Failure("This action needs Accessibility control. It is not enabled yet.")
        }

        var humanMessage = "Opening YouTube."
        val i: AndroidIntent = when (request.command.intentType) {
            Intent.OPEN_YOUTUBE -> {
                context.packageManager.getLaunchIntentForPackage(YouTubeDeepLinks.PACKAGE_NAME)
                    ?: AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
            }
            Intent.SEARCH_YOUTUBE -> {
                val q = request.command.query ?: return SkillResult.Failure("I need to know what to search for on YouTube.")
                humanMessage = "Searching YouTube for $q."
                YouTubeDeepLinks.getSearchIntent(q)
            }
            Intent.SEARCH_YOUTUBE_SHORTS -> {
                val q = request.command.query ?: return SkillResult.Failure("I need to know what to search for.")
                humanMessage = "Searching YouTube Shorts for $q."
                val i2 = YouTubeDeepLinks.getSearchIntent(q)
                i2.putExtra("query", "$q shorts")
                i2
            }
            Intent.OPEN_SHORTS -> webSection("https://www.youtube.com/shorts/", "Opening Shorts.")
            Intent.OPEN_HISTORY -> webSection("https://www.youtube.com/feed/history", "Opening YouTube history.")
            Intent.OPEN_PLAYLISTS -> webSection("https://www.youtube.com/feed/playlists", "Opening playlists.")
            Intent.OPEN_SUBSCRIPTIONS -> webSection("https://www.youtube.com/feed/subscriptions", "Opening subscriptions.")
            Intent.OPEN_YOU_PAGE -> webSection("https://www.youtube.com/feed/you", "Opening your YouTube page.")
            Intent.OPEN_YOUTUBE_TRENDING -> webSection("https://www.youtube.com/feed/trending", "Opening trending.")
            Intent.OPEN_YOUTUBE_GAMING -> webSection("https://www.youtube.com/gaming", "Opening YouTube Gaming.")
            Intent.OPEN_YOUTUBE_MOVIES -> webSection("https://www.youtube.com/feed/storefront", "Opening YouTube Movies.")
            Intent.OPEN_YOUTUBE_DOWNLOADS -> return openYouTubeSection("Downloads")
            Intent.OPEN_YOUTUBE_NOTIFICATIONS -> return openYouTubeSection("Notifications")
            Intent.OPEN_YOUTUBE_SETTINGS -> return openYouTubeSection("Settings")
            Intent.OPEN_WATCH_LATER -> {
                humanMessage = "Opening Watch Later."
                YouTubeDeepLinks.getWatchLaterIntent()
            }
            Intent.OPEN_LIKED_VIDEOS -> {
                humanMessage = "Opening Liked Videos."
                YouTubeDeepLinks.getLikedVideosIntent()
            }
            Intent.OPEN_YOUR_VIDEOS -> {
                humanMessage = "Opening Your Videos."
                AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("https://studio.youtube.com/")).apply { setPackage(YouTubeDeepLinks.PACKAGE_NAME) }
            }
            Intent.PLAY_VIDEO_URL -> {
                val url = request.command.url ?: return SkillResult.Failure("No URL provided")
                humanMessage = "Playing video."
                YouTubeDeepLinks.getPlayVideoUrlIntent(url)
            }
            Intent.UPLOAD_VIDEO -> {
                humanMessage = "Opening video upload."
                AndroidIntent(AndroidIntent.ACTION_SEND).apply {
                    type = "video/*"
                    setPackage(YouTubeDeepLinks.PACKAGE_NAME)
                }
            }
            Intent.OPEN_YOUTUBE_MUSIC -> {
                humanMessage = "Opening YouTube Music."
                YouTubeDeepLinks.getMusicIntent()
            }
            else -> return SkillResult.Failure("Unsupported YouTube operation.")
        }

        i.flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK

        return try {
            context.startActivity(i)

            // Log Memory
            val memoryStore = com.emeth.kernel.memory.MemoryStore(context)
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                val action = if (request.command.intentType == Intent.SEARCH_YOUTUBE) "search" else "play"
                val meta = buildString {
                    append("{\"package\":\"com.google.android.youtube\", \"action\":\"$action\"")
                    if (request.command.query != null) append(", \"query\":\"${request.command.query!!.replace("\"", "\\\"")}\"")
                    if (request.command.url != null) append(", \"url\":\"${request.command.url}\"")
                    
                    // Rule 4: STORE PLAY MEMORY CORRECTLY - extract videoId if playing URL
                    if (request.command.url != null && request.command.url!!.contains("v=")) {
                         val vid = request.command.url!!.substringAfter("v=").substringBefore("&")
                         append(", \"videoId\":\"$vid\"")
                    }
                    append("}")
                }
                memoryStore.store(
                    com.emeth.kernel.memory.MemoryType.APP_USAGE,
                    "youtube",
                    meta
                )
            }
            SkillResult.Success(humanMessage)
        } catch (e: Exception) {
            if (request.command.intentType == Intent.OPEN_SHORTS) {
                 try {
                     val fallback = AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("vnd.youtube://shorts"))
                     fallback.flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
                     context.startActivity(fallback)
                     SkillResult.Success(humanMessage)
                 } catch (ex: Exception) {
                     SkillResult.Failure("I can open YouTube, but Shorts deep-linking is not supported on this device yet.")
                 }
            } else {
                try {
                    i.setPackage(null) // allow browser to handle it
                    context.startActivity(i)
                    SkillResult.Success(humanMessage)
                } catch (ex: Exception) {
                    SkillResult.Failure("Failed to execute YouTube action.", ex)
                }
            }
        }
    }

    private fun webSection(url: String, message: String): AndroidIntent {
        return AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage(YouTubeDeepLinks.PACKAGE_NAME)
            putExtra("emeth_message", message)
        }
    }

    private fun openYouTubeSection(label: String): SkillResult {
        val launch = context.packageManager.getLaunchIntentForPackage(YouTubeDeepLinks.PACKAGE_NAME)
            ?: return SkillResult.Failure("YouTube is not installed.")
        launch.flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(launch)
        Thread.sleep(900)
        return if (com.emeth.kernel.access.EmethAccessibilityService.tapText(label)) {
            SkillResult.Success("Opened YouTube $label.")
        } else {
            SkillResult.Partial(
                "Opened YouTube. Enable Accessibility access so Emeth can select $label inside YouTube."
            )
        }
    }
}

class LastPlayedYouTubeSkill(private val context: Context) : Skill {
    override val id = "android.youtube.lastplayed"
    override val name = "Last Played YouTube Video"
    override val description = "Plays the last played YouTube video"

    override fun canHandle(intent: Intent) = intent == Intent.YOUTUBE_LAST_PLAYED

    override fun execute(request: SkillRequest): SkillResult {
        var lastVideoMemory: com.emeth.kernel.memory.MemoryEntry? = null
        kotlinx.coroutines.runBlocking {
            val engine = com.emeth.kernel.memory.MemoryQueryEngine(context)
            val memories = engine.queryByType(com.emeth.kernel.memory.MemoryType.APP_USAGE)
            
            lastVideoMemory = memories.firstOrNull { 
                it.content == "youtube" && it.metadata != null && it.metadata.contains("\"action\":\"play\"") && (it.metadata.contains("\"url\"") || it.metadata.contains("\"videoId\""))
            }
        }
        
        if (lastVideoMemory == null) {
            return SkillResult.Failure("I don’t have a YouTube video saved yet. Open a video through Emeth first.")
        }
        
        val meta = lastVideoMemory!!.metadata ?: ""
        val urlRegex = "\"url\":\"([^\"]+)\"".toRegex()
        val match = urlRegex.find(meta)
        val url = match?.groupValues?.get(1) ?: return SkillResult.Failure("I don’t have a YouTube video saved yet. Open a video through Emeth first.")
        
        val i = YouTubeDeepLinks.getPlayVideoUrlIntent(url)
        context.startActivity(i)
        
        return SkillResult.Success("Playing your last video.")
    }
}

class CameraPhotoSkill(private val context: Context) : Skill {
    override val id = "android.camera.photo"
    override val name = "Take Photo"
    override val description = "Opens camera to take a photo"

    override fun canHandle(intent: Intent) = intent == Intent.TAKE_PHOTO

    override fun execute(request: SkillRequest): SkillResult {
        val i = AndroidIntent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success("Opening Camera.")
    }
}

class CameraVideoSkill(private val context: Context) : Skill {
    override val id = "android.camera.video"
    override val name = "Record Video"
    override val description = "Opens camera to record video"

    override fun canHandle(intent: Intent) = intent == Intent.RECORD_VIDEO

    override fun execute(request: SkillRequest): SkillResult {
        val i = AndroidIntent(MediaStore.INTENT_ACTION_VIDEO_CAMERA).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success("Opening Camera for video.")
    }
}

class VoiceRecordSkill(private val context: Context) : Skill {
    override val id = "android.voice.record"
    override val name = "Record Voice Note"
    override val description = "Opens voice recorder"

    override fun canHandle(intent: Intent) = intent == Intent.RECORD_VOICE

    override fun execute(request: SkillRequest): SkillResult {
        val i = AndroidIntent(MediaStore.Audio.Media.RECORD_SOUND_ACTION).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(i)
            return SkillResult.Success("Opening Voice Recorder.")
        } catch (e: Exception) {
            return SkillResult.Failure("No voice recorder app found on this device.", e)
        }
    }
}
