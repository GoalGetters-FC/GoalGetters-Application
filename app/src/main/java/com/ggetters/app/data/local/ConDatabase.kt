package com.ggetters.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ggetters.app.data.local.converters.DateConverter
import com.ggetters.app.data.local.converters.UuidConverter

/**
 * Local [RoomDatabase] for the application configuration.
 */
@Database(
    version = 1,
    exportSchema = true // TODO: Add location to silence build warnings
)
@TypeConverters(
    UuidConverter::class,
    DateConverter::class,
)
abstract class ConDatabase : RoomDatabase() {
    
    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "ggetters-config.db"

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
}