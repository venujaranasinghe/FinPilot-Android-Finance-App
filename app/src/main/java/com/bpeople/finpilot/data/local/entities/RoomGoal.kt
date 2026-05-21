package com.bpeople.finpilot.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_goals")
data class RoomGoal(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "targetAmount")
    val targetAmount: Double,

    @ColumnInfo(name = "currentAmount")
    val currentAmount: Double,

    @ColumnInfo(name = "deadlineMillis")
    val deadlineMillis: Long?,

    @ColumnInfo(name = "monthlyRequired")
    val monthlyRequired: Double,

    @ColumnInfo(name = "isActive")
    val isActive: Boolean,
)

