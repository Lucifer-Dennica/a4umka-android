package com.example.a4umka.data

enum class BookmarkType { COLORING, NAME, CHEAT_SHEET, COUNTER }
enum class BookmarkCharacter { GIRL_BOW, BOY_SCARF, GIRL_CAT, BOY_PLANE }
enum class BookmarkPattern { LEAVES, STARS, WAVES, HEARTS }
enum class CheatSheetType { MULTIPLICATION, ALPHABET, NUMBER_COMPOSITION }
enum class CounterType { BOOKS, PAGES, DAYS }

data class BookmarkSettings(
    val type: BookmarkType = BookmarkType.COLORING,
    val character: BookmarkCharacter = BookmarkCharacter.GIRL_BOW,
    val pattern: BookmarkPattern = BookmarkPattern.LEAVES,
    val name: String = "",
    val cheatSheetType: CheatSheetType = CheatSheetType.MULTIPLICATION,
    val multiplier: Int = 7,
    val counterType: CounterType = CounterType.BOOKS
)

data class BookmarksConfig(
    val bookmarks: List<BookmarkSettings> = List(4) { BookmarkSettings() },
    val printBacksides: Boolean = true,
    val isBlackAndWhite: Boolean = false
)
