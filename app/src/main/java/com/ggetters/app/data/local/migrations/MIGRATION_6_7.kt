package com.ggetters.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 6 to 7
 * 
 * Changes:
 * - Allow team_id to be null in User table
 * - Update unique constraint from (auth_id, team_id) to just (auth_id)
 * - This allows users to sign up without being assigned to a team initially
 */
val MIGRATION_6_7 = Migration(6, 7) { database ->
    // Disable foreign key constraints temporarily
    database.execSQL("PRAGMA foreign_keys = OFF")
    
    // Create a new table with nullable team_id
    database.execSQL("""
        CREATE TABLE user_new (
            id TEXT NOT NULL PRIMARY KEY,
            created_at TEXT NOT NULL,
            updated_at TEXT NOT NULL,
            stained_at TEXT,
            auth_id TEXT NOT NULL,
            team_id TEXT,
            joined_at TEXT,
            role TEXT NOT NULL,
            name TEXT NOT NULL,
            surname TEXT NOT NULL,
            alias TEXT NOT NULL,
            date_of_birth TEXT,
            email TEXT,
            position TEXT,
            number INTEGER,
            status TEXT,
            health_weight REAL,
            health_height REAL,
            FOREIGN KEY(team_id) REFERENCES team(id) ON DELETE CASCADE ON UPDATE CASCADE
        )
    """)
    
    // Copy data from old table to new table, converting empty team_id to NULL
    database.execSQL("""
        INSERT INTO user_new 
        SELECT 
            id, created_at, updated_at, stained_at, auth_id,
            CASE WHEN team_id = '' THEN NULL ELSE team_id END as team_id,
            joined_at, role, name, surname, alias, date_of_birth,
            email, position, number, status, health_weight, health_height
        FROM user
    """)
    
    // Drop old table and rename new table
    database.execSQL("DROP TABLE user")
    database.execSQL("ALTER TABLE user_new RENAME TO user")
    
    // Create new indices
    database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_user_auth_id ON user (auth_id)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_user_team_id ON user (team_id)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_user_role ON user (role)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_user_status ON user (status)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_user_team_id_role ON user (team_id, role)")
    
    // Re-enable foreign key constraints
    database.execSQL("PRAGMA foreign_keys = ON")
}
