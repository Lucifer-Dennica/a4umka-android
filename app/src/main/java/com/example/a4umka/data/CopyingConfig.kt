package com.example.a4umka.data

enum class CopyingMode { COPYING, DICTATION }

data class CopyingConfig(
    val grade: Int = 1,
    val selectedStories: List<CopyingStory> = emptyList(),
    val mode: CopyingMode = CopyingMode.COPYING,
    val childrenCount: Int = 1,
    val lineStep: Int = 8, // в мм
    val signature: String = "",
    val addBlankPage: Boolean = false,
    val isBlackAndWhite: Boolean = false,
    val fontSize: Float = 16f
)
