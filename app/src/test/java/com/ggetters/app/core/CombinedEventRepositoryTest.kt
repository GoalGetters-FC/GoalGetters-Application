package com.ggetters.app.core

import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.EventCategory
import com.ggetters.app.data.model.EventStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDateTime

class CombinedEventRepositoryTest {

    @Test
    fun `event creation with required fields`() {
        val event = Event(
            teamId = "team1",
            name = "Training Session",
            category = EventCategory.PRACTICE,
            style = EventStyle.TRAINING
        )

        assertNotNull(event.id)
        assertEquals("team1", event.teamId)
        assertEquals("Training Session", event.name)
        assertEquals(EventCategory.PRACTICE, event.category)
        assertEquals(EventStyle.TRAINING, event.style)
    }

    @Test
    fun `event has default timestamps`() {
        val event = Event(
            teamId = "team1",
            name = "Match Day",
            category = EventCategory.MATCH,
            style = EventStyle.STANDARD
        )

        assertNotNull(event.createdAt)
        assertNotNull(event.updatedAt)
        assertNotNull(event.startAt)
        assertNull(event.stainedAt)
    }

    @Test
    fun `event with optional fields`() {
        val event = Event(
            teamId = "team1",
            creatorId = "coach1",
            name = "Championship Final",
            description = "Season finale match",
            category = EventCategory.MATCH,
            style = EventStyle.TOURNAMENT,
            location = "Stadium A"
        )

        assertEquals("coach1", event.creatorId)
        assertEquals("Championship Final", event.name)
        assertEquals("Season finale match", event.description)
        assertEquals("Stadium A", event.location)
    }

    @Test
    fun `event with different categories`() {
        val practice = Event(
            teamId = "team1",
            name = "Practice",
            category = EventCategory.PRACTICE,
            style = EventStyle.TRAINING
        )

        val match = Event(
            teamId = "team1",
            name = "Match",
            category = EventCategory.MATCH,
            style = EventStyle.STANDARD
        )

        val other = Event(
            teamId = "team1",
            name = "Team Building",
            category = EventCategory.OTHER,
            style = EventStyle.STANDARD
        )

        assertEquals(EventCategory.PRACTICE, practice.category)
        assertEquals(EventCategory.MATCH, match.category)
        assertEquals(EventCategory.OTHER, other.category)
    }

    @Test
    fun `event with different styles`() {
        val standard = Event(
            teamId = "team1",
            name = "League Match",
            category = EventCategory.MATCH,
            style = EventStyle.STANDARD
        )

        val friendly = Event(
            teamId = "team1",
            name = "Friendly Match",
            category = EventCategory.MATCH,
            style = EventStyle.FRIENDLY
        )

        val tournament = Event(
            teamId = "team1",
            name = "Cup Match",
            category = EventCategory.MATCH,
            style = EventStyle.TOURNAMENT
        )

        val training = Event(
            teamId = "team1",
            name = "Training",
            category = EventCategory.PRACTICE,
            style = EventStyle.TRAINING
        )

        assertEquals(EventStyle.STANDARD, standard.style)
        assertEquals(EventStyle.FRIENDLY, friendly.style)
        assertEquals(EventStyle.TOURNAMENT, tournament.style)
        assertEquals(EventStyle.TRAINING, training.style)
    }

    @Test
    fun `event with start and end times`() {
        val startTime = LocalDateTime.of(2024, 10, 15, 18, 0)
        val endTime = LocalDateTime.of(2024, 10, 15, 20, 0)

        val event = Event(
            teamId = "team1",
            name = "Evening Practice",
            category = EventCategory.PRACTICE,
            style = EventStyle.TRAINING,
            startAt = startTime,
            endAt = endTime
        )

        assertEquals(startTime, event.startAt)
        assertEquals(endTime, event.endAt)
    }

    @Test
    fun `event without end time`() {
        val event = Event(
            teamId = "team1",
            name = "Open Practice",
            category = EventCategory.PRACTICE,
            style = EventStyle.TRAINING
        )

        assertNotNull(event.startAt)
        assertNull(event.endAt)
    }

    @Test
    fun `multiple events for same team`() {
        val event1 = Event(
            teamId = "team1",
            name = "Morning Practice",
            category = EventCategory.PRACTICE,
            style = EventStyle.TRAINING
        )

        val event2 = Event(
            teamId = "team1",
            name = "Evening Match",
            category = EventCategory.MATCH,
            style = EventStyle.STANDARD
        )

        assertEquals("team1", event1.teamId)
        assertEquals("team1", event2.teamId)
        assertEquals("Morning Practice", event1.name)
        assertEquals("Evening Match", event2.name)
    }
}