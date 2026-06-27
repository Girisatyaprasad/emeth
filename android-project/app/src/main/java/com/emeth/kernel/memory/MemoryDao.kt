package com.emeth.kernel.memory

interface MemoryDao {
    fun insert(entry: MemoryEntry)
    fun getByType(type: String): List<MemoryEntry>
    fun getByTimeRange(startTime: Long, endTime: Long): List<MemoryEntry>
    fun searchByKeyword(keyword: String): List<MemoryEntry>
    fun searchByTypeAndTime(type: String, startTime: Long, endTime: Long): List<MemoryEntry>
    fun pruneOldEntries(type: String, cutoffTime: Long)
    fun clearAll()
}
