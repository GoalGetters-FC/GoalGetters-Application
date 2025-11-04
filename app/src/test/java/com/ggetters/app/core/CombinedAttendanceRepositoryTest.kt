package com.ggetters.app.core

import com.ggetters.app.data.model.Attendance
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CombinedAttendanceRepositoryTest {

    @Test
    fun `attendance object creation with required fields`() {
        val attendance = Attendance(
            eventId = "1",
            playerId = "101",
            status = 0,
            recordedBy = "coach"
        )

        assertEquals("1", attendance.eventId)
        assertEquals("101", attendance.playerId)
        assertEquals(0, attendance.status)
        assertEquals("coach", attendance.recordedBy)
    }

    @Test
    fun `attendance status values are integers`() {
        val present = Attendance("1", "101", 0, "coach")
        val absent = Attendance("1", "102", 1, "coach")
        val late = Attendance("1", "103", 2, "coach")
        val excused = Attendance("1", "104", 3, "coach")

        assertEquals(0, present.status)
        assertEquals(1, absent.status)
        assertEquals(2, late.status)
        assertEquals(3, excused.status)
    }

    @Test
    fun `attendance has default timestamps`() {
        val attendance = Attendance(
            eventId = "1",
            playerId = "101",
            status = 0,
            recordedBy = "coach"
        )

        assertNotNull(attendance.recordedAt)
        assertNotNull(attendance.createdAt)
        assertNotNull(attendance.updatedAt)
    }

    @Test
    fun `attendance with optional notes`() {
        val withNotes = Attendance(
            eventId = "1",
            playerId = "101",
            status = 0,
            recordedBy = "coach",
            notes = "Had doctor appointment"
        )

        val withoutNotes = Attendance(
            eventId = "1",
            playerId = "102",
            status = 0,
            recordedBy = "coach"
        )

        assertEquals("Had doctor appointment", withNotes.notes)
        assertEquals(null, withoutNotes.notes)
    }
}