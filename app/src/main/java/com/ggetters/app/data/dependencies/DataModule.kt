package com.ggetters.app.data.dependencies

import android.content.Context
import androidx.room.Room
import com.ggetters.app.data.local.AppDatabase
import com.ggetters.app.data.daos.TeamDao
import com.ggetters.app.data.daos.UserDao
import com.ggetters.app.data.repositories.`Online\TeamRepository`
import com.ggetters.app.data.repositories.OnlineUserRepository
import com.ggetters.app.data.repository.OfflineTeamRepository
import com.ggetters.app.data.repositories.OfflineUserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

    @Provides @Singleton
    fun provideUserFirestore(firestore: FirebaseFirestore): OnlineUserRepository =
        OnlineUserRepository(firestore)

    @Provides @Singleton
    fun provideTeamFirestore(firestore: FirebaseFirestore): `Online\TeamRepository` =
        `Online\TeamRepository`(firestore)

    @Provides @Singleton
    fun provideUserRepository(
        dao: UserDao,
        remote: OnlineUserRepository
    ): OfflineUserRepository = OfflineUserRepository(dao, remote)

    @Provides @Singleton
    fun provideTeamRepository(
        dao: TeamDao,
        remote: `Online\TeamRepository`
    ): OfflineTeamRepository = OfflineTeamRepository(dao, remote)
}
