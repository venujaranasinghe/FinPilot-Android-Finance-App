package com.bpeople.finpilot.ui.screens.goal

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.data.model.Goal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun GoalTrackerScreen(
    viewModel: GoalViewModel,
    onNavigateBack: () -> Unit = {},
    onEditGoal: (goalId: String) -> Unit = {},
) {
    val goalState by viewModel.goalState.collectAsState()
    val savingsHistory by viewModel.savingsHistory.collectAsState()

    val activeGoal = goalState.activeGoal

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Goal Tracker",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Goal Details Card
        if (activeGoal != null) {
            item {
                GoalDetailCard(
                    goal = activeGoal,
                    onEditGoal = { onEditGoal(activeGoal.id) }
                )
            }

            // Progress Section
            item {
                GoalProgressSection(
                    goal = activeGoal,
                    monthlyRequired = goalState.monthlyRequired
                )
            }

            // Status Badge
            item {
                GoalStatusBadge(
                    goal = activeGoal,
                    monthlyRequired = goalState.monthlyRequired
                )
            }

            // Monthly Savings History Chart
            item {
                SavingsHistoryChart(
                    savingsHistory = savingsHistory,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Log Savings Button
            item {
                LogSavingsSection(
                    viewModel = viewModel,
                    goal = activeGoal
                )
            }

            // Edit Goal Button
            item {
                Button(
                    onClick = { onEditGoal(activeGoal.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Goal",
                        modifier = Modifier
                            .size(20.dp)
                            .padding(end = 8.dp)
                    )
                    Text("Edit Goal", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            // No Goal State
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No Active Goal",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Create a goal to track your savings progress",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun GoalDetailCard(
    goal: Goal,
    onEditGoal: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        goal.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Target: LKR ${goal.targetAmount.toInt()}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                IconButton(
                    onClick = onEditGoal,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        "Saved",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        "LKR ${goal.currentAmount.toInt()}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Deadline",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        formatDate(goal.deadline?.toDate()),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalProgressSection(
    goal: Goal,
    monthlyRequired: Double,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Progress Overview",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Circular Progress Indicator
            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                val progress by animateFloatAsState(
                    targetValue = (goal.currentAmount / goal.targetAmount)
                        .coerceIn(0.0, 1.0).toFloat(),
                    label = "Progress Animation"
                )

                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(160.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round
                )

                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "${(progress * 100).roundToInt()}%",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Complete",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Required Monthly Savings
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Required Monthly Savings",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        "LKR ${monthlyRequired.roundToInt()}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Projected Completion
            val projectedDate = calculateProjectedCompletionDate(
                currentAmount = goal.currentAmount,
                targetAmount = goal.targetAmount,
                monthlyRequired = monthlyRequired
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Projected Completion",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        projectedDate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalStatusBadge(
    goal: Goal,
    monthlyRequired: Double,
) {
    val status = determineGoalStatus(
        currentAmount = goal.currentAmount,
        targetAmount = goal.targetAmount,
        monthlyRequired = monthlyRequired,
        deadline = goal.deadline?.toDate()
    )

    val (statusText, statusColor, statusBackgroundColor) = when (status) {
        GoalStatus.ON_TRACK -> Triple(
            "On Track",
            Color(0xFF2E7D32),
            Color(0xFF2E7D32).copy(alpha = 0.15f)
        )

        GoalStatus.AT_RISK -> Triple(
            "At Risk",
            Color(0xFFF57C00),
            Color(0xFFF57C00).copy(alpha = 0.15f)
        )

        GoalStatus.OFF_TRACK -> Triple(
            "Off Track",
            Color(0xFFC62828),
            Color(0xFFC62828).copy(alpha = 0.15f)
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = statusBackgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Goal Status",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    statusText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(statusColor.copy(alpha = 0.2f), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (status) {
                        GoalStatus.ON_TRACK -> "✓"
                        GoalStatus.AT_RISK -> "!"
                        GoalStatus.OFF_TRACK -> "✗"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun SavingsHistoryChart(
    savingsHistory: List<SavingsEntry>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Monthly Savings History",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (savingsHistory.isNotEmpty()) {
                // Simple Bar Chart Representation
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val maxAmount = savingsHistory.maxOfOrNull { it.amount } ?: 1.0

                    savingsHistory.takeLast(6).forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(32.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                entry.month,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(50.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
                                    .fillMaxWidth((entry.amount / maxAmount).toFloat())
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                "LKR ${entry.amount.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.width(80.dp),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }
            } else {
                Text(
                    "No savings history yet",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 20.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LogSavingsSection(
    viewModel: GoalViewModel,
    goal: Goal,
) {
    var showDialog by remember { mutableStateOf(false) }
    var savingsAmount by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Log Savings",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                "Add amount saved toward your goal",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { showDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    "+ Log Savings",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }

    if (showDialog) {
        LogSavingsDialog(
            currentAmount = goal.currentAmount,
            targetAmount = goal.targetAmount,
            onDismiss = { showDialog = false },
            onConfirm = { amount ->
                viewModel.logSavings(goalId = goal.id, amount = amount)
                showDialog = false
                savingsAmount = ""
            }
        )
    }
}

@Suppress("EXPERIMENTAL_API_USAGE")
@Composable
private fun LogSavingsDialog(
    currentAmount: Double,
    targetAmount: Double,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
) {
    var input by remember { mutableStateOf("") }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Log Savings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "Current: LKR ${currentAmount.toInt()} / LKR ${targetAmount.toInt()}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                androidx.compose.material3.TextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Amount (LKR)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = input.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        onConfirm(amount)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

// Helper Data Classes and Functions
data class SavingsEntry(
    val month: String,
    val amount: Double,
)

enum class GoalStatus {
    ON_TRACK,
    AT_RISK,
    OFF_TRACK,
}

private fun determineGoalStatus(
    currentAmount: Double,
    targetAmount: Double,
    monthlyRequired: Double,
    deadline: Date?,
): GoalStatus {
    if (targetAmount <= 0) return GoalStatus.OFF_TRACK
    if (deadline == null) return GoalStatus.OFF_TRACK

    val remaining = targetAmount - currentAmount
    val now = System.currentTimeMillis()
    val deadlineTime = deadline.time
    val monthsRemaining = ((deadlineTime - now) / (1000L * 60L * 60L * 24L * 30L)).toInt()

    if (monthsRemaining <= 0) {
        return if (currentAmount >= targetAmount) GoalStatus.ON_TRACK else GoalStatus.OFF_TRACK
    }

    val requiredMonthly = remaining / monthsRemaining
    val threshold = requiredMonthly * 0.8

    // On track: current monthly savings >= 80% of required
    // At risk: current monthly savings between 50-80% of required
    // Off track: current monthly savings < 50% of required

    return when {
        monthlyRequired >= threshold -> GoalStatus.ON_TRACK
        monthlyRequired >= threshold * 0.625 -> GoalStatus.AT_RISK
        else -> GoalStatus.OFF_TRACK
    }
}

private fun calculateProjectedCompletionDate(
    currentAmount: Double,
    targetAmount: Double,
    monthlyRequired: Double,
): String {
    if (monthlyRequired <= 0) return "Unknown"

    val remaining = targetAmount - currentAmount
    val monthsNeeded = (remaining / monthlyRequired).toInt().coerceAtLeast(0)

    val calendar = java.util.Calendar.getInstance().apply {
        add(java.util.Calendar.MONTH, monthsNeeded)
    }

    return SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)
}

private fun formatDate(date: Date?): String {
    if (date == null) return "No deadline"
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
}









