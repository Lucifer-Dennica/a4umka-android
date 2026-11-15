package com.example.a4umka.data

enum class SpeechSound(val letter: String, val name: String) {
    R("Р", "Р"), L("Л", "Л"), S("С", "С"), Z("З", "З"), SH("Ш", "Ш"), ZH("Ж", "Ж")
}

enum class SpeechMode { KIT, SYLLABLES, WORDS, WHERE_IS_SOUND, PHRASES }

data class SoundAutomationConfig(
    val sound: SpeechSound = SpeechSound.R,
    val mode: SpeechMode = SpeechMode.SYLLABLES,
    val showAnswers: Boolean = true
)
