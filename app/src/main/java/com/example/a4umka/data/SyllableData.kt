package com.example.a4umka.data

data class SyllableText(
    val title: String,
    val text: String,
    val questions: List<String> = emptyList()
)

object SyllableData {
    // Примеры готовых текстов (по категориям)
    val readyTexts = listOf(
        // --- Слоги и слова ---
        SyllableText(
            title = "Слова из двух слогов",
            text = "мама папа баба"
        ),
        SyllableText(
            title = "Что у нас дома",
            text = "ваза лампа диван"
        ),
        SyllableText(
            title = "Слова про животных",
            text = "кот собака коза"
        ),
        // --- Первые предложения ---
        SyllableText(
            title = "Кто где живёт",
            text = "лиса в норе"
        ),
        SyllableText(
            title = "Вкусные слова",
            text = "дыня слива малина"
        ),
        // --- Короткие рассказы ---
        SyllableText(
            title = "Кот и мышка",
            text = "Кот сидит на окошке. Мышка бежит в норку. Кот не поймал мышку."
        ),
        // --- Рассказы для 1 класса ---
        SyllableText(
            title = "Ёжик и яблоко",
            text = "Ёжик гулял по лесу. Он увидел яблоко. Яблоко было красное и вкусное. Ёжик взял яблоко и понёс его домой.",
            questions = listOf(
                "Кто гулял по лесу?",
                "Что увидел ёжик?",
                "Какое было яблоко?"
            )
        )
    )
}
