// ✅ DevClass.kt
package com.ggetters.app.core.utils

import android.app.Application
import android.content.pm.ApplicationInfo
import com.ggetters.app.data.model.*
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.broadcast.BroadcastRepository
import com.ggetters.app.data.repository.broadcaststatus.BroadcastStatusRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    private lateinit var code: String

    private lateinit var userId: String
    private lateinit var eventId: String
    private lateinit var broadcastId: String

    /**
     * Seeds dev data and performs sync (debug builds only).
     * - Clears RoomDB
     * - Seeds test data (optional)
     * - Logs counts
     * - Syncs to Firestore
     */
    suspend fun init() {
        if (!isDebuggable()) return

        if (isInitialized) {
            Clogger.w("DevClass", "Already initialized. Skipping.")
            return
        }
        isInitialized = true

        Clogger.i("DevClass", "🛠 Starting dev data seed...")

        // Optional: Comment out blocks below as needed
        //clearLocalDb()
        //seedTestData()
        //logRoomCounts()
        //syncToFirestore()

        Clogger.i("DevClass", "🏁 Dev init completed")
    }

    // ———————————————————————————————————————————————————————————————
// 🔹 STEP 1: Clear Local DB
    private suspend fun clearLocalDb() {
        runCatching {
            clearAll()
            Clogger.i("DevClass", "🧹 RoomDB cleared")
        }.onFailure {
            Clogger.e("DevClass", "❌ Failed to clear RoomDB", it)
        }
    }

    // ———————————————————————————————————————————————————————————————
// 🔹 STEP 2: Seed Test Data (comment lines inside as needed)
    private suspend fun seedTestData() {
        runCatching {
             seedTeam()
            // seedUser()
            // seedEvent()
            // seedAttendance()
            // seedBroadcast()
            // seedBroadcastStatus()
            // seedLineup()
            // seedLineupWithSpots()
        }.onFailure {
            Clogger.e("DevClass", "❌ Dev data seeding failed", it)
        }
    }

    // ———————————————————————————————————————————————————————————————
// 🔹 STEP 3: Log RoomDB Counts
    private suspend fun logRoomCounts() {
        runCatching {
            val teamCount       = teamRepo.all().first().size
            val userCount       = userRepo.all().first().size
            val eventCount      = eventRepo.all().first().size
            val attendanceCount = attendanceRepo.getAll().first().size
            val broadcastCount  = broadcastRepo.all().first().size
            val statusCount     = broadcastStatusRepo.all().first().size

            Clogger.i("DevClass", "📊 After seed → teams=$teamCount, users=$userCount, " +
                    "events=$eventCount, attendance=$attendanceCount, " +
                    "broadcasts=$broadcastCount, statuses=$statusCount")
        }.onFailure {
            Clogger.i("DevClass", "⚠️ Failed read-back check")
        }
    }

    // ———————————————————————————————————————————————————————————————
// 🔹 STEP 4: Sync to Firestore
    private suspend fun syncToFirestore() {
        runCatching {
            // teamRepo.sync()
            // userRepo.sync()
            // eventRepo.sync()
            // attendanceRepo.sync()
            // broadcastRepo.sync()
            // broadcastStatusRepo.sync()
            // lineupRepo.sync()
        }.onFailure {
            Clogger.e("DevClass", "❌ Sync failed", it)
        }
    }


    /** Only run on debug builds */
    private fun isDebuggable(): Boolean {
        return app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }


    /**
     * Deletes all local data from RoomDB.
     *
     * ⚠️ This only clears **local tables** in Room. Firestore data is unaffected.
     * Currently, only `Team` is cleared, as Room does not yet enforce relational hierarchy.
     *
     * In Firestore: `Team` is the root entity, and child entities are scoped under it.
     * In Room: entities are stored flatly without foreign key cascading, so deletions must be manual.
     *
     * TODO: Extend to include manual deletion of dependent tables (User, Event, etc.)
     */
    private suspend fun clearAll() {
        runCatching {
            val teamCount = teamRepo.all().first().size
            Clogger.i("DevClass", "Room check BEFORE deleteAll: $teamCount teams")

            teamRepo.deleteAll()

            Clogger.i("DevClass", "🧹 Local DB cleared successfully")
        }.onFailure {
            Clogger.e("DevClass", "❌ Failed to clear RoomDB", it)
        }
    }

    /**
     * Creates and persists a Team using the repository.
     * Only creates if no team with the same code exists (locally or remotely).
     */
    private suspend fun seedTeam() {
        runCatching {
            code = (1000..9999).random().toString()

            // 1. Check for existing team with this code
            val existingTeam = teamRepo.getByCode(code)
            if (existingTeam != null) {
                Clogger.i("DevClass", "🚫 Team with code=$code already exists: ID=${existingTeam.id}")
                // Optionally: set as active, or skip
                teamRepo.setActiveTeam(existingTeam)
                teamId = existingTeam.id
                return@runCatching // Early exit, no need to create
            }

            // 2. Create a new team
            teamId = UUID.randomUUID().toString()
            Clogger.i("DevClass", "(1)Seeding team: ID=$teamId, Code=$code")
            val now = Instant.now()
            val team = Team(
                id = teamId,
                createdAt = now,
                updatedAt = now,
                code = code,
                name = "Holywood Team",
                alias = "GG",
                description = "Hollywood Team",
                composition = TeamComposition.UNISEX_MALE,
                denomination = TeamDenomination.U18,
                yearFormed = "2025",
                contactCell = "+27123456789",
                contactMail = "dev@goalgetters.app",
                clubAddress = "Debug Street, Dev City",
                isActive = true // Set active for dev purposes
            )
            teamRepo.createTeam(team)
            Clogger.i("DevClass", "✅ Team seeded: $teamId, Code=$code")
        }.onFailure {
            Clogger.e("DevClass", "❌ Team seeding failed", it)
        }
        Clogger.i("DevClass", "Seed team finished.")
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
