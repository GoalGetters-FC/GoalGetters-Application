package com.ggetters.app.data.di

import com.ggetters.app.data.local.dao.EventDao
import com.ggetters.app.data.remote.firestore.EventFirestore
import com.ggetters.app.data.repository.event.CombinedEventRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.event.OfflineEventRepository
import com.ggetters.app.data.repository.event.OnlineEventRepository
import com.ggetters.app.data.repository.team.TeamRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EventRepositoryModule {

    @Provides
    @Singleton
    fun provideOfflineEventRepo(dao: EventDao): OfflineEventRepository =
        OfflineEventRepository(dao)

    @Provides
    @Singleton
    fun provideOnlineEventRepo(
        fs: EventFirestore,
        teamRepo: TeamRepository
    ): OnlineEventRepository =
        OnlineEventRepository(fs, teamRepo)

    @Provides
    @Singleton
    fun provideEventRepository(
        offline: OfflineEventRepository,
        online: OnlineEventRepository,
        teamRepo: TeamRepository
    ): EventRepository =
        CombinedEventRepository(offline, online, teamRepo)
}
