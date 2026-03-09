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
 * EXTREME Grid & Spacing Editor
 * Comprehensive layout and spacing controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridSpacingEditor(
    onNavigateBack: () -> Unit,
    layoutConfig: LayoutConfig,
    onLayoutConfigChange: (LayoutConfig) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Grid & Spacing") },
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
            // Grid Configuration
            item {
                GridConfigurationSection(
                    layoutConfig = layoutConfig,
                    onLayoutConfigChange = onLayoutConfigChange
                )
            }

            // Spacing Controls
            item {
                SpacingControlsSection(
                    layoutConfig = layoutConfig,
                    onLayoutConfigChange = onLayoutConfigChange
                )
            }

            // Alignment Options
            item {
                AlignmentOptionsSection(
                    layoutConfig = layoutConfig,
                    onLayoutConfigChange = onLayoutConfigChange
                )
            }

            // Content Padding
            item {
                ContentPaddingSection(
                    layoutConfig = layoutConfig,
                    onLayoutConfigChange = onLayoutConfigChange
                )
            }
        }
    }
}

@Composable
private fun GridConfigurationSection(
    layoutConfig: LayoutConfig,
    onLayoutConfigChange: (LayoutConfig) -> Unit
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
                text = "Grid Configuration",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )

            // Columns
            SliderWithLabel(
                label = "Columns (${layoutConfig.columns})",
                value = layoutConfig.columns.toFloat(),
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(columns = it.toInt()))
                },
                valueRange = 2f..12f,
                steps = 9
            )

            // Rows
            SliderWithLabel(
                label = "Rows (${layoutConfig.rows})",
                value = layoutConfig.rows.toFloat(),
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(rows = it.toInt()))
                },
                valueRange = 3f..20f,
                steps = 16
            )

            // Grid Type
            Text("Grid Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                GridType.entries.forEach { type ->
                    FilterChip(
                        selected = layoutConfig.gridType == type,
                        onClick = {
                            onLayoutConfigChange(layoutConfig.copy(gridType = type))
                        },
                        label = { Text(type.name) }
                    )
                }
            }

            // Item Aspect Ratio
            Text("Item Aspect Ratio", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                AspectRatio.entries.forEach { ratio ->
                    FilterChip(
                        selected = layoutConfig.itemAspectRatio == ratio,
                        onClick = {
                            onLayoutConfigChange(layoutConfig.copy(itemAspectRatio = ratio))
                        },
                        label = {
                            Text(
                                when (ratio) {
                                    AspectRatio.RATIO_1_1 -> "1:1"
                                    AspectRatio.RATIO_4_3 -> "4:3"
                                    AspectRatio.RATIO_16_9 -> "16:9"
                                    AspectRatio.CUSTOM -> "Custom"
                                }
                            )
                        }
                    )
                }
            }

            // Grid Gaps
            SliderWithLabel(
                label = "Grid Gaps (${layoutConfig.gridGaps.value.toInt()}dp)",
                value = layoutConfig.gridGaps.value,
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(gridGaps = it.dp))
                },
                valueRange = 0f..32f,
                steps = 31
            )

            // Section Margins
            SliderWithLabel(
                label = "Section Margins (${layoutConfig.sectionMargins.value.toInt()}dp)",
                value = layoutConfig.sectionMargins.value,
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(sectionMargins = it.dp))
                },
                valueRange = 0f..48f,
                steps = 47
            )
        }
    }
}

@Composable
private fun SpacingControlsSection(
    layoutConfig: LayoutConfig,
    onLayoutConfigChange: (LayoutConfig) -> Unit
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
                text = "Spacing Controls",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            // Inter-Item Spacing Horizontal
            SliderWithLabel(
                label = "Inter-Item Horizontal (${layoutConfig.interItemSpacingHorizontal.value.toInt()}dp)",
                value = layoutConfig.interItemSpacingHorizontal.value,
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(interItemSpacingHorizontal = it.dp))
                },
                valueRange = 0f..32f,
                steps = 31
            )

            // Inter-Item Spacing Vertical
            SliderWithLabel(
                label = "Inter-Item Vertical (${layoutConfig.interItemSpacingVertical.value.toInt()}dp)",
                value = layoutConfig.interItemSpacingVertical.value,
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(interItemSpacingVertical = it.dp))
                },
                valueRange = 0f..32f,
                steps = 31
            )

            // Section Spacing
            SliderWithLabel(
                label = "Section Spacing (${layoutConfig.sectionSpacing.value.toInt()}dp)",
                value = layoutConfig.sectionSpacing.value,
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(sectionSpacing = it.dp))
                },
                valueRange = 0f..48f,
                steps = 47
            )
        }
    }
}

@Composable
private fun AlignmentOptionsSection(
    layoutConfig: LayoutConfig,
    onLayoutConfigChange: (LayoutConfig) -> Unit
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
                text = "Alignment Options",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            // Horizontal Alignment
            Text("Horizontal Alignment", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                HorizontalAlignment.entries.forEach { alignment ->
                    FilterChip(
                        selected = layoutConfig.horizontalAlignment == alignment,
                        onClick = {
                            onLayoutConfigChange(layoutConfig.copy(horizontalAlignment = alignment))
                        },
                        label = { Text(alignment.name) }
                    )
                }
            }

            // Vertical Alignment
            Text("Vertical Alignment", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                VerticalAlignment.entries.forEach { alignment ->
                    FilterChip(
                        selected = layoutConfig.verticalAlignment == alignment,
                        onClick = {
                            onLayoutConfigChange(layoutConfig.copy(verticalAlignment = alignment))
                        },
                        label = { Text(alignment.name) }
                    )
                }
            }

            // Content Distribution
            Text("Content Distribution", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ContentDistribution.entries.forEach { distribution ->
                    FilterChip(
                        selected = layoutConfig.contentDistribution == distribution,
                        onClick = {
                            onLayoutConfigChange(layoutConfig.copy(contentDistribution = distribution))
                        },
                        label = { Text(distribution.name.replace("_", " ")) }
                    )
                }
            }

            // Item Ordering
            Text("Item Ordering", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ItemOrdering.entries.forEach { ordering ->
                    FilterChip(
                        selected = layoutConfig.itemOrdering == ordering,
                        onClick = {
                            onLayoutConfigChange(layoutConfig.copy(itemOrdering = ordering))
                        },
                        label = { Text(ordering.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentPaddingSection(
    layoutConfig: LayoutConfig,
    onLayoutConfigChange: (LayoutConfig) -> Unit
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
                text = "Content Padding",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            // Left Padding
            SliderWithLabel(
                label = "Left Padding (${layoutConfig.contentPaddingLeft.value.toInt()}dp)",
                value = layoutConfig.contentPaddingLeft.value,
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(contentPaddingLeft = it.dp))
                },
                valueRange = 0f..48f,
                steps = 47
            )

            // Right Padding
            SliderWithLabel(
                label = "Right Padding (${layoutConfig.contentPaddingRight.value.toInt()}dp)",
                value = layoutConfig.contentPaddingRight.value,
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(contentPaddingRight = it.dp))
                },
                valueRange = 0f..48f,
                steps = 47
            )

            // Top Padding
            SliderWithLabel(
                label = "Top Padding (${layoutConfig.contentPaddingTop.value.toInt()}dp)",
                value = layoutConfig.contentPaddingTop.value,
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(contentPaddingTop = it.dp))
                },
                valueRange = 0f..48f,
                steps = 47
            )

            // Bottom Padding
            SliderWithLabel(
                label = "Bottom Padding (${layoutConfig.contentPaddingBottom.value.toInt()}dp)",
                value = layoutConfig.contentPaddingBottom.value,
                onValueChange = {
                    onLayoutConfigChange(layoutConfig.copy(contentPaddingBottom = it.dp))
                },
                valueRange = 0f..48f,
                steps = 47
            )
        }
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
