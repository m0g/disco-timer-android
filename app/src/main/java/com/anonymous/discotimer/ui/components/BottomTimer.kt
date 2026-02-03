package com.anonymous.discotimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anonymous.discotimer.R
import com.anonymous.discotimer.ui.theme.BorderColor
import com.anonymous.discotimer.ui.theme.OverlayBackground

@Composable
fun BottomTimer(
    currentCycle: Int,
    totalCycles: Int,
    currentSet: Int,
    totalSets: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(OverlayBackground)
            .border(width = 1.dp, color = BorderColor, shape = RectangleShape)
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.cycles),
                fontSize = 28.sp,
                color = Color.White
            )
            Text(
                text = "$currentCycle/$totalCycles",
                fontSize = 66.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.sets),
                fontSize = 28.sp,
                color = Color.White
            )
            Text(
                text = "$currentSet/$totalSets",
                fontSize = 66.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}
