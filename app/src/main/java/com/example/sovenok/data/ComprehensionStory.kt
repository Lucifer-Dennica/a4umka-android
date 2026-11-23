package com.example.sovenok.data

data class ComprehensionStory(
    val id: String,
    val grade: Int,
    val title: String,
    val text: String,
    val questions: List<String>,
    val answers: List<String> = emptyList() // Добавлено для режима редактирования
) {
    // Автоматический подсчет слов (без учета заголовка)
    val totalWords: Int
        get() = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
}
