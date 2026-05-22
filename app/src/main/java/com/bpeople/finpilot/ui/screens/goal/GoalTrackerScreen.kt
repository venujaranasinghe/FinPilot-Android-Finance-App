package com.bpeople.finpilot.ui.screens.goal

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.theme.DarkBackground
import com.bpeople.finpilot.ui.theme.DarkBorder
import com.bpeople.finpilot.ui.theme.DarkGlassBg
import com.bpeople.finpilot.ui.theme.DarkGlassBorderLight
import com.bpeople.finpilot.ui.theme.DarkSurface
import com.bpeople.finpilot.ui.theme.DarkSurfaceVariant
import com.bpeople.finpilot.ui.theme.DarkTextHint
import com.bpeople.finpilot.ui.theme.DarkTextPrimary
import com.bpeople.finpilot.ui.theme.DarkTextSecondary
import com.bpeople.finpilot.ui.theme.GoalOrangeAccent
import com.bpeople.finpilot.ui.theme.GoalStatusGreen
import com.bpeople.finpilot.ui.theme.GoalStatusAmber
import com.bpeople.finpilot.ui.theme.GoalStatusRed
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ── Theme-aware color providers ────────────────────────────────────────────────
@Composable
private fun surfaceColor(): Color = if (isSystemInDarkTheme()) DarkSurface else Color.White

@Composable
private fun surfaceVariantColor(): Color = if (isSystemInDarkTheme()) DarkSurfaceVariant else Color(0xFFF9FAFB)

@Composable
private fun backgroundColor(): Color = if (isSystemInDarkTheme()) DarkBackground else Color(0xFFF9FAFB)

@Composable
private fun borderColor(): Color = if (isSystemInDarkTheme()) DarkBorder else Color(0xFFE5E7EB)

@Composable
private fun glassBgColor(): Color = if (isSystemInDarkTheme()) DarkGlassBg else Color.White

@Composable
private fun glassBorderLightColor(): Color = if (isSystemInDarkTheme()) DarkGlassBorderLight else Color(0xFFE5E7EB).copy(alpha = 0.5f)

@Composable
private fun textPrimaryColor(): Color = if (isSystemInDarkTheme()) DarkTextPrimary else Color(0xFF1F2937)

@Composable
private fun textSecondaryColor(): Color = if (isSystemInDarkTheme()) DarkTextSecondary else Color(0xFF4B5563)

@Composable
private fun textHintColor(): Color = if (isSystemInDarkTheme()) DarkTextHint else Color(0xFF6B7280)

@Composable
private fun heroBgColor(): Color = Color(0xFF1F2937)

// ── Formatter ─────────────────────────────────────────────────────────────────
private fun formatLKR(amount: Double): String = when {
    amount >= 1_000_000 -> "LKR %.1fM".format(amount / 1_000_000)
    amount >= 1_000 -> "LKR %.0fK".format(amount / 1_000)
    else -> "LKR %.0f".format(amount)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTrackerScreen(
    viewModel: GoalViewModel,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
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
        onNavigateToTransactions = onNavigateToTransactions,
        onNavigateToGoals = {},
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToIncome = onNavigateToIncome,
        onNavigateToSettings = onNavigateToSettings,
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = glassBgColor(),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                },
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
    onNavigateToIncome: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
) {
    val allGoals = goalState.allGoals
    val activeGoal = goalState.activeGoal
    var animationTrigger by remember { mutableStateOf(false) }
    val bgColor = backgroundColor()

    val pagerState = rememberPagerState(
        initialPage = goalState.selectedGoalIndex.coerceAtMost((allGoals.size - 1).coerceAtLeast(0)),
        pageCount = { allGoals.size },
    )

    LaunchedEffect(Unit) { animationTrigger = true }

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != goalState.selectedGoalIndex) {
            onSelectGoal(pagerState.currentPage)
        }
    }

    LaunchedEffect(goalState.selectedGoalIndex, allGoals.size) {
        if (allGoals.isEmpty() || pagerState.pageCount == 0) return@LaunchedEffect
        val targetPage = goalState.selectedGoalIndex.coerceIn(0, allGoals.lastIndex)
        if (pagerState.currentPage != targetPage && targetPage < pagerState.pageCount) {
            runCatching { pagerState.animateScrollToPage(targetPage) }
        }
    }

    Scaffold(
        containerColor = bgColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (activeGoal != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 110.dp),
                ) {
                    // ── Header Section ──────────────────────────────────
                    item {
                        GoalsHeader(
                            activeGoal = activeGoal,
                            onOpenCreate = onOpenCreate,
                            onOpenEdit = onOpenEdit,
                        )
                    }

                    // Hero Card with Pager
                    item {
                        GoalHeroCard(
                            allGoals = allGoals,
                            activeGoal = activeGoal,
                            pagerState = pagerState,
                            animationTrigger = animationTrigger,
                        )
                    }

                    // Goal Status
                    item {
                        GoalStatusCard(
                            goal = activeGoal,
                            monthlyRequired = goalState.monthlyRequired,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    // Timeline Intelligence
                    item {
                        TimelineCard(
                            goal = activeGoal,
                            monthlyRequired = goalState.monthlyRequired,
                            savingsHistory = savingsHistory,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // Savings Chart
                    item {
                        SavingsChartCard(
                            savingsHistory = savingsHistory,
                            monthlyRequired = goalState.monthlyRequired,
                            animationTrigger = animationTrigger,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // Savings Streak
                    item {
                        StreakCard(
                            savingsHistory = savingsHistory,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // Smart Suggestion
                    item {
                        SmartTipCard(
                            goal = activeGoal,
                            monthlyRequired = goalState.monthlyRequired,
                            onNavigateToExpense = onNavigateToExpense,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // Action Button
                    item {
                        LogSavingsButton(
                            onLogSavings = onLogSavings,
                            onWithdrawSavings = onWithdrawSavings,
                            goal = activeGoal,
                        )
                    }
                }
            } else {
                EmptyGoalState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    onCreateGoal = onOpenCreate,
                )
            }

            FinPilotBottomNavBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentTab = NavTab.GOALS,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToIncome = onNavigateToIncome,
                onNavigateToExpense = onNavigateToExpense,
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    }
}

// ── Goals Header ──────────────────────────────────────────────────────────────

@Composable
private fun GoalsHeader(
    activeGoal: Goal,
    onOpenCreate: () -> Unit,
    onOpenEdit: (Goal) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor())
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

                Text(
                    text = "My Goals",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimaryColor(),
                    letterSpacing = (-0.5).sp,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = onOpenCreate,
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Goal",
                        tint = textPrimaryColor(),
                        modifier = Modifier.size(24.dp),
                    )
                }
                IconButton(
                    onClick = { onOpenEdit(activeGoal) },
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Goal",
                        tint = textPrimaryColor(),
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

// ── Hero Goal Card ────────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GoalHeroCard(
    allGoals: List<Goal>,
    activeGoal: Goal,
    pagerState: androidx.compose.foundation.pager.PagerState,
    animationTrigger: Boolean,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = heroBgColor()),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background circles
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    radius = 130.dp.toPx(),
                    center = Offset(size.width * 0.90f, size.height * 0.12f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.03f),
                    radius = 80.dp.toPx(),
                    center = Offset(size.width * 0.80f, size.height * 0.65f),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (allGoals.isNotEmpty()) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        pageSpacing = 0.dp,
                    ) { page ->
                        val goal = allGoals.getOrNull(page) ?: return@HorizontalPager
                        val targetProgress = if (goal.targetAmount > 0.0) {
                            (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
                        } else 0f

                        val progressAnimatable = remember(page) { Animatable(0f) }
                        LaunchedEffect(page == pagerState.currentPage, animationTrigger) {
                            if (animationTrigger && page == pagerState.currentPage) {
                                progressAnimatable.snapTo(0f)
                                progressAnimatable.animateTo(
                                    targetValue = targetProgress,
                                    animationSpec = tween(1500, easing = FastOutSlowInEasing),
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            // Progress Ring
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                                Canvas(modifier = Modifier.size(180.dp)) {
                                    val strokeWidth = 14.dp.toPx()
                                    val ringRadius = size.minDimension / 2f - strokeWidth / 2f
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val startAngle = -90f

                                    drawArc(
                                        color = Color.White.copy(alpha = 0.2f),
                                        startAngle = startAngle,
                                        sweepAngle = 360f,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                        topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                                        size = Size(ringRadius * 2, ringRadius * 2),
                                    )

                                    val sweep = progressAnimatable.value * 360f
                                    if (sweep > 0f) {
                                        drawArc(
                                            color = Color.White,
                                            startAngle = startAngle,
                                            sweepAngle = sweep,
                                            useCenter = false,
                                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                                            topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
                                            size = Size(ringRadius * 2, ringRadius * 2),
                                        )
                                    }

                                    listOf(25, 50, 75, 100).forEach { milestone ->
                                        val milestoneProgress = milestone / 100f
                                        val angle = Math.toRadians((startAngle + milestoneProgress * 360f).toDouble())
                                        val dotX = center.x + ringRadius * cos(angle).toFloat()
                                        val dotY = center.y + ringRadius * sin(angle).toFloat()
                                        val reached = progressAnimatable.value >= milestoneProgress
                                        drawCircle(
                                            color = if (reached) GoalOrangeAccent else Color.White.copy(alpha = 0.4f),
                                            radius = 5.dp.toPx(),
                                            center = Offset(dotX, dotY),
                                        )
                                        if (reached) {
                                            drawCircle(
                                                color = Color.White,
                                                radius = 2.5.dp.toPx(),
                                                center = Offset(dotX, dotY),
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${(progressAnimatable.value * 100).roundToInt()}%",
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                    )
                                    Text(
                                        text = "saved",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.75f),
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Goal Info
                            Text(
                                text = goal.title,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = "LKR ${String.format("%,d", goal.currentAmount.toInt())} / LKR ${String.format("%,d", goal.targetAmount.toInt())}",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.padding(top = 6.dp),
                            )

                            // Mini stats
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
                                val monthlyNeeded = goal.monthlyRequired
                                val deadline = goal.deadline
                                val monthsLeft = if (deadline != null) {
                                    val now = Calendar.getInstance()
                                    val dl = Calendar.getInstance().apply { time = deadline.toDate() }
                                    ((dl.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 +
                                            dl.get(Calendar.MONTH) - now.get(Calendar.MONTH)).coerceAtLeast(0)
                                } else 0

                                MiniStat(label = "Remaining", value = formatLKR(remaining))
                                MiniStat(label = "Monthly", value = formatLKR(monthlyNeeded) + "/mo")
                                MiniStat(label = "Deadline", value = if (monthsLeft > 0) "$monthsLeft mo" else "None")
                            }
                        }
                    }

                    // Dots indicator
                    if (allGoals.size > 1) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            repeat(allGoals.size) { index ->
                                val selected = index == pagerState.currentPage
                                Box(
                                    modifier = Modifier
                                        .size(if (selected) 8.dp else 6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (selected) Color.White
                                            else Color.White.copy(alpha = 0.4f),
                                        ),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Mini Stat for Hero Card ───────────────────────────────────────────────────

@Composable
private fun MiniStat(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White.copy(alpha = 0.6f),
        )
    }
}

// ── Goal Status Card ──────────────────────────────────────────────────────────

@Composable
private fun GoalStatusCard(
    goal: Goal,
    monthlyRequired: Double,
    modifier: Modifier = Modifier,
) {
    val status = determineGoalStatus(
        currentAmount = goal.currentAmount,
        targetAmount = goal.targetAmount,
        monthlyRequired = monthlyRequired,
        deadline = goal.deadline?.toDate(),
    )

    val (statusText, statusColor, statusIcon) = when (status) {
        GoalStatus.ON_TRACK -> Triple("On Track", GoalStatusGreen, Icons.Rounded.EmojiEvents)
        GoalStatus.AT_RISK -> Triple("At Risk", GoalStatusAmber, Icons.Rounded.Flag)
        GoalStatus.OFF_TRACK -> Triple("Off Track", GoalStatusRed, Icons.Rounded.TrendingUp)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor()),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderLightColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Goal Status",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textSecondaryColor(),
                )
                Text(
                    statusText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
}

// ── Timeline Card ─────────────────────────────────────────────────────────────

@Composable
private fun TimelineCard(
    goal: Goal,
    monthlyRequired: Double,
    savingsHistory: List<SavingsEntry>,
    modifier: Modifier = Modifier,
) {
    val recentSaved = savingsHistory.lastOrNull()?.amount ?: 0.0
    val gap = monthlyRequired - recentSaved
    val gapColor = if (gap <= 0) GoalStatusGreen else GoalStatusRed
    val gapText = if (gap <= 0) {
        "Ahead by LKR ${String.format("%,d", (-gap).toInt())} this month"
    } else {
        "LKR ${String.format("%,d", gap.toInt())} short this month"
    }

    val projectedDate = calculateProjectedCompletionDate(
        currentAmount = goal.currentAmount,
        targetAmount = goal.targetAmount,
        monthlyRequired = monthlyRequired,
    )

    val now = Date()
    val deadlineDate = goal.deadline?.toDate()
    val monthsLeft = if (deadlineDate != null && deadlineDate.after(now)) {
        ((deadlineDate.time - now.time) / (1000L * 60L * 60L * 24L * 30L)).toInt()
    } else 0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor()),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderLightColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Timeline",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceVariantColor())
                        .padding(14.dp),
                ) {
                    Text("Projected", fontSize = 11.sp, color = textHintColor())
                    Text(
                        projectedDate,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor(),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceVariantColor())
                        .padding(14.dp),
                ) {
                    Text("Deadline", fontSize = 11.sp, color = textHintColor())
                    Text(
                        if (monthsLeft > 0) "$monthsLeft mo left" else formatDate(deadlineDate),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (monthsLeft in 1..2) GoalStatusRed else textPrimaryColor(),
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(gapColor.copy(alpha = 0.08f))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Monthly Gap", fontSize = 12.sp, color = textSecondaryColor())
                Text(gapText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = gapColor)
            }
        }
    }
}

// ── Savings Chart Card ────────────────────────────────────────────────────────

@Composable
private fun SavingsChartCard(
    savingsHistory: List<SavingsEntry>,
    monthlyRequired: Double,
    animationTrigger: Boolean,
    modifier: Modifier = Modifier,
) {
    val barAnim by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "bar",
    )

    val borderCol = borderColor()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor()),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderLightColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "Monthly Savings",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(GoalOrangeAccent))
                    Text("Saved", fontSize = 11.sp, color = textHintColor())
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(borderCol))
                    Text("Required", fontSize = 11.sp, color = textHintColor())
                }
            }

            if (savingsHistory.isNotEmpty()) {
                val maxVal = maxOf(savingsHistory.maxOfOrNull { it.amount } ?: 0.0, monthlyRequired, 1.0)

                Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
                    val totalW = size.width
                    val totalH = size.height
                    val groupW = totalW / savingsHistory.size
                    val barPad = 2.dp.toPx()
                    val barW = groupW / 2f - barPad - 2.dp.toPx()

                    savingsHistory.forEachIndexed { index, entry ->
                        val groupLeft = index * groupW + 4.dp.toPx()

                        val reqH = (monthlyRequired / maxVal * totalH * barAnim).toFloat().coerceAtLeast(0f)
                        drawRoundRect(
                            color = borderCol,
                            topLeft = Offset(groupLeft, totalH - reqH),
                            size = Size(barW, reqH),
                            cornerRadius = CornerRadius(3.dp.toPx()),
                        )

                        val savedH = (entry.amount / maxVal * totalH * barAnim).toFloat().coerceAtLeast(0f)
                        if (savedH > 0f) {
                            drawRoundRect(
                                color = GoalOrangeAccent,
                                topLeft = Offset(groupLeft + barW + barPad * 2, totalH - savedH),
                                size = Size(barW, savedH),
                                cornerRadius = CornerRadius(3.dp.toPx()),
                            )
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    savingsHistory.forEach { entry ->
                        Text(
                            entry.month,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            color = textHintColor(),
                        )
                    }
                }
            } else {
                Text(
                    "No savings data yet",
                    fontSize = 13.sp,
                    color = textHintColor(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ── Streak Card ───────────────────────────────────────────────────────────────

@Composable
private fun StreakCard(
    savingsHistory: List<SavingsEntry>,
    modifier: Modifier = Modifier,
) {
    val streak = savingsHistory.reversed().takeWhile { it.amount > 0 }.size

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor()),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderLightColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (streak > 0) GoalOrangeAccent.copy(alpha = 0.12f)
                            else surfaceVariantColor(),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.LocalFireDepartment,
                        contentDescription = null,
                        tint = if (streak > 0) GoalOrangeAccent else textHintColor(),
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column {
                    Text(
                        "Savings Streak",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor(),
                    )
                    Text(
                        "${streak} month${if (streak != 1) "s" else ""}",
                        fontSize = 13.sp,
                        color = if (streak > 0) GoalOrangeAccent else textHintColor(),
                    )
                }
            }

            if (savingsHistory.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    savingsHistory.takeLast(4).forEach { entry ->
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (entry.amount > 0) GoalOrangeAccent.copy(alpha = 0.12f)
                                    else surfaceVariantColor(),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (entry.amount > 0) {
                                Icon(
                                    Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = GoalOrangeAccent,
                                    modifier = Modifier.size(14.dp),
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(textHintColor().copy(alpha = 0.3f)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Smart Tip Card ────────────────────────────────────────────────────────────

@Composable
private fun SmartTipCard(
    goal: Goal,
    monthlyRequired: Double,
    onNavigateToExpense: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = goal.targetAmount - goal.currentAmount
    val suggestion = when {
        remaining <= 0 -> "You've reached your goal! Set a new challenge to keep the momentum going."
        monthlyRequired <= 0 -> "Add a deadline to get a personalised monthly savings plan."
        else -> "Save LKR ${String.format("%,d", monthlyRequired.toInt())}/mo. Review expenses to find room."
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onNavigateToExpense),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor()),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderLightColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GoalOrangeAccent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = GoalOrangeAccent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "Smart Suggestion",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor(),
                )
                Text(
                    suggestion,
                    fontSize = 12.sp,
                    color = textSecondaryColor(),
                    lineHeight = 18.sp,
                )
                Text(
                    "View expenses →",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoalOrangeAccent,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}

// ── Log Savings Button ────────────────────────────────────────────────────────

@Composable
private fun LogSavingsButton(
    onLogSavings: (String, Double) -> Unit,
    onWithdrawSavings: (String, Double) -> Unit,
    goal: Goal,
) {
    var showDialog by remember { mutableStateOf(false) }

    Button(
        onClick = { showDialog = true },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .height(56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GoalOrangeAccent,
            contentColor = Color.White,
        ),
    ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Log Savings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }

    if (showDialog) {
        LogSavingsDialog(
            currentAmount = goal.currentAmount,
            targetAmount = goal.targetAmount,
            onDismiss = { showDialog = false },
            onAdd = { amount -> onLogSavings(goal.id, amount); showDialog = false },
            onWithdraw = { amount -> onWithdrawSavings(goal.id, amount); showDialog = false },
        )
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyGoalState(modifier: Modifier = Modifier, onCreateGoal: () -> Unit) {
    Box(
        modifier = modifier.background(backgroundColor()),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(GoalOrangeAccent.copy(alpha = 0.1f))
                    .border(1.dp, GoalOrangeAccent.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.EmojiEvents,
                    contentDescription = null,
                    tint = GoalOrangeAccent,
                    modifier = Modifier.size(48.dp),
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                "No Goals Yet",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor(),
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Set your first savings goal and start\nbuilding your future, one month at a time.",
                fontSize = 15.sp,
                color = textHintColor(),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
            )
            Spacer(modifier = Modifier.height(36.dp))
            Button(
                onClick = onCreateGoal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GoalOrangeAccent),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Create Your First Goal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

// ── Create / Edit Bottom Sheet ────────────────────────────────────────────────

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
    val sheetSurfaceColor = glassBgColor()
    val sheetSurfaceVariantColor = surfaceVariantColor()
    val sheetTextPrimary = textPrimaryColor()
    val sheetTextSecondary = textSecondaryColor()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = sheetSurfaceVariantColor,
        focusedContainerColor = sheetSurfaceVariantColor,
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = GoalOrangeAccent,
        unfocusedTextColor = sheetTextPrimary,
        focusedTextColor = sheetTextPrimary,
        unfocusedLabelColor = sheetTextSecondary,
        focusedLabelColor = GoalOrangeAccent,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .align(Alignment.CenterHorizontally)
                .background(
                    sheetTextSecondary.copy(alpha = 0.3f),
                    RoundedCornerShape(2.dp),
                ),
        )

        Text(
            text = if (isEditing) "Edit Goal" else "Create New Goal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = sheetTextPrimary,
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
                    .padding(12.dp),
            )
        }

        OutlinedTextField(
            value = goalState.title,
            onValueChange = onTitleChange,
            label = { Text("Goal Name") },
            placeholder = { Text("e.g. MacBook Pro, Vacation Fund") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = fieldColors,
        )

        OutlinedTextField(
            value = goalState.targetAmount,
            onValueChange = onTargetAmountChange,
            label = { Text("Target Amount (LKR)") },
            placeholder = { Text("e.g. 490000") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors,
        )

        OutlinedTextField(
            value = goalState.currentAmount,
            onValueChange = onCurrentAmountChange,
            label = { Text("Already Saved (LKR)") },
            placeholder = { Text("0 if starting fresh") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = fieldColors,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(sheetSurfaceVariantColor)
                .border(1.dp, borderColor(), RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Target Date", style = MaterialTheme.typography.labelMedium, color = sheetTextSecondary)
                Text(selectedDateLabel, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = sheetTextPrimary)
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
                        calendar.get(Calendar.DAY_OF_MONTH),
                    ).show()
                },
            ) {
                Icon(Icons.Rounded.CalendarToday, contentDescription = "Pick date", tint = GoalOrangeAccent, modifier = Modifier.size(20.dp))
            }
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GoalOrangeAccent),
        ) {
            Text(
                text = if (isEditing) "Save Changes" else "Create Goal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

// ── Log Savings Dialog ────────────────────────────────────────────────────────

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

    val addColor = GoalStatusGreen
    val withdrawColor = GoalStatusRed
    val activeColor by animateColorAsState(
        targetValue = if (isWithdrawMode) withdrawColor else addColor,
        animationSpec = tween(300),
        label = "mode_color",
    )

    val dialogSurfaceColor = glassBgColor()
    val dialogSurfaceVariantColor = surfaceVariantColor()
    val dialogTextPrimary = textPrimaryColor()
    val dialogTextSecondary = textSecondaryColor()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = dialogSurfaceColor,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Adjust Savings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = dialogTextPrimary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(dialogSurfaceVariantColor.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    listOf(false to "Add Savings", true to "Withdraw").forEach { (withdraw, label) ->
                        val selected = isWithdrawMode == withdraw
                        val tabBg by animateColorAsState(
                            targetValue = if (selected) activeColor else Color.Transparent,
                            animationSpec = tween(300),
                            label = "tab_bg",
                        )
                        val tabContent by animateColorAsState(
                            targetValue = if (selected) Color.White else dialogTextSecondary,
                            animationSpec = tween(300),
                            label = "tab_content",
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(tabBg)
                                .clickable { isWithdrawMode = withdraw }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = tabContent)
                        }
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (isWithdrawMode)
                        "Current savings: LKR ${currentAmount.toInt()}. Enter amount to withdraw."
                    else
                        "Current: LKR ${currentAmount.toInt()} / LKR ${targetAmount.toInt()}",
                    fontSize = 13.sp,
                    color = dialogTextSecondary,
                )
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Amount (LKR)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = withdrawError,
                    supportingText = if (withdrawError) {
                        { Text("Cannot exceed current savings (LKR ${currentAmount.toInt()})", color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = dialogSurfaceVariantColor.copy(alpha = 0.3f),
                        focusedContainerColor = dialogSurfaceVariantColor.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = activeColor,
                        errorBorderColor = MaterialTheme.colorScheme.error,
                        unfocusedTextColor = dialogTextPrimary,
                        focusedTextColor = dialogTextPrimary,
                    ),
                    singleLine = true,
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
                enabled = !withdrawError && (input.toDoubleOrNull()?.let { it > 0 } == true),
                colors = ButtonDefaults.buttonColors(containerColor = activeColor),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(if (isWithdrawMode) "Withdraw" else "Add", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = dialogSurfaceVariantColor,
                    contentColor = dialogTextSecondary,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Cancel")
            }
        },
    )
}

// ── Helper Functions ──────────────────────────────────────────────────────────

data class SavingsEntry(val month: String, val amount: Double)

enum class GoalStatus { ON_TRACK, AT_RISK, OFF_TRACK }

private fun determineGoalStatus(
    currentAmount: Double,
    targetAmount: Double,
    monthlyRequired: Double,
    deadline: Date?,
): GoalStatus {
    if (targetAmount <= 0 || deadline == null) return GoalStatus.OFF_TRACK
    val remaining = targetAmount - currentAmount
    val now = System.currentTimeMillis()
    val monthsRemaining = ((deadline.time - now) / (1000L * 60L * 60L * 24L * 30L)).toInt()
    if (monthsRemaining <= 0) return if (currentAmount >= targetAmount) GoalStatus.ON_TRACK else GoalStatus.OFF_TRACK
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
    if (remaining <= 0) return "Complete!"
    val monthsNeeded = (remaining / monthlyRequired).toInt().coerceAtLeast(0)
    val calendar = Calendar.getInstance().apply { add(Calendar.MONTH, monthsNeeded) }
    return SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(calendar.time)
}

private fun formatDate(date: Date?): String {
    if (date == null) return "No deadline"
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
}