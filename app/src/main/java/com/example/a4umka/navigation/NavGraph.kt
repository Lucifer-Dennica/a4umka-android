package com.example.a4umka.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a4umka.data.*
import com.example.a4umka.ui.screens.CategoryScreen
import com.example.a4umka.ui.screens.HomeScreen
import com.example.a4umka.ui.screens.SettingsScreen
import com.example.a4umka.ui.screens.TaskScreen
import com.example.a4umka.ui.screens.hand.HandwritingAlphabetScreen
import com.example.a4umka.ui.screens.hand.LetterPracticeScreen
import com.example.a4umka.ui.screens.hand.PdfExportScreen
import com.example.a4umka.ui.screens.reading.*

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val CATEGORY = "category/{categoryId}"
    const val TASK = "task/{categoryId}/{taskId}"
    const val LETTER = "letter/{letter}"
    const val PDF_EXPORT = "pdf_export/{letter}"
    const val INTERACTIVE_RIBBON = "interactive_ribbon"
    const val INTERACTIVE_FILLWORD = "interactive_fillword"
    const val INTERACTIVE_FUNNY_STORY = "interactive_funny_story"
    const val INTERACTIVE_REBUS = "interactive_rebus"
    const val INTERACTIVE_LETTER_MATH = "interactive_letter_math"
    const val INTERACTIVE_PROVERB = "interactive_proverb"
    const val INTERACTIVE_FAIRY_TALE = "interactive_fairy_tale"
    const val INTERACTIVE_ANAGRAM = "interactive_anagram"
    const val INTERACTIVE_WORD_IN_WORD = "interactive_word_in_word"
    const val INTERACTIVE_CUSTOM_CROSSWORD = "interactive_custom_crossword"

    fun category(id: String) = "category/$id"
    fun task(categoryId: String, taskId: String) = "task/$categoryId/$taskId"
    fun letter(letter: String) = "letter/$letter"
    fun pdfExport(letter: String) = "pdf_export/$letter"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    var currentFillwordConfig by remember { mutableStateOf(FillwordConfig()) }
    var currentFunnyStory by remember { mutableStateOf<FunnyStory?>(null) }
    var currentRebusWords by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentLetterMathConfig by remember { mutableStateOf(LetterMathConfig()) }
    var currentProverbConfig by remember { mutableStateOf(ProverbConfig()) }
    var currentFairyTaleConfig by remember { mutableStateOf(FairyTaleConfig()) }
    var currentAnagramConfig by remember { mutableStateOf(AnagramConfig()) }
    var currentCustomCrosswordConfig by remember { mutableStateOf(CustomCrosswordConfig()) }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick = { navController.navigate(Routes.category(it)) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.CATEGORY,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { entry ->
            val categoryId = entry.arguments?.getString("categoryId").orEmpty()
            CategoryScreen(
                categoryId = categoryId,
                onTaskClick = { taskId -> navController.navigate(Routes.task(categoryId, taskId)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TASK,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { entry ->
            val categoryId = entry.arguments?.getString("categoryId").orEmpty()
            val taskId = entry.arguments?.getString("taskId").orEmpty()

            when {
                categoryId == "reading" && taskId == "alphabet_poster" -> AlphabetGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "letter_ribbon" -> RibbonGeneratorScreen(onBack = { navController.popBackStack() }, onPlayInteractive = { navController.navigate(Routes.INTERACTIVE_RIBBON) })
                categoryId == "reading" && taskId == "prepositions" -> PrepositionGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "syllable_tables" -> SyllableTableGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "assemble_word" -> AssembleWordGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "fillword" -> FillwordGeneratorScreen(onBack = { navController.popBackStack() }, onPlayInteractive = { currentFillwordConfig = it; navController.navigate(Routes.INTERACTIVE_FILLWORD) })
                categoryId == "reading" && taskId == "fillword_kids" -> FillwordKidsGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "funny_stories" -> FunnyStoryGeneratorScreen(onBack = { navController.popBackStack() }, onPlayInteractive = { currentFunnyStory = it; navController.navigate(Routes.INTERACTIVE_FUNNY_STORY) })
                categoryId == "reading" && taskId == "rebus_custom" -> RebusGeneratorScreen(onBack = { navController.popBackStack() }, onPlayInteractive = { currentRebusWords = it; navController.navigate(Routes.INTERACTIVE_REBUS) })
                categoryId == "reading" && taskId == "simple_rebus" -> SimpleRebusGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "letter_math" -> LetterMathGeneratorScreen(onBack = { navController.popBackStack() }, onPlayInteractive = { currentLetterMathConfig = it; navController.navigate(Routes.INTERACTIVE_LETTER_MATH) })
                categoryId == "reading" && taskId == "proverb_order" -> ProverbGeneratorScreen(onBack = { navController.popBackStack() }, onPlayInteractive = { currentProverbConfig = it; navController.navigate(Routes.INTERACTIVE_PROVERB) })
                categoryId == "reading" && taskId == "fairy_tales" -> FairyTaleGeneratorScreen(onBack = { navController.popBackStack() }, onPlayInteractive = { currentFairyTaleConfig = it; navController.navigate(Routes.INTERACTIVE_FAIRY_TALE) })
                categoryId == "reading" && taskId == "anagrams" -> AnagramGeneratorScreen(onBack = { navController.popBackStack() }, onPlayInteractive = { currentAnagramConfig = it; navController.navigate(Routes.INTERACTIVE_ANAGRAM) })
                categoryId == "reading" && taskId == "word_in_word" -> WordInWordGeneratorScreen(onBack = { navController.popBackStack() }, onPlayInteractive = { navController.navigate(Routes.INTERACTIVE_WORD_IN_WORD) })
                categoryId == "reading" && taskId == "crossword" -> CrosswordGeneratorScreen(onBack = { navController.popBackStack() })
                
                categoryId == "reading" && taskId == "custom_crossword" -> CustomCrosswordGeneratorScreen(
                    onBack = { navController.popBackStack() },
                    onPlayInteractive = { config -> 
                        currentCustomCrosswordConfig = config
                        navController.navigate(Routes.INTERACTIVE_CUSTOM_CROSSWORD) 
                    }
                )

                categoryId == "reading" && taskId == "syllables" -> SyllableGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "comprehension" -> ComprehensionGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "copying" -> CopyingGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "diary" -> DiaryGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "reading" && taskId == "booklet" -> BookletGeneratorScreen(onBack = { navController.popBackStack() })
                categoryId == "hand" && taskId == "prescriptions" -> HandwritingAlphabetScreen(onLetterClick = { navController.navigate(Routes.letter(it)) }, onBack = { navController.popBackStack() })
                else -> TaskScreen(categoryId, taskId, onBack = { navController.popBackStack() })
            }
        }

        composable(Routes.INTERACTIVE_RIBBON) { InteractiveRibbonScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.INTERACTIVE_FILLWORD) { InteractiveFillwordScreen(currentFillwordConfig, onBack = { navController.popBackStack() }) }
        composable(Routes.INTERACTIVE_FUNNY_STORY) { currentFunnyStory?.let { InteractiveFunnyStoryScreen(it, onBack = { navController.popBackStack() }) } }
        composable(Routes.INTERACTIVE_REBUS) { InteractiveRebusScreen(currentRebusWords, onBack = { navController.popBackStack() }) }
        composable(Routes.INTERACTIVE_LETTER_MATH) { InteractiveLetterMathScreen(currentLetterMathConfig, onBack = { navController.popBackStack() }) }
        composable(Routes.INTERACTIVE_PROVERB) { InteractiveProverbScreen(currentProverbConfig, onBack = { navController.popBackStack() }) }
        composable(Routes.INTERACTIVE_FAIRY_TALE) { InteractiveFairyTaleScreen(currentFairyTaleConfig, onBack = { navController.popBackStack() }) }
        composable(Routes.INTERACTIVE_ANAGRAM) { InteractiveAnagramScreen(currentAnagramConfig, onBack = { navController.popBackStack() }) }
        composable(Routes.INTERACTIVE_WORD_IN_WORD) { InteractiveWordInWordScreen(onBack = { navController.popBackStack() }) }
        
        composable(Routes.INTERACTIVE_CUSTOM_CROSSWORD) { 
            InteractiveCustomCrosswordScreen(
                config = currentCustomCrosswordConfig,
                onBack = { navController.popBackStack() }
            ) 
        }

        composable(
            route = Routes.LETTER,
            arguments = listOf(navArgument("letter") { type = NavType.StringType })
        ) { entry ->
            val letter = entry.arguments?.getString("letter").orEmpty()
            LetterPracticeScreen(letter = letter, onExportClick = { navController.navigate(Routes.pdfExport(letter)) }, onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.PDF_EXPORT,
            arguments = listOf(navArgument("letter") { type = NavType.StringType })
        ) { entry ->
            val letter = entry.arguments?.getString("letter").orEmpty()
            PdfExportScreen(letter = letter, onBack = { navController.popBackStack() })
        }
    }
}
