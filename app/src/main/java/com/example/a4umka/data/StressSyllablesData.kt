package com.example.a4umka.data

import kotlin.random.Random

/**
 * База слов для «Ударение и слоги».
 * ~50 слов от 1 до 4 слогов с разметкой слогов и ударения.
 */
object StressSyllablesData {

    val allWords = listOf(
        // === 1 слог ===
        StressWord("ГРИБ", "🍄", listOf("ГРИБ"), 0),
        StressWord("ДОМ", "🏠", listOf("ДОМ"), 0),
        StressWord("КОТ", "🐱", listOf("КОТ"), 0),
        StressWord("СЫР", "🧀", listOf("СЫР"), 0),
        StressWord("ЛУК", "🧅", listOf("ЛУК"), 0),
        StressWord("СОН", "😴", listOf("СОН"), 0),
        StressWord("ШАР", "🎈", listOf("ШАР"), 0),
        StressWord("МЕД", "🍯", listOf("МЕД"), 0),
        StressWord("СЛОН", "🐘", listOf("СЛОН"), 0),
        StressWord("ВОЛК", "🐺", listOf("ВОЛК"), 0),

        // === 2 слога ===
        StressWord("ЛУЖА", "💧", listOf("ЛУ", "ЖА"), 0),
        StressWord("ЗАЯЦ", "🐰", listOf("ЗА", "ЯЦ"), 0),
        StressWord("ВАЗА", "🏺", listOf("ВА", "ЗА"), 0),
        StressWord("РЫБА", "🐟", listOf("РЫ", "БА"), 0),
        StressWord("ЛИСА", "🦊", listOf("ЛИ", "СА"), 0),
        StressWord("ЛУНА", "🌙", listOf("ЛУ", "НА"), 0),
        StressWord("РОЗА", "🌹", listOf("РО", "ЗА"), 0),
        StressWord("ВОДА", "💧", listOf("ВО", "ДА"), 1),
        StressWord("ЗИМА", "❄️", listOf("ЗИ", "МА"), 1),
        StressWord("КАША", "🥣", listOf("КА", "ША"), 0),
        StressWord("ПЕСНЯ", "🎵", listOf("ПЕС", "НЯ"), 0),
        StressWord("КОШКА", "🐱", listOf("КОШ", "КА"), 0),
        StressWord("ЛОЖКА", "🥄", listOf("ЛОЖ", "КА"), 0),
        StressWord("МЯЧИК", "⚽", listOf("МЯ", "ЧИК"), 0),
        StressWord("ВЕДРО", "🪣", listOf("ВЕ", "ДРО"), 1),
        StressWord("ОСЕНЬ", "🍂", listOf("О", "СЕНЬ"), 1),
        StressWord("ДЕРЕВО", "🌳", listOf("ДЕ", "РЕ", "ВО"), 1),

        // === 3 слога ===
        StressWord("СОБАКА", "🐶", listOf("СО", "БА", "КА"), 1),
        StressWord("КАПУСТА", "🥬", listOf("КА", "ПУС", "ТА"), 1),
        StressWord("МАЛИНА", "🫐", listOf("МА", "ЛИ", "НА"), 1),
        StressWord("МАШИНА", "🚗", listOf("МА", "ШИ", "НА"), 1),
        StressWord("МОЛОКО", "🥛", listOf("МО", "ЛО", "КО"), 2),
        StressWord("БАБОЧКА", "🦋", listOf("БА", "БОЧ", "КА"), 0),
        StressWord("ЯГОДА", "🍓", listOf("Я", "ГО", "ДА"), 1),
        StressWord("КУРИЦА", "🐔", listOf("КУ", "РИ", "ЦА"), 1),
        StressWord("ПОГОДА", "🌤️", listOf("ПО", "ГО", "ДА"), 0),
        StressWord("РАБОТА", "💼", listOf("РА", "БО", "ТА"), 1),
        StressWord("ЗАМОК", "🏰", listOf("ЗА", "МОК"), 1),
        StressWord("КАРАНДАШ", "✏️", listOf("КА", "РАН", "ДАШ"), 1),
        StressWord("БОТИНКИ", "👢", listOf("БО", "ТИН", "КИ"), 0),
        StressWord("САПОГИ", "🥾", listOf("СА", "ПО", "ГИ"), 1),
        StressWord("ВАРЕЖКИ", "🧤", listOf("ВА", "РЕЖ", "КИ"), 1),

        // === 4 слога ===
        StressWord("РАДУГА", "🌈", listOf("РА", "ДУ", "ГА"), 1),
        StressWord("ПОМИДОР", "🍅", listOf("ПО", "МИ", "ДОР"), 2),
        StressWord("ЧЕРЕПАХА", "🐢", listOf("ЧЕ", "РЕ", "ПА", "ХА"), 2),
        StressWord("КУКУРУЗА", "🌽", listOf("КУ", "КУ", "РУ", "ЗА"), 3),
        StressWord("ПАЛЬТО", "🧥", listOf("ПАЛЬ", "ТО"), 1),
        StressWord("САМОЛЁТ", "✈️", listOf("СА", "МО", "ЛЁТ"), 2),
        StressWord("ПАРОВОЗ", "🚂", listOf("ПА", "РО", "ВОЗ"), 2),
        StressWord("ЯБЛОКО", "🍎", listOf("Я", "БЛО", "КО"), 1),
        StressWord("КОРОВА", "🐄", listOf("КО", "РО", "ВА"), 0),
        StressWord("ВОРОНА", "🐦‍⬛", listOf("ВО", "РО", "НА"), 1),
        StressWord("СОРОКА", "🐦", listOf("СО", "РО", "КА"), 1),
        StressWord("ДОРОГА", "🛣️", listOf("ДО", "РО", "ГА"), 1),
        StressWord("МОРОЗ", "🥶", listOf("МО", "РОЗ"), 0)
    )

    fun getRandomWords(count: Int, seed: Long): List<StressWord> {
        val random = Random(seed)
        return allWords.shuffled(random).take(minOf(count, allWords.size))
    }
}
