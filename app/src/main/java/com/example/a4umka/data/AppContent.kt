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
            TaskItem("syllables", "Слоги и слова", "Чтение по слогам"),
            TaskItem("rebus", "Ребусы", "Разгадай зашифрованные слова")
        )
        "hand" -> listOf(
            TaskItem("prescriptions", "Прописи букв", "Обводка букв по пунктиру"),
            TaskItem("neuro", "Нейропрописи", "Дорожки и лабиринты для рук"),
            TaskItem("strokes", "Штриховки", "Обведи и заштрихуй фигуры"),
            TaskItem("dictation", "Графические диктанты", "Рисуй по клеточкам")
        )
        // ... (остальные категории без изменений)
        "math" -> listOf(
            TaskItem("examples", "Примеры", "Генератор примеров на сложение и вычитание"),
            TaskItem("multiply", "Таблица умножения", "Тренировка умножения")
        )
        else -> emptyList()
    }
}
