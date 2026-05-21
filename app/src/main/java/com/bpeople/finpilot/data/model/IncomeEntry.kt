package com.bpeople.finpilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

/**
 * Matches Firestore income document exactly.
 * Firestore fields: source, amountOriginal, currencyOriginal, amountLKR, exchangeRate, date, label, type, projectRef
 */
@Entity(tableName = "income_entries")
@TypeConverters(Converters::class)
@IgnoreExtraProperties
data class IncomeEntry(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "",

    @ColumnInfo(name = "userId")
    val userId: String = "",

    @ColumnInfo(name = "source")
    val source: String = "",

    @ColumnInfo(name = "amountOriginal")
    val amountOriginal: Double = 0.0,

    @ColumnInfo(name = "currencyOriginal")
    val currencyOriginal: String = "LKR",

    @ColumnInfo(name = "amountLKR")
    val amountLKR: Double = 0.0,

    @ColumnInfo(name = "exchangeRate")
    val exchangeRate: Double = 1.0,

    @ColumnInfo(name = "date")
    val date: Timestamp? = null,

    @ColumnInfo(name = "label")
    val label: String? = null,

    @ColumnInfo(name = "type")
    val type: String = "ONE_OFF",

    @ColumnInfo(name = "projectRef")
    val projectRef: String? = null
) : Serializable

