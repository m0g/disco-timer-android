package com.anonymous.discotimer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anonymous.discotimer.R
import com.anonymous.discotimer.ui.components.*
import com.anonymous.discotimer.ui.theme.BorderColor
import com.anonymous.discotimer.utils.TimeFormatter
import com.anonymous.discotimer.viewmodel.TimerViewModel

@Composable
fun TimerViewScreen(
    onNavigateBack: () -> Unit,
    viewModel: TimerViewModel = viewModel()
) {
    val timerState by viewModel.timerState.collectAsState()
    var showBackDialog by remember { mutableStateOf(false) }

    BackHandler {
        showBackDialog = true
    }

    if (showBackDialog) {
        AlertDialog(
            onDismissRequest = { showBackDialog = false },
            title = { Text(stringResource(R.string.hold_on)) },
            text = { Text(stringResource(R.string.exit_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetTimer()
                        showBackDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text(stringResource(R.string.yes))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (timerState.isCompleted) {
        TimerCompletedScreen(onNavigateBack = {
            viewModel.resetTimer()
            onNavigateBack()
        })
    } else {
        GradientBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // Top bar with controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent)
                        .border(width = 2.dp, color = BorderColor)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.togglePause() }) {
                        Text(
                            text = if (timerState.isPaused) "▶️" else "⏸️",
                            fontSize = 48.sp
                        )
                    }

                    Text(
                        text = TimeFormatter.formatSeconds(timerState.remainingTime),
                        fontSize = 92.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(onClick = { viewModel.toggleMute() }) {
                        Text(
                            text = if (timerState.isMuted) "🔇" else "🔊",
                            fontSize = 48.sp
                        )
                    }
                }

                // Main timer display
                WorkTimer(
                    currentWorkTime = timerState.currentWorkTime,
                    onPress = { viewModel.togglePause() },
                    modifier = Modifier.weight(1f)
                )

                // Scroll timer showing remaining intervals
                ScrollTimer(
                    work = timerState.work,
                    cycles = timerState.cycles,
                    sets = timerState.sets,
                    currentTime = timerState.currentTime,
                    modifier = Modifier.weight(1f)
                )

                // Bottom progress display
                BottomTimer(
                    currentCycle = timerState.currentCycle,
                    totalCycles = timerState.cycles,
                    currentSet = timerState.currentSet,
                    totalSets = timerState.sets
                )
            }
        }
    }
}
