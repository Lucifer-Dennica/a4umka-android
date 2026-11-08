package com.example.a4umka.data

enum class DiaryPageType { SCHOOL, PICTURES }
enum class DiaryRuling { STANDARD, WIDE, GRID, NONE }
enum class DiaryColor { PINK, GREEN, BLUE, ORANGE, BLACK_WHITE }

data class DiaryConfig(
    val pageType: DiaryPageType = DiaryPageType.SCHOOL,
    val childName: String = "",
    val childGrade: String = "",
    val academicYear: String = "",
    val bookPagesCount: Int = 8,
    val ruling: DiaryRuling = DiaryRuling.STANDARD,
    val color: DiaryColor = DiaryColor.GREEN,
    
    // Что включить в дневник
    val includeTitlePage: Boolean = true,
    val includeBookList: Boolean = true,
    val includeSpeedTest: Boolean = false,
    val listRowsCount: Int = 14,

    // Поля для школьной страницы
    val showAuthor: Boolean = true,
    val showTitle: Boolean = true,
    val showGenre: Boolean = false,
    val showPages: Boolean = true,
    val showDates: Boolean = true,
    val showCharacters: Boolean = true,
    val showPlot: Boolean = true,
    val showMemorable: Boolean = true,
    val showLesson: Boolean = false,
    val showNewWords: Boolean = false,
    val showDrawing: Boolean = true,
    val showRating: Boolean = true
)
