package com.bpeople.finpilot.data.local

import com.bpeople.finpilot.data.model.Goal
import kotlinx.coroutines.flow.Flow

// Placeholder interface (removed Room annotations).
interface GoalDao {
    suspend fun upsert(goal: Goal)
    fun observeAll(userId: String): Flow<List<Goal>>
    fun observeById(userId: String, goalId: String): Flow<Goal?>
}

