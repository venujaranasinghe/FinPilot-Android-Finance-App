package com.bpeople.finpilot.ui.screens.expense

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Fastfood
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.LocalGroceryStore
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.Money
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Subscriptions
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ── Colour constants ──────────────────────────────────────────────────────────

private val Orange = Color(0xFFFF6B00)
private val OrangeLight = Color(0xFFFFF3E0)
private val AmberWarning = Color(0xFFF59E0B)
private val AmberWarningBg = Color(0xFFFFFBEB)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val BgGray = Color(0xFFF9FAFB)
private val TextPrimary = Color(0xFF1F2937)
private val TextSecondary = Color(0xFF6B7280)
private val Divider = Color(0xFFE5E7EB)

// Category colours (Orange primary, rest muted palette)
private val categoryColors = mapOf(
    "Food" to Color(0xFFFF6B00),
    "Transport" to Color(0xFF3B82F6),
    "Housing" to Color(0xFF8B5CF6),
    "Subscriptions" to Color(0xFF10B981),
    "Entertainment" to Color(0xFFF59E0B),
    "Health" to Color(0xFFEF4444),
    "Other" to Color(0xFF9CA3AF),
)

private fun categoryColor(cat: String): Color =
    categoryColors[cat] ?: Color(0xFF9CA3AF)

private fun categoryIcon(cat: String): ImageVector = when (cat) {
    "Food" -> Icons.Rounded.Restaurant
    "Transport" -> Icons.Rounded.DirectionsCar
    "Housing" -> Icons.Rounded.Home
    "Subscriptions" -> Icons.Rounded.Subscriptions
    "Entertainment" -> Icons.Rounded.Movie
    "Health" -> Icons.Rounded.LocalHospital
    else -> Icons.Rounded.MoreHoriz
}

private fun paymentMethodIcon(method: String): ImageVector = when {
    method.equals("Card", ignoreCase = true) -> Icons.Rounded.CreditCard
    method.equals("Cash", ignoreCase = true) -> Icons.Rounded.Money
    method.contains("Auto", ignoreCase = true) -> Icons.Rounded.Autorenew
    else -> Icons.Rounded.CreditCard
}

private fun formatLKRFull(amount: Double) = "LKR %,.0f".format(amount)
private fun formatLKR(amount: Double): String = when {
    amount >= 1_000_000 -> "LKR %.1fM".format(amount / 1_000_000)
    amount >= 1_000 -> "LKR %.0fK".format(amount / 1_000)
    else -> "LKR ${amount.roundToInt()}"
}

private fun dateLabel(millis: Long): String {
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR, -1) }
    val date = Calendar.getInstance().also { it.timeInMillis = millis }
    return when {
        isSameDay(date, today) -> "Today"
        isSameDay(date, yesterday) -> "Yesterday"
        else -> SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(millis))
    }
}

private fun isSameDay(a: Calendar, b: Calendar): Boolean =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

private fun currentMonthStartMillis(): Long = Calendar.getInstance().apply {
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

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
    val snackbarHostState = remember { SnackbarHostState() }

    // Submitted → close sheet, show snackbar
    LaunchedEffect(state.isSubmitted) {
        if (!state.isSubmitted) return@LaunchedEffect
        viewModel.consumeSubmitted()
        snackbarHostState.showSnackbar("Expense saved!")
    }

    // Insight message
    LaunchedEffect(state.insightMessage) {
        val insight = state.insightMessage ?: return@LaunchedEffect
        viewModel.consumeInsight()
        onExpenseAdded(insight)
    }

    // Pending delete undo
    LaunchedEffect(state.pendingDeleteEntry) {
        val entry = state.pendingDeleteEntry ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "${entry.category} expense deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.undoDelete()
            SnackbarResult.Dismissed -> viewModel.consumePendingDelete()
        }
    }

    // Errors
    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
    }

    ExpenseListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToIncome = onNavigateToIncome,
        onNavigateToExpense = {},
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
        onConfirmExchangeRate = viewModel::confirmExchangeRate,
        onDismissRateConfirmation = viewModel::dismissRateConfirmation,
        onRefreshRates = viewModel::refreshExchangeRates,
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
    onNavigateToExpense: () -> Unit,
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
    onConfirmExchangeRate: () -> Unit,
    onDismissRateConfirmation: () -> Unit,
    onRefreshRates: () -> Unit,
) {
    // Add expense bottom sheet
    if (state.showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = onHideAddSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = SurfaceWhite,
        ) {
            AddExpenseFormSheet(
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

    // Compute month data
    val monthStart = remember { currentMonthStartMillis() }
    val monthEntries = remember(state.entries) {
        state.entries.filter { (it.date?.toDate()?.time ?: 0L) >= monthStart }
    }
    val monthTotal = monthEntries.sumOf { it.amount }
    val committedTotal = monthEntries.filter { it.isRecurring }.sumOf { it.amount }
    val discretionaryTotal = monthEntries.filter { !it.isRecurring }.sumOf { it.amount }

    // Category totals for donut chart
    val categoryTotals = remember(monthEntries) {
        monthEntries.groupBy { it.category }
            .mapValues { (_, v) -> v.sumOf { it.amount } }
            .entries
            .sortedByDescending { it.value }
    }

    // Zombie subscriptions: recurring entries in current month
    val zombieSubscriptions = remember(monthEntries) {
        monthEntries
            .filter { it.isRecurring && it.paymentMethod.contains("Auto", ignoreCase = true) }
            .distinctBy { it.category + it.note + it.subCategory }
    }

    // Filter entries for list
    val displayEntries = remember(state.entries, state.selectedCategoryFilter) {
        if (state.selectedCategoryFilter == "All") state.entries
        else state.entries.filter { it.category.equals(state.selectedCategoryFilter, ignoreCase = true) }
    }

    // Group by date for sticky headers
    val groupedByDate = remember(displayEntries) {
        displayEntries
            .sortedByDescending { it.date?.seconds ?: 0L }
            .groupBy { entry ->
                val millis = entry.date?.toDate()?.time ?: 0L
                dateLabel(millis)
            }
            .entries
            .toList()
    }

    // Monthly trend data (last 6 months)
    val trendData = remember(state.entries) { computeMonthlyTrend(state.entries) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onShowAddSheet,
                icon = {
                    Icon(Icons.Rounded.Add, contentDescription = "Add Expense", modifier = Modifier.size(20.dp))
                },
                text = { Text("Add Expense", fontWeight = FontWeight.Bold) },
                containerColor = Orange,
                contentColor = Color.White,
            )
        },
        bottomBar = {
            FinPilotBottomNavBar(
                currentTab = NavTab.TRANSACTIONS,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToIncome = onNavigateToIncome,
                onNavigateToExpense = { /* already here */ },
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile,
            )
        },
        containerColor = BgGray,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { pv ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(pv),
            contentPadding = PaddingValues(bottom = 88.dp),
        ) {
                // ── Header ───────────────────────────────────────────────────
                item {
                    ExpenseHeaderCard(
                        monthTotal = monthTotal,
                        committedTotal = committedTotal,
                        discretionaryTotal = discretionaryTotal,
                        entryCount = monthEntries.size,
                    )
                }

                // ── Discretionary warning banner ─────────────────────────────
                if (!state.warningBannerDismissed && monthTotal > 0) {
                    val discRatio = discretionaryTotal / monthTotal
                    if (discRatio > 0.40) {
                        item {
                            DiscretionaryWarningBanner(
                                percent = (discRatio * 100).roundToInt(),
                                onDismiss = onDismissWarning,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                // ── Donut chart ──────────────────────────────────────────────
                if (categoryTotals.isNotEmpty()) {
                    item {
                        ExpenseCategoryDonutChart(
                            data = categoryTotals,
                            selectedCategory = state.selectedChartCategory,
                            onSegmentTap = onChartCategorySelect,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }

                // ── Category filter bar ───────────────────────────────────────
                item {
                    CategoryFilterBar(
                        categories = listOf("All") + ExpenseViewModel.CATEGORIES,
                        selected = state.selectedCategoryFilter,
                        onSelect = onCategoryFilterChange,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                // ── Zombie subscriptions section ─────────────────────────────
                if (zombieSubscriptions.isNotEmpty()) {
                    item {
                        ZombieSubscriptionsSection(
                            entries = zombieSubscriptions,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                }

                // ── Shimmer skeleton on load ─────────────────────────────────
                if (state.isLoading) {
                    items(5) {
                        ShimmerExpenseCard(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp),
                        )
                    }
                    return@LazyColumn
                }

                // ── Empty state ───────────────────────────────────────────────
                if (displayEntries.isEmpty() && !state.isLoading) {
                    item {
                        EmptyExpenseState(
                            category = state.selectedCategoryFilter,
                            onAddClick = onShowAddSheet,
                        )
                    }
                    return@LazyColumn
                }

                // ── Date-grouped expense list ─────────────────────────────────
                groupedByDate.forEach { (dateLabel, entries) ->
                    stickyHeader(key = "header_$dateLabel") {
                        DateStickyHeader(label = dateLabel)
                    }
                    items(entries, key = { it.id }) { entry ->
                        SwipeableExpenseRow(
                            entry = entry,
                            onDelete = { onDeleteEntry(entry) },
                            onDuplicate = { onDuplicateEntry(entry) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp),
                        )
                    }
                }

                // ── Monthly trend chart (collapsible) ─────────────────────────
                item {
                    MonthlyTrendSection(
                        trendData = trendData,
                        expanded = state.showTrendChart,
                        onToggle = onToggleTrendChart,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
    }
}

// ── Section 1: Header ─────────────────────────────────────────────────────────

@Composable
private fun ExpenseHeaderCard(
    monthTotal: Double,
    committedTotal: Double,
    discretionaryTotal: Double,
    entryCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(Orange, Color(0xFFFF8C00), BgGray)),
            )
            .padding(top = 52.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
    ) {
        // Decorative circles
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = 0.07f), 170.dp.toPx(), Offset(size.width * 0.92f, 0f))
            drawCircle(Color.White.copy(alpha = 0.05f), 110.dp.toPx(), Offset(size.width * 0.04f, size.height * 0.65f))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "This Month's Expenses",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 0.5.sp,
            )

            Text(
                text = formatLKRFull(monthTotal),
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )

            // Committed vs Discretionary pill badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExpenseBadge(
                    text = "${formatLKR(committedTotal)} committed",
                    bgColor = Color.White.copy(alpha = 0.20f),
                    textColor = Color.White,
                )
                ExpenseBadge(
                    text = "${formatLKR(discretionaryTotal)} discretionary",
                    bgColor = Color.White.copy(alpha = 0.20f),
                    textColor = Color.White,
                )
            }

            ExpenseBadge(
                text = "$entryCount entries",
                bgColor = Color.White.copy(alpha = 0.15f),
                textColor = Color.White.copy(alpha = 0.9f),
            )
        }
    }
}

@Composable
private fun ExpenseBadge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

// ── Discretionary warning banner ──────────────────────────────────────────────

@Composable
private fun DiscretionaryWarningBanner(
    percent: Int,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AmberWarningBg)
            .border(1.dp, AmberWarning.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(Icons.Rounded.Warning, null, tint = AmberWarning, modifier = Modifier.size(18.dp))
        Text(
            text = "Discretionary spend is $percent% of income — consider cutting back.",
            fontSize = 12.sp,
            color = Color(0xFF92400E),
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Rounded.Close,
            contentDescription = "Dismiss",
            tint = AmberWarning.copy(alpha = 0.7f),
            modifier = Modifier
                .size(18.dp)
                .clickable(onClick = onDismiss),
        )
    }
}

// ── Section 2: Donut chart ────────────────────────────────────────────────────

@Composable
private fun ExpenseCategoryDonutChart(
    data: List<Map.Entry<String, Double>>,
    selectedCategory: String?,
    onSegmentTap: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = data.sumOf { it.value }.coerceAtLeast(1.0)
    val categoryCount = data.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceWhite)
            .border(0.5.dp, Divider, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Spend by Category", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Donut chart canvas
            Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(160.dp)) {
                    val strokeWidth = 32.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    var startAngle = -90f

                    data.forEachIndexed { index, (cat, amt) ->
                        val sweep = (amt / total * 360f).toFloat()
                        val isSelected = selectedCategory == cat
                        val alpha = if (selectedCategory == null || isSelected) 1f else 0.35f
                        val extraStroke = if (isSelected) 6.dp.toPx() else 0f

                        drawArc(
                            color = categoryColor(cat).copy(alpha = alpha),
                            startAngle = startAngle + 1f,
                            sweepAngle = sweep - 2f,
                            useCenter = false,
                            topLeft = Offset(
                                center.x - radius - (extraStroke / 2),
                                center.y - radius - (extraStroke / 2),
                            ),
                            size = Size(
                                (radius * 2 + extraStroke),
                                (radius * 2 + extraStroke),
                            ),
                            style = Stroke(width = strokeWidth + extraStroke, cap = StrokeCap.Butt),
                        )
                        startAngle += sweep
                    }
                }

                // Center label
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$categoryCount",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (selectedCategory != null) categoryColor(selectedCategory) else Orange,
                    )
                    Text(
                        text = if (selectedCategory != null) selectedCategory else "categories",
                        fontSize = 10.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Legend
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                data.forEach { (cat, amt) ->
                    val pct = (amt / total * 100).roundToInt()
                    val isSelected = selectedCategory == cat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) categoryColor(cat).copy(alpha = 0.08f)
                                else Color.Transparent
                            )
                            .clickable { onSegmentTap(cat) }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(categoryColor(cat)),
                        )
                        Text(
                            cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) categoryColor(cat) else TextPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            "$pct%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary,
                        )
                    }
                }
            }
        }

        // Tap hint
        Text(
            text = "Tap a segment or legend to filter list",
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

// ── Section 3: Category filter bar ───────────────────────────────────────────

@Composable
private fun CategoryFilterBar(
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
            val bgColor by animateColorAsState(
                if (isActive) Orange else SurfaceWhite,
                animationSpec = tween(180),
                label = "chip_bg_$cat",
            )
            val textColor by animateColorAsState(
                if (isActive) Color.White else TextSecondary,
                animationSpec = tween(180),
                label = "chip_text_$cat",
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(bgColor)
                    .border(
                        width = 1.dp,
                        color = if (isActive) Orange else Divider,
                        shape = RoundedCornerShape(20.dp),
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
private fun DateStickyHeader(label: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgGray)
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 0.8.sp,
        )
    }
}

// ── Swipeable expense row ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableExpenseRow(
    entry: ExpenseEntry,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                SwipeToDismissBoxValue.StartToEnd -> { onDuplicate(); false } // reset after duplicate
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
            val isDuplicate = dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd
            val bgColor = when {
                isDelete -> Color(0xFFEF4444)
                isDuplicate -> Color(0xFF10B981)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(horizontal = 20.dp),
                contentAlignment = if (isDelete) Alignment.CenterEnd else Alignment.CenterStart,
            ) {
                if (isDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(22.dp))
                } else if (isDuplicate) {
                    Icon(Icons.Rounded.ContentCopy, null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        },
        modifier = modifier,
    ) {
        ExpenseListItem(entry = entry)
    }
}

// ── Single expense item ───────────────────────────────────────────────────────

@Composable
private fun ExpenseListItem(
    entry: ExpenseEntry,
    modifier: Modifier = Modifier,
) {
    val color = categoryColor(entry.category)
    val isAutoDebit = entry.paymentMethod.contains("Auto", ignoreCase = true) || entry.isRecurring

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(0.5.dp, Divider, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Category icon circle
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(color.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(categoryIcon(entry.category), null, tint = color, modifier = Modifier.size(20.dp))
        }

        // Merchant / note
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = entry.subCategory?.takeIf { it.isNotBlank() } ?: entry.category,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (isAutoDebit) {
                    Icon(
                        Icons.Rounded.Autorenew,
                        contentDescription = "Recurring",
                        tint = Orange.copy(alpha = 0.8f),
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
            entry.note?.takeIf { it.isNotBlank() }?.let { note ->
                Text(note, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }

        // Right side: amount + payment method chip
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = formatLKRFull(entry.amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFEF4444),
            )
            PaymentMethodChip(method = entry.paymentMethod)
        }
    }
}

@Composable
private fun PaymentMethodChip(method: String) {
    val isAutoDebit = method.contains("Auto", ignoreCase = true)
    val chipColor = when {
        method.equals("Card", ignoreCase = true) -> Color(0xFF3B82F6)
        method.equals("Cash", ignoreCase = true) -> Color(0xFF10B981)
        isAutoDebit -> Orange
        else -> TextSecondary
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(chipColor.copy(alpha = 0.1f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(paymentMethodIcon(method), null, tint = chipColor, modifier = Modifier.size(10.dp))
        Text(method, fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = chipColor)
    }
}

// ── Zombie subscriptions section ──────────────────────────────────────────────

@Composable
private fun ZombieSubscriptionsSection(
    entries: List<ExpenseEntry>,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(1.dp, AmberWarning.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
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
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                        .background(AmberWarning)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text("${entries.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                null,
                tint = AmberWarning,
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
                            .clip(RoundedCornerShape(10.dp))
                            .background(AmberWarningBg)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(categoryIcon(entry.category), null, tint = AmberWarning, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                entry.subCategory?.takeIf { it.isNotBlank() } ?: entry.category,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF78350F),
                            )
                            Text(
                                formatLKRFull(entry.amount) + "/mo",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberWarning.copy(alpha = 0.15f))
                                .border(1.dp, AmberWarning.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                        ) {
                            Text("Cancel?", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberWarning)
                        }
                    }
                }
            }
        }
    }
}

// ── Monthly trend section ─────────────────────────────────────────────────────

private data class MonthTrend(val label: String, val amount: Float)

private fun computeMonthlyTrend(entries: List<ExpenseEntry>): List<MonthTrend> {
    val cal = Calendar.getInstance()
    return (5 downTo 0).map { monthsBack ->
        val c = cal.clone() as Calendar
        c.add(Calendar.MONTH, -monthsBack)
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val label = SimpleDateFormat("MMM", Locale.getDefault()).format(c.time)
        val total = entries.filter { e ->
            val d = e.date?.toDate() ?: return@filter false
            val ec = Calendar.getInstance().also { it.time = d }
            ec.get(Calendar.YEAR) == year && ec.get(Calendar.MONTH) == month
        }.sumOf { it.amount }.toFloat()
        MonthTrend(label, total)
    }
}

@Composable
private fun MonthlyTrendSection(
    trendData: List<MonthTrend>,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceWhite)
            .border(0.5.dp, Divider, RoundedCornerShape(20.dp))
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
                "Monthly Spend Trend",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )
            Icon(
                if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp),
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            MonthlyBarChart(trendData = trendData)
        }
    }
}

@Composable
private fun MonthlyBarChart(trendData: List<MonthTrend>) {
    if (trendData.isEmpty()) return
    val maxVal = trendData.maxOf { it.amount }.coerceAtLeast(1f)
    val highestIndex = trendData.indexOfFirst { it.amount == trendData.maxOf { m -> m.amount } }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        ) {
            val barCount = trendData.size
            val spacing = 10.dp.toPx()
            val barWidth = (size.width - spacing * (barCount + 1)) / barCount
            val chartHeight = size.height - 20.dp.toPx()

            trendData.forEachIndexed { i, item ->
                val barH = (item.amount / maxVal) * chartHeight
                val left = spacing + i * (barWidth + spacing)
                val top = chartHeight - barH
                val isHighest = i == highestIndex

                // Bar
                drawRoundRect(
                    color = if (isHighest) Orange else Orange.copy(alpha = 0.45f),
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barH),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx()),
                )

                // Annotation dot on highest
                if (isHighest) {
                    drawCircle(
                        color = Orange,
                        radius = 4.dp.toPx(),
                        center = Offset(left + barWidth / 2, top - 8.dp.toPx()),
                    )
                }
            }
        }

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            trendData.forEach { item ->
                val isHighest = item.amount == trendData.maxOf { it.amount }
                Text(
                    item.label,
                    fontSize = 10.sp,
                    fontWeight = if (isHighest) FontWeight.Bold else FontWeight.Normal,
                    color = if (isHighest) Orange else TextSecondary,
                )
            }
        }

        // Highest annotation text
        if (trendData.isNotEmpty()) {
            val highest = trendData.maxByOrNull { it.amount }
            if (highest != null && highest.amount > 0f) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Orange.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        "Peak: ${highest.label} · ${formatLKRFull(highest.amount.toDouble())}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Orange,
                    )
                }
            }
        }
    }
}

// ── Shimmer skeleton ──────────────────────────────────────────────────────────

@Composable
private fun ShimmerExpenseCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )

    val shimmerColors = listOf(
        Color(0xFFE5E7EB),
        Color(0xFFF3F4F6),
        Color(0xFFE5E7EB),
    )

    val brush = Brush.horizontalGradient(
        colors = shimmerColors,
        startX = shimmerX * 400,
        endX = shimmerX * 400 + 400,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceWhite)
            .border(0.5.dp, Divider, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(42.dp).clip(CircleShape).background(brush))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.height(14.dp).fillMaxWidth(0.55f).clip(RoundedCornerShape(4.dp)).background(brush))
            Box(modifier = Modifier.height(10.dp).fillMaxWidth(0.35f).clip(RoundedCornerShape(4.dp)).background(brush))
        }
        Box(modifier = Modifier.height(14.dp).width(70.dp).clip(RoundedCornerShape(4.dp)).background(brush))
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyExpenseState(
    category: String,
    onAddClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = if (category == "All") "🧾" else when (category) {
                "Food" -> "🍔"
                "Transport" -> "🚗"
                "Housing" -> "🏠"
                "Subscriptions" -> "📺"
                "Entertainment" -> "🎬"
                "Health" -> "💊"
                else -> "📂"
            },
            fontSize = 48.sp,
        )
        Text(
            text = if (category == "All") "No expenses yet" else "No $category expenses",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
        )
        Text(
            text = "Tap the button below to record your first expense",
            fontSize = 13.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(Orange)
                .clickable(onClick = onAddClick)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text("Add Expense", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// ── Add Expense bottom sheet ──────────────────────────────────────────────────

private data class QuickAddPreset(
    val label: String,
    val emoji: String,
    val category: String,
    val merchant: String,
)

private val quickAddPresets = listOf(
    QuickAddPreset("PickMe", "🛵", "Transport", "PickMe"),
    QuickAddPreset("UberEats", "🍔", "Food", "UberEats"),
    QuickAddPreset("Gym", "💪", "Health", "Gym"),
)

@Composable
private fun AddExpenseFormSheet(
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
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Handle bar
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFE5E7EB)),
        )

        Text(
            "Add Expense",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
        )

        // Quick-add shortcuts
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            quickAddPresets.forEach { preset ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangeLight)
                        .border(1.dp, Orange.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable {
                            onCategoryChange(preset.category)
                            onSubCategoryChange(preset.merchant)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                    ) {
                        Text(preset.emoji, fontSize = 14.sp)
                        Text(preset.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Orange)
                    }
                }
            }
        }

        // Amount field (large)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Amount", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            OutlinedTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start,
                    color = TextPrimary,
                ),
                placeholder = {
                    Text(
                        "0.00",
                        style = TextStyle(
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD1D5DB),
                        ),
                    )
                },
                prefix = {
                    Text(
                        "LKR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Orange,
                        modifier = Modifier.padding(end = 6.dp),
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Divider,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                ),
                shape = RoundedCornerShape(14.dp),
            )
        }

        // Category picker (icon grid)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            CategoryIconGrid(
                categories = ExpenseViewModel.CATEGORIES,
                selected = state.category,
                onSelect = onCategoryChange,
            )
        }

        // Merchant / note field
        OutlinedTextField(
            value = state.subCategory,
            onValueChange = onSubCategoryChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Merchant / note") },
            placeholder = { Text("e.g. Keells, PickMe, Netflix") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Orange,
                unfocusedBorderColor = Divider,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
            ),
            shape = RoundedCornerShape(14.dp),
        )

        // Payment method toggle
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Payment Method", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                ExpenseViewModel.PAYMENT_METHODS.forEach { method ->
                    val isSelected = state.paymentMethod == method
                    val bgColor by animateColorAsState(
                        if (isSelected) Orange else SurfaceWhite,
                        animationSpec = tween(150),
                        label = "pm_$method",
                    )
                    val textColor by animateColorAsState(
                        if (isSelected) Color.White else TextSecondary,
                        animationSpec = tween(150),
                        label = "pm_text_$method",
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(
                                1.dp,
                                if (isSelected) Orange else Divider,
                                RoundedCornerShape(12.dp),
                            )
                            .clickable { onPaymentMethodChange(method) }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(paymentMethodIcon(method), null, tint = textColor, modifier = Modifier.size(14.dp))
                        Text(method, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    }
                }
            }
        }

        // Date selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF9FAFB))
                .border(1.dp, Divider, RoundedCornerShape(14.dp))
                .clickable {
                    val cal = Calendar.getInstance().also { it.timeInMillis = state.dateMillis }
                    DatePickerDialog(
                        context,
                        { _, y, m, d ->
                            onDateChange(Calendar.getInstance().also { c ->
                                c.set(y, m, d, 0, 0, 0)
                                c.set(Calendar.MILLISECOND, 0)
                            }.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH),
                    ).show()
                }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Rounded.CalendarToday, null, tint = Orange, modifier = Modifier.size(18.dp))
            Column {
                Text("Date", fontSize = 11.sp, color = TextSecondary)
                Text(
                    dateFormat.format(Date(state.dateMillis)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                )
            }
        }

        // Recurring auto-debit indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(if (state.isRecurring) OrangeLight else Color(0xFFF9FAFB))
                .border(
                    1.dp,
                    if (state.isRecurring) Orange.copy(alpha = 0.4f) else Divider,
                    RoundedCornerShape(14.dp),
                )
                .clickable { onRecurringChange(!state.isRecurring) }
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(
                    Icons.Rounded.Autorenew,
                    null,
                    tint = if (state.isRecurring) Orange else TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Column {
                    Text(
                        "Recurring / Auto-debit",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.isRecurring) Orange else TextPrimary,
                    )
                    Text("Marks this as a recurring charge", fontSize = 11.sp, color = TextSecondary)
                }
            }
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (state.isRecurring) Orange else Divider),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isRecurring) {
                    Icon(Icons.Rounded.Add, null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Error message
        state.errorMessage?.let { msg ->
            Text(msg, fontSize = 12.sp, color = Color(0xFFEF4444))
        }

        // Save button
        Button(
            onClick = onRequestSubmit,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange,
                contentColor = Color.White,
            ),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color.White,
                )
            } else {
                Text("Save Expense", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

// ── Category icon grid (3-column) ─────────────────────────────────────────────

@Composable
private fun CategoryIconGrid(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    val chunked = categories.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunked.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { cat ->
                    val isSelected = selected == cat
                    val color = categoryColor(cat)
                    val bgColor by animateColorAsState(
                        if (isSelected) color else Color(0xFFF9FAFB),
                        animationSpec = tween(180),
                        label = "cat_bg_$cat",
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bgColor)
                            .border(
                                1.5.dp,
                                if (isSelected) color else Divider,
                                RoundedCornerShape(12.dp),
                            )
                            .clickable { onSelect(cat) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            categoryIcon(cat),
                            null,
                            tint = if (isSelected) Color.White else color,
                            modifier = Modifier.size(22.dp),
                        )
                        Text(
                            cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) Color.White else TextPrimary,
                        )
                    }
                }
                // Fill empty cells in last row
                repeat(3 - row.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
