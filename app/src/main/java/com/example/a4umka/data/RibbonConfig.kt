package com.example.a4umka.data

enum class RibbonMode { DESK, MISSING, COLORING }
enum class RibbonCase { BOTH, UPPERCASE, LOWERCASE }
enum class MissingCount { EIGHT, SIXTEEN, ALL }

data class RibbonConfig(
    val mode: RibbonMode = RibbonMode.DESK,
    val case: RibbonCase = RibbonCase.BOTH,
    val missingCount: MissingCount = MissingCount.EIGHT,
    val showHint: Boolean = true
)
