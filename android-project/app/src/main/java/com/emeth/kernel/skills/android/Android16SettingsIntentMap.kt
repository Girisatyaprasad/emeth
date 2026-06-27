package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent
import java.util.Locale

object Android16SettingsIntentMap {
    private data class Route(val aliases: List<String>, val action: String)

    private val routes = listOf(
        Route(listOf("accessibility"), "android.settings.ACCESSIBILITY_SETTINGS"),
        Route(listOf("add account", "accounts"), "android.settings.ADD_ACCOUNT_SETTINGS"),
        Route(listOf("advanced memory protection"), "android.settings.ADVANCED_MEMORY_PROTECTION_SETTINGS"),
        Route(listOf("airplane mode"), "android.settings.AIRPLANE_MODE_SETTINGS"),
        Route(listOf("apn", "access point names"), "android.settings.APN_SETTINGS"),
        Route(listOf("developer options", "development"), "android.settings.APPLICATION_DEVELOPMENT_SETTINGS"),
        Route(listOf("app language", "app locale"), "android.settings.APP_LOCALE_SETTINGS"),
        Route(listOf("app notifications", "notification settings"), "android.settings.ALL_APPS_NOTIFICATION_SETTINGS"),
        Route(listOf("app usage", "screen time"), "android.settings.action.APP_USAGE_SETTINGS"),
        Route(listOf("auto rotate", "rotation"), "android.settings.AUTO_ROTATE_SETTINGS"),
        Route(listOf("battery saver", "power saver"), "android.settings.BATTERY_SAVER_SETTINGS"),
        Route(listOf("biometric", "face unlock", "fingerprint"), "android.settings.BIOMETRIC_ENROLL"),
        Route(listOf("bluetooth"), "android.settings.BLUETOOTH_SETTINGS"),
        Route(listOf("captions", "captioning"), "android.settings.CAPTIONING_SETTINGS"),
        Route(listOf("cast", "screen cast"), "android.settings.CAST_SETTINGS"),
        Route(listOf("credentials", "password provider"), "android.settings.CREDENTIAL_PROVIDER"),
        Route(listOf("data roaming", "roaming"), "android.settings.DATA_ROAMING_SETTINGS"),
        Route(listOf("data usage", "mobile data usage"), "android.settings.DATA_USAGE_SETTINGS"),
        Route(listOf("date and time", "date", "time"), "android.settings.DATE_SETTINGS"),
        Route(listOf("about phone", "device info"), "android.settings.DEVICE_INFO_SETTINGS"),
        Route(listOf("display", "screen"), "android.settings.DISPLAY_SETTINGS"),
        Route(listOf("screen saver", "dream"), "android.settings.DREAM_SETTINGS"),
        Route(listOf("home app", "launcher"), "android.settings.HOME_SETTINGS"),
        Route(listOf("keyboard", "input method"), "android.settings.INPUT_METHOD_SETTINGS"),
        Route(listOf("language", "locale"), "android.settings.LOCALE_SETTINGS"),
        Route(listOf("location"), "android.settings.LOCATION_SOURCE_SETTINGS"),
        Route(listOf("all apps", "manage apps", "applications"), "android.settings.MANAGE_ALL_APPLICATIONS_SETTINGS"),
        Route(listOf("all files access"), "android.settings.MANAGE_ALL_FILES_ACCESS_PERMISSION"),
        Route(listOf("default apps"), "android.settings.MANAGE_DEFAULT_APPS_SETTINGS"),
        Route(listOf("display over other apps", "overlay"), "android.settings.action.MANAGE_OVERLAY_PERMISSION"),
        Route(listOf("install unknown apps", "unknown sources"), "android.settings.MANAGE_UNKNOWN_APP_SOURCES"),
        Route(listOf("modify system settings", "write settings"), "android.settings.action.MANAGE_WRITE_SETTINGS"),
        Route(listOf("sim profiles", "esim", "sim"), "android.settings.MANAGE_ALL_SIM_PROFILES_SETTINGS"),
        Route(listOf("mobile network", "network operator"), "android.settings.NETWORK_OPERATOR_SETTINGS"),
        Route(listOf("nfc payment", "tap and pay"), "android.settings.NFC_PAYMENT_SETTINGS"),
        Route(listOf("nfc"), "android.settings.NFC_SETTINGS"),
        Route(listOf("night light", "night display"), "android.settings.NIGHT_DISPLAY_SETTINGS"),
        Route(listOf("notification access"), "android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"),
        Route(listOf("do not disturb access", "notification policy access"), "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS"),
        Route(listOf("print", "printing"), "android.settings.ACTION_PRINT_SETTINGS"),
        Route(listOf("privacy"), "android.settings.PRIVACY_SETTINGS"),
        Route(listOf("wallet", "quick access wallet"), "android.settings.QUICK_ACCESS_WALLET_SETTINGS"),
        Route(listOf("regional preferences", "region"), "android.settings.REGIONAL_PREFERENCES_SETTINGS"),
        Route(listOf("satellite"), "android.settings.SATELLITE_SETTING"),
        Route(listOf("security"), "android.settings.SECURITY_SETTINGS"),
        Route(listOf("sound", "audio"), "android.settings.SOUND_SETTINGS"),
        Route(listOf("storage", "internal storage"), "android.settings.INTERNAL_STORAGE_SETTINGS"),
        Route(listOf("sync"), "android.settings.SYNC_SETTINGS"),
        Route(listOf("usage access"), "android.settings.USAGE_ACCESS_SETTINGS"),
        Route(listOf("personal dictionary", "user dictionary"), "android.settings.USER_DICTIONARY_SETTINGS"),
        Route(listOf("voice input"), "android.settings.VOICE_INPUT_SETTINGS"),
        Route(listOf("vpn"), "android.settings.VPN_SETTINGS"),
        Route(listOf("webview"), "android.settings.WEBVIEW_SETTINGS"),
        Route(listOf("wifi ip", "ip settings"), "android.settings.WIFI_IP_SETTINGS"),
        Route(listOf("add wifi", "add network"), "android.settings.WIFI_ADD_NETWORKS"),
        Route(listOf("wifi", "wi fi"), "android.settings.WIFI_SETTINGS"),
        Route(listOf("wireless"), "android.settings.WIRELESS_SETTINGS"),
        Route(listOf("do not disturb", "zen mode"), "android.settings.ZEN_MODE_PRIORITY_SETTINGS")
    )

    fun resolve(context: Context, command: String): Intent {
        val normalized = command.lowercase(Locale.ROOT)
            .replace("-", " ")
            .replace(Regex("\\s+"), " ")
        val route = routes
            .flatMap { route -> route.aliases.map { alias -> route to alias } }
            .filter { (_, alias) -> normalized.contains(alias) }
            .maxByOrNull { (_, alias) -> alias.length }
            ?.first
        val preferred = Intent(route?.action ?: "android.settings.SETTINGS")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return if (preferred.resolveActivity(context.packageManager) != null) {
            preferred
        } else {
            Intent("android.settings.SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }
}
