package com.bpeople.finpilot.ui.screens.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.data.repository.IncomeRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
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
) : ViewModel() {

    data class IncomeUiState(
        val entries: List<IncomeEntry> = emptyList(),
        val source: String = "",
        val amountOriginal: String = "",
        val currencyOriginal: String = "LKR",
        val exchangeRate: String = "1.0",
        val amountLkrPreview: Double = 0.0,
        val label: String = "",
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
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
    }

    fun onSourceChange(value: String) {
        _incomeState.update { it.copy(source = value, errorMessage = null) }
    }

    fun onAmountOriginalChange(value: String) {
        _incomeState.update { current ->
            val updated = current.copy(amountOriginal = value, errorMessage = null)
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun onCurrencyChange(value: String) {
        _incomeState.update { current ->
            val updated = current.copy(currencyOriginal = value, errorMessage = null)
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun onExchangeRateChange(value: String) {
        _incomeState.update { current ->
            val updated = current.copy(exchangeRate = value, errorMessage = null)
            updated.copy(amountLkrPreview = calculateAmountLkr(updated))
        }
    }

    fun onLabelChange(value: String) {
        _incomeState.update { it.copy(label = value, errorMessage = null) }
    }

    fun submitIncome() {
        val state = _incomeState.value
        val amountOriginal = state.amountOriginal.toDoubleOrNull()
        val exchangeRate = state.exchangeRate.toDoubleOrNull()

        if (amountOriginal == null || amountOriginal <= 0) {
            _incomeState.update { it.copy(errorMessage = "Enter a valid amount") }
            return
        }
        if (exchangeRate == null || exchangeRate <= 0) {
            _incomeState.update { it.copy(errorMessage = "Enter a valid exchange rate") }
            return
        }

        val amountLkr = calculateAmountLkr(state)
        val entry = IncomeEntry(
            id = UUID.randomUUID().toString(),
            source = state.source,
            amountOriginal = amountOriginal,
            currencyOriginal = state.currencyOriginal,
            amountLKR = amountLkr,
            exchangeRate = exchangeRate,
            date = Timestamp.now(),
            label = state.label.ifBlank { null },
        )

        viewModelScope.launch {
            incomeRepository.addIncome(entry)
            _incomeState.update {
                it.copy(
                    amountOriginal = "",
                    exchangeRate = "1.0",
                    amountLkrPreview = 0.0,
                    label = "",
                    errorMessage = null,
                )
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
}

