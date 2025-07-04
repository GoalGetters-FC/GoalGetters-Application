package com.ggetters.app.data.contexts

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ggetters.app.data.converters.DateConverter
import com.ggetters.app.data.converters.UuidConverter
import com.ggetters.app.data.daos.TeamDao
import com.ggetters.app.data.daos.UserDao
import com.ggetters.app.data.models.Team
import com.ggetters.app.data.models.User

@Database(
    entities = [User::class, Team::class], version = 1, exportSchema = true
)
@TypeConverters(
    UuidConverter::class, DateConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        private var INSTANCE: AppDatabase? = null
        private const val DATABASE_NAME = "ggetters.db"

        /**
         * Returns a singleton instance of the [AppDatabase].
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