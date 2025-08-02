// ✅ AttendanceRepositoryModule.kt
package com.ggetters.app.data.di

import com.ggetters.app.data.local.dao.AttendanceDao
import com.ggetters.app.data.remote.firestore.AttendanceFirestore
import com.ggetters.app.data.repository.attendance.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AttendanceRepositoryModule {

    @Provides @Singleton
    fun provideOfflineAttendanceRepo(dao: AttendanceDao): OfflineAttendanceRepository =
        OfflineAttendanceRepository(dao)

    @Provides @Singleton
    fun provideOnlineAttendanceRepo(fs: AttendanceFirestore): OnlineAttendanceRepository =
        OnlineAttendanceRepository(fs)

    @Provides @Singleton
    fun provideAttendanceRepository(
        offline: OfflineAttendanceRepository,
        online: OnlineAttendanceRepository
    ): AttendanceRepository =
        CombinedAttendanceRepository(offline, online)
}
