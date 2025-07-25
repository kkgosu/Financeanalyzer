package com.davidbugayov.financeanalyzer.presentation.categories.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.domain.model.Category
import com.davidbugayov.financeanalyzer.domain.model.Transaction

data class UiCategory(
    val id: Long,
    val name: String,
    val isExpense: Boolean,
    val isCustom: Boolean = false,
    val count: Int = 0,
    val money: Money = Money.zero(),
    val percentage: Float = 0f,
    val transactions: List<Transaction> = emptyList(),
    val original: Category? = null,
    val color: Color = Color.Gray,
    val icon: ImageVector? = null,
) {
    companion object {
        fun custom(
            name: String,
            isExpense: Boolean,
            icon: ImageVector? = null,
            color: Color,
        ): UiCategory =
            UiCategory(
                id = System.currentTimeMillis(),
                name = name,
                isExpense = isExpense,
                isCustom = true,
                color = color,
                icon = icon,
            )
    }
}
