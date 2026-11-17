package com.example.a4umka.data

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
        MnemoText(
            id = "poteshki_1", title = "Как у нашего кота",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Как у нашего кота", "🐱"),
                MnemoLine("Шубка очень хороша", "🧥"),
                MnemoLine("Как у котика усы", "🐈"),
                MnemoLine("Красивой всей красы", "✨")
            )
        ),
        MnemoText(
            id = "poteshki_2", title = "Солнышко-вёдрышко",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Солнышко-вёдрышко", "☀️"),
                MnemoLine("Выгляни в окошечко", "🪟"),
                MnemoLine("Твои детки плачут", "😢"),
                MnemoLine("По камушкам скачут", "🪨")
            )
        ),
        MnemoText(
            id = "poteshki_3", title = "Ладушки, ладушки",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Ладушки, ладушки", "👏"),
                MnemoLine("Где были — у бабушки", "👵"),
                MnemoLine("Испекла нам оладушки", "🥞"),
                MnemoLine("Вкусные да пышные", "😋")
            )
        ),
        MnemoText(
            id = "poteshki_4", title = "Водичка-водичка",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_4_5, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Водичка-водичка", "💧"),
                MnemoLine("Умой моё личико", "👶"),
                MnemoLine("Чтобы глазоньки блестели", "✨"),
                MnemoLine("Чтобы щёчки краснели", "😊")
            )
        ),
        MnemoText(
            id = "poteshki_5", title = "Сорока-белобока",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Сорока-белобока", "🐦"),
                MnemoLine("Кашу варила", "🥘"),
                MnemoLine("Деток кормила", "🍼"),
                MnemoLine("Этому дала", "👦"),
                MnemoLine("Этому дала", "👧"),
                MnemoLine("А этому не дала", "😢")
            )
        ),
        MnemoText(
            id = "poteshki_6", title = "Идёт коза рогатая",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Идёт коза рогатая", "🐐"),
                MnemoLine("За малыми ребятами", "👶"),
                MnemoLine("Ножками топ-топ", "👣"),
                MnemoLine("Глазками хлоп-хлоп", "👀"),
                MnemoLine("Кто кашу не ест", "🥣"),
                MnemoLine("Того забодаю", "💢")
            )
        ),
        MnemoText(
            id = "poteshki_7", title = "Петушок, петушок",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Петушок, петушок", "🐓"),
                MnemoLine("Золотой гребешок", "👑"),
                MnemoLine("Масляна головушка", "💛"),
                MnemoLine("Шёлкова бородушка", "🧵"),
                MnemoLine("Что ты рано встаёшь", "🌅"),
                MnemoLine("Голосисто поёшь", "🎶")
            )
        ),
        MnemoText(
            id = "poteshki_8", title = "Огуречик, огуречик",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_5_6, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Огуречик, огуречик", "🥒"),
                MnemoLine("Не ходи на тот конечик", "🚫"),
                MnemoLine("Там мышка живёт", "🐭"),
                MnemoLine("Твой хвостик укуси́т", "🪤"),
                MnemoLine("Хвостик у неё остёр", "⚔️"),
                MnemoLine("Прячься в огород, дружок", "🌱")
            )
        ),
        MnemoText(
            id = "poteshki_9", title = "Наша Маша маленька",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_6_7, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Наша Маша маленька", "👧"),
                MnemoLine("На ней шубка аленька", "🧥"),
                MnemoLine("Опушка бобровая", "🟤"),
                MnemoLine("Маша чёрнобровая", "👁️"),
                MnemoLine("Ждёт у окошка подружек", "🪟"),
                MnemoLine("Чтоб играть с ней в игрушки", "🧸"),
                MnemoLine("Покатать на качелях", "🎠"),
                MnemoLine("Угостить блинами свежими", "🥞"),
                MnemoLine("И чаём с малиной сладким", "🍵")
            )
        ),
        MnemoText(
            id = "poteshki_10", title = "Тили-тили-тили-бом",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_6_7, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Тили-тили-тили-бом", "🔔"),
                MnemoLine("Сбил сосну зайчишка лбом", "🌲"),
                MnemoLine("Жалко бедного зайчишку", "🐰"),
                MnemoLine("Нос вспух, как у мышки", "🐭"),
                MnemoLine("Прискакал к нему медведь", "🐻"),
                MnemoLine("Стал зайчишку пожалеть", "🤗"),
                MnemoLine("Лёд к носу приложил", "🧊"),
                MnemoLine("И в сугроб положил", "❄️"),
                MnemoLine("Зайчик спит и нос сопит", "😴")
            )
        ),
        MnemoText(
            id = "poteshki_11", title = "Заиньки, заиньки",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_6_7, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Заиньки, заиньки", "🐰"),
                MnemoLine("Прибежали в садочек", "🌳"),
                MnemoLine("Стали грызть капустку", "🥬"),
                MnemoLine("Сладкую и хрусткую", "🥗"),
                MnemoLine("Прибежал хозяин в сад", "👨‍🌾"),
                MnemoLine("Стал зайчишек прогонять", "🚫"),
                MnemoLine("Палочкой стучит-постукивает", "🪵"),
                MnemoLine("Зайцы в лес пустились наутёк", "🏃"),
                MnemoLine("С тех пор в саду их и не видели", "🌲")
            )
        ),
        MnemoText(
            id = "poteshki_12", title = "Дождик, дождик, пуще",
            theme = MnemoTheme.POTESHKI, age = MnemoAge.AGE_6_7, type = MnemoType.POTESHKA,
            lines = listOf(
                MnemoLine("Дождик, дождик, пуще", "🌧️"),
                MnemoLine("Дам тебе я гущи", "🥣"),
                MnemoLine("Дам тебе ложку", "🥄"),
                MnemoLine("Хлебай понемножку", "🍞"),
                MnemoLine("Дождик, дождик, полно лить", "☔"),
                MnemoLine("Малых детушек мочить", "👶"),
                MnemoLine("Хватит по небу гулять", "☁️"),
                MnemoLine("Хватит землю поливать", "🌱"),
                MnemoLine("Выгляни скорее солнышко", "☀️")
            )
        )
    )

    // ===========================================================
    //                          СКАЗКИ
    // ===========================================================
    private val skazkiTexts = listOf(
        MnemoText(
            id = "skazki_1", title = "Курочка Ряба",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_4_5, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Жили-были дед да баба", "👴👵"),
                MnemoLine("И была у них курочка Ряба", "🐔"),
                MnemoLine("Снесла яичко не простое — золотое", "🥚"),
                MnemoLine("Дед бил-бил — не разбил", "🔨")
            )
        ),
        MnemoText(
            id = "skazki_2", title = "Колобок",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_6_7, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Испекла бабка колобок", "🍞"),
                MnemoLine("Положила на окно студить", "🪟"),
                MnemoLine("Колобок покатился по дорожке", "🛣️"),
                MnemoLine("Встретил зайца — спел песенку", "🐰"),
                MnemoLine("Встретил волка — обманул", "🐺"),
                MnemoLine("Встретил медведя — удрал", "🐻"),
                MnemoLine("Встретил лису — поверил ей", "🦊"),
                MnemoLine("Лиса его и съела", "👅"),
                MnemoLine("Вот и сказке конец", "📖")
            )
        ),
        MnemoText(
            id = "skazki_3", title = "Репка",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Посадил дед репку", "🌱"),
                MnemoLine("Выросла репка большая-пребольшая", "🥬"),
                MnemoLine("Дед тянет-потянет — вытянуть не может", "👴"),
                MnemoLine("Позвал дед бабку", "👵"),
                MnemoLine("Бабка за дедку, дедка за репку", "🤝"),
                MnemoLine("Тянут-потянут — вытянуть не могут", "💪")
            )
        ),
        MnemoText(
            id = "skazki_4", title = "Теремок",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Стоит в поле теремок-теремок", "🏠"),
                MnemoLine("Мышка-норушка в нём живёт", "🐭"),
                MnemoLine("Лягушка-квакушка пришла", "🐸"),
                MnemoLine("Зайчик-побегайчик прибежал", "🐰"),
                MnemoLine("Лисичка-сестричка пришла", "🦊"),
                MnemoLine("Волчок-серый бочок явился", "🐺")
            )
        ),
        MnemoText(
            id = "skazki_5", title = "Маша и медведь",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Пошла Маша в лес с подругами", "👧🌲"),
                MnemoLine("Заблудилась — в дом к медведю", "🐻"),
                MnemoLine("Медведь не пускает её домой", "🚫"),
                MnemoLine("Маша попросила короб", "📦"),
                MnemoLine("Села в короб, пирожки сверху", "🥧"),
                MnemoLine("Медведь нёс короб к деду с бабой", "🏠")
            )
        ),
        MnemoText(
            id = "skazki_6", title = "Гуси-лебеди",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Отец с матерью ушли на работу", "👨‍🌾👩‍🌾"),
                MnemoLine("А гуси-лебеди унесли братца", "🦢"),
                MnemoLine("Девочка побежала искать", "🏃‍♀️"),
                MnemoLine("Печка сказала — съешь пирожок", "🍞"),
                MnemoLine("Яблоня — стряси яблочко", "🍎"),
                MnemoLine("Речка — выпей молочка", "🥛")
            )
        ),
        MnemoText(
            id = "skazki_7", title = "Заюшкина избушка",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_6_7, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Жили-были лиса да заяц", "🦊🐰"),
                MnemoLine("У лисы избушка ледяная", "🧊"),
                MnemoLine("У зайца — лубяная, тёплая", "🪵"),
                MnemoLine("Весной лисья растаяла", "💧"),
                MnemoLine("Лиса попросилась к зайцу", "🚪"),
                MnemoLine("Да и выгнала его вон", "🦊"),
                MnemoLine("Заяц плачет, петух帮他", "🐓"),
                MnemoLine("Петух прогнал лису прочь", "⚔️"),
                MnemoLine("Заяц снова дома живёт", "🏠")
            )
        ),
        MnemoText(
            id = "skazki_8", title = "Волк и семеро козлят",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_6_7, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Жила-была коза с козлятами", "🐐"),
                MnemoLine("Уходила в лес траву щипать", "🌲"),
                MnemoLine("Козлятам наказывала дверь не открывать", "🚪"),
                MnemoLine("Волк подслушал — пришёл в дом", "🐺"),
                MnemoLine("Пел тонким голосом", "🎶"),
                MnemoLine("Козлята дверь отворили", "🔓"),
                MnemoLine("Волк их всех проглотал", "😱"),
                MnemoLine("Коза вернулась, спасла их", "💪"),
                MnemoLine("С тех пор волк им не страшен", "✨")
            )
        ),
        MnemoText(
            id = "skazki_9", title = "Лисичка со скалочкой",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_6_7, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Шла лисичка по дорожке", "🦊"),
                MnemoLine("Нашла скалочку на тропинке", "🥖"),
                MnemoLine("Постучалась в первый дом — пустили", "🚪"),
                MnemoLine("Скалочку — за петушка", "🐓"),
                MnemoLine("Петушка — за гусёночка", "🪿"),
                MnemoLine("Гусёночка — за индюшонка", "🦃"),
                MnemoLine("Индюшонка — за шубейку", "🧥"),
                MnemoLine("Хитрая лисичка всех обменяла", "✨"),
                MnemoLine("Богаче всех в лесу стала", "💰")
            )
        ),
        MnemoText(
            id = "skazki_10", title = "Зимовье зверей",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_6_7, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Собрались звери в лес зимовать", "🌲"),
                MnemoLine("Бык — брёвна таскал", "🐂"),
                MnemoLine("Баран — шерстью утеплял", "🐑"),
                MnemoLine("Свинья — землю рыла", "🐷"),
                MnemoLine("Кот — крышу крыл", "🐈"),
                MnemoLine("Петух — на страже стоял", "🐓"),
                MnemoLine("Волк и медведь хотели их съесть", "🐺🐻"),
                MnemoLine("Звери их испугали пустой башкой", "💀"),
                MnemoLine("С тех пор хищники их не трогают", "🚫")
            )
        ),
        MnemoText(
            id = "skazki_11", title = "Лиса и журавль",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Подружились лиса с журавлём", "🦊🦩"),
                MnemoLine("Лиса зовёт журавля в гости", "🏠"),
                MnemoLine("Кашу на тарелку размазала", "🥞"),
                MnemoLine("Журавль клювом — ничего не может", "🚫"),
                MnemoLine("Журавль зовёт лису в гости", "🏠"),
                MnemoLine("Окрошку в кувшин налил", "🫙")
            )
        ),
        MnemoText(
            id = "skazki_12", title = "Петушок и бобовое зёрнышко",
            theme = MnemoTheme.SKAZKI, age = MnemoAge.AGE_5_6, type = MnemoType.TALE,
            lines = listOf(
                MnemoLine("Петушок клевал бобовое зерно", "🐓"),
                MnemoLine("Подавился им — заболел", "😢"),
                MnemoLine("Курочка побежала за водой", "🐔"),
                MnemoLine("Вода просила у берёзы листьев", "🌳"),
                MnemoLine("Берёза — у коровы масла", "🐄"),
                MnemoLine("Корова — у косцов травы", "🌾")
            )
        )
    )

    // ===========================================================
    //                       ПРО ЖИВОТНЫХ
    // ===========================================================
    private val animalsTexts = listOf(
        MnemoText(
            id = "animals_1", title = "Котик",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_4_5, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("У меня живёт котик", "🐱"),
                MnemoLine("Шёрстка у него мягкая", "🧶"),
                MnemoLine("Глазки зелёные, как изумруды", "💚"),
                MnemoLine("Любит молоко лакать", "🥛")
            )
        ),
        MnemoText(
            id = "animals_2", title = "Собачка",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_4_5, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Гав-гав! Кто это?", "🐕"),
                MnemoLine("Это моя собачка Шарик", "🐶"),
                MnemoLine("Он любит косточку грызть", "🦴"),
                MnemoLine("И громко лает на чужих", "🔊")
            )
        ),
        MnemoText(
            id = "animals_3", title = "Лошадка",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_4_5, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Иго-го! Скачет лошадка", "🐎"),
                MnemoLine("У неё длинная грива", "💇"),
                MnemoLine("И пышный хвост до земли", "🐴"),
                MnemoLine("Любит овёс жевать", "🌾")
            )
        ),
        MnemoText(
            id = "animals_4", title = "Корова",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Му-у-у! Мычит корова", "🐄"),
                MnemoLine("У неё рога на голове", "🐂"),
                MnemoLine("Хвост длинный, мух отгоняет", "🪰"),
                MnemoLine("Травку на лугу щиплет", "🌿"),
                MnemoLine("Даёт нам вкусное молоко", "🥛"),
                MnemoLine("Из молока делают сыр и масло", "🧀")
            )
        ),
        MnemoText(
            id = "animals_5", title = "Ёжик",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("В лесу живёт колючий ёжик", "🦔"),
                MnemoLine("Спинка вся в иголках острых", "📌"),
                MnemoLine("Лапки маленькие, но быстрые", "🐾"),
                MnemoLine("Носиком он ищет жучков", "🐛"),
                MnemoLine("Любит яблоки и грибы носить", "🍎"),
                MnemoLine("Зимой он крепко спит в норе", "😴")
            )
        ),
        MnemoText(
            id = "animals_6", title = "Зайчик",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Прыг-прыг! Кто это?", "🐰"),
                MnemoLine("Это серый зайчик в поле", "🐇"),
                MnemoLine("Уши у него длинные-длинные", "👂"),
                MnemoLine("Хвостик короткий белый", "🤍"),
                MnemoLine("Летом шубка серая, зимой белая", "❄️"),
                MnemoLine("Любит капусту и морковку", "🥕")
            )
        ),
        MnemoText(
            id = "animals_7", title = "Лиса",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("В лесу живёт хитрая лисичка", "🦊"),
                MnemoLine("Шубка у неё рыжая-прерыжая", "🟧"),
                MnemoLine("Хвост пушистый, как у огня", "🔥"),
                MnemoLine("Лапки чёрные, как угольки", "🦶"),
                MnemoLine("Она мышкует — мышей ловит", "🐭"),
                MnemoLine("В норе живёт с лисятами", "🏡")
            )
        ),
        MnemoText(
            id = "animals_8", title = "Медведь",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("В лесу живёт большой медведь", "🐻"),
                MnemoLine("Шуба бурая, тёплая", "🟫"),
                MnemoLine("Лапы сильные с когтями", "🐾"),
                MnemoLine("Любит малину с кустов рвать", "🟥"),
                MnemoLine("И мёд дикий из дупла есть", "🍯"),
                MnemoLine("Рыбу в реке лапами ловит", "🐟"),
                MnemoLine("К осени жир нагуливает", "💪"),
                MnemoLine("И спит всю зиму в берлоге", "😴"),
                MnemoLine("Весной выходит с медвежатами", "🐻‍❄️")
            )
        ),
        MnemoText(
            id = "animals_9", title = "Волк",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Серый волк по лесу бродит", "🐺"),
                MnemoLine("Глаза жёлтые, злые-презлые", "👁️"),
                MnemoLine("Шерсть серая, лохматая", "🩶"),
                MnemoLine("Зубы острые, как иголки", "🦷"),
                MnemoLine("Воет по ночам на луну", "🌕"),
                MnemoLine("Живёт в логове с волчатами", "🏚️"),
                MnemoLine("Стаями ходит, добычу ищет", "🐗"),
                MnemoLine("Может долго бежать без устали", "🏃"),
                MnemoLine("Но людей он сам не трогает", "🚫")
            )
        ),
        MnemoText(
            id = "animals_10", title = "Белка",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("По деревьям скачет рыжая белка", "🐿️"),
                MnemoLine("Шёрстка гладкая, огненная", "🟧"),
                MnemoLine("Хвост большой, как зонтик", "🍂"),
                MnemoLine("На ушках кисточки торчат", "👂"),
                MnemoLine("Лапки цепкие, с коготками", "🐾"),
                MnemoLine("Грызёт орешки и шишки", "🌰"),
                MnemoLine("Делает запасы на зиму", "🍄"),
                MnemoLine("В дупле живёт, как в домике", "🌳"),
                MnemoLine("Прыгает с ветки на ветку ловко", "🌿")
            )
        ),
        MnemoText(
            id = "animals_11", title = "Лягушка",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("У пруда живёт зелёная лягушка", "🐸"),
                MnemoLine("Квакает громко по вечерам", "🔊"),
                MnemoLine("Глаза выпуклые, блестящие", "👁️"),
                MnemoLine("Кожа гладкая, скользкая", "🟢"),
                MnemoLine("Лапки сильные, для прыжков", "🦵"),
                MnemoLine("Питается мухами и комарами", "🦟"),
                MnemoLine("Язык длинный, липкий", "👅"),
                MnemoLine("Зимой спит подо льдом на дне", "❄️"),
                MnemoLine("Весной откладывает икру в воду", "🥚")
            )
        ),
        MnemoText(
            id = "animals_12", title = "Птички зимой",
            theme = MnemoTheme.ANIMALS, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Наступила холодная зима", "❄️"),
                MnemoLine("Снегом завалило все кусты", "🌲"),
                MnemoLine("Синички прилетели к кормушке", "🐦"),
                MnemoLine("Воробьи стайками чирикают", "🐦"),
                MnemoLine("Снегири на рябине сидят", "🎄"),
                MnemoLine("Дятел стучит по стволу", "🪵"),
                MnemoLine("Сорока стрекочет на заборе", "🦜"),
                MnemoLine("Ворона каркает на берёзе", "🦅"),
                MnemoLine("Мы им зёрнышек насыпали", "🌾")
            )
        )
    )

    // ===========================================================
    //          ОПИСАТЕЛЬНЫЕ СХЕМЫ (2 на категорию × 6 = 12)
    // ===========================================================
    val descriptiveSchemes = listOf(
        // Овощи
        DescriptiveScheme(id = "desc_veg_1", title = "Помидор", category = DescriptiveCategory.VEGETABLES, emoji = "🍅"),
        DescriptiveScheme(id = "desc_veg_2", title = "Огурец", category = DescriptiveCategory.VEGETABLES, emoji = "🥒"),
        // Фрукты
        DescriptiveScheme(id = "desc_fru_1", title = "Яблоко", category = DescriptiveCategory.FRUITS, emoji = "🍎"),
        DescriptiveScheme(id = "desc_fru_2", title = "Груша", category = DescriptiveCategory.FRUITS, emoji = "🍐"),
        // Животные
        DescriptiveScheme(id = "desc_an_1", title = "Корова", category = DescriptiveCategory.ANIMALS, emoji = "🐄"),
        DescriptiveScheme(id = "desc_an_2", title = "Лошадь", category = DescriptiveCategory.ANIMALS, emoji = "🐎"),
        // Птицы
        DescriptiveScheme(id = "desc_bird_1", title = "Воробей", category = DescriptiveCategory.BIRDS, emoji = "🐦"),
        DescriptiveScheme(id = "desc_bird_2", title = "Петух", category = DescriptiveCategory.BIRDS, emoji = "🐓"),
        // Игрушки
        DescriptiveScheme(id = "desc_toy_1", title = "Мяч", category = DescriptiveCategory.TOYS, emoji = "⚽"),
        DescriptiveScheme(id = "desc_toy_2", title = "Кукла", category = DescriptiveCategory.TOYS, emoji = "🧸"),
        // Транспорт
        DescriptiveScheme(id = "desc_tr_1", title = "Машина", category = DescriptiveCategory.TRANSPORT, emoji = "🚗"),
        DescriptiveScheme(id = "desc_tr_2", title = "Самолёт", category = DescriptiveCategory.TRANSPORT, emoji = "✈️"),
        // Одежда
        DescriptiveScheme(id = "desc_cl_1", title = "Шапка", category = DescriptiveCategory.CLOTHES, emoji = "🧢"),
        DescriptiveScheme(id = "desc_cl_2", title = "Куртка", category = DescriptiveCategory.CLOTHES, emoji = "🧥")
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
}
