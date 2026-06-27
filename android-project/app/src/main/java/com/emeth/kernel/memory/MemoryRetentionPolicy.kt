package com.emeth.kernel.memory

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryRetentionPolicy(context: Context) {
    private val db = MemoryDatabase.getDatabase(context)
    private val dao = db.memoryDao()

    // Define retention rules (e.g., 7 days for ephemeral events, infinite for facts)
    private val rules = mapOf(
        MemoryType.USER_FACT to -1L, // Never delete
        MemoryType.EXECUTED_COMMAND to 7L * 24 * 60 * 60 * 1000, // 7 days
        MemoryType.APP_USAGE to 7L * 24 * 60 * 60 * 1000,
        MemoryType.LOCATION_EVENT to 30L * 24 * 60 * 60 * 1000, // 30 days
        MemoryType.CALENDAR_EVENT to 30L * 24 * 60 * 60 * 1000,
        MemoryType.CONTACT_EVENT to 30L * 24 * 60 * 60 * 1000,
        MemoryType.WATCHER_EVENT to 7L * 24 * 60 * 60 * 1000,
        MemoryType.SYSTEM_EVENT to 3L * 24 * 60 * 60 * 1000 // 3 days
    )

    suspend fun enforcePolicies() {
        withContext(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            for ((type, duration) in rules) {
                if (duration > 0) {
                    val cutoff = now - duration
                    dao.pruneOldEntries(type.name, cutoff)
                }
            }
        }
    }
}
