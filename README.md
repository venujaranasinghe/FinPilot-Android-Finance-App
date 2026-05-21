# FinPilot Android Finance App

FinPilot is an Android personal finance app built with Kotlin and Jetpack Compose.
It is designed for users with mixed income sources (salary, freelance, foreign-currency income) and includes budgeting, expense tracking, goals, dashboard insights, and profile/settings management.

## 1. Product Overview

### Core capabilities
- Firebase authentication (email/password + Google sign-in)
- Profile management and display name updates
- Income entry with currency conversion support
- Expense entry with category, payment method, and recurring flag
- Goal tracking with savings logs and projection helpers
- Dashboard with charts, month-over-month comparisons, and insights
- Local Room cache for finance entities
- User settings using DataStore (theme + toggles)
- CSV export from settings

### Platforms and build targets
- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Architecture: MVVM + Repository + Hilt DI
- Min SDK: 26
- Compile SDK: 36
- Target SDK: 36
- JVM target/toolchain: 11

## 2. Tech Stack and Dependencies

### Build and plugins
- Android Gradle Plugin: 9.0.1
- Kotlin: 2.0.21
- Compose plugin: org.jetbrains.kotlin.plugin.compose
- Hilt plugin: com.google.dagger.hilt.android
- KSP: 2.0.21-1.0.27
- Google services plugin: 4.4.4

### Runtime libraries
- AndroidX Core KTX
- Lifecycle Runtime + ViewModel Compose
- Activity Compose
- Compose BOM (2024.09.00)
- Compose UI, Material3, Material icons
- Navigation Compose
- Coroutines Android
- Firebase BOM + Auth KTX + Firestore KTX
- Hilt Android + hilt-navigation-compose
- Room runtime + room-ktx + room-compiler (KSP)
- DataStore Preferences
- OkHttp 4.12.0
- Gson 2.10.1
- Google Play Services Auth
- yCharts 2.1.0

### Test libraries
- JUnit 4.13.2
- androidx.test core
- androidx.test ext junit
- Espresso core
- Room testing
- Robolectric
- kotlinx-coroutines-test

## 3. Project Structure

- app/src/main/java/com/bpeople/finpilot
  - di: Hilt providers
  - data/model: domain + persistence model classes
  - data/local: Room database, DAOs, migrations
  - data/repository: Firebase/Room/DataStore repositories
  - ui/navigation: routes and nav graph
  - ui/screens: auth, dashboard, income, expense, goal, profile, settings
  - ui/components: reusable UI components
  - ui/theme: Compose theme, colors, typography

## 4. Runtime Configuration and Secrets

### Required Firebase files
- app/google-services.json must be present and valid for your Firebase project.

### Exchange rate API key
- BuildConfig uses EXCHANGE_RATES_APP_ID from Gradle properties.
- Configure in one of:
  - gradle.properties (project)
  - ~/.gradle/gradle.properties (recommended for local secret)

Example:

```properties
EXCHANGE_RATES_APP_ID=your_openexchangerates_app_id
```

If missing, exchange-rate refresh throws an IllegalStateException with message Missing EXCHANGE_RATES_APP_ID.

## 5. App Architecture

### High-level flow
1. MainActivity loads settings from DataStore and applies theme.
2. FinPilotNavGraph starts at Splash route.
3. AuthViewModel controls authentication transitions.
4. Screen-specific ViewModels coordinate repositories.
5. Repositories talk to Firestore and local Room/DataStore.
6. UI observes StateFlow and renders Compose state.

### DI
- Hilt app class: FinPilotApplication
- AppModule provides:
  - FirebaseAuth
  - FirebaseFirestore
  - OkHttpClient
  - Gson
  - FinPilotDatabase
  - IncomeDao / ExpenseDao / GoalDao / FreelanceProjectDao

### Persistence
- Room DB name: finpilot.db
- Room version: 2
- Migration: MIGRATION_1_2 (no-op baseline)
- Downgrade behavior: fallbackToDestructiveMigrationOnDowngrade

### Settings persistence
- DataStore name: settings
- Stored keys:
  - notifications_enabled
  - dark_mode_enabled (legacy compatibility)
  - cloud_sync_enabled
  - biometrics_enabled
  - theme_mode

### Exchange-rate cache
- DataStore name: exchange_rates
- TTL: 6 hours
- OpenExchangeRates endpoint: /latest.json with app_id

## 6. Navigation Map

Defined in ui/navigation/Screen.kt and NavGraph.kt.

Routes:
- splash
- auth/login
- auth/register
- auth/forgot_password
- auth/verify_email
- dashboard
- income/add
- expense/add
- goal/{goalId}
- profile
- profile/settings

## 7. Data Model Contracts

### UserProfile
- uid
- displayName
- email
- baseCurrency (default LKR)
- createdAt

### IncomeEntry
- id
- userId
- source
- amountOriginal
- currencyOriginal
- amountLKR
- exchangeRate
- date
- label
- type
- projectRef

### ExpenseEntry
- id
- userId
- amount
- category
- subCategory
- paymentMethod
- date
- note
- isRecurring
- tags
- originalCurrency
- originalAmount

### Goal
- id
- userId
- title
- targetAmount
- currentAmount
- deadline
- monthlyRequired
- isActive

### FreelanceProject
- id
- userId
- clientName
- projectTitle
- agreedAmount
- paidAmount
- status
- entries

## 8. Firestore Layout Used by Repositories

- users/{uid}
  - income/{incomeId}
  - expenses/{expenseId}
  - goals/{goalId}
    - savingsLogs/{logId}
  - freelanceProjects/{projectId}

## 9. Build, Test, and Run

### Build debug APK
```bash
./gradlew :app:assembleDebug
```

### Run unit tests
```bash
./gradlew :app:testDebugUnitTest
```

### Run instrumentation tests (connected device/emulator)
```bash
./gradlew :app:connectedDebugAndroidTest
```

### Lint
```bash
./gradlew :app:lintDebug
```

## 10. Function Inventory (Production Code)

This section is generated from source declarations in app/src/main.
Line numbers may shift as code changes.

### Entry and DI
- MainActivity.kt
  - onCreate(savedInstanceState: Bundle?)
- di/AppModule.kt
  - provideFirebaseAuth()
  - provideFirebaseFirestore()
  - provideOkHttpClient()
  - provideGson()
  - provideDatabase(appContext: Context)
  - provideIncomeDao(db: FinPilotDatabase)
  - provideExpenseDao(db: FinPilotDatabase)
  - provideGoalDao(db: FinPilotDatabase)
  - provideProjectDao(db: FinPilotDatabase)

### Navigation and shell UI
- ui/navigation/NavGraph.kt
  - FinPilotNavGraph(navController: NavHostController = rememberNavController())
- ui/navigation/Screen.kt
  - NavRoutes.GoalTracker.createRoute(goalId: String = "default")
- ui/components/BottomNavBar.kt
  - FinPilotBottomNavBar(...)
  - NavBarItem(...)

### Theme
- ui/theme/Theme.kt
  - FinPilotTheme(...)

### Auth UI and logic
- ui/screens/auth/AuthViewModel.kt
  - getCurrentUserId()
  - onEmailChange(value: String)
  - onPasswordChange(value: String)
  - onConfirmPasswordChange(value: String)
  - onFullNameChange(value: String)
  - login()
  - signInWithGoogle(idToken: String)
  - register()
  - resendVerificationEmail()
  - forgotPassword(email: String)
  - clearResetState()
  - signOut()
  - validateLogin()
  - validateRegister()
  - clearError()
  - clearInfoMessage()
  - clearAuthSuccess()
- ui/screens/auth/LoginScreen.kt
  - LoginScreen(...)
  - validateAndLogin()
  - startGoogleSignIn(context: Context)
  - LoginScreenContent(...)
  - LoginPreview()
- ui/screens/auth/RegisterScreen.kt
  - RegisterScreen(...)
  - validateAndRegister()
  - RegisterScreenContent(...)
  - RegisterPreview()
- ui/screens/auth/ForgotPasswordScreen.kt
  - ForgotPasswordScreen(...)
  - submitReset()
  - ForgotPasswordContent(...)
  - ForgotPasswordPreview()
- ui/screens/auth/VerifyEmailScreen.kt
  - VerifyEmailScreen(...)
  - VerifyEmailPreview()
- ui/screens/auth/SplashScreen.kt
  - SplashScreen(...)
  - SplashContent(...)
  - SplashPreview()
- ui/screens/auth/AuthComponents.kt
  - AuthBackground(content: ...)
  - BlobCanvas(modifier: Modifier = Modifier)
  - AppLogo(size: Dp, modifier: Modifier = Modifier)
  - GlassCard(...)
  - AuthTextField(...)
  - GradientButton(...)
  - GoogleSignInButton(...)

### Dashboard UI and logic
- ui/screens/dashboard/DashboardViewModel.kt
  - buildRecentTransactions(incomes: List<IncomeEntry>, expenses: List<ExpenseEntry>)
  - buildIncomeBreakdown(incomes: List<IncomeEntry>)
  - buildExpensesByCategory(expenses: List<ExpenseEntry>)
  - buildMonthRanges()
  - List<IncomeEntry>.sumIncomeForRange(...)
  - List<ExpenseEntry>.sumExpenseForRange(...)
  - List<IncomeEntry>.filterIncomeByRange(...)
  - List<ExpenseEntry>.filterExpenseByRange(...)
  - percentChange(previous: Double, current: Double)
  - buildDidYouKnowInsight(...)
  - formatLkrShort(amount: Double)
- ui/screens/dashboard/DashboardScreen.kt
  - formatLKR(amount: Double)
  - formatLKRFull(amount: Double)
  - incomeSourceIcon(source: String)
  - SectionCard(...)
  - SectionHeader(title: String, subtitle: String? = null)
  - GradientProgressBar(...)
  - DashboardScreen(...)
  - QuickAddDialog(...)
  - QuickAddOption(...)
  - DashboardHeader(...)
  - HeroMetric(label: String, value: String, isPositive: Boolean)
  - SavingsRateBadge(rate: Int, color: Color)
  - InsightBanner(...)
  - MonthOverMonthContent(...)
  - MonthComparisonCard(comparison: DashboardViewModel.MonthComparison)
  - TopCategoryInsightsContent(...)
  - TopCategoryInsightCard(...)
  - DidYouKnowContent(message: String?)
  - RecentTransactionsContent(transactions: List<DashboardViewModel.RecentTransaction>)
  - RecentTransactionRow(...)
  - IncomeBreakdownContent(incomeBreakdown: Map<String, Double>)
  - IncomeSourceRow(source: String, amount: Double, percentage: Double, color: Color)
  - SpendingChartContent(expensesByCategory: Map<String, Double>)
  - GoalProgressContent(...)
  - ActiveGoalCard(...)
  - GoalMiniCard(goal: Goal)
  - GoalMetricCell(...)
  - BudgetRatioContent(...)
  - committedDiscretionaryHint(fixedCostsPercentage: Double)
  - chartColorForIndex(index: Int)
  - categoryColor(category: String)
  - LegendDot(color: Color, label: String)
  - RatioTile(...)
  - EmptyStateSection(...)
  - EmptyDataHint(message: String)
  - DashboardPreviewLight()
  - DashboardPreviewEmpty()
  - DashboardPreviewDark()

### Income UI and logic
- ui/screens/income/IncomeViewModel.kt
  - onSourceChange(value: String)
  - onAmountOriginalChange(value: String)
  - onCurrencyChange(value: String)
  - onLabelChange(value: String)
  - onDateChange(value: Long)
  - onIncomeTypeChange(value: String)
  - onProjectRefChange(value: String)
  - onHistoryDateRangeChange(value: HistoryDateRange)
  - onHistorySourceFilterChange(value: String?)
  - onHistoryIncomeTypeFilterChange(value: String?)
  - clearHistoryFilters()
  - consumeSubmitted()
  - requestSubmit()
  - confirmExchangeRate()
  - dismissRateConfirmation()
  - refreshExchangeRates()
  - addIncome()
  - calculateAmountLkr(state: IncomeUiState)
  - filterHistoryEntries(...)
  - formatRate(rate: Double)
- ui/screens/income/AddIncomeScreen.kt
  - AddIncomeScreen(...)
  - AddIncomeContent(...)
  - IncomeHistoryFilters(...)
  - IncomeFilterChip(...)
  - IncomeHistoryItem(entry: IncomeEntry)
  - SectionLabel(text: String)
  - SourceChip(...)
  - IncomeFormField(...)
  - ProjectDropdown(...)
  - AddIncomeScreenPreview()

### Expense UI and logic
- ui/screens/expense/ExpenseViewModel.kt
  - onAmountChange(value: String)
  - onCategoryChange(value: String)
  - onPaymentMethodChange(value: String)
  - onHistoryDateRangeChange(value: HistoryDateRange)
  - onHistoryCategoryFilterChange(value: String?)
  - onHistoryPaymentMethodFilterChange(value: String?)
  - clearHistoryFilters()
  - onDateChange(value: Long)
  - onNoteChange(value: String)
  - onSubCategoryChange(value: String)
  - onRecurringChange(value: Boolean)
  - consumeInsight()
  - onCurrencyChange(value: String)
  - requestSubmit()
  - confirmExchangeRate()
  - dismissRateConfirmation()
  - refreshExchangeRates()
  - addExpense()
  - buildGoalInsight(amount: Double, goal: Goal?)
  - calculateAmountLkr(state: ExpenseUiState)
  - filterHistoryEntries(...)
  - formatRate(rate: Double)
- ui/screens/expense/AddExpenseScreen.kt
  - AddExpenseScreen(...)
  - AddExpenseContent(...)
  - CategorySelector(...)
  - PaymentMethodSelector(...)
  - ExpenseHistoryFilters(...)
  - categoryIcon(category: String)
  - paymentMethodIcon(method: String)
  - SelectionPill(...)
  - ExpenseHistoryItem(entry: ExpenseEntry)

### Goal UI and logic
- ui/screens/goal/GoalViewModel.kt
  - selectGoal(index: Int)
  - onTitleChange(value: String)
  - onTargetAmountChange(value: String)
  - onCurrentAmountChange(value: String)
  - onDeadlineChange(millis: Long)
  - prepareCreateGoal()
  - prepareEditGoal(goal: Goal)
  - submitGoal()
  - logSavings(goalId: String, amount: Double)
  - withdrawSavings(goalId: String, amount: Double)
  - calculateProgress(goal: Goal?)
  - calculateMonthlyRequired(goal: Goal?)
  - calculateMonthlyRequired(targetAmount: Double, currentAmount: Double, deadlineMillis: Long?)
  - aggregateByMonth(entries: List<Pair<Long, Double>>)
- ui/screens/goal/GoalTrackerScreen.kt
  - GoalTrackerScreen(...)
  - openCreate()
  - openEdit(goal: Goal)
  - GoalTrackerScreenContent(...)
  - GoalStatusBadge(...)
  - SavingsHistoryChart(...)
  - LogSavingsSection(...)
  - CreateEditGoalBottomSheet(...)
  - LogSavingsDialog(...)
  - determineGoalStatus(...)
  - calculateProjectedCompletionDate(...)
  - formatDate(date: Date?)

### Profile and settings UI and logic
- ui/screens/profile/ProfileViewModel.kt
  - updateDisplayName(newName: String)
  - signOut()
- ui/screens/profile/ProfileScreen.kt
  - ProfileScreen(...)
  - ProfileHeader(...)
  - ProfileAccountCard(email: String?)
  - ProfileMenuCard(onNavigateToSettings: () -> Unit)
  - ProfileMenuRow(...)
  - ProfileInfoRow(...)
  - SignOutButton(onClick: () -> Unit)
  - initialsFrom(name: String?, email: String?)
  - EditNameDialog(...)
- ui/screens/profile/SettingsViewModel.kt
  - onNotificationsChange(enabled: Boolean)
  - onCloudSyncChange(enabled: Boolean)
  - onBiometricsChange(enabled: Boolean)
  - onThemeModeChange(mode: ThemeMode)
  - onChangePassword()
  - onExportData()
  - onDeleteAccount()
  - buildCsv(expenses: List<ExpenseEntry>, incomes: List<IncomeEntry>)
  - escapeCsv(value: String)
- ui/screens/profile/SettingsScreen.kt
  - SettingsScreen(...)
  - SettingsSectionCard(...)
  - SettingsRowDivider()
  - SettingsToggleRow(item: SettingToggle)
  - SettingsNavigationRow(item: SettingNavigation)
  - SettingsIcon(icon: ImageVector, isDestructive: Boolean = false)
  - ThemeModeRow(...)
  - ThemeMode.label()
  - createCsvExport(context: Context, csvContent: String)

### Repository layer
- data/repository/AuthRepository.kt
  - getCurrentUserId()
  - isLoggedIn()
  - getCurrentUserEmail()
  - login(email: String, password: String)
  - signInWithGoogle(idToken: String)
  - register(email: String, password: String, displayName: String)
  - resendVerificationEmail(email: String, password: String)
  - sendPasswordResetEmail(email: String)
  - deleteAccount()
  - signOut()
  - mapFirebaseError(e: Exception)
- data/repository/IncomeRepository.kt
  - observeIncome()
  - addIncome(entry: IncomeEntry)
  - attachListener(uid: String?)
- data/repository/ExpenseRepository.kt
  - observeExpenses()
  - addExpense(entry: ExpenseEntry)
  - attachListener(uid: String?)
- data/repository/GoalRepository.kt
  - observeGoals()
  - observeActiveGoal()
  - upsertGoal(goal: Goal)
  - logSavingsEntry(goalId: String, amount: Double)
  - observeSavingsLogs(goalId: String)
  - attachListener(uid: String?)
- data/repository/UserRepository.kt
  - getUserProfile()
  - saveUserProfile(profile: UserProfile)
  - updateDisplayName(displayName: String)
- data/repository/SettingsRepository.kt
  - setNotificationsEnabled(enabled: Boolean)
  - setCloudSyncEnabled(enabled: Boolean)
  - setBiometricsEnabled(enabled: Boolean)
  - setThemeMode(mode: ThemeMode)
- data/repository/ExchangeRatesRepository.kt
  - refreshRatesIfNeeded(force: Boolean = false)
  - rateToLkr(snapshot: ExchangeRatesSnapshot, currency: String)
  - parseRatesJson(json: String)
- data/repository/FinanceRepository.kt
  - addIncome(entry: IncomeEntry)
  - getIncomeByMonth(month: Int, year: Int)
  - addExpense(entry: ExpenseEntry)
  - getExpensesByMonth(month: Int, year: Int, category: String? = null)
  - getGoal(goalId: String)
  - updateGoalProgress(goalId: String, amount: Double)
  - addFreelanceProject(project: FreelanceProject)
  - updateProject(project: FreelanceProject)

### Local data layer
- data/local/FinPilotDatabase.kt
  - incomeDao()
  - expenseDao()
  - goalDao()
  - freelanceProjectDao()
- data/local/FinPilotDatabaseMigrations.kt
  - migrate(db: SupportSQLiteDatabase) [inside MIGRATION_1_2]
- data/local/IncomeDao.kt
  - upsert(entry: IncomeEntry)
  - upsertAll(entries: List<IncomeEntry>)
  - getByDateRange(userId: String, start: Long, end: Long)
  - deleteBetween(userId: String, start: Long, end: Long)
- data/local/ExpenseDao.kt
  - upsert(entry: ExpenseEntry)
  - upsertAll(entries: List<ExpenseEntry>)
  - getByDateRange(userId: String, start: Long, end: Long)
  - deleteBetween(userId: String, start: Long, end: Long)
  - getByCategoryAndDateRange(userId: String, category: String, start: Long, end: Long)
- data/local/GoalDao.kt
  - upsert(goal: Goal)
  - observeAll(userId: String)
  - observeById(userId: String, goalId: String)
- data/local/ProjectDao.kt
  - upsert(project: FreelanceProject)
  - upsertAll(projects: List<FreelanceProject>)
  - observeAll(userId: String)
  - observeById(userId: String, projectId: String)
- data/local/dao/IncomeDao.kt
  - insertIncome(entry: RoomIncome)
  - observeAll(userId: String)
  - getIncomeByMonth(userId: String, start: Long, end: Long)
  - deleteIncome(id: String)
  - deleteIncomeBetween(userId: String, start: Long, end: Long)
  - insertAll(entries: List<RoomIncome>)
- data/local/dao/ExpenseDao.kt
  - insertExpense(entry: RoomExpense)
  - getExpensesByMonth(userId: String, start: Long, end: Long)
  - getExpensesByCategory(userId: String, category: String, start: Long, end: Long)
  - deleteExpenseBetween(userId: String, start: Long, end: Long)
  - insertAll(entries: List<RoomExpense>)
- data/local/dao/GoalDao.kt
  - insertGoal(goal: RoomGoal)
  - getActiveGoal(userId: String)
  - updateGoalAmount(goalId: String, currentAmount: Double)
- data/local/dao/FreelanceProjectDao.kt
  - insertProject(project: RoomProject)
  - getProjectsByStatus(userId: String, status: String)

### Model conversion and helpers
- data/model/Converters.kt
  - fromStringList(list: List<String>?)
  - toStringList(json: String?)

## 11. Function Inventory (Tests)

### Unit tests
- app/src/test/java/com/bpeople/finpilot/ExampleUnitTest.kt
  - addition_isCorrect()
- app/src/test/java/com/bpeople/finpilot/data/local/FinPilotDatabaseTest.kt
  - setUp()
  - tearDown()
  - incomeDao_insertAndGetIncomeByMonth_returnsInserted()
  - expenseDao_insertAndQueryByMonthAndCategory_returnsInserted()
  - goalDao_insertGetActiveAndUpdateAmount_works()
  - projectDao_insertAndGetByStatus_returnsInserted()

### Instrumentation tests
- app/src/androidTest/java/com/bpeople/finpilot/ExampleInstrumentedTest.kt
  - useAppContext()

## 12. Operational Notes

- App theme is selected from DataStore and can be Light, Dark, or System.
- Dashboard totals use LKR for comparisons and net position.
- Exchange rates are read from OpenExchangeRates (base USD), converted via rateToLkr.
- Goal savings history query loads roughly last six months from Firestore subcollection.
- Settings CSV export relies on FileProvider declared in AndroidManifest.

## 13. Known Implementation Caveats

- FinanceRepository currently uses GlobalScope in some cache sync paths.
- Some older local DAO interfaces and newer Room entity DAOs coexist in the project.
- Firestore profile creation in registration swallows internal exception for profile write.

## 14. Recommended Next Improvements

- Consolidate duplicate DAO layers and remove unused legacy APIs.
- Replace GlobalScope with structured coroutine scope.
- Add repository-level tests for auth and exchange-rates behavior.
- Add CI workflow for build + unit test + lint.
