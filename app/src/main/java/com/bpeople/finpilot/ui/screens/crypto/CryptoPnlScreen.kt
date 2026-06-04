package com.bpeople.finpilot.ui.screens.crypto

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bpeople.finpilot.data.model.CryptoEntry
import com.bpeople.finpilot.ui.theme.LocalAppDarkTheme

private val Orange  = Color(0xFFF97316)
private val Amber   = Color(0xFFF59E0B)
private val Green   = Color(0xFF10B981)
private val Red     = Color(0xFFEF4444)
private val Indigo  = Color(0xFF6366F1)
private val Teal    = Color(0xFF14B8A6)
private val Purple  = Color(0xFF8B5CF6)

@Composable
fun CryptoPnlScreen(
    viewModel: CryptoPnlViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = LocalAppDarkTheme.current

    val bgGradient = if (isDark)
        Brush.verticalGradient(listOf(Color(0xFF050010), Color(0xFF000000), Color(0xFF0A0500)))
    else
        Brush.verticalGradient(listOf(Color(0xFFF5F3FF), Color(0xFFFFFFFF), Color(0xFFFFF7ED)))

    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let { snackbarHost.showSnackbar(it); viewModel.clearMessages() }
        state.successMessage?.let { snackbarHost.showSnackbar(it); viewModel.clearMessages() }
    }

    if (state.showDialog) {
        CryptoHoldingDialog(
            state = state,
            onDismiss = viewModel::closeDialog,
            onSave = viewModel::saveHolding,
            onSymbol = viewModel::setSymbol,
            onName = viewModel::setName,
            onQuantity = viewModel::setQuantity,
            onBuyPrice = viewModel::setBuyPrice,
            onCurrentPrice = viewModel::setCurrentPrice,
            onNote = viewModel::setNote,
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHost) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openAddDialog,
                containerColor = Orange,
                contentColor = Color.White,
            ) { Icon(Icons.Default.Add, "Add Holding") }
        },
    ) { pv ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pv),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item { CryptoTopBar(onNavigateBack = onNavigateBack, isDark = isDark) }
            item { CryptoSummaryCard(state = state, isDark = isDark) }

            if (state.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }}
            } else if (state.holdings.isEmpty()) {
                item { CryptoEmptyState() }
            } else {
                items(state.holdings, key = { it.id }) { entry ->
                    CryptoHoldingCard(
                        entry = entry, isDark = isDark,
                        onEdit = { viewModel.openEditDialog(entry) },
                        onDelete = { viewModel.deleteHolding(entry.id) },
                    )
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun CryptoTopBar(onNavigateBack: () -> Unit, isDark: Boolean) {
    Column(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back",
                    tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(4.dp))
            Icon(Icons.Default.ShowChart, null, tint = Orange, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Crypto P&L", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}

// ── Portfolio summary ─────────────────────────────────────────────────────────

@Composable
private fun CryptoSummaryCard(state: CryptoPnlViewModel.UiState, isDark: Boolean) {
    val isProfit = state.netPnl >= 0
    val pnlColor = if (isProfit) Green else Red

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Portfolio Summary", fontWeight = FontWeight.Bold, fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface)
                Icon(
                    if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    null, tint = pnlColor, modifier = Modifier.size(22.dp)
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryCol("Invested", "LKR ${fmtLKR(state.totalInvested)}", Orange)
                SummaryCol("Current Value", "LKR ${fmtLKR(state.totalCurrentValue)}", Indigo)
                SummaryCol("Net P&L", "${if (isProfit) "+" else ""}LKR ${fmtLKR(state.netPnl)}\n${
                    "%.1f".format(state.netPnlPercent)}%", pnlColor)
            }
        }
    }
}

@Composable
private fun SummaryCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color,
            maxLines = 2)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Holding card ──────────────────────────────────────────────────────────────

@Composable
private fun CryptoHoldingCard(
    entry: CryptoEntry,
    isDark: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val isProfit = entry.pnlLKR >= 0
    val accentColor = if (isProfit) Green else Red

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Orange.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) { Text(entry.symbol, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Orange) }
                    Column {
                        Text(entry.name.ifBlank { entry.symbol }, fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${entry.quantity} units", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isProfit) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        null, tint = accentColor, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("${"%.1f".format(entry.pnlPercent)}%", fontWeight = FontWeight.Bold,
                        fontSize = 13.sp, color = accentColor)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniLabel("Buy Price", "LKR ${fmtLKR(entry.buyPriceLKR)}", Orange)
                MiniLabel("Current", "LKR ${fmtLKR(entry.currentPriceLKR)}", Indigo)
                MiniLabel("P&L", "${if (isProfit) "+" else ""}LKR ${fmtLKR(entry.pnlLKR)}", accentColor)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, "Edit", tint = Teal, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = Red, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun MiniLabel(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun CryptoEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("₿", fontSize = 48.sp)
        Text("No crypto holdings yet", fontWeight = FontWeight.Bold, fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface)
        Text("Tap + to track your first holding", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Dialog ────────────────────────────────────────────────────────────────────

@Composable
private fun CryptoHoldingDialog(
    state: CryptoPnlViewModel.UiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onSymbol: (String) -> Unit,
    onName: (String) -> Unit,
    onQuantity: (String) -> Unit,
    onBuyPrice: (String) -> Unit,
    onCurrentPrice: (String) -> Unit,
    onNote: (String) -> Unit,
) {
    val title = if (state.editingEntry != null) "Edit Holding" else "Add Crypto Holding"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = state.symbol, onValueChange = onSymbol,
                    label = { Text("Symbol (e.g. BTC)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.name, onValueChange = onName,
                    label = { Text("Name (e.g. Bitcoin)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.quantity, onValueChange = onQuantity,
                    label = { Text("Quantity") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.buyPrice, onValueChange = onBuyPrice,
                    label = { Text("Buy Price (LKR/unit)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.currentPrice, onValueChange = onCurrentPrice,
                    label = { Text("Current Price (LKR/unit)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.note, onValueChange = onNote,
                    label = { Text("Note (optional)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save", color = Orange, fontWeight = FontWeight.Bold) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

private fun fmtLKR(v: Double): String = when {
    v >= 1_000_000 -> "%.2fM".format(v / 1_000_000)
    v >= 1_000     -> "%.1fK".format(v / 1_000)
    else           -> "%.2f".format(v)
}
