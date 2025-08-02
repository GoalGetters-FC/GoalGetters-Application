package com.ggetters.app.data.di

import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.remote.firestore.TeamFirestore
import com.ggetters.app.data.repository.team.CombinedTeamRepository
import com.ggetters.app.data.repository.team.OfflineTeamRepository
import com.ggetters.app.data.repository.team.OnlineTeamRepository
import com.ggetters.app.data.repository.team.TeamRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TeamRepositoryModule {

    @Provides @Singleton
    fun provideOfflineTeamRepo(dao: TeamDao): OfflineTeamRepository =
        OfflineTeamRepository(dao)

    @Provides @Singleton
    fun provideOnlineTeamRepo(fs: TeamFirestore): OnlineTeamRepository =
        OnlineTeamRepository(fs)

    @Provides @Singleton
    fun provideTeamRepository(
        offline: OfflineTeamRepository,
        online: OnlineTeamRepository
    ): TeamRepository =
        CombinedTeamRepository(offline, online)
}
