// app/src/main/java/com/ggetters/app/data/di/DataModule.kt
package com.ggetters.app.data.di

import android.content.Context
import androidx.room.Room
import com.ggetters.app.data.local.AppDatabase
import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.local.dao.UserDao
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing core data dependencies.
 *
 * - Firestore instance for remote data operations.
 * - Room database and DAOs for local caching.
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    /**
     * Provides a singleton [FirebaseFirestore] instance.
     *
     * @return the Firestore client for remote CRUD operations.
     */
    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    /**
     * Provides the Room [AppDatabase], configured with entities and type converters.
     *
     * @param ctx application context for database builder.
     * @return the Room database instance.
     */
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "goalgetters.db")
            .fallbackToDestructiveMigration()
            .build()

    /**
     * Provides the [UserDao] for user-related local operations.
     *
     * @param db the AppDatabase instance.
     * @return the DAO for User entity.
     */
    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    /**
     * Provides the [TeamDao] for team-related local operations.
     *
     * @param db the AppDatabase instance.
     * @return the DAO for Team entity.
     */
    @Provides
    @Singleton
    fun provideTeamDao(db: AppDatabase): TeamDao = db.teamDao()
}
