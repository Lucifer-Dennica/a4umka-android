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
        // ИСПРАВЛЕНО: все ударения проверены по правилам русского языка
        StressWord("ЛУЖА", "💧", listOf("ЛУ", "ЖА"), 0),          // лу́жа
        StressWord("ЗАЯЦ", "🐰", listOf("ЗА", "ЯЦ"), 0),          // за́яц
        StressWord("ВАЗА", "🏺", listOf("ВА", "ЗА"), 0),           // ва́за
        StressWord("РЫБА", "🐟", listOf("РЫ", "БА"), 0),          // ры́ба
        StressWord("ЛИСА", "🦊", listOf("ЛИ", "СА"), 0),          // ли́са — ударение на И
        StressWord("ЛУНА", "🌙", listOf("ЛУ", "НА"), 0),          // лу́на
        StressWord("РОЗА", "🌹", listOf("РО", "ЗА"), 0),           // ро́за
        StressWord("ВОДА", "💧", listOf("ВО", "ДА"), 1),          // вода́
        StressWord("ЗИМА", "❄️", listOf("ЗИ", "МА"), 1),          // зима́
        StressWord("КАША", "🥣", listOf("КА", "ША"), 0),           // ка́ша
        StressWord("ПЕСНЯ", "🎵", listOf("ПЕС", "НЯ"), 0),        // пе́сня
        StressWord("КОШКА", "🐱", listOf("КОШ", "КА"), 0),        // ко́шка
        StressWord("ЛОЖКА", "🥄", listOf("ЛОЖ", "КА"), 0),         // ло́жка
        StressWord("МЯЧИК", "⚽", listOf("МЯ", "ЧИК"), 0),         // мя́чик
        StressWord("ВЕДРО", "🪣", listOf("ВЕ", "ДРО"), 1),        // ведро́
        StressWord("ОСЕНЬ", "🍂", listOf("О", "СЕНЬ"), 1),        // осе́нь
        StressWord("ДЕРЕВО", "🌳", listOf("ДЕ", "РЕ", "ВО"), 1),  // дере́во

        // === 3 слога ===
        StressWord("СОБАКА", "🐶", listOf("СО", "БА", "КА"), 1),  // соба́ка
        StressWord("КАПУСТА", "🥬", listOf("КА", "ПУС", "ТА"), 1), // капу́ста
        StressWord("МАЛИНА", "🫐", listOf("МА", "ЛИ", "НА"), 1),   // мали́на
        StressWord("МАШИНА", "🚗", listOf("МА", "ШИ", "НА"), 1),  // маши́на
        StressWord("МОЛОКО", "🥛", listOf("МО", "ЛО", "КО"), 2),   // молоко́
        StressWord("БАБОЧКА", "🦋", listOf("БА", "БОЧ", "КА"), 0), // ба́бочка
        StressWord("ЯГОДА", "🍓", listOf("Я", "ГО", "ДА"), 1),   // я́года
        StressWord("КУРИЦА", "🐔", listOf("КУ", "РИ", "ЦА"), 1), // ку́рица
        StressWord("ПОГОДА", "🌤️", listOf("ПО", "ГО", "ДА"), 0), // по́года
        StressWord("РАБОТА", "💼", listOf("РА", "БО", "ТА"), 1), // рабо́та
        StressWord("ЗАМОК", "🏰", listOf("ЗА", "МОК"), 0),       // за́мок (дворец)
        StressWord("КАРАНДАШ", "✏️", listOf("КА", "РАН", "ДАШ"), 1), // каранда́ш
        StressWord("БОТИНКИ", "👢", listOf("БО", "ТИН", "КИ"), 0), // бо́тинки
        StressWord("САПОГИ", "🥾", listOf("СА", "ПО", "ГИ"), 2),  // сапоги́
        StressWord("ВАРЕЖКИ", "🧤", listOf("ВА", "РЕЖ", "КИ"), 1), // варе́жки

        // === 4 слога ===
        StressWord("РАДУГА", "🌈", listOf("РА", "ДУ", "ГА"), 0),  // ра́дуга (3 слога, ударение на 1)
        StressWord("ПОМИДОР", "🍅", listOf("ПО", "МИ", "ДОР"), 2), // помидо́р
        StressWord("ЧЕРЕПАХА", "🐢", listOf("ЧЕ", "РЕ", "ПА", "ХА"), 2), // черепа́ха
        StressWord("КУКУРУЗА", "🌽", listOf("КУ", "КУ", "РУ", "ЗА"), 3), // кукуруза́
        StressWord("ПАЛЬТО", "🧥", listOf("ПАЛЬ", "ТО"), 1),      // пальто́
        StressWord("САМОЛЁТ", "✈️", listOf("СА", "МО", "ЛЁТ"), 2), // самолёт
        StressWord("ПАРОВОЗ", "🚂", listOf("ПА", "РО", "ВОЗ"), 0), // па́ровоз
        StressWord("ЯБЛОКО", "🍎", listOf("Я", "БЛО", "КО"), 0),  // я́блоко
        StressWord("КОРОВА", "🐄", listOf("КО", "РО", "ВА"), 0),  // ко́рова
        StressWord("ВОРОНА", "🐦‍⬛", listOf("ВО", "РО", "НА"), 0), // во́рона
        StressWord("СОРОКА", "🐦", listOf("СО", "РО", "КА"), 0),  // со́рока
        StressWord("ДОРОГА", "🛣️", listOf("ДО", "РО", "ГА"), 0),  // до́рога
        StressWord("МОРОЗ", "🥶", listOf("МО", "РОЗ"), 0)         // мо́роз
    )

    fun getRandomWords(count: Int, seed: Long): List<StressWord> {
        val random = Random(seed)
        return allWords.shuffled(random).take(minOf(count, allWords.size))
    }
}
