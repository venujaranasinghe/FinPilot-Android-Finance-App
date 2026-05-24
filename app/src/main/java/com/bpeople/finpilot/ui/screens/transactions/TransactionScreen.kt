@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.bpeople.finpilot.ui.screens.transactions

import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlin.math.absoluteValue
import kotlin.math.pow
import kotlin.math.roundToInt
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.DirectionsBike
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpeople.finpilot.data.model.MonthlyBarData
import com.bpeople.finpilot.data.model.Period
import com.bpeople.finpilot.data.model.TransactionItem
import com.bpeople.finpilot.data.model.TransactionType
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.GlassTheme
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.components.DynamicHeaderBackground
import com.bpeople.finpilot.ui.components.wavyBottomShape
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.max
import kotlin.math.roundToInt
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight

// ── Enhanced Dark Theme Colors ─────────────────────────────────────────────────
private val DarkSurface = Color(0xFF1C1C1E)
private val DarkSurfaceVariant = Color(0xFF2C2C2E)
private val DarkBackground = Color(0xFF000000)
private val DarkBorder = Color(0xFF38383A)
private val DarkTextPrimary = Color(0xFFFFFFFF)
private val DarkTextSecondary = Color(0xFFEBEBF5).copy(alpha = 0.6f)
private val DarkTextHint = Color(0xFF636366)
private val DarkGlassBg = Color(0xFF1C1C1E).copy(alpha = 0.8f)
private val DarkGlassSurface = Color(0xFF2C2C2E).copy(alpha = 0.6f)
private val DarkGlassBorder = Color(0xFF38383A)
private val DarkGlassBorderLight = Color(0xFF38383A).copy(alpha = 0.5f)
private val DarkOrangeDim = Color(0xFFFF6B00).copy(alpha = 0.15f)
private val DarkIncomeCardBg = Color(0xFF1B8A4A).copy(alpha = 0.15f)
private val DarkExpenseCardBg = Color(0xFFD63B3B).copy(alpha = 0.15f)

// ── Theme constants ────────────────────────────────────────────────────────────
private val OrangePrimary = Color(0xFFF97316)
private val OrangePrimaryDeep = Color(0xFFFF6B00)
private val OrangeLight = Color(0xFFFFCFA8)
private val OrangeSurface = Color(0xFFFFF4ED)
private val OrangeBorder = Color(0xFFFFCFA8)
private val IncomeColor = Color(0xFF1B8A4A)
private val ExpenseColor = Color(0xFFD63B3B)
private val IncomeCardBg = Color(0xFFE8F5EE)
private val ExpenseCardBg = Color(0xFFFDE8E8)

// ── Theme-aware color providers ────────────────────────────────────────────────
@Composable
private fun textPrimaryColor(): Color = if (LocalAppDarkTheme.current) DarkTextPrimary else GlassTheme.TextPrimary

@Composable
private fun textSecondaryColor(): Color = if (LocalAppDarkTheme.current) DarkTextSecondary else GlassTheme.TextSecondary

@Composable
private fun textHintColor(): Color = if (LocalAppDarkTheme.current) DarkTextHint else GlassTheme.TextHint

@Composable
private fun glassBgColor(): Color = if (LocalAppDarkTheme.current) DarkGlassBg else GlassTheme.GlassBg

@Composable
private fun glassSurfaceColor(): Color = if (LocalAppDarkTheme.current) DarkGlassSurface else GlassTheme.GlassSurface

@Composable
private fun glassBorderColor(): Color = if (LocalAppDarkTheme.current) DarkGlassBorder else GlassTheme.GlassBorder

@Composable
private fun glassBorderLightColor(): Color = if (LocalAppDarkTheme.current) DarkGlassBorderLight else GlassTheme.GlassBorderLight

@Composable
private fun orangeDimColor(): Color = if (LocalAppDarkTheme.current) DarkOrangeDim else GlassTheme.OrangeDim

@Composable
private fun incomeSummaryBgColor(): Color = if (LocalAppDarkTheme.current) DarkIncomeCardBg else IncomeCardBg

@Composable
private fun expenseSummaryBgColor(): Color = if (LocalAppDarkTheme.current) DarkExpenseCardBg else ExpenseCardBg

@Composable
private fun backgroundColor(): Color = if (LocalAppDarkTheme.current) DarkBackground else GlassTheme.BgStart

@Composable
private fun backgroundMidColor(): Color = if (LocalAppDarkTheme.current) DarkSurface else GlassTheme.BgMid

@Composable
private fun orbPurpleColor(): Color = if (LocalAppDarkTheme.current) Color(0xFF8B5CF6).copy(alpha = 0.45f) else GlassTheme.OrbPurple

@Composable
private fun orbOrangeColor(): Color = if (LocalAppDarkTheme.current) Color(0xFFFF6B00).copy(alpha = 0.14f) else GlassTheme.OrbOrange

// ── Donut palette ─────────────────────────────────────────────────────────────
private val IncomePalette = listOf(
    "salary" to Color(0xFFFF6B00),
    "freelance" to Color(0xFFFFAD6B),
    "adsense" to Color(0xFFFFD4A8),
    "crypto" to Color(0xFFC54E00),
)
private val ExpensePalette = listOf(
    "rent" to Color(0xFFD63B3B),
    "food" to Color(0xFFE87070),
    "transport" to Color(0xFFF0A0A0),
    "subscriptions" to Color(0xFFF5C0C0),
    "other" to Color(0xFF888780),
)

private fun incomeColor(source: String): Color {
    val key = source.lowercase().trim()
    return IncomePalette.firstOrNull { (k, _) -> key.contains(k) }?.second
        ?: IncomePalette[key.hashCode().absoluteValue % IncomePalette.size].second
}

private fun expenseColor(category: String): Color {
    val key = category.lowercase().trim()
    return ExpensePalette.firstOrNull { (k, _) -> key.contains(k) }?.second
        ?: ExpensePalette[key.hashCode().absoluteValue % ExpensePalette.size].second
}

private fun sourceIcon(source: String): ImageVector = when {
    source.contains("salary", true) || source.contains("bank", true) -> Icons.Rounded.AccountBalance
    source.contains("freelance", true) || source.contains("code", true) -> Icons.Rounded.Code
    source.contains("adsense", true) || source.contains("ads", true) -> Icons.Rounded.Language
    source.contains("crypto", true) || source.contains("bitcoin", true) -> Icons.Rounded.CurrencyBitcoin
    source.contains("food", true) || source.contains("restaurant", true) -> Icons.Rounded.Restaurant
    source.contains("transport", true) || source.contains("bike", true) || source.contains("pickme", true) -> Icons.Rounded.DirectionsBike
    source.contains("rent", true) || source.contains("housing", true) -> Icons.Rounded.Home
    source.contains("sub", true) || source.contains("stream", true) -> Icons.Rounded.Subscriptions
    source.contains("freelance", true) || source.contains("work", true) -> Icons.Rounded.Work
    else -> Icons.Rounded.AttachMoney
}

// ── Formatters ─────────────────────────────────────────────────────────────────
private fun formatLKR(amount: Double): String {
    val nf = NumberFormat.getNumberInstance(Locale.US)
    nf.maximumFractionDigits = 0
    return "LKR ${nf.format(amount)}"
}

private fun formatPeriodLabel(period: Period): String {
    val cal = Calendar.getInstance()
    return when (period) {
        Period.WEEK -> "This Week"
        Period.MONTH -> SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
        Period.YEAR -> SimpleDateFormat("yyyy", Locale.getDefault()).format(cal.time)
    }
}

private fun formatDateHeader(millis: Long): String {
    if (millis == 0L) return "Unknown"
    val dateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    val cal = Calendar.getInstance()
    val now = Calendar.getInstance()
    cal.timeInMillis = millis
    return when {
        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) ->
            "Today · ${dateFormat.format(cal.time)}"
        cal.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1 ->
            "Yesterday"
        else -> dateFormat.format(cal.time)
    }
}

private fun formatTime(millis: Long): String {
    if (millis == 0L) return ""
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(java.util.Date(millis))
}

// ── Shimmer ─────────────────────────────────────────────────────────────────
@Composable
private fun Modifier.shimmerEffect(): Modifier {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_progress",
    )
    return drawWithContent {
        drawContent()
        val gradientWidth = size.width * 0.4f
        val startX = size.width * progress - gradientWidth
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.4f), Color.Transparent),
                startX = startX,
                endX = startX + gradientWidth,
            ),
        )
    }
}

@Composable
private fun ShimmerBox(modifier: Modifier = Modifier, height: Dp = 80.dp, cornerRadius: Dp = 12.dp) {
    val shimmerColor = if (LocalAppDarkTheme.current) DarkSurfaceVariant else MaterialTheme.colorScheme.outlineVariant
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerColor)
            .shimmerEffect()
    )
}

// ── Entry point ────────────────────────────────────────────────────────────────
@Composable
fun TransactionScreen(
    viewModel: TransactionViewModel = hiltViewModel(),
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToGoals: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onSeeAllTransactions: () -> Unit = {},
) {
    val transactions by viewModel.transactions.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val selectedCategoryFilter by viewModel.selectedCategoryFilter.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpenses by viewModel.totalExpenses.collectAsState()
    val netSavings by viewModel.netSavings.collectAsState()
    val incomeBySource by viewModel.incomeBySource.collectAsState()
    val expenseByCategory by viewModel.expenseByCategory.collectAsState()
    val monthlyBarData by viewModel.monthlyBarData.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val editingTransaction by viewModel.editingTransaction.collectAsState()
    val pendingDelete by viewModel.pendingDelete.collectAsState()
    val isLoadingMore by viewModel.isLoadingMore.collectAsState()
    val hasMore by viewModel.hasMore.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    // Pending delete → show snackbar
    LaunchedEffect(pendingDelete) {
        val item = pendingDelete ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "${item.displayName} deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )
        if (result == SnackbarResult.ActionPerformed) viewModel.undoDelete()
        else viewModel.confirmDelete()
    }

    val bgColor = backgroundColor()
    val bgMidColor = backgroundMidColor()
    val orangeGlowColor = orbOrangeColor()
    val purpleGlowColor = orbPurpleColor()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(bgColor, bgMidColor, bgColor)
                    )
                ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orangeGlowColor,
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.88f, size.height * 0.10f),
                        radius = 260.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.88f, size.height * 0.10f),
                    radius = 260.dp.toPx(),
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            purpleGlowColor,
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.72f),
                        radius = 220.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.15f, size.height * 0.72f),
                    radius = 220.dp.toPx(),
                )
            }

            // Search overlay
            AnimatedVisibility(
                visible = isSearchActive,
                enter = fadeIn() + slideInVertically { -40 },
                exit = fadeOut() + slideOutVertically { -40 },
            ) {
                SearchOverlay(
                    query = searchQuery,
                    onQueryChange = viewModel::setSearchQuery,
                    onClose = { viewModel.setSearchActive(false) },
                )
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh,
                state = pullRefreshState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                TransactionContent(
                    transactions = transactions,
                    selectedPeriod = selectedPeriod,
                    totalIncome = totalIncome,
                    totalExpenses = totalExpenses,
                    netSavings = netSavings,
                    incomeBySource = incomeBySource,
                    expenseByCategory = expenseByCategory,
                    monthlyBarData = monthlyBarData,
                    selectedCategoryFilter = selectedCategoryFilter,
                    onPeriodChange = viewModel::selectPeriod,
                    onSearchClick = { viewModel.setSearchActive(true) },
                    onCategoryFilterChange = viewModel::selectCategoryFilter,
                    onDeleteTransaction = viewModel::markForDelete,
                    onEditTransaction = viewModel::startEditTransaction,
                    onSeeAll = onSeeAllTransactions,
                    isLoadingMore = isLoadingMore,
                    hasMore = hasMore,
                    onLoadNextPage = viewModel::loadNextPage,
                )
            }

            FinPilotBottomNavBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentTab = NavTab.TRANSACTIONS,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToTransactions = { /* already here */ },
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    }

    // Edit bottom sheet
    editingTransaction?.let { item ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val sheetBgColor = glassBgColor()
        ModalBottomSheet(
            onDismissRequest = viewModel::clearEditTransaction,
            sheetState = sheetState,
            containerColor = sheetBgColor,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            TransactionEditSheet(
                item = item,
                onDismiss = viewModel::clearEditTransaction,
            )
        }
    }
}

// ── Main scrollable content ────────────────────────────────────────────────────
@Composable
private fun TransactionContent(
    transactions: List<TransactionItem>,
    selectedPeriod: Period,
    totalIncome: Double,
    totalExpenses: Double,
    netSavings: Double,
    incomeBySource: Map<String, Double>,
    expenseByCategory: Map<String, Double>,
    monthlyBarData: List<MonthlyBarData>,
    selectedCategoryFilter: String?,
    onPeriodChange: (Period) -> Unit,
    onSearchClick: () -> Unit,
    onCategoryFilterChange: (String?) -> Unit,
    onDeleteTransaction: (TransactionItem) -> Unit,
    onEditTransaction: (TransactionItem) -> Unit,
    onSeeAll: () -> Unit,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onLoadNextPage: () -> Unit,
) {
    var typeFilter by remember { mutableStateOf("All") }
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 10

    val filteredTransactions = remember(transactions, typeFilter) {
        when (typeFilter) {
            "Income" -> transactions.filter { it.type == TransactionType.INCOME }
            "Expenses" -> transactions.filter { it.type == TransactionType.EXPENSE }
            else -> transactions
        }
    }

    LaunchedEffect(filteredTransactions) { currentPage = 0 }

    val totalPages = max(1, (filteredTransactions.size + pageSize - 1) / pageSize)
    val pagedTransactions = remember(filteredTransactions, currentPage) {
        filteredTransactions.drop(currentPage * pageSize).take(pageSize)
    }

    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 110.dp),
    ) {
        // ── Section 1+2: Orange header + overlapping summary cards ────────────
        item {
            OrangeHeaderWithCards(
                selectedPeriod = selectedPeriod,
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                netSavings = netSavings,
                onPeriodChange = onPeriodChange,
                onSearchClick = onSearchClick,
            )
        }

        // ── Section 3: Bar chart ───────────────────────────────────────────────
        item {
            AnimatedContent(
                targetState = monthlyBarData,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically { 40 }).togetherWith(
                        fadeOut(tween(200))
                    )
                },
                label = "bar_chart_anim",
            ) { barData ->
                BarChartSection(barData = barData, selectedPeriod = selectedPeriod)
            }
        }

        // ── Section 4: Donut charts ────────────────────────────────────────────
        item {
            DonutChartsRow(
                incomeBySource = incomeBySource,
                expenseByCategory = expenseByCategory,
                totalIncome = totalIncome,
                totalExpenses = totalExpenses,
                selectedCategoryFilter = selectedCategoryFilter,
                onCategoryFilterChange = onCategoryFilterChange,
            )
        }

        // ── Section 5: Recent transactions ────────────────────────────────────
        stickyHeader {
            RecentTransactionsHeader(
                selectedCategoryFilter = selectedCategoryFilter,
                onClearFilter = { onCategoryFilterChange(null) },
                onSeeAll = onSeeAll,
            )
        }

        item {
            TransactionTypeToggle(
                selected = typeFilter,
                onSelect = { typeFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
        }

        if (filteredTransactions.isEmpty()) {
            item { TransactionEmptyState() }
        } else {
            item {
                TransactionTable(
                    transactions = pagedTransactions,
                    onDelete = onDeleteTransaction,
                    onEdit = onEditTransaction,
                )
            }
            item {
                TransactionPaginationBar(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    hasMore = hasMore,
                    isLoadingMore = isLoadingMore,
                    onPreviousPage = { currentPage-- },
                    onNextPage = {
                        currentPage++
                        if (currentPage >= totalPages - 1 && hasMore && !isLoadingMore) {
                            onLoadNextPage()
                        }
                    },
                )
            }
        }
    }
}

// ── Section 1 + 2: Orange header with overlapping cards ───────────────────────
@Composable
private fun OrangeHeaderWithCards(
    selectedPeriod: Period,
    totalIncome: Double,
    totalExpenses: Double,
    netSavings: Double,
    onPeriodChange: (Period) -> Unit,
    onSearchClick: () -> Unit,
) {
    val animatedIncome by animateFloatAsState(
        targetValue = totalIncome.toFloat(),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "income_anim",
    )
    val animatedExpenses by animateFloatAsState(
        targetValue = totalExpenses.toFloat(),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "expense_anim",
    )
    val animatedSavings by animateFloatAsState(
        targetValue = netSavings.toFloat(),
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "savings_anim",
    )
    val periodLabel = formatPeriodLabel(selectedPeriod)

    val bgStartColor = backgroundColor()
    val bgMidColor = backgroundMidColor()
    val orangeGlowColor = orbOrangeColor()
    val purpleGlowColor = orbPurpleColor()

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Hero glass gradient with soft glow
        DynamicHeaderBackground(
            patternType = "transaction",
            modifier = Modifier.matchParentSize().clip(wavyBottomShape())
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Spacer(modifier = Modifier.statusBarsPadding())

            // Top bar row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Transactions",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor(),
                )
                Row {
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = textPrimaryColor(),
                        )
                    }
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "Filter",
                            tint = textPrimaryColor(),
                        )
                    }
                }
            }

            // Period tab pill
            PeriodTabPill(
                selectedPeriod = selectedPeriod,
                onPeriodChange = onPeriodChange,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 12.dp),
            )

            // Summary cards — start here, overlapping the orange bg at bottom
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Income",
                    labelColor = IncomeColor,
                    amount = animatedIncome.toDouble(),
                    periodLabel = periodLabel,
                    icon = Icons.AutoMirrored.Rounded.TrendingUp,
                    iconTint = IncomeColor,
                    bgColor = incomeSummaryBgColor(),
                )
                SummaryCard(
                    modifier = Modifier.weight(1f),
                    label = "Expenses",
                    labelColor = ExpenseColor,
                    amount = animatedExpenses.toDouble(),
                    periodLabel = periodLabel,
                    icon = Icons.AutoMirrored.Rounded.TrendingDown,
                    iconTint = ExpenseColor,
                    bgColor = expenseSummaryBgColor(),
                )
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Net savings banner — below the overlap zone
    NetSavingsBanner(
        netSavings = animatedSavings.toDouble(),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
    )
}

@Composable
private fun PeriodTabPill(
    selectedPeriod: Period,
    onPeriodChange: (Period) -> Unit,
    modifier: Modifier = Modifier,
) {
    val periods = listOf(Period.WEEK to "Week", Period.MONTH to "Month", Period.YEAR to "Year")
    val density = LocalDensity.current
    val isDark = LocalAppDarkTheme.current

    // Track the center positions of each tab
    val tabCenters = remember { mutableStateListOf(0f, 0f, 0f) }

    // Single position value - directly updated for instant response
    val sliderX = remember { Animatable(0f) }
    var hasInitializedPosition by remember { mutableStateOf(false) }
    var isDragging by remember { mutableStateOf(false) }

    // Get the active index
    val activeIndex = periods.indexOfFirst { it.first == selectedPeriod }.coerceAtLeast(0)
    var swipeIndex by remember { mutableStateOf(activeIndex) }

    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    // Calculate parent padding offset in pixels (the 4.dp padding on the Box)
    val parentPaddingPx = with(density) { 4.dp.toPx() }

    // Animate to active tab when not dragging
    LaunchedEffect(activeIndex, tabCenters.toList()) {
        val target = tabCenters.getOrNull(activeIndex) ?: 0f
        if (target != 0f) {
            if (!hasInitializedPosition) {
                sliderX.snapTo(target)
                hasInitializedPosition = true
            } else if (!isDragging) {
                sliderX.animateTo(
                    targetValue = target,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    // Haptic feedback on index change during drag
    val closestIndex = remember(sliderX.value, tabCenters.toList()) {
        if (tabCenters.isEmpty() || tabCenters.all { it == 0f }) {
            swipeIndex
        } else {
            tabCenters.indices.minByOrNull { i ->
                (tabCenters[i] - sliderX.value).absoluteValue
            } ?: swipeIndex
        }
    }

    var lastHapticIndex by remember { mutableStateOf(activeIndex) }

    LaunchedEffect(closestIndex) {
        if (isDragging && closestIndex != lastHapticIndex) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            lastHapticIndex = closestIndex
        }
    }

    val pillWidth = 52.dp
    val pillWidthPx = with(density) { pillWidth.toPx() }
    val maxMagnifyDistance = with(density) { 40.dp.toPx() }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (isDark) {
                    Color(0xFF1A1A1A).copy(alpha = 0.6f)
                } else {
                    Color.White.copy(alpha = 0.6f)
                }
            )
            .border(
                width = 1.dp,
                brush = if (isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0x40FFFFFF),
                            Color(0x15FFFFFF),
                            Color(0x20FFFFFF),
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xF0FFFFFF),
                            Color(0x5DFFFFFF),
                            Color(0x66FFFFFF),
                        )
                    )
                },
                shape = RoundedCornerShape(50)
            )
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        isDragging = true
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val newX = (sliderX.value + dragAmount).coerceIn(
                            tabCenters.firstOrNull() ?: 0f,
                            tabCenters.lastOrNull() ?: 0f
                        )
                        coroutineScope.launch {
                            sliderX.snapTo(newX)
                        }
                    },
                    onDragEnd = {
                        isDragging = false
                        val finalIndex = tabCenters.indices.minByOrNull { i ->
                            (tabCenters[i] - sliderX.value).absoluteValue
                        } ?: swipeIndex
                        swipeIndex = finalIndex
                        onPeriodChange(periods[finalIndex].first)
                        coroutineScope.launch {
                            sliderX.animateTo(
                                targetValue = tabCenters.getOrNull(finalIndex) ?: 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    },
                    onDragCancel = {
                        isDragging = false
                        coroutineScope.launch {
                            sliderX.animateTo(
                                targetValue = tabCenters.getOrNull(swipeIndex) ?: 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    },
                )
            }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            periods.forEachIndexed { index, (period, label) ->
                val isActive = index == swipeIndex
                val center = tabCenters.getOrNull(index) ?: 0f

                // Magnifying glass effect - scale up when pill is near
                val scaleFactor = if (center != 0f && hasInitializedPosition) {
                    val distance = (center - sliderX.value).absoluteValue
                    if (distance < maxMagnifyDistance) {
                        // Calculate magnification based on proximity
                        val fraction = 1f - (distance / maxMagnifyDistance)
                        // Smooth easing for natural feel
                        val smoothFraction = kotlin.math.sin(fraction * Math.PI / 2).toFloat()
                        // Magnify up to 20% larger
                        1f + 0.20f * smoothFraction
                    } else {
                        1f
                    }
                } else {
                    if (isActive) 1.12f else 1f
                }

                // Also increase font weight based on proximity for stronger magnifying effect
                val fontWeight = if (center != 0f && hasInitializedPosition) {
                    val distance = (center - sliderX.value).absoluteValue
                    if (distance < maxMagnifyDistance * 0.7f) {
                        FontWeight.Bold
                    } else if (distance < maxMagnifyDistance) {
                        FontWeight.SemiBold
                    } else {
                        FontWeight.Medium
                    }
                } else {
                    if (isActive) FontWeight.Bold else FontWeight.Medium
                }

                val textScale by animateFloatAsState(
                    targetValue = scaleFactor,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh,
                    ),
                    label = "scale_$label",
                )

                Text(
                    text = label,
                    modifier = Modifier
                        .scale(textScale)
                        .onGloballyPositioned { coords ->
                            val textWidth = coords.size.width
                            val textLeft = coords.positionInParent().x + parentPaddingPx
                            val textCenter = textLeft + textWidth / 2f
                            if (index < tabCenters.size) {
                                tabCenters[index] = textCenter
                            }
                        }
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            swipeIndex = index
                            onPeriodChange(period)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    color = if (isActive) {
                        if (isDark) Color.White else Color.Black
                    } else {
                        textSecondaryColor()
                    },
                    fontSize = 12.sp,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // ── Magnifying glass pill - transparent with visible border ──
        if (hasInitializedPosition) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset {
                        IntOffset(
                            x = (sliderX.value - pillWidthPx / 2f).roundToInt(),
                            y = 0
                        )
                    }
                    .size(width = pillWidth, height = 30.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        brush = if (isDark) {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x80FFFFFF),
                                    Color(0x40FFFFFF),
                                    Color(0x20FFFFFF),
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFBBBBBB),
                                    Color(0x88AAAAAA),
                                    Color(0x44999999),
                                )
                            )
                        },
                        shape = RoundedCornerShape(50)
                    )
                    // Inner glow for magnifying glass effect
                    .border(
                        width = 0.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.05f),
                                Color.Transparent,
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}

@Composable
private fun SummaryCard(
    modifier: Modifier = Modifier,
    label: String,
    labelColor: Color,
    amount: Double,
    periodLabel: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(glassBgColor())
            .border(1.dp, glassBorderLightColor(), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = labelColor,
            )
        }
        Text(
            text = formatLKR(amount),
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimaryColor(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = periodLabel,
            fontSize = 11.sp,
            color = textHintColor(),
        )
    }
}

@Composable
private fun NetSavingsBanner(netSavings: Double, modifier: Modifier = Modifier) {
    val isPositive = netSavings >= 0
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(glassBgColor())
            .border(1.dp, glassBorderColor(), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Net savings this month",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimaryColor(),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = if (isPositive) Icons.AutoMirrored.Rounded.TrendingUp
                        else Icons.AutoMirrored.Rounded.TrendingDown,
                        contentDescription = null,
                        tint = if (isPositive) IncomeColor else ExpenseColor,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "${if (isPositive) "▲" else "▼"} vs last month",
                        fontSize = 11.sp,
                        color = if (isPositive) IncomeColor else ExpenseColor,
                    )
                }
            }
            Text(
                text = (if (netSavings >= 0) "+" else "") + formatLKR(netSavings),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPositive) OrangePrimary else ExpenseColor,
            )
        }
    }
}

// ── Section 3: Bar chart ───────────────────────────────────────────────────────
@Composable
private fun BarChartSection(barData: List<MonthlyBarData>, selectedPeriod: Period) {
    if (barData.isEmpty()) return
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderLightColor()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Income vs Expenses",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor(),
                )
                Text(
                    text = "6 months",
                    fontSize = 12.sp,
                    color = OrangePrimary,
                    fontWeight = FontWeight.Medium,
                )
            }

            // Legend
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LegendDot(color = OrangePrimary, label = "Income")
                LegendDot(color = OrangeLight, label = "Expenses")
            }

            // Chart canvas
            GroupedBarChart(
                barData = barData,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            )
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color),
        )
        Text(text = label, fontSize = 11.sp, color = textHintColor())
    }
}

@Composable
private fun GroupedBarChart(
    barData: List<MonthlyBarData>,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val maxVal = remember(barData) {
        barData.maxOfOrNull { maxOf(it.income, it.expenses) }.takeIf { it != null && it > 0 } ?: 1.0
    }

    // Animate bar heights
    val animatedBars = barData.mapIndexed { i, bar ->
        val aIncome by animateFloatAsState(
            targetValue = (bar.income / maxVal).toFloat(),
            animationSpec = tween(600, delayMillis = i * 60, easing = FastOutSlowInEasing),
            label = "income_$i",
        )
        val aExpense by animateFloatAsState(
            targetValue = (bar.expenses / maxVal).toFloat(),
            animationSpec = tween(600, delayMillis = i * 60 + 30, easing = FastOutSlowInEasing),
            label = "expense_$i",
        )
        Triple(bar, aIncome, aExpense)
    }

    val mutedLabelColor = textHintColor()
    val isDark = LocalAppDarkTheme.current
    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(barData) {
                    detectTapGestures { offset ->
                        val groupCount = barData.size
                        val groupWidth = size.width / groupCount
                        val tapped = (offset.x / groupWidth).toInt().coerceIn(0, groupCount - 1)
                        selectedIndex = if (selectedIndex == tapped) null else tapped
                    }
                },
        ) {
            val groupCount = barData.size
            val groupWidth = size.width / groupCount
            val barWidth = groupWidth * 0.28f
            val gap = groupWidth * 0.06f
            val cornerR = 6.dp.toPx()

            animatedBars.forEachIndexed { i, (bar, incomeRatio, expenseRatio) ->
                val groupStart = i * groupWidth
                val barBottom = size.height - 20.dp.toPx()
                val maxBarHeight = barBottom - 4.dp.toPx()

                val incomeHeight = (incomeRatio * maxBarHeight).coerceAtLeast(2f)
                val expenseHeight = (expenseRatio * maxBarHeight).coerceAtLeast(2f)

                val incomeLeft = groupStart + gap * 2
                val expenseLeft = incomeLeft + barWidth + gap

                val isSelected = selectedIndex == i
                val incomeAlpha = if (selectedIndex == null || isSelected) 1f else 0.4f
                val expenseAlpha = if (selectedIndex == null || isSelected) 1f else 0.4f

                // Income bar
                drawRoundRect(
                    color = OrangePrimary.copy(alpha = incomeAlpha),
                    topLeft = Offset(incomeLeft, barBottom - incomeHeight),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(cornerR),
                )
                // Expense bar
                drawRoundRect(
                    color = OrangeLight.copy(alpha = expenseAlpha),
                    topLeft = Offset(expenseLeft, barBottom - expenseHeight),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(cornerR),
                )

                // X-axis label
                val labelX = groupStart + groupWidth / 2f
                val labelY = size.height - 4.dp.toPx()
                val labelColor = if (isSelected) Color(0xFFFF6B00) else mutedLabelColor
                val measured = textMeasurer.measure(
                    text = bar.month,
                    style = TextStyle(fontSize = 10.sp, color = labelColor),
                )
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        x = labelX - measured.size.width / 2f,
                        y = labelY - measured.size.height,
                    ),
                )
            }
        }

        // Tooltip
        selectedIndex?.let { idx ->
            val bar = barData.getOrNull(idx) ?: return@let
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(8.dp))
                    .background(textPrimaryColor().copy(alpha = 0.9f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            ) {
                Text(
                    text = "${formatLKR(bar.income)} in · ${formatLKR(bar.expenses)} exp",
                    fontSize = 10.sp,
                    color = if (isDark) Color.Black else Color.White,
                )
            }
        }
    }
}

// ── Section 4: Donut charts ────────────────────────────────────────────────────
@Composable
private fun DonutChartsRow(
    incomeBySource: Map<String, Double>,
    expenseByCategory: Map<String, Double>,
    totalIncome: Double,
    totalExpenses: Double,
    selectedCategoryFilter: String?,
    onCategoryFilterChange: (String?) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Income donut
        DonutChartCard(
            modifier = Modifier.weight(1f),
            title = "Income sources",
            dataMap = incomeBySource,
            totalAmount = totalIncome,
            colorFn = ::incomeColor,
            selectedSlice = selectedCategoryFilter,
            onSliceTap = { /* income filter is separate */ },
        )
        // Expense donut
        DonutChartCard(
            modifier = Modifier.weight(1f),
            title = "Expense breakdown",
            dataMap = expenseByCategory,
            totalAmount = totalExpenses,
            colorFn = ::expenseColor,
            selectedSlice = selectedCategoryFilter,
            onSliceTap = onCategoryFilterChange,
        )
    }
}

@Composable
private fun DonutChartCard(
    modifier: Modifier = Modifier,
    title: String,
    dataMap: Map<String, Double>,
    totalAmount: Double,
    colorFn: (String) -> Color,
    selectedSlice: String?,
    onSliceTap: (String?) -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderLightColor()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textSecondaryColor(),
                textAlign = TextAlign.Center,
            )

            if (dataMap.isEmpty()) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(glassSurfaceColor()),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("No data", fontSize = 10.sp, color = textHintColor())
                }
            } else {
                DonutChart(
                    dataMap = dataMap,
                    totalAmount = totalAmount,
                    colorFn = colorFn,
                    selectedSlice = selectedSlice,
                    onSliceTap = onSliceTap,
                    modifier = Modifier.size(110.dp),
                )
            }

            // Mini category bars
            val sorted = dataMap.entries.sortedByDescending { it.value }.take(4)
            val total = sorted.sumOf { it.value }.coerceAtLeast(0.01)
            sorted.forEach { (key, value) ->
                MiniCategoryBarRow(
                    label = key.replaceFirstChar { it.titlecase() },
                    fraction = (value / total).toFloat(),
                    color = colorFn(key),
                )
            }
        }
    }
}

@Composable
private fun DonutChart(
    dataMap: Map<String, Double>,
    totalAmount: Double,
    colorFn: (String) -> Color,
    selectedSlice: String?,
    onSliceTap: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val entries = remember(dataMap) {
        val total = dataMap.values.sum().coerceAtLeast(0.01)
        dataMap.entries.sortedByDescending { it.value }.map { (k, v) ->
            Triple(k, v, (v / total * 360f).toFloat())
        }
    }

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(entries) {
                    detectTapGestures { offset ->
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val dx = offset.x - cx
                        val dy = offset.y - cy
                        val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                        val outerR = size.width / 2f
                        val innerR = outerR * 0.5f
                        if (dist < innerR || dist > outerR) {
                            onSliceTap(null)
                            return@detectTapGestures
                        }
                        var angle = Math.toDegrees(kotlin.math.atan2(dy.toDouble(), dx.toDouble())).toFloat() + 90f
                        if (angle < 0) angle += 360f
                        var cumulative = 0f
                        for ((k, _, sweep) in entries) {
                            if (angle >= cumulative && angle < cumulative + sweep) {
                                onSliceTap(k)
                                return@detectTapGestures
                            }
                            cumulative += sweep
                        }
                        onSliceTap(null)
                    }
                },
        ) {
            val strokeWidth = size.width * 0.2f
            val radius = (size.width - strokeWidth) / 2f
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(radius * 2, radius * 2)
            var startAngle = -90f

            entries.forEach { (key, _, sweepAngle) ->
                val isSelected = selectedSlice.equals(key, ignoreCase = true)
                val effectiveSweep = (sweepAngle - 2f).coerceAtLeast(1f)
                drawArc(
                    color = colorFn(key).copy(alpha = if (selectedSlice != null && !isSelected) 0.4f else 1f),
                    startAngle = startAngle + 1f,
                    sweepAngle = effectiveSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = if (isSelected) strokeWidth * 1.15f else strokeWidth,
                        cap = StrokeCap.Round,
                    ),
                )
                startAngle += sweepAngle
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = formatLKR(totalAmount),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor(),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun MiniCategoryBarRow(
    label: String,
    fraction: Float,
    color: Color,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "mini_bar_$label",
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = textHintColor(),
            modifier = Modifier.width(48.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(glassBorderColor()),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color),
            )
        }
        Text(
            text = "${(fraction * 100).toInt()}%",
            fontSize = 9.sp,
            color = textHintColor(),
        )
    }
}

// ── Section 5: Recent transactions ────────────────────────────────────────────
@Composable
private fun RecentTransactionsHeader(
    selectedCategoryFilter: String?,
    onClearFilter: () -> Unit,
    onSeeAll: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundMidColor().copy(alpha = 0.88f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Recent transactions",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = textPrimaryColor(),
            )
            Text(
                text = "See all",
                fontSize = 13.sp,
                color = OrangePrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSeeAll),
            )
        }
        AnimatedVisibility(visible = selectedCategoryFilter != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(orangeDimColor())
                        .border(1.dp, glassBorderColor(), RoundedCornerShape(50))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedCategoryFilter?.replaceFirstChar { it.titlecase() } ?: "",
                            fontSize = 12.sp,
                            color = OrangePrimary,
                        )
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Clear filter",
                            tint = OrangePrimary,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable(onClick = onClearFilter),
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = glassBorderColor(), thickness = 1.dp)
    }
}

@Composable
private fun DateGroupHeader(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(backgroundColor(), Color.Transparent),
                )
            )
            .padding(horizontal = 16.dp, vertical = 6.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.CalendarToday,
                contentDescription = null,
                tint = textHintColor(),
                modifier = Modifier.size(12.dp),
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = textHintColor(),
            )
        }
    }
}

@Composable
private fun SwipeToDismissTransactionRow(
    transaction: TransactionItem,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f },
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val progress by animateFloatAsState(
                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) 1f else 0f,
                label = "swipe_progress",
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(ExpenseColor.copy(alpha = 0.12f + progress * 0.5f))
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Delete",
                        tint = ExpenseColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(text = "Delete", fontSize = 10.sp, color = ExpenseColor)
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
    ) {
        TransactionRow(
            transaction = transaction,
            onLongPress = onEdit,
        )
    }
}

@Composable
private fun TransactionRow(
    transaction: TransactionItem,
    onLongPress: () -> Unit,
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val iconColor = if (isIncome) OrangePrimary else expenseColor(transaction.source)
    val iconBg = iconColor.copy(alpha = 0.12f)
    val amountPrefix = if (isIncome) "+" else "−"
    val amountColor = if (isIncome) IncomeColor else ExpenseColor

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(glassBgColor())
            .border(1.dp, glassBorderLightColor(), RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = onLongPress,
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = sourceIcon(transaction.source),
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp),
            )
        }

        // Middle content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = transaction.displayName,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimaryColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TransactionChip(
                    label = transaction.source.replaceFirstChar { it.titlecase() },
                    color = iconColor,
                )
                if (transaction.paymentMethod.isNotBlank()) {
                    TransactionChip(
                        label = transaction.paymentMethod.replaceFirstChar { it.titlecase() },
                        color = textSecondaryColor(),
                    )
                }
                if (transaction.isRecurring) {
                    TransactionChip(
                        label = "↻ Recurring",
                        color = Color(0xFF8B5CF6),
                    )
                }
            }
        }

        // Trailing amount + time
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "$amountPrefix${formatLKR(transaction.amountInLKR)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor,
            )
            if (transaction.currency != "LKR" && transaction.exchangeRate != null) {
                Text(
                    text = "${transaction.currency} ${transaction.amount}",
                    fontSize = 10.sp,
                    color = textHintColor(),
                )
            } else {
                Text(
                    text = formatTime(transaction.timestampMillis),
                    fontSize = 10.sp,
                    color = textHintColor(),
                )
            }
        }
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun TransactionChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .border(0.5.dp, color.copy(alpha = 0.28f), RoundedCornerShape(10.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp),
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

// ── Empty state ────────────────────────────────────────────────────────────────
@Composable
private fun TransactionEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(orangeDimColor())
                .border(1.dp, glassBorderColor(), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Rounded.AllInclusive,
                contentDescription = null,
                tint = OrangePrimary,
                modifier = Modifier.size(40.dp),
            )
        }
        Text(
            text = "No transactions yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimaryColor(),
        )
        Text(
            text = "Add your first income or expense\nto start tracking your finances.",
            fontSize = 13.sp,
            color = textHintColor(),
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(OrangePrimary)
                .padding(horizontal = 24.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Add Transaction",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

// ── Search overlay ─────────────────────────────────────────────────────────────
@Composable
private fun SearchOverlay(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundMidColor().copy(alpha = 0.96f))
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .statusBarsPadding(),
        contentAlignment = Alignment.Center,
    ) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search transactions...", fontSize = 14.sp, color = textHintColor()) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = OrangePrimary,
                )
            },
            trailingIcon = {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close search",
                        tint = textHintColor(),
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50)),
            singleLine = true,
            textStyle = TextStyle(color = textPrimaryColor()),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = glassSurfaceColor(),
                unfocusedContainerColor = glassSurfaceColor(),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = textPrimaryColor(),
                unfocusedTextColor = textPrimaryColor(),
                cursorColor = OrangePrimary,
            ),
        )
    }
}

// ── Edit bottom sheet ──────────────────────────────────────────────────────────
@Composable
private fun TransactionEditSheet(
    item: TransactionItem,
    onDismiss: () -> Unit,
) {
    val isIncome = item.type == TransactionType.INCOME
    val iconColor = if (isIncome) OrangePrimary else expenseColor(item.source)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(glassBorderColor())
                .align(Alignment.CenterHorizontally),
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = sourceIcon(item.source),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp),
                )
            }
            Column {
                Text(
                    text = item.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimaryColor(),
                )
                Text(
                    text = if (isIncome) "Income · ${item.source.replaceFirstChar { it.titlecase() }}"
                    else "Expense · ${item.source.replaceFirstChar { it.titlecase() }}",
                    fontSize = 12.sp,
                    color = textSecondaryColor(),
                )
            }
        }

        HorizontalDivider(color = glassBorderColor())

        // Amount
        EditDetailRow(label = "Amount", value = formatLKR(item.amountInLKR))
        if (item.currency != "LKR" && item.exchangeRate != null) {
            EditDetailRow(
                label = "Original",
                value = "${item.currency} ${"%.2f".format(item.amount)} → @${item.exchangeRate}",
            )
        }
        EditDetailRow(
            label = "Date",
            value = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())
                .format(java.util.Date(item.timestampMillis)),
        )
        EditDetailRow(
            label = "Payment",
            value = item.paymentMethod.replaceFirstChar { it.titlecase() },
        )
        if (!item.note.isNullOrBlank()) {
            EditDetailRow(label = "Note", value = item.note)
        }
        if (item.isRecurring) {
            EditDetailRow(label = "Recurring", value = "Yes ↻")
        }

        HorizontalDivider(color = glassBorderColor())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(orangeDimColor())
                .border(1.dp, glassBorderColor(), RoundedCornerShape(12.dp))
                .clickable(onClick = onDismiss)
                .padding(vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Close",
                color = OrangePrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
            )
        }
    }
}

@Composable
private fun EditDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(text = label, fontSize = 13.sp, color = textSecondaryColor())
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textPrimaryColor(),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

// ── Transaction Type Toggle (glass UI) ────────────────────────────────────────
@Composable
private fun TransactionTypeToggle(
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val options = listOf("All", "Income", "Expenses")
    val isDark = LocalAppDarkTheme.current
    val primaryColor = textPrimaryColor()
    val secondaryColor = textSecondaryColor()
    val activeBg = if (isDark) DarkSurfaceVariant else Color.White.copy(alpha = 0.85f)
    val borderColor = if (isDark) DarkGlassBorder else GlassTheme.GlassBorder
    val outerBg = if (isDark) Color(0xFF1A1A1A).copy(alpha = 0.65f) else Color.White.copy(alpha = 0.55f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(outerBg)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(4.dp),
    ) {
        options.forEach { option ->
            val isActive = option == selected
            val bgColor by animateColorAsState(
                targetValue = when {
                    isActive && option == "Income" -> IncomeColor.copy(alpha = 0.18f)
                    isActive && option == "Expenses" -> ExpenseColor.copy(alpha = 0.18f)
                    isActive -> activeBg
                    else -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "toggle_bg_$option",
            )
            val textColor by animateColorAsState(
                targetValue = when {
                    isActive && option == "Income" -> IncomeColor
                    isActive && option == "Expenses" -> ExpenseColor
                    isActive -> primaryColor
                    else -> secondaryColor
                },
                animationSpec = tween(200),
                label = "toggle_text_$option",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(bgColor)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onSelect(option) }
                    .padding(vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = option,
                    fontSize = 13.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = textColor,
                )
            }
        }
    }
}

// ── Transaction Table (glass card, very round corners) ────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionTable(
    transactions: List<TransactionItem>,
    onDelete: (TransactionItem) -> Unit,
    onEdit: (TransactionItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (transactions.isEmpty()) return
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor()),
        border = androidx.compose.foundation.BorderStroke(1.dp, glassBorderLightColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            transactions.forEachIndexed { index, txn ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { value ->
                        if (value == SwipeToDismissBoxValue.EndToStart) {
                            onDelete(txn)
                            true
                        } else false
                    },
                    positionalThreshold = { it * 0.40f },
                )
                SwipeToDismissBox(
                    state = dismissState,
                    enableDismissFromStartToEnd = false,
                    backgroundContent = {
                        val isDeleting = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
                        val progress by animateFloatAsState(
                            targetValue = if (isDeleting) 1f else 0f,
                            label = "table_swipe_$index",
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(ExpenseColor.copy(alpha = 0.10f + progress * 0.45f))
                                .padding(horizontal = 20.dp),
                            contentAlignment = Alignment.CenterEnd,
                        ) {
                            if (isDeleting) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Close,
                                        contentDescription = "Delete",
                                        tint = ExpenseColor,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Text(
                                        text = "Delete",
                                        fontSize = 9.sp,
                                        color = ExpenseColor,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                            }
                        }
                    },
                ) {
                    TransactionTableRow(
                        transaction = txn,
                        onLongPress = { onEdit(txn) },
                    )
                }
                if (index < transactions.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = glassBorderColor(),
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionTableRow(
    transaction: TransactionItem,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeColor else ExpenseColor
    val amountPrefix = if (isIncome) "+" else "−"
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(glassBgColor())
            .combinedClickable(onClick = {}, onLongClick = onLongPress)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = transaction.displayName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textPrimaryColor(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = transaction.source.replaceFirstChar { it.titlecase() },
                    fontSize = 11.sp,
                    color = textHintColor(),
                )
                if (transaction.isRecurring) {
                    Text(text = "· ↻", fontSize = 11.sp, color = Color(0xFF8B5CF6))
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (transaction.timestampMillis != 0L)
                SimpleDateFormat("d MMM", Locale.getDefault())
                    .format(java.util.Date(transaction.timestampMillis))
            else "",
            fontSize = 11.sp,
            color = textHintColor(),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "$amountPrefix${formatLKR(transaction.amountInLKR)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = amountColor,
            )
            if (transaction.currency != "LKR" && transaction.exchangeRate != null) {
                Text(
                    text = "${transaction.currency} ${transaction.amount}",
                    fontSize = 10.sp,
                    color = textHintColor(),
                )
            }
        }
    }
}

// ── Transaction Pagination Bar ────────────────────────────────────────────────
@Composable
private fun TransactionPaginationBar(
    currentPage: Int,
    totalPages: Int,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val canGoPrev = currentPage > 0
        val canGoNext = currentPage < totalPages - 1 || hasMore

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canGoPrev) glassSurfaceColor() else Color.Transparent)
                .border(
                    0.8.dp,
                    if (canGoPrev) glassBorderColor() else Color.Transparent,
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

        if (isLoadingMore) {
            CircularProgressIndicator(
                color = OrangePrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
            )
        } else {
            Text(
                text = "Page ${currentPage + 1} of ${if (hasMore && currentPage >= totalPages - 1) "${totalPages}+" else "$totalPages"}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = textHintColor(),
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canGoNext && !isLoadingMore) glassSurfaceColor() else Color.Transparent)
                .border(
                    0.8.dp,
                    if (canGoNext && !isLoadingMore) glassBorderColor() else Color.Transparent,
                    CircleShape,
                )
                .clickable(enabled = canGoNext && !isLoadingMore) { onNextPage() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next page",
                tint = if (canGoNext && !isLoadingMore) textPrimaryColor() else Color.Transparent,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
