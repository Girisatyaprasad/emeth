package com.emeth.kernel.skills

import com.emeth.kernel.intents.Intent
import com.emeth.kernel.intents.ParsedCommand
import com.emeth.kernel.skills.android.NextStepSuggestion
import com.emeth.kernel.skills.android.WhatsNextSkill
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WhatsNextSkillTest {
    @Test
    fun returnsActionableNextSteps() {
        val skill = WhatsNextSkill()
        val result = skill.execute(
            SkillRequest(
                ParsedCommand(
                    intentType = Intent.WHAT_IS_NEXT,
                    rawText = "what's next"
                )
            )
        )

        assertTrue(result is SkillResult.Success)
        val success = result as SkillResult.Success
        assertTrue(success.message!!.contains("confidence"))
        val data = success.data as List<*>
        assertEquals(3, data.size)
        assertTrue(data.first() is NextStepSuggestion)
    }
}
