package com.example.a4umka.data

import kotlin.random.Random

data class BaseObject(
    val emoji: String,
    val nominative: String,    // Именительный (стол)
    val prepositional: String, // Предложный (на столе, в коробке)
    val instrumental: String   // Творительный (под столом, за коробкой)
)

data class PrepositionTask(
    val preposition: String,
    val targetEmoji: String,
    val targetName: String,
    val base: BaseObject,
    val question: String,
    val correctAnswer: String,
    val options: List<String>,
    val wrongPrepositions: List<String>
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
        BaseObject("🪵", "стол", "столе", "столом"),
        BaseObject("🪑", "стул", "стуле", "стулом"),
        BaseObject("📦", "коробка", "коробке", "коробкой"),
        BaseObject("🧺", "корзина", "корзине", "корзиной"),
        BaseObject("🌳", "дерево", "дереве", "деревом"),
        BaseObject("🪜", "полка", "полке", "полкой")
    )

    private fun getAnswer(prep: String, base: BaseObject): String {
        val baseWord = if (prep == "на" || prep == "в") base.prepositional else base.instrumental
        return "$prep $baseWord"
    }

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
            
            val correctAnswer = getAnswer(prep, base)

            val wrongOptions = mutableSetOf<String>()
            val wrongPreps = mutableSetOf<String>()

            while (wrongOptions.size < 2) {
                val wrongPrep = prepositionsToUse.random()
                if (wrongPrep != prep) {
                    val wrongAns = getAnswer(wrongPrep, base)
                    wrongOptions.add(wrongAns)
                    wrongPreps.add(wrongPrep)
                }
            }
            
            val allOptions = (wrongOptions + correctAnswer).shuffled()

            tasks.add(
                PrepositionTask(
                    preposition = prep,
                    targetEmoji = target.first,
                    targetName = target.second,
                    base = base,
                    question = "Где ${target.second}?",
                    correctAnswer = correctAnswer,
                    options = allOptions,
                    wrongPrepositions = wrongPreps.toList()
                )
            )
        }
        return tasks
    }
}
