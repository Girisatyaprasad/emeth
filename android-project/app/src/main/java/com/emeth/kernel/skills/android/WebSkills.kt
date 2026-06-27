package com.emeth.kernel.skills.android

import android.app.SearchManager
import android.content.Context
import android.content.Intent as AndroidIntent
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult

class WebSearchSkill(private val context: Context) : Skill {
    override val id = "android.web.search"
    override val name = "Web Search"
    override val description = "Searches the web"

    override fun canHandle(intent: Intent) = intent == Intent.SEARCH_WEB

    override fun execute(request: SkillRequest): SkillResult {
        val query = request.command.query ?: request.command.rawText
            .replace(Regex("(?i)^(search|google|web search)\\s*(for)?\\s*"), "")
            .trim()

        if (query.isBlank()) {
            return SkillResult.Partial("What should I search for?")
        }

        val i = AndroidIntent(AndroidIntent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(i)
        return SkillResult.Success("Searching for $query.")
    }
}
