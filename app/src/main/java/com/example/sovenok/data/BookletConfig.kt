package com.example.sovenok.data

import kotlin.random.Random

enum class BookletType { STORY, ABOUT_ME, FAMILY, GRANDMA, GRANDPA, FRIENDS, DREAMS, RIDDLES, BLANK }
enum class BookletRuling { LINE_8MM, WIDE_10MM, GRID_5MM, NONE }
enum class BookletCharacter { NONE, BOY, GIRL }
enum class BookletFont { SYSTEM, CAVEAT }

data class BookletConfig(
    val type: BookletType = BookletType.STORY,
    val title: String = "Моя сказка",
    val author: String = "",
    val character: BookletCharacter = BookletCharacter.GIRL,
    val ruling: BookletRuling = BookletRuling.LINE_8MM,
    val showFoldLines: Boolean = true,
    val isBlackAndWhite: Boolean = false,
    val showDrawing: Boolean = true,
    val drawingOffPages: String = "",
    val titleFont: BookletFont = BookletFont.SYSTEM,
    val riddleSeed: Int = 0,
    val customTitles: List<String> = emptyList(),
    val customRiddles: List<Pair<String, String>> = emptyList()
)

data class PageContent(
    val title: String? = null,
    val prompt: String? = null,
    val previousAnswer: String? = null,
    val endAnswer: String? = null,
    val riddleAnswers: List<String>? = null,
    val isCover: Boolean = false,
    val isEnd: Boolean = false,
    val showLines: Boolean = true,
    val showDrawingBox: Boolean = true,
    val fullPageLines: Boolean = false
)

object BookletData {
    val defaultTitles = mapOf(
        BookletType.STORY to "Моя сказка",
        BookletType.ABOUT_ME to "Всё обо мне",
        BookletType.FAMILY to "Моя семья",
        BookletType.GRANDMA to "Для бабушки",
        BookletType.GRANDPA to "Подарок дедушке",
        BookletType.FRIENDS to "Мои друзья",
        BookletType.DREAMS to "Мои мечты",
        BookletType.RIDDLES to "Загадки",
        BookletType.BLANK to "Моя книжка"
    )

    private fun applyDrawingRules(pages: MutableList<PageContent>, config: BookletConfig): List<PageContent> {
        val offPages = config.drawingOffPages
            .split(",", ";", " ")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
        for (idx in pages.indices) {
            val p = pages[idx]
            if (p.isCover || p.isEnd) continue
            val pageNum = idx + 1
            val mustHide = !config.showDrawing || (pageNum in offPages && p.showDrawingBox)
            if (mustHide && p.showDrawingBox) {
                pages[idx] = p.copy(showDrawingBox = false, fullPageLines = true)
            }
        }
        return pages
    }

    fun getPages(config: BookletConfig): List<PageContent> {
        val pages = mutableListOf<PageContent>()
        pages.add(PageContent(isCover = true, showLines = false, showDrawingBox = true))

        // Если есть свои заголовки — используем их для STORY
        if (config.type == BookletType.STORY && config.customTitles.isNotEmpty()) {
            config.customTitles.take(6).forEach { title ->
                pages.add(PageContent(title = title, showLines = true, showDrawingBox = true))
            }
            // Дополняем до 6 страниц если своих заголовков меньше
            val remaining = 6 - config.customTitles.size.coerceAtMost(6)
            for (i in 1..remaining) {
                pages.add(PageContent(showLines = true, showDrawingBox = true))
            }
        } else {
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
                BookletType.FRIENDS -> {
                    val titles = listOf("Мой лучший друг", "Как мы познакомились", "Во что мы играем", "Что любим вместе", "Я хочу ему пожелать", "Наш секрет")
                    titles.forEach { pages.add(PageContent(title = it, showLines = true, showDrawingBox = true)) }
                }
                BookletType.DREAMS -> {
                    val titles = listOf("Кем я хочу стать", "Где я хочу побывать", "Чему хочу научиться", "Мой питомец мечты", "Что я подарю миру", "Когда я вырасту")
                    titles.forEach { pages.add(PageContent(title = it, showLines = true, showDrawingBox = true)) }
                }
                BookletType.RIDDLES -> {
                    // Если есть свои загадки — используем их
                    val riddlesToUse = if (config.customRiddles.isNotEmpty()) {
                        config.customRiddles.take(6)
                    } else {
                        val allRiddles = listOf(
                            "Рыжая, с пушистым хвостом, живёт в лесу, а кур считает лучше всех." to "лиса",
                            "Не котёнок, а мурлычет, любит рыбу и тепло." to "кот",
                            "Зимой лежит, весной бежит, а летом его не найдёшь." to "снег",
                            "Днём светит, греет, а ночью спать уходит." to "солнце",
                            "Сам деревянный, а голова железная, гвозди забивает." to "молоток",
                            "Без крыльев летит, без ног бежит, без рук рисует на стекле." to "мороз",
                            "Не портной, а всю жизнь с иголками ходит." to "ёж",
                            "Зимой спит, летом ульи ворошит." to "медведь",
                            "Кто зимой холодной ходит злой, голодный?" to "волк",
                            "Красная девица сидит в темнице, а коса на улице." to "морковь",
                            "Стоит в лесу кудряшка — белая рубашка, сердечко золотое." to "ромашка",
                            "Сидит дед во сто шуб одет." to "лук",
                            "Висит груша — нельзя скушать." to "лампочка",
                            "Шесть ног, а не паук; летает, а не птица." to "муха",
                            "Висит на ноге, обтянута кожей." to "барабан"
                        )
                        if (config.riddleSeed == 0) allRiddles.shuffled()
                        else allRiddles.shuffled(Random(config.riddleSeed))
                    }.take(6)

                    riddlesToUse.forEachIndexed { i, riddle ->
                        if (i == 0) {
                            pages.add(PageContent(prompt = riddle.first, showLines = true, showDrawingBox = false))
                        } else {
                            pages.add(PageContent(
                                prompt = riddle.first,
                                previousAnswer = riddlesToUse[i - 1].second,
                                showLines = true,
                                showDrawingBox = false
                            ))
                        }
                    }
                    pages.add(PageContent(
                        isEnd = true,
                        showLines = false,
                        showDrawingBox = false,
                        riddleAnswers = riddlesToUse.map { it.second }
                    ))
                    return pages
                }
                BookletType.BLANK -> {
                    for (i in 1..6) pages.add(PageContent(showLines = true, showDrawingBox = true))
                }
                else -> {}
            }
        }

        pages.add(PageContent(isEnd = true, showLines = false, showDrawingBox = true))
        return applyDrawingRules(pages, config)
    }
}
