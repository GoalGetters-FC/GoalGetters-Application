package com.ggetters.app.ui.central.models

data class Player(
    val id: String,
    val firstName: String,
    val lastName: String,
    val position: String,
    val jerseyNumber: String,
    val avatar: String? = null,
    val isActive: Boolean = true,
    val stats: PlayerStats = PlayerStats(),
    val email: String? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val joinedDate: String? = null
) {
    fun getFullName(): String = "$firstName $lastName"
    
    // Backward compatibility
    val name: String get() = getFullName()
}

data class PlayerStats(
    val matches: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val cleanSheets: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0
)

data class TeamStats(
    val totalMatches: Int,
    val wins: Int,
    val draws: Int,
    val losses: Int,
    val goalsScored: Int,
    val goalsConceded: Int,
    val points: Int,
    val cleanSheets: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0
) 