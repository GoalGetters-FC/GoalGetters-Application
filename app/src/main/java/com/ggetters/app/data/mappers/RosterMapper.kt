// app/src/main/java/com/ggetters/app/data/mappers/RosterMapper.kt
package com.ggetters.app.data.mappers

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.model.*

/**
 * Maps Users, Attendance, and Lineup into a unified list of RosterPlayer.
 */
object RosterMapper {

    fun merge(
        users: List<User>,
        attendances: List<Attendance>,
        lineup: Lineup?
    ): List<RosterPlayer> {
        val spots = lineup?.spots ?: emptyList()

        // Filter out coaches - only include players
        val playersOnly = users.filter { it.role != UserRole.COACH }

        return playersOnly.map { user ->
            val rsvp = attendances.find { it.playerId == user.id }
            val spot = spots.find { it.userId == user.id }
            
            val attendanceStatus = rsvp?.status
            val rsvpStatus = attendanceStatus?.toRsvpStatus() ?: RSVPStatus.NOT_RESPONDED
            
            Clogger.d("RosterMapper", "User ${user.name} ${user.surname}: attendanceStatus=$attendanceStatus, rsvpStatus=$rsvpStatus")

            RosterPlayer(
                playerId = user.id,
                playerName = "${user.name} ${user.surname}",
                jerseyNumber = user.number ?: 0,
                position = user.position?.name ?: "N/A",
                status = rsvpStatus,
                responseTime = rsvp?.recordedAt,
                notes = null,
                profileImageUrl = null,
                lineupRole = spot?.role,
                lineupPosition = spot?.position
            )
        }
    }

    private fun Int.toRsvpStatus(): RSVPStatus = when (this) {
        0 -> RSVPStatus.AVAILABLE  // Present
        1 -> RSVPStatus.UNAVAILABLE  // Absent
        2 -> RSVPStatus.MAYBE  // Late
        3 -> RSVPStatus.NOT_RESPONDED  // Excused
        else -> RSVPStatus.NOT_RESPONDED
    }
}
