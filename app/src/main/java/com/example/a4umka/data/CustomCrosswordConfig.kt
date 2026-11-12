package com.example.a4umka.data

enum class CrosswordMode { STANDARD, SCANWORD }

data class CustomCrosswordConfig(
    val title: String = "Мой кроссворд",
    val wordsInput: String = "",
    val mode: CrosswordMode = CrosswordMode.STANDARD,
    val showAnswers: Boolean = true,
    val seed: Long = System.currentTimeMillis() // Для кнопки "Перемешать сетку"
)

data class CustomWordPair(val word: String, val question: String)
