package com.ggetters.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.ggetters.app.core.utils.Clogger

/**
 * Migration from database version 11 to 12.
 * 
 * Changes:
 * - Changed notification foreign key constraints from CASCADE to NO_ACTION
 * - This prevents notifications from being deleted when users or teams are modified
 */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Clogger.i("Migration_11_12", "🔄 Starting migration from v11 to v12")
        Clogger.i("Migration_11_12", "📝 Changing notification foreign key constraints to NO_ACTION")
        
        try {
            // 1. Create new notification table with updated foreign keys
            db.execSQL(
                """
                CREATE TABLE notification_new (
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
                    FOREIGN KEY(user_id) REFERENCES user(id) ON UPDATE CASCADE ON DELETE NO ACTION,
                    FOREIGN KEY(team_id) REFERENCES team(id) ON UPDATE CASCADE ON DELETE NO ACTION
                )
                """.trimIndent()
            )
            Clogger.d("Migration_11_12", "✅ Created new notification table with NO_ACTION constraints")
            
            // 2. Copy all data from old table to new table
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
            Clogger.d("Migration_11_12", "✅ Copied data from old to new table")
            
            // 3. Drop old table
            db.execSQL("DROP TABLE notification")
            Clogger.d("Migration_11_12", "✅ Dropped old notification table")
            
            // 4. Rename new table to original name
            db.execSQL("ALTER TABLE notification_new RENAME TO notification")
            Clogger.d("Migration_11_12", "✅ Renamed new table to 'notification'")
            
            // 5. Recreate all indices
            db.execSQL("CREATE INDEX index_notification_user_id ON notification(user_id)")
            db.execSQL("CREATE INDEX index_notification_team_id ON notification(team_id)")
            db.execSQL("CREATE INDEX index_notification_type ON notification(type)")
            db.execSQL("CREATE INDEX index_notification_is_seen ON notification(is_seen)")
            db.execSQL("CREATE INDEX index_notification_is_pinned ON notification(is_pinned)")
            db.execSQL("CREATE INDEX index_notification_created_at ON notification(created_at)")
            db.execSQL("CREATE INDEX index_notification_user_id_is_seen ON notification(user_id, is_seen)")
            db.execSQL("CREATE INDEX index_notification_team_id_type ON notification(team_id, type)")
            Clogger.d("Migration_11_12", "✅ Recreated all indices")
            
            Clogger.i("Migration_11_12", "✅ Migration from v11 to v12 completed successfully")
        } catch (e: Exception) {
            Clogger.e("Migration_11_12", "❌ Migration failed", e)
            throw e
        }
    }
}

