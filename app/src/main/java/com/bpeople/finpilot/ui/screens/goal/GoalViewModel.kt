package com.bpeople.finpilot.ui.screens.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.data.repository.GoalRepository
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val goalRepository: GoalRepository,
) : ViewModel() {

    data class GoalUiState(
        val activeGoal: Goal? = null,
        val title: String = "",
        val targetAmount: String = "",
        val currentAmount: String = "",
        val deadlineMillis: Long? = null,
        val monthlyRequired: Double = 0.0,
        val progressPercent: Float = 0f,
        val errorMessage: String? = null,
    )

    private val _goalState = MutableStateFlow(GoalUiState())
    val goalState: StateFlow<GoalUiState> = _goalState.asStateFlow()

    init {
        goalRepository.observeActiveGoal()
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
            .let { flow ->
                viewModelScope.launch {
                    flow.collect { goal ->
                        val progress = calculateProgress(goal)
                        val monthlyRequired = calculateMonthlyRequired(goal)
                        _goalState.update {
                            it.copy(
                                activeGoal = goal,
                                progressPercent = progress,
                                monthlyRequired = monthlyRequired,
                            )
                        }
                    }
                }
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

    fun submitGoal() {
        val state = _goalState.value
        val targetAmount = state.targetAmount.toDoubleOrNull()
        val currentAmount = state.currentAmount.toDoubleOrNull() ?: 0.0

        if (targetAmount == null || targetAmount <= 0) {
            _goalState.update { it.copy(errorMessage = "Enter a valid target amount") }
            return
        }

        val deadline = state.deadlineMillis?.let { Timestamp(Date(it)) }
        val goal = Goal(
            id = state.activeGoal?.id ?: UUID.randomUUID().toString(),
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
                    title = "",
                    targetAmount = "",
                    currentAmount = "",
                    deadlineMillis = null,
                    errorMessage = null,
                )
            }
        }
    }

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
}

