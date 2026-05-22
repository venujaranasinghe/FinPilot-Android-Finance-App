package com.bpeople.finpilot.ui.screens.dashboard

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restaurant
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
import androidx.compose.ui.graphics.drawscope.DrawScope
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

// ── Enhanced Dark Theme Colors ───────────────────────────────────────────────
private val DarkSurface = Color(0xFF1C1C1E)
private val DarkSurfaceVariant = Color(0xFF2C2C2E)
private val DarkBackground = Color(0xFF000000)
private val DarkBorder = Color(0xFF38383A)
private val DarkTextPrimary = Color(0xFFFFFFFF)
private val DarkTextSecondary = Color(0xFFEBEBF5).copy(alpha = 0.6f)

private val LightSurface = Color(0xFFFFFFFF)
private val LightSurfaceVariant = Color(0xFFF2F2F7)
private val LightBackground = Color(0xFFF9FAFB)
private val LightBorder = Color(0xFFE5E7EB)
private val LightTextPrimary = Color(0xFF000000)
private val LightTextSecondary = Color(0xFF3C3C43).copy(alpha = 0.6f)

// ── Brand Colors (work well in both themes) ─────────────────────────────────
private val Orange = Color(0xFFF97316)
private val OrangeLight = Color(0xFFFF8C42)
private val OrangeHero1 = Color(0xFFFF6B35)
private val OrangeHero2 = Color(0xFFFF8C42)
private val GlassBlueDeep = Color(0xFFF97316)
private val GlassBlue = Color(0xFFFF8C42)
private val GlassBlueSoft = Color(0xFFFDBA74)
private val IncomePalette = listOf(
    Color(0xFFF97316), Color(0xFF4CAF50), Color(0xFF2196F3),
    Color(0xFF9C27B0), Color(0xFFF59E0B),
)
private val ExpensePalette = listOf(
    Color(0xFFF97316), Color(0xFFEF4444), Color(0xFF8B5CF6),
    Color(0xFF14B8A6), Color(0xFF6B7280), Color(0xFF3B82F6), Color(0xFF10B981),
)
private val GoalGradient = listOf(OrangeHero1, OrangeHero2, Color(0xFFFDBA74))

// ── Theme-aware color providers ──────────────────────────────────────────────
@Composable
private fun surfaceColor(): Color = if (isSystemInDarkTheme()) DarkSurface else LightSurface

@Composable
private fun surfaceVariantColor(): Color = if (isSystemInDarkTheme()) DarkSurfaceVariant else LightSurfaceVariant

@Composable
private fun backgroundColor(): Color = if (isSystemInDarkTheme()) DarkBackground else LightBackground

@Composable
private fun borderColor(): Color = if (isSystemInDarkTheme()) DarkBorder else LightBorder

@Composable
private fun textPrimaryColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
private fun textSecondaryColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant

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

// ── Glassmorphism card scaffold ───────────────────────────────────────────────
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = surfaceColor()),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSystemInDarkTheme()) DarkBorder else LightBorder
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
private fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) = GlassCard(modifier = modifier, content = content)

@Composable
private fun SectionRowHeader(title: String, actionLabel: String = "", onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimaryColor()
        )
        if (actionLabel.isNotBlank()) {
            Text(
                text = actionLabel,
                fontSize = 13.sp,
                color = GlassBlueDeep,
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
    trackColor: Color = Color.Unspecified,
    progressColor: Color = OrangeHero1,
) {
    val resolvedTrack = if (trackColor == Color.Unspecified) {
        if (isSystemInDarkTheme()) DarkSurfaceVariant else MaterialTheme.colorScheme.outlineVariant
    } else trackColor

    val animated by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(1000),
        label = "grad_bar",
    )
    Canvas(modifier = modifier.fillMaxWidth().height(height)) {
        val cr = CornerRadius(size.height / 2)
        drawRoundRect(color = resolvedTrack, cornerRadius = cr)
        if (animated > 0f) {
            drawRoundRect(
                color = progressColor,
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
    onNavigateToSettings: () -> Unit = {},
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

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = backgroundColor()
    ) { pv ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor()),
        ) {
            PullToRefreshBox(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv),
                state = pullState,
                isRefreshing = isRefreshing,
                onRefresh = { isRefreshing = true },
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 110.dp),
                ) {
                    item {
                        DashboardHeader(userName = userName)
                    }

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

                    item {
                        QuickActionsRow(
                            onAddIncome = onNavigateToIncome,
                            onAddExpense = onAddExpense,
                            onGoals = onNavigateToGoals,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        )
                    }

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

                    if (state.allGoals.isNotEmpty()) {
                        item {
                            GoalsCarousel(
                                goals = state.allGoals,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }

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

            FinPilotBottomNavBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentTab = NavTab.HOME,
                onNavigateToDashboard = {},
                onNavigateToIncome = onNavigateToIncome,
                onNavigateToExpense = onAddExpense,
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    }
}

// ── Section 1: Dashboard Header ───────────────────────────────────────────────
@Composable
private fun DashboardHeader(userName: String) {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        else      -> "Good Evening"
    }
    val dateLabel = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        .format(Calendar.getInstance().time)
        .uppercase(Locale.getDefault())

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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(GlassBlueDeep),
                    )
                    Text(
                        text = dateLabel,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlassBlueDeep,
                        letterSpacing = 1.1.sp,
                    )
                }

                Text(
                    text = greeting,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    color = textSecondaryColor(),
                    letterSpacing = 0.2.sp,
                )

                Text(
                    text = userName.ifBlank { "User" },
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimaryColor(),
                    letterSpacing = (-0.5).sp,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(surfaceColor())
                            .border(0.8.dp, borderColor(), RoundedCornerShape(13.dp)),
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = textPrimaryColor(),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .background(Color(0xFFEF4444), CircleShape)
                            .border(1.5.dp, backgroundColor(), CircleShape)
                            .align(Alignment.TopEnd),
                    )
                }
            }
        }
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

    // Hero card always uses dark theme aesthetic for better contrast
    val heroBg = Color(0xFF1F2937)
    val heroTextColor = Color.White

    Card(
        modifier = modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = heroBg),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
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
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Total Balance",
                        fontSize = 13.sp,
                        color = heroTextColor.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                    )
                    IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (balanceVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = heroTextColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Text(
                    text = if (balanceVisible) formatLKRFull(displayBalance) else "LKR ●●●●●",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = heroTextColor,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStatChip(label = "Income", value = formatLKR(monthlyIncome), isPositive = true)
                    MiniStatChip(label = "Expenses", value = formatLKR(monthlyExpenses), isPositive = false)
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
            imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(surfaceColor())
            .border(0.8.dp, borderColor(), RoundedCornerShape(20.dp)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            QuickActionButton(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "Add Income",
                onClick = onAddIncome,
            )
            QuickActionButton(
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                label = "Add Expense",
                onClick = onAddExpense,
            )
            QuickActionButton(
                icon = Icons.AutoMirrored.Filled.CompareArrows,
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
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "qa_scale",
    )

    val iconColor = textPrimaryColor()
    val iconBg = surfaceVariantColor()
    val iconBorder = borderColor()

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .clickable { pressed = true; onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(iconBg)
                .border(0.8.dp, iconBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(24.dp))
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = textSecondaryColor(),
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
        Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
            Text("No spending data yet", fontSize = 13.sp, color = textSecondaryColor(), textAlign = TextAlign.Center)
        }
        return
    }

    val sorted = expensesByCategory.entries.sortedByDescending { it.value }
    val maxVal = sorted.firstOrNull()?.value ?: 1.0
    val barCount = sorted.size.coerceAtMost(6)
    val bars = sorted.take(barCount)
    val axisLineColor = if (isSystemInDarkTheme()) DarkBorder else MaterialTheme.colorScheme.outline
    val barColor = if (isSystemInDarkTheme()) GlassBlueSoft else GlassBlue

    Canvas(modifier = Modifier.fillMaxWidth().height(160.dp)) {
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
                color = barColor,
                topLeft = Offset(left, top),
                size = Size(right - left, barHeight),
                cornerRadius = CornerRadius(6.dp.toPx()),
            )
        }

        drawLine(
            color = axisLineColor,
            start = Offset(16.dp.toPx(), maxHeight),
            end = Offset(size.width - 16.dp.toPx(), maxHeight),
            strokeWidth = 1.dp.toPx(),
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        sorted.take(barCount).forEach { (category, _) ->
            Text(
                text = category.take(5),
                fontSize = 9.sp,
                color = textSecondaryColor(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "TOTAL SPENT",
            fontSize = 10.sp,
            color = textSecondaryColor(),
            letterSpacing = 1.sp
        )
        Text(
            text = formatLKRFull(totalExpenses),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ExpenseRed
        )
    }
}

// ── Section 5: Income Sources Breakdown ──────────────────────────────────────
// ── Section 5: Income Sources Breakdown (Alternative with Custom Donut) ──────
@Composable
private fun IncomeSourcesBreakdown(
    incomeBreakdown: Map<String, Double>,
    onViewAll: () -> Unit,
) {
    SectionRowHeader(title = "Income Sources", actionLabel = "View all ->", onAction = onViewAll)

    val total = incomeBreakdown.values.sum().coerceAtLeast(1.0)
    val sorted = incomeBreakdown.entries.sortedByDescending { it.value }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (sorted.isNotEmpty()) {
            Box(
                modifier = Modifier.size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                CustomDonutChart(
                    data = sorted.mapIndexed { i, (_, value) ->
                        value.toFloat() to IncomePalette.getOrElse(i) { IncomePalette.last() }
                    },
                    modifier = Modifier.size(130.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sorted.forEachIndexed { i, (source, amount) ->
                val color = IncomePalette.getOrElse(i) { IncomePalette.last() }
                val pct = (amount / total * 100).roundToInt()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
                    Text(
                        text = source,
                        fontSize = 12.sp,
                        color = textPrimaryColor(),
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("$pct%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                    Text(
                        text = formatLKR(amount),
                        fontSize = 11.sp,
                        color = textSecondaryColor(),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// Custom Donut Chart without background issues
@Composable
private fun CustomDonutChart(
    data: List<Pair<Float, Color>>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.first.toDouble() }.toFloat()

    Canvas(modifier = modifier) {
        val strokeWidth = 40f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f

        data.forEach { (value, color) ->
            val sweepAngle = (value / total) * 360f
            if (sweepAngle > 0) {
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Butt
                    ),
                    topLeft = Offset(
                        center.x - radius,
                        center.y - radius
                    ),
                    size = Size(radius * 2, radius * 2)
                )
                startAngle += sweepAngle
            }
        }
    }
}

// ── Section 6: Goals Carousel ─────────────────────────────────────────────────
@Composable
private fun GoalsCarousel(goals: List<Goal>, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(pageCount = { goals.size })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
        ) { page ->
            val goal = goals[page]
            val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
            SavingsGoalCard(goal = goal, progressPercent = progress, monthlyRequired = goal.monthlyRequired)
        }

        if (goals.size > 1) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(goals.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 8.dp else 5.dp)
                            .background(
                                color = if (isSelected) GlassBlueDeep else if (isSystemInDarkTheme()) DarkBorder else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape,
                            ),
                    )
                }
            }
        }
    }
}

// ── Savings Goal Card ────────────────────────────────────────────────────────
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
        label = "goal_prog"
    )
    val pct = (animProg * 100).roundToInt()
    val remaining = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)

    // Goal card maintains dark theme aesthetic for consistency
    val goalBg = Color(0xFF1F2937)
    val goalTextColor = Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(goalBg)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                DashboardMilestoneRing(progress = animProg, modifier = Modifier.size(150.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(54.dp)) { drawDashboardLaptopIllustration() }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$pct%", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = goalTextColor)
                    Text(text = "saved", fontSize = 11.sp, color = goalTextColor.copy(alpha = 0.75f))
                }
            }

            Text(
                text = goal.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = goalTextColor,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${formatLKRFull(goal.currentAmount)} / ${formatLKRFull(goal.targetAmount)}",
                fontSize = 13.sp,
                color = goalTextColor.copy(alpha = 0.8f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GoalStatCell(
                    "$pct%", "Saved",
                    valueColor = goalTextColor,
                    labelColor = goalTextColor.copy(alpha = 0.7f)
                )
                GoalStatCell(
                    formatLKR(monthlyRequired) + "/mo", "Required",
                    valueColor = goalTextColor,
                    labelColor = goalTextColor.copy(alpha = 0.7f)
                )
                val deadline = goal.deadline
                val months = if (deadline != null) {
                    val now = Calendar.getInstance()
                    val dl = Calendar.getInstance().apply { time = deadline.toDate() }
                    ((dl.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 +
                            dl.get(Calendar.MONTH) - now.get(Calendar.MONTH)).coerceAtLeast(0)
                } else 0
                GoalStatCell(
                    "$months mo", "Remaining",
                    valueColor = goalTextColor,
                    labelColor = goalTextColor.copy(alpha = 0.7f)
                )
            }

            val motivText = if (remaining <= 0) "Goal complete. Outstanding work."
            else "Stay consistent to reach your goal."
            Text(
                text = motivText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = goalTextColor.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun DashboardMilestoneRing(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
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
            val reached = progress >= milestoneProgress
            drawCircle(
                color = if (reached) OrangeHero1 else Color.White.copy(alpha = 0.45f),
                radius = 5.dp.toPx(),
                center = Offset(dotX, dotY)
            )
            if (reached) drawCircle(
                color = Color.White,
                radius = 2.5.dp.toPx(),
                center = Offset(dotX, dotY)
            )
        }
    }
}

private fun DrawScope.drawDashboardLaptopIllustration() {
    val w = size.width
    val h = size.height
    val screenL = w * 0.12f; val screenT = h * 0.08f; val screenR = w * 0.88f; val screenB = h * 0.60f
    val screenW = screenR - screenL; val screenH = screenB - screenT

    drawRoundRect(
        color = Color.White.copy(alpha = 0.85f),
        topLeft = Offset(screenL, screenT),
        size = Size(screenW, screenH),
        cornerRadius = CornerRadius(6.dp.toPx())
    )
    drawRoundRect(
        color = OrangeHero1.copy(alpha = 0.28f),
        topLeft = Offset(screenL + 3.dp.toPx(), screenT + 3.dp.toPx()),
        size = Size(screenW - 6.dp.toPx(), screenH - 6.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx())
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.55f),
        radius = 4.dp.toPx(),
        center = Offset(w * 0.5f, (screenT + screenB) / 2f)
    )
    drawRoundRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(w * 0.24f, screenB),
        size = Size(w * 0.52f, 2.5f.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx())
    )

    val baseL = w * 0.08f; val baseT = screenB + 2.5f.dp.toPx()
    val baseR = w * 0.92f; val baseB = h * 0.88f
    drawRoundRect(
        color = Color.White.copy(alpha = 0.75f),
        topLeft = Offset(baseL, baseT),
        size = Size(baseR - baseL, baseB - baseT),
        cornerRadius = CornerRadius(4.dp.toPx())
    )

    val kbL = w * 0.16f; val kbT = baseT + 3.dp.toPx()
    val kbR = w * 0.84f; val kbB = baseB - 3.dp.toPx()
    val keyRows = 3; val keyCols = 8
    val rowH = (kbB - kbT) / keyRows; val colW = (kbR - kbL) / keyCols
    for (row in 0 until keyRows) {
        for (col in 0 until keyCols) {
            drawRoundRect(
                color = OrangeHero1.copy(alpha = 0.22f),
                topLeft = Offset(kbL + col * colW + 1.dp.toPx(), kbT + row * rowH + 1.dp.toPx()),
                size = Size(colW - 2.dp.toPx(), rowH - 2.dp.toPx()),
                cornerRadius = CornerRadius(1.5f.dp.toPx())
            )
        }
    }
}

@Composable
private fun GoalStatCell(
    value: String,
    label: String,
    valueColor: Color = textPrimaryColor(),
    labelColor: Color = textSecondaryColor()
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, fontSize = 10.sp, color = labelColor)
    }
}

// ── Financial Health Score ────────────────────────────────────────────────────
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
        score >= 80 -> GlassBlueDeep
        score >= 60 -> GlassBlue
        score >= 40 -> Color(0xFFF59E0B)
        else -> ExpenseRed
    }

    val animScore = remember { Animatable(0f) }
    LaunchedEffect(score) { animScore.animateTo(score / 100f, tween(1400)) }

    SectionRowHeader(title = "Financial Health Score")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        val arcTrackColor = if (isSystemInDarkTheme()) DarkSurfaceVariant else MaterialTheme.colorScheme.outlineVariant

        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.BottomCenter) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val strokeW = 18.dp.toPx()
                val padding = strokeW / 2 + 4.dp.toPx()
                val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
                val topLeft = Offset(padding, padding)

                drawArc(
                    color = arcTrackColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeW, cap = StrokeCap.Round)
                )
                drawArc(
                    color = GlassBlueDeep,
                    startAngle = 180f,
                    sweepAngle = 180f * animScore.value,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(strokeW, cap = StrokeCap.Round)
                )
                if (animScore.value > 0f) {
                    val angleRad = Math.toRadians((180.0 + 180.0 * animScore.value))
                    val cx = size.width / 2 + (arcSize.width / 2) * cos(angleRad).toFloat()
                    val cy = size.height / 2 + (arcSize.height / 2) * sin(angleRad).toFloat()
                    drawCircle(
                        color = GlassBlueDeep,
                        radius = strokeW / 2,
                        center = Offset(cx, cy)
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = score.toString(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = scoreColor
                )
                Text(
                    scoreLabel,
                    fontSize = 11.sp,
                    color = textSecondaryColor(),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val savingsRate = if (totalIncome > 0)
                ((totalIncome - totalExpenses) / totalIncome * 100).coerceIn(0.0, 100.0) else 0.0
            HealthScoreRow("Savings Rate", savingsRate.roundToInt(), "%")
            HealthScoreRow("Fixed Costs", (100 - fixedCostsPercentage.coerceIn(0.0, 100.0)).roundToInt(), "%")
            if (activeGoal != null)
                HealthScoreRow("Goal Progress", (goalProgressPercent * 100).roundToInt(), "%")
        }
    }
}

@Composable
private fun HealthScoreRow(label: String, value: Int, suffix: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 12.sp, color = textSecondaryColor())
        Text(
            "$value$suffix",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimaryColor()
        )
    }
}

private fun calculateHealthScore(
    totalIncome: Double,
    totalExpenses: Double,
    goalProgressPercent: Float,
    fixedCostsPercentage: Double
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
@Preview(showBackground = true, name = "Dashboard - Dark")
@Composable
private fun DashboardPreviewDark() {
    FinPilotTheme(darkTheme = true) {
        DashboardScreen(
            state = DashboardViewModel.DashboardUiState(
                totalIncome = 142000.0,
                totalExpenses = 28450.0,
                netPosition = 127450.0,
                activeGoal = Goal(
                    id = "1", userId = "u", title = "MacBook Pro M4",
                    targetAmount = 490000.0, currentAmount = 127450.0,
                    monthlyRequired = 46820.0, isActive = true
                ),
                goalProgressPercent = 0.26f,
                monthlyRequired = 46820.0,
                incomeBreakdown = mapOf(
                    "Salary" to 88000.0, "Freelance" to 35000.0,
                    "AdSense" to 11500.0, "Crypto" to 7000.0
                ),
                expensesByCategory = mapOf(
                    "Food" to 12000.0, "Transport" to 6000.0,
                    "Housing" to 8000.0, "Subscriptions" to 2450.0
                ),
                fixedCostsPercentage = 34.4,
                discretionaryPercentage = 65.6,
            ),
            userName = "Kavindu",
        )
    }
}

@Preview(showBackground = true, name = "Dashboard - Light")
@Composable
private fun DashboardPreviewLight() {
    FinPilotTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardViewModel.DashboardUiState(
                totalIncome = 142000.0, totalExpenses = 28450.0, netPosition = 127450.0,
                activeGoal = Goal(
                    id = "1", userId = "u", title = "MacBook Pro M4",
                    targetAmount = 490000.0, currentAmount = 127450.0,
                    monthlyRequired = 46820.0, isActive = true
                ),
                goalProgressPercent = 0.26f, monthlyRequired = 46820.0,
                incomeBreakdown = mapOf(
                    "Salary" to 88000.0, "Freelance" to 35000.0,
                    "AdSense" to 11500.0, "Crypto" to 7000.0
                ),
                expensesByCategory = mapOf(
                    "Food" to 12000.0, "Transport" to 6000.0,
                    "Housing" to 8000.0, "Subscriptions" to 2450.0
                ),
                fixedCostsPercentage = 34.4, discretionaryPercentage = 65.6,
            ),
            userName = "Kavindu",
        )
    }
}