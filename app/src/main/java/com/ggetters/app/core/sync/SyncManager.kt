package com.ggetters.app.core.sync

import com.ggetters.app.core.utils.Clogger
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.broadcast.BroadcastRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository,
    private val eventRepo: EventRepository,
    private val lineupRepo: LineupRepository,
    private val attendanceRepo: AttendanceRepository,
    private val broadcastRepo: BroadcastRepository
) {

    /** Sync all repositories concurrently. */
    suspend fun syncAll() = coroutineScope {
        try {
            Clogger.i("Sync", "Starting full sync")

            val results = listOf(
                async { teamRepo.sync() },
                async { userRepo.sync() },
                async { eventRepo.sync() },
                async { lineupRepo.sync() },
                async { attendanceRepo.sync() },
                async { broadcastRepo.sync() }
            )

            results.forEach { it.await() }
            Clogger.i("Sync", "Full sync complete")

        } catch (e: Exception) {
            Clogger.e("Sync", "Full sync failed", e)
            throw e
        }
    }
}
