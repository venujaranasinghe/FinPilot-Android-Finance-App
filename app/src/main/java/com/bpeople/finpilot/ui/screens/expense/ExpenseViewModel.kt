package com.bpeople.finpilot.ui.screens.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.data.repository.ExpenseRepository
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
class ExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) : ViewModel() {

    data class ExpenseUiState(
        val entries: List<ExpenseEntry> = emptyList(),
        val amount: String = "",
        val category: String = "OTHER",
        val paymentMethod: String = "CASH",
        val note: String = "",
        val isRecurring: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null,
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
    }

    fun onAmountChange(value: String) {
        _expenseState.update { it.copy(amount = value, errorMessage = null) }
    }

    fun onCategoryChange(value: String) {
        _expenseState.update { it.copy(category = value, errorMessage = null) }
    }

    fun onPaymentMethodChange(value: String) {
        _expenseState.update { it.copy(paymentMethod = value, errorMessage = null) }
    }

    fun onNoteChange(value: String) {
        _expenseState.update { it.copy(note = value, errorMessage = null) }
    }

    fun onRecurringChange(value: Boolean) {
        _expenseState.update { it.copy(isRecurring = value, errorMessage = null) }
    }

    fun submitExpense() {
        val state = _expenseState.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _expenseState.update { it.copy(errorMessage = "Enter a valid amount") }
            return
        }

        val entry = ExpenseEntry(
            id = UUID.randomUUID().toString(),
            amount = amount,
            category = state.category,
            paymentMethod = state.paymentMethod,
            note = state.note.ifBlank { null },
            isRecurring = state.isRecurring,
            date = Timestamp.now(),
        )

        viewModelScope.launch {
            expenseRepository.addExpense(entry)
            _expenseState.update {
                it.copy(
                    amount = "",
                    note = "",
                    isRecurring = false,
                    errorMessage = null,
                )
            }
        }
    }
}

