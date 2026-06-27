package com.emeth.kernel.spine

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object EmethSpineBootstrap {
    fun initialize(context: Context): AirOsSpine {
        val spine = AirOsSpine(context)
        spine.registerNode(kind = "assistant", label = "Emeth", version = 1)
        spine.publishUiMap(
            key = "emeth.navigation",
            payload = JSONObject()
                .put("screens", JSONArray(listOf("home", "automations", "access")))
                .put("entry", "home")
                .put("commandSurface", "home.ask")
        )
        spine.publishAccess(
            key = "android.app_intents",
            value = "available",
            payload = JSONObject().put("scope", "native_and_installed_apps")
        )
        spine.publishAccess(
            key = "android.accessibility",
            value = "user_grant_required",
            payload = JSONObject().put("scope", "screen_read_tap_type_global_actions")
        )
        spine.publishAccess(
            key = "android.notifications",
            value = "user_grant_required",
            payload = JSONObject().put("scope", "notification_read_and_exposed_actions")
        )
        spine.emitBehavior(
            kind = "node_started",
            payload = JSONObject().put("node", spine.nodeId)
        )
        return spine
    }
}
