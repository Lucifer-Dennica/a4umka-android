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
    val showImage: Boolean = true,  // <-- РАЗДЕЛЕНО
    val showWord: Boolean = true,   // <-- РАЗДЕЛЕНО
    val isCursive: Boolean = false,
    val startLetterIndex: Int = 0,
    val endLetterIndex: Int = 32,
    val cardsPerPage: Int = 4
)
