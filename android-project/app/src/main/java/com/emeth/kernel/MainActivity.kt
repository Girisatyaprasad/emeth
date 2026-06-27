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

        planner = com.emeth.kernel.DependencyGraph.providePlanner(this)
        
        setContent {
            EmethScreen(planner = planner)
        }
    }
}
