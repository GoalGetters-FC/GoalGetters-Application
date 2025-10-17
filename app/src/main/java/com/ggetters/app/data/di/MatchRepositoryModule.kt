package com.ggetters.app.data.di

import com.ggetters.app.data.repository.match.CombinedMatchDetailsRepository
import com.ggetters.app.data.repository.match.MatchDetailsRepository
import com.ggetters.app.data.repository.match.MatchEventRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binds the match facade to DI so VMs only depend on MatchDetailsRepository.
 *
 * @see <a href="https://dagger.dev/hilt/">Hilt</a>
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class MatchRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindMatchDetailsRepository(
        impl: CombinedMatchDetailsRepository
    ): MatchDetailsRepository

    @Binds
    @Singleton
    abstract fun bindMatchEventRepository(
        impl: com.ggetters.app.data.repository.match.CombinedMatchEventRepository
    ): MatchEventRepository
}
