package com.example.a4umka.data

enum class AlphabetCase { UPPERCASE, LOWERCASE, BOTH }
enum class AlphabetFilter { ALL, VOWELS, CONSONANTS }
enum class PosterColor { WHITE, GREEN, BLUE, PINK, ORANGE, BLACK_WHITE }
enum class AlphabetLayoutType { POSTER, FLASHCARDS, COLORING }

data class AlphabetConfig(
    val layoutType: AlphabetLayoutType = AlphabetLayoutType.POSTER,
    val case: AlphabetCase = AlphabetCase.BOTH,
    val filter: AlphabetFilter = AlphabetFilter.ALL,
    val color: PosterColor = PosterColor.WHITE,
    val showImages: Boolean = true,
    val isCursive: Boolean = false,
    val startLetterIndex: Int = 0, // Индексы для диапазона
    val endLetterIndex: Int = 32, // По умолчанию до 'Я' (33 буквы)
    val cardsPerPage: Int = 4 // Для режима карточек (1, 2, 4, 6)
)
