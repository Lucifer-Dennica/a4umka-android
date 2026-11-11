package com.example.a4umka.data

enum class LetterMathDifficulty { EASY, MEDIUM, HARD }
enum class LetterMathHint { NONE, PICTURE }

data class LetterMathConfig(
    val difficulty: LetterMathDifficulty = LetterMathDifficulty.EASY,
    val hint: LetterMathHint = LetterMathHint.NONE,
    val pagesCount: Int = 1
)
