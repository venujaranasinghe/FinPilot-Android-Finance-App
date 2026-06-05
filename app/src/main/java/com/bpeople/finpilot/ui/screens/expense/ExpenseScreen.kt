package com.bpeople.finpilot.ui.screens.expense

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.components.GlassTheme
import com.bpeople.finpilot.ui.components.LocalCurrentGlassTheme
import com.bpeople.finpilot.ui.components.DynamicHeaderBackground
import com.bpeople.finpilot.ui.components.wavyBottomShape
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlin.math.max
import kotlin.math.roundToInt

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatLKRFull(amount: Double) = "LKR %,.0f".format(amount)
private fun formatLKRShort(amount: Double): String = when {
    amount >= 1_000_000 -> "LKR %.1fM".format(amount / 1_000_000)
    amount >= 1_000     -> "LKR %.0fK".format(amount / 1_000)
    else                -> "LKR ${amount.roundToInt()}"
}

private fun dateLabel(millis: Long): String {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -1) }
    val date = Calendar.getInstance().also { it.timeInMillis = millis }
    return when {
        isSameDay(date, today)     -> "Today"
        isSameDay(date, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(millis))
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
            a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

private fun currentMonthStartMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

private data class MonthTrend(val label: String, val amount: Float)

private fun computeMonthlyTrend(entries: List<ExpenseEntry>): List<MonthTrend> {
    val cal = Calendar.getInstance()
    return (5 downTo 0).map { back ->
        val c = (cal.clone() as Calendar).also { it.add(Calendar.MONTH, -back) }
        val y = c.get(Calendar.YEAR); val m = c.get(Calendar.MONTH)
        val label = SimpleDateFormat("MMM", Locale.getDefault()).format(c.time)
        val total = entries.filter { e ->
            val ec = Calendar.getInstance().also { it.time = e.date?.toDate() ?: return@filter false }
            ec.get(Calendar.YEAR) == y && ec.get(Calendar.MONTH) == m
        }.sumOf { it.amount }.toFloat()
        MonthTrend(label, total)
    }
}

private fun categoryIcon(cat: String): ImageVector = when (cat) {
    "Food"          -> Icons.Rounded.Restaurant
    "Transport"     -> Icons.Rounded.DirectionsCar
    "Housing"       -> Icons.Rounded.Home
    "Subscriptions" -> Icons.Rounded.Subscriptions
    "Entertainment" -> Icons.Rounded.Movie
    "Health"        -> Icons.Rounded.LocalHospital
    else            -> Icons.Rounded.MoreHoriz
}

private fun paymentMethodIcon(method: String): ImageVector = when {
    method.equals("Card", ignoreCase = true)    -> Icons.Rounded.CreditCard
    method.equals("Cash", ignoreCase = true)    -> Icons.Rounded.Money
    method.contains("Auto", ignoreCase = true)  -> Icons.Rounded.Autorenew
    else                                        -> Icons.Rounded.CreditCard
}

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToIncome: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onExpenseAdded: (String) -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val state by viewModel.expenseState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.isSubmitted) {
        if (!state.isSubmitted) return@LaunchedEffect
        viewModel.consumeSubmitted()
        snackbarHostState.showSnackbar("Expense saved!")
    }
    LaunchedEffect(state.insightMessage) {
        val insight = state.insightMessage ?: return@LaunchedEffect
        viewModel.consumeInsight(); onExpenseAdded(insight)
    }
    LaunchedEffect(state.pendingDeleteEntry) {
        val entry = state.pendingDeleteEntry ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            "${entry.category} expense deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.undoDelete()
            SnackbarResult.Dismissed       -> viewModel.consumePendingDelete()
        }
    }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    ExpenseListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToIncome = onNavigateToIncome,
        onNavigateToTransactions = onNavigateToTransactions,
        onNavigateToGoals = onNavigateToGoals,
        onNavigateToProfile = onNavigateToProfile,
        onNavigateToSettings = onNavigateToSettings,
        onShowAddSheet = viewModel::onShowAddSheet,
        onHideAddSheet = viewModel::onHideAddSheet,
        onDeleteEntry = viewModel::deleteExpense,
        onDuplicateEntry = viewModel::duplicateExpense,
        onCategoryFilterChange = viewModel::onCategoryFilterChange,
        onChartCategorySelect = viewModel::onChartCategorySelect,
        onToggleTrendChart = viewModel::onToggleTrendChart,
        onDismissWarning = viewModel::dismissWarningBanner,
        onAmountChange = viewModel::onAmountChange,
        onCurrencyChange = viewModel::onCurrencyChange,
        onCategoryChange = viewModel::onCategoryChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
        onDateChange = viewModel::onDateChange,
        onNoteChange = viewModel::onNoteChange,
        onSubCategoryChange = viewModel::onSubCategoryChange,
        onRecurringChange = viewModel::onRecurringChange,
        onRequestSubmit = viewModel::requestSubmit,
        onRefreshRates = viewModel::refreshExchangeRates,
        onLoadNextPage = viewModel::loadNextPage,
        onToggleMonthlyView = viewModel::onToggleMonthlyView,
    )
}

// ── Main content ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseListContent(
    state: ExpenseViewModel.ExpenseUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateToDashboard: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    onShowAddSheet: () -> Unit,
    onHideAddSheet: () -> Unit,
    onDeleteEntry: (ExpenseEntry) -> Unit,
    onDuplicateEntry: (ExpenseEntry) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onChartCategorySelect: (String?) -> Unit,
    onToggleTrendChart: () -> Unit,
    onDismissWarning: () -> Unit,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubCategoryChange: (String) -> Unit,
    onRecurringChange: (Boolean) -> Unit,
    onRequestSubmit: () -> Unit,
    onRefreshRates: () -> Unit,
    onLoadNextPage: () -> Unit,
    onToggleMonthlyView: () -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current

    // Add expense bottom sheet
    if (state.showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = onHideAddSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = GlassTheme.BgMid,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            GlassAddExpenseSheet(
                state = state,
                onAmountChange = onAmountChange,
                onCurrencyChange = onCurrencyChange,
                onCategoryChange = onCategoryChange,
                onPaymentMethodChange = onPaymentMethodChange,
                onDateChange = onDateChange,
                onNoteChange = onNoteChange,
                onSubCategoryChange = onSubCategoryChange,
                onRecurringChange = onRecurringChange,
                onRequestSubmit = onRequestSubmit,
                onRefreshRates = onRefreshRates,
            )
        }
    }

    // ── Monthly data ──────────────────────────────────────────────────────────
    val monthStart = remember { currentMonthStartMillis() }
    val monthEntries = remember(state.entries) {
        state.entries.filter { (it.date?.toDate()?.time ?: 0L) >= monthStart }
    }
    // TOGGLE: use monthEntries or all entries based on showMonthlyView
    val activeEntries = if (state.showMonthlyView) monthEntries else state.entries
    val monthTotal = activeEntries.sumOf { it.amount }
    val committed = activeEntries.filter { it.isRecurring }.sumOf { it.amount }
    val discretionary = activeEntries.filter { !it.isRecurring }.sumOf { it.amount }

    val categoryTotals = remember(activeEntries) {
        activeEntries.groupBy { it.category }
            .mapValues { (_, v) -> v.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
    }

    // Zombie subscriptions always from monthEntries (current month makes more sense)
    val zombies = remember(monthEntries) {
        monthEntries
            .filter { it.isRecurring && it.paymentMethod.contains("Auto", ignoreCase = true) }
            .distinctBy { it.category + it.note + it.subCategory }
    }

    val displayEntries = remember(state.entries, state.selectedCategoryFilter) {
        if (state.selectedCategoryFilter == "All") state.entries
        else state.entries.filter { it.category.equals(state.selectedCategoryFilter, ignoreCase = true) }
    }

    val listState = rememberLazyListState()
    var currentPage by remember { mutableIntStateOf(0) }
    val pageSize = 10
    LaunchedEffect(displayEntries) { currentPage = 0 }
    val totalPages = max(1, (displayEntries.size + pageSize - 1) / pageSize)
    val pagedEntries = remember(displayEntries, currentPage) {
        displayEntries
            .sortedByDescending { it.date?.seconds ?: 0L }
            .drop(currentPage * pageSize)
            .take(pageSize)
    }

    val trendData = remember(state.entries) { computeMonthlyTrend(state.entries) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0),
    ) { pv ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(GlassTheme.BgStart, GlassTheme.BgMid, GlassTheme.BgEnd)
                    )
                )
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            GlassTheme.Orange.copy(alpha = 0.16f),
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
                            GlassTheme.OrangeLight.copy(alpha = 0.10f),
                            Color.Transparent,
                        ),
                        center = Offset(size.width * 0.18f, size.height * 0.70f),
                        radius = 220.dp.toPx(),
                    ),
                    center = Offset(size.width * 0.18f, size.height * 0.70f),
                    radius = 220.dp.toPx(),
                )
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(pv),
                contentPadding = PaddingValues(bottom = 88.dp),
            ) {
                // ── Hero header ──────────────────────────────────────────────
                item {
                    GlassExpenseHeader(
                        monthTotal = monthTotal,
                        committed = committed,
                        discretionary = discretionary,
                        entryCount = activeEntries.size,
                        showMonthlyView = state.showMonthlyView,
                        onToggleView = onToggleMonthlyView,
                        onAddClick = onShowAddSheet,
                    )
                }

                // ── Discretionary warning (only in monthly view) ──────────────
                if (!state.warningBannerDismissed && state.showMonthlyView && monthTotal > 0) {
                    val ratio = discretionary / monthTotal
                    if (ratio > 0.40) {
                        item {
                            GlassWarningBanner(
                                percent = (ratio * 100).roundToInt(),
                                onDismiss = onDismissWarning,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                // ── Donut chart ───────────────────────────────────────────────
                if (categoryTotals.isNotEmpty()) {
                    item {
                        GlassDonutCard(
                            data = categoryTotals,
                            selectedCategory = state.selectedChartCategory,
                            onSegmentTap = onChartCategorySelect,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }

                // ── Category filter bar ───────────────────────────────────────
                item {
                    GlassCategoryFilterBar(
                        categories = listOf("All") + ExpenseViewModel.CATEGORIES,
                        selected = state.selectedCategoryFilter,
                        onSelect = onCategoryFilterChange,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                // ── Zombie subscriptions ──────────────────────────────────────
                if (zombies.isNotEmpty()) {
                    item {
                        GlassZombieSection(
                            entries = zombies,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                }

                // ── Shimmer loading ───────────────────────────────────────────
                if (state.isLoading) {
                    items(5) {
                        GlassShimmerCard(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    return@LazyColumn
                }

                // ── Empty state ───────────────────────────────────────────────
                if (displayEntries.isEmpty()) {
                    item {
                        GlassEmptyState(
                            category = state.selectedCategoryFilter,
                            onAddClick = onShowAddSheet,
                        )
                    }
                    return@LazyColumn
                }

                // ── History table ─────────────────────────────────────────────
                item {
                    ExpenseHistoryTable(
                        entries = pagedEntries,
                        onDelete = onDeleteEntry,
                    )
                }

                // ── Pagination bar ────────────────────────────────────────────
                item {
                    ExpenseHistoryPaginationBar(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        hasMore = state.hasMore,
                        isLoadingMore = state.isLoadingMore,
                        onPreviousPage = { currentPage-- },
                        onNextPage = {
                            currentPage++
                            if (currentPage >= totalPages - 1 && state.hasMore && !state.isLoadingMore) {
                                onLoadNextPage()
                            }
                        },
                    )
                }

                // ── Monthly trend ─────────────────────────────────────────────
                item {
                    GlassMonthlyTrend(
                        trendData = trendData,
                        expanded = state.showTrendChart,
                        onToggle = onToggleTrendChart,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            // Floating Bottom Nav
            FinPilotBottomNavBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentTab = NavTab.EXPENSE,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToIncome = onNavigateToIncome,
                onNavigateToExpense = {},
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    }
}

// ── Monthly / All-Time Toggle ─────────────────────────────────────────────────

@Composable
private fun MonthlyAllTimeToggle(
    showMonthlyView: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GlassTheme.GlassSurface)
            .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(20.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(true to "This Month", false to "All Time").forEach { (isMonthly, label) ->
            val active = showMonthlyView == isMonthly
            val bg by animateColorAsState(
                targetValue = if (active) GlassTheme.Orange else Color.Transparent,
                animationSpec = tween(200),
                label = "expense_toggle_bg_$label",
            )
            val tc by animateColorAsState(
                targetValue = if (active) Color.White else GlassTheme.TextSecondary,
                animationSpec = tween(200),
                label = "expense_toggle_tc_$label",
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bg)
                    .clickable(enabled = !active) { onToggle() }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tc,
                )
            }
        }
    }
}

// ── Hero header ───────────────────────────────────────────────────────────────

@Composable
private fun GlassExpenseHeader(
    monthTotal: Double,
    committed: Double,
    discretionary: Double,
    entryCount: Int,
    showMonthlyView: Boolean,
    onToggleView: () -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        DynamicHeaderBackground(
            patternType = "expense",
            modifier = Modifier.matchParentSize().clip(wavyBottomShape())
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "My Expenses",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTheme.TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
                IconButton(onClick = onAddClick) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add Expense",
                        tint = GlassTheme.TextPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // Dynamic label reacts to toggle state
                Text(
                    text = if (showMonthlyView) "THIS MONTH'S EXPENSES" else "ALL TIME EXPENSES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 2.sp,
                    color = GlassTheme.TextHint,
                )
                Text(
                    formatLKRFull(monthTotal),
                    fontSize = 44.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTheme.TextPrimary,
                    letterSpacing = (-1.5).sp,
                )

                // Badge row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassHeroBadge("${formatLKRShort(committed)} committed")
                    GlassHeroBadge("${formatLKRShort(discretionary)} discretionary")
                }
                GlassHeroBadge("$entryCount entries")

                Spacer(modifier = Modifier.height(2.dp))

                // Toggle pill
                MonthlyAllTimeToggle(
                    showMonthlyView = showMonthlyView,
                    onToggle = onToggleView,
                )
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun GlassHeroBadge(text: String) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(GlassTheme.GlassSurface)
            .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = GlassTheme.TextSecondary)
    }
}

// ── Warning banner ────────────────────────────────────────────────────────────

@Composable
private fun GlassWarningBanner(percent: Int, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassTheme.AmberDim)
            .border(1.dp, GlassTheme.AmberBorder, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Warning, null, tint = GlassTheme.Amber, modifier = Modifier.size(18.dp))
        Text(
            "Discretionary spend is $percent% of income — consider cutting back.",
            fontSize = 12.sp,
            color = Color(0xFF92400E),
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Rounded.Close,
            contentDescription = "Dismiss",
            tint = GlassTheme.Amber.copy(alpha = 0.7f),
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onDismiss),
        )
    }
}

// ── Donut chart card ──────────────────────────────────────────────────────────

@Composable
private fun GlassDonutCard(
    data: List<Map.Entry<String, Double>>,
    selectedCategory: String?,
    onSegmentTap: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val total = data.sumOf { it.value }.coerceAtLeast(1.0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassTheme.GlassBg)
            .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            "SPEND BY CATEGORY",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = GlassTheme.TextHint,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Donut
            Box(modifier = Modifier.size(150.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    val strokeW = 28.dp.toPx()
                    val r = (size.minDimension - strokeW) / 2f
                    val cx = size.width / 2f; val cy = size.height / 2f
                    var startAngle = -90f

                    // Background ring
                    drawCircle(
                        color = GlassTheme.GlassBorder,
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(strokeW),
                    )

                    data.forEach { (cat, amt) ->
                        val sweep = (amt / total * 360f).toFloat()
                        val isSelected = selectedCategory == cat
                        val alpha = if (selectedCategory == null || isSelected) 1f else 0.25f
                        val extraStroke = if (isSelected) 8.dp.toPx() else 0f

                        drawArc(
                            color = GlassTheme.categoryColor(cat).copy(alpha = alpha),
                            startAngle = startAngle + 1.5f,
                            sweepAngle = sweep - 3f,
                            useCenter = false,
                            topLeft = Offset(cx - r - extraStroke / 2, cy - r - extraStroke / 2),
                            size = Size(r * 2 + extraStroke, r * 2 + extraStroke),
                            style = Stroke(strokeW + extraStroke, cap = StrokeCap.Round),
                        )
                        startAngle += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${data.size}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (selectedCategory != null) GlassTheme.categoryColor(selectedCategory)
                        else GlassTheme.OrangeLight,
                    )
                    Text(
                        selectedCategory ?: "categories",
                        fontSize = 10.sp,
                        color = GlassTheme.TextHint,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Legend
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.forEach { (cat, amt) ->
                    val pct = (amt / total * 100).roundToInt()
                    val isSelected = selectedCategory == cat
                    val catColor = GlassTheme.categoryColor(cat)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) catColor.copy(alpha = 0.12f) else Color.Transparent)
                            .clickable { onSegmentTap(if (isSelected) null else cat) }
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(catColor),
                        )
                        Text(
                            cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) catColor else GlassTheme.TextSecondary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "$pct%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GlassTheme.TextHint,
                        )
                    }
                }
            }
        }

        Text(
            "Tap legend to filter",
            fontSize = 10.sp,
            color = GlassTheme.TextHint,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

// ── Category filter bar ───────────────────────────────────────────────────────

@Composable
private fun GlassCategoryFilterBar(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        categories.forEach { cat ->
            val isActive = selected == cat
            val bg by animateColorAsState(
                if (isActive) GlassTheme.Orange else GlassTheme.GlassBg,
                tween(180), label = "filter_$cat",
            )
            val textColor by animateColorAsState(
                if (isActive) Color.White else GlassTheme.TextSecondary,
                tween(180), label = "filter_text_$cat",
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .border(
                        1.dp,
                        if (isActive) Color.Transparent else GlassTheme.GlassBorder,
                        RoundedCornerShape(20.dp),
                    )
                    .clickable { onSelect(cat) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(cat, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
            }
        }
    }
}

// ── Date sticky header ────────────────────────────────────────────────────────

@Composable
private fun GlassDateHeader(label: String) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(GlassTheme.BgStart, Color.Transparent)
                )
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        Text(
            label.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = GlassTheme.TextHint,
        )
    }
}

// ── Swipeable row ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlassSwipeableRow(
    entry: ExpenseEntry,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart   -> { onDelete(); true }
                SwipeToDismissBoxValue.StartToEnd   -> { onDuplicate(); false }
                else -> false
            }
        },
        positionalThreshold = { it * 0.40f },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val isDelete = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val isDup    = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val bgColor  = when {
                isDelete -> Color(0x33FF4D6D)
                isDup    -> Color(0x2234D399)
                else     -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(bgColor)
                    .border(1.dp, if (isDelete) Color(0x59FF4D6D) else if (isDup) Color(0x5934D399) else Color.Transparent, RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isDelete) Alignment.CenterEnd else Alignment.CenterStart,
            ) {
                if (isDelete) {
                    Icon(Icons.Default.Delete, null, tint = GlassTheme.Danger, modifier = Modifier.size(20.dp))
                } else if (isDup) {
                    Icon(Icons.Rounded.ContentCopy, null, tint = GlassTheme.Success, modifier = Modifier.size(20.dp))
                }
            }
        },
        modifier = modifier,
    ) {
        GlassExpenseItem(entry)
    }
}

// ── Expense list item ─────────────────────────────────────────────────────────

@Composable
private fun GlassExpenseItem(entry: ExpenseEntry, modifier: Modifier = Modifier) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val catColor = GlassTheme.categoryColor(entry.category)
    val isAutoDebit = entry.paymentMethod.contains("Auto", ignoreCase = true) || entry.isRecurring

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassTheme.GlassBg)
            .border(1.dp, GlassTheme.GlassBorderLight, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(catColor.copy(alpha = 0.15f))
                .border(1.dp, catColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(categoryIcon(entry.category), null, tint = catColor, modifier = Modifier.size(20.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    entry.subCategory?.takeIf { it.isNotBlank() } ?: entry.category,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTheme.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isAutoDebit) {
                    Icon(
                        Icons.Rounded.Autorenew,
                        null,
                        tint = GlassTheme.OrangeLight.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
            entry.note?.takeIf { it.isNotBlank() }?.let {
                Text(it, fontSize = 11.sp, color = GlassTheme.TextHint, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                formatLKRFull(entry.amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTheme.Danger,
            )
            GlassPaymentChip(method = entry.paymentMethod)
        }
    }
}

@Composable
private fun GlassPaymentChip(method: String) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val isAuto  = method.contains("Auto", ignoreCase = true)
    val isCard  = method.equals("Card", ignoreCase = true)
    val isCash  = method.equals("Cash", ignoreCase = true)
    val chipColor = when {
        isCard -> GlassTheme.CatTransport
        isCash -> GlassTheme.Success
        isAuto -> GlassTheme.Orange
        else   -> GlassTheme.TextHint
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(chipColor.copy(alpha = 0.12f))
            .border(0.5.dp, chipColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(paymentMethodIcon(method), null, tint = chipColor, modifier = Modifier.size(10.dp))
        Text(method, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = chipColor)
    }
}

// ── Zombie subscriptions ──────────────────────────────────────────────────────

@Composable
private fun GlassZombieSection(entries: List<ExpenseEntry>, modifier: Modifier = Modifier) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    var expanded by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassTheme.AmberDim)
            .border(1.dp, GlassTheme.AmberBorder, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("🧟", fontSize = 16.sp)
                Text(
                    "Zombie Subscriptions",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E),
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(GlassTheme.Amber)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        "${entries.size}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1A0808),
                    )
                }
            }
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                null,
                tint = GlassTheme.Amber,
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x14F59E0B))
                            .border(1.dp, Color(0x33F59E0B), RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(categoryIcon(entry.category), null, tint = GlassTheme.Amber, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                entry.subCategory?.takeIf { it.isNotBlank() } ?: entry.category,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF92400E),
                            )
                            Text(
                                "${formatLKRFull(entry.amount)}/mo",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0x26F59E0B))
                                .border(1.dp, Color(0x66F59E0B), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text("Cancel?", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GlassTheme.Amber)
                        }
                    }
                }
            }
        }
    }
}

// ── Monthly trend ─────────────────────────────────────────────────────────────

@Composable
private fun GlassMonthlyTrend(
    trendData: List<MonthTrend>,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassTheme.GlassBg)
            .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "MONTHLY TREND",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.5.sp,
                color = GlassTheme.TextHint,
            )
            Icon(
                if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                null,
                tint = GlassTheme.TextHint,
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            GlassBarChart(trendData = trendData)
        }
    }
}

@Composable
private fun GlassBarChart(trendData: List<MonthTrend>) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    if (trendData.isEmpty()) return
    val maxVal = trendData.maxOf { it.amount }.coerceAtLeast(1f)
    val highestIdx = trendData.indexOfFirst { it.amount == trendData.maxOf { m -> m.amount } }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(modifier = Modifier.fillMaxWidth().height(130.dp)) {
            val count = trendData.size
            val spacing = 8.dp.toPx()
            val barW = (size.width - spacing * (count + 1)) / count
            val chartH = size.height - 16.dp.toPx()

            trendData.forEachIndexed { i, item ->
                val barH = (item.amount / maxVal) * chartH
                val left = spacing + i * (barW + spacing)
                val top = chartH - barH
                val isHighest = i == highestIdx

                if (isHighest) {
                    drawRoundRect(
                        color = Color(0x33FF6B00),
                        topLeft = Offset(left - 4, top - 4),
                        size = Size(barW + 8, barH + 8),
                        cornerRadius = CornerRadius(8.dp.toPx()),
                    )
                }

                drawRoundRect(
                    color = if (isHighest) GlassTheme.Orange else GlassTheme.Orange.copy(alpha = 0.35f),
                    topLeft = Offset(left, top),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(6.dp.toPx()),
                )

                if (isHighest) {
                    drawCircle(
                        color = GlassTheme.OrangeLight,
                        radius = 4.dp.toPx(),
                        center = Offset(left + barW / 2, top - 10.dp.toPx()),
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            trendData.forEach { item ->
                val isHighest = item.amount == trendData.maxOf { it.amount }
                Text(
                    item.label,
                    fontSize = 10.sp,
                    fontWeight = if (isHighest) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHighest) GlassTheme.OrangeLight else GlassTheme.TextHint,
                )
            }
        }

        val peak = trendData.maxByOrNull { it.amount }
        if (peak != null && peak.amount > 0f) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clip(RoundedCornerShape(8.dp))
                    .background(GlassTheme.OrangeDim)
                    .border(1.dp, Color(0x40FF6B00), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp),
            ) {
                Text(
                    "Peak: ${peak.label} · ${formatLKRFull(peak.amount.toDouble())}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTheme.OrangeLight,
                )
            }
        }
    }
}

// ── Shimmer ───────────────────────────────────────────────────────────────────

@Composable
private fun GlassShimmerCard(modifier: Modifier = Modifier) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimX by transition.animateFloat(
        -1f, 2f,
        infiniteRepeatable(tween(1200, easing = FastOutLinearInEasing), RepeatMode.Restart),
        label = "sx",
    )
    val brush = Brush.horizontalGradient(
        listOf(Color(0x1AFFFFFF), Color(0x33FFFFFF), Color(0x1AFFFFFF)),
        startX = shimX * 400,
        endX = shimX * 400 + 400,
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(GlassTheme.GlassBg)
            .border(1.dp, GlassTheme.GlassBorderLight, RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(brush))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.height(13.dp).fillMaxWidth(0.5f).clip(RoundedCornerShape(4.dp)).background(brush))
            Box(modifier = Modifier.height(10.dp).fillMaxWidth(0.3f).clip(RoundedCornerShape(4.dp)).background(brush))
        }
        Box(modifier = Modifier.height(13.dp).width(68.dp).clip(RoundedCornerShape(4.dp)).background(brush))
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun GlassEmptyState(category: String, onAddClick: () -> Unit) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            when (category) {
                "All"           -> "🧾"
                "Food"          -> "🍔"
                "Transport"     -> "🚗"
                "Housing"       -> "🏠"
                "Subscriptions" -> "📺"
                "Entertainment" -> "🎬"
                "Health"        -> "💊"
                else            -> "📂"
            },
            fontSize = 52.sp,
        )
        Text(
            if (category == "All") "No expenses yet" else "No $category expenses",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = GlassTheme.TextPrimary,
        )
        Text(
            "Tap the button below to track your first expense",
            fontSize = 13.sp,
            color = GlassTheme.TextHint,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(GlassTheme.Orange)
                .clickable(onClick = onAddClick)
                .padding(horizontal = 28.dp, vertical = 14.dp),
        ) {
            Text("Add Expense", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

// ── Expense history table ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpenseHistoryTable(
    entries: List<ExpenseEntry>,
    onDelete: (ExpenseEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    if (entries.isEmpty()) return
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = GlassTheme.GlassBg),
        border = BorderStroke(1.dp, GlassTheme.GlassBorderLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            entries.forEachIndexed { index, entry ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) { onDelete(entry); true }
                        else false
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
                                    tint = GlassTheme.Danger,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    },
                ) {
                    ExpenseTableRow(entry = entry)
                }
                if (index < entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 0.5.dp,
                        color = GlassTheme.GlassBorder,
                    )
                }
            }
        }
    }
}

@Composable
private fun ExpenseTableRow(entry: ExpenseEntry, modifier: Modifier = Modifier) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
    val isAutoDebit = entry.paymentMethod.contains("Auto", ignoreCase = true) || entry.isRecurring
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(GlassTheme.GlassBg)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    entry.subCategory?.takeIf { it.isNotBlank() } ?: entry.category,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTheme.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isAutoDebit) {
                    Icon(
                        Icons.Rounded.Autorenew,
                        contentDescription = null,
                        tint = GlassTheme.OrangeLight.copy(alpha = 0.8f),
                        modifier = Modifier.size(13.dp),
                    )
                }
            }
            Text(
                entry.note?.takeIf { it.isNotBlank() } ?: entry.category,
                fontSize = 11.sp,
                color = GlassTheme.TextHint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            entry.date?.toDate()?.let { dateFormat.format(it) } ?: "",
            fontSize = 11.sp,
            color = GlassTheme.TextHint,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                "-${formatLKRFull(entry.amount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTheme.Danger,
            )
            Text(
                entry.paymentMethod,
                fontSize = 10.sp,
                color = GlassTheme.TextHint,
            )
        }
    }
}

@Composable
private fun ExpenseHistoryPaginationBar(
    currentPage: Int,
    totalPages: Int,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
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
                .background(if (canGoPrev) GlassTheme.GlassSurface else Color.Transparent)
                .border(
                    0.8.dp,
                    if (canGoPrev) GlassTheme.GlassBorder else Color.Transparent,
                    CircleShape,
                )
                .clickable(enabled = canGoPrev) { onPreviousPage() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "Previous page",
                tint = if (canGoPrev) GlassTheme.TextPrimary else Color.Transparent,
                modifier = Modifier.size(18.dp),
            )
        }

        if (isLoadingMore) {
            CircularProgressIndicator(
                color = GlassTheme.Orange,
                strokeWidth = 2.dp,
                modifier = Modifier.size(18.dp),
            )
        } else {
            Text(
                text = "Page ${currentPage + 1} of ${if (hasMore && currentPage >= totalPages - 1) "${totalPages}+" else "$totalPages"}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassTheme.TextHint,
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (canGoNext && !isLoadingMore) GlassTheme.GlassSurface else Color.Transparent)
                .border(
                    0.8.dp,
                    if (canGoNext && !isLoadingMore) GlassTheme.GlassBorder else Color.Transparent,
                    CircleShape,
                )
                .clickable(enabled = canGoNext && !isLoadingMore) { onNextPage() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = "Next page",
                tint = if (canGoNext && !isLoadingMore) GlassTheme.TextPrimary else Color.Transparent,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

// ── Add Expense bottom sheet ──────────────────────────────────────────────────

private data class QuickPreset(val label: String, val emoji: String, val category: String, val merchant: String)

private val quickPresets = listOf(
    QuickPreset("PickMe", "🛵", "Transport", "PickMe"),
    QuickPreset("UberEats", "🍔", "Food", "UberEats"),
    QuickPreset("Gym", "💪", "Health", "Gym"),
    QuickPreset("Keells", "🛒", "Food", "Keells"),
)

@Composable
private fun GlassAddExpenseSheet(
    state: ExpenseViewModel.ExpenseUiState,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onNoteChange: (String) -> Unit,
    onSubCategoryChange: (String) -> Unit,
    onRecurringChange: (Boolean) -> Unit,
    onRequestSubmit: () -> Unit,
    onRefreshRates: () -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val rateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val showExchange = state.currency != "LKR"

    val fieldBg = GlassTheme.GlassBg
    val textPrimary = GlassTheme.TextPrimary
    val textSecondary = GlassTheme.TextSecondary

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = fieldBg,
        focusedContainerColor = fieldBg,
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = GlassTheme.Orange,
        unfocusedTextColor = textPrimary,
        focusedTextColor = textPrimary,
        unfocusedLabelColor = textSecondary,
        focusedLabelColor = GlassTheme.Orange,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .align(Alignment.CenterHorizontally)
                .background(textSecondary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
        )

        Text(
            "Add Expense",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textPrimary,
        )

        // Error
        state.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                    .padding(12.dp),
            )
        }

        // Currency + Amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            var currencyExpanded by remember { mutableStateOf(false) }
            Box {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(fieldBg)
                        .clickable { currencyExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 18.dp),
                ) {
                    Text(state.currency, color = GlassTheme.Orange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                DropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false },
                    containerColor = GlassTheme.BgMid,
                ) {
                    ExpenseViewModel.CURRENCIES.forEach { cur ->
                        DropdownMenuItem(
                            text = { Text(cur, color = textPrimary) },
                            onClick = { onCurrencyChange(cur); currencyExpanded = false },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                label = { Text("Amount") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = fieldColors,
            )
        }

        // Exchange rate
        AnimatedVisibility(
            visible = showExchange,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(fieldBg)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "≈ LKR ${"%.2f".format(state.amountLkrPreview)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTheme.Orange,
                )
                val rateText = state.exchangeRateLastUpdatedMillis
                    ?.let { rateFormat.format(Date(it)) } ?: "unknown"
                Text(
                    "1 ${state.currency} = LKR ${state.exchangeRate} · $rateText" +
                            if (state.exchangeRateIsStale) " · stale" else "",
                    fontSize = 11.sp,
                    color = textSecondary,
                )
                TextButton(onClick = onRefreshRates, enabled = !state.isRefreshingRates) {
                    if (state.isRefreshingRates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = GlassTheme.Orange,
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                    Text("Refresh rates", fontSize = 12.sp, color = GlassTheme.Orange)
                }
            }
        }

        // Date
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(fieldBg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Date", style = MaterialTheme.typography.labelMedium, color = textSecondary)
                Text(
                    dateFormat.format(Date(state.dateMillis)),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary,
                )
            }
            TextButton(
                onClick = {
                    val cal = Calendar.getInstance().also { it.timeInMillis = state.dateMillis }
                    DatePickerDialog(context, { _, y, m, d ->
                        onDateChange(Calendar.getInstance().also { c ->
                            c.set(y, m, d, 0, 0, 0); c.set(Calendar.MILLISECOND, 0)
                        }.timeInMillis)
                    }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                },
            ) {
                Text("Change", color = GlassTheme.Orange)
            }
        }

        // Category
        Text(
            "CATEGORY",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = textSecondary,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ExpenseViewModel.CATEGORIES.forEach { name ->
                val isSelected = state.category == name
                val bg by animateColorAsState(
                    if (isSelected) GlassTheme.categoryColor(name) else fieldBg,
                    tween(220), label = "cat_bg_$name",
                )
                val contentColor by animateColorAsState(
                    if (isSelected) Color.White else textSecondary,
                    tween(220), label = "cat_fg_$name",
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(bg)
                        .clickable { onCategoryChange(name) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Text(
                        name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = contentColor,
                    )
                }
            }
        }

        // Payment method
        Text(
            "PAYMENT METHOD",
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = textSecondary,
        )
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ExpenseViewModel.PAYMENT_METHODS.forEach { name ->
                val isSelected = state.paymentMethod == name
                val bg by animateColorAsState(
                    if (isSelected) GlassTheme.Orange else fieldBg,
                    tween(220), label = "pay_bg_$name",
                )
                val contentColor by animateColorAsState(
                    if (isSelected) Color.White else textSecondary,
                    tween(220), label = "pay_fg_$name",
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(bg)
                        .clickable { onPaymentMethodChange(name) }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                ) {
                    Text(
                        name,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = contentColor,
                    )
                }
            }
        }

        // Merchant
        OutlinedTextField(
            value = state.subCategory,
            onValueChange = onSubCategoryChange,
            label = { Text("Merchant / Sub-category") },
            placeholder = { Text("e.g. Keells, PickMe, Netflix") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = fieldColors,
        )

        // Note
        OutlinedTextField(
            value = state.note,
            onValueChange = onNoteChange,
            label = { Text("Note (optional)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = fieldColors,
        )

        // Recurring
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(fieldBg)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    "Recurring / Auto-debit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textPrimary,
                )
                Text(
                    "Marks this as a recurring charge",
                    fontSize = 11.sp,
                    color = textSecondary,
                )
            }
            Switch(
                checked = state.isRecurring,
                onCheckedChange = onRecurringChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = GlassTheme.Orange,
                    uncheckedThumbColor = textSecondary,
                    uncheckedTrackColor = GlassTheme.GlassBorder,
                ),
            )
        }

        // Save
        Button(
            onClick = onRequestSubmit,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GlassTheme.Orange,
                disabledContainerColor = GlassTheme.Orange.copy(alpha = 0.4f),
            ),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Text("Save Expense", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun SheetLabel(text: String) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color = GlassTheme.TextHint,
    )
}

@Composable
private fun GlassCategoryGrid(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    categories.chunked(4).forEach { row ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            row.forEach { cat ->
                val isSel = selected == cat
                val catColor = GlassTheme.categoryColor(cat)
                val bg by animateColorAsState(
                    if (isSel) catColor else GlassTheme.GlassBg, tween(180), label = "cat_$cat",
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(bg)
                        .border(1.dp, if (isSel) Color.Transparent else GlassTheme.GlassBorder, RoundedCornerShape(14.dp))
                        .clickable { onSelect(cat) }
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        categoryIcon(cat), null,
                        tint = if (isSel) Color.White else catColor,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        cat,
                        fontSize = 10.sp, fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else GlassTheme.TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}
