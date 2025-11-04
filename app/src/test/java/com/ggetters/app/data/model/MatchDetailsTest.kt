package com.ggetters.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class MatchDetailsTest {
    @Test
    fun getFormattedScore_returnsHomeDashAway() {
        val md = MatchDetails(
            matchId = "m1",
            title = "T",
            homeTeam = "A",
            awayTeam = "B",
            venue = "V",
            date = Instant.now(),
            time = "12:00",
            homeScore = 2,
            awayScore = 1,
            createdBy = "u1"
        )
        assertEquals("2 - 1", md.getFormattedScore())
    }

    @Test
    fun isMatchStarted_trueForInProgress() {
        val md = MatchDetails(
            matchId = "m1",
            title = "T",
            homeTeam = "A",
            awayTeam = "B",
            venue = "V",
            date = Instant.now(),
            time = "12:00",
            status = MatchStatus.IN_PROGRESS,
            createdBy = "u1"
        )
        assertTrue(md.isMatchStarted())
    }

    @Test
    fun canStartMatch_requiresScheduledAndAtLeastElevenAvailable() {
        val stats = RSVPStats(available = 11)
        val md = MatchDetails(
            matchId = "m1",
            title = "T",
            homeTeam = "A",
            awayTeam = "B",
            venue = "V",
            date = Instant.now(),
            time = "12:00",
            status = MatchStatus.SCHEDULED,
            rsvpStats = stats,
            createdBy = "u1"
        )
        assertTrue(md.canStartMatch())

        val md2 = md.copy(status = MatchStatus.CANCELLED)
        assertFalse(md2.canStartMatch())
    }
}


