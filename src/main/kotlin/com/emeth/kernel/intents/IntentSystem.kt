package com.emeth.kernel.intents

sealed class Intent {
    object OPEN_APP : Intent()
    object SET_ALARM : Intent()
    object CREATE_REMINDER : Intent()
    object SEARCH_WEB : Intent()
    object UNKNOWN : Intent()
}

class IntentResolver {
    fun resolve(input: String): Intent {
        val lowerInput = input.lowercase()
        return when {
            lowerInput.contains("open") -> Intent.OPEN_APP
            lowerInput.contains("alarm") -> Intent.SET_ALARM
            lowerInput.contains("remind") -> Intent.CREATE_REMINDER
            lowerInput.contains("search") -> Intent.SEARCH_WEB
            else -> Intent.UNKNOWN
        }
    }
}
