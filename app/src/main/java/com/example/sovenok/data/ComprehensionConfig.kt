package com.example.sovenok.data

data class ComprehensionConfig(
    val grade: Int = 1,
    val selectedStory: ComprehensionStory? = null,
    val showWordCount: Boolean = true,
    val showAdultHints: Boolean = true,
    val showAnswerSheet: Boolean = true,
    val isPeachBackground: Boolean = false,
    val fontSize: Float = 18f
)
