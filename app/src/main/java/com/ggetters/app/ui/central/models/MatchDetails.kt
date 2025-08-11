package com.ggetters.app.ui.central.models

import java.util.*

/**
 * Data model for comprehensive match details including RSVP statistics,
 * player availability, and match information.
 */
data class MatchDetails(
    val matchId: String = UUID.randomUUID().toString(),
    val title: String,
    val homeTeam: String,
    val awayTeam: String,
    val venue: String,
    val date: Date,
    val time: String,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val rsvpStats: RSVPStats = RSVPStats(),
    val playerAvailability: List<PlayerAvailability> = emptyList(),
    val formation: String = "4-3-3",
    val createdBy: String,
    val createdAt: Date = Date()
) {
    fun getFormattedScore(): String {
        return "$homeScore - $awayScore"
    }
    
    fun getFormattedDateTime(): String {
        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return "${dateFormat.format(date)} at $time"
    }
    
    fun isMatchStarted(): Boolean {
        return status == MatchStatus.IN_PROGRESS || status == MatchStatus.PAUSED || 
               status == MatchStatus.HALF_TIME || status == MatchStatus.FULL_TIME
    }
    
    fun canStartMatch(): Boolean {
        return status == MatchStatus.SCHEDULED && rsvpStats.available >= 11
    }
}

/**
 * RSVP statistics for a match
 */
data class RSVPStats(
    val available: Int = 0,
    val maybe: Int = 0,
    val unavailable: Int = 0,
    val notResponded: Int = 0
) {
    val total: Int get() = available + maybe + unavailable + notResponded
    
    fun getFormattedSummary(): String {
        return "$available available, $maybe maybe, $unavailable unavailable"
    }
    
    fun getAvailabilityPercentage(): Float {
        return if (total > 0) (available.toFloat() / total.toFloat()) * 100 else 0f
    }
}

/**
 * Player availability for a specific match
 */
data class PlayerAvailability(
    val playerId: String,
    val playerName: String,
    val position: String,
    val jerseyNumber: Int,
    val status: RSVPStatus,
    val responseTime: Date? = null,
    val notes: String? = null,
    val profileImageUrl: String? = null
) {
    fun getStatusDisplayText(): String {
        return when (status) {
            RSVPStatus.AVAILABLE -> "Available"
            RSVPStatus.MAYBE -> "Maybe"
            RSVPStatus.UNAVAILABLE -> "Unavailable"
            RSVPStatus.NOT_RESPONDED -> "No Response"
        }
    }
    
    fun getStatusIcon(): String {
        return when (status) {
            RSVPStatus.AVAILABLE -> "✅"
            RSVPStatus.MAYBE -> "❓"
            RSVPStatus.UNAVAILABLE -> "❌"
            RSVPStatus.NOT_RESPONDED -> "⭕"
        }
    }
    
    fun getStatusColor(): String {
        return when (status) {
            RSVPStatus.AVAILABLE -> "#4CAF50"     // Green
            RSVPStatus.MAYBE -> "#FF9800"         // Orange
            RSVPStatus.UNAVAILABLE -> "#F44336"   // Red
            RSVPStatus.NOT_RESPONDED -> "#9E9E9E" // Gray
        }
    }
}

// Post-match result data classes
data class MatchResult(
    val matchId: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val matchDate: Long,
    val duration: Int, // in minutes
    val goals: List<MatchEvent>,
    val cards: List<MatchEvent>,
    val substitutions: List<MatchEvent>,
    val possession: Map<String, Int>, // home/away percentage
    val shots: Map<String, Int>,
    val shotsOnTarget: Map<String, Int>,
    val corners: Map<String, Int>,
    val fouls: Map<String, Int>,
    val notes: String? = null
) {
    fun getWinner(): String? = when {
        homeScore > awayScore -> homeTeam
        awayScore > homeScore -> awayTeam
        else -> null // Draw
    }
    
    fun getResultText(): String = when {
        homeScore > awayScore -> "Victory"
        awayScore > homeScore -> "Defeat"
        else -> "Draw"
    }
    
    fun getFormattedScore(): String = "$homeScore - $awayScore"
}

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
    val rating: Double = 0.0 // out of 10
) {
    fun hasOutstandingPerformance(): Boolean = goals >= 2 || rating >= 8.5
    
    fun getPerformanceLevel(): String = when {
        rating >= 8.0 -> "Excellent"
        rating >= 7.0 -> "Good"
        rating >= 6.0 -> "Average"
        else -> "Poor"
    }
}
