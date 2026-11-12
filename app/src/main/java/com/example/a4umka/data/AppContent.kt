package com.example.a4umka.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class Category(val id: String, val title: String, val icon: ImageVector)
data class TaskItem(val id: String, val title: String, val description: String)

object AppContent {
    val categories = listOf(
        Category("reading", "Чтение", Icons.Default.MenuBook),
        Category("hand", "Рука и моторика", Icons.Default.Edit),
        Category("logic", "Логика и внимание", Icons.Default.Extension),
        Category("math", "Математика", Icons.Default.Calculate),
        Category("world", "Окружающий мир", Icons.Default.Public),
        Category("english", "Английский", Icons.Default.Translate),
        Category("parents", "Родителям", Icons.Default.FamilyRestroom)
    )

    fun categoryById(id: String): Category? = categories.find { it.id == id }

    fun tasksFor(categoryId: String): List<TaskItem> = when (categoryId) {
        "reading" -> listOf(
            TaskItem("alphabet_poster", "Алфавит-плакат", "Генератор плакатов и карточек с буквами"),
            TaskItem("letter_ribbon", "Лента букв", "Гласные и согласные, звонкие и глухие"),
            TaskItem("syllables", "Слоги и слова", "Чтение по слогам с дугами и точками"),
            TaskItem("comprehension", "Чтение с вопросами", "Тексты для проверки понимания прочитанного"),
            TaskItem("copying", "Тексты для списывания и диктантов", "Тексты и линованные листы для письма"),
            TaskItem("diary", "Читательский дневник", "Шаблоны для заполнения после прочтения книг"),
            TaskItem("booklet", "Книжка-малышка", "Сложи лист А4 в мини-книжку на 8 страниц"),
            TaskItem("syllable_tables", "Слоговые таблицы", "Тренажёр для чтения слогов по строкам"),
            TaskItem("assemble_word", "Собери слово", "Соедини слоги, картинки и карточки"),
            TaskItem("fillword", "Филворд из своих слов", "Спрячь свои слова в сетке букв"),
            TaskItem("fillword_kids", "Филворд для малышей", "Готовый детский словарь с картинками"),
            TaskItem("funny_stories", "Весёлые истории", "Игра в чепуху: впиши слова и читай"),
            TaskItem("rebus_custom", "Ребус из своего слова", "Зашифруй слово по первым буквам картинок"),
            TaskItem("simple_rebus", "Ребусы", "Готовые ребусы по первым буквам"),
            TaskItem("letter_math", "Сложи слово", "Примеры с буквами: прибавь и убери"),
            TaskItem("proverb_order", "Собери пословицу", "Расставь месяцы и дни недели по порядку"),
            TaskItem("fairy_tales", "Назови сказку правильно", "Викторина по перепутанным названиям"),
            TaskItem("anagrams", "Анаграммы", "Переставь слоги или буквы, чтобы получилось слово")
        )
        "hand" -> listOf(
            TaskItem("prescriptions", "Прописи букв", "Обводка букв по пунктиру"),
            TaskItem("neuro", "Нейропрописи", "Дорожки и лабиринты для рук"),
            TaskItem("strokes", "Штриховки", "Обведи и заштрихуй фигуры"),
            TaskItem("dictation", "Графические диктанты", "Рисуй по клеточкам")
        )
        "logic" -> listOf(
            TaskItem("sudoku", "Судоку", "Логические сетки для детей"),
            TaskItem("differences", "Найди отличия", "Сравни две картинки"),
            TaskItem("sequence", "Продолжи ряд", "Найди закономерность"),
            TaskItem("schulte", "Таблицы Шульте", "Тренировка внимания")
        )
        "math" -> listOf(
            TaskItem("examples", "Примеры", "Генератор примеров на сложение и вычитание"),
            TaskItem("multiply", "Таблица умножения", "Тренировка умножения"),
            TaskItem("pyramids", "Числовые пирамиды", "Сложение по пирамиде"),
            TaskItem("mazes", "Математические лабиринты", "Лабиринт с примерами")
        )
        "world" -> listOf(
            TaskItem("dinosaurs", "Динозавры", "Познавательные задания"),
            TaskItem("body", "Тело человека", "Строение и органы"),
            TaskItem("road", "ПДД", "Правила дорожного движения"),
            TaskItem("animals", "Животные", "Кто где живёт")
        )
        "english" -> listOf(
            TaskItem("alphabet_en", "Алфавит", "Английский алфавит"),
            TaskItem("words", "Слова", "Первые английские слова"),
            TaskItem("colors", "Цвета", "Colours and numbers")
        )
        "parents" -> listOf(
            TaskItem("tips", "Советы", "Как заниматься с ребёнком"),
            TaskItem("about", "О приложении", "Информация и обратная связь")
        )
        else -> emptyList()
    }
}
