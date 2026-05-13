package com.bpeople.finpilot.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Room representation of an income entry. Dates stored as epoch millis for compatibility with Room. */
@Entity(tableName = "room_income_entries")
data class RoomIncome(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "amountOriginal")
    val amountOriginal: Double,

    @ColumnInfo(name = "currencyOriginal")
    val currencyOriginal: String,

    @ColumnInfo(name = "amountLKR")
    val amountLKR: Double,

    @ColumnInfo(name = "exchangeRate")
    val exchangeRate: Double,

    // store date as millis
    @ColumnInfo(name = "dateMillis")
    val dateMillis: Long?,

    @ColumnInfo(name = "label")
    val label: String?,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "projectRef")
    val projectRef: String?,
)

