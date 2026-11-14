package com.example.a4umka.data

import kotlin.random.Random

data class PrepositionTask(
    val preposition: String,
    val targetEmoji: String,
    val targetName: String,
    val baseEmoji: String,
    val baseName: String,
    val question: String,
    val correctAnswer: String,
    val options: List<String>
)

object PrepositionData {
    private val simplePrepositions = listOf("на", "под", "в")
    private val basicPrepositions = listOf("над", "за", "перед")
    private val allPrepositions = listOf("между", "слева от", "справа от")

    private val targets = listOf(
        Pair("⚽", "мяч"),
        Pair("🐱", "кот"),
        Pair("🐭", "мышка"),
        Pair("🐦", "птичка"),
        Pair("🍎", "яблоко"),
        Pair("🎲", "кубик"),
        Pair("🌸", "цветок")
    )

    private val bases = listOf(
        Pair("🪵", "столом"),
        Pair("🪑", "стулом"),
        Pair("📦", "коробкой"),
        Pair("🧺", "корзиной"),
        Pair("🌳", "деревом"),
        Pair("🪜", "полкой")
    )

    fun generateTasks(config: PrepositionConfig): List<PrepositionTask> {
        val tasks = mutableListOf<PrepositionTask>()
        val prepositionsToUse = when (config.level) {
            PrepositionLevel.SIMPLE -> simplePrepositions
            PrepositionLevel.BASIC -> simplePrepositions + basicPrepositions
            PrepositionLevel.ALL -> simplePrepositions + basicPrepositions + allPrepositions
        }

        repeat(config.tasksCount) {
            val prep = prepositionsToUse.random()
            val target = targets.random()
            val base = bases.random()
            
            // Формируем правильный ответ в зависимости от предлога
            val correctAnswer = when (prep) {
                "на" -> "на ${base.second}"
                "под" -> "под ${base.second}"
                "в" -> "в ${base.second}"
                "над" -> "над ${base.second}"
                "за" -> "за ${base.second}"
                "перед" -> "перед ${base.second}"
                "между" -> "между ${base.second}" // Для простоты используем одну базу
                "слева от" -> "слева от ${base.second}"
                "справа от" -> "справа от ${base.second}"
                else -> "на ${base.second}"
            }

            // Генерируем 2 неправильных варианта
            val wrongOptions = mutableSetOf<String>()
            while (wrongOptions.size < 2) {
                val wrongPrep = prepositionsToUse.random()
                if (wrongPrep != prep) {
                    val wrongAns = when (wrongPrep) {
                        "на" -> "на ${base.second}"
                        "под" -> "под ${base.second}"
                        "в" -> "в ${base.second}"
                        "над" -> "над ${base.second}"
                        "за" -> "за ${base.second}"
                        "перед" -> "перед ${base.second}"
                        "между" -> "между ${base.second}"
                        "слева от" -> "слева от ${base.second}"
                        "справа от" -> "справа от ${base.second}"
                        else -> "на ${base.second}"
                    }
                    wrongOptions.add(wrongAns)
                }
            }
            
            val allOptions = (wrongOptions + correctAnswer).shuffled()

            tasks.add(
                PrepositionTask(
                    preposition = prep,
                    targetEmoji = target.first,
                    targetName = target.second,
                    baseEmoji = base.first,
                    baseName = base.second,
                    question = "Где ${target.second}?",
                    correctAnswer = correctAnswer,
                    options = allOptions
                )
            )
        }
        return tasks
    }
}
