// ✅ DevClass.kt
package com.ggetters.app.core.utils

import android.app.Application
import android.content.pm.ApplicationInfo
import com.ggetters.app.core.usecases.ResetAndRehydrate
import com.ggetters.app.data.model.*
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.broadcast.BroadcastRepository
import com.ggetters.app.data.repository.broadcaststatus.BroadcastStatusRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

class DevClass @Inject constructor(
    private val app: Application,
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository,
    private val eventRepo: EventRepository,
    private val attendanceRepo: AttendanceRepository,
    private val broadcastRepo: BroadcastRepository,
    private val broadcastStatusRepo: BroadcastStatusRepository,
    private val lineupRepo: LineupRepository,
    private val resetAndRehydrate: ResetAndRehydrate
) {
    private var isInitialized = false

    private lateinit var teamId: String
    private lateinit var code: String
    private lateinit var userId: String
    private lateinit var eventId: String
    private lateinit var broadcastId: String

    private data class DevFlags(
        val clearLocal: Boolean = false,
        val seed: Boolean = false,
        val logCounts: Boolean = false,
        val pushToRemote: Boolean = false,
        val rehydrateFromRemote: Boolean = false
    )

    private val flags = DevFlags(
        clearLocal = false,
        seed = false,
        logCounts = true,
        pushToRemote = false,
        rehydrateFromRemote = true
    )

    suspend fun init() {
        if (!isDebuggable() || isInitialized) return
        isInitialized = true

        if (flags.clearLocal) clearLocalDb()
        if (flags.seed) seedTestData()
        if (flags.rehydrateFromRemote) {
            runCatching { resetAndRehydrate() }
                .onFailure { Clogger.e("DevClass", "rehydrate failed", it) }
        }
        if (flags.logCounts) logRoomCounts()
        if (flags.pushToRemote) syncToFirestore()
    }

    private suspend fun clearLocalDb() {
        runCatching { clearAll() }
            .onSuccess { Clogger.i("DevClass", "🧹 RoomDB cleared") }
            .onFailure { Clogger.e("DevClass", "❌ Failed to clear RoomDB", it) }
    }

    private suspend fun seedTestData() {
        runCatching {
            //seedTeam()
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

    private suspend fun logRoomCounts() {
        runCatching {
            val teamCount = teamRepo.all().first().size
            val userCount = userRepo.all().first().size
            val eventCount = eventRepo.all().first().size
            val attendanceCount = attendanceRepo.getAll().first().size
            val broadcastCount = broadcastRepo.all().first().size
            val statusCount = broadcastStatusRepo.all().first().size

            Clogger.i(
                "DevClass",
                "📊 After init → teams=$teamCount, users=$userCount, " +
                        "events=$eventCount, attendance=$attendanceCount, " +
                        "broadcasts=$broadcastCount, statuses=$statusCount"
            )
        }.onFailure {
            Clogger.i("DevClass", "⚠️ Failed read-back check")
        }
    }

    private suspend fun syncToFirestore() {
        runCatching {
            teamRepo.sync()
        }.onFailure {
            Clogger.e("DevClass", "❌ Sync failed", it)
        }
    }

    private fun isDebuggable(): Boolean {
        return app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    private suspend fun clearAll() {
        val beforeTeams = teamRepo.all().first().size
        Clogger.i("DevClass", "Room check BEFORE deleteAll: $beforeTeams teams")
        teamRepo.deleteAll()
        Clogger.i("DevClass", "🧹 Local DB (teams) cleared successfully")
    }

    private suspend fun seedTeam() {
        code = (1000..9999).random().toString()
        val existingTeam = teamRepo.getByCode(code)
        if (existingTeam != null) {
            Clogger.i("DevClass", "🚫 Team with code=$code already exists: ID=${existingTeam.id}")
            teamRepo.setActiveTeam(existingTeam)
            teamId = existingTeam.id
            return
        }

        teamId = UUID.randomUUID().toString()
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
            isActive = true
        )
        teamRepo.createTeam(team)
        Clogger.i("DevClass", "✅ Team seeded: $teamId, Code=$code")
    }

    private suspend fun seedUser() {
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
        Clogger.i("DevClass", "✅ User seeded: $userId")
    }

    private suspend fun seedEvent() {
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
            category = EventCategory.PRACTICE,
            style = EventStyle.FRIENDLY,
            startAt = now,
            endAt = now.plusHours(2),
            location = "Training Grounds"
        )
        eventRepo.upsert(event)
        Clogger.i("DevClass", "✅ Event seeded: $eventId")
    }

    private suspend fun seedLineup() {
        val lineupId = UUID.randomUUID().toString()
        val lineup = Lineup(
            id = lineupId,
            eventId = eventId,
            createdBy = userId,
            formation = "4-3-3",
            spots = emptyList()
        )
        lineupRepo.upsert(lineup)
        Clogger.i("DevClass", "✅ Lineup seeded for event: $eventId")
    }

    private suspend fun seedLineupWithSpots() {
        val lineupId = UUID.randomUUID().toString()
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
        val lineup = Lineup(
            id = lineupId,
            eventId = eventId,
            createdBy = userId,
            formation = "4-3-3",
            spots = spots
        )
        lineupRepo.upsert(lineup)
        Clogger.i("DevClass", "✅ Lineup with spots seeded for event: $eventId")
    }

    private suspend fun seedAttendance() {
        val attendance = Attendance(
            eventId = eventId,
            playerId = userId,
            status = 0,
            recordedBy = userId,
            recordedAt = Instant.now()
        )
        attendanceRepo.upsert(attendance)
        Clogger.i("DevClass", "✅ Attendance seeded: $eventId -> $userId")
    }

    private suspend fun seedBroadcast() {
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
        Clogger.i("DevClass", "✅ Broadcast seeded: $broadcastId")
    }

    private suspend fun seedBroadcastStatus() {
        val status = BroadcastStatus(
            broadcastId = broadcastId,
            recipientId = userId
        )
        broadcastStatusRepo.upsert(status)
        Clogger.i("DevClass", "✅ BroadcastStatus seeded: $broadcastId -> $userId")
    }
}
