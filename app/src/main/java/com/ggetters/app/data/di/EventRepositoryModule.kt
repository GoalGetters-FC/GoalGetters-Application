package com.ggetters.app.data.di

import com.ggetters.app.data.local.dao.EventDao
import com.ggetters.app.data.remote.firestore.EventFirestore
import com.ggetters.app.data.repository.event.CombinedEventRepository
import com.ggetters.app.data.repository.event.EventRepository
import com.ggetters.app.data.repository.event.OfflineEventRepository
import com.ggetters.app.data.repository.event.OnlineEventRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing Event repository dependencies.
 * 
 * TODO: Backend - Add proper error handling configuration
 * TODO: Backend - Add repository configuration options
 * TODO: Backend - Implement repository performance monitoring
 */
@Module
@InstallIn(SingletonComponent::class)
object EventRepositoryModule {

    // TODO: Backend - Add configuration for sync intervals
    // TODO: Backend - Add repository health checks
    // TODO: Backend - Implement repository metrics collection

    @Provides 
    @Singleton
    fun provideOfflineEventRepo(dao: EventDao): OfflineEventRepository =
        OfflineEventRepository(dao)

    @Provides 
    @Singleton
    fun provideOnlineEventRepo(fs: EventFirestore): OnlineEventRepository =
        OnlineEventRepository(fs)

    @Provides 
    @Singleton
    fun provideEventRepository(
        offline: OfflineEventRepository,
        online: OnlineEventRepository
    ): EventRepository =
        CombinedEventRepository(offline, online)

    // TODO: Backend - Add named qualifiers for different event repository strategies
    // TODO: Backend - Add configuration for retry policies
    // TODO: Backend - Implement repository caching strategies
    // TODO: Backend - Add repository monitoring and logging
} 