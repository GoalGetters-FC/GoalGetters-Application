package com.ggetters.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migration from version 7 to 8
 * 
 * Changes:
 * - Added Notification entity and table
 */
val MIGRATION_7_8 = Migration(7, 8) { database ->
    // Create the notification table with all required fields
    database.execSQL("""
        CREATE TABLE notification (
            id TEXT NOT NULL PRIMARY KEY,
            created_at INTEGER NOT NULL,
            updated_at INTEGER NOT NULL,
            stained_at INTEGER,
            user_id TEXT NOT NULL,
            team_id TEXT,
            title TEXT NOT NULL,
            subtitle TEXT NOT NULL,
            message TEXT NOT NULL,
            is_seen INTEGER NOT NULL,
            is_pinned INTEGER NOT NULL,
            type TEXT NOT NULL,
            sender TEXT NOT NULL,
            linked_event_type TEXT,
            linked_event_id TEXT,
            data TEXT NOT NULL,
            priority TEXT NOT NULL,
            expires_at INTEGER,
            action_url TEXT,
            FOREIGN KEY(user_id) REFERENCES user(id) ON DELETE CASCADE ON UPDATE CASCADE,
            FOREIGN KEY(team_id) REFERENCES team(id) ON DELETE CASCADE ON UPDATE CASCADE
        )
    """)
    
    // Create all required indices for the notification table
    database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_user_id ON notification (user_id)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_team_id ON notification (team_id)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_type ON notification (type)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_is_seen ON notification (is_seen)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_is_pinned ON notification (is_pinned)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_created_at ON notification (created_at)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_user_id_is_seen ON notification (user_id, is_seen)")
    database.execSQL("CREATE INDEX IF NOT EXISTS index_notification_team_id_type ON notification (team_id, type)")
}
