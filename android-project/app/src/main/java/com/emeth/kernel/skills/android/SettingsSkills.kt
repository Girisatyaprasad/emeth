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
