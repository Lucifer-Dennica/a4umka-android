package com.example.a4umka.data

data class RebusConfig(
    val title: String = "Разгадай моё слово",
    val wordsInput: String = "ШКОЛА\nДРУЖБА",
    val cardsPerRow: Int = 6,
    val wordsPerPage: Int = 1, // 1, 2 или 4
    val showHints: Boolean = true,
    val addAnswers: Boolean = true,
    val isBlackAndWhite: Boolean = false
)
