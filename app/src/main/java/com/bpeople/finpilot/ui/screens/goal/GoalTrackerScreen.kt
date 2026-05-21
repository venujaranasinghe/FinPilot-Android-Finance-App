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
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

private val OrangeAccent = Color(0xFFFF6B00)
private val OrangeSoft = Color(0xFFFF8F3C)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalTrackerScreen(
    viewModel: GoalViewModel,
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
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
        onNavigateToTransactions = onNavigateToTransactions,
        onNavigateToGoals = {},
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToIncome = onNavigateToIncome,
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
) {
    val allGoals = goalState.allGoals
    val activeGoal = goalState.activeGoal
    var animationTrigger by remember { mutableStateOf(false) }

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
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (activeGoal != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    // ── Gradient header ──────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        OrangeAccent,
                                        OrangeSoft.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.background,
                                    ),
                                ),
                            )
                            .padding(top = 44.dp, bottom = 20.dp),
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    "My Goals",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = onOpenCreate,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    IconButton(
                                        onClick = { activeGoal.let { onOpenEdit(it) } },
                                        modifier = Modifier
                                            .size(40.dp)
                                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            HorizontalPager(
                                state = pagerState,
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 40.dp),
                                pageSpacing = 16.dp,
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
                                val pageScale by animateFloatAsState(
                                    targetValue = if (page == pagerState.currentPage) 1f else 0.88f,
                                    animationSpec = tween(300),
                                    label = "scale",
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth().scale(pageScale),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    EnhancedGoalCard(
                                        goal = goal,
                                        progress = progressAnimatable.value,
                                        targetProgress = targetProgress,
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (allGoals.size > 1) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    repeat(allGoals.size) { index ->
                                        val selected = index == pagerState.currentPage
                                        val dotWidth by animateFloatAsState(
                                            targetValue = if (selected) 20f else 8f,
                                            animationSpec = tween(300),
                                            label = "dot",
                                        )
                                        Box(
                                            modifier = Modifier
                                                .width(dotWidth.dp)
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (selected) Color.White
                                                    else Color.White.copy(alpha = 0.4f),
                                                ),
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    // ── White surface details ────────────────────────────
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp,
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp)
                                .padding(top = 24.dp, bottom = 100.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                        ) {
                            GoalStatusBadge(goal = activeGoal, monthlyRequired = goalState.monthlyRequired)

                            TimelineIntelligenceCard(
                                goal = activeGoal,
                                monthlyRequired = goalState.monthlyRequired,
                                savingsHistory = savingsHistory,
                            )

                            GroupedSavingsBarChart(
                                savingsHistory = savingsHistory,
                                monthlyRequired = goalState.monthlyRequired,
                                animationTrigger = animationTrigger,
                            )

                            SavingsStreakCard(savingsHistory = savingsHistory)

                            SmartSuggestionCard(
                                goal = activeGoal,
                                monthlyRequired = goalState.monthlyRequired,
                                onNavigateToExpense = onNavigateToExpense,
                            )

                            LogSavingsSection(
                                onLogSavings = onLogSavings,
                                onWithdrawSavings = onWithdrawSavings,
                                goal = activeGoal,
                            )

                            Spacer(modifier = Modifier.height(20.dp))
                        }
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
            )
        }
    }
}

// ── Enhanced goal card with milestone ring + laptop illustration ─────────────

@Composable
private fun EnhancedGoalCard(
    goal: Goal,
    progress: Float,
    targetProgress: Float,
) {
    var showConfetti by remember { mutableStateOf(false) }
    val celebratedMilestones = remember { mutableSetOf<Int>() }

    LaunchedEffect(targetProgress) {
        val p = (targetProgress * 100).toInt()
        val newMilestone = listOf(25, 50, 75, 100).firstOrNull { it <= p && it !in celebratedMilestones }
        if (newMilestone != null) {
            celebratedMilestones.add(newMilestone)
            showConfetti = true
        }
    }

    Box(contentAlignment = Alignment.Center) {
        MilestoneRing(progress = progress, modifier = Modifier.size(210.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Canvas(modifier = Modifier.size(76.dp)) { drawLaptopIllustration() }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${(progress * 100).roundToInt()}%",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = "saved",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.75f),
            )
        }

        if (showConfetti) {
            ConfettiOverlay(
                modifier = Modifier.size(210.dp),
                onDone = { showConfetti = false },
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))
    Text(
        text = goal.title,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center,
    )
    Text(
        text = "LKR ${String.format("%,d", goal.currentAmount.toInt())} / LKR ${String.format("%,d", goal.targetAmount.toInt())}",
        fontSize = 13.sp,
        color = Color.White.copy(alpha = 0.75f),
        modifier = Modifier.padding(top = 4.dp),
    )
}

@Composable
private fun MilestoneRing(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val ringRadius = size.minDimension / 2f - strokeWidth / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        val startAngle = -90f

        drawArc(
            color = Color.White.copy(alpha = 0.25f),
            startAngle = startAngle,
            sweepAngle = 360f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(center.x - ringRadius, center.y - ringRadius),
            size = Size(ringRadius * 2, ringRadius * 2),
        )

        val sweep = progress * 360f
        if (sweep > 0f) {
            drawArc(
                brush = Brush.sweepGradient(
                    colors = listOf(Color.White.copy(alpha = 0.7f), Color.White, Color.White.copy(alpha = 0.7f)),
                    center = center,
                ),
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
            val reached = progress >= milestoneProgress
            drawCircle(
                color = if (reached) OrangeAccent else Color.White.copy(alpha = 0.45f),
                radius = 6.dp.toPx(),
                center = Offset(dotX, dotY),
            )
            if (reached) {
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = Offset(dotX, dotY),
                )
            }
        }
    }
}

private fun DrawScope.drawLaptopIllustration() {
    val w = size.width
    val h = size.height

    val screenL = w * 0.10f
    val screenT = h * 0.08f
    val screenR = w * 0.90f
    val screenB = h * 0.60f
    val screenW = screenR - screenL
    val screenH = screenB - screenT

    drawRoundRect(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(screenL, screenT),
        size = Size(screenW, screenH),
        cornerRadius = CornerRadius(6.dp.toPx()),
    )
    drawRoundRect(
        color = OrangeAccent.copy(alpha = 0.28f),
        topLeft = Offset(screenL + 4.dp.toPx(), screenT + 4.dp.toPx()),
        size = Size(screenW - 8.dp.toPx(), screenH - 8.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx()),
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.55f),
        radius = 5.dp.toPx(),
        center = Offset(w * 0.5f, (screenT + screenB) / 2f),
    )

    drawRoundRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(w * 0.22f, screenB),
        size = Size(w * 0.56f, 2.5f.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx()),
    )

    val baseL = w * 0.04f
    val baseT = screenB + 2.5f.dp.toPx()
    val baseR = w * 0.96f
    val baseB = h * 0.88f
    drawRoundRect(
        color = Color.White.copy(alpha = 0.75f),
        topLeft = Offset(baseL, baseT),
        size = Size(baseR - baseL, baseB - baseT),
        cornerRadius = CornerRadius(4.dp.toPx()),
    )

    val kbL = w * 0.11f
    val kbT = baseT + 3.dp.toPx()
    val kbR = w * 0.89f
    val kbB = baseB - 3.dp.toPx()
    val keyRows = 3
    val keyCols = 9
    val rowH = (kbB - kbT) / keyRows
    val colW = (kbR - kbL) / keyCols
    for (row in 0 until keyRows) {
        for (col in 0 until keyCols) {
            drawRoundRect(
                color = OrangeAccent.copy(alpha = 0.22f),
                topLeft = Offset(kbL + col * colW + 1.dp.toPx(), kbT + row * rowH + 1.dp.toPx()),
                size = Size(colW - 2.dp.toPx(), rowH - 2.dp.toPx()),
                cornerRadius = CornerRadius(1.5f.dp.toPx()),
            )
        }
    }
}

@Composable
private fun ConfettiOverlay(modifier: Modifier = Modifier, onDone: () -> Unit) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, animationSpec = tween(2000, easing = LinearEasing))
        onDone()
    }
    val prog = anim.value
    val confettiColors = remember {
        listOf(
            Color(0xFFFF6B00), Color(0xFFFFBB00), Color(0xFF00C4FF),
            Color(0xFF7C3AED), Color(0xFF10B981), Color.White,
        )
    }
    Canvas(modifier = modifier) {
        val rng = Random(42L)
        repeat(32) { i ->
            val startX = rng.nextFloat() * size.width
            val velX = (rng.nextFloat() - 0.5f) * 80.dp.toPx()
            val velY = 100.dp.toPx() + rng.nextFloat() * 120.dp.toPx()
            val pieceX = startX + velX * prog
            val pieceY = -10.dp.toPx() + velY * prog
            val alpha = (1f - prog * 1.4f).coerceIn(0f, 1f)
            if (alpha > 0f && pieceX in 0f..size.width && pieceY in 0f..size.height) {
                drawCircle(
                    color = confettiColors[i % confettiColors.size].copy(alpha = alpha),
                    radius = (3.dp.toPx() + rng.nextFloat() * 3.dp.toPx()),
                    center = Offset(pieceX, pieceY),
                )
            }
        }
    }
}

// ── Timeline intelligence ────────────────────────────────────────────────────

@Composable
private fun TimelineIntelligenceCard(
    goal: Goal,
    monthlyRequired: Double,
    savingsHistory: List<SavingsEntry>,
) {
    val recentSaved = savingsHistory.lastOrNull()?.amount ?: 0.0
    val gap = monthlyRequired - recentSaved
    val gapColor = if (gap <= 0) Color(0xFF10B981) else Color(0xFFEF4444)
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Rounded.TrendingUp, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
            Text(
                "Timeline Intelligence",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp),
            ) {
                Text("Projected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    projectedDate,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text("completion", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(14.dp),
            ) {
                Text("Deadline", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    if (monthsLeft > 0) "$monthsLeft mo left" else formatDate(deadlineDate),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (monthsLeft in 1..2) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(formatDate(deadlineDate), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(gapColor.copy(alpha = 0.1f))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("This month", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(gapText, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = gapColor)
        }
    }
}

// ── Grouped savings bar chart ────────────────────────────────────────────────

@Composable
private fun GroupedSavingsBarChart(
    savingsHistory: List<SavingsEntry>,
    monthlyRequired: Double,
    animationTrigger: Boolean,
) {
    val barAnim by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "bar",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "Monthly Savings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(OrangeAccent))
                Text("Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(MaterialTheme.colorScheme.outlineVariant))
                Text("Required", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (savingsHistory.isNotEmpty()) {
            val maxVal = maxOf(savingsHistory.maxOfOrNull { it.amount } ?: 0.0, monthlyRequired, 1.0)
            val barTrackColor = MaterialTheme.colorScheme.outline

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
                        color = barTrackColor,
                        topLeft = Offset(groupLeft, totalH - reqH),
                        size = Size(barW, reqH),
                        cornerRadius = CornerRadius(3.dp.toPx()),
                    )

                    val savedH = (entry.amount / maxVal * totalH * barAnim).toFloat().coerceAtLeast(0f)
                    if (savedH > 0f) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(OrangeSoft, OrangeAccent),
                                startY = totalH - savedH,
                                endY = totalH,
                            ),
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
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Text(
                "No savings history yet",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                textAlign = TextAlign.Center,
            )
        }
    }
}

// ── Savings streak card ──────────────────────────────────────────────────────

@Composable
private fun SavingsStreakCard(savingsHistory: List<SavingsEntry>) {
    val streak = savingsHistory.reversed().takeWhile { it.amount > 0 }.size
    val fireColor = OrangeAccent

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = if (streak > 0) fireColor else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    "Savings Streak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (streak > 0) fireColor.copy(alpha = 0.13f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            ) {
                Text(
                    "$streak month${if (streak != 1) "s" else ""}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (streak > 0) fireColor else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (savingsHistory.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                savingsHistory.forEach { entry ->
                    val hit = entry.amount > 0
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (hit) fireColor.copy(alpha = 0.13f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (hit) {
                                Icon(
                                    Icons.Rounded.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = fireColor,
                                    modifier = Modifier.size(16.dp),
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
                                )
                            }
                        }
                        Text(
                            entry.month,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (hit) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }
                }
            }
        }
    }
}

// ── Smart suggestion card ────────────────────────────────────────────────────

@Composable
private fun SmartSuggestionCard(
    goal: Goal,
    monthlyRequired: Double,
    onNavigateToExpense: () -> Unit,
) {
    val remaining = goal.targetAmount - goal.currentAmount
    val suggestion = when {
        remaining <= 0 -> "You've reached your goal! Set a new challenge to keep the momentum going."
        monthlyRequired <= 0 -> "Add a deadline to your goal to get a personalised monthly savings plan."
        else -> "You need to save LKR ${String.format("%,d", monthlyRequired.toInt())} per month. Review your discretionary spending to find room."
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(OrangeAccent.copy(alpha = 0.07f))
            .clickable(onClick = onNavigateToExpense)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(OrangeAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(20.dp))
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "Smart Suggestion",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent,
            )
            Text(
                suggestion,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                lineHeight = 18.sp,
            )
            Text(
                "View your expenses →",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = OrangeAccent,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

// ── Motivational empty state ─────────────────────────────────────────────────

@Composable
private fun EmptyGoalState(modifier: Modifier = Modifier, onCreateGoal: () -> Unit) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 32.dp),
        ) {
            Canvas(modifier = Modifier.size(150.dp)) { drawEmptyGoalIllustration() }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                "No Goals Yet",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Set your first savings goal and start building your future, one month at a time.",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
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
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
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

private fun DrawScope.drawEmptyGoalIllustration() {
    val w = size.width
    val h = size.height
    val cx = w * 0.5f
    val cy = h * 0.42f

    drawCircle(color = OrangeAccent.copy(alpha = 0.07f), radius = w * 0.46f, center = Offset(cx, cy))
    drawCircle(color = OrangeAccent.copy(alpha = 0.13f), radius = w * 0.30f, center = Offset(cx, cy))
    drawCircle(color = OrangeAccent.copy(alpha = 0.22f), radius = w * 0.17f, center = Offset(cx, cy))
    drawCircle(color = OrangeAccent, radius = w * 0.07f, center = Offset(cx, cy))

    for (i in 0..3) {
        val coinY = h * 0.84f - i * 7.dp.toPx()
        drawOval(
            color = if (i % 2 == 0) Color(0xFFFFBB00) else Color(0xFFFFA500),
            topLeft = Offset(cx - w * 0.18f, coinY - 4.dp.toPx()),
            size = Size(w * 0.36f, 8.dp.toPx()),
        )
    }
}

// ── Kept composables ─────────────────────────────────────────────────────────

@Composable
private fun GoalStatusBadge(goal: Goal, monthlyRequired: Double) {
    val status = determineGoalStatus(
        currentAmount = goal.currentAmount,
        targetAmount = goal.targetAmount,
        monthlyRequired = monthlyRequired,
        deadline = goal.deadline?.toDate(),
    )

    val (statusText, statusColor, statusBg) = when (status) {
        GoalStatus.ON_TRACK -> Triple("On Track", Color(0xFF10B981), Color(0xFF10B981).copy(alpha = 0.13f))
        GoalStatus.AT_RISK -> Triple("At Risk", Color(0xFFF59E0B), Color(0xFFF59E0B).copy(alpha = 0.13f))
        GoalStatus.OFF_TRACK -> Triple("Off Track", Color(0xFFEF4444), Color(0xFFEF4444).copy(alpha = 0.13f))
    }

    val animBg by animateColorAsState(targetValue = statusBg, label = "bg")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(animBg)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("Current Status", style = MaterialTheme.typography.labelMedium, color = statusColor.copy(alpha = 0.8f))
            Text(
                statusText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(statusColor.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                when (status) {
                    GoalStatus.ON_TRACK -> "✓"
                    GoalStatus.AT_RISK -> "!"
                    GoalStatus.OFF_TRACK -> "✗"
                },
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = statusColor,
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
            containerColor = OrangeAccent,
            contentColor = Color.White,
        ),
    ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Log / Adjust Savings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
        focusedBorderColor = OrangeAccent,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .align(Alignment.CenterHorizontally)
                .background(
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                    RoundedCornerShape(2.dp),
                ),
        )
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isEditing) "Edit Goal" else "Create New Goal",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
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
            placeholder = { Text("e.g. MacBook Pro, Vacation, Emergency Fund") },
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
            label = { Text("Amount Already Saved (LKR)") },
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
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Target Date", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(selectedDateLabel, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
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
                Icon(Icons.Rounded.CalendarToday, contentDescription = "Pick date", tint = OrangeAccent, modifier = Modifier.size(20.dp))
            }
        }

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
        ) {
            Text(
                text = if (isEditing) "Save Changes" else "Create Goal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
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

    val addColor = Color(0xFF10B981)
    val withdrawColor = Color(0xFFEF4444)
    val activeColor by animateColorAsState(
        targetValue = if (isWithdrawMode) withdrawColor else addColor,
        animationSpec = tween(300),
        label = "mode_color",
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Adjust Savings", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                            targetValue = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = activeColor,
                        errorBorderColor = MaterialTheme.colorScheme.error,
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
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text("Cancel")
            }
        },
    )
}

// ── Helper data classes and pure functions ────────────────────────────────────

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
