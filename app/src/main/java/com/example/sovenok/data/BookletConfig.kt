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
    val title: String? = null, // Заголовок страницы (например, "Меня зовут")
    val prompt: String? = null, // Текст загадки или подсказка
    val isCover: Boolean = false,
    val isEnd: Boolean = false,
    val showLines: Boolean = true,
    val showDrawingBox: Boolean = true
)

object BookletData {
    // Дефолтные названия обложек для каждой темы
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
                // Пока заглушка, логику отрисовки ответов сделаем на Шаге 4
                val riddles = listOf(
                    "Рыжая, с пушистым хвостом, живёт в лесу, а кур считает лучше всех.",
                    "Не котёнок, а мурлычет, любит рыбу и тепло.",
                    "Зимой лежит, весной бежит, а летом его не найдёшь.",
                    "Днём светит, греет, а ночью спать уходит.",
                    "Сам деревянный, а голова железная, гвозди забивает.",
                    "Без крыльев летит, без ног бежит, без рук рисует на стекле."
                )
                riddles.forEach { pages.add(PageContent(prompt = it, showLines = false, showDrawingBox = false)) }
            }
            BookletType.BLANK -> {
                // Чистая: без заголовков, поле рисунка будет поднято вверх (логика в PdfDrawer)
                for (i in 1..6) pages.add(PageContent(showLines = true, showDrawingBox = true))
            }
        }

        pages.add(PageContent(isEnd = true, showLines = false, showDrawingBox = true))
        return pages
    }
}
