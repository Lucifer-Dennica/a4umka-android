package com.example.sovenok.data

enum class SpeechSound(val letter: String) {
    R("Р"), L("Л"), S("С"), Z("З"), SH("Ш"), ZH("Ж")
}

enum class SoundPair(val first: SpeechSound, val second: SpeechSound, val title: String) {
    S_SH(SpeechSound.S, SpeechSound.SH, "С — Ш"),
    R_L(SpeechSound.R, SpeechSound.L, "Р — Л"),
    Z_ZH(SpeechSound.Z, SpeechSound.ZH, "З — Ж")
}

enum class SpeechMode { KIT, SYLLABLES, WORDS, WHERE_IS_SOUND, PHRASES, PAIRS }

data class SoundAutomationConfig(
    val sound: SpeechSound = SpeechSound.R,
    val mode: SpeechMode = SpeechMode.SYLLABLES,
    val pair: SoundPair = SoundPair.S_SH,
    val showAnswers: Boolean = true,
    // НОВОЕ: сколько столбцов на листе ответов (1 — широкие пилюли на весь лист,
    // 2 — компактная двухколоночная вёрстка). Применимо к листам "Где живёт звук?"
    // и "Различаем пару".
    val answerColumns: Int = 1
)
