package com.anonymous.discotimer.ui.screens

import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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

    // Keep screen on while timer is running
    val activity = LocalContext.current as? android.app.Activity
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

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
                        // .border(width = 2.dp, color = BorderColor)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            drawLine(
                                color = BorderColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.togglePause() }) {
                        Icon(
                            painter = painterResource(id = if (timerState.isPaused) R.drawable.play_arrow_48 else R.drawable.pause_48),
                            contentDescription = if (timerState.isPaused) "Play" else "Pause",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = TimeFormatter.formatSeconds(timerState.remainingTime),
                        fontSize = 92.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(onClick = { viewModel.toggleMute() }) {
                        Icon(
                            painter = painterResource(id = if (timerState.isMuted) R.drawable.volume_off_48 else R.drawable.volume_up_48),
                            contentDescription = if (timerState.isMuted) "Unmute" else "Mute",
                            tint = Color.White
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
