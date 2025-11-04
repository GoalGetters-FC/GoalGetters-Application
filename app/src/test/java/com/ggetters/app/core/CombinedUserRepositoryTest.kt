package com.ggetters.app.core

import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserPosition
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.model.UserStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class CombinedUserRepositoryTest {

    @Test
    fun `user creation with required fields`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1"
        )

        assertEquals("auth123", user.id)
        assertEquals("auth123", user.authId)
        assertEquals("team1", user.teamId)
    }

    @Test
    fun `user has default timestamps`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1"
        )

        assertNotNull(user.createdAt)
        assertNotNull(user.updatedAt)
        assertNull(user.stainedAt)
        assertNull(user.joinedAt)
    }

    @Test
    fun `user has default role`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1"
        )

        assertEquals(UserRole.FULL_TIME_PLAYER, user.role)
    }

    @Test
    fun `user with different roles`() {
        val player = User(
            id = "auth1",
            authId = "auth1",
            teamId = "team1",
            role = UserRole.FULL_TIME_PLAYER
        )

        val coach = User(
            id = "auth2",
            authId = "auth2",
            teamId = "team1",
            role = UserRole.COACH
        )

        assertEquals(UserRole.FULL_TIME_PLAYER, player.role)
        assertEquals(UserRole.COACH, coach.role)
    }

    @Test
    fun `user with name and surname`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1",
            name = "John",
            surname = "Doe"
        )

        assertEquals("John", user.name)
        assertEquals("Doe", user.surname)
    }

    @Test
    fun `user fullName combines name and surname`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1",
            name = "John",
            surname = "Doe"
        )

        assertEquals("John Doe", user.fullName())
    }

    @Test
    fun `user fullName handles empty name`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1"
        )

        assertEquals("", user.fullName())
    }

    @Test
    fun `user initials from name and surname`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1",
            name = "John",
            surname = "Doe"
        )

        assertEquals("JD", user.initials())
    }

    @Test
    fun `user initials handles empty name`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1"
        )

        assertEquals("", user.initials())
    }

    @Test
    fun `user with optional fields`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1",
            name = "John",
            surname = "Doe",
            alias = "JD",
            dateOfBirth = LocalDate.of(2000, 5, 15),
            email = "john@example.com",
            position = UserPosition.FORWARD,
            number = 10,
            status = UserStatus.ACTIVE,
            healthWeight = 75.5,
            healthHeight = 180.0
        )

        assertEquals("JD", user.alias)
        assertEquals(LocalDate.of(2000, 5, 15), user.dateOfBirth)
        assertEquals("john@example.com", user.email)
        assertEquals(UserPosition.FORWARD, user.position)
        assertEquals(10, user.number)
        assertEquals(UserStatus.ACTIVE, user.status)
        assertEquals(75.5, user.healthWeight!!, 0.01)
        assertEquals(180.0, user.healthHeight!!, 0.01)
    }

    @Test
    fun `notifyJoinedTeam sets joinedAt timestamp`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1"
        )

        assertNull(user.joinedAt)

        user.notifyJoinedTeam()

        assertNotNull(user.joinedAt)
    }

    @Test
    fun `user with different positions`() {
        val forward = User(
            id = "auth1",
            authId = "auth1",
            teamId = "team1",
            position = UserPosition.FORWARD
        )

        val midfielder = User(
            id = "auth2",
            authId = "auth2",
            teamId = "team1",
            position = UserPosition.MIDFIELDER
        )

        assertEquals(UserPosition.FORWARD, forward.position)
        assertEquals(UserPosition.MIDFIELDER, midfielder.position)
    }

    @Test
    fun `user with different statuses`() {
        val active = User(
            id = "auth1",
            authId = "auth1",
            teamId = "team1",
            status = UserStatus.ACTIVE
        )

        val inactive = User(
            id = "auth2",
            authId = "auth2",
            teamId = "team1",
            status = UserStatus.INJURY
        )

        assertEquals(UserStatus.ACTIVE, active.status)
        assertEquals(UserStatus.INJURY, inactive.status)
    }

    @Test
    fun `user with jersey number`() {
        val user = User(
            id = "auth123",
            authId = "auth123",
            teamId = "team1",
            number = 7
        )

        assertEquals(7, user.number)
    }

    @Test
    fun `multiple users for same team`() {
        val user1 = User(
            id = "auth1",
            authId = "auth1",
            teamId = "team1",
            name = "John"
        )

        val user2 = User(
            id = "auth2",
            authId = "auth2",
            teamId = "team1",
            name = "Jane"
        )

        assertEquals("team1", user1.teamId)
        assertEquals("team1", user2.teamId)
        assertEquals("John", user1.name)
        assertEquals("Jane", user2.name)
    }
}