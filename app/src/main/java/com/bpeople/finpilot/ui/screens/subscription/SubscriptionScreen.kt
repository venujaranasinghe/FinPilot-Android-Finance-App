package com.bpeople.finpilot.ui.screens.subscription

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
import androidx.compose.material.icons.filled.Subscriptions
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
import com.bpeople.finpilot.data.model.Subscription
import com.bpeople.finpilot.ui.theme.LocalAppDarkTheme

private val Orange  = Color(0xFFF97316)
private val Amber   = Color(0xFFF59E0B)
private val Green   = Color(0xFF10B981)
private val Red     = Color(0xFFEF4444)
private val Teal    = Color(0xFF14B8A6)
private val Indigo  = Color(0xFF6366F1)

private val CYCLES     = listOf("MONTHLY", "YEARLY", "WEEKLY")
private val CATEGORIES = listOf("Entertainment", "Utilities", "Software", "Health", "Education", "Finance", "Other")

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = LocalAppDarkTheme.current

    val bgGradient = if (isDark)
        Brush.verticalGradient(listOf(Color(0xFF000510), Color(0xFF000000), Color(0xFF050A00)))
    else
        Brush.verticalGradient(listOf(Color(0xFFEFF6FF), Color(0xFFFFFFFF), Color(0xFFF0FFF4)))

    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let { snackbarHost.showSnackbar(it); viewModel.clearMessages() }
        state.successMessage?.let { snackbarHost.showSnackbar(it); viewModel.clearMessages() }
    }

    if (state.showDialog) {
        SubscriptionDialog(
            state = state,
            onDismiss = viewModel::closeDialog,
            onSave = viewModel::saveSub,
            onName = viewModel::setName,
            onAmount = viewModel::setAmount,
            onCycle = viewModel::setBillingCycle,
            onCategory = viewModel::setCategory,
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
            ) { Icon(Icons.Default.Add, "Add Subscription") }
        },
    ) { pv ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pv),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            item { SubTopBar(onNavigateBack = onNavigateBack, isDark = isDark) }
            item { SubSummaryCard(state = state, isDark = isDark) }

            if (state.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }}
            } else if (state.subscriptions.isEmpty()) {
                item { SubEmptyState() }
            } else {
                items(state.subscriptions, key = { it.id }) { sub ->
                    SubscriptionCard(
                        sub = sub, isDark = isDark,
                        onEdit = { viewModel.openEditDialog(sub) },
                        onDelete = { viewModel.deleteSub(sub.id) },
                        onToggle = { viewModel.toggleActive(sub) },
                    )
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun SubTopBar(onNavigateBack: () -> Unit, isDark: Boolean) {
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
            Icon(Icons.Default.Subscriptions, null, tint = Orange, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Subscriptions", fontSize = 20.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface)
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}

// ── Summary card ──────────────────────────────────────────────────────────────

@Composable
private fun SubSummaryCard(state: SubscriptionViewModel.UiState, isDark: Boolean) {
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
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Subscription Overview", fontWeight = FontWeight.Bold, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SumCol("Monthly Cost", "LKR ${fmtLKR(state.monthlyTotal)}", Orange)
                SumCol("Yearly Cost", "LKR ${fmtLKR(state.yearlyTotal)}", Orange)
                SumCol("Active", "${state.active.size} / ${state.subscriptions.size}", Green)
            }
        }
    }
}

@Composable
private fun SumCol(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Subscription card ─────────────────────────────────────────────────────────

@Composable
private fun SubscriptionCard(
    sub: Subscription,
    isDark: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit,
) {
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
                Column(Modifier.weight(1f)) {
                    Text(sub.name, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface, maxLines = 1,
                        overflow = TextOverflow.Ellipsis)
                    Text(sub.category, fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = sub.isActive,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Orange,
                        checkedThumbColor = Color.White,
                    ),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("LKR ${fmtLKR(sub.amountLKR)} / ${sub.billingCycle.lowercase()}",
                        fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Orange)
                    if (sub.billingCycle != "MONTHLY") {
                        Text("≈ LKR ${fmtLKR(sub.monthlyEquivalent)}/mo",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row {
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
}

private fun categoryColor(cat: String): Color = when (cat) {
    "Entertainment" -> Color(0xFFEC4899)
    "Utilities"     -> Color(0xFF6366F1)
    "Software"      -> Color(0xFF14B8A6)
    "Health"        -> Color(0xFF10B981)
    "Education"     -> Color(0xFFF59E0B)
    "Finance"       -> Color(0xFFF97316)
    else            -> Color(0xFF8B5CF6)
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun SubEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("📱", fontSize = 48.sp)
        Text("No subscriptions tracked", fontWeight = FontWeight.Bold, fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface)
        Text("Tap + to add your first subscription", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Dialog ────────────────────────────────────────────────────────────────────

@Composable
private fun SubscriptionDialog(
    state: SubscriptionViewModel.UiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onName: (String) -> Unit,
    onAmount: (String) -> Unit,
    onCycle: (String) -> Unit,
    onCategory: (String) -> Unit,
    onNote: (String) -> Unit,
) {
    val title = if (state.editingSub != null) "Edit Subscription" else "Add Subscription"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = state.name, onValueChange = onName,
                    label = { Text("Service Name (e.g. Netflix)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.amount, onValueChange = onAmount,
                    label = { Text("Amount (LKR)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                Text("Billing Cycle", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    CYCLES.forEach { c ->
                        FilterChip(selected = state.billingCycle == c,
                            onClick = { onCycle(c) },
                            label = { Text(c, fontSize = 11.sp) })
                    }
                }
                Text("Category", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CATEGORIES) { cat ->
                        FilterChip(selected = state.category == cat,
                            onClick = { onCategory(cat) },
                            label = { Text(cat, fontSize = 11.sp) })
                    }
                }
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
    else           -> "%.0f".format(v)
}
