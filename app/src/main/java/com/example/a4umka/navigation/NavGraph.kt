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

object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val CATEGORY = "category/{categoryId}"
    const val TASK = "task/{categoryId}/{taskId}"

    fun category(id: String) = "category/$id"
    fun task(categoryId: String, taskId: String) = "task/$categoryId/$taskId"
}

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Routes.category(categoryId))
                },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.CATEGORY,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty()
            CategoryScreen(
                categoryId = categoryId,
                onTaskClick = { taskId ->
                    navController.navigate(Routes.task(categoryId, taskId))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.TASK,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("taskId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId").orEmpty()
            val taskId = backStackEntry.arguments?.getString("taskId").orEmpty()
            TaskScreen(
                categoryId = categoryId,
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
