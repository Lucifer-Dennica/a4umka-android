package com.example.a4umka.data

data class SoundItem(val text: String, val emoji: String = "")
data class SoundItemWithPos(val text: String, val emoji: String, val position: Int) // 0 - начало, 1 - середина, 2 - конец

data class SoundData(
    val syllables: List<SoundItem>,
    val wordsStart: List<SoundItem>,
    val wordsMiddle: List<SoundItem>,
    val wordsEnd: List<SoundItem>,
    val phrases: List<String>,
    val whereIsSound: List<SoundItemWithPos>
)

data class PairData(
    val syllables: List<Pair<String, String>>,
    val words: List<Triple<String, String, String>>
)

object SoundAutomationData {
    private val rData = SoundData(
        syllables = listOf("РА", "РО", "РУ", "РЫ", "АР", "ОР", "УР", "ЫР", "АРА", "ОРО", "УРУ", "ЫРЫ", "ТРА", "КРО", "БРУ", "ВРЫ", "РА-РО-РУ", "РО-РУ-РЫ").map { SoundItem(it) },
        wordsStart = listOf(SoundItem("РАК", "🦞"), SoundItem("РЫБА", "🐟"), SoundItem("РУКА", "✋"), SoundItem("РОЗА", "🌹"), SoundItem("РАДУГА", "🌈"), SoundItem("РУБАШКА", "👕")),
        wordsMiddle = listOf(SoundItem("КОРОВА", "🐄"), SoundItem("ВОРОНА", "🐦"), SoundItem("ГОРОХ", "🫛"), SoundItem("БАРАН", "🐏"), SoundItem("ПИРОГ", "🥧"), SoundItem("МОРОЗ", "❄️")),
        wordsEnd = listOf(SoundItem("КОМАР", "🦟"), SoundItem("ТОПОР", "🪓"), SoundItem("ЗАБОР", "🚧"), SoundItem("ПОМИДОР", "🍅"), SoundItem("МУХОМОР", "🍄"), SoundItem("КОВЁР", "🧶")),
        phrases = listOf("Ра-ра-ра — начинается игра", "Ры-ры-ры — у нас в руках шары", "Ру-ру-ру — бью рукою по шару", "Ро-ро-ро — у нас новое ведро"),
        whereIsSound = listOf(
            SoundItemWithPos("ГРУЗОВИК", "🚚", 1), SoundItemWithPos("РУКА", "✋", 0), SoundItemWithPos("ПОМИДОР", "🍅", 2), SoundItemWithPos("СВЕТОФОР", "🚦", 2),
            SoundItemWithPos("ЖИРАФ", "🦒", 1), SoundItemWithPos("РЫБА", "🐟", 0), SoundItemWithPos("РАДИО", "📻", 0), SoundItemWithPos("ТОПОР", "🪓", 2),
            SoundItemWithPos("КОМПЬЮТЕР", "💻", 2), SoundItemWithPos("РОМАШКА", "🌼", 0), SoundItemWithPos("БАРАН", "🐏", 1), SoundItemWithPos("КОРОВА", "🐄", 1)
        )
    )

    private val lData = SoundData(
        syllables = listOf("ЛА", "ЛО", "ЛУ", "ЛЫ", "АЛ", "ОЛ", "УЛ", "ЫЛ", "АЛА", "ОЛО", "УЛУ", "ЫЛЫ", "КЛА", "СЛО", "ПЛУ", "БЛЫ", "ЛА-ЛО-ЛУ", "ЛО-ЛУ-ЛЫ").map { SoundItem(it) },
        wordsStart = listOf(SoundItem("ЛУК", "🧅"), SoundItem("ЛАМПА", "💡"), SoundItem("ЛОЖКА", "🥄"), SoundItem("ЛОДКА", "🛶"), SoundItem("ЛУНА", "🌙"), SoundItem("ЛАНДЫШ", "🌼")),
        wordsMiddle = listOf(SoundItem("ПИЛА", "🪚"), SoundItem("ЮЛА", "🪀"), SoundItem("КУКЛА", "🪆"), SoundItem("ХАЛАТ", "🥼"), SoundItem("БУЛКА", "🥖"), SoundItem("ПАЛАТКА", "⛺")),
        wordsEnd = listOf(SoundItem("СТОЛ", "🪑"), SoundItem("СТУЛ", "🪑"), SoundItem("ФУТБОЛ", "⚽"), SoundItem("ОСЁЛ", "🫏"), SoundItem("ДЯТЕЛ", "🐦"), SoundItem("МЕЛ", "🖍️")),
        phrases = listOf("Ла-ла-ла — в комнате юла", "Лу-лу-лу — я сижу на стуле", "Ло-ло-ло — на улице тепло", "Лы-лы-лы — я мою полы"),
        whereIsSound = listOf(
            SoundItemWithPos("ЛОПАТА", "🪏", 0), SoundItemWithPos("ЮЛА", "🪀", 1), SoundItemWithPos("СТОЛ", "🪵", 2), SoundItemWithPos("ПАЛАТКА", "⛺", 1),
            SoundItemWithPos("ЛУНА", "🌙", 0), SoundItemWithPos("КУКЛА", "🪆", 1), SoundItemWithPos("ДЯТЕЛ", "🐦", 2), SoundItemWithPos("ЛЫЖИ", "🎿", 0),
            SoundItemWithPos("МОЛОКО", "🥛", 1), SoundItemWithPos("ЛОШАДЬ", "🐎", 0), SoundItemWithPos("ОСЁЛ", "🫏", 2), SoundItemWithPos("БУЛКА", "🥖", 1)
        )
    )

    private val sData = SoundData(
        syllables = listOf("СА", "СО", "СУ", "СЫ", "АС", "ОС", "УС", "ЫС", "АСА", "ОСО", "УСУ", "ЫСЫ", "СПА", "СТО", "СКУ", "СВЫ", "СА-СО-СУ", "СО-СУ-СЫ").map { SoundItem(it) },
        wordsStart = listOf(SoundItem("СОБАКА", "🐕"), SoundItem("СЛОН", "🐘"), SoundItem("СЫР", "🧀"), SoundItem("САМОЛЁТ", "✈️"), SoundItem("СОВА", "🦉"), SoundItem("САНКИ", "🛷")),
        wordsMiddle = listOf(SoundItem("ЛИСА", "🦊"), SoundItem("ОСА", "🐝"), SoundItem("КОСА", "🌾"), SoundItem("ВЕСЫ", "⚖️"), SoundItem("БУСЫ", "📿"), SoundItem("КОЛЕСО", "🛞")),
        wordsEnd = listOf(SoundItem("НОС", "👃"), SoundItem("ЛЕС", "🌲"), SoundItem("ПЁС", "🐕"), SoundItem("АВТОБУС", "🚌"), SoundItem("АНАНАС", "🍍"), SoundItem("ПЫЛЕСОС", "🧹")),
        phrases = listOf("Са-са-са — в лесу бегает лиса", "Со-со-со — у Вовы колесо", "Су-су-су — мы видели осу", "Сы-сы-сы — у папы есть усы"),
        whereIsSound = listOf(
            SoundItemWithPos("САМОЛЁТ", "✈️", 0), SoundItemWithPos("ЛИСА", "🦊", 1), SoundItemWithPos("АНАНАС", "🍍", 2), SoundItemWithPos("ВЕСЫ", "⚖️", 1),
            SoundItemWithPos("СЛОН", "🐘", 0), SoundItemWithPos("ОСА", "🐝", 1), SoundItemWithPos("ПЫЛЕСОС", "🧹", 2), SoundItemWithPos("СЫР", "🧀", 0),
            SoundItemWithPos("КОСА", "🌾", 1), SoundItemWithPos("СОВА", "🦉", 0), SoundItemWithPos("НОС", "👃", 2), SoundItemWithPos("БУСЫ", "📿", 1)
        )
    )

    private val zData = SoundData(
        syllables = listOf("ЗА", "ЗО", "ЗУ", "ЗЫ", "АЗ", "ОЗ", "УЗ", "ЫЗ", "АЗА", "ОЗО", "УЗУ", "ЫЗЫ", "ЗВА", "ЗДО", "ЗМУ", "ЗБЫ", "ЗА-ЗО-ЗУ", "ЗО-ЗУ-ЗЫ").map { SoundItem(it) },
        wordsStart = listOf(SoundItem("ЗАМОК", "🏰"), SoundItem("ЗАЯЦ", "🐇"), SoundItem("ЗУБЫ", "🦷"), SoundItem("ЗОНТ", "☂️"), SoundItem("ЗВЕЗДА", "⭐"), SoundItem("ЗЕБРА", "🦓")),
        wordsMiddle = listOf(SoundItem("РОЗА", "🌹"), SoundItem("КОЗА", "🐐"), SoundItem("ВАЗА", "🏺"), SoundItem("ГРОЗА", "⛈️"), SoundItem("МЕДУЗА", "🪼"), SoundItem("БИЗОН", "🦬")),
        wordsEnd = listOf(SoundItem("ТАЗ", "🥣"), SoundItem("ГЛАЗ", "👁️"), SoundItem("АРБУЗ", "🍉"), SoundItem("ПАРОВОЗ", "🚂"), SoundItem("СТРЕКОЗА", "🦟"), SoundItem("МЕДУЗА", "🪼")),
        phrases = listOf("За-за-за — у нас идёт коза", "Зу-зу-зу — мы поймали стрекозу", "Зы-зы-зы — не боимся мы грозы"),
        whereIsSound = listOf(
            SoundItemWithPos("ЗАМОК", "🏰", 0), SoundItemWithPos("КОЗА", "🐐", 1), SoundItemWithPos("АРБУЗ", "🍉", 2), SoundItemWithPos("РОЗА", "🌹", 1),
            SoundItemWithPos("ЗЕБРА", "🦓", 0), SoundItemWithPos("ВАЗА", "🏺", 1), SoundItemWithPos("ПАРОВОЗ", "🚂", 2), SoundItemWithPos("ЗОНТ", "☂️", 0),
            SoundItemWithPos("БИЗОН", "🦬", 1), SoundItemWithPos("ЗАЯЦ", "🐇", 0), SoundItemWithPos("ГЛАЗ", "👁️", 2), SoundItemWithPos("ГРОЗА", "⛈️", 1)
        )
    )

    private val shData = SoundData(
        syllables = listOf("ША", "ШО", "ШУ", "ШИ", "АШ", "ОШ", "УШ", "ЫШ", "АША", "ОШО", "УШУ", "ЫШИ", "ШПА", "ШТО", "ШКУ", "ШВЫ", "ША-ШО-ШУ", "ШО-ШУ-ШИ").map { SoundItem(it) },
        wordsStart = listOf(SoundItem("ШАР", "🎈"), SoundItem("ШАПКА", "🧢"), SoundItem("ШУБА", "🧥"), SoundItem("ШКОЛА", "🏫"), SoundItem("ШИШКА", "🌰"), SoundItem("ШОКОЛАД", "🍫")),
        wordsMiddle = listOf(SoundItem("МАШИНА", "🚗"), SoundItem("МЫШКА", "🐭"), SoundItem("КОШКА", "🐈"), SoundItem("ЛОШАДЬ", "🐎"), SoundItem("ВИШНЯ", "🍒"), SoundItem("ПОДУШКА", "🛏️")),
        wordsEnd = listOf(SoundItem("ДУШ", "🚿"), SoundItem("МЫШЬ", "🐭"), SoundItem("КАМЫШ", "🌾"), SoundItem("ЛАНДЫШ", "🌼"), SoundItem("МАЛЫШ", "👶"), SoundItem("ФИНИШ", "🏁")),
        phrases = listOf("Ша-ша-ша — мама моет малыша", "Шу-шу-шу — я письмо пишу", "Шо-шо-шо — летом хорошо", "Ши-ши-ши — на поляне малыши"),
        whereIsSound = listOf(
            SoundItemWithPos("ШАР", "🎈", 0), SoundItemWithPos("МАШИНА", "🚗", 1), SoundItemWithPos("ЛАНДЫШ", "🌼", 2), SoundItemWithPos("МЫШКА", "🐭", 1),
            SoundItemWithPos("ШАПКА", "🧢", 0), SoundItemWithPos("КОШКА", "🐈", 1), SoundItemWithPos("МАЛЫШ", "👶", 2), SoundItemWithPos("ШУБА", "🧥", 0),
            SoundItemWithPos("ВИШНЯ", "🍒", 1), SoundItemWithPos("ШКОЛА", "🏫", 0), SoundItemWithPos("ФИНИШ", "🏁", 2), SoundItemWithPos("ПОДУШКА", "🛏️", 1)
        )
    )

    private val zhData = SoundData(
        syllables = listOf("ЖА", "ЖО", "ЖУ", "ЖИ", "АЖ", "ОЖ", "УЖ", "ИЖ", "АЖА", "ОЖО", "УЖУ", "ИЖИ", "ЖБА", "ЖДО", "ЖМУ", "ЖВЫ", "ЖА-ЖО-ЖУ", "ЖО-ЖУ-ЖИ").map { SoundItem(it) },
        wordsStart = listOf(SoundItem("ЖУК", "🪲"), SoundItem("ЖАБА", "🐸"), SoundItem("ЖИРАФ", "🦒"), SoundItem("ЖЁЛУДЬ", "🌰"), SoundItem("ЖУРНАЛ", "📖"), SoundItem("ЖЕЛЕЗО", "🔩")),
        wordsMiddle = listOf(SoundItem("ЛУЖА", "💧"), SoundItem("НОЖИ", "🔪"), SoundItem("ЕЖИ", "🦔"), SoundItem("ОДЕЖДА", "👗"), SoundItem("СНЕЖИНКА", "❄️"), SoundItem("ПИЖАМА", "🩳")),
        wordsEnd = listOf(SoundItem("НОЖ", "🔪"), SoundItem("ЁЖ", "🦔"), SoundItem("ЧИЖ", "🐦"), SoundItem("БАГАЖ", "🧳"), SoundItem("СТРИЖ", "🐦"), SoundItem("ПЛЯЖ", "🏖️")),
        phrases = listOf("Жа-жа-жа — есть иголки у ежа", "Жу-жу-жу — молоко дадим ежу", "Жи-жи-жи — в лесу бегают ежи"),
        whereIsSound = listOf(
            SoundItemWithPos("ЖУК", "🪲", 0), SoundItemWithPos("ЛУЖА", "💧", 1), SoundItemWithPos("НОЖ", "🔪", 2), SoundItemWithPos("ЕЖИ", "🦔", 1),
            SoundItemWithPos("ЖАБА", "🐸", 0), SoundItemWithPos("НОЖИ", "🔪", 1), SoundItemWithPos("ЁЖ", "🦔", 2), SoundItemWithPos("ЖИРАФ", "🦒", 0),
            SoundItemWithPos("ОДЕЖДА", "👗", 1), SoundItemWithPos("ЖУРНАЛ", "📖", 0), SoundItemWithPos("БАГАЖ", "🧳", 2), SoundItemWithPos("ПИЖАМА", "🩳", 1)
        )
    )

    private val pairsData = mapOf(
        SoundPair.S_SH to PairData(
            syllables = listOf(Pair("СА", "ША"), Pair("СО", "ШО"), Pair("СУ", "ШУ"), Pair("СЫ", "ШИ")),
            words = listOf(Triple("САНКИ", "ШАПКА", "🛷"), Triple("СОВА", "ШУБА", "🦉"), Triple("СУП", "ШУМ", "🥣"), Triple("СЫР", "ШИШКА", "🧀"))
        ),
        SoundPair.R_L to PairData(
            syllables = listOf(Pair("РА", "ЛА"), Pair("РО", "ЛО"), Pair("РУ", "ЛУ"), Pair("РЫ", "ЛЫ")),
            words = listOf(Triple("РАК", "ЛАК", "🦞"), Triple("РОЖКИ", "ЛОЖКИ", "🥄"), Triple("РУКА", "ЛУНА", "✋"), Triple("РЫБА", "ЛЫЖИ", "🐟"))
        ),
        SoundPair.Z_ZH to PairData(
            syllables = listOf(Pair("ЗА", "ЖА"), Pair("ЗО", "ЖО"), Pair("ЗУ", "ЖУ"), Pair("ЗЫ", "ЖИ")),
            words = listOf(Triple("ЗАЯЦ", "ЖАБА", "🐇"), Triple("ЗУБЫ", "ЖУК", "🦷"), Triple("ЗОНТ", "ЖУРНАЛ", "☂️"), Triple("ЗЕБРА", "ЖИРАФ", "🦓"))
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
