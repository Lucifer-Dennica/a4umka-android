package com.example.a4umka.data

enum class PrepositionTaskType { FIND_WORD, FIND_PICTURE, DRAW_SELF }
enum class PrepositionLevel { SIMPLE, BASIC, ALL }

data class PrepositionConfig(
    val taskType: PrepositionTaskType = PrepositionTaskType.FIND_WORD,
    val level: PrepositionLevel = PrepositionLevel.BASIC,
    val tasksCount: Int = 6,
    val showAnswers: Boolean = true
)
