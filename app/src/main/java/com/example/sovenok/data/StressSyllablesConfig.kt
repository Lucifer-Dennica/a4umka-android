package com.example.sovenok.data

// === Конфиг генератора «Ударение и слоги» ===
data class StressSyllablesConfig(
    val wordsCount: Int = 6,        // 6, 8 или 10 слов
    val showAnswers: Boolean = true, // добавить страницу с ответами
    val randomSeed: Long = System.currentTimeMillis()
)

// === Одно слово с разметкой ===
data class StressWord(
    val word: String,              // "ЛУЖА" — полное слово заглавными
    val emoji: String,             // "💧"
    val syllables: List<String>,   // ["ЛУ", "ЖА"] — слоги заглавными
    val stressedSyllable: Int      // 0 — индекс ударного слога
) {
    val syllablesCount: Int get() = syllables.size
}
