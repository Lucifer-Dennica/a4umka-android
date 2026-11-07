package com.example.a4umka.data

enum class SyllableSource { READY, CUSTOM }

data class SyllableConfig(
    val source: SyllableSource = SyllableSource.READY,
    val selectedTexts: List<SyllableText> = emptyList(), // Список выбранных текстов
    val customTitle: String = "",
    val customText: String = "",
    val isLargeFont: Boolean = false,
    val showQuestions: Boolean = false,
    val isMultiSelectMode: Boolean = false // Включен ли режим мультивыбора
)
