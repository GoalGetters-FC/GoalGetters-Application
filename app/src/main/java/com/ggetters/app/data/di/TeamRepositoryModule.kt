// app/src/main/java/com/ggetters/app/data/di/TeamRepositoryModule.kt
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
 * Hilt module that binds TeamRepository implementations.
 *
 * - Binds a Room-backed OfflineTeamRepository under the "offlineTeam" qualifier.
 * - Binds a Firestore-backed OnlineTeamRepository under the "onlineTeam" qualifier.
 * - Provides a CombinedTeamRepository that orchestrates reads from offline and writes/syncs with online.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class TeamRepositoryModule {

    /** Bind the Room-backed implementation as a Singleton under @Named("offlineTeam") */
    @Binds
    @Singleton
    @Named("offlineTeam")
    abstract fun bindOfflineTeam(
        impl: OfflineTeamRepository
    ): TeamRepository

    /** Bind the Firestore-backed implementation as a Singleton under @Named("onlineTeam") */
    @Binds
    @Singleton
    @Named("onlineTeam")
    abstract fun bindOnlineTeam(
        impl: OnlineTeamRepository
    ): TeamRepository

    companion object {
        /** Provide the CombinedTeamRepository as the primary TeamRepository */
        @Provides
        @Singleton
        fun provideCombinedTeamRepository(
            @Named("offlineTeam") offline: TeamRepository,
            @Named("onlineTeam")  online: TeamRepository
        ): TeamRepository = CombinedTeamRepository(offline, online)
    }
}
