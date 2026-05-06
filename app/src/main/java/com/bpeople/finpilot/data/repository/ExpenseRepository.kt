package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.ExpenseEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor() {
    private val _expenseEntries = MutableStateFlow<List<ExpenseEntry>>(emptyList())

    fun observeExpenses(): Flow<List<ExpenseEntry>> = _expenseEntries.asStateFlow()

    suspend fun addExpense(entry: ExpenseEntry) {
        _expenseEntries.update { current -> current + entry }
    }
}

