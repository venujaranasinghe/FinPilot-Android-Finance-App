package com.bpeople.finpilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

/**
 * Matches Firestore goal document exactly.
 * Fields: title, targetAmount, currentAmount, deadline, monthlyRequired, isActive
 */
@Entity(tableName = "goals")
@TypeConverters(Converters::class)
@IgnoreExtraProperties
data class Goal(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "",

    @ColumnInfo(name = "userId")
    val userId: String = "",

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "targetAmount")
    val targetAmount: Double = 0.0,

    @ColumnInfo(name = "currentAmount")
    val currentAmount: Double = 0.0,

    @ColumnInfo(name = "deadline")
    val deadline: Timestamp? = null,

    @ColumnInfo(name = "monthlyRequired")
    val monthlyRequired: Double = 0.0,

    @ColumnInfo(name = "isActive")
    val isActive: Boolean = true
) : Serializable

