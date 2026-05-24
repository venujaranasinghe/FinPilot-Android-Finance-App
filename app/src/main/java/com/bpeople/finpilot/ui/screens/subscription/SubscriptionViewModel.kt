package com.bpeople.finpilot.ui.screens.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.Subscription
import com.bpeople.finpilot.data.repository.SubscriptionRepository
import com.bpeople.finpilot.data.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val repository: SubscriptionRepository,
) : ViewModel() {

    data class UiState(
        val subscriptions: List<Subscription> = emptyList(),
        val isLoading: Boolean = true,
        val showDialog: Boolean = false,
        val editingSub: Subscription? = null,
        val name: String = "",
        val amount: String = "",
        val billingCycle: String = "MONTHLY",
        val category: String = "Entertainment",
        val note: String = "",
        val errorMessage: String? = null,
        val successMessage: String? = null,
    ) {
        val active get() = subscriptions.filter { it.isActive }
        val monthlyTotal get() = active.sumOf { it.monthlyEquivalent }
        val yearlyTotal get() = monthlyTotal * 12
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeSubscriptions().collect { list ->
                _uiState.update { it.copy(subscriptions = list, isLoading = false) }
            }
        }
    }

    fun openAddDialog() = _uiState.update {
        it.copy(showDialog = true, editingSub = null,
            name = "", amount = "", billingCycle = "MONTHLY", category = "Entertainment", note = "")
    }

    fun openEditDialog(sub: Subscription) = _uiState.update {
        it.copy(showDialog = true, editingSub = sub,
            name = sub.name,
            amount = sub.amountLKR.toBigDecimal().stripTrailingZeros().toPlainString(),
            billingCycle = sub.billingCycle, category = sub.category, note = sub.note)
    }

    fun closeDialog() = _uiState.update { it.copy(showDialog = false) }

    fun setName(v: String) = _uiState.update { it.copy(name = v) }
    fun setAmount(v: String) = _uiState.update { it.copy(amount = v) }
    fun setBillingCycle(v: String) = _uiState.update { it.copy(billingCycle = v) }
    fun setCategory(v: String) = _uiState.update { it.copy(category = v) }
    fun setNote(v: String) = _uiState.update { it.copy(note = v) }

    fun saveSub() {
        val s = _uiState.value
        if (s.name.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Name is required") }
            return
        }
        val sub = s.editingSub?.copy(
            name = s.name,
            amountLKR = s.amount.toDoubleOrNull() ?: 0.0,
            billingCycle = s.billingCycle,
            category = s.category,
            note = s.note,
        ) ?: Subscription(
            name = s.name,
            amountLKR = s.amount.toDoubleOrNull() ?: 0.0,
            billingCycle = s.billingCycle,
            category = s.category,
            note = s.note,
        )
        viewModelScope.launch {
            repository.addOrUpdate(sub).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(showDialog = false, successMessage = "Subscription saved")
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(errorMessage = result.throwable.message ?: "Save failed")
                    }
                    else -> {}
                }
            }
        }
    }

    fun toggleActive(sub: Subscription) {
        viewModelScope.launch {
            repository.addOrUpdate(sub.copy(isActive = !sub.isActive)).collect {}
        }
    }

    fun deleteSub(id: String) {
        viewModelScope.launch {
            repository.delete(id).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update { it.copy(successMessage = "Subscription deleted") }
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
