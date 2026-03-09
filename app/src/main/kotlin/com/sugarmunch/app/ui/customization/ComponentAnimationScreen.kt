package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Per-Component Animation Control Screen
 * Individual control over every component's animation behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentAnimationScreen(
    onNavigateBack: () -> Unit,
    profile: AnimationProfile,
    onProfileChange: (AnimationProfile) -> Unit
) {
    var expandedComponent by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Component Animations") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Master Toggle
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(SugarDimens.Spacing.md),
                shape = RoundedCornerShape(SugarDimens.Radius.lg)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(SugarDimens.Spacing.lg),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Master Animation Control",
                            style = MaterialTheme.typography.titleMedium,
                            color = SugarDimens.Brand.hotPink
                        )
                        Text(
                            text = "Enable/disable all component animations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Switch(
                        checked = profile.masterEnabled,
                        onCheckedChange = {
                            onProfileChange(profile.copy(masterEnabled = it))
                        }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                // Component List
                item {
                    ComponentListSection(
                        profile = profile,
                        onProfileChange = onProfileChange,
                        expandedComponent = expandedComponent,
                        onComponentExpanded = { expandedComponent = it }
                    )
                }

                // Animation Curve
                item {
                    AnimationCurveSection(
                        profile = profile,
                        onProfileChange = onProfileChange
                    )
                }

                // Stagger Pattern
                item {
                    StaggerPatternSection(
                        profile = profile,
                        onProfileChange = onProfileChange
                    )
                }

                // Quality Preset
                item {
                    QualityPresetSection(
                        profile = profile,
                        onProfileChange = onProfileChange
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentListSection(
    profile: AnimationProfile,
    onProfileChange: (AnimationProfile) -> Unit,
    expandedComponent: String?,
    onComponentExpanded: (String?) -> Unit
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
                text = "Individual Components",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            // Component toggles
            val components = listOf(
                "cards" to "Cards",
                "buttons" to "Buttons",
                "text" to "Text",
                "images" to "Images",
                "icons" to "Icons",
                "dividers" to "Dividers",
                "bottom_sheets" to "Bottom Sheets",
                "dialogs" to "Dialogs",
                "snackbars" to "Snackbars",
                "progress_indicators" to "Progress Indicators"
            )

            components.forEach { (key, label) ->
                ComponentToggleRow(
                    label = label,
                    isEnabled = profile.componentAnimations[key]?.enabled ?: true,
                    config = profile.componentAnimations[key] ?: ComponentAnimationConfig(),
                    onToggleChange = {
                        val newConfigs = profile.componentAnimations.toMutableMap()
                        newConfigs[key] = (newConfigs[key] ?: ComponentAnimationConfig()).copy(enabled = it)
                        onProfileChange(profile.copy(componentAnimations = newConfigs))
                    },
                    isExpanded = expandedComponent == key,
                    onExpand = { onComponentExpanded(if (isExpanded) null else key) }
                )

                // Expanded settings
                if (expandedComponent == key) {
                    ComponentAnimationSettings(
                        config = profile.componentAnimations[key] ?: ComponentAnimationConfig(),
                        onConfigChange = {
                            val newConfigs = profile.componentAnimations.toMutableMap()
                            newConfigs[key] = it
                            onProfileChange(profile.copy(componentAnimations = newConfigs))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ComponentToggleRow(
    label: String,
    isEnabled: Boolean,
    config: ComponentAnimationConfig,
    onToggleChange: (Boolean) -> Unit,
    isExpanded: Boolean,
    onExpand: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onExpand),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge
                    )
                    AssistChip(
                        onClick = { },
                        label = { Text(config.type.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Filled.Animation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggleChange
                )
                Icon(
                    imageVector = if (isExpanded) {
                        androidx.compose.material.icons.Icons.Filled.ExpandLess
                    } else {
                        androidx.compose.material.icons.Icons.Filled.ExpandMore
                    },
                    contentDescription = if (isExpanded) "Collapse" else "Expand"
                )
            }
        }
    }
}

@Composable
private fun ComponentAnimationSettings(
    config: ComponentAnimationConfig,
    onConfigChange: (ComponentAnimationConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SugarDimens.Spacing.md),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
    ) {
        HorizontalDivider()

        // Animation Type
        Text("Animation Type", style = MaterialTheme.typography.labelLarge)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 80.dp),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            items(AnimationType.entries) { type ->
                FilterChip(
                    selected = config.type == type,
                    onClick = { onConfigChange(config.copy(type = type)) },
                    label = { Text(type.name) }
                )
            }
        }

        // Duration
        SliderWithLabel(
            label = "Duration (${config.duration}ms)",
            value = config.duration.toFloat(),
            onValueChange = { onConfigChange(config.copy(duration = it.toInt())) },
            valueRange = 100f..2000f,
            steps = 18
        )

        // Direction
        Text("Animation Direction", style = MaterialTheme.typography.labelLarge)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 70.dp),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            items(AnimationDirection.entries) { direction ->
                FilterChip(
                    selected = config.direction == direction,
                    onClick = { onConfigChange(config.copy(direction = direction)) },
                    label = { Text(direction.name.replace("_", " ")) }
                )
            }
        }

        // Scale Origin
        Text("Scale Origin", style = MaterialTheme.typography.labelLarge)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 70.dp),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            items(ScaleOrigin.entries) { origin ->
                FilterChip(
                    selected = config.scaleOrigin == origin,
                    onClick = { onConfigChange(config.copy(scaleOrigin = origin)) },
                    label = { Text(origin.name.replace("_", " ")) }
                )
            }
        }

        // Delay
        SliderWithLabel(
            label = "Delay (${config.delay}ms)",
            value = config.delay.toFloat(),
            onValueChange = { onConfigChange(config.copy(delay = it.toInt())) },
            valueRange = 0f..1000f,
            steps = 19
        )
    }
}

@Composable
private fun AnimationCurveSection(
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
                text = "Animation Curve",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                items(AnimationCurve.entries) { curve ->
                    FilterChip(
                        selected = profile.animationCurve == curve,
                        onClick = { onProfileChange(profile.copy(animationCurve = curve)) },
                        label = { Text(curve.name.replace("_", " ")) }
                    )
                }
            }

            if (profile.animationCurve == AnimationCurve.CUSTOM) {
                HorizontalDivider()
                Text("Custom Bezier Curve", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
                ) {
                    OutlinedTextField(
                        value = profile.customBezier.x1.toString(),
                        onValueChange = {
                            onProfileChange(
                                profile.copy(
                                    customBezier = profile.customBezier.copy(x1 = it.toFloatOrNull() ?: 0.25f)
                                )
                            )
                        },
                        label = { Text("X1") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = profile.customBezier.y1.toString(),
                        onValueChange = {
                            onProfileChange(
                                profile.copy(
                                    customBezier = profile.customBezier.copy(y1 = it.toFloatOrNull() ?: 0.1f)
                                )
                            )
                        },
                        label = { Text("Y1") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = profile.customBezier.x2.toString(),
                        onValueChange = {
                            onProfileChange(
                                profile.copy(
                                    customBezier = profile.customBezier.copy(x2 = it.toFloatOrNull() ?: 0.25f)
                                )
                            )
                        },
                        label = { Text("X2") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = profile.customBezier.y2.toString(),
                        onValueChange = {
                            onProfileChange(
                                profile.copy(
                                    customBezier = profile.customBezier.copy(y2 = it.toFloatOrNull() ?: 1f)
                                )
                            )
                        },
                        label = { Text("Y2") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun StaggerPatternSection(
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
                text = "Stagger Pattern",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            Text(
                text = "Controls how animations cascade across multiple elements",
                style = MaterialTheme.typography.bodySmall
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                items(StaggerPattern.entries) { pattern ->
                    FilterChip(
                        selected = profile.staggerPattern == pattern,
                        onClick = { onProfileChange(profile.copy(staggerPattern = pattern)) },
                        label = { Text(pattern.name.replace("_", " ")) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityPresetSection(
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
                text = "Quality Preset",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.yellow
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                items(QualityPreset.entries) { preset ->
                    QualityPresetCard(
                        preset = preset,
                        isSelected = profile.qualityPreset == preset,
                        onClick = { onProfileChange(profile.copy(qualityPreset = preset)) }
                    )
                }
            }

            // Frame Rate Cap
            HorizontalDivider(modifier = Modifier.padding(vertical = SugarDimens.Spacing.md))
            Text("Frame Rate Cap", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                FrameRateCap.entries.forEach { cap ->
                    FilterChip(
                        selected = profile.frameRateCap == cap,
                        onClick = { onProfileChange(profile.copy(frameRateCap = cap)) },
                        label = {
                            Text(
                                when (cap) {
                                    FrameRateCap.FPS_30 -> "30 FPS"
                                    FrameRateCap.FPS_60 -> "60 FPS"
                                    FrameRateCap.FPS_90 -> "90 FPS"
                                    FrameRateCap.FPS_120 -> "120 FPS"
                                    FrameRateCap.UNLIMITED -> "Unlimited"
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun QualityPresetCard(
    preset: QualityPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val description = when (preset) {
        QualityPreset.ULTRA -> "All effects, max particles, 60fps"
        QualityPreset.HIGH -> "Most effects, high particles"
        QualityPreset.MEDIUM -> "Balanced performance"
        QualityPreset.LOW -> "Reduced effects"
        QualityPreset.POWER_SAVER -> "Minimal animations"
        QualityPreset.ACCESSIBILITY -> "Essential only"
    }

    Card(
        modifier = Modifier
            .aspectRatio(1.2f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(SugarDimens.Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                SugarDimens.Brand.hotPink.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(SugarDimens.Spacing.md),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = preset.name.replace("_", " "),
                style = MaterialTheme.typography.titleSmall,
                color = if (isSelected) SugarDimens.Brand.hotPink else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            if (isSelected) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = SugarDimens.Brand.hotPink
                )
            }
        }
    }
}

// Helper components
@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
