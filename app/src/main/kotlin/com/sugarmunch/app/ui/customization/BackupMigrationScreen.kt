package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Backup & Migration Screen
 * Backup, restore, export, import, and migration tools
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupMigrationScreen(
    onNavigateBack: () -> Unit,
    onBackupCreated: (BackupType, List<BackupCategory>) -> Unit,
    onRestoreRequested: (String, Boolean) -> Unit,
    onExportRequested: (List<BackupCategory>) -> Unit,
    onImportRequested: (String, Boolean) -> Unit
) {
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var importJson by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Migration") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
        ) {
            // Backup Options
            item {
                BackupOptionsSection(
                    onCreateBackup = { showBackupDialog = true }
                )
            }

            // Restore Options
            item {
                RestoreOptionsSection(
                    onRestore = { showRestoreDialog = true }
                )
            }

            // Export/Import
            item {
                ExportImportSection(
                    onExport = { showExportDialog = true },
                    importJson = importJson,
                    onImportJsonChange = { importJson = it },
                    onImport = { json, merge ->
                        onImportRequested(json, merge)
                        importJson = ""
                    }
                )
            }

            // Migration Tools
            item {
                MigrationToolsSection()
            }

            // Backup History
            item {
                BackupHistorySection()
            }
        }
    }

    // Backup Dialog
    if (showBackupDialog) {
        BackupDialog(
            onDismiss = { showBackupDialog = false },
            onBackup = { type, categories ->
                onBackupCreated(type, categories)
                showBackupDialog = false
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        RestoreDialog(
            onDismiss = { showRestoreDialog = false },
            onRestore = { backupId, merge ->
                onRestoreRequested(backupId, merge)
                showRestoreDialog = false
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            onDismiss = { showExportDialog = false },
            onExport = { categories ->
                onExportRequested(categories)
                showExportDialog = false
            }
        )
    }
}

@Composable
private fun BackupOptionsSection(
    onCreateBackup: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Backup Options",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )

            // Full Backup
            BackupOptionRow(
                icon = "💾",
                title = "Full Backup",
                description = "Backup all settings, profiles, and presets",
                onClick = onCreateBackup
            )

            // Partial Backup
            BackupOptionRow(
                icon = "📦",
                title = "Partial Backup",
                description = "Select specific categories to backup",
                onClick = onCreateBackup
            )

            // Scheduled Backups
            BackupOptionRow(
                icon = "⏰",
                title = "Scheduled Backups",
                description = "Daily, weekly, or monthly automatic backups",
                onClick = { }
            )

            // Cloud Backup
            BackupOptionRow(
                icon = "☁️",
                title = "Cloud Backup",
                description = "Backup to Google Drive or Dropbox",
                onClick = { }
            )

            // Local Backup
            BackupOptionRow(
                icon = "📁",
                title = "Local Backup",
                description = "Save backup file to device storage",
                onClick = { }
            )

            // Incremental Backups
            SwitchWithLabel(
                label = "Incremental Backups (faster)",
                checked = true,
                onCheckedChange = { }
            )
        }
    }
}

@Composable
private fun BackupOptionRow(
    icon: String,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.ChevronRight,
                contentDescription = "Go"
            )
        }
    }
}

@Composable
private fun RestoreOptionsSection(
    onRestore: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Restore Options",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            // Full Restore
            BackupOptionRow(
                icon = "🔄",
                title = "Full Restore",
                description = "Restore all settings from backup",
                onClick = onRestore
            )

            // Selective Restore
            BackupOptionRow(
                icon = "🎯",
                title = "Selective Restore",
                description = "Choose specific categories to restore",
                onClick = onRestore
            )

            // Merge with Current
            SwitchWithLabel(
                label = "Merge with current settings",
                checked = false,
                onCheckedChange = { }
            )

            // Preview Before Restore
            SwitchWithLabel(
                label = "Preview before restore",
                checked = true,
                onCheckedChange = { }
            )

            // Rollback to Previous Backup
            BackupOptionRow(
                icon = "⏮️",
                title = "Rollback",
                description = "Restore to previous backup version",
                onClick = onRestore
            )
        }
    }
}

@Composable
private fun ExportImportSection(
    onExport: () -> Unit,
    importJson: String,
    onImportJsonChange: (String) -> Unit,
    onImport: (String, Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Export / Import",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            // Export Button
            Button(
                onClick = onExport,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Settings as JSON")
            }

            HorizontalDivider()

            // Import Section
            Text(
                text = "Import from JSON",
                style = MaterialTheme.typography.labelLarge
            )

            OutlinedTextField(
                value = importJson,
                onValueChange = onImportJsonChange,
                label = { Text("Paste JSON string") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                maxLines = 5
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                var merge by remember { mutableStateOf(false) }

                Checkbox(
                    checked = merge,
                    onCheckedChange = { merge = it }
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Merge with current settings",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "If unchecked, current settings will be replaced",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            Button(
                onClick = { onImport(importJson, merge) },
                modifier = Modifier.fillMaxWidth(),
                enabled = importJson.isNotEmpty()
            ) {
                Text("Import Settings")
            }
        }
    }
}

@Composable
private fun MigrationToolsSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Migration Tools",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            // Import from Other Launchers
            MigrationOptionRow(
                icon = "📲",
                title = "Import from Other Launchers",
                description = "Migrate settings from Nova, Microsoft, etc."
            )

            // Import from Previous Versions
            MigrationOptionRow(
                icon = "⬆️",
                title = "Import from Previous Versions",
                description = "Upgrade old SugarMunch settings"
            )

            // Settings Converter
            MigrationOptionRow(
                icon = "🔄",
                title = "Settings Converter",
                description = "Convert old format to new format"
            )

            // Conflict Resolution
            Text("Conflict Resolution", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("Keep Both") }
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("Overwrite") }
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("Skip") }
                )
            }
        }
    }
}

@Composable
private fun MigrationOptionRow(
    icon: String,
    title: String,
    description: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleLarge
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            OutlinedButton(onClick = { }) {
                Text("Start")
            }
        }
    }
}

@Composable
private fun BackupHistorySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Backup History",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.yellow
            )

            // Recent backups
            val backups = listOf(
                "Full Backup - Today, 10:30 AM - 2.5 MB" to "2026-03-08 10:30",
                "Partial Backup - Yesterday - 1.2 MB" to "2026-03-07 15:45",
                "Full Backup - 3 days ago - 2.4 MB" to "2026-03-05 09:00"
            )

            backups.forEach { (label, timestamp) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = label.first().toString() + label.substring(1).split(" - ").first(),
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                    ) {
                        TextButton(onClick = { }) {
                            Text("Restore")
                        }
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                                contentDescription = "Delete"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwitchWithLabel(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

// Dialog Composables
@Composable
private fun BackupDialog(
    onDismiss: () -> Unit,
    onBackup: (BackupType, List<BackupCategory>) -> Unit
) {
    var backupType by remember { mutableStateOf(BackupType.FULL) }
    var selectedCategories by remember { mutableStateOf(BackupCategory.entries) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Backup") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                // Backup Type
                Text("Backup Type", style = MaterialTheme.typography.labelLarge)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                ) {
                    FilterChip(
                        selected = backupType == BackupType.FULL,
                        onClick = { backupType = BackupType.FULL },
                        label = { Text("Full") }
                    )
                    FilterChip(
                        selected = backupType == BackupType.PARTIAL,
                        onClick = { backupType = BackupType.PARTIAL },
                        label = { Text("Partial") }
                    )
                }

                if (backupType == BackupType.PARTIAL) {
                    Text("Categories", style = MaterialTheme.typography.labelLarge)
                    BackupCategory.entries.forEach { category ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = category in selectedCategories,
                                onCheckedChange = {
                                    selectedCategories = if (it) {
                                        selectedCategories + category
                                    } else {
                                        selectedCategories - category
                                    }
                                }
                            )
                            Text(category.name)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onBackup(backupType, selectedCategories) }) {
                Text("Create Backup")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RestoreDialog(
    onDismiss: () -> Unit,
    onRestore: (String, Boolean) -> Unit
) {
    var selectedBackup by remember { mutableStateOf<String?>(null) }
    var merge by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore Backup") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text("Select Backup", style = MaterialTheme.typography.labelLarge)
                listOf("backup_1", "backup_2", "backup_3").forEach { backupId ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedBackup == backupId,
                            onClick = { selectedBackup = backupId }
                        )
                        Text(backupId, modifier = Modifier.padding(start = SugarDimens.Spacing.sm))
                    }
                }

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = merge,
                        onCheckedChange = { merge = it }
                    )
                    Text(
                        text = "Merge with current settings",
                        modifier = Modifier.padding(start = SugarDimens.Spacing.sm)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedBackup?.let { onRestore(it, merge) } },
                enabled = selectedBackup != null
            ) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (List<BackupCategory>) -> Unit
) {
    var selectedCategories by remember { mutableStateOf(BackupCategory.entries) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Settings") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text("Select categories to export:", style = MaterialTheme.typography.labelLarge)
                BackupCategory.entries.forEach { category ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = category in selectedCategories,
                            onCheckedChange = {
                                selectedCategories = if (it) {
                                    selectedCategories + category
                                } else {
                                    selectedCategories - category
                                }
                            }
                        )
                        Text(category.name)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onExport(selectedCategories) }) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
