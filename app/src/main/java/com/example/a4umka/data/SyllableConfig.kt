package com.example.a4umka.data

enum class SyllableSource { READY, CUSTOM }

data class SyllableConfig(
    val source: SyllableSource = SyllableSource.READY,
    val title: String = "",
    val text: String = "",
    val isLargeFont: Boolean = false,
    val showQuestions: Boolean = false
)
