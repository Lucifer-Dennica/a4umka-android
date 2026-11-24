package com.example.sovenok.data

enum class CopyingMode { COPYING, DICTATION }

data class CopyingConfig(
    val grade: Int = 1,
    val selectedStories: List<CopyingStory> = emptyList(),
    val mode: CopyingMode = CopyingMode.COPYING,
    val childrenCount: Int = 1,
    val lineStep: Int = 8,
    val signature: String = "",
    val addBlankPage: Boolean = false,
    val isBlackAndWhite: Boolean = false,
    val fontSize: Float = 16f,
    val combineOnOnePage: Boolean = false
    val showPrepSheet: Boolean = false
)
