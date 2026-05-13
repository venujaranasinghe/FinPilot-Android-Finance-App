package com.bpeople.finpilot.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_expense_entries")
data class RoomExpense(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "subCategory")
    val subCategory: String?,

    @ColumnInfo(name = "paymentMethod")
    val paymentMethod: String,

    @ColumnInfo(name = "dateMillis")
    val dateMillis: Long?,

    @ColumnInfo(name = "note")
    val note: String?,

    @ColumnInfo(name = "isRecurring")
    val isRecurring: Boolean,

    @ColumnInfo(name = "tags")
    val tags: List<String>,

    @ColumnInfo(name = "originalCurrency")
    val originalCurrency: String?,

    @ColumnInfo(name = "originalAmount")
    val originalAmount: Double?,
)

