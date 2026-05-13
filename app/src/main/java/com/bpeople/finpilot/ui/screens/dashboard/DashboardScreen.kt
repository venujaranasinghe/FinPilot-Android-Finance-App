package com.bpeople.finpilot.ui.screens.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.DonutPieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.theme.ExpenseRed
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.ui.theme.IncomeGreen
import com.bpeople.finpilot.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.absoluteValue

// ── Colour palettes ───────────────────────────────────────────────────────────

private val IncomePalette = listOf(
    Color(0xFF6366F1), // Indigo  – Salary
    Color(0xFF2DD4BF), // Teal    – Freelance
    Color(0xFFF59E0B), // Amber   – AdSense
    Color(0xFF8B5CF6), // Purple  – Crypto
    Color(0xFFF97316), // Orange  – fallback
)

private val ExpensePalette = listOf(
    Color(0xFFF97316),
    Color(0xFFEF4444),
    Color(0xFF8B5CF6),
    Color(0xFF14B8A6),
    Color(0xFF6B7280),
    Color(0xFF3B82F6),
    Color(0xFF10B981),
)

private val GoalGradient = listOf(Color(0xFFF97316), Color(0xFFFB923C), Color(0xFFFDBA74))

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatLKR(amount: Double): String = when {
    amount >= 1_000_000 -> "LKR %.2fM".format(amount / 1_000_000)
    amount >= 1_000     -> "LKR %.0fK".format(amount / 1_000)
    else                -> "LKR ${amount.roundToInt()}"
}

private fun formatLKRFull(amount: Double): String = "LKR %,.0f".format(amount)

private fun incomeSourceIcon(source: String): ImageVector = when {
    source.contains("Salary", ignoreCase = true)   -> Icons.Default.AccountBalance
    source.contains("Freelance", ignoreCase = true) -> Icons.Default.Work
    source.contains("AdSense", ignoreCase = true)   -> Icons.Default.PhoneAndroid
    source.contains("Crypto", ignoreCase = true)    -> Icons.Default.CurrencyBitcoin
    source.contains("Stock", ignoreCase = true) ||
    source.contains("Investment", ignoreCase = true) -> Icons.Default.ShowChart
    else                                             -> Icons.Default.AttachMoney
}

// ── Shared Components ─────────────────────────────────────────────────────────

/** Card shell with 20dp radius, subtle border and elevated shadow. */
@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp),
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content() }
    }
}

/** Section title with a 3 dp coloured left accent bar. */
@Composable
private fun SectionHeader(title: String, subtitle: String? = null) {
    val accentColor = MaterialTheme.colorScheme.primary
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(if (subtitle != null) 32.dp else 20.dp)
                .background(accentColor, RoundedCornerShape(2.dp)),
        )
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                letterSpacing = 0.1.sp,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.2.sp,
                )
            }
        }
    }
}

/** Gradient-filled custom progress bar drawn via Canvas. */
@Composable
private fun GradientProgressBar(
    progress: Float,
    gradientColors: List<Color>,
    trackColor: Color,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000),
        label = "gradient_bar",
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val cornerRadius = CornerRadius(this.size.height / 2)
        // Track
        drawRoundRect(color = trackColor, cornerRadius = cornerRadius)
        // Fill
        if (animatedProgress > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = gradientColors,
                    endX = this.size.width * animatedProgress,
                ),
                size = Size(this.size.width * animatedProgress, this.size.height),
                cornerRadius = cornerRadius,
            )
        }
    }
}

// ── Main Screen ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    state: DashboardViewModel.DashboardUiState,
    userName: String = "",
    insightMessage: String? = null,
    onAddExpense: () -> Unit = {},
    onNavigateToIncome: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var localInsight by remember(insightMessage) { mutableStateOf(insightMessage) }
    var isRefreshing by remember { mutableStateOf(false) }
    var showQuickAddDialog by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) return@LaunchedEffect
        kotlinx.coroutines.delay(600)
        isRefreshing = false
    }

    val hasData = state.totalIncome > 0.0 || state.totalExpenses > 0.0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier
                    .size(60.dp)
                    .padding(bottom = 80.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Add",
                    modifier = Modifier.size(26.dp),
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            state = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = true },
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
            ) {
                item {
                    DashboardHeader(
                        totalIncome = state.totalIncome,
                        totalExpenses = state.totalExpenses,
                        netPosition = state.netPosition,
                        userName = userName,
                        onLogout = onLogout,
                    )
                }

                val insight = localInsight
                if (!insight.isNullOrBlank()) {
                    item {
                        InsightBanner(
                            message = insight,
                            onDismiss = { localInsight = null },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                }

                if (!hasData) {
                    item {
                        EmptyStateSection(
                            onAddExpense = onAddExpense,
                            onNavigateToIncome = onNavigateToIncome,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }
                } else {
                    item {
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            MonthOverMonthContent(
                                currentMonthLabel = state.currentMonthLabel,
                                previousMonthLabel = state.previousMonthLabel,
                                comparisons = state.monthOverMonthComparisons,
                            )
                        }
                    }
                    item {
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            TopCategoryInsightsContent(
                                currentMonthLabel = state.currentMonthLabel,
                                previousMonthLabel = state.previousMonthLabel,
                                insights = state.topCategoryInsights,
                            )
                        }
                    }
                    item {
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            DidYouKnowContent(message = state.didYouKnowInsight)
                        }
                    }
                    item {
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            RecentTransactionsContent(transactions = state.recentTransactions)
                        }
                    }
                    item {
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            IncomeBreakdownContent(incomeBreakdown = state.incomeBreakdown)
                        }
                    }
                    item {
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            SpendingChartContent(expensesByCategory = state.expensesByCategory)
                        }
                    }
                    item {
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            GoalProgressContent(
                                activeGoal = state.activeGoal,
                                allGoals = state.allGoals,
                                progressPercent = state.goalProgressPercent,
                                monthlyRequired = state.monthlyRequired,
                                onNavigateToGoals = onNavigateToGoals,
                            )
                        }
                    }
                    item {
                        SectionCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                            BudgetRatioContent(
                                fixedPercentage = state.fixedCostsPercentage,
                                discretionaryPercentage = state.discretionaryPercentage,
                                totalExpenses = state.totalExpenses,
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
        FinPilotBottomNavBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            currentTab = NavTab.DASHBOARD,
            onNavigateToDashboard = {},
            onNavigateToIncome = onNavigateToIncome,
            onNavigateToExpense = onAddExpense,
            onNavigateToGoals = onNavigateToGoals,
            onNavigateToProfile = onNavigateToProfile,
        )

        if (showQuickAddDialog) {
            QuickAddDialog(
                onDismiss = { showQuickAddDialog = false },
                onAddExpenseIncome = {
                    showQuickAddDialog = false
                    onAddExpense()
                },
                onAddGoal = {
                    showQuickAddDialog = false
                    onNavigateToGoals()
                },
            )
        }
        }
    }
}

// ── Quick Add Dialog ──────────────────────────────────────────────────────────

@Composable
private fun QuickAddDialog(
    onDismiss: () -> Unit,
    onAddExpenseIncome: () -> Unit,
    onAddGoal: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Title
                Text(
                    text = "What would you like to add?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Expense / Income option
                QuickAddOption(
                    icon = Icons.Default.AttachMoney,
                    label = "Expense / Income",
                    description = "Record a transaction",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = onAddExpenseIncome,
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Goal option
                QuickAddOption(
                    icon = Icons.Default.Flag,
                    label = "Goal",
                    description = "Set a savings target",
                    iconTint = WarningAmber,
                    onClick = onAddGoal,
                )
            }
        }
    }
}

@Composable
private fun QuickAddOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(iconTint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader(
    totalIncome: Double,
    totalExpenses: Double,
    netPosition: Double,
    userName: String,
    onLogout: () -> Unit,
) {
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hourOfDay < 12 -> "Good morning"
        hourOfDay < 17 -> "Good afternoon"
        else           -> "Good evening"
    }
    val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMM yyyy"))
    val isPositive = netPosition >= 0
    val savingsRate = if (totalIncome > 0) ((netPosition / totalIncome) * 100).roundToInt() else 0
    val savingsRateColor = when {
        savingsRate >= 20 -> IncomeGreen
        savingsRate >= 10 -> WarningAmber
        else              -> ExpenseRed
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Gradient wash
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.72f), bgColor),
                        endY = size.height,
                    ),
                )
                // Decorative highlight circles
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = 190.dp.toPx(),
                    center = Offset(size.width * 0.88f, size.height * 0.08f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = 130.dp.toPx(),
                    center = Offset(size.width * 0.04f, size.height * 0.50f),
                )
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 32.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            // Greeting row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = greeting,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f),
                        letterSpacing = 0.3.sp,
                    )
                    Text(
                        text = "${userName.ifBlank { "there" }} 👋",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.60f),
                        letterSpacing = 0.2.sp,
                    )
                }

                IconButton(
                    onClick = onLogout,
                    modifier = Modifier
                        .background(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape,
                        )
                        .size(44.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign out",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }

            // Net Balance card with left accent bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                ) {
                    // Left accent bar
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.55f),
                                    ),
                                ),
                                shape = RoundedCornerShape(topStart = 22.dp, bottomStart = 22.dp),
                            ),
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 18.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "NET BALANCE  ·  THIS MONTH",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.5.sp,
                        )

                        Text(
                            text = formatLKRFull(netPosition),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPositive) IncomeGreen else ExpenseRed,
                        )

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            HeroMetric(
                                label = "INCOME",
                                value = formatLKR(totalIncome),
                                isPositive = true,
                            )
                            // Vertical divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            )
                            HeroMetric(
                                label = "EXPENSES",
                                value = formatLKR(totalExpenses),
                                isPositive = false,
                            )
                            // Vertical divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            )
                            SavingsRateBadge(
                                rate = savingsRate,
                                color = savingsRateColor,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMetric(label: String, value: String, isPositive: Boolean) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(
                imageVector = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = if (isPositive) IncomeGreen else ExpenseRed,
            )
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
        }
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) IncomeGreen else ExpenseRed,
        )
    }
}

@Composable
private fun SavingsRateBadge(rate: Int, color: Color) {
    Box(contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { (rate / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.size(52.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 5.dp,
            strokeCap = StrokeCap.Round,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                text = "$rate%",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
            )
            Text(
                text = "SAVED",
                fontSize = 7.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

// ── Insight Banner ────────────────────────────────────────────────────────────

@Composable
private fun InsightBanner(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = WarningAmber
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp),
            )
            .background(accentColor.copy(alpha = 0.08f))
            .height(IntrinsicSize.Min),
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .fillMaxHeight()
                .background(accentColor, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("💡", fontSize = 18.sp)
            Text(
                text = message,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                lineHeight = 18.sp,
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                Text(
                    "✕",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ── Monthly Insights ──────────────────────────────────────────────────────────

@Composable
private fun MonthOverMonthContent(
    currentMonthLabel: String,
    previousMonthLabel: String,
    comparisons: List<DashboardViewModel.MonthComparison>,
) {
    SectionHeader(
        title = "Month-over-Month",
        subtitle = "$currentMonthLabel vs $previousMonthLabel",
    )

    if (comparisons.isEmpty()) {
        EmptyDataHint("Not enough data to compare months yet.")
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        comparisons.forEach { comparison ->
            MonthComparisonCard(comparison = comparison)
        }
    }
}

@Composable
private fun MonthComparisonCard(comparison: DashboardViewModel.MonthComparison) {
    val change = comparison.changePercentage
    val directionUp = change >= 0.0
    val trendIsGood = if (comparison.increaseIsGood) change >= 0.0 else change <= 0.0

    val accentColor = when {
        change == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
        trendIsGood -> IncomeGreen
        else -> ExpenseRed
    }
    val deltaPrefix = if (change > 0.0) "+" else ""

    Column(
        modifier = Modifier
            .width(172.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = 0.07f))
            .border(0.5.dp, accentColor.copy(alpha = 0.30f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = comparison.label.uppercase(),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.8.sp,
            )
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(accentColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (directionUp) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        Text(
            text = formatLKRFull(comparison.currentAmount),
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Box(
            modifier = Modifier
                .background(accentColor.copy(alpha = 0.13f), RoundedCornerShape(20.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        ) {
            Text(
                text = "$deltaPrefix${"%.1f".format(change)}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            thickness = 0.5.dp,
        )

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "LAST MONTH",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.6.sp,
            )
            Text(
                text = formatLKRFull(comparison.previousAmount),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun TopCategoryInsightsContent(
    currentMonthLabel: String,
    previousMonthLabel: String,
    insights: List<DashboardViewModel.CategoryInsight>,
) {
    SectionHeader(
        title = "Top 3 Category Insights",
        subtitle = "$currentMonthLabel spending leaders",
    )

    if (insights.isEmpty()) {
        EmptyDataHint("No current-month category data yet.")
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        insights.forEachIndexed { index, insight ->
            TopCategoryInsightCard(
                rank = index + 1,
                previousMonthLabel = previousMonthLabel,
                insight = insight,
            )
        }
    }
}

private val RankGold = Color(0xFFFFB300)
private val RankSilver = Color(0xFF9E9E9E)
private val RankBronze = Color(0xFFBF8553)

@Composable
private fun TopCategoryInsightCard(
    rank: Int,
    previousMonthLabel: String,
    insight: DashboardViewModel.CategoryInsight,
) {
    val change = insight.changePercentage
    val isDown = change <= 0.0
    val changeColor = when {
        change == 0.0 -> MaterialTheme.colorScheme.onSurfaceVariant
        isDown -> IncomeGreen
        else -> ExpenseRed
    }
    val trendPrefix = if (change > 0.0) "+" else ""
    val rankColor = when (rank) {
        1 -> RankGold
        2 -> RankSilver
        else -> RankBronze
    }
    val shareProgress = (insight.sharePercentage / 100).coerceIn(0.0, 1.0).toFloat()

    Column(
        modifier = Modifier
            .width(195.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f))
            .border(
                width = 0.5.dp,
                color = rankColor.copy(alpha = 0.35f),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Rank badge + change chip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(rankColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text = "#$rank",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = rankColor,
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Icon(
                    imageVector = if (change >= 0.0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = changeColor,
                    modifier = Modifier.size(11.dp),
                )
                Text(
                    text = "$trendPrefix${"%.1f".format(change)}%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = changeColor,
                )
            }
        }

        // Category name
        Text(
            text = insight.category,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Current spend
        Text(
            text = formatLKRFull(insight.currentAmount),
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ExpenseRed,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        // Share of total spend progress
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "OF TOTAL",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp,
                )
                Text(
                    text = "${insight.sharePercentage.roundToInt()}%",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(shareProgress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(ExpenseRed, ExpenseRed.copy(alpha = 0.65f)),
                            ),
                        ),
                )
            }
        }

        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
            thickness = 0.5.dp,
        )

        // vs previous month
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "VS ${previousMonthLabel.uppercase()}",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp,
            )
            Text(
                text = formatLKRFull(insight.previousAmount),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DidYouKnowContent(message: String?) {
    SectionHeader(title = "Did You Know?", subtitle = "Auto-generated insight")

    if (message.isNullOrBlank()) {
        EmptyDataHint("Add more transactions this month to unlock contextual insights.")
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WarningAmber.copy(alpha = 0.10f))
            .border(
                width = 0.5.dp,
                color = WarningAmber.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp),
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = "💡",
            fontSize = 16.sp,
        )
        Text(
            text = message,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RecentTransactionsContent(transactions: List<DashboardViewModel.RecentTransaction>) {
    SectionHeader(title = "Recent Transactions", subtitle = "Latest 5 entries across income and expenses")

    if (transactions.isEmpty()) {
        EmptyDataHint("No recent transactions to show yet.")
        return
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        transactions.forEach { transaction ->
            RecentTransactionRow(
                transaction = transaction,
                formattedDate = dateFormat.format(transaction.dateMillis),
            )
        }
    }
}

@Composable
private fun RecentTransactionRow(
    transaction: DashboardViewModel.RecentTransaction,
    formattedDate: String,
) {
    val accentColor = if (transaction.isExpense) ExpenseRed else IncomeGreen
    val amountPrefix = if (transaction.isExpense) "-" else "+"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accentColor.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (transaction.isExpense) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = transaction.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$amountPrefix${formatLKRFull(transaction.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ── Income Breakdown ──────────────────────────────────────────────────────────

@Composable
private fun IncomeBreakdownContent(incomeBreakdown: Map<String, Double>) {
    val totalIncome = incomeBreakdown.values.sum().coerceAtLeast(1.0)
    val sorted = incomeBreakdown.entries.sortedByDescending { it.value }

    SectionHeader(title = "Income This Month", subtitle = "Breakdown by source")

    if (incomeBreakdown.isEmpty()) {
        EmptyDataHint("No income entries recorded yet.")
        return
    }

    sorted.forEachIndexed { index, (source, amount) ->
        IncomeSourceRow(
            source = source,
            amount = amount,
            percentage = amount / totalIncome * 100,
            color = IncomePalette.getOrElse(index) { IncomePalette.last() },
        )
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        thickness = 1.dp,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Total Income",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = formatLKRFull(totalIncome),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = IncomeGreen,
        )
    }
}

@Composable
private fun IncomeSourceRow(source: String, amount: Double, percentage: Double, color: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(750),
        label = "income_$source",
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon badge
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = incomeSourceIcon(source),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp),
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = source,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Percentage chip
                    Box(
                        modifier = Modifier
                            .background(color.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "${percentage.roundToInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                        )
                    }
                    Text(
                        text = formatLKRFull(amount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                listOf(color, color.copy(alpha = 0.65f)),
                            ),
                        ),
                )
            }
        }
    }
}

// ── Spending Donut Chart ──────────────────────────────────────────────────────

@Composable
private fun SpendingChartContent(expensesByCategory: Map<String, Double>) {
    SectionHeader(
        title = "Spending by Category",
        subtitle = "Where your money went this month",
    )

    if (expensesByCategory.isEmpty()) {
        EmptyDataHint("No expenses recorded yet.")
        return
    }

    val totalExpenses = expensesByCategory.values.sum().coerceAtLeast(1.0)
    val sorted = expensesByCategory.entries.sortedByDescending { it.value }

    val slices = sorted.mapIndexed { index, (category, amount) ->
        PieChartData.Slice(
            label = category,
            value = amount.toFloat(),
            color = ExpensePalette.getOrElse(index) { ExpensePalette.last() },
        )
    }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val pieChartConfig = PieChartConfig(
        strokeWidth = 58f,
        isAnimationEnable = true,
        showSliceLabels = false,
        isSumVisible = false,
        backgroundColor = surfaceColor,
        activeSliceAlpha = 0.9f,
        inActiveSliceAlpha = 0.65f,
    )

    DonutPieChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(340.dp),
        pieChartData = PieChartData(slices = slices, plotType = PlotType.Donut),
        pieChartConfig = pieChartConfig,
    )

    // Total spend summary row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "TOTAL SPENT",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 1.sp,
        )
        Text(
            text = formatLKRFull(totalExpenses),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ExpenseRed,
        )
    }

    HorizontalDivider(
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
        thickness = 1.dp,
    )

    // Legend
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        sorted.forEachIndexed { index, (category, amount) ->
            val color = ExpensePalette.getOrElse(index) { ExpensePalette.last() }
            val pct = (amount / totalExpenses * 100).roundToInt()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .background(color.copy(alpha = 0.10f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "$pct%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = color,
                        )
                    }
                    Text(
                        text = formatLKRFull(amount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                    )
                }
            }
        }
    }
}

// ── Goal Progress ─────────────────────────────────────────────────────────────

@Composable
private fun GoalProgressContent(
    activeGoal: Goal?,
    allGoals: List<Goal>,
    progressPercent: Float,
    monthlyRequired: Double,
    onNavigateToGoals: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionHeader(title = "Savings Goals", subtitle = "Track all your targets")
            if (activeGoal != null) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp),
                )
            }
        }

        if (activeGoal == null) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🎯", fontSize = 32.sp)
                }
                Text(
                    text = "No active savings goal",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "Set a goal to track your savings progress and stay on target.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                )
                OutlinedButton(
                    onClick = onNavigateToGoals,
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Set a Savings Goal", fontSize = 13.sp)
                }
            }
            return
        }

        // Active goal card
        ActiveGoalCard(
            goal = activeGoal,
            progressPercent = progressPercent,
            monthlyRequired = monthlyRequired,
        )

        // Other goals section
        if (allGoals.size > 1) {
            val otherGoals = allGoals.filter { it.id != activeGoal.id }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Other Goals",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${otherGoals.size} goals",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    otherGoals.forEach { goal ->
                        GoalMiniCard(goal = goal)
                    }
                }
            }
        }

        // View all button
        OutlinedButton(
            onClick = onNavigateToGoals,
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            shape = RoundedCornerShape(10.dp),
        ) {
            Text("Manage All Goals", fontSize = 13.sp)
        }
    }
}

@Composable
private fun ActiveGoalCard(
    goal: Goal,
    progressPercent: Float,
    monthlyRequired: Double,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1200),
        label = "goal_progress",
    )
    val completedPct = (animatedProgress * 100).roundToInt()
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp),
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Title row + badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "Active Goal",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.3.sp,
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                completedPct >= 100 -> IncomeGreen
                                completedPct >= 50  -> IncomeGreen.copy(alpha = 0.15f)
                                else                -> MaterialTheme.colorScheme.primary
                            },
                            shape = RoundedCornerShape(20.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        text = "$completedPct%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            completedPct >= 100 -> Color.White
                            completedPct >= 50  -> IncomeGreen
                            else                -> Color.White
                        },
                    )
                }
            }

            // Saved / Target / Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                GoalMetricCell(
                    label = "Saved",
                    value = formatLKRFull(goal.currentAmount),
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .align(Alignment.CenterVertically)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                )
                GoalMetricCell(
                    label = "Target",
                    value = formatLKRFull(goal.targetAmount),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    align = Alignment.CenterHorizontally,
                )
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .align(Alignment.CenterVertically)
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                )
                GoalMetricCell(
                    label = "Remaining",
                    value = formatLKRFull(remaining),
                    color = WarningAmber,
                    modifier = Modifier.weight(1f),
                    align = Alignment.End,
                )
            }

            // Gradient progress bar
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                GradientProgressBar(
                    progress = animatedProgress,
                    gradientColors = GoalGradient,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    height = 12.dp,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "LKR 0",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = formatLKRFull(goal.targetAmount),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Monthly savings tip
            if (monthlyRequired > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(IncomeGreen.copy(alpha = 0.1f))
                        .border(0.5.dp, IncomeGreen.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = IncomeGreen,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Save ${formatLKRFull(monthlyRequired)}/month",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                        lineHeight = 16.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalMiniCard(goal: Goal) {
    val progressPercent = if (goal.targetAmount > 0.0) {
        (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
    } else {
        0f
    }
    val completedPct = (progressPercent * 100).roundToInt()
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp),
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = goal.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "$completedPct%",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        completedPct >= 100 -> IncomeGreen
                        completedPct >= 50  -> WarningAmber
                        else                -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatLKRFull(goal.currentAmount),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen,
                    )
                    Text(
                        text = "Saved",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formatLKRFull(remaining),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningAmber,
                    )
                    Text(
                        text = "Remaining",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            GradientProgressBar(
                progress = progressPercent,
                gradientColors = GoalGradient,
                trackColor = MaterialTheme.colorScheme.surface,
                height = 6.dp,
            )
        }
    }
}

@Composable
private fun GoalMetricCell(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier,
    align: Alignment.Horizontal = Alignment.Start,
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = align,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.5.sp,
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Budget Ratio ──────────────────────────────────────────────────────────────

@Composable
private fun BudgetRatioContent(
    fixedPercentage: Double,
    discretionaryPercentage: Double,
    totalExpenses: Double,
) {
    SectionHeader(title = "Budget Allocation", subtitle = "Committed vs flexible spending")

    // Combined split bar
    val totalPct = (fixedPercentage + discretionaryPercentage).coerceAtLeast(1.0)
    val fixedWeight = (fixedPercentage / totalPct).toFloat().coerceIn(0.01f, 0.99f)
    val discWeight = 1f - fixedWeight

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp)),
        ) {
            Box(
                modifier = Modifier
                    .weight(fixedWeight)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(ExpenseRed, ExpenseRed.copy(alpha = 0.75f)),
                        ),
                    ),
            )
            Box(
                modifier = Modifier
                    .weight(discWeight)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFF818CF8).copy(alpha = 0.75f), Color(0xFF818CF8)),
                        ),
                    ),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LegendDot(color = ExpenseRed, label = "Fixed  ${fixedPercentage.roundToInt()}%")
            LegendDot(color = Color(0xFF818CF8), label = "Flexible  ${discretionaryPercentage.roundToInt()}%")
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        RatioTile(
            label = "Fixed Costs",
            percentage = fixedPercentage,
            amount = totalExpenses * (fixedPercentage / 100),
            color = ExpenseRed,
            modifier = Modifier.weight(1f),
        )
        RatioTile(
            label = "Discretionary",
            percentage = discretionaryPercentage,
            amount = totalExpenses * (discretionaryPercentage / 100),
            color = Color(0xFF818CF8),
            modifier = Modifier.weight(1f),
        )
    }

    // Advice banner
    val (adviceIcon, adviceText, adviceColor) = when {
        fixedPercentage > 70 -> Triple("⚠️", "Fixed costs are very high. Consider reducing recurring expenses to improve cash flow.", WarningAmber)
        fixedPercentage > 50 -> Triple("📊", "Fixed costs exceed the 50% benchmark. Look for opportunities to cut committed spending.", MaterialTheme.colorScheme.primary)
        else                 -> Triple("✅", "Your fixed costs are healthy — you have solid financial flexibility.", IncomeGreen)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(adviceColor.copy(alpha = 0.08f))
            .border(0.5.dp, adviceColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(adviceIcon, fontSize = 14.sp)
        Text(
            text = adviceText,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
            lineHeight = 17.sp,
        )
    }
}

private fun committedDiscretionaryHint(fixedCostsPercentage: Double): String {
    val fixedRounded = fixedCostsPercentage.coerceAtLeast(0.0).roundToInt()
    return if (fixedCostsPercentage >= 50.0) {
        "Your fixed costs are ${fixedRounded}% of income. Aim for <50% for better flexibility."
    } else {
        "Nice! Your fixed costs are ${fixedRounded}% of income — below the recommended 50% threshold."
    }
}

private fun chartColorForIndex(index: Int): Color {
    val palette = listOf(
        Color(0xFF1976D2),
        Color(0xFF388E3C),
        Color(0xFFFBC02D),
        Color(0xFFD32F2F),
        Color(0xFF7B1FA2),
        Color(0xFF00838F),
    )
    return palette[index % palette.size]
}

private fun categoryColor(category: String): Color {
    // A small deterministic palette based on category text.
    val colors = listOf(
        Color(0xFFFF6F00),
        Color(0xFF1976D2),
        Color(0xFF7B1FA2),
        Color(0xFF388E3C),
        Color(0xFFD32F2F),
        Color(0xFF455A64),
    )
    val idx = (category.trim().lowercase().hashCode().absoluteValue) % colors.size
    return colors[idx]
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RatioTile(
    label: String,
    percentage: Double,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (percentage / 100).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(750),
        label = "ratio_$label",
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(color.copy(alpha = 0.07f))
            .border(0.5.dp, color.copy(alpha = 0.20f), RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = "${percentage.roundToInt()}%",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(color),
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 0.2.sp,
        )
        Text(
            text = formatLKR(amount),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

// ── Empty States ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyStateSection(
    onAddExpense: () -> Unit,
    onNavigateToIncome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp),
            )
            .background(MaterialTheme.colorScheme.surface)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("📊", fontSize = 36.sp, textAlign = TextAlign.Center)
        }
        Text(
            text = "Your dashboard is empty",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Add your first income or expense to unlock charts, goal tracking, and your complete financial snapshot.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onNavigateToIncome,
                shape = RoundedCornerShape(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("Add Income", fontSize = 13.sp)
            }
            androidx.compose.material3.Button(
                onClick = onAddExpense,
                shape = RoundedCornerShape(10.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("Add Expense", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun EmptyDataHint(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Dashboard – data (light)")
@Composable
private fun DashboardPreviewLight() {
    FinPilotTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardViewModel.DashboardUiState(
                totalIncome = 600000.0,
                totalExpenses = 45000.0,
                netPosition = 555000.0,
                activeGoal = Goal(
                    id = "p", userId = "u",
                    title = "Emergency Fund",
                    targetAmount = 490000.0,
                    currentAmount = 95000.0,
                    monthlyRequired = 20000.0,
                    isActive = true,
                ),
                goalProgressPercent = 0.19f,
                monthlyRequired = 20000.0,
                incomeBreakdown = mapOf(
                    "Salary" to 450000.0,
                    "Freelance" to 100000.0,
                    "AdSense" to 30000.0,
                    "Crypto" to 20000.0,
                ),
                expensesByCategory = mapOf(
                    "Food" to 18000.0,
                    "Transport" to 12000.0,
                    "Entertainment" to 8000.0,
                    "Other" to 7000.0,
                ),
                fixedCostsPercentage = 65.0,
                discretionaryPercentage = 35.0,
            ),
            insightMessage = "Tip: Keep fixed costs below 50% of income.",
        )
    }
}

@Preview(showBackground = true, name = "Dashboard – empty state")
@Composable
private fun DashboardPreviewEmpty() {
    FinPilotTheme(darkTheme = false) {
        DashboardScreen(state = DashboardViewModel.DashboardUiState())
    }
}

@Preview(showBackground = true, name = "Dashboard – data (dark)")
@Composable
private fun DashboardPreviewDark() {
    FinPilotTheme(darkTheme = true) {
        DashboardScreen(
            state = DashboardViewModel.DashboardUiState(
                totalIncome = 600000.0,
                totalExpenses = 45000.0,
                netPosition = 555000.0,
                fixedCostsPercentage = 65.0,
                discretionaryPercentage = 35.0,
                incomeBreakdown = mapOf(
                    "Salary" to 450000.0,
                    "Freelance" to 100000.0,
                    "AdSense" to 30000.0,
                    "Crypto" to 20000.0,
                ),
                expensesByCategory = mapOf(
                    "Food" to 18000.0,
                    "Transport" to 12000.0,
                    "Entertainment" to 8000.0,
                    "Other" to 7000.0,
                ),
            ),
        )
    }
}
