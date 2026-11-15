package com.example.a4umka.data

enum class SoundSchemeLength { THREE, FOUR, FIVE, MIX }
enum class SoundSchemeColors { TWO, THREE }

data class SoundSchemeConfig(
    val length: SoundSchemeLength = SoundSchemeLength.THREE,
    val colors: SoundSchemeColors = SoundSchemeColors.TWO,
    val showAnswers: Boolean = true
)
