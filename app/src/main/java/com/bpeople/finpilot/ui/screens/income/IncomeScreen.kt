package com.bpeople.finpilot.ui.screens.income

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.data.model.FreelanceProject
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.GlassTheme
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.components.DynamicHeaderBackground
import com.bpeople.finpilot.ui.components.wavyBottomShape
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import kotlin.math.max
import java.util.Locale
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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

private data class IncomeMonthTrend(val label: String, val amount: Float)

private fun computeIncomeMonthlyTrend(entries: List<IncomeEntry>): List<IncomeMonthTrend> {
    val cal = Calendar.getInstance()
    return (5 downTo 0).map { back ->
        val c = (cal.clone() as Calendar).also { it.add(Calendar.MONTH, -back) }
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH)
        val label = SimpleDateFormat("MMM", Locale.getDefault()).format(c.time)
        val total = entries.filter { e ->
            val ec = Calendar.getInstance().also { it.time = e.date?.toDate() ?: return@filter false }
            ec.get(Calendar.YEAR) == y && ec.get(Calendar.MONTH) == m
        }.sumOf { it.amountLKR }.toFloat()
        IncomeMonthTrend(label, total)
    }
}

private fun sourceIcon(source: String): ImageVector = when {
    source.equals("Salary", ignoreCase = true) -> Icons.Rounded.AccountBalance
    source.equals("Freelance", ignoreCase = true) -> Icons.Rounded.Code
    source.equals("AdSense", ignoreCase = true) -> Icons.Rounded.Language
    source.equals("Crypto", ignoreCase = true) -> Icons.Rounded.CurrencyBitcoin
    source.equals("Business", ignoreCase = true) -> Icons.Rounded.Work
    else -> Icons.Rounded.AttachMoney
}

private fun sourceColor(source: String): Color = when {
    source.equals("Salary", ignoreCase = true) -> GlassTheme.Success
    source.equals("Freelance", ignoreCase = true) -> Color(0xFF3B82F6)
    source.equals("AdSense", ignoreCase = true) -> Color(0xFF22C55E)
    source.equals("Crypto", ignoreCase = true) -> Color(0xFF8B5CF6)
    else -> GlassTheme.TextHint
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun IncomeScreen(
    viewModel: IncomeViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToExpense: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
) {
    val state by viewModel.incomeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isSubmitted) {
        if (!state.isSubmitted) return@LaunchedEffect
        viewModel.consumeSubmitted()
        snackbarHostState.showSnackbar("Income saved!")
        scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSheet = false }
    }

    LaunchedEffect(state.pendingDeleteEntry) {
        val entry = state.pendingDeleteEntry ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            "${entry.source} income deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short,
        )
        when (result) {
            SnackbarResult.ActionPerformed -> viewModel.undoDelete()
            SnackbarResult.Dismissed -> viewModel.consumePendingDelete()
        }
    }
    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.consumeError()
    }

    val monthStart = remember { currentMonthStartMillis() }
    val monthEntries = remember(state.entries) {
        state.entries.filter { (it.date?.toDate()?.time ?: 0L) >= monthStart }
    }
    val monthTotal = monthEntries.sumOf { it.amountLKR }
    val recurringTotal = monthEntries.filter { it.type == "RECURRING" }.sumOf { it.amountLKR }
    val nonRecurringTotal = monthEntries.filter { it.type != "RECURRING" }.sumOf { it.amountLKR }
    val sourceTotals = remember(monthEntries) {
        monthEntries.groupBy { it.source }
            .mapValues { (_, list) -> list.sumOf { it.amountLKR } }
            .entries.sortedByDescending { it.value }
    }
    var selectedSourceFilter by rememberSaveable { mutableStateOf("All") }
    val displayEntries = remember(state.entries, selectedSourceFilter) {
        if (selectedSourceFilter == "All") state.entries
        else state.entries.filter { it.source.equals(selectedSourceFilter, ignoreCase = true) }
    }
    val trendData = remember(state.entries) { computeIncomeMonthlyTrend(state.entries) }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
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
                            GlassTheme.OrbGreen,
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
                            GlassTheme.OrbPurple,
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
                item {
                    GlassIncomeHeader(
                        monthTotal = monthTotal,
                        recurringTotal = recurringTotal,
                        nonRecurringTotal = nonRecurringTotal,
                        entryCount = monthEntries.size,
                        onAddClick = { showAddSheet = true },
                    )
                }

                if (sourceTotals.isNotEmpty()) {
                    item {
                        GlassIncomeSourceSummaryCard(
                            sourceTotals = sourceTotals,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }

                item {
                    GlassIncomeSourceFilterBar(
                        sources = listOf("All") + state.availableSources,
                        selected = selectedSourceFilter,
                        onSelect = { selectedSourceFilter = it },
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                if (state.isLoading) {
                    items(4) {
                        GlassShimmerCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }
                    return@LazyColumn
                }

                if (displayEntries.isEmpty()) {
                    item {
                        GlassEmptyIncomeState(
                            onAddClick = { showAddSheet = true },
                            selectedSourceFilter = selectedSourceFilter,
                        )
                    }
                    return@LazyColumn
                }

                item {
                    IncomeHistoryTable(
                        entries = pagedEntries,
                        onDelete = { entry -> viewModel.deleteIncome(entry) },
                    )
                }

                item {
                    IncomeHistoryPaginationBar(
                        currentPage = currentPage,
                        totalPages = totalPages,
                        hasMore = state.hasMore,
                        isLoadingMore = state.isLoadingMore,
                        onPreviousPage = { currentPage-- },
                        onNextPage = {
                            currentPage++
                            if (currentPage >= totalPages - 1 && state.hasMore && !state.isLoadingMore) {
                                viewModel.loadNextPage()
                            }
                        },
                    )
                }

                item {
                    GlassIncomeMonthlyTrend(
                        trendData = trendData,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            FinPilotBottomNavBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentTab = NavTab.INCOME,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToIncome = {},
                onNavigateToExpense = onNavigateToExpense,
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSettings = onNavigateToSettings,
            )
        }
    }

    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = sheetState,
            containerColor = GlassTheme.GlassSurface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        ) {
            AddIncomeFormSheet(
                state = state,
                onSourceChange = viewModel::onSourceChange,
                onAmountChange = viewModel::onAmountOriginalChange,
                onCurrencyChange = viewModel::onCurrencyChange,
                onDateChange = viewModel::onDateChange,
                onLabelChange = viewModel::onLabelChange,
                onIncomeTypeChange = viewModel::onIncomeTypeChange,
                onProjectRefChange = viewModel::onProjectRefChange,
                onRequestSubmit = viewModel::requestSubmit,
                onConfirmExchangeRate = viewModel::confirmExchangeRate,
                onDismissRateConfirmation = viewModel::dismissRateConfirmation,
                onRefreshRates = viewModel::refreshExchangeRates,
            )
        }
    }
}

@Composable
private fun GlassIncomeHeader(
    monthTotal: Double,
    recurringTotal: Double,
    nonRecurringTotal: Double,
    entryCount: Int,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        DynamicHeaderBackground(
            patternType = "income",
            modifier = Modifier.matchParentSize().clip(wavyBottomShape())
        )
        androidx.compose.foundation.layout.Column(
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
                    text = "My Income",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GlassTheme.TextPrimary,
                    letterSpacing = (-0.5).sp,
                )
                IconButton(onClick = onAddClick) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add Income",
                        tint = GlassTheme.TextPrimary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    "THIS MONTH'S INCOME",
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassIncomeBadge("${formatLKRShort(recurringTotal)} recurring")
                    GlassIncomeBadge("${formatLKRShort(nonRecurringTotal)} other")
                }
                GlassIncomeBadge("$entryCount entries")
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun GlassIncomeBadge(text: String) {
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

// ── Income History Table ──────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncomeHistoryTable(
    entries: List<IncomeEntry>,
    onDelete: (IncomeEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    IncomeTableRow(entry = entry)
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
private fun IncomeTableRow(entry: IncomeEntry, modifier: Modifier = Modifier) {
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
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
            Text(
                entry.label?.takeIf { it.isNotBlank() } ?: entry.source,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassTheme.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                entry.source,
                fontSize = 11.sp,
                color = GlassTheme.TextHint,
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
                "+${entry.currencyOriginal} ${"%.2f".format(entry.amountOriginal)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTheme.Success,
            )
            if (entry.currencyOriginal != "LKR") {
                Text(
                    "≈ ${formatLKRShort(entry.amountLKR)}",
                    fontSize = 10.sp,
                    color = GlassTheme.TextHint,
                )
            }
        }
    }
}

@Composable
private fun IncomeHistoryPaginationBar(
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
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.height(13.dp).fillMaxWidth(0.5f).clip(RoundedCornerShape(4.dp)).background(brush))
            Box(modifier = Modifier.height(10.dp).fillMaxWidth(0.3f).clip(RoundedCornerShape(4.dp)).background(brush))
        }
        Box(modifier = Modifier.height(13.dp).width(68.dp).clip(RoundedCornerShape(4.dp)).background(brush))
    }
}

@Composable
private fun GlassEmptyIncomeState(onAddClick: () -> Unit, selectedSourceFilter: String) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("💰", fontSize = 52.sp)
        Text(
            if (selectedSourceFilter == "All") "No income yet" else "No $selectedSourceFilter income yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = GlassTheme.TextPrimary,
        )
        Text(
            "Tap the button below to add your first income",
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
            Text("Add Income", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
private fun GlassIncomeSourceSummaryCard(
    sourceTotals: List<Map.Entry<String, Double>>,
    modifier: Modifier = Modifier,
) {
    val max = sourceTotals.maxOfOrNull { it.value }?.takeIf { it > 0 } ?: 1.0
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassTheme.GlassBg)
            .border(1.dp, GlassTheme.GlassBorderLight, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            "SOURCE BREAKDOWN",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = GlassTheme.TextHint,
        )
        sourceTotals.take(4).forEach { entry ->
            val ratio = (entry.value / max).toFloat().coerceIn(0f, 1f)
            androidx.compose.foundation.layout.Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(entry.key, color = GlassTheme.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(formatLKRShort(entry.value), color = GlassTheme.TextSecondary, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(GlassTheme.GlassSurface),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(ratio)
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GlassTheme.Orange, GlassTheme.Orange.copy(alpha = 0.55f))
                                )
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassIncomeSourceFilterBar(
    sources: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val scroll = androidx.compose.foundation.rememberScrollState()
        Row(
            modifier = Modifier.horizontalScroll(scroll),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sources.forEach { source ->
                val active = source == selected
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (active) GlassTheme.Orange.copy(alpha = 0.14f) else GlassTheme.GlassSurface)
                        .border(
                            1.dp,
                            if (active) GlassTheme.Orange.copy(alpha = 0.45f) else GlassTheme.GlassBorder,
                            RoundedCornerShape(18.dp),
                        )
                        .clickable { onSelect(source) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                ) {
                    Text(
                        source,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (active) GlassTheme.Orange else GlassTheme.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun GlassIncomeMonthlyTrend(
    trendData: List<IncomeMonthTrend>,
    modifier: Modifier = Modifier,
) {
    val maxValue = trendData.maxOfOrNull { it.amount }?.takeIf { it > 0f } ?: 1f
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassTheme.GlassBg)
            .border(1.dp, GlassTheme.GlassBorderLight, RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "MONTHLY TREND",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp,
            color = GlassTheme.TextHint,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            trendData.forEach { point ->
                val ratio = (point.amount / maxValue).coerceIn(0f, 1f)
                androidx.compose.foundation.layout.Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(18.dp)
                            .height((16.dp + (88.dp * ratio)))
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(GlassTheme.Orange.copy(alpha = 0.95f), GlassTheme.Orange.copy(alpha = 0.35f))
                                )
                            ),
                    )
                    Text(point.label, fontSize = 10.sp, color = GlassTheme.TextHint)
                }
            }
        }
    }
}

// ── Add Income Form Sheet ─────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddIncomeFormSheet(
    state: IncomeViewModel.IncomeUiState,
    onSourceChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onLabelChange: (String) -> Unit,
    onIncomeTypeChange: (String) -> Unit,
    onProjectRefChange: (String) -> Unit,
    onRequestSubmit: () -> Unit,
    onConfirmExchangeRate: () -> Unit,
    onDismissRateConfirmation: () -> Unit,
    onRefreshRates: () -> Unit,
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val rateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val showExchange = state.currencyOriginal != "LKR"
    val selectedDateLabel = remember(state.dateMillis) { dateFormat.format(Date(state.dateMillis)) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = GlassTheme.GlassSurface,
        focusedContainerColor = GlassTheme.GlassSurface,
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = GlassTheme.Orange,
        unfocusedTextColor = GlassTheme.TextPrimary,
        focusedTextColor = GlassTheme.TextPrimary,
        unfocusedLabelColor = GlassTheme.TextSecondary,
        focusedLabelColor = GlassTheme.Orange,
    )

    if (state.showRateConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissRateConfirmation,
            containerColor = GlassTheme.GlassSurface,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    "Confirm exchange rate",
                    color = GlassTheme.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                val updatedText = state.exchangeRateLastUpdatedMillis
                    ?.let { rateFormat.format(Date(it)) } ?: "unknown"
                Text(
                    "Use 1 ${state.currencyOriginal} = LKR ${state.exchangeRate} (updated $updatedText)?",
                    color = GlassTheme.TextSecondary,
                )
            },
            confirmButton = {
                Button(
                    onClick = onConfirmExchangeRate,
                    colors = ButtonDefaults.buttonColors(containerColor = GlassTheme.Orange),
                    shape = RoundedCornerShape(12.dp),
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = onDismissRateConfirmation) {
                    Text("Cancel", color = GlassTheme.TextSecondary)
                }
            },
        )
    }

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
                .background(
                    GlassTheme.TextSecondary.copy(alpha = 0.3f),
                    RoundedCornerShape(2.dp),
                ),
        )

        Text(
            text = "Add Income",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = GlassTheme.TextPrimary,
        )

        // Error message
        if (state.errorMessage != null) {
            Text(
                text = state.errorMessage,
                color = GlassTheme.Danger,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GlassTheme.Danger.copy(alpha = 0.08f))
                    .padding(12.dp),
            )
        }

        // Amount field
        OutlinedTextField(
            value = state.amountOriginal,
            onValueChange = onAmountChange,
            label = { Text("Amount") },
            placeholder = { Text("0.00", color = GlassTheme.TextHint) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
            colors = fieldColors,
        )

        // Currency row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassTheme.GlassSurface)
                .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Currency",
                    fontSize = 12.sp,
                    color = GlassTheme.TextSecondary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    state.currencyOriginal,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTheme.TextPrimary,
                )
            }
            var currencyExpanded by remember { mutableStateOf(false) }
            Box {
                TextButton(onClick = { currencyExpanded = true }) {
                    Text("Change", color = GlassTheme.Orange, fontWeight = FontWeight.SemiBold)
                }
                DropdownMenu(
                    expanded = currencyExpanded,
                    onDismissRequest = { currencyExpanded = false },
                    containerColor = GlassTheme.GlassSurface,
                ) {
                    IncomeViewModel.CURRENCIES.forEach { cur ->
                        DropdownMenuItem(
                            text = { Text(cur, color = GlassTheme.TextPrimary) },
                            onClick = { onCurrencyChange(cur); currencyExpanded = false },
                        )
                    }
                }
            }
        }

        // Exchange rate info (non-LKR)
        AnimatedVisibility(
            visible = showExchange,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(GlassTheme.OrangeDim)
                    .border(1.dp, GlassTheme.Orange.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    "≈ LKR ${"%.2f".format(state.amountLkrPreview)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTheme.Orange,
                )
                val updatedText = state.exchangeRateLastUpdatedMillis
                    ?.let { rateFormat.format(Date(it)) } ?: "unknown"
                Text(
                    text = if (state.exchangeRateAvailable)
                        "1 ${state.currencyOriginal} = LKR ${state.exchangeRate} · $updatedText" +
                            if (state.exchangeRateIsStale) " · stale" else ""
                    else "Rate unavailable",
                    fontSize = 11.sp,
                    color = GlassTheme.TextSecondary,
                )
                TextButton(
                    onClick = onRefreshRates,
                    enabled = !state.isRefreshingRates,
                ) {
                    if (state.isRefreshingRates) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.5.dp,
                            color = GlassTheme.Orange,
                        )
                        Spacer(Modifier.width(4.dp))
                    }
                    Text("Refresh rates", fontSize = 12.sp, color = GlassTheme.Orange)
                }
            }
        }

        // Source selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassTheme.GlassSurface)
                .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Income Source",
                fontSize = 12.sp,
                color = GlassTheme.TextSecondary,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.availableSources.forEach { src ->
                    val isSelected = state.source == src
                    val bg by animateColorAsState(
                        if (isSelected) GlassTheme.Orange else GlassTheme.GlassBg,
                        tween(200),
                        label = "src_bg",
                    )
                    val tc by animateColorAsState(
                        if (isSelected) Color.White else GlassTheme.TextSecondary,
                        tween(200),
                        label = "src_tc",
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bg)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else GlassTheme.GlassBorder,
                                RoundedCornerShape(20.dp),
                            )
                            .clickable { onSourceChange(src) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(src, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = tc)
                    }
                }
            }
        }

        // Income type selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassTheme.GlassSurface)
                .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Income Type",
                fontSize = 12.sp,
                color = GlassTheme.TextSecondary,
                fontWeight = FontWeight.Medium,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IncomeViewModel.INCOME_TYPES.forEach { type ->
                    val isSelected = state.incomeType == type
                    val bg by animateColorAsState(
                        if (isSelected) GlassTheme.Orange else GlassTheme.GlassBg,
                        tween(200),
                        label = "type_bg",
                    )
                    val tc by animateColorAsState(
                        if (isSelected) Color.White else GlassTheme.TextSecondary,
                        tween(200),
                        label = "type_tc",
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .border(
                                1.dp,
                                if (isSelected) Color.Transparent else GlassTheme.GlassBorder,
                                RoundedCornerShape(12.dp),
                            )
                            .clickable { onIncomeTypeChange(type) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            type,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tc,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }

        // Note field
        OutlinedTextField(
            value = state.label,
            onValueChange = onLabelChange,
            label = { Text("Note (optional)") },
            placeholder = { Text("Description or reference", color = GlassTheme.TextHint) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = fieldColors,
        )

        // Date row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassTheme.GlassSurface)
                .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(16.dp))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Date",
                    fontSize = 12.sp,
                    color = GlassTheme.TextSecondary,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    selectedDateLabel,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GlassTheme.TextPrimary,
                )
            }
            TextButton(
                onClick = {
                    val cal = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val picked = Calendar.getInstance().apply {
                                set(year, month, day, 0, 0, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            onDateChange(picked.timeInMillis)
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH),
                    ).show()
                },
            ) {
                Text("Change", color = GlassTheme.Orange, fontWeight = FontWeight.SemiBold)
            }
        }

        // Freelance project link
        AnimatedVisibility(
            visible = state.source == "Freelance",
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            IncomeProjectRow(
                projects = state.projects,
                selectedId = state.projectRef,
                onSelect = onProjectRefChange,
            )
        }

        // Submit button
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
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    color = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text("Save Income", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun IncomeProjectRow(
    projects: List<FreelanceProject>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = projects.firstOrNull { it.id == selectedId }
    val label = selected?.let { "${it.projectTitle} — ${it.clientName}" }
        ?: if (projects.isEmpty()) "No projects" else "Select project"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassTheme.GlassSurface)
            .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Link to Project",
                fontSize = 12.sp,
                color = GlassTheme.TextSecondary,
                fontWeight = FontWeight.Medium,
            )
            Text(
                label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected != null) GlassTheme.TextPrimary else GlassTheme.TextHint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box {
            TextButton(
                onClick = { if (projects.isNotEmpty()) expanded = true },
                enabled = projects.isNotEmpty(),
            ) {
                Text("Change", color = GlassTheme.Orange, fontWeight = FontWeight.SemiBold)
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = GlassTheme.GlassSurface,
            ) {
                DropdownMenuItem(
                    text = { Text("None", color = GlassTheme.TextPrimary) },
                    onClick = { onSelect(""); expanded = false },
                )
                projects.forEach { p ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    p.projectTitle,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = GlassTheme.TextPrimary,
                                )
                                Text(p.clientName, fontSize = 12.sp, color = GlassTheme.TextHint)
                            }
                        },
                        onClick = { onSelect(p.id); expanded = false },
                    )
                }
            }
        }
    }
}
