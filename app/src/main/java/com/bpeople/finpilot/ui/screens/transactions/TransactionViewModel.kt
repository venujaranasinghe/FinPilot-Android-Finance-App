package com.bpeople.finpilot.ui.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.MonthlyBarData
import com.bpeople.finpilot.data.model.Period
import com.bpeople.finpilot.data.model.TransactionItem
import com.bpeople.finpilot.data.model.TransactionType
import com.bpeople.finpilot.data.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
) : ViewModel() {

    private val _selectedPeriod = MutableStateFlow(Period.MONTH)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<String?>(null)
    val selectedCategoryFilter: StateFlow<String?> = _selectedCategoryFilter.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _editingTransaction = MutableStateFlow<TransactionItem?>(null)
    val editingTransaction: StateFlow<TransactionItem?> = _editingTransaction.asStateFlow()

    private val _pendingDelete = MutableStateFlow<TransactionItem?>(null)
    val pendingDelete: StateFlow<TransactionItem?> = _pendingDelete.asStateFlow()

    private val allTransactions: StateFlow<List<TransactionItem>> =
        transactionRepository.observeTransactions()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val transactions: StateFlow<List<TransactionItem>> = combine(
        allTransactions,
        _selectedPeriod,
        _searchQuery,
        _selectedCategoryFilter,
        _pendingDelete,
    ) { txns, period, query, catFilter, pendingDeleteItem ->
        txns
            .filter { it.id != pendingDeleteItem?.id }
            .filter { it.isInPeriod(period) }
            .filter { catFilter == null || it.source.equals(catFilter, ignoreCase = true) }
            .filter {
                query.isBlank() ||
                    it.displayName.contains(query, ignoreCase = true) ||
                    it.note?.contains(query, ignoreCase = true) == true
            }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val totalIncome: StateFlow<Double> = combine(allTransactions, _selectedPeriod) { txns, period ->
        txns.filter { it.type == TransactionType.INCOME && it.isInPeriod(period) }
            .sumOf { it.amountInLKR }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val totalExpenses: StateFlow<Double> = combine(allTransactions, _selectedPeriod) { txns, period ->
        txns.filter { it.type == TransactionType.EXPENSE && it.isInPeriod(period) }
            .sumOf { it.amountInLKR }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val netSavings: StateFlow<Double> = combine(totalIncome, totalExpenses) { inc, exp ->
        inc - exp
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val incomeBySource: StateFlow<Map<String, Double>> = combine(allTransactions, _selectedPeriod) { txns, period ->
        txns.filter { it.type == TransactionType.INCOME && it.isInPeriod(period) }
            .groupBy { it.source.lowercase().trim() }
            .mapValues { (_, items) -> items.sumOf { it.amountInLKR } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val expenseByCategory: StateFlow<Map<String, Double>> = combine(allTransactions, _selectedPeriod) { txns, period ->
        txns.filter { it.type == TransactionType.EXPENSE && it.isInPeriod(period) }
            .groupBy { it.source.lowercase().trim() }
            .mapValues { (_, items) -> items.sumOf { it.amountInLKR } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val monthlyBarData: StateFlow<List<MonthlyBarData>> = allTransactions.map { txns ->
        val fmt = SimpleDateFormat("MMM", Locale.getDefault())
        (5 downTo 0).map { monthsAgo ->
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -monthsAgo) }
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH)
            val monthLabel = fmt.format(cal.time)
            val income = txns.filter {
                val d = Calendar.getInstance().apply { timeInMillis = it.timestampMillis }
                d.get(Calendar.YEAR) == year && d.get(Calendar.MONTH) == month &&
                    it.type == TransactionType.INCOME
            }.sumOf { it.amountInLKR }
            val expense = txns.filter {
                val d = Calendar.getInstance().apply { timeInMillis = it.timestampMillis }
                d.get(Calendar.YEAR) == year && d.get(Calendar.MONTH) == month &&
                    it.type == TransactionType.EXPENSE
            }.sumOf { it.amountInLKR }
            MonthlyBarData(monthLabel, income, expense)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ── Actions ────────────────────────────────────────────────────────────────

    fun selectPeriod(period: Period) {
        _selectedPeriod.value = period
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchActive(active: Boolean) {
        _isSearchActive.value = active
        if (!active) _searchQuery.value = ""
    }

    fun selectCategoryFilter(category: String?) {
        _selectedCategoryFilter.value =
            if (_selectedCategoryFilter.value.equals(category, ignoreCase = true)) null else category
    }

    fun startEditTransaction(transaction: TransactionItem) {
        _editingTransaction.value = transaction
    }

    fun clearEditTransaction() {
        _editingTransaction.value = null
    }

    fun markForDelete(item: TransactionItem) {
        _pendingDelete.value = item
    }

    fun undoDelete() {
        _pendingDelete.value = null
    }

    fun confirmDelete() {
        val item = _pendingDelete.value ?: return
        viewModelScope.launch {
            transactionRepository.deleteTransaction(item)
            _pendingDelete.value = null
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(800)
            _isRefreshing.value = false
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun TransactionItem.isInPeriod(period: Period): Boolean {
        if (timestampMillis == 0L) return false
        val now = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = timestampMillis }
        return when (period) {
            Period.WEEK -> {
                val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }
                !date.before(weekAgo)
            }
            Period.MONTH ->
                date.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                    date.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            Period.YEAR ->
                date.get(Calendar.YEAR) == now.get(Calendar.YEAR)
        }
    }
}
