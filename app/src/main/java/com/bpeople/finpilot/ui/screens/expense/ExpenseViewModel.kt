package com.bpeople.finpilot.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.repository.ExpenseRepository
import com.bpeople.finpilot.data.repository.GoalRepository
import com.bpeople.finpilot.data.repository.ExchangeRatesRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
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
    private val exchangeRatesRepository: ExchangeRatesRepository,
) : ViewModel() {

    enum class HistoryDateRange(val label: String) {
        ALL_TIME("All time"),
        THIS_MONTH("This month"),
        LAST_30_DAYS("Last 30 days"),
        LAST_7_DAYS("Last 7 days"),
    }

    data class ExpenseUiState(
        val entries: List<ExpenseEntry> = emptyList(),
        val filteredEntries: List<ExpenseEntry> = emptyList(),
        val amount: String = "",
        val category: String = CATEGORIES.first(),
        val paymentMethod: String = PAYMENT_METHODS.first(),
        val historyDateRange: HistoryDateRange = HistoryDateRange.ALL_TIME,
        val historyCategoryFilter: String? = null,
        val historyPaymentMethodFilter: String? = null,
        val dateMillis: Long = System.currentTimeMillis(),
        val note: String = "",
        val subCategory: String = "",
        val isRecurring: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val insightMessage: String? = null,
        val activeGoal: Goal? = null,
        val currency: String = CURRENCIES.first(),
        val exchangeRate: String = "1.0",
        val amountLkrPreview: Double = 0.0,
        val exchangeRateLastUpdatedMillis: Long? = null,
        val exchangeRateIsStale: Boolean = false,
        val exchangeRateAvailable: Boolean = true,
        val exchangeRateConfirmed: Boolean = true,
        val showRateConfirmation: Boolean = false,
        val isRefreshingRates: Boolean = false,
    )

    private var latestRatesSnapshot = ExchangeRatesRepository.ExchangeRatesSnapshot()

    private val _expenseState = MutableStateFlow(ExpenseUiState())
    val expenseState: StateFlow<ExpenseUiState> = _expenseState.asStateFlow()

    init {
        expenseRepository.observeExpenses()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            .let { flow ->
                viewModelScope.launch {
                    flow.collect { entries ->
                        _expenseState.update { current ->
                            val updated = current.copy(entries = entries)
                            updated.copy(filteredEntries = filterHistoryEntries(updated, entries))
                        }
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

        viewModelScope.launch {
            exchangeRatesRepository.refreshRatesIfNeeded()
        }

        viewModelScope.launch {
            exchangeRatesRepository.rates.collect { snapshot ->
                latestRatesSnapshot = snapshot
                _expenseState.update { current ->
                    val rate = exchangeRatesRepository.rateToLkr(snapshot, current.currency)
                    val rateAvailable = current.currency == "LKR" || rate != null
                    val resolvedRate = rate ?: 1.0
                    val rateChanged = current.exchangeRate.toDoubleOrNull() != resolvedRate
                    val confirmed = if (current.currency == "LKR") true
                    else if (rateChanged) false else current.exchangeRateConfirmed
                    val updated = current.copy(
                        exchangeRate = formatRate(resolvedRate),
                        exchangeRateLastUpdatedMillis = snapshot.lastUpdatedMillis.takeIf { it > 0 },
                        exchangeRateIsStale = snapshot.isStale,
                        exchangeRateAvailable = rateAvailable,
                        exchangeRateConfirmed = confirmed,
                    )
                    updated.copy(amountLkrPreview = calculateAmountLkr(updated))
                }
            }
        }
    }

    fun onAmountChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _expenseState.update { current ->
            val updated = current.copy(amount = filtered, errorMessage = null)
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun onCategoryChange(value: String) {
        _expenseState.update { it.copy(category = value, errorMessage = null) }
    }

    fun onPaymentMethodChange(value: String) {
        _expenseState.update { it.copy(paymentMethod = value, errorMessage = null) }
    }

    fun onHistoryDateRangeChange(value: HistoryDateRange) {
        _expenseState.update { current ->
            val updated = current.copy(historyDateRange = value)
            updated.copy(filteredEntries = filterHistoryEntries(updated, updated.entries))
        }
    }

    fun onHistoryCategoryFilterChange(value: String?) {
        _expenseState.update { current ->
            val updated = current.copy(historyCategoryFilter = value)
            updated.copy(filteredEntries = filterHistoryEntries(updated, updated.entries))
        }
    }

    fun onHistoryPaymentMethodFilterChange(value: String?) {
        _expenseState.update { current ->
            val updated = current.copy(historyPaymentMethodFilter = value)
            updated.copy(filteredEntries = filterHistoryEntries(updated, updated.entries))
        }
    }

    fun clearHistoryFilters() {
        _expenseState.update { current ->
            val updated = current.copy(
                historyDateRange = HistoryDateRange.ALL_TIME,
                historyCategoryFilter = null,
                historyPaymentMethodFilter = null,
            )
            updated.copy(filteredEntries = filterHistoryEntries(updated, updated.entries))
        }
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
        _expenseState.update { current ->
            val rate = exchangeRatesRepository.rateToLkr(latestRatesSnapshot, value)
            val rateAvailable = value == "LKR" || rate != null
            val updated = current.copy(
                currency = value,
                exchangeRate = formatRate(rate ?: 1.0),
                exchangeRateAvailable = rateAvailable,
                exchangeRateConfirmed = value == "LKR",
                errorMessage = null,
            )
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun requestSubmit() {
        val state = _expenseState.value
        if (state.currency != "LKR" && !state.exchangeRateAvailable) {
            _expenseState.update { it.copy(errorMessage = "Exchange rate unavailable. Try again later.") }
            return
        }
        if (state.currency != "LKR" && !state.exchangeRateConfirmed) {
            _expenseState.update { it.copy(showRateConfirmation = true, errorMessage = null) }
            return
        }
        addExpense()
    }

    fun confirmExchangeRate() {
        _expenseState.update { it.copy(exchangeRateConfirmed = true, showRateConfirmation = false) }
        requestSubmit()
    }

    fun dismissRateConfirmation() {
        _expenseState.update { it.copy(showRateConfirmation = false) }
    }

    fun refreshExchangeRates() {
        _expenseState.update { it.copy(isRefreshingRates = true) }
        viewModelScope.launch {
            exchangeRatesRepository.refreshRatesIfNeeded(force = true)
                .onFailure { error ->
                    _expenseState.update {
                        it.copy(errorMessage = error.message ?: "Failed to refresh exchange rates")
                    }
                }
            _expenseState.update { it.copy(isRefreshingRates = false) }
        }
    }

    fun addExpense() {
        val state = _expenseState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _expenseState.update { it.copy(errorMessage = "Enter a valid amount") }
            return
        }

        val baseAmount = calculateAmountLkr(state)

        val entry = ExpenseEntry(
            id = UUID.randomUUID().toString(),
            amount = baseAmount,
            originalCurrency = state.currency,
            originalAmount = amount,
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
                        exchangeRateConfirmed = true,
                        showRateConfirmation = false,
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

    private fun calculateAmountLkr(state: ExpenseUiState): Double {
        val amount = state.amount.toDoubleOrNull() ?: return 0.0
        val rate = state.exchangeRate.toDoubleOrNull() ?: 1.0
        return amount * rate
    }

    private fun filterHistoryEntries(
        state: ExpenseUiState,
        entries: List<ExpenseEntry>,
    ): List<ExpenseEntry> {
        val now = System.currentTimeMillis()
        val startMillis = when (state.historyDateRange) {
            HistoryDateRange.ALL_TIME -> null
            HistoryDateRange.THIS_MONTH -> Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            HistoryDateRange.LAST_30_DAYS -> now - 30L * 24 * 60 * 60 * 1000
            HistoryDateRange.LAST_7_DAYS -> now - 7L * 24 * 60 * 60 * 1000
        }

        return entries.filter { entry ->
            val entryMillis = entry.date?.toDate()?.time ?: return@filter false
            val matchesDate = startMillis == null || entryMillis >= startMillis
            val matchesCategory = state.historyCategoryFilter.isNullOrBlank() ||
                entry.category.equals(state.historyCategoryFilter, ignoreCase = true)
            val matchesPaymentMethod = state.historyPaymentMethodFilter.isNullOrBlank() ||
                entry.paymentMethod.equals(state.historyPaymentMethodFilter, ignoreCase = true)

            matchesDate && matchesCategory && matchesPaymentMethod
        }
    }

    private fun formatRate(rate: Double): String = String.format("%.4f", rate)

    companion object {
        val CATEGORIES = listOf("Food", "Transport", "Housing", "Subscriptions", "Entertainment", "Health", "Other")
        val PAYMENT_METHODS = listOf("Card", "Cash", "Bank Transfer", "Auto-Debit")
        val CURRENCIES = listOf("LKR", "USD", "EUR", "GBP", "AUD", "SGD")
    }
}
