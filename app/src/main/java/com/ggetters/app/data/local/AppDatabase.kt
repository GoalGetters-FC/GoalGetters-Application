package com.ggetters.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.local.entities.UserEntity
import com.ggetters.app.data.local.entities.TeamEntity
import java.util.Date

object DateTypeConverter {
    @TypeConverter fun fromDate(d: Date?): Long? = d?.time
    @TypeConverter
    fun toDate(t: Long?): Date? = t?.let { Date(it) }
}

/**
 * Room database setup.
 * TODO: Define entities (e.g., User, Profile, etc.).
 * TODO: Create DAOs for data access.
 * TODO: Configure Room database builder and migrations.
 * TODO: Add dependency injection if needed.
 */

@Database(
    entities = [UserEntity::class, TeamEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(DateTypeConverter::class) // if needed for Date <-> Long
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
}