package com.emeth.kernel.skills.android

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent as AndroidIntent
import android.provider.Settings
import com.emeth.kernel.access.EmethAccessibilityService
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult

class PhoneControlSkill(private val context: Context) : Skill {
    override val id = "android.phone.control"
    override val name = "Phone Control"
    override val description = "Controls Android global UI through Emeth Accessibility"

    override fun canHandle(intent: Intent): Boolean {
        return intent in setOf(
            Intent.PHONE_BACK,
            Intent.PHONE_HOME,
            Intent.OPEN_NOTIFICATION_SHADE,
            Intent.OPEN_QUICK_SETTINGS,
            Intent.OPEN_ACCESSIBILITY_SETUP,
            Intent.TAP_TEXT,
            Intent.TAP_INDEX,
            Intent.TYPE_TEXT,
            Intent.SCROLL_UP,
            Intent.SCROLL_DOWN,
            Intent.READ_SCREEN
        )
    }

    override fun execute(request: SkillRequest): SkillResult {
        if (request.command.intentType == Intent.OPEN_ACCESSIBILITY_SETUP) {
            return openAccessibilitySettings()
        }

        if (!EmethAccessibilityService.isEnabled()) {
            openAccessibilitySettings()
            return SkillResult.Partial("Enable Emeth Access to let Emeth control apps and system UI.")
        }

        val action = when (request.command.intentType) {
            Intent.PHONE_BACK -> AccessibilityService.GLOBAL_ACTION_BACK
            Intent.PHONE_HOME -> AccessibilityService.GLOBAL_ACTION_HOME
            Intent.OPEN_NOTIFICATION_SHADE -> AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS
            Intent.OPEN_QUICK_SETTINGS -> AccessibilityService.GLOBAL_ACTION_QUICK_SETTINGS
            else -> null
        }

        if (action != null) {
            return if (EmethAccessibilityService.performGlobal(action)) {
                SkillResult.Success("Done.")
            } else {
                SkillResult.Failure("Emeth Access could not perform that phone action.")
            }
        }

        return when (request.command.intentType) {
            Intent.TAP_TEXT -> {
                val target = request.command.uiTarget ?: return SkillResult.Partial("What should I tap?")
                if (EmethAccessibilityService.tapText(target)) {
                    SkillResult.Success("Tapped $target.")
                } else {
                    SkillResult.Failure("I couldn't find $target on screen.")
                }
            }
            Intent.TAP_INDEX -> {
                val index = request.command.uiIndex ?: return SkillResult.Partial("Which screen item number should I tap?")
                if (EmethAccessibilityService.tapIndex(index)) {
                    SkillResult.Success("Tapped item $index.")
                } else {
                    SkillResult.Failure("I couldn't tap item $index.")
                }
            }
            Intent.TYPE_TEXT -> {
                val text = request.command.uiTarget ?: return SkillResult.Partial("What should I type?")
                if (EmethAccessibilityService.typeText(text)) {
                    SkillResult.Success("Typed.")
                } else {
                    SkillResult.Failure("I couldn't find a text field.")
                }
            }
            Intent.SCROLL_DOWN -> {
                if (EmethAccessibilityService.scrollForward()) SkillResult.Success("Scrolled.") else SkillResult.Failure("I couldn't scroll down here.")
            }
            Intent.SCROLL_UP -> {
                if (EmethAccessibilityService.scrollBackward()) SkillResult.Success("Scrolled.") else SkillResult.Failure("I couldn't scroll up here.")
            }
            Intent.READ_SCREEN -> {
                val snapshot = EmethAccessibilityService.snapshot()
                    ?: return SkillResult.Failure("I can't read the screen yet. Enable Emeth Access.")
                val visible = snapshot.nodes.take(12).joinToString("; ") {
                    "${it.index}. ${it.text}"
                }
                SkillResult.Success(
                    if (visible.isBlank()) "I don't see readable controls on this screen." else "On screen: $visible",
                    snapshot
                )
            }
            else -> SkillResult.Failure("Unsupported phone control action.")
        }
    }

    private fun openAccessibilitySettings(): SkillResult {
        val intent = AndroidIntent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        return SkillResult.Success("Opened Accessibility settings. Enable Emeth Access.")
    }
}
