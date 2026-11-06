package com.example.a4umka.data

enum class AlphabetCase { UPPERCASE, LOWERCASE, BOTH }
enum class AlphabetFilter { ALL, VOWELS, CONSONANTS }
enum class PosterTheme { WHITE, GREEN, PASTEL }

data class AlphabetConfig(
    val case: AlphabetCase = AlphabetCase.BOTH,
    val filter: AlphabetFilter = AlphabetFilter.ALL,
    val theme: PosterTheme = PosterTheme.WHITE,
    val showImages: Boolean = true,
    val isCursive: Boolean = false // <-- НОВОЕ: по умолчанию печатные буквы
)
