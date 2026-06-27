package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent as AndroidIntent
import android.provider.MediaStore
import android.provider.Settings
import android.provider.AlarmClock
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import java.lang.reflect.Modifier

class AndroidActionSkill(private val context: Context) : Skill {
    override val id = "android.action.execute"
    override val name = "Android Action"
    override val description = "Executes a public Android activity action confirmed by PackageManager"

    override fun canHandle(intent: Intent): Boolean = intent == Intent.EXECUTE_ANDROID_ACTION

    override fun execute(request: SkillRequest): SkillResult {
        val requested = request.command.rawText
            .replace(Regex("^(?:run|execute|open)\\s+(?:android\\s+)?(?:intent|action)\\s+"), "")
            .trim()
        val action = resolveAction(requested)
            ?: return SkillResult.Failure("I couldn't find a public Android action named $requested.")

        val variants = listOf(
            AndroidIntent(action),
            AndroidIntent(action).setType("text/plain"),
            AndroidIntent(action).setType("image/*"),
            AndroidIntent(action).setType("video/*"),
            AndroidIntent(action).setType("audio/*")
        )
        val executable = variants.firstOrNull {
            it.resolveActivity(context.packageManager) != null
        } ?: return SkillResult.Failure(
            "This phone has no exported activity that accepts $action without additional data."
        )
        executable.addFlags(AndroidIntent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(executable)
        return SkillResult.Success("Executed $action.")
    }

    private fun resolveAction(requested: String): String? {
        if (requested.startsWith("android.")) return requested
        val fieldName = requested.uppercase()
        val owners = listOf(
            AndroidIntent::class.java,
            Settings::class.java,
            AlarmClock::class.java,
            MediaStore::class.java
        )
        for (owner in owners) {
            val field = owner.fields.firstOrNull {
                Modifier.isStatic(it.modifiers) && it.name == fieldName && it.type == String::class.java
            } ?: continue
            return runCatching { field.get(null) as? String }.getOrNull()
        }
        return null
    }
}
