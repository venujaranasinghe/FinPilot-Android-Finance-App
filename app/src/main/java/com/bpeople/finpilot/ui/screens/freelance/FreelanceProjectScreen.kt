package com.bpeople.finpilot.ui.screens.freelance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.bpeople.finpilot.data.model.FreelanceProject
import com.bpeople.finpilot.ui.theme.LocalAppDarkTheme

private val Orange = Color(0xFFF97316)
private val Teal   = Color(0xFF14B8A6)
private val Indigo = Color(0xFF6366F1)
private val Amber  = Color(0xFFF59E0B)
private val Red    = Color(0xFFEF4444)
private val Green  = Color(0xFF10B981)

private val STATUS_OPTIONS = listOf("OPEN", "ACTIVE", "COMPLETED", "CANCELLED")

@Composable
fun FreelanceProjectScreen(
    viewModel: FreelanceProjectViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val isDark = LocalAppDarkTheme.current

    val bgGradient = if (isDark)
        Brush.verticalGradient(listOf(Color(0xFF0A0500), Color(0xFF000000), Color(0xFF050010)))
    else
        Brush.verticalGradient(listOf(Color(0xFFFFF7ED), Color(0xFFF5F3FF), Color(0xFFFFFFFF)))

    // Snackbar
    val snackbarHost = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage, state.successMessage) {
        state.errorMessage?.let { snackbarHost.showSnackbar(it); viewModel.clearMessages() }
        state.successMessage?.let { snackbarHost.showSnackbar(it); viewModel.clearMessages() }
    }

    if (state.showDialog) {
        FreelanceProjectDialog(
            state = state,
            onDismiss = viewModel::closeDialog,
            onSave = viewModel::saveProject,
            onClientName = viewModel::setClientName,
            onProjectTitle = viewModel::setProjectTitle,
            onAgreedAmount = viewModel::setAgreedAmount,
            onPaidAmount = viewModel::setPaidAmount,
            onStatus = viewModel::setStatus,
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
            ) { Icon(Icons.Default.Add, "Add Project") }
        },
    ) { pv ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(pv),
            contentPadding = PaddingValues(bottom = 96.dp),
        ) {
            // Top bar
            item {
                FreelanceTopBar(onNavigateBack = onNavigateBack, isDark = isDark)
            }

            // Summary card
            item {
                FreelanceSummaryCard(state = state, isDark = isDark)
            }

            // Project list
            if (state.isLoading) {
                item { Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Orange)
                }}
            } else if (state.projects.isEmpty()) {
                item { FreelanceEmptyState(isDark = isDark) }
            } else {
                items(state.projects, key = { it.id }) { project ->
                    FreelanceProjectCard(
                        project = project,
                        isDark = isDark,
                        onEdit = { viewModel.openEditDialog(project) },
                        onDelete = { viewModel.deleteProject(project.id) },
                    )
                }
            }
        }
    }
}

// ── Top bar ───────────────────────────────────────────────────────────────────

@Composable
private fun FreelanceTopBar(onNavigateBack: () -> Unit, isDark: Boolean) {
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
            Icon(Icons.Default.Business, null, tint = Orange, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
            Text("Freelance Projects", fontSize = 20.sp, fontWeight = FontWeight.Bold,
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
private fun FreelanceSummaryCard(state: FreelanceProjectViewModel.UiState, isDark: Boolean) {
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
            Text("Portfolio Overview", fontWeight = FontWeight.Bold, fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                SummaryItem("Total Agreed", "LKR ${fmtLKR(state.totalAgreed)}", Orange)
                SummaryItem("Total Paid", "LKR ${fmtLKR(state.totalPaid)}", Green)
                SummaryItem("Outstanding", "LKR ${fmtLKR(state.totalOutstanding)}", Amber)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatChip("${state.projects.size} Total", Indigo)
                StatChip("${state.activeCount} Active", Teal)
                StatChip("${state.completedCount} Done", Green)
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StatChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(0.6.dp, color.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) { Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color) }
}

// ── Project card ──────────────────────────────────────────────────────────────

@Composable
private fun FreelanceProjectCard(
    project: FreelanceProject,
    isDark: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val statusColor = when (project.status) {
        "ACTIVE"    -> Teal
        "COMPLETED" -> Green
        "CANCELLED" -> Red
        else        -> Amber
    }
    val outstanding = project.agreedAmount - project.paidAmount

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
                verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(project.projectTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(project.clientName, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .border(0.6.dp, statusColor.copy(alpha = 0.40f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
                ) { Text(project.status, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = statusColor) }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AmountLabel("Agreed", project.agreedAmount, Orange)
                AmountLabel("Paid", project.paidAmount, Green)
                AmountLabel("Due", outstanding, if (outstanding > 0) Amber else Green)
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically) {
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
private fun AmountLabel(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("LKR ${fmtLKR(amount)}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun FreelanceEmptyState(isDark: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("💼", fontSize = 48.sp)
        Text("No freelance projects yet", fontWeight = FontWeight.Bold, fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface)
        Text("Tap + to add your first project", fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ── Add/Edit dialog ───────────────────────────────────────────────────────────

@Composable
private fun FreelanceProjectDialog(
    state: FreelanceProjectViewModel.UiState,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    onClientName: (String) -> Unit,
    onProjectTitle: (String) -> Unit,
    onAgreedAmount: (String) -> Unit,
    onPaidAmount: (String) -> Unit,
    onStatus: (String) -> Unit,
) {
    val title = if (state.editingProject != null) "Edit Project" else "Add Project"
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = state.clientName, onValueChange = onClientName,
                    label = { Text("Client Name") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.projectTitle, onValueChange = onProjectTitle,
                    label = { Text("Project Title") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.agreedAmount, onValueChange = onAgreedAmount,
                    label = { Text("Agreed Amount (LKR)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = state.paidAmount, onValueChange = onPaidAmount,
                    label = { Text("Paid Amount (LKR)") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth())
                Text("Status", fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    STATUS_OPTIONS.forEach { opt ->
                        val selected = state.status == opt
                        FilterChip(
                            selected = selected,
                            onClick = { onStatus(opt) },
                            label = { Text(opt, fontSize = 11.sp) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("Save", color = Orange, fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

private fun fmtLKR(amount: Double): String = when {
    amount >= 1_000_000 -> "%.2fM".format(amount / 1_000_000)
    amount >= 1_000     -> "%.1fK".format(amount / 1_000)
    else                -> "%.0f".format(amount)
}
