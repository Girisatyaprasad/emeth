package com.emeth.kernel.watchers

import android.content.Context
import kotlinx.coroutines.launch

class TriggerEngine(private val context: Context) {
    private val registry = WatcherRegistry(context)
    private val evaluator = ConditionEvaluator(context)
    private val dispatcher = ActionDispatcher(context)

    fun evaluateAll() {
        val watchers = registry.getAllWatchers()
        for (watcher in watchers) {
            if (evaluator.evaluate(watcher)) {
                dispatcher.dispatch(watcher.action)
                
                val memoryStore = com.emeth.kernel.memory.MemoryStore(context)
                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                    val meta = "{\"watcher_type\":\"${watcher.type.name}\", \"action\":\"${watcher.action}\"}"
                    memoryStore.store(com.emeth.kernel.memory.MemoryType.WATCHER_EVENT, "Watcher triggered", meta)
                }
                
                // Consume the watcher after triggering to prevent spamming
                registry.removeWatcher(watcher.id)
            }
        }
    }
}
