// app/src/main/java/com/ggetters/app/data/di/UserRepositoryModule.kt
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
 * Hilt module that binds UserRepository implementations.
 *
 * - Binds a Room-backed OfflineUserRepository under the "offlineUser" qualifier.
 * - Binds a Firestore-backed OnlineUserRepository under the "onlineUser" qualifier.
 * - Provides a CombinedUserRepository that orchestrates reads from offline and writes/syncs with online.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class UserRepositoryModule {

    /**
     * Bind the offline (Room) implementation for UserRepository.
     *
     * @param impl the Room-based OfflineUserRepository
     * @return the UserRepository qualified as "offlineUser"
     */
    @Binds
    @Singleton
    @Named("offlineUser")
    abstract fun bindOfflineUser(
        impl: OfflineUserRepository
    ): UserRepository

    /**
     * Bind the online (Firestore) implementation for UserRepository.
     *
     * @param impl the Firestore-based OnlineUserRepository
     * @return the UserRepository qualified as "onlineUser"
     */
    @Binds
    @Singleton
    @Named("onlineUser")
    abstract fun bindOnlineUser(
        impl: OnlineUserRepository
    ): UserRepository

    companion object {
        /**
         * Provides the CombinedUserRepository that reads from offline cache,
         * writes to both offline and online, and handles sync.
         *
         * @param offline the UserRepository qualified as "offlineUser"
         * @param online  the UserRepository qualified as "onlineUser"
         * @return the CombinedUserRepository as the primary UserRepository
         */
        @Provides
        @Singleton
        fun provideUserRepository(
            @Named("offlineUser") offline: UserRepository,
            @Named("onlineUser")  online: UserRepository
        ): UserRepository = CombinedUserRepository(offline, online)
    }
}
