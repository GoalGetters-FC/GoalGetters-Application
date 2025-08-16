
package com.ggetters.app.core.utils

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.UserRole
import com.ggetters.app.data.repository.user.UserRepository
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

// Mock ViewModel class since SomeViewModel doesn't exist
class SomeViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _userLiveData = MutableLiveData<User?>()
    val userLiveData = _userLiveData

    fun loadUser(userId: String) {
        viewModelScope.launch {
            try {
                val user = userRepository.getLocalByAuthId(userId)
                _userLiveData.value = user
            } catch (e: Exception) {
                // Handle error gracefully
                _userLiveData.value = null
            }
        }
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [33],
    manifest = Config.NONE,
    application = Application::class
)
class SomeViewModelTest {

    // Use JUnit 5 extension instead of Rule
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: SomeViewModel

    @BeforeEach
    fun setup() {
        // Clear all mocks before each test
        clearAllMocks()

        // Mock static Android Log to prevent RuntimeException
        mockkStatic("android.util.Log")
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>()) } returns 0
        every { android.util.Log.v(any(), any()) } returns 0

        // Set main dispatcher for testing
        Dispatchers.setMain(testDispatcher)

        // Create mock repository
        userRepository = mockk(relaxed = true)

        // Create ViewModel instance
        viewModel = SomeViewModel(userRepository)
    }

    @AfterEach
    fun tearDown() {
        // Reset main dispatcher
        Dispatchers.resetMain()
        // Clean up all mocks
        unmockkAll()
    }

    @Test
    fun `SomeViewModel can be instantiated without throwing exception`() {
        // Assert
        assertDoesNotThrow {
            SomeViewModel(userRepository)
        }
    }

    @Test
    fun `when loadUser called emits user state`() = runTest {
        // Arrange
        val testUser = User(
            id = "u1",
            name = "UT",
            surname = "TestSurname",
            alias = "test-alias",
            authId = "auth123",
            teamId = "team1",
            role = UserRole.FULL_TIME_PLAYER,
            dateOfBirth = LocalDate.of(1990, 1, 1),
            email = "test@example.com",

        )
        coEvery { userRepository.getLocalByAuthId("u1") } returns testUser

        // Act
        viewModel.loadUser("u1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertEquals(testUser, viewModel.userLiveData.value)
        coVerify { userRepository.getLocalByAuthId("u1") }
    }


    @Test
    fun `when loadUser called multiple times should update state correctly`() = runTest {
        // Arrange
        val user1 = User(
            id = "u1",
            name = "User 1",
            surname = "Surname1",
            alias = "user1-alias",
            authId = "auth1",
            teamId = "team1",
            role = UserRole.FULL_TIME_PLAYER,
            dateOfBirth = LocalDate.of(1990, 1, 1),
            email = "user1@example.com",

        )
        val user2 = User(
            id = "u2",
            name = "User 2",
            surname = "Surname2",
            alias = "user2-alias",
            authId = "auth2",
            teamId = "team2",
            role = UserRole.PART_TIME_PLAYER,
            dateOfBirth = LocalDate.of(1991, 1, 1),
            email = "user2@example.com",

        )

        coEvery { userRepository.getLocalByAuthId("u1") } returns user1
        coEvery { userRepository.getLocalByAuthId("u2") } returns user2

        // Act
        viewModel.loadUser("u1")
        testDispatcher.scheduler.advanceUntilIdle()
        val firstResult = viewModel.userLiveData.value

        viewModel.loadUser("u2")
        testDispatcher.scheduler.advanceUntilIdle()
        val secondResult = viewModel.userLiveData.value

        // Assert
        assertEquals(user1, firstResult)
        assertEquals(user2, secondResult)
        coVerify { userRepository.getLocalByAuthId("u1") }
        coVerify { userRepository.getLocalByAuthId("u2") }
    }

    @Test
    fun `userLiveData should be initialized properly`() {
        // Assert
        assertNotNull(viewModel.userLiveData)
    }

    @Test
    fun `loadUser should work with coroutine context`() = runTest {
        // Arrange
        val testUser = User(
            id = "test",
            name = "Test User",
            surname = "TestSurname",
            alias = "test-alias",
            authId = "auth-test",
            teamId = "test-team",
            role = UserRole.FULL_TIME_PLAYER,
            dateOfBirth = LocalDate.of(1990, 1, 1),
            email = "test@example.com",

        )
        coEvery { userRepository.getLocalByAuthId("test") } returns testUser

        // Act
        val result = runCatching {
            viewModel.loadUser("test")
            testDispatcher.scheduler.advanceUntilIdle()
        }

        // Assert
        assert(result.isSuccess) { "loadUser should complete successfully: ${result.exceptionOrNull()}" }
        assertEquals(testUser, viewModel.userLiveData.value)
    }


    @Test
    fun `loadUser should handle different user roles correctly`() = runTest {
        // Arrange - Test with different UserRole values
        val playerUser = User(
            id = "player",
            name = "Player User",
            surname = "PlayerSurname",
            alias = "player-alias",
            authId = "player-auth",
            teamId = "player-team",
            role = UserRole.FULL_TIME_PLAYER,
            dateOfBirth = LocalDate.of(1985, 6, 15),
            email = "player@example.com",

        )

        val partTimeUser = User(
            id = "part-time",
            name = "Part Time User",
            surname = "PartTimeSurname",
            alias = "part-time-alias",
            authId = "part-time-auth",
            teamId = "part-time-team",
            role = UserRole.PART_TIME_PLAYER,
            dateOfBirth = LocalDate.of(1992, 3, 20),
            email = "parttime@example.com",

        )

        coEvery { userRepository.getLocalByAuthId("player") } returns playerUser
        coEvery { userRepository.getLocalByAuthId("part-time") } returns partTimeUser

        // Act & Assert for Full Time Player
        viewModel.loadUser("player")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(UserRole.FULL_TIME_PLAYER, viewModel.userLiveData.value?.role)

        // Act & Assert for Part Time Player
        viewModel.loadUser("part-time")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(UserRole.PART_TIME_PLAYER, viewModel.userLiveData.value?.role)
    }
}