package com.ggetters.app.data.di

import com.ggetters.app.data.local.dao.BroadcastDao
import com.ggetters.app.data.remote.firestore.BroadcastFirestore
import com.ggetters.app.data.repository.broadcast.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BroadcastRepositoryModule {

    @Provides @Singleton
    fun provideOfflineBroadcastRepo(dao: BroadcastDao) =
        OfflineBroadcastRepository(dao)

    @Provides @Singleton
    fun provideOnlineBroadcastRepo(fs: BroadcastFirestore) =
        OnlineBroadcastRepository(fs)

    @Provides @Singleton
    fun provideBroadcastRepo(
        offline: OfflineBroadcastRepository,
        online: OnlineBroadcastRepository
    ) = CombinedBroadcastRepository(offline, online)
}
