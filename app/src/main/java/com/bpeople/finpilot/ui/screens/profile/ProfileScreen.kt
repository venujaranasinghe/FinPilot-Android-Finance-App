@file:OptIn(ExperimentalMaterial3Api::class)

package com.bpeople.finpilot.ui.screens.profile

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
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
import com.bpeople.finpilot.ui.theme.DarkBackground
import com.bpeople.finpilot.ui.theme.DarkBorder
import com.bpeople.finpilot.ui.theme.DarkGlassBg
import com.bpeople.finpilot.ui.theme.DarkGlassBorderLight
import com.bpeople.finpilot.ui.theme.DarkSurface
import com.bpeople.finpilot.ui.theme.DarkSurfaceVariant
import com.bpeople.finpilot.ui.theme.DarkTextPrimary
import com.bpeople.finpilot.ui.theme.DarkTextSecondary
import com.bpeople.finpilot.ui.theme.DarkTextHint
import kotlinx.coroutines.launch

// ─── Theme-aware colors ──────────────────────────────────────────────────────
private val OrangeMain = Color(0xFFFF6B00)
private val IncomeGreen = Color(0xFF10B981)
private val ExpenseRed = Color(0xFFEF4444)

@Composable
private fun surfaceColor(): Color = if (isSystemInDarkTheme()) DarkSurface else Color.White

@Composable
private fun surfaceVariantColor(): Color = if (isSystemInDarkTheme()) DarkSurfaceVariant else Color(0xFFF9FAFB)

@Composable
private fun backgroundColor(): Color = if (isSystemInDarkTheme()) DarkBackground else Color(0xFFF9FAFB)

@Composable
private fun borderColor(): Color = if (isSystemInDarkTheme()) DarkBorder else Color(0xFFE5E7EB)

@Composable
private fun glassBgColor(): Color = if (isSystemInDarkTheme()) DarkGlassBg else Color.White

@Composable
private fun glassBorderLightColor(): Color = if (isSystemInDarkTheme()) DarkGlassBorderLight else Color(0xFFE5E7EB).copy(alpha = 0.5f)

@Composable
private fun textPrimaryColor(): Color = if (isSystemInDarkTheme()) DarkTextPrimary else Color(0xFF1F2937)

@Composable
private fun textSecondaryColor(): Color = if (isSystemInDarkTheme()) DarkTextSecondary else Color(0xFF4B5563)

@Composable
private fun textHintColor(): Color = if (isSystemInDarkTheme()) DarkTextHint else Color(0xFF6B7280)

@Composable
private fun heroBgColor(): Color = Color(0xFF1F2937)

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

    val bgColor = backgroundColor()

    Scaffold(
        containerColor = bgColor,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 110.dp),
            ) {
                // Header
                item {
                    ProfileHeader(
                        name = displayName,
                        email = email,
                        onEditName = { showEditNameDialog = true }
                    )
                }

                // Currency Settings
                item {
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
                }

                // Income Sources
                item {
                    SectionLabel("Income Sources")
                    IncomeSourcesCard(
                        sources = uiState.incomeSources,
                        onToggle = onToggleIncomeSource,
                        onAddNew = { showAddSourceSheet = true }
                    )
                }

                // Notifications
                item {
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
                }

                // App Settings
                item {
                    SectionLabel("App Settings")
                    AppSettingsCard(
                        darkMode = uiState.darkModeEnabled,
                        onDarkMode = onDarkMode,
                        onNavigateToSettings = onNavigateToSettings,
                        onExportCsv = onExportCsv,
                        onClearCache = onClearCache,
                    )
                }

                // Account
                item {
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
                onNavigateToProfile = onNavigateToProfile,
                onNavigateToSettings = onNavigateToSettings
            )
        }
    }
}

// ─── Profile Header ──────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(
    name: String?,
    email: String?,
    onEditName: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor())
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {

                Text(
                    text = "Profile",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimaryColor(),
                    letterSpacing = (-0.5).sp,
                )
            }

            IconButton(
                onClick = onEditName,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(glassBgColor())
                    .border(0.8.dp, borderColor(), RoundedCornerShape(13.dp)),
            ) {
                Icon(
                    Icons.Rounded.Edit,
                    contentDescription = "Edit Profile",
                    tint = textPrimaryColor(),
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        // Avatar and user info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = heroBgColor()),
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(OrangeMain, OrangeMain.copy(alpha = 0.7f)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initialsFrom(name, email),
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = name ?: "Your Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    if (!email.isNullOrBlank()) {
                        Text(
                            text = email,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.65f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

// ─── Currency Settings ───────────────────────────────────────────────────────

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
    GlassCard {
        CurrencyRow(flag = "🇱🇰", label = "LKR — Sri Lankan Rupee", isPrimary = true)
        HorizontalDivider(color = borderColor())
        Text(
            "Secondary Currencies",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textHintColor(),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        CurrencyToggleRow(flag = "🇺🇸", label = "USD — US Dollar", enabled = usdEnabled, onToggle = onUsdToggle)
        Spacer(modifier = Modifier.height(8.dp))
        CurrencyToggleRow(flag = "💵", label = "USDT — Tether", enabled = usdtEnabled, onToggle = onUsdtToggle)

        HorizontalDivider(color = borderColor(), modifier = Modifier.padding(vertical = 12.dp))

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Auto-convert using live rates",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimaryColor()
                )
                Text(
                    text = rateLastUpdated,
                    fontSize = 11.sp,
                    color = textHintColor()
                )
            }
            OrangeSwitch(checked = autoConvert, onCheckedChange = onAutoConvertToggle)
        }
    }
}

@Composable
private fun CurrencyRow(flag: String, label: String, isPrimary: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(flag, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textPrimaryColor())
            if (isPrimary) Text("Primary", fontSize = 11.sp, color = OrangeMain, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CurrencyToggleRow(flag: String, label: String, enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(flag, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(10.dp))
        Text(label, modifier = Modifier.weight(1f), fontSize = 13.sp, color = textPrimaryColor())
        OrangeSwitch(checked = enabled, onCheckedChange = onToggle)
    }
}

// ─── Income Sources ──────────────────────────────────────────────────────────

@Composable
private fun IncomeSourcesCard(
    sources: List<IncomeSource>,
    onToggle: (String) -> Unit,
    onAddNew: () -> Unit,
) {
    GlassCard {
        sources.forEachIndexed { index, source ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimaryColor()
                )
                ActiveChip(active = source.isActive, onClick = { onToggle(source.id) })
            }
            if (index < sources.lastIndex) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = borderColor())
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        TextButton(onClick = onAddNew, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Add, contentDescription = null, tint = OrangeMain, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add New Source", color = OrangeMain, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun ActiveChip(active: Boolean, onClick: () -> Unit) {
    val bg = if (active) IncomeGreen.copy(alpha = 0.12f) else surfaceVariantColor()
    val textColor = if (active) IncomeGreen else textSecondaryColor()
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
    ) {
        Text(
            text = if (active) "Active" else "Inactive",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
        )
    }
}

// ─── Notification Preferences ────────────────────────────────────────────────

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
    GlassCard {
        NotifRow("Salary reminder", "25th of every month", salaryReminder, onSalaryReminder)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = borderColor())
        NotifRow("Weekly spend summary", "Every Sunday", weeklySummary, onWeeklySummary)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = borderColor())
        NotifRow("Goal milestone alerts", "At 25%, 50%, 75%", goalMilestone, onGoalMilestone)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = borderColor())
        NotifRow("Budget overspend alert", "When spend exceeds threshold", budgetOverspend, onBudgetOverspend)
        if (budgetOverspend) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = budgetThreshold,
                onValueChange = { onThresholdChange(it.filter { c -> c.isDigit() }) },
                label = { Text("Threshold (LKR)", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangeMain,
                    focusedLabelColor = OrangeMain,
                    cursorColor = OrangeMain,
                    unfocusedTextColor = textPrimaryColor(),
                    focusedTextColor = textPrimaryColor(),
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
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textPrimaryColor())
            Text(subtitle, fontSize = 11.sp, color = textHintColor())
        }
        OrangeSwitch(checked = checked, onCheckedChange = onChecked)
    }
}

// ─── App Settings ────────────────────────────────────────────────────────────

@Composable
private fun AppSettingsCard(
    darkMode: Boolean,
    onDarkMode: (Boolean) -> Unit,
    onNavigateToSettings: () -> Unit,
    onExportCsv: () -> Unit,
    onClearCache: () -> Unit,
) {
    GlassCard {
        SettingsRow(Icons.Rounded.Settings, "Preferences", "Notifications, security & more", onClick = onNavigateToSettings)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = borderColor())

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(OrangeMain.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.DarkMode, contentDescription = null, tint = OrangeMain, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Dark Mode", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textPrimaryColor())
                Text("Toggle dark / light theme", fontSize = 11.sp, color = textHintColor())
            }
            OrangeSwitch(checked = darkMode, onCheckedChange = onDarkMode)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = borderColor())
        SettingsRow(Icons.Rounded.FileDownload, "Export to CSV", "Download all transactions", onClick = onExportCsv)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = borderColor())
        SettingsRow(Icons.Rounded.Storage, "Clear Cache", "Free up local storage", onClick = onClearCache)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = borderColor())

        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(surfaceVariantColor()),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Info, contentDescription = null, tint = textHintColor(), modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("App Version", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textPrimaryColor())
                Text("1.0.0 (build 1)", fontSize = 11.sp, color = textHintColor())
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(OrangeMain.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = OrangeMain, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textPrimaryColor())
            Text(subtitle, fontSize = 11.sp, color = textHintColor())
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = textHintColor(), modifier = Modifier.size(18.dp))
    }
}

// ─── Danger Zone ─────────────────────────────────────────────────────────────

@Composable
private fun DangerZoneCard(
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    GlassCard {
        OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ExpenseRed),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(onClick = onDeleteAccount, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Delete, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Delete Account", color = ExpenseRed, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ─── Shared Components ───────────────────────────────────────────────────────

@Composable
private fun GlassCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = glassBgColor()),
        border = BorderStroke(1.dp, glassBorderLightColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            content()
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = textSecondaryColor(),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun OrangeSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    val isDark = isSystemInDarkTheme()
    val animProgress by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "switch_anim"
    )

    val trackWidth = 52.dp
    val trackHeight = 32.dp
    val thumbSize = 28.dp
    val thumbPadding = 2.dp
    val density = LocalDensity.current

    val thumbOffsetPx = with(density) {
        (thumbPadding).toPx() + animProgress * (trackWidth - thumbSize - thumbPadding * 2).toPx()
    }

    Box(
        modifier = Modifier
            .size(width = trackWidth, height = trackHeight)
            .clip(RoundedCornerShape(50))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled,
                onClick = { onCheckedChange(!checked) }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track background with glass effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (checked) {
                        Brush.horizontalGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0xFF1A3A2A).copy(alpha = 0.6f),
                                    Color(0xFF0D2818).copy(alpha = 0.6f),
                                )
                            } else {
                                listOf(
                                    Color(0xFFE8F5E9).copy(alpha = 0.6f),
                                    Color(0xFFC8E6C9).copy(alpha = 0.6f),
                                )
                            }
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = if (isDark) {
                                listOf(
                                    Color(0xFF2C2C2E).copy(alpha = 0.6f),
                                    Color(0xFF1C1C1E).copy(alpha = 0.6f),
                                )
                            } else {
                                listOf(
                                    Color(0xFFE5E5EA).copy(alpha = 0.6f),
                                    Color(0xFFD1D1D6).copy(alpha = 0.6f),
                                )
                            }
                        )
                    }
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0x40FFFFFF),
                                Color(0x15FFFFFF),
                            )
                        } else {
                            listOf(
                                Color(0x30FFFFFF),
                                Color(0x10FFFFFF),
                            )
                        }
                    ),
                    shape = RoundedCornerShape(50)
                )
        )

        // Inner shadow/top highlight
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 4.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.5f),
                            Color.Transparent,
                        )
                    )
                )
        )

        // Thumb with glass effect
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetPx.roundToInt(), 0) }
                .padding(thumbPadding)
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (isDark) {
                            listOf(
                                Color(0xFFEEEEEE),
                                Color(0xFFDDDDDD),
                            )
                        } else {
                            listOf(
                                Color.White,
                                Color(0xFFF5F5F5),
                            )
                        }
                    )
                )
                .border(
                    width = 0.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x33FFFFFF),
                            Color(0x00FFFFFF),
                        )
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Thumb inner shadow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (checked) {
                                listOf(
                                    if (isDark) Color.White.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f),
                                    Color.Transparent,
                                )
                            } else {
                                listOf(
                                    Color.Transparent,
                                    Color.Transparent,
                                )
                            }
                        )
                    )
            )
        }

    }
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

// ─── Dialogs (unchanged) ─────────────────────────────────────────────────────

@Composable
fun EditNameDialog(currentName: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var name by remember(currentName) { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor(),
        title = { Text("Edit Display Name", fontWeight = FontWeight.Bold, color = textPrimaryColor()) },
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
                    unfocusedTextColor = textPrimaryColor(),
                    focusedTextColor = textPrimaryColor(),
                ),
                shape = RoundedCornerShape(12.dp),
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }) {
                Text("Save", color = OrangeMain, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DeleteAccountDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor(),
        title = { Text("Delete Account?", fontWeight = FontWeight.Bold, color = textPrimaryColor()) },
        text = {
            Text(
                "This action is permanent. All your data will be erased and cannot be recovered.",
                color = textSecondaryColor()
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

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
        containerColor = glassBgColor(),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.width(40.dp).height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(textSecondaryColor().copy(alpha = 0.3f))
                    .align(Alignment.CenterHorizontally)
            )

            Text("Add Income Source", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textPrimaryColor())

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                emojiOptions.forEach { e ->
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (e == emoji) OrangeMain.copy(alpha = 0.15f) else surfaceVariantColor())
                            .border(if (e == emoji) BorderStroke(1.5.dp, OrangeMain) else BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(10.dp))
                            .clickable { emoji = e },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(e, fontSize = 20.sp)
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
                    unfocusedTextColor = textPrimaryColor(),
                    focusedTextColor = textPrimaryColor(),
                ),
                shape = RoundedCornerShape(12.dp),
            )

            Button(
                onClick = {
                    if (label.isNotBlank()) {
                        onAdd(IncomeSource(id = label.trim().lowercase().replace(" ", "_"), label = label.trim(), icon = emoji, isActive = true))
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeMain),
                enabled = label.isNotBlank(),
            ) {
                Text("Add Source", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    FinPilotTheme {
        ProfileScreen(
            displayName = "Venujan Aranasinghe",
            email = "venujan@example.com",
            uiState = ProfileUiState(),
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