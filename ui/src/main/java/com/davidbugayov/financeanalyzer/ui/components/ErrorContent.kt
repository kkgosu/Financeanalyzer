package com.davidbugayov.financeanalyzer.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.ui.R

/**
 * Компонент для отображения ошибки.
 * Показывает сообщение об ошибке и кнопку для повторной попытки.
 *
 * @param error Текст ошибки или null для использования стандартного сообщения
 * @param onRetry Callback, вызываемый при нажатии на кнопку повтора
 */
@Composable
fun ErrorContent(
    error: String?,
    onRetry: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = error ?: stringResource(R.string.error_occurred),
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            Text(stringResource(R.string.retry))
        }
    }
}
