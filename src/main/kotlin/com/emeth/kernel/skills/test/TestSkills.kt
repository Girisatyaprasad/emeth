package com.emeth.kernel.skills.test

import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import com.emeth.kernel.intents.Intent

class AppLaunchSkill : Skill {
    override val id = "test.app.launch"
    override val name = "App Launcher"
    override val description = "Launches applications"

    override fun canHandle(intent: Intent): Boolean {
        return intent == Intent.OPEN_APP
    }

    override fun execute(request: SkillRequest): SkillResult {
        return SkillResult.Success("App launched successfully")
    }
}

class AlarmSkill : Skill {
    override val id = "test.alarm"
    override val name = "Alarm Setter"
    override val description = "Sets alarms"

    override fun canHandle(intent: Intent): Boolean {
        return intent == Intent.SET_ALARM
    }

    override fun execute(request: SkillRequest): SkillResult {
        return SkillResult.Success("Alarm set successfully")
    }
}

class ReminderSkill : Skill {
    override val id = "test.reminder"
    override val name = "Reminder Creator"
    override val description = "Creates reminders"

    override fun canHandle(intent: Intent): Boolean {
        return intent == Intent.CREATE_REMINDER
    }

    override fun execute(request: SkillRequest): SkillResult {
        return SkillResult.Success("Reminder created successfully")
    }
}
