package com.emeth.kernel.nlu

object TextNormalizer {
    private val fillerWords = listOf(
        "can you",
        "could you",
        "please",
        "i want to",
        "i want a",
        "i want",
        "i need to",
        "i need",
        "would you",
        "tell me",
        "show me",
        "take me to",
        "take me",
        "go to",
        "open up",
        "start playing",
        "let me watch",
        "let me see",
        "i want to watch",
        "i want to see",
        "can we watch",
        "can i see",
        "play me"
    )

    fun normalize(rawText: String): String {
        var text = rawText.lowercase()
        
        // Remove punctuation
        text = text.replace(Regex("[.,?!]"), "")
        
        // Normalize common voice transcription artifacts
        // 5.30 am -> 5:30am
        text = text.replace(Regex("([0-9]+)\\.([0-9]{2})\\s*(am|pm)?"), "$1:$2$3")
        text = text.replace(Regex("([0-9]+)\\s+(am|pm)"), "$1$2")
        text = text.replace(" a m", "am")
        text = text.replace(" p m", "pm")
        
        // Remove filler words
        for (filler in fillerWords) {
            text = text.replace(Regex("\\b$filler\\b"), "")
        }

        // Normalize whitespace
        text = text.trim().replace(Regex("\\s+"), " ")

        return text
    }
}
