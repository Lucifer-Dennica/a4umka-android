package com.example.a4umka.data

enum class FunnyStoryTheme { ALL, FAIRY_TALE, ABOUT_ME, ADVENTURE, ANIMALS }
enum class FunnyStoryWriter { ADULT, CHILD }

data class FunnyStory(
    val title: String,
    val theme: FunnyStoryTheme,
    val questions: List<String>,
    val template: String
)

data class FunnyStoryConfig(
    val theme: FunnyStoryTheme = FunnyStoryTheme.ALL,
    val count: Int = 3,
    val writer: FunnyStoryWriter = FunnyStoryWriter.ADULT,
    val isBlackAndWhite: Boolean = false,
    val currentStories: List<FunnyStory> = emptyList()
)
