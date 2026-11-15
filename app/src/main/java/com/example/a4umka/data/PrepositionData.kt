package com.example.a4umka.data

import kotlin.random.Random

enum class PrepositionEnum(val ruName: String, val fileName: String) {
    NA("на", "na"),
    POD("под", "pod"),
    V("в", "v"),
    NAD("над", "nad"),
    ZA("за", "za"),
    PERED("перед", "pered"),
    MEZHDU("между", "mezhdu"),
    SLEVA("слева от", "sleva"),
    SPRAVA("справа от", "sprava")
}

enum class ScenePair(val folderName: String, val targetName: String, val baseName: String, val targetNom: String, val baseNom: String) {
    MOUSE_BASKET("mouse_basket", "мышка", "корзина", "мышка", "корзина"),
    CAT_SHELF("cat_shelf", "кот", "полка", "кот", "полка"),
    APPLE_SHELF("apple_shelf", "яблоко", "полка", "яблоко", "полка"),
    BIRD_TREE("bird_tree", "птичка", "дерево", "птичка", "дерево"),
    TOYS_BOX("toys_box", "игрушки", "коробка", "игрушки", "коробка")
}

data class PrepositionTask(
    val preposition: PrepositionEnum,
    val scene: ScenePair,
    val question: String,
    val correctAnswer: String,
    val options: List<String>,
    val wrongPrepositions: List<PrepositionEnum>
)

object PrepositionData {
    private val simplePrepositions = listOf(PrepositionEnum.NA, PrepositionEnum.POD, PrepositionEnum.V)
    private val basicPrepositions = listOf(PrepositionEnum.NAD, PrepositionEnum.ZA, PrepositionEnum.PERED)
    private val allPrepositions = listOf(PrepositionEnum.MEZHDU, PrepositionEnum.SLEVA, PrepositionEnum.SPRAVA)

    private fun getBaseWord(baseNom: String, prep: PrepositionEnum): String {
        // Простая логика падежей для наших базовых слов
        return when (baseNom) {
            "корзина" -> if (prep == PrepositionEnum.NA || prep == PrepositionEnum.V) "корзине" else "корзиной"
            "полка" -> if (prep == PrepositionEnum.NA || prep == PrepositionEnum.V) "полке" else "полкой"
            "дерево" -> if (prep == PrepositionEnum.NA || prep == PrepositionEnum.V) "дереве" else "деревом"
            "коробка" -> if (prep == PrepositionEnum.NA || prep == PrepositionEnum.V) "коробке" else "коробкой"
            else -> "предметом"
        }
    }

    private fun getAnswer(prep: PrepositionEnum, scene: ScenePair): String {
        return "${prep.ruName} ${getBaseWord(scene.baseNom, prep)}"
    }

    fun generateTasks(config: PrepositionConfig): List<PrepositionTask> {
        val tasks = mutableListOf<PrepositionTask>()
        val prepositionsToUse = when (config.level) {
            PrepositionLevel.SIMPLE -> simplePrepositions
            PrepositionLevel.BASIC -> simplePrepositions + basicPrepositions
            PrepositionLevel.ALL -> simplePrepositions + basicPrepositions + allPrepositions
        }

        val allScenes = ScenePair.values()

        repeat(config.tasksCount) {
            val prep = prepositionsToUse.random()
            val scene = allScenes.random()
            
            val correctAnswer = getAnswer(prep, scene)

            val wrongOptions = mutableSetOf<String>()
            val wrongPreps = mutableSetOf<PrepositionEnum>()

            while (wrongOptions.size < 2) {
                val wrongPrep = prepositionsToUse.random()
                if (wrongPrep != prep) {
                    val wrongAns = getAnswer(wrongPrep, scene)
                    wrongOptions.add(wrongAns)
                    wrongPreps.add(wrongPrep)
                }
            }
            
            val allOptions = (wrongOptions + correctAnswer).shuffled()

            tasks.add(
                PrepositionTask(
                    preposition = prep,
                    scene = scene,
                    question = "Где ${scene.targetNom}?",
                    correctAnswer = correctAnswer,
                    options = allOptions,
                    wrongPrepositions = wrongPreps.toList()
                )
            )
        }
        return tasks
    }
}
