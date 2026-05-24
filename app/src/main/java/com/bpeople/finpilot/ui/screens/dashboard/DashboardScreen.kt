package com.bpeople.finpilot.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import com.bpeople.finpilot.ui.theme.LocalAppDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.bpeople.finpilot.R
import com.bpeople.finpilot.data.model.Goal
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.components.DynamicHeaderBackground
import com.bpeople.finpilot.ui.components.wavyBottomShape
import com.bpeople.finpilot.ui.theme.ExpenseRed
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import kotlinx.coroutines.delay
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

// ── Color Theme Mappings ──────────────────────────────────────────────────────
@Composable
private fun surfaceColor(): Color = if (LocalAppDarkTheme.current) Color(0xFF1E1E24) else Color(0xFFFFFFFF)

@Composable
private fun surfaceVariantColor(): Color = if (LocalAppDarkTheme.current) Color(0xFF2D2D37) else Color(0xFFF3F1F8)

@Composable
private fun textPrimaryColor(): Color = if (LocalAppDarkTheme.current) Color(0xFFFFFFFF) else Color(0xFF1C1C24)

@Composable
private fun textSecondaryColor(): Color = if (LocalAppDarkTheme.current) Color(0xFF9E9EB2) else Color(0xFF7A7A8E)

@Composable
private fun borderStrokeColor(): Color = if (LocalAppDarkTheme.current) Color(0xFF323242) else Color(0xFFE8E6F0)

// ── Custom Gradient Avatars ───────────────────────────────────────────────────
@Composable
private fun StyledAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    isSelected: Boolean = false
) {
    val gradients = when (name.lowercase()) {
        "lay" -> listOf(Color(0xFFEC4899), Color(0xFFEA580C))
        "nina" -> listOf(Color(0xFFFBBF24), Color(0xFFF87171))
        "kim" -> listOf(Color(0xFF38BDF8), Color(0xFF3B82F6))
        "john" -> listOf(Color(0xFF34D399), Color(0xFF059669))
        "nomaa" -> listOf(Color(0xFFFB7185), Color(0xFFE11D48))
        else -> listOf(Color(0xFFFB923C), Color(0xFFF97316))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFFF97316) else Color.White.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .background(Brush.linearGradient(gradients))
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(1).uppercase(),
            fontSize = (size.value * 0.45f).sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

// ── Superscript Amount Formatting (LKR) ──────────────────────────────────────
@Composable
private fun StyledBalance(
    amount: Double,
    textColor: Color = Color.Unspecified,
    modifier: Modifier = Modifier
) {
    val resolvedColor = if (textColor == Color.Unspecified) textPrimaryColor() else textColor
    val amountStr = "%,.2f".format(amount.absoluteValue)
    val parts = amountStr.split(".")
    val integerPart = parts[0]
    val decimalPart = parts.getOrNull(1) ?: "00"

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "LKR ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = resolvedColor,
            modifier = Modifier.offset(y = 10.dp)
        )
        Text(
            text = integerPart,
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            color = resolvedColor,
            letterSpacing = (-1).sp
        )
        Text(
            text = ".$decimalPart",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = resolvedColor,
            modifier = Modifier.offset(y = 5.dp)
        )
    }
}

// ── Finance Card Data Model ───────────────────────────────────────────────────
private data class FinanceCard(
    val id: String,
    val label: String,
    val subtitle: String,
    val amount: Double,
    val gradient: Brush,
    val accentColor: Color = Color.White.copy(alpha = 0.85f),
    val textColor: Color = Color.White
)

// ── Custom Grid Icon (Mockup grid icon) ───────────────────────────────────────
@Composable
private fun GridIcon(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.size(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(6.dp).background(textPrimaryColor(), RoundedCornerShape(1.5.dp)))
            Box(modifier = Modifier.size(6.dp).background(textPrimaryColor(), RoundedCornerShape(1.5.dp)))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(modifier = Modifier.size(6.dp).background(textPrimaryColor(), RoundedCornerShape(1.5.dp)))
            Box(modifier = Modifier.size(6.dp).background(textPrimaryColor(), RoundedCornerShape(1.5.dp)))
        }
    }
}

// ── Main Composable Screen ────────────────────────────────────────────────────
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
    onDeleteTransaction: (id: String, isExpense: Boolean) -> Unit = { _, _ -> },
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    var activeCardIndex by remember { mutableIntStateOf(0) }

    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 10
    LaunchedEffect(state.recentTransactions.size) { currentPage = 0 }
    val totalPages = max(1, (state.recentTransactions.size + pageSize - 1) / pageSize)
    val pagedTransactions = remember(state.recentTransactions, currentPage) {
        state.recentTransactions.drop(currentPage * pageSize).take(pageSize)
    }

    LaunchedEffect(isRefreshing) {
        if (!isRefreshing) return@LaunchedEffect
        kotlinx.coroutines.delay(600)
        isRefreshing = false
    }

    val goalAmount = state.activeGoal?.currentAmount ?: 0.0
    val goalTarget = state.activeGoal?.targetAmount?.takeIf { it > 0 } ?: 1.0
    val goalPct = ((goalAmount / goalTarget) * 100).roundToInt().coerceIn(0, 100)

    val cards = remember(state.netPosition, state.totalIncome, state.totalExpenses, goalAmount) {
        listOf(
            FinanceCard(
                id = "balance",
                label = "Total Balance",
                subtitle = "Net position across all accounts",
                amount = state.netPosition,
                gradient = Brush.linearGradient(listOf(Color(0xFFF97316), Color(0xFFFFEDD5)))
            ),
            FinanceCard(
                id = "income",
                label = "Total Income",
                subtitle = "All income sources combined",
                amount = state.totalIncome,
                gradient = Brush.linearGradient(listOf(Color(0xFF059669), Color(0xFF6EE7B7))),
                accentColor = Color.White.copy(alpha = 0.9f)
            ),
            FinanceCard(
                id = "expense",
                label = "Total Expenses",
                subtitle = "Spending across all categories",
                amount = state.totalExpenses,
                gradient = Brush.linearGradient(listOf(Color(0xFFDC2626), Color(0xFFFCA5A5))),
                accentColor = Color.White.copy(alpha = 0.9f)
            ),
            FinanceCard(
                id = "goals",
                label = "Savings Goals",
                subtitle = "$goalPct% of target reached",
                amount = goalAmount,
                gradient = Brush.linearGradient(listOf(Color(0xFFD97706), Color(0xFFFDE68A))),
                accentColor = Color.White.copy(alpha = 0.9f),
                textColor = Color(0xFF1C1C24)
            )
        )
    }

    val backgroundBrush = if (LocalAppDarkTheme.current) {
        Brush.verticalGradient(listOf(Color(0xFF0F0C1B), Color(0xFF05020A)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF4EFFF), Color(0xFFFAF8FF)))
    }

    Scaffold(
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                containerColor = Color.Transparent
            ) { pv ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundBrush)
                        .padding(pv)
                ) {
                    PullToRefreshBox(
                        modifier = Modifier.fillMaxSize(),
                        state = pullState,
                        isRefreshing = isRefreshing,
                        onRefresh = { isRefreshing = true }
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                    // Header and Balance Section with Wavy Orange/Yellow Background
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            DynamicHeaderBackground(
                                patternType = "dashboard",
                                modifier = Modifier.matchParentSize().clip(wavyBottomShape())
                            )
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                            ) {
                                // Customized DashboardHeader with dark glassmorphic text & icons
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        StyledAvatar(name = "lay", size = 42.dp)
                                        Column {
                                            Text(
                                                text = "Hello,",
                                                fontSize = 13.sp,
                                                color = Color(0xFF0F172A).copy(alpha = 0.6f)
                                            )
                                            Text(
                                                text = "${userName.ifBlank { "Lay" }}!",
                                                fontSize = 18.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        // Notification bell
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.5f))
                                                .border(0.8.dp, Color(0x1A0F172A), RoundedCornerShape(12.dp))
                                                .clickable { },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "Notifications",
                                                tint = Color(0xFF0F172A),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(Color(0xFFF97316), CircleShape)
                                                    .border(1.5.dp, Color.White, CircleShape)
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = (-3).dp, y = 3.dp)
                                            )
                                        }

                                        // Grid menu
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Color.White.copy(alpha = 0.5f))
                                                .border(0.8.dp, Color(0x1A0F172A), RoundedCornerShape(12.dp))
                                                .clickable { },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            // Render custom grid icon with dark theme color
                                            Column(
                                                modifier = Modifier.size(16.dp),
                                                verticalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF0F172A), RoundedCornerShape(1.5.dp)))
                                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF0F172A), RoundedCornerShape(1.5.dp)))
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF0F172A), RoundedCornerShape(1.5.dp)))
                                                    Box(modifier = Modifier.size(6.dp).background(Color(0xFF0F172A), RoundedCornerShape(1.5.dp)))
                                                }
                                            }
                                        }
                                    }
                                }

                                // Bank cards on top of wavy background
                                FinanceCardStackSection(
                                    cards = cards,
                                    activeCardIndex = activeCardIndex,
                                    onCardChanged = { newIndex -> activeCardIndex = newIndex },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 12.dp)
                                )
                                Spacer(modifier = Modifier.height(36.dp))

                            }
                        }
                    }

                    // Monthly Overview Bar Chart
                    item {
                        MonthlyOverviewChart(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        )
                    }

                    item {
                        CategoryDistributionPieChart(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    item {
                        IncomeExpenseRatioCard(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        )
                    }

                    // Last Actions Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Last actions",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimaryColor()
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF97316).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = state.recentTransactions.size.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF97316)
                                    )
                                }
                            }
                            // "See all" navigation button
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onNavigateToTransactions() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = "See all",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFF97316)
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "View all transactions",
                                    tint = Color(0xFFF97316),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // Last Actions Items
                    if (state.recentTransactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No recent actions",
                                    fontSize = 13.sp,
                                    color = textSecondaryColor()
                                )
                            }
                        }
                    } else {
                        item {
                            TransactionHistoryTable(
                                transactions = pagedTransactions,
                                onDelete = onDeleteTransaction,
                            )
                        }
                        item {
                            TransactionHistoryPaginationBar(
                                currentPage = currentPage,
                                totalPages = totalPages,
                                onPreviousPage = { currentPage-- },
                                onNextPage = { currentPage++ },
                            )
                        }
                        item {
                            ViewAllTransactionsButton(onClick = onNavigateToTransactions)
                        }
                    }

                    // Smart Insights Banner
                    item {
                        SmartInsightsBanner(
                            state = state,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        )
                    }


                        }
                    }

                    // Bottom Nav Bar
                    FinPilotBottomNavBar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        currentTab = NavTab.HOME,
                        onNavigateToDashboard = {},
                        onNavigateToIncome = onNavigateToIncome,
                        onNavigateToExpense = onAddExpense,
                        onNavigateToTransactions = onNavigateToTransactions,
                        onNavigateToGoals = onNavigateToGoals,
                        onNavigateToProfile = onNavigateToProfile,
                        onNavigateToSettings = onNavigateToSettings
                    )
                }
            }
}

// ── Header Section ────────────────────────────────────────────────────────────
@Composable
private fun DashboardHeader(userName: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StyledAvatar(name = "lay", size = 42.dp)
            Column {
                Text(
                    text = "Hello,",
                    fontSize = 13.sp,
                    color = textSecondaryColor()
                )
                Text(
                    text = "${userName.ifBlank { "Lay" }}!",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor()
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Notification bell
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor())
                    .border(0.8.dp, borderStrokeColor(), RoundedCornerShape(12.dp))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = textPrimaryColor(),
                    modifier = Modifier.size(18.dp)
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color(0xFFF97316), CircleShape)
                        .border(1.5.dp, surfaceColor(), CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-3).dp, y = 3.dp)
                )
            }

            // Grid menu
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(surfaceColor())
                    .border(0.8.dp, borderStrokeColor(), RoundedCornerShape(12.dp))
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                GridIcon()
            }
        }
    }
}

// ── Finance Overlapping Cards Stack ──────────────────────────────────────────
@Composable
private fun FinanceCardStackSection(
    cards: List<FinanceCard>,
    activeCardIndex: Int,
    onCardChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        cards.forEachIndexed { i, card ->
            val relativeIndex = (i - activeCardIndex).mod(cards.size)

            val rotation by animateFloatAsState(
                targetValue = when (relativeIndex) {
                    0 -> 0f
                    1 -> -5f
                    2 -> 5f
                    else -> -5f
                },
                animationSpec = spring(stiffness = 500f),
                label = "rot_$i"
            )

            val scale by animateFloatAsState(
                targetValue = when (relativeIndex) {
                    0 -> 1.0f
                    1 -> 0.92f
                    2 -> 0.84f
                    else -> 0.76f
                },
                animationSpec = spring(stiffness = 500f),
                label = "scale_$i"
            )

            val offsetY by animateDpAsState(
                targetValue = when (relativeIndex) {
                    0 -> 0.dp
                    1 -> (-16).dp
                    2 -> (-28).dp
                    else -> (-36).dp
                },
                animationSpec = spring(stiffness = 500f),
                label = "offsetY_$i"
            )

            val offsetX by animateDpAsState(
                targetValue = when (relativeIndex) {
                    0 -> 0.dp
                    1 -> (-14).dp
                    2 -> 14.dp
                    else -> 0.dp
                },
                animationSpec = spring(stiffness = 500f),
                label = "offsetX_$i"
            )

            val zIndex = when (relativeIndex) {
                0 -> 4f
                1 -> 3f
                2 -> 2f
                else -> 1f
            }

            Box(
                modifier = Modifier
                    .zIndex(zIndex)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        rotationZ = rotation
                    }
                    .offset(x = offsetX, y = offsetY)
                    .fillMaxWidth(0.97f)
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(card.gradient)
                    .pointerInput(activeCardIndex) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                onCardChanged((activeCardIndex + 1).mod(cards.size))
                            },
                            onHorizontalDrag = { change, _ -> change.consume() }
                        )
                    }
                    .clickable {
                        if (relativeIndex != 0) {
                            onCardChanged(i)
                        }
                    }
            ) {
                // Card header: label top-left, FinPilot badge top-right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = card.label,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = card.textColor.copy(alpha = 0.85f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "FinPilot",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = card.textColor.copy(alpha = 0.9f)
                        )
                    }
                }

                // Glassmorphic bottom panel: amount + subtitle
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color.White.copy(alpha = 0.15f))
                        .border(
                            width = 0.5.dp,
                            color = Color.White.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        // LKR Amount in big bold
                        val amountFormatted = if (card.amount >= 1_000_000)
                            "LKR %,.1fM".format(card.amount / 1_000_000)
                        else if (card.amount >= 1_000)
                            "LKR %,.0fK".format(card.amount / 1_000)
                        else
                            "LKR %,.2f".format(card.amount)
                        Text(
                            text = amountFormatted,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = card.textColor,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = card.subtitle,
                            fontSize = 10.sp,
                            color = card.textColor.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// ── Transaction Item Composable ───────────────────────────────────────────────
@Composable
private fun TransactionItem(
    tx: DashboardViewModel.RecentTransaction
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(tx.dateMillis)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(surfaceColor())
            .border(0.8.dp, borderStrokeColor(), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = tx.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimaryColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = tx.subtitle.ifBlank { dateStr },
                fontSize = 11.sp,
                color = textSecondaryColor()
            )
        }

        // Amount side
        val sign = if (tx.isExpense) "-" else "+"
        val amountFormatted = if (tx.amount.absoluteValue >= 1_000)
            "LKR %,.0f".format(tx.amount.absoluteValue)
        else
            "LKR %,.2f".format(tx.amount.absoluteValue)

        Text(
            text = "$sign $amountFormatted",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (tx.isExpense) ExpenseRed else IncomeGreen
        )
    }
}

// ── Transaction History Table ─────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionHistoryTable(
    transactions: List<DashboardViewModel.RecentTransaction>,
    onDelete: (id: String, isExpense: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (transactions.isEmpty()) return
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor()),
        border = BorderStroke(0.8.dp, borderStrokeColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            transactions.forEachIndexed { index, tx ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            onDelete(tx.id, tx.isExpense)
                            true
                        } else false
                    },
                    positionalThreshold = { it * 0.40f },
                )
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val isDelete = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(if (isDelete) Color(0x33EF4444) else Color.Transparent)
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (isDelete) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    },
                ) {
                    TransactionTableRow(tx = tx)
                }
                if (index < transactions.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = borderStrokeColor(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionTableRow(
    tx: DashboardViewModel.RecentTransaction,
    modifier: Modifier = Modifier,
) {
    val dateStr = remember(tx.dateMillis) {
        SimpleDateFormat("dd MMM", Locale.getDefault()).format(tx.dateMillis)
    }
    val sign = if (tx.isExpense) "−" else "+"
    val amountFormatted = if (tx.amount.absoluteValue >= 1_000)
        "LKR %,.0f".format(tx.amount.absoluteValue)
    else
        "LKR %,.2f".format(tx.amount.absoluteValue)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(surfaceColor())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = tx.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimaryColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = tx.subtitle.ifBlank { dateStr },
                fontSize = 11.sp,
                color = textSecondaryColor(),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = dateStr,
            fontSize = 11.sp,
            color = textSecondaryColor(),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "$sign $amountFormatted",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimaryColor(),
        )
    }
}

@Composable
private fun TransactionHistoryPaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val canGoPrev = currentPage > 0
        val canGoNext = currentPage < totalPages - 1

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canGoPrev) surfaceColor() else Color.Transparent)
                .border(
                    0.8.dp,
                    if (canGoPrev) borderStrokeColor() else Color.Transparent,
                    CircleShape,
                )
                .clickable(enabled = canGoPrev) { onPreviousPage() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Previous page",
                tint = if (canGoPrev) textPrimaryColor() else Color.Transparent,
                modifier = Modifier.size(18.dp),
            )
        }

        Text(
            text = "Page ${currentPage + 1} of $totalPages",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textSecondaryColor(),
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canGoNext) surfaceColor() else Color.Transparent)
                .border(
                    0.8.dp,
                    if (canGoNext) borderStrokeColor() else Color.Transparent,
                    CircleShape,
                )
                .clickable(enabled = canGoNext) { onNextPage() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next page",
                tint = if (canGoNext) textPrimaryColor() else Color.Transparent,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

private data class MonthlyBarEntry(
    val label: String,
    val amount: Double,
    val isSelected: Boolean,
)

// ── Monthly Comparison Bar Chart (Real Analytics) ───────────────────────────
@Composable
private fun MonthlyOverviewChart(
    state: DashboardViewModel.DashboardUiState,
    modifier: Modifier = Modifier
) {
    val comparisons = state.monthOverMonthComparisons
    val maxAmount = remember(comparisons) {
        comparisons.maxOfOrNull { maxOf(it.currentAmount, it.previousAmount) }?.coerceAtLeast(1.0) ?: 1.0
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor()),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, borderStrokeColor())
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Analytics",
                        fontSize = 13.sp,
                        color = textSecondaryColor()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${state.currentMonthLabel} vs ${state.previousMonthLabel}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor()
                    )
                }
                // Net trend badge
                val netComp = comparisons.firstOrNull {
                    it.label.contains("Net", ignoreCase = true) || it.label.contains("Balance", ignoreCase = true)
                } ?: comparisons.maxByOrNull { it.currentAmount }
                netComp?.let { comp ->
                    val isPositive = if (comp.increaseIsGood) comp.changePercentage >= 0 else comp.changePercentage < 0
                    val badgeColor = if (isPositive) IncomeGreen else ExpenseRed
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeColor.copy(alpha = 0.12f))
                            .border(0.8.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (comp.changePercentage >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = badgeColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${kotlin.math.abs(comp.changePercentage).roundToInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legend: current vs previous month
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFF97316)))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = state.currentMonthLabel, fontSize = 10.sp, color = textSecondaryColor())
                Spacer(modifier = Modifier.width(14.dp))
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(borderStrokeColor()))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = state.previousMonthLabel, fontSize = 10.sp, color = textSecondaryColor())
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (comparisons.isEmpty()) {
                Text(
                    text = "No analytics data yet",
                    fontSize = 12.sp,
                    color = textSecondaryColor()
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    comparisons.forEach { comparison ->
                        val currentRatio = (comparison.currentAmount / maxAmount).toFloat().coerceIn(0.08f, 1f)
                        val previousRatio = (comparison.previousAmount / maxAmount).toFloat().coerceIn(0.08f, 1f)
                        val isPositiveChange = if (comparison.increaseIsGood) comparison.changePercentage >= 0 else comparison.changePercentage < 0
                        val barColor = when {
                            comparison.label.contains("Income", ignoreCase = true) -> IncomeGreen
                            comparison.label.contains("Expense", ignoreCase = true) -> ExpenseRed
                            else -> Color(0xFFF97316)
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            val pct = comparison.changePercentage.roundToInt()
                            Text(
                                text = "${if (pct >= 0) "+" else ""}$pct%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositiveChange) IncomeGreen else ExpenseRed
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Grouped bars: previous (gray) left, current (colored) right
                            Row(
                                modifier = Modifier.fillMaxWidth(0.88f),
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp * previousRatio)
                                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                        .background(borderStrokeColor())
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(130.dp * currentRatio)
                                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(barColor, barColor.copy(alpha = 0.55f))
                                            )
                                        )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = comparison.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textSecondaryColor(),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarItem(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    heightRatio: Float,
    isSelected: Boolean
) {
    val barColor = if (isSelected) {
        Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFFFEDD5)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFEBEBF5).copy(alpha = 0.7f), Color(0xFFEBEBF5).copy(alpha = 0.7f)))
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,
        modifier = modifier
    ) {
        Text(
            text = "LKR %,.0f".format(amount),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = textSecondaryColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(100.dp * heightRatio)
                .clip(RoundedCornerShape(8.dp))
                .background(barColor),
            contentAlignment = Alignment.TopCenter
        ) {
            if (isSelected) {
                // Hollow white dot near the top of the selected bar
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.5.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF97316))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFFF97316) else textSecondaryColor()
        )
    }
}

// ── Category Distribution Pie Chart (Real Analytics) ────────────────────────
@Composable
private fun CategoryDistributionPieChart(
    state: DashboardViewModel.DashboardUiState,
    modifier: Modifier = Modifier
) {
    val categories = remember(state.expensesByCategory) {
        state.expensesByCategory.entries
            .sortedByDescending { it.value }
            .take(5)
    }
    val total = categories.sumOf { it.value }.coerceAtLeast(1.0)
    val palette = listOf(
        Color(0xFFF97316),
        Color(0xFF3B82F6),
        Color(0xFF10B981),
        Color(0xFFF59E0B),
        Color(0xFFEF4444),
    )
    val trackColor = borderStrokeColor()

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor()),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, borderStrokeColor())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(124.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    if (categories.isEmpty()) {
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height),
                            style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Butt)
                        )
                    } else {
                        var start = -90f
                        categories.forEachIndexed { index, entry ->
                            val sweep = ((entry.value / total) * 360f).toFloat()
                            drawArc(
                                color = palette[index % palette.size],
                                startAngle = start,
                                sweepAngle = max(2f, sweep),
                                useCenter = false,
                                topLeft = Offset.Zero,
                                size = Size(size.width, size.height),
                                style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Butt)
                            )
                            start += sweep
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = Color(0xFFF97316),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "LKR %,.0f".format(categories.sumOf { it.value }),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor()
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Expense Distribution",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor(),
                )
                Text(
                    text = state.currentMonthLabel,
                    fontSize = 12.sp,
                    color = textSecondaryColor(),
                )

                if (categories.isEmpty()) {
                    Text(
                        text = "No category data yet",
                        fontSize = 12.sp,
                        color = textSecondaryColor(),
                    )
                } else {
                    categories.forEachIndexed { index, entry ->
                        val percent = ((entry.value / total) * 100).roundToInt()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(palette[index % palette.size])
                                )
                                Text(
                                    text = entry.key,
                                    fontSize = 12.sp,
                                    color = textPrimaryColor(),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "$percent%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSecondaryColor(),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Income vs Expense Ratio Card ─────────────────────────────────────────────
@Composable
private fun IncomeExpenseRatioCard(
    state: DashboardViewModel.DashboardUiState,
    modifier: Modifier = Modifier
) {
    val income = state.totalIncome.coerceAtLeast(0.01)
    val expenses = state.totalExpenses.coerceAtLeast(0.0)
    val expenseRatio = (expenses / income).coerceIn(0.0, 1.0).toFloat()
    val savingsRate = ((1.0 - expenseRatio.toDouble()) * 100).coerceIn(0.0, 100.0).roundToInt()
    val savingsColor = when {
        savingsRate >= 20 -> IncomeGreen
        savingsRate > 0  -> Color(0xFFF97316)
        else             -> ExpenseRed
    }
    val animatedExpenseRatio by animateFloatAsState(
        targetValue = expenseRatio,
        animationSpec = tween(durationMillis = 1000),
        label = "expenseRatio"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor()),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, borderStrokeColor())
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Savings Rate",
                        fontSize = 13.sp,
                        color = textSecondaryColor()
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Income vs Expenses",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimaryColor()
                    )
                }
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(savingsColor.copy(alpha = 0.12f))
                        .border(1.dp, savingsColor.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$savingsRate%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = savingsColor
                        )
                        Text(
                            text = "saved",
                            fontSize = 8.sp,
                            color = savingsColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stacked ratio bar: expense (left/red) + savings (right/green)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(ExpenseRed.copy(alpha = 0.18f))
            ) {
                // Expense portion
                if (animatedExpenseRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedExpenseRatio)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(listOf(ExpenseRed.copy(alpha = 0.8f), ExpenseRed))
                            )
                    )
                }
                // Savings portion
                if (animatedExpenseRatio < 1f) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(1f - animatedExpenseRatio)
                            .fillMaxHeight()
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(5.dp))
                            .background(
                                Brush.horizontalGradient(listOf(IncomeGreen, IncomeGreen.copy(alpha = 0.7f)))
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(ExpenseRed))
                    Column {
                        Text(text = "Expenses", fontSize = 10.sp, color = textSecondaryColor())
                        Text(
                            text = "LKR %,.0f".format(expenses),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Income", fontSize = 10.sp, color = textSecondaryColor())
                        Text(
                            text = "LKR %,.0f".format(income),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IncomeGreen))
                }
            }
        }
    }
}

// ── View All Transactions Button ─────────────────────────────────────────────
@Composable
private fun ViewAllTransactionsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF97316).copy(alpha = 0.08f))
            .border(1.dp, Color(0xFFF97316).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = Color(0xFFF97316),
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "View Full Transaction History & Analytics",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF97316)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFFF97316),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// ── Smart Insights Banner ─────────────────────────────────────────────────────
private enum class InsightType { Positive, Warning, Alert, Info }

private data class SmartInsight(
    val message: String,
    val detail: String,
    val type: InsightType,
    val icon: ImageVector,
)

private fun buildSmartInsights(state: DashboardViewModel.DashboardUiState): List<SmartInsight> {
    val insights = mutableListOf<SmartInsight>()

    // Category change insights
    for (cat in state.topCategoryInsights) {
        when {
            cat.changePercentage >= 30.0 -> insights.add(
                SmartInsight(
                    message = "${cat.category} spending up ${cat.changePercentage.roundToInt()}%",
                    detail = "Compared to last month. Consider reviewing your ${cat.category.lowercase()} budget.",
                    type = InsightType.Warning,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                )
            )
            cat.changePercentage <= -20.0 -> insights.add(
                SmartInsight(
                    message = "${cat.category} spending down ${(-cat.changePercentage).roundToInt()}%",
                    detail = "Great job reducing ${cat.category.lowercase()} costs vs last month.",
                    type = InsightType.Positive,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                )
            )
            cat.sharePercentage >= 40.0 -> insights.add(
                SmartInsight(
                    message = "${cat.category} is ${cat.sharePercentage.roundToInt()}% of expenses",
                    detail = "Your biggest category this month. Consider setting a spending limit.",
                    type = InsightType.Warning,
                    icon = Icons.Default.PieChart,
                )
            )
        }
    }

    // Goal progress
    state.activeGoal?.let { goal ->
        val pct = (state.goalProgressPercent * 100).roundToInt()
        when {
            pct >= 80 -> insights.add(
                SmartInsight(
                    message = "Almost there on \"${goal.title}\"!",
                    detail = "You're $pct% toward your goal. Keep the momentum!",
                    type = InsightType.Positive,
                    icon = Icons.Default.Flag,
                )
            )
            pct >= 40 -> insights.add(
                SmartInsight(
                    message = "\"${goal.title}\" is $pct% complete",
                    detail = "Need LKR ${"%,.0f".format(state.monthlyRequired)}/month to stay on track.",
                    type = InsightType.Info,
                    icon = Icons.Default.Flag,
                )
            )
            else -> insights.add(
                SmartInsight(
                    message = "\"${goal.title}\" needs attention",
                    detail = "Only $pct% reached. Contribute LKR ${"%,.0f".format(state.monthlyRequired)} this month.",
                    type = InsightType.Info,
                    icon = Icons.Default.Flag,
                )
            )
        }
    }

    // Month-over-month expense spike
    state.monthOverMonthComparisons.firstOrNull { it.label == "Expenses" }?.let { comp ->
        if (comp.changePercentage >= 25.0) {
            insights.add(
                SmartInsight(
                    message = "Expenses up ${comp.changePercentage.roundToInt()}% this month",
                    detail = "Spending rose significantly. Review recent transactions to find the cause.",
                    type = InsightType.Alert,
                    icon = Icons.Default.ReceiptLong,
                )
            )
        }
    }

    // Income growth
    state.monthOverMonthComparisons.firstOrNull { it.label == "Income" }?.let { comp ->
        if (comp.changePercentage >= 10.0) {
            insights.add(
                SmartInsight(
                    message = "Income grew ${comp.changePercentage.roundToInt()}% this month",
                    detail = "Your earnings increased vs last month. Consider allocating the extra to savings.",
                    type = InsightType.Positive,
                    icon = Icons.Default.AttachMoney,
                )
            )
        }
    }

    // High fixed costs
    if (state.fixedCostsPercentage >= 55.0) {
        insights.add(
            SmartInsight(
                message = "${state.fixedCostsPercentage.roundToInt()}% of spending is recurring",
                detail = "High fixed costs reduce flexibility. Audit subscriptions and recurring bills.",
                type = InsightType.Warning,
                icon = Icons.Default.Refresh,
            )
        )
    }

    // Net balance direction
    state.monthOverMonthComparisons.firstOrNull { it.label == "Net Balance" }?.let { comp ->
        when {
            comp.changePercentage >= 15.0 -> insights.add(
                SmartInsight(
                    message = "Net balance up ${comp.changePercentage.roundToInt()}% vs last month",
                    detail = "You saved more than last month. Excellent financial discipline!",
                    type = InsightType.Positive,
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                )
            )
            comp.changePercentage <= -15.0 -> insights.add(
                SmartInsight(
                    message = "Net balance dropped ${(-comp.changePercentage).roundToInt()}%",
                    detail = "Your savings ratio fell this month. Consider trimming discretionary spend.",
                    type = InsightType.Alert,
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                )
            )
        }
    }

    // Fallback tip
    state.didYouKnowInsight?.let { tip ->
        if (insights.size < 2 && insights.none { it.detail == tip }) {
            insights.add(
                SmartInsight(
                    message = "Did you know?",
                    detail = tip.removePrefix("Did you know? "),
                    type = InsightType.Info,
                    icon = Icons.Default.Lightbulb,
                )
            )
        }
    }

    return insights.take(5)
}

@Composable
private fun SmartInsightsBanner(
    state: DashboardViewModel.DashboardUiState,
    modifier: Modifier = Modifier,
) {
    val insights = remember(state) { buildSmartInsights(state) }
    if (insights.isEmpty()) return

    val pagerState = rememberPagerState { insights.size }

    LaunchedEffect(pagerState) {
        while (true) {
            delay(4_000L)
            val next = (pagerState.currentPage + 1) % insights.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Smart insights",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor()
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF7C3AED).copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "AI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF7C3AED)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 20.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            SmartInsightCard(insight = insights[page])
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            insights.forEachIndexed { i, _ ->
                val isSelected = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(width = if (isSelected) 20.dp else 6.dp, height = 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isSelected) Color(0xFF7C3AED)
                            else Color(0xFF7C3AED).copy(alpha = 0.25f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SmartInsightCard(insight: SmartInsight) {
    val gradientColors = when (insight.type) {
        InsightType.Positive -> listOf(Color(0xFF16A34A), Color(0xFF4ADE80))
        InsightType.Warning  -> listOf(Color(0xFFD97706), Color(0xFFFBBF24))
        InsightType.Alert    -> listOf(Color(0xFFDC2626), Color(0xFFFCA5A5))
        InsightType.Info     -> listOf(Color(0xFF7C3AED), Color(0xFFA78BFA))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradientColors))
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = insight.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.message,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = insight.detail,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Compose Preview ──────────────────────────────────────────────────────────
@Preview(showBackground = true, name = "Dashboard Preview")
@Composable
private fun DashboardRedesignPreview() {
    FinPilotTheme(darkTheme = false) {
        DashboardScreen(
            state = DashboardViewModel.DashboardUiState(
                totalIncome = 150000.0,
                totalExpenses = 25000.0,
                netPosition = 3567.37,
                recentTransactions = listOf(
                    DashboardViewModel.RecentTransaction(
                        id = "1",
                        title = "Spotify",
                        subtitle = "Yesterday",
                        amount = 12.90,
                        dateMillis = System.currentTimeMillis() - 86400000L,
                        isExpense = true
                    )
                )
            ),
            userName = "Lay"
        )
    }
}