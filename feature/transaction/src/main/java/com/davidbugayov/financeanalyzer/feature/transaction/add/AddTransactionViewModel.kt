package com.davidbugayov.financeanalyzer.feature.transaction.add

import android.app.Application
import android.content.Context
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewModelScope
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.Result as CoreResult
import com.davidbugayov.financeanalyzer.data.preferences.SourcePreferences
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.domain.model.Wallet
import com.davidbugayov.financeanalyzer.domain.repository.WalletRepository
import com.davidbugayov.financeanalyzer.domain.usecase.transaction.AddTransactionUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.wallet.UpdateWalletBalancesUseCase
import com.davidbugayov.financeanalyzer.domain.usecase.widgets.UpdateWidgetsUseCase
import com.davidbugayov.financeanalyzer.feature.transaction.add.model.AddTransactionState
import com.davidbugayov.financeanalyzer.feature.transaction.base.BaseTransactionViewModel
import com.davidbugayov.financeanalyzer.feature.transaction.base.model.BaseTransactionEvent
import com.davidbugayov.financeanalyzer.feature.transaction.validation.ValidationBuilder
import com.davidbugayov.financeanalyzer.navigation.NavigationManager
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import java.math.BigDecimal
import java.util.Date
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import com.davidbugayov.financeanalyzer.analytics.CrashLoggerProvider

class AddTransactionViewModel(
    private val addTransactionUseCase: AddTransactionUseCase,
    categoriesViewModel: CategoriesViewModel,
    sourcePreferences: SourcePreferences,
    walletRepository: WalletRepository,
    private val updateWidgetsUseCase: UpdateWidgetsUseCase,
    private val updateWalletBalancesUseCase: UpdateWalletBalancesUseCase,
    private val navigationManager: NavigationManager,
    application: Application,
) : BaseTransactionViewModel<AddTransactionState, BaseTransactionEvent>(
        categoriesViewModel,
        sourcePreferences,
        walletRepository,
        updateWalletBalancesUseCase,
        application.resources,
    ) {
    override val _state = MutableStateFlow(AddTransactionState())

    private val _wallets = MutableStateFlow<List<Wallet>>(emptyList())
    override val wallets: List<Wallet>
        get() = _wallets.value

    init {
        loadInitialData()
        loadWallets()
        loadSources()
    }

    fun onNavigateBack() {
        navigationManager.navigate(NavigationManager.Command.NavigateUp)
    }

    fun onNavigateToImport() {
        navigationManager.navigate(NavigationManager.Command.Navigate("import"))
    }

    override fun loadWallets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val walletsList = walletRepository.getAllWallets()
                _wallets.value = walletsList
                Timber.d("ТРАНЗАКЦИЯ: Загружено %d кошельков", walletsList.size)
            } catch (e: Exception) {
                Timber.e(e, "Ошибка при загрузке кошельков")
                _wallets.value = emptyList()
            }
        }
    }

    override fun loadInitialData() {
        viewModelScope.launch {
            categoriesViewModel.expenseCategories.collect { expenseCategories ->
                _state.update { it.copy(expenseCategories = expenseCategories) }
            }
        }
        viewModelScope.launch {
            categoriesViewModel.incomeCategories.collect { incomeCategories ->
                _state.update { it.copy(incomeCategories = incomeCategories) }
            }
        }
        _state.update { it.copy(availableCategoryIcons = availableCategoryIcons) }
    }

    private fun validateInput(
        amount: String,
        categoryId: String,
    ): Boolean {
        Timber.d("ТРАНЗАКЦИЯ: validateInput - Входящая сумма для валидации: '%s'", amount)
        val validationBuilder = ValidationBuilder()

        _state.update {
            it.copy(
                amountError = false,
                categoryError = false,
                sourceError = false,
            )
        }

        if (amount.isBlank()) {
            validationBuilder.addAmountError()
            Timber.d("ТРАНЗАКЦИЯ: Ошибка валидации - пустая сумма")
            CrashLoggerProvider.crashLogger.logException(Exception("Пустая сумма"))
        } else {
            try {
                val amountValue = amount.replace(",", ".").toBigDecimalOrNull() ?: BigDecimal.ZERO
                if (amountValue <= BigDecimal.ZERO) {
                    validationBuilder.addAmountError()
                    Timber.d(
                        "ТРАНЗАКЦИЯ: Ошибка валидации - сумма меньше или равна нулю: %f",
                        amountValue,
                    )
                    CrashLoggerProvider.crashLogger.logException(Exception("Сумма меньше или равна нулю: $amountValue"))
                }
            } catch (e: Exception) {
                validationBuilder.addAmountError()
                Timber.e("ТРАНЗАКЦИЯ: Ошибка валидации при парсинге суммы: %s", e.message)
                CrashLoggerProvider.crashLogger.logException(e)
            }
        }

        if (categoryId.isBlank()) {
            validationBuilder.addCategoryError()
            Timber.d("ТРАНЗАКЦИЯ: Ошибка валидации - пустая категория")
            CrashLoggerProvider.crashLogger.logException(Exception("Пустая категория"))
        }

        val validationResult = validationBuilder.build()
        _state.update {
            it.copy(
                amountError = validationResult.hasAmountError,
                categoryError = validationResult.hasCategoryError,
                sourceError = validationResult.hasSourceError,
            )
        }

        Timber.d(
            "ТРАНЗАКЦИЯ: validateInput - Результат: isValid=%b, hasAmountError=%b",
            validationResult.isValid,
            validationResult.hasAmountError,
        )
        return validationResult.isValid
    }

    override fun submitTransaction(context: Context) {
        viewModelScope.launch {
            val currentState = _state.value
            _state.update { it.copy(isLoading = true) }

            val moneyFromExpression = parseMoneyExpression(currentState.amount)
            val amountForValidation = moneyFromExpression.amount.toPlainString()

            if (!validateInput(amountForValidation, currentState.category)) {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }

            val transactionToSave = prepareTransactionForAdd(moneyFromExpression)

            try {
                val result = addTransactionUseCase(transactionToSave)
                if (result is CoreResult.Success) {
                    updateWalletBalancesUseCase(
                        transactionToSave.walletIds ?: emptyList(),
                        transactionToSave.amount,
                        null,
                    )
                    incrementCategoryUsage(transactionToSave.category, transactionToSave.isExpense)
                    incrementSourceUsage(transactionToSave.source)
                    updateWidgetsUseCase()

                    // Триггер достижения за добавление транзакции
                    com.davidbugayov.financeanalyzer.domain.achievements.AchievementTrigger.onTransactionAdded()

                    // Запрос отзыва (только RuStore). Реализация предоставляется в app-модуле.
                    // Для независимой компиляции feature-модуля просто пропускаем вызов здесь.

                    _state.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true,
                        )
                    }
                } else if (result is CoreResult.Error) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = result.exception.message,
                        )
                    }
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message,
                    )
                }
            }
        }
    }

    private fun prepareTransactionForAdd(parsedMoney: Money): Transaction {
        val currentState = _state.value
        val finalAmount =
            if (currentState.isExpense) {
                parsedMoney.copy(amount = parsedMoney.amount.negate())
            } else {
                parsedMoney
            }

        val selectedWalletIds =
            getWalletIdsForTransaction(
                isExpense = currentState.isExpense,
                addToWallet = currentState.addToWallet,
                selectedWallets = currentState.selectedWallets,
            )

        return Transaction(
            id = UUID.randomUUID().toString(),
            title = currentState.title,
            amount = finalAmount,
            category = currentState.category,
            note = currentState.note,
            date = currentState.selectedDate,
            isExpense = currentState.isExpense,
            source = currentState.source,
            sourceColor = currentState.sourceColor,
            walletIds = selectedWalletIds,
        )
    }

    override fun onEvent(
        event: BaseTransactionEvent,
        context: Context,
    ) {
        when (event) {
            is BaseTransactionEvent.Submit -> submitTransaction(context)
            is BaseTransactionEvent.AddCustomSource -> handleBaseEvent(event, context)
            is BaseTransactionEvent.DeleteSource -> super.handleBaseEvent(event, context)
            else -> handleBaseEvent(event, context)
        }
    }

    override fun updateCategoryPositions() {
        // Not implemented for Add screen
    }

    override fun copyState(
        state: AddTransactionState,
        title: String,
        amount: String,
        amountError: Boolean,
        category: String,
        categoryError: Boolean,
        note: String,
        selectedDate: Date,
        isExpense: Boolean,
        showDatePicker: Boolean,
        showCategoryPicker: Boolean,
        showCustomCategoryDialog: Boolean,
        showCancelConfirmation: Boolean,
        customCategory: String,
        showSourcePicker: Boolean,
        showCustomSourceDialog: Boolean,
        customSource: String,
        source: String,
        sourceColor: Int,
        showColorPicker: Boolean,
        isLoading: Boolean,
        error: String?,
        isSuccess: Boolean,
        successMessage: String,
        expenseCategories: List<com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory>,
        incomeCategories: List<com.davidbugayov.financeanalyzer.presentation.categories.model.UiCategory>,
        sources: List<com.davidbugayov.financeanalyzer.domain.model.Source>,
        categoryToDelete: String?,
        sourceToDelete: String?,
        showDeleteCategoryConfirmDialog: Boolean,
        showDeleteSourceConfirmDialog: Boolean,
        editMode: Boolean,
        transactionToEdit: Transaction?,
        addToWallet: Boolean,
        selectedWallets: List<String>,
        showWalletSelector: Boolean,
        targetWalletId: String?,
        forceExpense: Boolean,
        sourceError: Boolean,
        preventAutoSubmit: Boolean,
        selectedExpenseCategory: String,
        selectedIncomeCategory: String,
        availableCategoryIcons: List<ImageVector>,
        customCategoryIcon: ImageVector?,
    ): AddTransactionState {
        return state.copy(
            title = title,
            amount = amount,
            amountError = amountError,
            category = category,
            categoryError = categoryError,
            note = note,
            selectedDate = selectedDate,
            isExpense = isExpense,
            showDatePicker = showDatePicker,
            showCategoryPicker = showCategoryPicker,
            showCustomCategoryDialog = showCustomCategoryDialog,
            showCancelConfirmation = showCancelConfirmation,
            customCategory = customCategory,
            showSourcePicker = showSourcePicker,
            showCustomSourceDialog = showCustomSourceDialog,
            customSource = customSource,
            source = source,
            sourceColor = sourceColor,
            showColorPicker = showColorPicker,
            isLoading = isLoading,
            error = error,
            isSuccess = isSuccess,
            successMessage = successMessage,
            expenseCategories = expenseCategories,
            incomeCategories = incomeCategories,
            sources = sources,
            categoryToDelete = categoryToDelete,
            sourceToDelete = sourceToDelete,
            showDeleteCategoryConfirmDialog = showDeleteCategoryConfirmDialog,
            showDeleteSourceConfirmDialog = showDeleteSourceConfirmDialog,
            editMode = editMode,
            transactionToEdit = transactionToEdit,
            addToWallet = addToWallet,
            selectedWallets = selectedWallets,
            showWalletSelector = showWalletSelector,
            targetWalletId = targetWalletId,
            forceExpense = forceExpense,
            sourceError = sourceError,
            preventAutoSubmit = preventAutoSubmit,
            selectedExpenseCategory = selectedExpenseCategory,
            selectedIncomeCategory = selectedIncomeCategory,
            availableCategoryIcons = availableCategoryIcons,
            customCategoryIcon = customCategoryIcon,
        )
    }

    fun setCategory(category: String) {
        val walletsList = _wallets.value
        val matchedWallet = walletsList.find { it.name == category }
        if (matchedWallet != null && _state.value.isExpense) {
            _state.update {
                it.copy(
                    category = category,
                    addToWallet = true,
                    selectedWallets = listOf(matchedWallet.id),
                    targetWalletId = matchedWallet.id,
                )
            }
        } else {
            _state.update { it.copy(category = category) }
        }
    }

    /**
     * Устанавливает тип транзакции (расход или доход) принудительно
     * @param isExpense true для расхода, false для дохода
     */
    fun setForceExpense(isExpense: Boolean) {
        _state.update {
            it.copy(
                isExpense = isExpense,
                forceExpense = isExpense,
            )
        }
    }

    /**
     * Инициализация экрана: загружает данные и сбрасывает поля, сохраняя forceExpense.
     */
    fun initializeScreen() {
        // Сначала загружаем базовые данные
        loadInitialData()
        loadWallets()
        loadSources()

        // Затем сбрасываем поля, но сохраняем forceExpense и isExpense
        val currentForceExpense = _state.value.forceExpense
        val currentIsExpense = _state.value.isExpense

        resetFields()

        // Восстанавливаем forceExpense и isExpense после сброса
        _state.update {
            it.copy(
                forceExpense = currentForceExpense,
                isExpense = currentIsExpense,
            )
        }

        Timber.d(
            "AddTransactionViewModel: initializeScreen завершена, forceExpense=%b, isExpense=%b",
            _state.value.forceExpense,
            _state.value.isExpense,
        )
    }

    override fun handleBaseEvent(
        event: BaseTransactionEvent,
        context: Context,
    ) {
        when (event) {
            is BaseTransactionEvent.AddCustomSource -> {
                val trimmedName = event.source.trim()
                if (trimmedName.length >= 2) {
                    val newSource =
                        com.davidbugayov.financeanalyzer.domain.model.Source(
                            name = trimmedName,
                            color = event.color,
                            isCustom = true,
                        )
                    val updatedSources =
                        com.davidbugayov.financeanalyzer.feature.transaction.base.util.addCustomSource(
                            sourcePreferences,
                            _state.value.sources,
                            newSource,
                        )
                    _state.update { state ->
                        state.copy(
                            sources = updatedSources,
                            showCustomSourceDialog = false,
                            customSource = "",
                            source = trimmedName,
                            sourceColor = event.color,
                        )
                    }
                }
            }
            is BaseTransactionEvent.DeleteSource -> super.handleBaseEvent(event, context)
            else -> super.handleBaseEvent(event, context)
        }
    }
}
