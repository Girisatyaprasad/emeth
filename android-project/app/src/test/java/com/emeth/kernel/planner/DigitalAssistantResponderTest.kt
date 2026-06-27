package com.emeth.kernel.planner

import com.emeth.kernel.intents.Intent
import com.emeth.kernel.intents.ParsedCommand
import com.emeth.kernel.skills.SkillResult
import org.junit.Assert.assertTrue
import org.junit.Test

class DigitalAssistantResponderTest {
    @Test
    fun greetingIntroducesAssistant() {
        val result = DigitalAssistantResponder.respondToNoSkill(command("hello"))

        assertTrue(result is SkillResult.Success)
        assertTrue((result as SkillResult.Success).message!!.contains("I'm Emeth"))
    }

    @Test
    fun capabilityQuestionListsSupportedActions() {
        val result = DigitalAssistantResponder.respondToNoSkill(command("what can you do"))

        assertTrue(result is SkillResult.Success)
        assertTrue((result as SkillResult.Success).message!!.contains("automations"))
    }

    @Test
    fun unknownCommandReturnsHelpfulPartial() {
        val result = DigitalAssistantResponder.respondToNoSkill(command("write my essay"))

        assertTrue(result is SkillResult.Partial)
        assertTrue((result as SkillResult.Partial).message.contains("can't do"))
    }

    private fun command(rawText: String): ParsedCommand {
        return ParsedCommand(
            intentType = Intent.UNKNOWN,
            rawText = rawText
        )
    }
}
