package com.example.a4umka.data

enum class ProverbSortType { NUMBERS, DAYS, MONTHS, LETTERS }
enum class ProverbAge { AGE_5_6, AGE_6_7 }

data class ProverbConfig(
    val sortTypes: Set<ProverbSortType> = setOf(ProverbSortType.NUMBERS, ProverbSortType.DAYS, ProverbSortType.MONTHS),
    val age: ProverbAge = ProverbAge.AGE_5_6,
    val pagesCount: Int = 1
)

data class ProverbTask(
    val instruction: String,
    val cards: List<Pair<String, String>>, // Pair(ItemName, Syllable)
    val correctSequence: List<String>, // Правильный порядок предметов (для ответа)
    val phrase: String, // Собранная фраза
    val wordBreaks: List<Int> // Индексы, после которых нужно сделать пробел в клеточках
)
