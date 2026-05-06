package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.IncomeEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepository @Inject constructor() {
    private val _incomeEntries = MutableStateFlow<List<IncomeEntry>>(emptyList())

    fun observeIncome(): Flow<List<IncomeEntry>> = _incomeEntries.asStateFlow()

    suspend fun addIncome(entry: IncomeEntry) {
        _incomeEntries.update { current -> current + entry }
    }
}

