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
import com.ggetters.app.data.local.migrations.MIGRATION_2_3
import com.ggetters.app.data.local.migrations.MIGRATION_3_4

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
    
    version = 4,
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
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
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
    abstract fun eventDao(): EventDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun lineupDao(): LineupDao

}
