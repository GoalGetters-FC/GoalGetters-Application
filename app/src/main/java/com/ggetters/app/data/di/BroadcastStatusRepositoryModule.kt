package com.ggetters.app.data.di

import com.ggetters.app.data.local.dao.BroadcastStatusDao
import com.ggetters.app.data.remote.firestore.BroadcastStatusFirestore
import com.ggetters.app.data.repository.broadcaststatus.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BroadcastStatusRepoModule {

    @Provides @Singleton
    fun provideOfflineStatusRepo(dao: BroadcastStatusDao): OfflineBroadcastStatusRepository =
        OfflineBroadcastStatusRepository(dao)

    @Provides @Singleton
    fun provideOnlineStatusRepo(fs: BroadcastStatusFirestore): OnlineBroadcastStatusRepository =
        OnlineBroadcastStatusRepository(fs)

    @Provides @Singleton
    fun provideStatusRepo(
        offline: OfflineBroadcastStatusRepository,
        online: OnlineBroadcastStatusRepository
    ): BroadcastStatusRepository =
        CombinedBroadcastStatusRepository(offline, online)
}
