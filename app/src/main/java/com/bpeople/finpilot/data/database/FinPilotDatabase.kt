package com.bpeople.finpilot.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.bpeople.finpilot.data.database.dao.ExpenseDao
import com.bpeople.finpilot.data.database.dao.FreelanceProjectDao
import com.bpeople.finpilot.data.database.dao.GoalDao
import com.bpeople.finpilot.data.database.dao.IncomeDao
import com.bpeople.finpilot.data.model.Converters
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.data.model.FreelanceProject
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.model.IncomeEntry

/**
 * FinPilot Room Database - local SQLite storage that mirrors Firestore structure.
 * Provides offline support and reactive UI updates through Flow-based DAOs.
 *
 * Database version: 1
 * Entities: IncomeEntry, ExpenseEntry, Goal, FreelanceProject
 */
@Database(
    entities = [
        IncomeEntry::class,
        ExpenseEntry::class,
        Goal::class,
        FreelanceProject::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class FinPilotDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao
    abstract fun freelanceProjectDao(): FreelanceProjectDao
}
