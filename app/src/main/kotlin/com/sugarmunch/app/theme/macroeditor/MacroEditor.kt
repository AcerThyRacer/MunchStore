package com.sugarmunch.app.theme.macroeditor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sugarmunch.app.theme.macros.*

/**
 * Macro Editor - Create and manage programmable theme macros
 *
 * Features:
 * - Visual macro builder
 * - Trigger configuration
 * - Action selection
 * - Condition builder
 * - Macro testing
 * - Import/Export
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroEditor(
    macros: List<ThemeMacro>,
    onMacroAdded: (ThemeMacro) -> Unit,
    onMacroUpdated: (ThemeMacro) -> Unit,
    onMacroDeleted: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedMacro by remember { mutableStateOf<ThemeMacro?>(null) }
    var showCreateMacroDialog by remember { mutableStateOf(false) }
    var showTestMacroDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Macro Editor")
                        Text(
                            text = "${macros.count { it.isEnabled }}/${macros.size} macros active",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showImportExportDialog = true }) {
                        Icon(Icons.Outlined.Sync, contentDescription = "Import/Export")
                    }
                }
            )
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Macro List
            MacroListPanel(
                macros = macros,
                selectedMacro = selectedMacro,
                onMacroSelected = { selectedMacro = it },
                onMacroToggled = { macro ->
                    onMacroUpdated(macro.copy(isEnabled = !macro.isEnabled))
                },
                onMacroDeleted = onMacroDeleted,
                onCreateClick = { showCreateMacroDialog = true },
                modifier = Modifier.weight(1f)
            )

            // Macro Details
            if (selectedMacro != null) {
                MacroDetailPanel(
                    macro = selectedMacro!!,
                    onMacroUpdated = onMacroUpdated,
                    onTestClick = { showTestMacroDialog = true },
                    modifier = Modifier.weight(1.5f)
                )
            } else {
                EmptyStatePanel(
                    title = "No Macro Selected",
                    description = "Select a macro to view details or create a new one",
                    icon = Icons.Outlined.Timeline,
                    onCreateClick = { showCreateMacroDialog = true },
                    modifier = Modifier.weight(1.5f)
                )
            }
        }
    }

    // Create/Edit Macro Dialog
    if (showCreateMacroDialog) {
        CreateMacroDialog(
            onDismiss = { showCreateMacroDialog = false },
            onMacroCreated = { newMacro ->
                onMacroAdded(newMacro)
                showCreateMacroDialog = false
            }
        )
    }

    // Test Macro Dialog
    if (showTestMacroDialog && selectedMacro != null) {
        TestMacroDialog(
            macro = selectedMacro!!,
            onDismiss = { showTestMacroDialog = false }
        )
    }

    // Import/Export Dialog
    if (showImportExportDialog) {
        ImportExportDialog(
            macros = macros,
            onDismiss = { showImportExportDialog = false },
            onImport = { importedMacros ->
                importedMacros.forEach { onMacroAdded(it) }
            }
        )
    }
}

// ═════════════════════════════════════════════════════════════
// MACRO LIST PANEL
// ═════════════════════════════════════════════════════════════

@Composable
private fun MacroListPanel(
    macros: List<ThemeMacro>,
    selectedMacro: ThemeMacro?,
    onMacroSelected: (ThemeMacro) -> Unit,
    onMacroToggled: (ThemeMacro) -> Unit,
    onMacroDeleted: (String) -> Unit,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(8.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MACROS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                onClick = onCreateClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Macro", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Macro List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(macros, key = { it.id }) { macro ->
                MacroListItem(
                    macro = macro,
                    isSelected = macro.id == selectedMacro?.id,
                    onClick = { onMacroSelected(macro) },
                    onToggle = { onMacroToggled(macro) },
                    onDelete = { onMacroDeleted(macro.id) }
                )
            }

            if (macros.isEmpty()) {
                item {
                    EmptyMacroList(modifier = Modifier.padding(32.dp))
                }
            }
        }

        // Preset Macros Section
        PresetMacrosSection(onMacroAdded = { })
    }
}

@Composable
private fun MacroListItem(
    macro: ThemeMacro,
    isSelected: Boolean,
    onClick: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (!macro.isEnabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Indicator
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (macro.isEnabled) Color(0xFF4CAF50) else Color.Gray
                    )
            )

            // Macro Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = macro.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (macro.isEnabled) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
                Text(
                    text = getTriggerDescription(macro.trigger),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            // Toggle Switch
            Switch(
                checked = macro.isEnabled,
                onCheckedChange = { onToggle() },
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PresetMacrosSection(onMacroAdded: (ThemeMacro) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "PRESET MACROS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(PRESET_MACRO_TEMPLATES) { template ->
                    AssistChip(
                        onClick = { onMacroAdded(template) },
                        label = { Text(template.name) },
                        leadingIcon = {
                            Icon(Icons.Default.Add, contentDescription = null, Modifier.size(16.dp))
                        }
                    )
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// MACRO DETAIL PANEL
// ═════════════════════════════════════════════════════════════

@Composable
private fun MacroDetailPanel(
    macro: ThemeMacro,
    onMacroUpdated: (ThemeMacro) -> Unit,
    onTestClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var expandedSection by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
            .horizontalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = macro.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = macro.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked = macro.isEnabled,
                onCheckedChange = { onMacroUpdated(macro.copy(isEnabled = it)) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Trigger Section
        MacroSection(
            title = "TRIGGER",
            icon = Icons.Default.Trigger,
            isExpanded = expandedSection == "trigger",
            onExpandClick = { expandedSection = if (expandedSection == "trigger") null else "trigger" }
        ) {
            TriggerEditor(
                trigger = macro.trigger,
                onTriggerChange = { newTrigger ->
                    onMacroUpdated(macro.copy(trigger = newTrigger))
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Action Section
        MacroSection(
            title = "ACTION",
            icon = Icons.Default.PlayArrow,
            isExpanded = expandedSection == "action",
            onExpandClick = { expandedSection = if (expandedSection == "action") null else "action" }
        ) {
            ActionEditor(
                action = macro.action,
                onActionChange = { newAction ->
                    onMacroUpdated(macro.copy(action = newAction))
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Conditions Section
        MacroSection(
            title = "CONDITIONS (${macro.conditions.size})",
            icon = Icons.Default.Tune,
            isExpanded = expandedSection == "conditions",
            onExpandClick = { expandedSection = if (expandedSection == "conditions") null else "conditions" }
        ) {
            ConditionsEditor(
                conditions = macro.conditions,
                onConditionsChange = { newConditions ->
                    onMacroUpdated(macro.copy(conditions = newConditions))
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Test Button
        Button(
            onClick = onTestClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Test, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Macro")
        }
    }
}

@Composable
private fun TriggerEditor(
    trigger: MacroTrigger,
    onTriggerChange: (MacroTrigger) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Trigger Type Selector
        Text(
            text = "Trigger Type: ${getTriggerName(trigger)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(TRIGGER_TYPES) { type ->
                FilterChip(
                    selected = type::class.java == trigger::class.java,
                    onClick = { onTriggerChange(createDefaultTrigger(type)) },
                    label = { Text(type.simpleName ?: "Unknown") }
                )
            }
        }

        // Trigger-specific configuration would go here
        // For brevity, showing a placeholder
        OutlinedTextField(
            value = "Configure trigger parameters...",
            onValueChange = { },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            maxLines = 2
        )
    }
}

@Composable
private fun ActionEditor(
    action: MacroAction,
    onActionChange: (MacroAction) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Action Type: ${getActionName(action)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(ACTION_TYPES) { type ->
                FilterChip(
                    selected = type::class.java == action::class.java,
                    onClick = { onActionChange(createDefaultAction(type)) },
                    label = { Text(type.simpleName ?: "Unknown") }
                )
            }
        }
    }
}

@Composable
private fun ConditionsEditor(
    conditions: List<MacroCondition>,
    onConditionsChange: (List<MacroCondition>) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Conditions must ALL be true for macro to execute",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            IconButton(onClick = { /* Add condition */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Condition")
            }
        }

        conditions.forEach { condition ->
            ConditionChip(
                condition = condition,
                onRemove = { /* Remove condition */ }
            )
        }

        if (conditions.isEmpty()) {
            Text(
                text = "No conditions - macro will execute whenever trigger fires",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════
// CREATE MACRO DIALOG
// ═════════════════════════════════════════════════════════════

@Composable
private fun CreateMacroDialog(
    onDismiss: () -> Unit,
    onMacroCreated: (ThemeMacro) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedTriggerType by remember { mutableStateOf<MacroTrigger>(MacroTrigger.TimeTrigger(8, 0)) }
    var selectedActionType by remember { mutableStateOf<MacroAction>(MacroAction.SwitchTheme("default")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Macro") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Macro Name") },
                        placeholder = { Text("My Custom Macro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        placeholder = { Text("What does this macro do?") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )
                }

                item {
                    Text(
                        text = "Select Trigger Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TRIGGER_TYPES) { type ->
                            FilterChip(
                                selected = type::class.java == selectedTriggerType::class.java,
                                onClick = { selectedTriggerType = createDefaultTrigger(type) },
                                label = { Text(type.simpleName ?: "Unknown") }
                            )
                        }
                    }
                }

                item {
                    Text(
                        text = "Select Action Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ACTION_TYPES) { type ->
                            FilterChip(
                                selected = type::class.java == selectedActionType::class.java,
                                onClick = { selectedActionType = createDefaultAction(type) },
                                label = { Text(type.simpleName ?: "Unknown") }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newMacro = ThemeMacro(
                        name = name.ifBlank { "New Macro" },
                        description = description,
                        trigger = selectedTriggerType,
                        action = selectedActionType
                    )
                    onMacroCreated(newMacro)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create Macro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// ═════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═════════════════════════════════════════════════════════════

@Composable
private fun MacroSection(
    title: String,
    icon: ImageVector,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = onExpandClick) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand"
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                content()
            }
        }
    }
}

@Composable
private fun ConditionChip(
    condition: MacroCondition,
    onRemove: () -> Unit
) {
    AssistChip(
        onClick = { },
        label = { Text(getConditionName(condition)) },
        trailingIcon = {
            IconButton(onClick = onRemove, Modifier.size(16.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", Modifier.size(14.dp))
            }
        }
    )
}

@Composable
private fun EmptyStatePanel(
    title: String,
    description: String,
    icon: ImageVector,
    onCreateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onCreateClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create Macro")
        }
    }
}

@Composable
private fun EmptyMacroList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Outlined.Timeline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "No macros yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═════════════════════════════════════════════════════════════
// UTILITIES
// ═════════════════════════════════════════════════════════════

private fun getTriggerName(trigger: MacroTrigger): String {
    return trigger::class.java.simpleName.replace("Trigger", "")
}

private fun getActionName(action: MacroAction): String {
    return action::class.java.simpleName.replace("Action", "")
}

private fun getConditionName(condition: MacroCondition): String {
    return condition::class.java.simpleName
}

private fun getTriggerDescription(trigger: MacroTrigger): String {
    return when (trigger) {
        is MacroTrigger.TimeTrigger -> "Daily at ${String.format("%02d:%02d", trigger.hour, trigger.minute)}"
        is MacroTrigger.AppTrigger -> "When ${trigger.packageNames.size} app(s) launch"
        is MacroTrigger.BatteryTrigger -> "Battery ${trigger.condition.displayName} ${trigger.threshold}%"
        is MacroTrigger.MusicTrigger -> "When music ${if (trigger.onPlay) "plays" else "pauses"}"
        is MacroTrigger.WeatherTrigger -> "When weather is ${trigger.conditions.joinToString()}"
        is MacroTrigger.IntervalTrigger -> "Every ${trigger.intervalMinutes} minutes"
    }
}

private val TRIGGER_TYPES = listOf(
    MacroTrigger.TimeTrigger::class,
    MacroTrigger.AppTrigger::class,
    MacroTrigger.BatteryTrigger::class,
    MacroTrigger.MusicTrigger::class,
    MacroTrigger.WeatherTrigger::class,
    MacroTrigger.IntervalTrigger::class
)

private val ACTION_TYPES = listOf(
    MacroAction.SwitchTheme::class,
    MacroAction.AnimateIntensity::class,
    MacroAction.ApplyGranularConfig::class,
    MacroAction.CycleThemes::class,
    MacroAction.RandomTheme::class
)

private fun createDefaultTrigger(type: Class<out MacroTrigger>): MacroTrigger {
    return when (type) {
        MacroTrigger.TimeTrigger::class -> MacroTrigger.TimeTrigger(8, 0)
        MacroTrigger.AppTrigger::class -> MacroTrigger.AppTrigger(setOf())
        MacroTrigger.BatteryTrigger::class -> MacroTrigger.BatteryTrigger(20, MacroTrigger.BatteryCondition.BELOW)
        MacroTrigger.MusicTrigger::class -> MacroTrigger.MusicTrigger()
        MacroTrigger.WeatherTrigger::class -> MacroTrigger.WeatherTrigger(setOf(WeatherCondition.RAINY))
        MacroTrigger.IntervalTrigger::class -> MacroTrigger.IntervalTrigger(30)
        else -> MacroTrigger.TimeTrigger(8, 0)
    }
}

private fun createDefaultAction(type: Class<out MacroAction>): MacroAction {
    return when (type) {
        MacroAction.SwitchTheme::class -> MacroAction.SwitchTheme("default")
        MacroAction.AnimateIntensity::class -> MacroAction.AnimateIntensity(targetThemeIntensity = 1.5f)
        MacroAction.ApplyGranularConfig::class -> MacroAction.ApplyGranularConfig(GranularThemeConfig.DEFAULT)
        MacroAction.CycleThemes::class -> MacroAction.CycleThemes(listOf("theme1", "theme2"), 5)
        MacroAction.RandomTheme::class -> MacroAction.RandomTheme()
        else -> MacroAction.SwitchTheme("default")
    }
}

private val PRESET_MACRO_TEMPLATES = listOf(
    PresetMacros.CIRCADIAN_RHYTHM.first(),
    PresetMacros.GAMING_MODE,
    PresetMacros.BATTERY_SAVER,
    PresetMacros.MUSIC_VISUALIZER,
    PresetMacros.RAINY_DAY
)

// Test and Import/Export dialogs would be implemented similarly
@Composable
private fun TestMacroDialog(macro: ThemeMacro, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Test Macro: ${macro.name}") },
        text = { Text("Macro testing simulation would run here...") },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ImportExportDialog(
    macros: List<ThemeMacro>,
    onDismiss: () -> Unit,
    onImport: (List<ThemeMacro>) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import/Export Macros") },
        text = { Text("JSON import/export functionality...") },
        confirmButton = {
            Button(onClick = onDismiss) { Text("Close") }
        }
    )
}
