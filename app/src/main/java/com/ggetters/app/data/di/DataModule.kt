package com.ggetters.app.data.di

import android.content.Context
import androidx.room.Room
import com.ggetters.app.data.local.AppDatabase
import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.repository.team.CombinedTeamRepository
import com.ggetters.app.data.repository.team.OfflineTeamRepository
import com.ggetters.app.data.repository.team.OnlineTeamRepository
import com.ggetters.app.data.repository.team.TeamRepository
import com.ggetters.app.data.repository.user.CombinedUserRepository
import com.ggetters.app.data.repository.user.OnlineUserRepository
import com.ggetters.app.data.repository.user.UserRepository
import com.ggetters.app.data.repository.user.OfflineUserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
object DataModule {

    @Provides @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "goalgetters.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides @Singleton
    fun provideTeamDao(db: AppDatabase): TeamDao = db.teamDao()
}

