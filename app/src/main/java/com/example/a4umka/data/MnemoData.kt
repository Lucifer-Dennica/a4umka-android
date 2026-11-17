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
        MnemoText(
            id = "autumn_1", title = "Листья жёлтые летят",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Листья жёлтые летят", "🍂"),
                MnemoLine("Под ногами шелестят", "👣"),
                MnemoLine("По небу тучки бегут", "☁️"),
                MnemoLine("Дождик тут и там стучит", "🌧️")
            )
        ),
        MnemoText(
            id = "autumn_2", title = "Осень пришла",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Пожелтел на дереве лист", "🍁"),
                MnemoLine("Ветер тихо листья носит", "💨"),
                MnemoLine("Птицы на юг улетают", "🕊️"),
                MnemoLine("Звери норы копают", "🦔")
            )
        ),
        MnemoText(
            id = "autumn_3", title = "Осенний лес",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Пошёл Миша в осенний лес", "🌲"),
                MnemoLine("Под ногами шуршат листья", "🍂"),
                MnemoLine("На пне сидит рыжая белка", "🐿️"),
                MnemoLine("Она грызёт орешек", "🌰"),
                MnemoLine("Миша помахал ей рукой", "👋"),
                MnemoLine("И побежал домой", "🏃")
            )
        ),
        MnemoText(
            id = "autumn_4", title = "Дождик осенний",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Кап-кап-кап — стучит дождь", "💧"),
                MnemoLine("По стеклу окна ведёт", "🪟"),
                MnemoLine("Лужи большие на дворе", "💦"),
                MnemoLine("Зонтик я в руках держу", "☂️"),
                MnemoLine("В сапогах по лужам шлёп", "🥾"),
                MnemoLine("До крыльца я дойду сам", "🏠")
            )
        ),
        MnemoText(
            id = "autumn_5", title = "Сбор урожая",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Пришла осень в огород", "🌾"),
                MnemoLine("Бабка тянет морковку", "🥕"),
                MnemoLine("Дед копает картошку", "🥔"),
                MnemoLine("Внучка срывает капусту", "🥬"),
                MnemoLine("Жучка лает на кролика", "🐕"),
                MnemoLine("Корзины полны — домой", "🧺")
            )
        ),
        MnemoText(
            id = "autumn_6", title = "Осень в лесу",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Ходит осень по лесам", "🌲"),
                MnemoLine("Красит листья в разные цвета", "🍁"),
                MnemoLine("Ёжик таскает яблоки", "🦔"),
                MnemoLine("Белка сушит грибочки", "🍄"),
                MnemoLine("Медведь берлогу строит", "🐻"),
                MnemoLine("Лиса в норе свернулась", "🦊"),
                MnemoLine("Зайчик шубку поменял", "🐇"),
                MnemoLine("Птицы клином улетают", "🕊️"),
                MnemoLine("Лес к зиме готовится", "❄️")
            )
        ),
        MnemoText(
            id = "autumn_7", title = "Грибы",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Рано утром в лес пошли", "🌅"),
                MnemoLine("С корзинкой в руках идём", "🧺"),
                MnemoLine("Под берёзой — боровик", "🍄"),
                MnemoLine("У пенька — лисички ряд", "🍄"),
                MnemoLine("На тропинке — мухомор", "🔴"),
                MnemoLine("Его в корзину не берём", "🚫"),
                MnemoLine("Под ёлкой прячется опёнок", "🌲"),
                MnemoLine("Наклонились — собрали", "🙌"),
                MnemoLine("Полна корзина — домой", "🏠")
            )
        ),
        MnemoText(
            id = "autumn_8", title = "Осенний день",
            theme = MnemoTheme.AUTUMN, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Утром туман на лугу", "🌫️"),
                MnemoLine("Солнце греет еле-еле", "🌅"),
                MnemoLine("Листья падают кружась", "🍂"),
                MnemoLine("Ветер гонит облака", "💨"),
                MnemoLine("Грачи на проводах сидят", "🐦"),
                MnemoLine("Паутина в воздухе", "🕸️"),
                MnemoLine("День становится короче", "🌗"),
                MnemoLine("Вечером рано темнеет", "🌆"),
                MnemoLine("Осень вступает в права", "🍁")
            )
        )
    )

    // ===========================================================
    //                          ЗИМА
    // ===========================================================
    private val winterTexts = listOf(
        MnemoText(
            id = "winter_1", title = "Снег идёт",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("С неба падают снежинки", "❄️"),
                MnemoLine("Белым станет всё вокруг", "⬜"),
                MnemoLine("Дети лепят снеговика", "⛄"),
                MnemoLine("Радостно на дворе", "😄")
            )
        ),
        MnemoText(
            id = "winter_2", title = "Зимой в лесу",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Деревья в снегу стоят", "🌲"),
                MnemoLine("Зайчик прыгает в сугроб", "🐇"),
                MnemoLine("Птички у кормушки ждут", "🐦"),
                MnemoLine("Я им зёрнышек несу", "🌾")
            )
        ),
        MnemoText(
            id = "winter_3", title = "Зимняя прогулка",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Оделся тепло и пошёл", "🧥"),
                MnemoLine("На мне шапка и шарф", "🧣"),
                MnemoLine("Варежки тёплые на руках", "🧤"),
                MnemoLine("На ногах валенки", "🥾"),
                MnemoLine("Снег хрустит под ногами", "👣"),
                MnemoLine("Свежий воздух — как хорошо", "✨")
            )
        ),
        MnemoText(
            id = "winter_4", title = "Снеговик",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Дети вышли во двор", "🚪"),
                MnemoLine("Снега навалили кучу", "⛄"),
                MnemoLine("Слепили три больших шара", "⚪"),
                MnemoLine("Поставили друг на друга", "🧱"),
                MnemoLine("Нос — морковка, глаза — уголь", "🥕"),
                MnemoLine("Ведро на голову надели", "🪣")
            )
        ),
        MnemoText(
            id = "winter_5", title = "Новый год",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_5_6, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Вот и праздник Новый год", "🎉"),
                MnemoLine("Ёлку в дом мы принесли", "🎄"),
                MnemoLine("Шарики повесили", "🎄"),
                MnemoLine("Гирлянды зажгли огнями", "✨"),
                MnemoLine("Дед Мороз идёт к нам в гости", "🎅"),
                MnemoLine("Подарки под ёлкой лежат", "🎁")
            )
        ),
        MnemoText(
            id = "winter_6", title = "Зимние забавы",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Наступила снежная зима", "❄️"),
                MnemoLine("Ребята на горку пришли", "⛷️"),
                MnemoLine("На санках катаемся вниз", "🛷"),
                MnemoLine("На коньках по льду скользим", "⛸️"),
                MnemoLine("Лепят крепость из снега", "🏰"),
                MnemoLine("Играют в снежки активно", "❄️"),
                MnemoLine("Лыжи надели — по лыжне", "🎿"),
                MnemoLine("Холодно, но весело всем", "😄"),
                MnemoLine("Домой идём за чаем греться", "☕")
            )
        ),
        MnemoText(
            id = "winter_7", title = "Как звери зимуют",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Медведь в берлоге спит", "🐻"),
                MnemoLine("Ёж под листьями свернулся", "🦔"),
                MnemoLine("Барсук в норе отдыхает", "🦡"),
                MnemoLine("Белка по веткам скачет", "🐿️"),
                MnemoLine("Заяц шубку поменял", "🐇"),
                MnemoLine("Лиса мышкует в снегу", "🦊"),
                MnemoLine("Волк стаями бродит", "🐺"),
                MnemoLine("Птицы на юг улетели", "🕊️"),
                MnemoLine("Каждый по-своему зиму ждёт", "❄️")
            )
        ),
        MnemoText(
            id = "winter_8", title = "Мороз и солнце",
            theme = MnemoTheme.WINTER, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Мороз и солнце — день чудес", "☀️"),
                MnemoLine("Снег искрится под ногами", "✨"),
                MnemoLine("Деревья в серебре стоят", "🌲"),
                MnemoLine("Сосульки на крышах висят", "🧊"),
                MnemoLine("Дым из труб столбом идёт", "🏭"),
                MnemoLine("Лёд на реке крепкий-крепкий", "🧊"),
                MnemoLine("Дети катаются на коньках", "⛸️"),
                MnemoLine("Щёки румяные от мороза", "❄️"),
                MnemoLine("Зимний день такой прекрасен", "🌟")
            )
        )
    )

    // ===========================================================
    //                          ВЕСНА
    // ===========================================================
    private val springTexts = listOf(
        MnemoText(
            id = "spring_1", title = "Весна пришла",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Снег растаял, бегут ручьи", "💧"),
                MnemoLine("Солнышко греет сильнее", "☀️"),
                MnemoLine("Появились первые цветы", "🌷"),
                MnemoLine("Птички весело поют", "🐦")
            )
        ),
        MnemoText(
            id = "spring_2", title = "Подснежники",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("В лесу под снегом — чудо", "❄️"),
                MnemoLine("Там подснежник расцвёл", "🌸"),
                MnemoLine("Беленький, маленький цветок", "🌼"),
                MnemoLine("Вестник тёплой весны", "🌤️")
            )
        ),
        MnemoText(
            id = "spring_3", title = "Весна в лесу",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Снег в лесу растаял", "💧"),
                MnemoLine("Появились проталины", "🌿"),
                MnemoLine("На деревьях набухли почки", "🌱"),
                MnemoLine("Вернулись с юга грачи", "🐦"),
                MnemoLine("Медведь проснулся в берлоге", "🐻"),
                MnemoLine("Зашевелился ёжик в норе", "🦔")
            )
        ),
        MnemoText(
            id = "spring_4", title = "Весенний сад",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_5_6, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("В саду расцвели деревья", "🌸"),
                MnemoLine("Яблоня в розовом наряде", "🍎"),
                MnemoLine("Вишня в белом кружеве", "🌸"),
                MnemoLine("Пчёлы жужжат над цветами", "🐝"),
                MnemoLine("Бабочки порхают легко", "🦋"),
                MnemoLine("Пахнет весной и теплом", "☀️")
            )
        ),
        MnemoText(
            id = "spring_5", title = "Скворцы прилетели",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Папа повесил скворечник", "🏠"),
                MnemoLine("На берёзе, высоко", "🌲"),
                MnemoLine("Через неделю прилетели скворцы", "🐦"),
                MnemoLine("Стали петь по утрам звонко", "🎶"),
                MnemoLine("Самка села высиживать птенцов", "🥚"),
                MnemoLine("Скоро вылетят маленькие", "🐤")
            )
        ),
        MnemoText(
            id = "spring_6", title = "Весеннее пробуждение",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Зима ушла, пришла весна", "🌸"),
                MnemoLine("Солнце греет всё сильней", "☀️"),
                MnemoLine("Снег растаял, мокро кругом", "💧"),
                MnemoLine("Побежали ручьи звонко", "💦"),
                MnemoLine("На деревьях почки лопнули", "🌱"),
                MnemoLine("Появилась зелёная травка", "🌿"),
                MnemoLine("Первоцветы украсили луг", "🌼"),
                MnemoLine("Птицы весёлые песни поют", "🐦"),
                MnemoLine("Природа проснулась ото сна", "🌅")
            )
        ),
        MnemoText(
            id = "spring_7", title = "Как мы огород сажали",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Наступила тёплая весна", "🌤️"),
                MnemoLine("Папа вскопал грядки", "🪏"),
                MnemoLine("Мы принесли семена", "🌱"),
                MnemoLine("Мама посадила редиску", "🔴"),
                MnemoLine("Я сажал морковку", "🥕"),
                MnemoLine("Сестра — горох и фасоль", "🫛"),
                MnemoLine("Полили всё тёплой водой", "💧"),
                MnemoLine("Поставили пугало на грядки", "🧑‍🌾"),
                MnemoLine("Ждём урожая к лету", "☀️")
            )
        ),
        MnemoText(
            id = "spring_8", title = "Весенний дождик",
            theme = MnemoTheme.SPRING, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Тучка по небу плывёт", "☁️"),
                MnemoLine("Дождик тёплый полил поля", "🌧️"),
                MnemoLine("Земля напилась воды вдоволь", "💧"),
                MnemoLine("Травка полезла зелёная", "🌱"),
                MnemoLine("Деревья умылись росой", "🌲"),
                MnemoLine("Цветы подняли головки", "🌷"),
                MnemoLine("Птицы купаются в лужах", "🐦"),
                MnemoLine("Радуга дугой на небе", "🌈"),
                MnemoLine("Снова солнышко сияет", "☀️")
            )
        )
    )

    // ===========================================================
    //                          ЛЕТО
    // ===========================================================
    private val summerTexts = listOf(
        MnemoText(
            id = "summer_1", title = "Лето красное",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_4_5, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Солнце ярко светит", "☀️"),
                MnemoLine("Травка зелёная кругом", "🌿"),
                MnemoLine("Цветочки пёстрые в полях", "🌸"),
                MnemoLine("Птички весело поют", "🐦")
            )
        ),
        MnemoText(
            id = "summer_2", title = "На реке",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_4_5, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Жаркий день, идём на речку", "🌊"),
                MnemoLine("Вода в реке прохладная", "💧"),
                MnemoLine("Купаемся и плескаемся", "🏊"),
                MnemoLine("На песке лежим загораем", "🏖️")
            )
        ),
        MnemoText(
            id = "summer_3", title = "Летний день",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_5_6, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Утром солнце встаёт рано", "🌅"),
                MnemoLine("Роса блестит на траве", "💧"),
                MnemoLine("Птицы поют на все лады", "🐦"),
                MnemoLine("Пчёлы летят на луга", "🐝"),
                MnemoLine("День длинный-длинный стоит", "☀️"),
                MnemoLine("Вечером закат золотой", "🌇")
            )
        ),
        MnemoText(
            id = "summer_4", title = "На даче",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Летом мы живём на даче", "🏡"),
                MnemoLine("У нас большой огород", "🌱"),
                MnemoLine("Поливаем грядки водой", "💧"),
                MnemoLine("Помидоры краснеют на солнце", "🍅"),
                MnemoLine("Огурцы висят на плети", "🥒"),
                MnemoLine("Клубника сладкая в саду", "🍓")
            )
        ),
        MnemoText(
            id = "summer_5", title = "Лесные ягоды",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_5_6, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Пошли в лес за ягодами", "🧺"),
                MnemoLine("На полянке земляника", "🍓"),
                MnemoLine("Под кустами черника", "🫐"),
                MnemoLine("На кочках морошка", "🍊"),
                MnemoLine("Малина в малиннике зреет", "🟥"),
                MnemoLine("Корзина полная — домой", "🏠")
            )
        ),
        MnemoText(
            id = "summer_6", title = "Жаркий полдень",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Солнце в зените палит", "☀️"),
                MnemoLine("Воздух дрожит от жары", "🔥"),
                MnemoLine("Трава пожелтела на лугу", "🌾"),
                MnemoLine("Кузнечики стрекочут в траве", "🦗"),
                MnemoLine("Птицы спрятались в тень", "🌳"),
                MnemoLine("Пчёлы в ульях жужжат", "🐝"),
                MnemoLine("Собака лежит в тени", "🐕"),
                MnemoLine("Люди в реке спасаются", "🌊"),
                MnemoLine("Ждём вечерней прохлады", "🌆")
            )
        ),
        MnemoText(
            id = "summer_7", title = "Летний луг",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_6_7, type = MnemoType.STORY,
            lines = listOf(
                MnemoLine("Вышли мы на луг летним днём", "🌞"),
                MnemoLine("Трава на нём высокая", "🌾"),
                MnemoLine("Цветы разные-разноцветные", "🌼"),
                MnemoLine("Бабочки порхают над цветами", "🦋"),
                MnemoLine("Пчёлы жужжат, собирают нектар", "🐝"),
                MnemoLine("Кузнечики скачут под ногами", "🦗"),
                MnemoLine("В траве прячется божья коровка", "🐞"),
                MnemoLine("Стрекоза на стебле отдыхает", "🟦"),
                MnemoLine("А вдали звенит ручей", "💧")
            )
        ),
        MnemoText(
            id = "summer_8", title = "Летний вечер",
            theme = MnemoTheme.SUMMER, age = MnemoAge.AGE_6_7, type = MnemoType.POEM,
            lines = listOf(
                MnemoLine("Солнце садится за лес", "🌇"),
                MnemoLine("Небо окрасилось в розовое", "🌸"),
                MnemoLine("Тени на земле удлинились", "🌑"),
                MnemoLine("Птицы стихли в гнёздах", "🐦"),
                MnemoLine("Светлячки зажглись в траве", "✨"),
                MnemoLine("Звёзды на небе замигали", "⭐"),
                MnemoLine("Луна выплыла на небо", "🌙"),
                MnemoLine("Кузнечики затянули песню", "🦗"),
                MnemoLine("Тихий летний вечер настал", "🌆")
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
