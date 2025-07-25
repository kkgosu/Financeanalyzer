package com.davidbugayov.financeanalyzer.presentation.import_transaction.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.davidbugayov.financeanalyzer.feature.transaction.R
import com.davidbugayov.financeanalyzer.ui.theme.LocalSuccessColor

/**
 * Класс, представляющий результаты импорта транзакций
 */
data class ImportResults(
    val importedCount: Int = 0,
    val skippedCount: Int = 0,
    val errorMessage: String? = null,
    val fileName: String = "", // Имя импортируемого файла
    val bankName: String? = null, // Название банка
)

/**
 * Компонент для отображения результатов импорта
 */
@Composable
fun ImportResultsSection(
    importResults: ImportResults?,
    modifier: Modifier = Modifier,
) {
    if (importResults == null) return

    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(bottom = dimensionResource(R.dimen.space_small)),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = dimensionResource(R.dimen.card_elevation),
                ),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.space_small)),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Иконка успеха или ошибки
                Icon(
                    imageVector =
                        if (importResults.errorMessage != null) {
                            Icons.Rounded.Error
                        } else {
                            Icons.Rounded.CheckCircle
                        },
                    contentDescription = null,
                    tint =
                        if (importResults.errorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            LocalSuccessColor.current
                        },
                    modifier = Modifier.size(dimensionResource(R.dimen.import_icon_size_large)),
                )

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

                // Заголовок результата
                Text(
                    text =
                        if (importResults.errorMessage != null) {
                            stringResource(R.string.import_error)
                        } else {
                            stringResource(R.string.import_completed)
                        },
                    style = MaterialTheme.typography.titleLarge,
                    color =
                        if (importResults.errorMessage != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            LocalSuccessColor.current
                        },
                )

                // Отображение названия банка, если оно есть
                if (!importResults.bankName.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))
                    Text(
                        text = importResults.bankName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                    )
                }

                // Отображение имени файла, если оно есть
                if (importResults.fileName.isNotBlank()) {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_tiny)))
                    Text(
                        text = importResults.fileName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

                // Текст с деталями
                if (importResults.errorMessage != null) {
                    // Сообщение об ошибке
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                            ),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_card)),
                    ) {
                        Text(
                            text = importResults.errorMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(dimensionResource(R.dimen.space_small)),
                            textAlign = TextAlign.Center,
                        )
                    }
                } else {
                    // Статистика импорта
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_card)),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(dimensionResource(R.dimen.space_small)),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text =
                                    stringResource(
                                        R.string.import_summary,
                                        importResults.importedCount,
                                        importResults.skippedCount,
                                    ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.space_small)))

                // Удалена кнопка "Готово" по запросу пользователя
            }
        }
    }
}
