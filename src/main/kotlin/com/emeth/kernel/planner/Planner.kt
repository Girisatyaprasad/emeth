package com.emeth.kernel.planner

import com.emeth.kernel.intents.IntentResolver
import com.emeth.kernel.skills.SkillRegistry
import com.emeth.kernel.execution.ExecutionEngine
import com.emeth.kernel.skills.SkillResult

class Planner(
    private val intentResolver: IntentResolver,
    private val skillRegistry: SkillRegistry,
    private val executionEngine: ExecutionEngine
) {
    fun process(input: String): SkillResult? {
        val intent = intentResolver.resolve(input)
        val skill = skillRegistry.discover(intent)
        
        return if (skill != null) {
            executionEngine.execute(skill, intent)
        } else {
            null // Handle unknown intent / no skill
        }
    }
}
