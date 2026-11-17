package com.example.a4umka.data

// === Режимы генератора ===
enum class MnemoMode {
    READY_TABLE,        // Готовая мнемотаблица: эмодзи + номера + текст под таблицей
    DRAW_YOURSELF,      // Нарисуй сам: пустые клетки с номерами + текст
    DESCRIPTIVE_STORY   // Описательный рассказ: картинка + 6 вопросов по плану
}

// === Темы (как на сайте) ===
enum class MnemoTheme(val title: String) {
    AUTUMN("Осень"),
    WINTER("Зима"),
    SPRING("Весна"),
    SUMMER("Лето"),
    POTESHKI("Потешки"),
    SKAZKI("Сказки"),
    ANIMALS("Про животных"),
    ALL("Все темы")
}

// === Возраст → число клеток ===
enum class MnemoAge(val title: String, val cellsCount: Int) {
    AGE_4_5("4–5 лет", 4),    // 2×2
    AGE_5_6("5–6 лет", 6),    // 2×3
    AGE_6_7("6–7 лет", 9)     // 3×3
}

// === Тип текста ===
enum class MnemoType(val title: String) {
    POEM("Стих"),
    TALE("Сказка"),
    STORY("Рассказ"),
    POTESHKA("Потешка"),
    DESCRIPTIVE("Описательный рассказ")
}

// === Категории описательных схем ===
enum class DescriptiveCategory(val title: String, val emoji: String) {
    VEGETABLES("Овощи", "🥕"),
    FRUITS("Фрукты", "🍎"),
    ANIMALS("Животные", "🐻"),
    BIRDS("Птицы", "🐦"),
    TOYS("Игрушки", "🧸"),
    TRANSPORT("Транспорт", "🚗"),
    CLOTHES("Одежда", "👕")
}

// === Одна строка текста с эмодзи-подсказкой ===
data class MnemoLine(
    val text: String,
    val emoji: String
)

// === Текст мнемотаблицы ===
data class MnemoText(
    val id: String,
    val title: String,
    val theme: MnemoTheme,
    val age: MnemoAge,
    val type: MnemoType,
    val lines: List<MnemoLine>
) {
    val cellsCount: Int get() = age.cellsCount
}

// === Описательная схема (одна) ===
data class DescriptiveScheme(
    val id: String,
    val title: String,           // "Помидор"
    val category: DescriptiveCategory,
    val emoji: String,           // "🍅"
    // 6 коротких вопросов по плану — помещаются в ячейки 2×3 рядом с иконкой:
    val questions: List<String> = listOf(
        "Что это?",
        "Какого цвета?",
        "Какой формы?",
        "Какой на вкус?",
        "Где растёт?",
        "Что из него готовят?"
    )
)

// === Конфиг генератора ===
data class MnemoConfig(
    val mode: MnemoMode = MnemoMode.READY_TABLE,
    val theme: MnemoTheme = MnemoTheme.AUTUMN,
    val selectedText: MnemoText? = null,           // null = «любой из темы»
    val showText: Boolean = true,                  // печатать текст под таблицей
    // Для режима DESCRIPTIVE_STORY:
    val descriptiveCategory: DescriptiveCategory = DescriptiveCategory.VEGETABLES,
    val selectedSchemeId: String? = null,          // null = «любая из категории»
    // Сколько листов печатать в режиме DESCRIPTIVE_STORY.
    // На каждом листе 2 описательные схемы. 1 лист = 2 картинки, 2 листа = 4, 3 листа = 6.
    val descriptiveSheetsCount: Int = 1,
    // НОВОЕ: seed для случайного выбора схем. Меняется кнопкой «Обновить».
    val randomSeed: Long = System.currentTimeMillis()
)
