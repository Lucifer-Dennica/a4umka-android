package com.example.a4umka.data

data class CustomCrosswordConfig(
    val title: String = "Мой кроссворд",
    val wordsInput: String = "",
    val showAnswers: Boolean = true,
    val seed: Long = System.currentTimeMillis() // Для кнопки "Другая раскладка"
)

data class CustomWordPair(val word: String, val question: String)
