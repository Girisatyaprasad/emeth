package com.emeth.kernel.skills.android

import android.content.Context
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import com.emeth.kernel.watchers.Watcher
import com.emeth.kernel.watchers.WatcherAction
import com.emeth.kernel.watchers.WatcherCondition
import com.emeth.kernel.watchers.WatcherRegistry
import com.emeth.kernel.watchers.WatcherType
import com.emeth.kernel.watchers.ConditionOp
import java.util.UUID

class CreateWatcherSkill(private val context: Context) : Skill {
    override val id = "system.watcher.create"
    override val name = "Create Watcher"
    override val description = "Creates a new background watcher"

    override fun canHandle(intent: Intent) = intent == Intent.CREATE_WATCHER

    override fun execute(request: SkillRequest): SkillResult {
        val command = request.command
        
        if (command.conditionType == null || command.thresholdValue == null) {
            return SkillResult.Failure("Missing condition type or threshold value.")
        }

        val type = try {
            WatcherType.valueOf(command.conditionType)
        } catch (e: Exception) {
            return SkillResult.Failure("Unknown watcher type: ${command.conditionType}")
        }

        val watcher = Watcher(
            id = UUID.randomUUID().toString(),
            type = type,
            condition = WatcherCondition(
                op = command.conditionOp?.let { ConditionOp.valueOf(it) } ?: defaultOp(type),
                targetValue = command.thresholdValue
            ),
            action = WatcherAction(
                type = "local_notification",
                payload = watcherPayload(type, command)
            )
        )

        val registry = WatcherRegistry(context)
        registry.addWatcher(watcher)

        return SkillResult.Success(watcherConfirmation(type, command.thresholdValue, watcher.action.payload))
    }

    private fun defaultOp(type: WatcherType): ConditionOp {
        return when (type) {
            WatcherType.BATTERY_LEVEL,
            WatcherType.STORAGE_LEVEL -> ConditionOp.LESS_THAN_EQUAL
            WatcherType.TIME_OF_DAY -> ConditionOp.EQUAL
            else -> ConditionOp.GREATER_THAN_EQUAL
        }
    }

    private fun watcherPayload(type: WatcherType, command: com.emeth.kernel.intents.ParsedCommand): String {
        return when (type) {
            WatcherType.TIME_OF_DAY -> command.message ?: "Time for your Emeth automation."
            WatcherType.BATTERY_LEVEL -> "Battery reached ${command.thresholdValue?.toInt() ?: 0}%."
            WatcherType.STEP_COUNT -> "You reached ${command.thresholdValue?.toInt() ?: 0} steps."
            else -> "Automation triggered for ${type.name.lowercase().replace("_", " ")}."
        }
    }

    private fun watcherConfirmation(type: WatcherType, targetValue: Float?, payload: String): String {
        return when (type) {
            WatcherType.TIME_OF_DAY -> "Automation created for ${formatMinutes(targetValue)}."
            WatcherType.BATTERY_LEVEL -> "Automation created for battery at ${targetValue?.toInt() ?: 0}%."
            WatcherType.STEP_COUNT -> "Automation created for ${targetValue?.toInt() ?: 0} steps."
            else -> "Automation created: $payload"
        }
    }

    private fun formatMinutes(value: Float?): String {
        val total = value?.toInt() ?: return "the chosen time"
        val hour24 = total / 60
        val minute = total % 60
        val ampm = if (hour24 >= 12) "PM" else "AM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return "$hour12:${minute.toString().padStart(2, '0')} $ampm"
    }
}
