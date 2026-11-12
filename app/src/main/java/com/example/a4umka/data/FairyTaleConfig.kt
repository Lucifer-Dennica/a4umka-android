package com.example.a4umka.data

enum class FairyTaleTaskType { FIX_NAMES, CONNECT_HALVES, GUESS_PICTURES }
enum class FairyTaleAge { AGE_5_6, AGE_6_7 }

data class FairyTaleConfig(
    val taskTypes: Set<FairyTaleTaskType> = setOf(
        FairyTaleTaskType.FIX_NAMES,
        FairyTaleTaskType.GUESS_PICTURES,
        FairyTaleTaskType.CONNECT_HALVES
    ),
    val age: FairyTaleAge = FairyTaleAge.AGE_5_6,
    val pagesCount: Int = 1
)
