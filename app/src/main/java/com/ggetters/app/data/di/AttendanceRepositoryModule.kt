// app/src/main/java/com/ggetters/app/data/di/AttendanceRepositoryModule.kt
package com.ggetters.app.data.di

import com.ggetters.app.data.local.dao.AttendanceDao
import com.ggetters.app.data.remote.firestore.AttendanceFirestore
import com.ggetters.app.data.repository.attendance.AttendanceRepository
import com.ggetters.app.data.repository.attendance.CombinedAttendanceRepository
import com.ggetters.app.data.repository.attendance.OfflineAttendanceRepository
import com.ggetters.app.data.repository.attendance.OnlineAttendanceRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.team.TeamRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AttendanceRepositoryModule {

    @Provides
    @Singleton
    fun provideOfflineAttendanceRepo(
        dao: AttendanceDao
    ): OfflineAttendanceRepository = OfflineAttendanceRepository(dao)

    @Provides
    @Singleton
    fun provideOnlineAttendanceRepo(
        fs: AttendanceFirestore,
        teamRepo: TeamRepository,
        eventRepo: EventRepository
    ): OnlineAttendanceRepository = OnlineAttendanceRepository(
        firestore = fs,
        teamRepo = teamRepo,
        eventRepo = eventRepo
    )

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        offline: OfflineAttendanceRepository,
        online: OnlineAttendanceRepository,
        eventRepo: EventRepository
    ): AttendanceRepository = CombinedAttendanceRepository(
        offline = offline,
        online = online,
        eventRepo = eventRepo
    )
}
