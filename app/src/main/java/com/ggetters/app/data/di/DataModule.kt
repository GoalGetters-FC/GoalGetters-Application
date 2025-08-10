// app/src/main/java/com/ggetters/app/data/di/DataModule.kt
package com.ggetters.app.data.di

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.room.RoomDatabase
import com.ggetters.app.core.services.AuthService
import com.ggetters.app.core.services.GoogleAuthClient
import com.ggetters.app.data.local.AppDatabase
import com.ggetters.app.data.local.dao.AttendanceDao
import com.ggetters.app.data.local.dao.BroadcastDao
import com.ggetters.app.data.local.dao.BroadcastStatusDao
import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.local.dao.EventDao
import com.ggetters.app.data.local.dao.LineupDao
import com.google.firebase.auth.FirebaseAuth
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

    // --- Contexts


    /**
     * Injects a singleton [FirebaseAuth] instance.
     *
     * **Note:** This dependency injection is fulfilled using the *Dagger Hilt*
     * Dependency Injection (DI) library. See the [documentation](https://developer.android.com/training/dependency-injection/hilt-android)
     * to learn more about usage details and implementations.
     *
     * **Usage:**
     *
     * ```
     * // Inject into constructors (preferred)
     * class Service @Inject constructor(
     *     private val auth: FirebaseAuth
     * ) { ... }
     * ```
     *
     * ```
     * // Inject into an object/class property
     * @Inject lateinit var auth: FirebaseAuth
     * ```
     *
     * @return [Singleton] instance of [FirebaseAuth].
     */
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    
    @Provides
    @Singleton
    fun provideCredentialManager(
        @ApplicationContext ctx: Context
    ): CredentialManager = CredentialManager.create(ctx)

    
    @Provides
    @Singleton
    fun provideAuthService(firebaseAuth: FirebaseAuth): AuthService = AuthService(firebaseAuth)


    /**
     * Provides the Room [AppDatabase] using the singleton getDatabase() method.
     * This ensures only one instance of the database is created, and configs
     * stay in the [AppDatabase] class.
     *
     * @param context application context for database builder.
     * @return the Room database instance.
     */
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase = AppDatabase.getDatabase(context)


    // --- DAOs


    /**
     * Injects the [UserDao].
     *
     * @param source the [RoomDatabase] instance to access.
     * @return [UserDao]
     */
    @Provides
    @Singleton
    fun provideUserDao(source: AppDatabase): UserDao = source.userDao()


    /**
     * Injects the [TeamDao].
     *
     * @param source the [RoomDatabase] instance to access.
     * @return [TeamDao]
     */
    @Provides
    @Singleton
    fun provideTeamDao(source: AppDatabase): TeamDao = source.teamDao()

    @Provides
    fun provideBroadcastDao(db: AppDatabase): BroadcastDao =
        db.broadcastDao()

    @Provides
    fun provideBroadcastStatusDao(db: AppDatabase): BroadcastStatusDao =
        db.broadcastStatusDao()

    @Provides
    @Singleton
    fun provideEventDao(db: AppDatabase): EventDao = 
        db.eventDao()

    @Provides
    @Singleton
    fun provideAttendanceDao(db: AppDatabase): AttendanceDao = db.attendanceDao()

    @Provides
    @Singleton
    fun provideLineupDao(db: AppDatabase): LineupDao = db.lineupDao()


    // TODO: Backend - Add AttendanceDao provider
    // TODO: Backend - Add LineupDao provider
    // TODO: Backend - Add PerformanceLogDao provider
}
