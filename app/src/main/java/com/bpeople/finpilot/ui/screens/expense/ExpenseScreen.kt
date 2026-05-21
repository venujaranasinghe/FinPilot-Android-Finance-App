package com.bpeople.finpilot.ui.screens.expense

import android.app.DatePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.components.GlassTheme
import java.text.SimpleDateFormat
import java.util.*
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
    onExpenseAdded: (String) -> Unit,
) {
    val state by viewModel.expenseState.collectAsState()
    val pagedHistory = viewModel.pagedHistory.collectAsLazyPagingItems()
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
        pagedHistory = pagedHistory,
        snackbarHostState = snackbarHostState,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToIncome = onNavigateToIncome,
        onNavigateToTransactions = onNavigateToTransactions,
        onNavigateToGoals = onNavigateToGoals,
        onNavigateToProfile = onNavigateToProfile,
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
    )
}

// ── Main content ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseListContent(
    state: ExpenseViewModel.ExpenseUiState,
    pagedHistory: androidx.paging.compose.LazyPagingItems<ExpenseEntry>,
    snackbarHostState: SnackbarHostState,
    onNavigateToDashboard: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
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
) {
    // Popup dialog for adding expense
    if (state.showAddSheet) {
        Dialog(
            onDismissRequest = onHideAddSheet,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = GlassTheme.BgMid),
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
    }

    // Monthly data
    val monthStart = remember { currentMonthStartMillis() }
    val monthEntries = remember(state.entries) {
        state.entries.filter { (it.date?.toDate()?.time ?: 0L) >= monthStart }
    }
    val monthTotal = monthEntries.sumOf { it.amount }
    val committed = monthEntries.filter { it.isRecurring }.sumOf { it.amount }
    val discretionary = monthEntries.filter { !it.isRecurring }.sumOf { it.amount }

    val categoryTotals = remember(monthEntries) {
        monthEntries.groupBy { it.category }
            .mapValues { (_, v) -> v.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
    }

    val zombies = remember(monthEntries) {
        monthEntries
            .filter { it.isRecurring && it.paymentMethod.contains("Auto", ignoreCase = true) }
            .distinctBy { it.category + it.note + it.subCategory }
    }

    val isHistoryLoading = pagedHistory.loadState.refresh is LoadState.Loading

    val trendData = remember(state.entries) { computeMonthlyTrend(state.entries) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddSheet,
                icon = {
                    Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                },
                text = {
                    Text("Add Expense", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                },
                containerColor = GlassTheme.Orange,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp),
            )
        },
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
                        entryCount = monthEntries.size,
                    )
                }

                // ── Discretionary warning ─────────────────────────────────────
                if (!state.warningBannerDismissed && monthTotal > 0) {
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
                if (isHistoryLoading) {
                    items(5) {
                        GlassShimmerCard(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                    return@LazyColumn
                }

                // ── Empty state ───────────────────────────────────────────────
                if (pagedHistory.itemCount == 0) {
                    item {
                        GlassEmptyState(
                            category = state.selectedCategoryFilter,
                            onAddClick = onShowAddSheet,
                        )
                    }
                    return@LazyColumn
                }

                // ── Date-grouped list ─────────────────────────────────────────
                items(
                    count = pagedHistory.itemCount,
                    key = { index -> pagedHistory[index]?.id ?: "expense_placeholder_$index" },
                ) { index ->
                    val entry = pagedHistory[index] ?: return@items
                    GlassSwipeableRow(
                        entry = entry,
                        onDelete = { onDeleteEntry(entry) },
                        onDuplicate = { onDuplicateEntry(entry) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp),
                    )
                }

                if (pagedHistory.loadState.append is LoadState.Loading) {
                    items(2) {
                        GlassShimmerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
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
                currentTab = NavTab.TRANSACTIONS,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToIncome = onNavigateToIncome,
                onNavigateToExpense = {},
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile,
            )
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                // Orange orb top-right
                drawCircle(
                    color = Color(0x2EFF6B00),
                    radius = 220.dp.toPx(),
                    center = Offset(size.width * 0.92f, -60.dp.toPx()),
                )
                // Purple orb left
                drawCircle(
                    color = Color(0x1A534AB7),
                    radius = 150.dp.toPx(),
                    center = Offset(size.width * 0.05f, size.height * 0.7f),
                )
            }
            .padding(top = 52.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                "THIS MONTH'S EXPENSES",
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
        }
    }
}

@Composable
private fun GlassHeroBadge(text: String) {
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
        // Category icon with glow tint
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

        // Text
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

        // Right side
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

                // Bar glow background
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

// ── Add Expense popup ────────────────────────────────────────────────────────

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
    val context = LocalContext.current
    val density = LocalDensity.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val rateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val showExchange = state.currency != "LKR"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "Add Expense",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = GlassTheme.TextPrimary,
        )

        // Amount
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetLabel("Amount")

            OutlinedTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                    color = GlassTheme.TextPrimary,
                ),
                placeholder = {
                    Text(
                        "0.00",
                        style = TextStyle(
                            fontSize = 28.sp, fontWeight = FontWeight.ExtraBold,
                            color = GlassTheme.TextHint,
                        ),
                    )
                },
                leadingIcon = {
                    var expanded by remember { mutableStateOf(false) }
                    var currencyMenuWidthPx by remember { mutableStateOf(0) }
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(GlassTheme.GlassBg)
                                .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(10.dp))
                                .clickable { expanded = true }
                                .onGloballyPositioned { currencyMenuWidthPx = it.size.width }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(state.currency, fontWeight = FontWeight.ExtraBold, color = GlassTheme.OrangeLight, fontSize = 14.sp)
                            Icon(Icons.Rounded.ArrowDropDown, null, tint = GlassTheme.OrangeLight, modifier = Modifier.size(16.dp))
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.width(with(density) { currencyMenuWidthPx.toDp() }),
                            shape = RoundedCornerShape(14.dp),
                            containerColor = GlassTheme.BgMid,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.GlassBorder),
                        ) {
                            ExpenseViewModel.CURRENCIES.forEach { cur ->
                                DropdownMenuItem(
                                    text = { Text(cur, color = GlassTheme.TextPrimary) },
                                    onClick = { onCurrencyChange(cur); expanded = false },
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassTheme.Orange,
                    unfocusedBorderColor = GlassTheme.GlassBorder,
                    focusedContainerColor = GlassTheme.GlassSurface,
                    unfocusedContainerColor = GlassTheme.GlassBg,
                    cursorColor = GlassTheme.Orange,
                ),
            )

            AnimatedVisibility(
                showExchange,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(GlassTheme.OrangeDim)
                        .border(1.dp, Color(0x40FF6B00), RoundedCornerShape(14.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "≈ LKR ${"%.2f".format(state.amountLkrPreview)}",
                        fontSize = 16.sp, fontWeight = FontWeight.Bold, color = GlassTheme.OrangeLight,
                    )
                    val rateText = state.exchangeRateLastUpdatedMillis
                        ?.let { rateFormat.format(Date(it)) } ?: "unknown"
                    Text(
                        "1 ${state.currency} = LKR ${state.exchangeRate} · $rateText" +
                                if (state.exchangeRateIsStale) " · stale" else "",
                        fontSize = 11.sp, color = GlassTheme.TextSecondary,
                    )
                    TextButton(onClick = onRefreshRates, enabled = !state.isRefreshingRates) {
                        if (state.isRefreshingRates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp, color = GlassTheme.OrangeLight,
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text("Refresh rates", fontSize = 12.sp, color = GlassTheme.OrangeLight)
                    }
                }
            }
        }

        // Category dropdown
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SheetLabel("Category")
            var categoryExpanded by remember { mutableStateOf(false) }
            var categoryMenuWidthPx by remember { mutableStateOf(0) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.category,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { categoryMenuWidthPx = it.size.width },
                    label = { Text("Category") },
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            tint = GlassTheme.TextHint,
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = GlassTheme.TextPrimary,
                        disabledBorderColor = GlassTheme.GlassBorder,
                        disabledContainerColor = GlassTheme.GlassBg,
                        disabledLabelColor = GlassTheme.TextHint,
                        disabledTrailingIconColor = GlassTheme.TextHint,
                    ),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { categoryExpanded = true }
                )
                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false },
                    modifier = Modifier.width(with(density) { categoryMenuWidthPx.toDp() }),
                    shape = RoundedCornerShape(14.dp),
                    containerColor = GlassTheme.BgMid,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.GlassBorder),
                ) {
                    ExpenseViewModel.CATEGORIES.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category, color = GlassTheme.TextPrimary) },
                            onClick = {
                                onCategoryChange(category)
                                categoryExpanded = false
                            },
                        )
                    }
                }
            }
        }

        // Merchant
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetLabel("Merchant (optional)")
            OutlinedTextField(
                value = state.subCategory,
                onValueChange = onSubCategoryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g. Keells, PickMe, Netflix", fontSize = 13.sp, color = GlassTheme.TextHint) },
                textStyle = TextStyle(fontSize = 14.sp, color = GlassTheme.TextPrimary),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassTheme.Orange,
                    unfocusedBorderColor = GlassTheme.GlassBorder,
                    focusedContainerColor = GlassTheme.GlassSurface,
                    unfocusedContainerColor = GlassTheme.GlassBg,
                    cursorColor = GlassTheme.Orange,
                ),
            )
        }

        // Note
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetLabel("Note (optional)")
            OutlinedTextField(
                value = state.note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("e.g. Friday lunch with team", fontSize = 13.sp, color = GlassTheme.TextHint) },
                textStyle = TextStyle(fontSize = 14.sp, color = GlassTheme.TextPrimary),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GlassTheme.Orange,
                    unfocusedBorderColor = GlassTheme.GlassBorder,
                    focusedContainerColor = GlassTheme.GlassSurface,
                    unfocusedContainerColor = GlassTheme.GlassBg,
                    cursorColor = GlassTheme.Orange,
                ),
            )
        }

        // Payment method dropdown
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SheetLabel("Payment Method")
            var paymentExpanded by remember { mutableStateOf(false) }
            var paymentMenuWidthPx by remember { mutableStateOf(0) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = state.paymentMethod,
                    onValueChange = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { paymentMenuWidthPx = it.size.width },
                    label = { Text("Payment Method") },
                    readOnly = true,
                    enabled = false,
                    trailingIcon = {
                        Icon(
                            Icons.Rounded.ArrowDropDown,
                            contentDescription = null,
                            tint = GlassTheme.TextHint,
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = GlassTheme.TextPrimary,
                        disabledBorderColor = GlassTheme.GlassBorder,
                        disabledContainerColor = GlassTheme.GlassBg,
                        disabledLabelColor = GlassTheme.TextHint,
                        disabledTrailingIconColor = GlassTheme.TextHint,
                    ),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { paymentExpanded = true }
                )
                DropdownMenu(
                    expanded = paymentExpanded,
                    onDismissRequest = { paymentExpanded = false },
                    modifier = Modifier.width(with(density) { paymentMenuWidthPx.toDp() }),
                    shape = RoundedCornerShape(14.dp),
                    containerColor = GlassTheme.BgMid,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.GlassBorder),
                ) {
                    ExpenseViewModel.PAYMENT_METHODS.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method, color = GlassTheme.TextPrimary) },
                            onClick = {
                                onPaymentMethodChange(method)
                                paymentExpanded = false
                            },
                        )
                    }
                }
            }
        }

        // Date
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetLabel("Date")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(GlassTheme.GlassBg)
                    .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(14.dp))
                    .clickable {
                        val cal = Calendar.getInstance().also { it.timeInMillis = state.dateMillis }
                        DatePickerDialog(context, { _, y, m, d ->
                            onDateChange(Calendar.getInstance().also { c ->
                                c.set(y, m, d, 0, 0, 0); c.set(Calendar.MILLISECOND, 0)
                            }.timeInMillis)
                        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Rounded.CalendarToday, null, tint = GlassTheme.OrangeLight, modifier = Modifier.size(20.dp))
                Text(
                    dateFormat.format(Date(state.dateMillis)),
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = GlassTheme.TextPrimary,
                )
            }
        }

        // Recurring toggle
        GlassRecurringRow(isRecurring = state.isRecurring, onRecurringChange = onRecurringChange)

        // Error
        state.errorMessage?.let {
            Text(it, fontSize = 12.sp, color = GlassTheme.Danger)
        }

        // Save
        GlassSaveButton(isLoading = state.isLoading, onClick = onRequestSubmit)
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(
        text.uppercase(),
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
        color = GlassTheme.TextHint,
    )
}

@Composable
private fun GlassRecurringRow(
    isRecurring: Boolean,
    onRecurringChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassTheme.GlassBg)
            .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Rounded.Autorenew,
                contentDescription = null,
                tint = GlassTheme.OrangeLight,
                modifier = Modifier.size(18.dp),
            )
            Column {
                Text(
                    text = "Recurring expense",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTheme.TextPrimary,
                )
                Text(
                    text = "Track fixed monthly costs",
                    fontSize = 11.sp,
                    color = GlassTheme.TextHint,
                )
            }
        }

        Switch(
            checked = isRecurring,
            onCheckedChange = onRecurringChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = GlassTheme.Orange,
                uncheckedThumbColor = GlassTheme.TextHint,
                uncheckedTrackColor = GlassTheme.GlassBorder,
            ),
        )
    }
}

@Composable
private fun GlassSaveButton(
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GlassTheme.Orange,
            disabledContainerColor = GlassTheme.Orange.copy(alpha = 0.6f),
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.White,
            )
        } else {
            Text(
                text = "Save Expense",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
            )
        }
    }
}