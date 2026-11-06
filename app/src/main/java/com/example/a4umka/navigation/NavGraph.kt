package com.example.a4umka.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.a4umka.ui.screens.CategoryScreen
import com.example.a4umka.ui.screens.HomeScreen
import com.example.a4umka.ui.screens.SettingsScreen
import com.example.a4umka.ui.screens.TaskScreen
import com.example.a4umka.ui.screens.hand.HandwritingAlphabetScreen
import com.example.a4umka.ui.screens.hand.LetterPracticeScreen
import com.example.a4umka.ui.screens.hand.PdfExportScreen
import com.example.a4umka.ui.screens.reading.AlphabetGeneratorScreen
import com.example.a4umka.ui.screens.reading.RibbonGeneratorScreen // <-- НОВЫЙ ИМПОРТ

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val CATEGORY = "category/{categoryId}"
    const val TASK = "task/{categoryId}/{taskId}"
    const val LETTER = "letter/{letter}"
    const val PDF_EXPORT = "pdf_export/{letter}"

    fun category(id: String) = "category/$id"
    fun task(categoryId: String, taskId: String) = "task/$categoryId/$taskId"
    fun letter(letter: String) = "letter/$letter"
    fun pdfExport(letter: String) = "pdf_export/$letter"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

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
                // --- ЧТЕНИЕ ---
                categoryId == "reading" && taskId == "alphabet_poster" -> {
                    AlphabetGeneratorScreen(onBack = { navController.popBackStack() })
                }
                categoryId == "reading" && taskId == "letter_ribbon" -> { // <-- НОВЫЙ МАРШРУТ
                    RibbonGeneratorScreen(onBack = { navController.popBackStack() })
                }
                
                // --- РУКА И МОТОРИКА ---
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
