package com.bpeople.finpilot.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.data.model.NotificationItem
import com.bpeople.finpilot.data.model.NotificationPriority
import com.bpeople.finpilot.data.model.NotificationType
import com.bpeople.finpilot.data.repository.ExpenseRepository
import com.bpeople.finpilot.data.repository.GoalRepository
import com.bpeople.finpilot.data.repository.IncomeRepository
import com.bpeople.finpilot.data.repository.NotificationsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─────────────────────────────────────────────────────────────────────────────
// ViewModel
// ─────────────────────────────────────────────────────────────────────────────

/**
 * ViewModel for the Notifications screen.
 *
 * Combines four reactive streams — income, expenses, goals, and notification
 * preferences — to build a deterministic list of [NotificationItem]s that
 * reflect the user's current financial state.
 *
 * ### Notification generation rules
 *
 * | Type               | Condition                                                |
 * |--------------------|----------------------------------------------------------|
 * | Salary Reminder    | Today is the 25th AND [salaryReminderEnabled] is true    |
 * | Weekly Summary     | Today is Sunday AND [weeklySummaryEnabled] is true       |
 * | Goal Milestone 25  | goal.progress >= 25 % AND not yet dismissed               |
 * | Goal Milestone 50  | goal.progress >= 50 % AND not yet dismissed               |
 * | Goal Milestone 75  | goal.progress >= 75 % AND not yet dismissed               |
 * | Budget Overspend   | month expenses > threshold AND [budgetOverspendEnabled]  |
 *
 * ### Sort order
 * URGENT → HIGH → NORMAL → LOW, then newest timestamp first within each tier.
 *
 * All state is immutable and exposed as [StateFlow] — no mutable state leaks
 * to the UI layer in violation of NFR-06.
 */
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository,
    private val notificationsRepository: NotificationsRepository,
) : ViewModel() {

    // ─── UI State ─────────────────────────────────────────────────────────────

    /**
     * Represents all data the Notifications screen needs in a single snapshot.
     *
     * @property notifications      Sorted, filtered list of active notifications.
     * @property allNotifications   Same list before filter chip is applied.
     * @property unreadCount        Number of [NotificationItem]s where [NotificationItem.isActive] is true.
     * @property prefs              Current notification preference snapshot.
     * @property isLoading          True only on the initial load before the first emission arrives.
     * @property thisMonthExpenses  Running total of LKR expenses in the current calendar month.
     * @property thisWeekExpenses   Running total of LKR expenses in the last 7 days.
     * @property thisWeekIncome     Running total of LKR income in the last 7 days.
     * @property activeGoals        Goal progress summaries used for milestone detection.
     * @property budgetThresholdInput Raw string bound to the threshold text field.
     * @property activeFilter       Currently selected type filter chip; null = show all.
     */
    data class NotificationsUiState(
        val notifications: List<NotificationItem> = emptyList(),
        val allNotifications: List<NotificationItem> = emptyList(),
        val unreadCount: Int = 0,
        val prefs: NotificationsRepository.NotificationPreferences =
            NotificationsRepository.NotificationPreferences(),
        val isLoading: Boolean = true,
        val thisMonthExpenses: Double = 0.0,
        val thisWeekExpenses: Double = 0.0,
        val thisWeekIncome: Double = 0.0,
        val activeGoals: List<GoalProgressSummary> = emptyList(),
        val budgetThresholdInput: String = "10000",
        val activeFilter: NotificationType? = null,
    )

    /**
     * Lightweight summary of a single goal's progress, attached to [NotificationsUiState]
     * so the screen can render milestone details without hitting the repository.
     */
    data class GoalProgressSummary(
        val goal: Goal,
        val progressPercent: Float,
        val milestonesReached: List<Int>,
        val remaining: Double,
    )

    // ─── Internal state ───────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    // ─── Reactive pipeline ────────────────────────────────────────────────────

    init {
        viewModelScope.launch {
            combine(
                incomeRepository.observeIncome(),
                expenseRepository.observeExpenses(),
                goalRepository.observeGoals(),
                notificationsRepository.preferences,
            ) { incomes, expenses, goals, prefs ->
                buildUiState(
                    incomes  = incomes,
                    expenses = expenses,
                    goals    = goals,
                    prefs    = prefs,
                    currentFilter = _uiState.value.activeFilter,
                    thresholdInput = _uiState.value.budgetThresholdInput,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    // ─── UI State construction ────────────────────────────────────────────────

    private fun buildUiState(
        incomes: List<IncomeEntry>,
        expenses: List<ExpenseEntry>,
        goals: List<Goal>,
        prefs: NotificationsRepository.NotificationPreferences,
        currentFilter: NotificationType?,
        thresholdInput: String,
    ): NotificationsUiState {
        val now      = Calendar.getInstance()
        val year     = now.get(Calendar.YEAR)
        val month    = now.get(Calendar.MONTH)
        val dom      = now.get(Calendar.DAY_OF_MONTH)
        val dow      = now.get(Calendar.DAY_OF_WEEK)
        val isoWeek  = now.get(Calendar.WEEK_OF_YEAR)
        val monthFmt = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

        // ── Date range boundaries ─────────────────────────────────────────────
        val weekStartMs  = Calendar.getInstance()
            .also { it.add(Calendar.DAY_OF_YEAR, -6); it.startOfDay() }.timeInMillis
        val weekEndMs    = Calendar.getInstance().also { it.endOfDay() }.timeInMillis
        val monthStartMs = Calendar.getInstance()
            .also { it.set(Calendar.DAY_OF_MONTH, 1); it.startOfDay() }.timeInMillis

        // ── Financial aggregates ──────────────────────────────────────────────
        val thisWeekExpenses = expenses.sumOf { e ->
            val ms = e.date?.toDate()?.time ?: return@sumOf 0.0
            if (ms in weekStartMs..weekEndMs) e.amount else 0.0
        }
        val thisWeekIncome = incomes.sumOf { i ->
            val ms = i.date?.toDate()?.time ?: return@sumOf 0.0
            if (ms in weekStartMs..weekEndMs) i.amountLKR else 0.0
        }
        val thisMonthExpenses = expenses.sumOf { e ->
            val ms = e.date?.toDate()?.time ?: return@sumOf 0.0
            if (ms >= monthStartMs) e.amount else 0.0
        }

        // ── Goal progress summaries ───────────────────────────────────────────
        val activeGoals = goals.filter { it.isActive }.map { goal ->
            val progress = if (goal.targetAmount > 0.0) {
                (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
            } else 0f
            val reached = NotificationsRepository.MILESTONE_THRESHOLDS.filter { m ->
                progress >= m / 100f
            }
            GoalProgressSummary(
                goal             = goal,
                progressPercent  = progress,
                milestonesReached = reached,
                remaining        = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0),
            )
        }

        // ── Build raw notification list ───────────────────────────────────────
        val rawNotifications = buildList {

            // ── 1. Salary Reminder: fires on the 25th of every month ──────────
            if (prefs.salaryReminderEnabled && dom == 25) {
                val salaryId = NotificationItem.salaryId(year, month)
                if (salaryId !in prefs.dismissedIds) {
                    add(
                        NotificationItem(
                            id          = salaryId,
                            type        = NotificationType.SALARY_REMINDER,
                            title       = "Salary Day! 💰",
                            message     = "Today is the 25th — your salary is expected. " +
                                "Remember to log it in FinPilot so your ${monthFmt.format(now.time)} " +
                                "income total stays accurate.",
                            timestamp   = buildSalaryTimestamp(year, month),
                            isRead      = salaryId in prefs.readIds,
                            priority    = NotificationPriority.HIGH,
                            actionLabel = "Log Income",
                            extraData   = mapOf(
                                "month" to monthFmt.format(now.time),
                                "day"   to "25",
                            ),
                        )
                    )
                }
            }

            // ── 2. Weekly Spend Summary: fires every Sunday ───────────────────
            if (prefs.weeklySummaryEnabled && dow == Calendar.SUNDAY) {
                val weeklyId = NotificationItem.weeklyId(year, isoWeek)
                if (weeklyId !in prefs.dismissedIds) {
                    add(
                        NotificationItem(
                            id          = weeklyId,
                            type        = NotificationType.WEEKLY_SUMMARY,
                            title       = "Your Weekly Financial Recap 📊",
                            message     = buildWeeklySummaryMessage(thisWeekIncome, thisWeekExpenses),
                            timestamp   = buildWeeklyTimestamp(year, isoWeek),
                            isRead      = weeklyId in prefs.readIds,
                            priority    = NotificationPriority.NORMAL,
                            actionLabel = "View Transactions",
                            extraData   = mapOf(
                                "income"   to thisWeekIncome.toString(),
                                "expenses" to thisWeekExpenses.toString(),
                                "net"      to (thisWeekIncome - thisWeekExpenses).toString(),
                                "week"     to isoWeek.toString(),
                            ),
                        )
                    )
                }
            }

            // ── 3. Goal Milestone Alerts: 25 %, 50 %, 75 % ───────────────────
            if (prefs.goalMilestoneEnabled) {
                activeGoals.forEach { gp ->
                    gp.milestonesReached.forEach { milestone ->
                        val milestoneId = NotificationItem.milestoneId(gp.goal.id, milestone)
                        val isShown     = "${gp.goal.id}_${milestone}" in prefs.shownMilestones
                        if (milestoneId !in prefs.dismissedIds && !isShown) {
                            val type = when (milestone) {
                                25   -> NotificationType.GOAL_MILESTONE_25
                                50   -> NotificationType.GOAL_MILESTONE_50
                                else -> NotificationType.GOAL_MILESTONE_75
                            }
                            add(
                                NotificationItem(
                                    id          = milestoneId,
                                    type        = type,
                                    title       = buildMilestoneTitle(milestone),
                                    message     = buildMilestoneMessage(
                                        goalTitle     = gp.goal.title,
                                        milestone     = milestone,
                                        current       = gp.goal.currentAmount,
                                        target        = gp.goal.targetAmount,
                                        remaining     = gp.remaining,
                                    ),
                                    timestamp   = System.currentTimeMillis(),
                                    isRead      = milestoneId in prefs.readIds,
                                    priority    = NotificationPriority.HIGH,
                                    actionLabel = "View Goal",
                                    extraData   = mapOf(
                                        "goalId"        to gp.goal.id,
                                        "goalTitle"     to gp.goal.title,
                                        "milestone"     to milestone.toString(),
                                        "progress"      to gp.progressPercent.toString(),
                                        "currentAmount" to gp.goal.currentAmount.toString(),
                                        "targetAmount"  to gp.goal.targetAmount.toString(),
                                        "remaining"     to gp.remaining.toString(),
                                    ),
                                )
                            )
                            // Record that we surfaced this milestone so it doesn't
                            // re-generate on every subsequent snapshot — fire-and-forget.
                            viewModelScope.launch {
                                notificationsRepository.recordMilestoneShown(gp.goal.id, milestone)
                            }
                        }
                    }
                }
            }

            // ── 4. Budget Overspend Alert: spend exceeds threshold ────────────
            if (prefs.budgetOverspendEnabled && thisMonthExpenses > prefs.budgetThreshold) {
                val budgetId = NotificationItem.budgetId(year, month)
                if (budgetId !in prefs.dismissedIds) {
                    val overspentBy = thisMonthExpenses - prefs.budgetThreshold
                    val monthLabel  = monthFmt.format(now.time)
                    add(
                        NotificationItem(
                            id          = budgetId,
                            type        = NotificationType.BUDGET_OVERSPEND,
                            title       = "Budget Limit Exceeded ⚠️",
                            message     = buildBudgetMessage(
                                threshold   = prefs.budgetThreshold,
                                spent       = thisMonthExpenses,
                                overspentBy = overspentBy,
                                monthLabel  = monthLabel,
                            ),
                            timestamp   = System.currentTimeMillis(),
                            isRead      = budgetId in prefs.readIds,
                            priority    = NotificationPriority.URGENT,
                            actionLabel = "Review Expenses",
                            extraData   = mapOf(
                                "threshold"   to prefs.budgetThreshold.toString(),
                                "spent"       to thisMonthExpenses.toString(),
                                "overspentBy" to overspentBy.toString(),
                                "month"       to monthLabel,
                            ),
                        )
                    )
                }
            }
        }

        // ── Sort: priority descending, then timestamp descending ──────────────
        val sorted = rawNotifications.sortedWith(
            compareByDescending<NotificationItem> { it.priority.ordinal }
                .thenByDescending { it.timestamp }
        )

        // ── Apply type filter chip ────────────────────────────────────────────
        val filtered = if (currentFilter != null) sorted.filter { it.type == currentFilter }
                       else sorted

        val unreadCount = sorted.count { it.isActive }

        return NotificationsUiState(
            notifications       = filtered,
            allNotifications    = sorted,
            unreadCount         = unreadCount,
            prefs               = prefs,
            isLoading           = false,
            thisMonthExpenses   = thisMonthExpenses,
            thisWeekExpenses    = thisWeekExpenses,
            thisWeekIncome      = thisWeekIncome,
            activeGoals         = activeGoals,
            budgetThresholdInput = thresholdInput.ifBlank { prefs.budgetThreshold.toInt().toString() },
            activeFilter        = currentFilter,
        )
    }

    // ─── User action handlers ─────────────────────────────────────────────────

    /** Dismiss a single notification by [id] — persisted across restarts. */
    fun dismissNotification(id: String) {
        viewModelScope.launch { notificationsRepository.dismissNotification(id) }
    }

    /** Dismiss all currently visible notifications in one DataStore write. */
    fun dismissAll() {
        viewModelScope.launch {
            val ids = _uiState.value.allNotifications.map { it.id }
            notificationsRepository.dismissAll(ids)
        }
    }

    /** Mark a single notification as read (reduces unread badge count). */
    fun markNotificationRead(id: String) {
        viewModelScope.launch { notificationsRepository.markRead(id) }
    }

    /** Mark all visible notifications as read in one DataStore write. */
    fun markAllRead() {
        viewModelScope.launch {
            val ids = _uiState.value.allNotifications.map { it.id }
            notificationsRepository.markAllRead(ids)
        }
    }

    /** Toggle the salary reminder preference. */
    fun setSalaryReminderEnabled(enabled: Boolean) {
        viewModelScope.launch { notificationsRepository.setSalaryReminderEnabled(enabled) }
    }

    /** Toggle the weekly summary preference. */
    fun setWeeklySummaryEnabled(enabled: Boolean) {
        viewModelScope.launch { notificationsRepository.setWeeklySummaryEnabled(enabled) }
    }

    /** Toggle the goal milestone alerts preference. */
    fun setGoalMilestoneEnabled(enabled: Boolean) {
        viewModelScope.launch { notificationsRepository.setGoalMilestoneEnabled(enabled) }
    }

    /** Toggle the budget overspend alert preference. */
    fun setBudgetOverspendEnabled(enabled: Boolean) {
        viewModelScope.launch { notificationsRepository.setBudgetOverspendEnabled(enabled) }
    }

    /**
     * Update the raw threshold text field without committing to DataStore.
     * Call [commitBudgetThreshold] when the user finishes editing.
     */
    fun updateBudgetThresholdInput(raw: String) {
        _uiState.update { it.copy(budgetThresholdInput = raw) }
    }

    /**
     * Validate and persist [budgetThresholdInput] to DataStore.
     * Invalid or negative values are silently rejected.
     */
    fun commitBudgetThreshold() {
        val threshold = _uiState.value.budgetThresholdInput.toDoubleOrNull() ?: return
        if (threshold < 0.0) return
        viewModelScope.launch { notificationsRepository.setBudgetThreshold(threshold) }
    }

    /**
     * Set the active filter chip for type-based filtering.
     * Passing null resets to "show all".
     */
    fun setActiveFilter(type: NotificationType?) {
        _uiState.update { current ->
            val filtered = if (type != null) current.allNotifications.filter { it.type == type }
                           else current.allNotifications
            current.copy(activeFilter = type, notifications = filtered)
        }
    }

    // ─── Private message builders ─────────────────────────────────────────────

    private fun buildWeeklySummaryMessage(income: Double, expenses: Double): String {
        val net      = income - expenses
        val netLkr   = formatLkr(net.coerceAtLeast(0.0))
        val surplus  = net >= 0.0
        val emoji    = if (surplus) "💪" else "📉"
        val netLabel = if (surplus) "net surplus of $netLkr" else "net deficit of ${formatLkr(-net)}"
        val advice   = if (surplus) "Great financial discipline this week — keep the momentum going!"
                       else "Consider reviewing your discretionary spending for next week."
        return "This week you earned ${formatLkr(income)} and spent ${formatLkr(expenses)}, " +
               "leaving a $netLabel. $advice $emoji"
    }

    private fun buildMilestoneTitle(milestone: Int): String = when (milestone) {
        25   -> "Goal 25 % Reached! 🎯"
        50   -> "Halfway There — 50 %! 🏅"
        75   -> "Almost Done — 75 %! 🔥"
        else -> "Goal Milestone Reached!"
    }

    private fun buildMilestoneMessage(
        goalTitle: String,
        milestone: Int,
        current: Double,
        target: Double,
        remaining: Double,
    ): String {
        val savedFmt     = formatLkr(current)
        val targetFmt    = formatLkr(target)
        val remainFmt    = formatLkr(remaining)
        val encouragement = when (milestone) {
            25   -> "You've completed the first quarter of your journey — $remainFmt still to go."
            50   -> "Halfway there! You need $remainFmt more to cross the finish line."
            else -> "Just 25 % left — $remainFmt more and your goal is complete!"
        }
        return "You've saved $savedFmt of your $targetFmt target for \"$goalTitle\". $encouragement"
    }

    private fun buildBudgetMessage(
        threshold: Double,
        spent: Double,
        overspentBy: Double,
        monthLabel: String,
    ): String {
        val pctOver = ((overspentBy / threshold.coerceAtLeast(1.0)) * 100).roundToInt()
        return "Your spending in $monthLabel reached ${formatLkr(spent)}, exceeding your " +
               "${formatLkr(threshold)} budget by ${formatLkr(overspentBy)} ($pctOver % over limit). " +
               "Reviewing your discretionary categories may help bring spending back on track."
    }

    private fun formatLkr(amount: Double): String {
        val safe = amount.coerceAtLeast(0.0)
        return when {
            safe >= 1_000_000 -> "LKR %.1fM".format(safe / 1_000_000)
            safe >= 1_000     -> "LKR %.0fK".format(safe / 1_000)
            else              -> "LKR %.0f".format(safe)
        }
    }

    private fun buildSalaryTimestamp(year: Int, month: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, 25, 9, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun buildWeeklyTimestamp(year: Int, week: Int): Long =
        Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.WEEK_OF_YEAR, week)
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
}

// ─── Calendar extensions (file-private) ──────────────────────────────────────

private fun Calendar.startOfDay() {
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}

private fun Calendar.endOfDay() {
    set(Calendar.HOUR_OF_DAY, 23)
    set(Calendar.MINUTE, 59)
    set(Calendar.SECOND, 59)
    set(Calendar.MILLISECOND, 999)
}
