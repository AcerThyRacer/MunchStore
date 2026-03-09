package com.sugarmunch.wear.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.sugarmunch.wear.ui.theme.WearTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Wear OS Activity
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearTheme {
                WearApp()
            }
        }
    }
}

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            val viewModel: WearViewModel = hiltViewModel()
            WearHomeScreen(
                onNavigateToEffects = { navController.navigate("effects") },
                onNavigateToThemes = { navController.navigate("themes") },
                onNavigateToStats = { navController.navigate("stats") },
                viewModel = viewModel
            )
        }
        composable("effects") {
            WearEffectsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("themes") {
            WearThemesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        composable("stats") {
            WearStatsScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
