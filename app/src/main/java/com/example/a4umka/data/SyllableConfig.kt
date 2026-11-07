package com.example.a4umka.data

enum class SyllableSource { READY, CUSTOM }

data class SyllableConfig(
    val source: SyllableSource = SyllableSource.READY,
    val selectedTexts: List<SyllableText> = emptyList(),
    val customTitle: String = "",
    val customText: String = "",
    val fontSize: Float = 24f,       // <-- Размер шрифта
    val textSpacing: Float = 60f,    // <-- Отступ между текстами
    val showQuestions: Boolean = false,
    val isMultiSelectMode: Boolean = false
)
