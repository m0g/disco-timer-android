package com.anonymous.discotimer.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.anonymous.discotimer.ui.screens.TimerFormScreen
import com.anonymous.discotimer.ui.screens.TimerViewScreen
import com.anonymous.discotimer.viewmodel.TimerViewModel

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
