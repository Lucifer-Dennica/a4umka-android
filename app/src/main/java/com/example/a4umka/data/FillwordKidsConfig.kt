package com.example.a4umka.data

enum class FillwordKidsDifficulty { EASY, MEDIUM, HARD, SUPER_HARD }

data class FillwordKidsConfig(
    val difficulty: FillwordKidsDifficulty = FillwordKidsDifficulty.EASY,
    val onlyPictures: Boolean = false,
    val addTraps: Boolean = false,
    val addAnswers: Boolean = true
)
