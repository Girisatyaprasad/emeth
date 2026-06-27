package com.emeth.kernel.skills

import com.emeth.kernel.intents.Intent
import com.emeth.kernel.intents.ParsedCommand

data class SkillRequest(val command: ParsedCommand, val payload: Any? = null)

sealed class SkillResult {
    data class Success(val message: String? = null, val data: Any? = null) : SkillResult()
    data class Failure(val reason: String, val exception: Throwable? = null) : SkillResult()
    data class Partial(val message: String, val data: Any? = null) : SkillResult()
}

interface Skill {
    val id: String
    val name: String
    val description: String
    fun canHandle(intent: Intent): Boolean
    fun execute(request: SkillRequest): SkillResult
}

class SkillRegistry {
    private val skills = mutableMapOf<String, Skill>()

    @Synchronized
    fun register(skill: Skill) {
        skills[skill.id] = skill
    }

    @Synchronized
    fun unregister(skillId: String) {
        skills.remove(skillId)
    }

    @Synchronized
    fun discover(intent: Intent): Skill? {
        return skills.values.firstOrNull { it.canHandle(intent) }
    }
}
