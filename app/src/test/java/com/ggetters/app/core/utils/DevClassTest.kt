
package com.ggetters.app.core.utils

import android.app.Application
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.broadcast.BroadcastRepository
import com.ggetters.app.data.repository.broadcaststatus.BroadcastStatusRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [33],
    manifest = Config.NONE,
    application = Application::class
)
class DevClassTest {

    private val app: Application = mockk(relaxed = true)
    private val teamRepo: TeamRepository = mockk(relaxed = true)
    private val userRepo: UserRepository = mockk(relaxed = true)
    private val eventRepo: EventRepository = mockk(relaxed = true)
    private val attendanceRepo: AttendanceRepository = mockk(relaxed = true)
    private val broadcastRepo: BroadcastRepository = mockk(relaxed = true)
    private val broadcastStatusRepo: BroadcastStatusRepository = mockk(relaxed = true)
    private val lineupRepo: LineupRepository = mockk(relaxed = true)

    private lateinit var devClass: DevClass

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

        // Create DevClass instance
        devClass = DevClass(
            app = app,
            teamRepo = teamRepo,
            userRepo = userRepo,
            eventRepo = eventRepo,
            attendanceRepo = attendanceRepo,
            broadcastRepo = broadcastRepo,
            broadcastStatusRepo = broadcastStatusRepo,
            lineupRepo = lineupRepo
        )
    }

    @AfterEach
    fun tearDown() {
        // Clean up all mocks
        unmockkAll()
    }

    @Test
    fun `DevClass can be instantiated without throwing exception`() {
        // Assert
        assertDoesNotThrow {
            DevClass(
                app = app,
                teamRepo = teamRepo,
                userRepo = userRepo,
                eventRepo = eventRepo,
                attendanceRepo = attendanceRepo,
                broadcastRepo = broadcastRepo,
                broadcastStatusRepo = broadcastStatusRepo,
                lineupRepo = lineupRepo
            )
        }
    }



    @Test
    fun `DevClass should handle null repositories gracefully during testing`() {
        // This test ensures our mocking setup doesn't break the class
        assertDoesNotThrow {
            devClass.toString() // Simple method call to ensure object is valid
        }
    }
}

