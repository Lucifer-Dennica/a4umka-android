package com.example.sovenok.data

enum class SyllableSource { READY, CUSTOM }

data class SyllableConfig(
    val source: SyllableSource = SyllableSource.READY,
    val selectedTexts: List<SyllableText> = emptyList(),
    val customTitle: String = "",
    val customText: String = "",
    val customQuestions: List<String> = emptyList(), 
    val isMultiSelectMode: Boolean = false,
    val fontSize: Float = 24f,
    val textSpacing: Float = 40f,
    val showQuestions: Boolean = true
)
