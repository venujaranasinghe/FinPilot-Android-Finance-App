package com.bpeople.finpilot.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.bpeople.finpilot.data.model.FreelanceProject
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.data.repository.ExchangeRatesRepository
import com.bpeople.finpilot.data.repository.FreelanceProjectRepository
import com.bpeople.finpilot.data.repository.IncomeRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val freelanceProjectRepository: FreelanceProjectRepository,
    private val exchangeRatesRepository: ExchangeRatesRepository,
) : ViewModel() {

    enum class HistoryDateRange(val label: String) {
        ALL_TIME("All time"),
        THIS_MONTH("This month"),
        LAST_30_DAYS("Last 30 days"),
        LAST_7_DAYS("Last 7 days"),
    }

    data class IncomeUiState(
        val entries: List<IncomeEntry> = emptyList(),
        val filteredEntries: List<IncomeEntry> = emptyList(),
        val projects: List<FreelanceProject> = emptyList(),
        val source: String = SOURCES.first(),
        val historyDateRange: HistoryDateRange = HistoryDateRange.ALL_TIME,
        val historySourceFilter: String? = null,
        val historyIncomeTypeFilter: String? = null,
        val amountOriginal: String = "",
        val currencyOriginal: String = "LKR",
        val exchangeRate: String = "1.0",
        val amountLkrPreview: Double = 0.0,
        val exchangeRateLastUpdatedMillis: Long? = null,
        val exchangeRateIsStale: Boolean = false,
        val exchangeRateAvailable: Boolean = true,
        val exchangeRateConfirmed: Boolean = true,
        val exchangeRateManualOverride: Boolean = false,
        val showRateConfirmation: Boolean = false,
        val isRefreshingRates: Boolean = false,
        val dateMillis: Long = System.currentTimeMillis(),
        val label: String = "",
        val incomeType: String = INCOME_TYPES.first(),
        val projectRef: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSubmitted: Boolean = false,
        // Sheet & delete state
        val showAddSheet: Boolean = false,
        val pendingDeleteEntry: IncomeEntry? = null,
        val showMonthlyView: Boolean = true,
    )

    private var latestRatesSnapshot = ExchangeRatesRepository.ExchangeRatesSnapshot()

    private val _incomeState = MutableStateFlow(IncomeUiState())
    val incomeState: StateFlow<IncomeUiState> = _incomeState.asStateFlow()
    private val historyRefreshTrigger = MutableStateFlow(0)

    val pagedHistory: kotlinx.coroutines.flow.Flow<PagingData<IncomeEntry>> = combine(
        _incomeState
            .map { it.historySourceFilter }
            .distinctUntilChanged(),
        historyRefreshTrigger,
    ) { sourceFilter, _ -> sourceFilter }
        .flatMapLatest { sourceFilter -> incomeRepository.observeIncomePaged(sourceFilter) }
        .cachedIn(viewModelScope)

    init {
        incomeRepository.observeIncome()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            .let { flow ->
                viewModelScope.launch {
                    flow.collect { entries ->
                        _incomeState.update { current ->
                            val updated = current.copy(entries = entries)
                            updated.copy(filteredEntries = filterHistoryEntries(updated, entries))
                        }
                    }
                }
            }

        freelanceProjectRepository.observeProjects()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            .let { flow ->
                viewModelScope.launch {
                    flow.collect { projects ->
                        _incomeState.update { it.copy(projects = projects) }
                    }
                }
            }

        viewModelScope.launch {
            exchangeRatesRepository.refreshRatesIfNeeded()
        }

        viewModelScope.launch {
            exchangeRatesRepository.rates.collect { snapshot ->
                latestRatesSnapshot = snapshot
                _incomeState.update { current ->
                    val rate = exchangeRatesRepository.rateToLkr(snapshot, current.currencyOriginal)
                    val rateAvailable = current.currencyOriginal == "LKR" || rate != null
                    val resolvedRate = rate ?: 1.0
                    val shouldUseRepoRate = current.currencyOriginal != "LKR" && !current.exchangeRateManualOverride
                    val exchangeRateValue = when {
                        current.currencyOriginal == "LKR" -> "1.0"
                        shouldUseRepoRate && rate != null -> formatRate(resolvedRate)
                        else -> current.exchangeRate
                    }
                    val rateChanged = current.exchangeRate.toDoubleOrNull() != resolvedRate
                    val confirmed = if (current.currencyOriginal == "LKR") true
                    else if (rateChanged) false else current.exchangeRateConfirmed
                    val updated = current.copy(
                        exchangeRate = exchangeRateValue,
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

    fun onSourceChange(value: String) {
        _incomeState.update { current ->
            val updated = current.copy(
                source = value,
                projectRef = if (value != "Freelance") "" else current.projectRef,
                errorMessage = null,
            )
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun onAmountOriginalChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _incomeState.update { current ->
            val updated = current.copy(amountOriginal = filtered, errorMessage = null)
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun onCurrencyChange(value: String) {
        _incomeState.update { current ->
            val rate = exchangeRatesRepository.rateToLkr(latestRatesSnapshot, value)
            val rateAvailable = value == "LKR" || rate != null
            val updated = current.copy(
                currencyOriginal = value,
                exchangeRate = when {
                    value == "LKR" -> "1.0"
                    rate != null -> formatRate(rate)
                    else -> ""
                },
                exchangeRateAvailable = rateAvailable,
                exchangeRateConfirmed = value == "LKR",
                exchangeRateManualOverride = false,
                errorMessage = null,
            )
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun onExchangeRateChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _incomeState.update { current ->
            val updated = current.copy(
                exchangeRate = filtered,
                exchangeRateManualOverride = true,
                errorMessage = null,
            )
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun onLabelChange(value: String) {
        _incomeState.update { it.copy(label = value, errorMessage = null) }
    }

    fun onDateChange(value: Long) {
        _incomeState.update { it.copy(dateMillis = value, errorMessage = null) }
    }

    fun onIncomeTypeChange(value: String) {
        _incomeState.update { it.copy(incomeType = value, errorMessage = null) }
    }

    fun onProjectRefChange(value: String) {
        _incomeState.update { it.copy(projectRef = value, errorMessage = null) }
    }

    fun onHistoryDateRangeChange(value: HistoryDateRange) {
        _incomeState.update { current ->
            val updated = current.copy(historyDateRange = value)
            updated.copy(filteredEntries = filterHistoryEntries(updated, updated.entries))
        }
    }

    fun onHistorySourceFilterChange(value: String?) {
        _incomeState.update { current ->
            val updated = current.copy(historySourceFilter = value)
            updated.copy(filteredEntries = filterHistoryEntries(updated, updated.entries))
        }
        refreshHistory()
    }

    fun onHistoryIncomeTypeFilterChange(value: String?) {
        _incomeState.update { current ->
            val updated = current.copy(historyIncomeTypeFilter = value)
            updated.copy(filteredEntries = filterHistoryEntries(updated, updated.entries))
        }
    }

    fun clearHistoryFilters() {
        _incomeState.update { current ->
            val updated = current.copy(
                historyDateRange = HistoryDateRange.ALL_TIME,
                historySourceFilter = null,
                historyIncomeTypeFilter = null,
            )
            updated.copy(filteredEntries = filterHistoryEntries(updated, updated.entries))
        }
        refreshHistory()
    }

    fun consumeSubmitted() {
        _incomeState.update { it.copy(isSubmitted = false) }
    }

    fun requestSubmit() {
        val state = _incomeState.value
        if (state.currencyOriginal != "LKR" && !state.exchangeRateAvailable) {
            _incomeState.update { it.copy(errorMessage = "Exchange rate unavailable. Try again later.") }
            return
        }
        if (state.currencyOriginal != "LKR" && !state.exchangeRateConfirmed) {
            _incomeState.update { it.copy(showRateConfirmation = true, errorMessage = null) }
            return
        }
        addIncome()
    }

    fun confirmExchangeRate() {
        _incomeState.update { it.copy(exchangeRateConfirmed = true, showRateConfirmation = false) }
        requestSubmit()
    }

    fun dismissRateConfirmation() {
        _incomeState.update { it.copy(showRateConfirmation = false) }
    }

    fun refreshExchangeRates() {
        _incomeState.update { it.copy(isRefreshingRates = true) }
        viewModelScope.launch {
            exchangeRatesRepository.refreshRatesIfNeeded(force = true)
                .onFailure { error ->
                    _incomeState.update {
                        it.copy(errorMessage = error.message ?: "Failed to refresh exchange rates")
                    }
                }
            _incomeState.update { it.copy(isRefreshingRates = false) }
        }
    }

    fun addIncome() {
        val state = _incomeState.value
        val amountOriginal = state.amountOriginal.toDoubleOrNull()

        if (amountOriginal == null || amountOriginal <= 0) {
            _incomeState.update { it.copy(errorMessage = "Enter a valid amount") }
            return
        }

        val effectiveCurrency = state.currencyOriginal
        val resolvedRate = if (effectiveCurrency == "LKR") 1.0 else state.exchangeRate.toDoubleOrNull()

        if (effectiveCurrency != "LKR" && (resolvedRate == null || resolvedRate <= 0.0)) {
            _incomeState.update { it.copy(errorMessage = "Enter a valid exchange rate") }
            return
        }

        val amountLkr = amountOriginal * (resolvedRate ?: 1.0)
        val typeKey = INCOME_TYPE_KEYS[state.incomeType] ?: "ONE_OFF"

        val entry = IncomeEntry(
            id = UUID.randomUUID().toString(),
            source = state.source,
            amountOriginal = amountOriginal,
            currencyOriginal = effectiveCurrency,
            amountLKR = amountLkr,
            exchangeRate = resolvedRate ?: 1.0,
            date = Timestamp(Date(state.dateMillis)),
            label = state.label.ifBlank { null },
            type = typeKey,
            projectRef = if (state.source == "Freelance") state.projectRef.ifBlank { null } else null,
        )

        _incomeState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            runCatching {
                incomeRepository.addIncome(entry)
            }.onSuccess {
                _incomeState.update {
                    it.copy(
                        amountOriginal = "",
                        currencyOriginal = "LKR",
                        exchangeRate = "1.0",
                        amountLkrPreview = 0.0,
                        exchangeRateLastUpdatedMillis = it.exchangeRateLastUpdatedMillis,
                        exchangeRateIsStale = it.exchangeRateIsStale,
                        exchangeRateAvailable = true,
                        exchangeRateConfirmed = true,
                        exchangeRateManualOverride = false,
                        showRateConfirmation = false,
                        label = "",
                        projectRef = "",
                        showAddSheet = false,
                        isLoading = false,
                        errorMessage = null,
                        isSubmitted = true,
                    )
                }
                refreshHistory()
            }.onFailure { throwable ->
                _incomeState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Failed to save income",
                    )
                }
            }
        }
    }

    fun onShowAddSheet() {
        _incomeState.update {
            it.copy(
                showAddSheet = true,
                amountOriginal = "",
                currencyOriginal = "LKR",
                exchangeRate = "1.0",
                amountLkrPreview = 0.0,
                exchangeRateConfirmed = true,
                exchangeRateManualOverride = false,
                showRateConfirmation = false,
                dateMillis = System.currentTimeMillis(),
                label = "",
                projectRef = "",
                errorMessage = null,
                isSubmitted = false,
            )
        }
    }

    fun onHideAddSheet() {
        _incomeState.update { current ->
            current.copy(
                showAddSheet = false,
                amountOriginal = "",
                currencyOriginal = "LKR",
                exchangeRate = "1.0",
                amountLkrPreview = 0.0,
                exchangeRateConfirmed = true,
                exchangeRateManualOverride = false,
                showRateConfirmation = false,
                label = "",
                projectRef = "",
                errorMessage = null,
            )
        }
    }

    fun onToggleMonthlyView() {
        _incomeState.update { it.copy(showMonthlyView = !it.showMonthlyView) }
    }

    fun deleteIncome(entry: IncomeEntry) {
        _incomeState.update { it.copy(pendingDeleteEntry = entry) }
        viewModelScope.launch {
            runCatching { incomeRepository.deleteIncome(entry.id) }
                .onSuccess { refreshHistory() }
                .onFailure { t ->
                    _incomeState.update {
                        it.copy(
                            pendingDeleteEntry = null,
                            errorMessage = t.message ?: "Failed to delete entry",
                        )
                    }
                }
        }
    }

    fun undoDelete() {
        val entry = _incomeState.value.pendingDeleteEntry ?: return
        _incomeState.update { it.copy(pendingDeleteEntry = null) }
        viewModelScope.launch {
            runCatching { incomeRepository.addIncome(entry) }
                .onSuccess { refreshHistory() }
        }
    }

    fun consumePendingDelete() {
        _incomeState.update { it.copy(pendingDeleteEntry = null) }
    }

    fun consumeError() {
        _incomeState.update { it.copy(errorMessage = null) }
    }

    private fun calculateAmountLkr(state: IncomeUiState): Double {
        val amountOriginal = state.amountOriginal.toDoubleOrNull() ?: return 0.0
        val rate = if (state.currencyOriginal == "LKR") 1.0 else state.exchangeRate.toDoubleOrNull() ?: return 0.0
        return amountOriginal * rate
    }

    private fun filterHistoryEntries(
        state: IncomeUiState,
        entries: List<IncomeEntry>,
    ): List<IncomeEntry> {
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
            val displayType = DISPLAY_TYPE_BY_KEY[entry.type] ?: entry.type
            val matchesDate = startMillis == null || entryMillis >= startMillis
            val matchesSource = state.historySourceFilter.isNullOrBlank() ||
                entry.source.equals(state.historySourceFilter, ignoreCase = true)
            val matchesIncomeType = state.historyIncomeTypeFilter.isNullOrBlank() ||
                displayType.equals(state.historyIncomeTypeFilter, ignoreCase = true)

            matchesDate && matchesSource && matchesIncomeType
        }
    }

    private fun formatRate(rate: Double): String = String.format("%.4f", rate)

    private fun refreshHistory() {
        historyRefreshTrigger.update { it + 1 }
    }

    companion object {
        val SOURCES = listOf("Salary", "Freelance", "AdSense", "Crypto", "Other")
        val CURRENCIES = listOf("LKR", "USD", "USDT", "ETH")
        val INCOME_TYPES = listOf("Recurring", "One-off", "Variable")
        val INCOME_TYPE_KEYS = mapOf(
            "One-off" to "ONE_OFF",
            "Recurring" to "RECURRING",
            "Variable" to "VARIABLE",
        )
        val DISPLAY_TYPE_BY_KEY = INCOME_TYPE_KEYS.entries.associate { (display, key) -> key to display }
    }
}
