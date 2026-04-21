package com.anonymous.discotimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
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
    onIntervalClick: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val totalIntervals = cycles * sets
    val currentIndex = if (work > 0 && totalIntervals > 0) {
        (currentTime / work).coerceIn(0, totalIntervals - 1)
    } else 0

    val workArray = (0 until totalIntervals).map { index ->
        "${index + 1}. Work: $work"
    }

    val listState = rememberLazyListState()
    val itemHeight = 60.dp

    LaunchedEffect(currentIndex) {
        listState.animateScrollToItem(currentIndex)
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val bottomPadding = (maxHeight - itemHeight).coerceAtLeast(0.dp)
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = bottomPadding)
        ) {
            itemsIndexed(workArray) { index, item ->
                Text(
                    text = item,
                    fontSize = 32.sp,
                    textAlign = TextAlign.Center,
                    color = if (index == currentIndex) Color.Black else Color.White,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable { onIntervalClick(index) }
                        .background(if (index == currentIndex) CurrentIntervalBackground else Color.Transparent)
                        .drawBehind {
                            val strokeWidth = 2.dp.toPx()
                            drawLine(
                                color = BorderColor,
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = strokeWidth
                            )
                        }
                        .wrapContentHeight(Alignment.CenterVertically)
                )
            }
        }
    }
}
