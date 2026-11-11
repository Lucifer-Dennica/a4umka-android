package com.example.a4umka.data

data class SimpleRebusWord(val word: String, val emojis: List<String>)

object SimpleRebusData {
    val threeLetters = listOf(
        SimpleRebusWord("КОТ", listOf("🐱", "☁️", "🐯")),
        SimpleRebusWord("ДОМ", listOf("🦤", "☁️", "🐭")),
        SimpleRebusWord("ЛУК", listOf("🦊", "🦆", "🐱")),
        SimpleRebusWord("СОН", listOf("🐘", "☁️", "🦏")),
        SimpleRebusWord("МАК", listOf("🐭", "🍍", "🐱")),
        SimpleRebusWord("РАК", listOf("🐟", "🍍", "🐱")),
        SimpleRebusWord("МЁД", listOf("🐭", "🦔", "🏠")),
        SimpleRebusWord("ЛЕС", listOf("🦊", "🦔", "🐘")),
        SimpleRebusWord("СЫР", listOf("🐘", "🍋", "🐟")),
        SimpleRebusWord("ЖУК", listOf("🦒", "🦆", "🐱")),
        SimpleRebusWord("БЫК", listOf("🦋", "🍋", "🐱")),
        SimpleRebusWord("НОС", listOf("🦏", "☁️", "🐘"))
    )

    val threeFourLetters = listOf(
        SimpleRebusWord("ЛИСА", listOf("🦊", "🍎", "🐘", "🍉")),
        SimpleRebusWord("ЛУНА", listOf("🦊", "🦆", "🦏", "🍎")),
        SimpleRebusWord("РЫБА", listOf("🐟", "🍋", "🦋", "🍎")),
        SimpleRebusWord("РОЗА", listOf("🐟", "☁️", "🦓", "🍎")),
        SimpleRebusWord("ЗИМА", listOf("🦓", "🦃", "🐭", "🍎")),
        SimpleRebusWord("КОЗА", listOf("🐱", "☁️", "🦓", "🍎")),
        SimpleRebusWord("СЛОН", listOf("🐘", "🦊", "☁️", "🦏")),
        SimpleRebusWord("ТИГР", listOf("🐯", "🦃", "🦒", "🐟"))
    )

    fun getRandomWords(length: SimpleRebusLength, count: Int): List<SimpleRebusWord> {
        val source = if (length == SimpleRebusLength.THREE) threeLetters else threeFourLetters
        return source.shuffled().take(count)
    }
}
