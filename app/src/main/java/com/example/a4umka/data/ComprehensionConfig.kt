package com.example.a4umka.data

data class ComprehensionConfig(
    val grade: Int = 1,
    val selectedStory: ComprehensionStory? = null,
    val showWordCount: Boolean = true,
    val showAdultHints: Boolean = true,
    val showAnswerSheet: Boolean = false,
    val isBlackAndWhite: Boolean = false
)
