package com.example.a4umka.data

data class SoundItem(val text: String, val emoji: String = "")

data class SoundData(
    val syllables: List<SoundItem>,
    val wordsStart: List<SoundItem>,
    val wordsMiddle: List<SoundItem>,
    val wordsEnd: List<SoundItem>,
    val phrases: List<String>
)

object SoundAutomationData {
    private val rData = SoundData(
        syllables = listOf(
            SoundItem("РА"), SoundItem("РО"), SoundItem("РУ"), SoundItem("РЫ"),
            SoundItem("АР"), SoundItem("ОР"), SoundItem("УР"), SoundItem("ЫР"),
            SoundItem("АРА"), SoundItem("ОРО"), SoundItem("УРУ"), SoundItem("ЫРЫ"),
            SoundItem("ТРА"), SoundItem("КРО"), SoundItem("БРУ"), SoundItem("ВРЫ"),
            SoundItem("РА-РО-РУ"), SoundItem("РО-РУ-РЫ")
        ),
        wordsStart = listOf(
            SoundItem("РАК", "🦞"), SoundItem("РЫБА", "🐟"), SoundItem("РУКА", "✋"),
            SoundItem("РОЗА", "🌹"), SoundItem("РАДУГА", "🌈"), SoundItem("РУБАШКА", "👕")
        ),
        wordsMiddle = listOf(
            SoundItem("КОРОВА", "🐄"), SoundItem("ВОРОНА", "🐦"), SoundItem("ГОРОХ", "🫛"),
            SoundItem("БАРАН", "🐏"), SoundItem("ПИРОГ", "🥧"), SoundItem("МОРОЗ", "❄️")
        ),
        wordsEnd = listOf(
            SoundItem("КОМАР", "🦟"), SoundItem("ТОПОР", "🪓"), SoundItem("ЗАБОР", "🚧"),
            SoundItem("ПОМИДОР", "🍅"), SoundItem("МУХОМОР", "🍄"), SoundItem("КОВЁР", "🧶")
        ),
        phrases = listOf(
            "Ра-ра-ра — начинается игра",
            "Ры-ры-ры — у нас в руках шары",
            "Ру-ру-ру — бью рукою по шару",
            "Ро-ро-ро — у нас новое ведро"
        )
    )

    private val lData = SoundData(
        syllables = listOf(
            SoundItem("ЛА"), SoundItem("ЛО"), SoundItem("ЛУ"), SoundItem("ЛЫ"),
            SoundItem("АЛ"), SoundItem("ОЛ"), SoundItem("УЛ"), SoundItem("ЫЛ"),
            SoundItem("АЛА"), SoundItem("ОЛО"), SoundItem("УЛУ"), SoundItem("ЫЛЫ"),
            SoundItem("КЛА"), SoundItem("СЛО"), SoundItem("ПЛУ"), SoundItem("БЛЫ"),
            SoundItem("ЛА-ЛО-ЛУ"), SoundItem("ЛО-ЛУ-ЛЫ")
        ),
        wordsStart = listOf(
            SoundItem("ЛУК", "🧅"), SoundItem("ЛАМПА", "💡"), SoundItem("ЛОЖКА", "🥄"),
            SoundItem("ЛОДКА", "🛶"), SoundItem("ЛУНА", "🌙"), SoundItem("ЛАНДЫШ", "🌼")
        ),
        wordsMiddle = listOf(
            SoundItem("ПИЛА", "🪚"), SoundItem("ЮЛА", "🪀"), SoundItem("КУКЛА", "🪆"),
            SoundItem("ХАЛАТ", "🥼"), SoundItem("БУЛКА", "🥖"), SoundItem("ПАЛАТКА", "⛺")
        ),
        wordsEnd = listOf(
            SoundItem("СТОЛ", "🪑"), SoundItem("СТУЛ", "🪑"), SoundItem("ФУТБОЛ", "⚽"),
            SoundItem("ОСЁЛ", "🫏"), SoundItem("ДЯТЕЛ", "🐦"), SoundItem("МЕЛ", "🖍️")
        ),
        phrases = listOf(
            "Ла-ла-ла — в комнате юла",
            "Лу-лу-лу — я сижу на стуле",
            "Ло-ло-ло — на улице тепло",
            "Лы-лы-лы — я мою полы"
        )
    )

    private val sData = SoundData(
        syllables = listOf(
            SoundItem("СА"), SoundItem("СО"), SoundItem("СУ"), SoundItem("СЫ"),
            SoundItem("АС"), SoundItem("ОС"), SoundItem("УС"), SoundItem("ЫС"),
            SoundItem("АСА"), SoundItem("ОСО"), SoundItem("УСУ"), SoundItem("ЫСЫ"),
            SoundItem("СПА"), SoundItem("СТО"), SoundItem("СКУ"), SoundItem("СВЫ"),
            SoundItem("СА-СО-СУ"), SoundItem("СО-СУ-СЫ")
        ),
        wordsStart = listOf(
            SoundItem("СОБАКА", "🐕"), SoundItem("СЛОН", "🐘"), SoundItem("СЫР", "🧀"),
            SoundItem("САМОЛЁТ", "✈️"), SoundItem("СОВА", "🦉"), SoundItem("САНКИ", "🛷")
        ),
        wordsMiddle = listOf(
            SoundItem("ЛИСА", "🦊"), SoundItem("ОСА", "🐝"), SoundItem("КОСА", "🌾"),
            SoundItem("ВЕСЫ", "⚖️"), SoundItem("БУСЫ", "📿"), SoundItem("КОЛЕСО", "🛞")
        ),
        wordsEnd = listOf(
            SoundItem("НОС", "👃"), SoundItem("ЛЕС", "🌲"), SoundItem("ПЁС", "🐕"),
            SoundItem("АВТОБУС", "🚌"), SoundItem("АНАНАС", "🍍"), SoundItem("ПЫЛЕСОС", "🧹")
        ),
        phrases = listOf(
            "Са-са-са — в лесу бегает лиса",
            "Со-со-со — у Вовы колесо",
            "Су-су-су — мы видели осу",
            "Сы-сы-сы — у папы есть усы"
        )
    )

    private val zData = SoundData(
        syllables = listOf(
            SoundItem("ЗА"), SoundItem("ЗО"), SoundItem("ЗУ"), SoundItem("ЗЫ"),
            SoundItem("ЗА-ЗО-ЗУ"), SoundItem("ЗО-ЗУ-ЗЫ")
        ),
        wordsStart = listOf(
            SoundItem("ЗАМОК", "🏰"), SoundItem("ЗАЯЦ", "🐇"), SoundItem("ЗУБЫ", "🦷"),
            SoundItem("ЗОНТ", "☂️"), SoundItem("ЗВЕЗДА", "⭐"), SoundItem("ЗЕБРА", "🦓")
        ),
        wordsMiddle = listOf(
            SoundItem("РОЗА", "🌹"), SoundItem("КОЗА", "🐐"), SoundItem("ВАЗА", "🏺"),
            SoundItem("ГРОЗА", "⛈️"), SoundItem("МЕДУЗА", "🪼"), SoundItem("БИЗОН", "🦬")
        ),
        wordsEnd = listOf(),
        phrases = listOf(
            "За-за-за — у нас идёт коза",
            "Зу-зу-зу — мы поймали стрекозу",
            "Зы-зы-зы — не боимся мы грозы"
        )
    )

    private val shData = SoundData(
        syllables = listOf(
            SoundItem("ША"), SoundItem("ШО"), SoundItem("ШУ"), SoundItem("ШИ"),
            SoundItem("АШ"), SoundItem("ОШ"), SoundItem("УШ"), SoundItem("ЫШ"),
            SoundItem("АША"), SoundItem("ОШО"), SoundItem("УШУ"), SoundItem("ЫШИ"),
            SoundItem("ШПА"), SoundItem("ШТО"), SoundItem("ШКУ"), SoundItem("ШВЫ"),
            SoundItem("ША-ШО-ШУ"), SoundItem("ШО-ШУ-ШИ")
        ),
        wordsStart = listOf(
            SoundItem("ШАР", "🎈"), SoundItem("ШАПКА", "🧢"), SoundItem("ШУБА", "🧥"),
            SoundItem("ШКОЛА", "🏫"), SoundItem("ШИШКА", "🌰"), SoundItem("ШОКОЛАД", "🍫")
        ),
        wordsMiddle = listOf(
            SoundItem("МАШИНА", "🚗"), SoundItem("МЫШКА", "🐭"), SoundItem("КОШКА", "🐈"),
            SoundItem("ЛОШАДЬ", "🐎"), SoundItem("ВИШНЯ", "🍒"), SoundItem("ПОДУШКА", "🛏️")
        ),
        wordsEnd = listOf(
            SoundItem("ДУШ", "🚿"), SoundItem("МЫШЬ", "🐭"), SoundItem("КАМЫШ", "🌾"),
            SoundItem("ЛАНДЫШ", "🌼"), SoundItem("МАЛЫШ", "👶"), SoundItem("ФИНИШ", "🏁")
        ),
        phrases = listOf(
            "Ша-ша-ша — мама моет малыша",
            "Шу-шу-шу — я письмо пишу",
            "Шо-шо-шо — летом хорошо",
            "Ши-ши-ши — на поляне малыши"
        )
    )

    private val zhData = SoundData(
        syllables = listOf(
            SoundItem("ЖА"), SoundItem("ЖО"), SoundItem("ЖУ"), SoundItem("ЖИ"),
            SoundItem("ЖА-ЖО-ЖУ"), SoundItem("ЖО-ЖУ-ЖИ")
        ),
        wordsStart = listOf(
            SoundItem("ЖУК", "🪲"), SoundItem("ЖАБА", "🐸"), SoundItem("ЖИРАФ", "🦒"),
            SoundItem("ЖЁЛУДЬ", "🌰"), SoundItem("ЖУРНАЛ", "📖"), SoundItem("ЖЕЛЕЗО", "🔩")
        ),
        wordsMiddle = listOf(
            SoundItem("ЛУЖА", "💧"), SoundItem("НОЖИ", "🔪"), SoundItem("ЕЖИ", "🦔"),
            SoundItem("ОДЕЖДА", "👗"), SoundItem("СНЕЖИНКА", "❄️"), SoundItem("ПИЖАМА", "🩳")
        ),
        wordsEnd = listOf(),
        phrases = listOf(
            "Жа-жа-жа — есть иголки у ежа",
            "Жу-жу-жу — молоко дадим ежу",
            "Жи-жи-жи — в лесу бегают ежи"
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
}
