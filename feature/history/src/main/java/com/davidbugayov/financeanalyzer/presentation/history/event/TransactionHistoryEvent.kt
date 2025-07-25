package com.davidbugayov.financeanalyzer.presentation.history.event

import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.navigation.model.PeriodType
import com.davidbugayov.financeanalyzer.presentation.history.model.GroupingType
import java.util.Date

/**
 * События для экрана истории транзакций.
 * Следует принципу открытости/закрытости (OCP) из SOLID.
 */
sealed class TransactionHistoryEvent {
    data class SetGroupingType(val type: GroupingType) : TransactionHistoryEvent()

    data class SetPeriodType(val type: PeriodType) : TransactionHistoryEvent()

    data class SetCategories(val categories: List<String>) : TransactionHistoryEvent()

    data class SetSources(val sources: List<String>) : TransactionHistoryEvent()

    data class SetDateRange(val startDate: Date, val endDate: Date) : TransactionHistoryEvent()

    data class SetStartDate(val date: Date) : TransactionHistoryEvent()

    data class SetEndDate(val date: Date) : TransactionHistoryEvent()

    data object ReloadTransactions : TransactionHistoryEvent()

    data object LoadMoreTransactions : TransactionHistoryEvent()

    // События для удаления транзакции
    data class DeleteTransaction(val transaction: Transaction) : TransactionHistoryEvent()

    data class UpdateTransaction(val transaction: Transaction) : TransactionHistoryEvent()

    data class ShowDeleteConfirmDialog(val transaction: Transaction) : TransactionHistoryEvent()

    data object HideDeleteConfirmDialog : TransactionHistoryEvent()

    // События для управления категориями
    data class DeleteCategory(val category: String, val isExpense: Boolean) : TransactionHistoryEvent()

    data class ShowDeleteCategoryConfirmDialog(val category: String, val isExpense: Boolean) : TransactionHistoryEvent()

    data object HideDeleteCategoryConfirmDialog : TransactionHistoryEvent()

    // События для управления источниками
    data class DeleteSource(val source: String) : TransactionHistoryEvent()

    data class ShowDeleteSourceConfirmDialog(val source: String) : TransactionHistoryEvent()

    data object HideDeleteSourceConfirmDialog : TransactionHistoryEvent()

    // События для управления диалогами
    data object ShowPeriodDialog : TransactionHistoryEvent()

    data object HidePeriodDialog : TransactionHistoryEvent()

    data object ShowCategoryDialog : TransactionHistoryEvent()

    data object HideCategoryDialog : TransactionHistoryEvent()

    data object ShowSourceDialog : TransactionHistoryEvent()

    data object HideSourceDialog : TransactionHistoryEvent()

    data object ShowStartDatePicker : TransactionHistoryEvent()

    data object HideStartDatePicker : TransactionHistoryEvent()

    data object ShowEndDatePicker : TransactionHistoryEvent()

    data object HideEndDatePicker : TransactionHistoryEvent()

    // Навигация к добавлению транзакции
    data object NavigateToAddTransaction : TransactionHistoryEvent()
}
