package com.ggetters.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Make userId nullable in notification table
        // SQLite doesn't support ALTER COLUMN, so we need to recreate the table
        
        // Check if table exists
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='notification'")
        val tableExists = cursor.moveToFirst()
        cursor.close()
        
        if (!tableExists) {
            // Table doesn't exist, just create it fresh
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notification (
                    id TEXT NOT NULL PRIMARY KEY,
                    created_at INTEGER NOT NULL,
                    updated_at INTEGER NOT NULL,
                    stained_at INTEGER,
                    title TEXT NOT NULL,
                    subtitle TEXT NOT NULL,
                    message TEXT NOT NULL,
                    type TEXT NOT NULL,
                    sender TEXT NOT NULL DEFAULT '',
                    user_id TEXT,
                    team_id TEXT,
                    is_seen INTEGER NOT NULL DEFAULT 0,
                    is_pinned INTEGER NOT NULL DEFAULT 0,
                    linked_event_id TEXT,
                    linked_event_type TEXT,
                    data TEXT,
                    priority TEXT NOT NULL DEFAULT 'NORMAL',
                    expires_at INTEGER,
                    action_url TEXT,
                    FOREIGN KEY(user_id) REFERENCES user(id) ON UPDATE CASCADE ON DELETE CASCADE,
                    FOREIGN KEY(team_id) REFERENCES team(id) ON UPDATE CASCADE ON DELETE CASCADE
                )
                """.trimIndent()
            )
            
            // Create indices
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_user_id ON notification(user_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_team_id ON notification(team_id)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_type ON notification(type)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_is_seen ON notification(is_seen)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_is_pinned ON notification(is_pinned)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_created_at ON notification(created_at)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_user_id_is_seen ON notification(user_id, is_seen)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_team_id_type ON notification(team_id, type)")
            return
        }
        
        // 1. Create new table with nullable user_id
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS notification_new (
                id TEXT NOT NULL PRIMARY KEY,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                stained_at INTEGER,
                title TEXT NOT NULL,
                subtitle TEXT NOT NULL,
                message TEXT NOT NULL,
                type TEXT NOT NULL,
                sender TEXT NOT NULL DEFAULT '',
                user_id TEXT,
                team_id TEXT,
                is_seen INTEGER NOT NULL DEFAULT 0,
                is_pinned INTEGER NOT NULL DEFAULT 0,
                linked_event_id TEXT,
                linked_event_type TEXT,
                data TEXT,
                priority TEXT NOT NULL DEFAULT 'NORMAL',
                expires_at INTEGER,
                action_url TEXT,
                FOREIGN KEY(user_id) REFERENCES user(id) ON UPDATE CASCADE ON DELETE CASCADE,
                FOREIGN KEY(team_id) REFERENCES team(id) ON UPDATE CASCADE ON DELETE CASCADE
            )
            """.trimIndent()
        )
        
        // 2. Copy data from old table to new table
        // Use COALESCE to provide default values for any NULL fields
        db.execSQL(
            """
            INSERT INTO notification_new (
                id, created_at, updated_at, stained_at, title, subtitle, message, type, sender,
                user_id, team_id, is_seen, is_pinned, linked_event_id, linked_event_type,
                data, priority, expires_at, action_url
            )
            SELECT 
                id, created_at, updated_at, stained_at, 
                COALESCE(title, ''), 
                COALESCE(subtitle, ''), 
                COALESCE(message, ''), 
                type, 
                COALESCE(sender, ''), 
                user_id, team_id, 
                COALESCE(is_seen, 0), 
                COALESCE(is_pinned, 0), 
                linked_event_id, linked_event_type, data, 
                COALESCE(priority, 'NORMAL'), 
                expires_at, action_url
            FROM notification
            """.trimIndent()
        )
        
        // 3. Drop old table
        db.execSQL("DROP TABLE IF EXISTS notification")
        
        // 4. Rename new table to original name
        db.execSQL("ALTER TABLE notification_new RENAME TO notification")
        
        // 5. Recreate indices
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_user_id ON notification(user_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_team_id ON notification(team_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_type ON notification(type)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_is_seen ON notification(is_seen)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_is_pinned ON notification(is_pinned)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_created_at ON notification(created_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_user_id_is_seen ON notification(user_id, is_seen)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_notification_team_id_type ON notification(team_id, type)")
    }
}

