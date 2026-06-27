package com.emeth.kernel.skills.memory

import android.content.Context
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import com.emeth.kernel.memory.MemoryQueryEngine
import com.emeth.kernel.memory.MemoryType
import kotlinx.coroutines.runBlocking

class MemorySkill(private val context: Context) : Skill {
    override val id = "android.memory"
    override val name = "Memory Skill"
    override val description = "Queries memory based on intent"

    private val queryEngine = MemoryQueryEngine(context)

    override fun canHandle(intent: Intent): Boolean {
        return intent == Intent.QUERY_MEMORY_TODAY ||
               intent == Intent.QUERY_MEMORY_APPS_TODAY ||
               intent == Intent.QUERY_MEMORY_YESTERDAY ||
               intent == Intent.QUERY_MEMORY_SEARCH ||
               intent == Intent.QUERY_LAST_SCREEN ||
               intent == Intent.QUERY_LAST_RECIPE
    }

    override fun execute(request: SkillRequest): SkillResult {
        return runBlocking {
            when (request.command.intentType) {
                Intent.QUERY_MEMORY_TODAY -> {
                    val entries = queryEngine.queryToday().filter { it.type == MemoryType.EXECUTED_COMMAND.name }
                    val summaryList = entries.map { entry ->
                        // Attempt to clean up JSON
                        if (entry.content.startsWith("set alarm to", ignoreCase = true) || entry.content.startsWith("set alarm for", ignoreCase = true)) {
                            entry.content.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                        } else {
                            "Executed: ${entry.content}"
                        }
                    }.joinToString("\n")
                    SkillResult.Success(if (summaryList.isEmpty()) "Nothing recorded today." else summaryList, entries)
                }
                Intent.QUERY_MEMORY_APPS_TODAY -> {
                    val entries = queryEngine.queryToday().filter { it.type == MemoryType.APP_USAGE.name }
                    val summaryList = entries.map { entry ->
                        if (entry.metadata?.contains("\"action\":\"search\"") == true) {
                            // Try to extract query
                            val queryMatch = "\"query\":\"([^\"]+)\"".toRegex().find(entry.metadata ?: "")
                            val query = queryMatch?.groupValues?.get(1) ?: "something"
                            "Searched ${entry.content} for $query"
                        } else {
                            "Opened ${entry.content.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}"
                        }
                    }.joinToString("\n")
                    SkillResult.Success(if (summaryList.isEmpty()) "No apps recorded today." else summaryList, entries)
                }
                Intent.QUERY_MEMORY_YESTERDAY -> {
                    val entries = queryEngine.queryYesterday()
                    val summaryList = entries.take(10).map { it.content }.joinToString("\n")
                    SkillResult.Success(if (summaryList.isEmpty()) "Nothing recorded yesterday." else summaryList, entries)
                }
                Intent.QUERY_MEMORY_SEARCH -> {
                    val entries = queryEngine.queryByType(MemoryType.APP_USAGE)
                    if (entries.isNotEmpty()) {
                        val lastApp = entries.first()
                        SkillResult.Success("Last app opened: ${lastApp.content.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }}")
                    } else {
                        SkillResult.Success("No app usage found")
                    }
                }
                Intent.QUERY_LAST_SCREEN -> {
                    val entry = queryEngine.queryByType(MemoryType.SCREEN_SNAPSHOT).firstOrNull()
                    if (entry == null) {
                        SkillResult.Success("No screen snapshot saved yet.")
                    } else {
                        SkillResult.Success("Last screen: ${entry.content}", entry)
                    }
                }
                Intent.QUERY_LAST_RECIPE -> {
                    val entry = queryEngine.queryByType(MemoryType.RECIPE_TRACE).firstOrNull()
                    if (entry == null) {
                        SkillResult.Success("No recipe trace saved yet.")
                    } else {
                        SkillResult.Success("Last recipe: ${entry.content}", entry)
                    }
                }
                else -> SkillResult.Failure("Unknown memory query intent")
            }
        }
    }
}
