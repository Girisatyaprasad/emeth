package com.emeth.kernel.planner

import android.content.Context
import com.emeth.kernel.intents.IntentResolver
import com.emeth.kernel.skills.SkillRegistry
import com.emeth.kernel.execution.ExecutionEngine
import com.emeth.kernel.skills.SkillResult
import com.emeth.kernel.memory.MemoryStore
import com.emeth.kernel.memory.MemoryType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Planner(
    private val context: Context,
    private val intentResolver: IntentResolver,
    private val skillRegistry: SkillRegistry,
    private val executionEngine: ExecutionEngine
) {
    private val memoryStore = MemoryStore(context)
    private val scope = CoroutineScope(Dispatchers.IO)
    var lastDebugInfo: String = ""

    fun process(input: String): SkillResult? {
        val normalized = com.emeth.kernel.nlu.TextNormalizer.normalize(input)
        val parsedCommand = intentResolver.resolve(input)
        val skill = skillRegistry.discover(parsedCommand.intentType)
        
        lastDebugInfo = "normalized text = $normalized\nresolved intent = ${parsedCommand.intentType}\nconfidence = ${parsedCommand.confidence}\nreasons = ${parsedCommand.matchReasons.joinToString()}\nselected skill = ${skill?.javaClass?.simpleName ?: "null"}"

        if (parsedCommand.confidence < 0.45f) {
            return SkillResult.Failure("I didn't quite understand that.")
        } else if (parsedCommand.confidence < 0.75f) {
            if (parsedCommand.intentType == com.emeth.kernel.intents.Intent.SET_ALARM && parsedCommand.timeHour == null) {
                return SkillResult.Partial("What time should I set the alarm for?")
            }
            if (parsedCommand.intentType == com.emeth.kernel.intents.Intent.OPEN_APP && parsedCommand.targetApp == null) {
                return SkillResult.Partial("Which app do you want me to open?")
            }
            return SkillResult.Partial("Could you clarify what you mean?")
        }
        
        return if (skill != null) {
            val result = executionEngine.execute(skill, parsedCommand)
            // Log memory
            scope.launch {
                if (result is SkillResult.Success || result is SkillResult.Partial) {
                    val meta = "{\"intent\":\"${parsedCommand.intentType.name}\", \"skill_id\":\"${skill.id}\"}"
                    memoryStore.store(MemoryType.EXECUTED_COMMAND, input, meta)
                } else if (result is SkillResult.Failure) {
                    val meta = "{\"intent\":\"${parsedCommand.intentType.name}\", \"skill_id\":\"${skill.id}\", \"error\":\"${result.reason}\"}"
                    memoryStore.store(MemoryType.SYSTEM_EVENT, input, meta)
                }
            }
            result
        } else {
            val fallback = DigitalAssistantResponder.respondToNoSkill(parsedCommand)
            scope.launch {
                val meta = "{\"intent\":\"${parsedCommand.intentType.name}\", \"assistant_fallback\":true}"
                memoryStore.store(MemoryType.SYSTEM_EVENT, input, meta)
            }
            fallback
        }
    }
}
