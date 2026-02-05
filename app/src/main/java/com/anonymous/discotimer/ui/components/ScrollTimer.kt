package com.anonymous.discotimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anonymous.discotimer.ui.theme.BorderColor
import com.anonymous.discotimer.ui.theme.CurrentIntervalBackground

@Composable
fun ScrollTimer(
    work: Int,
    cycles: Int,
    sets: Int,
    currentTime: Int,
    modifier: Modifier = Modifier
) {
    val totalTime = work * cycles * sets
    val elapsedTime = currentTime

    val workArray = (0 until cycles * sets).map { index ->
        "${index + 1}. Work: $work"
    }.filterIndexed { index, _ ->
        (index + 1) * work > elapsedTime
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        itemsIndexed(workArray) { index, item ->
            Text(
                text = item,
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                color = if (index == 0) Color.Black else Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index == 0) CurrentIntervalBackground else Color.Transparent)
                    .drawBehind {
                        val strokeWidth = 2.dp.toPx()
                        drawLine(
                            color = BorderColor,
                            start = Offset(0f, size.height),
                            end = Offset(size.width, size.height),
                            strokeWidth = strokeWidth
                        )
                    }
                    .padding(vertical = 10.dp)
            )
        }
    }
}
