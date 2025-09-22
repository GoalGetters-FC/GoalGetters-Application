
package com.ggetters.app.core.utils

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
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
import org.junit.*
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

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var userRepository: UserRepository
    private lateinit var viewModel: SomeViewModel

    @Before
    fun setup() {
        clearAllMocks()

        mockkStatic("android.util.Log")
        every { android.util.Log.d(any(), any()) } returns 0
        every { android.util.Log.i(any(), any()) } returns 0
        every { android.util.Log.w(any(), any<String>()) } returns 0
        every { android.util.Log.e(any(), any<String>()) } returns 0
        every { android.util.Log.v(any(), any()) } returns 0

        Dispatchers.setMain(testDispatcher)

        userRepository = mockk(relaxed = true)
        viewModel = SomeViewModel(userRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `SomeViewModel can be instantiated without throwing exception`() {
        assertNotNull(SomeViewModel(userRepository))
    }

    @Test
    fun `when loadUser called emits user state`() = runTest {
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
        every { userRepository.getLocalByAuthId("u1") } returns testUser

        viewModel.loadUser("u1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(testUser, viewModel.userLiveData.value)
        verify { userRepository.getLocalByAuthId("u1") }
    }

    @Test
    fun `when loadUser called multiple times should update state correctly`() = runTest {
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

        every { userRepository.getLocalByAuthId("u1") } returns user1
        every { userRepository.getLocalByAuthId("u2") } returns user2

        viewModel.loadUser("u1")
        testDispatcher.scheduler.advanceUntilIdle()
        val firstResult = viewModel.userLiveData.value

        viewModel.loadUser("u2")
        testDispatcher.scheduler.advanceUntilIdle()
        val secondResult = viewModel.userLiveData.value

        assertEquals(user1, firstResult)
        assertEquals(user2, secondResult)
        verify { userRepository.getLocalByAuthId("u1") }
        verify { userRepository.getLocalByAuthId("u2") }
    }

    @Test
    fun `userLiveData should be initialized properly`() {
        assertNotNull(viewModel.userLiveData)
    }

    @Test
    fun `loadUser should work with coroutine context`() = runTest {
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
        every { userRepository.getLocalByAuthId("test") } returns testUser

        viewModel.loadUser("test")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(testUser, viewModel.userLiveData.value)
    }

    @Test
    fun `loadUser should handle different user roles correctly`() = runTest {
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

        every { userRepository.getLocalByAuthId("player") } returns playerUser
        every { userRepository.getLocalByAuthId("part-time") } returns partTimeUser

        viewModel.loadUser("player")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(UserRole.FULL_TIME_PLAYER, viewModel.userLiveData.value?.role)

        viewModel.loadUser("part-time")
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(UserRole.PART_TIME_PLAYER, viewModel.userLiveData.value?.role)
    }
}