package com.bpeople.finpilot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bpeople.finpilot.data.local.entities.RoomExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(entry: RoomExpense): Long

    @Query("SELECT * FROM room_expense_entries WHERE userId = :userId AND dateMillis BETWEEN :start AND :end ORDER BY dateMillis DESC")
    fun getExpensesByMonth(userId: String, start: Long, end: Long): Flow<List<RoomExpense>>

    @Query("SELECT * FROM room_expense_entries WHERE userId = :userId AND category = :category AND dateMillis BETWEEN :start AND :end ORDER BY dateMillis DESC")
    fun getExpensesByCategory(userId: String, category: String, start: Long, end: Long): Flow<List<RoomExpense>>

    @Query("DELETE FROM room_expense_entries WHERE userId = :userId AND dateMillis BETWEEN :start AND :end")
    suspend fun deleteExpenseBetween(userId: String, start: Long, end: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<RoomExpense>): List<Long>
}




