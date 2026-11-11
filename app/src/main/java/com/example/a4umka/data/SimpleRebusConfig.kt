package com.example.a4umka.data

enum class SimpleRebusLength { THREE, THREE_FOUR }

data class SimpleRebusConfig(
    val length: SimpleRebusLength = SimpleRebusLength.THREE,
    val addAnswers: Boolean = true
)
