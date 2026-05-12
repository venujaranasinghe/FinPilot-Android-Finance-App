package com.bpeople.finpilot.data.local

import com.bpeople.finpilot.data.model.FreelanceProject
import kotlinx.coroutines.flow.Flow

// Placeholder interface (Room annotations removed to keep builds working while iterating).
interface ProjectDao {
    suspend fun upsert(project: FreelanceProject)
    suspend fun upsertAll(projects: List<FreelanceProject>)
    fun observeAll(userId: String): Flow<List<FreelanceProject>>
    fun observeById(userId: String, projectId: String): Flow<FreelanceProject?>
}

