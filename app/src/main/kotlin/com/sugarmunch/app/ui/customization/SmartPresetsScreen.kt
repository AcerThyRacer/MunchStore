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
 * EXTREME Smart Presets Screen
 * Contextual presets and AI-powered suggestions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartPresetsScreen(
    onNavigateBack: () -> Unit,
    presets: List<PresetConfig>,
    onPresetActivated: (PresetConfig) -> Unit,
    onPresetCreated: (PresetConfig) -> Unit,
    onPresetDeleted: (String) -> Unit
) {
    var showCreatePreset by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Presets") },
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { showCreatePreset = true }) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Add,
                            contentDescription = "Create Preset"
                        )
                    }
                }
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
            // AI-Powered Suggestions
            item {
                AiSuggestionsSection(
                    presets = presets,
                    onSuggestionAccepted = { /* Accept AI suggestion */ }
                )
            }

            // Contextual Presets
            item {
                Text(
                    text = "Contextual Presets",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.hotPink
                )
            }

            items(PresetCategory.entries) { category ->
                val categoryPresets = presets.filter { it.category == category }
                if (categoryPresets.isNotEmpty()) {
                    PresetCategoryCard(
                        category = category,
                        presets = categoryPresets,
                        onPresetActivated = onPresetActivated,
                        onPresetDeleted = onPresetDeleted
                    )
                }
            }

            // Custom Presets
            item {
                val customPresets = presets.filter { it.category == PresetCategory.CUSTOM }
                if (customPresets.isNotEmpty()) {
                    CustomPresetsSection(
                        presets = customPresets,
                        onPresetActivated = onPresetActivated,
                        onPresetDeleted = onPresetDeleted
                    )
                }
            }

            // Preset Evolution
            item {
                PresetEvolutionSection(
                    presets = presets
                )
            }
        }
    }
}

@Composable
private fun AiSuggestionsSection(
    presets: List<PresetConfig>,
    onSuggestionAccepted: (PresetConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = SugarDimens.Brand.hotPink.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✨ AI Suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.hotPink
                )
                AssistChip(
                    onClick = { },
                    label = { Text("Learn More") },
                    leadingIcon = {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Text(
                text = "Based on your usage patterns, we suggest:",
                style = MaterialTheme.typography.bodySmall
            )

            // AI Suggestions
            val suggestions = listOf(
                "Create a 'Focus Mode' preset with minimal animations",
                "Try a warmer color temperature in the evening",
                "Enable battery saver preset at 30% battery"
            )

            suggestions.forEach { suggestion ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
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
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Check,
                                contentDescription = "Accept",
                                tint = SugarDimens.Brand.mint
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetCategoryCard(
    category: PresetCategory,
    presets: List<PresetConfig>,
    onPresetActivated: (PresetConfig) -> Unit,
    onPresetDeleted: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = category.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium,
                    color = when (category) {
                        PresetCategory.MORNING -> SugarDimens.Brand.yellow
                        PresetCategory.EVENING -> SugarDimens.Brand.candyOrange
                        PresetCategory.NIGHT -> SugarDimens.Brand.deepPurple
                        PresetCategory.READING -> SugarDimens.Brand.mint
                        PresetCategory.GAMING -> SugarDimens.Brand.hotPink
                        PresetCategory.BATTERY_SAVER -> SugarDimens.Brand.green
                        PresetCategory.PRODUCTIVITY -> SugarDimens.Brand.bubblegumBlue
                        PresetCategory.CUSTOM -> MaterialTheme.colorScheme.onSurface
                    }
                )
            }

            presets.forEach { preset ->
                PresetRow(
                    preset = preset,
                    onActivate = { onPresetActivated(preset) },
                    onDelete = { onPresetDeleted(preset.id) }
                )
            }
        }
    }
}

@Composable
private fun PresetRow(
    preset: PresetConfig,
    onActivate: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onActivate),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = preset.name,
                    style = MaterialTheme.typography.labelLarge
                )
                if (preset.description.isNotEmpty()) {
                    Text(
                        text = preset.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                ) {
                    if (preset.usageCount > 0) {
                        Text(
                            text = "Used ${preset.usageCount} times",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    if (preset.rating > 0) {
                        Text(
                            text = "★ ${preset.rating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SugarDimens.Brand.yellow
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                Button(
                    onClick = onActivate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SugarDimens.Brand.hotPink
                    )
                ) {
                    Text("Activate")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun CustomPresetsSection(
    presets: List<PresetConfig>,
    onPresetActivated: (PresetConfig) -> Unit,
    onPresetDeleted: (String) -> Unit
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
                text = "Custom Presets",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            presets.forEach { preset ->
                PresetRow(
                    preset = preset,
                    onActivate = { onPresetActivated(preset) },
                    onDelete = { onPresetDeleted(preset.id) }
                )
            }
        }
    }
}

@Composable
private fun PresetEvolutionSection(
    presets: List<PresetConfig>
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
                text = "Preset Evolution",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            Text(
                text = "Auto-refine presets based on your adjustments",
                style = MaterialTheme.typography.bodySmall
            )

            // Auto-refine toggle
            SwitchWithLabel(
                label = "Auto-refine presets",
                checked = true,
                onCheckedChange = { }
            )

            // Merge similar presets
            SwitchWithLabel(
                label = "Merge similar presets",
                checked = false,
                onCheckedChange = { }
            )

            // Archive unused presets
            SwitchWithLabel(
                label = "Archive unused presets (30 days)",
                checked = true,
                onCheckedChange = { }
            )

            // Usage stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ProfileStat(
                    label = "Active Presets",
                    value = presets.count { it.lastUsedAt != null && it.lastUsedAt!! > System.currentTimeMillis() - 86400000 * 7 }.toString()
                )
                ProfileStat(
                    label = "Archived",
                    value = presets.count { it.usageCount == 0 }.toString()
                )
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

@Composable
private fun ProfileStat(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = SugarDimens.Brand.hotPink
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}
