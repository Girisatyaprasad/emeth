package com.emeth.kernel.intents

import android.content.Context
import android.content.Intent as AndroidIntent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.provider.Settings
import android.provider.AlarmClock
import com.emeth.kernel.spine.AirOsSpine
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Modifier

data class ResolvedIntentContract(
    val owner: String,
    val field: String,
    val action: String,
    val handlers: List<String>
)

object IntentCapabilityScanner {
    fun scan(context: Context): List<ResolvedIntentContract> {
        val packageManager = context.packageManager
        val contracts = listOf(
            AndroidIntent::class.java,
            Settings::class.java,
            AlarmClock::class.java,
            MediaStore::class.java
        ).flatMap { owner ->
            owner.fields.mapNotNull { field ->
                if (!Modifier.isStatic(field.modifiers) ||
                    field.type != String::class.java ||
                    !field.name.startsWith("ACTION_") && !field.name.startsWith("INTENT_ACTION_")
                ) {
                    return@mapNotNull null
                }
                val action = runCatching { field.get(null) as? String }.getOrNull() ?: return@mapNotNull null
                Triple(owner.simpleName, field.name, action)
            }
        }.distinctBy { it.third }

        return contracts.mapNotNull { (owner, field, action) ->
            val handlers = queryHandlers(packageManager, action)
            if (handlers.isEmpty()) null else ResolvedIntentContract(owner, field, action, handlers)
        }.sortedBy { it.action }
    }

    fun scanAndPublish(context: Context): List<ResolvedIntentContract> {
        val resolved = scan(context)
        val payload = JSONObject()
            .put("resolvedContractCount", resolved.size)
            .put("handlerCount", resolved.sumOf { it.handlers.size })
            .put(
                "contracts",
                JSONArray(resolved.map { contract ->
                    JSONObject()
                        .put("owner", contract.owner)
                        .put("field", contract.field)
                        .put("action", contract.action)
                        .put("handlers", JSONArray(contract.handlers))
                })
            )
        AirOsSpine(context).publishAccess(
            key = "android.intent.catalog",
            value = "resolved",
            payload = payload
        )
        return resolved
    }

    private fun queryHandlers(packageManager: PackageManager, action: String): List<String> {
        val variants = listOf(
            AndroidIntent(action),
            AndroidIntent(action).setType("text/plain"),
            AndroidIntent(action).setType("image/*"),
            AndroidIntent(action).setType("video/*"),
            AndroidIntent(action).setType("audio/*")
        )
        return variants.flatMap { intent ->
            packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }.map { it.activityInfo.packageName + "/" + it.activityInfo.name }
            .distinct()
    }
}
