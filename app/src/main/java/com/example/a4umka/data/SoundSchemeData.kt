package com.example.a4umka.data

import kotlin.random.Random

enum class SoundType { VOWEL, HARD, SOFT }

data class SoundWord(
    val word: String,
    val emoji: String,
    val sounds: List<SoundType>
)

object SoundSchemeData {
    // База слов подобрана так, чтобы буквы совпадали со звуками
    // (нет ь, ъ, я, е, ё, ю, й)
    
    private val words3 = listOf(
        SoundWord("РАК", "🦞", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("БЫК", "🐂", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("СЫР", "🧀", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("КИТ", "🐋", listOf(SoundType.SOFT, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("ДОМ", "🏠", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("СУП", "🥣", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("ШАР", "🎈", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("КОТ", "🐱", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("ЛЕС", "🌲", listOf(SoundType.SOFT, SoundType.VOWEL, SoundType.HARD))
    )

    private val words4 = listOf(
        SoundWord("ЛУНА", "🌙", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL)),
        SoundWord("ЛИСА", "🦊", listOf(SoundType.SOFT, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL)),
        SoundWord("КРОТ", "🦫", listOf(SoundType.HARD, SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("ЗУБЫ", "🦷", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL)),
        SoundWord("ВОЛК", "🐺", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD, SoundType.HARD)),
        SoundWord("СЛОН", "🐘", listOf(SoundType.HARD, SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("ЗИМА", "❄️", listOf(SoundType.SOFT, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL)),
        SoundWord("РОЗА", "🌹", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL))
    )

    private val words5 = listOf(
        SoundWord("ЗАБОР", "🚧", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("БАНАН", "🍌", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("ТРАВА", "🌿", listOf(SoundType.HARD, SoundType.HARD, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL)),
        SoundWord("КОШКА", "🐈", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.HARD, SoundType.HARD, SoundType.VOWEL)),
        SoundWord("ПЛИТА", "🍳", listOf(SoundType.HARD, SoundType.SOFT, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL)),
        SoundWord("ЛИМОН", "🍋", listOf(SoundType.SOFT, SoundType.VOWEL, SoundType.HARD, SoundType.VOWEL, SoundType.HARD)),
        SoundWord("РУЧКА", "🖊️", listOf(SoundType.HARD, SoundType.VOWEL, SoundType.SOFT, SoundType.HARD, SoundType.VOWEL))
    )

    fun getWords(config: SoundSchemeConfig, count: Int = 6): List<SoundWord> {
        val source = when (config.length) {
            SoundSchemeLength.THREE -> words3
            SoundSchemeLength.FOUR -> words4
            SoundSchemeLength.FIVE -> words5
            SoundSchemeLength.MIX -> words3 + words4 + words5
        }
        return source.shuffled().take(count)
    }
}
