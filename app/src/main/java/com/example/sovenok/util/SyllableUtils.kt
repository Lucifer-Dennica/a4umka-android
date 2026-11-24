package com.example.sovenok.util

object SyllableUtils {
    private val vowels = setOf(
        'а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я',
        'А', 'Е', 'Ё', 'И', 'О', 'У', 'Ы', 'Э', 'Ю', 'Я'
    )
    private val softHard = setOf('ь', 'ъ', 'й', 'Ь', 'Ъ', 'Й')

    fun splitIntoSyllables(word: String): List<String> {
        if (word.isBlank()) return emptyList()
        
        val result = mutableListOf<String>()
        var currentSyllable = StringBuilder()
        var hasVowel = false
        
        for (i in word.indices) {
            val char = word[i]
            currentSyllable.append(char)
            
            if (char in vowels) {
                hasVowel = true
            } else if (hasVowel && i < word.length - 1) {
                val nextChar = word[i + 1]
                // Разрываем слог, если после согласной идет гласная, 
                // и текущая согласная не является 'й', 'ь' или 'ъ'
                if (nextChar in vowels && char !in softHard) {
                    result.add(currentSyllable.toString())
                    currentSyllable = StringBuilder()
                    hasVowel = false
                }
            }
        }
        
        if (currentSyllable.isNotEmpty()) {
            result.add(currentSyllable.toString())
        }
        
        return result
    }
}
