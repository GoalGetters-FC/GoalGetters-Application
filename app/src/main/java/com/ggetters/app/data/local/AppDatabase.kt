package com.ggetters.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.UuidConverter
import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.User

/**
 * Room database for the GoalGetters app.
 *
 * Contains the local cache tables for [User] and [Team], and provides
 * Data Access Objects (DAOs) for each entity.
 *
 * @property userDao Provides CRUD operations for [User] entities.
 * @property teamDao Provides CRUD operations for [Team] entities.
 */
@Database(
    entities = [User::class, Team::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(
    UuidConverter::class,  // Convert UUID to String and back
    DateConverter::class   // Convert Instant (or Date) to Long and back
)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Get the DAO for [User] operations.
     *
     * @return a [UserDao] instance for inserting, querying, and deleting users.
     */
    abstract fun userDao(): UserDao

    /**
     * Get the DAO for [Team] operations.
     *
     * @return a [TeamDao] instance for inserting, querying, and deleting teams.
     */
    abstract fun teamDao(): TeamDao
}
