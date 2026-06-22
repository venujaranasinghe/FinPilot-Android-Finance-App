package com.bpeople.finpilot.data.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bpeople.finpilot.data.database.dao.GoalDao
import com.bpeople.finpilot.data.model.Goal
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

/**
 * Instrumented test for GoalDao operations.
 * Tests insert, update, delete, and query operations with status filters.
 */
@RunWith(AndroidJUnit4::class)
class GoalDaoTest {

    private lateinit var database: FinPilotDatabase
    private lateinit var goalDao: GoalDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FinPilotDatabase::class.java).build()
        goalDao = database.goalDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndRetrieveGoal() = runBlocking {
        val goal = Goal(
            id = "goal_1",
            userId = "user_1",
            title = "Vacation Fund",
            targetAmount = 500000.0,
            currentAmount = 100000.0,
            deadline = Timestamp.now(),
            monthlyRequired = 50000.0,
            isActive = true
        )

        goalDao.insertGoal(goal)
        val retrieved = goalDao.getGoalById("goal_1")

        assert(retrieved != null)
        assert(retrieved?.title == "Vacation Fund")
        assert(retrieved?.targetAmount == 500000.0)
    }

    @Test
    fun insertMultipleAndGetActive() = runBlocking {
        val goals = listOf(
            Goal(
                id = "goal_1",
                userId = "user_1",
                title = "Vacation Fund",
                targetAmount = 500000.0,
                currentAmount = 100000.0,
                deadline = Timestamp.now(),
                isActive = true
            ),
            Goal(
                id = "goal_2",
                userId = "user_1",
                title = "Car Fund",
                targetAmount = 2000000.0,
                currentAmount = 500000.0,
                deadline = Timestamp.now(),
                isActive = true
            ),
            Goal(
                id = "goal_3",
                userId = "user_1",
                title = "Old Goal",
                targetAmount = 100000.0,
                currentAmount = 100000.0,
                deadline = Timestamp.now(),
                isActive = false
            )
        )

        goalDao.insertGoalList(goals)
        val activeGoals = goalDao.getActiveGoals("user_1").first()

        assert(activeGoals.size == 2)
        assert(activeGoals.all { it.isActive })
    }

    @Test
    fun updateGoalAmount() = runBlocking {
        val goal = Goal(
            id = "goal_1",
            userId = "user_1",
            title = "Vacation Fund",
            targetAmount = 500000.0,
            currentAmount = 100000.0,
            deadline = Timestamp.now(),
            isActive = true
        )

        goalDao.insertGoal(goal)
        goalDao.updateGoalAmount("goal_1", 150000.0, 50000.0)

        val retrieved = goalDao.getGoalById("goal_1")
        assert(retrieved?.currentAmount == 150000.0)
        assert(retrieved?.monthlyRequired == 50000.0)
    }

    @Test
    fun deactivateGoal() = runBlocking {
        val goal = Goal(
            id = "goal_1",
            userId = "user_1",
            title = "Vacation Fund",
            targetAmount = 500000.0,
            currentAmount = 500000.0,
            deadline = Timestamp.now(),
            isActive = true
        )

        goalDao.insertGoal(goal)
        goalDao.deactivateGoal("goal_1")

        val retrieved = goalDao.getGoalById("goal_1")
        assert(!retrieved!!.isActive)
    }

    @Test
    fun getInactiveGoals() = runBlocking {
        val goals = listOf(
            Goal(
                id = "goal_1",
                userId = "user_1",
                title = "Active Goal",
                targetAmount = 500000.0,
                isActive = true,
                deadline = Timestamp.now()
            ),
            Goal(
                id = "goal_2",
                userId = "user_1",
                title = "Completed Goal",
                targetAmount = 100000.0,
                isActive = false,
                deadline = Timestamp.now()
            )
        )

        goalDao.insertGoalList(goals)
        val inactiveGoals = goalDao.getInactiveGoals("user_1").first()

        assert(inactiveGoals.size == 1)
        assert(!inactiveGoals[0].isActive)
    }

    @Test
    fun deleteGoal() = runBlocking {
        val goal = Goal(
            id = "goal_1",
            userId = "user_1",
            title = "Vacation Fund",
            targetAmount = 500000.0,
            currentAmount = 100000.0,
            deadline = Timestamp.now(),
            isActive = true
        )

        goalDao.insertGoal(goal)
        goalDao.deleteGoal(goal)

        val retrieved = goalDao.getGoalById("goal_1")
        assert(retrieved == null)
    }

    @Test
    fun deleteAllGoalsForUser() = runBlocking {
        val goals = listOf(
            Goal(
                id = "goal_1",
                userId = "user_1",
                title = "Goal 1",
                targetAmount = 500000.0,
                deadline = Timestamp.now()
            ),
            Goal(
                id = "goal_2",
                userId = "user_1",
                title = "Goal 2",
                targetAmount = 1000000.0,
                deadline = Timestamp.now()
            )
        )

        goalDao.insertGoalList(goals)
        goalDao.deleteAllGoalsForUser("user_1")

        val result = goalDao.getAllGoalsForUser("user_1").first()
        assert(result.isEmpty())
    }

    @Test
    fun getActiveGoalCount() = runBlocking {
        val goals = listOf(
            Goal(
                id = "goal_1",
                userId = "user_1",
                title = "Goal 1",
                targetAmount = 500000.0,
                isActive = true,
                deadline = Timestamp.now()
            ),
            Goal(
                id = "goal_2",
                userId = "user_1",
                title = "Goal 2",
                targetAmount = 1000000.0,
                isActive = true,
                deadline = Timestamp.now()
            ),
            Goal(
                id = "goal_3",
                userId = "user_1",
                title = "Goal 3",
                targetAmount = 200000.0,
                isActive = false,
                deadline = Timestamp.now()
            )
        )

        goalDao.insertGoalList(goals)
        val count = goalDao.getActiveGoalCount("user_1")

        assert(count == 2)
    }

    @Test
    fun getOverallGoalProgress() = runBlocking {
        val goals = listOf(
            Goal(
                id = "goal_1",
                userId = "user_1",
                title = "Goal 1",
                targetAmount = 500000.0,
                currentAmount = 250000.0,
                isActive = true,
                deadline = Timestamp.now()
            ),
            Goal(
                id = "goal_2",
                userId = "user_1",
                title = "Goal 2",
                targetAmount = 1000000.0,
                currentAmount = 500000.0,
                isActive = true,
                deadline = Timestamp.now()
            )
        )

        goalDao.insertGoalList(goals)
        val progress = goalDao.getOverallGoalProgress("user_1")

        assert(progress["totalCurrent"] == 750000.0)
        assert(progress["totalTarget"] == 1500000.0)
    }
}
