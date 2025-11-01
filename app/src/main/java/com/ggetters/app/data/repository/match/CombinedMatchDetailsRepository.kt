package com.ggetters.app.data.repository.match

import com.ggetters.app.data.model.MatchDetails
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Combined implementation of MatchDetailsRepository.
 * Builds comprehensive match details by combining data from multiple repositories.
 */
@Singleton
class CombinedMatchDetailsRepository @Inject constructor(
    private val events: EventRepository,
    private val attendance: AttendanceRepository,
    private val users: UserRepository,
    private val teams: TeamRepository,
    private val matchEvents: MatchEventRepository
) : MatchDetailsRepository {

    override fun matchDetailsFlow(matchId: String): Flow<MatchDetails> {
        val eventFlow = events.all()
            .map { list -> list.firstOrNull { it.id == matchId } }
            .filterNotNull()

        val rsvpFlow = attendance.getByEventId(matchId)
        val homeTeamNameFlow = teams.getActiveTeam().map { it?.name ?: "Home" }
        val matchEventsFlow = matchEvents.getEventsByMatchId(matchId)

        return combine(eventFlow, rsvpFlow, homeTeamNameFlow, matchEventsFlow) { evt, atts, homeName, matchEvents ->
            val start: Instant = evt.startAt
                .atZone(ZoneId.systemDefault())
                .toInstant()
            val end: Instant? = evt.endAt?.atZone(ZoneId.systemDefault())?.toInstant()

            // "Match vs Liverpool (Completed)" → "Liverpool"
            val awayName = parseOpponentFromName(evt.name, homeName)

            // Parse "Chelsea 2–1 Liverpool" (if present) or calculate from events
            val (homeScore, awayScore) = if (matchEvents.isNotEmpty()) {
                calculateScoresFromEvents(matchEvents, homeName)
            } else {
                parseScores(evt.description, homeName, awayName) ?: (0 to 0)
            }

            MatchDetails(
                matchId = evt.id,
                title = evt.name,
                homeTeam = homeName,
                awayTeam = awayName,
                venue = evt.location ?: "TBD",
                date = start,
                time = DateTimeFormatter.ofPattern("HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(start),
                homeScore = homeScore,
                awayScore = awayScore,
                status = inferStatusFromStartEndAndEvents(start, end, matchEvents),
                rsvpStats = atts.toRsvpStats(),
                playerAvailability = emptyList(), // (enrich later)
                createdBy = evt.creatorId ?: "System"
            )
        }
    }

    override fun eventsFlow(matchId: String): Flow<List<MatchEvent>> {
        return matchEvents.getEventsByMatchId(matchId)
    }

    override suspend fun setRSVP(matchId: String, playerId: String, status: RSVPStatus) {
        // Convert RSVPStatus to integer for Attendance model
        // Must match RosterMapper mapping: 0=AVAILABLE, 1=UNAVAILABLE, 2=MAYBE, 3=NOT_RESPONDED
        val statusInt = when (status) {
            RSVPStatus.AVAILABLE -> 0      // Present
            RSVPStatus.UNAVAILABLE -> 1    // Absent/Unavailable
            RSVPStatus.MAYBE -> 2          // Late/Maybe
            RSVPStatus.NOT_RESPONDED -> 3  // Excused/Not responded
        }
        
        // Get existing attendance or create new one
        val existing = attendance.getById(matchId, playerId)
        val updatedAttendance = existing?.copy(status = statusInt) ?: com.ggetters.app.data.model.Attendance(
            eventId = matchId,
            playerId = playerId,
            status = statusInt,
            recordedBy = "system"
        )
        
        // Update attendance through the attendance repository
        attendance.upsert(updatedAttendance)
    }

    override suspend fun addEvent(event: MatchEvent) {
        matchEvents.insertEvent(event)
    }

    /**
     * Parse opponent team name from event title
     */
    private fun parseOpponentFromName(eventName: String?, homeName: String): String {
        if (eventName.isNullOrBlank()) return "Away Team"
        
        // If the event name is just "Match", "Practice", etc., return generic opponent
        val genericTitles = listOf("Match", "Practice", "Game", "Event", "Training")
        if (genericTitles.contains(eventName.trim())) {
            return "Away Team"
        }
        
        // Remove home team name and common suffixes
        val cleanName = eventName
            .replace(" vs ", " v ")
            .replace(" v ", " v ")
            .replace(" (Completed)", "")
            .replace(" (Live)", "")
            .replace(" (Scheduled)", "")
            .trim()
        
        // Split by common separators and find the away team
        val parts = cleanName.split(Regex("\\s+(?:vs?|v|against|@)\\s+", RegexOption.IGNORE_CASE))
        
        return when {
            parts.size >= 2 -> {
                // Find the part that's not the home team
                parts.firstOrNull { !it.equals(homeName, ignoreCase = true) } ?: "Away Team"
            }
            parts.size == 1 -> {
                // Single team name, might be the opponent
                if (parts[0].equals(homeName, ignoreCase = true)) "Away Team" else parts[0]
            }
            else -> "Away Team"
        }
    }

    /**
     * Parse scores from event description
     */
    private fun parseScores(description: String?, homeName: String, awayName: String): Pair<Int, Int>? {
        if (description.isNullOrBlank()) return null
        
        // Look for score patterns like "2-1", "2:1", "2 – 1"
        val scorePattern = Regex("(\\d+)\\s*[-:–]\\s*(\\d+)")
        val match = scorePattern.find(description)
        
        return match?.let {
            val homeScore = it.groupValues[1].toIntOrNull() ?: 0
            val awayScore = it.groupValues[2].toIntOrNull() ?: 0
            homeScore to awayScore
        }
    }

    /**
     * Infer match status from start time
     */
    private fun inferStatusFromStart(start: Instant): com.ggetters.app.data.model.MatchStatus {
        val now = Instant.now()
        val startTime = start
        val endTime = start.plus(Duration.ofMinutes(90)) // Assume 90-minute matches
        
        return when {
            now.isBefore(startTime) -> com.ggetters.app.data.model.MatchStatus.SCHEDULED
            now.isAfter(endTime) -> com.ggetters.app.data.model.MatchStatus.FULL_TIME
            else -> com.ggetters.app.data.model.MatchStatus.IN_PROGRESS
        }
    }
    
    private fun inferStatusFromStartAndEvents(start: Instant, events: List<MatchEvent>): com.ggetters.app.data.model.MatchStatus {
        val now = Instant.now()
        val startTime = start
        val endTime = start.plus(Duration.ofMinutes(90)) // Assume 90-minute matches
        
        // If there are events recorded, consider the match as in progress or live
        return when {
            now.isBefore(startTime) && events.isEmpty() -> com.ggetters.app.data.model.MatchStatus.SCHEDULED
            now.isBefore(startTime) && events.isNotEmpty() -> com.ggetters.app.data.model.MatchStatus.IN_PROGRESS // Live with events
            now.isAfter(endTime) -> com.ggetters.app.data.model.MatchStatus.FULL_TIME
            else -> com.ggetters.app.data.model.MatchStatus.IN_PROGRESS
        }
    }

    private fun inferStatusFromStartEndAndEvents(start: Instant, end: Instant?, events: List<MatchEvent>): com.ggetters.app.data.model.MatchStatus {
        val now = Instant.now()
        val startTime = start
        val endTime = end ?: start.plus(Duration.ofMinutes(90))
        
        // If there are any events, the match is considered in progress
        return when {
            events.isEmpty() && now.isBefore(startTime) -> com.ggetters.app.data.model.MatchStatus.SCHEDULED
            now.isAfter(endTime) && events.isNotEmpty() -> com.ggetters.app.data.model.MatchStatus.FULL_TIME
            events.isNotEmpty() -> com.ggetters.app.data.model.MatchStatus.IN_PROGRESS
            now.isBefore(startTime) -> com.ggetters.app.data.model.MatchStatus.SCHEDULED
            now.isAfter(endTime) -> com.ggetters.app.data.model.MatchStatus.FULL_TIME
            else -> com.ggetters.app.data.model.MatchStatus.IN_PROGRESS
        }
    }
    
    private fun calculateScoresFromEvents(events: List<MatchEvent>, homeTeamName: String): Pair<Int, Int> {
        var homeScore = 0
        var awayScore = 0
        
        events.forEach { event ->
            if (event.eventType == com.ggetters.app.data.model.MatchEventType.GOAL) {
                // Check if it's an opponent goal
                val isOpponentGoal = event.details["isOpponentGoal"] as? Boolean ?: false
                
                if (isOpponentGoal) {
                    awayScore++
                } else {
                    homeScore++
                }
            }
        }
        
        return homeScore to awayScore
    }

    /**
     * Extension function to convert attendance list to RSVP stats
     */
    private fun List<com.ggetters.app.data.model.Attendance>.toRsvpStats(): com.ggetters.app.data.model.RSVPStats {
        var available = 0
        var maybe = 0
        var unavailable = 0
        var notResponded = 0
        
        forEach { attendance ->
            when (attendance.status) {
                0 -> available++
                1 -> unavailable++
                2 -> maybe++
                else -> notResponded++
            }
        }
        
        return com.ggetters.app.data.model.RSVPStats(
            available = available,
            maybe = maybe,
            unavailable = unavailable,
            notResponded = notResponded
        )
    }
}