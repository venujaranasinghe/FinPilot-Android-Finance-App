# FinPilot Room Database Implementation

## Overview
The FinPilot Room database provides offline-first local storage that mirrors the Firestore cloud database. It allows the app to function seamlessly without internet connectivity by maintaining a local SQLite copy of user data.

## Database Architecture

### Core Components
- **FinPilotDatabase** - Main Room database class
- **4 DAOs** - Data Access Objects for each entity
- **Entities** - Already defined model classes with @Entity annotations
- **Converters** - Custom TypeConverters for complex types (Timestamp, List<String>)
- **DatabaseModule** - Hilt DI module for dependency injection

### Database Schema
```
finpilot_database (v1)
├── income_entries (IncomeEntry)
├── expense_entries (ExpenseEntry)
├── goals (Goal)
└── freelance_projects (FreelanceProject)
```

## Entities

### IncomeEntry
**Table:** `income_entries`
- Stores income data with multi-currency support
- Tracks sources, amounts in original currency and LKR, exchange rates
- Supports recurring vs. one-off income
- Fields: id, userId, source, amountOriginal, currencyOriginal, amountLKR, exchangeRate, date, label, type, projectRef

### ExpenseEntry
**Table:** `expense_entries`
- Stores expense transactions with categorization
- Tracks payment methods and recurring expenses
- Supports tagging for flexible categorization
- Fields: id, userId, amount, category, subCategory, paymentMethod, date, note, isRecurring, tags, originalCurrency

### Goal
**Table:** `goals`
- Stores financial goals with progress tracking
- Includes deadline and monthly required amount calculations
- Supports active/inactive status for archive
- Fields: id, userId, title, targetAmount, currentAmount, deadline, monthlyRequired, isActive

### FreelanceProject
**Table:** `freelance_projects`
- Tracks freelance projects with payment status
- Stores client and project details
- Tracks agreed vs. paid amounts
- Fields: id, userId, clientName, projectTitle, agreedAmount, paidAmount, status, entries

## DAO Operations

### IncomeDao
```kotlin
// Insert operations
insertIncome(income: IncomeEntry): Long
insertIncomeList(incomes: List<IncomeEntry>)

// Query operations (all return Flow<List> for reactive updates)
getIncomeByMonth(userId, year, month): Flow<List<IncomeEntry>>
getAllIncomeForUser(userId): Flow<List<IncomeEntry>>
getRecurringIncome(userId): Flow<List<IncomeEntry>>
getIncomeBySource(userId, source): Flow<List<IncomeEntry>>

// One-time queries
getTotalIncomeForMonth(userId, year, month): Double
getTotalIncomeForYear(userId, year): Double

// Update/Delete
updateIncome(income: IncomeEntry)
deleteIncome(income: IncomeEntry)
deleteAllIncomeForUser(userId)
```

### ExpenseDao
```kotlin
// Insert operations
insertExpense(expense: ExpenseEntry): Long
insertExpenseList(expenses: List<ExpenseEntry>)

// Query operations (all return Flow<List> for reactive updates)
getExpensesByMonth(userId, year, month): Flow<List<ExpenseEntry>>
getExpensesByCategory(userId, category): Flow<List<ExpenseEntry>>
getExpensesByCategoryAndMonth(userId, category, year, month): Flow<List<ExpenseEntry>>
getAllExpensesForUser(userId): Flow<List<ExpenseEntry>>
getRecurringExpenses(userId): Flow<List<ExpenseEntry>>
getExpensesByPaymentMethod(userId, paymentMethod): Flow<List<ExpenseEntry>>
getAllCategories(userId): Flow<List<String>>

// One-time queries
getTotalExpensesForMonth(userId, year, month): Double
getTotalExpensesForYear(userId, year): Double
getTotalExpensesForCategoryAndMonth(userId, category, year, month): Double

// Update/Delete
updateExpense(expense: ExpenseEntry)
deleteExpense(expense: ExpenseEntry)
deleteAllExpensesForUser(userId)
```

### GoalDao
```kotlin
// Insert operations
insertGoal(goal: Goal): Long
insertGoalList(goals: List<Goal>)

// Query operations (all return Flow for reactive updates)
getActiveGoals(userId): Flow<List<Goal>>
getActiveGoal(goalId): Flow<Goal?>
getAllGoalsForUser(userId): Flow<List<Goal>>
getInactiveGoals(userId): Flow<List<Goal>>
getGoalsDueSoon(userId, daysAhead): Flow<List<Goal>>
getOverdueGoals(userId): Flow<List<Goal>>

// One-time queries
getGoalById(goalId): Goal?
getActiveGoalCount(userId): Int
getOverallGoalProgress(userId): Map<String, Double>

// Update/Delete
updateGoal(goal: Goal)
updateGoalAmount(goalId, currentAmount, monthlyRequired)
updateGoalProgress(goalId, currentAmount)
deactivateGoal(goalId)
reactivateGoal(goalId)
deleteGoal(goal: Goal)
deleteAllGoalsForUser(userId)
```

### FreelanceProjectDao
```kotlin
// Insert operations
insertProject(project: FreelanceProject): Long
insertProjectList(projects: List<FreelanceProject>)

// Query operations (all return Flow<List> for reactive updates)
getProjectsByStatus(userId, status): Flow<List<FreelanceProject>>
getActiveProjects(userId): Flow<List<FreelanceProject>>
getCompletedProjects(userId): Flow<List<FreelanceProject>>
getAllProjectsForUser(userId): Flow<List<FreelanceProject>>
getProjectsByClient(userId, clientName): Flow<List<FreelanceProject>>
getUnpaidProjects(userId): Flow<List<FreelanceProject>>
getAllClients(userId): Flow<List<String>>

// One-time queries (returns non-Flow)
getProjectById(projectId): Flow<FreelanceProject?>
getProjectByIdOnce(projectId): FreelanceProject?
getTotalAgreedAmount(userId): Double
getTotalPaidAmount(userId): Double
getTotalOutstandingAmount(userId): Double
getOutstandingAmountForProject(projectId): Double
getActiveProjectCount(userId): Int

// Update/Delete
updateProject(project: FreelanceProject)
updateProjectPayment(projectId, paidAmount)
updateProjectStatus(projectId, status)
deleteProject(project: FreelanceProject)
deleteAllProjectsForUser(userId)
```

## Usage Examples

### In ViewModels with Hilt
```kotlin
@HiltViewModel
class IncomeViewModel @Inject constructor(
    private val incomeDao: IncomeDao
) : ViewModel() {
    
    fun getMonthlyIncome(userId: String, year: Int, month: Int) {
        incomeDao.getIncomeByMonth(userId, year, month)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }
}
```

### Offline Sync Strategy
1. **Read from local database** - UI always reads from Room first
2. **Listen to Firestore** - When online, sync Firestore changes to Room
3. **Batch updates** - Use `insertIncomeList()` etc. for efficient syncing
4. **Clear on user switch** - Call `deleteAllIncomeForUser()` when switching users

### Example: Sync Income from Firestore
```kotlin
coroutineScope.launch {
    try {
        val firestoreIncomes = firebaseRepository.fetchUserIncomes(userId)
        incomeDao.insertIncomeList(firestoreIncomes)
    } catch (e: Exception) {
        // Continue using cached data from Room
        Log.e("Sync", "Failed to sync incomes", e)
    }
}
```

## Database Version & Migrations

### Current Version: 1
No migrations needed for v1 (initial release).

### Future Migrations
When adding columns or tables, create migration files:
```kotlin
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE income_entries ADD COLUMN description TEXT")
    }
}
```

Then add to DatabaseModule:
```kotlin
.addMigrations(MIGRATION_1_2)
.build()
```

## Testing

### Running Tests
All DAOs have comprehensive instrumented tests. Run with:
```bash
./gradlew connectedAndroidTest
```

### Test Coverage
- **IncomeDaoTest** - 7 tests covering insert, query, update, delete, totals, recurring
- **ExpenseDaoTest** - 9 tests covering categories, months, totals, recurring
- **GoalDaoTest** - 9 tests covering active/inactive, progress tracking, counts
- **FreelanceProjectDaoTest** - 11 tests covering status, payment tracking, client filtering

### Key Test Scenarios
✅ Insert and retrieve single/multiple entries
✅ Update entity fields
✅ Delete operations (single and batch)
✅ Query by time period (month/year)
✅ Query by category/status/client
✅ Aggregate queries (totals, counts)
✅ Reactive Flow subscriptions
✅ User isolation (data belongs to userId)

## Best Practices

### 1. Always Use Suspend Functions
```kotlin
// Correct - use in coroutine context
viewModelScope.launch {
    val income = incomeDao.getIncomeById(id)
}

// Wrong - blocking main thread
val income = incomeDao.getIncomeById(id)
```

### 2. Use Flow for UI Updates
```kotlin
// Correct - reactive updates
incomeDao.getIncomeByMonth(userId, year, month)
    .collectLatest { incomes ->
        updateUI(incomes)
    }

// Less ideal - one-time query
val incomes = incomeDao.getIncomeByMonth(userId, year, month).first()
```

### 3. Batch Operations for Performance
```kotlin
// Correct - single transaction
incomeDao.insertIncomeList(largeListOfIncomes)

// Inefficient - multiple transactions
largeListOfIncomes.forEach { incomeDao.insertIncome(it) }
```

### 4. Clear User Data on Logout
```kotlin
viewModelScope.launch {
    val userId = getCurrentUserId()
    incomeDao.deleteAllIncomeForUser(userId)
    expenseDao.deleteAllExpensesForUser(userId)
    goalDao.deleteAllGoalsForUser(userId)
    projectDao.deleteAllProjectsForUser(userId)
}
```

### 5. Use One-Time Queries Only When Necessary
```kotlin
// For aggregates that don't need updates
val totalIncome = incomeDao.getTotalIncomeForMonth(userId, 2026, 5)

// For UI that needs reactive updates
incomeDao.getIncomeByMonth(userId, 2026, 5).collect { incomes ->
    // UI updates automatically
}
```

## File Structure
```
data/
├── database/
│   ├── FinPilotDatabase.kt          # Main database class
│   ├── DatabaseModule.kt            # Hilt DI configuration
│   └── dao/
│       ├── IncomeDao.kt
│       ├── ExpenseDao.kt
│       ├── GoalDao.kt
│       └── FreelanceProjectDao.kt
├── model/
│   ├── IncomeEntry.kt
│   ├── ExpenseEntry.kt
│   ├── Goal.kt
│   ├── FreelanceProject.kt
│   └── Converters.kt
└── repository/
    └── [Repositories that use DAOs]
```

## Troubleshooting

### Database Schema Mismatch
**Error:** "column does not exist"
- **Solution:** Check entity annotations match table structure
- Run: `./gradlew clean build` to regenerate Room code

### Coroutine Context Issues
**Error:** "Not on the main thread"
- **Solution:** Ensure database operations are in `viewModelScope.launch` or background thread
- Use `withContext(Dispatchers.IO)` for explicit background execution

### Type Converter Issues
**Error:** "Cannot find TypeAdapter for type"
- **Solution:** Verify Converters.kt has @TypeConverter for custom types
- Check Timestamp and List<String> are properly converted

## Future Enhancements
- Add database backup/restore functionality
- Implement database encryption for sensitive data
- Add query performance monitoring
- Create database inspection tools for debugging
