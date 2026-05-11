package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.IncomeEntry
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncomeRepository @Inject constructor() {
    private val _incomeEntries = MutableStateFlow<List<IncomeEntry>>(
        listOf(
            IncomeEntry(
                id = "1",
                source = "Salary",
                amountLKR = 450000.0,
                amountOriginal = 450000.0,
                currencyOriginal = "LKR",
                exchangeRate = 1.0,
                date = Timestamp(Date()),
                label = "Monthly salary"
            ),
            IncomeEntry(
                id = "2",
                source = "Freelance",
                amountLKR = 75000.0,
                amountOriginal = 75000.0,
                currencyOriginal = "LKR",
                exchangeRate = 1.0,
                date = Timestamp(Date(System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000))),
                label = "Web development project"
            ),
            IncomeEntry(
                id = "3",
                source = "AdSense",
                amountLKR = 18000.0,
                amountOriginal = 18000.0,
                currencyOriginal = "LKR",
                exchangeRate = 1.0,
                date = Timestamp(Date(System.currentTimeMillis() - (14 * 24 * 60 * 60 * 1000))),
                label = "YouTube earnings"
            ),
        )
    )

    fun observeIncome(): Flow<List<IncomeEntry>> = _incomeEntries.asStateFlow()

    suspend fun addIncome(entry: IncomeEntry) {
        _incomeEntries.update { current -> current + entry }
    }
}
