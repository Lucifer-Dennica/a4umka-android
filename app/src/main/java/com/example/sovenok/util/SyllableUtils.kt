package com.example.sovenok.util

object SyllableUtils {
    private val vowels = setOf(
        'а', 'е', 'ё', 'и', 'о', 'у', 'ы', 'э', 'ю', 'я',
        'А', 'Е', 'Ё', 'И', 'О', 'У', 'Ы', 'Э', 'Ю', 'Я'
    )

    fun splitIntoSyllables(word: String): List<String> {
        if (word.isBlank()) return emptyList()
        
        val result = mutableListOf<String>()
        var currentSyllable = StringBuilder()
        var consonantCount = 0
        
        for (i in word.indices) {
            val char = word[i]
            
            if (char in vowels) {
                // Гласная - завершаем текущий слог
                currentSyllable.append(char)
                result.add(currentSyllable.toString())
                currentSyllable = StringBuilder()
                consonantCount = 0
            } else {
                // Согласная
                if (currentSyllable.isEmpty()) {
                    // Согласная перед гласной
                    consonantCount++
                    currentSyllable.append(char)
                    
                    // Если следующая буква гласная, оставляем эту согласную в текущем слоге
                    // Если следующая буква согласная, и у нас уже есть 1 согласная, 
                    // то текущую делаем отдельным слогом
                    if (i + 1 < word.length && word[i + 1] !in vowels && consonantCount >= 1) {
                        result.add(currentSyllable.toString())
                        currentSyllable = StringBuilder()
                        consonantCount = 0
                    }
                } else {
                    // Согласная после гласной (в конце слова или перед следующей гласной)
                    currentSyllable.append(char)
                }
            }
        }
        
        // Добавляем остаток
        if (currentSyllable.isNotEmpty()) {
            result.add(currentSyllable.toString())
        }
        
        return result
    }
}
