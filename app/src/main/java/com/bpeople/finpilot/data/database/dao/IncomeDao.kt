package com.bpeople.finpilot.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bpeople.finpilot.data.model.IncomeEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for IncomeEntry entity.
 * Provides reactive Flow-based queries for offline-first income management.
 */
@Dao
interface IncomeDao {
    /**
     * Insert a new income entry or replace if it already exists.
     * Used for syncing with Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntry): Long

    /**
     * Insert multiple income entries in a batch operation.
     * Useful for syncing large datasets from Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncomeList(incomes: List<IncomeEntry>)

    /**
     * Update an existing income entry.
     */
    @Update
    suspend fun updateIncome(income: IncomeEntry)

    /**
     * Delete an income entry.
     */
    @Delete
    suspend fun deleteIncome(income: IncomeEntry)

    /**
     * Delete all income entries for a specific user.
     * Used when switching users or clearing cache.
     */
    @Query("DELETE FROM income_entries WHERE userId = :userId")
    suspend fun deleteAllIncomeForUser(userId: String)

    /**
     * Get all income entries for a user in a specific month (reactive).
     * Returns Flow<List<IncomeEntry>> for automatic UI updates.
     *
     * @param userId User identifier
     * @param year Year to filter by (e.g., 2024)
     * @param month Month to filter by (1-12)
     */
    @Query(
        """
        SELECT * FROM income_entries 
        WHERE userId = :userId 
        AND CAST(strftime('%Y', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :year
        AND CAST(strftime('%m', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :month
        ORDER BY date DESC
        """
    )
    fun getIncomeByMonth(userId: String, year: Int, month: Int): Flow<List<IncomeEntry>>

    /**
     * Get all income entries for a user (reactive).
     * Used for dashboards and summaries.
     */
    @Query("SELECT * FROM income_entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllIncomeForUser(userId: String): Flow<List<IncomeEntry>>

    /**
     * Get a specific income entry by ID.
     */
    @Query("SELECT * FROM income_entries WHERE id = :incomeId")
    suspend fun getIncomeById(incomeId: String): IncomeEntry?

    /**
     * Get all recurring income entries for a user.
     */
    @Query(
        "SELECT * FROM income_entries WHERE userId = :userId AND type = 'RECURRING' ORDER BY date DESC"
    )
    fun getRecurringIncome(userId: String): Flow<List<IncomeEntry>>

    /**
     * Get income entries by source type.
     */
    @Query("SELECT * FROM income_entries WHERE userId = :userId AND source = :source ORDER BY date DESC")
    fun getIncomeBySource(userId: String, source: String): Flow<List<IncomeEntry>>

    /**
     * Get total income amount for a specific month (one-time query).
     */
    @Query(
        """
        SELECT COALESCE(SUM(amountLKR), 0.0) FROM income_entries 
        WHERE userId = :userId 
        AND CAST(strftime('%Y', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :year
        AND CAST(strftime('%m', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :month
        """
    )
    suspend fun getTotalIncomeForMonth(userId: String, year: Int, month: Int): Double

    /**
     * Get total income amount for a specific year.
     */
    @Query(
        """
        SELECT COALESCE(SUM(amountLKR), 0.0) FROM income_entries 
        WHERE userId = :userId 
        AND CAST(strftime('%Y', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :year
        """
    )
    suspend fun getTotalIncomeForYear(userId: String, year: Int): Double
}
