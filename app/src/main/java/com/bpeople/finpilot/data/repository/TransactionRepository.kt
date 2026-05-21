package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.MonthlyBarData
import com.bpeople.finpilot.data.model.TransactionItem
import com.bpeople.finpilot.data.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
) {
    fun observeTransactions(): Flow<List<TransactionItem>> = combine(
        incomeRepository.observeIncome(),
        expenseRepository.observeExpenses(),
    ) { income, expenses ->
        val incomeItems = income.map { entry ->
            TransactionItem(
                id = entry.id,
                type = TransactionType.INCOME,
                source = entry.source.lowercase().trim(),
                displayName = entry.source.trim().replaceFirstChar { it.titlecase() }.ifBlank { "Income" },
                amount = entry.amountOriginal,
                currency = entry.currencyOriginal,
                amountInLKR = entry.amountLKR,
                exchangeRate = entry.exchangeRate.takeIf { it != 1.0 },
                note = entry.label,
                paymentMethod = "bank",
                isRecurring = entry.type.equals("RECURRING", ignoreCase = true),
                timestampMillis = entry.date?.toDate()?.time ?: 0L,
            )
        }
        val expenseItems = expenses.map { entry ->
            val originalAmountLKR = if (entry.originalCurrency != null && entry.originalCurrency != "LKR") {
                entry.amount
            } else {
                entry.amount
            }
            TransactionItem(
                id = entry.id,
                type = TransactionType.EXPENSE,
                source = entry.category.lowercase().trim(),
                displayName = entry.category.trim().replaceFirstChar { it.titlecase() }.ifBlank { "Expense" },
                amount = entry.originalAmount ?: entry.amount,
                currency = entry.originalCurrency ?: "LKR",
                amountInLKR = originalAmountLKR,
                exchangeRate = null,
                note = entry.note,
                paymentMethod = entry.paymentMethod.lowercase(),
                isRecurring = entry.isRecurring,
                timestampMillis = entry.date?.toDate()?.time ?: 0L,
            )
        }
        (incomeItems + expenseItems).sortedByDescending { it.timestampMillis }
    }

    suspend fun deleteTransaction(item: TransactionItem) {
        when (item.type) {
            TransactionType.INCOME -> incomeRepository.deleteIncome(item.id)
            TransactionType.EXPENSE -> expenseRepository.deleteExpense(item.id)
        }
    }
}
