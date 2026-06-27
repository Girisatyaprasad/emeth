package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent as AndroidIntent
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult

class ThirdPartyAppSkill(private val context: Context) : Skill {
    override val id = "android.thirdparty"
    override val name = "Third-Party App Launcher"
    override val description = "Resolves natural language commands into deep links or specific actions for third-party applications like social media, productivity, and entertainment."

    override fun canHandle(intent: Intent): Boolean {
        return intent == Intent.OPEN_THIRD_PARTY_ACTION
    }

    override fun execute(request: SkillRequest): SkillResult {
        val result = AppDeepLinkMap.resolve(context, request.command.rawText, request.command.query)
        
        if (result == null) {
            return SkillResult.Failure("I could not figure out how to open that app or perform that action.")
        }

        val (intent, appName) = result

        return try {
            context.startActivity(intent)
            SkillResult.Success("Opening $appName...")
        } catch (e: Exception) {
            SkillResult.Failure("I tried to open $appName, but the app might not be installed or it rejected the action.", e)
        }
    }
}
