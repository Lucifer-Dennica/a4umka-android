package com.example.a4umka.data

import kotlin.random.Random

data class WordItem(
    val word: String,
    val syllables: List<String>,
    val emoji: String,
    val difficulty: AssembleDifficulty
)

object AssembleWordData {
    val allWords = listOf(
        // EASY (2 слога)
        WordItem("лиса", listOf("ли", "са"), "🦊", AssembleDifficulty.EASY),
        WordItem("коза", listOf("ко", "за"), "🐐", AssembleDifficulty.EASY),
        WordItem("луна", listOf("лу", "на"), "🌙", AssembleDifficulty.EASY),
        WordItem("мама", listOf("ма", "ма"), "👩", AssembleDifficulty.EASY),
        WordItem("рыба", listOf("ры", "ба"), "🐟", AssembleDifficulty.EASY),
        WordItem("вода", listOf("во", "да"), "💧", AssembleDifficulty.EASY),
        WordItem("рука", listOf("ру", "ка"), "✋", AssembleDifficulty.EASY),
        WordItem("нога", listOf("но", "га"), "🦵", AssembleDifficulty.EASY),
        WordItem("гора", listOf("го", "ра"), "⛰️", AssembleDifficulty.EASY),
        WordItem("зима", listOf("зи", "ма"), "❄️", AssembleDifficulty.EASY),
        WordItem("лето", listOf("ле", "то"), "☀️", AssembleDifficulty.EASY),
        WordItem("мыло", listOf("мы", "ло"), "🧼", AssembleDifficulty.EASY),
        WordItem("кино", listOf("ки", "но"), "🎬", AssembleDifficulty.EASY),
        WordItem("небо", listOf("не", "бо"), "☁️", AssembleDifficulty.EASY),
        WordItem("море", listOf("мо", "ре"), "🌊", AssembleDifficulty.EASY),
        
        // MEDIUM (3 слога)
        WordItem("машина", listOf("ма", "ши", "на"), "🚗", AssembleDifficulty.MEDIUM),
        WordItem("собака", listOf("со", "ба", "ка"), "🐶", AssembleDifficulty.MEDIUM),
        WordItem("корова", listOf("ко", "ро", "ва"), "🐮", AssembleDifficulty.MEDIUM),
        WordItem("молоко", listOf("мо", "ло", "ко"), "🥛", AssembleDifficulty.MEDIUM),
        WordItem("ворона", listOf("во", "ро", "на"), "🐦", AssembleDifficulty.MEDIUM),
        WordItem("береза", listOf("бе", "ре", "за"), "🌳", AssembleDifficulty.MEDIUM),
        WordItem("карета", listOf("ка", "ре", "та"), "🎠", AssembleDifficulty.MEDIUM),
        WordItem("бананы", listOf("ба", "на", "ны"), "🍌", AssembleDifficulty.MEDIUM),
        WordItem("помидор", listOf("по", "ми", "дор"), "🍅", AssembleDifficulty.MEDIUM),
        WordItem("огурец", listOf("о", "гу", "рец"), "🥒", AssembleDifficulty.MEDIUM),
        WordItem("телефон", listOf("те", "ле", "фон"), "📱", AssembleDifficulty.MEDIUM),
        
        // SUPER_HARD (4 слога)
        WordItem("черепаха", listOf("че", "ре", "па", "ха"), "🐢", AssembleDifficulty.SUPER_HARD),
        WordItem("кукуруза", listOf("ку", "ку", "ру", "за"), "🌽", AssembleDifficulty.SUPER_HARD),
        WordItem("гусеница", listOf("гу", "се", "ни", "ца"), "🐛", AssembleDifficulty.SUPER_HARD),
        WordItem("пирамида", listOf("пи", "ра", "ми", "да"), "🔺", AssembleDifficulty.SUPER_HARD),
        WordItem("велосипед", listOf("ве", "ло", "си", "пед"), "🚲", AssembleDifficulty.SUPER_HARD),
        WordItem("мотоцикл", listOf("мо", "то", "цикл"), "🏍️", AssembleDifficulty.SUPER_HARD),
        WordItem("самолет", listOf("са", "мо", "лет"), "✈️", AssembleDifficulty.SUPER_HARD)
    )

    private val firstStepVowels = setOf('а', 'о', 'у', 'ы', 'и')

    fun getWordsForTask(config: AssembleWordConfig, count: Int = 7): List<WordItem> {
        var filtered = allWords.filter { 
            when (config.difficulty) {
                AssembleDifficulty.EASY -> it.difficulty == AssembleDifficulty.EASY
                AssembleDifficulty.MEDIUM -> it.difficulty == AssembleDifficulty.MEDIUM
                AssembleDifficulty.HARD -> it.difficulty == AssembleDifficulty.EASY || it.difficulty == AssembleDifficulty.MEDIUM
                AssembleDifficulty.SUPER_HARD -> it.difficulty == AssembleDifficulty.SUPER_HARD
            }
        }

        if (config.firstStepVowelsOnly) {
            filtered = filtered.filter { word ->
                word.word.all { char -> char !in "яёюе" } // Упрощенная проверка
            }
        }

        return filtered.shuffled().take(count)
    }
}
