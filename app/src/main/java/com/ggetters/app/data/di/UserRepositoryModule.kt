// app/src/main/java/com/ggetters/app/data/di/UserRepositoryModule.kt
package com.ggetters.app.data.di

import com.ggetters.app.data.repository.user.CombinedUserRepository
import com.ggetters.app.data.repository.user.OfflineUserRepository
import com.ggetters.app.data.repository.user.OnlineUserRepository
import com.ggetters.app.data.repository.user.UserRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserRepositoryModule {

    // 1) Bind the Room-backed implementation as a Singleton
    @Binds
    @Singleton
    @Named("offlineUser")
    abstract fun bindOfflineUser(
        impl: OfflineUserRepository
    ): UserRepository

    // 2) Bind the Firestore-backed implementation as a Singleton
    @Binds
    @Singleton
    @Named("onlineUser")
    abstract fun bindOnlineUser(
        impl: OnlineUserRepository
    ): UserRepository

    companion object {
        // 3) Provide the CombinedUserRepository as the “primary” UserRepository
        @Provides
        @Singleton
        fun provideCombinedUserRepository(
            @Named("offlineUser") offline: UserRepository,
            @Named("onlineUser")  online: UserRepository
        ): UserRepository = CombinedUserRepository(offline, online)
    }
}
