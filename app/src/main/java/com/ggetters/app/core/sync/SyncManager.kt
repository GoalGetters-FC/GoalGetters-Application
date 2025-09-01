package com.ggetters.app.core.sync

import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.broadcast.BroadcastRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import javax.inject.Inject

class SyncManager @Inject constructor(
    private val teamRepo: TeamRepository,
    private val userRepo: UserRepository,
    private val eventRepo: EventRepository,
    private val lineupRepo: LineupRepository,
    private val attendanceRepo: AttendanceRepository,
    private val broadcastRepo: BroadcastRepository
) {
    suspend fun syncAll() {
        teamRepo.sync()
        userRepo.sync()
        eventRepo.sync()
        lineupRepo.sync()
        attendanceRepo.sync()
        broadcastRepo.sync()
    }
}
