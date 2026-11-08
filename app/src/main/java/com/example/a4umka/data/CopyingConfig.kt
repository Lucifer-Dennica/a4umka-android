package com.example.a4umka.data

enum class CopyingMode { COPYING, DICTATION }
enum class CopyingLineType { STANDARD, SLANTED }

data class CopyingConfig(
    val mode: CopyingMode = CopyingMode.COPYING,
    val title: String = "Утро в лесу",
    val text: String = "Ранним утром я вышел из дома. В лесу стояла тишина. Только птицы пели в вышине. На траве блестела роса. Я долго гулял по узкой тропинке.",
    val isLargeFont: Boolean = false,
    val lineType: CopyingLineType = CopyingLineType.STANDARD,
    val linesCount: Int = 12
)
