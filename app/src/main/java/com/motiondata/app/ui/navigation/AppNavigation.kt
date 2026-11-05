package com.motiondata.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.motiondata.app.data.local.AppDatabase
import com.motiondata.app.data.repository.MotionRepositoryImpl
import com.motiondata.app.sensor.MotionSensorManager
import com.motiondata.app.ui.screens.history.HistoryScreen
import com.motiondata.app.ui.screens.history.HistoryViewModel
import com.motiondata.app.ui.screens.home.HomeScreen
import com.motiondata.app.ui.screens.home.HomeViewModel
import com.motiondata.app.ui.screens.recording.LiveRecordingScreen
import com.motiondata.app.ui.screens.recording.LiveRecordingViewModel
import com.motiondata.app.ui.screens.settings.SettingsScreen
import com.motiondata.app.ui.screens.settings.SettingsViewModel
import com.motiondata.app.ui.screens.setup.SetupScreen
import com.motiondata.app.ui.screens.setup.SetupViewModel
import com.motiondata.app.ui.screens.summary.SummaryScreen
import com.motiondata.app.ui.screens.summary.SummaryViewModel
import com.motiondata.app.ui.screens.visualization.VisualizationScreen
import com.motiondata.app.ui.screens.visualization.VisualizationViewModel
import java.net.URLDecoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Initialize Database, Repository, and Sensor Manager
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { MotionRepositoryImpl(database.sessionDao(), database.sensorSampleDao()) }
    val sensorManager = remember { MotionSensorManager(context) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Screen 1: Home Dashboard
        composable(Screen.Home.route) {
            val homeViewModel = remember { HomeViewModel(repository) }
            HomeScreen(
                viewModel = homeViewModel,
                onNavigateToSetup = { navController.navigate(Screen.Setup.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToVisualization = { sessionId ->
                    navController.navigate(Screen.Visualization.createRoute(sessionId))
                }
            )
        }

        // Screen 2: Recording Setup
        composable(Screen.Setup.route) {
            val setupViewModel = remember { SetupViewModel(sensorManager) }
            SetupScreen(
                viewModel = setupViewModel,
                onBackClick = { navController.popBackStack() },
                onStartRecording = { sessionId, name, label, hz, limit ->
                    navController.navigate(
                        Screen.LiveRecording.createRoute(sessionId, name, label, hz, limit)
                    )
                }
            )
        }

        // Screen 3: Live Recording
        composable(
            route = Screen.LiveRecording.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("label") { type = NavType.StringType },
                navArgument("hz") { type = NavType.IntType },
                navArgument("limit") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val rawName = backStackEntry.arguments?.getString("name") ?: "Session"
            val rawLabel = backStackEntry.arguments?.getString("label") ?: "Action"
            val hz = backStackEntry.arguments?.getInt("hz") ?: 50
            val limit = backStackEntry.arguments?.getInt("limit") ?: 0

            val name = URLDecoder.decode(rawName, "UTF-8")
            val label = URLDecoder.decode(rawLabel, "UTF-8")

            val liveViewModel = remember { LiveRecordingViewModel(sensorManager, repository) }

            LiveRecordingScreen(
                viewModel = liveViewModel,
                sessionId = sessionId,
                datasetName = name,
                actionLabel = label,
                targetHz = hz,
                durationLimitSec = limit,
                onRecordingCompleted = { completedId ->
                    navController.navigate(Screen.Summary.createRoute(completedId)) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        // Screen 4: Recording Summary
        composable(
            route = Screen.Summary.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val summaryViewModel = remember { SummaryViewModel(repository) }

            SummaryScreen(
                viewModel = summaryViewModel,
                sessionId = sessionId,
                onNavigateToVisualization = { id ->
                    navController.navigate(Screen.Visualization.createRoute(id))
                },
                onNavigateHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        // Screen 5: Dataset Visualization
        composable(
            route = Screen.Visualization.route,
            arguments = listOf(
                navArgument("sessionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val visViewModel = remember { VisualizationViewModel(repository) }

            VisualizationScreen(
                viewModel = visViewModel,
                sessionId = sessionId,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Screen 6: Dataset History
        composable(Screen.History.route) {
            val historyViewModel = remember { HistoryViewModel(repository) }
            HistoryScreen(
                viewModel = historyViewModel,
                onBackClick = { navController.popBackStack() },
                onNavigateToVisualization = { id ->
                    navController.navigate(Screen.Visualization.createRoute(id))
                }
            )
        }

        // Screen 7: Settings
        composable(Screen.Settings.route) {
            val settingsViewModel = remember { SettingsViewModel(sensorManager) }
            SettingsScreen(
                viewModel = settingsViewModel,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
