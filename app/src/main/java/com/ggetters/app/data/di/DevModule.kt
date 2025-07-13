// app/src/main/java/com/ggetters/app/di/DevModule.kt
package com.ggetters.app.data.di

import com.ggetters.app.core.utils.DevClass
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DevModule {
    @Provides
    fun provideDevClass(
        teamRepo: TeamRepository,
        userRepo: UserRepository
    ): DevClass = DevClass( teamRepo, userRepo )
}
