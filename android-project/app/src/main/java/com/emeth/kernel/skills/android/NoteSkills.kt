package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent as AndroidIntent
import android.os.Build
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult

class NoteSkill(private val context: Context) : Skill {
    override val id = "android.notes"
    override val name = "Notes"
    override val description = "Creates notes through Android's native note contract"

    override fun canHandle(intent: Intent): Boolean = intent == Intent.CREATE_NOTE

    override fun execute(request: SkillRequest): SkillResult {
        val text = request.command.message
            ?: request.command.rawText
                .replace(Regex("^(?:create|make|write|take)?\\s*(?:a\\s+)?note(?:\\s+(?:that|saying|about|to))?\\s*"), "")
                .trim()
        if (text.isBlank()) return SkillResult.Partial("What should the note say?")

        val noteIntent = if (Build.VERSION.SDK_INT >= 34) {
            AndroidIntent(AndroidIntent.ACTION_CREATE_NOTE)
        } else {
            AndroidIntent(AndroidIntent.ACTION_SEND).setType("text/plain")
        }.apply {
            putExtra(AndroidIntent.EXTRA_TEXT, text)
            putExtra(AndroidIntent.EXTRA_TITLE, text.take(48))
            addFlags(AndroidIntent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (noteIntent.resolveActivity(context.packageManager) == null) {
            return SkillResult.Failure(
                "No installed notes app accepts Android's note contract. Install or enable a compatible notes app."
            )
        }
        context.startActivity(noteIntent)
        return SkillResult.Success("Prepared note: $text")
    }
}
