// ✅ DevClass.kt
package com.ggetters.app.core.utils

import android.app.Application
import com.ggetters.app.data.model.*
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.broadcast.BroadcastRepository
import com.ggetters.app.data.repository.broadcaststatus.BroadcastStatusRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

/**
 * Development utility to seed sample data in debug builds.
 *
 * Runs suspending seed functions sequentially within an IO coroutine.
 * Guards against multiple invocations and non-debug installation.
 */
class DevClass @Inject constructor(
    private val app: Application,
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository,
    private val eventRepo: EventRepository,
    private val attendanceRepo: AttendanceRepository,
    private val broadcastRepo: BroadcastRepository,
    private val broadcastStatusRepo: BroadcastStatusRepository,
    private val lineupRepo: LineupRepository
) {

    private var isInitialized = false

    // Shared IDs for linking seeded entities
    private lateinit var teamId: String
    private lateinit var userId: String
    private lateinit var eventId: String
    private lateinit var broadcastId: String

    /**
     * Public entry point. Launches seeding in IO context if app is debuggable.
     */
    suspend fun init() {
        Clogger.i("DevClass", "$isInitialized")
        if (isInitialized) return
        isInitialized = true

        Clogger.i("DevClass", "Pass 2: $isInitialized")

        try {
            clearAll()
            Clogger.i("DevClass", "🧹 Database cleared 1")
        } catch (
            e: Exception
        ) {
            Clogger.e("DevClass", "Failed to clear database", e)
        }

        try {
            seedTeam()
            seedUser()
            seedEvent()
            seedAttendance()
            seedBroadcast()
            seedBroadcastStatus()
            seedLineup()
            seedLineupWithSpots()
        } catch (e: Exception) {
            Clogger.e("DevClass", "Failed to seed dev data", e)
        }

        // 🔍 read-back checks
        val teamCount       = teamRepo.all().first().size
        val userCount       = userRepo.all().first().size
        val eventCount      = eventRepo.all().first().size
        val attendanceCount = attendanceRepo.getAll().first().size
        val broadcastCount  = broadcastRepo.all().first().size
        val statusCount     = broadcastStatusRepo.all().first().size


        Clogger.i("DevClass", "📊 After seeding → teams=$teamCount, users=$userCount, " +
                "events=$eventCount, attendance=$attendanceCount, " +
                "broadcasts=$broadcastCount, statuses=$statusCount")

        Clogger.i("DevClass", "🏁 Dev data seed completed")
    }

    /** Deletes all rows from every table in child→parent order. */
    private suspend fun clearAll() {
        runCatching {

            teamRepo.deleteAll()
            userRepo.deleteAll()
            eventRepo.deleteAll()
            attendanceRepo.deleteAll()
            broadcastRepo.deleteAll()
            broadcastStatusRepo.deleteAll()
            lineupRepo.deleteAll()
            Clogger.i("DevClass", "🧹 Database cleared 3")

            // child tables first
//            broadcastStatusRepo.all().first().forEach { broadcastStatusRepo.delete(it) }
//            broadcastRepo.all().first().forEach { broadcastRepo.delete(it) }
//            attendanceRepo.deleteAll()

            // then parents
//            eventRepo.all().first().forEach { eventRepo.delete(it) }
//            userRepo.all().first().forEach { userRepo.delete(it) }
//            teamRepo.all().first().forEach { teamRepo.delete(it) }


        }.onSuccess {
            Clogger.i("DevClass", "🧹 Database cleared 2")
        }.onFailure {
            Clogger.e("DevClass", "🧹 Failed to clear database", it)
        }
    }

    /**
     * Creates and persists a Team.
     */
    private suspend fun seedTeam() {
        runCatching {
            teamId = UUID.randomUUID().toString()
            val now = Instant.now()
            val team = Team(
                id = teamId,
                createdAt = now,
                updatedAt = now,
                code = "DEV",
                name = "Dev Team",
                alias = "DVT",
                description = "Development Team",
                composition = TeamComposition.UNISEX_MALE,
                denomination = TeamDenomination.OPEN,
                yearFormed = "2025",
                contactCell = "+27123456789",
                contactMail = "dev@goalgetters.app",
                clubAddress = "Debug Street, Dev City"
            )
            teamRepo.upsert(team)
        }.onSuccess {
            Clogger.i("DevClass", "✅ Team seeded: $teamId")
        }.onFailure {
            Clogger.e("DevClass", "❌ Team seeding failed", it)
        }
    }

    /**
     * Creates and persists a User linked to the seeded Team.
     */
    private suspend fun seedUser() {
        runCatching {
            userId = UUID.randomUUID().toString()
            val user = User(
                id = userId,
                authId = "dev-auth-${UUID.randomUUID()}",
                teamId = teamId,
                role = UserRole.FULL_TIME_PLAYER,
                name = "Dev",
                surname = "Tester",
                alias = "dev01",
                dateOfBirth = LocalDate.now().minusYears(20),
                email = "dev@test.com",
                position = UserPosition.MIDFIELDER,
                number = 7,
                status = UserStatus.ACTIVE,
                healthHeight = 1.8,
                healthWeight = 75.0
            )
            userRepo.upsert(user)
        }.onSuccess {
            Clogger.i("DevClass", "✅ User seeded: $userId")
        }.onFailure {
            Clogger.e("DevClass", "❌ User seeding failed", it)
        }
    }

    /**
     * Creates and persists an Event linked to Team and User.
     */
    private suspend fun seedEvent() {
        runCatching {
            eventId = UUID.randomUUID().toString()
            val now = LocalDateTime.now()
            val event = Event(
                id = eventId,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                teamId = teamId,
                creatorId = userId,
                name = "Test Match",
                description = "Seeded match event",
                category = 1,
                style = 0,
                startAt = now,
                endAt = now.plusHours(2),
                location = "Training Grounds"
            )
            eventRepo.upsert(event)
        }.onSuccess {
            Clogger.i("DevClass", "✅ Event seeded: $eventId")
        }.onFailure {
            Clogger.e("DevClass", "❌ Event seeding failed", it)
        }
    }

    /**
     * Creates and persists a Lineup linked to the seeded Event.
     */
    private suspend fun seedLineup() {
        runCatching {
            val lineupId = UUID.randomUUID().toString()
            val lineup = Lineup(
                id = lineupId,
                eventId = eventId,               // Make sure `eventId` is already seeded
                createdBy = userId,              // Use the same dev user ID
                formation = "4-3-3",
                spotsJson = "[]"                 // Placeholder; can populate later
            )
            lineupRepo.upsert(lineup)
        }.onSuccess {
            Clogger.i("DevClass", "✅ Lineup seeded for event: $eventId")
        }.onFailure {
            Clogger.e("DevClass", "❌ Lineup seeding failed", it)
        }
    }

    private suspend fun seedLineupWithSpots() {
        runCatching {
            val lineupId = UUID.randomUUID().toString()

            // Fake list of lineup spots
            val spots = listOf(
                LineupSpot(userId = "p01", number = 1, position = "GK", role = SpotRole.STARTER),
                LineupSpot(userId = "p02", number = 4, position = "CB", role = SpotRole.STARTER),
                LineupSpot(userId = "p03", number = 5, position = "CB", role = SpotRole.STARTER),
                LineupSpot(userId = "p04", number = 7, position = "CM", role = SpotRole.STARTER),
                LineupSpot(userId = "p05", number = 10, position = "ST", role = SpotRole.STARTER),

                LineupSpot(userId = "p06", number = 12, position = "", role = SpotRole.BENCH),
                LineupSpot(userId = "p07", number = 14, position = "", role = SpotRole.BENCH),

                LineupSpot(userId = "p08", number = 15, position = "", role = SpotRole.RESERVE),
                LineupSpot(userId = "p09", number = 16, position = "", role = SpotRole.RESERVE)
            )

            val spotsJson = Gson().toJson(spots)

            val lineup = Lineup(
                id = lineupId,
                eventId = eventId,       // make sure `eventId` is seeded
                createdBy = userId,      // your dev/test user
                formation = "4-3-3",
                spotsJson = spotsJson
            )

            lineupRepo.upsert(lineup)
        }.onSuccess {
            Clogger.i("DevClass", "✅ Lineup with spots seeded for event: $eventId")
        }.onFailure {
            Clogger.e("DevClass", "❌ Failed to seed lineup with spots", it)
        }
    }



    /**
     * Creates and persists an Attendance record for the Event and User.
     */
    private suspend fun seedAttendance() {
        runCatching {
            val attendance = Attendance(
                eventId = eventId,
                playerId = userId,
                status = 0,
                recordedBy = userId,
                recordedAt = Instant.now()
            )
            attendanceRepo.upsert(attendance)
        }.onSuccess {
            Clogger.i("DevClass", "✅ Attendance seeded: $eventId -> $userId")
        }.onFailure {
            Clogger.e("DevClass", "❌ Attendance seeding failed", it)
        }
    }

    /**
     * Creates and persists a Broadcast message for the Team.
     */
    private suspend fun seedBroadcast() {
        runCatching {
            broadcastId = UUID.randomUUID().toString()
            val broadcast = Broadcast(
                id = broadcastId,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                userId = userId,
                teamId = teamId,
                conferenceId = null,
                category = 0,
                message = "Welcome to Dev Team!"
            )
            broadcastRepo.upsert(broadcast)
        }.onSuccess {
            Clogger.i("DevClass", "✅ Broadcast seeded: $broadcastId")
        }.onFailure {
            Clogger.e("DevClass", "❌ Broadcast seeding failed", it)
        }
    }

    /**
     * Creates and persists a BroadcastStatus linking Broadcast and User.
     */
    private suspend fun seedBroadcastStatus() {
        runCatching {
            val status = BroadcastStatus(
                broadcastId = broadcastId,
                recipientId = userId
            )
            broadcastStatusRepo.upsert(status)
        }.onSuccess {
            Clogger.i("DevClass", "✅ BroadcastStatus seeded: $broadcastId -> $userId")
        }.onFailure {
            Clogger.e("DevClass", "❌ BroadcastStatus seeding failed", it)
        }
    }
}
