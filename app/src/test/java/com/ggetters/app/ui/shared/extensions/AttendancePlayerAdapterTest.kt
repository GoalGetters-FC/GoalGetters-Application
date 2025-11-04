package com.ggetters.app.ui.shared.extensions

import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.model.AttendanceWithUser
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AttendancePlayerAdapterTest {

    @Test
    fun `attendanceWithUser creation combines attendance and user`() {
        val attendance = Attendance(
            eventId = "event1",
            playerId = "user1",
            status = 0,
            recordedBy = "coach1"
        )

        val user = User(
            id = "user1",
            authId = "auth1",
            teamId = "team1",
            name = "John",
            surname = "Doe",
            number = 10
        )

        val attendanceWithUser = AttendanceWithUser(
            attendance = attendance,
            user = user
        )

        assertEquals(attendance, attendanceWithUser.attendance)
        assertEquals(user, attendanceWithUser.user)
    }

    @Test
    fun `attendance status values map correctly`() {
        val presentAttendance = Attendance("event1", "user1", 0, "coach")
        val absentAttendance = Attendance("event1", "user2", 1, "coach")
        val lateAttendance = Attendance("event1", "user3", 2, "coach")
        val excusedAttendance = Attendance("event1", "user4", 3, "coach")

        assertEquals(0, presentAttendance.status) // Present
        assertEquals(1, absentAttendance.status)  // Absent
        assertEquals(2, lateAttendance.status)    // Late
        assertEquals(3, excusedAttendance.status) // Excused
    }

    @Test
    fun `user fullName displays correctly for adapter`() {
        val user = User(
            id = "user1",
            authId = "auth1",
            teamId = "team1",
            name = "John",
            surname = "Doe"
        )

        assertEquals("John Doe", user.fullName())
    }

    @Test
    fun `user jersey number displays correctly`() {
        val userWithNumber = User(
            id = "user1",
            authId = "auth1",
            teamId = "team1",
            name = "John",
            surname = "Doe",
            number = 10
        )

        val userWithoutNumber = User(
            id = "user2",
            authId = "auth2",
            teamId = "team1",
            name = "Jane",
            surname = "Smith"
        )

        assertEquals(10, userWithNumber.number)
        assertEquals(null, userWithoutNumber.number)
    }

    @Test
    fun `multiple attendance records with different statuses`() {
        val user1 = User(
            id = "user1",
            authId = "auth1",
            teamId = "team1",
            name = "John",
            surname = "Doe"
        )
        val user2 = User(
            id = "user2",
            authId = "auth2",
            teamId = "team1",
            name = "Jane",
            surname = "Smith"
        )

        val attendance1 = Attendance("event1", "user1", 0, "coach") // Present
        val attendance2 = Attendance("event1", "user2", 1, "coach") // Absent

        val record1 = AttendanceWithUser(attendance1, user1)
        val record2 = AttendanceWithUser(attendance2, user2)

        assertEquals(0, record1.attendance.status)
        assertEquals(1, record2.attendance.status)
        assertEquals("John Doe", record1.user.fullName())
        assertEquals("Jane Smith", record2.user.fullName())
    }

    @Test
    fun `attendance items are different when users differ`() {
        val user1 = User(
            id = "user1",
            authId = "auth1",
            teamId = "team1",
            name = "John",
            surname = "Doe"
        )
        val user2 = User(
            id = "user2",
            authId = "auth2",
            teamId = "team1",
            name = "Jane",
            surname = "Smith"
        )

        val attendance1 = Attendance("event1", "user1", 0, "coach")
        val attendance2 = Attendance("event1", "user2", 0, "coach")

        val record1 = AttendanceWithUser(attendance1, user1)
        val record2 = AttendanceWithUser(attendance2, user2)

        assertNotEquals(record1.user.id, record2.user.id)
    }

    @Test
    fun `attendance items are different when status differs`() {
        val user = User(
            id = "user1",
            authId = "auth1",
            teamId = "team1",
            name = "John",
            surname = "Doe"
        )

        val presentAttendance = Attendance("event1", "user1", 0, "coach")
        val absentAttendance = Attendance("event1", "user1", 1, "coach")

        val presentRecord = AttendanceWithUser(presentAttendance, user)
        val absentRecord = AttendanceWithUser(absentAttendance, user)

        assertNotEquals(presentRecord.attendance.status, absentRecord.attendance.status)
    }

    @Test
    fun `attendance for player with all roles`() {
        val player = User(
            id = "user1",
            authId = "auth1",
            teamId = "team1",
            role = UserRole.FULL_TIME_PLAYER,
            name = "John",
            surname = "Doe",
            number = 7
        )

        val attendance = Attendance("event1", "user1", 0, "coach")
        val record = AttendanceWithUser(attendance, player)

        assertEquals(UserRole.FULL_TIME_PLAYER, record.user.role)
        assertEquals(7, record.user.number)
        assertEquals(0, record.attendance.status)
    }

    @Test
    fun `list of attendance records maintains order`() {
        val user1 = User(
            id = "user1",
            authId = "auth1",
            teamId = "team1",
            name = "Alice",
            surname = "A",
            number = 1
        )
        val user2 = User(
            id = "user2",
            authId = "auth2",
            teamId = "team1",
            name = "Bob",
            surname = "B",
            number = 2
        )
        val user3 = User(
            id = "user3",
            authId = "auth3",
            teamId = "team1",
            name = "Charlie",
            surname = "C",
            number = 3
        )

        val records = listOf(
            AttendanceWithUser(Attendance("event1", "user1", 0, "coach"), user1),
            AttendanceWithUser(Attendance("event1", "user2", 1, "coach"), user2),
            AttendanceWithUser(Attendance("event1", "user3", 2, "coach"), user3)
        )

        assertEquals(3, records.size)
        assertEquals("Alice A", records[0].user.fullName())
        assertEquals("Bob B", records[1].user.fullName())
        assertEquals("Charlie C", records[2].user.fullName())
    }
}