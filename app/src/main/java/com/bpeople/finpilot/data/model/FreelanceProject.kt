package com.bpeople.finpilot.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.firestore.IgnoreExtraProperties
import java.io.Serializable

/**
 * Matches Firestore freelanceProjects document.
 * Fields: clientName, projectTitle, agreedAmount, paidAmount, status, entries[]
 * entries[] is modelled as List<String> (e.g., income entry ids) and stored with a TypeConverter.
 */
@Entity(tableName = "freelance_projects")
@TypeConverters(Converters::class)
@IgnoreExtraProperties
data class FreelanceProject(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = "",

    @ColumnInfo(name = "userId")
    val userId: String = "",

    @ColumnInfo(name = "clientName")
    val clientName: String = "",

    @ColumnInfo(name = "projectTitle")
    val projectTitle: String = "",

    @ColumnInfo(name = "agreedAmount")
    val agreedAmount: Double = 0.0,

    @ColumnInfo(name = "paidAmount")
    val paidAmount: Double = 0.0,

    @ColumnInfo(name = "status")
    val status: String = "OPEN",

    @ColumnInfo(name = "entries")
    val entries: List<String> = emptyList()
) : Serializable

