package com.sugarmunch.app.theme.granular

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.theme.model.*

/**
 * Granular Theme Controls - 26 independent sliders for hyper-customization
 *
 * Categories:
 * - Colors (8 sliders)
 * - Gradient (6 sliders)
 * - Particles (4 sliders)
 * - Animations (4 sliders)
 * - Advanced (4 sliders)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GranularControlsPanel(
    config: GranularThemeConfig,
    onConfigChange: (GranularThemeConfig) -> Unit,
    onNavigateBack: () -> Unit,
    onApplyPreset: (GranularThemeConfig) -> Unit
) {
    var selectedCategory by remember { mutableStateOf<GranularCategory?>(null) }
    var showPresetsDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    val filteredSliders = remember(selectedCategory) {
        if (selectedCategory == null) {
            GranularSliderConfigs.ALL_SLIDERS
        } else {
            GranularSliderConfigs.getSlidersByCategory(selectedCategory)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Granular Controls")
                        Text(
                            text = "26 independent adjustments",
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
                    IconButton(onClick = { showPresetsDialog = true }) {
                        Icon(Icons.Outlined.Bookmark, contentDescription = "Presets")
                    }
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Reset")
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
            // Category Navigation
            CategoryNavigation(
                selectedCategory = selectedCategory,
                onCategorySelected = { selectedCategory = it },
                modifier = Modifier.weight(1f)
            )

            // Sliders Panel
            GranularSlidersPanel(
                sliders = filteredSliders,
                config = config,
                onConfigChange = onConfigChange,
                modifier = Modifier.weight(2f)
            )

            // Live Preview Panel
            LivePreviewPanel(
                config = config,
                modifier = Modifier.weight(1.5f)
            )
        }
    }

    // Presets Dialog
    if (showPresetsDialog) {
        GranularPresetsDialog(
            onPresetSelected = { preset ->
                onApplyPreset(preset)
                showPresetsDialog = false
            },
            onDismiss = { showPresetsDialog = false }
        )
    }

    // Reset Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Granular Controls") },
            text = { Text("This will reset all sliders to their default values. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onApplyPreset(GranularThemeConfig.DEFAULT)
                        showResetDialog = false
                    }
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ═════════════════════════════════════════════════════════════
// CATEGORY NAVIGATION
// ═════════════════════════════════════════════════════════════

@Composable
private fun CategoryNavigation(
    selectedCategory: GranularCategory?,
    onCategorySelected: (GranularCategory?) -> Unit,
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
        Text(
            text = "CATEGORIES",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp)
        )

        // All Button
        FilterChip(
            selected = selectedCategory == null,
            onClick = { onCategorySelected(null) },
            label = { Text("All") },
            leadingIcon = if (selectedCategory == null) {
                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
            } else null,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Buttons
        GranularCategory.entries.forEach { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { Text(category.displayName) },
                leadingIcon = { Text(category.icon) },
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Quick Stats
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "QUICK STATS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                StatRow("Active Sliders", "26")
                StatRow("Modified", countModifiedSliders())
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// GRANULAR SLIDERS PANEL
// ═════════════════════════════════════════════════════════════

@Composable
private fun GranularSlidersPanel(
    sliders: List<GranularSliderConfig>,
    config: GranularThemeConfig,
    onConfigChange: (GranularThemeConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${sliders.size} CONTROLS",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            AssistChip(
                onClick = { },
                label = { Text("Reset Category") },
                leadingIcon = {
                    Icon(Icons.Outlined.Refresh, contentDescription = null, Modifier.size(16.dp))
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sliders List
        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sliders, key = { it.key }) { sliderConfig ->
                GranularSliderCard(
                    sliderConfig = sliderConfig,
                    currentValue = getSliderValue(config, sliderConfig.key),
                    onValueChange = { newValue ->
                        onConfigChange(setSliderValue(config, sliderConfig.key, newValue))
                    }
                )
            }
        }
    }
}

@Composable
private fun GranularSliderCard(
    sliderConfig: GranularSliderConfig,
    currentValue: Float,
    onValueChange: (Float) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = sliderConfig.icon,
                        fontSize = 24.sp
                    )
                    Column {
                        Text(
                            text = sliderConfig.label,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = sliderConfig.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Value Display
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatValue(currentValue, sliderConfig.unit, sliderConfig.step),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            // Slider
            Slider(
                value = currentValue,
                onValueChange = onValueChange,
                valueRange = sliderConfig.min..sliderConfig.max,
                steps = ((sliderConfig.max - sliderConfig.min) / sliderConfig.step).toInt() - 1
            )

            // Min/Max Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${sliderConfig.min}${sliderConfig.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${sliderConfig.max}${sliderConfig.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reset Button
            if (isDefaultValue(currentValue, sliderConfig.defaultValue, sliderConfig.step)) {
                AssistChip(
                    onClick = { onValueChange(sliderConfig.defaultValue) },
                    label = { Text("Reset to Default") },
                    leadingIcon = {
                        Icon(Icons.Outlined.Refresh, contentDescription = null, Modifier.size(16.dp))
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// LIVE PREVIEW PANEL
// ═════════════════════════════════════════════════════════════

@Composable
private fun LivePreviewPanel(
    config: GranularThemeConfig,
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
            .padding(16.dp)
    ) {
        Text(
            text = "LIVE PREVIEW",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Color Preview
        ColorPreviewSection(config)

        Spacer(modifier = Modifier.height(16.dp))

        // Gradient Preview
        GradientPreviewSection(config)

        Spacer(modifier = Modifier.height(16.dp))

        // Stats
        GranularStats(config)
    }
}

@Composable
private fun ColorPreviewSection(config: GranularThemeConfig) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Color Adjustments",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Preview Colors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ColorSwatch(
                    color = Color(0xFFFF69B4).adjustColor(config),
                    label = "Primary",
                    modifier = Modifier.weight(1f)
                )
                ColorSwatch(
                    color = Color(0xFF9370DB).adjustColor(config),
                    label = "Secondary",
                    modifier = Modifier.weight(1f)
                )
                ColorSwatch(
                    color = Color(0xFF00BFFF).adjustColor(config),
                    label = "Accent",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun GradientPreviewSection(config: GranularThemeConfig) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Gradient Settings",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Gradient Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF69B4).adjustColor(config),
                                Color(0xFF9370DB).adjustColor(config)
                            ),
                            start = androidx.compose.ui.geometry.Offset(
                                config.gradientOffsetX * 100,
                                config.gradientOffsetY * 100
                            ),
                            end = androidx.compose.ui.geometry.Offset(
                                100 + config.gradientOffsetX * 100,
                                100 + config.gradientOffsetY * 100
                            )
                        )
                    )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatRow("Angle", "${config.gradientAngle.toInt()}°")
                StatRow("Spread", "${(config.gradientSpread * 100).toInt()}%")
                StatRow("Smoothness", "${(config.gradientSmoothness * 100).toInt()}%")
            }
        }
    }
}

@Composable
private fun GranularStats(config: GranularThemeConfig) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Configuration Stats",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )

            StatRow("Particle Density", "${(config.particleDensity * 100).toInt()}%")
            StatRow("Animation Speed", "${config.animationSpeed}x")
            StatRow("Bloom Intensity", "${(config.bloomIntensity * 100).toInt()}%")
            StatRow("Color Vibrance", "${(config.colorVibrance * 100).toInt()}%")
        }
    }
}

// ═════════════════════════════════════════════════════════════
// PRESETS DIALOG
// ═════════════════════════════════════════════════════════════

@Composable
private fun GranularPresetsDialog(
    onPresetSelected: (GranularThemeConfig) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Preset") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PresetCard(
                        name = "Minimal",
                        description = "Subtle, clean appearance",
                        icon = "🎨",
                        onClick = { onPresetSelected(GranularThemeConfig.MINIMAL) }
                    )
                }
                item {
                    PresetCard(
                        name = "Balanced",
                        description = "Default balanced settings",
                        icon = "⚖️",
                        onClick = { onPresetSelected(GranularThemeConfig.BALANCED) }
                    )
                }
                item {
                    PresetCard(
                        name = "Intense",
                        description = "Vibrant and energetic",
                        icon = "🔥",
                        onClick = { onPresetSelected(GranularThemeConfig.INTENSE) }
                    )
                }
                item {
                    PresetCard(
                        name = "Maximum",
                        description = "All settings at max",
                        icon = "💥",
                        onClick = { onPresetSelected(GranularThemeConfig.MAXIMUM) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PresetCard(
    name: String,
    description: String,
    icon: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = icon,
                fontSize = 32.sp
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "Select",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═════════════════════════════════════════════════════════════

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun getSliderValue(config: GranularThemeConfig, key: String): Float {
    return config.toMap()[key] ?: 1f
}

private fun setSliderValue(config: GranularThemeConfig, key: String, value: Float): GranularThemeConfig {
    val map = config.toMap().toMutableMap()
    map[key] = value
    return GranularThemeConfig.fromMap(map)
}

private fun formatValue(value: Float, unit: String, step: Float): String {
    val formatted = if (step >= 1f) value.toInt().toString() else String.format("%.1f", value)
    return "$formatted$unit"
}

private fun isDefaultValue(value: Float, defaultValue: Float, step: Float): Boolean {
    return kotlin.math.abs(value - defaultValue) < step
}

private fun countModifiedSliders(): String {
    return "0" // Would count actual modified sliders
}

private fun Color.adjustColor(config: GranularThemeConfig): Color {
    val hsl = this.toHsl()
    var h = hsl[0]
    var s = hsl[1]
    var l = hsl[2]

    // Apply adjustments based on config
    s = (s * config.colorVibrance).coerceIn(0f, 1f)
    l = (l * config.highlightIntensity).coerceIn(0f, 1f)

    return Color.hsl(h, s, l, this.alpha)
}
