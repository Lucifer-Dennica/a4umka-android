package com.example.a4umka.data

import kotlin.random.Random

data class FairyTale(val title: String, val emojis: String)

data class FixNameTask(val brokenName: String, val correctName: String)
data class ConnectHalvesTask(val starts: List<String>, val ends: List<String>, val answers: List<Pair<String, String>>)

data class FairyTaleTask(
    val fixNames: List<FixNameTask> = emptyList(),
    val guessPictures: List<FairyTale> = emptyList(),
    val connectHalves: ConnectHalvesTask? = null
)

object FairyTaleData {
    private val easyTales = listOf(
        FairyTale("Репка", "👴👵👧🐶🐱🐭"),
        FairyTale("Колобок", "👴👵🥯🦊"),
        FairyTale("Теремок", "🏠🐭🐸🐰🦊🐺🐻"),
        FairyTale("Курочка Ряба", "🐔🥚🐭"),
        FairyTale("Красная Шапочка", "👧🔴🧺🐺"),
        FairyTale("Кот в сапогах", "🐱👢👑"),
        FairyTale("Маша и медведь", "👧🐻🏠"),
        FairyTale("Три медведя", "👧🐻🐻🐻🥣"),
        FairyTale("Гуси-лебеди", "🦢🦢👦👧🏠"),
        FairyTale("Заюшкина избушка", "🐰🦊🏠🧊")
    )

    private val hardTales = listOf(
        FairyTale("Снежная королева", "❄️👑🪞👦👧"),
        FairyTale("Дюймовочка", "🌸👧🐸🐭🐝"),
        FairyTale("Русалочка", "🧜‍♀️🌊👑"),
        FairyTale("Бременские музыканты", "🐴🐶🐱🐓🏰"),
        FairyTale("Дикие лебеди", "🦢🦢👧👑"),
        FairyTale("Сказка о рыбаке и рыбке", "👴🎣🐟👵"),
        FairyTale("Золушка", "👠🎃🐭👑"),
        FairyTale("Буратино", "🪵🔑🐢👃"),
        FairyTale("Карлик Нос", "🧙‍♂️👃👑"),
        FairyTale("Сказка о царе Салтане", "👑⚓🐿️🦢")
    )

    fun generateTask(config: FairyTaleConfig): FairyTaleTask {
        val tales = if (config.age == FairyTaleAge.AGE_5_6) easyTales else hardTales
        var currentTales = tales.shuffled()

        // 1. Исправь названия
        val fixNames = mutableListOf<FixNameTask>()
        if (config.taskTypes.contains(FairyTaleTaskType.FIX_NAMES)) {
            val count = if (config.age == FairyTaleAge.AGE_5_6) 4 else 6
            val selected = currentTales.take(count)
            val halves = selected.map { 
                val words = it.title.split(" ")
                if (words.size > 1) Pair(words.first(), words.drop(1).joinToString(" "))
                else Pair(words.first().take(3), words.first().drop(3))
            }
            
            // Перемешиваем вторые половины
            val shuffledSeconds = halves.map { it.second }.shuffled()
            
            for (i in selected.indices) {
                if (halves[i].second != shuffledSeconds[i]) {
                    fixNames.add(FixNameTask("${halves[i].first} ${shuffledSeconds[i]}", selected[i].title))
                } else {
                    fixNames.add(FixNameTask("${shuffledSeconds[(i+1)%count]} ${halves[i].second}", selected[i].title))
                }
            }
        }

        // 2. Угадай по картинкам
        val guessPictures = mutableListOf<FairyTale>()
        if (config.taskTypes.contains(FairyTaleTaskType.GUESS_PICTURES)) {
            guessPictures.addAll(currentTales.shuffled().take(4))
        }

        // 3. Соедини половинки
        var connectHalves: ConnectHalvesTask? = null
        if (config.taskTypes.contains(FairyTaleTaskType.CONNECT_HALVES)) {
            val count = if (config.age == FairyTaleAge.AGE_5_6) 3 else 4
            val selected = currentTales.shuffled().take(count)
            val pairs = selected.map { 
                val words = it.title.split(" ")
                if (words.size > 1) Pair(words.first(), words.drop(1).joinToString(" "))
                else Pair(words.first().take(3), words.first().drop(3))
            }
            connectHalves = ConnectHalvesTask(
                starts = pairs.map { it.first }.shuffled(),
                ends = pairs.map { it.second }.shuffled(),
                answers = pairs
            )
        }

        return FairyTaleTask(fixNames, guessPictures, connectHalves)
    }
}
