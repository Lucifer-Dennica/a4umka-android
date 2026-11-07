package com.example.a4umka.util

object SyllableUtils {
    private val vowels = "аеёиоуыэюяАЕЁИОУЫЭЮЯ"

    // Простой алгоритм для детей: слог = согласная(ые) + гласная + возможная согласная
    fun splitIntoSyllables(word: String): List<String> {
        if (word.isEmpty()) return emptyList()
        if (word.length == 1) return listOf(word)

        val syllables = mutableListOf<String>()
        var currentSyllable = StringBuilder()
        var hasVowel = false

        for (char in word) {
            currentSyllable.append(char)
            if (char in vowels) {
                hasVowel = true
            } else if (hasVowel && currentSyllable.length > 1) {
                // Если текущий слог уже содержит гласную, и мы встретили согласную,
                // это может быть конец слога (например, "ма-ши-на")
                // Но для простоты и чтобы не разрывать "ст" и т.п., мы делаем слог
                // до следующей гласной.
            }
        }

        // Более простой и надежный для детей алгоритм: делим перед каждой согласной,
        // за которой следует гласная, кроме случаев с й, ь, ъ.
        var i = 0
        val result = mutableListOf<String>()
        var start = 0
        while (i < word.length) {
            if (i > start && i < word.length - 1) {
                val currentChar = word[i]
                val nextChar = word[i + 1]
                if (currentChar !in vowels && nextChar in vowels) {
                    // Нашли начало нового слога
                    result.add(word.substring(start, i))
                    start = i
                }
            }
            i++
        }
        result.add(word.substring(start))
        return result
    }
}
