package com.example.a4umka.data

// Класс для слов: само слово, эмодзи, индекс выделяемой буквы, позиция звука (0-начало, 1-середина, 2-конец)
data class SoundWord(val word: String, val emoji: String, val highlightIndex: Int, val position: Int)

data class SoundData(
    val syllables: List<Pair<String, Int>>, // Строка и индекс для выделения
    val wordsStart: List<SoundWord>,
    val wordsMiddle: List<SoundWord>,
    val wordsEnd: List<SoundWord>,
    val phrases: List<Pair<String, Int>>, // Фраза и индекс начала выделения
    val whereIsSound: List<SoundWord>
)

data class PairWord(val word: String, val emoji: String, val correctSound: String)

data class PairData(
    val syllables: List<String>,
    val words: List<PairWord>
)

object SoundAutomationData {
    private val rData = SoundData(
        syllables = listOf(
            Pair("РА", 0), Pair("РО", 0), Pair("РУ", 0), Pair("РЫ", 0),
            Pair("АР", 1), Pair("ОР", 1), Pair("УР", 1), Pair("ЫР", 1),
            Pair("АРА", 1), Pair("ОРО", 1), Pair("УРУ", 1), Pair("ЫРЫ", 1),
            Pair("ТРА", 1), Pair("КРО", 1), Pair("БРУ", 1), Pair("ВРЫ", 1),
            Pair("РА-РО-РУ", 0), Pair("РО-РУ-РЫ", 0)
        ),
        wordsStart = listOf(
            SoundWord("РАК", "🦞", 0, 0), SoundWord("РУЧКА", "🖊️", 0, 0), SoundWord("РУКА", "✋", 0, 0), SoundWord("РАКУШКА", "🐚", 0, 0)
        ),
        wordsMiddle = listOf(
            SoundWord("ЗЕБРА", "🦓", 2, 1), SoundWord("ШОРТЫ", "🩳", 2, 1), SoundWord("ШАРФ", "🧣", 2, 1), SoundWord("ГРУША", "🍐", 1, 1)
        ),
        wordsEnd = listOf(
            SoundWord("ПОВАР", "👨‍🍳", 4, 2), SoundWord("ТОПОР", "🪓", 4, 2), SoundWord("ТИГР", "🐯", 3, 2), SoundWord("СЫР", "🧀", 2, 2)
        ),
        phrases = listOf(
            Pair("Ор-ор-ор — вот высокий забор", 0),
            Pair("Ур-ур-ур — у бабушки много кур", 0),
            Pair("Ар-ар-ар — закипает самовар", 0),
            Pair("Ыр-ыр-ыр — это вкусный сыр", 0),
            Pair("Ру-ру-ру — мы играем в кенгуру", 0),
            Pair("Жираф высокий", 0),
            Pair("У Тамары красный шар", 8),
            Pair("Вот вкусный торт", 8),
            Pair("Корова ест траву", 4),
            Pair("Тигр рычит: р-р-р!", 0),
            Pair("У Романа ракета", 2)
        ),
        whereIsSound = listOf(
            SoundWord("ПОМИДОР", "🍅", 6, 2), SoundWord("КОМПЬЮТЕР", "💻", 7, 2), SoundWord("ДРАКОН", "🐉", 1, 1), SoundWord("РАДУГА", "🌈", 0, 0),
            SoundWord("БОБР", "🦫", 3, 2), SoundWord("КОРОВА", "🐄", 2, 1), SoundWord("РОЗА", "🌹", 0, 0), SoundWord("РАК", "🦞", 0, 0),
            SoundWord("РУЧКА", "🖊️", 0, 0), SoundWord("СВЕТОФОР", "🚦", 6, 2), SoundWord("КРАБ", "🦀", 1, 1), SoundWord("КОРОНА", "👑", 2, 1)
        )
    )

    private val lData = SoundData(
        syllables = listOf(
            Pair("ЛА", 0), Pair("ЛО", 0), Pair("ЛУ", 0), Pair("ЛЫ", 0),
            Pair("АЛ", 1), Pair("ОЛ", 1), Pair("УЛ", 1), Pair("ЫЛ", 1),
            Pair("АЛА", 1), Pair("ОЛО", 1), Pair("УЛУ", 1), Pair("ЫЛЫ", 1),
            Pair("КЛА", 1), Pair("СЛО", 1), Pair("ПЛУ", 1), Pair("БЛЫ", 1),
            Pair("ЛА-ЛО-ЛУ", 0), Pair("ЛО-ЛУ-ЛЫ", 0)
        ),
        wordsStart = listOf(
            SoundWord("ЛУК", "🧅", 0, 0), SoundWord("ЛАМПА", "💡", 0, 0), SoundWord("ЛОЖКА", "🥄", 0, 0), SoundWord("ЛОДКА", "🛶", 0, 0)
        ),
        wordsMiddle = listOf(
            SoundWord("ПИЛА", "🪚", 2, 1), SoundWord("ЮЛА", "🪀", 1, 1), SoundWord("КУКЛА", "🪆", 2, 1), SoundWord("ХАЛАТ", "🥼", 2, 1)
        ),
        wordsEnd = listOf(
            SoundWord("СТОЛ", "🪵", 3, 2), SoundWord("СТУЛ", "🪑", 3, 2), SoundWord("ФУТБОЛ", "⚽", 5, 2), SoundWord("ОСЁЛ", "🫏", 3, 2)
        ),
        phrases = listOf(
            Pair("Ла-ла-ла — в комнате юла", 0),
            Pair("Лу-лу-лу — я сижу на стуле", 0),
            Pair("Ло-ло-ло — на улице тепло", 0),
            Pair("Лы-лы-лы — я мою полы", 0),
            Pair("Мила мыла пол", 0),
            Pair("Клава пила сок", 1)
        ),
        whereIsSound = listOf(
            SoundWord("ЛОПАТА", "🪏", 0, 0), SoundWord("ЮЛА", "🪀", 1, 1), SoundWord("СТОЛ", "🪵", 3, 2), SoundWord("ПАЛАТКА", "⛺", 3, 1),
            SoundWord("ЛУНА", "🌙", 0, 0), SoundWord("КУКЛА", "🪆", 2, 1), SoundWord("ДЯТЕЛ", "🐦", 3, 2), SoundWord("ЛЫЖИ", "🎿", 0, 0),
            SoundWord("МОЛОКО", "🥛", 2, 1), SoundWord("ЛОШАДЬ", "🐎", 0, 0), SoundWord("ОСЁЛ", "🫏", 3, 2), SoundWord("БУЛКА", "🥖", 2, 1)
        )
    )

    private val sData = SoundData(
        syllables = listOf(
            Pair("СА", 0), Pair("СО", 0), Pair("СУ", 0), Pair("СЫ", 0),
            Pair("АС", 1), Pair("ОС", 1), Pair("УС", 1), Pair("ЫС", 1),
            Pair("АСА", 1), Pair("ОСО", 1), Pair("УСУ", 1), Pair("ЫСЫ", 1),
            Pair("СПА", 0), Pair("СТО", 0), Pair("СКУ", 0), Pair("СВЫ", 0),
            Pair("СА-СО-СУ", 0), Pair("СО-СУ-СЫ", 0)
        ),
        wordsStart = listOf(
            SoundWord("СОБАКА", "🐕", 0, 0), SoundWord("СЛОН", "🐘", 0, 0), SoundWord("СЫР", "🧀", 0, 0), SoundWord("САМОЛЁТ", "✈️", 0, 0)
        ),
        wordsMiddle = listOf(
            SoundWord("ЛИСА", "🦊", 2, 1), SoundWord("ОСА", "🐝", 1, 1), SoundWord("КОСА", "🌾", 2, 1), SoundWord("ВЕСЫ", "⚖️", 2, 1)
        ),
        wordsEnd = listOf(
            SoundWord("НОС", "👃", 2, 2), SoundWord("ЛЕС", "🌲", 2, 2), SoundWord("ПЁС", "🐕", 2, 2), SoundWord("АВТОБУС", "🚌", 6, 2)
        ),
        phrases = listOf(
            Pair("Са-са-са — в лесу бегает лиса", 0),
            Pair("Со-со-со — у Вовы колесо", 0),
            Pair("Су-су-су — мы видели осу", 0),
            Pair("Сы-сы-сы — у папы есть усы", 0),
            Pair("У Сони новая сумка", 2),
            Pair("В саду стоит стол", 2)
        ),
        whereIsSound = listOf(
            SoundWord("САМОЛЁТ", "✈️", 0, 0), SoundWord("ЛИСА", "🦊", 2, 1), SoundWord("АНАНАС", "🍍", 5, 2), SoundWord("ВЕСЫ", "⚖️", 2, 1),
            SoundWord("СЛОН", "🐘", 0, 0), SoundWord("ОСА", "🐝", 1, 1), SoundWord("ПЫЛЕСОС", "🧹", 7, 2), SoundWord("СЫР", "🧀", 0, 0),
            SoundWord("КОСА", "🌾", 2, 1), SoundWord("СОВА", "🦉", 0, 0), SoundWord("НОС", "👃", 2, 2), SoundWord("БУСЫ", "📿", 2, 1)
        )
    )

    private val zData = SoundData(
        syllables = listOf(
            Pair("ЗА", 0), Pair("ЗО", 0), Pair("ЗУ", 0), Pair("ЗЫ", 0),
            Pair("АЗ", 1), Pair("ОЗ", 1), Pair("УЗ", 1), Pair("ЫЗ", 1),
            Pair("АЗА", 1), Pair("ОЗО", 1), Pair("УЗУ", 1), Pair("ЫЗЫ", 1),
            Pair("ЗВА", 0), Pair("ЗДО", 0), Pair("ЗМУ", 0), Pair("ЗБЫ", 0),
            Pair("ЗА-ЗО-ЗУ", 0), Pair("ЗО-ЗУ-ЗЫ", 0)
        ),
        wordsStart = listOf(
            SoundWord("ЗАМОК", "🏰", 0, 0), SoundWord("ЗАЯЦ", "🐇", 0, 0), SoundWord("ЗУБЫ", "🦷", 0, 0), SoundWord("ЗОНТ", "☂️", 0, 0)
        ),
        wordsMiddle = listOf(
            SoundWord("РОЗА", "🌹", 2, 1), SoundWord("КОЗА", "🐐", 2, 1), SoundWord("ВАЗА", "🏺", 2, 1), SoundWord("ГРОЗА", "⛈️", 2, 1)
        ),
        wordsEnd = listOf(
            SoundWord("ТАЗ", "🥣", 2, 2), SoundWord("ГЛАЗ", "👁️", 3, 2), SoundWord("АРБУЗ", "🍉", 4, 2), SoundWord("ПАРОВОЗ", "🚂", 6, 2)
        ),
        phrases = listOf(
            Pair("За-за-за — у нас идёт коза", 0),
            Pair("Зу-зу-зу — мы поймали стрекозу", 0),
            Pair("Зы-зы-зы — не боимся мы грозы", 0),
            Pair("У Зои новый зонт", 2),
            Pair("В вазе стоят розы", 7)
        ),
        whereIsSound = listOf(
            SoundWord("ЗАМОК", "🏰", 0, 0), SoundWord("КОЗА", "🐐", 2, 1), SoundWord("АРБУЗ", "🍉", 4, 2), SoundWord("РОЗА", "🌹", 2, 1),
            SoundWord("ЗЕБРА", "🦓", 0, 0), SoundWord("ВАЗА", "🏺", 2, 1), SoundWord("ПАРОВОЗ", "🚂", 6, 2), SoundWord("ЗОНТ", "☂️", 0, 0),
            SoundWord("БИЗОН", "🦬", 2, 1), SoundWord("ЗАЯЦ", "🐇", 0, 0), SoundWord("ГЛАЗ", "👁️", 3, 2), SoundWord("ГРОЗА", "⛈️", 2, 1)
        )
    )

    private val shData = SoundData(
        syllables = listOf(
            Pair("ША", 0), Pair("ШО", 0), Pair("ШУ", 0), Pair("ШИ", 0),
            Pair("АШ", 1), Pair("ОШ", 1), Pair("УШ", 1), Pair("ЫШ", 1),
            Pair("АША", 1), Pair("ОШО", 1), Pair("УШУ", 1), Pair("ЫШИ", 1),
            Pair("ШПА", 0), Pair("ШТО", 0), Pair("ШКУ", 0), Pair("ШВЫ", 0),
            Pair("ША-ШО-ШУ", 0), Pair("ШО-ШУ-ШИ", 0)
        ),
        wordsStart = listOf(
            SoundWord("ШАР", "🎈", 0, 0), SoundWord("ШАПКА", "🧢", 0, 0), SoundWord("ШУБА", "🧥", 0, 0), SoundWord("ШКОЛА", "🏫", 0, 0)
        ),
        wordsMiddle = listOf(
            SoundWord("МАШИНА", "🚗", 2, 1), SoundWord("МЫШКА", "🐭", 2, 1), SoundWord("КОШКА", "🐈", 2, 1), SoundWord("ЛОШАДЬ", "🐎", 2, 1)
        ),
        wordsEnd = listOf(
            SoundWord("ДУШ", "🚿", 2, 2), SoundWord("МЫШЬ", "🐭", 3, 2), SoundWord("КАМЫШ", "🌾", 4, 2), SoundWord("ЛАНДЫШ", "🌼", 5, 2)
        ),
        phrases = listOf(
            Pair("Ша-ша-ша — мама моет малыша", 0),
            Pair("Шу-шу-шу — я письмо пишу", 0),
            Pair("Шо-шо-шо — летом хорошо", 0),
            Pair("Ши-ши-ши — на поляне малыши", 0),
            Pair("У Маши новая шуба", 4)
        ),
        whereIsSound = listOf(
            SoundWord("ШАР", "🎈", 0, 0), SoundWord("МАШИНА", "🚗", 2, 1), SoundWord("ЛАНДЫШ", "🌼", 5, 2), SoundWord("МЫШКА", "🐭", 2, 1),
            SoundWord("ШАПКА", "🧢", 0, 0), SoundWord("КОШКА", "🐈", 2, 1), SoundWord("МАЛЫШ", "👶", 4, 2), SoundWord("ШУБА", "🧥", 0, 0),
            SoundWord("ВИШНЯ", "🍒", 2, 1), SoundWord("ШКОЛА", "🏫", 0, 0), SoundWord("ФИНИШ", "🏁", 4, 2), SoundWord("ПОДУШКА", "🛏️", 4, 1)
        )
    )

    private val zhData = SoundData(
        syllables = listOf(
            Pair("ЖА", 0), Pair("ЖО", 0), Pair("ЖУ", 0), Pair("ЖИ", 0),
            Pair("АЖ", 1), Pair("ОЖ", 1), Pair("УЖ", 1), Pair("ИЖ", 1),
            Pair("АЖА", 1), Pair("ОЖО", 1), Pair("УЖУ", 1), Pair("ИЖИ", 1),
            Pair("ЖБА", 0), Pair("ЖДО", 0), Pair("ЖМУ", 0), Pair("ЖВЫ", 0),
            Pair("ЖА-ЖО-ЖУ", 0), Pair("ЖО-ЖУ-ЖИ", 0)
        ),
        wordsStart = listOf(
            SoundWord("ЖУК", "🪲", 0, 0), SoundWord("ЖАБА", "🐸", 0, 0), SoundWord("ЖИРАФ", "🦒", 0, 0), SoundWord("ЖЁЛУДЬ", "🌰", 0, 0)
        ),
        wordsMiddle = listOf(
            SoundWord("ЛУЖА", "💧", 2, 1), SoundWord("НОЖИ", "🔪", 2, 1), SoundWord("ЕЖИ", "🦔", 1, 1), SoundWord("ОДЕЖДА", "👗", 2, 1)
        ),
        wordsEnd = listOf(
            SoundWord("НОЖ", "🔪", 2, 2), SoundWord("ЁЖ", "🦔", 1, 2), SoundWord("ЧИЖ", "🐦", 2, 2), SoundWord("БАГАЖ", "🧳", 4, 2)
        ),
        phrases = listOf(
            Pair("Жа-жа-жа — есть иголки у ежа", 0),
            Pair("Жу-жу-жу — молоко дадим ежу", 0),
            Pair("Жи-жи-жи — в лесу бегают ежи", 0),
            Pair("У Жени новый журнал", 2)
        ),
        whereIsSound = listOf(
            SoundWord("ЖУК", "🪲", 0, 0), SoundWord("ЛУЖА", "💧", 2, 1), SoundWord("НОЖ", "🔪", 2, 2), SoundWord("ЕЖИ", "🦔", 1, 1),
            SoundWord("ЖАБА", "🐸", 0, 0), SoundWord("НОЖИ", "🔪", 2, 1), SoundWord("ЁЖ", "🦔", 1, 2), SoundWord("ЖИРАФ", "🦒", 0, 0),
            SoundWord("ОДЕЖДА", "👗", 2, 1), SoundWord("ЖУРНАЛ", "📖", 0, 0), SoundWord("БАГАЖ", "🧳", 4, 2), SoundWord("ПИЖАМА", "🩳", 2, 1)
        )
    )

    private val pairsData = mapOf(
        SoundPair.S_SH to PairData(
            syllables = listOf("СА — ША", "СО — ШО", "СУ — ШУ", "СЫ — ШИ", "АС — АШ", "ОС — ОШ", "УС — УШ", "АСА — АША"),
            words = listOf(
                PairWord("ГРУША", "🍐", "Ш"), PairWord("КОШКА", "🐈", "Ш"), PairWord("КОМПАС", "🧭", "С"), PairWord("ПОДСОЛНУХ", "🌻", "С"),
                PairWord("БАБУШКА", "👵", "Ш"), PairWord("СОК", "🧃", "С"), PairWord("РОМАШКА", "🌼", "Ш"), PairWord("АНАНАС", "🍍", "С"),
                PairWord("МОСТ", "🌉", "С"), PairWord("ШАРФ", "🧣", "Ш"), PairWord("ШАХМАТЫ", "♟️", "Ш"), PairWord("СКРИПКА", "🎻", "С")
            )
        ),
        SoundPair.R_L to PairData(
            syllables = listOf("РА — ЛА", "РО — ЛО", "РУ — ЛУ", "РЫ — ЛЫ", "АР — АЛ", "ОР — ОЛ", "УР — УЛ", "АРА — АЛА"),
            words = listOf(
                PairWord("ЖИРАФ", "🦒", "Р"), PairWord("ЛОДКА", "🛶", "Л"), PairWord("ЛАМА", "🦙", "Л"), PairWord("ЁЛКА", "🎄", "Л"),
                PairWord("ОБЛАКО", "☁️", "Л"), PairWord("БАКЛАЖАН", "🍆", "Л"), PairWord("РАК", "🦞", "Р"), PairWord("СЫР", "🧀", "Р"),
                PairWord("РУКА", "✋", "Р"), PairWord("ЛЫЖИ", "🎿", "Л"), PairWord("КОРОНА", "👑", "Р"), PairWord("ПИРОЖНОЕ", "🍰", "Р")
            )
        ),
        SoundPair.Z_ZH to PairData(
            syllables = listOf("ЗА — ЖА", "ЗО — ЖО", "ЗУ — ЖУ", "ЗЫ — ЖИ", "АЗА — АЖА", "ОЗО — ОЖО", "УЗУ — УЖУ", "АЗА — АЖА"),
            words = listOf(
                PairWord("ЛЫЖИ", "🎿", "Ж"), PairWord("НОЖНИЦЫ", "✂️", "Ж"), PairWord("ЖОНГЛЁР", "🤹", "Ж"), PairWord("РОЗА", "🌹", "З"),
                PairWord("ПАЗЛ", "🧩", "З"), PairWord("БАКЛАЖАН", "🍆", "Ж"), PairWord("НОЖИК", "🔪", "Ж"), PairWord("ГРУЗОВИК", "🚚", "З"),
                PairWord("ВАЗА", "🏺", "З"), PairWord("ЗУБ", "🦷", "З"), PairWord("ЗАЯЦ", "🐇", "З"), PairWord("ЖИЛЕТ", "🦺", "Ж")
            )
        )
    )

    fun getData(sound: SpeechSound): SoundData {
        return when (sound) {
            SpeechSound.R -> rData
            SpeechSound.L -> lData
            SpeechSound.S -> sData
            SpeechSound.Z -> zData
            SpeechSound.SH -> shData
            SpeechSound.ZH -> zhData
        }
    }

    fun getPairData(pair: SoundPair): PairData = pairsData[pair] ?: pairsData[SoundPair.S_SH]!!
}
