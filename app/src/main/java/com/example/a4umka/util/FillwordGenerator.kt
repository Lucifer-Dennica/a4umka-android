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

    fun generate(words: List<String>, manualSize: Int?, allowBackwards: Boolean, allowVertical: Boolean = true): FillwordGrid {
        val maxWordLen = words.maxOfOrNull { it.length } ?: 4
        val totalLetters = words.sumOf { it.length }
        val autoSize = (Math.sqrt((totalLetters * 2.5).toDouble()).toInt() + 1).coerceAtLeast(maxWordLen + 1)
        val size = (manualSize ?: autoSize).coerceAtLeast(maxWordLen + 1)

        val grid = Array(size) { Array(size) { ' ' } }
        val placedWords = mutableListOf<PlacedWord>()

        val sortedWords = words.sortedByDescending { it.length }

        for (word in sortedWords) {
            var placed = false
            var attempts = 0
            while (!placed && attempts < 100) {
                attempts++
                val directions = mutableListOf(Pair(1, 0)) // Всегда вправо
                if (allowVertical) directions.add(Pair(0, 1)) // Вниз
                if (allowBackwards) {
                    directions.add(Pair(-1, 0))
                    if (allowVertical) directions.add(Pair(0, -1))
                }

                val (dx, dy) = directions.random()
                
                val maxStartX = if (dx == 1) size - word.length else size - 1
                val minStartX = if (dx == -1) word.length - 1 else 0
                val maxStartY = if (dy == 1) size - word.length else size - 1
                val minStartY = if (dy == -1) word.length - 1 else 0

                if (maxStartX < minStartX || maxStartY < minStartY) continue

                val startX = Random.nextInt(minStartX, maxStartX + 1)
                val startY = Random.nextInt(minStartY, maxStartY + 1)

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
