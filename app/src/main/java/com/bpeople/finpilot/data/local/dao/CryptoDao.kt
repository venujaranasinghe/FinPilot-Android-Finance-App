package com.bpeople.finpilot.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bpeople.finpilot.data.local.entities.RoomCryptoEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CryptoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: RoomCryptoEntry)

    @Query("DELETE FROM room_crypto_holdings WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM room_crypto_holdings WHERE userId = :userId")
    fun observeHoldings(userId: String): Flow<List<RoomCryptoEntry>>
}
