package com.profilefinder.ui

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.profilefinder.ui.screens.*

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ImageAnalysis : Screen("image_analysis")
    object ProfileSearch : Screen("profile_search")
    object Results : Screen("results")
    object History : Screen("history")
}

@Composable
fun ProfileFinderNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { slideInHorizontally { it } + fadeIn() },
        exitTransition = { slideOutHorizontally { -it } + fadeOut() },
        popEnterTransition = { slideInHorizontally { -it } + fadeIn() },
        popExitTransition = { slideOutHorizontally { it } + fadeOut() }
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToImageAnalysis = { navController.navigate(Screen.ImageAnalysis.route) },
                onNavigateToProfileSearch = { navController.navigate(Screen.ProfileSearch.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }
        composable(Screen.ImageAnalysis.route) {
            ImageAnalysisScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.ProfileSearch.route) {
            ProfileSearchScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.History.route) {
            HistoryScreen(onBack = { navController.popBackStack() })
        }
    }
}
