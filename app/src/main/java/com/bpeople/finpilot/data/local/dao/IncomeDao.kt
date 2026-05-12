package com.bpeople.finpilot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bpeople.finpilot.data.local.entities.RoomIncome
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(entry: RoomIncome): Long

    @Query("SELECT * FROM room_income_entries WHERE userId = :userId ORDER BY dateMillis DESC")
    fun observeAll(userId: String): Flow<List<RoomIncome>>

    @Query("SELECT * FROM room_income_entries WHERE userId = :userId AND dateMillis BETWEEN :start AND :end ORDER BY dateMillis DESC")
    fun getIncomeByMonth(userId: String, start: Long, end: Long): Flow<List<RoomIncome>>

    @Query("DELETE FROM room_income_entries WHERE id = :id")
    suspend fun deleteIncome(id: String): Int

    @Query("DELETE FROM room_income_entries WHERE userId = :userId AND dateMillis BETWEEN :start AND :end")
    suspend fun deleteIncomeBetween(userId: String, start: Long, end: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<RoomIncome>): List<Long>
}




