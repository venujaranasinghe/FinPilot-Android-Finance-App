package com.bpeople.finpilot.ui.screens.crypto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.CryptoEntry
import com.bpeople.finpilot.data.repository.CryptoRepository
import com.bpeople.finpilot.data.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CryptoPnlViewModel @Inject constructor(
    private val repository: CryptoRepository,
) : ViewModel() {

    data class UiState(
        val holdings: List<CryptoEntry> = emptyList(),
        val isLoading: Boolean = true,
        val showDialog: Boolean = false,
        val editingEntry: CryptoEntry? = null,
        val symbol: String = "",
        val name: String = "",
        val quantity: String = "",
        val buyPrice: String = "",
        val currentPrice: String = "",
        val note: String = "",
        val errorMessage: String? = null,
        val successMessage: String? = null,
    ) {
        val totalInvested get() = holdings.sumOf { it.investedLKR }
        val totalCurrentValue get() = holdings.sumOf { it.currentValueLKR }
        val netPnl get() = totalCurrentValue - totalInvested
        val netPnlPercent get() = if (totalInvested > 0) (netPnl / totalInvested) * 100 else 0.0
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeHoldings().collect { list ->
                _uiState.update { it.copy(holdings = list, isLoading = false) }
            }
        }
    }

    fun openAddDialog() = _uiState.update {
        it.copy(showDialog = true, editingEntry = null,
            symbol = "", name = "", quantity = "", buyPrice = "", currentPrice = "", note = "")
    }

    fun openEditDialog(entry: CryptoEntry) = _uiState.update {
        it.copy(showDialog = true, editingEntry = entry,
            symbol = entry.symbol, name = entry.name,
            quantity = entry.quantity.toBigDecimal().stripTrailingZeros().toPlainString(),
            buyPrice = entry.buyPriceLKR.toBigDecimal().stripTrailingZeros().toPlainString(),
            currentPrice = entry.currentPriceLKR.toBigDecimal().stripTrailingZeros().toPlainString(),
            note = entry.note)
    }

    fun closeDialog() = _uiState.update { it.copy(showDialog = false) }

    fun setSymbol(v: String) = _uiState.update { it.copy(symbol = v.uppercase()) }
    fun setName(v: String) = _uiState.update { it.copy(name = v) }
    fun setQuantity(v: String) = _uiState.update { it.copy(quantity = v) }
    fun setBuyPrice(v: String) = _uiState.update { it.copy(buyPrice = v) }
    fun setCurrentPrice(v: String) = _uiState.update { it.copy(currentPrice = v) }
    fun setNote(v: String) = _uiState.update { it.copy(note = v) }

    fun saveHolding() {
        val s = _uiState.value
        if (s.symbol.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Symbol is required (e.g. BTC)") }
            return
        }
        val entry = s.editingEntry?.copy(
            symbol = s.symbol, name = s.name,
            quantity = s.quantity.toDoubleOrNull() ?: 0.0,
            buyPriceLKR = s.buyPrice.toDoubleOrNull() ?: 0.0,
            currentPriceLKR = s.currentPrice.toDoubleOrNull() ?: 0.0,
            note = s.note,
        ) ?: CryptoEntry(
            symbol = s.symbol, name = s.name,
            quantity = s.quantity.toDoubleOrNull() ?: 0.0,
            buyPriceLKR = s.buyPrice.toDoubleOrNull() ?: 0.0,
            currentPriceLKR = s.currentPrice.toDoubleOrNull() ?: 0.0,
            note = s.note,
        )
        viewModelScope.launch {
            repository.addOrUpdate(entry).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(showDialog = false, successMessage = "Holding saved")
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(errorMessage = result.throwable.message ?: "Save failed")
                    }
                    else -> {}
                }
            }
        }
    }

    fun deleteHolding(id: String) {
        viewModelScope.launch {
            repository.delete(id).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update { it.copy(successMessage = "Holding deleted") }
                    is Result.Error -> _uiState.update {
                        it.copy(errorMessage = result.throwable.message ?: "Delete failed")
                    }
                    else -> {}
                }
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
}
