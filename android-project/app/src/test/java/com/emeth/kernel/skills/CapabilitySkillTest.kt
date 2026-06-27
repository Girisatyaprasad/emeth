package com.emeth.kernel.skills

import com.emeth.kernel.capabilities.Capability
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.intents.ParsedCommand
import com.emeth.kernel.skills.android.CapabilitySkill
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilitySkillTest {
    @Test
    fun returnsCapabilityLedger() {
        val skill = CapabilitySkill()
        val result = skill.execute(
            SkillRequest(
                ParsedCommand(
                    intentType = Intent.QUERY_CAPABILITIES,
                    rawText = "what can you do headlessly"
                )
            )
        )

        assertTrue(result is SkillResult.Success)
        val success = result as SkillResult.Success
        assertTrue(success.message!!.contains("Headless now"))
        assertTrue(success.message.contains("Needs confirmation"))
        assertTrue((success.data as List<*>).first() is Capability)
    }
}
