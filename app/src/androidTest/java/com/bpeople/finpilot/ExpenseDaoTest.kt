package com.bpeople.finpilot.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bpeople.finpilot.data.database.dao.ExpenseDao
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test for ExpenseDao operations.
 * Tests insert, update, delete, and query operations with category and date filters.
 */
@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {

    private lateinit var database: FinPilotDatabase
    private lateinit var expenseDao: ExpenseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FinPilotDatabase::class.java).build()
        expenseDao = database.expenseDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveExpense() = runBlocking {
        val expense = ExpenseEntry(
            id = "expense_1",
            userId = "user_1",
            amount = 5000.0,
            category = "FOOD",
            paymentMethod = "CARD",
            date = Timestamp.now(),
            note = "Grocery shopping"
        )

        expenseDao.insertExpense(expense)
        val retrieved = expenseDao.getExpenseById("expense_1")

        assert(retrieved != null)
        assert(retrieved?.category == "FOOD")
        assert(retrieved?.amount == 5000.0)
    }

    @Test
    fun insertMultipleAndQueryByMonth() = runBlocking {
        val now = Timestamp.now()

        val expenses = listOf(
            ExpenseEntry(
                id = "expense_1",
                userId = "user_1",
                amount = 5000.0,
                category = "FOOD",
                date = now
            ),
            ExpenseEntry(
                id = "expense_2",
                userId = "user_1",
                amount = 3000.0,
                category = "TRANSPORT",
                date = now
            )
        )

        expenseDao.insertExpenseList(expenses)
        val result = expenseDao.getExpensesByMonth("user_1", 2026, 5).first()

        assert(result.size == 2)
        assert(result.sumOf { it.amount } == 8000.0)
    }

    @Test
    fun queryByCategory() = runBlocking {
        val now = Timestamp.now()

        val expenses = listOf(
            ExpenseEntry(
                id = "expense_1",
                userId = "user_1",
                amount = 5000.0,
                category = "FOOD",
                date = now
            ),
            ExpenseEntry(
                id = "expense_2",
                userId = "user_1",
                amount = 3000.0,
                category = "FOOD",
                date = now
            ),
            ExpenseEntry(
                id = "expense_3",
                userId = "user_1",
                amount = 2000.0,
                category = "TRANSPORT",
                date = now
            )
        )

        expenseDao.insertExpenseList(expenses)
        val result = expenseDao.getExpensesByCategory("user_1", "FOOD").first()

        assert(result.size == 2)
        assert(result.sumOf { it.amount } == 8000.0)
    }

    @Test
    fun updateExpenseEntry() = runBlocking {
        val expense = ExpenseEntry(
            id = "expense_1",
            userId = "user_1",
            amount = 5000.0,
            category = "FOOD",
            date = Timestamp.now()
        )

        expenseDao.insertExpense(expense)
        val updated = expense.copy(amount = 6000.0)
        expenseDao.updateExpense(updated)

        val retrieved = expenseDao.getExpenseById("expense_1")
        assert(retrieved?.amount == 6000.0)
    }

    @Test
    fun deleteExpenseEntry() = runBlocking {
        val expense = ExpenseEntry(
            id = "expense_1",
            userId = "user_1",
            amount = 5000.0,
            category = "FOOD",
            date = Timestamp.now()
        )

        expenseDao.insertExpense(expense)
        expenseDao.deleteExpense(expense)

        val retrieved = expenseDao.getExpenseById("expense_1")
        assert(retrieved == null)
    }

    @Test
    fun getTotalExpensesForMonth() = runBlocking {
        val now = Timestamp.now()

        val expenses = listOf(
            ExpenseEntry(
                id = "expense_1",
                userId = "user_1",
                amount = 5000.0,
                category = "FOOD",
                date = now
            ),
            ExpenseEntry(
                id = "expense_2",
                userId = "user_1",
                amount = 3000.0,
                category = "TRANSPORT",
                date = now
            )
        )

        expenseDao.insertExpenseList(expenses)
        val total = expenseDao.getTotalExpensesForMonth("user_1", 2026, 5)

        assert(total == 8000.0)
    }

    @Test
    fun getRecurringExpenses() = runBlocking {
        val now = Timestamp.now()

        val expenses = listOf(
            ExpenseEntry(
                id = "expense_1",
                userId = "user_1",
                amount = 5000.0,
                category = "FOOD",
                isRecurring = true,
                date = now
            ),
            ExpenseEntry(
                id = "expense_2",
                userId = "user_1",
                amount = 3000.0,
                category = "TRANSPORT",
                isRecurring = false,
                date = now
            )
        )

        expenseDao.insertExpenseList(expenses)
        val result = expenseDao.getRecurringExpenses("user_1").first()

        assert(result.size == 1)
        assert(result[0].category == "FOOD")
    }

    @Test
    fun deleteAllExpensesForUser() = runBlocking {
        val now = Timestamp.now()

        val expenses = listOf(
            ExpenseEntry(
                id = "expense_1",
                userId = "user_1",
                amount = 5000.0,
                category = "FOOD",
                date = now
            ),
            ExpenseEntry(
                id = "expense_2",
                userId = "user_1",
                amount = 3000.0,
                category = "TRANSPORT",
                date = now
            )
        )

        expenseDao.insertExpenseList(expenses)
        expenseDao.deleteAllExpensesForUser("user_1")

        val result = expenseDao.getAllExpensesForUser("user_1").first()
        assert(result.isEmpty())
    }

    @Test
    fun getAllCategories() = runBlocking {
        val now = Timestamp.now()

        val expenses = listOf(
            ExpenseEntry(
                id = "expense_1",
                userId = "user_1",
                amount = 5000.0,
                category = "FOOD",
                date = now
            ),
            ExpenseEntry(
                id = "expense_2",
                userId = "user_1",
                amount = 3000.0,
                category = "FOOD",
                date = now
            ),
            ExpenseEntry(
                id = "expense_3",
                userId = "user_1",
                amount = 2000.0,
                category = "TRANSPORT",
                date = now
            )
        )

        expenseDao.insertExpenseList(expenses)
        val categories = expenseDao.getAllCategories("user_1").first()

        assert(categories.size == 2)
        assert(categories.contains("FOOD"))
        assert(categories.contains("TRANSPORT"))
    }
}
