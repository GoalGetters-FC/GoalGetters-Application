package com.ggetters.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 8 to 9.
 * Adds match_events table for tracking match events like goals, cards, substitutions.
 */
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create match_events table
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS match_events (
                id TEXT NOT NULL PRIMARY KEY,
                matchId TEXT NOT NULL,
                eventType TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                minute INTEGER NOT NULL,
                playerId TEXT,
                playerName TEXT,
                teamId TEXT,
                teamName TEXT,
                details TEXT,
                createdBy TEXT NOT NULL,
                isConfirmed INTEGER NOT NULL DEFAULT 1
            )
        """)
        
        // Create indices for performance
        database.execSQL("CREATE INDEX IF NOT EXISTS index_match_events_matchId ON match_events (matchId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_match_events_eventType ON match_events (eventType)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_match_events_playerId ON match_events (playerId)")
        database.execSQL("CREATE INDEX IF NOT EXISTS index_match_events_minute ON match_events (minute)")
    }
}
