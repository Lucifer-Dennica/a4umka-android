package com.example.a4umka.data

data class CrosswordWord(val word: String, val emoji: String)
data class PlacedWord(val word: String, val emoji: String, val x: Int, val y: Int, val isAcross: Boolean, val num: Int)
data class CrosswordGrid(val cells: Map<Pair<Int, Int>, Char>, val words: List<PlacedWord>)

object CrosswordData {
    private val easyWords = listOf(CrosswordWord("КОТ", "🐱"), CrosswordWord("РАК", "🦞"), CrosswordWord("ДОМ", "🏠"), CrosswordWord("ЛУК", "🧅"))
    private val mediumWords = listOf(CrosswordWord("ЛИСА", "🦊"), CrosswordWord("РЫБА", "🐟"), CrosswordWord("СНЕГ", "❄️"), CrosswordWord("МЯЧ", "⚽"), CrosswordWord("СЫР", "🧀"))
    private val hardWords = listOf(CrosswordWord("БАНАН", "🍌"), CrosswordWord("ЛИМОН", "🍋"), CrosswordWord("АРБУЗ", "🍉"), CrosswordWord("ЯБЛОКО", "🍎"), CrosswordWord("ГРУША", "🍐"), CrosswordWord("СЛИВА", "🍑"), CrosswordWord("ВИШНЯ", "🍒"))

    fun generateGrid(difficulty: CrosswordDifficulty): CrosswordGrid {
        val words = when(difficulty) {
            CrosswordDifficulty.EASY -> easyWords.shuffled()
            CrosswordDifficulty.MEDIUM -> mediumWords.shuffled()
            CrosswordDifficulty.HARD -> hardWords.shuffled()
        }

        // Упрощенный алгоритм: ставим первое слово горизонтально, остальные ищем куда приткнуть
        val placed = mutableListOf<PlacedWord>()
        val cells = mutableMapOf<Pair<Int, Int>, Char>()
        var numCounter = 1

        val first = words.first()
        placed.add(PlacedWord(first.word, first.emoji, 5, 5, true, numCounter++))
        for (i in first.word.indices) {
            cells[Pair(5 + i, 5)] = first.word[i]
        }

        for (wordObj in words.drop(1)) {
            var foundSpot = false
            // Ищем пересечения
            for (pw in placed) {
                if (foundSpot) break
                for (i in pw.word.indices) {
                    val char = pw.word[i]
                    val intersectIndex = wordObj.word.indexOf(char)
                    if (intersectIndex != -1) {
                        // Пытаемся поставить перпендикулярно
                        if (pw.isAcross) {
                            val nx = pw.x + i
                            val ny = pw.y - intersectIndex
                            if (isValidPlacement(cells, wordObj.word, nx, ny, false)) {
                                placed.add(PlacedWord(wordObj.word, wordObj.emoji, nx, ny, false, numCounter++))
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
                                placed.add(PlacedWord(wordObj.word, wordObj.emoji, nx, ny, true, numCounter++))
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
        }
        return CrosswordGrid(cells, placed)
    }

    private fun isValidPlacement(cells: Map<Pair<Int, Int>, Char>, word: String, x: Int, y: Int, isAcross: Boolean): Boolean {
        for (i in word.indices) {
            val cx = if (isAcross) x + i else x
            val cy = if (isAcross) y else y + i
            val existing = cells[Pair(cx, cy)]
            if (existing != null && existing != word[i]) return false
        }
        return true
    }
}
