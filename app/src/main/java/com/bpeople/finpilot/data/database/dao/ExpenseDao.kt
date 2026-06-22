package com.bpeople.finpilot.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bpeople.finpilot.data.model.ExpenseEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ExpenseEntry entity.
 * Provides reactive Flow-based queries for offline-first expense management.
 */
@Dao
interface ExpenseDao {
    /**
     * Insert a new expense entry or replace if it already exists.
     * Used for syncing with Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntry): Long

    /**
     * Insert multiple expense entries in a batch operation.
     * Useful for syncing large datasets from Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseList(expenses: List<ExpenseEntry>)

    /**
     * Update an existing expense entry.
     */
    @Update
    suspend fun updateExpense(expense: ExpenseEntry)

    /**
     * Delete an expense entry.
     */
    @Delete
    suspend fun deleteExpense(expense: ExpenseEntry)

    /**
     * Delete all expense entries for a specific user.
     * Used when switching users or clearing cache.
     */
    @Query("DELETE FROM expense_entries WHERE userId = :userId")
    suspend fun deleteAllExpensesForUser(userId: String)

    /**
     * Get all expense entries for a user in a specific month (reactive).
     * Returns Flow<List<ExpenseEntry>> for automatic UI updates.
     *
     * @param userId User identifier
     * @param year Year to filter by (e.g., 2024)
     * @param month Month to filter by (1-12)
     */
    @Query(
        """
        SELECT * FROM expense_entries 
        WHERE userId = :userId 
        AND CAST(strftime('%Y', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :year
        AND CAST(strftime('%m', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :month
        ORDER BY date DESC
        """
    )
    fun getExpensesByMonth(userId: String, year: Int, month: Int): Flow<List<ExpenseEntry>>

    /**
     * Get all expense entries for a user by category (reactive).
     * Used for category-based analytics and filtering.
     */
    @Query(
        """
        SELECT * FROM expense_entries 
        WHERE userId = :userId AND category = :category 
        ORDER BY date DESC
        """
    )
    fun getExpensesByCategory(userId: String, category: String): Flow<List<ExpenseEntry>>

    /**
     * Get all expense entries for a user by category in a specific month (reactive).
     */
    @Query(
        """
        SELECT * FROM expense_entries 
        WHERE userId = :userId AND category = :category
        AND CAST(strftime('%Y', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :year
        AND CAST(strftime('%m', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :month
        ORDER BY date DESC
        """
    )
    fun getExpensesByCategoryAndMonth(
        userId: String,
        category: String,
        year: Int,
        month: Int
    ): Flow<List<ExpenseEntry>>

    /**
     * Get all expense entries for a user (reactive).
     * Used for dashboards and full expense views.
     */
    @Query("SELECT * FROM expense_entries WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpensesForUser(userId: String): Flow<List<ExpenseEntry>>

    /**
     * Get a specific expense entry by ID.
     */
    @Query("SELECT * FROM expense_entries WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: String): ExpenseEntry?

    /**
     * Get all recurring expense entries for a user (reactive).
     */
    @Query(
        "SELECT * FROM expense_entries WHERE userId = :userId AND isRecurring = 1 ORDER BY date DESC"
    )
    fun getRecurringExpenses(userId: String): Flow<List<ExpenseEntry>>

    /**
     * Get expense entries by payment method.
     */
    @Query(
        "SELECT * FROM expense_entries WHERE userId = :userId AND paymentMethod = :paymentMethod ORDER BY date DESC"
    )
    fun getExpensesByPaymentMethod(userId: String, paymentMethod: String): Flow<List<ExpenseEntry>>

    /**
     * Get total expense amount for a specific month (one-time query).
     */
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0) FROM expense_entries 
        WHERE userId = :userId 
        AND CAST(strftime('%Y', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :year
        AND CAST(strftime('%m', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :month
        """
    )
    suspend fun getTotalExpensesForMonth(userId: String, year: Int, month: Int): Double

    /**
     * Get total expense amount for a specific year.
     */
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0) FROM expense_entries 
        WHERE userId = :userId 
        AND CAST(strftime('%Y', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :year
        """
    )
    suspend fun getTotalExpensesForYear(userId: String, year: Int): Double

    /**
     * Get total expense amount for a specific category in a month.
     */
    @Query(
        """
        SELECT COALESCE(SUM(amount), 0.0) FROM expense_entries 
        WHERE userId = :userId AND category = :category
        AND CAST(strftime('%Y', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :year
        AND CAST(strftime('%m', datetime(date / 1000, 'unixepoch')) AS INTEGER) = :month
        """
    )
    suspend fun getTotalExpensesForCategoryAndMonth(
        userId: String,
        category: String,
        year: Int,
        month: Int
    ): Double

    /**
     * Get all distinct categories for a user.
     */
    @Query("SELECT DISTINCT category FROM expense_entries WHERE userId = :userId")
    fun getAllCategories(userId: String): Flow<List<String>>
}
