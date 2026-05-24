package com.bpeople.finpilot.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_crypto_holdings")
data class RoomCryptoEntry(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "symbol")
    val symbol: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "quantity")
    val quantity: Double,

    @ColumnInfo(name = "buyPriceLKR")
    val buyPriceLKR: Double,

    @ColumnInfo(name = "currentPriceLKR")
    val currentPriceLKR: Double,

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "purchasedAtMillis")
    val purchasedAtMillis: Long?,
)
