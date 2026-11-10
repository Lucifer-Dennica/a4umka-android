package com.example.a4umka.data

enum class AssembleTaskType { CONNECT, PICTURE, CARDS }
enum class AssembleDifficulty { EASY, MEDIUM, HARD, SUPER_HARD }

data class AssembleWordConfig(
    val taskType: AssembleTaskType = AssembleTaskType.CONNECT,
    val difficulty: AssembleDifficulty = AssembleDifficulty.EASY,
    val colorSyllables: Boolean = true,
    val firstStepVowelsOnly: Boolean = false,
    val emojiHints: Boolean = false,
    val addAnswerSheet: Boolean = true
)
