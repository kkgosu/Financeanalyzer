package com.davidbugayov.financeanalyzer.presentation.import_transaction.utils

import android.content.Context
import com.davidbugayov.financeanalyzer.feature.transaction.R
import timber.log.Timber

/**
 * Класс для обработки ошибок импорта и преобразования их в понятные пользователю сообщения
 */
class ImportErrorHandler(context: Context) {
    private val appContext = context.applicationContext

    /**
     * Преобразует техническое сообщение об ошибке в понятное пользователю
     *
     * @param originalMessage Исходное сообщение об ошибке
     * @return Сообщение для пользователя
     */
    fun getUserFriendlyErrorMessage(originalMessage: String): String {
        // Подробное логирование для отладки
        Timber.d("Обработка ошибки импорта: '$originalMessage'")

        // Определяем тип ошибки по содержимому сообщения
        val userFriendlyMessage =
            when {
                originalMessage.contains("statistics", ignoreCase = true) ||
                    originalMessage.contains("статистич", ignoreCase = true) ||
                    (
                        originalMessage.contains("движени", ignoreCase = true) &&
                            originalMessage.contains("средств", ignoreCase = true)
                    ) -> {
                    Timber.d("Определен тип ошибки: файл со статистикой")
                    appContext.getString(R.string.import_error_statistics_file)
                }
                originalMessage.contains("unsupported", ignoreCase = true) -> {
                    Timber.d("Определен тип ошибки: неподдерживаемый формат")
                    appContext.getString(R.string.import_error_unsupported_format)
                }
                originalMessage.contains("format", ignoreCase = true) -> {
                    Timber.d("Определен тип ошибки: неизвестный формат")
                    appContext.getString(R.string.import_error_unknown_format)
                }
                originalMessage.contains("read", ignoreCase = true) ||
                    originalMessage.contains("open", ignoreCase = true) -> {
                    Timber.d("Определен тип ошибки: ошибка чтения файла")
                    appContext.getString(R.string.import_error_file_read)
                }
                originalMessage.contains("no transaction", ignoreCase = true) ||
                    originalMessage.contains("empty", ignoreCase = true) -> {
                    Timber.d("Определен тип ошибки: нет транзакций")
                    appContext.getString(R.string.import_error_no_transactions)
                }
                originalMessage.contains("date", ignoreCase = true) -> {
                    Timber.d("Определен тип ошибки: неверный формат даты")
                    appContext.getString(R.string.import_error_date_format)
                }
                originalMessage.contains("csv", ignoreCase = true) -> {
                    Timber.d("Определен тип ошибки: проблема с форматом CSV")
                    appContext.getString(R.string.import_error_csv_format)
                }
                else -> {
                    Timber.d("Неизвестный тип ошибки, возвращаем общее сообщение")
                    appContext.getString(R.string.import_error_unknown, "", originalMessage)
                }
            }

        Timber.i("Ошибка импорта преобразована: '$originalMessage' -> '$userFriendlyMessage'")
        return userFriendlyMessage
    }
}
