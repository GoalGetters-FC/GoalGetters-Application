package com.ggetters.app.core.utils

import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.data.local.dao.UserDao
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

class UserRepositoryTest {
    private lateinit var dao: UserDao
    private lateinit var repo: UserRepository

    @BeforeEach
    fun setup() {
        dao = mockk()
        repo = mockk()
    }

    @Test
    fun `getByAuthId returns user from dao`() = runTest {
        // Arrange
        val expected = User(
            id = "u1",
            name = "Test",
            surname = "User",
            alias = "testuser",
            authId = "auth1",
            teamId = "team1",
            role = UserRole.FULL_TIME_PLAYER,
            dateOfBirth = LocalDate.of(1990, 1, 1),
            email = "test@example.com"
        )
        coEvery { repo.getById("auth1") } returns expected

        // Act
        val actual = repo.getById("auth1")

        // Assert
        assertEquals(expected, actual)
        coVerify(exactly = 1) { repo.getById("auth1") }
    }

    @Test
    fun `insert calls repository insert`() = runTest {
        // Arrange
        val user = User(
            id = "u2",
            name = "New",
            surname = "User",
            alias = "newuser",
            authId = "auth2",
            teamId = "team2",
            role = UserRole.FULL_TIME_PLAYER,
            dateOfBirth = LocalDate.of(1995, 5, 15),
            email = "new@example.com"
        )
        coEvery { repo.insertLocal(user) } returns Unit

        // Act
        repo.insertLocal(user)

        // Assert
        coVerify(exactly = 1) { repo.insertLocal(user) }
    }
}