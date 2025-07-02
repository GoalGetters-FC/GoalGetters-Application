package com.ggetters.app.data.remote.model

import com.google.firebase.Timestamp

// TeamDto.kt
data class TeamDto(
    val id: String = "",
    val code: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now(),
    val stashedAt: Timestamp? = null
)
