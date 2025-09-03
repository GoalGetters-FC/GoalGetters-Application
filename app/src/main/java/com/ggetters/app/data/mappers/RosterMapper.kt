// app/src/main/java/com/ggetters/app/data/mappers/RosterMapper.kt
package com.ggetters.app.data.mappers

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

        return users.map { user ->
            val rsvp = attendances.find { it.playerId == user.id }
            val spot = spots.find { it.userId == user.id }

            RosterPlayer(
                playerId = user.id,
                playerName = "${user.name} ${user.surname}",
                jerseyNumber = user.number ?: 0,
                position = user.position?.name ?: "N/A",
                status = rsvp?.status?.toRsvpStatus() ?: RSVPStatus.NOT_RESPONDED,
                responseTime = rsvp?.recordedAt,
                notes = null,
                profileImageUrl = null,
                lineupRole = spot?.role,
                lineupPosition = spot?.position
            )
        }
    }

    private fun Int.toRsvpStatus(): RSVPStatus = when (this) {
        1 -> RSVPStatus.AVAILABLE
        2 -> RSVPStatus.MAYBE
        3 -> RSVPStatus.UNAVAILABLE
        else -> RSVPStatus.NOT_RESPONDED
    }
}
