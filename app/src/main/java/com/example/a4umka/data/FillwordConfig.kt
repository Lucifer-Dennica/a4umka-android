package com.example.a4umka.data

enum class FillwordSizeMode { AUTO, MANUAL }
enum class FillwordVariants { ONE, TWO, FOUR }

data class FillwordConfig(
    val wordsInput: String = "МЕДВЕДЬ, ЛИСА, ВОЛК, ЗАЯЦ, БЕЛКА",
    val sizeMode: FillwordSizeMode = FillwordSizeMode.AUTO,
    val manualSize: Int = 11,
    val allowBackwards: Boolean = false,
    val title: String = "Филворд",
    val showNameField: Boolean = false,
    val showWordList: Boolean = true,
    val addAnswers: Boolean = true,
    val addTraps: Boolean = false,
    val variants: FillwordVariants = FillwordVariants.ONE
) {
    fun getParsedWords(): List<String> {
        return wordsInput.split(",", "\n")
            .map { it.trim().uppercase() }
            .filter { it.isNotBlank() && it.all { char -> char.isLetter() } }
            .distinct()
    }
}
