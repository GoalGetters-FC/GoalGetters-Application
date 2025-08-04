package com.ggetters.app.ui.central.models

import java.util.*

data class MatchEvent(
    val id: String = UUID.randomUUID().toString(),
    val matchId: String,
    val eventType: MatchEventType,
    val timestamp: Long = System.currentTimeMillis(),
    val minute: Int, // Match minute when event occurred
    val playerId: String? = null,
    val playerName: String? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val details: Map<String, Any> = emptyMap(), // Additional event-specific data
    val createdBy: String, // Coach/admin who recorded the event
    val isConfirmed: Boolean = true
) {
    fun getFormattedTime(): String {
        return "${minute}'"
    }
    
    fun getEventDescription(): String {
        return when (eventType) {
            MatchEventType.GOAL -> "Goal by ${playerName ?: "Unknown"}"
            MatchEventType.YELLOW_CARD -> "Yellow card for ${playerName ?: "Unknown"}"
            MatchEventType.RED_CARD -> "Red card for ${playerName ?: "Unknown"}"
            MatchEventType.SUBSTITUTION -> {
                val playerOut = details["playerOut"] as? String ?: "Unknown"
                val playerIn = details["playerIn"] as? String ?: "Unknown"
                "$playerIn ↔ $playerOut"
            }
            MatchEventType.MATCH_START -> "Match started"
            MatchEventType.MATCH_END -> "Match ended"
            MatchEventType.HALF_TIME -> "Half time"
            MatchEventType.SCORE_UPDATE -> {
                val homeScore = details["homeScore"] as? Int ?: 0
                val awayScore = details["awayScore"] as? Int ?: 0
                "Score updated: $homeScore - $awayScore"
            }
        }
    }
}

enum class MatchEventType {
    GOAL,           // Goal scored
    YELLOW_CARD,    // Yellow card
    RED_CARD,       // Red card
    SUBSTITUTION,   // Player substitution
    MATCH_START,    // Match started
    MATCH_END,      // Match ended
    HALF_TIME,      // Half time
    SCORE_UPDATE    // Manual score update
}

enum class GoalType {
    OPEN_PLAY,      // Regular goal
    PENALTY,        // Penalty goal
    FREE_KICK,      // Free kick goal
    OWN_GOAL,       // Own goal
    HEADER,         // Header goal
    VOLLEY          // Volley goal
}

enum class CardType {
    YELLOW,         // Yellow card
    RED,            // Red card
    SECOND_YELLOW   // Second yellow (red)
}

data class MatchState(
    val matchId: String,
    val status: MatchStatus,
    val currentMinute: Int = 0,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homeTeam: String,
    val awayTeam: String,
    val startTime: Long? = null,
    val endTime: Long? = null,
    val isPaused: Boolean = false,
    val events: List<MatchEvent> = emptyList(),
    val lineup: MatchLineup? = null
) {
    fun getFormattedScore(): String {
        return "$homeScore - $awayScore"
    }
    
    fun getMatchDuration(): String {
        if (startTime == null) return "0:00"
        
        val duration = if (endTime != null) {
            endTime - startTime
        } else {
            System.currentTimeMillis() - startTime
        }
        
        val minutes = (duration / 60000).toInt()
        val seconds = ((duration % 60000) / 1000).toInt()
        return String.format("%d:%02d", minutes, seconds)
    }
    
    fun isMatchActive(): Boolean {
        return status == MatchStatus.IN_PROGRESS || status == MatchStatus.PAUSED
    }
}

enum class MatchStatus {
    SCHEDULED,      // Match is scheduled but not started
    IN_PROGRESS,    // Match is currently being played
    PAUSED,         // Match is paused
    HALF_TIME,      // Half time break
    FULL_TIME,      // Match has ended
    CANCELLED       // Match was cancelled
}

data class MatchLineup(
    val matchId: String,
    val formation: String = "4-3-3", // e.g., "4-3-3", "4-4-2"
    val startingPlayers: List<LineupPlayer> = emptyList(),
    val substitutes: List<LineupPlayer> = emptyList(),
    val substitutions: List<Substitution> = emptyList()
) {
    fun getPlayerOnField(playerId: String): LineupPlayer? {
        return startingPlayers.find { it.playerId == playerId }
    }
    
    fun getSubstitute(playerId: String): LineupPlayer? {
        return substitutes.find { it.playerId == playerId }
    }
    
    fun getFormationPositions(): List<String> {
        return when (formation) {
            "4-3-3" -> listOf("GK", "LB", "CB", "CB", "RB", "CM", "CM", "CM", "LW", "ST", "RW")
            "4-4-2" -> listOf("GK", "LB", "CB", "CB", "RB", "LM", "CM", "CM", "RM", "ST", "ST")
            "3-5-2" -> listOf("GK", "CB", "CB", "CB", "LWB", "CM", "CM", "CM", "RWB", "ST", "ST")
            else -> listOf("GK", "LB", "CB", "CB", "RB", "CM", "CM", "CM", "LW", "ST", "RW")
        }
    }
}

data class LineupPlayer(
    val playerId: String,
    val playerName: String,
    val position: String, // e.g., "GK", "CB", "CM", "ST"
    val jerseyNumber: Int,
    val isOnField: Boolean = true,
    val minutesPlayed: Int = 0,
    val goals: Int = 0,
    val assists: Int = 0,
    val yellowCards: Int = 0,
    val redCards: Int = 0
) {
    fun getDisplayName(): String {
        return "$jerseyNumber. $playerName"
    }
}

data class Substitution(
    val id: String = UUID.randomUUID().toString(),
    val minute: Int,
    val playerOut: LineupPlayer,
    val playerIn: LineupPlayer,
    val timestamp: Long = System.currentTimeMillis()
) {
    fun getDescription(): String {
        return "${playerIn.playerName} ↔ ${playerOut.playerName} (${minute}')"
    }
} 