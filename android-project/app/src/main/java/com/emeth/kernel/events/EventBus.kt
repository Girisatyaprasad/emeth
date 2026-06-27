package com.emeth.kernel.events

import java.util.concurrent.CopyOnWriteArrayList

enum class EventType {
    USER_INPUT,
    SKILL_STARTED,
    SKILL_COMPLETED,
    SKILL_FAILED,
    CONTEXT_UPDATED
}

data class Event(val type: EventType, val payload: Any? = null)

interface EventListener {
    fun onEvent(event: Event)
}

class EventBus {
    private val listeners = CopyOnWriteArrayList<EventListener>()

    fun subscribe(listener: EventListener) {
        listeners.addIfAbsent(listener)
    }

    fun unsubscribe(listener: EventListener) {
        listeners.remove(listener)
    }

    fun publish(event: Event) {
        listeners.forEach { it.onEvent(event) }
    }
}
