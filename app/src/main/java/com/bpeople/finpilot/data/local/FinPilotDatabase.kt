package com.bpeople.finpilot.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bpeople.finpilot.data.local.entities.RoomCryptoEntry
import com.bpeople.finpilot.data.local.entities.RoomExpense
import com.bpeople.finpilot.data.local.entities.RoomGoal
import com.bpeople.finpilot.data.local.entities.RoomIncome
import com.bpeople.finpilot.data.local.entities.RoomProject
import com.bpeople.finpilot.data.local.entities.RoomSubscription
import com.bpeople.finpilot.data.model.Converters
import com.bpeople.finpilot.data.local.dao.CryptoDao
import com.bpeople.finpilot.data.local.dao.IncomeDao
import com.bpeople.finpilot.data.local.dao.ExpenseDao
import com.bpeople.finpilot.data.local.dao.GoalDao
import com.bpeople.finpilot.data.local.dao.FreelanceProjectDao
import com.bpeople.finpilot.data.local.dao.SubscriptionDao

@Database(
    entities = [
        RoomIncome::class,
        RoomExpense::class,
        RoomGoal::class,
        RoomProject::class,
        RoomCryptoEntry::class,
        RoomSubscription::class,
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FinPilotDatabase : RoomDatabase() {
    abstract fun incomeDao(): IncomeDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao
    abstract fun freelanceProjectDao(): FreelanceProjectDao
    abstract fun cryptoDao(): CryptoDao
    abstract fun subscriptionDao(): SubscriptionDao
}


