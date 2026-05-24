@file:OptIn(ExperimentalMaterial3Api::class)

package com.bpeople.finpilot.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.bpeople.finpilot.ui.theme.LocalAppDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Business
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Subscriptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bpeople.finpilot.ui.components.FinPilotBottomNavBar
import com.bpeople.finpilot.ui.components.NavTab
import com.bpeople.finpilot.ui.components.DynamicHeaderBackground
import com.bpeople.finpilot.ui.components.wavyBottomShape
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
private fun surfaceColor(): Color = if (LocalAppDarkTheme.current) DarkSurface else Color.White

@Composable
private fun surfaceVariantColor(): Color = if (LocalAppDarkTheme.current) DarkSurfaceVariant else Color(0xFFF9FAFB)

@Composable
private fun backgroundColor(): Color = if (LocalAppDarkTheme.current) DarkBackground else Color(0xFFF9FAFB)

@Composable
private fun borderColor(): Color = if (LocalAppDarkTheme.current) DarkBorder else Color(0xFFE5E7EB)

@Composable
private fun glassBgColor(): Color = if (LocalAppDarkTheme.current) DarkGlassBg else Color.White

@Composable
private fun glassBorderLightColor(): Color = if (LocalAppDarkTheme.current) DarkGlassBorderLight else Color(0xFFE5E7EB).copy(alpha = 0.5f)

@Composable
private fun textPrimaryColor(): Color = if (LocalAppDarkTheme.current) DarkTextPrimary else Color(0xFF1F2937)

@Composable
private fun textSecondaryColor(): Color = if (LocalAppDarkTheme.current) DarkTextSecondary else Color(0xFF4B5563)

@Composable
private fun textHintColor(): Color = if (LocalAppDarkTheme.current) DarkTextHint else Color(0xFF6B7280)

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
    onChangePin: () -> Unit = {},
    onUpdateDisplayName: (String) -> Unit,
    onToggleIncomeSource: (String) -> Unit = {},
    onAddIncomeSource: (IncomeSource) -> Unit = {},
    onNavigateToFreelance: () -> Unit = {},
    onNavigateToCrypto: () -> Unit = {},
    onNavigateToSubscriptions: () -> Unit = {},
) {
    var showEditNameDialog by rememberSaveable { mutableStateOf(false) }
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
                        onEditName = { showEditNameDialog = true },
                        onNavigateToSettings = onNavigateToSettings,
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

                // Tools
                item {
                    SectionLabel("Tools")
                    ToolsCard(
                        onFreelance = onNavigateToFreelance,
                        onCrypto = onNavigateToCrypto,
                        onSubscriptions = onNavigateToSubscriptions,
                    )
                }

                // Account
                item {
                    SectionLabel("Account")
                    DangerZoneCard(
                        onSignOut = onLogout,
                        onChangePin = onChangePin,
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
    onNavigateToSettings: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        DynamicHeaderBackground(
            patternType = "profile",
            modifier = Modifier.matchParentSize().clip(wavyBottomShape())
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
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
                        color = Color(0xFF0F172A),
                        letterSpacing = (-0.5).sp,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                            .border(0.8.dp, Color(0x1A0F172A), RoundedCornerShape(13.dp)),
                    ) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp),
                        )
                    }

                    IconButton(
                        onClick = onEditName,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(Color.White.copy(alpha = 0.4f))
                            .border(0.8.dp, Color(0x1A0F172A), RoundedCornerShape(13.dp)),
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "Edit Profile",
                            tint = Color(0xFF0F172A),
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            // Avatar and user info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
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
                                .background(Brush.radialGradient(listOf(Color(0xFFFF8C42), Color(0xFFFF6B00)))),
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
                            color = Color(0xFF0F172A),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )

                        if (!email.isNullOrBlank()) {
                            Text(
                                text = email,
                                fontSize = 13.sp,
                                color = Color(0xFF0F172A).copy(alpha = 0.65f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
            }
            Spacer(modifier = Modifier.height(36.dp))
        }
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

// ─── Tools Card ──────────────────────────────────────────────────────────────

@Composable
private fun ToolsCard(
    onFreelance: () -> Unit,
    onCrypto: () -> Unit,
    onSubscriptions: () -> Unit,
) {
    val isDark = LocalAppDarkTheme.current
    val surfCol = if (isDark) DarkSurface else Color.White
    val borderCol = if (isDark) DarkBorder else Color(0xFFE5E7EB)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = surfCol),
        border = BorderStroke(0.7.dp, borderCol),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            ToolRow(
                icon = Icons.Rounded.Business,
                label = "Freelance Projects",
                subtitle = "Track clients & payments",
                color = Color(0xFFF97316),
                onClick = onFreelance,
            )
            HorizontalDivider(modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                color = borderCol, thickness = 0.5.dp)
            ToolRow(
                icon = Icons.Rounded.CurrencyBitcoin,
                label = "Crypto P&L",
                subtitle = "Track holdings & returns",
                color = Color(0xFF8B5CF6),
                onClick = onCrypto,
            )
            HorizontalDivider(modifier = Modifier.padding(start = 72.dp, end = 20.dp),
                color = borderCol, thickness = 0.5.dp)
            ToolRow(
                icon = Icons.Rounded.Subscriptions,
                label = "Subscriptions",
                subtitle = "Manage recurring costs",
                color = Color(0xFF14B8A6),
                onClick = onSubscriptions,
            )
        }
    }
    Spacer(Modifier.height(14.dp))
}

@Composable
private fun ToolRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (LocalAppDarkTheme.current) DarkTextPrimary else Color(0xFF1F2937))
            Text(subtitle, style = MaterialTheme.typography.bodySmall,
                color = if (LocalAppDarkTheme.current) DarkTextSecondary else Color(0xFF4B5563))
        }
        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp))
    }
}

// ─── Danger Zone ─────────────────────────────────────────────────────────────

@Composable
private fun DangerZoneCard(
    onSignOut: () -> Unit,
    onChangePin: () -> Unit,
) {
    var showSignOutDialog by remember { mutableStateOf(false) }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = surfaceColor(),
            title = {
                Text("Sign Out", fontWeight = FontWeight.Bold, color = textPrimaryColor())
            },
            text = {
                Text(
                    "Are you sure you want to sign out?",
                    color = textSecondaryColor(),
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    onSignOut()
                }) {
                    Text("Sign Out", color = ExpenseRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancel", color = textSecondaryColor())
                }
            }
        )
    }

    GlassCard {
        OutlinedButton(
            onClick = onChangePin,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, OrangeMain),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeMain),
        ) {
            Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Change PIN", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = { showSignOutDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ExpenseRed),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRed),
        ) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
            displayName = "Venujan Anandakumar",
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