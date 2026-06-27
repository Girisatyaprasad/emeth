package com.emeth.kernel.execution

import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.events.EventBus
import com.emeth.kernel.events.Event
import com.emeth.kernel.events.EventType

class ExecutionEngine(private val eventBus: EventBus) {
    fun execute(skill: Skill, intent: Intent): SkillResult {
        eventBus.publish(Event(EventType.SKILL_STARTED, skill.id))
        
        val request = SkillRequest(intent = intent)
        
        return try {
            val result = skill.execute(request)
            when (result) {
                is SkillResult.Success, is SkillResult.Partial -> {
                    eventBus.publish(Event(EventType.SKILL_COMPLETED, skill.id))
                }
                is SkillResult.Failure -> {
                    eventBus.publish(Event(EventType.SKILL_FAILED, skill.id))
                }
            }
            result
        } catch (e: Exception) {
            eventBus.publish(Event(EventType.SKILL_FAILED, skill.id))
            SkillResult.Failure("Unhandled exception", e)
        }
    }
}
