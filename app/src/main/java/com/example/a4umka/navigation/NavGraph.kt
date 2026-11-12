package com.example.a4umka.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a4umka.data.FillwordConfig
import com.example.a4umka.data.FunnyStory
import com.example.a4umka.data.LetterMathConfig
import com.example.a4umka.data.ProverbConfig
import com.example.a4umka.data.FairyTaleConfig
import com.example.a4umka.ui.screens.CategoryScreen
import com.example.a4umka.ui.screens.HomeScreen
import com.example.a4umka.ui.screens.SettingsScreen
import com.example.a4umka.ui.screens.TaskScreen
import com.example.a4umka.ui.screens.hand.HandwritingAlphabetScreen
import com.example.a4umka.ui.screens.hand.LetterPracticeScreen
import com.example.a4umka.ui.screens.hand.PdfExportScreen
import com.example.a4umka.ui.screens.reading.AlphabetGeneratorScreen
import com.example.a4umka.ui.screens.reading.InteractiveRibbonScreen
import com.example.a4umka.ui.screens.reading.RibbonGeneratorScreen
import com.example.a4umka.ui.screens.reading.SyllableTableGeneratorScreen
import com.example.a4umka.ui.screens.reading.AssembleWordGeneratorScreen
import com.example.a4umka.ui.screens.reading.FillwordGeneratorScreen
import com.example.a4umka.ui.screens.reading.FillwordKidsGeneratorScreen
import com.example.a4umka.ui.screens.reading.FunnyStoryGeneratorScreen
import com.example.a4umka.ui.screens.reading.RebusGeneratorScreen
import com.example.a4umka.ui.screens.reading.SimpleRebusGeneratorScreen
import com.example.a4umka.ui.screens.reading.LetterMathGeneratorScreen
import com.example.a4umka.ui.screens.reading.ProverbGeneratorScreen
import com.example.a4umka.ui.screens.reading.FairyTaleGeneratorScreen
import com.example.a4umka.ui.screens.reading.InteractiveFillwordScreen
import com.example.a4umka.ui.screens.reading.InteractiveFunnyStoryScreen
import com.example.a4umka.ui.screens.reading.InteractiveRebusScreen
import com.example.a4umka.ui.screens.reading.InteractiveLetterMathScreen
import com.example.a4umka.ui.screens.reading.InteractiveProverbScreen
import com.example.a4umka.ui.screens.reading.InteractiveFairyTaleScreen
import com.example.a4umka.ui.screens.reading.SyllableGeneratorScreen
import com.example.a4umka.ui.screens.reading.ComprehensionGeneratorScreen
import com.example.a4umka.ui.screens.reading.CopyingGeneratorScreen
import com.example.a4umka.ui.screens.reading.DiaryGeneratorScreen
import com.example.a4umka.ui.screens.reading.BookletGeneratorScreen

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val CATEGORY = "category/{categoryId}"
    const val TASK = "task/{categoryId}/{taskId}"
    const val LETTER = "letter/{letter}"
    const val PDF_EXPORT = "pdf_export/{letter}"
    const val INTERACTIVE_RIBBON = "interactive_ribbon"
    const val SYLLABLE_TABLES = "syllable_tables"
    const val ASSEMBLE_WORD = "assemble_word"
    const val FILLWORD = "fillword"
    const val FILLWORD_KIDS = "fillword_kids"
    const val INTERACTIVE_FILLWORD = "interactive_fillword"
    const val FUNNY_STORIES = "funny_stories"
    const val INTERACTIVE_FUNNY_STORY = "interactive_funny_story"
    const val REBUS_CUSTOM = "rebus_custom"
    const val SIMPLE_REBUS = "simple_rebus"
    const val INTERACTIVE_REBUS = "interactive_rebus"
    const val LETTER_MATH = "letter_math"
    const val INTERACTIVE_LETTER_MATH = "interactive_letter_math"
    const val PROVERB_ORDER = "proverb_order"
    const val INTERACTIVE_PROVERB = "interactive_proverb"
    const val FAIRY_TALES = "fairy_tales"
    const val INTERACTIVE_FAIRY_TALE = "interactive_fairy_tale"
    const val SYLLABLE_READING = "syllable_reading"
    const val COMPREHENSION = "comprehension"
    const val COPYING = "copying"
    const val DIARY = "diary"
    const val BOOKLET = "booklet"

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
                categoryId == "reading" && taskId == "alphabet_poster" -> {
                    AlphabetGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "letter_ribbon" -> {
                    RibbonGeneratorScreen(
                        onBack = { navController.popBackStack() },
                        onPlayInteractive = { navController.navigate(Routes.INTERACTIVE_RIBBON) }
                    )
                }
                categoryId == "reading" && taskId == "syllable_tables" -> {
                    SyllableTableGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "assemble_word" -> {
                    AssembleWordGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "fillword" -> {
                    FillwordGeneratorScreen(
                        onBack = { navController.popBackStack() },
                        onPlayInteractive = { config ->
                            currentFillwordConfig = config
                            navController.navigate(Routes.INTERACTIVE_FILLWORD)
                        }
                    )
                }
                categoryId == "reading" && taskId == "fillword_kids" -> {
                    FillwordKidsGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "funny_stories" -> {
                    FunnyStoryGeneratorScreen(
                        onBack = { navController.popBackStack() },
                        onPlayInteractive = { story ->
                            currentFunnyStory = story
                            navController.navigate(Routes.INTERACTIVE_FUNNY_STORY)
                        }
                    )
                }
                categoryId == "reading" && taskId == "rebus_custom" -> {
                    RebusGeneratorScreen(
                        onBack = { navController.popBackStack() },
                        onPlayInteractive = { words ->
                            currentRebusWords = words
                            navController.navigate(Routes.INTERACTIVE_REBUS)
                        }
                    )
                }
                categoryId == "reading" && taskId == "simple_rebus" -> {
                    SimpleRebusGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "letter_math" -> {
                    LetterMathGeneratorScreen(
                        onBack = { navController.popBackStack() },
                        onPlayInteractive = { config ->
                            currentLetterMathConfig = config
                            navController.navigate(Routes.INTERACTIVE_LETTER_MATH)
                        }
                    )
                }
                categoryId == "reading" && taskId == "proverb_order" -> {
                    ProverbGeneratorScreen(
                        onBack = { navController.popBackStack() },
                        onPlayInteractive = { config ->
                            currentProverbConfig = config
                            navController.navigate(Routes.INTERACTIVE_PROVERB)
                        }
                    )
                }
                categoryId == "reading" && taskId == "fairy_tales" -> {
                    FairyTaleGeneratorScreen(
                        onBack = { navController.popBackStack() },
                        onPlayInteractive = { config ->
                            currentFairyTaleConfig = config
                            navController.navigate(Routes.INTERACTIVE_FAIRY_TALE)
                        }
                    )
                }
                categoryId == "reading" && taskId == "syllables" -> {
                    SyllableGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "comprehension" -> {
                    ComprehensionGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "copying" -> {
                    CopyingGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "diary" -> {
                    DiaryGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "booklet" -> {
                    BookletGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "hand" && taskId == "prescriptions" -> {
                    HandwritingAlphabetScreen(
                        onLetterClick = { letter -> navController.navigate(Routes.letter(letter)) },
                        onBack = { navController.popBackStack() }
                    )
                }
                else -> {
                    TaskScreen(categoryId, taskId, onBack = { navController.popBackStack() })
                }
            }
        }

        composable(Routes.INTERACTIVE_RIBBON) {
            InteractiveRibbonScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.INTERACTIVE_FILLWORD) {
            InteractiveFillwordScreen(
                config = currentFillwordConfig,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.INTERACTIVE_FUNNY_STORY) {
            currentFunnyStory?.let { story ->
                InteractiveFunnyStoryScreen(
                    story = story,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        composable(Routes.INTERACTIVE_REBUS) {
            InteractiveRebusScreen(
                words = currentRebusWords,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.INTERACTIVE_LETTER_MATH) {
            InteractiveLetterMathScreen(
                config = currentLetterMathConfig,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.INTERACTIVE_PROVERB) {
            InteractiveProverbScreen(
                config = currentProverbConfig,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.INTERACTIVE_FAIRY_TALE) {
            InteractiveFairyTaleScreen(
                config = currentFairyTaleConfig,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.LETTER,
            arguments = listOf(navArgument("letter") { type = NavType.StringType })
        ) { entry ->
            val letter = entry.arguments?.getString("letter").orEmpty()
            LetterPracticeScreen(
                letter = letter,
                onExportClick = { navController.navigate(Routes.pdfExport(letter)) },
                onBack = { navController.popBackStack() }
            )
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
