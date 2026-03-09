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
 * EXTREME Effect Intensity Matrix Screen
 * Per-effect controls and effect combinations
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EffectMatrixScreen(
    onNavigateBack: () -> Unit,
    effectConfigs: Map<String, EffectConfig>,
    onEffectConfigsChange: (Map<String, EffectConfig>) -> Unit
) {
    var selectedEffect by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Effect Matrix") },
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
            // Effect List
            items(50) { index ->
                val effectId = "effect_$index"
                val config = effectConfigs[effectId] ?: EffectConfig()

                EffectRow(
                    effectId = effectId,
                    effectName = "Effect ${index + 1}",
                    config = config,
                    isEnabled = config.enabled,
                    onToggleChange = { enabled ->
                        val newConfigs = effectConfigs.toMutableMap()
                        newConfigs[effectId] = config.copy(enabled = enabled)
                        onEffectConfigsChange(newConfigs)
                    },
                    onClick = { selectedEffect = effectId }
                )
            }

            // Effect Combinations
            item {
                EffectCombinationsSection(
                    effectConfigs = effectConfigs,
                    onEffectConfigsChange = onEffectConfigsChange
                )
            }

            // Context-Aware Effects
            item {
                ContextAwareEffectsSection(
                    effectConfigs = effectConfigs,
                    onEffectConfigsChange = onEffectConfigsChange
                )
            }
        }
    }
}

@Composable
private fun EffectRow(
    effectId: String,
    effectName: String,
    config: EffectConfig,
    isEnabled: Boolean,
    onToggleChange: (Boolean) -> Unit,
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = effectName,
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                ) {
                    AssistChip(
                        onClick = { },
                        label = { Text("Intensity: ${config.intensity}") },
                        leadingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.BlurOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text("Speed: ${config.speed}x") },
                        leadingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Speed,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleChange
            )
        }
    }
}

@Composable
private fun EffectCombinationsSection(
    effectConfigs: Map<String, EffectConfig>,
    onEffectConfigsChange: (Map<String, EffectConfig>) -> Unit
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
                text = "Effect Combinations",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )

            Text(
                text = "Create and save effect presets",
                style = MaterialTheme.typography.bodySmall
            )

            // Save current combination button
            Button(
                onClick = { /* Save preset */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Current Combination as Preset")
            }

            // Effect layers
            Text("Effect Layers", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("Background") }
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("Midground") }
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("Foreground") }
                )
            }
        }
    }
}

@Composable
private fun ContextAwareEffectsSection(
    effectConfigs: Map<String, EffectConfig>,
    onEffectConfigsChange: (Map<String, EffectConfig>) -> Unit
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
                text = "Context-Aware Effects",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            // Different effects for different screens
            SwitchWithLabel(
                label = "Different effects per screen",
                checked = true,
                onCheckedChange = { }
            )

            // Different effects for different times
            SwitchWithLabel(
                label = "Time-based effect activation",
                checked = false,
                onCheckedChange = { }
            )

            // Different effects for different battery levels
            SwitchWithLabel(
                label = "Battery-based effect scaling",
                checked = true,
                onCheckedChange = { }
            )

            // Different effects for different apps
            SwitchWithLabel(
                label = "App-specific effects",
                checked = false,
                onCheckedChange = { }
            )
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
