package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.ExpenseEntry
import com.google.firebase.Timestamp
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExpenseRepository @Inject constructor() {
    private val _expenseEntries = MutableStateFlow<List<ExpenseEntry>>(
        listOf(
            ExpenseEntry(
                id = "e1",
                amount = 2500.0,
                category = "Food",
                date = Timestamp(Date()),
                note = "Lunch at restaurant"
            ),
            ExpenseEntry(
                id = "e2",
                amount = 1800.0,
                category = "Transport",
                date = Timestamp(Date(System.currentTimeMillis() - (1 * 24 * 60 * 60 * 1000))),
                note = "PickMe rides"
            ),
            ExpenseEntry(
                id = "e3",
                amount = 15000.0,
                category = "Housing",
                date = Timestamp(Date(System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000))),
                note = "Rent payment"
            ),
            ExpenseEntry(
                id = "e4",
                amount = 3200.0,
                category = "Entertainment",
                date = Timestamp(Date(System.currentTimeMillis() - (3 * 24 * 60 * 60 * 1000))),
                note = "Movie and dinner"
            ),
            ExpenseEntry(
                id = "e5",
                amount = 5000.0,
                category = "Subscriptions",
                date = Timestamp(Date(System.currentTimeMillis() - (4 * 24 * 60 * 60 * 1000))),
                note = "Netflix, Spotify, Gym"
            ),
        )
    )

    fun observeExpenses(): Flow<List<ExpenseEntry>> = _expenseEntries.asStateFlow()

    suspend fun addExpense(entry: ExpenseEntry) {
        _expenseEntries.update { current -> current + entry }
    }
}
