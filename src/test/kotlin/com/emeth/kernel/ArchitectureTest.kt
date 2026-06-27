package com.emeth.kernel

import com.emeth.kernel.events.Event
import com.emeth.kernel.events.EventBus
import com.emeth.kernel.events.EventListener
import com.emeth.kernel.events.EventType
import com.emeth.kernel.execution.ExecutionEngine
import com.emeth.kernel.intents.IntentResolver
import com.emeth.kernel.planner.Planner
import com.emeth.kernel.skills.SkillRegistry
import com.emeth.kernel.skills.SkillResult
import com.emeth.kernel.skills.test.AlarmSkill
import com.emeth.kernel.skills.test.AppLaunchSkill
import com.emeth.kernel.skills.test.ReminderSkill
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArchitectureTest {

    @Test
    fun testArchitectureSpine() {
        val eventBus = EventBus()
        val registry = SkillRegistry()
        val resolver = IntentResolver()
        val executionEngine = ExecutionEngine(eventBus)
        val planner = Planner(resolver, registry, executionEngine)

        // Register Test Skills
        registry.register(AppLaunchSkill())
        registry.register(AlarmSkill())
        registry.register(ReminderSkill())

        val events = mutableListOf<Event>()
        eventBus.subscribe(object : EventListener {
            override fun onEvent(event: Event) {
                events.add(event)
            }
        })

        // Test 1: App Launch
        val result1 = planner.process("open youtube")
        assertTrue(result1 is SkillResult.Success)
        assertTrue(events.any { it.type == EventType.SKILL_STARTED && it.payload == "test.app.launch" })
        assertTrue(events.any { it.type == EventType.SKILL_COMPLETED && it.payload == "test.app.launch" })
        events.clear()

        // Test 2: Set Alarm
        val result2 = planner.process("set alarm for 7am")
        assertTrue(result2 is SkillResult.Success)
        assertTrue(events.any { it.type == EventType.SKILL_STARTED && it.payload == "test.alarm" })
        assertTrue(events.any { it.type == EventType.SKILL_COMPLETED && it.payload == "test.alarm" })
        events.clear()

        // Test 3: Reminder
        val result3 = planner.process("remind me to call mom")
        assertTrue(result3 is SkillResult.Success)
        assertTrue(events.any { it.type == EventType.SKILL_STARTED && it.payload == "test.reminder" })
        assertTrue(events.any { it.type == EventType.SKILL_COMPLETED && it.payload == "test.reminder" })
        events.clear()
    }
}
