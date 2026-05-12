package com.bpeople.finpilot.data.local

import com.bpeople.finpilot.data.model.IncomeEntry
import kotlinx.coroutines.flow.Flow

// Room DAO support was added earlier but caused build-time KSP issues in this branch.
// Keep a plain interface as a placeholder so other code can compile. The actual Room
// implementation can be reintroduced in a follow-up change once KSP issues are resolved.
interface IncomeDao {
    suspend fun upsert(entry: IncomeEntry)
    suspend fun upsertAll(entries: List<IncomeEntry>)
    fun getByDateRange(userId: String, start: Long, end: Long): Flow<List<IncomeEntry>>
    suspend fun deleteBetween(userId: String, start: Long, end: Long)
}

