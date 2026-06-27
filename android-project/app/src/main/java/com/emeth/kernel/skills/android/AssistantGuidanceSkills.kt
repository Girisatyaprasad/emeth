package com.emeth.kernel.skills.android

import android.content.Context
import com.emeth.kernel.capabilities.CapabilityRegistry
import com.emeth.kernel.capabilities.ExecutionMode
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.permissions.PermissionCockpit
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult

data class NextStepSuggestion(
    val title: String,
    val reason: String,
    val exampleCommand: String
)

class WhatsNextSkill : Skill {
    override val id = "assistant.whats_next"
    override val name = "What's Next"
    override val description = "Explains the next practical Emeth build steps"

    override fun canHandle(intent: Intent): Boolean = intent == Intent.WHAT_IS_NEXT

    override fun execute(request: SkillRequest): SkillResult {
        val suggestions = listOf(
            NextStepSuggestion(
                title = "Real confidence and clarification",
                reason = "Emeth should know when it is unsure and ask one useful follow-up instead of guessing.",
                exampleCommand = "Set up confidence scoring for missing contact, file, time, and query details."
            ),
            NextStepSuggestion(
                title = "Pending command continuation",
                reason = "When Emeth asks a question, the next reply should complete that same task.",
                exampleCommand = "If I say 'send WhatsApp' and then 'to Rahul', continue the WhatsApp intent."
            ),
            NextStepSuggestion(
                title = "Make core skills finish more work",
                reason = "The biggest assistant-feel upgrade is replacing placeholder opens with entity-driven actions.",
                exampleCommand = "Use extracted query/contact/duration data in web, contact, timer, reminder, and calendar skills."
            )
        )

        return SkillResult.Success(
            "Next: build real confidence, clarification, and pending-command continuation. After that, make core skills use extracted entities so Emeth completes tasks instead of only opening screens.",
            suggestions
        )
    }
}

class CapabilitySkill : Skill {
    override val id = "assistant.capabilities"
    override val name = "Capability Ledger"
    override val description = "Explains what Emeth can do headlessly, through app intents, or only with confirmation"

    override fun canHandle(intent: Intent): Boolean = intent == Intent.QUERY_CAPABILITIES

    override fun execute(request: SkillRequest): SkillResult {
        val headless = CapabilityRegistry.byMode(ExecutionMode.HEADLESS).joinToString("; ") { it.label }
        val confirmation = CapabilityRegistry.byMode(ExecutionMode.CONFIRMATION_REQUIRED).joinToString("; ") { it.label }
        val blocked = CapabilityRegistry.byMode(ExecutionMode.UNSUPPORTED).joinToString("; ") { it.label }

        val message = buildString {
            append(CapabilityRegistry.summary())
            append(" Headless now: ")
            append(headless)
            append(". Needs confirmation: ")
            append(confirmation)
            append(". Not available through public app intents: ")
            append(blocked)
            append(".")
        }

        return SkillResult.Success(message, CapabilityRegistry.all)
    }
}

class PermissionStatusSkill(private val context: Context) : Skill {
    override val id = "assistant.permissions"
    override val name = "Permission Cockpit"
    override val description = "Reports which Emeth access bridges and permissions are enabled"

    override fun canHandle(intent: Intent): Boolean = intent == Intent.QUERY_PERMISSION_STATUS

    override fun execute(request: SkillRequest): SkillResult {
        val statuses = PermissionCockpit.current(context)
        val enabled = statuses.count { it.enabled }
        val missing = statuses.filterNot { it.enabled }

        val message = if (missing.isEmpty()) {
            "All tracked Emeth access bridges are enabled."
        } else {
            "Emeth access: $enabled/${statuses.size} enabled. Missing: ${missing.joinToString(", ") { it.label }}."
        }

        return SkillResult.Success(message, statuses)
    }
}
