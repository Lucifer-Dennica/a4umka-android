package com.example.sovenok.util

object SyllableUtils {
    private val vowels = setOf(
        'а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я',
        'А', 'Е', 'Ё', 'И', 'О', 'У', 'Ы', 'Э', 'Ю', 'Я'
    )

    fun splitIntoSyllables(word: String): List<String> {
        if (word.isBlank()) return emptyList()

        val result = mutableListOf<String>()
        var i = 0

        while (i < word.length) {
            val currentChar = word[i]

            if (currentChar in vowels) {
                // Гласная — отдельный слог из 1 буквы (точка)
                result.add(currentChar.toString())
                i++
            } else {
                // Согласная
                if (i + 1 < word.length && word[i + 1] in vowels) {
                    // Следующая гласная — берём 2 буквы (дуга)
                    result.add(word.substring(i, i + 2))
                    i += 2
                } else {
                    // Следующая согласная или конец слова — 1 буква (точка)
                    result.add(currentChar.toString())
                    i++
                }
            }
        }

        return result
    }
}
