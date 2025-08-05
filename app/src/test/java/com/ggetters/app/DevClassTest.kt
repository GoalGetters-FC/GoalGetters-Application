package com.ggetters.app.core.utils

import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.User
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.time.Instant
import java.util.*

@ExperimentalCoroutinesApi
class DevClassTest {

    // Mock dependencies
    private val mockTeamRepo = mockk<TeamRepository>(relaxed = true)
    private val mockUserRepo = mockk<UserRepository>(relaxed = true)

    // System under test
    private lateinit var devClass: DevClass

    // Test dispatcher for coroutines
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        // Set up test coroutine dispatcher
        Dispatchers.setMain(testDispatcher)

        // Initialize the class under test
        devClass = DevClass(mockTeamRepo, mockUserRepo)

        // Clear all mocks
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        // Reset main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun `constructor initializes with correct dependencies`() {
        // Given, When, Then
        // The constructor should complete without throwing exceptions
        // and the class should be in its initial state
        assertNotNull(devClass)
    }

    // ========== TESTS FOR COMMENTED CODE ==========
    // These tests show how you would test the init() method if it were uncommented

    @Test
    fun `init should create team and user when called for first time`() = runTest {
        // Given - Mock repository behavior within coroutine scope
        coEvery { mockTeamRepo.getById(any()) } returns null

        // When
        // devClass.init() // Uncomment this when init() is uncommented

        // Then
        // verify { mockTeamRepo.getById(any()) }
        // coVerify { mockTeamRepo.save(any()) }
        // coVerify { mockUserRepo.save(any()) }
    }

    @Test
    fun `init should not create team when team already exists`() = runTest {
        // Given
        val existingTeam = mockk<Team>(relaxed = true)
        coEvery { mockTeamRepo.getById(any()) } returns existingTeam

        // When
        // devClass.init() // Uncomment this when init() is uncommented

        // Then
        // verify { mockTeamRepo.getById(any()) }
        // coVerify(exactly = 0) { mockTeamRepo.save(any()) } // Should not save team
        // coVerify { mockUserRepo.save(any()) } // Should still save user
    }

    @Test
    fun `init should handle user save failure gracefully`() = runTest {
        // Given
        coEvery { mockTeamRepo.getById(any()) } returns null

        // When
        // devClass.init() // Uncomment this when init() is uncommented

        // Then - should not throw exception, error should be caught and logged
        // coVerify { mockTeamRepo.save(any()) }
        // coVerify { mockUserRepo.save(any()) }
    }

    @Test
    fun `init should only run once even when called multiple times`() = runTest {
        // Given
        coEvery{ mockTeamRepo.getById(any()) } returns null

        // When
        // devClass.init() // First call
        // devClass.init() // Second call

        // Then - repositories should only be called once due to isInitialized flag
        // verify(exactly = 1) { mockTeamRepo.getById(any()) }
        // coVerify(atMost = 1) { mockTeamRepo.save(any()) }
        // coVerify(exactly = 1) { mockUserRepo.save(any()) }
    }

    @Test
    fun `init creates team with correct properties when team does not exist`() = runTest {
        // Given
        coEvery { mockTeamRepo.getById(any()) } returns null
        val teamSlot = slot<Team>()

        // When
        // devClass.init()

        // Then - verify team properties match the implementation
        // coVerify { mockTeamRepo.save(capture(teamSlot)) }
        // val capturedTeam = teamSlot.captured
        // assertEquals("DEV002", capturedTeam.code)
        // assertEquals("Dev Team", capturedTeam.name)
        // assertNotNull(capturedTeam.createdAt)
        // assertNotNull(capturedTeam.updatedAt)
        // assertNotNull(capturedTeam.id) // Will be a UUID string
    }

    @Test
    fun `init creates user with correct properties`() = runTest {
        // Given
        coEvery { mockTeamRepo.getById(any()) } returns null
        val userSlot = slot<User>()

        // When
        // devClass.init()

        // Then - verify user properties match the implementation
        // coVerify { mockUserRepo.save(capture(userSlot)) }
        // val capturedUser = userSlot.captured
        // assertTrue(capturedUser.authId.startsWith("test-"))
        // assertNotNull(capturedUser.teamId) // Will be the generated team ID
        // assertEquals(2, capturedUser.role)
        // assertEquals("Test2", capturedUser.name)
        // assertEquals("User2", capturedUser.surname)
        // assertEquals("tester2", capturedUser.alias)
        // assertNotNull(capturedUser.dateOfBirth)
    }
}