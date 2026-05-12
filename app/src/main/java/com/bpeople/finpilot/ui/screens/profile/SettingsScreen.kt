package com.bpeople.finpilot.ui.screens.profile

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Brightness4
import androidx.compose.material.icons.rounded.Brightness7
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.bpeople.finpilot.data.model.ThemeMode
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsViewModel.SettingsUiState,
    events: Flow<SettingsViewModel.SettingsEvent>,
    onNavigateBack: () -> Unit,
    onNotificationsChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onCloudSyncChange: (Boolean) -> Unit,
    onBiometricsChange: (Boolean) -> Unit,
    onChangePassword: () -> Unit,
    onExportData: () -> Unit,
    onDeleteAccount: () -> Unit,
    onAccountDeleted: () -> Unit,
) {
    var showExportDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    val preferences = listOf(
        SettingToggle(
            icon = Icons.Rounded.Notifications,
            title = "Notifications",
            subtitle = "Budget alerts and reminders",
            checked = state.notificationsEnabled,
            onCheckedChange = onNotificationsChange
        ),
        SettingToggle(
            icon = Icons.Rounded.CloudDownload,
            title = "Cloud sync",
            subtitle = "Back up data automatically",
            checked = state.cloudSyncEnabled,
            onCheckedChange = onCloudSyncChange
        )
    )

    val security = listOf(
        SettingToggle(
            icon = Icons.Rounded.Fingerprint,
            title = "Biometric unlock",
            subtitle = "Use fingerprint or face",
            checked = state.biometricsEnabled,
            onCheckedChange = onBiometricsChange
        )
    )

    val accountActions = listOf(
        SettingNavigation(
            icon = Icons.Rounded.Lock,
            title = "Change password",
            subtitle = "Update your login credentials",
            onClick = onChangePassword
        ),
        SettingNavigation(
            icon = Icons.Rounded.CloudDownload,
            title = "Export data",
            subtitle = "Download your transactions",
            onClick = { showExportDialog = true }
        ),
        SettingNavigation(
            icon = Icons.Rounded.Delete,
            title = "Delete account",
            subtitle = "Remove profile and data",
            isDestructive = true,
            onClick = { showDeleteDialog = true }
        )
    )

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(events) {
        events.collectLatest { event ->
            when (event) {
                is SettingsViewModel.SettingsEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is SettingsViewModel.SettingsEvent.ExportReady -> {
                    val exportResult = createCsvExport(context, event.csvContent)
                    if (exportResult == null) {
                        snackbarHostState.showSnackbar("Export failed. Please try again.")
                    } else {
                        val (uri, fileName) = exportResult
                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                            type = "text/csv"
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "FinPilot Export")
                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            android.content.Intent.createChooser(intent, "Export $fileName")
                        )
                    }
                }
                SettingsViewModel.SettingsEvent.AccountDeleted -> {
                    onAccountDeleted()
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                SettingsSectionCard(title = "Preferences") {
                    ThemeModeRow(
                        selectedMode = state.themeMode,
                        onModeSelected = onThemeModeChange
                    )
                    SettingsRowDivider()
                    preferences.forEachIndexed { index, toggle ->
                        SettingsToggleRow(toggle)
                        if (index < preferences.lastIndex) {
                            SettingsRowDivider()
                        }
                    }
                }
            }

            item {
                SettingsSectionCard(title = "Security") {
                    security.forEachIndexed { index, toggle ->
                        SettingsToggleRow(toggle)
                        if (index < security.lastIndex) {
                            SettingsRowDivider()
                        }
                    }
                }
            }

            item {
                SettingsSectionCard(title = "Account") {
                    accountActions.forEachIndexed { index, action ->
                        SettingsNavigationRow(action)
                        if (index < accountActions.lastIndex) {
                            SettingsRowDivider()
                        }
                    }
                }
            }

            item {
                // App version footer
                Text(
                    text = "FinPilot · v1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text(text = "Export data") },
            text = { Text(text = "Create a CSV file and share it from your Downloads folder?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExportDialog = false
                        onExportData()
                    }
                ) {
                    Text(text = "Export")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(text = "Delete account") },
            text = { Text(text = "This permanently removes your account and data. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsRowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 72.dp, end = 20.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        thickness = 0.5.dp
    )
}

@Composable
private fun SettingsToggleRow(item: SettingToggle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(item.icon)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = item.checked,
            onCheckedChange = item.onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsNavigationRow(item: SettingNavigation) {
    val titleColor = if (item.isDestructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(item.icon, isDestructive = item.isDestructive)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = titleColor
            )
            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector, isDestructive: Boolean = false) {
    val containerColor = if (isDestructive) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    val iconTint = if (isDestructive) MaterialTheme.colorScheme.error
    else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun ThemeModeRow(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = when (selectedMode) {
                    ThemeMode.LIGHT -> Icons.Rounded.Brightness7
                    ThemeMode.DARK -> Icons.Rounded.Brightness4
                    ThemeMode.SYSTEM -> Icons.Rounded.PhoneAndroid
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Appearance",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                ThemeMode.values().forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = ThemeMode.values().size
                        ),
                        onClick = { onModeSelected(mode) },
                        selected = selectedMode == mode,
                        icon = {}
                    ) {
                        Text(
                            text = mode.label(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

private fun ThemeMode.label(): String {
    return when (this) {
        ThemeMode.SYSTEM -> "System"
        ThemeMode.LIGHT -> "Light"
        ThemeMode.DARK -> "Dark"
    }
}

private data class SettingToggle(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val checked: Boolean,
    val onCheckedChange: (Boolean) -> Unit,
)

private data class SettingNavigation(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val isDestructive: Boolean = false,
    val onClick: () -> Unit,
)

private fun createCsvExport(context: android.content.Context, csvContent: String): Pair<android.net.Uri, String>? {
    val downloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: return null
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val fileName = "finpilot_export_$timestamp.csv"
    val file = File(downloadsDir, fileName)
    return try {
        file.writeText(csvContent)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        uri to fileName
    } catch (_: Exception) {
        null
    }
}

