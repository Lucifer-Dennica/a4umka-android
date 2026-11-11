package com.example.a4umka.data

import kotlin.random.Random

data class MathProblem(
    val targetWord: String,
    val emoji: String,
    val formula: String
)

object LetterMathData {
    private val words = listOf(
        Pair("КОТ", "🐱"), Pair("ДОМ", "🏠"), Pair("ЛУК", "🧅"),
        Pair("СЫР", "🧀"), Pair("МЯЧ", "⚽"), Pair("ЛЕС", "🌲"),
        Pair("САД", "🌳"), Pair("МЁД", "🍯"), Pair("ГОРА", "⛰️"),
        Pair("РЫБА", "🐟"), Pair("ЛИСА", "🦊"), Pair("ЛУНА", "🌙"),
        Pair("ЗИМА", "❄️"), Pair("ВЕСНА", "🌱"), Pair("ЛЕТО", "☀️"),
        Pair("ОСЕНЬ", "🍂"), Pair("ШКОЛА", "🏫"), Pair("КНИГА", "📚"),
        Pair("СТОЛ", "🪑"), Pair("СТУЛ", "🪑"), Pair("ЯБЛОКО", "🍎"),
        Pair("БАНАН", "🍌"), Pair("ЛИМОН", "🍋"), Pair("АРБУЗ", "🍉"),
        Pair("ЦВЕТОК", "🌸"), Pair("ДЕРЕВО", "🌳"), Pair("МАШИНА", "🚗"),
        Pair("САМОЛЁТ", "✈️"), Pair("РАКЕТА", "🚀"), Pair("РОБОТ", "🤖"),
        Pair("ТЕЛЕФОН", "📱"), Pair("ЧАСЫ", "⏰"), Pair("КЛЮЧ", "🔑"),
        Pair("ЗВЕЗДА", "⭐"), Pair("ТУЧА", "☁️"), Pair("СНЕГ", "🌨️"),
        Pair("ДОЖДЬ", "🌧️"), Pair("ПТИЦА", "🐦"), Pair("СОБАКА", "🐶")
    )

    private val alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"

    fun generateProblems(config: LetterMathConfig, count: Int): List<MathProblem> {
        val result = mutableListOf<MathProblem>()
        val shuffledWords = words.shuffled()

        for (i in 0 until count) {
            val (word, emoji) = shuffledWords[i % words.size]
            val formula = generateFormula(word, config.difficulty)
            result.add(MathProblem(word, emoji, formula))
        }
        return result
    }

    private fun generateFormula(word: String, difficulty: LetterMathDifficulty): String {
        val ops = mutableListOf<String>()
        
        // Количество лишних букв
        val noiseCount = when (difficulty) {
            LetterMathDifficulty.EASY -> 1
            LetterMathDifficulty.MEDIUM -> 2
            LetterMathDifficulty.HARD -> 3
        }

        // Разбиваем слово на кусочки (1-2 буквы) для красоты
        val chunks = mutableListOf<String>()
        var i = 0
        while (i < word.length) {
            if (i == word.length - 1 || Random.nextBoolean()) {
                chunks.add(word[i].toString())
                i++
            } else {
                chunks.add(word.substring(i, i + 2))
                i += 2
            }
        }

        // Генерируем шум
        val noises = mutableListOf<Char>()
        for (n in 0 until noiseCount) {
            noises.add(alphabet.random())
        }

        // Собираем формулу с плюсами
        val finalSequence = mutableListOf<String>()
        
        // Добавляем кусочки и шум вперемешку
        val allItems = mutableListOf<Pair<String, Boolean>>() // String, isNoise
        chunks.forEach { allItems.add(Pair(it, false)) }
        noises.forEach { allItems.add(Pair(it.toString(), true)) }
        
        // Перемешиваем, но сохраняем порядок кусочков слова
        val itemsToAdd = mutableListOf<String>()
        var noiseIndex = 0
        for (chunk in chunks) {
            // Перед кусочком может быть шум
            if (noiseIndex < noises.size && Random.nextBoolean()) {
                itemsToAdd.add(noises[noiseIndex].toString())
                noiseIndex++
            }
            itemsToAdd.add(chunk)
        }
        // Оставшийся шум в конец
        while (noiseIndex < noises.size) {
            itemsToAdd.add(noises[noiseIndex].toString())
            noiseIndex++
        }

        // Строим строку: +А +Б -Б ...
        val formulaBuilder = StringBuilder()
        formulaBuilder.append(itemsToAdd[0])
        for (j in 1 until itemsToAdd.size) {
            formulaBuilder.append(" + ")
            formulaBuilder.append(itemsToAdd[j])
        }

        // Вычитаем весь шум в конце
        for (noise in noises) {
            formulaBuilder.append(" − ")
            formulaBuilder.append(noise)
        }

        return formulaBuilder.toString()
    }
}
