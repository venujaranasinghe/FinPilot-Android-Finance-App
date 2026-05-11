package com.bpeople.finpilot.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.repository.ExpenseRepository
import com.bpeople.finpilot.data.repository.GoalRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlin.math.ceil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    goalRepository: GoalRepository,
) : ViewModel() {

    data class ExpenseUiState(
        val entries: List<ExpenseEntry> = emptyList(),
        val amount: String = "",
        val category: String = CATEGORIES.first(),
        val paymentMethod: String = PAYMENT_METHODS.first(),
        val dateMillis: Long = System.currentTimeMillis(),
        val note: String = "",
        val subCategory: String = "",
        val isRecurring: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val insightMessage: String? = null,
        val activeGoal: Goal? = null,
        val currency: String = CURRENCIES.first(),
    )

    private val _expenseState = MutableStateFlow(ExpenseUiState())
    val expenseState: StateFlow<ExpenseUiState> = _expenseState.asStateFlow()

    init {
        expenseRepository.observeExpenses()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            .let { flow ->
                viewModelScope.launch {
                    flow.collect { entries ->
                        _expenseState.update { it.copy(entries = entries) }
                    }
                }
            }

        goalRepository.observeActiveGoal()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
            .let { flow ->
                viewModelScope.launch {
                    flow.collect { goal ->
                        _expenseState.update { it.copy(activeGoal = goal) }
                    }
                }
            }
    }

    fun onAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _expenseState.update { it.copy(amount = filtered, errorMessage = null) }
    }

    fun onCategoryChange(value: String) {
        _expenseState.update { it.copy(category = value, errorMessage = null) }
    }

    fun onPaymentMethodChange(value: String) {
        _expenseState.update { it.copy(paymentMethod = value, errorMessage = null) }
    }

    fun onDateChange(value: Long) {
        _expenseState.update { it.copy(dateMillis = value, errorMessage = null) }
    }

    fun onNoteChange(value: String) {
        _expenseState.update { it.copy(note = value, errorMessage = null) }
    }

    fun onSubCategoryChange(value: String) {
        _expenseState.update { it.copy(subCategory = value, errorMessage = null) }
    }

    fun onRecurringChange(value: Boolean) {
        _expenseState.update { it.copy(isRecurring = value, errorMessage = null) }
    }

    fun consumeInsight() {
        _expenseState.update { it.copy(insightMessage = null) }
    }

    fun onCurrencyChange(value: String) {
        _expenseState.update { it.copy(currency = value, errorMessage = null) }
    }

    fun addExpense() {
        val state = _expenseState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _expenseState.update { it.copy(errorMessage = "Enter a valid amount") }
            return
        }

        val originalAmount = amount
        val baseAmount = if (state.currency == "LKR") amount else convertToBase(amount, state.currency)

        val entry = ExpenseEntry(
            id = UUID.randomUUID().toString(),
            amount = baseAmount,
            originalCurrency = state.currency,
            originalAmount = originalAmount,
            category = state.category,
            subCategory = state.subCategory.ifBlank { null },
            paymentMethod = state.paymentMethod,
            note = state.note.ifBlank { null },
            isRecurring = state.isRecurring,
            date = Timestamp(java.util.Date(state.dateMillis)),
        )

        _expenseState.update { it.copy(isLoading = true, errorMessage = null, insightMessage = null) }

        viewModelScope.launch {
            runCatching {
                expenseRepository.addExpense(entry)
            }.onSuccess {
                _expenseState.update {
                    it.copy(
                        amount = "",
                        note = "",
                        subCategory = "",
                        isRecurring = false,
                        isLoading = false,
                        errorMessage = null,
                        insightMessage = buildGoalInsight(amount, it.activeGoal),
                    )
                }
            }.onFailure { throwable ->
                _expenseState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to add expense",
                    )
                }
            }
        }
    }

    private fun buildGoalInsight(amount: Double, goal: Goal?): String {
        val monthlyRequired = goal?.monthlyRequired ?: 0.0
        if (goal == null || monthlyRequired <= 0.0) {
            return "Expense saved. This reduces your available savings by LKR ${"%.2f".format(amount)} today."
        }

        val dailyRequired = monthlyRequired / 30.0
        val delayDays = if (dailyRequired <= 0.0) 0 else ceil(amount / dailyRequired).toInt()
        return "Expense saved. At your current pace, this may delay ${goal.title} by about $delayDays day(s)."
    }

    private fun convertToBase(amount: Double, currency: String): Double {
        val rate = EXCHANGE_RATES[currency] ?: 1.0
        return amount * rate
    }

    companion object {
        val CATEGORIES = listOf("Food", "Transport", "Housing", "Subscriptions", "Entertainment", "Health", "Other")
        val PAYMENT_METHODS = listOf("Card", "Cash", "Bank Transfer", "Auto-Debit")
        val CURRENCIES = listOf("LKR", "USD", "EUR", "GBP", "AUD", "SGD")
        
        // Static exchange rates relative to 1 unit of foreign currency = X LKR
        // e.g., 1 USD = 300 LKR
        val EXCHANGE_RATES = mapOf(
            "LKR" to 1.0,
            "USD" to 300.0,
            "EUR" to 325.0,
            "GBP" to 380.0,
            "AUD" to 195.0,
            "SGD" to 220.0
        )
    }
}
