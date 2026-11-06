package com.example.a4umka.data

enum class RibbonColor { RED, BLUE, GREEN, BOTH, WHITE }

data class RibbonLetter(val char: String, val color: RibbonColor)

object RibbonData {
    // Гласные (верхний и нижний ряд)
    val topVowels = listOf(
        RibbonLetter("Аа", RibbonColor.RED),
        RibbonLetter("Оо", RibbonColor.RED),
        RibbonLetter("Уу", RibbonColor.RED),
        RibbonLetter("Ыы", RibbonColor.RED),
        RibbonLetter("Ээ", RibbonColor.RED)
    )
    val bottomVowels = listOf(
        RibbonLetter("Яя", RibbonColor.RED),
        RibbonLetter("Ёё", RibbonColor.RED),
        RibbonLetter("Юю", RibbonColor.RED),
        RibbonLetter("Ии", RibbonColor.RED),
        RibbonLetter("Ее", RibbonColor.RED)
    )

    // Согласные (звонкие - верхний ряд)
    val topConsonants = listOf(
        RibbonLetter("Лл", RibbonColor.GREEN),
        RibbonLetter("Мм", RibbonColor.GREEN),
        RibbonLetter("Нн", RibbonColor.GREEN),
        RibbonLetter("Рр", RibbonColor.GREEN),
        RibbonLetter("Йй", RibbonColor.GREEN),
        RibbonLetter("Бб", RibbonColor.BOTH),
        RibbonLetter("Вв", RibbonColor.BOTH),
        RibbonLetter("Гг", RibbonColor.BOTH),
        RibbonLetter("Дд", RibbonColor.BOTH),
        RibbonLetter("Жж", RibbonColor.BLUE),
        RibbonLetter("Зз", RibbonColor.BOTH)
    )
    
    // Согласные (глухие - нижний ряд)
    val bottomConsonants = listOf(
        RibbonLetter("Пп", RibbonColor.BOTH),
        RibbonLetter("Фф", RibbonColor.BOTH),
        RibbonLetter("Кк", RibbonColor.BOTH),
        RibbonLetter("Тт", RibbonColor.BOTH),
        RibbonLetter("Шш", RibbonColor.BLUE),
        RibbonLetter("Сс", RibbonColor.BOTH),
        RibbonLetter("Хх", RibbonColor.BOTH),
        RibbonLetter("Цц", RibbonColor.BLUE),
        RibbonLetter("Чч", RibbonColor.GREEN),
        RibbonLetter("Щщ", RibbonColor.GREEN)
    )

    // Знаки
    val signs = listOf(
        RibbonLetter("ь", RibbonColor.WHITE),
        RibbonLetter("ъ", RibbonColor.WHITE)
    )
}
