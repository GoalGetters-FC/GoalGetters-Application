package com.ggetters.app.core

import com.ggetters.app.data.model.Broadcast
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class CombinedBroadcastRepositoryTest {

    @Test
    fun `broadcast object creation with required fields`() {
        val broadcast = Broadcast(
            userId = "user1",
            teamId = "team1",
            conferenceId = null,
            category = 0,
            message = "Practice tomorrow at 5pm"
        )

        assertNotNull(broadcast.id)
        assertEquals("user1", broadcast.userId)
        assertEquals("team1", broadcast.teamId)
        assertEquals(0, broadcast.category)
        assertEquals("Practice tomorrow at 5pm", broadcast.message)
    }

    @Test
    fun `broadcast has default timestamps`() {
        val broadcast = Broadcast(
            userId = "user1",
            teamId = "team1",
            conferenceId = null,
            category = 0,
            message = "Team meeting tonight"
        )

        assertNotNull(broadcast.createdAt)
        assertNotNull(broadcast.updatedAt)
        assertNull(broadcast.stainedAt)
        assertNull(broadcast.stashedAt)
    }

    @Test
    fun `broadcast with different categories`() {
        val announcement = Broadcast(
            userId = "coach1",
            teamId = "team1",
            conferenceId = null,
            category = 0,
            message = "Match postponed"
        )

        val reminder = Broadcast(
            userId = "coach1",
            teamId = "team1",
            conferenceId = null,
            category = 1,
            message = "Bring equipment"
        )

        assertEquals(0, announcement.category)
        assertEquals(1, reminder.category)
    }

    @Test
    fun `stain marks broadcast as read`() {
        val broadcast = Broadcast(
            userId = "user1",
            teamId = "team1",
            conferenceId = null,
            category = 0,
            message = "Test message"
        )

        assertNull(broadcast.stainedAt)

        broadcast.stain()

        assertNotNull(broadcast.stainedAt)
    }

    @Test
    fun `unstain clears read flag`() {
        val broadcast = Broadcast(
            userId = "user1",
            teamId = "team1",
            conferenceId = null,
            category = 0,
            message = "Test message"
        )

        broadcast.stain()
        assertNotNull(broadcast.stainedAt)

        broadcast.unstain()
        assertNull(broadcast.stainedAt)
    }

    @Test
    fun `stash archives broadcast`() {
        val broadcast = Broadcast(
            userId = "user1",
            teamId = "team1",
            conferenceId = null,
            category = 0,
            message = "Test message"
        )

        assertNull(broadcast.stashedAt)

        broadcast.stash()

        assertNotNull(broadcast.stashedAt)
    }

    @Test
    fun `unstash un-archives broadcast`() {
        val broadcast = Broadcast(
            userId = "user1",
            teamId = "team1",
            conferenceId = null,
            category = 0,
            message = "Test message"
        )

        broadcast.stash()
        assertNotNull(broadcast.stashedAt)

        broadcast.unstash()
        assertNull(broadcast.stashedAt)
    }

    @Test
    fun `broadcast with conference id`() {
        val broadcast = Broadcast(
            userId = "user1",
            teamId = "team1",
            conferenceId = "conf1",
            category = 0,
            message = "Conference update"
        )

        assertEquals("conf1", broadcast.conferenceId)
    }
}