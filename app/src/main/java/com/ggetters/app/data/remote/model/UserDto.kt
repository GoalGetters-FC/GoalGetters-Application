package com.ggetters.app.data.remote.model

import com.google.firebase.Timestamp

// UserDto.kt
data class UserDto(
    val id: String = "",
    val teamId: String? = null,
    val authId: String = "",
    val code: String = "",
    val name: String = "",
    val surname: String = "",
    val alias: String? = null,
    val role: Int = 0,
    val gender: String? = null,
    val dateOfBirth: Timestamp = Timestamp.now(),
    val annexedAt: Timestamp? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val stampedAt: Timestamp? = null
)
