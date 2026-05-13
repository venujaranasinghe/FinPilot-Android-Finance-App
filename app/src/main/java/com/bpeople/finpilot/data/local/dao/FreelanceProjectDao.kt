package com.bpeople.finpilot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bpeople.finpilot.data.local.entities.RoomProject
import kotlinx.coroutines.flow.Flow

@Dao
interface FreelanceProjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: RoomProject): Long

    @Query("SELECT * FROM room_freelance_projects WHERE userId = :userId AND status = :status ORDER BY id DESC")
    fun getProjectsByStatus(userId: String, status: String): Flow<List<RoomProject>>
}


