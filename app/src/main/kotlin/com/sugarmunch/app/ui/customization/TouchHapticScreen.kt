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
 * EXTREME Touch & Haptic Customization Screen
 * Touch feedback and haptic pattern controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TouchHapticScreen(
    onNavigateBack: () -> Unit,
    hapticPattern: HapticPattern,
    onHapticPatternChange: (HapticPattern) -> Unit,
    touchFeedbackConfig: TouchFeedbackConfig = TouchFeedbackConfig(),
    onTouchFeedbackConfigChange: (TouchFeedbackConfig) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Touch & Haptic") },
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
            // Touch Feedback
            item {
                TouchFeedbackSection(
                    config = touchFeedbackConfig,
                    onConfigChange = onTouchFeedbackConfigChange
                )
            }

            // Haptic Patterns
            item {
                HapticPatternsSection(
                    hapticPattern = hapticPattern,
                    onHapticPatternChange = onHapticPatternChange
                )
            }

            // Haptic Intensity by Context
            item {
                HapticIntensitySection(
                    hapticPattern = hapticPattern,
                    onHapticPatternChange = onHapticPatternChange
                )
            }

            // Haptic Scheduling
            item {
                HapticSchedulingSection(
                    hapticPattern = hapticPattern,
                    onHapticPatternChange = onHapticPatternChange
                )
            }

            // Custom Pattern Creator
            item {
                CustomPatternCreatorSection(
                    hapticPattern = hapticPattern,
                    onHapticPatternChange = onHapticPatternChange
                )
            }
        }
    }
}

@Composable
private fun TouchFeedbackSection(
    config: TouchFeedbackConfig,
    onConfigChange: (TouchFeedbackConfig) -> Unit
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
                text = "Touch Feedback",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )

            // Visual Touch Indicator Type
            Text("Visual Indicator Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                TouchIndicatorType.entries.forEach { type ->
                    FilterChip(
                        selected = config.visualIndicator == type,
                        onClick = { onConfigChange(config.copy(visuallIndicator = type)) },
                        label = { Text(type.name) }
                    )
                }
            }

            // Indicator Size
            SliderWithLabel(
                label = "Indicator Size (${config.indicatorSize.toInt()}dp)",
                value = config.indicatorSize,
                onValueChange = { onConfigChange(config.copy(indicatorSize = it)) },
                valueRange = 10f..100f,
                steps = 17
            )

            // Indicator Duration
            SliderWithLabel(
                label = "Indicator Duration (${config.indicatorDuration}ms)",
                value = config.indicatorDuration.toFloat(),
                onValueChange = { onConfigChange(config.copy(indicatorDuration = it.toInt())) },
                valueRange = 100f..1000f,
                steps = 17
            )

            // Show for All Touches
            SwitchWithLabel(
                label = "Show for all touches (vs interactive only)",
                checked = config.showForAllTouches,
                onCheckedChange = { onConfigChange(config.copy(showForAllTouches = it)) }
            )
        }
    }
}

@Composable
private fun HapticPatternsSection(
    hapticPattern: HapticPattern,
    onHapticPatternChange: (HapticPattern) -> Unit
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
                text = "Haptic Patterns",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            Text(
                text = "20+ preset vibration patterns",
                style = MaterialTheme.typography.bodySmall
            )

            // Click Pattern
            Text("Click Pattern", style = MaterialTheme.typography.labelLarge)
            HapticPresetSelector(
                selected = hapticPattern.clickPattern,
                onSelected = { onHapticPatternChange(hapticPattern.copy(clickPattern = it)) }
            )

            // Success Pattern
            Text("Success Pattern", style = MaterialTheme.typography.labelLarge)
            HapticPresetSelector(
                selected = hapticPattern.successPattern,
                onSelected = { onHapticPatternChange(hapticPattern.copy(successPattern = it)) }
            )

            // Error Pattern
            Text("Error Pattern", style = MaterialTheme.typography.labelLarge)
            HapticPresetSelector(
                selected = hapticPattern.errorPattern,
                onSelected = { onHapticPatternChange(hapticPattern.copy(errorPattern = it)) }
            )

            // Warning Pattern
            Text("Warning Pattern", style = MaterialTheme.typography.labelLarge)
            HapticPresetSelector(
                selected = hapticPattern.warningPattern,
                onSelected = { onHapticPatternChange(hapticPattern.copy(warningPattern = it)) }
            )
        }
    }
}

@Composable
private fun HapticPresetSelector(
    selected: HapticPreset,
    onSelected: (HapticPreset) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
    ) {
        HapticPreset.entries.forEach { preset ->
            FilterChip(
                selected = selected == preset,
                onClick = { onSelected(preset) },
                label = { Text(preset.name.replace("_", " ")) }
            )
        }
    }
}

@Composable
private fun HapticIntensitySection(
    hapticPattern: HapticPattern,
    onHapticPatternChange: (HapticPattern) -> Unit
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
                text = "Haptic Intensity by Context",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            // Buttons Intensity
            SliderWithLabel(
                label = "Buttons (${(hapticPattern.buttonIntensity * 100).toInt()}%)",
                value = hapticPattern.buttonIntensity,
                onValueChange = { onHapticPatternChange(hapticPattern.copy(buttonIntensity = it)) },
                valueRange = 0f..1f,
                steps = 19
            )

            // Sliders Intensity
            SliderWithLabel(
                label = "Sliders (${(hapticPattern.sliderIntensity * 100).toInt()}%)",
                value = hapticPattern.sliderIntensity,
                onValueChange = { onHapticPatternChange(hapticPattern.copy(sliderIntensity = it)) },
                valueRange = 0f..1f,
                steps = 19
            )

            // Scrolling Intensity
            SliderWithLabel(
                label = "Scrolling (${(hapticPattern.scrollingIntensity * 100).toInt()}%)",
                value = hapticPattern.scrollingIntensity,
                onValueChange = { onHapticPatternChange(hapticPattern.copy(scrollingIntensity = it)) },
                valueRange = 0f..1f,
                steps = 19
            )

            // Gestures Intensity
            SliderWithLabel(
                label = "Gestures (${(hapticPattern.gestureIntensity * 100).toInt()}%)",
                value = hapticPattern.gestureIntensity,
                onValueChange = { onHapticPatternChange(hapticPattern.copy(gestureIntensity = it)) },
                valueRange = 0f..1f,
                steps = 19
            )

            // Notifications Intensity
            SliderWithLabel(
                label = "Notifications (${(hapticPattern.notificationIntensity * 100).toInt()}%)",
                value = hapticPattern.notificationIntensity,
                onValueChange = { onHapticPatternChange(hapticPattern.copy(notificationIntensity = it)) },
                valueRange = 0f..1f,
                steps = 19
            )

            // System Events Intensity
            SliderWithLabel(
                label = "System Events (${(hapticPattern.systemEventIntensity * 100).toInt()}%)",
                value = hapticPattern.systemEventIntensity,
                onValueChange = { onHapticPatternChange(hapticPattern.copy(systemEventIntensity = it)) },
                valueRange = 0f..1f,
                steps = 19
            )
        }
    }
}

@Composable
private fun HapticSchedulingSection(
    hapticPattern: HapticPattern,
    onHapticPatternChange: (HapticPattern) -> Unit
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
                text = "Haptic Scheduling",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            // Quiet Hours
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Quiet Hours",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(
                        text = "${hapticPattern.quietHoursStart} - ${hapticPattern.quietHoursEnd}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = hapticPattern.quietHoursEnabled,
                    onCheckedChange = {
                        onHapticPatternChange(hapticPattern.copy(quietHoursEnabled = it))
                    }
                )
            }

            // Meeting Mode
            SwitchWithLabel(
                label = "Meeting Mode (reduced haptics)",
                checked = hapticPattern.meetingMode,
                onCheckedChange = {
                    onHapticPatternChange(hapticPattern.copy(meetingMode = it))
                }
            )

            // Night Mode
            SwitchWithLabel(
                label = "Night Mode (gentle haptics only)",
                checked = hapticPattern.nightMode,
                onCheckedChange = {
                    onHapticPatternChange(hapticPattern.copy(nightMode = it))
                }
            )
        }
    }
}

@Composable
private fun CustomPatternCreatorSection(
    hapticPattern: HapticPattern,
    onHapticPatternChange: (HapticPattern) -> Unit
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
                text = "Custom Pattern Creator",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.yellow
            )

            Text(
                text = "Create tap-hold-tap sequences",
                style = MaterialTheme.typography.bodySmall
            )

            // Pattern segments display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                hapticPattern.customPattern.forEachIndexed { index, segment ->
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { },
                        shape = RoundedCornerShape(8.dp),
                        color = SugarDimens.Brand.hotPink.copy(alpha = 0.3f)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${segment.duration}",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            // Add segment button
            OutlinedButton(
                onClick = {
                    val newSegment = HapticSegment(duration = 50, amplitude = 128, delay = 0)
                    val newPattern = hapticPattern.customPattern + newSegment
                    onHapticPatternChange(hapticPattern.copy(customPattern = newPattern))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add Segment")
            }

            // Test pattern button
            Button(
                onClick = { /* Test haptic pattern */ },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Test Pattern")
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
