package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent as AndroidIntent
import android.provider.AlarmClock
import android.provider.CalendarContract
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import java.util.Calendar

class CalendarSkill(private val context: Context) : Skill {
    override val id = "android.calendar"
    override val name = "Calendar"
    override val description = "Opens Calendar"

    override fun canHandle(intent: Intent) = intent == Intent.OPEN_CALENDAR

    override fun execute(request: SkillRequest): SkillResult {
        val i = AndroidIntent(AndroidIntent.ACTION_VIEW, CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success("Opened Calendar")
    }
}

class AddCalendarEventSkill(private val context: Context) : Skill {
    override val id = "android.calendar.add"
    override val name = "Add Calendar Event"
    override val description = "Adds an event to the calendar"

    override fun canHandle(intent: Intent) = intent == Intent.ADD_CALENDAR_EVENT

    override fun execute(request: SkillRequest): SkillResult {
        val i = AndroidIntent(AndroidIntent.ACTION_INSERT)
            .setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE, "New Event")
            .apply { flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK }
        context.startActivity(i)
        return SkillResult.Success("Opened Add Calendar Event")
    }
}

class AlarmSkill(private val context: Context) : Skill {
    override val id = "android.alarm"
    override val name = "Set Alarm"
    override val description = "Sets an alarm"

    override fun canHandle(intent: Intent) = intent == Intent.SET_ALARM

    override fun execute(request: SkillRequest): SkillResult {
        val command = request.command
        val hour = command.timeHour
        val minute = command.timeMinute ?: 0
        
        if (hour == null) {
            return SkillResult.Failure("No time specified for the alarm.")
        }
        
        val i = AndroidIntent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            if (command.repeatDays != null && command.repeatDays.isNotEmpty()) {
                val arrayList = ArrayList<Int>()
                arrayList.addAll(command.repeatDays)
                putExtra(AlarmClock.EXTRA_DAYS, arrayList)
            }
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        
        val repeatStr = if (command.repeatDays != null && command.repeatDays.isNotEmpty()) " recurring" else ""
        val ampm = if (hour >= 12) "PM" else "AM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        val minStr = minute.toString().padStart(2, '0')
        return SkillResult.Success("Set$repeatStr alarm for $displayHour:$minStr $ampm")
    }
}

class TimerSkill(private val context: Context) : Skill {
    override val id = "android.timer"
    override val name = "Set Timer"
    override val description = "Sets a timer"

    override fun canHandle(intent: Intent) = intent == Intent.SET_TIMER

    override fun execute(request: SkillRequest): SkillResult {
        val duration = request.command.durationSeconds
            ?: return SkillResult.Partial("How long should the timer run?")
        val i = AndroidIntent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, duration)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success("Set timer for ${formatDuration(duration)}.")
    }

    private fun formatDuration(seconds: Int): String = when {
        seconds % 3600 == 0 -> "${seconds / 3600} hour${if (seconds == 3600) "" else "s"}"
        seconds % 60 == 0 -> "${seconds / 60} minute${if (seconds == 60) "" else "s"}"
        else -> "$seconds second${if (seconds == 1) "" else "s"}"
    }
}

class StopwatchSkill(private val context: Context) : Skill {
    override val id = "android.stopwatch"
    override val name = "Start Stopwatch"
    override val description = "Starts the stopwatch"

    override fun canHandle(intent: Intent) = intent == Intent.START_STOPWATCH

    override fun execute(request: SkillRequest): SkillResult {
        // ACTION_SHOW_STOPWATCH might not be standard on all, but falling back to alarms app
        val i = AndroidIntent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success("Opened Stopwatch/Alarms")
    }
}
