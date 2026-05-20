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

// ── Colour constants ──────────────────────────────────────────────────────────

private val Orange = Color(0xFFF97316)
private val OrangeLight = Color(0xFFFF8C42)
private val OrangeHero1 = Color(0xFFFF6B35)
private val OrangeHero2 = Color(0xFFFF8C42)
private val GlassBlueDeep = Color(0xFFF97316)
private val GlassBlue = Color(0xFFFF8C42)
private val GlassBlueSoft = Color(0xFFFDBA74)
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
private val GoalGradient = listOf(OrangeHero1, OrangeHero2, Color(0xFFFDBA74))

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

/**
 * Reusable glass card — semi-transparent frosted surface with a luminous
 * hairline border. Adapts automatically to dark / light theme.
 */
@Composable
private fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    // Blue-tinted glass fill
    val glassFill = if (isDark) GlassBlue.copy(alpha = 0.12f) else GlassBlue.copy(alpha = 0.08f)
    // Top-edge specular shimmer
    val shimmerBrush = Brush.verticalGradient(
        listOf(
            Color.White.copy(alpha = if (isDark) 0.14f else 0.90f),
            Color.White.copy(alpha = if (isDark) 0.04f else 0.40f),
        ),
    )
    // Hairline border
    val borderBrush = Brush.linearGradient(
        listOf(
            GlassBlueDeep.copy(alpha = if (isDark) 0.35f else 0.40f),
            Color.White.copy(alpha = if (isDark) 0.08f else 0.50f),
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            // Glass base fill
            .background(glassFill, shape)
            // Top-edge shimmer overlay
            .background(shimmerBrush, shape)
            // Luminous border
            .border(0.8.dp, borderBrush, shape),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) { content() }
    }
}

// Legacy alias so call-sites that used SectionCard keep compiling while we
// migrate them incrementally.
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
        Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
    gradientColors: List<Color> = GoalGradient,
    trackColor: Color = Color.Unspecified,
) {
    val resolvedTrack = if (trackColor == Color.Unspecified) MaterialTheme.colorScheme.outlineVariant else trackColor
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
    val isDark = isSystemInDarkTheme()

    // ── Dynamic background gradient ───────────────────────────────────────────
    val bgGradient = if (isDark) {
        Brush.verticalGradient(
            listOf(Color(0xFF0A0500), Color(0xFF000000), Color(0xFF050010)),
        )
    } else {
        Brush.verticalGradient(
            listOf(Color(0xFFFFF7ED), Color(0xFFF5F3FF), Color(0xFFFFFFFF)),
        )
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) return@LaunchedEffect
        kotlinx.coroutines.delay(600)
        isRefreshing = false
    }

    Scaffold(contentWindowInsets = WindowInsets(0, 0, 0, 0)) { pv ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient),
        ) {
            // ── Ambient glow blobs ─────────────────────────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Blue radial glow — top-right
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassBlueDeep.copy(alpha = if (isDark) 0.28f else 0.18f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.07f),
                        radius = 260.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.07f),
                    radius = 260.dp.toPx(),
                )
                // Blue glow — mid-left
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassBlue.copy(alpha = if (isDark) 0.15f else 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.08f, size.height * 0.42f),
                        radius = 220.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.08f, size.height * 0.42f),
                    radius = 220.dp.toPx(),
                )
                // Subtle blue glow — bottom-center
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassBlueSoft.copy(alpha = if (isDark) 0.12f else 0.07f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.5f, size.height * 0.85f),
                        radius = 200.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.85f),
                    radius = 200.dp.toPx(),
                )
            }

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

                    // ── Section 6: Savings Goals Carousel ─────────────────────
                    if (state.allGoals.isNotEmpty()) {
                        item {
                            GoalsCarousel(
                                goals = state.allGoals,
                                modifier = Modifier.padding(vertical = 8.dp),
                            )
                        }
                    }

                    // ── Section 7: Committed vs Discretionary ────────────────
                    //                    if (state.totalExpenses > 0) {
                    //                        item {
                    //                            SectionCard(
                    //                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    //                            ) {
                    //                                CommittedVsDiscretionary(
                    //                                    committedPercent = state.fixedCostsPercentage,
                    //                                    discretionaryPercent = state.discretionaryPercentage,
                    //                                    totalExpenses = state.totalExpenses,
                    //                                )
                    //                            }
                    //                        }
                    //                    }



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

    val isDark = isSystemInDarkTheme()
    val glassFill = if (isDark) GlassBlue.copy(alpha = 0.12f) else GlassBlue.copy(alpha = 0.08f)
    val borderColor = if (isDark) GlassBlueDeep.copy(alpha = 0.18f) else GlassBlueDeep.copy(alpha = 0.25f)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(glassFill)
                .border(
                    width = 0.dp,
                    color = Color.Transparent,
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "$greeting, ${userName.ifBlank { "there" }} 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Here's your financial summary",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Notification bell — glass circle
                Box {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (isDark) GlassBlue.copy(alpha = 0.18f)
                                else GlassBlue.copy(alpha = 0.12f)
                            )
                            .border(
                                0.8.dp,
                                if (isDark) GlassBlueDeep.copy(alpha = 0.32f)
                                else GlassBlueDeep.copy(alpha = 0.25f),
                                CircleShape,
                            ),
                    ) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = MaterialTheme.colorScheme.onSurface,
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

                // Avatar — gradient circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(GlassBlueDeep, GlassBlue, GlassBlueSoft),
                            ),
                        )
                        .border(1.2.dp, Color.White.copy(alpha = 0.35f), CircleShape),
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

        // Gradient accent line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(GlassBlueDeep, GlassBlue, GlassBlueSoft, Color.Transparent),
                    ),
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
        modifier = modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // Deep blue gradient base
                .background(
                    brush = Brush.linearGradient(
                        listOf(GlassBlueDeep, GlassBlue, GlassBlueSoft, Color(0xFFDBEAFE)),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    ),
                ),
        ) {
            // ── Decorative geometry & glass shine ─────────────────────────────
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Large soft circle — top-right
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f),
                    radius = 130.dp.toPx(),
                    center = Offset(size.width * 0.90f, size.height * 0.12f),
                )
                // Medium circle — mid-right
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = 80.dp.toPx(),
                    center = Offset(size.width * 0.80f, size.height * 0.65f),
                )
                // Small highlight circle — top-left
                drawCircle(
                    color = Color.White.copy(alpha = 0.06f),
                    radius = 45.dp.toPx(),
                    center = Offset(size.width * 0.15f, size.height * 0.18f),
                )
            }

            // Glass shimmer stripe across the top edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.18f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )

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
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                    )
                    IconButton(onClick = onToggleVisibility, modifier = Modifier.size(24.dp)) {
                        Icon(
                            imageVector = if (balanceVisible) Icons.Default.Visibility
                                          else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                // Balance amount
                Text(
                    text = if (balanceVisible) formatLKRFull(displayBalance) else "LKR ●●●●●",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
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
    val isDark = isSystemInDarkTheme()
    val glassFill = if (isDark) GlassBlue.copy(alpha = 0.12f) else GlassBlue.copy(alpha = 0.08f)
    val borderBrush = Brush.linearGradient(
        listOf(
            GlassBlueDeep.copy(alpha = if (isDark) 0.35f else 0.40f),
            Color.White.copy(alpha = if (isDark) 0.08f else 0.50f),
        ),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(glassFill)
            .border(0.8.dp, borderBrush, RoundedCornerShape(20.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            QuickActionButton(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                label = "Add Income",
                tint = Color(0xFF10B981),
                onClick = onAddIncome,
            )
            QuickActionButton(
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                label = "Add Expense",
                tint = Color(0xFFEF4444),
                onClick = onAddExpense,
            )
            QuickActionButton(
                icon = Icons.AutoMirrored.Filled.CompareArrows,
                label = "Transfer",
                tint = Color(0xFF6366F1),
                onClick = {},
            )
            QuickActionButton(
                icon = Icons.Default.EmojiEvents,
                label = "Goals",
                tint = GlassBlueDeep,
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
    tint: Color = Orange,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 500f),
        label = "qa_scale",
    )
    val isDark = isSystemInDarkTheme()
    val iconBg = Brush.radialGradient(
        listOf(tint.copy(alpha = if (isDark) 0.22f else 0.14f), Color.Transparent),
    )
    val iconBorder = tint.copy(alpha = if (isDark) 0.35f else 0.22f)

    Column(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
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
                .clip(CircleShape)
                .background(iconBg)
                .border(0.8.dp, iconBorder, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(24.dp),
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    val axisLineColor = MaterialTheme.colorScheme.outline

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
                    listOf(GlassBlueDeep, GlassBlue),
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
            color = axisLineColor,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        Text("TOTAL SPENT", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
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
                        backgroundColor = MaterialTheme.colorScheme.surface,
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
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text("$pct%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
                    Text(
                        text = formatLKR(amount),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

// ── Section 6: Goals Carousel ─────────────────────────────────────────────────

@Composable
private fun GoalsCarousel(
    goals: List<Goal>,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { goals.size })

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
        ) { page ->
            val goal = goals[page]
            val progress = if (goal.targetAmount > 0) {
                (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
            } else 0f
            SavingsGoalCard(
                goal = goal,
                progressPercent = progress,
                monthlyRequired = goal.monthlyRequired,
            )
        }

        if (goals.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
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
                                color = if (isSelected) GlassBlueDeep else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape,
                            ),
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(GoalGradient))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(22.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                DashboardMilestoneRing(progress = animProg, modifier = Modifier.size(150.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(54.dp)) { drawDashboardLaptopIllustration() }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$pct%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Text(
                        text = "saved",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
            }

            Text(
                text = goal.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "${formatLKRFull(goal.currentAmount)} / ${formatLKRFull(goal.targetAmount)}",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f),
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                GoalStatCell("$pct%", "Saved", valueColor = Color.White, labelColor = Color.White.copy(alpha = 0.7f))
                GoalStatCell(formatLKR(monthlyRequired) + "/mo", "Required", valueColor = Color.White, labelColor = Color.White.copy(alpha = 0.7f))
                val deadline = goal.deadline
                val months = if (deadline != null) {
                    val now = Calendar.getInstance()
                    val dl = Calendar.getInstance().apply { time = deadline.toDate() }
                    ((dl.get(Calendar.YEAR) - now.get(Calendar.YEAR)) * 12 +
                        dl.get(Calendar.MONTH) - now.get(Calendar.MONTH)).coerceAtLeast(0)
                } else 0
                GoalStatCell("$months mo", "Remaining", valueColor = Color.White, labelColor = Color.White.copy(alpha = 0.7f))
            }

            val motivText = if (remaining <= 0) "Goal complete! Time to celebrate 🎉" else "Stay consistent to reach your goal"
            Text(
                text = motivText,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
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
                color = if (reached) OrangeHero1 else Color.White.copy(alpha = 0.45f),
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
}

private fun DrawScope.drawDashboardLaptopIllustration() {
    val w = size.width
    val h = size.height

    val screenL = w * 0.12f
    val screenT = h * 0.08f
    val screenR = w * 0.88f
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
        color = OrangeHero1.copy(alpha = 0.28f),
        topLeft = Offset(screenL + 3.dp.toPx(), screenT + 3.dp.toPx()),
        size = Size(screenW - 6.dp.toPx(), screenH - 6.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx()),
    )
    drawCircle(
        color = Color.White.copy(alpha = 0.55f),
        radius = 4.dp.toPx(),
        center = Offset(w * 0.5f, (screenT + screenB) / 2f),
    )

    drawRoundRect(
        color = Color.White.copy(alpha = 0.5f),
        topLeft = Offset(w * 0.24f, screenB),
        size = Size(w * 0.52f, 2.5f.dp.toPx()),
        cornerRadius = CornerRadius(2.dp.toPx()),
    )

    val baseL = w * 0.08f
    val baseT = screenB + 2.5f.dp.toPx()
    val baseR = w * 0.92f
    val baseB = h * 0.88f
    drawRoundRect(
        color = Color.White.copy(alpha = 0.75f),
        topLeft = Offset(baseL, baseT),
        size = Size(baseR - baseL, baseB - baseT),
        cornerRadius = CornerRadius(4.dp.toPx()),
    )

    val kbL = w * 0.16f
    val kbT = baseT + 3.dp.toPx()
    val kbR = w * 0.84f
    val kbB = baseB - 3.dp.toPx()
    val keyRows = 3
    val keyCols = 8
    val rowH = (kbB - kbT) / keyRows
    val colW = (kbR - kbL) / keyCols
    for (row in 0 until keyRows) {
        for (col in 0 until keyCols) {
            drawRoundRect(
                color = OrangeHero1.copy(alpha = 0.22f),
                topLeft = Offset(kbL + col * colW + 1.dp.toPx(), kbT + row * rowH + 1.dp.toPx()),
                size = Size(colW - 2.dp.toPx(), rowH - 2.dp.toPx()),
                cornerRadius = CornerRadius(1.5f.dp.toPx()),
            )
        }
    }
}

@Composable
private fun GoalStatCell(
    value: String,
    label: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, fontSize = 10.sp, color = labelColor)
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
    val isDark = isSystemInDarkTheme()

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        // Left box — committed (blue glass tint)
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFEDD5).copy(alpha = if (isDark) 0.20f else 0.70f),
                            Color(0xFFFED7AA).copy(alpha = if (isDark) 0.14f else 0.55f),
                        ),
                    ),
                )
                .border(
                    0.8.dp,
                    Brush.linearGradient(
                        listOf(OrangeHero1.copy(alpha = 0.45f), OrangeHero2.copy(alpha = 0.18f)),
                    ),
                    RoundedCornerShape(18.dp),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Default.Home, null, tint = OrangeHero1, modifier = Modifier.size(18.dp))
            Text("Committed", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatLKRFull(committedAmt), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Rent, gym, subscriptions", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }

        // Right box — discretionary (blue glass tint)
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            Color(0xFFFFF7ED).copy(alpha = if (isDark) 0.18f else 0.70f),
                            Color(0xFFFDE68A).copy(alpha = if (isDark) 0.12f else 0.55f),
                        ),
                    ),
                )
                .border(
                    0.8.dp,
                    Brush.linearGradient(
                        listOf(
                            OrangeHero2.copy(alpha = 0.40f),
                            OrangeLight.copy(alpha = 0.15f),
                        ),
                    ),
                    RoundedCornerShape(18.dp),
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(Icons.Default.AttachMoney, null, tint = OrangeHero2, modifier = Modifier.size(18.dp))
            Text("Discretionary", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatLKRFull(discAmt), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Available to spend", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.30f))
            .border(0.6.dp, OrangeHero2.copy(alpha = if (isDark) 0.20f else 0.40f), RoundedCornerShape(7.dp)),
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
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
                    .background(OrangeLight.copy(alpha = 0.25f)),
            )
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(OrangeHero1))
            Text("Committed ${committedPercent.roundToInt()}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(OrangeHero2))
            Text("Flexible ${discretionaryPercent.roundToInt()}%", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            .clip(RoundedCornerShape(14.dp))
            .background(GlassBlue.copy(alpha = 0.08f))
            .border(0.6.dp, GlassBlue.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                formattedDate,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        score >= 80 -> GlassBlueDeep
        score >= 60 -> GlassBlue
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
        val arcTrackColor = MaterialTheme.colorScheme.outlineVariant
        Box(modifier = Modifier.size(140.dp), contentAlignment = Alignment.BottomCenter) {
            Canvas(modifier = Modifier.size(140.dp)) {
                val strokeW = 18.dp.toPx()
                val padding = strokeW / 2 + 4.dp.toPx()
                val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
                val topLeft = Offset(padding, padding)

                // Background arc
                drawArc(
                    color = arcTrackColor,
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
                        listOf(GlassBlue, GlassBlueDeep),
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
                    drawCircle(color = GlassBlueDeep, radius = strokeW / 2, center = Offset(cx, cy))
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
                Text(scoreLabel, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
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
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("$value$suffix", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
