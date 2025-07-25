package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common

import android.net.Uri
import kotlinx.coroutines.flow.Flow

/**
 * Интерфейс для всех UseCase, выполняющих импорт транзакций.
 * Определяет единый контракт для импорта транзакций из различных источников.
 */
interface ImportTransactionsUseCase {
    /**
     * Импортирует транзакции из файла по указанному URI.
     *
     * @param uri URI файла для импорта
     * @param progressCallback Колбэк для отслеживания прогресса
     * @return Поток с информацией о ходе импорта и его результатах
     */
    fun importTransactions(
        uri: Uri,
        progressCallback: ImportProgressCallback,
    ): Flow<ImportResult>
}
