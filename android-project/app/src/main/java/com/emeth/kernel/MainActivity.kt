package com.emeth.kernel

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.emeth.kernel.events.EventBus
import com.emeth.kernel.execution.ExecutionEngine
import com.emeth.kernel.intents.IntentResolver
import com.emeth.kernel.skills.SkillRegistry
import com.emeth.kernel.skills.android.*
import com.emeth.kernel.planner.Planner
import com.emeth.kernel.ui.EmethScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var planner: Planner
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.emeth.kernel.spine.EmethSpineBootstrap.initialize(this)
        com.emeth.kernel.health.StepCounterRepository.start(this)
        CoroutineScope(Dispatchers.IO).launch {
            com.emeth.kernel.intents.IntentCapabilityScanner.scanAndPublish(this@MainActivity)
        }
        
        val eventBus = EventBus()
        val registry = SkillRegistry()
        val resolver = IntentResolver()
        val engine = ExecutionEngine(eventBus)
        
        // Register Skills
        registry.register(CapabilitySkill())
        registry.register(PermissionStatusSkill(this))
        registry.register(WhatsNextSkill())
        registry.register(OpenAppSkill(this))
        registry.register(NotificationSkill(this))
        registry.register(ContactsSkill(this))
        registry.register(BrowserSkill(this))
        
        // Media Skills
        registry.register(YouTubeSkill(this))
        registry.register(LastPlayedYouTubeSkill(this))
        registry.register(CameraPhotoSkill(this))
        registry.register(CameraVideoSkill(this))
        registry.register(VoiceRecordSkill(this))
        
        // Health
        registry.register(StepCountSkill(this))

        // Watchers
        registry.register(CreateWatcherSkill(this))
        
        // System Skills
        registry.register(FlashlightSkill(this))
        registry.register(VolumeSkill(this))
        registry.register(MuteSkill(this))
        registry.register(BrightnessSkill(this))
        registry.register(ReadClipboardSkill(this))
        registry.register(StorageSkill(this))
        registry.register(RamSkill(this))
        registry.register(BatterySkill(this))
        registry.register(FileManagementSkill(this))
        registry.register(PhoneControlSkill(this))
        registry.register(PhoneRecipeSkill(this))
        
        // Communication Skills
        registry.register(CallContactSkill(this))
        registry.register(SmsContactSkill(this))
        registry.register(WhatsAppSkill(this))
        registry.register(ContactsSearchSkill(this))
        
        // Productivity Skills
        registry.register(CalendarSkill(this))
        registry.register(AddCalendarEventSkill(this))
        registry.register(NoteSkill(this))
        registry.register(AlarmSkill(this))
        registry.register(TimerSkill(this))
        registry.register(StopwatchSkill(this))
        
        // Web Skills
        registry.register(WebSearchSkill(this))
        
        // Settings Skills
        registry.register(SettingsSkill(this))
        registry.register(SettingsWifiSkill(this))
        registry.register(SettingsBluetoothSkill(this))
        registry.register(SettingsDisplaySkill(this))
        registry.register(SettingsSoundSkill(this))
        registry.register(SettingsAccessibilitySkill(this))
        registry.register(SettingsSecuritySkill(this))
        registry.register(SettingsAppsSkill(this))
        registry.register(SettingsBatterySkill(this))
        registry.register(SettingsStorageSkill(this))
        registry.register(SettingsLocationSkill(this))
        registry.register(SettingsDateTimeSkill(this))
        
        // Memory Skill
        registry.register(com.emeth.kernel.skills.memory.MemorySkill(this))

        // Enqueue Watcher WorkManager
        val workRequest = androidx.work.PeriodicWorkRequestBuilder<com.emeth.kernel.watchers.WatcherWorker>(15, java.util.concurrent.TimeUnit.MINUTES)
            .build()
        androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "EmethWatcherWorker",
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
        
        val watcherRegistry = com.emeth.kernel.watchers.WatcherRegistry(this)
        watcherRegistry.removeSeededDemoWatchers()

        planner = Planner(this, resolver, registry, engine)
        
        setContent {
            EmethScreen(planner = planner)
        }
    }
}
