@file:OptIn(ExperimentalMaterial3Api::class)

package com.bpeople.finpilot.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.TrendingDown
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

// ─── Palette helpers ─────────────────────────────────────────────────────────

private val OrangeMain = Color(0xFFFF6B00)
private val IncomeGreen = Color(0xFF10B981)
private val ExpenseRed = Color(0xFFEF4444)
private val AmberScore = Color(0xFFF59E0B)
private val GreenScore = Color(0xFF22C55E)

private fun healthScoreColor(score: Int): Color = when {
    score < 40 -> ExpenseRed
    score < 70 -> AmberScore
    else -> GreenScore
}

private fun formatLkr(amount: Double): String {
    val fmt = NumberFormat.getNumberInstance(Locale.US)
    fmt.maximumFractionDigits = 0
    return "LKR ${fmt.format(amount)}"
}

// ─── Root screen ─────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    displayName: String?,
    email: String?,
    uiState: ProfileUiState = ProfileUiState(),
    onNavigateToDashboard: () -> Unit,
    onNavigateToIncome: () -> Unit,
    onNavigateToExpense: () -> Unit,
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToGoals: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit,
    onUpdateDisplayName: (String) -> Unit,
    onDeleteAccount: () -> Unit = {},
    onExportCsv: () -> Unit = {},
    onClearCache: () -> Unit = {},
    onSetUsdEnabled: (Boolean) -> Unit = {},
    onSetUsdtEnabled: (Boolean) -> Unit = {},
    onSetAutoConvert: (Boolean) -> Unit = {},
    onToggleIncomeSource: (String) -> Unit = {},
    onAddIncomeSource: (IncomeSource) -> Unit = {},
    onNotifySalaryReminder: (Boolean) -> Unit = {},
    onNotifyWeeklySummary: (Boolean) -> Unit = {},
    onNotifyGoalMilestone: (Boolean) -> Unit = {},
    onNotifyBudgetOverspend: (Boolean) -> Unit = {},
    onBudgetThreshold: (String) -> Unit = {},
    onDarkMode: (Boolean) -> Unit = {},
) {
    var showEditNameDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    var showAddSourceSheet by rememberSaveable { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    if (showEditNameDialog) {
        EditNameDialog(
            currentName = displayName ?: "",
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                onUpdateDisplayName(newName)
                showEditNameDialog = false
            }
        )
    }

    if (showDeleteDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onDeleteAccount()
            }
        )
    }

    if (showAddSourceSheet) {
        AddIncomeSourceSheet(
            sheetState = sheetState,
            onDismiss = {
                scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSourceSheet = false }
            },
            onAdd = { source ->
                onAddIncomeSource(source)
                scope.launch { sheetState.hide() }.invokeOnCompletion { showAddSourceSheet = false }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(bottom = 96.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1 — PROFILE HEADER
                ProfileHeader(
                    name = displayName,
                    email = email,
                    healthScore = uiState.healthScore,
                    onEditName = { showEditNameDialog = true }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 24.dp, bottom = 36.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 2 — FINANCIAL SNAPSHOT
                    SectionLabel("Financial Snapshot")
                    FinancialSnapshotGrid(
                        thisMonthIncome = uiState.thisMonthIncome,
                        thisMonthExpenses = uiState.thisMonthExpenses,
                        goalProgress = uiState.goalProgressPercent,
                        incomeVsLast = uiState.incomeVsLastMonth,
                        expenseVsLast = uiState.expenseVsLastMonth,
                        onTapIncome = onNavigateToIncome,
                        onTapExpense = onNavigateToExpense,
                        onTapGoal = onNavigateToGoals,
                    )

                    // 3 — CURRENCY SETTINGS
                    SectionLabel("Currency Settings")
                    CurrencySettingsCard(
                        usdEnabled = uiState.usdEnabled,
                        usdtEnabled = uiState.usdtEnabled,
                        autoConvert = uiState.autoConvert,
                        rateLastUpdated = uiState.rateLastUpdated,
                        onUsdToggle = onSetUsdEnabled,
                        onUsdtToggle = onSetUsdtEnabled,
                        onAutoConvertToggle = onSetAutoConvert,
                    )

                    // 4 — INCOME SOURCES
                    SectionLabel("Income Sources")
                    IncomeSourcesCard(
                        sources = uiState.incomeSources,
                        onToggle = onToggleIncomeSource,
                        onAddNew = { showAddSourceSheet = true }
                    )

                    // 5 — NOTIFICATION PREFERENCES
                    SectionLabel("Notifications")
                    NotificationPreferencesCard(
                        salaryReminder = uiState.notifySalaryReminder,
                        weeklySummary = uiState.notifyWeeklySummary,
                        goalMilestone = uiState.notifyGoalMilestone,
                        budgetOverspend = uiState.notifyBudgetOverspend,
                        budgetThreshold = uiState.budgetOverspendThreshold,
                        onSalaryReminder = onNotifySalaryReminder,
                        onWeeklySummary = onNotifyWeeklySummary,
                        onGoalMilestone = onNotifyGoalMilestone,
                        onBudgetOverspend = onNotifyBudgetOverspend,
                        onThresholdChange = onBudgetThreshold,
                    )

                    // 6 — APP SETTINGS
                    SectionLabel("App Settings")
                    AppSettingsCard(
                        darkMode = uiState.darkModeEnabled,
                        onDarkMode = onDarkMode,
                        onNavigateToSettings = onNavigateToSettings,
                        onExportCsv = onExportCsv,
                        onClearCache = onClearCache,
                    )

                    // 7 — ACHIEVEMENTS
                    SectionLabel("Achievements")
                    AchievementsRow(achievements = uiState.achievements)

                    // 8 — DANGER ZONE
                    SectionLabel("Account")
                    DangerZoneCard(
                        onSignOut = onLogout,
                        onDeleteAccount = { showDeleteDialog = true }
                    )
                }
            }

            FinPilotBottomNavBar(
                modifier = Modifier.align(Alignment.BottomCenter),
                currentTab = NavTab.PROFILE,
                onNavigateToDashboard = onNavigateToDashboard,
                onNavigateToIncome = onNavigateToIncome,
                onNavigateToExpense = onNavigateToExpense,
                onNavigateToTransactions = onNavigateToTransactions,
                onNavigateToGoals = onNavigateToGoals,
                onNavigateToProfile = onNavigateToProfile
            )
        }
    }
}

// ─── 1. Profile Header ────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(
    name: String?,
    email: String?,
    healthScore: Int,
    onEditName: () -> Unit,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val bgColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(primaryColor, primaryColor.copy(alpha = 0.72f), bgColor),
                        endY = size.height,
                    )
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.07f),
                    radius = 190.dp.toPx(),
                    center = Offset(size.width * 0.88f, size.height * 0.08f),
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.04f),
                    radius = 130.dp.toPx(),
                    center = Offset(size.width * 0.04f, size.height * 0.50f),
                )
            }
            .padding(top = 52.dp, bottom = 28.dp)
    ) {
        // Edit pencil — top-right
        IconButton(
            onClick = onEditName,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp)
                .size(44.dp)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Edit Profile",
                tint = OrangeMain,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Avatar with orange border ring
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(108.dp)) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(OrangeMain, OrangeMain.copy(alpha = 0.7f))))
                        .border(
                            width = 3.dp,
                            brush = Brush.linearGradient(listOf(OrangeMain, MaterialTheme.colorScheme.tertiary)),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsFrom(name, email),
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = name ?: "Your Profile",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Junior Software Engineer",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.height(3.dp))

            if (!email.isNullOrBlank()) {
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.55f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            HealthScoreBadge(score = healthScore)
        }
    }
}

@Composable
private fun HealthScoreBadge(score: Int) {
    val color = healthScoreColor(score)
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = Color.White.copy(alpha = 0.15f),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.8f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                text = "Score: $score / 100",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// ─── 2. Financial Snapshot 2×2 Grid ──────────────────────────────────────────

@Composable
private fun FinancialSnapshotGrid(
    thisMonthIncome: Double,
    thisMonthExpenses: Double,
    goalProgress: Float,
    incomeVsLast: Double,
    expenseVsLast: Double,
    onTapIncome: () -> Unit,
    onTapExpense: () -> Unit,
    onTapGoal: () -> Unit,
) {
    val net = thisMonthIncome - thisMonthExpenses
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SnapshotCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.TrendingUp,
                iconTint = IncomeGreen,
                label = "This Month's Income",
                value = formatLkr(thisMonthIncome),
                trend = incomeVsLast,
                trendPositiveIsGood = true,
                onClick = onTapIncome,
            )
            SnapshotCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.TrendingDown,
                iconTint = ExpenseRed,
                label = "This Month's Expenses",
                value = formatLkr(thisMonthExpenses),
                trend = expenseVsLast,
                trendPositiveIsGood = false,
                onClick = onTapExpense,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SnapshotCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.Star,
                iconTint = OrangeMain,
                label = "Goal Progress",
                value = "${(goalProgress * 100).toInt()}%",
                trend = null,
                trendPositiveIsGood = true,
                onClick = onTapGoal,
            )
            SnapshotCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.CurrencyExchange,
                iconTint = if (net >= 0) IncomeGreen else ExpenseRed,
                label = "Net Position",
                value = formatLkr(net),
                trend = null,
                trendPositiveIsGood = true,
                onClick = {},
            )
        }
    }
}

@Composable
private fun SnapshotCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    trend: Double?,
    trendPositiveIsGood: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangeMain.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 14.sp,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (trend != null) {
                Spacer(modifier = Modifier.height(4.dp))
                val isGood = if (trendPositiveIsGood) trend >= 0 else trend <= 0
                val trendColor = if (isGood) IncomeGreen else ExpenseRed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (trend >= 0) Icons.Rounded.TrendingUp else Icons.Rounded.TrendingDown,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = formatLkr(abs(trend)),
                        style = MaterialTheme.typography.labelSmall,
                        color = trendColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

// ─── 3. Currency Settings ─────────────────────────────────────────────────────

@Composable
private fun CurrencySettingsCard(
    usdEnabled: Boolean,
    usdtEnabled: Boolean,
    autoConvert: Boolean,
    rateLastUpdated: String,
    onUsdToggle: (Boolean) -> Unit,
    onUsdtToggle: (Boolean) -> Unit,
    onAutoConvertToggle: (Boolean) -> Unit,
) {
    ProfileCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🇱🇰", fontSize = 22.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Primary Currency",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "LKR — Sri Lankan Rupee",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Secondary Currencies",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        CurrencyToggleRow(flag = "🇺🇸", label = "USD — US Dollar", enabled = usdEnabled, onToggle = onUsdToggle)
        Spacer(modifier = Modifier.height(6.dp))
        CurrencyToggleRow(flag = "💵", label = "USDT — Tether", enabled = usdtEnabled, onToggle = onUsdtToggle)

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Auto-convert using live rates",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = rateLastUpdated,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OrangeSwitch(checked = autoConvert, onCheckedChange = onAutoConvertToggle)
        }
    }
}

@Composable
private fun CurrencyToggleRow(flag: String, label: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(flag, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        OrangeSwitch(checked = enabled, onCheckedChange = onToggle)
    }
}

// ─── 4. Income Sources ────────────────────────────────────────────────────────

@Composable
private fun IncomeSourcesCard(
    sources: List<IncomeSource>,
    onToggle: (String) -> Unit,
    onAddNew: () -> Unit,
) {
    ProfileCard {
        sources.forEachIndexed { index, source ->
            IncomeSourceRow(source = source, onToggle = { onToggle(source.id) })
            if (index < sources.lastIndex) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onAddNew, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = OrangeMain, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add New Source", color = OrangeMain, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun IncomeSourceRow(source: IncomeSource, onToggle: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OrangeMain.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(source.icon, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            source.label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
        ActiveChip(active = source.isActive, onClick = onToggle)
    }
}

@Composable
private fun ActiveChip(active: Boolean, onClick: () -> Unit) {
    val bg = if (active) IncomeGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (active) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(
        shape = RoundedCornerShape(50.dp),
        color = bg,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = if (active) "Active" else "Add",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}

// ─── 5. Notification Preferences ─────────────────────────────────────────────

@Composable
private fun NotificationPreferencesCard(
    salaryReminder: Boolean,
    weeklySummary: Boolean,
    goalMilestone: Boolean,
    budgetOverspend: Boolean,
    budgetThreshold: String,
    onSalaryReminder: (Boolean) -> Unit,
    onWeeklySummary: (Boolean) -> Unit,
    onGoalMilestone: (Boolean) -> Unit,
    onBudgetOverspend: (Boolean) -> Unit,
    onThresholdChange: (String) -> Unit,
) {
    ProfileCard {
        NotifRow("Salary received reminder", "25th of every month", salaryReminder, onSalaryReminder)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        NotifRow("Weekly spend summary", "Every Sunday", weeklySummary, onWeeklySummary)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        NotifRow("Goal milestone alerts", "At 25%, 50%, 75% of goal", goalMilestone, onGoalMilestone)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        NotifRow("Budget overspend alert", "When spend exceeds threshold", budgetOverspend, onBudgetOverspend)
        if (budgetOverspend) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = budgetThreshold,
                onValueChange = { onThresholdChange(it.filter { c -> c.isDigit() }) },
                label = { Text("Threshold (LKR)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeMain,
                    focusedLabelColor = OrangeMain,
                    cursorColor = OrangeMain,
                ),
                shape = RoundedCornerShape(12.dp),
            )
        }
    }
}

@Composable
private fun NotifRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        OrangeSwitch(checked = checked, onCheckedChange = onChecked)
    }
}

// ─── 6. App Settings ──────────────────────────────────────────────────────────

@Composable
private fun AppSettingsCard(
    darkMode: Boolean,
    onDarkMode: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    onExportCsv: () -> Unit,
    onClearCache: () -> Unit,
) {
    ProfileCard {
        SettingsActionRow(
            icon = Icons.Rounded.Settings,
            label = "Preferences",
            subtitle = "Notifications, security & more",
            onClick = onNavigateToSettings,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangeMain.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = OrangeMain, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Dark Mode", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("Toggle dark / light theme", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            OrangeSwitch(checked = darkMode, onCheckedChange = onDarkMode, enabled = true)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        SettingsActionRow(
            icon = Icons.Rounded.FileDownload,
            label = "Export to CSV",
            subtitle = "Download all transactions",
            onClick = onExportCsv,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        SettingsActionRow(
            icon = Icons.Rounded.Storage,
            label = "Clear Cache",
            subtitle = "Free up local storage",
            onClick = onClearCache,
        )
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("App Version", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("1.0.0 (build 1)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OrangeMain.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = OrangeMain, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── 7. Achievements ──────────────────────────────────────────────────────────

@Composable
private fun AchievementsRow(achievements: List<Achievement>) {
    val displayList = achievements.ifEmpty {
        listOf(
            Achievement("", "Locked", "", "🔒", false),
            Achievement("", "Locked", "", "🔒", false),
            Achievement("", "Locked", "", "🔒", false),
            Achievement("", "Locked", "", "🔒", false),
        )
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(displayList) { achievement ->
            AchievementBadge(achievement)
        }
    }
}

@Composable
private fun AchievementBadge(achievement: Achievement) {
    val alpha = if (achievement.unlocked) 1f else 0.4f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    if (achievement.unlocked) OrangeMain.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant
                )
                .border(
                    width = 2.dp,
                    color = if (achievement.unlocked) OrangeMain.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = achievement.icon.ifBlank { "🔒" },
                fontSize = 26.sp,
                color = Color.Unspecified.copy(alpha = alpha)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = achievement.label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (achievement.unlocked) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha),
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 13.sp,
        )
    }
}

// ─── 8. Danger Zone ───────────────────────────────────────────────────────────

@Composable
private fun DangerZoneCard(
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onDeleteAccount, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                "Delete Account",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Dialogs ──────────────────────────────────────────────────────────────────

@Composable
fun EditNameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Display Name") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeMain,
                    focusedLabelColor = OrangeMain,
                    cursorColor = OrangeMain,
                ),
                shape = RoundedCornerShape(12.dp),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }) {
                Text("Save", color = OrangeMain, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun DeleteAccountDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Account?") },
        text = {
            Text(
                "This action is permanent. All your data — income, expenses, goals — will be erased and cannot be recovered.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Delete Forever", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// ─── Add Income Source Bottom Sheet ──────────────────────────────────────────

@Composable
private fun AddIncomeSourceSheet(
    sheetState: androidx.compose.material3.SheetState,
    onDismiss: () -> Unit,
    onAdd: (IncomeSource) -> Unit,
) {
    var label by rememberSaveable { mutableStateOf("") }
    var emoji by rememberSaveable { mutableStateOf("💰") }
    val emojiOptions = listOf("💰", "🏦", "📈", "🎮", "🎨", "🛠️", "🚗", "✍️")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Add Income Source",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojiOptions.forEach { e ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (e == emoji) OrangeMain.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant,
                        border = if (e == emoji) BorderStroke(1.5.dp, OrangeMain) else null,
                        modifier = Modifier
                            .size(42.dp)
                            .clickable { emoji = e }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text(e, fontSize = 20.sp)
                        }
                    }
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = { label = it },
                label = { Text("Source Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeMain,
                    focusedLabelColor = OrangeMain,
                    cursorColor = OrangeMain,
                ),
                shape = RoundedCornerShape(12.dp),
            )

            Button(
                onClick = {
                    if (label.isNotBlank()) {
                        onAdd(
                            IncomeSource(
                                id = label.trim().lowercase().replace(" ", "_"),
                                label = label.trim(),
                                icon = emoji,
                                isActive = true,
                            )
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeMain),
                enabled = label.isNotBlank(),
            ) {
                Text("Add Source", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ─── Shared helpers ───────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

@Composable
private fun OrangeSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = OrangeMain,
            uncheckedThumbColor = Color.White,
            uncheckedTrackColor = MaterialTheme.colorScheme.outline,
        )
    )
}

private fun initialsFrom(name: String?, email: String?): String {
    val source = when {
        !name.isNullOrBlank() -> name
        !email.isNullOrBlank() -> email.substringBefore("@").replace('.', ' ')
        else -> "User"
    }
    val parts = source.trim().split(" ").filter { it.isNotBlank() }
    val first = parts.getOrNull(0)?.firstOrNull()?.uppercaseChar() ?: 'U'
    val second = parts.getOrNull(1)?.firstOrNull()?.uppercaseChar() ?: '\u0000'
    return if (second == '\u0000') "$first" else "$first$second"
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    FinPilotTheme {
        ProfileScreen(
            displayName = "Venujan Aranasinghe",
            email = "venujan@example.com",
            uiState = ProfileUiState(
                thisMonthIncome = 185000.0,
                thisMonthExpenses = 92000.0,
                goalProgressPercent = 0.62f,
                incomeVsLastMonth = 15000.0,
                expenseVsLastMonth = -3000.0,
                healthScore = 62,
                achievements = listOf(
                    Achievement("first_income", "First Income Logged", "", "💰", true),
                    Achievement("seven_day", "7-Day Streak", "", "🔥", true),
                    Achievement("goal_set", "Goal Set", "", "🎯", false),
                    Achievement("first_month", "First Month Complete", "", "📅", false),
                )
            ),
            onNavigateToDashboard = {},
            onNavigateToIncome = {},
            onNavigateToExpense = {},
            onNavigateToGoals = {},
            onNavigateToProfile = {},
            onNavigateToSettings = {},
            onLogout = {},
            onUpdateDisplayName = {},
        )
    }
}
