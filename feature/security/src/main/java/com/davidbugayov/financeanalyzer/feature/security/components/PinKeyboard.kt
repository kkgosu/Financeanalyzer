package com.davidbugayov.financeanalyzer.feature.security.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Цифровая клавиатура для ввода PIN-кода
 */
@Composable
fun PinKeyboard(
    onNumberClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Первый ряд: 1, 2, 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(3) { index ->
                PinKeyboardButton(
                    text = (index + 1).toString(),
                    onClick = { onNumberClick((index + 1).toString()) },
                )
            }
        }

        // Второй ряд: 4, 5, 6
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(3) { index ->
                PinKeyboardButton(
                    text = (index + 4).toString(),
                    onClick = { onNumberClick((index + 4).toString()) },
                )
            }
        }

        // Третий ряд: 7, 8, 9
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            repeat(3) { index ->
                PinKeyboardButton(
                    text = (index + 7).toString(),
                    onClick = { onNumberClick((index + 7).toString()) },
                )
            }
        }

        // Четвертый ряд: пусто, 0, backspace
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            // Пустое место
            Spacer(modifier = Modifier.size(64.dp))

            // Кнопка 0
            PinKeyboardButton(
                text = "0",
                onClick = { onNumberClick("0") },
            )

            // Кнопка backspace
            PinKeyboardButton(
                onClick = onBackspaceClick,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Удалить",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

/**
 * Кнопка цифровой клавиатуры
 */
@Composable
private fun PinKeyboardButton(
    text: String? = null,
    onClick: () -> Unit,
    content: @Composable (() -> Unit)? = null,
) {
    Box(
        modifier =
            Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = CircleShape,
                )
                .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (content != null) {
            content()
        } else {
            Text(
                text = text ?: "",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
} 
