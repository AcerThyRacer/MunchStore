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
 * EXTREME Experimental Features Lab Screen
 * Beta features and debug tools
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentalLabScreen(
    onNavigateBack: () -> Unit,
    experimentalFlags: ExperimentalFlags,
    onFlagsChange: (ExperimentalFlags) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Experimental Lab") },
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
            // Warning Banner
            item {
                WarningBanner()
            }

            // Beta Features
            item {
                BetaFeaturesSection(
                    experimentalFlags = experimentalFlags,
                    onFlagsChange = onFlagsChange
                )
            }

            // Debug Tools
            item {
                DebugToolsSection(
                    experimentalFlags = experimentalFlags,
                    onFlagsChange = onFlagsChange
                )
            }

            // Performance Tuning
            item {
                PerformanceTuningSection()
            }
        }
    }
}

@Composable
private fun WarningBanner() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.lg),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Filled.Warning,
                contentDescription = "Warning",
                tint = MaterialTheme.colorScheme.onErrorContainer
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Experimental Features",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "These features are in beta and may be unstable. Use at your own risk.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun BetaFeaturesSection(
    experimentalFlags: ExperimentalFlags,
    onFlagsChange: (ExperimentalFlags) -> Unit
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
                text = "Beta Features",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )

            Text(
                text = "Cutting-edge features under active development",
                style = MaterialTheme.typography.bodySmall
            )

            // Next-gen Particle Engine
            ExperimentalFeatureRow(
                icon = "✨",
                title = "Next-gen Particle Engine",
                description = "Advanced particle rendering with GPU acceleration",
                isEnabled = experimentalFlags.nextGenParticleEngine,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(nextGenParticleEngine = it))
                }
            )

            // AI-powered Theme Generator
            ExperimentalFeatureRow(
                icon = "🤖",
                title = "AI Theme Generator",
                description = "Generate themes using machine learning",
                isEnabled = experimentalFlags.aiPoweredThemeGenerator,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(aiPoweredThemeGenerator = it))
                }
            )

            // Voice-controlled Customization
            ExperimentalFeatureRow(
                icon = "🎤",
                title = "Voice Control",
                description = "Control customization with voice commands",
                isEnabled = experimentalFlags.voiceControlledCustomization,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(voiceControlledCustomization = it))
                }
            )

            // AR Preview Mode
            ExperimentalFeatureRow(
                icon = "🥽",
                title = "AR Preview",
                description = "Preview themes in augmented reality",
                isEnabled = experimentalFlags.arPreviewMode,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(arPreviewMode = it))
                }
            )

            // Gesture Learning
            ExperimentalFeatureRow(
                icon = "👆",
                title = "Gesture Learning",
                description = "AI learns your gesture preferences",
                isEnabled = experimentalFlags.gestureLearning,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(gestureLearning = it))
                }
            )

            // Predictive Animations
            ExperimentalFeatureRow(
                icon = "🔮",
                title = "Predictive Animations",
                description = "Pre-render animations based on prediction",
                isEnabled = experimentalFlags.predictiveAnimations,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(predictiveAnimations = it))
                }
            )

            // Advanced Haptics
            ExperimentalFeatureRow(
                icon = "📳",
                title = "Advanced Haptics",
                description = "Enhanced haptic feedback patterns",
                isEnabled = experimentalFlags.advancedHaptics,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(advancedHaptics = it))
                }
            )

            // Neural Theme Adaptation
            ExperimentalFeatureRow(
                icon = "🧠",
                title = "Neural Adaptation",
                description = "Themes adapt based on usage patterns",
                isEnabled = experimentalFlags.neuralThemeAdaptation,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(neuralThemeAdaptation = it))
                }
            )
        }
    }
}

@Composable
private fun ExperimentalFeatureRow(
    icon: String,
    title: String,
    description: String,
    isEnabled: Boolean,
    onToggleChange: (Boolean) -> Unit
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
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggleChange
            )
        }
    }
}

@Composable
private fun DebugToolsSection(
    experimentalFlags: ExperimentalFlags,
    onFlagsChange: (ExperimentalFlags) -> Unit
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
                text = "Debug Tools",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            Text(
                text = "Development and debugging overlays",
                style = MaterialTheme.typography.bodySmall
            )

            // FPS Counter Overlay
            ExperimentalFeatureRow(
                icon = "📊",
                title = "FPS Counter",
                description = "Show frames per second overlay",
                isEnabled = experimentalFlags.fpsCounterOverlay,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(fpsCounterOverlay = it))
                }
            )

            // GPU Usage Overlay
            ExperimentalFeatureRow(
                icon = "🎮",
                title = "GPU Usage",
                description = "Show GPU utilization overlay",
                isEnabled = experimentalFlags.gpuUsageOverlay,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(gpuUsageOverlay = it))
                }
            )

            // Memory Usage Overlay
            ExperimentalFeatureRow(
                icon = "💾",
                title = "Memory Usage",
                description = "Show memory consumption overlay",
                isEnabled = experimentalFlags.memoryUsageOverlay,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(memoryUsageOverlay = it))
                }
            )

            // Animation Timeline Viewer
            ExperimentalFeatureRow(
                icon = "📈",
                title = "Animation Timeline",
                description = "Visualize animation timing",
                isEnabled = experimentalFlags.animationTimelineViewer,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(animationTimelineViewer = it))
                }
            )

            // Touch Event Visualizer
            ExperimentalFeatureRow(
                icon = "👆",
                title = "Touch Visualizer",
                description = "Show touch events on screen",
                isEnabled = experimentalFlags.touchEventVisualizer,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(touchEventVisualizer = it))
                }
            )

            // Network Activity Monitor
            ExperimentalFeatureRow(
                icon = "🌐",
                title = "Network Monitor",
                description = "Show network activity overlay",
                isEnabled = experimentalFlags.networkActivityMonitor,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(networkActivityMonitor = it))
                }
            )

            // Battery Drain Analyzer
            ExperimentalFeatureRow(
                icon = "🔋",
                title = "Battery Analyzer",
                description = "Analyze battery consumption",
                isEnabled = experimentalFlags.batteryDrainAnalyzer,
                onToggleChange = {
                    onFlagsChange(experimentalFlags.copy(batteryDrainAnalyzer = it))
                }
            )
        }
    }
}

@Composable
private fun PerformanceTuningSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Performance Tuning",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            Text(
                text = "Advanced performance optimization settings",
                style = MaterialTheme.typography.bodySmall
            )

            // Thread Priority Adjustment
            Text("Thread Priority", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                FilterChip(
                    selected = true,
                    onClick = { },
                    label = { Text("Normal") }
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("High") }
                )
                FilterChip(
                    selected = false,
                    onClick = { },
                    label = { Text("Max") }
                )
            }

            // Render Thread Isolation
            SwitchWithLabel(
                label = "Render Thread Isolation",
                checked = false,
                onCheckedChange = { }
            )

            // Animation Frame Budget
            SliderWithLabel(
                label = "Animation Frame Budget (16ms)",
                value = 16f,
                onValueChange = { },
                valueRange = 8f..33f,
                steps = 24
            )

            // Memory Cache Sizes
            SliderWithLabel(
                label = "Memory Cache Size (128MB)",
                value = 128f,
                onValueChange = { },
                valueRange = 64f..512f,
                steps = 7
            )

            // Image Cache Limits
            SliderWithLabel(
                label = "Image Cache Limit (256MB)",
                value = 256f,
                onValueChange = { },
                valueRange = 128f..1024f,
                steps = 7
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
