package com.bpeople.finpilot.ui.screens.expense

import android.app.DatePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.data.model.ExpenseEntry
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.GlassTheme
import com.bpeople.finpilot.ui.components.LocalCurrentGlassTheme
import com.bpeople.finpilot.ui.components.NavTab
import java.text.SimpleDateFormat
import java.util.*

// ── Entry point ───────────────────────────────────────────────────────────────

@Composable
fun AddExpenseScreen(
    viewModel: ExpenseViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToIncome: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onExpenseAdded: (String) -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val state by viewModel.expenseState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }
    LaunchedEffect(state.insightMessage) {
        val insight = state.insightMessage ?: return@LaunchedEffect
        viewModel.consumeInsight()
        onExpenseAdded(insight)
    }

    AddExpenseContent(
        state = state,
        snackbarHostState = snackbarHostState,
        onNavigateToDashboard = onNavigateToDashboard,
        onNavigateToIncome = onNavigateToIncome,
        onNavigateToTransactions = onNavigateToTransactions,
        onNavigateToGoals = onNavigateToGoals,
        onNavigateToProfile = onNavigateToProfile,
        onAmountChange = viewModel::onAmountChange,
        onCurrencyChange = viewModel::onCurrencyChange,
        onCategoryChange = viewModel::onCategoryChange,
        onPaymentMethodChange = viewModel::onPaymentMethodChange,
        onDateChange = viewModel::onDateChange,
        onSubCategoryChange = viewModel::onSubCategoryChange,
        onNoteChange = viewModel::onNoteChange,
        onRecurringChange = viewModel::onRecurringChange,
        onHistoryDateRangeChange = viewModel::onHistoryDateRangeChange,
        onHistoryCategoryFilterChange = viewModel::onHistoryCategoryFilterChange,
        onHistoryPaymentMethodFilterChange = viewModel::onHistoryPaymentMethodFilterChange,
        onClearHistoryFilters = viewModel::clearHistoryFilters,
        onRequestSubmit = viewModel::requestSubmit,
        onConfirmExchangeRate = viewModel::confirmExchangeRate,
        onDismissRateConfirmation = viewModel::dismissRateConfirmation,
        onRefreshRates = viewModel::refreshExchangeRates,
    )
}

// ── Content ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseContent(
    state: ExpenseViewModel.ExpenseUiState,
    snackbarHostState: SnackbarHostState,
    onNavigateToDashboard: () -> Unit,
    onNavigateToIncome: () -> Unit = {},
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPaymentMethodChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onSubCategoryChange: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onRecurringChange: (Boolean) -> Unit,
    onHistoryDateRangeChange: (ExpenseViewModel.HistoryDateRange) -> Unit,
    onHistoryCategoryFilterChange: (String?) -> Unit,
    onHistoryPaymentMethodFilterChange: (String?) -> Unit,
    onClearHistoryFilters: () -> Unit,
    onRequestSubmit: () -> Unit,
    onConfirmExchangeRate: () -> Unit,
    onDismissRateConfirmation: () -> Unit,
    onRefreshRates: () -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val rateFormat = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val showExchangeRate = state.currency != "LKR"

    val sheetSurfaceVariantColor = GlassTheme.GlassBg
    val sheetTextPrimary = GlassTheme.TextPrimary
    val sheetTextSecondary = GlassTheme.TextSecondary

    val fieldColors = OutlinedTextFieldDefaults.colors(
        unfocusedContainerColor = sheetSurfaceVariantColor,
        focusedContainerColor = sheetSurfaceVariantColor,
        unfocusedBorderColor = Color.Transparent,
        focusedBorderColor = GlassTheme.Orange,
        unfocusedTextColor = sheetTextPrimary,
        focusedTextColor = sheetTextPrimary,
        unfocusedLabelColor = sheetTextSecondary,
        focusedLabelColor = GlassTheme.Orange,
    )

    // Exchange rate confirmation dialog
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
                    ?.let { rateFormat.format(Date(it)) } ?: "unknown"
                Text(
                    "Use 1 ${state.currency} = LKR ${state.exchangeRate} (updated $updatedText)?",
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GlassTheme.BgMid),
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(sheetTextSecondary.copy(alpha = 0.3f), RoundedCornerShape(2.dp)),
            )

            // Title
            Text(
                "Add Expense",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = sheetTextPrimary,
            )

            // Error message
            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f))
                        .padding(12.dp),
                )
            }

            // Currency selector + amount
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
                            .background(sheetSurfaceVariantColor)
                            .clickable { currencyExpanded = true }
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                    ) {
                        Text(
                            state.currency,
                            color = GlassTheme.Orange,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                    DropdownMenu(
                        expanded = currencyExpanded,
                        onDismissRequest = { currencyExpanded = false },
                        containerColor = GlassTheme.BgMid,
                    ) {
                        ExpenseViewModel.CURRENCIES.forEach { cur ->
                            DropdownMenuItem(
                                text = { Text(cur, color = GlassTheme.TextPrimary) },
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

            // Exchange rate info
            AnimatedVisibility(
                visible = showExchangeRate,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(sheetSurfaceVariantColor)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        "≈ LKR ${"%.2f".format(state.amountLkrPreview)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlassTheme.Orange,
                    )
                    val updatedText = state.exchangeRateLastUpdatedMillis
                        ?.let { rateFormat.format(Date(it)) } ?: "unknown"
                    val staleSuffix = if (state.exchangeRateIsStale) " · stale" else ""
                    Text(
                        text = if (state.exchangeRateAvailable)
                            "1 ${state.currency} = LKR ${state.exchangeRate} · $updatedText$staleSuffix"
                        else "Rate unavailable",
                        fontSize = 11.sp,
                        color = sheetTextSecondary,
                    )
                    TextButton(
                        onClick = onRefreshRates,
                        enabled = !state.isLoading && !state.isRefreshingRates,
                    ) {
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

            // Date picker row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(sheetSurfaceVariantColor)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = sheetTextSecondary,
                    )
                    Text(
                        dateFormat.format(Date(state.dateMillis)),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = sheetTextPrimary,
                    )
                }
                TextButton(
                    onClick = {
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
                color = sheetTextSecondary,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Food", "Transport", "Housing", "Subscriptions", "Entertainment", "Health", "Other").forEach { name ->
                    val isSelected = state.category == name
                    val bg by animateColorAsState(
                        if (isSelected) GlassTheme.categoryColor(name) else sheetSurfaceVariantColor,
                        tween(220),
                        label = "cat_bg_$name",
                    )
                    val contentColor by animateColorAsState(
                        if (isSelected) Color.White else sheetTextSecondary,
                        tween(220),
                        label = "cat_fg_$name",
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
                color = sheetTextSecondary,
            )
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listOf("Card", "Cash", "Bank Transfer", "Auto-Debit").forEach { name ->
                    val isSelected = state.paymentMethod == name
                    val bg by animateColorAsState(
                        if (isSelected) GlassTheme.Orange else sheetSurfaceVariantColor,
                        tween(220),
                        label = "pay_bg_$name",
                    )
                    val contentColor by animateColorAsState(
                        if (isSelected) Color.White else sheetTextSecondary,
                        tween(220),
                        label = "pay_fg_$name",
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

            // Merchant / sub-category
            OutlinedTextField(
                value = state.subCategory,
                onValueChange = onSubCategoryChange,
                label = { Text("Merchant / Sub-category") },
                placeholder = { Text("e.g. UberEats, Gym") },
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

            // Recurring toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(sheetSurfaceVariantColor)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Recurring / Auto-debit",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = sheetTextPrimary,
                    )
                    Text(
                        "Marks this as a recurring charge",
                        fontSize = 11.sp,
                        color = sheetTextSecondary,
                    )
                }
                Switch(
                    checked = state.isRecurring,
                    onCheckedChange = onRecurringChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GlassTheme.Orange,
                        uncheckedThumbColor = sheetTextSecondary,
                        uncheckedTrackColor = GlassTheme.GlassBorder,
                    ),
                )
            }

            // Save button
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
                    Text("Save Expense", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            if (state.entries.isNotEmpty()) {
                GlassExpenseHistory(
                    state = state,
                    onHistoryDateRangeChange = onHistoryDateRangeChange,
                    onHistoryCategoryFilterChange = onHistoryCategoryFilterChange,
                    onHistoryPaymentMethodFilterChange = onHistoryPaymentMethodFilterChange,
                    onClearHistoryFilters = onClearHistoryFilters,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Amount header ─────────────────────────────────────────────────────────────

@Composable
private fun GlassAmountHeader(
    state: ExpenseViewModel.ExpenseUiState,
    showExchangeRate: Boolean,
    rateFormat: SimpleDateFormat,
    onAmountChange: (String) -> Unit,
    onCurrencyChange: (String) -> Unit,
    onRefreshRates: () -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                // Orange orb top-right
                drawCircle(
                    color = Color(0x2EFF6B00),
                    radius = 200.dp.toPx(),
                    center = Offset(size.width * 0.9f, -40.dp.toPx()),
                )
                // Purple orb bottom-left
                drawCircle(
                    color = Color(0x1A534AB7),
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
                text = "NEW EXPENSE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp,
                color = GlassTheme.TextHint,
            )

            Spacer(Modifier.height(4.dp))

            // Currency selector + amount field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Currency dropdown pill
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GlassTheme.OrangeDim)
                            .border(1.dp, Color(0x66FF6B00), RoundedCornerShape(20.dp))
                            .clickable { expanded = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            state.currency,
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
                        ExpenseViewModel.CURRENCIES.forEach { cur ->
                            DropdownMenuItem(
                                text = { Text(cur, color = GlassTheme.TextPrimary) },
                                onClick = { onCurrencyChange(cur); expanded = false },
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Bare amount input – huge, centered
                BasicAmountInput(
                    value = state.amount,
                    onValueChange = onAmountChange,
                )
            }

            // Exchange rate preview
            AnimatedVisibility(
                visible = showExchangeRate,
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
                        color = GlassTheme.OrangeLight,
                    )
                    val updatedText = state.exchangeRateLastUpdatedMillis
                        ?.let { rateFormat.format(Date(it)) } ?: "unknown"
                    val staleSuffix = if (state.exchangeRateIsStale) " · stale" else ""
                    Text(
                        text = if (state.exchangeRateAvailable) {
                            "1 ${state.currency} = LKR ${state.exchangeRate} · $updatedText$staleSuffix"
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
                                color = GlassTheme.OrangeLight,
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            "Refresh rates",
                            fontSize = 12.sp,
                            color = GlassTheme.OrangeLight,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BasicAmountInput(value: String, onValueChange: (String) -> Unit) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    // We use a BasicTextField for the zero-chrome look
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

// ── Form helpers ──────────────────────────────────────────────────────────────

@Composable
private fun GlassFormSection(
    title: String,
    content: @Composable () -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
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
private fun GlassDateRow(
    dateMillis: Long,
    dateFormat: SimpleDateFormat,
    context: android.content.Context,
    onDateChange: (Long) -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
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
        // Icon circle
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GlassTheme.OrangeDim)
                .border(1.dp, Color(0x40FF6B00), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.CalendarToday,
                contentDescription = null,
                tint = GlassTheme.OrangeLight,
                modifier = Modifier.size(16.dp),
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text("Selected date", fontSize = 10.sp, color = GlassTheme.TextHint)
            Text(
                dateFormat.format(Date(dateMillis)),
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
private fun GlassCategorySelector(selected: String, onSelected: (String) -> Unit) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val cats = listOf(
        "Food" to Icons.Rounded.Restaurant,
        "Transport" to Icons.Rounded.DirectionsCar,
        "Housing" to Icons.Rounded.Home,
        "Subscriptions" to Icons.Rounded.Subscriptions,
        "Entertainment" to Icons.Rounded.Movie,
        "Health" to Icons.Rounded.Favorite,
        "Other" to Icons.Rounded.Category,
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        cats.forEach { (name, icon) ->
            GlassPill(
                text = name,
                icon = icon,
                isSelected = selected == name,
                accentColor = GlassTheme.categoryColor(name),
                onClick = { onSelected(name) },
            )
        }
    }
}

@Composable
private fun GlassPaymentSelector(selected: String, onSelected: (String) -> Unit) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val methods = listOf(
        "Card" to Icons.Rounded.CreditCard,
        "Cash" to Icons.Rounded.Money,
        "Bank Transfer" to Icons.Rounded.AccountBalance,
        "Auto-Debit" to Icons.Rounded.Autorenew,
    )
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        methods.forEach { (name, icon) ->
            GlassPill(
                text = name,
                icon = icon,
                isSelected = selected == name,
                accentColor = GlassTheme.Orange,
                onClick = { onSelected(name) },
            )
        }
    }
}

@Composable
fun GlassPill(
    text: String,
    icon: ImageVector? = null,
    isSelected: Boolean,
    accentColor: Color = GlassTheme.Orange,
    onClick: () -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val bg by animateColorAsState(
        if (isSelected) accentColor else GlassTheme.GlassBg,
        tween(220),
        label = "pill_bg",
    )
    val contentColor by animateColorAsState(
        if (isSelected) Color.White else GlassTheme.TextSecondary,
        tween(220),
        label = "pill_fg",
    )
    val borderColor by animateColorAsState(
        if (isSelected) accentColor.copy(alpha = 0f) else GlassTheme.GlassBorder,
        tween(220),
        label = "pill_border",
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
        if (icon != null) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor,
        )
    }
}

@Composable
private fun GlassTextField(value: String, onValueChange: (String) -> Unit, placeholder: String) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
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
            focusedBorderColor = GlassTheme.Orange,
            unfocusedBorderColor = GlassTheme.GlassBorder,
            focusedContainerColor = GlassTheme.GlassSurface,
            unfocusedContainerColor = GlassTheme.GlassBg,
            cursorColor = GlassTheme.Orange,
        ),
    )
}

@Composable
fun GlassRecurringRow(isRecurring: Boolean, onRecurringChange: (Boolean) -> Unit) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val bg by animateColorAsState(
        if (isRecurring) Color(0x1AFF6B00) else GlassTheme.GlassBg,
        tween(250),
        label = "recurring_bg",
    )
    val borderColor by animateColorAsState(
        if (isRecurring) Color(0x66FF6B00) else GlassTheme.GlassBorder,
        tween(250),
        label = "recurring_border",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isRecurring) GlassTheme.OrangeDim else GlassTheme.GlassBg),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Rounded.Autorenew,
                    contentDescription = null,
                    tint = if (isRecurring) GlassTheme.OrangeLight else GlassTheme.TextHint,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column {
                Text(
                    "Recurring / Auto-debit",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isRecurring) GlassTheme.OrangeLight else GlassTheme.TextPrimary,
                )
                Text(
                    "Marks this as a recurring charge",
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
fun GlassSaveButton(isLoading: Boolean, onClick: () -> Unit) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = GlassTheme.Orange,
            contentColor = Color.White,
            disabledContainerColor = GlassTheme.Orange.copy(alpha = 0.4f),
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
            Text("Save Expense", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

// ── History section ───────────────────────────────────────────────────────────

@Composable
private fun GlassExpenseHistory(
    state: ExpenseViewModel.ExpenseUiState,
    onHistoryDateRangeChange: (ExpenseViewModel.HistoryDateRange) -> Unit,
    onHistoryCategoryFilterChange: (String?) -> Unit,
    onHistoryPaymentMethodFilterChange: (String?) -> Unit,
    onClearHistoryFilters: () -> Unit,
) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val pageSize = 10
    var historyPage by remember { mutableStateOf(0) }
    val sorted = state.filteredEntries.sortedByDescending { it.date?.seconds ?: 0L }
    val totalPages = if (sorted.isEmpty()) 0 else (sorted.size + pageSize - 1) / pageSize
    val current = if (totalPages == 0) 0 else historyPage.coerceIn(0, totalPages - 1)
    val paged = sorted.drop(current * pageSize).take(pageSize)
    val hasFilters = state.historyDateRange != ExpenseViewModel.HistoryDateRange.ALL_TIME ||
            !state.historyCategoryFilter.isNullOrBlank() ||
            !state.historyPaymentMethodFilter.isNullOrBlank()

    HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = GlassTheme.GlassBorder,
        thickness = 0.5.dp,
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Expense History",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = GlassTheme.TextPrimary,
        )
        Text(
            if (hasFilters) "${sorted.size} of ${state.entries.size}" else "${sorted.size} records",
            fontSize = 11.sp,
            color = GlassTheme.TextHint,
        )
    }

    // Date range filters
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ExpenseViewModel.HistoryDateRange.entries.forEach { range ->
            GlassPill(
                text = range.label,
                isSelected = state.historyDateRange == range,
                onClick = { onHistoryDateRangeChange(range) },
            )
        }
    }

    // Entries
    if (paged.isEmpty()) {
        Text(
            "No expenses match the current filters.",
            fontSize = 13.sp,
            color = GlassTheme.TextHint,
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            paged.forEach { entry -> GlassExpenseHistoryItem(entry) }
        }
    }

    // Pagination
    if (totalPages > 0) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(
                onClick = { historyPage = (current - 1).coerceAtLeast(0) },
                enabled = current > 0,
            ) { Text("← Prev", color = GlassTheme.OrangeLight) }
            Text(
                "Page ${current + 1} of $totalPages",
                fontSize = 11.sp,
                color = GlassTheme.TextHint,
            )
            TextButton(
                onClick = { historyPage = (current + 1).coerceAtMost(totalPages - 1) },
                enabled = current < totalPages - 1,
            ) { Text("Next →", color = GlassTheme.OrangeLight) }
        }
    }
}

@Composable
private fun GlassExpenseHistoryItem(entry: ExpenseEntry) {
    @Suppress("LocalVariableName") val GlassTheme = LocalCurrentGlassTheme.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val catColor = GlassTheme.categoryColor(entry.category)

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
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(catColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                entry.category.take(2).uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = catColor,
            )
        }

        // Info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                entry.category,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = GlassTheme.TextPrimary,
            )
            Text(
                entry.note?.takeIf { it.isNotBlank() } ?: entry.paymentMethod,
                fontSize = 11.sp,
                color = GlassTheme.TextSecondary,
            )
            Text(
                entry.date?.toDate()?.let { dateFormat.format(it) } ?: "",
                fontSize = 10.sp,
                color = GlassTheme.TextHint,
            )
        }

        // Amount
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "-LKR ${"%.2f".format(entry.amount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GlassTheme.Danger,
            )
            if (entry.isRecurring) {
                Text("recurring", fontSize = 10.sp, color = GlassTheme.TextHint)
            }
        }
    }
}