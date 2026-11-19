package com.example.sovenok.data

data class AutoWord(val word: String, val emoji: String, val highlightIndex: Int, val position: Int)

data class SoundData(
    val syllables: List<Pair<String, Int>>,
    val wordsStart: List<AutoWord>,
    val wordsMiddle: List<AutoWord>,
    val wordsEnd: List<AutoWord>,
    val phrases: List<Pair<String, Int>>,
    val whereIsSound: List<AutoWord>
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
            Pair("А РО РУ", 0), Pair("О РУ РЫ", 0) // Убрали дефисы, поставили пробелы
        ),
        wordsStart = listOf(
            AutoWord("РАК", "🦞", 0, 0), AutoWord("РУЧКА", "🖊️", 0, 0), AutoWord("РУКА", "✋", 0, 0), AutoWord("РАКУШКА", "🐚", 0, 0)
        ),
        wordsMiddle = listOf(
            AutoWord("ЗЕБРА", "🦓", 3, 1), 
            AutoWord("ШОРТЫ", "🩳", 2, 1), 
            AutoWord("ШАРФ", "🧣", 2, 1), 
            AutoWord("ГРУША", "🍐", 1, 1)
        ),
        wordsEnd = listOf(
            AutoWord("ПОВАР", "👨‍🍳", 4, 2), AutoWord("ТОПОР", "🪓", 4, 2), AutoWord("ТИГР", "🐯", 3, 2), AutoWord("СЫР", "🧀", 2, 2)
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
            AutoWord("ПОМИДОР", "🍅", 6, 2), 
            AutoWord("КОМПЬЮТЕР", "💻", 8, 2), 
            AutoWord("ДРАКОН", "🐉", 1, 1), 
            AutoWord("РАДУГА", "🌈", 0, 0),
            AutoWord("БОБР", "🦫", 3, 2), 
            AutoWord("КОРОВА", "🐄", 2, 1), 
            AutoWord("РОЗА", "🌹", 0, 0), 
            AutoWord("РАК", "🦞", 0, 0),
            AutoWord("РУЧКА", "🖊️", 0, 0), 
            AutoWord("СВЕТОФОР", "🚦", 7, 2), 
            AutoWord("КРАБ", "🦀", 1, 1), 
            AutoWord("КОРОНА", "👑", 2, 1)
        )
    )

    private val lData = SoundData(
        syllables = listOf(
            Pair("ЛА", 0), Pair("ЛО", 0), Pair("ЛУ", 0), Pair("ЛЫ", 0),
            Pair("АЛ", 1), Pair("ОЛ", 1), Pair("УЛ", 1), Pair("ЫЛ", 1),
            Pair("АЛА", 1), Pair("ОЛО", 1), Pair("УЛУ", 1), Pair("ЫЛЫ", 1),
            Pair("КЛА", 1), Pair("СЛО", 1), Pair("ПЛУ", 1), Pair("БЛЫ", 1),
            Pair("ЛА ЛО ЛУ", 0), Pair("ЛО ЛУ ЛЫ", 0)
        ),
        wordsStart = listOf(
            AutoWord("ЛУК", "🧅", 0, 0), AutoWord("ЛАМПА", "💡", 0, 0), AutoWord("ЛОЖКА", "🥄", 0, 0), AutoWord("ЛОДКА", "🛶", 0, 0)
        ),
        wordsMiddle = listOf(
            AutoWord("ПИЛА", "🪚", 2, 1), AutoWord("ЮЛА", "🪀", 1, 1), AutoWord("КУКЛА", "🪆", 3, 1), AutoWord("ХАЛАТ", "🥼", 2, 1)
        ),
        wordsEnd = listOf(
            AutoWord("СТОЛ", "🪵", 3, 2), AutoWord("СТУЛ", "🪑", 3, 2), AutoWord("ФУТБОЛ", "⚽", 5, 2), AutoWord("ОСЁЛ", "🫏", 3, 2)
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
            AutoWord("ЛОПАТА", "🪏", 0, 0), AutoWord("ЮЛА", "🪀", 1, 1), AutoWord("СТОЛ", "🪵", 3, 2), AutoWord("ПАЛАТКА", "⛺", 3, 1),
            AutoWord("ЛУНА", "🌙", 0, 0), AutoWord("КУКЛА", "🪆", 3, 1), AutoWord("ДЯТЕЛ", "🐦", 3, 2), AutoWord("ЛЫЖИ", "🎿", 0, 0),
            AutoWord("МОЛОКО", "🥛", 2, 1), AutoWord("ЛОШАДЬ", "🐎", 0, 0), AutoWord("ОСЁЛ", "🫏", 3, 2), AutoWord("БУЛКА", "🥖", 2, 1)
        )
    )

    private val sData = SoundData(
        syllables = listOf(
            Pair("СА", 0), Pair("СО", 0), Pair("СУ", 0), Pair("СЫ", 0),
            Pair("АС", 1), Pair("ОС", 1), Pair("УС", 1), Pair("ЫС", 1),
            Pair("АСА", 1), Pair("ОСО", 1), Pair("УСУ", 1), Pair("ЫСЫ", 1),
            Pair("СПА", 0), Pair("СТО", 0), Pair("СКУ", 0), Pair("СВЫ", 0),
            Pair("СА СО СУ", 0), Pair("СО СУ СЫ", 0)
        ),
        wordsStart = listOf(
            AutoWord("СОБАКА", "🐕", 0, 0), AutoWord("СЛОН", "🐘", 0, 0), AutoWord("СЫР", "🧀", 0, 0), AutoWord("САМОЛЁТ", "✈️", 0, 0)
        ),
        wordsMiddle = listOf(
            AutoWord("ЛИСА", "🦊", 2, 1), AutoWord("ОСА", "🐝", 1, 1), AutoWord("КОСА", "🌾", 2, 1), AutoWord("ВЕСЫ", "⚖️", 2, 1)
        ),
        wordsEnd = listOf(
            AutoWord("НОС", "👃", 2, 2), AutoWord("ЛЕС", "🌲", 2, 2), AutoWord("ПЁС", "🐕", 2, 2), AutoWord("АВТОБУС", "🚌", 6, 2)
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
            AutoWord("САМОЛЁТ", "✈️", 0, 0), AutoWord("ЛИСА", "🦊", 2, 1), AutoWord("АНАНАС", "🍍", 5, 2), AutoWord("ВЕСЫ", "⚖️", 2, 1),
            AutoWord("СЛОН", "🐘", 0, 0), AutoWord("ОСА", "🐝", 1, 1), AutoWord("ПЫЛЕСОС", "🧹", 7, 2), AutoWord("СЫР", "🧀", 0, 0),
            AutoWord("КОСА", "🌾", 2, 1), AutoWord("СОВА", "🦉", 0, 0), AutoWord("НОС", "👃", 2, 2), AutoWord("БУСЫ", "📿", 2, 1)
        )
    )

    private val zData = SoundData(
        syllables = listOf(
            Pair("ЗА", 0), Pair("ЗО", 0), Pair("ЗУ", 0), Pair("ЗЫ", 0),
            Pair("АЗ", 1), Pair("ОЗ", 1), Pair("УЗ", 1), Pair("ЫЗ", 1),
            Pair("АЗА", 1), Pair("ОЗО", 1), Pair("УЗУ", 1), Pair("ЫЗЫ", 1),
            Pair("ЗВА", 0), Pair("ЗДО", 0), Pair("ЗМУ", 0), Pair("ЗБЫ", 0),
            Pair("ЗА ЗО ЗУ", 0), Pair("ЗО ЗУ ЗЫ", 0)
        ),
        wordsStart = listOf(
            AutoWord("ЗАМОК", "🏰", 0, 0), AutoWord("ЗАЯЦ", "🐇", 0, 0), AutoWord("ЗУБЫ", "🦷", 0, 0), AutoWord("ЗОНТ", "☂️", 0, 0)
        ),
        wordsMiddle = listOf(
            AutoWord("РОЗА", "🌹", 2, 1), AutoWord("КОЗА", "🐐", 2, 1), AutoWord("ВАЗА", "🏺", 2, 1), AutoWord("ГРОЗА", "⛈️", 2, 1)
        ),
        wordsEnd = listOf(
            AutoWord("ТАЗ", "🥣", 2, 2), AutoWord("ГЛАЗ", "👁️", 3, 2), AutoWord("АРБУЗ", "🍉", 4, 2), AutoWord("ПАРОВОЗ", "🚂", 6, 2)
        ),
        phrases = listOf(
            Pair("За-за-за — у нас идёт коза", 0),
            Pair("Зу-зу-зу — мы поймали стрекозу", 0),
            Pair("Зы-зы-зы — не боимся мы грозы", 0),
            Pair("У Зои новый зонт", 2),
            Pair("В вазе стоят розы", 7)
        ),
        whereIsSound = listOf(
            AutoWord("ЗАМОК", "🏰", 0, 0), AutoWord("КОЗА", "🐐", 2, 1), AutoWord("АРБУЗ", "🍉", 4, 2), AutoWord("РОЗА", "🌹", 2, 1),
            AutoWord("ЗЕБРА", "🦓", 0, 0), AutoWord("ВАЗА", "🏺", 2, 1), AutoWord("ПАРОВОЗ", "🚂", 6, 2), AutoWord("ЗОНТ", "☂️", 0, 0),
            AutoWord("БИЗОН", "🦬", 2, 1), AutoWord("ЗАЯЦ", "🐇", 0, 0), AutoWord("ГЛАЗ", "👁️", 3, 2), AutoWord("ГРОЗА", "⛈️", 2, 1)
        )
    )

    private val shData = SoundData(
        syllables = listOf(
            Pair("ША", 0), Pair("ШО", 0), Pair("ШУ", 0), Pair("ШИ", 0),
            Pair("АШ", 1), Pair("ОШ", 1), Pair("УШ", 1), Pair("ЫШ", 1),
            Pair("АША", 1), Pair("ОШО", 1), Pair("УШУ", 1), Pair("ЫШИ", 1),
            Pair("ШПА", 0), Pair("ШТО", 0), Pair("ШКУ", 0), Pair("ШВЫ", 0),
            Pair("ША ШО ШУ", 0), Pair("ШО ШУ ШИ", 0)
        ),
        wordsStart = listOf(
            AutoWord("ШАР", "🎈", 0, 0), AutoWord("ШАПКА", "🧢", 0, 0), AutoWord("ШУБА", "🧥", 0, 0), AutoWord("ШКОЛА", "🏫", 0, 0)
        ),
        wordsMiddle = listOf(
            AutoWord("МАШИНА", "🚗", 2, 1), AutoWord("МЫШКА", "🐭", 2, 1), AutoWord("КОШКА", "🐈", 2, 1), AutoWord("ЛОШАДЬ", "🐎", 2, 1)
        ),
        wordsEnd = listOf(
            AutoWord("ДУШ", "🚿", 2, 2), AutoWord("МЫШЬ", "🐭", 3, 2), AutoWord("КАМЫШ", "🌾", 4, 2), AutoWord("ЛАНДЫШ", "🌼", 5, 2)
        ),
        phrases = listOf(
            Pair("Ша-ша-ша — мама моет малыша", 0),
            Pair("Шу-шу-шу — я письмо пишу", 0),
            Pair("Шо-шо-шо — летом хорошо", 0),
            Pair("Ши-ши-ши — на поляне малыши", 0),
            Pair("У Маши новая шуба", 4)
        ),
        whereIsSound = listOf(
            AutoWord("ШАР", "🎈", 0, 0), AutoWord("МАШИНА", "🚗", 2, 1), AutoWord("ЛАНДЫШ", "🌼", 5, 2), AutoWord("МЫШКА", "🐭", 2, 1),
            AutoWord("ШАПКА", "🧢", 0, 0), AutoWord("КОШКА", "🐈", 2, 1), AutoWord("МАЛЫШ", "👶", 4, 2), AutoWord("ШУБА", "🧥", 0, 0),
            AutoWord("ВИШНЯ", "🍒", 2, 1), AutoWord("ШКОЛА", "🏫", 0, 0), AutoWord("ФИНИШ", "🏁", 4, 2), AutoWord("ПОДУШКА", "🛏️", 4, 1)
        )
    )

    private val zhData = SoundData(
        syllables = listOf(
            Pair("ЖА", 0), Pair("ЖО", 0), Pair("ЖУ", 0), Pair("ЖИ", 0),
            Pair("АЖ", 1), Pair("ОЖ", 1), Pair("УЖ", 1), Pair("ИЖ", 1),
            Pair("АЖА", 1), Pair("ОЖО", 1), Pair("УЖУ", 1), Pair("ИЖИ", 1),
            Pair("ЖБА", 0), Pair("ЖДО", 0), Pair("ЖМУ", 0), Pair("ЖВЫ", 0),
            Pair("ЖА ЖО ЖУ", 0), Pair("ЖО ЖУ ЖИ", 0)
        ),
        wordsStart = listOf(
            AutoWord("ЖУК", "🪲", 0, 0), AutoWord("ЖАБА", "🐸", 0, 0), AutoWord("ЖИРАФ", "🦒", 0, 0), AutoWord("ЖЁЛУДЬ", "🌰", 0, 0)
        ),
        wordsMiddle = listOf(
            AutoWord("ЛУЖА", "💧", 2, 1), AutoWord("НОЖИ", "🔪", 2, 1), AutoWord("ЕЖИ", "🦔", 1, 1), AutoWord("ОДЕЖДА", "👗", 2, 1)
        ),
        wordsEnd = listOf(
            AutoWord("НОЖ", "🔪", 2, 2), AutoWord("ЁЖ", "🦔", 1, 2), AutoWord("ЧИЖ", "🐦", 2, 2), AutoWord("БАГАЖ", "🧳", 4, 2)
        ),
        phrases = listOf(
            Pair("Жа-жа-жа — есть иголки у ежа", 0),
            Pair("Жу-жу-жу — молоко дадим ежу", 0),
            Pair("Жи-жи-жи — в лесу бегают ежи", 0),
            Pair("У Жени новый журнал", 2)
        ),
        whereIsSound = listOf(
            AutoWord("ЖУК", "🪲", 0, 0), AutoWord("ЛУЖА", "💧", 2, 1), AutoWord("НОЖ", "🔪", 2, 2), AutoWord("ЕЖИ", "🦔", 1, 1),
            AutoWord("ЖАБА", "🐸", 0, 0), AutoWord("НОЖИ", "🔪", 2, 1), AutoWord("ЁЖ", "🦔", 1, 2), AutoWord("ЖИРАФ", "🦒", 0, 0),
            AutoWord("ОДЕЖДА", "👗", 2, 1), AutoWord("ЖУРНАЛ", "📖", 0, 0), AutoWord("БАГАЖ", "🧳", 4, 2), AutoWord("ПИЖАМА", "🩳", 2, 1)
        )
    )

    private val pairsData = mapOf(
        SoundPair.S_SH to PairData(
            syllables = listOf("СА-ША", "СО-ШО", "СУ-ШУ", "СЫ-ШИ", "АС-АШ", "ОС-ОШ", "УС-УШ", "АСА-АША"),
            words = listOf(
                PairWord("ГРУША", "🍐", "Ш"), PairWord("КОШКА", "🐈", "Ш"), PairWord("КОМПАС", "🧭", "С"), PairWord("ПОДСОЛНУХ", "🌻", "С"),
                PairWord("БАБУШКА", "👵", "Ш"), PairWord("СОК", "🧃", "С"), PairWord("РОМАШКА", "🌼", "Ш"), PairWord("АНАНАС", "🍍", "С"),
                PairWord("МОСТ", "🌉", "С"), PairWord("ШАРФ", "🧣", "Ш"), PairWord("ШАХМАТЫ", "♟️", "Ш"), PairWord("СКРИПКА", "🎻", "С")
            )
        ),
        SoundPair.R_L to PairData(
            syllables = listOf("РА-ЛА", "РО-ЛО", "РУ-ЛУ", "РЫ-ЛЫ", "АР-АЛ", "ОР-ОЛ", "УР-УЛ", "АРА-АЛА"),
            words = listOf(
                PairWord("ЖИРАФ", "🦒", "Р"), PairWord("ЛОДКА", "🛶", "Л"), PairWord("ЛАМА", "🦙", "Л"), PairWord("ЁЛКА", "🎄", "Л"),
                PairWord("ОБЛАКО", "☁️", "Л"), PairWord("БАКЛАЖАН", "🍆", "Л"), PairWord("РАК", "🦞", "Р"), PairWord("СЫР", "🧀", "Р"),
                PairWord("РУКА", "✋", "Р"), PairWord("ЛЫЖИ", "🎿", "Л"), PairWord("КОРОНА", "👑", "Р"), PairWord("ПИРОЖНОЕ", "🍰", "Р")
            )
        ),
        SoundPair.Z_ZH to PairData(
            syllables = listOf("ЗА-ЖА", "ЗО-ЖО", "ЗУ-ЖУ", "ЗЫ-ЖИ", "АЗА-АЖА", "ОЗО-ОЖО", "УЗУ-УЖУ", "АЗА-АЖА"),
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
