package com.bpeople.finpilot.ui.screens.profile

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import androidx.compose.ui.tooling.preview.Preview
import com.bpeople.finpilot.ui.theme.FinPilotTheme
import com.bpeople.finpilot.data.model.ThemeMode
import kotlinx.coroutines.flow.emptyFlow

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
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .padding(4.dp)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                Spacer(modifier = Modifier.height(4.dp))
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
                Spacer(modifier = Modifier.height(4.dp))
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
                Spacer(modifier = Modifier.height(24.dp))
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
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            content()
        }
    }
}

@Composable
private fun SettingsRowDivider() {
    HorizontalDivider(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )
}

@Composable
private fun SettingsToggleRow(item: SettingToggle) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(item.icon)
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
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
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsNavigationRow(item: SettingNavigation) {
    val titleColor = if (item.isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .clickable { item.onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SettingsIcon(item.icon, isDestructive = item.isDestructive)
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsIcon(icon: ImageVector, isDestructive: Boolean = false) {
    val containerColor = if (isDestructive) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    }
    val iconTint = if (isDestructive) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.primary
    }
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint
        )
    }
}

@Composable
private fun ThemeModeRow(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Appearance",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeMode.values().forEachIndexed { index, mode ->
                val isSelected = selectedMode == mode
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = ThemeMode.values().size
                    ),
                    onClick = { onModeSelected(mode) },
                    selected = isSelected,
                    icon = {}
                ) {
                    Text(text = mode.label())
                }
            }
        }
        Text(
            text = "System follows your device theme",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SettingsScreenPreview() {
    FinPilotTheme {
        SettingsScreen(
            state = SettingsViewModel.SettingsUiState(
                notificationsEnabled = true,
                cloudSyncEnabled = true,
                biometricsEnabled = true,
                themeMode = ThemeMode.SYSTEM
            ),
            events = emptyFlow(),
            onNavigateBack = {},
            onNotificationsChange = {},
            onThemeModeChange = {},
            onCloudSyncChange = {},
            onBiometricsChange = {},
            onChangePassword = {},
            onExportData = {},
            onDeleteAccount = {},
            onAccountDeleted = {}
        )
    }
}
