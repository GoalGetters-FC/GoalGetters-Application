// app/src/main/java/com/ggetters/app/data/model/PlayerMatchStats.kt
package com.ggetters.app.data.model

/**
 * Domain model representing a player’s performance in a single match.
 *
 * This aggregates basic player info (id, name, jersey, position)
 * with their in-game performance stats.
 *
 * @property playerId Unique identifier of the player.
 * @property playerName Full display name of the player.
 * @property jerseyNumber Shirt/jersey number of the player.
 * @property position Playing position (e.g., "GK", "CB", "ST").
 * @property minutesPlayed Total minutes the player was on the pitch.
 * @property goals Number of goals scored by the player.
 * @property assists Number of assists provided by the player.
 * @property shots Total shots taken.
 * @property shotsOnTarget Shots on target.
 * @property passes Total passes attempted.
 * @property passAccuracy Pass accuracy as a percentage (0–100).
 * @property tackles Successful tackles made.
 * @property interceptions Interceptions completed.
 * @property fouls Fouls committed.
 * @property yellowCards Number of yellow cards received.
 * @property redCards Number of red cards received.
 * @property rating Performance rating (out of 10).
 */
data class PlayerMatchStats(
    val playerId: String,
    val playerName: String,
    val jerseyNumber: Int,
    val position: String,
    val minutesPlayed: Int,
    val goals: Int = 0,
    val assists: Int = 0,
    val shots: Int = 0,
    val shotsOnTarget: Int = 0,
    val passes: Int = 0,
    val passAccuracy: Int = 0, // percentage
    val tackles: Int = 0,
    val interceptions: Int = 0,
    val fouls: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0,
    val rating: Double = 0.0 // performance rating out of 10
) {
    /**
     * Returns true if this performance is considered outstanding.
     */
    fun hasOutstandingPerformance(): Boolean =
        goals >= 2 || rating >= 8.5

    /**
     * Provides a simple label for the performance.
     */
    fun getPerformanceLevel(): String = when {
        rating >= 8.0 -> "Excellent"
        rating >= 7.0 -> "Good"
        rating >= 6.0 -> "Average"
        else -> "Poor"
    }
}
