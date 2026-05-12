package com.bpeople.finpilot.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Central place to keep Room migrations.
 *
 * Current strategy:
 * - Prefer explicit migrations for upgrades.
 * - Allow destructive migration on *downgrade* only.
 */
object FinPilotDatabaseMigrations {

    /**
     * v1 -> v2
     *
     * v2 introduces an explicit migration baseline (no schema changes from v1).
     */
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // No-op: schema unchanged. Keeping this migration avoids runtime crashes
            // when upgrading an existing v1 install to v2.
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2,
    )
}

