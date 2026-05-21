package com.bpeople.finpilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

/**
 * Matches Firestore expense document exactly.
 * Firestore fields: amount, category, subCategory, paymentMethod, date, note, isRecurring, tags
 */
@Entity(tableName = "expense_entries")
@TypeConverters(Converters::class)
@IgnoreExtraProperties
data class ExpenseEntry(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "",

    @ColumnInfo(name = "userId")
    val userId: String = "",

    @ColumnInfo(name = "amount")
    val amount: Double = 0.0,

    @ColumnInfo(name = "category")
    val category: String = "OTHER",

    @ColumnInfo(name = "subCategory")
    val subCategory: String? = null,

    @ColumnInfo(name = "paymentMethod")
    val paymentMethod: String = "CASH",

    @ColumnInfo(name = "date")
    val date: Timestamp? = null,

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "isRecurring")
    val isRecurring: Boolean = false,

    @ColumnInfo(name = "tags")
    val tags: List<String> = emptyList(),

    @ColumnInfo(name = "originalCurrency")
    val originalCurrency: String? = null,

    @ColumnInfo(name = "originalAmount")
    val originalAmount: Double? = null
) : Serializable

