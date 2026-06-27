package com.emeth.kernel.skills

import com.emeth.kernel.intents.Intent
import com.emeth.kernel.intents.ParsedCommand
import java.util.EnumMap

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
    private val skillsByIntent = EnumMap<Intent, Skill>(Intent::class.java)

    @Synchronized
    fun register(skill: Skill) {
        skills[skill.id]?.let { old ->
            skillsByIntent.entries.removeAll { it.value.id == old.id }
        }
        skills[skill.id] = skill
        Intent.entries.forEach { intent ->
            if (skill.canHandle(intent)) {
                skillsByIntent.putIfAbsent(intent, skill)
            }
        }
    }

    @Synchronized
    fun unregister(skillId: String) {
        skills.remove(skillId)
        skillsByIntent.entries.removeAll { it.value.id == skillId }
    }

    @Synchronized
    fun discover(intent: Intent): Skill? {
        return skillsByIntent[intent]
    }

    @Synchronized
    fun supportedIntents(): Set<Intent> = skillsByIntent.keys.toSet()
}
