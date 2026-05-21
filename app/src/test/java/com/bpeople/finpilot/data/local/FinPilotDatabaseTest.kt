package com.bpeople.finpilot.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.bpeople.finpilot.data.local.entities.RoomExpense
import com.bpeople.finpilot.data.local.entities.RoomGoal
import com.bpeople.finpilot.data.local.entities.RoomIncome
import com.bpeople.finpilot.data.local.entities.RoomProject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class FinPilotDatabaseTest {

    private lateinit var db: FinPilotDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        db = Room.inMemoryDatabaseBuilder(context, FinPilotDatabase::class.java)
            .allowMainThreadQueries()
            .addMigrations(*FinPilotDatabaseMigrations.ALL)
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun incomeDao_insertAndGetIncomeByMonth_returnsInserted() = runTest {
        val dao = db.incomeDao()
        val userId = "u1"
        val start = 1000L
        val end = 2000L

        val row = RoomIncome(
            id = "inc1",
            userId = userId,
            source = "Salary",
            amountOriginal = 100.0,
            currencyOriginal = "USD",
            amountLKR = 30000.0,
            exchangeRate = 300.0,
            dateMillis = 1500L,
            label = "May",
            type = "income",
            projectRef = null,
        )

        dao.insertIncome(row)

        val result = dao.getIncomeByMonth(userId, start, end).first()
        assertEquals(1, result.size)
        assertEquals("inc1", result.first().id)
    }

    @Test
    fun expenseDao_insertAndQueryByMonthAndCategory_returnsInserted() = runTest {
        val dao = db.expenseDao()
        val userId = "u1"
        val start = 0L
        val end = 10_000L

        dao.insertExpense(
            RoomExpense(
                id = "exp1",
                userId = userId,
                amount = 250.0,
                category = "Food",
                subCategory = "Lunch",
                paymentMethod = "Cash",
                dateMillis = 5000L,
                note = null,
                isRecurring = false,
                tags = listOf("work"),
                originalCurrency = null,
                originalAmount = null,
            )
        )

        val byMonth = dao.getExpensesByMonth(userId, start, end).first()
        assertEquals(1, byMonth.size)

        val byCategory = dao.getExpensesByCategory(userId, "Food", start, end).first()
        assertEquals(1, byCategory.size)
        assertEquals("exp1", byCategory.first().id)

        val none = dao.getExpensesByCategory(userId, "Transport", start, end).first()
        assertTrue(none.isEmpty())
    }

    @Test
    fun goalDao_insertGetActiveAndUpdateAmount_works() = runTest {
        val dao = db.goalDao()
        val userId = "u1"

        dao.insertGoal(
            RoomGoal(
                id = "g1",
                userId = userId,
                title = "Emergency Fund",
                targetAmount = 1000.0,
                currentAmount = 100.0,
                deadlineMillis = 9999L,
                monthlyRequired = 100.0,
                isActive = true,
            )
        )

        val active = dao.getActiveGoal(userId).first()
        assertEquals(1, active.size)
        assertEquals("g1", active.first().id)

        dao.updateGoalAmount("g1", 250.0)
        val updated = dao.getActiveGoal(userId).first()
        assertEquals(250.0, updated.first().currentAmount, 0.0001)
    }

    @Test
    fun projectDao_insertAndGetByStatus_returnsInserted() = runTest {
        val dao = db.freelanceProjectDao()
        val userId = "u1"

        dao.insertProject(
            RoomProject(
                id = "p1",
                userId = userId,
                clientName = "Client",
                projectTitle = "Website",
                agreedAmount = 500.0,
                paidAmount = 0.0,
                status = "ACTIVE",
                entries = listOf("entry1"),
            )
        )

        val active = dao.getProjectsByStatus(userId, "ACTIVE").first()
        assertEquals(1, active.size)
        assertEquals("p1", active.first().id)

        val done = dao.getProjectsByStatus(userId, "DONE").first()
        assertTrue(done.isEmpty())
    }
}



