package com.bpeople.finpilot.data.database

import android.content.Context
import androidx.room.Room
import com.bpeople.finpilot.data.database.dao.ExpenseDao
import com.bpeople.finpilot.data.database.dao.FreelanceProjectDao
import com.bpeople.finpilot.data.database.dao.GoalDao
import com.bpeople.finpilot.data.database.dao.IncomeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for providing FinPilotDatabase and all DAOs.
 * Database is created as a singleton to maintain a single instance throughout app lifecycle.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides singleton instance of FinPilotDatabase.
     * Database is created with Room.databaseBuilder and uses the app context.
     *
     * Migration strategy: For version 1, no migrations are needed.
     * Future migrations should be added here using .addMigrations() before .build()
     */
    @Singleton
    @Provides
    fun provideFinPilotDatabase(
        @ApplicationContext context: Context
    ): FinPilotDatabase {
        return Room.databaseBuilder(
            context,
            FinPilotDatabase::class.java,
            "finpilot_database"
        ).build()
    }

    /**
     * Provides IncomeDao from singleton database instance.
     */
    @Singleton
    @Provides
    fun provideIncomeDao(database: FinPilotDatabase): IncomeDao {
        return database.incomeDao()
    }

    /**
     * Provides ExpenseDao from singleton database instance.
     */
    @Singleton
    @Provides
    fun provideExpenseDao(database: FinPilotDatabase): ExpenseDao {
        return database.expenseDao()
    }

    /**
     * Provides GoalDao from singleton database instance.
     */
    @Singleton
    @Provides
    fun provideGoalDao(database: FinPilotDatabase): GoalDao {
        return database.goalDao()
    }

    /**
     * Provides FreelanceProjectDao from singleton database instance.
     */
    @Singleton
    @Provides
    fun provideFreelanceProjectDao(database: FinPilotDatabase): FreelanceProjectDao {
        return database.freelanceProjectDao()
    }
}
