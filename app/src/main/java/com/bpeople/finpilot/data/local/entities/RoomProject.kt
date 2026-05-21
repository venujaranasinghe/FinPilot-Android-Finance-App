package com.bpeople.finpilot.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_freelance_projects")
data class RoomProject(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "userId")
    val userId: String,

    @ColumnInfo(name = "clientName")
    val clientName: String,

    @ColumnInfo(name = "projectTitle")
    val projectTitle: String,

    @ColumnInfo(name = "agreedAmount")
    val agreedAmount: Double,

    @ColumnInfo(name = "paidAmount")
    val paidAmount: Double,

    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "entries")
    val entries: List<String>,
)

