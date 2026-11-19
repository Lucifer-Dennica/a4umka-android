package com.example.sovenok.data

// === Группы безударных гласных ===
enum class VowelGroup(val title: String, val vowels: List<Char>) {
    O_A("О/А", listOf('о', 'а')),
    E_I("Е/И", listOf('е', 'и')),
    YA("Я", listOf('я'))
}

// === Тип задания ===
enum class VowelTaskType {
    INSERT_AND_CHECK,   // вставь букву и запиши проверочное слово
    INSERT_ONLY          // только вставь букву
}

// === Одно слово с проверкой ===
data class UnstressedWord(
    val word: String,              // "холмы" — с пропуском: "х_лмы"
    val missingIndex: Int,         // 1 — индекс пропущенной буквы (0-based в строке ниже)
    val correctLetter: Char,       // 'о'
    val checkWord: String,         // "холм"
    val group: VowelGroup          // O_A
) {
    // Слово с пропуском: "х_лмы"
    val displayWord: String
        get() = word.substring(0, missingIndex) + "_" + word.substring(missingIndex + 1)
}

// === Конфиг ===
data class UnstressedVowelsConfig(
    val groups: Set<VowelGroup> = setOf(VowelGroup.O_A, VowelGroup.E_I),
    val taskType: VowelTaskType = VowelTaskType.INSERT_AND_CHECK,
    val wordsCount: Int = 12,      // 12, 16, 20, 24
    val showAnswers: Boolean = true,
    val randomSeed: Long = System.currentTimeMillis()
)
