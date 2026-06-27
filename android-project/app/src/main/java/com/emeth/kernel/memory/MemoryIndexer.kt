package com.emeth.kernel.memory

class MemoryIndexer {
    fun extractKeywords(content: String): List<String> {
        val stopWords = setOf("the", "is", "at", "which", "on", "a", "an", "and", "or", "but", "in", "to", "for", "with")
        val words = content.lowercase().split(Regex("\\W+"))
        return words.filter { it.isNotBlank() && !stopWords.contains(it) }
    }

    fun buildIndexString(content: String): String {
        return extractKeywords(content).joinToString(" ")
    }
}
