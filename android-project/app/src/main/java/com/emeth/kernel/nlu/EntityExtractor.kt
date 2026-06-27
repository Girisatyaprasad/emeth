package com.emeth.kernel.nlu

import java.util.Calendar

data class ExtractedEntities(
    val timeHour: Int? = null,
    val timeMinute: Int? = null,
    val durationSeconds: Int? = null,
    val repeatDays: List<Int>? = null,
    val query: String? = null,
    val message: String? = null,
    val contactName: String? = null,
    val fileQuery: String? = null,
    val actionMode: String? = null,
    val uiTarget: String? = null,
    val uiIndex: Int? = null,
    val recipeName: String? = null,
    val targetApp: String? = null,
    val thresholdValue: Float? = null,
    val conditionType: String? = null,
    val conditionOp: String? = null,
    val url: String? = null
)

object EntityExtractor {
    fun extract(expandedText: String): ExtractedEntities {
        var timeHour: Int? = null
        var timeMinute: Int? = null
        var durationSeconds: Int? = null
        var repeatDays: List<Int>? = null
        var query: String? = null
        var message: String? = null
        var contactName: String? = null
        var fileQuery: String? = null
        var actionMode: String? = null
        var uiTarget: String? = null
        var uiIndex: Int? = null
        var recipeName: String? = null
        var targetApp: String? = null
        var thresholdValue: Float? = null
        var conditionType: String? = null
        var conditionOp: String? = null
        var url: String? = null

        // URL Extraction
        val urlRegex = "https?://[^\\s]+".toRegex()
        val urlMatch = urlRegex.find(expandedText)
        if (urlMatch != null) {
            url = urlMatch.groupValues[0]
        }

        // Time extraction
        val timeRegex = "\\b([0-9]{1,2})(?::([0-9]{2}))?\\s*(am|pm)?\\b(?!\\s*%)".toRegex()
        val timeMatch = timeRegex.find(expandedText)
        if (timeMatch != null) {
            val h = timeMatch.groupValues[1].toInt()
            val m = timeMatch.groupValues[2].takeIf { it.isNotEmpty() }?.toInt() ?: 0
            val ampm = timeMatch.groupValues[3]
            val hasTimeSignal = ampm.isNotBlank() || timeMatch.groupValues[2].isNotBlank() || expandedText.contains("alarm")
            if (hasTimeSignal && h in 0..23 && m in 0..59) {
                timeHour = h
                if (ampm == "pm" && h < 12) timeHour = h + 12
                if (ampm == "am" && h == 12) timeHour = 0
                timeMinute = m
            }
        }

        if (expandedText.contains("timer")) {
            val durationMatch = Regex("\\b([0-9]+)\\s*(seconds?|secs?|minutes?|mins?|hours?|hrs?)\\b").find(expandedText)
            if (durationMatch != null) {
                val amount = durationMatch.groupValues[1].toIntOrNull()
                val unit = durationMatch.groupValues[2]
                durationSeconds = amount?.let {
                    when {
                        unit.startsWith("hour") || unit.startsWith("hr") -> it * 3600
                        unit.startsWith("min") -> it * 60
                        else -> it
                    }
                }
            }
        }

        if (expandedText.contains("canonical_repeat_all")) {
            repeatDays = listOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY)
        } else if (expandedText.contains("canonical_repeat_weekdays")) {
            repeatDays = listOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY)
        } else if (expandedText.contains("canonical_repeat_weekends")) {
            repeatDays = listOf(Calendar.SUNDAY, Calendar.SATURDAY)
        }

        if (expandedText.contains("canonical_search_shorts")) {
            val q = expandedText.substringAfter("canonical_search_shorts").trim()
            if (q.isNotEmpty()) {
                query = q
                targetApp = "YouTube"
            }
        } else if (expandedText.contains("canonical_youtube_search")) {
            val q = expandedText.substringAfter("canonical_youtube_search").trim()
            if (q.isNotEmpty()) {
                query = q
                targetApp = "YouTube"
            }
        } else if (expandedText.contains("canonical_search") && expandedText.contains("canonical_target_youtube")) {
            val regex = "canonical_search (.*?) canonical_target_youtube".toRegex()
            val match = regex.find(expandedText)
            if (match != null) {
                query = match.groupValues[1].trim()
                targetApp = "YouTube"
            }
        } else if (expandedText.contains("youtube") && !expandedText.contains("canonical_youtube_last_played")) {
            targetApp = "YouTube"
            val q = expandedText.substringAfter("youtube").trim()
            if (q.isNotEmpty() && expandedText.contains("canonical_search")) {
                 query = q
            } else if (expandedText.split(" ").size == 2 && expandedText.startsWith("youtube")) {
                query = expandedText.substringAfter("youtube").trim()
            }
        } else if (expandedText.contains("canonical_search")) {
            val q = expandedText.substringAfter("canonical_search").trim()
            if (q.isNotEmpty()) {
                query = q
            }
        } else if (expandedText.startsWith("search ")) {
            query = expandedText.removePrefix("search ").removePrefix("for ").trim()
        } else if (expandedText.startsWith("google ")) {
            query = expandedText.removePrefix("google ").trim()
        }

        val stepWatcherRegex = "(?:remind|tell|notify)?\\s*(?:me|)?\\s*(?:when|at)?\\s*(?:i\\s+)?(?:hit|reach)?\\s*([0-9]+)\\s*(?:canonical_steps|steps)".toRegex()
        val stepWatcherMatch = stepWatcherRegex.find(expandedText)
        if (stepWatcherMatch != null) {
            thresholdValue = stepWatcherMatch.groupValues[1].toFloat()
            conditionType = "STEP_COUNT"
            conditionOp = "GREATER_THAN_EQUAL"
        }

        val batteryWatcherRegex = "(?:battery|charge).*?(hit|hits|reach|reaches|above|over|drop|drops|below|under|less than|at|to)\\s*([0-9]{1,3})\\s*%?".toRegex()
        val batteryWatcherMatch = batteryWatcherRegex.find(expandedText)
        if (batteryWatcherMatch != null && (expandedText.contains("remind") || expandedText.contains("notify") || expandedText.contains("tell") || expandedText.contains("ring"))) {
            val opWord = batteryWatcherMatch.groupValues[1]
            thresholdValue = batteryWatcherMatch.groupValues[2].toFloat().coerceIn(0f, 100f)
            conditionType = "BATTERY_LEVEL"
            conditionOp = if (opWord in setOf("drop", "drops", "below", "under", "less than") ||
                expandedText.contains("drops to") ||
                expandedText.contains("drop to") ||
                expandedText.contains("below")
            ) {
                "LESS_THAN_EQUAL"
            } else {
                "GREATER_THAN_EQUAL"
            }
        }

        if (timeHour != null &&
            conditionType == null &&
            !expandedText.contains("alarm") &&
            (expandedText.contains("remind") || expandedText.contains("notify") || expandedText.contains("tell") || expandedText.contains("say") || expandedText.contains("ring"))
        ) {
            thresholdValue = (timeHour * 60 + (timeMinute ?: 0)).toFloat()
            conditionType = "TIME_OF_DAY"
            conditionOp = "EQUAL"
        }

        actionMode = when {
            expandedText.contains("headless") || expandedText.contains("without opening") || expandedText.contains("without screen") -> "headless"
            expandedText.contains("2 step") || expandedText.contains("two step") || expandedText.contains("pre send") || expandedText.contains("presend") -> "two_step"
            else -> null
        }

        message = extractMessage(expandedText)
        contactName = extractContactName(expandedText)
        uiTarget = extractUiTarget(expandedText)
        uiIndex = extractUiIndex(expandedText)
        recipeName = extractRecipeName(expandedText)
        fileQuery = extractFileQuery(expandedText)

        return ExtractedEntities(
            timeHour = timeHour,
            timeMinute = timeMinute,
            durationSeconds = durationSeconds,
            repeatDays = repeatDays,
            query = query,
            message = message,
            contactName = contactName,
            fileQuery = fileQuery,
            actionMode = actionMode,
            uiTarget = uiTarget,
            uiIndex = uiIndex,
            recipeName = recipeName,
            targetApp = targetApp,
            thresholdValue = thresholdValue,
            conditionType = conditionType,
            conditionOp = conditionOp,
            url = url
        )
    }

    private fun extractContactName(text: String): String? {
        val patterns = listOf(
            "(?:call|dial)\\s+(.+)",
            "(?:send|text|message)\\s+.+?\\s+to\\s+(.+?)(?:\\s+(?:on|via)\\s+(?:whatsapp|sms))?$",
            "(?:send|text|message)\\s+(?:whatsapp|sms)\\s+(?:to\\s+)?(.+?)(?:\\s+(?:saying|message|text)\\s+.+)?$",
            "(?:open|message)\\s+(.+?)\\s+(?:on|in)\\s+whatsapp$"
        )
        for (pattern in patterns) {
            val value = Regex(pattern).find(text)?.groupValues?.getOrNull(1)
                ?.replace(Regex("\\s+(?:headless|two_step|two step)$"), "")
                ?.trim()
            if (!value.isNullOrBlank() && value !in setOf("whatsapp", "sms", "message")) {
                return value
            }
        }
        return null
    }

    private fun extractMessage(text: String): String? {
        val quoted = Regex("\"([^\"]+)\"|'([^']+)'").find(text)
        if (quoted != null) {
            return quoted.groupValues.drop(1).firstOrNull { it.isNotBlank() }?.trim()
        }

        val markers = listOf("message", "saying", "say", "text")
        for (marker in markers) {
            val match = Regex("\\b$marker\\b\\s+(.+?)(?:\\s+to\\s+whatsapp|\\s+on\\s+whatsapp|$)").find(text)
            val value = match?.groupValues?.getOrNull(1)
                ?.replace(Regex("\\s+(?:at|by|on)\\s+[0-9]{1,2}:?[0-9]{0,2}\\s*(?:am|pm)?$"), "")
                ?.trim()
            if (!value.isNullOrBlank()) return value
        }

        return null
    }

    private fun extractFileQuery(text: String): String? {
        val duplicateRequest = text.contains("duplicate")
        if (duplicateRequest && !text.contains("named")) return null
        val looksFileLike = text.contains("file") ||
            text.contains("resume") ||
            text.contains("download") ||
            text.contains("pdf") ||
            text.contains("image") ||
            text.contains("photo") ||
            text.contains("video") ||
            text.contains("document")
        if (!looksFileLike) return null

        val patterns = listOf(
            "(?:canonical_search)\\s+(?:file\\s+)?(.+)",
            "(?:find|fetch|search|locate|get|show|open)\\s+(?:file\\s+)?(.+)",
            "(?:delete|remove)\\s+(?:file\\s+)?(.+)",
            "(?:move)\\s+(?:file\\s+)?(.+)"
        )

        for (pattern in patterns) {
            val match = Regex(pattern).find(text)
            val candidate = match?.groupValues?.getOrNull(1)
                ?.replace(Regex("\\b(?:from|in|to|into|smartly|carefully|the|my)\\b"), " ")
                ?.trim()
                ?.replace(Regex("\\s+"), " ")
            if (!candidate.isNullOrBlank() && candidate !in setOf("duplicates", "duplicate files")) {
                return candidate
            }
        }

        return null
    }

    private fun extractUiTarget(text: String): String? {
        val quoted = Regex("\"([^\"]+)\"|'([^']+)'").find(text)
        if (quoted != null) {
            return quoted.groupValues.drop(1).firstOrNull { it.isNotBlank() }?.trim()
        }

        val patterns = listOf(
            "(?:tap|click|press|select)\\s+(.+)",
            "(?:type|enter|write)\\s+(.+)"
        )

        for (pattern in patterns) {
            val value = Regex(pattern).find(text)?.groupValues?.getOrNull(1)?.trim()
            if (!value.isNullOrBlank()) return value
        }

        return null
    }

    private fun extractUiIndex(text: String): Int? {
        val match = Regex("\\b(?:tap|click|press|select)\\s+(?:number\\s+|item\\s+)?([0-9]{1,2})\\b").find(text)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun extractRecipeName(text: String): String? {
        return when {
            text.startsWith("inspect ") || text.contains("open") && text.contains("read screen") -> "open_and_read"
            text.contains("search") && (text.contains("then read") || text.contains("and read screen")) -> "search_and_read"
            text.contains("whatsapp") && text.contains("message") && (text.contains("prepare") || text.contains("draft")) -> "prepare_whatsapp_message"
            text.startsWith("run recipe ") -> text.removePrefix("run recipe ").trim().replace(" ", "_")
            else -> null
        }
    }
}
