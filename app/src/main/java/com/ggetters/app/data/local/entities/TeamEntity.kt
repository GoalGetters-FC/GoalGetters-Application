package com.ggetters.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val code: String,            // join-code
    val createdAt: Date,
    val updatedAt: Date,
    val stashedAt: Date? = null  // soft-delete timestamp
)
