package com.example.a4umka.data

enum class SyllableWorkType { TABLE, CARDS }
enum class SyllableVowelStep { FIRST, ALL }
enum class SyllableType { OPEN, REVERSE, CLOSED, CLUSTER }

data class SyllableTableConfig(
    val workType: SyllableWorkType = SyllableWorkType.TABLE,
    val consonants: Set<String> = setOf("Л", "М", "Н", "Р", "С"),
    val vowelStep: SyllableVowelStep = SyllableVowelStep.FIRST,
    val syllableType: SyllableType = SyllableType.OPEN,
    val shuffle: Boolean = false,
    val findGame: Boolean = true,
    val syllableOfTheDay: Boolean = true,
    val closedSuffix: String = "М", // Для закрытых слогов
    val clusterList: List<String> = listOf("СТ", "ТР", "КР") // Для стечения
)

object SyllableTableData {
    val allConsonants = listOf("Б", "В", "Г", "Д", "Ж", "З", "К", "Л", "М", "Н", "П", "Р", "С", "Т", "Ф", "Х", "Ц", "Ч", "Ш", "Щ")
    val firstConsonants = listOf("М", "Н", "Л", "Р", "С")
    
    val vowelsFirst = listOf("А", "О", "У", "Ы", "И")
    val vowelsAll = listOf("А", "О", "У", "Ы", "Э", "Я", "Ё", "Ю", "И", "Е")

    fun getVowels(step: SyllableVowelStep): List<String> {
        return if (step == SyllableVowelStep.FIRST) vowelsFirst else vowelsAll
    }

    // Проверка на запрещенные сочетания (ЖИ-ШИ, ЧА-ЩА и т.д.)
    fun isValidSyllable(cons: String, vowel: String): Boolean {
        // Ж, Ш + Ы, Я, Ю -> нельзя
        if (cons in listOf("Ж", "Ш") && vowel in listOf("Ы", "Я", "Ю")) return false
        // Ч, Щ + Я, Ю -> нельзя
        if (cons in listOf("Ч", "Щ") && vowel in listOf("Я", "Ю")) return false
        // Г, К, Х + Ы -> нельзя
        if (cons in listOf("Г", "К", "Х") && vowel == "Ы") return false
        return true
    }
}
