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
