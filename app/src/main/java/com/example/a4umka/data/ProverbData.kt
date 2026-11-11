package com.example.a4umka.data

import kotlin.random.Random

object ProverbData {
    private val months = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
    private val days = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
    private val numbers10 = (1..10).map { it.toString() }
    private val numbers30 = (1..30).map { it.toString() }
    private val letters = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".map { it.toString() }

    // Фразы разбиты на слоги (для 5-6 лет короткие, для 6-7 длинные)
    private val proverbsShort = listOf(
        "де|лу вре|мя, по|те|хе час",
        "ти|ше е|дешь — даль|ше бу|дешь",
        "без тру|да не вы|нешь и рыб|ку из пру|да",
        "у|тро ве|че|ра муд|ре|не|е",
        "семь раз от|мерь — один раз от|режь"
    )
    
    private val proverbsLong = listOf(
        "не и|мей сто руб|лей, а и|мей сто дру|зей",
        "ста|рый друг луч|ше но|вых двух",
        "де|ло ма|сте|ра бо|ит|ся",
        "по|спе|шишь — лю|дей на|сме|шишь",
        "тер|пе|ни|е и труд всё пе|ре|трут",
        "один в по|ле не во|ин"
    )

    fun generateTask(config: ProverbConfig): ProverbTask? {
        if (config.sortTypes.isEmpty()) return null

        val type = config.sortTypes.random()
        val sourceItems = when (type) {
            ProverbSortType.NUMBERS -> if (config.age == ProverbAge.AGE_5_6) numbers10 else numbers30.shuffled()
            ProverbSortType.DAYS -> days
            ProverbSortType.MONTHS -> months
            ProverbSortType.LETTERS -> letters
        }

        // Выбираем фразу
        val phrasePool = if (config.age == ProverbAge.AGE_5_6) proverbsShort else proverbsLong
        val phraseRaw = phrasePool.random()
        
        val syllables = phraseRaw.split("|")
        
        // Если слогов больше, чем предметов, отбрасываем эту фразу (или берем другую)
        if (syllables.size > sourceItems.size) return null

        // Берем нужное количество предметов и перемешиваем их для карточек
        val selectedItems = sourceItems.shuffled().take(syllables.size).sorted()
        val shuffledItems = selectedItems.shuffled()

        // Сопоставляем предметы и слоги в правильном порядке
        val correctOrder = selectedItems // Они уже отсортированы
        val cards = selectedItems.mapIndexed { index, item ->
            Pair(item, syllables[index].uppercase())
        }.shuffled() // Перемешиваем для вывода на экран

        // Считаем пробелы (по исходной строке)
        val wordBreaks = mutableListOf<Int>()
        var currentIndex = 0
        for (word in phraseRaw.split(" ")) {
            val syllableCount = word.count { it == '|' } + 1
            currentIndex += syllableCount
            wordBreaks.add(currentIndex)
        }

        val instruction = when (type) {
            ProverbSortType.NUMBERS -> "Расставь числа по порядку — от меньшего к большему"
            ProverbSortType.DAYS -> "Расставь дни недели по порядку — с понедельника по воскресенье"
            ProverbSortType.MONTHS -> "Расставь месяцы по порядку — с января по декабрь"
            ProverbSortType.LETTERS -> "Расставь буквы по алфавиту"
        }

        val finalPhrase = phraseRaw.replace("|", "")

        return ProverbTask(instruction, cards, correctOrder, finalPhrase, wordBreaks)
    }
}
