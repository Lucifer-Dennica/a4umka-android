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
            TaskItem("copying", "Списывание и диктанты", "Тексты и линованные листы для письма"),
            TaskItem("diary", "Читательский дневник", "Шаблоны для заполнения после прочтения книг"), // <-- НОВОЕ
            TaskItem("rebus", "Ребусы", "Разгадай зашифрованные слова")
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
