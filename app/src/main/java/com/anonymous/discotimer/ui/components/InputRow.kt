package com.anonymous.discotimer.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun InputRow(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    minValue: Int = 1,
    step: Int = 1,
    modifier: Modifier = Modifier
) {
    var textValue by remember(value) { mutableStateOf(value.toString()) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            fontSize = 24.sp
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (value > minValue) {
                Button(
                    onClick = {
                        val newValue = (value - step).coerceAtLeast(minValue)
                        onValueChange(newValue)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("-", fontSize = 18.sp)
                }
            }

            BasicTextField(
                value = textValue,
                onValueChange = { newText ->
                    textValue = newText
                    newText.toIntOrNull()?.let { newValue ->
                        if (newValue >= minValue) {
                            onValueChange(newValue)
                        }
                    }
                },
                modifier = Modifier.width(50.dp),
                textStyle = TextStyle(
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    onValueChange(value + step)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("+", fontSize = 18.sp)
            }
        }
    }
}
