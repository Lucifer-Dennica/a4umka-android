package com.example.a4umka.data

enum class RibbonMode { DESK, BOARD, MISSING, COLORING }
enum class RibbonCase { BOTH, UPPERCASE, LOWERCASE }
enum class MissingCount { EIGHT, SIXTEEN, ALL }
enum class RibbonBgColor { WHITE, PEACH }
enum class RibbonFont { REGULAR, CURSIVE }

data class RibbonConfig(
    val mode: RibbonMode = RibbonMode.DESK,
    val case: RibbonCase = RibbonCase.BOTH,
    val missingCount: MissingCount = MissingCount.EIGHT,
    val showHint: Boolean = true,
    val showAnswers: Boolean = false,
    val bgColor: RibbonBgColor = RibbonBgColor.WHITE,
    val font: RibbonFont = RibbonFont.REGULAR,
    val seed: Long = System.currentTimeMillis()
)
