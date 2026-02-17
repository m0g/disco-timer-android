package com.anonymous.discotimer.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.anonymous.discotimer.BuildConfig
import com.anonymous.discotimer.R
import com.anonymous.discotimer.ui.components.GradientBackground
import com.anonymous.discotimer.ui.components.InputRow
import com.anonymous.discotimer.utils.TimeFormatter
import com.anonymous.discotimer.viewmodel.TimerViewModel
import kotlin.system.exitProcess

@Composable
fun TimerFormScreen(
    onStartTimer: () -> Unit,
    viewModel: TimerViewModel = viewModel()
) {
    val timerState by viewModel.timerState.collectAsState()

    BackHandler {
        exitProcess(0)
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section with disco ball and title
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = "🪩",
                        fontSize = 92.sp,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Disco Timer",
                        fontSize = 42.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = TimeFormatter.humanizeDuration(timerState.totalTime),
                        fontSize = 32.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = "${timerState.totalIntervals} ${stringResource(R.string.intervals)}",
                            fontSize = 32.sp,
                            color = Color.White
                        )
                        Text(
                            text = "${timerState.sets} ${stringResource(R.string.sets)}",
                            fontSize = 32.sp,
                            color = Color.White
                        )
                    }
                }

                // Mute button in top right
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 16.dp, end = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (timerState.isMuted) R.drawable.volume_off_48 else R.drawable.volume_up_48),
                        contentDescription = if (timerState.isMuted) "Unmute" else "Mute",
                        tint = Color.White
                    )
                }
            }

            // Middle section with inputs
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                InputRow(
                    label = stringResource(R.string.work),
                    value = timerState.work,
                    onValueChange = { viewModel.setWork(it) },
                    minValue = 5,
                    step = 5
                )

                InputRow(
                    label = stringResource(R.string.cycles),
                    value = timerState.cycles,
                    onValueChange = { viewModel.setCycles(it) },
                    minValue = 1
                )

                InputRow(
                    label = stringResource(R.string.sets),
                    value = timerState.sets,
                    onValueChange = { viewModel.setSets(it) },
                    minValue = 1
                )

                InputRow(
                    label = stringResource(R.string.prepare),
                    value = timerState.prepare,
                    onValueChange = { viewModel.setPrepare(it) },
                    minValue = 0,
                    step = 5
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bottom section with start button
            Button(
                onClick = {
                    viewModel.startTimer()
                    onStartTimer()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = stringResource(R.string.start),
                    color = Color.White,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = "v${BuildConfig.VERSION_NAME}",
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
        )
    }
}
