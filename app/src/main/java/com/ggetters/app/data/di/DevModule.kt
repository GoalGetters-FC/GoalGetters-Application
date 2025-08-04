package com.ggetters.app.data.di

import android.app.Application
import com.ggetters.app.core.utils.DevClass
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.broadcast.BroadcastRepository
import com.ggetters.app.data.repository.broadcaststatus.BroadcastStatusRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DevModule {

    @Provides
    @Singleton
    fun provideDevClass(
        app: Application,
        teamRepo: TeamRepository,
        userRepo: UserRepository,
        attendanceRepo: AttendanceRepository,
        eventRepo: EventRepository,
        broadcastRepo: BroadcastRepository,
        broadcastStatusRepo: BroadcastStatusRepository,
        lineupRepo: LineupRepository
    ): DevClass = DevClass(
        app = app,
        teamRepo = teamRepo,
        userRepo = userRepo,
        attendanceRepo = attendanceRepo,
        eventRepo = eventRepo,
        broadcastRepo = broadcastRepo,
        broadcastStatusRepo = broadcastStatusRepo,
        lineupRepo = lineupRepo
    )
}
