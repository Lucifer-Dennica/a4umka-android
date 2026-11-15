package com.example.a4umka.data

enum class SpeechSound(val letter: String) {
    R("Р"), L("Л"), S("С"), Z("З"), SH("Ш"), ZH("Ж")
}

enum class SpeechMode { KIT, SYLLABLES, WORDS, WHERE_IS_SOUND, PHRASES }

data class SoundAutomationConfig(
    val sound: SpeechSound = SpeechSound.R,
    val mode: SpeechMode = SpeechMode.SYLLABLES,
    val showAnswers: Boolean = true
)
