package com.example.a4umka.data

enum class BookletType { STORY, ABOUT_ME, FAMILY, GRANDMA, RIDDLES, BLANK }
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
    val isCover: Boolean = false,
    val isEnd: Boolean = false,
    val showLines: Boolean = true,
    val showDrawingBox: Boolean = true
)

object BookletData {
    fun getPages(config: BookletConfig): List<PageContent> {
        val pages = mutableListOf<PageContent>()
        pages.add(PageContent(isCover = true, showLines = false, showDrawingBox = true))

        when (config.type) {
            BookletType.STORY, BookletType.BLANK -> {
                for (i in 1..6) pages.add(PageContent(showLines = true, showDrawingBox = true))
            }
            BookletType.ABOUT_ME -> {
                val prompts = listOf("Меня зовут...", "Моя семья...", "Мой день...", "Мои друзья...", "Мои любимые игры...", "Что я умею...")
                prompts.forEach { pages.add(PageContent(prompt = it)) }
            }
            BookletType.FAMILY -> {
                val prompts = listOf("Моя мама...", "Мой папа...", "Мои бабушка и дедушка...", "Мы вместе...", "Наш дом...", "Наши традиции...")
                prompts.forEach { pages.add(PageContent(prompt = it)) }
            }
            BookletType.GRANDMA -> {
                val prompts = listOf("Дорогая бабушка...", "Я помню, как мы...", "Я люблю, когда ты...", "Спасибо тебе за...", "Мое пожелание...", "Твой подарок...")
                prompts.forEach { pages.add(PageContent(prompt = it)) }
            }
            BookletType.RIDDLES -> {
                val riddles = listOf(
                    "Не портной, а всю жизнь с иголками ходит. (Ёж)" to "Ёж",
                    "Зимой спит, летом ульи ворошит. (Медведь)" to "Медведь",
                    "Хвост пушистый, мех золотистый. (Лиса)" to "Лиса",
                    "Кто зимой холодной ходит злой, голодный? (Волк)" to "Волк",
                    "Белый, да не сахар, ног нет, да идёт. (Снег)" to "Снег",
                    "Красная девица сидит в темнице, а коса на улице. (Морковь)" to "Морковь"
                )
                for (i in 0 until 3) {
                    pages.add(PageContent(prompt = riddles[i].first, showLines = false, showDrawingBox = false))
                    pages.add(PageContent(title = "Отгадка: ${riddles[i].second}", showLines = false, showDrawingBox = true))
                }
            }
        }
        pages.add(PageContent(isEnd = true, showLines = false, showDrawingBox = true))
        return pages
    }
}
