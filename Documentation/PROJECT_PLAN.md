# FinPilot — Complete Project Plan
**SE3092 | Platform Based Development — Assignment 01**
**Deadline: 24 May 2026 | Current Date: 22 April 2026 | Time Remaining: ~32 days**

---

## Table of Contents
1. [Assignment Overview & Key Constraints](#1-assignment-overview--key-constraints)
2. [Problem Analysis — Kavindu's Breakdown](#2-problem-analysis--kavindu's-breakdown)
3. [Derived Requirements](#3-derived-requirements)
4. [System Architecture](#4-system-architecture)
5. [Firebase Firestore Schema](#5-firebase-firestore-schema)
6. [Screen Inventory & Navigation Graph](#6-screen-inventory--navigation-graph)
7. [MVVM Layer Breakdown](#7-mvvm-layer-breakdown)
8. [Week-by-Week Execution Plan](#8-week-by-week-execution-plan)
9. [Feature Implementation Checklist](#9-feature-implementation-checklist)
10. [Evaluation Criterion Coverage](#10-evaluation-criterion-coverage)
11. [Deliverables Checklist](#11-deliverables-checklist)
12. [Risk Register](#12-risk-register)

---

## 1. Assignment Overview & Key Constraints

| Property | Value |
|---|---|
| App Type | Android — Personal Finance Management |
| Language | Kotlin 100% (no Java in app layer, Kotlin DSL Gradle) |
| UI | Jetpack Compose + Material Design 3 (no XML layouts) |
| Architecture | MVVM — strict layer separation |
| Auth | Firebase Authentication (email/password mandatory; Google Sign-In = bonus) |
| Database | Firebase Firestore (real-time listeners, security rules required) |
| Local Cache | Room (strongly recommended for offline persistence) |
| DI | Hilt (preferred) or Koin |
| State | StateFlow / LiveData — no raw mutable state exposed to UI |
| Async | Kotlin Coroutines — viewModelScope only |
| Navigation | Navigation Compose — single-activity, typed arguments |
| Min SDK | API 26 (Android 8.0 Oreo) |
| Target SDK | 34+ |
| Submission | LMS upload + live demo session |

---

## 2. Problem Analysis — Kavindu's Breakdown

### 2.1 Root Causes of All Four Previous Tool Failures

| Tool Kavindu Tried | Why It Failed |
|---|---|
| Google Sheet | Manually entered formulas broke on row inserts; data entry overhead too high |
| Play Store budget app | USD only; no cloud sync; assumed single income source + fixed monthly budget |
| Notes app (bank exports) | Became unreadable in 4 days; zero structure or categorisation |
| Bank's native summary | Card transactions only; omitted cash + secondary accounts; miscategorised spending |

**Shared underlying failure**: Every tool was either too rigid, too manual, too narrow in data model, or too unpleasant to open daily. None handled irregular multi-source income.

### 2.2 Income Structure Analysis

| Source | Currency | Frequency | Predictability |
|---|---|---|---|
| Salary (startup) | LKR | Monthly (25th) | Medium — varies LKR 120k–140k |
| Freelance projects | LKR | Irregular (milestone-based) | Low — LKR 20k–90k per project |
| Google AdSense | USD → LKR | Monthly | Low — varies LKR 3.5k–19k equivalent |
| Crypto trading (Binance) | USDT/ETH/altcoins | Irregular | Very Low — gains and losses |

**Key design insight**: The system must treat each income source as a typed entity with its own currency, cycle, and certainty level. A single `amount` field per record is insufficient — the system needs `originalCurrency`, `originalAmount`, `lkrEquivalent`, `exchangeRate`, and `source` type.

### 2.3 Savings Goal Analysis

- Target: LKR 490,000 (MacBook Pro M4)
- Deadline: 12 months from app adoption
- Current savings: LKR 11,200
- Required monthly savings to reach goal: **(490,000 − 11,200) ÷ 12 = LKR 39,900/month**
- The app must surface this number and track daily progress toward it

### 2.4 Expense Invisibility Analysis

- Spending fragmented across: bank card, cash, PickMe, UberEats, auto-debit (secondary account), subscriptions
- No category system exists
- He discovered LKR 24,000/month on coffee/dining only by reviewing 3 months of bank statements
- He does not understand his discretionary vs. committed spending split
- **Design insight**: expense entry must be fast (< 10 seconds), category-tagged at input time, and the dashboard must show discretionary vs. committed split as the primary KPI

---

## 3. Derived Requirements

### 3.1 Functional Requirements

#### FR-01: Multi-Source Income Tracking
- User can log income from 4 named source types: Salary, Freelance, AdSense, Crypto
- Each income entry stores: source type, original currency (LKR/USD/USDT/ETH), original amount, LKR equivalent, exchange rate used, date, notes
- Freelance entries support project name and milestone tagging
- System computes and displays total monthly income in LKR normalised across all sources

#### FR-02: Expense Logging with Minimal Friction
- Quick-add expense flow completable in < 3 taps
- Mandatory fields: amount (LKR), category, date (defaults to today)
- Optional fields: notes, payment method (card/cash/UberEats/PickMe/auto-debit)
- Pre-defined category list: Housing, Food & Dining, Transport, Subscriptions, Gym, Entertainment, Freelance Tools, Savings, Other
- Category is selected from chips, not typed

#### FR-03: Spending Category Visualisation
- Pie/donut chart showing spending distribution by category for current month
- Stacked bar chart for committed (rent, subscriptions, gym) vs. discretionary spending
- Monthly trend line chart (last 6 months of total spend)
- All charts update in real-time via Firestore listeners

#### FR-04: Savings Goal Tracker
- User defines a goal: name, target amount (LKR), target date
- System calculates: required monthly savings rate, current savings buffer, on-track/off-track status
- Progress bar on dashboard shows % of goal reached
- Dashboard shows a "Days remaining" and "Monthly savings needed" widget
- Visual alert when user is spending at a rate that will miss the goal

#### FR-05: Dashboard — Financial Clarity Screen
- Monthly income (actual received, not estimated) vs. monthly expenses
- Current net position (income − expenses for this month)
- Goal progress widget
- Discretionary vs. committed spending ratio
- Recent transactions list (last 5)
- All data loads from Firestore with real-time listeners

#### FR-06: Monthly Summary & Insights
- Month-over-month expense comparison
- Top 3 spending categories surfaced as insight cards
- Income vs. expense trend over rolling 6 months
- "Did you know" style insight: e.g., "You spent LKR 18,000 on food this month — 12% more than last month"

#### FR-07: Firebase Authentication
- Email/password registration and sign-in (mandatory)
- Google Sign-In (bonus credit)
- All user data is scoped to authenticated UID in Firestore
- Session persistence across app restarts

#### FR-08: Offline Persistence
- Room database mirrors Firestore data locally
- App is fully functional for read operations when offline
- Write operations queue and sync when connectivity is restored
- No blank screens or crashes when offline

#### FR-09: Currency Handling
- User can input USD/USDT amounts; app prompts for exchange rate at time of entry
- Alternatively, a stored reference rate (updatable manually) converts foreign currency amounts to LKR
- All dashboard totals are displayed in LKR only

#### FR-10: Recurring Expense Awareness
- User can flag an expense as recurring (monthly)
- Dashboard shows a "committed monthly outgoings" total derived from flagged recurring entries
- System warns if recurring commitments exceed a user-defined threshold

### 3.2 Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR-01 | App must install and run on Android 8.0 (API 26) and above |
| NFR-02 | No unhandled exceptions; all network failures show a user-facing error state |
| NFR-03 | No blank screens on empty datasets — onboarding state with guidance text |
| NFR-04 | Firestore Security Rules must prevent cross-user data access |
| NFR-05 | MVVM layer separation strictly enforced — no business logic in Composables |
| NFR-06 | No mutable state exposed directly from ViewModel to UI |
| NFR-07 | All async operations run in viewModelScope using Kotlin Coroutines |
| NFR-08 | UI response time < 300ms for local cached reads |
| NFR-09 | App size < 50 MB |
| NFR-10 | Material Design 3 theming applied consistently throughout |

### 3.3 Constraints

- No Java files in the application layer
- No XML layouts
- No manual ViewModel instantiation inside Composables
- APK must connect to a live Firebase project (not local emulator)
- Single-activity architecture with Navigation Compose

---

## 4. System Architecture

### 4.1 MVVM Layer Map

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer                         │
│  Composable Screens (observe StateFlow from VM)     │
│  AuthScreen │ Dashboard │ Income │ Expense │ Goals  │
│  Reports    │ Settings  │ Onboarding                │
└──────────────────────┬──────────────────────────────┘
                       │ collect() / observeAsState()
┌──────────────────────▼──────────────────────────────┐
│                 ViewModel Layer                      │
│  AuthViewModel │ DashboardViewModel                 │
│  IncomeViewModel │ ExpenseViewModel                 │
│  GoalViewModel │ ReportsViewModel                   │
│  (StateFlow<UiState> │ viewModelScope │ Hilt @HiltVM)│
└──────────────────────┬──────────────────────────────┘
                       │ suspend functions / Flow
┌──────────────────────▼──────────────────────────────┐
│               Repository Layer                       │
│  AuthRepository │ IncomeRepository                  │
│  ExpenseRepository │ GoalRepository                 │
│  UserRepository                                     │
│  (Coordinates Firestore ↔ Room │ exposed as Flow)   │
└──────┬───────────────────────────────┬──────────────┘
       │                               │
┌──────▼──────┐               ┌────────▼───────┐
│  Remote     │               │  Local Cache   │
│  Firebase   │               │  Room Database │
│  Firestore  │               │  (Entity DAOs) │
│  Firebase   │               └────────────────┘
│  Auth       │
└─────────────┘
```

### 4.2 Package Structure

```
app/
└── src/main/java/com/finpilot/
    ├── di/                        # Hilt modules
    │   ├── AppModule.kt
    │   ├── FirebaseModule.kt
    │   └── DatabaseModule.kt
    ├── data/
    │   ├── local/
    │   │   ├── db/AppDatabase.kt
    │   │   ├── dao/IncomeDao.kt
    │   │   ├── dao/ExpenseDao.kt
    │   │   ├── dao/GoalDao.kt
    │   │   └── entity/          # Room entities
    │   ├── remote/
    │   │   ├── FirestoreIncomeSource.kt
    │   │   ├── FirestoreExpenseSource.kt
    │   │   └── FirestoreGoalSource.kt
    │   ├── repository/
    │   │   ├── AuthRepository.kt
    │   │   ├── IncomeRepository.kt
    │   │   ├── ExpenseRepository.kt
    │   │   └── GoalRepository.kt
    │   └── model/               # Domain models (pure Kotlin data classes)
    │       ├── Income.kt
    │       ├── Expense.kt
    │       ├── SavingsGoal.kt
    │       └── User.kt
    ├── domain/
    │   └── usecase/             # Optional: use cases for complex business logic
    ├── ui/
    │   ├── navigation/
    │   │   ├── NavGraph.kt
    │   │   └── Screen.kt        # sealed class Screen
    │   ├── theme/
    │   │   ├── Theme.kt
    │   │   ├── Color.kt
    │   │   └── Type.kt
    │   ├── screens/
    │   │   ├── auth/
    │   │   │   ├── LoginScreen.kt
    │   │   │   └── RegisterScreen.kt
    │   │   ├── onboarding/
    │   │   │   └── OnboardingScreen.kt
    │   │   ├── dashboard/
    │   │   │   └── DashboardScreen.kt
    │   │   ├── income/
    │   │   │   ├── IncomeListScreen.kt
    │   │   │   └── AddIncomeScreen.kt
    │   │   ├── expense/
    │   │   │   ├── ExpenseListScreen.kt
    │   │   │   └── AddExpenseScreen.kt
    │   │   ├── goals/
    │   │   │   ├── GoalScreen.kt
    │   │   │   └── AddGoalScreen.kt
    │   │   ├── reports/
    │   │   │   └── ReportsScreen.kt
    │   │   └── settings/
    │   │       └── SettingsScreen.kt
    │   └── viewmodel/
    │       ├── AuthViewModel.kt
    │       ├── DashboardViewModel.kt
    │       ├── IncomeViewModel.kt
    │       ├── ExpenseViewModel.kt
    │       ├── GoalViewModel.kt
    │       └── ReportsViewModel.kt
    └── util/
        ├── CurrencyConverter.kt
        ├── DateUtils.kt
        └── Extensions.kt
```

---

## 5. Firebase Firestore Schema

### 5.1 Collection Structure

```
users/                                  ← root collection
  {uid}/                                ← document per authenticated user
    email: String
    displayName: String
    createdAt: Timestamp
    defaultCurrency: "LKR"
    referenceRates: {                   ← manually updated exchange rates
      USD_LKR: Number,
      USDT_LKR: Number,
      ETH_LKR: Number
    }

    income/                             ← sub-collection
      {incomeId}/
        sourceType: String              ← "SALARY" | "FREELANCE" | "ADSENSE" | "CRYPTO"
        originalCurrency: String        ← "LKR" | "USD" | "USDT" | "ETH"
        originalAmount: Number
        lkrEquivalent: Number           ← always stored; computed at entry time
        exchangeRateUsed: Number        ← 1.0 for LKR, actual rate for foreign
        date: Timestamp
        month: String                   ← "2026-04" for easy monthly queries
        projectName: String?            ← freelance only
        milestone: String?              ← freelance only
        notes: String?
        createdAt: Timestamp

    expenses/                           ← sub-collection
      {expenseId}/
        amount: Number                  ← always LKR
        category: String               ← see category enum below
        isRecurring: Boolean
        paymentMethod: String          ← "CARD" | "CASH" | "PICKME" | "UBEREATS" | "AUTO_DEBIT"
        date: Timestamp
        month: String                  ← "2026-04"
        notes: String?
        createdAt: Timestamp

    goals/                             ← sub-collection
      {goalId}/
        name: String                   ← "MacBook Pro M4"
        targetAmount: Number           ← 490000
        currentSaved: Number           ← updated when user logs savings
        targetDate: Timestamp
        createdAt: Timestamp
        isActive: Boolean
```

### 5.2 Expense Categories (Enum)

```
HOUSING          ← rent, utilities
FOOD_DINING      ← restaurants, UberEats, coffee
TRANSPORT        ← PickMe, Uber, fuel
SUBSCRIPTIONS    ← Netflix, Spotify, etc.
HEALTH_FITNESS   ← gym, pharmacy
ENTERTAINMENT    ← games, events
FREELANCE_TOOLS  ← domains, hosting, software
SAVINGS          ← intentional savings transfer
OTHER
```

### 5.3 Indexing Strategy

Firestore composite indexes required:
- `income` → `(month ASC, date DESC)` — for monthly income queries
- `income` → `(sourceType ASC, month ASC)` — for per-source monthly totals
- `expenses` → `(month ASC, date DESC)` — for monthly expense queries
- `expenses` → `(category ASC, month ASC)` — for category breakdown
- `expenses` → `(isRecurring ASC, month ASC)` — for recurring totals

### 5.4 Firestore Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;

      match /income/{incomeId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      match /expenses/{expenseId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      match /goals/{goalId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```

---

## 6. Screen Inventory & Navigation Graph

### 6.1 All Screens

| Screen | Route | Purpose |
|---|---|---|
| SplashScreen | `splash` | Auth state check, route to Login or Dashboard |
| OnboardingScreen | `onboarding` | First-launch only; sets goal and reference rates |
| LoginScreen | `auth/login` | Email/password sign-in |
| RegisterScreen | `auth/register` | Account creation |
| DashboardScreen | `dashboard` | Home — income vs expense, goal widget, recent txns |
| IncomeListScreen | `income/list` | All income entries, filterable by month |
| AddIncomeScreen | `income/add` | Quick-add income form |
| ExpenseListScreen | `expense/list` | All expenses, filterable by month and category |
| AddExpenseScreen | `expense/add` | Quick-add expense form (< 3 taps) |
| GoalScreen | `goal` | Savings goal detail — progress, timeline, required rate |
| AddGoalScreen | `goal/add` | Create a new savings goal |
| ReportsScreen | `reports` | Charts: category pie, discretionary vs committed bar, trend |
| SettingsScreen | `settings` | Currency rates, profile, sign out |

### 6.2 Navigation Flow

```
App Launch
    │
    ▼
SplashScreen
    ├── Not authenticated ──► LoginScreen ──► RegisterScreen
    │                              │
    │                              ▼
    └── Authenticated ────► OnboardingScreen (first launch only)
                                   │
                                   ▼
                            DashboardScreen  ◄────────────────────┐
                            (Bottom Nav Hub)                      │
                            ├── Income Tab ──► IncomeListScreen   │
                            │                      └── AddIncomeScreen
                            ├── Expense Tab ─► ExpenseListScreen  │
                            │                      └── AddExpenseScreen
                            ├── Goal Tab ───► GoalScreen          │
                            │                      └── AddGoalScreen
                            └── Reports Tab ─► ReportsScreen      │
                                                                   │
                            FAB (quick add expense) ──────────────┘
```

---

## 7. MVVM Layer Breakdown

### 7.1 ViewModels and Their Responsibilities

#### AuthViewModel
- States: `LoginUiState`, `RegisterUiState` (loading, success, error)
- Actions: `login(email, password)`, `register(email, password, name)`, `signOut()`
- Observes: Firebase Auth state via `authStateFlow`

#### DashboardViewModel
- States: `DashboardUiState` containing:
  - `monthlyIncome: Double` (LKR total)
  - `monthlyExpense: Double` (LKR total)
  - `netPosition: Double`
  - `goalProgress: GoalProgressModel`
  - `recentTransactions: List<TransactionItem>`
  - `committedTotal: Double`
  - `discretionaryTotal: Double`
- Collects real-time Firestore snapshots via Repository Flows
- Calculates goal status on state update

#### IncomeViewModel
- States: `IncomeListUiState`, `AddIncomeUiState`
- Actions: `addIncome(IncomeFormData)`, `deleteIncome(id)`, `filterByMonth(yearMonth)`
- Handles currency conversion before saving (calls CurrencyConverter util)

#### ExpenseViewModel
- States: `ExpenseListUiState`, `AddExpenseUiState`
- Actions: `addExpense(ExpenseFormData)`, `deleteExpense(id)`, `filterByCategory(category)`, `filterByMonth(yearMonth)`

#### GoalViewModel
- States: `GoalUiState`
- Computed fields: `monthsRemaining`, `requiredMonthlySavings`, `onTrack: Boolean`
- Actions: `createGoal(GoalFormData)`, `updateSavedAmount(amount)`

#### ReportsViewModel
- States: `ReportsUiState` containing chart data models
- Aggregates: `categoryBreakdown: Map<String, Double>`, `monthlyTrend: List<MonthlyTotal>`, `committedVsDiscretionary: Pair<Double, Double>`

---

## 8. Week-by-Week Execution Plan

### Week 1 (Apr 22 – Apr 28): Foundation & Design

**Day 1–2 (Apr 22–23): Analysis & Documentation**
- [ ] Write Problem Analysis section of Technical Design Document
- [ ] Derive and document all Functional Requirements (FR-01 to FR-10)
- [ ] Derive Non-Functional Requirements and Constraints
- [ ] Complete Firestore schema in documentation

**Day 3–4 (Apr 24–25): Architecture Diagrams & Wireframes**
- [ ] Draw MVVM architecture diagram (use draw.io or Figma)
- [ ] Create wireframes for minimum 3 critical screens:
  1. Dashboard Screen (most important — Kavindu's daily view)
  2. Add Expense Screen (friction must be eliminated)
  3. Goal Tracker Screen (MacBook Fund progress)
- [ ] Create wireframes for additional screens: Login, Add Income, Reports
- [ ] Export wireframes to PNG and add to Documentation folder

**Day 5–7 (Apr 26–28): Android Project Setup**
- [ ] Create new Android project in Android Studio (Kotlin DSL, Jetpack Compose)
- [ ] Set `minSdk = 26`, `targetSdk = 34`
- [ ] Configure `build.gradle.kts` with all dependencies:
  - Hilt
  - Firebase (Auth, Firestore)
  - Room
  - Navigation Compose
  - Kotlin Coroutines
  - Compose Material 3
  - MPAndroidChart or Vico (charts)
- [ ] Create Firebase project on console.firebase.google.com
- [ ] Download `google-services.json` and add to `app/`
- [ ] Enable Firebase Authentication (email/password)
- [ ] Enable Firestore, set to production mode
- [ ] Implement Hilt application class and base modules
- [ ] Create full package structure (as defined in §4.2)

---

### Week 2 (Apr 29 – May 5): Auth + Data Layer

**Day 8–10: Firebase Auth + Repository Layer**
- [ ] Implement `AuthRepository` (register, login, signOut, authState flow)
- [ ] Implement `AuthViewModel` with `StateFlow<AuthUiState>`
- [ ] Implement `LoginScreen` composable — email/password fields, error states
- [ ] Implement `RegisterScreen` composable
- [ ] Implement `SplashScreen` that routes based on auth state
- [ ] Test auth flows end-to-end on emulator

**Day 11–12: Data Models + Room**
- [ ] Define domain models: `Income`, `Expense`, `SavingsGoal`, `User`
- [ ] Define Room entities mirroring domain models
- [ ] Implement `AppDatabase` with all DAOs
- [ ] Implement `IncomeDao`, `ExpenseDao`, `GoalDao` with relevant queries
- [ ] Write Hilt `DatabaseModule`

**Day 13–14: Firestore Data Sources**
- [ ] Implement `FirestoreIncomeSource` — CRUD + Flow of monthly income
- [ ] Implement `FirestoreExpenseSource` — CRUD + Flow of monthly expenses
- [ ] Implement `FirestoreGoalSource` — CRUD + Flow
- [ ] Implement `UserRepository` — creates user document on first login, stores reference rates
- [ ] Deploy Firestore Security Rules

---

### Week 3 (May 6 – May 12): Core Screens Implementation

**Day 15–17: Income Module**
- [ ] Implement `IncomeRepository` (Firestore → Room sync via Flow)
- [ ] Implement `IncomeViewModel`
- [ ] Implement `AddIncomeScreen`:
  - Source type selector (Salary / Freelance / AdSense / Crypto) as segmented chips
  - Currency selector (LKR / USD / USDT / ETH)
  - Amount field + exchange rate field (shown only for non-LKR)
  - Date picker
  - Project name field (shown only for Freelance)
  - Notes field (optional)
  - Save button → validates → calls ViewModel
- [ ] Implement `IncomeListScreen` with month filter

**Day 18–20: Expense Module**
- [ ] Implement `ExpenseRepository`
- [ ] Implement `ExpenseViewModel`
- [ ] Implement `AddExpenseScreen` (must be < 3 taps to complete):
  - Amount field (opens numeric keyboard immediately)
  - Category chips (visible immediately, no dropdown)
  - Payment method chips
  - Date (defaults to today, expandable)
  - Recurring toggle
  - Notes (collapsed by default)
- [ ] Implement `ExpenseListScreen` with month + category filters
- [ ] Add Floating Action Button on Dashboard → navigates to AddExpenseScreen

**Day 21: Goal Module**
- [ ] Implement `GoalRepository`
- [ ] Implement `GoalViewModel` with computed fields (months remaining, required monthly savings)
- [ ] Implement `AddGoalScreen`
- [ ] Implement `GoalScreen` with progress bar and analytics

---

### Week 4 (May 13 – May 19): Dashboard + Reports + Polish

**Day 22–23: Dashboard Screen**
- [ ] Implement `DashboardViewModel` — collect from Income + Expense + Goal repos in parallel
- [ ] Implement `DashboardScreen`:
  - Monthly income total (LKR, real-time)
  - Monthly expense total (LKR, real-time)
  - Net position card (income − expenses, colour-coded)
  - Goal progress bar with percentage and "LKR X remaining" and "Save LKR Y/month"
  - Committed vs. discretionary spending split (two metric cards)
  - Recent transactions list (last 5, income and expenses merged)
- [ ] Implement bottom navigation bar (Dashboard / Income / Expenses / Reports)

**Day 24–25: Reports Screen**
- [ ] Implement `ReportsViewModel` with aggregation logic
- [ ] Implement `ReportsScreen`:
  - Donut chart — expense category breakdown (current month)
  - Bar chart — committed vs. discretionary spending
  - Line chart — monthly total income & expense trend (last 6 months)
  - Insight cards — top 3 categories, month-over-month change
- [ ] Add month selector to Reports

**Day 26–27: Settings + Onboarding**
- [ ] Implement `OnboardingScreen` (shown on first login only):
  - Step 1: Set savings goal (name, target, date)
  - Step 2: Set initial reference rates (USD/USDT/ETH to LKR)
- [ ] Implement `SettingsScreen`:
  - Update reference exchange rates
  - Profile info
  - Sign out

**Day 28: Error Handling & Empty States**
- [ ] Every screen must handle: loading state, error state (with retry), empty state (with CTA)
- [ ] Implement network connectivity check — show offline banner
- [ ] Verify no crashes on empty datasets

---

### Week 5 (May 20 – May 24): Testing, Documentation & Submission

**Day 29–30 (May 20–21): Testing & Stability**
- [ ] Test entire app on Android 8.0 emulator (API 26)
- [ ] Test on Android 12 / 13 physical device or emulator
- [ ] Verify all Firestore Security Rules work correctly
- [ ] Test offline behaviour — kill network, verify Room cache serves data
- [ ] Test auth edge cases: wrong password, duplicate email, session expiry
- [ ] Fix any crashes or visual regressions found

**Day 31 (May 22): Technical Design Document — Final**
- [ ] Section 1: Introduction & Project Overview
- [ ] Section 2: Problem Analysis (Kavindu scenario, root cause analysis, requirement derivation)
- [ ] Section 3: Functional Requirements (FR-01 to FR-10 table)
- [ ] Section 4: Non-Functional Requirements & Constraints
- [ ] Section 5: Firebase Firestore Schema (with justification)
- [ ] Section 6: MVVM Architecture Diagram (exported from draw.io)
- [ ] Section 7: Navigation Graph Diagram
- [ ] Section 8: Annotated Wireframes (min 3, with callouts explaining design decisions)
- [ ] Section 9: Design Decision Justifications (why each major choice addresses Kavindu's failures)
- [ ] Section 10: Firestore Security Rules (full listing)
- [ ] Export to PDF, verify minimum 15 pages

**Day 32 (May 23): APK + README + Final Commit**
- [ ] Update `README.md`:
  - Project overview
  - Firebase configuration steps
  - Build instructions
  - Architectural summary
- [ ] Build signed debug APK connected to live Firebase
- [ ] Test APK installation on clean device/emulator
- [ ] Final commit — verify commit history shows iterative development
- [ ] Push to GitHub

**Day 33 (May 24): Submission**
- [ ] Upload to LMS: GitHub repo link + APK + Technical Design Document PDF
- [ ] Prepare for live demo:
  - Demo script: onboarding → add income (4 sources) → add expenses → view dashboard → view reports → show goal progress
  - Be ready to explain: ViewModel StateFlow design, Firestore schema choices, how architecture addresses Kavindu's failures

---

## 9. Feature Implementation Checklist

### Must-Have (Core Problem Coverage)
- [ ] Multi-source income logging (Salary, Freelance, AdSense, Crypto)
- [ ] Multi-currency input with LKR normalisation
- [ ] Expense logging with category tags
- [ ] Dashboard: income vs. expense vs. net
- [ ] Dashboard: goal progress widget
- [ ] Dashboard: committed vs. discretionary split
- [ ] Reports: category donut chart
- [ ] Savings goal with computed required monthly savings
- [ ] Firebase Auth (email/password)
- [ ] Firestore real-time listeners on dashboard
- [ ] Room offline cache
- [ ] Error states on all screens
- [ ] Empty states with guidance on all screens

### Should-Have (Quality Differentiators)
- [ ] Onboarding flow (goal + exchange rates setup on first login)
- [ ] Month selector on income/expense list screens
- [ ] 6-month trend chart in Reports
- [ ] Insight cards ("you spent 12% more on food this month")
- [ ] Recurring expense toggle + committed outgoings total
- [ ] Freelance project name and milestone fields
- [ ] Quick add expense via FAB from Dashboard

### Bonus / Nice-to-Have
- [ ] Google Sign-In (explicit bonus credit in assignment)
- [ ] Goal "on-track / off-track" banner on Dashboard
- [ ] Push notification: "You haven't logged an expense today"
- [ ] Currency rate auto-fetch via exchange rate API

---

## 10. Evaluation Criterion Coverage

| Criterion | Weight | How This Plan Addresses It |
|---|---|---|
| Problem Analysis & Requirement Extraction | 20% | §2 documents root causes of all 4 failures, income structure analysis, and 10 FRs + NFRs derived directly from the scenario |
| Architecture & Code Quality | 20% | Strict MVVM (§4), StateFlow, Hilt DI, Coroutines in viewModelScope, no business logic in Composables |
| Jetpack Compose UI & UX Design | 15% | Material 3, < 3 tap expense entry, category chips (no dropdowns), bottom nav, annotated wireframes |
| Firebase Integration | 15% | Auth (email/password + Google bonus), Firestore with real-time listeners, composite indexes, Security Rules, Room offline cache |
| Feature Completeness & Innovation | 10% | All core FRs covered + insight cards + recurring expense tracking + goal "on-track" status |
| Technical Documentation | 10% | Min 15-page PDF with all required artefacts, scenario-specific content throughout |
| Demo & Presentation | 10% | §8 Week 5 includes demo script and architecture explanation prep |

---

## 11. Deliverables Checklist

- [ ] **Source Code Repository** — private GitHub repo, iterative commit history, `README.md` complete
- [ ] **Firestore Security Rules** — deployed to live Firebase project, also included in documentation
- [ ] **Technical Design Document** — PDF, min 15 pages, contains all required artefacts
- [ ] **APK File** — debug-signed, connects to live Firebase, tested on API 26+
- [ ] **LMS Submission** — repo link + APK + PDF uploaded before 24 May 2026
- [ ] **Live Demo** — scheduled during demo week, runs without errors, team prepared to explain architecture

---

## 12. Risk Register

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| Firebase quota exceeded during development | Low | Medium | Use a test UID with minimal writes during development |
| MPAndroidChart / Vico incompatibility with target SDK | Medium | Medium | Prototype charts in Week 1 spike; have fallback (Canvas-based chart) |
| Firestore Security Rules blocking queries | Medium | High | Test rules in Firebase Console Rules Playground before deployment |
| App crashes on API 26 due to newer API usage | Medium | High | Test on API 26 emulator from Week 2 onwards, not just at the end |
| Exchange rate API rate limiting (if used) | Low | Low | Manual rate entry is always the fallback |
| Insufficient commit history for academic integrity | Low | High | Commit after every meaningful unit of work — minimum 1 commit/day |
| Technical Design Document under 15 pages | Medium | Medium | Start documentation in Week 1, not Week 5 |

---

## Appendix A: Gradle Dependencies Reference

```kotlin
// build.gradle.kts (app)
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    kapt("com.google.dagger:hilt-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Charts (Vico — Compose-native)
    implementation("com.patrykandpatrick.vico:compose-m3:1.15.0")

    // DataStore (for onboarding flag)
    implementation("androidx.datastore:datastore-preferences:1.0.0")
}
```

---

## Appendix B: Key Design Decisions Justified Against Kavindu's Failures

| Design Decision | Failure It Prevents |
|---|---|
| Category chips instead of text input or dropdowns | Eliminates the friction of typing; mirrors the < 3 tap goal for expense logging |
| Income stored with `lkrEquivalent` pre-computed | Prevents the "4 apps open to calculate income" problem; dashboard always shows one accurate total |
| `month` field on every income/expense document | Enables efficient Firestore monthly queries without full collection scans |
| Real-time Firestore listeners on Dashboard | Ensures data is always current without a manual refresh — removes the "stale data" friction |
| Room local cache as primary read source | Dashboard loads instantly from cache even on slow networks, preventing the blank-screen abandonment cycle |
| Goal progress computed in ViewModel, displayed on Dashboard | Makes the MacBook goal financially visible every time the app is opened — not abstract |
| Onboarding captures goal and exchange rates on first login | Ensures the app is personalised from launch; no "fill in everything before it's useful" barrier |
| Recurring expense flag + committed outgoings total | Gives Kavindu the committed vs. discretionary split he has never seen, within the first session |
