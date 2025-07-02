package com.ggetters.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val teamId: String?,         // FK to TeamEntity.id
    val authId: String,
    val code: String,            // join-code or shorthand identifier
    val name: String,
    val surname: String,
    val alias: String? = null,   // optional nickname
    val role: Int,
    val gender: String? = null,
    val dateOfBirth: Date,
    val annexedAt: Date? = null, // when added to the team
    val createdAt: Date,
    val updatedAt: Date,
    val stampedAt: Date? = null  // soft-delete / stash timestamp
)

