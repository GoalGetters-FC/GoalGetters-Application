package com.ggetters.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_statistics")
data class PlayerStatistics(
    @PrimaryKey
    val playerId: String,
    val scheduled: Int = 0,
    val attended: Int = 0,
    val missed: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val matches: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val cleanSheets: Int = 0,
    val weight: Double = 0.0,
    val minutesPlayed: Int = 0
) {
    val attendanceRate: Double
        get() = if (scheduled > 0) (attended.toDouble() / scheduled.toDouble()) * 100 else 0.0
}
