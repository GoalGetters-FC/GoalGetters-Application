package com.ggetters.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ggetters.app.BuildConfig
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.EnumConverter
import com.ggetters.app.data.local.converters.LineupSpotConverter
import com.ggetters.app.data.local.converters.UuidConverter
import com.ggetters.app.data.local.dao.AttendanceDao
import com.ggetters.app.data.local.dao.BroadcastDao
import com.ggetters.app.data.local.dao.BroadcastStatusDao
import com.ggetters.app.data.local.dao.EventDao
import com.ggetters.app.data.local.dao.LineupDao
import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.model.Attendance
import com.ggetters.app.data.model.Broadcast
import com.ggetters.app.data.model.BroadcastStatus
import com.ggetters.app.data.model.Event
import com.ggetters.app.data.model.Lineup
import com.ggetters.app.data.model.PerformanceLog
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.User
import com.ggetters.app.data.local.migrations.MIGRATION_1_2
import com.ggetters.app.data.local.migrations.MIGRATION_2_3
import com.ggetters.app.data.local.migrations.MIGRATION_3_4
import com.ggetters.app.data.local.migrations.MIGRATION_4_5
import com.ggetters.app.data.local.migrations.MIGRATION_5_6
import com.ggetters.app.data.local.migrations.MIGRATION_6_7

/**
 * Fresh baseline schema (v1). No migrations registered.
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
    version = 7,
    exportSchema = true
)
@TypeConverters(
    UuidConverter::class,
    DateConverter::class,
    EnumConverter::class,
    LineupSpotConverter::class,
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
    abstract fun broadcastDao(): BroadcastDao
    abstract fun broadcastStatusDao(): BroadcastStatusDao
    abstract fun eventDao(): EventDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun lineupDao(): LineupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // New name so we don't clash with old files
        private const val DATABASE_NAME = "ggetters_v1.db"

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5,
                            MIGRATION_5_6, MIGRATION_6_7
                        )
                        .apply {
                            if (BuildConfig.DEBUG) {
                                // Dev safety nets; keep if you like
                                fallbackToDestructiveMigration(true)
                                fallbackToDestructiveMigrationOnDowngrade(true)
                            }
                        }
                        .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
