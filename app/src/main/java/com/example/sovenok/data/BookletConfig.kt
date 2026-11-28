package com.example.sovenok.data

enum class BookletType { STORY, ABOUT_ME, FAMILY, GRANDMA, GRANDPA, RIDDLES, BLANK }
enum class BookletRuling { LINE_8MM, WIDE_10MM, GRID_5MM, NONE }
enum class BookletCharacter { NONE, BOY, GIRL }

data class BookletConfig(
    val type: BookletType = BookletType.STORY,
    val title: String = "Моя сказка",
    val author: String = "",
    val character: BookletCharacter = BookletCharacter.GIRL,
    val ruling: BookletRuling = BookletRuling.LINE_8MM,
    val showFoldLines: Boolean = true,
    val isBlackAndWhite: Boolean = false
)

data class PageContent(
    val title: String? = null,
    val prompt: String? = null,
    val previousAnswer: String? = null, // Ответ на предыдущую загадку (рисуется серым)
    val endAnswer: String? = null, // Ответ на последнюю загадку (для страницы "Конец")
    val isCover: Boolean = false,
    val isEnd: Boolean = false,
    val showLines: Boolean = true,
    val showDrawingBox: Boolean = true
)

object BookletData {
    val defaultTitles = mapOf(
        BookletType.STORY to "Моя сказка",
        BookletType.ABOUT_ME to "Всё обо мне",
        BookletType.FAMILY to "Моя семья",
        BookletType.GRANDMA to "Для бабушки",
        BookletType.GRANDPA to "Подарок дедушке",
        BookletType.RIDDLES to "Загадки",
        BookletType.BLANK to "Моя книжка"
    )

    fun getPages(config: BookletConfig): List<PageContent> {
        val pages = mutableListOf<PageContent>()
        pages.add(PageContent(isCover = true, showLines = false, showDrawingBox = true))

        when (config.type) {
            BookletType.STORY -> {
                for (i in 1..6) pages.add(PageContent(showLines = true, showDrawingBox = true))
            }
            BookletType.ABOUT_ME -> {
                val titles = listOf("Меня зовут", "Моя семья", "Что я люблю есть", "Мой день", "Моя любимая игра", "Кем я стану")
                titles.forEach { pages.add(PageContent(title = it, showLines = true, showDrawingBox = true)) }
            }
            BookletType.FAMILY -> {
                val titles = listOf("Это я", "Моя мама", "Мой папа", "Вместе мы любим", "Наше любимое место", "За что я люблю семью")
                titles.forEach { pages.add(PageContent(title = it, showLines = true, showDrawingBox = true)) }
            }
            BookletType.GRANDMA -> {
                val titles = listOf("Моя бабушка", "За что я её люблю", "Мы вместе играем", "Её любимое блюдо", "Мой подарок", "Я желаю бабушке")
                titles.forEach { pages.add(PageContent(title = it, showLines = true, showDrawingBox = true)) }
            }
            BookletType.GRANDPA -> {
                val titles = listOf("Мой дедушка", "За что я его люблю", "Мы вместе играем", "Его любимое блюдо", "Мой подарок", "Я желаю дедушке")
                titles.forEach { pages.add(PageContent(title = it, showLines = true, showDrawingBox = true)) }
            }
            BookletType.RIDDLES -> {
                val allRiddles = listOf(
                    "Рыжая, с пушистым хвостом, живёт в лесу, а кур считает лучше всех." to "лиса",
                    "Не котёнок, а мурлычет, любит рыбу и тепло." to "кот",
                    "Зимой лежит, весной бежит, а летом его не найдёшь." to "снег",
                    "Днём светит, греет, а ночью спать уходит." to "солнце",
                    "Сам деревянный, а голова железная, гвозди забивает." to "молоток",
                    "Без крыльев летит, без ног бежит, без рук рисует на стекле." to "мороз",
                    "Не портной, а всю жизнь с иголками ходит." to "ёж",
                    "Зимой спит, летом ульи ворошит." to "медведь",
                    "Хвост пушистый, мех золотистый." to "лиса",
                    "Кто зимой холодной ходит злой, голодный?" to "волк",
                    "Белый, да не сахар, ног нет, да идёт." to "снег",
                    "Красная девица сидит в темнице, а коса на улице." to "морковь",
                    "Стоит в лесу кудряшка — белая рубашка, сердечко золотое." to "ромашка",
                    "Сидит дед во сто шуб одет." to "лук",
                    "Висит груша — нельзя скушать." to "лампочка"
                )
                val selected = allRiddles.shuffled().take(6)
                
                for (i in selected.indices) {
                    if (i == 0) {
                        pages.add(PageContent(prompt = selected[i].first, showLines = false, showDrawingBox = false))
                    } else {
                        pages.add(PageContent(
                            prompt = selected[i].first,
                            previousAnswer = selected[i - 1].second,
                            showLines = false,
                            showDrawingBox = false
                        ))
                    }
                }
                // Последняя страница "Конец" будет иметь ответ на 6-ю загадку
                pages.add(PageContent(isEnd = true, showLines = false, showDrawingBox = true, endAnswer = selected.last().second))
                return pages
            }
            BookletType.BLANK -> {
                for (i in 1..6) pages.add(PageContent(showLines = true, showDrawingBox = true))
            }
        }

        pages.add(PageContent(isEnd = true, showLines = false, showDrawingBox = true))
        return pages
    }
}
