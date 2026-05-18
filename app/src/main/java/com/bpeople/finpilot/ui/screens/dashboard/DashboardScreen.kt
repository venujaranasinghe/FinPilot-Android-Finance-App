package com.bpeople.finpilot.ui.screens.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ── Colour constants ──────────────────────────────────────────────────────────

private val Orange = Color(0xFFF97316)
private val OrangeLight = Color(0xFFFF8C42)
private val OrangeHero1 = Color(0xFFFF6B35)
private val OrangeHero2 = Color(0xFFFF8C42)
private val IncomePalette = listOf(
    Color(0xFFF97316),
    Color(0xFF4CAF50),
    Color(0xFF2196F3),
    Color(0xFF9C27B0),
    Color(0xFFF59E0B),
)
private val ExpensePalette = listOf(
    Color(0xFFF97316), Color(0xFFEF4444), Color(0xFF8B5CF6),
    Color(0xFF14B8A6), Color(0xFF6B7280), Color(0xFF3B82F6), Color(0xFF10B981),
)
private val GoalGradient = listOf(Color(0xFFF97316), Color(0xFFFB923C), Color(0xFFFDBA74))

// ── Formatters ─────────────────────────────────────────────────────────────────

private fun formatLKR(amount: Double): String = when {
    amount >= 1_000_000 -> "LKR %.2fM".format(amount / 1_000_000)
    amount >= 1_000 -> "LKR %.0fK".format(amount / 1_000)
    else -> "LKR ${amount.roundToInt()}"
}

private fun formatLKRFull(amount: Double): String = "LKR %,.0f".format(amount)

private fun incomeSourceIcon(source: String): ImageVector = when {
    source.contains("Salary", ignoreCase = true) -> Icons.Rounded.AccountBalance
    source.contains("Freelance", ignoreCase = true) -> Icons.Rounded.Work
    source.contains("AdSense", ignoreCase = true) -> Icons.Rounded.PhoneAndroid
    source.contains("Crypto", ignoreCase = true) -> Icons.Rounded.CurrencyBitcoin
    else -> Icons.Default.AttachMoney
}

private fun categoryIcon(category: String): ImageVector = when {
    category.contains("Food", ignoreCase = true) ||
    category.contains("Restaurant", ignoreCase = true) -> Icons.Default.Restaurant
    category.contains("Transport", ignoreCase = true) ||
    category.contains("Car", ignoreCase = true) -> Icons.Default.DirectionsCar
    category.contains("Housing", ignoreCase = true) ||
    category.contains("Rent", ignoreCase = true) -> Icons.Default.Home
    category.contains("Sub", ignoreCase = true) ||
    category.contains("Stream", ignoreCase = true) -> Icons.Default.PlayArrow
    else -> Icons.Default.AttachMoney
}

private fun categoryCircleColor(category: String): Color {
    val idx = (category.trim().lowercase().hashCode().absoluteValue) % ExpensePalette.size
    return ExpensePalette[idx]
}

// ── Section scaffold ──────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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

@Composable
private fun SectionRowHeader(title: String, actionLabel: String = "", onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
        if (actionLabel.isNotBlank()) {
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                color = Orange,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction),
            )
        }
    }
}

@Composable
private fun GradientProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 10.dp,
    gradientColors: List<Color> = GoalGradient,
    trackColor: Color = Color(0xFFF3F4F6),
) {
    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label = "grad_bar",
    )
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val cr = CornerRadius(size.height / 2)
        drawRoundRect(color = trackColor, cornerRadius = cr)
        if (animated > 0f) {
            drawRoundRect(
                brush = Brush.horizontalGradient(gradientColors, endX = size.width * animated),
                size = Size(size.width * animated, size.height),
                cornerRadius = cr,
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
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var balanceVisible by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) return@LaunchedEffect
        kotlinx.coroutines.delay(600)
        isRefreshing = false
    }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { pv ->
        Box(modifier = Modifier.fillMaxSize()) {
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF9FAFB))
                    .padding(pv),
                state = pullState,
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 110.dp),
                ) {
                    // ── Section 1: Header ────────────────────────────────────
                    item {
                        DashboardHeader(userName = userName)
                    }

                    // ── Section 2: Hero Balance Card ─────────────────────────
                    item {
                        HeroBalanceCard(
                            totalBalance = state.netPosition,
                            monthlyIncome = state.totalIncome,
                            monthlyExpenses = state.totalExpenses,
                            balanceVisible = balanceVisible,
                            onToggleVisibility = { balanceVisible = !balanceVisible },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }

                    // ── Section 3: Quick Actions ──────────────────────────────
                    item {
                        QuickActionsRow(
                            onAddIncome = onNavigateToIncome,
                            onAddExpense = onAddExpense,
                            onGoals = onNavigateToGoals,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

                    // ── Section 4: Spending Overview (bar chart) ──────────────
                    item {
                        SectionCard(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            SpendingOverviewChart(
                                expensesByCategory = state.expensesByCategory,
                                totalExpenses = state.totalExpenses,
                            )
                        }
                    }

                    // ── Section 5: Income Sources Breakdown ───────────────────
                    if (state.incomeBreakdown.isNotEmpty()) {
                        item {
                            SectionCard(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                IncomeSourcesBreakdown(
                                    incomeBreakdown = state.incomeBreakdown,
                                    onViewAll = onNavigateToIncome,
                                )
                            }
                        }
                    }

                    // ── Section 6: Savings Goal Card ──────────────────────────
                    if (state.activeGoal != null) {
                        item {
                            SavingsGoalCard(
                                goal = state.activeGoal,
                                progressPercent = state.goalProgressPercent,
                                monthlyRequired = state.monthlyRequired,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                    }

                    // ── Section 7: Committed vs Discretionary ────────────────
                    if (state.totalExpenses > 0) {
                        item {
                            SectionCard(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            ) {
                                CommittedVsDiscretionary(
                                    committedPercent = state.fixedCostsPercentage,
                                    discretionaryPercent = state.discretionaryPercentage,
                                    totalExpenses = state.totalExpenses,
                                )
                            }
                        }
                    }

                    // ── Section 8: Recent Transactions ────────────────────────
                    if (state.recentTransactions.isNotEmpty()) {
                        item {
                            SectionRowHeader(
                                title = "Recent Transactions",
                                actionLabel = "See all →",
                                onAction = onAddExpense,
                            )
                            Spacer(modifier = Modifier.height(0.dp))
                        }

                        items(state.recentTransactions) { tx ->
                            val df = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
                            TransactionRow(
                                transaction = tx,
                                formattedDate = df.format(tx.dateMillis),
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 1.dp),
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = Color(0xFFE5E7EB),
                            )
                        }
                    }

                    // ── Section 9: Financial Health Score ────────────────────
                    item {
                        SectionCard(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            FinancialHealthScore(
                                totalIncome = state.totalIncome,
                                totalExpenses = state.totalExpenses,
                                activeGoal = state.activeGoal,
                                goalProgressPercent = state.goalProgressPercent,
                                fixedCostsPercentage = state.fixedCostsPercentage,
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }

            // Floating Bottom Nav
            FinPilotBottomNavBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentTab = NavTab.HOME,
                onNavigateToDashboard = {},
                onNavigateToIncome = onNavigateToIncome,
                onNavigateToExpense = onAddExpense,
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile,
            )
        }
    }
}

// ── Section 1: Dashboard Header ───────────────────────────────────────────────

@Composable
private fun DashboardHeader(userName: String) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }
    val initials = userName.split(" ")
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .take(2)
        .joinToString("")
        .ifBlank { "U" }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "$greeting, ${userName.ifBlank { "there" }} 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                )
                Text(
                    text = "Here's your financial summary",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Notification bell
                Box {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFF9FAFB), CircleShape),
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = Color(0xFF374151),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    // Unread dot
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                            .align(Alignment.TopEnd),
                    )
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            brush = Brush.linearGradient(listOf(OrangeHero1, OrangeHero2)),
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = initials,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }

        // Orange accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(
                    brush = Brush.horizontalGradient(listOf(OrangeHero1, OrangeHero2, Color.Transparent)),
                ),
        )
    }
}

// ── Section 2: Hero Balance Card ─────────────────────────────────────────────

@Composable
private fun HeroBalanceCard(
    totalBalance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    balanceVisible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val animCounter = remember { Animatable(0f) }
    LaunchedEffect(totalBalance) {
        animCounter.snapTo(0f)
        animCounter.animateTo(1f, animationSpec = tween(1200))
    }
    val displayBalance = totalBalance * animCounter.value

    Card(
        modifier = modifier.fillMaxWidth().height(190.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(listOf(OrangeHero1, OrangeHero2)),
                ),
        ) {
            // Decorative circles
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f),
                    radius = 120.dp.toPx(),
                    center = Offset(size.width * 0.88f, size.height * 0.15f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = 80.dp.toPx(),
                    center = Offset(size.width * 0.78f, size.height * 0.55f),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Total Balance",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (balanceVisible) Icons.Default.Visibility
                                          else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Balance amount
                Text(
                    text = if (balanceVisible) formatLKRFull(displayBalance) else "LKR ●●●●●",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )

                // Income / Expense chips
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatChip(
                        label = "Income",
                        value = formatLKR(monthlyIncome),
                        isPositive = true,
                    )
                    MiniStatChip(
                        label = "Expenses",
                        value = formatLKR(monthlyExpenses),
                        isPositive = false,
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStatChip(label: String, value: String, isPositive: Boolean) {
    val bg = if (isPositive) Color(0xFF10B981).copy(alpha = 0.22f) else Color(0xFFEF4444).copy(alpha = 0.22f)
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(13.dp),
        )
        Text(
            text = "$label: $value",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
    }
}

// ── Section 3: Quick Actions ──────────────────────────────────────────────────

@Composable
private fun QuickActionsRow(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onGoals: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            QuickActionButton(
                icon = Icons.Default.TrendingUp,
                label = "Add Income",
                onClick = onAddIncome,
            )
            QuickActionButton(
                icon = Icons.Default.TrendingDown,
                label = "Add Expense",
                onClick = onAddExpense,
            )
            QuickActionButton(
                icon = Icons.Default.CompareArrows,
                label = "Transfer",
                onClick = {},
            )
            QuickActionButton(
                icon = Icons.Default.EmojiEvents,
                label = "Goals",
                onClick = onGoals,
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.9f else 1f,
        animationSpec = spring(),
        label = "qa_scale",
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(14.dp))
            .clickable {
                pressed = true
                onClick()
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(Orange.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Orange,
                modifier = Modifier.size(26.dp),
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

// ── Section 4: Spending Overview Chart ───────────────────────────────────────

@Composable
private fun SpendingOverviewChart(
    expensesByCategory: Map<String, Double>,
    totalExpenses: Double,
) {
    SectionRowHeader(title = "Spending Overview", actionLabel = "This month")

    if (expensesByCategory.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "No spending data yet",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF),
                textAlign = TextAlign.Center,
            )
        }
        return
    }

    val sorted = expensesByCategory.entries.sortedByDescending { it.value }
    val maxVal = sorted.firstOrNull()?.value ?: 1.0

    // Custom canvas bar chart
    val barCount = sorted.size.coerceAtMost(6)
    val bars = sorted.take(barCount)

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
    ) {
        val barW = (size.width - 32.dp.toPx()) / barCount
        val maxHeight = size.height - 28.dp.toPx()

        bars.forEachIndexed { i, (_, value) ->
            val ratio = (value / maxVal).toFloat().coerceIn(0f, 1f)
            val barHeight = maxHeight * ratio
            val left = 16.dp.toPx() + i * barW + barW * 0.2f
            val right = left + barW * 0.6f
            val top = maxHeight - barHeight
            val bottom = maxHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    listOf(OrangeHero1, OrangeHero2),
                    startY = top,
                    endY = bottom,
                ),
                topLeft = Offset(left, top),
                size = Size(right - left, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx()),
            )
        }

        // x-axis line
        drawLine(
            color = Color(0xFFE5E7EB),
            start = Offset(16.dp.toPx(), maxHeight),
            end = Offset(size.width - 16.dp.toPx(), maxHeight),
            strokeWidth = 1.dp.toPx(),
        )
    }

    // X-axis labels
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround,
    ) {
        sorted.take(barCount).forEach { (category, _) ->
            Text(
                text = category.take(5),
                fontSize = 9.sp,
                color = Color(0xFF9CA3AF),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
            )
        }
    }

    // Total row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("TOTAL SPENT", fontSize = 10.sp, color = Color(0xFF9CA3AF), letterSpacing = 1.sp)
        Text(
            text = formatLKRFull(totalExpenses),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ExpenseRed,
        )
    }
}

// ── Section 5: Income Sources Breakdown ──────────────────────────────────────

@Composable
private fun IncomeSourcesBreakdown(
    incomeBreakdown: Map<String, Double>,
    onViewAll: () -> Unit,
) {
    SectionRowHeader(title = "Income Sources", actionLabel = "View all →", onAction = onViewAll)

    val total = incomeBreakdown.values.sum().coerceAtLeast(1.0)
    val sorted = incomeBreakdown.entries.sortedByDescending { it.value }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Donut chart
        val slices = sorted.mapIndexed { i, (label, value) ->
            PieChartData.Slice(label, value.toFloat(), IncomePalette.getOrElse(i) { IncomePalette.last() })
        }

        if (slices.isNotEmpty()) {
            Box(modifier = Modifier.size(130.dp)) {
                DonutPieChart(
                    modifier = Modifier.size(130.dp),
                    pieChartData = PieChartData(slices = slices, plotType = PlotType.Donut),
                    pieChartConfig = PieChartConfig(
                        strokeWidth = 40f,
                        isAnimationEnable = true,
                        showSliceLabels = false,
                        isSumVisible = false,
                        backgroundColor = Color.White,
                        activeSliceAlpha = 0.9f,
                        inActiveSliceAlpha = 0.65f,
                    ),
                )
            }
        }

        // Legend
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sorted.forEachIndexed { i, (source, amount) ->
                val color = IncomePalette.getOrElse(i) { IncomePalette.last() }
                val pct = (amount / total * 100).roundToInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                    Text(
                        text = source,
                        fontSize = 12.sp,
                        color = Color(0xFF374151),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text("$pct%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                    Text(
                        text = formatLKR(amount),
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ── Section 6: Savings Goal Card ─────────────────────────────────────────────

@Composable
private fun SavingsGoalCard(
    goal: Goal,
    progressPercent: Float,
    monthlyRequired: Double,
    modifier: Modifier = Modifier,
) {
    val animProg by animateFloatAsState(
        targetValue = progressPercent.coerceIn(0f, 1f),
        animationSpec = tween(1200),
        label = "goal_prog",
    )
    val pct = (animProg * 100).roundToInt()
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val isOnTrack = monthlyRequired > 0 && goal.currentAmount > 0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
            .background(Color.White),
    ) {
        // Left orange accent border
        Box(
            modifier = Modifier
                .width(4.dp)
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(listOf(OrangeHero1, OrangeHero2)),
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                ),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Default.EmojiEvents, null, tint = Orange, modifier = Modifier.size(18.dp))
                    Text(goal.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                }
                Box(
                    modifier = Modifier
                        .background(IncomeGreen.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text("Active", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = IncomeGreen)
                }
            }

            // Progress bar
            GradientProgressBar(progress = animProg, height = 10.dp)

            // Saved / Target
            Text(
                text = "${formatLKRFull(goal.currentAmount)} saved of ${formatLKRFull(goal.targetAmount)}",
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
            )

            // Stats row
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GoalStatCell("$pct%", "Saved")
                GoalStatCell(formatLKR(monthlyRequired) + "/mo", "Required")
                val deadline = goal.deadline
                val months = if (deadline != null) {
                    val now = Calendar.getInstance()
                    val dl = Calendar.getInstance().apply { time = deadline.toDate() }
                    ((dl.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 +
                        dl.get(Calendar.MONTH) - now.get(Calendar.MONTH)).coerceAtLeast(0)
                } else 0
                GoalStatCell("$months mo", "Remaining")
            }

            // Motivational text
            val motivText = if (isOnTrack) "You're on track! Keep it up 🎯" else "Stay consistent to reach your goal ⚡"
            val motivColor = if (isOnTrack) IncomeGreen else Orange
            Text(
                text = motivText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = motivColor,
            )
        }
    }
}

@Composable
private fun GoalStatCell(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
        Text(label, fontSize = 10.sp, color = Color(0xFF9CA3AF))
    }
}

// ── Section 7: Committed vs Discretionary ────────────────────────────────────

@Composable
private fun CommittedVsDiscretionary(
    committedPercent: Double,
    discretionaryPercent: Double,
    totalExpenses: Double,
) {
    SectionRowHeader(title = "Budget Breakdown")

    val committedAmt = totalExpenses * (committedPercent / 100)
    val discAmt = totalExpenses * (discretionaryPercent / 100)

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Left box — committed
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Orange.copy(alpha = 0.08f))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Default.Home, null, tint = Orange, modifier = Modifier.size(18.dp))
            Text("Committed", fontSize = 12.sp, color = Color(0xFF6B7280))
            Text(formatLKRFull(committedAmt), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Text("Rent, gym, subscriptions", fontSize = 10.sp, color = Color(0xFF9CA3AF), maxLines = 1)
        }

        // Right box — discretionary
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF3F4F6))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Default.AttachMoney, null, tint = Color(0xFF6B7280), modifier = Modifier.size(18.dp))
            Text("Discretionary", fontSize = 12.sp, color = Color(0xFF6B7280))
            Text(formatLKRFull(discAmt), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
            Text("Available to spend", fontSize = 10.sp, color = Color(0xFF9CA3AF), maxLines = 1)
        }
    }

    // Segmented ratio bar
    val total = (committedPercent + discretionaryPercent).coerceAtLeast(1.0)
    val committedWeight = (committedPercent / total).toFloat().coerceIn(0.05f, 0.95f)
    val discWeight = 1f - committedWeight

    val animCommitted by animateFloatAsState(
        targetValue = committedWeight,
        animationSpec = tween(900),
        label = "ratio_bar",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(6.dp)),
    ) {
        Box(
            modifier = Modifier
                .weight(animCommitted)
                .fillMaxSize()
                .background(Brush.horizontalGradient(listOf(OrangeHero1, OrangeHero2))),
        )
        Box(
            modifier = Modifier
                .weight(1f - animCommitted)
                .fillMaxSize()
                .background(Color(0xFFE5E7EB)),
        )
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Orange))
            Text("Committed ${committedPercent.roundToInt()}%", fontSize = 10.sp, color = Color(0xFF6B7280))
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Color(0xFFE5E7EB)))
            Text("Flexible ${discretionaryPercent.roundToInt()}%", fontSize = 10.sp, color = Color(0xFF6B7280))
        }
    }
}

// ── Section 8: Transaction Row ────────────────────────────────────────────────

@Composable
private fun TransactionRow(
    transaction: DashboardViewModel.RecentTransaction,
    formattedDate: String,
    modifier: Modifier = Modifier,
) {
    val color = if (transaction.isExpense) ExpenseRed else IncomeGreen
    val icon = categoryIcon(transaction.title)
    val circleColor = if (transaction.isExpense) categoryCircleColor(transaction.title) else IncomeGreen
    val prefix = if (transaction.isExpense) "- " else "+ "

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(circleColor.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = circleColor, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                transaction.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                formattedDate,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF),
            )
        }

        Text(
            text = "$prefix${formatLKRFull(transaction.amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

// ── Section 9: Financial Health Score ─────────────────────────────────────────

@Composable
private fun FinancialHealthScore(
    totalIncome: Double,
    totalExpenses: Double,
    activeGoal: Goal?,
    goalProgressPercent: Float,
    fixedCostsPercentage: Double,
) {
    val score = calculateHealthScore(totalIncome, totalExpenses, goalProgressPercent, fixedCostsPercentage)
    val scoreLabel = when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Good"
        score >= 40 -> "Fair"
        else -> "Needs Work"
    }
    val scoreColor = when {
        score >= 80 -> IncomeGreen
        score >= 60 -> Orange
        score >= 40 -> Color(0xFFF59E0B)
        else -> ExpenseRed
    }

    val animScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animScore.animateTo(score / 100f, tween(1400))
    }

    SectionRowHeader(title = "Financial Health Score")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // Semi-circle arc
        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.BottomCenter) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val strokeW = 18.dp.toPx()
                val padding = strokeW / 2 + 4.dp.toPx()
                val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
                val topLeft = Offset(padding, padding)

                // Background arc
                drawArc(
                    color = Color(0xFFF3F4F6),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeW, cap = StrokeCap.Round),
                )

                // Progress arc
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(OrangeHero2, OrangeHero1),
                        center = Offset(size.width / 2, size.height / 2),
                    ),
                    startAngle = 180f,
                    sweepAngle = 180f * animScore.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeW, cap = StrokeCap.Round),
                )

                // Dot at the tip
                if (animScore.value > 0f) {
                    val angleRad = Math.toRadians((180.0 + 180.0 * animScore.value))
                    val cx = size.width / 2 + (arcSize.width / 2) * cos(angleRad).toFloat()
                    val cy = size.height / 2 + (arcSize.height / 2) * sin(angleRad).toFloat()
                    drawCircle(color = OrangeHero1, radius = strokeW / 2, center = Offset(cx, cy))
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp),
            ) {
                Text(
                    text = score.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scoreColor,
                )
                Text(scoreLabel, fontSize = 11.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
            }
        }

        // Score breakdown
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            val savingsRate = if (totalIncome > 0) ((totalIncome - totalExpenses) / totalIncome * 100).coerceIn(0.0, 100.0) else 0.0
            HealthScoreRow("Savings Rate", savingsRate.roundToInt(), "%")
            HealthScoreRow("Fixed Costs", (100 - fixedCostsPercentage.coerceIn(0.0, 100.0)).roundToInt(), "%")
            if (activeGoal != null) {
                HealthScoreRow("Goal Progress", (goalProgressPercent * 100).roundToInt(), "%")
            }
        }
    }
}

@Composable
private fun HealthScoreRow(label: String, value: Int, suffix: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 12.sp, color = Color(0xFF6B7280))
        Text("$value$suffix", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
    }
}

private fun calculateHealthScore(
    totalIncome: Double,
    totalExpenses: Double,
    goalProgressPercent: Float,
    fixedCostsPercentage: Double,
): Int {
    if (totalIncome <= 0) return 50
    val savingsRate = ((totalIncome - totalExpenses) / totalIncome * 100).coerceIn(-100.0, 100.0)
    val savingsScore = when {
        savingsRate >= 30 -> 35
        savingsRate >= 20 -> 30
        savingsRate >= 10 -> 22
        savingsRate >= 0 -> 15
        else -> 0
    }
    val fixedScore = when {
        fixedCostsPercentage <= 30 -> 25
        fixedCostsPercentage <= 50 -> 20
        fixedCostsPercentage <= 70 -> 12
        else -> 5
    }
    val goalScore = (goalProgressPercent * 25).roundToInt().coerceIn(0, 25)
    val incomeScore = if (totalIncome > 0) 15 else 0
    return (savingsScore + fixedScore + goalScore + incomeScore).coerceIn(0, 100)
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Dashboard – light")
@Composable
private fun DashboardPreviewLight() {
    FinPilotTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardViewModel.DashboardUiState(
                totalIncome = 142000.0,
                totalExpenses = 28450.0,
                netPosition = 127450.0,
                activeGoal = Goal(
                    id = "1", userId = "u",
                    title = "MacBook Pro M4",
                    targetAmount = 490000.0,
                    currentAmount = 127450.0,
                    monthlyRequired = 46820.0,
                    isActive = true,
                ),
                goalProgressPercent = 0.26f,
                monthlyRequired = 46820.0,
                incomeBreakdown = mapOf(
                    "Salary" to 88000.0,
                    "Freelance" to 35000.0,
                    "AdSense" to 11500.0,
                    "Crypto" to 7000.0,
                ),
                expensesByCategory = mapOf(
                    "Food" to 12000.0,
                    "Transport" to 6000.0,
                    "Housing" to 8000.0,
                    "Subscriptions" to 2450.0,
                ),
                fixedCostsPercentage = 34.4,
                discretionaryPercentage = 65.6,
            ),
            userName = "Kavindu",
        )
    }
}

@Preview(showBackground = true, name = "Dashboard – empty")
@Composable
private fun DashboardPreviewEmpty() {
    FinPilotTheme(darkTheme = false) {
        DashboardScreen(state = DashboardViewModel.DashboardUiState(), userName = "Kavindu")
    }
}
