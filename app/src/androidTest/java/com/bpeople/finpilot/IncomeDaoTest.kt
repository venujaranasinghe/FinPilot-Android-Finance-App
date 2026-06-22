package com.bpeople.finpilot.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bpeople.finpilot.data.database.dao.IncomeDao
import com.bpeople.finpilot.data.model.IncomeEntry
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

/**
 * Instrumented test for IncomeDao operations.
 * Tests insert, update, delete, and query operations with various filters.
 */
@RunWith(AndroidJUnit4::class)
class IncomeDaoTest {

    private lateinit var database: FinPilotDatabase
    private lateinit var incomeDao: IncomeDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FinPilotDatabase::class.java).build()
        incomeDao = database.incomeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveIncome() = runBlocking {
        val income = IncomeEntry(
            id = "income_1",
            userId = "user_1",
            source = "Salary",
            amountOriginal = 5000.0,
            currencyOriginal = "USD",
            amountLKR = 1650000.0,
            exchangeRate = 330.0,
            date = Timestamp.now(),
            label = "Monthly Salary",
            type = "RECURRING"
        )

        incomeDao.insertIncome(income)
        val retrieved = incomeDao.getIncomeById("income_1")

        assert(retrieved != null)
        assert(retrieved?.source == "Salary")
        assert(retrieved?.amountLKR == 1650000.0)
    }

    @Test
    fun insertMultipleAndQueryByMonth() = runBlocking {
        val calendar = Calendar.getInstance()
        val now = Timestamp(calendar.time)

        val incomes = listOf(
            IncomeEntry(
                id = "income_1",
                userId = "user_1",
                source = "Salary",
                amountLKR = 100000.0,
                date = now
            ),
            IncomeEntry(
                id = "income_2",
                userId = "user_1",
                source = "Freelance",
                amountLKR = 50000.0,
                date = now
            )
        )

        incomeDao.insertIncomeList(incomes)
        val result = incomeDao.getIncomeByMonth("user_1", 2026, 5).first()

        assert(result.size == 2)
        assert(result.sumOf { it.amountLKR } == 150000.0)
    }

    @Test
    fun updateIncomeEntry() = runBlocking {
        val income = IncomeEntry(
            id = "income_1",
            userId = "user_1",
            source = "Salary",
            amountLKR = 100000.0,
            date = Timestamp.now()
        )

        incomeDao.insertIncome(income)
        val updated = income.copy(amountLKR = 120000.0)
        incomeDao.updateIncome(updated)

        val retrieved = incomeDao.getIncomeById("income_1")
        assert(retrieved?.amountLKR == 120000.0)
    }

    @Test
    fun deleteIncomeEntry() = runBlocking {
        val income = IncomeEntry(
            id = "income_1",
            userId = "user_1",
            source = "Salary",
            amountLKR = 100000.0,
            date = Timestamp.now()
        )

        incomeDao.insertIncome(income)
        incomeDao.deleteIncome(income)

        val retrieved = incomeDao.getIncomeById("income_1")
        assert(retrieved == null)
    }

    @Test
    fun getTotalIncomeForMonth() = runBlocking {
        val calendar = Calendar.getInstance()
        val now = Timestamp(calendar.time)

        val incomes = listOf(
            IncomeEntry(
                id = "income_1",
                userId = "user_1",
                source = "Salary",
                amountLKR = 100000.0,
                date = now
            ),
            IncomeEntry(
                id = "income_2",
                userId = "user_1",
                source = "Bonus",
                amountLKR = 50000.0,
                date = now
            )
        )

        incomeDao.insertIncomeList(incomes)
        val total = incomeDao.getTotalIncomeForMonth("user_1", 2026, 5)

        assert(total == 150000.0)
    }

    @Test
    fun getRecurringIncome() = runBlocking {
        val now = Timestamp.now()

        val incomes = listOf(
            IncomeEntry(
                id = "income_1",
                userId = "user_1",
                source = "Salary",
                type = "RECURRING",
                amountLKR = 100000.0,
                date = now
            ),
            IncomeEntry(
                id = "income_2",
                userId = "user_1",
                source = "Freelance",
                type = "ONE_OFF",
                amountLKR = 50000.0,
                date = now
            )
        )

        incomeDao.insertIncomeList(incomes)
        val result = incomeDao.getRecurringIncome("user_1").first()

        assert(result.size == 1)
        assert(result[0].source == "Salary")
    }

    @Test
    fun deleteAllIncomeForUser() = runBlocking {
        val now = Timestamp.now()

        val incomes = listOf(
            IncomeEntry(
                id = "income_1",
                userId = "user_1",
                source = "Salary",
                amountLKR = 100000.0,
                date = now
            ),
            IncomeEntry(
                id = "income_2",
                userId = "user_1",
                source = "Freelance",
                amountLKR = 50000.0,
                date = now
            )
        )

        incomeDao.insertIncomeList(incomes)
        incomeDao.deleteAllIncomeForUser("user_1")

        val result = incomeDao.getAllIncomeForUser("user_1").first()
        assert(result.isEmpty())
    }
}
