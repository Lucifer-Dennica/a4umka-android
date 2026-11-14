package com.example.a4umka.data

import kotlin.random.Random

data class PlacedWord(val word: String, val emoji: String, val x: Int, val y: Int, val isAcross: Boolean, val num: Int)
data class CrosswordGrid(val cells: Map<Pair<Int, Int>, Char>, val words: List<PlacedWord>, val failedWords: List<String> = emptyList())

object CustomCrosswordData {
    // ... (темы остаются без изменений)
    val themes = mapOf(
        "Животные" to "ЛИСА — Рыжая плутовка из сказок\nВОЛК — Серый разбойник\nЗАЯЦ — Трусишка с длинными ушами\nМЕДВЕДЬ — Спит всю зиму в берлоге\nЕЖ — Колючий клубок\nБЕЛКА — Прыгает по веткам с орешком",
        "Школа" to "РУЧКА — Пишет синим по бумаге\nТЕТРАДЬ — В ней пишут домашнее задание\nДОСКА — На ней пишут мелом\nПОРТФЕЛЬ — В нем носят учебники\nЗВОНОК — Зовет на урок и с урока\nУРОК — Время, когда учитель объясняет",
        "Еда" to "ЯБЛОКО — Фрукт, который упал на Ньютона\nХЛЕБ — Всему голова\nМОЛОКО — Белый напиток от коровы\nСУП — Первое блюдо\nСЫР — Продукт с дырками\nБАНАН — Желтый фрукт для обезьян",
        "Транспорт" to "МАШИНА — Едет по дороге на четырех колесах\nСАМОЛЕТ — Летает в небе\nКОРАБЛЬ — Плавает по морю\nПОЕЗД — Едет по рельсам\nВЕЛОСИПЕД — У него есть педали\nАВТОБУС — Везет пассажиров по городу",
        "Космос" to "КОСМОС — Там летают ракеты и звезды\nЛУНА — Спутник Земли\nСОЛНЦЕ — Звезда, которая светит днем\nРАКЕТА — Летит в космос\nЗВЕЗДА — Светится ночью в небе\nПЛАНЕТА — Земля, Марс или Юпитер"
    )

    fun parseInput(input: String): List<CustomWordPair> {
        return input.lines().mapNotNull { line ->
            val parts = line.split("—", "-", ":").map { it.trim() }
            if (parts.size >= 2) {
                val word = parts[0].uppercase().replace("Ё", "Е").filter { it.isLetter() }
                val question = parts[1]
                if (word.length >= 3 && word.length <= 12) {
                    CustomWordPair(word, question)
                } else null
            } else null
        }
    }

    fun generateGrid(words: List<CustomWordPair>, seed: Long): CrosswordGrid {
        val random = Random(seed)
        val placed = mutableListOf<PlacedWord>()
        val cells = mutableMapOf<Pair<Int, Int>, Char>()
        val failedWords = mutableListOf<String>()
        var numCounter = 1

        if (words.isEmpty()) return CrosswordGrid(cells, placed)

        // Сортируем по длине (сначала длинные)
        val sortedWords = words.sortedByDescending { it.word.length }

        // Ставим первое слово
        val first = sortedWords.first()
        placed.add(PlacedWord(first.word, "❓", 10, 10, true, numCounter++))
        for (i in first.word.indices) {
            cells[Pair(10 + i, 10)] = first.word[i]
        }

        // Пытаемся пристроить остальные
        for (wordObj in sortedWords.drop(1)) {
            var foundSpot = false
            // Перемешиваем существующие слова для разной раскладки
            val placedSnapshot = placed.toList().shuffled(random) 
            
            for (pw in placedSnapshot) {
                if (foundSpot) break
                for (i in pw.word.indices) {
                    val char = pw.word[i]
                    val intersectIndex = wordObj.word.indexOf(char)
                    if (intersectIndex != -1) {
                        if (pw.isAcross) {
                            val nx = pw.x + i
                            val ny = pw.y - intersectIndex
                            if (isValidPlacement(cells, wordObj.word, nx, ny, false)) {
                                placed.add(PlacedWord(wordObj.word, "❓", nx, ny, false, numCounter++))
                                for (j in wordObj.word.indices) {
                                    cells[Pair(nx, ny + j)] = wordObj.word[j]
                                }
                                foundSpot = true
                                break
                            }
                        } else {
                            val nx = pw.x - intersectIndex
                            val ny = pw.y + i
                            if (isValidPlacement(cells, wordObj.word, nx, ny, true)) {
                                placed.add(PlacedWord(wordObj.word, "❓", nx, ny, true, numCounter++))
                                for (j in wordObj.word.indices) {
                                    cells[Pair(nx + j, ny)] = wordObj.word[j]
                                }
                                foundSpot = true
                                break
                            }
                        }
                    }
                }
            }
            if (!foundSpot) {
                failedWords.add(wordObj.word)
            }
        }
        return CrosswordGrid(cells, placed, failedWords)
    }

    private fun isValidPlacement(cells: Map<Pair<Int, Int>, Char>, word: String, x: Int, y: Int, isAcross: Boolean): Boolean {
        for (i in word.indices) {
            val cx = if (isAcross) x + i else x
            val cy = if (isAcross) y else y + i
            val existing = cells[Pair(cx, cy)]
            
            if (existing != null && existing != word[i]) return false
            
            // Проверка на соседние слова, чтобы они не слипались
            if (existing == null) {
                if (isAcross) {
                    if (cells[Pair(cx, cy - 1)] != null || cells[Pair(cx, cy + 1)] != null) return false
                } else {
                    if (cells[Pair(cx - 1, cy)] != null || cells[Pair(cx + 1, cy)] != null) return false
                }
            }
        }
        return true
    }
}
