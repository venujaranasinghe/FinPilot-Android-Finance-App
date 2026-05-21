package com.bpeople.finpilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Matches Firestore users/{uid} document fields exactly.
 * Fields: uid, displayName, email, baseCurrency, createdAt
 */
@Entity(tableName = "user_profiles")
@TypeConverters(Converters::class)
@IgnoreExtraProperties
data class UserProfile(
    @PrimaryKey
    @ColumnInfo(name = "uid")
    val uid: String = "",

    @ColumnInfo(name = "displayName")
    val displayName: String? = null,

    @ColumnInfo(name = "email")
    val email: String? = null,

    @ColumnInfo(name = "baseCurrency")
    val baseCurrency: String = "LKR",

    @ColumnInfo(name = "createdAt")
    val createdAt: Timestamp? = null
)
