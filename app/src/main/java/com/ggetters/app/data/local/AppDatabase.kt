package com.ggetters.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.EnumConverter
import com.ggetters.app.data.local.converters.LineupSpotConverter
import com.ggetters.app.data.local.converters.UuidConverter
import com.ggetters.app.data.local.dao.AttendanceDao
import com.ggetters.app.data.local.dao.BroadcastDao
import com.ggetters.app.data.local.dao.BroadcastStatusDao
import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.local.dao.EventDao
import com.ggetters.app.data.local.dao.LineupDao
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.User
import com.ggetters.app.data.model.Broadcast
import com.ggetters.app.data.model.BroadcastStatus
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.model.Lineup
import com.ggetters.app.data.model.PerformanceLog

/**
 * Local [RoomDatabase] for the application.
 *
 * @property userDao
 * @property teamDao
 */
@Database(
    entities = [
        User::class,
        Team::class,
        Broadcast::class,
        BroadcastStatus::class,
        Event::class,
        Attendance::class,
        Lineup::class,
        PerformanceLog::class
    ],
    
    // Configuration
    
    version = 2,  // TODO: Backend - Implement migration from v1 to v2
    exportSchema = true // TODO: Add location to silence build warnings
)
@TypeConverters(
    UuidConverter::class,
    DateConverter::class,
    EnumConverter::class,
    LineupSpotConverter::class,
)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "ggetters.db"

        /**
         * Singleton instance of the [AppDatabase].
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, AppDatabase::class.java, DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(true) // Dev Class temp test
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    // DAO Accessors
    
    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
    abstract fun broadcastDao(): BroadcastDao
    abstract fun broadcastStatusDao(): BroadcastStatusDao
    
    // TODO: Backend - Add Event-related DAOs
    abstract fun eventDao(): EventDao

    abstract fun attendanceDao(): AttendanceDao
    abstract fun lineupDao(): LineupDao

    // TODO: Backend - Create and add AttendanceDao
    // TODO: Backend - Create and add LineupDao  
    // TODO: Backend - Create and add PerformanceLogDao
}
