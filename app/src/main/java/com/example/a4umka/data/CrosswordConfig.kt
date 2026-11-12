package com.example.a4umka.data

enum class CrosswordDifficulty { EASY, MEDIUM, HARD }

data class CrosswordConfig(
    val difficulty: CrosswordDifficulty = CrosswordDifficulty.EASY,
    val showWordsInClues: Boolean = false,
    val showAnswers: Boolean = true,
    val perPage: Int = 1 // 1 или 2 кроссворда на лист
)
