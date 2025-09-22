package com.ggetters.app.ui.shared.extensions

import com.ggetters.app.data.model.MatchEvent
import com.ggetters.app.data.model.MatchEventType
import org.junit.Assert.assertEquals
import org.junit.Test

class MatchEventExtensionsTest {
    @Test
    fun getFormattedTime_returnsMinuteWithTick() {
        val event = MatchEvent(
            matchId = "m1",
            eventType = MatchEventType.GOAL,
            minute = 23,
            createdBy = "u1"
        )

        assertEquals("23'", event.getFormattedTime())
    }

    @Test
    fun getEventDescription_handlesGoalWithPlayer() {
        val event = MatchEvent(
            matchId = "m1",
            eventType = MatchEventType.GOAL,
            minute = 10,
            playerName = "Alice",
            createdBy = "u1"
        )

        assertEquals("Goal by Alice", event.getEventDescription())
    }
}


