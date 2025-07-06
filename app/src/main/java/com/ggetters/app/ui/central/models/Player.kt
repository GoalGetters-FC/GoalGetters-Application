package com.ggetters.app.ui.central.models

data class Player(
    val id: String,
    val name: String,
    val position: String,
    val jerseyNumber: String,
    val avatar: String?,
    val isActive: Boolean,
    val stats: PlayerStats,
    val email: String? = null,
    val phone: String? = null,
    val dateOfBirth: String? = null,
    val joinedDate: String? = null
)

data class PlayerStats(
    val goals: Int,
    val assists: Int,
    val matches: Int,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val cleanSheets: Int = 0,
    val minutesPlayed: Int = 0
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