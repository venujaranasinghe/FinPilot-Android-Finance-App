package com.bpeople.finpilot.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.repository.ExpenseRepository
import com.bpeople.finpilot.data.repository.GoalRepository
import com.bpeople.finpilot.data.repository.IncomeRepository
import com.bpeople.finpilot.data.model.UserProfile
import com.bpeople.finpilot.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DashboardViewModel @Inject constructor(
    incomeRepository: IncomeRepository,
    expenseRepository: ExpenseRepository,
    goalRepository: GoalRepository,
    userRepository: UserRepository
) : ViewModel() {

    val userProfile: StateFlow<UserProfile?> = userRepository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private fun buildIncomeBreakdown(incomes: List<com.bpeople.finpilot.data.model.IncomeEntry>): Map<String, Double> {
        if (incomes.isEmpty()) return emptyMap()

        val grouped = incomes
            .groupBy { entry -> entry.source.trim().ifBlank { "Other" } }
            .mapValues { (_, items) -> items.sumOf { it.amountLKR } }
            .filterValues { it > 0.0 }

        if (grouped.isEmpty()) return emptyMap()

        // Keep UI readable: show top 4 sources, merge the rest into "Other".
        val sorted = grouped.entries.sortedByDescending { it.value }
        val top = sorted.take(4).associate { it.key to it.value }.toMutableMap()
        val restTotal = sorted.drop(4).sumOf { it.value }
        if (restTotal > 0.0) {
            top["Other"] = (top["Other"] ?: 0.0) + restTotal
        }
        return top
    }

    private fun buildExpensesByCategory(expenses: List<com.bpeople.finpilot.data.model.ExpenseEntry>): Map<String, Double> {
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
    )

    val dashboardState: StateFlow<DashboardUiState> = combine(
        incomeRepository.observeIncome(),
        expenseRepository.observeExpenses(),
        goalRepository.observeGoals(),
    ) { incomes, expenses, goals ->
        val activeGoal = goals.firstOrNull { it.isActive }
        val totalIncome = incomes.sumOf { it.amountLKR }
        val totalExpenses = expenses.sumOf { it.amount }
        val netPosition = totalIncome - totalExpenses

        // Income breakdown grouped by source name
        val incomeBreakdown: Map<String, Double> = incomes
            .groupBy { it.source }
            .mapValues { (_, items) -> items.sumOf { it.amountLKR } }
            .filter { it.value > 0 }

        // Expenses grouped by category
        val expensesByCategory: Map<String, Double> = expenses
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }
            .filter { it.value > 0 }

        // Fixed = recurring expenses; discretionary = non-recurring
        val fixedCosts = expenses.filter { it.isRecurring }.sumOf { it.amount }
        val fixedPercentage = if (totalExpenses > 0) (fixedCosts / totalExpenses * 100) else 0.0
        val discretionaryPercentage = if (totalExpenses > 0) 100.0 - fixedPercentage else 0.0

        val progressPercent = if (activeGoal != null && activeGoal.targetAmount > 0.0) {
            (activeGoal.currentAmount / activeGoal.targetAmount).coerceIn(0.0, 1.0).toFloat()
        } else {
            0f
        }

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
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DashboardUiState())
}

