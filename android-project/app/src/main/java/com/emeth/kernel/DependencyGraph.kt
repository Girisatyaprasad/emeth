package com.emeth.kernel

import android.content.Context
import com.emeth.kernel.events.EventBus
import com.emeth.kernel.execution.ExecutionEngine
import com.emeth.kernel.intents.IntentResolver
import com.emeth.kernel.planner.Planner
import com.emeth.kernel.skills.SkillRegistry
import com.emeth.kernel.skills.android.*
import com.emeth.kernel.skills.memory.MemorySkill

object DependencyGraph {
    fun providePlanner(context: Context): Planner {
        val eventBus = EventBus()
        val registry = SkillRegistry()
        val resolver = IntentResolver()
        val engine = ExecutionEngine(eventBus)
        
        // Register Skills
        registry.register(CapabilitySkill())
        registry.register(PermissionStatusSkill(context))
        registry.register(AndroidActionSkill(context))
        registry.register(WhatsNextSkill())
        registry.register(OpenAppSkill(context))
        registry.register(NotificationSkill(context))
        registry.register(ContactsSkill(context))
        registry.register(BrowserSkill(context))
        
        // Media Skills
        registry.register(YouTubeSkill(context))
        registry.register(LastPlayedYouTubeSkill(context))
        registry.register(CameraPhotoSkill(context))
        registry.register(CameraVideoSkill(context))
        registry.register(VoiceRecordSkill(context))
        
        // Health
        registry.register(StepCountSkill(context))

        // Watchers
        registry.register(CreateWatcherSkill(context))
        
        // System Skills
        registry.register(FlashlightSkill(context))
        registry.register(VolumeSkill(context))
        registry.register(MuteSkill(context))
        registry.register(BrightnessSkill(context))
        registry.register(ReadClipboardSkill(context))
        registry.register(SetClipboardSkill(context))
        registry.register(ToggleWifiSkill(context))
        registry.register(ToggleBluetoothSkill(context))
        registry.register(ToggleHotspotSkill(context))
        registry.register(StorageSkill(context))
        registry.register(RamSkill(context))
        registry.register(BatterySkill(context))
        registry.register(FileManagementSkill(context))
        registry.register(PhoneControlSkill(context))
        registry.register(PhoneRecipeSkill(context))
        
        // Communication Skills
        registry.register(CallContactSkill(context))
        registry.register(SmsContactSkill(context))
        registry.register(WhatsAppSkill(context))
        registry.register(ContactsSearchSkill(context))
        
        // Productivity Skills
        registry.register(CalendarSkill(context))
        registry.register(AddCalendarEventSkill(context))
        registry.register(NoteSkill(context))
        registry.register(AlarmSkill(context))
        registry.register(TimerSkill(context))
        registry.register(StopwatchSkill(context))
        
        // Web Skills
        registry.register(WebSearchSkill(context))
        registry.register(OpenBrowserSkill(context))
        registry.register(CheckWeatherSkill(context))
        
        // Settings Skills
        registry.register(SettingsSkill(context))
        registry.register(SettingsWifiSkill(context))
        registry.register(SettingsBluetoothSkill(context))
        registry.register(SettingsDisplaySkill(context))
        registry.register(SettingsSoundSkill(context))
        registry.register(SettingsAccessibilitySkill(context))
        registry.register(SettingsSecuritySkill(context))
        registry.register(SettingsAppsSkill(context))
        registry.register(SettingsBatterySkill(context))
        registry.register(SettingsStorageSkill(context))
        registry.register(SettingsLocationSkill(context))
        registry.register(SettingsDateTimeSkill(context))
        registry.register(SettingsNetworkSkill(context))
        registry.register(SettingsNfcSkill(context))
        registry.register(SettingsCastSkill(context))
        registry.register(SettingsHotspotSkill(context))
        registry.register(SettingsAirplaneModeSkill(context))
        registry.register(SettingsVpnSkill(context))
        registry.register(SettingsDataRoamingSkill(context))
        registry.register(SettingsPrivacySkill(context))
        registry.register(SettingsBiometricSkill(context))
        registry.register(SettingsDeveloperSkill(context))
        registry.register(SettingsAboutSkill(context))
        registry.register(SettingsAccountSkill(context))
        registry.register(SettingsNotificationsSkill(context))
        
        // Third-Party App Skills
        registry.register(ThirdPartyAppSkill(context))

        // Memory Skill
        registry.register(MemorySkill(context))

        return Planner(context, resolver, registry, engine)
    }
}
