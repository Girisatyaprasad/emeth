package com.emeth.kernel.memory

import java.util.UUID

data class MemoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: String? = null
)
