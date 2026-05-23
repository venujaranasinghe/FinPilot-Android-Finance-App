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
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.CurrencyBitcoin
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
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
import com.bpeople.finpilot.ui.theme.ExpenseRed
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

// ── Color Theme Mappings ──────────────────────────────────────────────────────
@Composable
private fun surfaceColor(): Color = if (isSystemInDarkTheme()) Color(0xFF1E1E24) else Color(0xFFFFFFFF)

@Composable
private fun surfaceVariantColor(): Color = if (isSystemInDarkTheme()) Color(0xFF2D2D37) else Color(0xFFF3F1F8)

@Composable
private fun textPrimaryColor(): Color = if (isSystemInDarkTheme()) Color(0xFFFFFFFF) else Color(0xFF1C1C24)

@Composable
private fun textSecondaryColor(): Color = if (isSystemInDarkTheme()) Color(0xFF9E9EB2) else Color(0xFF7A7A8E)

@Composable
private fun borderStrokeColor(): Color = if (isSystemInDarkTheme()) Color(0xFF323242) else Color(0xFFE8E6F0)

// ── Custom Gradient Avatars ───────────────────────────────────────────────────
@Composable
private fun StyledAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: androidx.compose.ui.unit.Dp = 48.dp,
    isSelected: Boolean = false
) {
    val gradients = when (name.lowercase()) {
        "lay" -> listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
        "nina" -> listOf(Color(0xFFFBBF24), Color(0xFFF87171))
        "kim" -> listOf(Color(0xFF38BDF8), Color(0xFF3B82F6))
        "john" -> listOf(Color(0xFF34D399), Color(0xFF059669))
        "nomaa" -> listOf(Color(0xFFFB7185), Color(0xFFE11D48))
        else -> listOf(Color(0xFFA78BFA), Color(0xFF6366F1))
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color(0xFF8A6FFF) else Color.White.copy(alpha = 0.5f),
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
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onLogout: () -> Unit = {},
) {
    var isRefreshing by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    var activeCardIndex by remember { mutableIntStateOf(0) }

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
                gradient = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFD8B4FE)))
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

    val backgroundBrush = if (isSystemInDarkTheme()) {
        Brush.verticalGradient(listOf(Color(0xFF0F0C1B), Color(0xFF05020A)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFF4EFFF), Color(0xFFFAF8FF)))
    }

    val dashboardFontFamily = remember {
        FontFamily(
            Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
            Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
            Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
            Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
        )
    }

    val baseTypography = MaterialTheme.typography
    val dashboardTypography = baseTypography.copy(
        displayLarge = baseTypography.displayLarge.copy(fontFamily = dashboardFontFamily),
        displayMedium = baseTypography.displayMedium.copy(fontFamily = dashboardFontFamily),
        displaySmall = baseTypography.displaySmall.copy(fontFamily = dashboardFontFamily),
        headlineLarge = baseTypography.headlineLarge.copy(fontFamily = dashboardFontFamily),
        headlineMedium = baseTypography.headlineMedium.copy(fontFamily = dashboardFontFamily),
        headlineSmall = baseTypography.headlineSmall.copy(fontFamily = dashboardFontFamily),
        titleLarge = baseTypography.titleLarge.copy(fontFamily = dashboardFontFamily),
        titleMedium = baseTypography.titleMedium.copy(fontFamily = dashboardFontFamily),
        titleSmall = baseTypography.titleSmall.copy(fontFamily = dashboardFontFamily),
        bodyLarge = baseTypography.bodyLarge.copy(fontFamily = dashboardFontFamily),
        bodyMedium = baseTypography.bodyMedium.copy(fontFamily = dashboardFontFamily),
        bodySmall = baseTypography.bodySmall.copy(fontFamily = dashboardFontFamily),
        labelLarge = baseTypography.labelLarge.copy(fontFamily = dashboardFontFamily),
        labelMedium = baseTypography.labelMedium.copy(fontFamily = dashboardFontFamily),
        labelSmall = baseTypography.labelSmall.copy(fontFamily = dashboardFontFamily),
    )

    MaterialTheme(typography = dashboardTypography) {
        ProvideTextStyle(LocalTextStyle.current.copy(fontFamily = dashboardFontFamily)) {
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
                    // Header Section
                    item {
                        DashboardHeader(
                            userName = userName,
                            onNavigateToNotifications = onNavigateToNotifications,
                        )
                    }

                    // Balance Section
                    item {
                        val activeCard = cards.getOrNull(activeCardIndex)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = activeCard?.label ?: "Your balance",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF8A6FFF)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(surfaceColor())
                                        .border(0.8.dp, borderStrokeColor(), CircleShape)
                                        .clickable {
                                            activeCardIndex = (activeCardIndex - 1).mod(cards.size)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowLeft,
                                        contentDescription = "Previous Card",
                                        tint = textPrimaryColor(),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                StyledBalance(
                                    amount = activeCard?.amount ?: state.netPosition
                                )

                                Spacer(modifier = Modifier.width(16.dp))

                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(surfaceColor())
                                        .border(0.8.dp, borderStrokeColor(), CircleShape)
                                        .clickable {
                                            activeCardIndex = (activeCardIndex + 1).mod(cards.size)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowRight,
                                        contentDescription = "Next Card",
                                        tint = textPrimaryColor(),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            // Pagination dots
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                cards.forEachIndexed { i, _ ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (i == activeCardIndex) 8.dp else 5.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (i == activeCardIndex) Color(0xFF8A6FFF)
                                                else Color(0xFF8A6FFF).copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }
                        }
                    }

                    // Overlapping Swipable Card Stack
                    item {
                        FinanceCardStackSection(
                            cards = cards,
                            activeCardIndex = activeCardIndex,
                            onCardChanged = { newIndex -> activeCardIndex = newIndex },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
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
                                        .background(Color(0xFF8A6FFF).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = state.recentTransactions.size.toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8A6FFF)
                                    )
                                }
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
                        items(state.recentTransactions) { tx ->
                            TransactionItem(tx = tx)
                        }
                    }

                    // Monthly Overview Bar Chart (from top-left of sample UI)
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

                    // Quick action shortcuts
                    item {
                        QuickActionsSection(
                            onAddGoal = onNavigateToGoals,
                            onAddIncome = onNavigateToIncome,
                            onAddExpense = onAddExpense,
                            onOpenTransactions = onNavigateToTransactions,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
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
    }
}

// ── Header Section ────────────────────────────────────────────────────────────
@Composable
private fun DashboardHeader(
    userName: String,
    onNavigateToNotifications: () -> Unit = {},
) {
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
                    .clickable { onNavigateToNotifications() },
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
                        .background(Color(0xFF8A6FFF), CircleShape)
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
            .height(220.dp)
            .padding(horizontal = 24.dp),
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
                    .fillMaxWidth(0.92f)
                    .height(185.dp)
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
                        .height(90.dp)
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
    val methodColor = surfaceVariantColor()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceColor())
            .border(0.8.dp, borderStrokeColor(), RoundedCornerShape(16.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(methodColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        tx.title.contains("Salary", true) -> Icons.Rounded.AccountBalance
                        tx.title.contains("Food", true) -> Icons.Default.Restaurant
                        tx.title.contains("Car", true) || tx.title.contains("Transport", true) -> Icons.Default.DirectionsCar
                        tx.title.contains("Rent", true) || tx.title.contains("Home", true) -> Icons.Default.Home
                        tx.title.contains("Sub", true) || tx.title.contains("Spotify", true) -> Icons.Default.PlayArrow
                        else -> Icons.Default.AttachMoney
                    },
                    contentDescription = null,
                    tint = textPrimaryColor(),
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = tx.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor()
                )
                Text(
                    text = tx.subtitle.ifBlank { dateStr },
                    fontSize = 11.sp,
                    color = textSecondaryColor()
                )
            }
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
            color = if (tx.isExpense) textPrimaryColor() else IncomeGreen
        )
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
    val bars = remember(comparisons) {
        comparisons.mapIndexed { index, comparison ->
            MonthlyBarEntry(
                label = comparison.label,
                amount = comparison.currentAmount,
                isSelected = index == comparisons.maxByOrNull { it.currentAmount }?.let { comparisons.indexOf(it) }
            )
        }
    }
    val maxAmount = bars.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor()),
        border = androidx.compose.foundation.BorderStroke(0.8.dp, borderStrokeColor())
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Chart header and badge
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

                val selected = comparisons.maxByOrNull { it.currentAmount }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceVariantColor())
                        .border(0.8.dp, borderStrokeColor(), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = selected?.let { "LKR %,.0f".format(it.currentAmount) } ?: "No data",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = textPrimaryColor()
                        )
                        Text(
                            text = selected?.let {
                                val prefix = if (it.changePercentage >= 0) "↑" else "↓"
                                "$prefix ${kotlin.math.abs(it.changePercentage).roundToInt()}% ${it.label}"
                            } ?: "",
                            fontSize = 10.sp,
                            color = textSecondaryColor()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                if (bars.isEmpty()) {
                    Text(
                        text = "No analytics data yet",
                        fontSize = 12.sp,
                        color = textSecondaryColor(),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                } else {
                    bars.forEach { bar ->
                        val ratio = (bar.amount / maxAmount).toFloat().coerceIn(0.12f, 1f)
                        BarItem(
                            modifier = Modifier.weight(1f),
                            label = bar.label,
                            amount = bar.amount,
                            heightRatio = ratio,
                            isSelected = bar.isSelected
                        )
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
        Brush.verticalGradient(listOf(Color(0xFF8A6FFF), Color(0xFFE5C0FF)))
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
                        .background(Color(0xFF8A6FFF))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color(0xFF8A6FFF) else textSecondaryColor()
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
        Color(0xFF8A6FFF),
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
                        tint = Color(0xFF8A6FFF),
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

private data class QuickAction(
    val title: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

// ── Quick Actions Row ────────────────────────────────────────────────────────
@Composable
private fun QuickActionsSection(
    onAddGoal: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onOpenTransactions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val actions = remember(onAddGoal, onAddIncome, onAddExpense, onOpenTransactions) {
        listOf(
            QuickAction("Goal", Icons.Default.Flag, onAddGoal),
            QuickAction("Income", Icons.Default.AttachMoney, onAddIncome),
            QuickAction("Expense", Icons.Default.ReceiptLong, onAddExpense),
            QuickAction("History", Icons.Default.Home, onOpenTransactions),
        )
    }

    Column(modifier = modifier) {
        Text(
            text = "Quick actions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimaryColor(),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(actions) { action ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(surfaceColor())
                        .border(0.8.dp, borderStrokeColor(), RoundedCornerShape(12.dp))
                        .clickable { action.onClick() }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.title,
                        tint = Color(0xFF8A6FFF),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = action.title,
                        fontSize = 11.sp,
                        color = textSecondaryColor(),
                        fontWeight = FontWeight.Medium
                    )
                }
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