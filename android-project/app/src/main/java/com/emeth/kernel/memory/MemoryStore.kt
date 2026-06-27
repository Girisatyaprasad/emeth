package com.emeth.kernel.memory

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MemoryStore(context: Context) {
    private val db = MemoryDatabase.getDatabase(context)
    private val dao = db.memoryDao()

    suspend fun store(type: MemoryType, content: String, metadata: String? = null) {
        withContext(Dispatchers.IO) {
            dao.insert(
                MemoryEntry(
                    type = type.name,
                    content = content,
                    metadata = metadata
                )
            )
        }
    }
}
