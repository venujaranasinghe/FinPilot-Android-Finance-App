package com.bpeople.finpilot.ui.screens.income

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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Loop
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.data.model.FreelanceProject
import com.bpeople.finpilot.data.model.IncomeEntry
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.components.GlassTheme
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

// ── Colour constants ──────────────────────────────────────────────────────────

private val Orange = Color(0xFFFF6B00)
private val OrangeLight = Color(0xFFFFF3E0)

private fun sourceColor(source: String): Color = when {
    source.equals("Salary", ignoreCase = true) -> GlassTheme.Orange
    source.equals("Freelance", ignoreCase = true) -> Color(0xFF2196F3)
    source.equals("AdSense", ignoreCase = true) -> Color(0xFF16A34A)
    source.equals("Crypto", ignoreCase = true) -> Color(0xFF9C27B0)
    else -> Color(0xFF6B7280)
}

private fun sourceIcon(source: String): ImageVector = when {
    source.equals("Salary", ignoreCase = true) -> Icons.Rounded.AccountBalance
    source.equals("Freelance", ignoreCase = true) -> Icons.Rounded.Code
    source.equals("AdSense", ignoreCase = true) -> Icons.Rounded.Language
    source.equals("Crypto", ignoreCase = true) -> Icons.Rounded.CurrencyBitcoin
    else -> Icons.Rounded.AttachMoney
}

private fun formatLKRFull(amount: Double) = "LKR %,.0f".format(amount)
private fun formatLKR(amount: Double): String = when {
    amount >= 1_000_000 -> "LKR %.1fM".format(amount / 1_000_000)
    amount >= 1_000 -> "LKR %.0fK".format(amount / 1_000)
    else -> "LKR ${amount.roundToInt()}"
}

// ── Entry point ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(
    viewModel: IncomeViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToIncome: () -> Unit = {},
    onNavigateToExpense: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onIncomeAdded: () -> Unit,
) {
    val state by viewModel.incomeState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Income submitted → close sheet, show toast
    LaunchedEffect(state.isSubmitted) {
        if (!state.isSubmitted) return@LaunchedEffect
        viewModel.consumeSubmitted()
        snackbarHostState.showSnackbar("Income saved!")
        onIncomeAdded()
    }

    // Delete pending → show undo snackbar
    LaunchedEffect(state.pendingDeleteEntry) {
        val entry = state.pendingDeleteEntry ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "${entry.source} entry deleted",
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
        viewModel.consumeError()
    }

    AddIncomeContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToIncome = onNavigateToIncome,
        onNavigateToExpense = onNavigateToExpense,
        onNavigateToTransactions = onNavigateToTransactions,
        onNavigateToGoals = onNavigateToGoals,
        onNavigateToProfile = onNavigateToProfile,
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

// ── Main content ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeContent(
    state: IncomeViewModel.IncomeUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateToDashboard: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToExpense: () -> Unit,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
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
    val density = LocalDensity.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val rateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val showExchange = state.currencyOriginal != "LKR"
    var popupOffsetY by remember { mutableStateOf(0f) }
    val dismissThresholdPx = with(density) { 120.dp.toPx() }
    val recentEntries = remember(state.entries) {
        state.entries.sortedByDescending { it.date?.seconds ?: 0L }.take(5)
    }

    if (state.showRateConfirmation) {
        AlertDialog(
            onDismissRequest = onDismissRateConfirmation,
            containerColor = GlassTheme.BgMid,
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
                    ?.let { rateFormat.format(java.util.Date(it)) } ?: "unknown"
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

    // ── Glassmorphic popup background ─────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(GlassTheme.BgStart, GlassTheme.BgMid, GlassTheme.BgEnd),
                )
            ),
    ) {
        // Background orb decorations
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlassTheme.Orange.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.04f),
                    radius = 280.dp.toPx(),
                ),
                center = Offset(size.width * 0.85f, size.height * 0.04f),
                radius = 280.dp.toPx(),
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(GlassTheme.OrbPurple, Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.18f),
                    radius = 200.dp.toPx(),
                ),
                center = Offset(size.width * 0.15f, size.height * 0.18f),
                radius = 200.dp.toPx(),
            )
        }

        // Snackbar at top
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
        )

        // ── Glass popup card ──────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .align(Alignment.BottomCenter)
                .graphicsLayer { translationY = popupOffsetY }
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        popupOffsetY = (popupOffsetY + delta).coerceAtLeast(0f)
                    },
                    onDragStopped = {
                        if (popupOffsetY > dismissThresholdPx) {
                            onNavigateToIncome()
                        } else {
                            popupOffsetY = 0f
                        }
                    },
                )
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(GlassTheme.GlassSurface)
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        listOf(GlassTheme.GlassBorder, Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                ),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                // ── Header section ────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    GlassTheme.Orange.copy(alpha = 0.10f),
                                    Color.Transparent,
                                )
                            )
                        )
                        .padding(horizontal = 20.dp)
                        .padding(top = 12.dp, bottom = 24.dp),
                ) {
                    // Drag handle
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(GlassTheme.GlassBorder),
                    )

                    Spacer(Modifier.height(16.dp))

                    // Title + close button row
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "ADD INCOME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 2.sp,
                                color = GlassTheme.TextHint,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                "Record a new entry",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = GlassTheme.TextPrimary,
                                textAlign = TextAlign.Center,
                            )
                        }
                        // Close / back button
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(GlassTheme.GlassBg)
                                .border(1.dp, GlassTheme.GlassBorder, CircleShape)
                                .clickable { onNavigateToIncome() },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = GlassTheme.TextSecondary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Amount card ───────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        GlassTheme.Orange.copy(alpha = 0.14f),
                                        GlassTheme.GlassBg,
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                GlassTheme.Orange.copy(alpha = 0.25f),
                                RoundedCornerShape(20.dp),
                            )
                            .padding(16.dp),
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                "AMOUNT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.5.sp,
                                color = GlassTheme.TextHint,
                                textAlign = TextAlign.Center,
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                // Currency dropdown pill
                                var expanded by remember { mutableStateOf(false) }
                                Box {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(GlassTheme.OrangeDim)
                                            .border(
                                                1.dp,
                                                Color(0x66FF6B00),
                                                RoundedCornerShape(20.dp),
                                            )
                                            .clickable { expanded = true }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Text(
                                            state.currencyOriginal,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GlassTheme.OrangeLight,
                                        )
                                        Icon(
                                            Icons.Rounded.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = GlassTheme.OrangeLight,
                                            modifier = Modifier.size(16.dp),
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false },
                                        containerColor = GlassTheme.BgMid,
                                    ) {
                                        IncomeViewModel.CURRENCIES.forEach { cur ->
                                            DropdownMenuItem(
                                                text = { Text(cur, color = GlassTheme.TextPrimary) },
                                                onClick = {
                                                    onCurrencyChange(cur)
                                                    expanded = false
                                                },
                                            )
                                        }
                                    }
                                }
                                IncomeBasicAmountInput(
                                    value = state.amountOriginal,
                                    onValueChange = onAmountChange,
                                )
                            }
                            // Exchange rate preview
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
                                        .border(
                                            1.dp,
                                            Color(0x40FF6B00),
                                            RoundedCornerShape(12.dp),
                                        )
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        "≈ LKR ${"%.2f".format(state.amountLkrPreview)}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GlassTheme.OrangeLight,
                                        textAlign = TextAlign.Center,
                                    )
                                    val updatedText = state.exchangeRateLastUpdatedMillis
                                        ?.let { rateFormat.format(java.util.Date(it)) } ?: "unknown"
                                    val staleSuffix = if (state.exchangeRateIsStale) " · stale" else ""
                                    Text(
                                        text = if (state.exchangeRateAvailable)
                                            "1 ${state.currencyOriginal} = LKR ${state.exchangeRate} · $updatedText$staleSuffix"
                                        else "Rate unavailable",
                                        fontSize = 11.sp,
                                        color = GlassTheme.TextHint,
                                    )
                                    TextButton(
                                        onClick = onRefreshRates,
                                        enabled = !state.isLoading && !state.isRefreshingRates,
                                    ) {
                                        if (state.isRefreshingRates) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                strokeWidth = 1.5.dp,
                                                color = GlassTheme.OrangeLight,
                                            )
                                            Spacer(Modifier.width(6.dp))
                                        }
                                        Text("Refresh rates", fontSize = 12.sp, color = GlassTheme.OrangeLight)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = GlassTheme.GlassBorder, thickness = 0.5.dp)

                // ── Scrollable form fields ────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    IncomeFormSection("Date") {
                        IncomeDateRow(
                            dateMillis = state.dateMillis,
                            dateFormat = dateFormat,
                            context = context,
                            onDateChange = onDateChange,
                        )
                    }

                    IncomeFormSection("Source") {
                        IncomeSourceSelector(
                            selected = state.source,
                            onSelected = onSourceChange,
                        )
                    }

                    IncomeFormSection("Income Type") {
                        IncomeTypeSelector(
                            selected = state.incomeType,
                            onSelected = onIncomeTypeChange,
                        )
                    }

                    IncomeFormSection("Details") {
                        IncomeTextField(
                            value = state.label,
                            onValueChange = onLabelChange,
                            placeholder = "Description or reference",
                        )
                    }

                    AnimatedVisibility(
                        visible = state.source == "Freelance",
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        IncomeFormSection("Link to Project") {
                            IncomeProjectDropdown(
                                projects = state.projects,
                                selectedId = state.projectRef,
                                onSelect = onProjectRefChange,
                            )
                        }
                    }

                    IncomeSaveButton(
                        isLoading = state.isLoading,
                        onClick = onRequestSubmit,
                    )

                    if (recentEntries.isNotEmpty()) {
                        IncomeFormSection("Recent Income") {
                            IncomeRecentHistory(entries = recentEntries)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun IncomeRecentHistory(entries: List<IncomeEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.forEach { entry ->
            IncomeRecentHistoryItem(entry = entry)
        }
    }
}

@Composable
private fun IncomeRecentHistoryItem(entry: IncomeEntry) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val accent = sourceColor(entry.source)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GlassTheme.GlassBg)
            .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(sourceIcon(entry.source), contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = entry.label?.takeIf { it.isNotBlank() } ?: entry.source,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassTheme.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.date?.toDate()?.let { dateFormat.format(it) } ?: "",
                fontSize = 11.sp,
                color = GlassTheme.TextHint,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "+${entry.currencyOriginal} ${"%.2f".format(entry.amountOriginal)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTheme.Success,
            )
            if (entry.currencyOriginal != "LKR") {
                Text(
                    text = "≈ ${formatLKR(entry.amountLKR)}",
                    fontSize = 10.sp,
                    color = GlassTheme.TextHint,
                )
            }
        }
    }
}

@Composable
private fun IncomeAmountHeader(
    state: IncomeViewModel.IncomeUiState,
    showExchange: Boolean,
    rateFormat: SimpleDateFormat,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onRefreshRates: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawCircle(
                    color = Color(0x1A10B981),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width * 0.9f, -40.dp.toPx()),
                )
                drawCircle(
                    color = Color(0x143B82F6),
                    radius = 140.dp.toPx(),
                    center = Offset(0f, size.height),
                )
            }
            .padding(top = 52.dp, bottom = 36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "NEW INCOME",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = GlassTheme.TextHint,
            )

            Spacer(Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GlassTheme.Success.copy(alpha = 0.16f))
                            .border(1.dp, GlassTheme.Success.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            state.currencyOriginal,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlassTheme.Success,
                        )
                        Icon(
                            Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            tint = GlassTheme.Success,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        containerColor = GlassTheme.BgMid,
                    ) {
                        IncomeViewModel.CURRENCIES.forEach { cur ->
                            DropdownMenuItem(
                                text = { Text(cur, color = GlassTheme.TextPrimary) },
                                onClick = { onCurrencyChange(cur); expanded = false },
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                IncomeBasicAmountInput(
                    value = state.amountOriginal,
                    onValueChange = onAmountChange,
                )
            }

            AnimatedVisibility(
                visible = showExchange,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "≈ LKR ${"%.2f".format(state.amountLkrPreview)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GlassTheme.Success,
                    )
                    val updatedText = state.exchangeRateLastUpdatedMillis
                        ?.let { rateFormat.format(java.util.Date(it)) } ?: "unknown"
                    val staleSuffix = if (state.exchangeRateIsStale) " · stale" else ""
                    Text(
                        text = if (state.exchangeRateAvailable) {
                            "1 ${state.currencyOriginal} = LKR ${state.exchangeRate} · $updatedText$staleSuffix"
                        } else "Rate unavailable",
                        fontSize = 11.sp,
                        color = GlassTheme.TextHint,
                        textAlign = TextAlign.Center,
                    )
                    TextButton(
                        onClick = onRefreshRates,
                        enabled = !state.isLoading && !state.isRefreshingRates,
                    ) {
                        if (state.isRefreshingRates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = GlassTheme.Success,
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            "Refresh rates",
                            fontSize = 12.sp,
                            color = GlassTheme.Success,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomeBasicAmountInput(value: String, onValueChange: (String) -> Unit) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontSize = 56.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GlassTheme.TextPrimary,
            textAlign = TextAlign.Start,
            letterSpacing = (-2).sp,
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        decorationBox = { innerField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        "0.00",
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GlassTheme.TextHint,
                        letterSpacing = (-2).sp,
                    )
                }
                innerField()
            }
        }
    )
}

@Composable
private fun IncomeFormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            title.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.5.sp,
            color = GlassTheme.TextHint,
        )
        content()
    }
}

@Composable
private fun IncomeDateRow(
    dateMillis: Long,
    dateFormat: SimpleDateFormat,
    context: android.content.Context,
    onDateChange: (Long) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(GlassTheme.GlassBg)
            .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(16.dp))
            .clickable {
                val cal = Calendar.getInstance().also { it.timeInMillis = dateMillis }
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
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GlassTheme.Success.copy(alpha = 0.16f))
                .border(1.dp, GlassTheme.Success.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.CalendarToday,
                contentDescription = null,
                tint = GlassTheme.Success,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Selected date", fontSize = 10.sp, color = GlassTheme.TextHint)
            Text(
                dateFormat.format(java.util.Date(dateMillis)),
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassTheme.TextPrimary,
            )
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = GlassTheme.TextHint,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun IncomeSourceSelector(selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IncomeViewModel.SOURCES.forEach { src ->
            val accent = sourceColor(src)
            IncomePill(
                text = src,
                icon = sourceIcon(src),
                isSelected = selected == src,
                accentColor = accent,
                onClick = { onSelected(src) },
            )
        }
    }
}

@Composable
private fun IncomeTypeSelector(selected: String, onSelected: (String) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        IncomeViewModel.INCOME_TYPES.forEach { type ->
            IncomePill(
                text = type,
                icon = Icons.Rounded.Loop,
                isSelected = selected == type,
                accentColor = GlassTheme.Success,
                onClick = { onSelected(type) },
            )
        }
    }
}

@Composable
private fun IncomePill(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val bg by animateColorAsState(
        if (isSelected) accentColor else GlassTheme.GlassBg,
        tween(220),
        label = "pill_bg_$text",
    )
    val contentColor by animateColorAsState(
        if (isSelected) Color.White else GlassTheme.TextSecondary,
        tween(220),
        label = "pill_fg_$text",
    )
    val borderColor by animateColorAsState(
        if (isSelected) accentColor.copy(alpha = 0f) else GlassTheme.GlassBorder,
        tween(220),
        label = "pill_border_$text",
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(14.dp),
        )
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
        )
    }
}

@Composable
private fun IncomeTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        placeholder = {
            Text(placeholder, fontSize = 13.sp, color = GlassTheme.TextHint)
        },
        textStyle = TextStyle(fontSize = 14.sp, color = GlassTheme.TextPrimary),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GlassTheme.Success,
            unfocusedBorderColor = GlassTheme.GlassBorder,
            focusedContainerColor = GlassTheme.GlassSurface,
            unfocusedContainerColor = GlassTheme.GlassBg,
            cursorColor = GlassTheme.Success,
        ),
    )
}

@Composable
private fun IncomeProjectDropdown(
    projects: List<FreelanceProject>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = projects.firstOrNull { it.id == selectedId }
    val label = selected?.let { "${it.projectTitle} — ${it.clientName}" }
        ?: if (projects.isEmpty()) "No projects" else "Select project"

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(GlassTheme.GlassBg)
                .border(1.dp, GlassTheme.GlassBorder, RoundedCornerShape(16.dp))
                .clickable(enabled = projects.isNotEmpty()) { expanded = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Rounded.Work, null, tint = GlassTheme.Success, modifier = Modifier.size(18.dp))
                Text(
                    label,
                    fontSize = 14.sp,
                    color = if (selected != null) GlassTheme.TextPrimary else GlassTheme.TextHint,
                )
            }
            Icon(Icons.Rounded.ArrowDropDown, null, tint = GlassTheme.TextHint)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = GlassTheme.BgMid,
        ) {
            DropdownMenuItem(text = { Text("None", color = GlassTheme.TextPrimary) }, onClick = { onSelect(""); expanded = false })
            projects.forEach { p ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(p.projectTitle, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = GlassTheme.TextPrimary)
                            Text(p.clientName, fontSize = 12.sp, color = GlassTheme.TextHint)
                        }
                    },
                    onClick = { onSelect(p.id); expanded = false },
                )
            }
        }
    }
}

@Composable
private fun IncomeSaveButton(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GlassTheme.Success,
            contentColor = Color.White,
            disabledContainerColor = GlassTheme.Success.copy(alpha = 0.4f),
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = Color.White,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Save Income", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ── Section 1: Total Income Header ───────────────────────────────────────────

@Composable
private fun TotalIncomeHeaderCard(
    totalIncome: Double,
    showMonthly: Boolean,
    onToggleView: () -> Unit,
    sourceCount: Int,
    entryCount: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(listOf(Orange, Color(0xFFFF8C00), Color(0xFFF9FAFB))),
            )
            .padding(top = 48.dp, bottom = 36.dp, start = 24.dp, end = 24.dp),
    ) {
        // Decorative circles
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(Color.White.copy(alpha = 0.07f), 160.dp.toPx(), Offset(size.width * 0.9f, 0f))
            drawCircle(Color.White.copy(alpha = 0.05f), 100.dp.toPx(), Offset(size.width * 0.05f, size.height * 0.6f))
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Monthly / All-time toggle
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                TogglePill("Monthly", showMonthly, onToggleView)
                TogglePill("All time", !showMonthly, onToggleView)
            }

            // Total label
            Text(
                text = "Total Income",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 0.5.sp,
            )

            // Amount (no count-up on re-compose, just display)
            Text(
                text = formatLKRFull(totalIncome),
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )

            // Badges row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IncomeBadge("$sourceCount sources")
                IncomeBadge("$entryCount entries")
            }
        }
    }
}

@Composable
private fun TogglePill(label: String, active: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        if (active) Color.White else Color.Transparent,
        animationSpec = tween(200),
        label = "toggle_pill",
    )
    val textColor by animateColorAsState(
        if (active) Orange else Color.White.copy(alpha = 0.8f),
        animationSpec = tween(200),
        label = "toggle_text",
    )
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
    }
}

@Composable
private fun IncomeBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ── Section 2: Stacked Bar ────────────────────────────────────────────────────

@Composable
private fun IncomeStackedBarSection(
    bySource: Map<String, Double>,
    modifier: Modifier = Modifier,
) {
    if (bySource.isEmpty()) return
    val total = bySource.values.sum().coerceAtLeast(1.0)
    val sorted = bySource.entries.sortedByDescending { it.value }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text("Income by Source", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF374151))

        // Segmented bar — draw with Canvas for type-safe sizing
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp)),
        ) {
            var xOffset = 0f
            sorted.forEach { (src, amt) ->
                val segW = (amt / total).toFloat().coerceAtLeast(0.01f) * size.width
                drawRect(color = sourceColor(src), topLeft = Offset(xOffset, 0f), size = androidx.compose.ui.geometry.Size(segW, size.height))
                xOffset += segW
            }
        }

        // Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            sorted.forEach { (src, amt) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(sourceColor(src)))
                    Text(
                        text = "$src ${(amt / total * 100).roundToInt()}%",
                        fontSize = 10.sp,
                        color = Color(0xFF6B7280),
                    )
                }
            }
        }
    }
}

// ── Section 3: Income Source Group Card ──────────────────────────────────────

@Composable
private fun IncomeSourceGroupCard(
    source: String,
    entries: List<IncomeEntry>,
    projects: List<FreelanceProject>,
    onDeleteEntry: (IncomeEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val color = sourceColor(source)
    val totalLkr = entries.sumOf { it.amountLKR }
    val monthlyTrend = computeMonthlyTrend(entries)
    var expanded by rememberSaveable(source) { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(0.5.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp)),
    ) {
        // Left-color accent bar + header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(androidx.compose.ui.unit.Dp.Unspecified),
        ) {
            // Left colored border
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
                    .background(color, RoundedCornerShape(topStart = 20.dp, bottomStart = 0.dp)),
            )
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Icon circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(color.copy(alpha = 0.12f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(sourceIcon(source), null, tint = color, modifier = Modifier.size(22.dp))
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(source, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937))
                    Text(
                        "${entries.size} entries",
                        fontSize = 12.sp,
                        color = Color(0xFF9CA3AF),
                    )
                }

                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        formatLKRFull(totalLkr),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                    )
                    Icon(
                        if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        null,
                        tint = Color(0xFF9CA3AF),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }

        // Source-specific chips + sparkline (always visible)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 16.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Source-specific chips
                Row(
                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    sourceChips(source, entries, projects).forEach { (label, chipColor) ->
                        SourceInfoChip(label = label, color = chipColor)
                    }
                }

                // Sparkline
                if (monthlyTrend.any { it > 0f }) {
                    SparklineChart(
                        data = monthlyTrend,
                        color = color,
                        modifier = Modifier.width(80.dp).height(32.dp),
                    )
                }
            }
        }

        // Expandable entries list
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.5.dp)
                val sorted = entries.sortedByDescending { it.date?.seconds ?: 0L }
                sorted.forEach { entry ->
                    SwipeableEntryRow(
                        entry = entry,
                        onDelete = { onDeleteEntry(entry) },
                    )
                    HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun SourceInfoChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}

private fun sourceChips(
    source: String,
    entries: List<IncomeEntry>,
    projects: List<FreelanceProject>,
): List<Pair<String, Color>> {
    val chips = mutableListOf<Pair<String, Color>>()
    when {
        source.equals("Salary", ignoreCase = true) -> {
            val recurring = entries.filter { it.type == "RECURRING" }
            if (recurring.isNotEmpty()) chips += "Recurring" to Color(0xFFFF6B00)
            val day = entries.firstNotNullOfOrNull { e ->
                e.date?.toDate()?.let { d ->
                    Calendar.getInstance().apply { time = d }.get(Calendar.DAY_OF_MONTH)
                }
            }
            if (day != null) chips += "Received ${day}th" to Color(0xFF6B7280)
        }
        source.equals("Freelance", ignoreCase = true) -> {
            chips += "Freelance" to Color(0xFF2196F3)
            val hasOutstanding = projects.any { p ->
                p.status == "OPEN" && p.paidAmount < p.agreedAmount
            }
            if (hasOutstanding) chips += "⚠ Invoice pending" to Color(0xFFF59E0B)
        }
        source.equals("AdSense", ignoreCase = true) -> {
            chips += "AdSense" to Color(0xFF4CAF50)
            val hasUsd = entries.any { it.currencyOriginal == "USD" }
            if (hasUsd) chips += "USD → LKR" to Color(0xFF2196F3)
        }
        source.equals("Crypto", ignoreCase = true) -> {
            chips += "Crypto" to Color(0xFF9C27B0)
            val net = entries.sumOf { it.amountLKR }
            chips += if (net >= 0) "Net P&L ↑" to Color(0xFF4CAF50) else "Net P&L ↓" to Color(0xFFEF4444)
        }
        else -> chips += source to Color(0xFF6B7280)
    }
    return chips
}

// ── Sparkline chart ───────────────────────────────────────────────────────────

@Composable
private fun SparklineChart(
    data: List<Float>,
    color: Color,
    modifier: Modifier = Modifier,
) {
    if (data.size < 2) return
    Canvas(modifier = modifier) {
        val maxVal = data.max().coerceAtLeast(1f)
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()
        data.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - (v / maxVal) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        // Fill under line
        val fillPath = Path().apply {
            addPath(path)
            lineTo((data.size - 1) * stepX, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(fillPath, Brush.verticalGradient(listOf(color.copy(alpha = 0.25f), Color.Transparent)))
        drawPath(path, color = color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
    }
}

private fun computeMonthlyTrend(entries: List<IncomeEntry>): List<Float> {
    val cal = Calendar.getInstance()
    return (5 downTo 0).map { monthsBack ->
        val c = cal.clone() as Calendar
        c.add(Calendar.MONTH, -monthsBack)
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        entries.filter { e ->
            val d = e.date?.toDate() ?: return@filter false
            val ec = Calendar.getInstance().apply { time = d }
            ec.get(Calendar.YEAR) == year && ec.get(Calendar.MONTH) == month
        }.sumOf { it.amountLKR }.toFloat()
    }
}

// ── Swipeable Entry Row ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableEntryRow(
    entry: IncomeEntry,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        },
        positionalThreshold = { it * 0.45f },
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEF4444))
                    .padding(end = 20.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Delete, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Text("Delete", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        },
    ) {
        EntryRow(entry = entry)
    }
}

@Composable
private fun EntryRow(entry: IncomeEntry) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Type badge
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(sourceColor(entry.source).copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                sourceIcon(entry.source),
                null,
                tint = sourceColor(entry.source),
                modifier = Modifier.size(18.dp),
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = entry.label?.ifBlank { null } ?: entry.source,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1F2937),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = entry.date?.toDate()?.let { dateFormat.format(it) } ?: "",
                fontSize = 11.sp,
                color = Color(0xFF9CA3AF),
            )
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "+${entry.currencyOriginal} ${"%.2f".format(entry.amountOriginal)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981),
            )
            if (entry.currencyOriginal != "LKR") {
                Text(
                    "≈ ${formatLKR(entry.amountLKR)}",
                    fontSize = 10.sp,
                    color = Color(0xFF9CA3AF),
                )
            }
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyIncomeState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // SVG-style canvas illustration
        Canvas(modifier = Modifier.size(120.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2
            // Outer circle
            drawCircle(Color(0xFFFFF3E0), radius = size.minDimension / 2)
            // Coin stack — 3 horizontal ellipses
            val coinColor = Color(0xFFFF6B00)
            listOf(0f, 14f, 28f).forEach { yOffset ->
                drawOval(
                    color = coinColor.copy(alpha = 0.8f - yOffset * 0.01f),
                    topLeft = Offset(cx - 26.dp.toPx(), cy - 8.dp.toPx() - yOffset.dp.toPx()),
                    size = androidx.compose.ui.geometry.Size(52.dp.toPx(), 16.dp.toPx()),
                )
            }
            // Chart line above coins
            val path = Path().apply {
                moveTo(cx - 30.dp.toPx(), cy - 48.dp.toPx())
                lineTo(cx - 10.dp.toPx(), cy - 58.dp.toPx())
                lineTo(cx + 10.dp.toPx(), cy - 48.dp.toPx())
                lineTo(cx + 30.dp.toPx(), cy - 62.dp.toPx())
            }
            drawPath(path, color = coinColor, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
        }

        Text(
            "No income recorded yet",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1F2937),
            textAlign = TextAlign.Center,
        )
        Text(
            "Add your first entry to start tracking your earnings.",
            fontSize = 14.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )
        Button(
            onClick = onAddClick,
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(14.dp),
        ) {
            Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Income", fontWeight = FontWeight.Bold)
        }
    }
}

// ── Shimmer ───────────────────────────────────────────────────────────────────

@Composable
private fun ShimmerIncomeCard(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_x",
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(Color(0xFFE0E0E0), Color(0xFFF5F5F5), Color(0xFFE0E0E0)),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 300f),
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(shimmerBrush))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.5f).height(14.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
            Box(modifier = Modifier.fillMaxWidth(0.3f).height(11.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
        }
        Box(modifier = Modifier.width(60.dp).height(16.dp).clip(RoundedCornerShape(4.dp)).background(shimmerBrush))
    }
}

// ── Add Income Form (Bottom Sheet content) ────────────────────────────────────

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
    onRefreshRates: () -> Unit,
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val rateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val showExchangeInfo = state.currencyOriginal != "LKR"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            "Add Income",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF1F2937),
            modifier = Modifier.padding(top = 4.dp),
        )

        // ── Source selector ────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetSectionLabel("Income Source")
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IncomeViewModel.SOURCES.forEach { src ->
                    val selected = state.source == src
                    val bg by animateColorAsState(
                        if (selected) sourceColor(src) else Color(0xFFF3F4F6),
                        label = "src_chip",
                    )
                    val tc by animateColorAsState(
                        if (selected) Color.White else Color(0xFF6B7280),
                        label = "src_chip_text",
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bg)
                            .clickable { onSourceChange(src) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Text(src, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = tc)
                    }
                }
            }
        }

        // ── Amount + Currency ──────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetSectionLabel("Amount")
            OutlinedTextField(
                value = state.amountOriginal,
                onValueChange = onAmountChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F2937)),
                placeholder = {
                    Text(
                        "0.00",
                        style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD1D5DB)),
                    )
                },
                leadingIcon = {
                    var expanded by remember { mutableStateOf(false) }
                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { expanded = true }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                state.currencyOriginal,
                                fontWeight = FontWeight.Bold,
                                color = Orange,
                                fontSize = 15.sp,
                            )
                            Icon(Icons.Rounded.ArrowDropDown, null, tint = Orange, modifier = Modifier.size(18.dp))
                        }
                        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            IncomeViewModel.CURRENCIES.forEach { cur ->
                                DropdownMenuItem(
                                    text = { Text(cur) },
                                    onClick = { onCurrencyChange(cur); expanded = false },
                                )
                            }
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                ),
            )

            AnimatedVisibility(
                visible = showExchangeInfo,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "≈ LKR ${"%.2f".format(state.amountLkrPreview)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Orange,
                    )
                    val updatedText = state.exchangeRateLastUpdatedMillis
                        ?.let { rateFormat.format(java.util.Date(it)) } ?: "unknown"
                    Text(
                        "Rate: 1 ${state.currencyOriginal} = LKR ${state.exchangeRate} · $updatedText" +
                            if (state.exchangeRateIsStale) " (stale)" else "",
                        fontSize = 11.sp,
                        color = Color(0xFF6B7280),
                    )
                    TextButton(
                        onClick = onRefreshRates,
                        enabled = !state.isRefreshingRates,
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        if (state.isRefreshingRates) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.5.dp,
                                color = Orange,
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text("Refresh rates", fontSize = 12.sp, color = Orange)
                    }
                }
            }
        }

        // ── Date picker ────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetSectionLabel("Date")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF9FAFB))
                    .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = state.dateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, day ->
                                val selected = Calendar.getInstance().apply {
                                    set(year, month, day, 0, 0, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onDateChange(selected.timeInMillis)
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH),
                        ).show()
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(Icons.Rounded.CalendarToday, null, tint = Orange, modifier = Modifier.size(20.dp))
                Text(
                    dateFormat.format(java.util.Date(state.dateMillis)),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1F2937),
                )
            }
        }

        // ── Income Type ────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetSectionLabel("Income Type")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IncomeViewModel.INCOME_TYPES.forEach { type ->
                    val sel = state.incomeType == type
                    val bg by animateColorAsState(if (sel) Orange else Color(0xFFF3F4F6), label = "type_bg")
                    val tc by animateColorAsState(if (sel) Color.White else Color(0xFF6B7280), label = "type_tc")
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(bg)
                            .clickable { onIncomeTypeChange(type) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(type, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = tc, textAlign = TextAlign.Center)
                    }
                }
            }
        }

        // ── Note ──────────────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SheetSectionLabel("Note (optional)")
            OutlinedTextField(
                value = state.label,
                onValueChange = onLabelChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Description or reference", color = Color(0xFFD1D5DB)) },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Orange,
                    unfocusedBorderColor = Color(0xFFE5E7EB),
                ),
            )
        }

        // ── Freelance project link ─────────────────────────────────────────
        AnimatedVisibility(
            visible = state.source == "Freelance",
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically(),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SheetSectionLabel("Link to Project (optional)")
                ProjectDropdown(
                    projects = state.projects,
                    selectedId = state.projectRef,
                    onSelect = onProjectRefChange,
                )
            }
        }

        // ── Submit button ─────────────────────────────────────────────────
        Button(
            onClick = onRequestSubmit,
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange, disabledContainerColor = Orange.copy(alpha = 0.5f)),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(strokeWidth = 2.dp, color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Save Income", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SheetSectionLabel(text: String) {
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B7280))
}

@Composable
private fun ProjectDropdown(
    projects: List<FreelanceProject>,
    selectedId: String,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = projects.firstOrNull { it.id == selectedId }
    val label = selected?.let { "${it.projectTitle} — ${it.clientName}" }
        ?: if (projects.isEmpty()) "No projects" else "Select project"

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFF9FAFB))
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(14.dp))
                .clickable(enabled = projects.isNotEmpty()) { expanded = true }
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(Icons.Rounded.Work, null, tint = Orange, modifier = Modifier.size(18.dp))
                Text(
                    label,
                    fontSize = 14.sp,
                    color = if (selected != null) Color(0xFF1F2937) else Color(0xFFD1D5DB),
                )
            }
            Icon(Icons.Rounded.ArrowDropDown, null, tint = Color(0xFF9CA3AF))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text("None") }, onClick = { onSelect(""); expanded = false })
            projects.forEach { p ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(p.projectTitle, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(p.clientName, fontSize = 12.sp, color = Color(0xFF9CA3AF))
                        }
                    },
                    onClick = { onSelect(p.id); expanded = false },
                )
            }
        }
    }
}

