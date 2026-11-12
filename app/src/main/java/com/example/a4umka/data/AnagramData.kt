package com.example.a4umka.data

import kotlin.random.Random

data class AnagramWord(
    val word: String,
    val emoji: String,
    val syllables: List<String>,
    val letters: List<String>
)

data class AnagramTask(
    val word: AnagramWord,
    val shuffledParts: List<String>
)

object AnagramData {
    private val allWords = listOf(
        AnagramWord("МОЛОКО", "🥛", listOf("МО", "ЛО", "КО"), "МОЛОКО".map { it.toString() }),
        AnagramWord("ПАНАМА", "👒", listOf("ПА", "НА", "МА"), "ПАНАМА".map { it.toString() }),
        AnagramWord("ГОРА", "⛰️", listOf("ГО", "РА"), "ГОРА".map { it.toString() }),
        AnagramWord("РЫБА", "🐟", listOf("РЫ", "БА"), "РЫБА".map { it.toString() }),
        AnagramWord("МОРЕ", "🌊", listOf("МО", "РЕ"), "МОРЕ".map { it.toString() }),
        AnagramWord("КАПУСТА", "🥬", listOf("КА", "ПУС", "ТА"), "КАПУСТА".map { it.toString() }),
        AnagramWord("РОЗА", "🌹", listOf("РО", "ЗА"), "РОЗА".map { it.toString() }),
        AnagramWord("ШАРЫ", "🎈", listOf("ША", "РЫ"), "ШАРЫ".map { it.toString() }),
        AnagramWord("КОТ", "🐱", listOf("КОТ"), "КОТ".map { it.toString() }),
        AnagramWord("СОБАКА", "🐶", listOf("СО", "БА", "КА"), "СОБАКА".map { it.toString() }),
        AnagramWord("ЛИСА", "🦊", listOf("ЛИ", "СА"), "ЛИСА".map { it.toString() }),
        AnagramWord("ЛУНА", "🌙", listOf("ЛУ", "НА"), "ЛУНА".map { it.toString() }),
        AnagramWord("ЗВЕЗДА", "⭐", listOf("ЗВЕЗ", "ДА"), "ЗВЕЗДА".map { it.toString() }),
        AnagramWord("ДОМ", "🏠", listOf("ДОМ"), "ДОМ".map { it.toString() }),
        AnagramWord("ЦВЕТОК", "🌸", listOf("ЦВЕ", "ТОК"), "ЦВЕТОК".map { it.toString() }),
        AnagramWord("ЯБЛОКО", "🍎", listOf("ЯБ", "ЛО", "КО"), "ЯБЛОКО".map { it.toString() }),
        AnagramWord("СЫР", "🧀", listOf("СЫР"), "СЫР".map { it.toString() }),
        AnagramWord("ХЛЕБ", "🍞", listOf("ХЛЕБ"), "ХЛЕБ".map { it.toString() }),
        AnagramWord("МЯЧ", "⚽", listOf("МЯЧ"), "МЯЧ".map { it.toString() }),
        AnagramWord("СЛОН", "🐘", listOf("СЛОН"), "СЛОН".map { it.toString() })
    )

    fun generateTask(config: AnagramConfig): AnagramTask {
        val word = allWords.random()
        val parts = if (config.type == AnagramType.SYLLABLES) word.syllables else word.letters
        
        var shuffled = parts.shuffled()
        // Гарантируем, что порядок изменится
        while (shuffled == parts && parts.size > 1) {
            shuffled = parts.shuffled()
        }
        
        return AnagramTask(word, shuffled)
    }
}
