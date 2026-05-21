package com.bpeople.finpilot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bpeople.finpilot.data.local.entities.RoomGoal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: RoomGoal): Long

    /**
     * Returns 0..1 active goals (kept as a List to ensure all DAO query methods expose Flow<List<...>>.
     */
    @Query("SELECT * FROM room_goals WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun getActiveGoal(userId: String): Flow<List<RoomGoal>>

    @Query("UPDATE room_goals SET currentAmount = :currentAmount WHERE id = :goalId")
    suspend fun updateGoalAmount(goalId: String, currentAmount: Double): Int
}




