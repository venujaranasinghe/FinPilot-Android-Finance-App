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

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS room_crypto_holdings (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    symbol TEXT NOT NULL,
                    name TEXT NOT NULL,
                    quantity REAL NOT NULL,
                    buyPriceLKR REAL NOT NULL,
                    currentPriceLKR REAL NOT NULL,
                    note TEXT NOT NULL,
                    purchasedAtMillis INTEGER
                )""".trimIndent()
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS room_subscriptions (
                    id TEXT NOT NULL PRIMARY KEY,
                    userId TEXT NOT NULL,
                    name TEXT NOT NULL,
                    amountLKR REAL NOT NULL,
                    billingCycle TEXT NOT NULL,
                    nextBillingMillis INTEGER NOT NULL,
                    isActive INTEGER NOT NULL,
                    category TEXT NOT NULL,
                    note TEXT NOT NULL
                )""".trimIndent()
            )
        }
    }

    val ALL: Array<Migration> = arrayOf(
        MIGRATION_1_2,
        MIGRATION_2_3,
    )
}

