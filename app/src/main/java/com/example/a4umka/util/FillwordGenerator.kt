package com.example.a4umka.util

import kotlin.random.Random

data class PlacedWord(
    val word: String,
    val startX: Int,
    val startY: Int,
    val dx: Int,
    val dy: Int
)

data class FillwordGrid(
    val letters: Array<Array<Char>>,
    val size: Int,
    val placedWords: List<PlacedWord>
)

object FillwordGenerator {
    private val alphabet = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"

    fun generate(words: List<String>, manualSize: Int?, allowBackwards: Boolean): FillwordGrid {
        val maxWordLen = words.maxOfOrNull { it.length } ?: 4
        
        // Автоматический подбор размера
        val totalLetters = words.sumOf { it.length }
        val autoSize = (Math.sqrt((totalLetters * 2.5).toDouble()).toInt() + 1).coerceAtLeast(maxWordLen + 1)
        val size = (manualSize ?: autoSize).coerceAtLeast(maxWordLen)

        val grid = Array(size) { Array(size) { ' ' } }
        val placedWords = mutableListOf<PlacedWord>()

        // Сортируем слова по убыванию длины (длинные сложнее разместить)
        val sortedWords = words.sortedByDescending { it.length }

        for (word in sortedWords) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 100) {
                attempts++
                val directions = mutableListOf(
                    Pair(1, 0),  // Вправо
                    Pair(0, 1)   // Вниз
                )
                if (allowBackwards) {
                    directions.add(Pair(-1, 0)) // Влево
                    directions.add(Pair(0, -1)) // Вверх
                }

                val (dx, dy) = directions.random()
                val startX = if (dx == -1) Random.nextInt(word.length - 1, size) else Random.nextInt(0, size - word.length + (if (dx == 1) 1 else 0))
                val startY = if (dy == -1) Random.nextInt(word.length - 1, size) else Random.nextInt(0, size - word.length + (if (dy == 1) 1 else 0))

                var fits = true
                for (i in word.indices) {
                    val x = startX + i * dx
                    val y = startY + i * dy
                    if (x < 0 || x >= size || y < 0 || y >= size) {
                        fits = false; break
                    }
                    val existing = grid[y][x]
                    if (existing != ' ' && existing != word[i]) {
                        fits = false; break
                    }
                }

                if (fits) {
                    for (i in word.indices) {
                        grid[startY + i * dy][startX + i * dx] = word[i]
                    }
                    placedWords.add(PlacedWord(word, startX, startY, dx, dy))
                    placed = true
                }
            }
        }

        // Заполняем пустые клетки случайными буквами
        for (y in 0 until size) {
            for (x in 0 until size) {
                if (grid[y][x] == ' ') {
                    grid[y][x] = alphabet.random()
                }
            }
        }

        return FillwordGrid(grid, size, placedWords)
    }
}
