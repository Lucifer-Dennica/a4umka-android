package com.example.sovenok.data

enum class AnagramType { SYLLABLES, LETTERS }

data class AnagramConfig(
    val type: AnagramType = AnagramType.SYLLABLES,
    val showAnswers: Boolean = true,
    val pagesCount: Int = 1
)
