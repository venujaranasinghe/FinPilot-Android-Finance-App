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

        // Calculate income breakdown by source
        val incomeBreakdown: Map<String, Double> = mapOf(
            "Salary" to incomes.filter { it.source.contains("Salary", ignoreCase = true) }
                .sumOf { it.amountLKR },
            "Freelance" to incomes.filter { it.source.contains("Freelance", ignoreCase = true) }
                .sumOf { it.amountLKR },
            "AdSense" to incomes.filter { it.source.contains("AdSense", ignoreCase = true) }
                .sumOf { it.amountLKR },
            "Crypto" to incomes.filter { it.source.contains("Crypto", ignoreCase = true) }
                .sumOf { it.amountLKR },
        )

        // Calculate expenses by category
        val expensesByCategory: Map<String, Double> = expenses
            .groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        // Calculate committed vs discretionary
        val fixedCosts = totalExpenses * 0.65
        val discretionarySpend = totalExpenses * 0.35
        val fixedPercentage = if (totalIncome > 0) (fixedCosts / totalIncome * 100) else 0.0
        val discretionaryPercentage = if (totalIncome > 0) (discretionarySpend / totalIncome * 100) else 0.0

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

