package com.ggetters.app.core

import com.ggetters.app.data.model.Lineup
import com.ggetters.app.data.model.LineupSpot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CombinedLineupRepositoryTest {

    @Test
    fun `lineup creation with required fields`() {
        val lineup = Lineup(
            eventId = "event1",
            formation = "4-4-2"
        )

        assertNotNull(lineup.id)
        assertEquals("event1", lineup.eventId)
        assertEquals("4-4-2", lineup.formation)
    }

    @Test
    fun `lineup has default timestamps`() {
        val lineup = Lineup(
            eventId = "event1",
            formation = "4-3-3"
        )

        assertNotNull(lineup.createdAt)
        assertNotNull(lineup.updatedAt)
        assertNull(lineup.stainedAt)
    }

    @Test
    fun `lineup with empty spots by default`() {
        val lineup = Lineup(
            eventId = "event1",
            formation = "4-4-2"
        )

        assertTrue(lineup.spots.isEmpty())
    }

    @Test
    fun `lineup with creator`() {
        val lineup = Lineup(
            eventId = "event1",
            createdBy = "coach1",
            formation = "3-5-2"
        )

        assertEquals("coach1", lineup.createdBy)
    }

    @Test
    fun `lineup with different formations`() {
        val formation442 = Lineup(
            eventId = "event1",
            formation = "4-4-2"
        )

        val formation433 = Lineup(
            eventId = "event1",
            formation = "4-3-3"
        )

        val formation352 = Lineup(
            eventId = "event1",
            formation = "3-5-2"
        )

        assertEquals("4-4-2", formation442.formation)
        assertEquals("4-3-3", formation433.formation)
        assertEquals("3-5-2", formation352.formation)
    }



    @Test
    fun `multiple lineups for same event`() {
        val lineup1 = Lineup(
            eventId = "event1",
            formation = "4-4-2",
            createdBy = "coach1"
        )

        val lineup2 = Lineup(
            eventId = "event1",
            formation = "4-3-3",
            createdBy = "coach2"
        )

        assertEquals("event1", lineup1.eventId)
        assertEquals("event1", lineup2.eventId)
        assertEquals("4-4-2", lineup1.formation)
        assertEquals("4-3-3", lineup2.formation)
    }

    @Test
    fun `lineup without creator`() {
        val lineup = Lineup(
            eventId = "event1",
            formation = "4-4-2"
        )

        assertNull(lineup.createdBy)
    }
}