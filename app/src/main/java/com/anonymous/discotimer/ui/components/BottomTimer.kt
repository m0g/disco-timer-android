package com.anonymous.discotimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anonymous.discotimer.R
import com.anonymous.discotimer.ui.theme.BorderColor
import com.anonymous.discotimer.ui.theme.CurrentIntervalBackground
import com.anonymous.discotimer.ui.theme.OverlayBackground

@Composable
fun BottomTimer(
    currentCycle: Int,
    totalCycles: Int,
    currentSet: Int,
    totalSets: Int,
    modifier: Modifier = Modifier
) {
    val dotWidth = 24.dp
    val dotHeight = 10.dp
    val gap = 6.dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OverlayBackground)
            .border(width = 1.dp, color = BorderColor, shape = RectangleShape)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .navigationBarsPadding()
            .semantics {
                contentDescription =
                    "Set $currentSet of $totalSets, cycle $currentCycle of $totalCycles"
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            // verticalAlignment = Alignment.CenterVertically,
            // horizontalArrangement = Arrangement.spacedBy(8.dp)
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.cycles),
                fontSize = 22.sp,
                color = Color.White
            )
            Text(
                text = "$currentCycle/$totalCycles",
                fontSize = 41.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(gap),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                for (row in 1..totalSets) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        for (col in 1..totalCycles) {
                            val isCompleted = row < currentSet ||
                                (row == currentSet && col < currentCycle)
                            val isCurrent = row == currentSet && col == currentCycle
                            Dot(
                                width = dotWidth,
                                height = dotHeight,
                                isCompleted = isCompleted,
                                isCurrent = isCurrent
                            )
                        }
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.sets),
                fontSize = 22.sp,
                color = Color.White
            )
            Text(
                text = "$currentSet/$totalSets",
                fontSize = 41.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun Dot(
    width: Dp,
    height: Dp,
    isCompleted: Boolean,
    isCurrent: Boolean
) {
    val color = when {
        isCurrent -> CurrentIntervalBackground
        isCompleted -> Color.White
        else -> Color.White.copy(alpha = 0.25f)
    }
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(percent = 50))
            .background(color)
    )
}
