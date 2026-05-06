package com.bpeople.finpilot.data.repository

import com.bpeople.finpilot.data.model.Goal
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor() {
    private val _goals = MutableStateFlow<List<Goal>>(emptyList())

    fun observeGoals(): Flow<List<Goal>> = _goals.asStateFlow()

    fun observeActiveGoal(): Flow<Goal?> = _goals.map { goals ->
        goals.firstOrNull { it.isActive }
    }

    suspend fun upsertGoal(goal: Goal) {
        val id = if (goal.id.isBlank()) UUID.randomUUID().toString() else goal.id
        val updated = goal.copy(id = id)
        _goals.update { current ->
            val index = current.indexOfFirst { it.id == id }
            if (index >= 0) {
                current.toMutableList().apply { set(index, updated) }
            } else {
                current + updated
            }
        }
    }
}

