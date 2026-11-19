package com.example.sovenok.data

/**
 * База мнемотаблиц. ~80 текстов: 4 сезона × 8 + потешки 12 + сказки 12 + животные 12 + 12 описательных схем.
 * Тексты — народные потешки/заклички, краткие пересказы народных сказок, классика XIX–нач. XX в.,
 * короткие авторские описания животных и Seasons-стихи (в общественном достоянии или народные).
 */
object MnemoData {

    // ===========================================================
    //                          ОСЕНЬ
    // ===========================================================
    private val autumnTexts = listOf(
        // 4-5 лет (4 клетки)
        MnemoText(
            id = "autumn_1", title = "Листья жёлтые летят",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Листья жёлтые летят,", "🍂"),
                MnemoLine("Под ногами шелестят.", "👣"),
                MnemoLine("Тучки по небу бегут,", "☁️"),
                MnemoLine("Дождик льётся тут и тут.", "🌧️")
            )
        ),
        MnemoText(
            id = "autumn_2", title = "Тучка хмурится, ворчит",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Тучка хмурится, ворчит,", "☁️"),
                MnemoLine("Дождик по окну стучит.", "💧"),
                MnemoLine("Сапоги скорей надень —", "🥾"),
                MnemoLine("Будем в лужах шлёпать день!", "💦")
            )
        ),
        MnemoText(
            id = "autumn_3", title = "Яблоко румяное",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Яблоко румяное", "🍎"),
                MnemoLine("Выросло в саду.", "🌳"),
                MnemoLine("Я его сорву сейчас", "✋"),
                MnemoLine("И маме отнесу.", "👩")
            )
        ),
        MnemoText(
            id = "autumn_4", title = "Белка прыгает по веткам",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Белка прыгает по веткам,", "🐿️"),
                MnemoLine("Носит шишки своим деткам.", "🌰"),
                MnemoLine("Спрячет в дупле грибок —", "🍄"),
                MnemoLine("Будет сытым мой зверёк.", "😋")
            )
        ),
        // 5-6 лет (6 клеток)
        MnemoText(
            id = "autumn_5", title = "Осень",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Наступила осень.", "🍂"),
                MnemoLine("Небо хмурое, часто идёт дождь.", "🌧️"),
                MnemoLine("Листья на деревьях желтеют и опадают.", "🍁"),
                MnemoLine("Птицы улетают в тёплые края.", "🕊️"),
                MnemoLine("Белка запасает на зиму грибы и орехи.", "🐿️"),
                MnemoLine("Люди собирают урожай.", "🧺")
            )
        ),
        MnemoText(
            id = "autumn_6", title = "Осень по лесу гуляет",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Осень по лесу гуляет,", "🌲"),
                MnemoLine("Листья с веток обрывает.", "🍂"),
                MnemoLine("Ёжик в норку поспешил,", "🦔"),
                MnemoLine("Спать до солнышка решил.", "😴"),
                MnemoLine("Белка шишки собирает,", "🐿️"),
                MnemoLine("Зиму в гости поджидает.", "❄️")
            )
        ),
        MnemoText(
            id = "autumn_7", title = "Урожай",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Осенью в огороде собирают урожай.", "🌾"),
                MnemoLine("Бабушка копает картошку.", "🥔"),
                MnemoLine("Дедушка срезает большую тыкву.", "🎃"),
                MnemoLine("Мама собирает помидоры и огурцы.", "🍅"),
                MnemoLine("Я дёргаю морковку за зелёный хвостик.", "🥕"),
                MnemoLine("Вечером мы варим вкусный суп.", "🍲")
            )
        ),
        MnemoText(
            id = "autumn_8", title = "За грибами",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Осенью мы с папой пошли в лес за грибами.", "🌲"),
                MnemoLine("Взяли с собой корзинку.", "🧺"),
                MnemoLine("Под берёзой нашли подберёзовик.", "🍄"),
                MnemoLine("В траве прятались рыжие лисички.", "🦊"),
                MnemoLine("А мухомор мы не тронули — он ядовитый.", "🔴"),
                MnemoLine("Дома мама сварила грибной суп.", "🍲")
            )
        ),
        MnemoText(
            id = "autumn_9", title = "Перелётные птицы",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Осенью становится холодно.", "🥶"),
                MnemoLine("Насекомые прячутся, и птицам нечего есть.", "🐛"),
                MnemoLine("Птицы собираются в стаи.", "🐦"),
                MnemoLine("Утки и гуси улетают на юг.", "🦢"),
                MnemoLine("Там тепло и много корма.", "☀️"),
                MnemoLine("Весной птицы вернутся домой.", "🌸")
            )
        ),
        MnemoText(
            id = "autumn_10", title = "Что надеть осенью",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Осенью на улице прохладно.", "🍂"),
                MnemoLine("Мы надеваем тёплую куртку.", "🧥"),
                MnemoLine("На голову — шапку.", "🧢"),
                MnemoLine("На шею — шарф.", "🧣"),
                MnemoLine("Когда идёт дождь, берём зонтик.", "☂️"),
                MnemoLine("А на ноги — резиновые сапоги.", "🥾")
            )
        ),
        MnemoText(
            id = "autumn_11", title = "Листопад",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Листопад, листопад —", "🍂"),
                MnemoLine("Листья по ветру летят.", "💨"),
                MnemoLine("Жёлтый, красный, золотой —", "🍁"),
                MnemoLine("Лес стоит совсем другой.", "🌲"),
                MnemoLine("Соберу букет большой", "💐"),
                MnemoLine("И домой возьму с собой.", "🏠")
            )
        ),
        // 6-7 лет (9 клеток)
        MnemoText(
            id = "autumn_12", title = "Как звери готовятся к зиме",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Осенью звери готовятся к зиме.", "🍂"),
                MnemoLine("Белка сушит грибы на ветках.", "🐿️"),
                MnemoLine("Ёжик носит в норку сухие листья.", "🦔"),
                MnemoLine("Медведь ищет берлогу.", "🐻"),
                MnemoLine("Заяц меняет серую шубку на белую.", "🐇"),
                MnemoLine("Барсук запасает корешки и жёлуди.", "🦡"),
                MnemoLine("Бобры строят хатку из веток.", "🦫"),
                MnemoLine("Мышка тащит в норку зёрнышки.", "🐭"),
                MnemoLine("Теперь зверям не страшна зима.", "❄️")
            )
        ),
        MnemoText(
            id = "autumn_13", title = "Осень в лесу",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Ходит осень по лесам,", "🌲"),
                MnemoLine("Красит листья в разные цвета.", "🍁"),
                MnemoLine("Ёжик таскает яблоки.", "🦔"),
                MnemoLine("Белка сушит грибочки.", "🐿️"),
                MnemoLine("Медведь берлогу строит.", "🐻"),
                MnemoLine("Лиса в норе свернулась.", "🦊"),
                MnemoLine("Зайчик шубку поменял.", "🐇"),
                MnemoLine("Птицы клином улетают.", "🕊️"),
                MnemoLine("Лес к зиме готовится.", "❄️")
            )
        )
    )

    // ===========================================================
    //                          ЗИМА
    // ===========================================================
    private val winterTexts = listOf(
        // 4-5 лет (4 клетки)
        MnemoText(
            id = "winter_1", title = "Снег пушистый за окном",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Снег пушистый за окном,", "❄️"),
                MnemoLine("Белым стал и сад, и дом.", "🏠"),
                MnemoLine("Мы на санках мчим с горы —", "🛷"),
                MnemoLine("Нет веселее поры!", "😄")
            )
        ),
        MnemoText(
            id = "winter_2", title = "Снеговик",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Мы слепили снеговика:", "⛄"),
                MnemoLine("Нос-морковка, два уголька.", "🥕"),
                MnemoLine("Шапка-ведро на голове —", "🪣"),
                MnemoLine("Стоит довольный во дворе.", "😊")
            )
        ),
        MnemoText(
            id = "winter_3", title = "Снежинка",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Снежинка с неба прилетела,", "❄️"),
                MnemoLine("На варежку мою присела.", "🧤"),
                MnemoLine("Я подышал — и вот беда:", "💨"),
                MnemoLine("Осталась капелька-вода.", "💧")
            )
        ),
        MnemoText(
            id = "winter_4", title = "Как на тоненький ледок",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Как на тоненький ледок выпал беленький снежок.", "❄️"),
                MnemoLine("Выпал беленький снежок, ехал Ванечка-дружок.", "🛷"),
                MnemoLine("Ваня ехал, поспешал,", "🏇"),
                MnemoLine("Со добра коня упал.", "😮")
            )
        ),
        // 5-6 лет (6 клеток)
        MnemoText(
            id = "winter_5", title = "Зима",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Пришла зима.", "❄️"),
                MnemoLine("На улице холодно, дует ветер.", "💨"),
                MnemoLine("Падает снег, всё вокруг белое.", "⛄"),
                MnemoLine("Реки покрылись льдом.", "🧊"),
                MnemoLine("Медведь спит в берлоге.", "🐻"),
                MnemoLine("Дети катаются на санках и лепят снеговика.", "⛄")
            )
        ),
        MnemoText(
            id = "winter_6", title = "Новый год",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Скоро Новый год!", "🎉"),
                MnemoLine("Мы наряжаем ёлку игрушками.", "🎄"),
                MnemoLine("На верхушке горит звезда.", "⭐"),
                MnemoLine("Под ёлкой лежат подарки.", "🎁"),
                MnemoLine("В гости приходит Дед Мороз.", "🎅"),
                MnemoLine("Все водят хоровод и поют песни.", "🎶")
            )
        ),
        MnemoText(
            id = "winter_7", title = "Зимующие птицы",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Зимой многие птицы остаются с нами.", "🐦"),
                MnemoLine("Это воробьи, синицы и снегири.", "🐦"),
                MnemoLine("Им холодно и голодно.", "🥶"),
                MnemoLine("Мы сделали для птиц кормушку.", "🏠"),
                MnemoLine("Насыпали в неё семечки и крошки.", "🌾"),
                MnemoLine("Птицы прилетают и благодарят нас песней.", "🎶")
            )
        ),
        MnemoText(
            id = "winter_8", title = "Белый снег лежит кругом",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_5_6, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Белый снег лежит кругом,", "❄️"),
                MnemoLine("Мы на горку все бегом!", "🏃"),
                MnemoLine("Санки мчатся с ветерком,", "🛷"),
                MnemoLine("Лепим бабу мы потом.", "⛄"),
                MnemoLine("А на катке у пруда —", "🧊"),
                MnemoLine("Гладкий, крепкий лёд всегда.", "⛸️")
            )
        ),
        MnemoText(
            id = "winter_9", title = "Зимой в лесу",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Зимой в лесу тихо.", "🌲"),
                MnemoLine("Деревья стоят в белых шапках.", "🌲"),
                MnemoLine("На снегу видны следы зайца.", "🐇"),
                MnemoLine("Лиса ищет мышей под снегом.", "🦊"),
                MnemoLine("Сорока трещит на ветке.", "🐦"),
                MnemoLine("Медведь крепко спит в берлоге до весны.", "🐻")
            )
        ),
        // 6-7 лет (9 клеток)
        MnemoText(
            id = "winter_10", title = "Зимние забавы",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Зимой много снега на улице.", "❄️"),
                MnemoLine("Дети катаются с горы на санках.", "🛷"),
                MnemoLine("На коньках скользят по льду.", "⛸️"),
                MnemoLine("Лепят снеговика во дворе.", "⛄"),
                MnemoLine("Играют в снежки весело.", "❄️"),
                MnemoLine("Строят крепость из снега.", "🏰"),
                MnemoLine("На лыжах катаются по лыжне.", "🎿"),
                MnemoLine("Щёки румяные от мороза.", "🥶"),
                MnemoLine("Холодно, но очень весело!", "😄")
            )
        ),
        MnemoText(
            id = "winter_11", title = "Как звери зимуют",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Зимой лесным зверям холодно.", "❄️"),
                MnemoLine("Медведь спит в тёплой берлоге.", "🐻"),
                MnemoLine("Ёж свернулся клубком под листьями.", "🦔"),
                MnemoLine("Барсук спит в глубокой норе.", "🦡"),
                MnemoLine("Белка прыгает по веткам.", "🐿️"),
                MnemoLine("Заяц в белой шубке бегает.", "🐇"),
                MnemoLine("Лиса мышкует — ищет мышей под снегом.", "🦊"),
                MnemoLine("Волк бродит по лесу стаями.", "🐺"),
                MnemoLine("У каждого свой способ зимовать.", "🌲")
            )
        )
    )

    // ===========================================================
    //                          ВЕСНА
    // ===========================================================
    private val springTexts = listOf(
        // 4-5 лет (4 клетки)
        MnemoText(
            id = "spring_1", title = "Солнце светит, снег растаял",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Солнце светит, снег растаял,", "☀️"),
                MnemoLine("Побежал в овраг ручей.", "💧"),
                MnemoLine("Скворушка домой вернулся,", "🐦"),
                MnemoLine("Стало в мире веселей.", "😊")
            )
        ),
        MnemoText(
            id = "spring_2", title = "Первый цветок",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Из-под снега на лужок", "❄️"),
                MnemoLine("Появился первый цветок —", "🌸"),
                MnemoLine("Нежный, белый, голубой,", "🌼"),
                MnemoLine("Он принёс весну с собой.", "🌷")
            )
        ),
        MnemoText(
            id = "spring_3", title = "Кораблик",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Мы кораблик смастерили,", "🛶"),
                MnemoLine("По ручью его пустили.", "💧"),
                MnemoLine("Плыви, кораблик, далеко —", "🌊"),
                MnemoLine("Тебе на солнышке легко!", "☀️")
            )
        ),
        MnemoText(
            id = "spring_4", title = "Ёжик спал всю зиму в норке",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Ёжик спал всю зиму в норке,", "🦔"),
                MnemoLine("Под листвой на тихой горке.", "🍂"),
                MnemoLine("А весной проснулся он:", "🌅"),
                MnemoLine("Солнце светит, птичий звон!", "🐦")
            )
        ),
        MnemoText(
            id = "spring_5", title = "Бабочка проснулась",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Бабочка проснулась,", "🦋"),
                MnemoLine("Крылышки раскрыла.", "🦋"),
                MnemoLine("На цветок уселась —", "🌸"),
                MnemoLine("Весна наступила!", "🌷")
            )
        ),
        MnemoText(
            id = "spring_6", title = "Жаворонки, прилетите",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Жаворонки, прилетите,", "🐦"),
                MnemoLine("Студёну зиму унесите,", "❄️"),
                MnemoLine("Тёплу весну принесите:", "🌸"),
                MnemoLine("Зима нам надоела, весь хлеб у нас поела!", "🌾")
            )
        ),
        // 5-6 лет (6 клеток)
        MnemoText(
            id = "spring_7", title = "Весна",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Наступила весна.", "🌸"),
                MnemoLine("Солнце светит ярче и греет.", "☀️"),
                MnemoLine("Тает снег, бегут ручьи.", "💧"),
                MnemoLine("На деревьях появляются листочки.", "🌱"),
                MnemoLine("Прилетают птицы и вьют гнёзда.", "🐦"),
                MnemoLine("Из берлоги выходит медведь.", "🐻")
            )
        ),
        MnemoText(
            id = "spring_8", title = "Скворечник",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Весной мы с дедушкой сделали скворечник.", "🏠"),
                MnemoLine("Повесили его на высокую берёзу.", "🌳"),
                MnemoLine("Скоро прилетел скворец.", "🐦"),
                MnemoLine("Он поселился в домике.", "🏡"),
                MnemoLine("Скворец носил в домик травинки и пух.", "🪹"),
                MnemoLine("А потом у него появились птенцы.", "🐣")
            )
        ),
        MnemoText(
            id = "spring_9", title = "Огород весной",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Весной мы копаем грядки.", "🌱"),
                MnemoLine("Сажаем семена.", "🌰"),
                MnemoLine("Поливаем их водой из лейки.", "💧"),
                MnemoLine("Скоро появляются ростки.", "🌱"),
                MnemoLine("Мы выдёргиваем сорняки.", "🌿"),
                MnemoLine("Летом у нас будет урожай.", "🥕")
            )
        ),
        MnemoText(
            id = "spring_10", title = "Весна, весна красная",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Весна, весна красная!", "🌸"),
                MnemoLine("Приди, весна, с радостью,", "😊"),
                MnemoLine("С великою милостью:", "✨"),
                MnemoLine("Со льном высоким,", "🌾"),
                MnemoLine("С корнем глубоким,", "🌳"),
                MnemoLine("С хлебом обильным!", "🌾")
            )
        ),
        // 6-7 лет (9 клеток)
        MnemoText(
            id = "spring_11", title = "Весеннее пробуждение",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Зима ушла, пришла весна.", "🌸"),
                MnemoLine("Солнце греет всё сильней.", "☀️"),
                MnemoLine("Снег растаял, мокро кругом.", "💧"),
                MnemoLine("Побежали ручьи звонко.", "💦"),
                MnemoLine("На деревьях почки лопнули.", "🌱"),
                MnemoLine("Появилась зелёная травка.", "🌿"),
                MnemoLine("Первоцветы украсили луг.", "🌼"),
                MnemoLine("Птицы весёлые песни поют.", "🐦"),
                MnemoLine("Природа проснулась ото сна.", "🌅")
            )
        ),
        MnemoText(
            id = "spring_12", title = "Весенний лес",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Весной лес оживает.", "🌲"),
                MnemoLine("Снег растаял, появились проталины.", "🌿"),
                MnemoLine("На деревьях набухли почки.", "🌱"),
                MnemoLine("Вернулись с юга грачи.", "🐦"),
                MnemoLine("Медведь проснулся в берлоге.", "🐻"),
                MnemoLine("Зашевелился ёжик в норе.", "🦔"),
                MnemoLine("Заяц меняет шубку на серую.", "🐇"),
                MnemoLine("Распустились подснежники.", "🌸"),
                MnemoLine("Лес наполняется звуками.", "🎶")
            )
        )
    )

    // ===========================================================
    //                          ЛЕТО
    // ===========================================================
    private val summerTexts = listOf(
        // 4-5 лет (4 клетки)
        MnemoText(
            id = "summer_1", title = "Летом солнышко печёт",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Летом солнышко печёт,", "☀️"),
                MnemoLine("Речка тёплая течёт.", "🌊"),
                MnemoLine("Мы купаемся с утра —", "🏊"),
                MnemoLine("Эх, весёлая пора!", "😄")
            )
        ),
        MnemoText(
            id = "summer_2", title = "Радуга-дуга встаёт",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Дождик кончился, и вот —", "🌧️"),
                MnemoLine("Радуга-дуга встаёт!", "🌈"),
                MnemoLine("Словно мостик через речку,", "🌉"),
                MnemoLine("Словно яркое колечко.", "💍")
            )
        ),
        MnemoText(
            id = "summer_3", title = "Одуванчик",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Одуванчик золотой", "🌼"),
                MnemoLine("Был весёлый, молодой.", "☀️"),
                MnemoLine("А теперь он — пух седой:", "🌬️"),
                MnemoLine("Дунешь — пух летит гурьбой!", "🌬️")
            )
        ),
        MnemoText(
            id = "summer_4", title = "Прыгает кузнечик",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Прыгает кузнечик", "🦗"),
                MnemoLine("По зелёной травке,", "🌿"),
                MnemoLine("Звонко он стрекочет", "🎶"),
                MnemoLine("На лесной полянке.", "🌳")
            )
        ),
        MnemoText(
            id = "summer_5", title = "Радуга-дуга",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Радуга-дуга,", "🌈"),
                MnemoLine("Не давай дождя,", "🚫"),
                MnemoLine("Давай солнышка —", "☀️"),
                MnemoLine("Колоколнышка!", "🔔")
            )
        ),
        // 5-6 лет (6 клеток)
        MnemoText(
            id = "summer_6", title = "Лето",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Наступило лето.", "☀️"),
                MnemoLine("Жарко, светит яркое солнце.", "🌞"),
                MnemoLine("Цветут цветы, летают бабочки.", "🦋"),
                MnemoLine("В лесу поспевают ягоды.", "🍓"),
                MnemoLine("Дети купаются в речке.", "🏊"),
                MnemoLine("Иногда бывает гроза с громом и молнией.", "⛈️")
            )
        ),
        MnemoText(
            id = "summer_7", title = "На даче",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Летом мы живём на даче.", "🏡"),
                MnemoLine("Утром я помогаю бабушке поливать огород.", "💧"),
                MnemoLine("Днём мы ходим на речку.", "🏊"),
                MnemoLine("В лесу собираем землянику.", "🍓"),
                MnemoLine("Вечером пьём чай на веранде.", "☕"),
                MnemoLine("А ночью смотрим на звёзды.", "⭐")
            )
        ),
        MnemoText(
            id = "summer_8", title = "Летом в лесу",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Летом в лесу много интересного.", "🌲"),
                MnemoLine("Поют птицы.", "🐦"),
                MnemoLine("Жужжат пчёлы.", "🐝"),
                MnemoLine("На полянке растут цветы и ягоды.", "🌸"),
                MnemoLine("Муравьи строят муравейник.", "🐜"),
                MnemoLine("В кустах прячется ёжик.", "🦔")
            )
        ),
        MnemoText(
            id = "summer_9", title = "На море",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Летом мы поехали на море.", "🌊"),
                MnemoLine("Там тёплый песок и солнце.", "🏖️"),
                MnemoLine("Я построил замок из песка.", "🏰"),
                MnemoLine("Мы купались в тёплых волнах.", "🏊"),
                MnemoLine("Я нашёл красивую ракушку.", "🐚"),
                MnemoLine("На море было очень весело!", "😄")
            )
        ),
        // 6-7 лет (9 клеток)
        MnemoText(
            id = "summer_10", title = "Жаркий полдень",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Солнце в зените палит.", "☀️"),
                MnemoLine("Воздух дрожит от жары.", "🔥"),
                MnemoLine("Трава пожелтела на лугу.", "🌾"),
                MnemoLine("Кузнечики стрекочут в траве.", "🦗"),
                MnemoLine("Птицы спрятались в тень.", "🌳"),
                MnemoLine("Пчёлы в ульях жужжат.", "🐝"),
                MnemoLine("Собака лежит в тени.", "🐕"),
                MnemoLine("Люди в реке спасаются.", "🏊"),
                MnemoLine("Ждём вечерней прохлады.", "🌆")
            )
        ),
        MnemoText(
            id = "summer_11", title = "Летний вечер",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Солнце садится за лес.", "🌇"),
                MnemoLine("Небо окрасилось в розовое.", "🌸"),
                MnemoLine("Тени на земле удлинились.", "🌑"),
                MnemoLine("Птицы стихли в гнёздах.", "🐦"),
                MnemoLine("Светлячки зажглись в траве.", "✨"),
                MnemoLine("Звёзды на небе замигали.", "⭐"),
                MnemoLine("Луна выплыла на небо.", "🌙"),
                MnemoLine("Кузнечики затянули песню.", "🦗"),
                MnemoLine("Тихий летний вечер настал.", "🌆")
            )
        )
    )

    // ===========================================================
    //                       ПОТЕШКИ
    // ===========================================================
    private val poteshkiTexts = listOf(
        // === 4-5 лет (4 клетки) ===
        MnemoText(
            id = "poteshki_1", title = "Как у нашего кота",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Как у нашего кота шубка очень хороша,", "🐱"),
                MnemoLine("Как у котика усы удивительной красы,", "🐈"),
                MnemoLine("Глаза смелые,", "👀"),
                MnemoLine("Зубки белые.", "🦷")
            )
        ),
        MnemoText(
            id = "poteshki_2", title = "Солнышко-вёдрышко",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Солнышко-вёдрышко,", "☀️"),
                MnemoLine("Выгляни в окошко!", "🪟"),
                MnemoLine("Твои детки плачут,", "😢"),
                MnemoLine("По камушкам скачут.", "🪨")
            )
        ),
        MnemoText(
            id = "poteshki_3", title = "Наша Маша маленька",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Наша Маша маленька,", "👧"),
                MnemoLine("На ней шубка аленька,", "🧥"),
                MnemoLine("Опушка бобровая,", "🟤"),
                MnemoLine("Маша чернобровая.", "👁️")
            )
        ),
        MnemoText(
            id = "poteshki_4", title = "Пальчик-мальчик",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Пальчик-мальчик, где ты был?", "👆"),
                MnemoLine("С этим братцем в лес ходил,", "👋"),
                MnemoLine("С этим братцем щи варил,", "🍲"),
                MnemoLine("С этим братцем кашу ел, с этим братцем песни пел.", "🎵")
            )
        ),
        MnemoText(
            id = "poteshki_5", title = "Киска, киска, брысь",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Киска, киска, киска, брысь!", "🐱"),
                MnemoLine("На дорожку не садись:", "🛤️"),
                MnemoLine("Наша деточка пойдёт,", "👶"),
                MnemoLine("Через киску упадёт.", "💥")
            )
        ),
        MnemoText(
            id = "poteshki_6", title = "Баю-баюшки-баю",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Баю-баюшки-баю,", "🌙"),
                MnemoLine("Не ложися на краю:", "🛏️"),
                MnemoLine("Придёт серенький волчок,", "🐺"),
                MnemoLine("Он ухватит за бочок.", "😱")
            )
        ),
        MnemoText(
            id = "poteshki_7", title = "Ай, качи-качи-качи",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Ай, качи-качи-качи!", "🎶"),
                MnemoLine("Глянь — баранки, калачи!", "🥨"),
                MnemoLine("С пылу, с жару из печи —", "🔥"),
                MnemoLine("Все румяны, горячи.", "🍞")
            )
        ),
        MnemoText(
            id = "poteshki_8", title = "Совушка-сова",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Совушка-сова,", "🦉"),
                MnemoLine("Большая голова,", "🧠"),
                MnemoLine("На пеньке сидит,", "🪵"),
                MnemoLine("Во все стороны глядит.", "👀")
            )
        ),
        MnemoText(
            id = "poteshki_9", title = "Ходит котик по лавочке",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Ходит котик по лавочке,", "🐱"),
                MnemoLine("Водит кошку за лапочки.", "🐈"),
                MnemoLine("Топы-топы по лавочке,", "🐾"),
                MnemoLine("Цапы-цапы за лапочки.", "🤝")
            )
        ),
        MnemoText(
            id = "poteshki_10", title = "Огуречик, огуречик",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Огуречик, огуречик,", "🥒"),
                MnemoLine("Не ходи на тот конечик:", "🚫"),
                MnemoLine("Там мышка живёт,", "🐭"),
                MnemoLine("Тебе хвостик отгрызёт.", "🪤")
            )
        ),
        MnemoText(
            id = "poteshki_11", title = "Заинька, попляши",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Заинька, попляши, серенький, попляши!", "🐰"),
                MnemoLine("Вот так, вот так попляши!", "💃"),
                MnemoLine("Заинька, топни ножкой, серенький, топни ножкой!", "🦵"),
                MnemoLine("Вот так, вот так топни ножкой!", "👣")
            )
        ),
        // === 5-6 лет (6 клеток) ===
        MnemoText(
            id = "poteshki_12", title = "Сорока-белобока",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Сорока-белобока", "🐦"),
                MnemoLine("Кашу варила,", "🥘"),
                MnemoLine("Деток кормила.", "🍼"),
                MnemoLine("Этому дала, этому дала,", "👦👧"),
                MnemoLine("А этому не дала:", "😢"),
                MnemoLine("— Ты воды не носил, дров не рубил!", "🪵")
            )
        ),
        MnemoText(
            id = "poteshki_13", title = "Божья коровка",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Божья коровка,", "🐞"),
                MnemoLine("Улети на небо,", "🌤️"),
                MnemoLine("Там твои детки", "👶"),
                MnemoLine("Кушают конфетки.", "🍬"),
                MnemoLine("Всем по одной,", "🍭"),
                MnemoLine("А тебе ни одной!", "🙅")
            )
        ),
        MnemoText(
            id = "poteshki_14", title = "Дождик, дождик, пуще",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Дождик, дождик, пуще!", "🌧️"),
                MnemoLine("Дам тебе я гущи.", "🥣"),
                MnemoLine("Выйду на крылечко,", "🚪"),
                MnemoLine("Дам огуречка.", "🥒"),
                MnemoLine("Дам и хлеба каравай —", "🍞"),
                MnemoLine("Сколько хочешь поливай!", "💧")
            )
        ),
        MnemoText(
            id = "poteshki_15", title = "Петушок, петушок",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Петушок, петушок,", "🐓"),
                MnemoLine("Золотой гребешок,", "👑"),
                MnemoLine("Масляна головушка, шёлкова бородушка,", "💛"),
                MnemoLine("Что ты рано встаёшь,", "🌅"),
                MnemoLine("Голосисто поёшь,", "🎶"),
                MnemoLine("Деткам спать не даёшь?", "😴")
            )
        ),
        MnemoText(
            id = "poteshki_16", title = "Идёт коза рогатая",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Идёт коза рогатая", "🐐"),
                MnemoLine("За малыми ребятами.", "👶"),
                MnemoLine("Ножками топ-топ!", "👣"),
                MnemoLine("Глазками хлоп-хлоп!", "👀"),
                MnemoLine("Кто кашу не ест, молока не пьёт —", "🥣"),
                MnemoLine("Забодает, забодает!", "💢")
            )
        ),
        MnemoText(
            id = "poteshki_17", title = "Сидит белка на тележке",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Сидит белка на тележке,", "🐿️"),
                MnemoLine("Продаёт она орешки:", "🌰"),
                MnemoLine("Лисичке-сестричке,", "🦊"),
                MnemoLine("Воробью, синичке,", "🐦"),
                MnemoLine("Мишке толстопятому,", "🐻"),
                MnemoLine("Заиньке усатому.", "🐰")
            )
        ),
        MnemoText(
            id = "poteshki_18", title = "Водичка, водичка",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Водичка, водичка,", "💧"),
                MnemoLine("Умой моё личико,", "👶"),
                MnemoLine("Чтобы глазки блестели,", "✨"),
                MnemoLine("Чтобы щёчки краснели,", "😊"),
                MnemoLine("Чтоб смеялся роток,", "😄"),
                MnemoLine("Чтоб кусался зубок.", "🦷")
            )
        ),
        MnemoText(
            id = "poteshki_19", title = "Тень-тень-потетень",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Тень-тень-потетень, выше города плетень.", "🏙️"),
                MnemoLine("Сели звери под плетень, похвалялися весь день.", "🪵"),
                MnemoLine("Похвалялася лиса: «Всему свету я краса!»", "🦊"),
                MnemoLine("Похвалялся зайка: «Пойди догоняй-ка!»", "🐰"),
                MnemoLine("Похвалялися ежи: «У нас шубы хороши!»", "🦔"),
                MnemoLine("Похвалялся медведь: «Могу песни я петь!»", "🐻")
            )
        ),
        MnemoText(
            id = "poteshki_20", title = "Зайчишка-трусишка",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Зайчишка-трусишка по полю бежал,", "🐰"),
                MnemoLine("В огород забежал,", "🌱"),
                MnemoLine("Морковку нашёл,", "🥕"),
                MnemoLine("Капустку нашёл,", "🥬"),
                MnemoLine("Сидит, грызёт.", "😋"),
                MnemoLine("Ай, кто-то идёт!", "😱")
            )
        ),
        MnemoText(
            id = "poteshki_21", title = "Курочка-рябушечка",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("— Курочка-рябушечка, куда пошла?", "🐔"),
                MnemoLine("— На речку.", "🏞️"),
                MnemoLine("— Курочка-рябушечка, зачем пошла?", "❓"),
                MnemoLine("— За водичкой.", "💧"),
                MnemoLine("— Курочка-рябушечка, зачем тебе водичка?", "💧"),
                MnemoLine("— Цыпляток поить. Они пить хотят!", "🐥")
            )
        ),
        MnemoText(
            id = "poteshki_22", title = "Гуси, гуси",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("— Гуси, гуси! — Га-га-га!", "🦢"),
                MnemoLine("— Есть хотите? — Да-да-да!", "😋"),
                MnemoLine("— Так летите же домой!", "🏠"),
                MnemoLine("— Серый волк под горой", "🐺"),
                MnemoLine("Не пускает нас домой!", "🚫"),
                MnemoLine("— Ну летите как хотите, только крылья берегите!", "🪶")
            )
        ),
        // === 6-7 лет (9 клеток) ===
        MnemoText(
            id = "poteshki_23", title = "Ладушки",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_6_7, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Ладушки, ладушки!", "👏"),
                MnemoLine("— Где были? — У бабушки.", "👵"),
                MnemoLine("— Что ели? — Кашку.", "🥣"),
                MnemoLine("— Что пили? — Простоквашку.", "🥛"),
                MnemoLine("Кашка сладенька,", "🍯"),
                MnemoLine("Бабушка добренька.", "❤️"),
                MnemoLine("Попили, поели,", "🍽️"),
                MnemoLine("Шу-у… Домой полетели,", "🕊️"),
                MnemoLine("На головку сели!", "👶")
            )
        ),
        // НОВЫЕ — длинные потешки по образцу для 9 клеток
        MnemoText(
            id = "poteshki_24", title = "Сорока-ворона",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_6_7, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Сорока-ворона кашу варила,", "🐦"),
                MnemoLine("На порог скакала,", "🚪"),
                MnemoLine("Гостей поджидала.", "👨‍👩‍👧"),
                MnemoLine("Приехали гости,", "🐎"),
                MnemoLine("Стали кланяться:", "🙇"),
                MnemoLine("«Здравствуй, сорока-ворона!»,", "👋"),
                MnemoLine("«Чем вас угощать?»", "🍽️"),
                MnemoLine("«Кашкой с молоком»,", "🥛"),
                MnemoLine("«И пряником с медком!»", "🍯")
            )
        ),
        MnemoText(
            id = "poteshki_25", title = "Идёт коза по лесу",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_6_7, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Идёт коза по лесу,", "🐐"),
                MnemoLine("Ищет коза траву.", "🌿"),
                MnemoLine("Найдёт травку — жуёт,", "🍽️"),
                MnemoLine("Найдёт цветочек — срывает,", "🌸"),
                MnemoLine("А потом плясать идёт.", "💃"),
                MnemoLine("Топ-топ — ножками,", "👣"),
                MnemoLine("Хлоп-хлоп — лапками,", "👏"),
                MnemoLine("А козлята вдогонку бегут,", "🏃"),
                MnemoLine("Хоровод вокруг мамы ведут.", "🌀")
            )
        )
    )

    // ===========================================================
    //                          СКАЗКИ
    // ===========================================================
    private val skazkiTexts = listOf(
        // === 5-6 лет (6 клеток) ===
        MnemoText(
            id = "skazki_1", title = "Репка",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Посадил дед репку.", "🌱"),
                MnemoLine("Выросла репка большая-пребольшая.", "🥬"),
                MnemoLine("Стал дед репку тянуть: тянет-потянет, вытянуть не может.", "👴"),
                MnemoLine("Позвал дед бабку, бабка — внучку.", "👵👧"),
                MnemoLine("Внучка позвала Жучку, Жучка — кошку, кошка — мышку.", "🐕🐈🐭"),
                MnemoLine("Тянут-потянут — и вытянули репку!", "🎉")
            )
        ),
        MnemoText(
            id = "skazki_2", title = "Курочка Ряба",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Жили-были дед да баба.", "👴👵"),
                MnemoLine("Была у них курочка Ряба.", "🐔"),
                MnemoLine("Снесла курочка яичко, не простое — золотое.", "🥚"),
                MnemoLine("Дед бил-бил — не разбил. Баба била-била — не разбила.", "🔨"),
                MnemoLine("Мышка бежала, хвостиком махнула — яичко упало и разбилось.", "🐭"),
                MnemoLine("Курочка говорит: «Не плачьте, снесу вам яичко простое».", "🥚")
            )
        ),
        MnemoText(
            id = "skazki_3", title = "Теремок",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Стоит в поле теремок.", "🏠"),
                MnemoLine("Прибежала мышка-норушка, потом лягушка-квакушка.", "🐸🐭"),
                MnemoLine("Прискакал зайчик-побегайчик.", "🐰"),
                MnemoLine("Пришли лисичка-сестричка и волчок — серый бочок.", "🦊🐺"),
                MnemoLine("Пришёл медведь, полез в теремок — и раздавил его.", "🐻"),
                MnemoLine("Звери построили новый теремок, лучше прежнего.", "🏡")
            )
        ),
        MnemoText(
            id = "skazki_4", title = "Маша и медведь",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Пошла Маша в лес за грибами и заблудилась.", "👧🍄"),
                MnemoLine("Нашла избушку, а там жил медведь.", "🏠🐻"),
                MnemoLine("Стала Маша у медведя жить, по хозяйству помогать.", "🧹"),
                MnemoLine("Напекла Маша пирожков и спряталась в короб.", "🥧📦"),
                MnemoLine("Медведь понёс короб в деревню, к дедушке и бабушке.", "🚶"),
                MnemoLine("Собаки залаяли, медведь убежал, а Маша вернулась домой.", "🐕")
            )
        ),
        MnemoText(
            id = "skazki_5", title = "Заюшкина избушка",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Была у зайца избушка лубяная, а у лисы — ледяная.", "🐰🦊"),
                MnemoLine("Пришла весна — у лисы избушка растаяла.", "💧"),
                MnemoLine("Попросилась лиса к зайцу, да его же и выгнала.", "🚪"),
                MnemoLine("Не смогли зайчику помочь ни собака, ни медведь.", "🐕🐻"),
                MnemoLine("Помог петух с косой: «Несу косу на плечах!»", "🐓"),
                MnemoLine("Лиса испугалась и убежала, а зайчик вернулся домой.", "🏠")
            )
        ),
        MnemoText(
            id = "skazki_6", title = "Лиса и журавль",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Подружилась лиса с журавлём.", "🦊🦩"),
                MnemoLine("Позвала лиса журавля в гости и подала кашу на тарелке.", "🥘"),
                MnemoLine("Журавль стучал клювом, но ничего не съел.", "🦩"),
                MnemoLine("Позвал журавль лису к себе и подал окрошку в кувшине.", "🫙"),
                MnemoLine("Лиса вокруг кувшина ходила, но ничего не достала.", "🦊"),
                MnemoLine("С тех пор дружба у них врозь.", "💔")
            )
        ),
        MnemoText(
            id = "skazki_7", title = "Каша из топора",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Шёл солдат домой и попросился к старушке переночевать.", "🪖"),
                MnemoLine("Старушка сказала, что угостить нечем.", "👵"),
                MnemoLine("Солдат взял топор и стал варить из него кашу.", "🪓"),
                MnemoLine("Попросил немножко крупы, соли и масла.", "🌾"),
                MnemoLine("Каша получилась вкусная.", "😋"),
                MnemoLine("А топор солдат забрал с собой.", "🎒")
            )
        ),
        MnemoText(
            id = "skazki_8", title = "Петушок и бобовое зёрнышко",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Петушок клевал зёрнышки и подавился.", "🐓"),
                MnemoLine("Курочка побежала к корове за маслом.", "🐔🐄"),
                MnemoLine("Корова сказала: «Принеси травы от косцов».", "🌾"),
                MnemoLine("Косцы попросили хлеба у пекарей.", "🍞"),
                MnemoLine("Пекари дали хлеба, косцы — травы, корова — масла.", "🤝"),
                MnemoLine("Курочка смазала горлышко, и петушок запел: «Ку-ка-ре-ку!»", "🎶")
            )
        ),
        MnemoText(
            id = "skazki_9", title = "Снегурочка",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Жили-были дед да баба, и не было у них детей.", "👴👵"),
                MnemoLine("Зимой слепили они из снега девочку.", "❄️"),
                MnemoLine("Девочка ожила — это была Снегурочка.", "👧"),
                MnemoLine("Весной Снегурочка стала грустной.", "😢"),
                MnemoLine("Летом подружки позвали её прыгать через костёр.", "🔥"),
                MnemoLine("Снегурочка прыгнула — и растаяла белым облачком.", "☁️")
            )
        ),
        MnemoText(
            id = "skazki_10", title = "Рукавичка",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Шёл дед лесом и потерял рукавичку.", "🧤"),
                MnemoLine("Первой в неё забралась мышка.", "🐭"),
                MnemoLine("Потом лягушка и зайчик.", "🐸🐰"),
                MnemoLine("Потом лисичка и волк.", "🦊🐺"),
                MnemoLine("Последним залез медведь — стало очень тесно.", "🐻"),
                MnemoLine("Дед вернулся, собака залаяла — и звери разбежались.", "🐕")
            )
        ),
        // === 6-7 лет (9 клеток) ===
        MnemoText(
            id = "skazki_11", title = "Колобок",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_6_7, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Испекла бабушка колобок.", "🍞"),
                MnemoLine("Положила его на окошко остудиться.", "🪟"),
                MnemoLine("Колобок спрыгнул и покатился по дорожке.", "🛣️"),
                MnemoLine("Навстречу ему заяц.", "🐰"),
                MnemoLine("Потом волк.", "🐺"),
                MnemoLine("Потом медведь.", "🐻"),
                MnemoLine("Всем колобок пел песенку и укатывался.", "🎶"),
                MnemoLine("А потом встретил лису.", "🦊"),
                MnemoLine("Лиса его — ам! — и съела.", "👅")
            )
        ),
        MnemoText(
            id = "skazki_12", title = "Гуси-лебеди",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_6_7, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Родители ушли и велели дочке беречь братца.", "👨‍👩‍👧"),
                MnemoLine("Девочка заигралась, а гуси-лебеди унесли братца.", "🦢"),
                MnemoLine("Побежала девочка догонять.", "🏃‍♀️"),
                MnemoLine("Печка просила съесть пирожок.", "🍞"),
                MnemoLine("Яблоня просила съесть яблочко.", "🍎"),
                MnemoLine("Речка просила выпить киселька.", "🥛"),
                MnemoLine("Девочка нашла братца в избушке Бабы-яги.", "🏚️"),
                MnemoLine("Схватила братца и побежала домой.", "🏃‍♀️"),
                MnemoLine("Речка, яблоня и печка её спрятали — дети вернулись домой.", "🏠")
            )
        ),
        MnemoText(
            id = "skazki_13", title = "Волк и семеро козлят",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_6_7, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Жила-была коза с козлятами.", "🐐"),
                MnemoLine("Уходила коза в лес и велела никому не открывать.", "🌲"),
                MnemoLine("Пришёл волк и запел грубым голосом.", "🐺"),
                MnemoLine("Козлята не открыли: «Это не мамин голос!»", "🚪"),
                MnemoLine("Волк сделал голос тоньше.", "🎤"),
                MnemoLine("Козлята открыли — и волк их проглотил.", "😱"),
                MnemoLine("Только самый маленький спрятался в печке.", "🔥"),
                MnemoLine("Вернулась коза, и они вместе проучили волка.", "💪"),
                MnemoLine("Козлята выскочили целые и невредимые!", "🎉")
            )
        ),
        // НОВЫЕ — русские народные сказки по образцу
        MnemoText(
            id = "skazki_14", title = "Лисичка со скалочкой",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Шла лисичка по дорожке и нашла скалочку.", "🦊"),
                MnemoLine("Постучалась в первый дом — пустили переночевать.", "🚪"),
                MnemoLine("Скалочку — за петушка.", "🥖"),
                MnemoLine("Петушка — за гусёночка.", "🐓"),
                MnemoLine("Гусёночка — за индюшонка.", "🦃"),
                MnemoLine("Индюшонка — за шубейку — всех обменяла!", "🧥")
            )
        ),
        MnemoText(
            id = "skazki_15", title = "Зимовье зверей",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_6_7, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Собрались звери в лес зимовать.", "🌲"),
                MnemoLine("Бык — брёвна таскал.", "🐂"),
                MnemoLine("Баран — шерстью утеплял.", "🐑"),
                MnemoLine("Свинья — землю рыла.", "🐷"),
                MnemoLine("Кот — крышу крыл.", "🐈"),
                MnemoLine("Петух — на страже стоял.", "🐓"),
                MnemoLine("Волк и медведь хотели их съесть.", "🐺🐻"),
                MnemoLine("Звери их испугали пустой башкой.", "💀"),
                MnemoLine("С тех пор хищники их не трогают.", "🚫")
            )
        )
    )

    // ===========================================================
    //                       ПРО ЖИВОТНЫХ
    // ===========================================================
    private val animalsTexts = listOf(
        // === 4-5 лет (4 клетки) — короткие описания для малышей ===
        MnemoText(
            id = "animals_1", title = "Котик",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_4_5, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это котик.", "🐱"),
                MnemoLine("У него мягкая шёрстка.", "🧶"),
                MnemoLine("Котик любит молоко.", "🥛"),
                MnemoLine("Он ловит мышей.", "🐭")
            )
        ),
        MnemoText(
            id = "animals_2", title = "Собачка",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_4_5, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это собачка.", "🐶"),
                MnemoLine("Она лает: «Гав-гав!»", "🔊"),
                MnemoLine("Собачка охраняет дом.", "🏠"),
                MnemoLine("Она любит косточку.", "🦴")
            )
        ),
        MnemoText(
            id = "animals_3", title = "Птичка",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_4_5, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это птичка.", "🐦"),
                MnemoLine("У неё есть крылышки.", "🪽"),
                MnemoLine("Птичка летает в небе.", "☁️"),
                MnemoLine("Она поёт: «Чик-чирик!»", "🎶")
            )
        ),
        MnemoText(
            id = "animals_4", title = "Рыбка",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_4_5, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это рыбка.", "🐟"),
                MnemoLine("Она живёт в воде.", "💧"),
                MnemoLine("У рыбки есть хвостик.", "🐠"),
                MnemoLine("Она плавает быстро-быстро.", "🏊")
            )
        ),
        // === 5-6 лет (6 клеток) — основные оригинальные тексты ===
        MnemoText(
            id = "animals_5", title = "Ёжик",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это ёжик.", "🦔"),
                MnemoLine("У него на спине колючие иголки.", "📌"),
                MnemoLine("Ёжик живёт в лесу.", "🌲"),
                MnemoLine("Он ест жуков и червяков.", "🐛"),
                MnemoLine("Зимой ёжик спит в норке.", "😴"),
                MnemoLine("Весной он просыпается и ищет еду.", "🌱")
            )
        ),
        MnemoText(
            id = "animals_6", title = "Кошка",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это кошка.", "🐱"),
                MnemoLine("У неё мягкая шёрстка и длинный хвост.", "🐈"),
                MnemoLine("Кошка живёт дома с людьми.", "🏠"),
                MnemoLine("Она любит молоко и рыбку.", "🥛"),
                MnemoLine("Кошка ловит мышей.", "🐭"),
                MnemoLine("Котята играют с клубком.", "🧶")
            )
        ),
        MnemoText(
            id = "animals_7", title = "Собака",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это собака.", "🐕"),
                MnemoLine("Она живёт в будке или дома.", "🏡"),
                MnemoLine("Собака охраняет дом и лает на чужих.", "🔊"),
                MnemoLine("Она любит грызть косточку.", "🦴"),
                MnemoLine("Собака — верный друг человека.", "🤝"),
                MnemoLine("Детёныш собаки — щенок.", "🐶")
            )
        ),
        MnemoText(
            id = "animals_8", title = "Корова",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это корова.", "🐄"),
                MnemoLine("У коровы есть рога и хвост.", "🐂"),
                MnemoLine("Корова живёт на ферме.", "🌾"),
                MnemoLine("Она ест траву и сено.", "🌿"),
                MnemoLine("Корова даёт людям молоко.", "🥛"),
                MnemoLine("Корова мычит: «Му-у!»", "📢")
            )
        ),
        MnemoText(
            id = "animals_9", title = "Лиса",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это лиса.", "🦊"),
                MnemoLine("У неё рыжая шубка и пушистый хвост.", "🔥"),
                MnemoLine("Лиса живёт в лесу, в норе.", "🌲"),
                MnemoLine("Она охотится на мышей.", "🐭"),
                MnemoLine("Лиса хитрая и осторожная.", "🧠"),
                MnemoLine("Детёныш лисы — лисёнок.", "🦊")
            )
        ),
        MnemoText(
            id = "animals_10", title = "Медведь",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это медведь.", "🐻"),
                MnemoLine("Он большой, сильный и косолапый.", "💪"),
                MnemoLine("Медведь живёт в лесу.", "🌲"),
                MnemoLine("Он любит мёд, ягоды и рыбу.", "🍯"),
                MnemoLine("Зимой медведь спит в берлоге.", "😴"),
                MnemoLine("Детёныш медведя — медвежонок.", "🐻")
            )
        ),
        MnemoText(
            id = "animals_11", title = "Заяц",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это заяц.", "🐰"),
                MnemoLine("У него длинные уши и короткий хвостик.", "👂"),
                MnemoLine("Летом шубка у зайца серая, а зимой — белая.", "🐇"),
                MnemoLine("Заяц ест траву, морковку и кору деревьев.", "🥕"),
                MnemoLine("Он быстро бегает и далеко прыгает.", "🏃"),
                MnemoLine("Детёныш зайца — зайчонок.", "🐰")
            )
        ),
        MnemoText(
            id = "animals_12", title = "Белка",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это белка.", "🐿️"),
                MnemoLine("У неё пушистый хвост и кисточки на ушах.", "🍂"),
                MnemoLine("Белка живёт в дупле на дереве.", "🌳"),
                MnemoLine("Она ест орехи, шишки и грибы.", "🌰"),
                MnemoLine("Белка ловко прыгает с ветки на ветку.", "🌿"),
                MnemoLine("Осенью белка делает запасы на зиму.", "🍄")
            )
        ),
        MnemoText(
            id = "animals_13", title = "Курица",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это курица.", "🐔"),
                MnemoLine("Она живёт в курятнике.", "🏚️"),
                MnemoLine("Курица клюёт зёрнышки.", "🌾"),
                MnemoLine("Она несёт яйца.", "🥚"),
                MnemoLine("Из яиц вылупляются цыплята.", "🐥"),
                MnemoLine("Курица кудахчет: «Ко-ко-ко!»", "🔊")
            )
        ),
        MnemoText(
            id = "animals_14", title = "Воробей",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это воробей.", "🐦"),
                MnemoLine("Он маленький и серый.", "🩶"),
                MnemoLine("Воробей живёт рядом с людьми.", "🏙️"),
                MnemoLine("Он клюёт крошки и семечки.", "🍞"),
                MnemoLine("Воробей чирикает: «Чик-чирик!»", "🎶"),
                MnemoLine("Зимой воробей не улетает на юг.", "❄️")
            )
        ),
        MnemoText(
            id = "animals_15", title = "Лошадь",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это лошадь.", "🐎"),
                MnemoLine("У неё длинная грива и хвост.", "💇"),
                MnemoLine("Лошадь живёт в конюшне.", "🏠"),
                MnemoLine("Она ест сено и овёс.", "🌾"),
                MnemoLine("Лошадь возит людей и грузы.", "🛻"),
                MnemoLine("Детёныш лошади — жеребёнок.", "🐴")
            )
        ),
        // === 6-7 лет (9 клеток) — мои по образцу ===
        MnemoText(
            id = "animals_16", title = "Дельфин",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это дельфин.", "🐬"),
                MnemoLine("Он живёт в море.", "🌊"),
                MnemoLine("Дельфин очень умный.", "🧠"),
                MnemoLine("У него гладкое тело и острый нос.", "🔺"),
                MnemoLine("Дельфин плавает очень быстро.", "🏊"),
                MnemoLine("Он дышит воздухом.", "💨"),
                MnemoLine("Дельфины живут стаями.", "👥"),
                MnemoLine("Они умеют общаться звуками.", "🎶"),
                MnemoLine("Люди любят дельфинов за доброту.", "❤️")
            )
        ),
        MnemoText(
            id = "animals_17", title = "Пчела",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Это пчела.", "🐝"),
                MnemoLine("Она маленькая и полосатая.", "🟡"),
                MnemoLine("Пчела живёт в улье.", "🏠"),
                MnemoLine("Она летает над цветами.", "🌸"),
                MnemoLine("Пчела собирает нектар.", "🍯"),
                MnemoLine("Из нектара получается мёд.", "🍯"),
                MnemoLine("Пчёлы живут большой семьёй.", "👨‍👩‍👧"),
                MnemoLine("У пчелы есть жало.", "⚠️"),
                MnemoLine("Пчёлы приносят пользу людям.", "✨")
            )
        )
    )

    // ===========================================================
    //          ОПИСАТЕЛЬНЫЕ СХЕМЫ (много на категорию)
    // ===========================================================
    val descriptiveSchemes = listOf(
        // === Овощи (12) ===
        DescriptiveScheme(id = "desc_veg_1", title = "Помидор", category = DescriptiveCategory.VEGETABLES, emoji = "🍅"),
        DescriptiveScheme(id = "desc_veg_2", title = "Огурец", category = DescriptiveCategory.VEGETABLES, emoji = "🥒"),
        DescriptiveScheme(id = "desc_veg_3", title = "Морковь", category = DescriptiveCategory.VEGETABLES, emoji = "🥕"),
        DescriptiveScheme(id = "desc_veg_4", title = "Лук", category = DescriptiveCategory.VEGETABLES, emoji = "🧅"),
        DescriptiveScheme(id = "desc_veg_5", title = "Капуста", category = DescriptiveCategory.VEGETABLES, emoji = "🥬"),
        DescriptiveScheme(id = "desc_veg_6", title = "Чеснок", category = DescriptiveCategory.VEGETABLES, emoji = "🧄"),
        DescriptiveScheme(id = "desc_veg_7", title = "Картошка", category = DescriptiveCategory.VEGETABLES, emoji = "🥔"),
        DescriptiveScheme(id = "desc_veg_8", title = "Перец", category = DescriptiveCategory.VEGETABLES, emoji = "🫑"),
        DescriptiveScheme(id = "desc_veg_9", title = "Тыква", category = DescriptiveCategory.VEGETABLES, emoji = "🎃"),
        DescriptiveScheme(id = "desc_veg_10", title = "Горошек", category = DescriptiveCategory.VEGETABLES, emoji = "🟢"),
        DescriptiveScheme(id = "desc_veg_11", title = "Баклажан", category = DescriptiveCategory.VEGETABLES, emoji = "🍆"),
        DescriptiveScheme(id = "desc_veg_12", title = "Кукуруза", category = DescriptiveCategory.VEGETABLES, emoji = "🌽"),
        // === Фрукты (14) — добавлены ягоды ===
        DescriptiveScheme(id = "desc_fru_1", title = "Яблоко", category = DescriptiveCategory.FRUITS, emoji = "🍎"),
        DescriptiveScheme(id = "desc_fru_2", title = "Груша", category = DescriptiveCategory.FRUITS, emoji = "🍐"),
        DescriptiveScheme(id = "desc_fru_3", title = "Банан", category = DescriptiveCategory.FRUITS, emoji = "🍌"),
        DescriptiveScheme(id = "desc_fru_4", title = "Апельсин", category = DescriptiveCategory.FRUITS, emoji = "🍊"),
        DescriptiveScheme(id = "desc_fru_5", title = "Виноград", category = DescriptiveCategory.FRUITS, emoji = "🍇"),
        DescriptiveScheme(id = "desc_fru_6", title = "Клубника", category = DescriptiveCategory.FRUITS, emoji = "🍓"),
        DescriptiveScheme(id = "desc_fru_7", title = "Лимон", category = DescriptiveCategory.FRUITS, emoji = "🍋"),
        DescriptiveScheme(id = "desc_fru_8", title = "Арбуз", category = DescriptiveCategory.FRUITS, emoji = "🍉"),
        DescriptiveScheme(id = "desc_fru_9", title = "Персик", category = DescriptiveCategory.FRUITS, emoji = "🍑"),
        DescriptiveScheme(id = "desc_fru_10", title = "Ананас", category = DescriptiveCategory.FRUITS, emoji = "🍍"),
        DescriptiveScheme(id = "desc_fru_11", title = "Вишня", category = DescriptiveCategory.FRUITS, emoji = "🍒"),
        DescriptiveScheme(id = "desc_fru_12", title = "Черника", category = DescriptiveCategory.FRUITS, emoji = "🫐"),
        DescriptiveScheme(id = "desc_fru_13", title = "Дыня", category = DescriptiveCategory.FRUITS, emoji = "🍈"),
        DescriptiveScheme(id = "desc_fru_14", title = "Мандарин", category = DescriptiveCategory.FRUITS, emoji = "🍊"),
        // === Животные (15) ===
        DescriptiveScheme(id = "desc_an_1", title = "Корова", category = DescriptiveCategory.ANIMALS, emoji = "🐄"),
        DescriptiveScheme(id = "desc_an_2", title = "Лошадь", category = DescriptiveCategory.ANIMALS, emoji = "🐎"),
        DescriptiveScheme(id = "desc_an_3", title = "Собака", category = DescriptiveCategory.ANIMALS, emoji = "🐕"),
        DescriptiveScheme(id = "desc_an_4", title = "Кошка", category = DescriptiveCategory.ANIMALS, emoji = "🐈"),
        DescriptiveScheme(id = "desc_an_5", title = "Кролик", category = DescriptiveCategory.ANIMALS, emoji = "🐇"),
        DescriptiveScheme(id = "desc_an_6", title = "Свинья", category = DescriptiveCategory.ANIMALS, emoji = "🐷"),
        DescriptiveScheme(id = "desc_an_7", title = "Медведь", category = DescriptiveCategory.ANIMALS, emoji = "🐻"),
        DescriptiveScheme(id = "desc_an_8", title = "Лиса", category = DescriptiveCategory.ANIMALS, emoji = "🦊"),
        DescriptiveScheme(id = "desc_an_9", title = "Волк", category = DescriptiveCategory.ANIMALS, emoji = "🐺"),
        DescriptiveScheme(id = "desc_an_10", title = "Заяц", category = DescriptiveCategory.ANIMALS, emoji = "🐰"),
        DescriptiveScheme(id = "desc_an_11", title = "Белка", category = DescriptiveCategory.ANIMALS, emoji = "🐿️"),
        DescriptiveScheme(id = "desc_an_12", title = "Ёжик", category = DescriptiveCategory.ANIMALS, emoji = "🦔"),
        DescriptiveScheme(id = "desc_an_13", title = "Панда", category = DescriptiveCategory.ANIMALS, emoji = "🐼"),
        DescriptiveScheme(id = "desc_an_14", title = "Обезьяна", category = DescriptiveCategory.ANIMALS, emoji = "🐵"),
        DescriptiveScheme(id = "desc_an_15", title = "Овца", category = DescriptiveCategory.ANIMALS, emoji = "🐑"),
        // === Птицы (10) ===
        DescriptiveScheme(id = "desc_bird_1", title = "Воробей", category = DescriptiveCategory.BIRDS, emoji = "🐦"),
        DescriptiveScheme(id = "desc_bird_2", title = "Петух", category = DescriptiveCategory.BIRDS, emoji = "🐓"),
        DescriptiveScheme(id = "desc_bird_3", title = "Утка", category = DescriptiveCategory.BIRDS, emoji = "🦆"),
        DescriptiveScheme(id = "desc_bird_4", title = "Гусь", category = DescriptiveCategory.BIRDS, emoji = "🦢"),
        DescriptiveScheme(id = "desc_bird_5", title = "Курица", category = DescriptiveCategory.BIRDS, emoji = "🐔"),
        DescriptiveScheme(id = "desc_bird_6", title = "Сова", category = DescriptiveCategory.BIRDS, emoji = "🦉"),
        DescriptiveScheme(id = "desc_bird_7", title = "Попугай", category = DescriptiveCategory.BIRDS, emoji = "🦜"),
        DescriptiveScheme(id = "desc_bird_8", title = "Дятел", category = DescriptiveCategory.BIRDS, emoji = "🐦"),
        DescriptiveScheme(id = "desc_bird_9", title = "Аист", category = DescriptiveCategory.BIRDS, emoji = "🕊️"),
        DescriptiveScheme(id = "desc_bird_10", title = "Пингвин", category = DescriptiveCategory.BIRDS, emoji = "🐧"),
        // === Игрушки (10) ===
        DescriptiveScheme(id = "desc_toy_1", title = "Мяч", category = DescriptiveCategory.TOYS, emoji = "⚽"),
        DescriptiveScheme(id = "desc_toy_2", title = "Кукла", category = DescriptiveCategory.TOYS, emoji = "🪆"),
        DescriptiveScheme(id = "desc_toy_3", title = "Машинка", category = DescriptiveCategory.TOYS, emoji = "🚗"),
        DescriptiveScheme(id = "desc_toy_4", title = "Пазл", category = DescriptiveCategory.TOYS, emoji = "🧩"),
        DescriptiveScheme(id = "desc_toy_5", title = "Пирамида", category = DescriptiveCategory.TOYS, emoji = "🎯"),
        DescriptiveScheme(id = "desc_toy_6", title = "Мишка", category = DescriptiveCategory.TOYS, emoji = "🧸"),
        DescriptiveScheme(id = "desc_toy_7", title = "Барабан", category = DescriptiveCategory.TOYS, emoji = "🥁"),
        DescriptiveScheme(id = "desc_toy_8", title = "Робот", category = DescriptiveCategory.TOYS, emoji = "🤖"),
        DescriptiveScheme(id = "desc_toy_9", title = "Юла", category = DescriptiveCategory.TOYS, emoji = "🌀"),
        DescriptiveScheme(id = "desc_toy_10", title = "Воздушный змей", category = DescriptiveCategory.TOYS, emoji = "🪁"),
        // === Транспорт (11) ===
        DescriptiveScheme(id = "desc_tr_1", title = "Машина", category = DescriptiveCategory.TRANSPORT, emoji = "🚗"),
        DescriptiveScheme(id = "desc_tr_2", title = "Самолёт", category = DescriptiveCategory.TRANSPORT, emoji = "✈️"),
        DescriptiveScheme(id = "desc_tr_3", title = "Поезд", category = DescriptiveCategory.TRANSPORT, emoji = "🚂"),
        DescriptiveScheme(id = "desc_tr_4", title = "Корабль", category = DescriptiveCategory.TRANSPORT, emoji = "🚢"),
        DescriptiveScheme(id = "desc_tr_5", title = "Автобус", category = DescriptiveCategory.TRANSPORT, emoji = "🚌"),
        DescriptiveScheme(id = "desc_tr_6", title = "Велосипед", category = DescriptiveCategory.TRANSPORT, emoji = "🚲"),
        DescriptiveScheme(id = "desc_tr_7", title = "Трактор", category = DescriptiveCategory.TRANSPORT, emoji = "🚜"),
        DescriptiveScheme(id = "desc_tr_8", title = "Вертолёт", category = DescriptiveCategory.TRANSPORT, emoji = "🚁"),
        DescriptiveScheme(id = "desc_tr_9", title = "Лодка", category = DescriptiveCategory.TRANSPORT, emoji = "⛵"),
        DescriptiveScheme(id = "desc_tr_10", title = "Ракета", category = DescriptiveCategory.TRANSPORT, emoji = "🚀"),
        DescriptiveScheme(id = "desc_tr_11", title = "Грузовик", category = DescriptiveCategory.TRANSPORT, emoji = "🚛"),
        // === Одежда (10) ===
        DescriptiveScheme(id = "desc_cl_1", title = "Кепка", category = DescriptiveCategory.CLOTHES, emoji = "🧢"),
        DescriptiveScheme(id = "desc_cl_2", title = "Куртка", category = DescriptiveCategory.CLOTHES, emoji = "🧥"),
        DescriptiveScheme(id = "desc_cl_3", title = "Шарф", category = DescriptiveCategory.CLOTHES, emoji = "🧣"),
        DescriptiveScheme(id = "desc_cl_4", title = "Рубашка", category = DescriptiveCategory.CLOTHES, emoji = "👔"),
        DescriptiveScheme(id = "desc_cl_5", title = "Платье", category = DescriptiveCategory.CLOTHES, emoji = "👗"),
        DescriptiveScheme(id = "desc_cl_6", title = "Ботинки", category = DescriptiveCategory.CLOTHES, emoji = "👢"),
        DescriptiveScheme(id = "desc_cl_7", title = "Варежки", category = DescriptiveCategory.CLOTHES, emoji = "🧤"),
        DescriptiveScheme(id = "desc_cl_8", title = "Носки", category = DescriptiveCategory.CLOTHES, emoji = "🧦"),
        DescriptiveScheme(id = "desc_cl_9", title = "Футболка", category = DescriptiveCategory.CLOTHES, emoji = "👕"),
        DescriptiveScheme(id = "desc_cl_10", title = "Кроссовки", category = DescriptiveCategory.CLOTHES, emoji = "👟")
    )

    // ===========================================================
    //                       ПУБЛИЧНЫЕ МЕТОДЫ
    // ===========================================================
    val allTexts: List<MnemoText> = autumnTexts + winterTexts + springTexts + summerTexts +
        poteshkiTexts + skazkiTexts + animalsTexts

    fun getTextsByTheme(theme: MnemoTheme): List<MnemoText> {
        return if (theme == MnemoTheme.ALL) allTexts else allTexts.filter { it.theme == theme }
    }

    fun getTextsByThemeAndAge(theme: MnemoTheme, age: MnemoAge): List<MnemoText> {
        return getTextsByTheme(theme).filter { it.age == age }
    }

    fun getRandomText(theme: MnemoTheme): MnemoText? {
        val list = getTextsByTheme(theme)
        return if (list.isEmpty()) null else list.random()
    }

    fun getTextById(id: String): MnemoText? = allTexts.find { it.id == id }

    fun getSchemesByCategory(category: DescriptiveCategory): List<DescriptiveScheme> {
        return descriptiveSchemes.filter { it.category == category }
    }

    fun getSchemeById(id: String): DescriptiveScheme? = descriptiveSchemes.find { it.id == id }

    fun getRandomScheme(category: DescriptiveCategory): DescriptiveScheme? {
        val list = getSchemesByCategory(category)
        return if (list.isEmpty()) null else list.random()
    }

    /**
     * План вопросов и иконок для каждой категории описательного рассказа.
     * Свой набор из 6 пунктов для овощей, животных, птиц, игрушек, транспорта, одежды.
     */
    fun getQuestionsForCategory(category: DescriptiveCategory): QuestionPlan {
        return when (category) {
            // Овощи и Фрукты — одинаковый план
            DescriptiveCategory.VEGETABLES,
            DescriptiveCategory.FRUITS -> QuestionPlan(
                questions = listOf(
                    "Что это?",
                    "Какого цвета?",
                    "Какой формы?",
                    "Какой на вкус?",
                    "Где растёт?",
                    "Что из него готовят?"
                ),
                icons = listOf("❓", "🎨", "🔷", "👅", "🌱", "🍽️")
            )
            DescriptiveCategory.ANIMALS -> QuestionPlan(
                questions = listOf(
                    "Кто это?",
                    "Где живёт?",
                    "Большой или маленький?",
                    "Какие у него части тела?",
                    "Чем питается?",
                    "Как зовут детёныша?"
                ),
                icons = listOf("❓", "🏠", "📏", "🐾", "🍽️", "🐣")
            )
            DescriptiveCategory.BIRDS -> QuestionPlan(
                questions = listOf(
                    "Кто это?",
                    "Какие у неё перья?",
                    "Где живёт?",
                    "Чем питается?",
                    "Где вьёт гнездо?",
                    "Улетает на зиму или остаётся?"
                ),
                icons = listOf("❓", "🎨", "🏠", "🍽️", "🪺", "✈️")
            )
            DescriptiveCategory.TOYS -> QuestionPlan(
                questions = listOf(
                    "Что это?",
                    "Какого цвета?",
                    "Какой формы?",
                    "Из чего сделана?",
                    "Как с ней играют?",
                    "Почему она тебе нравится?"
                ),
                icons = listOf("❓", "🎨", "🔷", "🪵", "🤲", "❤️")
            )
            DescriptiveCategory.TRANSPORT -> QuestionPlan(
                questions = listOf(
                    "Что это?",
                    "Где ездит: по земле, по воде или по воздуху?",
                    "Какого цвета?",
                    "Из каких частей состоит?",
                    "Кто им управляет?",
                    "Кого или что перевозит?"
                ),
                icons = listOf("❓", "🛣️", "🎨", "⚙️", "🧑‍✈️", "📦")
            )
            DescriptiveCategory.CLOTHES -> QuestionPlan(
                questions = listOf(
                    "Что это?",
                    "Какого цвета?",
                    "Из чего сшито?",
                    "В какую погоду носят?",
                    "На какую часть тела надевают?",
                    "Как за этим ухаживать?"
                ),
                icons = listOf("❓", "🎨", "🧵", "🌡️", "🧍", "🧺")
            )
        }
    }
}
