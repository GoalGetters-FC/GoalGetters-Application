package com.ggetters.app.data.di

import com.ggetters.app.data.repository.team.CombinedTeamRepository
import com.ggetters.app.data.repository.team.OfflineTeamRepository
import com.ggetters.app.data.repository.team.OnlineTeamRepository
import com.ggetters.app.data.repository.team.TeamRepository
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
abstract class TeamRepositoryModule {

    @Binds @Singleton @Named("offlineTeam")
    abstract fun bindOfflineTeam(
        impl: OfflineTeamRepository
    ): TeamRepository

    @Binds @Singleton @Named("onlineTeam")
    abstract fun bindOnlineTeam(
        impl: OnlineTeamRepository
    ): TeamRepository

    @Module
    @InstallIn(SingletonComponent::class)
    companion object {
        @Provides @Singleton
        fun provideTeamRepository(
            @Named("offlineTeam") offline: TeamRepository,
            @Named("onlineTeam")  online: TeamRepository
        ): TeamRepository = CombinedTeamRepository(offline, online)
    }
}
