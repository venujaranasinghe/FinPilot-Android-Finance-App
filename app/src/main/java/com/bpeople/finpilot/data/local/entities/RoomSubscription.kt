package com.bpeople.finpilot.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_subscriptions")
data class RoomSubscription(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "amountLKR")
    val amountLKR: Double,

    @ColumnInfo(name = "billingCycle")
    val billingCycle: String,

    @ColumnInfo(name = "nextBillingMillis")
    val nextBillingMillis: Long,

    @ColumnInfo(name = "isActive")
    val isActive: Boolean,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "note")
    val note: String,
)
