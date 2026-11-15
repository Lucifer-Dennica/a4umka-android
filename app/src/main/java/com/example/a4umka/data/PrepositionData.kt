package com.example.a4umka.data

import kotlin.random.Random

enum class SceneObject(val title: String) {
    TABLE("стол"), CHAIR("стул"), BOX("коробка"), BASKET("корзина"), TREE("дерево"), SHELF("полка"),
    BALL("мяч"), CAT("кот"), MOUSE("мышка"), BIRD("птичка"), APPLE("яблоко"), DICE("кубик"), FLOWER("цветок")
}

data class PrepositionTask(
    val preposition: String,
    val targetObject: SceneObject,
    val baseObject: SceneObject,
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
        SceneObject.BALL, SceneObject.CAT, SceneObject.MOUSE, SceneObject.BIRD, 
        SceneObject.APPLE, SceneObject.DICE, SceneObject.FLOWER
    )

    private val bases = listOf(
        SceneObject.TABLE, SceneObject.CHAIR, SceneObject.BOX, 
        SceneObject.BASKET, SceneObject.TREE, SceneObject.SHELF
    )

    private fun getBaseWord(base: SceneObject, prep: String): String {
        return when (base) {
            SceneObject.TABLE -> if (prep == "на" || prep == "в") "столе" else "столом"
            SceneObject.CHAIR -> if (prep == "на" || prep == "в") "стуле" else "стулом"
            SceneObject.BOX -> if (prep == "на" || prep == "в") "коробке" else "коробкой"
            SceneObject.BASKET -> if (prep == "на" || prep == "в") "корзине" else "корзиной"
            SceneObject.TREE -> if (prep == "на" || prep == "в") "дереве" else "деревом"
            SceneObject.SHELF -> if (prep == "на" || prep == "в") "полке" else "полкой"
            else -> "предметом"
        }
    }

    private fun getAnswer(prep: String, base: SceneObject): String {
        return "$prep ${getBaseWord(base, prep)}"
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
                    targetObject = target,
                    baseObject = base,
                    question = "Где ${target.title}?",
                    correctAnswer = correctAnswer,
                    options = allOptions,
                    wrongPrepositions = wrongPreps.toList()
                )
            )
        }
        return tasks
    }
}
