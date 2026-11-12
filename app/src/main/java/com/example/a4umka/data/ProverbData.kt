package com.example.a4umka.data

import kotlin.random.Random

object ProverbData {
    private val months = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
    private val days = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
    private val numbers10 = (1..10).map { it.toString() }
    private val numbers30 = (1..30).map { it.toString() }
    private val letters = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ".map { it.toString() }

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
        // Убрали .shuffled() отсюда, чтобы сохранить правильный порядок чисел
        val sourceItems = when (type) {
            ProverbSortType.NUMBERS -> if (config.age == ProverbAge.AGE_5_6) numbers10 else numbers30
            ProverbSortType.DAYS -> days
            ProverbSortType.MONTHS -> months
            ProverbSortType.LETTERS -> letters
        }

        val phrasePool = if (config.age == ProverbAge.AGE_5_6) proverbsShort else proverbsLong
        val phraseRaw = phrasePool.random()
        val syllables = phraseRaw.split("|")

        if (syllables.size > sourceItems.size) return null

        // 1. Берем случайные элементы (например, месяцы)
        val selectedItems = sourceItems.shuffled().take(syllables.size)

        // 2. Сортируем их ПРАВИЛЬНО (по индексу в изначальном списке, а не по алфавиту)
        val correctOrder = selectedItems.sortedBy { sourceItems.indexOf(it) }

        // 3. Привязываем слоги к ПРАВИЛЬНОМУ порядку (Январь = первый слог, Февраль = второй и т.д.)
        val cards = correctOrder.mapIndexed { index, item ->
            Pair(item, syllables[index].uppercase())
        }.shuffled()

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
