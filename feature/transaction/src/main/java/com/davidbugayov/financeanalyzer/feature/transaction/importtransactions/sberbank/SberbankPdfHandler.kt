package com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.sberbank

import android.content.Context
import android.net.Uri
import com.davidbugayov.financeanalyzer.domain.repository.TransactionRepository
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.FileType
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.common.ImportTransactionsUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.importtransactions.handlers.AbstractPdfBankHandler
import java.io.BufferedInputStream
import timber.log.Timber

/**
 * Хендлер для PDF-выписок Сбербанка
 */
class SberbankPdfHandler(
    transactionRepository: TransactionRepository,
    context: Context,
) : AbstractPdfBankHandler(transactionRepository, context) {
    override val bankName: String = "Сбербанк PDF"

    // Ключевые слова для определения PDF-файлов Сбербанка
    override val pdfKeywords: List<String> =
        listOf(
            "sberbank",
            "сбербанк",
            "сбер",
            "sber",
            "выписка по счету",
            "выписка по счёту",
            "выписка сбербанк",
        )

    /**
     * Проверяет, может ли данный хендлер обработать файл по имени и содержимому
     */
    override fun canHandle(
        fileName: String,
        uri: Uri,
        fileType: FileType,
    ): Boolean {
        if (!supportsFileType(fileType)) return false
        val hasKeyword = pdfKeywords.any { fileName.lowercase().contains(it.lowercase()) }
        // Дополнительная проверка по содержимому файла, если в имени только "выписка"
        if (fileName.lowercase().contains("выписка") && !hasKeyword) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val buffer = ByteArray(2048)
                    val bis = BufferedInputStream(inputStream)
                    val bytesRead = bis.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        val content = String(buffer, 0, bytesRead)
                        val sberIndicators =
                            listOf(
                                "СБЕРБАНК",
                                "SBERBANK",
                                "СберБанк",
                                "Сбербанк",
                                "900 www.sberbank.ru",
                                "СберБанк Онлайн",
                                "ПАО Сбербанк",
                                "Для проверки подлинности документа",
                            )
                        val hasSberIndicator =
                            sberIndicators.any {
                                content.contains(
                                    it,
                                    ignoreCase = true,
                                )
                            }
                        if (hasSberIndicator) {
                            Timber.d(
                                "[$bankName Handler] Найден индикатор Сбербанка в содержимом файла",
                            )
                            return true
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.w(
                    "[$bankName Handler] Ошибка при чтении файла для определения: ${e.message}",
                )
            }
        }
        return hasKeyword
    }

    /**
     * Создаёт UseCase для импорта PDF-выписки Сбербанка
     */
    override fun createImporter(fileType: FileType): ImportTransactionsUseCase {
        if (supportsFileType(fileType)) {
            Timber.d("[$bankName Handler] Создание SberbankPdfImportUseCase")
            return SberbankPdfImportUseCase(context, transactionRepository)
        }
        throw IllegalArgumentException("[$bankName Handler] не поддерживает тип файла: $fileType")
    }
}
