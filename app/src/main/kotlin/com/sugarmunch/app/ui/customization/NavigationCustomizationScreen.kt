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
 * EXTREME Navigation Customization Screen
 * Navigation style and behavior controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationCustomizationScreen(
    onNavigateBack: () -> Unit,
    navigationConfig: NavigationConfig,
    onNavigationConfigChange: (NavigationConfig) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Navigation") },
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
            // Navigation Style
            item {
                NavigationStyleSection(
                    navigationConfig = navigationConfig,
                    onNavigationConfigChange = onNavigationConfigChange
                )
            }

            // Navigation Appearance
            item {
                NavigationAppearanceSection(
                    navigationConfig = navigationConfig,
                    onNavigationConfigChange = onNavigationConfigChange
                )
            }

            // Navigation Behavior
            item {
                NavigationBehaviorSection(
                    navigationConfig = navigationConfig,
                    onNavigationConfigChange = onNavigationConfigChange
                )
            }
        }
    }
}

@Composable
private fun NavigationStyleSection(
    navigationConfig: NavigationConfig,
    onNavigationConfigChange: (NavigationConfig) -> Unit
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
                text = "Navigation Style",
                style = MaterialTheme.typography.titleLarge,
                color = SugarDimens.Brand.hotPink
            )

            // Style Selector
            NavigationStyle.entries.forEach { style ->
                NavigationStyleRow(
                    style = style,
                    isSelected = navigationConfig.style == style,
                    onClick = { onNavigationConfigChange(navigationConfig.copy(style = style)) }
                )
            }
        }
    }
}

@Composable
private fun NavigationStyleRow(
    style: NavigationStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (style) {
        NavigationStyle.BOTTOM_BAR -> "⬇️"
        NavigationStyle.RAIL_LEFT -> "⬅️"
        NavigationStyle.RAIL_RIGHT -> "➡️"
        NavigationStyle.DRAWER_LEFT -> "📂"
        NavigationStyle.DRAWER_RIGHT -> "📁"
        NavigationStyle.TOP_TABS -> "⬆️"
        NavigationStyle.GESTURE_ONLY -> "👆"
        NavigationStyle.FAB -> "🔘"
        NavigationStyle.PIE_MENU -> "🎯"
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
                Text(
                    text = style.name.replace("_", " "),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isSelected) SugarDimens.Brand.hotPink else MaterialTheme.colorScheme.onSurface
                )
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun NavigationAppearanceSection(
    navigationConfig: NavigationConfig,
    onNavigationConfigChange: (NavigationConfig) -> Unit
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
                text = "Navigation Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            // Height/Width
            SliderWithLabel(
                label = when (navigationConfig.style) {
                    NavigationStyle.BOTTOM_BAR, NavigationStyle.TOP_TABS -> "Height (${navigationConfig.height.value.toInt()}dp)"
                    NavigationStyle.RAIL_LEFT, NavigationStyle.RAIL_RIGHT, NavigationStyle.DRAWER_LEFT, NavigationStyle.DRAWER_RIGHT -> "Width (${navigationConfig.width.value.toInt()}dp)"
                    else -> "Size"
                },
                value = if (navigationConfig.style == NavigationStyle.BOTTOM_BAR || navigationConfig.style == NavigationStyle.TOP_TABS) {
                    navigationConfig.height.value
                } else {
                    navigationConfig.width.value
                },
                onValueChange = {
                    if (navigationConfig.style == NavigationStyle.BOTTOM_BAR || navigationConfig.style == NavigationStyle.TOP_TABS) {
                        onNavigationConfigChange(navigationConfig.copy(height = it.dp))
                    } else {
                        onNavigationConfigChange(navigationConfig.copy(width = it.dp))
                    }
                },
                valueRange = 40f..80f,
                steps = 39
            )

            // Background Type
            Text("Background Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                NavigationBackground.entries.forEach { bg ->
                    FilterChip(
                        selected = navigationConfig.backgroundType == bg,
                        onClick = {
                            onNavigationConfigChange(navigationConfig.copy(backgroundType = bg))
                        },
                        label = { Text(bg.name) }
                    )
                }
            }

            // Elevation
            SliderWithLabel(
                label = "Elevation (${navigationConfig.elevation.value.toInt()}dp)",
                value = navigationConfig.elevation.value,
                onValueChange = {
                    onNavigationConfigChange(navigationConfig.copy(elevation = it.dp))
                },
                valueRange = 0f..24f,
                steps = 23
            )

            // Corner Radius
            SliderWithLabel(
                label = "Corner Radius (${navigationConfig.cornerRadius.value.toInt()}dp)",
                value = navigationConfig.cornerRadius.value,
                onValueChange = {
                    onNavigationConfigChange(navigationConfig.copy(cornerRadius = it.dp))
                },
                valueRange = 0f..40f,
                steps = 39
            )

            // Icon Size
            SliderWithLabel(
                label = "Icon Size (${navigationConfig.iconSize.value.toInt()}dp)",
                value = navigationConfig.iconSize.value,
                onValueChange = {
                    onNavigationConfigChange(navigationConfig.copy(iconSize = it.dp))
                },
                valueRange = 16f..40f,
                steps = 23
            )

            // Label Position
            Text("Label Position", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                LabelPosition.entries.forEach { pos ->
                    FilterChip(
                        selected = navigationConfig.labelPosition == pos,
                        onClick = {
                            onNavigationConfigChange(navigationConfig.copy(labelPosition = pos))
                        },
                        label = { Text(pos.name) }
                    )
                }
            }

            // Label Size
            SliderWithLabel(
                label = "Label Size (${navigationConfig.labelSize.toInt()}sp)",
                value = navigationConfig.labelSize,
                onValueChange = {
                    onNavigationConfigChange(navigationConfig.copy(labelSize = it))
                },
                valueRange = 10f..18f,
                steps = 7
            )

            // Active Indicator
            Text("Active Indicator", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ActiveIndicatorType.entries.forEach { indicator ->
                    FilterChip(
                        selected = navigationConfig.activeIndicator == indicator,
                        onClick = {
                            onNavigationConfigChange(navigationConfig.copy(activeIndicator = indicator))
                        },
                        label = { Text(indicator.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NavigationBehaviorSection(
    navigationConfig: NavigationConfig,
    onNavigationConfigChange: (NavigationConfig) -> Unit
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
                text = "Navigation Behavior",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            // Auto-hide on scroll
            SwitchWithLabel(
                label = "Auto-hide on scroll",
                checked = navigationConfig.autoHideOnScroll,
                onCheckedChange = {
                    onNavigationConfigChange(navigationConfig.copy(autoHideOnScroll = it))
                }
            )

            // Shrink on scroll
            SwitchWithLabel(
                label = "Shrink on scroll",
                checked = navigationConfig.shrinkOnScroll,
                onCheckedChange = {
                    onNavigationConfigChange(navigationConfig.copy(shrinkOnScroll = it))
                }
            )

            // Transform to FAB
            SwitchWithLabel(
                label = "Transform to FAB when hidden",
                checked = navigationConfig.transformToFAB,
                onCheckedChange = {
                    onNavigationConfigChange(navigationConfig.copy(transformToFAB = it))
                }
            )

            // Badge Style
            Text("Badge Style", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                BadgeStyle.entries.forEach { style ->
                    FilterChip(
                        selected = navigationConfig.badgeStyle == style,
                        onClick = {
                            onNavigationConfigChange(navigationConfig.copy(badgeStyle = style))
                        },
                        label = { Text(style.name) }
                    )
                }
            }

            // Badge Position
            Text("Badge Position", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                BadgePosition.entries.forEach { pos ->
                    FilterChip(
                        selected = navigationConfig.badgePosition == pos,
                        onClick = {
                            onNavigationConfigChange(navigationConfig.copy(badgePosition = pos))
                        },
                        label = { Text(pos.name.replace("_", " ")) }
                    )
                }
            }

            // Transition Animation
            Text("Transition Animation", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                NavigationTransition.entries.forEach { transition ->
                    FilterChip(
                        selected = navigationConfig.transitionAnimation == transition,
                        onClick = {
                            onNavigationConfigChange(navigationConfig.copy(transitionAnimation = transition))
                        },
                        label = { Text(transition.name.replace("_", " ")) }
                    )
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
