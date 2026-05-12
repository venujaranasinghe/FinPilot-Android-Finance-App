package com.bpeople.finpilot.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bpeople.finpilot.data.model.Goal
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Goal entity.
 * Provides reactive Flow-based queries for offline-first goal management.
 */
@Dao
interface GoalDao {
    /**
     * Insert a new goal or replace if it already exists.
     * Used for syncing with Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    /**
     * Insert multiple goals in a batch operation.
     * Useful for syncing large datasets from Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoalList(goals: List<Goal>)

    /**
     * Update an existing goal.
     */
    @Update
    suspend fun updateGoal(goal: Goal)

    /**
     * Update just the goal amount fields without replacing the entire entity.
     * More efficient for incremental progress updates.
     */
    @Query(
        """
        UPDATE goals 
        SET currentAmount = :currentAmount, monthlyRequired = :monthlyRequired 
        WHERE id = :goalId
        """
    )
    suspend fun updateGoalAmount(goalId: String, currentAmount: Double, monthlyRequired: Double)

    /**
     * Update goal progress (currentAmount only).
     */
    @Query("UPDATE goals SET currentAmount = :currentAmount WHERE id = :goalId")
    suspend fun updateGoalProgress(goalId: String, currentAmount: Double)

    /**
     * Delete a goal.
     */
    @Delete
    suspend fun deleteGoal(goal: Goal)

    /**
     * Delete all goals for a specific user.
     * Used when switching users or clearing cache.
     */
    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllGoalsForUser(userId: String)

    /**
     * Get all active goals for a user (reactive).
     * Returns Flow<List<Goal>> for automatic UI updates.
     *
     * @param userId User identifier
     */
    @Query(
        "SELECT * FROM goals WHERE userId = :userId AND isActive = 1 ORDER BY deadline ASC"
    )
    fun getActiveGoals(userId: String): Flow<List<Goal>>

    /**
     * Get a specific active goal by ID (reactive).
     * Useful for goal detail screens.
     */
    @Query("SELECT * FROM goals WHERE id = :goalId AND isActive = 1")
    fun getActiveGoal(goalId: String): Flow<Goal?>

    /**
     * Get all goals (active and inactive) for a user (reactive).
     */
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY deadline ASC")
    fun getAllGoalsForUser(userId: String): Flow<List<Goal>>

    /**
     * Get all inactive/completed goals for a user (reactive).
     */
    @Query("SELECT * FROM goals WHERE userId = :userId AND isActive = 0 ORDER BY deadline DESC")
    fun getInactiveGoals(userId: String): Flow<List<Goal>>

    /**
     * Get a specific goal by ID (one-time query).
     */
    @Query("SELECT * FROM goals WHERE id = :goalId")
    suspend fun getGoalById(goalId: String): Goal?

    /**
     * Mark a goal as inactive (completed or abandoned).
     */
    @Query("UPDATE goals SET isActive = 0 WHERE id = :goalId")
    suspend fun deactivateGoal(goalId: String)

    /**
     * Reactivate a previously inactive goal.
     */
    @Query("UPDATE goals SET isActive = 1 WHERE id = :goalId")
    suspend fun reactivateGoal(goalId: String)

    /**
     * Get the number of active goals for a user.
     */
    @Query("SELECT COUNT(*) FROM goals WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveGoalCount(userId: String): Int

    /**
     * Get goals that are due soon (within specified days).
     */
    @Query(
        """
        SELECT * FROM goals 
        WHERE userId = :userId AND isActive = 1 
        AND datetime(deadline / 1000, 'unixepoch') <= datetime('now', '+' || :daysAhead || ' days')
        ORDER BY deadline ASC
        """
    )
    fun getGoalsDueSoon(userId: String, daysAhead: Int): Flow<List<Goal>>

    /**
     * Get goals that are overdue.
     */
    @Query(
        """
        SELECT * FROM goals 
        WHERE userId = :userId AND isActive = 1 
        AND datetime(deadline / 1000, 'unixepoch') < datetime('now')
        ORDER BY deadline ASC
        """
    )
    fun getOverdueGoals(userId: String): Flow<List<Goal>>

    /**
     * Get overall progress across all active goals.
     * Returns ratio of total current amount to total target amount.
     */
    @Query(
        """
        SELECT 
            COALESCE(SUM(currentAmount), 0.0) as totalCurrent,
            COALESCE(SUM(targetAmount), 0.0) as totalTarget
        FROM goals 
        WHERE userId = :userId AND isActive = 1
        """
    )
    suspend fun getOverallGoalProgress(userId: String): Map<String, Double>
}
