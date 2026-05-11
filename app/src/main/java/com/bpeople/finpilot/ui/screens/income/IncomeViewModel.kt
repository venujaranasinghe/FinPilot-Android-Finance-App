package com.bpeople.finpilot.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.FreelanceProject
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.data.repository.FreelanceProjectRepository
import com.bpeople.finpilot.data.repository.IncomeRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val freelanceProjectRepository: FreelanceProjectRepository,
) : ViewModel() {

    data class IncomeUiState(
        val entries: List<IncomeEntry> = emptyList(),
        val projects: List<FreelanceProject> = emptyList(),
        val source: String = SOURCES.first(),
        val amountOriginal: String = "",
        val currencyOriginal: String = "LKR",
        val exchangeRate: String = "1.0",
        val amountLkrPreview: Double = 0.0,
        val dateMillis: Long = System.currentTimeMillis(),
        val label: String = "",
        val incomeType: String = INCOME_TYPES.first(),
        val projectRef: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
        val isSubmitted: Boolean = false,
    )

    private val _incomeState = MutableStateFlow(IncomeUiState())
    val incomeState: StateFlow<IncomeUiState> = _incomeState.asStateFlow()

    init {
        incomeRepository.observeIncome()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            .let { flow ->
                viewModelScope.launch {
                    flow.collect { entries ->
                        _incomeState.update { it.copy(entries = entries) }
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
    }

    fun onSourceChange(value: String) {
        _incomeState.update { current ->
            val newCurrency = if (value == "Salary") "LKR" else current.currencyOriginal
            val newRate = if (newCurrency == "LKR") "1.0" else current.exchangeRate
            val updated = current.copy(
                source = value,
                currencyOriginal = newCurrency,
                exchangeRate = newRate,
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
            val newRate = if (value == "LKR") "1.0" else current.exchangeRate
            val updated = current.copy(currencyOriginal = value, exchangeRate = newRate, errorMessage = null)
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun onExchangeRateChange(value: String) {
        val filtered = value.filter { it.isDigit() || it == '.' }
        _incomeState.update { current ->
            val updated = current.copy(exchangeRate = filtered, errorMessage = null)
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

    fun consumeSubmitted() {
        _incomeState.update { it.copy(isSubmitted = false) }
    }

    fun addIncome() {
        val state = _incomeState.value
        val amountOriginal = state.amountOriginal.toDoubleOrNull()
        val exchangeRate = state.exchangeRate.toDoubleOrNull()

        if (amountOriginal == null || amountOriginal <= 0) {
            _incomeState.update { it.copy(errorMessage = "Enter a valid amount") }
            return
        }
        if (state.currencyOriginal != "LKR" && (exchangeRate == null || exchangeRate <= 0)) {
            _incomeState.update { it.copy(errorMessage = "Enter a valid exchange rate") }
            return
        }

        val resolvedRate = if (state.currencyOriginal == "LKR") 1.0 else (exchangeRate ?: 1.0)
        val amountLkr = calculateAmountLkr(state)
        val typeKey = INCOME_TYPE_KEYS[state.incomeType] ?: "ONE_OFF"

        val entry = IncomeEntry(
            id = UUID.randomUUID().toString(),
            source = state.source,
            amountOriginal = amountOriginal,
            currencyOriginal = state.currencyOriginal,
            amountLKR = amountLkr,
            exchangeRate = resolvedRate,
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
                        currencyOriginal = if (it.source == "Salary") "LKR" else "LKR",
                        exchangeRate = "1.0",
                        amountLkrPreview = 0.0,
                        label = "",
                        projectRef = "",
                        isLoading = false,
                        errorMessage = null,
                        isSubmitted = true,
                    )
                }
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

    private fun calculateAmountLkr(state: IncomeUiState): Double {
        val amountOriginal = state.amountOriginal.toDoubleOrNull() ?: return 0.0
        val exchangeRate = state.exchangeRate.toDoubleOrNull() ?: return 0.0
        return if (state.currencyOriginal.uppercase() == "LKR") {
            amountOriginal
        } else {
            amountOriginal * exchangeRate
        }
    }

    companion object {
        val SOURCES = listOf("Salary", "Freelance", "AdSense", "Crypto", "Other")
        val CURRENCIES = listOf("LKR", "USD", "USDT", "ETH")
        val INCOME_TYPES = listOf("One-off", "Recurring", "Variable")
        val INCOME_TYPE_KEYS = mapOf(
            "One-off" to "ONE_OFF",
            "Recurring" to "RECURRING",
            "Variable" to "VARIABLE",
        )
    }
}

