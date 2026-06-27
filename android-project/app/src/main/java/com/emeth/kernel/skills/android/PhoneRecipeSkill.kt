package com.emeth.kernel.skills.android

import android.app.SearchManager
import android.content.Context
import android.content.Intent as AndroidIntent
import android.net.Uri
import com.emeth.kernel.access.EmethAccessibilityService
import com.emeth.kernel.access.ScreenSnapshot
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.intents.ParsedCommand
import com.emeth.kernel.memory.MemoryStore
import com.emeth.kernel.memory.MemoryType
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class RecipeTrace(
    val recipe: String,
    val steps: List<String>,
    val snapshot: ScreenSnapshot? = null
)

class PhoneRecipeSkill(private val context: Context) : Skill {
    override val id = "android.phone.recipe"
    override val name = "Phone Recipe Runner"
    override val description = "Runs deterministic multi-step phone recipes"
    private val memoryStore = MemoryStore(context)

    override fun canHandle(intent: Intent): Boolean = intent == Intent.RUN_PHONE_RECIPE

    override fun execute(request: SkillRequest): SkillResult {
        val command = request.command
        return when (command.recipeName) {
            "open_and_read" -> openAndRead(command)
            "search_and_read" -> searchAndRead(command)
            "prepare_whatsapp_message" -> prepareWhatsAppMessage(command)
            else -> SkillResult.Partial("I need a known recipe, like inspect app, search and read screen, or prepare WhatsApp message.")
        }
    }

    private fun openAndRead(command: ParsedCommand): SkillResult {
        val app = command.targetApp ?: command.rawText
            .lowercase(Locale.ROOT)
            .removePrefix("inspect")
            .replace("open", "")
            .replace("and read screen", "")
            .replace("read screen", "")
            .trim()

        if (app.isBlank()) return SkillResult.Partial("Which app should I inspect?")

        val launch = resolveLaunchIntent(app) ?: return SkillResult.Failure("I couldn't find an installed app named $app.")
        context.startActivity(launch.first)
        Thread.sleep(900)
        val snapshot = EmethAccessibilityService.snapshot()
        val summary = snapshotSummary(snapshot)
        val trace = RecipeTrace("open_and_read", listOf("open:${launch.second}", "read_screen"), snapshot)
        persistTrace(trace)
        return SkillResult.Success(
            "Opened ${launch.second}. $summary",
            trace
        )
    }

    private fun searchAndRead(command: ParsedCommand): SkillResult {
        val query = command.query ?: command.rawText
            .lowercase(Locale.ROOT)
            .replace("then read screen", "")
            .replace("and read screen", "")
            .replace(Regex("^(search|google|web search)\\s*(for)?\\s*"), "")
            .trim()

        if (query.isBlank()) return SkillResult.Partial("What should I search for?")

        val intent = AndroidIntent(AndroidIntent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        Thread.sleep(1200)
        val snapshot = EmethAccessibilityService.snapshot()
        val trace = RecipeTrace("search_and_read", listOf("search:$query", "read_screen"), snapshot)
        persistTrace(trace)
        return SkillResult.Success(
            "Searched for $query. ${snapshotSummary(snapshot)}",
            trace
        )
    }

    private fun prepareWhatsAppMessage(command: ParsedCommand): SkillResult {
        val message = command.message ?: command.rawText
            .substringAfter("message", "")
            .ifBlank { command.rawText.substringAfter("send", "") }
            .trim()

        if (message.isBlank()) {
            return SkillResult.Partial("What message should I prepare for WhatsApp?")
        }

        val phone = Regex("\\+?[0-9][0-9\\s-]{7,}").find(command.rawText)?.value?.filter { it.isDigit() }
        val intent = if (!phone.isNullOrBlank()) {
            AndroidIntent(AndroidIntent.ACTION_VIEW, Uri.parse("https://wa.me/$phone?text=${Uri.encode(message)}"))
        } else {
            AndroidIntent(AndroidIntent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(AndroidIntent.EXTRA_TEXT, message)
                setPackage("com.whatsapp")
            }
        }.apply {
            flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
        }

        context.startActivity(intent)
        Thread.sleep(900)
        val snapshot = EmethAccessibilityService.snapshot()
        val trace = RecipeTrace("prepare_whatsapp_message", listOf("open_whatsapp_share", "read_screen"), snapshot)
        persistTrace(trace)
        return SkillResult.Success(
            "Prepared WhatsApp message. ${snapshotSummary(snapshot)}",
            trace
        )
    }

    private fun resolveLaunchIntent(target: String): Pair<AndroidIntent, String>? {
        val packageManager = context.packageManager
        return packageManager.getInstalledApplications(0)
            .asSequence()
            .mapNotNull { appInfo ->
                val label = packageManager.getApplicationLabel(appInfo).toString()
                val score = appMatchScore(target, label, appInfo.packageName)
                if (score > 0) Triple(appInfo.packageName, label, score) else null
            }
            .sortedByDescending { it.third }
            .firstOrNull()
            ?.let { (packageName, label, _) ->
                val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                    flags = AndroidIntent.FLAG_ACTIVITY_NEW_TASK
                } ?: return@let null
                intent to label
            }
    }

    private fun appMatchScore(query: String, label: String, packageName: String): Int {
        val q = query.lowercase(Locale.ROOT)
        val l = label.lowercase(Locale.ROOT)
        val p = packageName.lowercase(Locale.ROOT)
        return when {
            l == q -> 100
            l.startsWith(q) -> 80
            l.contains(q) -> 60
            p.contains(q.replace(" ", "")) -> 30
            else -> 0
        }
    }

    private fun snapshotSummary(snapshot: ScreenSnapshot?): String {
        if (snapshot == null) return "Enable Emeth Access to read the next screen."
        val visible = snapshot.nodes.take(6).joinToString("; ") { "${it.index}. ${it.text}" }
        return if (visible.isBlank()) {
            "I don't see readable controls yet."
        } else {
            "Screen: $visible"
        }
    }

    private fun persistTrace(trace: RecipeTrace) {
        runBlocking {
            memoryStore.store(
                MemoryType.RECIPE_TRACE,
                trace.recipe,
                trace.toJson()
            )
            trace.snapshot?.let { snapshot ->
                memoryStore.store(
                    MemoryType.SCREEN_SNAPSHOT,
                    snapshot.packageName ?: "unknown",
                    snapshot.toJson()
                )
            }
        }
    }

    private fun RecipeTrace.toJson(): String {
        return JSONObject().apply {
            put("recipe", recipe)
            put("steps", JSONArray(steps))
            snapshot?.let { put("snapshot", JSONObject(it.toJson())) }
        }.toString()
    }

    private fun ScreenSnapshot.toJson(): String {
        return JSONObject().apply {
            put("packageName", packageName)
            put("windowTitle", windowTitle)
            put("nodes", JSONArray(nodes.take(20).map { node ->
                JSONObject().apply {
                    put("index", node.index)
                    put("text", node.text)
                    put("className", node.className)
                    put("clickable", node.clickable)
                    put("editable", node.editable)
                    put("scrollable", node.scrollable)
                    put("bounds", node.bounds)
                }
            }))
        }.toString()
    }
}
