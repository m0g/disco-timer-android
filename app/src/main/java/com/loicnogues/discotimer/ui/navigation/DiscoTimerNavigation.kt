package com.loicnogues.discotimer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.loicnogues.discotimer.ui.screens.TimerFormScreen
import com.loicnogues.discotimer.ui.screens.TimerViewScreen
import com.loicnogues.discotimer.viewmodel.TimerViewModel

sealed class Screen(val route: String) {
    object TimerForm : Screen("timer_form")
    object TimerView : Screen("timer_view")
}

@Composable
fun DiscoTimerNavigation() {
    val navController = rememberNavController()
    val viewModel: TimerViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.TimerForm.route
    ) {
        composable(Screen.TimerForm.route) {
            TimerFormScreen(
                onStartTimer = {
                    navController.navigate(Screen.TimerView.route)
                },
                viewModel = viewModel
            )
        }

        composable(Screen.TimerView.route) {
            TimerViewScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                viewModel = viewModel
            )
        }
    }
}
