package com.emeth.kernel.intents

data class ParsedCommand(
    val intentType: Intent,
    val rawText: String,
    val targetApp: String? = null,
    val query: String? = null,
    val contactName: String? = null,
    val message: String? = null,
    val fileQuery: String? = null,
    val actionMode: String? = null,
    val uiTarget: String? = null,
    val uiIndex: Int? = null,
    val recipeName: String? = null,
    val timeHour: Int? = null,
    val timeMinute: Int? = null,
    val durationSeconds: Int? = null,
    val repeatDays: List<Int>? = null, // Using Calendar.SUNDAY, etc.
    val url: String? = null,
    val thresholdValue: Float? = null,
    val conditionType: String? = null,
    val conditionOp: String? = null,
    val confidence: Float = 1.0f,
    val matchReasons: List<String> = emptyList(),
    val youtubeOperation: String? = null,
    val videoId: String? = null,
    val playlistId: String? = null,
    val tab: String? = null,
    val source: String? = null
)
