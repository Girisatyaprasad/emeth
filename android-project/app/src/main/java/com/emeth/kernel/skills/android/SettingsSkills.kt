package com.emeth.kernel.skills.android

import android.content.Context
import android.content.Intent as AndroidIntent
import android.provider.Settings
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.skills.Skill
import com.emeth.kernel.skills.SkillRequest
import com.emeth.kernel.skills.SkillResult

abstract class BaseSettingsSkill(
    private val context: Context,
    override val id: String,
    override val name: String,
    override val description: String,
    private val targetIntent: Intent,
    private val action: String
) : Skill {
    override fun canHandle(intent: Intent) = intent == targetIntent

    override fun execute(request: SkillRequest): SkillResult {
        val i = Android16SettingsIntentMap.resolve(context, request.command.rawText)
        context.startActivity(i)
        return SkillResult.Success("Opened $name")
    }
}

class SettingsWifiSkill(context: Context) : BaseSettingsSkill(context, "android.settings.wifi", "WiFi Settings", "Opens WiFi settings", Intent.OPEN_SETTINGS_WIFI, Settings.ACTION_WIFI_SETTINGS)
class SettingsBluetoothSkill(context: Context) : BaseSettingsSkill(context, "android.settings.bluetooth", "Bluetooth Settings", "Opens Bluetooth settings", Intent.OPEN_SETTINGS_BLUETOOTH, Settings.ACTION_BLUETOOTH_SETTINGS)
class SettingsDisplaySkill(context: Context) : BaseSettingsSkill(context, "android.settings.display", "Display Settings", "Opens Display settings", Intent.OPEN_SETTINGS_DISPLAY, Settings.ACTION_DISPLAY_SETTINGS)
class SettingsSoundSkill(context: Context) : BaseSettingsSkill(context, "android.settings.sound", "Sound Settings", "Opens Sound settings", Intent.OPEN_SETTINGS_SOUND, Settings.ACTION_SOUND_SETTINGS)
class SettingsAccessibilitySkill(context: Context) : BaseSettingsSkill(context, "android.settings.accessibility", "Accessibility Settings", "Opens Accessibility settings", Intent.OPEN_SETTINGS_ACCESSIBILITY, Settings.ACTION_ACCESSIBILITY_SETTINGS)
class SettingsSecuritySkill(context: Context) : BaseSettingsSkill(context, "android.settings.security", "Security Settings", "Opens Security settings", Intent.OPEN_SETTINGS_SECURITY, Settings.ACTION_SECURITY_SETTINGS)
class SettingsAppsSkill(context: Context) : BaseSettingsSkill(context, "android.settings.apps", "Apps Settings", "Opens Apps settings", Intent.OPEN_SETTINGS_APPS, Settings.ACTION_APPLICATION_SETTINGS)
class SettingsBatterySkill(context: Context) : BaseSettingsSkill(context, "android.settings.battery_page", "Battery Settings", "Opens Battery settings page", Intent.OPEN_SETTINGS_BATTERY, AndroidIntent.ACTION_POWER_USAGE_SUMMARY)
class SettingsStorageSkill(context: Context) : BaseSettingsSkill(context, "android.settings.storage", "Storage Settings", "Opens Storage settings", Intent.OPEN_SETTINGS_STORAGE, Settings.ACTION_INTERNAL_STORAGE_SETTINGS)
class SettingsLocationSkill(context: Context) : BaseSettingsSkill(context, "android.settings.location", "Location Settings", "Opens Location settings", Intent.OPEN_SETTINGS_LOCATION, Settings.ACTION_LOCATION_SOURCE_SETTINGS)
class SettingsDateTimeSkill(context: Context) : BaseSettingsSkill(context, "android.settings.date_time", "Date & Time Settings", "Opens Date & Time settings", Intent.OPEN_SETTINGS_DATE_TIME, Settings.ACTION_DATE_SETTINGS)
class SettingsNetworkSkill(context: Context) : BaseSettingsSkill(context, "android.settings.network", "Network Settings", "Opens Network settings", Intent.OPEN_SETTINGS_NETWORK, Settings.ACTION_WIRELESS_SETTINGS)
class SettingsNfcSkill(context: Context) : BaseSettingsSkill(context, "android.settings.nfc", "NFC Settings", "Opens NFC settings", Intent.OPEN_SETTINGS_NFC, Settings.ACTION_NFC_SETTINGS)
class SettingsCastSkill(context: Context) : BaseSettingsSkill(context, "android.settings.cast", "Cast Settings", "Opens Cast settings", Intent.OPEN_SETTINGS_CAST, Settings.ACTION_CAST_SETTINGS)
class SettingsHotspotSkill(context: Context) : BaseSettingsSkill(context, "android.settings.hotspot", "Hotspot Settings", "Opens Hotspot settings", Intent.OPEN_SETTINGS_HOTSPOT, "android.settings.TETHER_SETTINGS")
class SettingsAirplaneModeSkill(context: Context) : BaseSettingsSkill(context, "android.settings.airplane_mode", "Airplane Mode Settings", "Opens Airplane Mode settings", Intent.OPEN_SETTINGS_AIRPLANE_MODE, Settings.ACTION_AIRPLANE_MODE_SETTINGS)
class SettingsVpnSkill(context: Context) : BaseSettingsSkill(context, "android.settings.vpn", "VPN Settings", "Opens VPN settings", Intent.OPEN_SETTINGS_VPN, Settings.ACTION_VPN_SETTINGS)
class SettingsDataRoamingSkill(context: Context) : BaseSettingsSkill(context, "android.settings.data_roaming", "Data Roaming Settings", "Opens Data Roaming settings", Intent.OPEN_SETTINGS_DATA_ROAMING, Settings.ACTION_DATA_ROAMING_SETTINGS)
class SettingsPrivacySkill(context: Context) : BaseSettingsSkill(context, "android.settings.privacy", "Privacy Settings", "Opens Privacy settings", Intent.OPEN_SETTINGS_PRIVACY, Settings.ACTION_PRIVACY_SETTINGS)
class SettingsBiometricSkill(context: Context) : BaseSettingsSkill(context, "android.settings.biometric", "Biometric Settings", "Opens Biometric settings", Intent.OPEN_SETTINGS_BIOMETRIC, Settings.ACTION_BIOMETRIC_ENROLL)
class SettingsDeveloperSkill(context: Context) : BaseSettingsSkill(context, "android.settings.developer", "Developer Settings", "Opens Developer settings", Intent.OPEN_SETTINGS_DEVELOPER, Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
class SettingsAboutSkill(context: Context) : BaseSettingsSkill(context, "android.settings.about", "About Phone Settings", "Opens About Phone settings", Intent.OPEN_SETTINGS_ABOUT, Settings.ACTION_DEVICE_INFO_SETTINGS)
class SettingsAccountSkill(context: Context) : BaseSettingsSkill(context, "android.settings.account", "Account Settings", "Opens Account settings", Intent.OPEN_SETTINGS_ACCOUNT, Settings.ACTION_SYNC_SETTINGS)
class SettingsNotificationsSkill(context: Context) : BaseSettingsSkill(context, "android.settings.notifications", "Notifications Settings", "Opens Notifications settings", Intent.OPEN_SETTINGS_NOTIFICATIONS, "android.settings.ALL_APPS_NOTIFICATION_SETTINGS")
class SettingsSkill(private val appContext: Context) : Skill {
    override val id = "android.settings"
    override val name = "Settings"
    override val description = "Opens the best matching Android 16 Settings page"
    override fun canHandle(intent: Intent) = intent == Intent.OPEN_SETTINGS

    override fun execute(request: SkillRequest): SkillResult {
        val target = Android16SettingsIntentMap.resolve(appContext, request.command.rawText)
        appContext.startActivity(target)
        return SkillResult.Success("Opened the matching Settings page.")
    }
}
