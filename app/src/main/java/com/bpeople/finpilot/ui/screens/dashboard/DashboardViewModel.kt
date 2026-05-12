package com.bpeople.finpilot.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.repository.ExpenseRepository
import com.bpeople.finpilot.data.repository.GoalRepository
import com.bpeople.finpilot.data.repository.IncomeRepository
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
) : ViewModel() {

    data class DashboardUiState(
        val totalIncome: Double = 0.0,
        val totalExpenses: Double = 0.0,
        val netPosition: Double = 0.0,
        val activeGoal: Goal? = null,
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
        goalRepository.observeActiveGoal(),
    ) { incomes, expenses, goal ->
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

        val progressPercent = if (goal != null && goal.targetAmount > 0.0) {
            (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
        } else {
            0f
        }

        DashboardUiState(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netPosition = netPosition,
            activeGoal = goal,
            goalProgressPercent = progressPercent,
            monthlyRequired = goal?.monthlyRequired ?: 0.0,
            incomeBreakdown = incomeBreakdown,
            expensesByCategory = expensesByCategory,
            fixedCostsPercentage = fixedPercentage,
            discretionaryPercentage = discretionaryPercentage,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DashboardUiState())
}

