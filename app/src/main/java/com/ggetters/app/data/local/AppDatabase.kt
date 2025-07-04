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

@Database(
    entities = [User::class, Team::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(UuidConverter::class, DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun teamDao(): TeamDao
}
