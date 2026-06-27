package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent as AndroidIntent
import android.net.Uri
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import java.net.URLEncoder

class WhatsAppMessageSkill(private val context: Context) : Skill {
    override val id = "app.whatsapp.message"
    override val name = "WhatsApp Message"
    override val description = "Sends a message via WhatsApp"
    
    override fun canHandle(intent: Intent) = intent == Intent.SEND_WHATSAPP

    override fun execute(request: SkillRequest): SkillResult {
        val phone = request.command.contactName ?: return SkillResult.Failure("Phone number required")
        val message = request.command.message ?: ""
        
        val uri = Uri.parse("whatsapp://send?phone=$phone&text=${URLEncoder.encode(message, "UTF-8")}")
        val intent = AndroidIntent(AndroidIntent.ACTION_VIEW, uri).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            setPackage("com.whatsapp")
        }
        
        return try {
            context.startActivity(intent)
            SkillResult.Success("Opened WhatsApp to send message")
        } catch (e: Exception) {
            SkillResult.Failure("WhatsApp not installed", e)
        }
    }
}

class WhatsAppCallSkill(private val context: Context) : Skill {
    override val id = "app.whatsapp.call"
    override val name = "WhatsApp Call"
    override val description = "Initiates a WhatsApp voice call"
    
    override fun canHandle(intent: Intent) = intent == Intent.OPEN_WHATSAPP_CALLS

    override fun execute(request: SkillRequest): SkillResult {
        // WhatsApp doesn't have a reliable public intent for calls without contacts lookup.
        // We will fallback to opening the app for now if phone is provided.
        val phone = request.command.contactName
        return if (phone != null) {
            val uri = Uri.parse("whatsapp://send?phone=$phone")
            val intent = AndroidIntent(AndroidIntent.ACTION_VIEW, uri).apply {
                flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
            SkillResult.Success("Opened WhatsApp chat to initiate call")
        } else {
            val intent = context.packageManager.getLaunchIntentForPackage("com.whatsapp")
            if (intent != null) {
                intent.flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                SkillResult.Success("Opened WhatsApp")
            } else {
                SkillResult.Failure("WhatsApp not installed")
            }
        }
    }
}

class YouTubeSearchSkill(private val context: Context) : Skill {
    override val id = "app.youtube.search"
    override val name = "YouTube Search"
    override val description = "Searches YouTube"
    
    override fun canHandle(intent: Intent) = intent == Intent.SEARCH_YOUTUBE

    override fun execute(request: SkillRequest): SkillResult {
        val query = request.command.query ?: request.command.message ?: return SkillResult.Failure("Query required")
        val intent = AndroidIntent(AndroidIntent.ACTION_SEARCH).apply {
            setPackage("com.google.android.youtube")
            putExtra("query", query)
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        
        return try {
            context.startActivity(intent)
            SkillResult.Success("Searching YouTube for '$query'")
        } catch (e: Exception) {
            // Fallback to web
            val webIntent = AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${URLEncoder.encode(query, "UTF-8")}")).apply {
                flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            SkillResult.Success("Searching YouTube Web for '$query'")
        }
    }
}

class YouTubePlaySkill(private val context: Context) : Skill {
    override val id = "app.youtube.play"
    override val name = "YouTube Play"
    override val description = "Plays a YouTube video"
    
    override fun canHandle(intent: Intent) = intent == Intent.PLAY_VIDEO_URL

    override fun execute(request: SkillRequest): SkillResult {
        val videoId = request.command.url ?: return SkillResult.Failure("Video ID required")
        val intent = AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("vnd.youtube:$videoId")).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        
        return try {
            context.startActivity(intent)
            SkillResult.Success("Playing YouTube video")
        } catch (e: Exception) {
            val webIntent = AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$videoId")).apply {
                flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
            SkillResult.Success("Playing YouTube video on Web")
        }
    }
}
