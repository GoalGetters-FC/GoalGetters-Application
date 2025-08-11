package com.ggetters.app.core.usecases

import com.ggetters.app.data.local.AppDatabase
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.broadcast.BroadcastRepository
import com.ggetters.app.data.repository.broadcaststatus.BroadcastStatusRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ResetAndRehydrate @Inject constructor(
    private val db: AppDatabase,
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository,
    private val eventRepo: EventRepository,
    private val attendanceRepo: AttendanceRepository,
    private val broadcastRepo: BroadcastRepository,
    private val broadcastStatusRepo: BroadcastStatusRepository,
    private val lineupRepo: LineupRepository
) {
    suspend operator fun invoke() = withContext(Dispatchers.IO) {
        db.clearAllTables()           // <-- no runInTransaction wrapper
        teamRepo.sync()

        val teams = teamRepo.all().first()
        teams.forEach { t ->
            userRepo.hydrateForTeam(t.id)
            eventRepo.hydrateForTeam(t.id)
            attendanceRepo.hydrateForTeam(t.id)
            broadcastRepo.hydrateForTeam(t.id)
            broadcastStatusRepo.hydrateForTeam(t.id)
            lineupRepo.hydrateForTeam(t.id)
        }
    }
}
