package com.ggetters.app.data.di

import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.remote.firestore.UserFirestore
import com.ggetters.app.data.repository.user.CombinedUserRepository
import com.ggetters.app.data.repository.user.OfflineUserRepository
import com.ggetters.app.data.repository.user.OnlineUserRepository
import com.ggetters.app.data.repository.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserRepositoryModule {

    @Provides @Singleton
    fun provideOfflineUserRepo(dao: UserDao): OfflineUserRepository =
        OfflineUserRepository(dao)

    @Provides @Singleton
    fun provideOnlineUserRepo(fs: UserFirestore): OnlineUserRepository =
        OnlineUserRepository(fs)

    @Provides @Singleton
    fun provideUserRepository(
        offline: OfflineUserRepository,
        online: OnlineUserRepository
    ): UserRepository =
        CombinedUserRepository(offline, online)
}
