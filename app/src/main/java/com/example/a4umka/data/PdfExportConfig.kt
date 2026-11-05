package com.example.a4umka.data

enum class LetterCase { UPPERCASE, LOWERCASE, BOTH }

data class PdfExportConfig(
    val letterCase: LetterCase = LetterCase.BOTH,
    val rowsCount: Int = 4,
    val isCursive: Boolean = true,
    val showGuides: Boolean = true
)
