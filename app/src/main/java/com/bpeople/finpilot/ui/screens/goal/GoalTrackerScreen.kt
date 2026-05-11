package com.bpeople.finpilot.ui.screens.goal

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTrackerScreen(
    viewModel: GoalViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
) {
    val goalState by viewModel.goalState.collectAsState()
    val savingsHistory by viewModel.savingsHistory.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showSheet by remember { mutableStateOf(false) }
    var isEditing by remember { mutableStateOf(false) }

    fun openCreate() {
        viewModel.prepareCreateGoal()
        isEditing = false
        showSheet = true
    }

    fun openEdit(goal: Goal) {
        viewModel.prepareEditGoal(goal)
        isEditing = true
        showSheet = true
    }

    GoalTrackerScreenContent(
        goalState = goalState,
        savingsHistory = savingsHistory,
        onOpenCreate = { openCreate() },
        onOpenEdit = { goal -> openEdit(goal) },
        onSelectGoal = viewModel::selectGoal,
        onLogSavings = { goalId, amount -> viewModel.logSavings(goalId, amount) },
        onWithdrawSavings = { goalId, amount -> viewModel.withdrawSavings(goalId, amount) },
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToExpense = onNavigateToExpense,
        onNavigateToGoals = { /* currently here */ },
        onNavigateToProfile = onNavigateToProfile
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            CreateEditGoalBottomSheet(
                isEditing = isEditing,
                goalState = goalState,
                onTitleChange = viewModel::onTitleChange,
                onTargetAmountChange = viewModel::onTargetAmountChange,
                onCurrentAmountChange = viewModel::onCurrentAmountChange,
                onDeadlineChange = viewModel::onDeadlineChange,
                onSubmit = {
                    viewModel.submitGoal()
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                },
                onDismiss = {
                    scope.launch { sheetState.hide() }.invokeOnCompletion { showSheet = false }
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GoalTrackerScreenContent(
    goalState: GoalViewModel.GoalUiState,
    savingsHistory: List<SavingsEntry>,
    onOpenCreate: () -> Unit = {},
    onOpenEdit: (Goal) -> Unit = {},
    onSelectGoal: (Int) -> Unit = {},
    onLogSavings: (goalId: String, amount: Double) -> Unit = { _, _ -> },
    onWithdrawSavings: (goalId: String, amount: Double) -> Unit = { _, _ -> },
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {}
) {
    val allGoals = goalState.allGoals
    val activeGoal = goalState.activeGoal
    var animationTrigger by remember { mutableStateOf(false) }

    val pagerState = rememberPagerState(
        initialPage = goalState.selectedGoalIndex.coerceAtMost((allGoals.size - 1).coerceAtLeast(0)),
        pageCount = { allGoals.size }
    )

    LaunchedEffect(Unit) { animationTrigger = true }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != goalState.selectedGoalIndex) {
            onSelectGoal(pagerState.currentPage)
        }
    }

    LaunchedEffect(goalState.selectedGoalIndex) {
        if (allGoals.isNotEmpty() && pagerState.currentPage != goalState.selectedGoalIndex) {
            pagerState.animateScrollToPage(goalState.selectedGoalIndex)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            FinPilotBottomNavBar(
                currentTab = NavTab.GOALS,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToExpense = onNavigateToExpense,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    ) { innerPadding ->
        if (activeGoal != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Pager Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.background
                                )
                            )
                        )
                        .padding(top = 40.dp, bottom = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "My Goals",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                // Edit current goal
                                IconButton(
                                    onClick = { activeGoal?.let { onOpenEdit(it) } },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.surface, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Goal",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                // Add new goal
                                IconButton(
                                    onClick = onOpenCreate,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Add Goal",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Swipeable Goal Cards
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 32.dp),
                            pageSpacing = 16.dp
                        ) { page ->
                            val goal = allGoals[page]
                            val progressValue = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
                            val progress by animateFloatAsState(
                                targetValue = if (animationTrigger && page == pagerState.currentPage) progressValue else 0f,
                                animationSpec = tween(durationMillis = 1500),
                                label = "Progress $page"
                            )
                            val pageScale by animateFloatAsState(
                                targetValue = if (page == pagerState.currentPage) 1f else 0.88f,
                                animationSpec = tween(300),
                                label = "scale"
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(pageScale),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier.size(180.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier.size(180.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                                        strokeWidth = 12.dp,
                                        strokeCap = StrokeCap.Round
                                    )
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "${(progress * 100).roundToInt()}%",
                                            fontSize = 32.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = "Done",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = goal.title,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "LKR ${goal.currentAmount.toInt()} / LKR ${goal.targetAmount.toInt()}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Page indicator dots
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(allGoals.size) { index ->
                                val selected = index == pagerState.currentPage
                                val dotWidth by animateFloatAsState(
                                    targetValue = if (selected) 20f else 8f,
                                    animationSpec = tween(300),
                                    label = "dot_width"
                                )
                                val dotColor by animateColorAsState(
                                    targetValue = if (selected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.25f),
                                    animationSpec = tween(300),
                                    label = "dot_color"
                                )
                                Box(
                                    modifier = Modifier
                                        .width(dotWidth.dp)
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(dotColor)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Target: ${formatDate(activeGoal?.deadline?.toDate())}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }

                // Form Area
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp)
                            .padding(top = 32.dp, bottom = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        
                        // Status Badge
                        GoalStatusBadge(
                            goal = activeGoal,
                            monthlyRequired = goalState.monthlyRequired
                        )

                        // Required Monthly Savings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Required Monthly Savings",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    "LKR ${goalState.monthlyRequired.roundToInt()}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        // Projected Completion
                        val projectedDate = calculateProjectedCompletionDate(
                            currentAmount = activeGoal.currentAmount,
                            targetAmount = activeGoal.targetAmount,
                            monthlyRequired = goalState.monthlyRequired
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Projected Completion",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    projectedDate,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        // Savings History Chart
                        SavingsHistoryChart(
                            savingsHistory = savingsHistory,
                            modifier = Modifier.fillMaxWidth(),
                            animationTrigger = animationTrigger
                        )

                        // Log Savings Button
                        LogSavingsSection(
                            onLogSavings = onLogSavings,
                            onWithdrawSavings = onWithdrawSavings,
                            goal = activeGoal
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        } else {
            // No Goal State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎯", fontSize = 40.sp)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        "No Active Goal",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Set a savings goal and track your progress beautifully.",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onOpenCreate,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Create a Goal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
            Color(0xFF10B981), // IncomeGreen
            Color(0xFF10B981).copy(alpha = 0.15f)
        )
        GoalStatus.AT_RISK -> Triple(
            "At Risk",
            Color(0xFFF59E0B), // WarningAmber
            Color(0xFFF59E0B).copy(alpha = 0.15f)
        )
        GoalStatus.OFF_TRACK -> Triple(
            "Off Track",
            Color(0xFFEF4444), // ExpenseRed
            Color(0xFFEF4444).copy(alpha = 0.15f)
        )
    }

    val animatedBg by animateColorAsState(targetValue = statusBackgroundColor, label = "bg_color")
    val animatedScale by animateFloatAsState(targetValue = 1f, animationSpec = tween(500), label = "scale")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(animatedScale)
            .clip(RoundedCornerShape(16.dp))
            .background(animatedBg)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Current Status",
                style = MaterialTheme.typography.labelMedium,
                color = statusColor.copy(alpha = 0.8f)
            )
            Text(
                statusText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(statusColor.copy(alpha = 0.2f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when (status) {
                    GoalStatus.ON_TRACK -> "✓"
                    GoalStatus.AT_RISK -> "!"
                    GoalStatus.OFF_TRACK -> "✗"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
private fun SavingsHistoryChart(
    savingsHistory: List<SavingsEntry>,
    modifier: Modifier = Modifier,
    animationTrigger: Boolean
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Recent Savings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (savingsHistory.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val maxAmount = savingsHistory.maxOfOrNull { it.amount } ?: 1.0

                savingsHistory.takeLast(6).forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            entry.month,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.width(40.dp)
                        )
                        
                        val targetFill = (entry.amount / maxAmount).toFloat()
                        val animatedFill by animateFloatAsState(
                            targetValue = if (animationTrigger) targetFill else 0f,
                            animationSpec = tween(durationMillis = 1000),
                            label = "Bar Animation"
                        )
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(animatedFill)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Text(
                            "LKR ${entry.amount.toInt()}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.width(80.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        } else {
            Text(
                "No savings history yet.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LogSavingsSection(
    onLogSavings: (String, Double) -> Unit,
    onWithdrawSavings: (String, Double) -> Unit,
    goal: Goal,
) {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Log Savings",
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Log / Adjust Savings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }

    if (showDialog) {
        LogSavingsDialog(
            currentAmount = goal.currentAmount,
            targetAmount = goal.targetAmount,
            onDismiss = { showDialog = false },
            onAdd = { amount ->
                onLogSavings(goal.id, amount)
                showDialog = false
            },
            onWithdraw = { amount ->
                onWithdrawSavings(goal.id, amount)
                showDialog = false
            }
        )
    }
}

// ── Create / Edit Goal Bottom Sheet ──────────────────────────────────────────

@Composable
fun CreateEditGoalBottomSheet(
    isEditing: Boolean,
    goalState: GoalViewModel.GoalUiState,
    onTitleChange: (String) -> Unit,
    onTargetAmountChange: (String) -> Unit,
    onCurrentAmountChange: (String) -> Unit,
    onDeadlineChange: (Long) -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val selectedDateLabel = goalState.deadlineMillis?.let { dateFormat.format(Date(it)) } ?: "Pick a date"
    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = MaterialTheme.colorScheme.primary
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .align(Alignment.CenterHorizontally)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp))
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Title
        Text(
            text = if (isEditing) "Edit Goal" else "Create New Goal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        if (goalState.errorMessage != null) {
            Text(
                text = goalState.errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    .padding(12.dp)
            )
        }

        // Goal Name
        OutlinedTextField(
            value = goalState.title,
            onValueChange = onTitleChange,
            label = { Text("Goal Name") },
            placeholder = { Text("e.g. New Car, Vacation, Emergency Fund") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = fieldColors
        )

        // Target Amount
        OutlinedTextField(
            value = goalState.targetAmount,
            onValueChange = onTargetAmountChange,
            label = { Text("Target Amount (LKR)") },
            placeholder = { Text("e.g. 500000") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors
        )

        // Starting Amount (optional)
        OutlinedTextField(
            value = goalState.currentAmount,
            onValueChange = onCurrentAmountChange,
            label = { Text("Amount Already Saved (LKR)") },
            placeholder = { Text("0 if starting fresh") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors
        )

        // Date Picker
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Target Date",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    selectedDateLabel,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            TextButton(
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        goalState.deadlineMillis?.let { timeInMillis = it }
                    }
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val picked = Calendar.getInstance().apply {
                                set(year, month, day, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onDeadlineChange(picked.timeInMillis)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = "Pick date",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Submit Button
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = if (isEditing) "Save Changes" else "Create Goal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun LogSavingsDialog(
    currentAmount: Double,
    targetAmount: Double,
    onDismiss: () -> Unit,
    onAdd: (Double) -> Unit,
    onWithdraw: (Double) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    var isWithdrawMode by remember { mutableStateOf(false) }
    val inputAmount = input.toDoubleOrNull() ?: 0.0
    val withdrawError = isWithdrawMode && inputAmount > currentAmount

    val addColor = Color(0xFF10B981)    // green
    val withdrawColor = Color(0xFFEF4444) // red
    val activeColor by animateColorAsState(
        targetValue = if (isWithdrawMode) withdrawColor else addColor,
        animationSpec = tween(300),
        label = "mode_color"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Adjust Savings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                // Mode Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(false to "Add Savings", true to "Withdraw").forEach { (isWithdraw, label) ->
                        val selected = isWithdrawMode == isWithdraw
                        val tabBg by animateColorAsState(
                            targetValue = if (selected) activeColor else Color.Transparent,
                            animationSpec = tween(300),
                            label = "tab_bg"
                        )
                        val tabContent by animateColorAsState(
                            targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            animationSpec = tween(300),
                            label = "tab_content"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tabBg)
                                .clickable { isWithdrawMode = isWithdraw }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = tabContent
                            )
                        }
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isWithdrawMode)
                        "Current savings: LKR ${currentAmount.toInt()}. Enter the amount to withdraw."
                    else
                        "Current savings: LKR ${currentAmount.toInt()} / LKR ${targetAmount.toInt()}.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Amount (LKR)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = withdrawError,
                    supportingText = if (withdrawError) {{
                        Text(
                            "Cannot withdraw more than current savings (LKR ${currentAmount.toInt()})",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }} else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = activeColor,
                        errorBorderColor = MaterialTheme.colorScheme.error
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = input.toDoubleOrNull()
                    if (amount != null && amount > 0 && !withdrawError) {
                        if (isWithdrawMode) onWithdraw(amount) else onAdd(amount)
                    }
                },
                enabled = !withdrawError && input.toDoubleOrNull()?.let { it > 0 } == true,
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    if (isWithdrawMode) "Withdraw" else "Add",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GoalTrackerScreenPreview() {
    FinPilotTheme {
        GoalTrackerScreenContent(
            goalState = GoalViewModel.GoalUiState(
                allGoals = listOf(
                    Goal(id = "1", title = "New Car", targetAmount = 5000000.0, currentAmount = 2500000.0, isActive = true),
                    Goal(id = "2", title = "Vacation", targetAmount = 300000.0, currentAmount = 80000.0, isActive = true),
                    Goal(id = "3", title = "Emergency Fund", targetAmount = 1000000.0, currentAmount = 450000.0, isActive = true),
                ),
                selectedGoalIndex = 0,
                monthlyRequired = 50000.0
            ),
            savingsHistory = listOf(
                SavingsEntry("Jan", 40000.0),
                SavingsEntry("Feb", 50000.0),
                SavingsEntry("Mar", 45000.0),
                SavingsEntry("Apr", 60000.0)
            )
        )
    }
}
