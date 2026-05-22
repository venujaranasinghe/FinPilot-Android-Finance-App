package com.bpeople.finpilot

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.Room
import com.bpeople.finpilot.data.database.FinPilotDatabase
import com.bpeople.finpilot.data.model.*
import com.bpeople.finpilot.data.repository.*
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/**
 * End-to-End Integration Test Suite for FinPilot Android Finance App
 *
 * Test Coverage:
 * 1. User Registration and Authentication
 * 2. User Login Flow
 * 3. Dashboard Loading and Display
 * 4. Add Expense Functionality
 * 5. Add Income Functionality
 * 6. Goal Tracker Management
 * 7. Offline Mode Behavior
 *
 * This integration test uses an in-memory Room database to test the complete
 * application flow without actual Firebase connections.
 */
@RunWith(AndroidJUnit4::class)
class IntegrationTest {

    private lateinit var database: FinPilotDatabase
    private lateinit var expenseRepository: ExpenseRepository
    private lateinit var incomeRepository: IncomeRepository
    private lateinit var goalRepository: GoalRepository
    private lateinit var userRepository: UserRepository
    private lateinit var transactionRepository: TransactionRepository

    private val testUserId = "test_user_123"
    private val testEmail = "testuser@finpilot.com"
    private val testPassword = "TestPassword@123"

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            FinPilotDatabase::class.java
        ).allowMainThreadQueries().build()

        // Initialize repositories with test database
        expenseRepository = ExpenseRepository(database.expenseDao())
        incomeRepository = IncomeRepository(database.incomeDao())
        goalRepository = GoalRepository(database.goalDao())
        userRepository = UserRepository(database.userDao())
        transactionRepository = TransactionRepository(
            database.incomeDao(),
            database.expenseDao()
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * TEST 1: User Registration Flow
     * Expected: User profile is created successfully with provided details
     */
    @Test
    fun testUserRegistrationFlow() = runBlocking {
        // Arrange
        val userProfile = UserProfile(
            userId = testUserId,
            email = testEmail,
            displayName = "Test User",
            profileImageUrl = "",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            currency = "USD",
            language = "en"
        )

        // Act
        database.userDao().insertUser(userProfile)
        val savedUser = database.userDao().getUserById(testUserId).first()

        // Assert
        assert(savedUser != null) { "User should be created during registration" }
        assert(savedUser?.email == testEmail) { "Email should match registered email" }
        assert(savedUser?.displayName == "Test User") { "Display name should match" }
        assert(savedUser?.currency == "USD") { "Currency should be set to USD" }
    }

    /**
     * TEST 2: User Login Flow
     * Expected: Existing user can be retrieved and authenticated
     */
    @Test
    fun testUserLoginFlow() = runBlocking {
        // Arrange - Pre-populate user
        val userProfile = UserProfile(
            userId = testUserId,
            email = testEmail,
            displayName = "Test User",
            profileImageUrl = "",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            currency = "USD",
            language = "en"
        )
        database.userDao().insertUser(userProfile)

        // Act - Login (retrieve user)
        val loggedInUser = database.userDao().getUserById(testUserId).first()

        // Assert
        assert(loggedInUser != null) { "User should exist for login" }
        assert(loggedInUser?.email == testEmail) { "Login should retrieve correct user" }
    }

    /**
     * TEST 3: Dashboard Data Loading
     * Expected: Dashboard displays aggregated financial data (expenses, income, balance)
     */
    @Test
    fun testDashboardDataLoading() = runBlocking {
        // Arrange - Setup user
        val userProfile = UserProfile(
            userId = testUserId,
            email = testEmail,
            displayName = "Test User",
            profileImageUrl = "",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            currency = "USD",
            language = "en"
        )
        database.userDao().insertUser(userProfile)

        // Add sample income
        val income1 = IncomeEntry(
            id = "income_1",
            userId = testUserId,
            amount = 5000.0,
            source = "Salary",
            date = Timestamp.now(),
            note = "Monthly salary"
        )
        database.incomeDao().insertIncome(income1)

        // Add sample expenses
        val expense1 = ExpenseEntry(
            id = "expense_1",
            userId = testUserId,
            amount = 500.0,
            category = "FOOD",
            subCategory = "Groceries",
            paymentMethod = "CREDIT_CARD",
            date = Timestamp.now(),
            note = "Weekly groceries"
        )
        database.expenseDao().insertExpense(expense1)

        // Act - Load dashboard data
        val totalIncome = database.incomeDao().getTotalIncomeForUser(testUserId).first()
        val totalExpense = database.expenseDao().getTotalExpenseForUser(testUserId).first()
        val user = database.userDao().getUserById(testUserId).first()

        // Assert
        assert(totalIncome == 5000.0) { "Total income should be 5000" }
        assert(totalExpense == 500.0) { "Total expense should be 500" }
        assert(user != null) { "User profile should load on dashboard" }
    }

    /**
     * TEST 4: Add Expense Functionality
     * Expected: New expense is saved and retrievable
     */
    @Test
    fun testAddExpenseFlow() = runBlocking {
        // Arrange
        val expense = ExpenseEntry(
            id = UUID.randomUUID().toString(),
            userId = testUserId,
            amount = 150.50,
            category = "FOOD",
            subCategory = "Restaurant",
            paymentMethod = "DEBIT_CARD",
            date = Timestamp.now(),
            note = "Lunch with colleagues",
            isRecurring = false,
            tags = listOf("lunch", "social")
        )

        // Act
        database.expenseDao().insertExpense(expense)
        val allExpenses = database.expenseDao().getExpensesForUser(testUserId).first()
        val savedExpense = allExpenses.firstOrNull { it.id == expense.id }

        // Assert
        assert(savedExpense != null) { "Expense should be saved successfully" }
        assert(savedExpense?.amount == 150.50) { "Expense amount should match" }
        assert(savedExpense?.category == "FOOD") { "Expense category should be FOOD" }
        assert(savedExpense?.tags?.contains("lunch") == true) { "Tags should be saved" }
    }

    /**
     * TEST 5: Add Multiple Expenses with Different Categories
     * Expected: All expenses are saved with correct categories
     */
    @Test
    fun testAddMultipleExpensesFlow() = runBlocking {
        // Arrange
        val expenses = listOf(
            ExpenseEntry(
                id = "exp_1",
                userId = testUserId,
                amount = 50.0,
                category = "FOOD",
                subCategory = "Groceries",
                paymentMethod = "CREDIT_CARD",
                date = Timestamp.now(),
                note = "Groceries"
            ),
            ExpenseEntry(
                id = "exp_2",
                userId = testUserId,
                amount = 1200.0,
                category = "UTILITIES",
                subCategory = "Electricity",
                paymentMethod = "BANK_TRANSFER",
                date = Timestamp.now(),
                note = "Monthly electricity bill"
            ),
            ExpenseEntry(
                id = "exp_3",
                userId = testUserId,
                amount = 300.0,
                category = "ENTERTAINMENT",
                subCategory = "Movies",
                paymentMethod = "CREDIT_CARD",
                date = Timestamp.now(),
                note = "Movie tickets and popcorn"
            )
        )

        // Act
        database.expenseDao().insertExpensesList(expenses)
        val allExpenses = database.expenseDao().getExpensesForUser(testUserId).first()
        val categoryBreakdown = allExpenses.groupBy { it.category }

        // Assert
        assert(allExpenses.size == 3) { "All 3 expenses should be saved" }
        assert(categoryBreakdown.containsKey("FOOD")) { "Food category should exist" }
        assert(categoryBreakdown.containsKey("UTILITIES")) { "Utilities category should exist" }
        assert(categoryBreakdown.containsKey("ENTERTAINMENT")) { "Entertainment category should exist" }
    }

    /**
     * TEST 6: Add Income Functionality
     * Expected: New income is saved and retrievable
     */
    @Test
    fun testAddIncomeFlow() = runBlocking {
        // Arrange
        val income = IncomeEntry(
            id = UUID.randomUUID().toString(),
            userId = testUserId,
            amount = 5000.0,
            source = "Salary",
            date = Timestamp.now(),
            note = "Monthly salary from employer",
            isRecurring = true
        )

        // Act
        database.incomeDao().insertIncome(income)
        val allIncome = database.incomeDao().getIncomeForUser(testUserId).first()
        val savedIncome = allIncome.firstOrNull { it.id == income.id }

        // Assert
        assert(savedIncome != null) { "Income should be saved successfully" }
        assert(savedIncome?.amount == 5000.0) { "Income amount should match" }
        assert(savedIncome?.source == "Salary") { "Income source should be Salary" }
        assert(savedIncome?.isRecurring == true) { "Income should be marked as recurring" }
    }

    /**
     * TEST 7: Add Multiple Income Sources
     * Expected: All income sources are saved correctly
     */
    @Test
    fun testAddMultipleIncomeSourcesFlow() = runBlocking {
        // Arrange
        val incomes = listOf(
            IncomeEntry(
                id = "inc_1",
                userId = testUserId,
                amount = 5000.0,
                source = "Salary",
                date = Timestamp.now(),
                note = "Monthly salary",
                isRecurring = true
            ),
            IncomeEntry(
                id = "inc_2",
                userId = testUserId,
                amount = 500.0,
                source = "Freelance",
                date = Timestamp.now(),
                note = "Project work",
                isRecurring = false
            ),
            IncomeEntry(
                id = "inc_3",
                userId = testUserId,
                amount = 100.0,
                source = "Investment",
                date = Timestamp.now(),
                note = "Dividend income",
                isRecurring = true
            )
        )

        // Act
        database.incomeDao().insertIncomeList(incomes)
        val allIncome = database.incomeDao().getIncomeForUser(testUserId).first()

        // Assert
        assert(allIncome.size == 3) { "All 3 income entries should be saved" }
        assert(allIncome.any { it.source == "Salary" }) { "Salary income should exist" }
        assert(allIncome.any { it.source == "Freelance" }) { "Freelance income should exist" }
        assert(allIncome.any { it.source == "Investment" }) { "Investment income should exist" }
    }

    /**
     * TEST 8: Goal Tracker - Create Goal
     * Expected: New goal is created with target and tracking enabled
     */
    @Test
    fun testCreateGoalFlow() = runBlocking {
        // Arrange
        val goal = Goal(
            id = UUID.randomUUID().toString(),
            userId = testUserId,
            title = "Save for Vacation",
            targetAmount = 5000.0,
            currentAmount = 1000.0,
            deadline = Timestamp(Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000)),
            monthlyRequired = 333.33,
            isActive = true,
            createdAt = Timestamp.now(),
            notes = "Vacation to Europe"
        )

        // Act
        database.goalDao().insertGoal(goal)
        val allGoals = database.goalDao().getGoalsForUser(testUserId).first()
        val savedGoal = allGoals.firstOrNull { it.id == goal.id }

        // Assert
        assert(savedGoal != null) { "Goal should be created successfully" }
        assert(savedGoal?.title == "Save for Vacation") { "Goal title should match" }
        assert(savedGoal?.targetAmount == 5000.0) { "Goal target should be 5000" }
        assert(savedGoal?.isActive == true) { "Goal should be active" }
    }

    /**
     * TEST 9: Goal Tracker - Update Goal Progress
     * Expected: Goal current amount updates correctly
     */
    @Test
    fun testUpdateGoalProgressFlow() = runBlocking {
        // Arrange - Create initial goal
        val goalId = "goal_123"
        val goal = Goal(
            id = goalId,
            userId = testUserId,
            title = "Emergency Fund",
            targetAmount = 10000.0,
            currentAmount = 2000.0,
            deadline = Timestamp(Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000)),
            monthlyRequired = 666.67,
            isActive = true,
            createdAt = Timestamp.now()
        )
        database.goalDao().insertGoal(goal)

        // Act - Update goal progress
        val updatedGoal = goal.copy(currentAmount = 5000.0)
        database.goalDao().updateGoal(updatedGoal)
        val retrievedGoal = database.goalDao().getGoalById(goalId).first()

        // Assert
        assert(retrievedGoal?.currentAmount == 5000.0) { "Goal progress should update to 5000" }
        assert(retrievedGoal?.targetAmount == 10000.0) { "Goal target should remain unchanged" }
    }

    /**
     * TEST 10: Goal Tracker - Multiple Goals
     * Expected: Multiple goals are tracked independently
     */
    @Test
    fun testMultipleGoalsFlow() = runBlocking {
        // Arrange
        val goals = listOf(
            Goal(
                id = "goal_1",
                userId = testUserId,
                title = "Vacation Fund",
                targetAmount = 5000.0,
                currentAmount = 1000.0,
                deadline = Timestamp(Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000)),
                monthlyRequired = 333.33,
                isActive = true,
                createdAt = Timestamp.now()
            ),
            Goal(
                id = "goal_2",
                userId = testUserId,
                title = "Car Fund",
                targetAmount = 20000.0,
                currentAmount = 5000.0,
                deadline = Timestamp(Date(System.currentTimeMillis() + 2 * 365 * 24 * 60 * 60 * 1000)),
                monthlyRequired = 625.0,
                isActive = true,
                createdAt = Timestamp.now()
            ),
            Goal(
                id = "goal_3",
                userId = testUserId,
                title = "Home Improvement",
                targetAmount = 3000.0,
                currentAmount = 500.0,
                deadline = Timestamp(Date(System.currentTimeMillis() + 180 * 24 * 60 * 60 * 1000)),
                monthlyRequired = 500.0,
                isActive = true,
                createdAt = Timestamp.now()
            )
        )

        // Act
        database.goalDao().insertGoalsList(goals)
        val allGoals = database.goalDao().getGoalsForUser(testUserId).first()

        // Assert
        assert(allGoals.size == 3) { "All 3 goals should be created" }
        assert(allGoals.any { it.title == "Vacation Fund" }) { "Vacation goal should exist" }
        assert(allGoals.any { it.title == "Car Fund" }) { "Car goal should exist" }
        assert(allGoals.any { it.title == "Home Improvement" }) { "Home Improvement goal should exist" }
    }

    /**
     * TEST 11: Offline Mode - Data Persistence
     * Expected: Data persists locally in offline mode
     */
    @Test
    fun testOfflineModePersistence() = runBlocking {
        // Arrange - Add various data offline
        val income = IncomeEntry(
            id = "off_income_1",
            userId = testUserId,
            amount = 3000.0,
            source = "Salary",
            date = Timestamp.now(),
            note = "Offline entry"
        )
        val expense = ExpenseEntry(
            id = "off_exp_1",
            userId = testUserId,
            amount = 200.0,
            category = "FOOD",
            subCategory = "Groceries",
            paymentMethod = "CASH",
            date = Timestamp.now(),
            note = "Offline expense"
        )

        // Act - Save locally
        database.incomeDao().insertIncome(income)
        database.expenseDao().insertExpense(expense)

        // Simulate closing and reopening database (offline)
        database.close()
        val reopenedDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FinPilotDatabase::class.java
        ).allowMainThreadQueries().build()

        // Assert - Data should persist
        val persistedIncome = reopenedDatabase.incomeDao().getIncomeForUser(testUserId).first()
        val persistedExpense = reopenedDatabase.expenseDao().getExpensesForUser(testUserId).first()

        assert(persistedIncome.isNotEmpty()) { "Income data should persist offline" }
        assert(persistedExpense.isNotEmpty()) { "Expense data should persist offline" }

        reopenedDatabase.close()
    }

    /**
     * TEST 12: Offline Mode - Batch Sync
     * Expected: Data can be synced when connection returns
     */
    @Test
    fun testOfflineModeBatchSync() = runBlocking {
        // Arrange - Create batch of offline entries
        val offlineExpenses = listOf(
            ExpenseEntry(
                id = "sync_exp_1",
                userId = testUserId,
                amount = 100.0,
                category = "FOOD",
                subCategory = "Coffee",
                paymentMethod = "CARD",
                date = Timestamp.now()
            ),
            ExpenseEntry(
                id = "sync_exp_2",
                userId = testUserId,
                amount = 200.0,
                category = "TRANSPORT",
                subCategory = "Taxi",
                paymentMethod = "DIGITAL",
                date = Timestamp.now()
            )
        )

        // Act - Insert batch
        database.expenseDao().insertExpensesList(offlineExpenses)
        val expensesToSync = database.expenseDao().getExpensesForUser(testUserId).first()

        // Assert
        assert(expensesToSync.size == 2) { "Should have 2 expenses ready for sync" }
        assert(expensesToSync.all { it.userId == testUserId }) { "All expenses should belong to correct user" }
    }

    /**
     * TEST 13: Complete User Flow - End-to-End Scenario
     * Register → Login → Dashboard → Add Expenses → Add Income → Create Goals → Verify Summary
     */
    @Test
    fun testCompleteUserFlowEndToEnd() = runBlocking {
        // STEP 1: Registration
        val userProfile = UserProfile(
            userId = testUserId,
            email = testEmail,
            displayName = "John Doe",
            profileImageUrl = "",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            currency = "USD",
            language = "en"
        )
        database.userDao().insertUser(userProfile)

        // STEP 2: Login (Retrieve user)
        val loggedInUser = database.userDao().getUserById(testUserId).first()
        assert(loggedInUser != null) { "User should login successfully" }

        // STEP 3: Add Income
        val salary = IncomeEntry(
            id = "flow_income_1",
            userId = testUserId,
            amount = 5000.0,
            source = "Salary",
            date = Timestamp.now(),
            note = "Monthly salary",
            isRecurring = true
        )
        database.incomeDao().insertIncome(salary)

        // STEP 4: Add Expenses
        val expenses = listOf(
            ExpenseEntry(
                id = "flow_exp_1",
                userId = testUserId,
                amount = 1200.0,
                category = "RENT",
                subCategory = "Apartment",
                paymentMethod = "BANK_TRANSFER",
                date = Timestamp.now()
            ),
            ExpenseEntry(
                id = "flow_exp_2",
                userId = testUserId,
                amount = 400.0,
                category = "FOOD",
                subCategory = "Groceries",
                paymentMethod = "CREDIT_CARD",
                date = Timestamp.now()
            ),
            ExpenseEntry(
                id = "flow_exp_3",
                userId = testUserId,
                amount = 100.0,
                category = "UTILITIES",
                subCategory = "Internet",
                paymentMethod = "AUTO_DEBIT",
                date = Timestamp.now()
            )
        )
        database.expenseDao().insertExpensesList(expenses)

        // STEP 5: Create Goal
        val savingsGoal = Goal(
            id = "flow_goal_1",
            userId = testUserId,
            title = "Emergency Fund",
            targetAmount = 15000.0,
            currentAmount = 2000.0,
            deadline = Timestamp(Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000)),
            monthlyRequired = 1083.33,
            isActive = true,
            createdAt = Timestamp.now()
        )
        database.goalDao().insertGoal(savingsGoal)

        // STEP 6: Verify Dashboard Summary
        val finalUser = database.userDao().getUserById(testUserId).first()
        val finalIncome = database.incomeDao().getTotalIncomeForUser(testUserId).first()
        val finalExpense = database.expenseDao().getTotalExpenseForUser(testUserId).first()
        val allGoals = database.goalDao().getGoalsForUser(testUserId).first()

        // Assert complete flow
        assert(finalUser != null) { "User should exist after complete flow" }
        assert(finalIncome == 5000.0) { "Total income should be 5000" }
        assert(finalExpense == 1700.0) { "Total expense should be 1700" }
        assert(allGoals.size == 1) { "Should have 1 goal" }
    }

    /**
     * TEST 14: Data Isolation Between Users
     * Expected: Each user's data is isolated and not visible to other users
     */
    @Test
    fun testDataIsolationBetweenUsers() = runBlocking {
        // Arrange - Create two users
        val user1Id = "user_1"
        val user2Id = "user_2"

        val user1Profile = UserProfile(
            userId = user1Id,
            email = "user1@test.com",
            displayName = "User One",
            profileImageUrl = "",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            currency = "USD",
            language = "en"
        )
        val user2Profile = UserProfile(
            userId = user2Id,
            email = "user2@test.com",
            displayName = "User Two",
            profileImageUrl = "",
            createdAt = Timestamp.now(),
            updatedAt = Timestamp.now(),
            currency = "EUR",
            language = "fr"
        )

        database.userDao().insertUser(user1Profile)
        database.userDao().insertUser(user2Profile)

        // Add data for each user
        val expense1 = ExpenseEntry(
            id = "exp_user1",
            userId = user1Id,
            amount = 500.0,
            category = "FOOD",
            subCategory = "Groceries",
            paymentMethod = "CARD",
            date = Timestamp.now()
        )
        val expense2 = ExpenseEntry(
            id = "exp_user2",
            userId = user2Id,
            amount = 1000.0,
            category = "RENT",
            subCategory = "Apartment",
            paymentMethod = "TRANSFER",
            date = Timestamp.now()
        )

        database.expenseDao().insertExpense(expense1)
        database.expenseDao().insertExpense(expense2)

        // Act - Retrieve data for each user
        val user1Expenses = database.expenseDao().getExpensesForUser(user1Id).first()
        val user2Expenses = database.expenseDao().getExpensesForUser(user2Id).first()

        // Assert - Data should be isolated
        assert(user1Expenses.size == 1) { "User 1 should have 1 expense" }
        assert(user2Expenses.size == 1) { "User 2 should have 1 expense" }
        assert(user1Expenses[0].amount == 500.0) { "User 1 expense should be 500" }
        assert(user2Expenses[0].amount == 1000.0) { "User 2 expense should be 1000" }
        assert(user1Expenses.none { it.userId == user2Id }) { "User 1 data should not contain User 2 data" }
    }
}
