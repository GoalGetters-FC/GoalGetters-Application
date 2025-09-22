package com.ggetters.app.data.local.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

const val OLD_VERSION =  1   // e.g., 7
const val NEW_VERSION =  2   // e.g., 8

val MIGRATION_1_2 = object : Migration(OLD_VERSION, NEW_VERSION) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Create the new table schema (no 'code', 'joined_at' instead of 'annexed_at')
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `user_new` (
              `id` TEXT NOT NULL,
              `created_at` INTEGER NOT NULL,
              `updated_at` INTEGER NOT NULL,
              `stained_at` INTEGER,
              
              `auth_id` TEXT NOT NULL,
              `team_id` TEXT NOT NULL,
              `joined_at` INTEGER,                -- from old annexed_at
              
              `role` TEXT NOT NULL,
              `name` TEXT NOT NULL,
              `surname` TEXT NOT NULL,
              `alias` TEXT NOT NULL,
              
              `date_of_birth` TEXT,               -- now nullable (was NOT NULL)
              `email` TEXT,
              `position` TEXT,
              `number` INTEGER,
              `status` TEXT,
              `health_weight` REAL,
              `health_height` REAL,
              
              PRIMARY KEY(`id`),
              FOREIGN KEY(`team_id`) REFERENCES `team`(`id`) 
                ON UPDATE CASCADE ON DELETE CASCADE
            )
            """.trimIndent()
        )

        // 2) Copy data from old table to new (map annexed_at -> joined_at, drop code)
        db.execSQL(
            """
            INSERT INTO `user_new` (
              `id`, `created_at`, `updated_at`, `stained_at`,
              `auth_id`, `team_id`, `joined_at`,
              `role`, `name`, `surname`, `alias`,
              `date_of_birth`, `email`, `position`, `number`, `status`,
              `health_weight`, `health_height`
            )
            SELECT
              `id`, `created_at`, `updated_at`, `stained_at`,
              `auth_id`, `team_id`, `annexed_at` AS `joined_at`,
              `role`, `name`, `surname`, `alias`,
              `date_of_birth`, `email`, `position`, `number`, `status`,
              `health_weight`, `health_height`
            FROM `user`
            """.trimIndent()
        )

        // 3) Drop old table
        db.execSQL("""DROP TABLE `user`""")

        // 4) Rename new table to the original name
        db.execSQL("""ALTER TABLE `user_new` RENAME TO `user`""")

        // 5) Recreate indices to match the new entity
        // (a) Unique id (PK is already unique, but Room declared an index—recreate it)
        db.execSQL("""CREATE UNIQUE INDEX IF NOT EXISTS `index_user_id` ON `user` (`id`)""")

        // (b) Unique composite auth_id + team_id
        db.execSQL("""CREATE UNIQUE INDEX IF NOT EXISTS `index_user_auth_id_team_id` ON `user` (`auth_id`, `team_id`)""")

        // (c) team_id lookup
        db.execSQL("""CREATE INDEX IF NOT EXISTS `index_user_team_id` ON `user` (`team_id`)""")
    }
}


/** DB v2 -> v3: align Event table indexes with @Entity expectations. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Defensive cleanup: remove any indices not declared on the entity
        db.execSQL("DROP INDEX IF EXISTS index_event_team_id_start_at")
        db.execSQL("DROP INDEX IF EXISTS index_event_team_id_category")
        db.execSQL("DROP INDEX IF EXISTS index_event_team_id_stained")

        // Ensure ONLY the indices Room expects are present
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_event_creator_id
            ON event(creator_id)
        """.trimIndent())

        db.execSQL("""
            CREATE INDEX IF NOT EXISTS index_event_team_id
            ON event(team_id)
        """.trimIndent())
    }
}

// create new migration for event, eventcategory, and eventstyle

/** DB v3 -> v4: migrate Event.category/style from Int -> String (Enum). */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Add temporary new string columns
        db.execSQL("ALTER TABLE event ADD COLUMN category_tmp TEXT")
        db.execSQL("ALTER TABLE event ADD COLUMN style_tmp TEXT")

        // 2) Copy + map old numeric values into new string columns
        db.execSQL(
            """
            UPDATE event
            SET category_tmp = CASE category
                WHEN 0 THEN 'PRACTICE'
                WHEN 1 THEN 'MATCH'
                ELSE 'OTHER'
            END
            """.trimIndent()
        )

        db.execSQL(
            """
            UPDATE event
            SET style_tmp = CASE style
                WHEN 0 THEN 'STANDARD'
                WHEN 1 THEN 'FRIENDLY'
                WHEN 2 THEN 'TOURNAMENT'
                ELSE 'TRAINING'
            END
            """.trimIndent()
        )

        // 3) Create new table schema (with TEXT instead of INT for category/style)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS event_new (
                id TEXT NOT NULL PRIMARY KEY,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                stained_at INTEGER,
                
                team_id TEXT NOT NULL,
                creator_id TEXT,
                
                name TEXT NOT NULL,
                description TEXT,
                
                category TEXT NOT NULL,
                style TEXT NOT NULL,
                
                start_at TEXT NOT NULL,
                end_at TEXT,
                location TEXT,
                
                FOREIGN KEY(team_id) REFERENCES team(id)
                  ON UPDATE CASCADE ON DELETE CASCADE,
                FOREIGN KEY(creator_id) REFERENCES user(id)
                  ON UPDATE CASCADE ON DELETE SET NULL
            )
            """.trimIndent()
        )

        // 4) Copy across values into new schema
        db.execSQL(
            """
            INSERT INTO event_new (
                id, created_at, updated_at, stained_at,
                team_id, creator_id,
                name, description,
                category, style,
                start_at, end_at, location
            )
            SELECT
                id, created_at, updated_at, stained_at,
                team_id, creator_id,
                name, description,
                category_tmp, style_tmp,
                start_at, end_at, location
            FROM event
            """.trimIndent()
        )

        // 5) Drop old table and rename new one
        db.execSQL("DROP TABLE event")
        db.execSQL("ALTER TABLE event_new RENAME TO event")

        // 6) Recreate indices (Room expects these)
        db.execSQL("CREATE INDEX IF NOT EXISTS index_event_creator_id ON event(creator_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_event_team_id ON event(team_id)")
    }
}

/** DB v4 -> v5: migrate Lineup.spots_json (TEXT) -> Lineup.spots (via TypeConverter). */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // 1) Create new table schema with `spots` column (TEXT, handled by TypeConverter)
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS lineup_new (
                id TEXT NOT NULL PRIMARY KEY,
                created_at INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                stained_at INTEGER,
                
                event_id TEXT NOT NULL,
                created_by TEXT,
                formation TEXT NOT NULL,
                spots TEXT NOT NULL, -- new column, replaces spots_json
                
                FOREIGN KEY(event_id) REFERENCES event(id)
                  ON UPDATE CASCADE ON DELETE CASCADE,
                FOREIGN KEY(created_by) REFERENCES user(id)
                  ON UPDATE CASCADE ON DELETE SET NULL
            )
            """.trimIndent()
        )

        // 2) Copy old data across, mapping spots_json -> spots
        // If spots_json is null, default to "[]"
        db.execSQL(
            """
            INSERT INTO lineup_new (
                id, created_at, updated_at, stained_at,
                event_id, created_by, formation, spots
            )
            SELECT
                id, created_at, updated_at, stained_at,
                event_id, created_by, formation,
                COALESCE(spots_json, '[]')
            FROM lineup
            """.trimIndent()
        )

        // 3) Drop old table
        db.execSQL("DROP TABLE lineup")

        // 4) Rename new table
        db.execSQL("ALTER TABLE lineup_new RENAME TO lineup")

        // 5) Recreate indices to match @Entity
        db.execSQL("CREATE INDEX IF NOT EXISTS index_lineup_event_id ON lineup(event_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_lineup_created_by ON lineup(created_by)")
    }
}