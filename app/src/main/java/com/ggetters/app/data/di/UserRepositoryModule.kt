package com.ggetters.app.data.di

import com.ggetters.app.data.repository.user.CombinedUserRepository
import com.ggetters.app.data.repository.user.OnlineUserRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.data.repository.user.OfflineUserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * DataModule provides all app-level singletons for data access using Dagger Hilt.
 *
 * - Supplies a singleton Firestore instance.
 * - Builds the Room database and provides DAOs.
 * - Supplies Firestore data sources for users and teams.
 * - Provides repositories that coordinate between local and remote sources.
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class UserRepositoryModule {

    @Binds @Singleton @Named("offlineUser")
    abstract fun bindOfflineUser(
        impl: OfflineUserRepository
    ): UserRepository

    @Binds @Singleton @Named("onlineUser")
    abstract fun bindOnlineUser(
        impl: OnlineUserRepository
    ): UserRepository

    @Module
    @InstallIn(SingletonComponent::class)
    companion object {
        @Provides @Singleton
        fun provideUserRepository(
            @Named("offlineUser") offline: UserRepository,
            @Named("onlineUser")  online: UserRepository
        ): UserRepository = CombinedUserRepository(offline, online)
    }
}
