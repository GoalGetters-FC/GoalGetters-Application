package com.ggetters.app.data.repository.notification

import com.ggetters.app.data.local.dao.NotificationDao
import com.ggetters.app.data.remote.firestore.NotificationFirestore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        combinedNotificationRepository: CombinedNotificationRepository
    ): NotificationRepository
}
