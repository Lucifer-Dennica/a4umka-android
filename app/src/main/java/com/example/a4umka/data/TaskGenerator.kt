// app/src/main/java/com/example/a4umka/data/TaskGenerator.kt
package com.example.a4umka.data

import kotlin.random.Random

object TaskGenerator {
    fun generateMathExamples(count: Int = 10, maxNumber: Int = 10): List<String> {
        return (1..count).map {
            val a = Random.nextInt(1, maxNumber + 1)
            val b = Random.nextInt(1, maxNumber + 1)
            "$a + $b = ___"
        }
    }
}
