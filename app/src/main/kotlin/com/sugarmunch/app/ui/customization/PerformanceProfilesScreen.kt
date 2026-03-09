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
 * EXTREME Performance Profiles Screen
 * Quality presets and performance optimization controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceProfilesScreen(
    onNavigateBack: () -> Unit,
    profile: AnimationProfile,
    onProfileChange: (AnimationProfile) -> Unit
) {
    var showBatterySettings by remember { mutableStateOf(false) }
    var showDeviceSettings by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance Profiles") },
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
            // Quality Presets
            item {
                QualityPresetsSection(
                    profile = profile,
                    onProfileChange = onProfileChange
                )
            }

            // Battery-Based Scaling
            item {
                BatteryScalingCard(
                    expanded = showBatterySettings,
                    onExpand = { showBatterySettings = it },
                    profile = profile,
                    onProfileChange = onProfileChange
                )
            }

            // Device-Specific Optimization
            item {
                DeviceOptimizationCard(
                    expanded = showDeviceSettings,
                    onExpand = { showDeviceSettings = it },
                    profile = profile,
                    onProfileChange = onProfileChange
                )
            }

            // Smart Throttling
            item {
                SmartThrottlingCard(
                    profile = profile,
                    onProfileChange = onProfileChange
                )
            }
        }
    }
}

@Composable
private fun QualityPresetsSection(
    profile: AnimationProfile,
    onProfileChange: (AnimationProfile) -> Unit
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
                text = "Quality Presets",
                style = MaterialTheme.typography.titleLarge,
                color = SugarDimens.Brand.hotPink
            )

            Text(
                text = "Choose a preset that balances visual quality and performance",
                style = MaterialTheme.typography.bodyMedium
            )

            // Preset Grid
            Column(verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)) {
                QualityPreset.entries.forEach { preset ->
                    QualityPresetRow(
                        preset = preset,
                        isSelected = profile.qualityPreset == preset,
                        onClick = { onProfileChange(profile.copy(qualityPreset = preset)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityPresetRow(
    preset: QualityPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (title, description, icon) = when (preset) {
        QualityPreset.ULTRA -> "Ultra" to "All effects, max particles, 60fps" to "✨"
        QualityPreset.HIGH -> "High" to "Most effects, high particles" to "🌟"
        QualityPreset.MEDIUM -> "Medium" to "Balanced performance" to "⚖️"
        QualityPreset.LOW -> "Low" to "Reduced effects" to "📉"
        QualityPreset.POWER_SAVER -> "Power Saver" to "Minimal animations" to "🔋"
        QualityPreset.ACCESSIBILITY -> "Accessibility" to "Essential only" to "♿"
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        color = if (isSelected) {
            SugarDimens.Brand.hotPink.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.lg),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineSmall
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isSelected) SugarDimens.Brand.hotPink else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun BatteryScalingCard(
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    profile: AnimationProfile,
    onProfileChange: (AnimationProfile) -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Battery-Based Scaling",
                        style = MaterialTheme.typography.titleMedium,
                        color = SugarDimens.Brand.mint
                    )
                    Text(
                        text = "Automatically adjust quality based on battery level",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = { onExpand(!expanded) }) {
                    Icon(
                        imageVector = if (expanded) {
                            androidx.compose.material.icons.Icons.Filled.ExpandLess
                        } else {
                            androidx.compose.material.icons.Icons.Filled.ExpandMore
                        },
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            if (expanded) {
                HorizontalDivider()

                // Auto-reduce at 20% battery
                SwitchWithLabel(
                    label = "Auto-reduce at 20% battery",
                    checked = profile.powerSaveMode,
                    onCheckedChange = {
                        onProfileChange(profile.copy(powerSaveMode = it))
                    }
                )

                // Auto-reduce at 50% battery
                SwitchWithLabel(
                    label = "Auto-reduce at 50% battery",
                    checked = profile.qualityPreset == QualityPreset.LOW,
                    onCheckedChange = {
                        onProfileChange(
                            profile.copy(
                                qualityPreset = if (it) QualityPreset.LOW else QualityPreset.MEDIUM
                            )
                        )
                    }
                )

                // Disable on battery saver
                SwitchWithLabel(
                    label = "Disable animations on battery saver",
                    checked = profile.reduceMotion,
                    onCheckedChange = {
                        onProfileChange(profile.copy(reduceMotion = it))
                    }
                )

                // Always full performance when charging
                SwitchWithLabel(
                    label = "Always full performance when charging",
                    checked = profile.qualityPreset == QualityPreset.ULTRA,
                    onCheckedChange = {
                        onProfileChange(
                            profile.copy(
                                qualityPreset = if (it) QualityPreset.ULTRA else QualityPreset.HIGH
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun DeviceOptimizationCard(
    expanded: Boolean,
    onExpand: (Boolean) -> Unit,
    profile: AnimationProfile,
    onProfileChange: (AnimationProfile) -> Unit
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Device Optimization",
                        style = MaterialTheme.typography.titleMedium,
                        color = SugarDimens.Brand.candyOrange
                    )
                    Text(
                        text = "Fine-tune performance for your device",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                IconButton(onClick = { onExpand(!expanded) }) {
                    Icon(
                        imageVector = if (expanded) {
                            androidx.compose.material.icons.Icons.Filled.ExpandLess
                        } else {
                            androidx.compose.material.icons.Icons.Filled.ExpandMore
                        },
                        contentDescription = if (expanded) "Collapse" else "Expand"
                    )
                }
            }

            if (expanded) {
                HorizontalDivider()

                // Frame Rate Cap
                Text("Frame Rate Cap", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                ) {
                    FrameRateCap.entries.forEach { cap ->
                        FilterChip(
                            selected = profile.frameRateCap == cap,
                            onClick = { onProfileChange(profile.copy(frameRateCap = cap)) },
                            label = {
                                Text(
                                    when (cap) {
                                        FrameRateCap.FPS_30 -> "30"
                                        FrameRateCap.FPS_60 -> "60"
                                        FrameRateCap.FPS_90 -> "90"
                                        FrameRateCap.FPS_120 -> "120"
                                        FrameRateCap.UNLIMITED -> "∞"
                                    }
                                )
                            }
                        )
                    }
                }

                // GPU/CPU Preference
                Text("Render Preference", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                ) {
                    FilterChip(
                        selected = true,
                        onClick = { },
                        label = { Text("Auto-detect") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text("GPU Preferred") }
                    )
                    FilterChip(
                        selected = false,
                        onClick = { },
                        label = { Text("CPU Preferred") }
                    )
                }
            }
        }
    }
}

@Composable
private fun SmartThrottlingCard(
    profile: AnimationProfile,
    onProfileChange: (AnimationProfile) -> Unit
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
                text = "Smart Throttling",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            Text(
                text = "Automatically reduce animations in specific scenarios",
                style = MaterialTheme.typography.bodySmall
            )

            // Reduce when app in background
            SwitchWithLabel(
                label = "Reduce when app in background",
                checked = true,
                onCheckedChange = { }
            )

            // Reduce during calls
            SwitchWithLabel(
                label = "Reduce during phone calls",
                checked = true,
                onCheckedChange = { }
            )

            // Reduce when screen recording
            SwitchWithLabel(
                label = "Reduce when screen recording",
                checked = false,
                onCheckedChange = { }
            )

            // Reduce based on thermal state
            SwitchWithLabel(
                label = "Reduce based on thermal state",
                checked = true,
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
