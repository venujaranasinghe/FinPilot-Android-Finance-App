package com.bpeople.finpilot.ui.screens.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.repository.GoalRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
) : ViewModel() {

    data class GoalUiState(
        val allGoals: List<Goal> = emptyList(),
        val selectedGoalIndex: Int = 0,
        val editingGoalId: String? = null,
        val pendingSelectionGoalId: String? = null,
        // Form fields for the create/edit bottom sheet
        val title: String = "",
        val targetAmount: String = "",
        val currentAmount: String = "",
        val deadlineMillis: Long? = null,
        val monthlyRequired: Double = 0.0,
        val progressPercent: Float = 0f,
        val errorMessage: String? = null,
    ) {
        /** The goal currently displayed in the pager. */
        val activeGoal: Goal? get() = allGoals.getOrNull(selectedGoalIndex)
    }

    private val _goalState = MutableStateFlow(GoalUiState())
    val goalState: StateFlow<GoalUiState> = _goalState.asStateFlow()

    private val _savingsHistory = MutableStateFlow<List<SavingsEntry>>(emptyList())
    val savingsHistory: StateFlow<List<SavingsEntry>> = _savingsHistory.asStateFlow()

    init {
        // ── 1. Observe all goals from Firestore ──────────────────────────
        goalRepository.observeGoals()
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            .let { flow ->
                viewModelScope.launch {
                    flow.collect { goals ->
                        val currentState = _goalState.value
                        val pendingSelectionIndex = currentState.pendingSelectionGoalId
                            ?.let { pendingId -> goals.indexOfFirst { it.id == pendingId } }
                            ?.takeIf { it >= 0 }

                        val index = pendingSelectionIndex
                            ?: currentState.selectedGoalIndex
                                .coerceAtMost((goals.size - 1).coerceAtLeast(0))

                        val selected = goals.getOrNull(index)
                        _goalState.update {
                            it.copy(
                                allGoals = goals,
                                selectedGoalIndex = index,
                                pendingSelectionGoalId = if (pendingSelectionIndex != null) null else it.pendingSelectionGoalId,
                                progressPercent = calculateProgress(selected),
                                monthlyRequired = calculateMonthlyRequired(selected),
                            )
                        }
                    }
                }
            }

        // ── 2. Reactively stream savingsLogs for the active goal ──────────
        viewModelScope.launch {
            _goalState
                .map { it.activeGoal?.id }
                .distinctUntilChanged()
                .flatMapLatest { goalId ->
                    if (goalId != null) goalRepository.observeSavingsLogs(goalId)
                    else flowOf(emptyList())
                }
                .collect { rawEntries ->
                    _savingsHistory.value = aggregateByMonth(rawEntries)
                }
        }
    }

    /** Switch the displayed goal when the user swipes the pager. */
    fun selectGoal(index: Int) {
        val goals = _goalState.value.allGoals
        val goal = goals.getOrNull(index) ?: return
        _goalState.update {
            it.copy(
                selectedGoalIndex = index,
                progressPercent = calculateProgress(goal),
                monthlyRequired = calculateMonthlyRequired(goal),
            )
        }
    }

    fun onTitleChange(value: String) {
        _goalState.update { it.copy(title = value, errorMessage = null) }
    }

    fun onTargetAmountChange(value: String) {
        _goalState.update { it.copy(targetAmount = value, errorMessage = null) }
    }

    fun onCurrentAmountChange(value: String) {
        _goalState.update { it.copy(currentAmount = value, errorMessage = null) }
    }

    fun onDeadlineChange(millis: Long) {
        _goalState.update { it.copy(deadlineMillis = millis, errorMessage = null) }
    }

    /** Resets the form fields to a blank state for creating a brand-new goal. */
    fun prepareCreateGoal() {
        _goalState.update {
            it.copy(
                editingGoalId = null,
                title = "",
                targetAmount = "",
                currentAmount = "",
                deadlineMillis = null,
                errorMessage = null,
            )
        }
    }

    /** Pre-fills the form fields with an existing goal's data for editing. */
    fun prepareEditGoal(goal: Goal) {
        _goalState.update {
            it.copy(
                editingGoalId = goal.id,
                title = goal.title,
                targetAmount = goal.targetAmount.toInt().toString(),
                currentAmount = goal.currentAmount.toInt().toString(),
                deadlineMillis = goal.deadline?.toDate()?.time,
                errorMessage = null,
            )
        }
    }

    fun submitGoal() {
        val state = _goalState.value
        val targetAmount = state.targetAmount.toDoubleOrNull()
        val currentAmount = state.currentAmount.toDoubleOrNull() ?: 0.0

        if (state.title.isBlank()) {
            _goalState.update { it.copy(errorMessage = "Enter a goal name") }
            return
        }
        if (targetAmount == null || targetAmount <= 0) {
            _goalState.update { it.copy(errorMessage = "Enter a valid target amount") }
            return
        }

        val deadline = state.deadlineMillis?.let { Timestamp(Date(it)) }
        val goal = Goal(
            id = state.editingGoalId ?: UUID.randomUUID().toString(),
            title = state.title,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            deadline = deadline,
            monthlyRequired = calculateMonthlyRequired(targetAmount, currentAmount, deadline),
            isActive = true,
        )

        viewModelScope.launch {
            goalRepository.upsertGoal(goal)
            _goalState.update {
                it.copy(
                    editingGoalId = null,
                    pendingSelectionGoalId = if (state.editingGoalId == null) goal.id else null,
                    title = "",
                    targetAmount = "",
                    currentAmount = "",
                    deadlineMillis = null,
                    errorMessage = null,
                )
            }
        }
    }

    /**
     * Adds [amount] to the goal's currentAmount in Firestore AND writes an
     * individual entry to the goal's `savingsLogs` subcollection so the
     * monthly-history chart shows real data.
     */
    fun logSavings(goalId: String, amount: Double) {
        viewModelScope.launch {
            if (amount > 0) {
                // Atomic server-side increment — avoids race condition when multiple
                // devices log savings at the same time (no read-then-write).
                goalRepository.incrementGoalAmount(goalId, amount)
                goalRepository.logSavingsEntry(goalId, amount)
            }
        }
    }

    /**
     * Reduces the goal's currentAmount in Firestore.
     * Withdrawals are NOT written as savings-log entries so the chart only
     * reflects money actually put toward the goal each month.
     */
    fun withdrawSavings(goalId: String, amount: Double) {
        viewModelScope.launch {
            val goal = _goalState.value.allGoals.firstOrNull { it.id == goalId }
            if (goal != null && amount > 0) {
                val newAmount = (goal.currentAmount - amount).coerceAtLeast(0.0)
                val updated = goal.copy(currentAmount = newAmount)
                goalRepository.upsertGoal(updated)
            }
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private fun calculateProgress(goal: Goal?): Float {
        if (goal == null || goal.targetAmount <= 0.0) return 0f
        return (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    }

    private fun calculateMonthlyRequired(goal: Goal?): Double {
        if (goal == null) return 0.0
        return calculateMonthlyRequired(goal.targetAmount, goal.currentAmount, goal.deadline)
    }

    private fun calculateMonthlyRequired(
        targetAmount: Double,
        currentAmount: Double,
        deadline: Timestamp?,
    ): Double {
        if (deadline == null) return 0.0
        val nowMillis = System.currentTimeMillis()
        val endMillis = deadline.toDate().time
        val monthsRemaining = max(1, ((endMillis - nowMillis) / (1000L * 60L * 60L * 24L * 30L)).toInt())
        val remaining = max(0.0, targetAmount - currentAmount)
        return remaining / monthsRemaining
    }

    /**
     * Groups raw (timestampMillis, amount) pairs from the Firestore savingsLogs
     * subcollection into calendar-month buckets covering the last 6 months.
     * Months with no entries appear as LKR 0 so the bar chart is always full-width.
     */
    private fun aggregateByMonth(entries: List<Pair<Long, Double>>): List<SavingsEntry> {
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        // Key: "YYYY-MM"
        val grouped = entries.groupBy { (millis, _) ->
            val cal = Calendar.getInstance().apply { timeInMillis = millis }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
        }

        return (5 downTo 0).map { i ->
            val cal = Calendar.getInstance().apply { add(Calendar.MONTH, -i) }
            val key = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}"
            val monthName = monthFormat.format(cal.time)
            val total = grouped[key]?.sumOf { it.second } ?: 0.0
            SavingsEntry(monthName, total.coerceAtLeast(0.0))
        }
    }
}
