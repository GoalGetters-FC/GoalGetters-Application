package com.ggetters.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.UuidConverter
import com.ggetters.app.data.local.dao.TeamDao
import com.ggetters.app.data.local.dao.UserDao
import com.ggetters.app.data.model.Team
import com.ggetters.app.data.model.User

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
    ], 
    
    // Configuration
    
    version = 1, 
    exportSchema = true // TODO: Add location to silence build warnings
)
@TypeConverters(
    UuidConverter::class,
    DateConverter::class,
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    // DAO Accessors
    
    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
}
