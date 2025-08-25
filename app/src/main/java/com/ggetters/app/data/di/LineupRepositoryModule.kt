package com.ggetters.app.data.di

import com.ggetters.app.data.local.dao.LineupDao
import com.ggetters.app.data.remote.firestore.LineupFirestore
import com.ggetters.app.data.repository.lineup.CombinedLineupRepository
import com.ggetters.app.data.repository.lineup.LineupRepository
import com.ggetters.app.data.repository.lineup.OfflineLineupRepository
import com.ggetters.app.data.repository.lineup.OnlineLineupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LineupRepositoryModule {

    @Provides
    @Singleton
    fun provideOfflineLineupRepo(dao: LineupDao): OfflineLineupRepository =
        OfflineLineupRepository(dao)

    @Provides @Singleton
    fun provideOnlineLineupRepo(fs: LineupFirestore): OnlineLineupRepository =
        OnlineLineupRepository(firestore = fs)

    @Provides @Singleton
    fun provideLineupRepository(
        offline: OfflineLineupRepository,
        online: OnlineLineupRepository
    ): LineupRepository = CombinedLineupRepository(offline, online)
}
