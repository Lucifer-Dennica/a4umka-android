package com.example.sovenok.data

enum class DiaryPageType { SCHOOL, PICTURES }
enum class DiaryRuling { STANDARD, WIDE, NONE }
enum class DiaryColor { GREEN, ORANGE, BLUE, PINK, BLACK_WHITE }
enum class DiaryFont { SYSTEM, CAVEAT }

data class DiaryBook(
    val author: String = "",
    val title: String = "",
    val genre: String = "",
    val pages: String = "",
    val dates: String = "",
    val characters: String = "",
    val plot: String = "",
    val memorable: String = "",
    val lesson: String = "",
    val newWords: String = "",
    val rating: Int = 0 
) {
    fun isEmpty(): Boolean =
        author.isBlank() && title.isBlank() && genre.isBlank() && pages.isBlank() &&
        dates.isBlank() && characters.isBlank() && plot.isBlank() && memorable.isBlank() &&
        lesson.isBlank() && newWords.isBlank() && rating == 0

    fun statusText(): String = when {
        title.isNotBlank() -> title
        !isEmpty() -> "заполнена"
        else -> "не заполнена"
    }
}

data class DiaryConfig(
    val pageType: DiaryPageType = DiaryPageType.SCHOOL,
    val childName: String = "",
    val childGrade: String = "",
    val academicYear: String = "",
    val bookPagesCount: Int = 8,
    val ruling: DiaryRuling = DiaryRuling.STANDARD,
    val color: DiaryColor = DiaryColor.GREEN,
    val titleFont: DiaryFont = DiaryFont.CAVEAT,
    val labelFont: DiaryFont = DiaryFont.SYSTEM,
    val valueFont: DiaryFont = DiaryFont.CAVEAT,
    val includeTitlePage: Boolean = true,
    val includeBookList: Boolean = true,
    val includeSpeedTest: Boolean = false,
    val listRowsCount: Int = 14,
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
    val showRating: Boolean = true,
    val books: List<DiaryBook> = List(16) { DiaryBook() }
)
