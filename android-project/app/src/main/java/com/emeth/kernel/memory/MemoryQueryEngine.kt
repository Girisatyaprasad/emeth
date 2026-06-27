package com.emeth.kernel.memory

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

class MemoryQueryEngine(context: Context) {
    private val db = MemoryDatabase.getDatabase(context)
    private val dao = db.memoryDao()

    suspend fun queryByType(type: MemoryType): List<MemoryEntry> {
        return withContext(Dispatchers.IO) {
            dao.getByType(type.name)
        }
    }

    suspend fun queryByTimeRange(startTimeMs: Long, endTimeMs: Long): List<MemoryEntry> {
        return withContext(Dispatchers.IO) {
            dao.getByTimeRange(startTimeMs, endTimeMs)
        }
    }

    suspend fun queryByKeyword(keyword: String): List<MemoryEntry> {
        return withContext(Dispatchers.IO) {
            dao.searchByKeyword(keyword)
        }
    }

    suspend fun queryToday(): List<MemoryEntry> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()
        return queryByTimeRange(startTime, endTime)
    }

    suspend fun queryYesterday(): List<MemoryEntry> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val endTime = calendar.timeInMillis - 1
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val startTime = calendar.timeInMillis
        return queryByTimeRange(startTime, endTime)
    }
}
