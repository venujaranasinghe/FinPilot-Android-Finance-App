package com.bpeople.finpilot.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bpeople.finpilot.data.model.FreelanceProject
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for FreelanceProject entity.
 * Provides reactive Flow-based queries for offline-first freelance project management.
 */
@Dao
interface FreelanceProjectDao {
    /**
     * Insert a new freelance project or replace if it already exists.
     * Used for syncing with Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: FreelanceProject): Long

    /**
     * Insert multiple freelance projects in a batch operation.
     * Useful for syncing large datasets from Firestore.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectList(projects: List<FreelanceProject>)

    /**
     * Update an existing freelance project.
     */
    @Update
    suspend fun updateProject(project: FreelanceProject)

    /**
     * Update project payment status.
     * Useful for incremental payment tracking.
     */
    @Query("UPDATE freelance_projects SET paidAmount = :paidAmount WHERE id = :projectId")
    suspend fun updateProjectPayment(projectId: String, paidAmount: Double)

    /**
     * Update project status.
     */
    @Query("UPDATE freelance_projects SET status = :status WHERE id = :projectId")
    suspend fun updateProjectStatus(projectId: String, status: String)

    /**
     * Delete a freelance project.
     */
    @Delete
    suspend fun deleteProject(project: FreelanceProject)

    /**
     * Delete all freelance projects for a specific user.
     * Used when switching users or clearing cache.
     */
    @Query("DELETE FROM freelance_projects WHERE userId = :userId")
    suspend fun deleteAllProjectsForUser(userId: String)

    /**
     * Get all freelance projects for a user with specific status (reactive).
     * Returns Flow<List<FreelanceProject>> for automatic UI updates.
     *
     * @param userId User identifier
     * @param status Project status (e.g., "OPEN", "IN_PROGRESS", "COMPLETED", "PAID")
     */
    @Query(
        "SELECT * FROM freelance_projects WHERE userId = :userId AND status = :status ORDER BY id DESC"
    )
    fun getProjectsByStatus(userId: String, status: String): Flow<List<FreelanceProject>>

    /**
     * Get all open/in-progress freelance projects for a user (reactive).
     * Useful for active project dashboard.
     */
    @Query(
        """
        SELECT * FROM freelance_projects 
        WHERE userId = :userId AND status IN ('OPEN', 'IN_PROGRESS') 
        ORDER BY id DESC
        """
    )
    fun getActiveProjects(userId: String): Flow<List<FreelanceProject>>

    /**
     * Get all completed freelance projects for a user (reactive).
     */
    @Query(
        "SELECT * FROM freelance_projects WHERE userId = :userId AND status = 'COMPLETED' ORDER BY id DESC"
    )
    fun getCompletedProjects(userId: String): Flow<List<FreelanceProject>>

    /**
     * Get all freelance projects for a user (reactive).
     * Used for full project view.
     */
    @Query("SELECT * FROM freelance_projects WHERE userId = :userId ORDER BY id DESC")
    fun getAllProjectsForUser(userId: String): Flow<List<FreelanceProject>>

    /**
     * Get a specific freelance project by ID (reactive).
     */
    @Query("SELECT * FROM freelance_projects WHERE id = :projectId")
    fun getProjectById(projectId: String): Flow<FreelanceProject?>

    /**
     * Get a specific freelance project by ID (one-time query).
     */
    @Query("SELECT * FROM freelance_projects WHERE id = :projectId")
    suspend fun getProjectByIdOnce(projectId: String): FreelanceProject?

    /**
     * Get projects by client name (reactive).
     * Useful for filtering projects by client.
     */
    @Query(
        "SELECT * FROM freelance_projects WHERE userId = :userId AND clientName = :clientName ORDER BY id DESC"
    )
    fun getProjectsByClient(userId: String, clientName: String): Flow<List<FreelanceProject>>

    /**
     * Get all distinct client names for a user.
     */
    @Query("SELECT DISTINCT clientName FROM freelance_projects WHERE userId = :userId ORDER BY clientName")
    fun getAllClients(userId: String): Flow<List<String>>

    /**
     * Get total amount agreed across all projects for a user.
     */
    @Query(
        """
        SELECT COALESCE(SUM(agreedAmount), 0.0) 
        FROM freelance_projects WHERE userId = :userId
        """
    )
    suspend fun getTotalAgreedAmount(userId: String): Double

    /**
     * Get total amount paid across all projects for a user.
     */
    @Query(
        """
        SELECT COALESCE(SUM(paidAmount), 0.0) 
        FROM freelance_projects WHERE userId = :userId
        """
    )
    suspend fun getTotalPaidAmount(userId: String): Double

    /**
     * Get total amount outstanding (agreed - paid) across all projects.
     */
    @Query(
        """
        SELECT COALESCE(SUM(agreedAmount - paidAmount), 0.0) 
        FROM freelance_projects WHERE userId = :userId
        """
    )
    suspend fun getTotalOutstandingAmount(userId: String): Double

    /**
     * Get outstanding amount for a specific project.
     */
    @Query(
        "SELECT (agreedAmount - paidAmount) FROM freelance_projects WHERE id = :projectId"
    )
    suspend fun getOutstandingAmountForProject(projectId: String): Double

    /**
     * Get projects with unpaid amounts (reactive).
     * Useful for payment reminders.
     */
    @Query(
        """
        SELECT * FROM freelance_projects 
        WHERE userId = :userId AND (agreedAmount > paidAmount) 
        ORDER BY id DESC
        """
    )
    fun getUnpaidProjects(userId: String): Flow<List<FreelanceProject>>

    /**
     * Get the number of active projects for a user.
     */
    @Query(
        "SELECT COUNT(*) FROM freelance_projects WHERE userId = :userId AND status IN ('OPEN', 'IN_PROGRESS')"
    )
    suspend fun getActiveProjectCount(userId: String): Int
}
