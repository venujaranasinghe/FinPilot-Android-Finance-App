package com.bpeople.finpilot.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.model.ThemeMode
import com.bpeople.finpilot.data.model.UserProfile
import com.bpeople.finpilot.data.repository.AuthRepository
import com.bpeople.finpilot.data.repository.ExpenseRepository
import com.bpeople.finpilot.data.repository.GoalRepository
import com.bpeople.finpilot.data.repository.IncomeRepository
import com.bpeople.finpilot.data.repository.SettingsRepository
import com.bpeople.finpilot.data.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class IncomeSource(
    val id: String,
    val label: String,
    val icon: String,        // emoji icon key
    val isActive: Boolean,
)

data class Achievement(
    val id: String,
    val label: String,
    val description: String,
    val icon: String,        // emoji
    val unlocked: Boolean,
)

data class ProfileUiState(
    // snapshot
    val thisMonthIncome: Double = 0.0,
    val thisMonthExpenses: Double = 0.0,
    val activeGoal: Goal? = null,
    val goalProgressPercent: Float = 0f,
    val incomeVsLastMonth: Double = 0.0,   // positive = up
    val expenseVsLastMonth: Double = 0.0,  // positive = up (bad)
    // currency
    val usdEnabled: Boolean = true,
    val usdtEnabled: Boolean = false,
    val autoConvert: Boolean = true,
    val rateLastUpdated: String = "Updated 2h ago",
    // income sources
    val incomeSources: List<IncomeSource> = defaultIncomeSources(),
    // notifications
    val notifySalaryReminder: Boolean = true,
    val notifyWeeklySummary: Boolean = true,
    val notifyGoalMilestone: Boolean = true,
    val notifyBudgetOverspend: Boolean = true,
    val budgetOverspendThreshold: String = "10000",
    // dark mode
    val darkModeEnabled: Boolean = false,
    // achievements
    val achievements: List<Achievement> = emptyList(),
    // computed financial health score 0-100
    val healthScore: Int = 0,
) {
    companion object {
        fun defaultIncomeSources() = listOf(
            IncomeSource("salary", "Salary", "💼", true),
            IncomeSource("freelance", "Freelance", "💻", true),
            IncomeSource("adsense", "AdSense", "📢", false),
            IncomeSource("crypto", "Crypto", "₿", false),
        )
    }
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val currentUser: StateFlow<FirebaseUser?> = authRepository.currentUser
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val userProfile: StateFlow<UserProfile?> = userRepository.getUserProfile()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    darkModeEnabled = prefs.themeMode == ThemeMode.DARK
                )
            }
        }

        viewModelScope.launch {
            userRepository.observeIncomeSources().collect { maps ->
                if (maps.isNotEmpty()) {
                    val sources = maps.map { m ->
                        IncomeSource(
                            id = m["id"] as? String ?: "",
                            label = m["label"] as? String ?: "",
                            icon = m["icon"] as? String ?: "💰",
                            isActive = m["isActive"] as? Boolean ?: true,
                        )
                    }.filter { it.id.isNotBlank() }
                    _uiState.value = _uiState.value.copy(incomeSources = sources)
                }
            }
        }

        viewModelScope.launch {
            combine(
                incomeRepository.observeIncome(),
                expenseRepository.observeExpenses(),
                goalRepository.observeGoals(),
            ) { incomes, expenses, goals ->
                val cal = Calendar.getInstance()
                val thisMonth = cal.get(Calendar.MONTH)
                val thisYear = cal.get(Calendar.YEAR)
                cal.add(Calendar.MONTH, -1)
                val prevMonth = cal.get(Calendar.MONTH)
                val prevYear = cal.get(Calendar.YEAR)

                fun monthOf(ts: com.google.firebase.Timestamp?): Pair<Int, Int> {
                    if (ts == null) return Pair(-1, -1)
                    val c = Calendar.getInstance().also { it.time = ts.toDate() }
                    return Pair(c.get(Calendar.MONTH), c.get(Calendar.YEAR))
                }

                val thisIncome = incomes.filter { monthOf(it.date) == Pair(thisMonth, thisYear) }
                    .sumOf { it.amountLKR }
                val prevIncome = incomes.filter { monthOf(it.date) == Pair(prevMonth, prevYear) }
                    .sumOf { it.amountLKR }

                val thisExpenses = expenses.filter { monthOf(it.date) == Pair(thisMonth, thisYear) }
                    .sumOf { it.amount }
                val prevExpenses = expenses.filter { monthOf(it.date) == Pair(prevMonth, prevYear) }
                    .sumOf { it.amount }

                val activeGoal = goals.firstOrNull { it.isActive }
                val progress = if (activeGoal != null && activeGoal.targetAmount > 0) {
                    (activeGoal.currentAmount / activeGoal.targetAmount).coerceIn(0.0, 1.0).toFloat()
                } else 0f

                // Compute a simple health score
                val savingsRate = if (thisIncome > 0) ((thisIncome - thisExpenses) / thisIncome) else 0.0
                val healthScore = (savingsRate * 60 + progress * 40).toInt().coerceIn(0, 100)

                val achievements = buildAchievements(
                    hasIncome = incomes.isNotEmpty(),
                    hasGoal = goals.isNotEmpty(),
                    hasMonthComplete = thisExpenses > 0 && thisIncome > 0,
                    streakDays = 7, // simplified placeholder
                )

                Triple(
                    Triple(thisIncome, thisExpenses, activeGoal),
                    Triple(progress, thisIncome - prevIncome, thisExpenses - prevExpenses),
                    Triple(healthScore, achievements, goals.isNotEmpty()),
                )
            }.collect { (snapshot, trends, extras) ->
                val (thisIncome, thisExpenses, activeGoal) = snapshot
                val (progress, incomeVsLast, expVsLast) = trends
                val (healthScore, achievements, _) = extras
                _uiState.value = _uiState.value.copy(
                    thisMonthIncome = thisIncome,
                    thisMonthExpenses = thisExpenses,
                    activeGoal = activeGoal,
                    goalProgressPercent = progress,
                    incomeVsLastMonth = incomeVsLast,
                    expenseVsLastMonth = expVsLast,
                    healthScore = healthScore,
                    achievements = achievements,
                )
            }
        }
    }

    // ── Preference mutations ─────────────────────────────────────────────────

    fun setUsdEnabled(v: Boolean) { _uiState.value = _uiState.value.copy(usdEnabled = v) }
    fun setUsdtEnabled(v: Boolean) { _uiState.value = _uiState.value.copy(usdtEnabled = v) }
    fun setAutoConvert(v: Boolean) { _uiState.value = _uiState.value.copy(autoConvert = v) }

    fun toggleIncomeSource(id: String) {
        val updated = _uiState.value.incomeSources.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
        _uiState.value = _uiState.value.copy(incomeSources = updated)
        persistIncomeSources(updated)
    }

    fun addIncomeSource(source: IncomeSource) {
        val updated = _uiState.value.incomeSources + source
        _uiState.value = _uiState.value.copy(incomeSources = updated)
        persistIncomeSources(updated)
    }

    private fun persistIncomeSources(sources: List<IncomeSource>) {
        viewModelScope.launch {
            val maps = sources.map { s ->
                mapOf("id" to s.id, "label" to s.label, "icon" to s.icon, "isActive" to s.isActive)
            }
            userRepository.updateIncomeSources(maps)
        }
    }

    fun setNotifySalaryReminder(v: Boolean) { _uiState.value = _uiState.value.copy(notifySalaryReminder = v) }
    fun setNotifyWeeklySummary(v: Boolean) { _uiState.value = _uiState.value.copy(notifyWeeklySummary = v) }
    fun setNotifyGoalMilestone(v: Boolean) { _uiState.value = _uiState.value.copy(notifyGoalMilestone = v) }
    fun setNotifyBudgetOverspend(v: Boolean) { _uiState.value = _uiState.value.copy(notifyBudgetOverspend = v) }
    fun setBudgetOverspendThreshold(v: String) { _uiState.value = _uiState.value.copy(budgetOverspendThreshold = v) }
    fun setDarkMode(v: Boolean) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(if (v) ThemeMode.DARK else ThemeMode.LIGHT)
        }
    }

    // ── Auth ─────────────────────────────────────────────────────────────────

    fun updateDisplayName(newName: String) {
        viewModelScope.launch { userRepository.updateDisplayName(newName) }
    }

    fun signOut() { viewModelScope.launch { authRepository.signOut() } }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun buildAchievements(
        hasIncome: Boolean,
        hasGoal: Boolean,
        hasMonthComplete: Boolean,
        streakDays: Int,
    ): List<Achievement> = listOf(
        Achievement("first_income", "First Income Logged", "You logged your first income!", "💰", hasIncome),
        Achievement("seven_day", "7-Day Streak", "Opened the app 7 days in a row", "🔥", streakDays >= 7),
        Achievement("goal_set", "Goal Set", "You set your first financial goal", "🎯", hasGoal),
        Achievement("first_month", "First Month Complete", "Tracked a full month of finances", "📅", hasMonthComplete),
    )
}

