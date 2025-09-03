package com.ggetters.app.data.model

import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale
import java.util.UUID

// Contains all of the UI models related to Matches, Attendance, Lineups, and Match Details
// TEMP SOLUTION


/**
 * RSVP status for player availability.
 *
 * Used across Attendance, Lineups, and MatchDetails.
 */
enum class RSVPStatus {
    AVAILABLE,
    MAYBE,
    UNAVAILABLE,
    NOT_RESPONDED
}

/**
 * Aggregated RSVP statistics for a given match.
 *
 * @property available Number of players marked as AVAILABLE
 * @property maybe Number of players marked as MAYBE
 * @property unavailable Number of players marked as UNAVAILABLE
 * @property notResponded Number of players who have NOT_RESPONDED
 */
data class RSVPStats(
    val available: Int = 0,
    val maybe: Int = 0,
    val unavailable: Int = 0,
    val notResponded: Int = 0
) {
    val total: Int get() = available + maybe + unavailable + notResponded
    val canStart: Boolean get() = available >= 11
}

/**
 * Enumeration of the lifecycle states a match can be in.
 */
enum class MatchStatus {
    SCHEDULED,
    IN_PROGRESS,
    PAUSED,
    HALF_TIME,
    FULL_TIME,
    CANCELLED
}

/**
 * Unified view of a player for a match:
 *  - Base user info
 *  - RSVP / availability
 *  - Lineup assignment (position, starter/bench/reserve)
 */
data class RosterPlayer(
    val playerId: String,
    val playerName: String,
    val jerseyNumber: Int,
    val position: String,
    val status: RSVPStatus,
    val responseTime: Instant? = null,
    val notes: String? = null,
    val profileImageUrl: String? = null,

    // Lineup details
    val lineupRole: SpotRole? = null,     // e.g., STARTER, BENCH, RESERVE
    val lineupPosition: String? = null    // e.g., "CB", "GK"
)

/**
 * Comprehensive match information including metadata, RSVP statistics,
 * player availability, and formation details.
 *
 * Typically built by combining multiple repositories (events, users,
 * attendance, lineup).
 */
data class MatchDetails(
    val matchId: String,
    val title: String,
    val homeTeam: String,
    val awayTeam: String,
    val venue: String,
    val date: Instant, // changed from Long
    val time: String,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val status: MatchStatus = MatchStatus.SCHEDULED,
    val rsvpStats: RSVPStats = RSVPStats(),
    val playerAvailability: List<RosterPlayer> = emptyList(),
    val formation: String = "4-3-3",
    val createdBy: String,
    val createdAt: Instant = Instant.now()
) {
    fun getFormattedScore(): String = "$homeScore - $awayScore"

    fun getFormattedDateTime(): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return "${dateFormat.format(Date.from(date))} at $time" // Instant → Date
    }

    fun isMatchStarted(): Boolean =
        status == MatchStatus.IN_PROGRESS ||
                status == MatchStatus.PAUSED ||
                status == MatchStatus.HALF_TIME ||
                status == MatchStatus.FULL_TIME

    fun canStartMatch(): Boolean =
        status == MatchStatus.SCHEDULED && rsvpStats.available >= 11
}

//match event
// create it here
data class MatchEvent(
    val id: String = UUID.randomUUID().toString(),
    val matchId: String,
    val eventType: MatchEventType,
    val timestamp: Long = System.currentTimeMillis(),
    val minute: Int,
    val playerId: String? = null,
    val playerName: String? = null,
    val teamId: String? = null,
    val teamName: String? = null,
    val details: Map<String, Any> = emptyMap(),
    val createdBy: String,
    val isConfirmed: Boolean = true
)

enum class MatchEventType {
    GOAL,           // Goal scored
    YELLOW_CARD,    // Yellow card issued
    RED_CARD,       // Red card issued
    SUBSTITUTION,   // Player substitution
    MATCH_START,    // Match started
    MATCH_END,      // Match ended
    HALF_TIME,      // Half time break

    SCORE_UPDATE    // Manual score update
}


/**
 * Domain model representing the result of a completed match.
 *
 * @property matchId ID of the match.
 * @property homeTeam Name of the home team.
 * @property awayTeam Name of the away team.
 * @property homeScore Final score for the home team.
 * @property awayScore Final score for the away team.
 * @property matchDate Timestamp of when the match occurred.
 * @property duration Duration of the match in minutes.
 * @property goals List of goal events.
 * @property cards List of card events.
 * @property substitutions List of substitution events.
 * @property possession Map with "home"/"away" possession percentages.
 * @property shots Map with "home"/"away" total shots.
 * @property shotsOnTarget Map with "home"/"away" shots on target.
 * @property corners Map with "home"/"away" corner kicks.
 * @property fouls Map with "home"/"away" fouls committed.
 * @property notes Optional notes about the match.
 */
data class MatchResult(
    val matchId: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeScore: Int,
    val awayScore: Int,
    val matchDate: Long = Instant.now().toEpochMilli(),
    val duration: Int = 90,
    val goals: List<MatchEvent> = emptyList(),
    val cards: List<MatchEvent> = emptyList(),
    val substitutions: List<MatchEvent> = emptyList(),
    val possession: Map<String, Int> = emptyMap(),
    val shots: Map<String, Int> = emptyMap(),
    val shotsOnTarget: Map<String, Int> = emptyMap(),
    val corners: Map<String, Int> = emptyMap(),
    val fouls: Map<String, Int> = emptyMap(),
    val notes: String? = null
) {
    fun getWinner(): String? = when {
        homeScore > awayScore -> homeTeam
        awayScore > homeScore -> awayTeam
        else -> null
    }

    fun getResultText(): String = when {
        homeScore > awayScore -> "Victory"
        awayScore > homeScore -> "Defeat"
        else -> "Draw"
    }

    fun getFormattedScore(): String = "$homeScore - $awayScore"
}

/**
 * Enumeration representing the different ways a goal can be scored.
 */
enum class GoalType {
    /** Regular goal during open play */
    OPEN_PLAY,

    /** Goal scored from a penalty kick */
    PENALTY,

    /** Goal scored directly from a free kick */
    FREE_KICK,

    /** Own goal (scored against own team) */
    OWN_GOAL,

    /** Goal scored with a header */
    HEADER,

    /** Goal scored via volley */
    VOLLEY
}

/**
 * Enumeration representing the types of cards a player can receive.
 */
enum class CardType {
    /** Standard yellow card */
    YELLOW,

    /** Straight red card */
    RED,

    /** Second yellow card resulting in red */
    SECOND_YELLOW
}