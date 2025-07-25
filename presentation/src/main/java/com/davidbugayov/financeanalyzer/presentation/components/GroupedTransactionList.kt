package com.davidbugayov.financeanalyzer.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.davidbugayov.financeanalyzer.core.model.Money
import com.davidbugayov.financeanalyzer.core.util.formatForDisplay
import com.davidbugayov.financeanalyzer.domain.model.Transaction
import com.davidbugayov.financeanalyzer.presentation.categories.CategoriesViewModel
import com.davidbugayov.financeanalyzer.ui.R as UiR

/**
 * Простой список с группировкой и «аккордеоном».
 * @param groups Map<key, List<Transaction>> – уже сгруппированные транзакции (ключ отображается как title)
 */
@Composable
fun GroupedTransactionList(
    groups: Map<String, List<Transaction>>, // ключ -> список
    categoriesViewModel: CategoriesViewModel,
    onTransactionClick: (Transaction) -> Unit,
    onTransactionLongClick: (Transaction) -> Unit,
) {
    // Состояние свёрнутости по ключу группы (в памяти экрана)
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        // Сортируем ключи по убыванию (предположим, ключ содержит дату/текст)
        val sortedKeys = groups.keys.sortedDescending()
        sortedKeys.forEach { key ->
            val list = groups[key].orEmpty()
            val total: Money = list.fold(Money.zero()) { acc, tx -> acc + tx.amount }
            val expanded = expandedMap.getOrPut(key) { true }

            // Header
            item(key + "_header") {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { expandedMap[key] = !expanded },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val tintColor =
                        if (total.amount.signum() < 0) {
                            colorResource(id = UiR.color.expense_primary)
                        } else {
                            colorResource(id = UiR.color.income_primary)
                        }

                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = tintColor,
                        modifier = Modifier.padding(end = 4.dp),
                    )
                    Text(
                        text = key,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = total.formatForDisplay(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tintColor,
                    )
                }
            }

            if (expanded) {
                items(list) { tx ->
                    TransactionItem(
                        transaction = tx,
                        categoriesViewModel = categoriesViewModel,
                        onClick = { onTransactionClick(tx) },
                        onTransactionLongClick = { onTransactionLongClick(tx) },
                        animated = false,
                        animationDelay = 0L,
                    )
                }
            }
        }
        item("fab_spacer") { Spacer(modifier = Modifier.height(80.dp)) }
    }
}
