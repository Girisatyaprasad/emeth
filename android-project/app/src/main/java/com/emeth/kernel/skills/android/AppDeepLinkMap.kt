package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.net.URLEncoder
import java.util.Locale

object AppDeepLinkMap {
    data class AppRoute(
        val appName: String,
        val packageId: String?,
        val aliases: List<String>,
        val buildIntent: (String, String?) -> Intent? // (command, extractedQuery) -> Intent
    )

    private fun safeEncode(value: String?): String {
        return value?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
    }

    private val routes = listOf(
        // ================= SOCIAL & MESSAGING =================
        AppRoute(
            appName = "Instagram",
            packageId = "com.instagram.android",
            aliases = listOf("instagram", "insta"),
            buildIntent = { cmd, _ ->
                when {
                    cmd.contains("camera") || cmd.contains("story") -> Intent(Intent.ACTION_VIEW, Uri.parse("instagram://camera"))
                    cmd.contains("message") || cmd.contains("dm") || cmd.contains("direct") -> Intent(Intent.ACTION_VIEW, Uri.parse("instagram://sharesheet"))
                    cmd.contains("profile") -> Intent(Intent.ACTION_VIEW, Uri.parse("instagram://user?username=me"))
                    else -> Intent(Intent.ACTION_VIEW, Uri.parse("instagram://app"))
                }
            }
        ),
        AppRoute(
            appName = "Facebook",
            packageId = "com.facebook.katana",
            aliases = listOf("facebook", "fb"),
            buildIntent = { cmd, _ ->
                when {
                    cmd.contains("notification") -> Intent(Intent.ACTION_VIEW, Uri.parse("fb://notifications"))
                    cmd.contains("profile") -> Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile"))
                    cmd.contains("page") -> Intent(Intent.ACTION_VIEW, Uri.parse("fb://pages"))
                    else -> Intent(Intent.ACTION_VIEW, Uri.parse("fb://feed"))
                }
            }
        ),
        AppRoute(
            appName = "X (Twitter)",
            packageId = "com.twitter.android",
            aliases = listOf("twitter", "x", "tweet"),
            buildIntent = { cmd, query ->
                when {
                    cmd.contains("compose") || cmd.contains("post") || cmd.contains("tweet") -> {
                        val msg = query ?: ""
                        Intent(Intent.ACTION_VIEW, Uri.parse("twitter://post?message=${safeEncode(msg)}"))
                    }
                    cmd.contains("search") -> {
                        val q = query ?: ""
                        Intent(Intent.ACTION_VIEW, Uri.parse("twitter://search?query=${safeEncode(q)}"))
                    }
                    cmd.contains("message") || cmd.contains("dm") -> Intent(Intent.ACTION_VIEW, Uri.parse("twitter://messages"))
                    else -> Intent(Intent.ACTION_VIEW, Uri.parse("twitter://timeline"))
                }
            }
        ),
        AppRoute(
            appName = "Telegram",
            packageId = "org.telegram.messenger",
            aliases = listOf("telegram", "tg"),
            buildIntent = { cmd, query ->
                when {
                    cmd.contains("message") || cmd.contains("send") -> {
                        // Very generic text sharing if no specific username
                        val msg = query ?: ""
                        Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, msg)
                            setPackage("org.telegram.messenger")
                        }
                    }
                    else -> Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve"))
                }
            }
        ),

        // ================= PRODUCTIVITY =================
        AppRoute(
            appName = "Google Maps",
            packageId = "com.google.android.apps.maps",
            aliases = listOf("map", "maps", "navigation", "navigate", "directions"),
            buildIntent = { cmd, query ->
                val q = query ?: ""
                when {
                    cmd.contains("navigate") || cmd.contains("directions") || cmd.contains("take me to") -> {
                        Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q=${safeEncode(q)}"))
                    }
                    cmd.contains("street view") -> {
                        Intent(Intent.ACTION_VIEW, Uri.parse("google.streetview:cbll=${safeEncode(q)}"))
                    }
                    else -> Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${safeEncode(q)}"))
                }
            }
        ),
        AppRoute(
            appName = "Gmail",
            packageId = "com.google.android.gm",
            aliases = listOf("gmail", "email", "mail"),
            buildIntent = { cmd, query ->
                when {
                    cmd.contains("compose") || cmd.contains("write") || cmd.contains("send") -> {
                        Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:")
                            putExtra(Intent.EXTRA_SUBJECT, query ?: "")
                        }
                    }
                    else -> {
                        val i = Intent(Intent.ACTION_VIEW)
                        i.setPackage("com.google.android.gm")
                        i
                    }
                }
            }
        ),
        AppRoute(
            appName = "Play Store",
            packageId = "com.android.vending",
            aliases = listOf("play store", "app store", "market"),
            buildIntent = { cmd, query ->
                val q = query ?: ""
                if (cmd.contains("search") || cmd.contains("find") || q.isNotEmpty()) {
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=${safeEncode(q)}"))
                } else {
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://apps"))
                }
            }
        ),
        AppRoute(
            appName = "Clock",
            packageId = null, // Varies by OEM
            aliases = listOf("clock", "alarm", "timer", "stopwatch"),
            buildIntent = { cmd, _ ->
                when {
                    cmd.contains("alarm") -> Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                    cmd.contains("timer") -> Intent(android.provider.AlarmClock.ACTION_SET_TIMER)
                    else -> Intent(android.provider.AlarmClock.ACTION_SHOW_ALARMS)
                }
            }
        ),

        // ================= ENTERTAINMENT =================
        AppRoute(
            appName = "Spotify",
            packageId = "com.spotify.music",
            aliases = listOf("spotify"),
            buildIntent = { cmd, query ->
                val q = query ?: ""
                when {
                    cmd.contains("search") -> Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${safeEncode(q)}"))
                    cmd.contains("play") && q.isNotEmpty() -> Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${safeEncode(q)}")) // Usually plays if premium
                    else -> Intent(Intent.ACTION_VIEW, Uri.parse("spotify:app"))
                }
            }
        ),
        AppRoute(
            appName = "Netflix",
            packageId = "com.netflix.mediaclient",
            aliases = listOf("netflix"),
            buildIntent = { cmd, query ->
                val q = query ?: ""
                if (cmd.contains("search") || cmd.contains("play") || q.isNotEmpty()) {
                    Intent(Intent.ACTION_VIEW, Uri.parse("nflx://www.netflix.com/search?q=${safeEncode(q)}"))
                } else {
                    val i = Intent(Intent.ACTION_MAIN)
                    i.setPackage("com.netflix.mediaclient")
                    i
                }
            }
        )
    )

    /**
     * Resolves a natural language command into an Intent, and returns a Pair<Intent, String(appName)>.
     */
    fun resolve(context: Context, command: String, extractedQuery: String?): Pair<Intent, String>? {
        val normalized = command.lowercase(Locale.ROOT)
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
        
        // Find the best route based on aliases present in the command
        val match = routes.firstOrNull { route -> 
            route.aliases.any { alias -> Regex("\\b$alias\\b").containsMatchIn(normalized) } 
        } ?: return null

        val intent = match.buildIntent(normalized, extractedQuery)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } ?: return null

        // If the intent can't be resolved, fallback to the package launcher if we have a package ID
        if (intent.resolveActivity(context.packageManager) == null) {
            if (match.packageId != null) {
                val fallbackIntent = context.packageManager.getLaunchIntentForPackage(match.packageId)
                if (fallbackIntent != null) {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    return Pair(fallbackIntent, match.appName)
                }
            }
            return null // Completely unresolvable and no fallback
        }

        return Pair(intent, match.appName)
    }
}
