package com.bpeople.finpilot.data.local

import com.bpeople.finpilot.data.model.ExpenseEntry
import kotlinx.coroutines.flow.Flow

// Placeholder interface (Room annotations removed to avoid build-time KSP errors).
interface ExpenseDao {
    suspend fun upsert(entry: ExpenseEntry)
    suspend fun upsertAll(entries: List<ExpenseEntry>)
    fun getByDateRange(userId: String, start: Long, end: Long): Flow<List<ExpenseEntry>>
    suspend fun deleteBetween(userId: String, start: Long, end: Long)
    fun getByCategoryAndDateRange(userId: String, category: String, start: Long, end: Long): Flow<List<ExpenseEntry>>
}

