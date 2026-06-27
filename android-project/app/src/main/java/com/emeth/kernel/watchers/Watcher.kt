package com.emeth.kernel.watchers

enum class WatcherType {
    TIME_OF_DAY,
    BATTERY_LEVEL,
    STORAGE_LEVEL,
    STEP_COUNT,
    SCREEN_TIME,
    WIFI_DISCONNECT,
    BLUETOOTH_DISCONNECT,
    MISSED_CALL,
    CALENDAR_EVENT_START
}

enum class ConditionOp {
    GREATER_THAN_EQUAL,
    LESS_THAN_EQUAL,
    EQUAL,
    TRIGGERED // for events like disconnect
}

data class WatcherCondition(
    val op: ConditionOp,
    val targetValue: Float? = null
)

data class WatcherAction(
    val type: String = "local_notification",
    val payload: String
)

data class Watcher(
    val id: String,
    val type: WatcherType,
    val condition: WatcherCondition,
    val action: WatcherAction
)
