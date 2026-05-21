package com.bpeople.finpilot.ui.screens.income

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.GlassTheme
import com.bpeople.finpilot.ui.components.NavTab
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
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
) {
    val state by viewModel.incomeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

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
    LaunchedEffect(state.isSubmitted) {
        if (!state.isSubmitted) return@LaunchedEffect
        snackbarHostState.showSnackbar("Income saved!")
        viewModel.consumeSubmitted()
    }

    if (state.showAddSheet) {
        IncomeAddDialog(
            state = state,
            onDismiss = viewModel::onHideAddSheet,
            onSourceChange = viewModel::onSourceChange,
            onAmountChange = viewModel::onAmountOriginalChange,
            onCurrencyChange = viewModel::onCurrencyChange,
            onDateChange = viewModel::onDateChange,
            onLabelChange = viewModel::onLabelChange,
            onIncomeTypeChange = viewModel::onIncomeTypeChange,
            onProjectRefChange = viewModel::onProjectRefChange,
            onRequestSubmit = viewModel::requestSubmit,
            onRefreshRates = viewModel::refreshExchangeRates,
            onConfirmExchangeRate = viewModel::confirmExchangeRate,
            onDismissRateConfirmation = viewModel::dismissRateConfirmation,
        )
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
    var selectedSourceFilter by rememberSaveable { mutableStateOf(state.historySourceFilter ?: "All") }
    val trendData = remember(state.entries) { computeIncomeMonthlyTrend(state.entries) }

    val pageSize = IncomeViewModel.HISTORY_PAGE_SIZE
    val totalItems = state.filteredEntries.size
    val pageCount = maxOf(1, (totalItems + pageSize - 1) / pageSize)
    val pageIndex = state.historyPageIndex.coerceIn(0, pageCount - 1)
    val pageEntries = remember(state.filteredEntries, pageIndex) {
        state.filteredEntries.drop(pageIndex * pageSize).take(pageSize)
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::onShowAddSheet,
                icon = { androidx.compose.material3.Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp)) },
                text = { Text("Add Income", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp) },
                containerColor = GlassTheme.Orange,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp),
            )
        },
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
                        sources = listOf("All") + IncomeViewModel.SOURCES,
                        selected = selectedSourceFilter,
                        onSelect = {
                            selectedSourceFilter = it
                            viewModel.onHistorySourceFilterChange(it.takeUnless { source -> source == "All" })
                        },
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }

                if (pageEntries.isEmpty()) {
                    item {
                        GlassEmptyIncomeState(
                            onAddClick = viewModel::onShowAddSheet,
                            selectedSourceFilter = selectedSourceFilter,
                        )
                    }
                    return@LazyColumn
                }

                items(
                    items = pageEntries,
                    key = { entry -> entry.id },
                ) { entry ->
                    GlassSwipeableIncomeRow(
                        entry = entry,
                        onDelete = { viewModel.deleteIncome(entry) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 3.dp),
                    )
                }

                item {
                    GlassPaginationControls(
                        pageIndex = pageIndex,
                        pageCount = pageCount,
                        onPrevious = viewModel::goToPreviousHistoryPage,
                        onNext = viewModel::goToNextHistoryPage,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
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
            )
        }
    }
}

@Composable
private fun GlassPaginationControls(
    pageIndex: Int,
    pageCount: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassTheme.GlassSurface)
            .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Page ${pageIndex + 1} of $pageCount",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = GlassTheme.TextSecondary,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onPrevious, enabled = pageIndex > 0) {
                Text("Prev", color = GlassTheme.TextSecondary)
            }
            Button(
                onClick = onNext,
                enabled = pageIndex < pageCount - 1,
                colors = ButtonDefaults.buttonColors(containerColor = GlassTheme.Orange),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GlassIncomeHeader(
    monthTotal: Double,
    recurringTotal: Double,
    nonRecurringTotal: Double,
    entryCount: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawCircle(
                    color = Color(0x2EFF6B00),
                    radius = 220.dp.toPx(),
                    center = Offset(size.width * 0.92f, -60.dp.toPx()),
                )
                drawCircle(
                    color = Color(0x1A534AB7),
                    radius = 150.dp.toPx(),
                    center = Offset(size.width * 0.05f, size.height * 0.7f),
                )
            }
            .padding(top = 52.dp, bottom = 32.dp, start = 24.dp, end = 24.dp),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun GlassSwipeableIncomeRow(
    entry: IncomeEntry,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> { onDelete(); true }
                else -> false
            }
        },
        positionalThreshold = { it * 0.40f },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val isDelete = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val bgColor = if (isDelete) Color(0x33EF4444) else Color.Transparent
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(18.dp))
                    .background(bgColor)
                    .border(1.dp, if (isDelete) Color(0x59EF4444) else Color.Transparent, RoundedCornerShape(18.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (isDelete) {
                    androidx.compose.material3.Icon(Icons.Default.Delete, null, tint = GlassTheme.Danger, modifier = Modifier.size(20.dp))
                }
            }
        },
        modifier = modifier,
    ) {
        GlassIncomeItem(entry)
    }
}

@Composable
private fun GlassIncomeItem(entry: IncomeEntry, modifier: Modifier = Modifier) {
    val accent = sourceColor(entry.source)
    val dateFormat = remember { SimpleDateFormat("dd MMM", Locale.getDefault()) }
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
                .background(accent.copy(alpha = 0.15f))
                .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.material3.Icon(sourceIcon(entry.source), null, tint = accent, modifier = Modifier.size(20.dp))
        }

        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                entry.label?.takeIf { it.isNotBlank() } ?: entry.source,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassTheme.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                entry.date?.toDate()?.let { dateFormat.format(it) } ?: "",
                fontSize = 11.sp,
                color = GlassTheme.TextHint,
            )
        }

        androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                "+${entry.currencyOriginal} ${"%.2f".format(entry.amountOriginal)}",
                fontSize = 14.sp,
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

@Composable
private fun IncomeAddDialog(
    state: IncomeViewModel.IncomeUiState,
    onDismiss: () -> Unit,
    onSourceChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onLabelChange: (String) -> Unit,
    onIncomeTypeChange: (String) -> Unit,
    onProjectRefChange: (String) -> Unit,
    onRequestSubmit: () -> Unit,
    onRefreshRates: () -> Unit,
    onConfirmExchangeRate: () -> Unit,
    onDismissRateConfirmation: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val rateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val showExchange = state.currencyOriginal != "LKR"

    if (state.showRateConfirmation) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = onDismissRateConfirmation,
            containerColor = GlassTheme.BgMid,
            shape = RoundedCornerShape(20.dp),
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
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissRateConfirmation) {
                    Text("Cancel", color = GlassTheme.TextSecondary)
                }
            },
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            shape = RoundedCornerShape(22.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = GlassTheme.BgMid),
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "Add Income",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlassTheme.TextPrimary,
                )

                var sourceExpanded by remember { mutableStateOf(false) }
                var sourceMenuWidthPx by remember { mutableStateOf(0) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.source,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { sourceMenuWidthPx = it.size.width },
                        label = { Text("Source") },
                        readOnly = true,
                        enabled = false,
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
                            .clickable { sourceExpanded = true }
                    )
                    DropdownMenu(
                        expanded = sourceExpanded,
                        onDismissRequest = { sourceExpanded = false },
                        modifier = Modifier.width(with(density) { sourceMenuWidthPx.toDp() }),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = GlassTheme.BgMid,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.GlassBorder),
                    ) {
                        IncomeViewModel.SOURCES.forEach { source ->
                            DropdownMenuItem(
                                text = { Text(source, color = GlassTheme.TextPrimary) },
                                onClick = {
                                    onSourceChange(source)
                                    sourceExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.amountOriginal,
                    onValueChange = onAmountChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount") },
                    leadingIcon = {
                        var currencyExpanded by remember { mutableStateOf(false) }
                        var currencyChipWidthPx by remember { mutableStateOf(0) }
                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(GlassTheme.GlassBg)
                                    .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(10.dp))
                                    .clickable { currencyExpanded = true }
                                    .onGloballyPositioned { currencyChipWidthPx = it.size.width }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    state.currencyOriginal,
                                    fontWeight = FontWeight.Bold,
                                    color = GlassTheme.TextPrimary,
                                    fontSize = 13.sp,
                                )
                                Icon(
                                    Icons.Rounded.ArrowDropDown,
                                    null,
                                    tint = GlassTheme.TextHint,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                            DropdownMenu(
                                expanded = currencyExpanded,
                                onDismissRequest = { currencyExpanded = false },
                                modifier = Modifier.width(with(density) { currencyChipWidthPx.toDp() }),
                                shape = RoundedCornerShape(14.dp),
                                containerColor = GlassTheme.BgMid,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.GlassBorder),
                            ) {
                                IncomeViewModel.CURRENCIES.forEach { currency ->
                                    DropdownMenuItem(
                                        text = { Text(currency, color = GlassTheme.TextPrimary) },
                                        onClick = {
                                            onCurrencyChange(currency)
                                            currencyExpanded = false
                                        },
                                    )
                                }
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassTheme.Orange,
                        unfocusedBorderColor = GlassTheme.GlassBorder,
                        focusedContainerColor = GlassTheme.GlassSurface,
                        unfocusedContainerColor = GlassTheme.GlassBg,
                        cursorColor = GlassTheme.Orange,
                    ),
                )

                if (showExchange) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GlassTheme.OrangeDim)
                            .border(1.dp, Color(0x40FF6B00), RoundedCornerShape(12.dp))
                            .padding(10.dp),
                    ) {
                        androidx.compose.foundation.layout.Column(
                            verticalArrangement = Arrangement.spacedBy(3.dp),
                        ) {
                            Text(
                                "≈ LKR ${"%.2f".format(state.amountLkrPreview)}",
                                color = GlassTheme.OrangeLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                            )
                            val updatedText = state.exchangeRateLastUpdatedMillis
                                ?.let { rateFormat.format(Date(it)) } ?: "unknown"
                            Text(
                                "1 ${state.currencyOriginal} = LKR ${state.exchangeRate} · $updatedText" +
                                    if (state.exchangeRateIsStale) " · stale" else "",
                                color = GlassTheme.TextSecondary,
                                fontSize = 11.sp,
                            )
                            TextButton(
                                onClick = onRefreshRates,
                                enabled = !state.isRefreshingRates,
                                contentPadding = PaddingValues(0.dp),
                            ) {
                                Text("Refresh rates", color = GlassTheme.OrangeLight)
                            }
                        }
                    }
                }

                var typeExpanded by remember { mutableStateOf(false) }
                var typeMenuWidthPx by remember { mutableStateOf(0) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.incomeType,
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { typeMenuWidthPx = it.size.width },
                        label = { Text("Income type") },
                        readOnly = true,
                        enabled = false,
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
                            .clickable { typeExpanded = true }
                    )
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false },
                        modifier = Modifier.width(with(density) { typeMenuWidthPx.toDp() }),
                        shape = RoundedCornerShape(14.dp),
                        containerColor = GlassTheme.BgMid,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassTheme.GlassBorder),
                    ) {
                        IncomeViewModel.INCOME_TYPES.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type, color = GlassTheme.TextPrimary) },
                                onClick = {
                                    onIncomeTypeChange(type)
                                    typeExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = state.label,
                    onValueChange = onLabelChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Label (optional)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GlassTheme.Orange,
                        unfocusedBorderColor = GlassTheme.GlassBorder,
                        focusedContainerColor = GlassTheme.GlassSurface,
                        unfocusedContainerColor = GlassTheme.GlassBg,
                        cursorColor = GlassTheme.Orange,
                    ),
                )

                if (state.source == "Freelance") {
                    OutlinedTextField(
                        value = state.projectRef,
                        onValueChange = onProjectRefChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Project reference (optional)") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GlassTheme.Orange,
                            unfocusedBorderColor = GlassTheme.GlassBorder,
                            focusedContainerColor = GlassTheme.GlassSurface,
                            unfocusedContainerColor = GlassTheme.GlassBg,
                            cursorColor = GlassTheme.Orange,
                        ),
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GlassTheme.GlassBg)
                        .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            val cal = Calendar.getInstance().also { it.timeInMillis = state.dateMillis }
                            DatePickerDialog(context, { _, y, m, d ->
                                onDateChange(
                                    Calendar.getInstance().also { picked ->
                                        picked.set(y, m, d, 0, 0, 0)
                                        picked.set(Calendar.MILLISECOND, 0)
                                    }.timeInMillis
                                )
                            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(Icons.Rounded.CalendarToday, null, tint = GlassTheme.OrangeLight, modifier = Modifier.size(18.dp))
                    Text(
                        dateFormat.format(Date(state.dateMillis)),
                        color = GlassTheme.TextPrimary,
                        fontWeight = FontWeight.Medium,
                    )
                }

                state.errorMessage?.let {
                    Text(it, color = GlassTheme.Danger, fontSize = 12.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel", color = GlassTheme.TextSecondary)
                    }
                    Button(
                        onClick = onRequestSubmit,
                        enabled = !state.isLoading,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = GlassTheme.Orange),
                    ) {
                        Text(if (state.isLoading) "Saving..." else "Save")
                    }
                }
            }
        }
    }
}
