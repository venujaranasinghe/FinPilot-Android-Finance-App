package com.bpeople.finpilot.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.data.model.UserProfile
import com.bpeople.finpilot.data.repository.ExpenseRepository
import com.bpeople.finpilot.data.repository.GoalRepository
import com.bpeople.finpilot.data.repository.IncomeRepository
import com.bpeople.finpilot.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    goalRepository: GoalRepository,
    userRepository: UserRepository
) : ViewModel() {

    data class RecentTransaction(
        val id: String,
        val title: String,
        val subtitle: String,
        val amount: Double,
        val dateMillis: Long,
        val isExpense: Boolean,
    )

    data class MonthComparison(
        val label: String,
        val currentAmount: Double,
        val previousAmount: Double,
        val changePercentage: Double,
        val increaseIsGood: Boolean,
    )

    data class CategoryInsight(
        val category: String,
        val currentAmount: Double,
        val previousAmount: Double,
        val sharePercentage: Double,
        val changePercentage: Double,
    )

    data class DashboardUiState(
        val totalIncome: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val netPosition: Double = 0.0,
        val activeGoal: Goal? = null,
        val allGoals: List<Goal> = emptyList(),
        val goalProgressPercent: Float = 0f,
        val monthlyRequired: Double = 0.0,
        val incomeBreakdown: Map<String, Double> = emptyMap(),
        val expensesByCategory: Map<String, Double> = emptyMap(),
        val fixedCostsPercentage: Double = 0.0,
        val discretionaryPercentage: Double = 0.0,
        val currentMonthLabel: String = "",
        val previousMonthLabel: String = "",
        val monthOverMonthComparisons: List<MonthComparison> = emptyList(),
        val topCategoryInsights: List<CategoryInsight> = emptyList(),
        val didYouKnowInsight: String? = null,
        val recentTransactions: List<RecentTransaction> = emptyList(),
    )

    val userProfile: StateFlow<UserProfile?> = userRepository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val dashboardState: StateFlow<DashboardUiState> = combine(
        incomeRepository.observeIncome(),
        expenseRepository.observeExpenses(),
        goalRepository.observeGoals(),
    ) { incomes, expenses, goals ->
        val activeGoal = goals.firstOrNull { it.isActive }
        val totalIncome = incomes.sumOf { it.amountLKR }
        val totalExpenses = expenses.sumOf { it.amount }
        val netPosition = totalIncome - totalExpenses

        val incomeBreakdown = buildIncomeBreakdown(incomes)
        val expensesByCategory = buildExpensesByCategory(expenses)

        val monthRanges = buildMonthRanges()
        val currentMonthExpensesFiltered = expenses.filterExpenseByRange(
            monthRanges.currentMonthStartMillis,
            monthRanges.nextMonthStartMillis,
        )
        val currentMonthExpenseTotalForRatio = currentMonthExpensesFiltered.sumOf { it.amount }
        val fixedCosts = currentMonthExpensesFiltered.filter { it.isRecurring }.sumOf { it.amount }
        val fixedPercentage = if (currentMonthExpenseTotalForRatio > 0) (fixedCosts / currentMonthExpenseTotalForRatio * 100) else 0.0
        val discretionaryPercentage = if (currentMonthExpenseTotalForRatio > 0) 100.0 - fixedPercentage else 0.0

        val progressPercent = if (activeGoal != null && activeGoal.targetAmount > 0.0) {
            (activeGoal.currentAmount / activeGoal.targetAmount).coerceIn(0.0, 1.0).toFloat()
        } else {
            0f
        }

        val recentTransactions = buildRecentTransactions(incomes = incomes, expenses = expenses)

        val currentMonthIncome = incomes.sumIncomeForRange(
            monthRanges.currentMonthStartMillis,
            monthRanges.nextMonthStartMillis,
        ) { it.amountLKR }
        val previousMonthIncome = incomes.sumIncomeForRange(
            monthRanges.previousMonthStartMillis,
            monthRanges.currentMonthStartMillis,
        ) { it.amountLKR }

        val currentMonthExpenses = expenses.sumExpenseForRange(
            monthRanges.currentMonthStartMillis,
            monthRanges.nextMonthStartMillis,
        ) { it.amount }
        val previousMonthExpenses = expenses.sumExpenseForRange(
            monthRanges.previousMonthStartMillis,
            monthRanges.currentMonthStartMillis,
        ) { it.amount }

        val currentMonthNet = currentMonthIncome - currentMonthExpenses
        val previousMonthNet = previousMonthIncome - previousMonthExpenses

        val monthComparisons = listOf(
            MonthComparison(
                label = "Income",
                currentAmount = currentMonthIncome,
                previousAmount = previousMonthIncome,
                changePercentage = percentChange(previousMonthIncome, currentMonthIncome),
                increaseIsGood = true,
            ),
            MonthComparison(
                label = "Expenses",
                currentAmount = currentMonthExpenses,
                previousAmount = previousMonthExpenses,
                changePercentage = percentChange(previousMonthExpenses, currentMonthExpenses),
                increaseIsGood = false,
            ),
            MonthComparison(
                label = "Net Balance",
                currentAmount = currentMonthNet,
                previousAmount = previousMonthNet,
                changePercentage = percentChange(previousMonthNet, currentMonthNet),
                increaseIsGood = true,
            ),
        )

        val currentCategoryTotals = expenses
            .filterExpenseByRange(monthRanges.currentMonthStartMillis, monthRanges.nextMonthStartMillis)
            .groupBy { it.category.trim().ifBlank { "Other" } }
            .mapValues { (_, items) -> items.sumOf { it.amount }.coerceAtLeast(0.0) }
            .filterValues { it > 0.0 }

        val previousCategoryTotals = expenses
            .filterExpenseByRange(monthRanges.previousMonthStartMillis, monthRanges.currentMonthStartMillis)
            .groupBy { it.category.trim().ifBlank { "Other" } }
            .mapValues { (_, items) -> items.sumOf { it.amount }.coerceAtLeast(0.0) }
            .filterValues { it > 0.0 }

        val currentMonthExpenseTotal = currentCategoryTotals.values.sum()
        val topCategoryInsights = currentCategoryTotals
            .entries
            .sortedByDescending { it.value }
            .take(3)
            .map { (category, amount) ->
                val previousAmount = previousCategoryTotals[category] ?: 0.0
                val share = if (currentMonthExpenseTotal > 0.0) {
                    (amount / currentMonthExpenseTotal) * 100
                } else {
                    0.0
                }
                CategoryInsight(
                    category = category,
                    currentAmount = amount,
                    previousAmount = previousAmount,
                    sharePercentage = share,
                    changePercentage = percentChange(previousAmount, amount),
                )
            }

        val didYouKnow = buildDidYouKnowInsight(
            topCategoryInsights = topCategoryInsights,
            currentMonthIncome = currentMonthIncome,
            currentMonthExpenses = currentMonthExpenses,
            currentMonthNet = currentMonthNet,
            previousMonthNet = previousMonthNet,
            fixedCostsPercentage = fixedPercentage,
            currentMonthLabel = monthRanges.currentMonthLabel,
        )

        DashboardUiState(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netPosition = netPosition,
            activeGoal = activeGoal,
            allGoals = goals,
            goalProgressPercent = progressPercent,
            monthlyRequired = activeGoal?.monthlyRequired ?: 0.0,
            incomeBreakdown = incomeBreakdown,
            expensesByCategory = expensesByCategory,
            fixedCostsPercentage = fixedPercentage,
            discretionaryPercentage = discretionaryPercentage,
            currentMonthLabel = monthRanges.currentMonthLabel,
            previousMonthLabel = monthRanges.previousMonthLabel,
            monthOverMonthComparisons = monthComparisons,
            topCategoryInsights = topCategoryInsights,
            didYouKnowInsight = didYouKnow,
            recentTransactions = recentTransactions,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DashboardUiState())

    fun quickSend(amount: Double, recipientName: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val entry = ExpenseEntry(
                    id = java.util.UUID.randomUUID().toString(),
                    amount = amount,
                    category = "Transfer",
                    note = "Quick send to $recipientName",
                    date = com.google.firebase.Timestamp.now(),
                    paymentMethod = "Card"
                )
                expenseRepository.addExpense(entry)
                onComplete()
            } catch (e: Exception) {
                // handle failure or log it
            }
        }
    }

    fun deleteTransaction(id: String, isExpense: Boolean) {
        viewModelScope.launch {
            if (isExpense) expenseRepository.deleteExpense(id)
            else incomeRepository.deleteIncome(id)
        }
    }

    private fun buildRecentTransactions(
        incomes: List<IncomeEntry>,
        expenses: List<ExpenseEntry>,
    ): List<RecentTransaction> {
        val incomeTransactions = incomes.mapNotNull { entry ->
            val dateMillis = entry.date?.toDate()?.time ?: return@mapNotNull null
            RecentTransaction(
                id = entry.id,
                title = entry.source.trim().ifBlank { "Income" },
                subtitle = entry.label?.trim().orEmpty().ifBlank { "Income" },
                amount = entry.amountLKR,
                dateMillis = dateMillis,
                isExpense = false,
            )
        }

        val expenseTransactions = expenses.mapNotNull { entry ->
            val dateMillis = entry.date?.toDate()?.time ?: return@mapNotNull null
            val subCat = entry.subCategory?.trim().orEmpty()
            val note = entry.note?.trim().orEmpty()
            val desc = buildString {
                if (subCat.isNotEmpty()) append(subCat)
                if (note.isNotEmpty()) {
                    if (isNotEmpty()) append(" - ")
                    append(note)
                }
            }.ifBlank { entry.paymentMethod.trim() }.ifBlank { "Unspecified" }
            RecentTransaction(
                id = entry.id,
                title = entry.category.trim().ifBlank { "Expense" },
                subtitle = desc,
                amount = entry.amount,
                dateMillis = dateMillis,
                isExpense = true,
            )
        }

        return (incomeTransactions + expenseTransactions)
            .sortedByDescending { it.dateMillis }
    }

    private fun buildIncomeBreakdown(incomes: List<IncomeEntry>): Map<String, Double> {
        if (incomes.isEmpty()) return emptyMap()

        val grouped = incomes
            .groupBy { entry -> entry.source.trim().ifBlank { "Other" } }
            .mapValues { (_, items) -> items.sumOf { it.amountLKR } }
            .filterValues { it > 0.0 }

        if (grouped.isEmpty()) return emptyMap()

        val sorted = grouped.entries.sortedByDescending { it.value }
        val top = sorted.take(4).associate { it.key to it.value }.toMutableMap()
        val restTotal = sorted.drop(4).sumOf { it.value }
        if (restTotal > 0.0) {
            top["Other"] = (top["Other"] ?: 0.0) + restTotal
        }
        return top
    }

    private fun buildExpensesByCategory(expenses: List<ExpenseEntry>): Map<String, Double> {
        if (expenses.isEmpty()) return emptyMap()

        val grouped = expenses
            .groupBy { entry -> entry.category.trim().ifBlank { "Other" } }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .filterValues { it > 0.0 }

        if (grouped.isEmpty()) return emptyMap()

        val sorted = grouped.entries.sortedByDescending { it.value }
        val top = sorted.take(5).associate { it.key to it.value }.toMutableMap()
        val restTotal = sorted.drop(5).sumOf { it.value }
        if (restTotal > 0.0) {
            top["Other"] = (top["Other"] ?: 0.0) + restTotal
        }
        return top
    }

    private data class MonthRanges(
        val previousMonthStartMillis: Long,
        val currentMonthStartMillis: Long,
        val nextMonthStartMillis: Long,
        val currentMonthLabel: String,
        val previousMonthLabel: String,
    )

    private fun buildMonthRanges(): MonthRanges {
        val formatter = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val currentStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val nextStart = currentStart.clone() as Calendar
        nextStart.add(Calendar.MONTH, 1)

        val previousStart = currentStart.clone() as Calendar
        previousStart.add(Calendar.MONTH, -1)

        return MonthRanges(
            previousMonthStartMillis = previousStart.timeInMillis,
            currentMonthStartMillis = currentStart.timeInMillis,
            nextMonthStartMillis = nextStart.timeInMillis,
            currentMonthLabel = formatter.format(currentStart.time),
            previousMonthLabel = formatter.format(previousStart.time),
        )
    }

    private fun List<IncomeEntry>.sumIncomeForRange(
        startMillis: Long,
        endMillis: Long,
        amountSelector: (IncomeEntry) -> Double,
    ): Double = filterIncomeByRange(startMillis, endMillis).sumOf(amountSelector)

    private fun List<ExpenseEntry>.sumExpenseForRange(
        startMillis: Long,
        endMillis: Long,
        amountSelector: (ExpenseEntry) -> Double,
    ): Double = filterExpenseByRange(startMillis, endMillis).sumOf(amountSelector)

    private fun List<IncomeEntry>.filterIncomeByRange(startMillis: Long, endMillis: Long): List<IncomeEntry> =
        filter { income ->
            val millis = income.date?.toDate()?.time ?: return@filter false
            millis in startMillis until endMillis
        }

    private fun List<ExpenseEntry>.filterExpenseByRange(startMillis: Long, endMillis: Long): List<ExpenseEntry> =
        filter { expense ->
            val millis = expense.date?.toDate()?.time ?: return@filter false
            millis in startMillis until endMillis
        }

    private fun percentChange(previous: Double, current: Double): Double {
        if (previous == 0.0 && current == 0.0) return 0.0
        if (previous == 0.0) return if (current >= 0.0) 100.0 else -100.0

        return ((current - previous) / previous.absoluteValue) * 100
    }

    private fun buildDidYouKnowInsight(
        topCategoryInsights: List<CategoryInsight>,
        currentMonthIncome: Double,
        currentMonthExpenses: Double,
        currentMonthNet: Double,
        previousMonthNet: Double,
        fixedCostsPercentage: Double,
        currentMonthLabel: String,
    ): String? {
        if (currentMonthIncome <= 0.0 && currentMonthExpenses <= 0.0) return null

        val topCategory = topCategoryInsights.firstOrNull()
        if (topCategory != null && topCategory.sharePercentage >= 35.0) {
            return "Did you know? ${topCategory.category} made up ${topCategory.sharePercentage.roundToInt()}% of your spending in $currentMonthLabel."
        }

        if (fixedCostsPercentage >= 60.0) {
            return "Did you know? ${fixedCostsPercentage.roundToInt()}% of your spending is recurring, which can limit monthly flexibility."
        }

        if (currentMonthNet > previousMonthNet && currentMonthNet > 0.0) {
            val improvement = (currentMonthNet - previousMonthNet).absoluteValue
            return "Did you know? Your net balance improved by ${formatLkrShort(improvement)} compared with last month."
        }

        if (currentMonthExpenses > 0.0) {
            val dayOfMonth = Calendar.getInstance().get(Calendar.DAY_OF_MONTH).coerceAtLeast(1)
            val avgDailySpend = currentMonthExpenses / dayOfMonth
            return "Did you know? Your average daily spend this month is ${formatLkrShort(avgDailySpend)}."
        }

        return null
    }

    private fun formatLkrShort(amount: Double): String {
        val safe = amount.coerceAtLeast(0.0)
        return when {
            safe >= 1_000_000 -> "LKR %.1fM".format(safe / 1_000_000)
            safe >= 1_000 -> "LKR %.0fK".format(safe / 1_000)
            else -> "LKR %.0f".format(safe)
        }
    }
}

