package com.ggetters.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create player_statistics table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS `player_statistics` (
                `playerId` TEXT NOT NULL,
                `scheduled` INTEGER NOT NULL DEFAULT 0,
                `attended` INTEGER NOT NULL DEFAULT 0,
                `missed` INTEGER NOT NULL DEFAULT 0,
                `goals` INTEGER NOT NULL DEFAULT 0,
                `assists` INTEGER NOT NULL DEFAULT 0,
                `matches` INTEGER NOT NULL DEFAULT 0,
                `yellowCards` INTEGER NOT NULL DEFAULT 0,
                `redCards` INTEGER NOT NULL DEFAULT 0,
                `cleanSheets` INTEGER NOT NULL DEFAULT 0,
                `weight` REAL NOT NULL DEFAULT 0.0,
                `minutesPlayed` INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY(`playerId`)
            )
        """)
    }
}
