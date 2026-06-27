package com.emeth.kernel.planner

import com.emeth.kernel.intents.Intent
import com.emeth.kernel.intents.ParsedCommand
import com.emeth.kernel.skills.SkillResult

object DigitalAssistantResponder {
    fun respondToNoSkill(command: ParsedCommand): SkillResult {
        val text = command.rawText.trim().lowercase()

        if (text.isBlank()) {
            return SkillResult.Partial("What would you like me to help with?")
        }

        if (isGreeting(text)) {
            return SkillResult.Success("Hi, I'm Emeth.")
        }

        if (asksIdentity(text)) {
            return SkillResult.Success("I'm Emeth, your phone assistant.")
        }

        if (asksCapabilities(text)) {
            return SkillResult.Success(
                "I can run device checks, file search, app intents, and automations. WhatsApp can be prepared through app intents; silent send or chat mute needs a future Accessibility bridge."
            )
        }

        if (command.intentType == Intent.OPEN_APP && command.targetApp.isNullOrBlank()) {
            return SkillResult.Partial("Which app do you want me to open?")
        }

        if (text.contains("whatsapp")) {
            return SkillResult.Partial("WhatsApp silent actions need Accessibility or user confirmation.")
        }

        return SkillResult.Partial("I can't do that yet.")
    }

    private fun isGreeting(text: String): Boolean {
        return text in setOf("hi", "hello", "hey", "yo", "good morning", "good afternoon", "good evening")
    }

    private fun asksIdentity(text: String): Boolean {
        return text.contains("who are you") ||
            text.contains("what are you") ||
            text.contains("your name") ||
            text == "emeth"
    }

    private fun asksCapabilities(text: String): Boolean {
        return text.contains("what can you do") ||
            text.contains("help me") ||
            text == "help" ||
            text.contains("capabilities") ||
            text.contains("commands")
    }
}
