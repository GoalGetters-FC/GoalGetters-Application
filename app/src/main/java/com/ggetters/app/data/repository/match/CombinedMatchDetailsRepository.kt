package com.ggetters.app.data.repository.match

import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.model.MatchDetails
import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import com.ggetters.app.data.model.MatchStatus
import com.ggetters.app.data.model.RSVPStatus
import com.ggetters.app.data.model.RSVPStats
import com.ggetters.app.data.repository.attendance.CombinedAttendanceRepository
import com.ggetters.app.data.repository.event.CombinedEventRepository
import com.ggetters.app.data.repository.team.TeamRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Singleton
class CombinedMatchDetailsRepository @Inject constructor(
    private val events: CombinedEventRepository,
    private val attendance: CombinedAttendanceRepository,
    private val teams: TeamRepository
) : MatchDetailsRepository {

    override fun matchDetailsFlow(matchId: String): Flow<MatchDetails> {
        val eventFlow = events.all()
            .map { list -> list.firstOrNull { it.id == matchId } }
            .filterNotNull()

        val rsvpFlow = attendance.getByEventId(matchId)
        val homeTeamNameFlow = teams.getActiveTeam().map { it?.name ?: "Home" }

        return combine(eventFlow, rsvpFlow, homeTeamNameFlow) { evt, atts, homeName ->
            val start: Instant = evt.startAt
                ?.atZone(ZoneId.systemDefault())
                ?.toInstant()
                ?: Instant.now()

            // “Match vs Liverpool (Completed)” → “Liverpool”
            val awayName = parseOpponentFromName(evt.name, homeName)

            // Parse “Chelsea 2–1 Liverpool” (if present)
            val (homeScore, awayScore) = parseScores(evt.description, homeName, awayName) ?: (0 to 0)

            MatchDetails(
                matchId = evt.id,
                title = evt.name ?: "",
                homeTeam = homeName,
                awayTeam = awayName,
                venue = evt.location ?: "TBD",
                date = start,
                time = DateTimeFormatter.ofPattern("HH:mm")
                    .withZone(ZoneId.systemDefault())
                    .format(start),
                homeScore = homeScore,
                awayScore = awayScore,
                status = inferStatusFromStart(start),
                rsvpStats = atts.toRsvpStats(),
                playerAvailability = emptyList(), // (enrich later)
                createdBy = evt.creatorId ?: "System"
            )
        }
    }

    // Build a synthetic timeline so the completed, seeded match shows events in UI
    override fun eventsFlow(matchId: String): Flow<List<MatchEvent>> {
        val eventFlow = events.all()
            .map { list -> list.firstOrNull { it.id == matchId } }
            .filterNotNull()
        val homeTeamNameFlow = teams.getActiveTeam().map { it?.name ?: "Home" }

        return combine(eventFlow, homeTeamNameFlow) { evt, homeName ->
            val awayName = parseOpponentFromName(evt.name, homeName)

            val endMin = runCatching {
                val s = evt.startAt
                val e = evt.endAt
                if (s != null && e != null) {
                    Duration.between(
                        s.atZone(ZoneId.systemDefault()),
                        e.atZone(ZoneId.systemDefault())
                    ).toMinutes().toInt().coerceAtLeast(90)
                } else 90
            }.getOrDefault(90)

            val list = mutableListOf<MatchEvent>()

            // Kickoff
            list += MatchEvent(
                matchId = evt.id,
                eventType = MatchEventType.MATCH_START,
                minute = 0,
                createdBy = "system"
            )

            // Half-time (best-effort)
            list += MatchEvent(
                matchId = evt.id,
                eventType = MatchEventType.HALF_TIME,
                minute = 45,
                createdBy = "system"
            )

            // Goals from “Scorers: ...”
            list += parseGoalsFromDescription(
                evtId = evt.id,
                desc = evt.description,
                homeTeam = homeName,
                awayTeam = awayName
            )

            // Full-time
            list += MatchEvent(
                matchId = evt.id,
                eventType = MatchEventType.MATCH_END,
                minute = endMin,
                createdBy = "system"
            )

            // Newest first like your desired UI
            list.sortedWith(
                compareByDescending<MatchEvent> { it.minute }
                    .thenBy { it.eventType.ordinal }
            )
        }
    }

    override suspend fun setRSVP(matchId: String, playerId: String, status: RSVPStatus) {
        val mapped = when (status) {
            RSVPStatus.AVAILABLE     -> 0 // Present
            RSVPStatus.MAYBE         -> 2 // Late (or treat as Available if you prefer)
            RSVPStatus.UNAVAILABLE   -> 1 // Absent
            RSVPStatus.NOT_RESPONDED -> 3 // Not responded
        }
        val att = Attendance(
            eventId = matchId,
            playerId = playerId,
            status = mapped,
            recordedBy = "system"
        )
        attendance.upsert(att)
    }

    override suspend fun addEvent(event: MatchEvent) {
        // TODO: wire to performance/timeline repo when available
    }

    // -------- helpers --------

    private fun inferStatusFromStart(start: Instant): MatchStatus =
        if (Instant.now().isBefore(start)) MatchStatus.SCHEDULED else MatchStatus.FULL_TIME

    /** Attendance(Int) → RSVP buckets. */
    private fun List<Attendance>.toRsvpStats(): RSVPStats {
        var available = 0
        var maybe = 0
        var unavailable = 0
        var notResponded = 0
        for (a in this) {
            when (a.status) {
                0 -> available++      // Present
                2 -> maybe++          // Late
                1, 3 -> unavailable++ // Absent / Excused / treat NR as unavailable until roster join
                else -> notResponded++
            }
        }
        return RSVPStats(available, maybe, unavailable, notResponded)
    }

    /** Extract opponent from an event name like “Match vs Liverpool (Completed)”. */
    private fun parseOpponentFromName(name: String?, homeName: String): String {
        if (name.isNullOrBlank()) return "Opponent"
        val m = Regex("(?i)\\bvs\\b\\s*[:\\-]?\\s*(.+)").find(name)
        val raw = (m?.groupValues?.get(1) ?: "Opponent")
        return raw.replace(Regex("\\(.*\\)\$"), "").trim()
            .takeIf { it.isNotEmpty() && !it.equals(homeName, ignoreCase = true) }
            ?: "Opponent"
    }

    /**
     * Parse scores from description like:
     * “Full-time: Chelsea 2–1 Liverpool. Scorers: …”
     * Returns Pair(home, away) or null.
     */
    private fun parseScores(desc: String?, home: String, away: String): Pair<Int, Int>? {
        if (desc.isNullOrBlank()) return null

        // Strict: with team names
        val strict = Regex(
            "(?i)${Regex.escape(home)}\\s*(\\d+)\\s*[–-]\\s*(\\d+)\\s*${Regex.escape(away)}"
        ).find(desc)
        if (strict != null) {
            val h = strict.groupValues[1].toIntOrNull()
            val a = strict.groupValues[2].toIntOrNull()
            if (h != null && a != null) return h to a
        }

        // Fallback: first “N–M” pair
        val generic = Regex("(\\d+)\\s*[–-]\\s*(\\d+)").find(desc)
        if (generic != null) {
            val h = generic.groupValues[1].toIntOrNull()
            val a = generic.groupValues[2].toIntOrNull()
            if (h != null && a != null) return h to a
        }
        return null
    }

    /**
     * Build GOAL events from a “Scorers: …” string.
     * Example: "Scorers: Jackson 23', Palmer 67' — Opponent 78'"
     * Home scorers left of the dash, away scorers to the right.
     */
    private fun parseGoalsFromDescription(
        evtId: String,
        desc: String?,
        homeTeam: String,
        awayTeam: String
    ): List<MatchEvent> {
        if (desc.isNullOrBlank()) return emptyList()
        val idx = desc.indexOf("Scorers:", ignoreCase = true)
        if (idx == -1) return emptyList()
        val payload = desc.substring(idx + 8).trim()

        // Split home/away lists around em/en dash or hyphen
        val parts = payload.split(Regex("\\s+[–—-]\\s+"), limit = 2)
        val homePart = parts.getOrNull(0) ?: ""
        val awayPart = parts.getOrNull(1) ?: ""

        fun goalsFrom(part: String, teamId: String, teamName: String): List<MatchEvent> {
            // Names with unicode letters, spaces, dots, apostrophes; minute like 23'
            val r = Regex("([\\p{L}.'`\\-\\s]+?)\\s+(\\d+)'")
            return r.findAll(part).map { m ->
                val player = m.groupValues[1].trim()
                val minute = m.groupValues[2].toIntOrNull() ?: 0
                MatchEvent(
                    matchId = evtId,
                    eventType = MatchEventType.GOAL,
                    minute = minute,
                    playerName = player,
                    teamId = teamId,
                    teamName = teamName,
                    createdBy = "system"
                )
            }.toList()
        }

        return goalsFrom(homePart, "home", homeTeam) + goalsFrom(awayPart, "away", awayTeam)
    }
}
