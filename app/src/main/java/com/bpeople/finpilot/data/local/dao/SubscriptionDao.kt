package com.bpeople.finpilot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bpeople.finpilot.data.local.entities.RoomSubscription
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(sub: RoomSubscription)

    @Query("DELETE FROM room_subscriptions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM room_subscriptions WHERE userId = :userId")
    fun observeSubscriptions(userId: String): Flow<List<RoomSubscription>>
}
