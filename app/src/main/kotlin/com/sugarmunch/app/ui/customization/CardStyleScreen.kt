package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
 * EXTREME Card & Item Styles Screen
 * Comprehensive card architecture and styling controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardStyleScreen(
    onNavigateBack: () -> Unit,
    cardStyleConfig: CardStyleConfig,
    onCardStyleConfigChange: (CardStyleConfig) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Card Styles") },
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
            // Card Architecture
            item {
                CardArchitectureSection(
                    cardStyleConfig = cardStyleConfig,
                    onCardStyleConfigChange = onCardStyleConfigChange
                )
            }

            // Content Layout
            item {
                ContentLayoutSection(
                    cardStyleConfig = cardStyleConfig,
                    onCardStyleConfigChange = onCardStyleConfigChange
                )
            }

            // Interactive States
            item {
                InteractiveStatesSection(
                    cardStyleConfig = cardStyleConfig,
                    onCardStyleConfigChange = onCardStyleConfigChange
                )
            }

            // Preview
            item {
                CardPreviewSection(
                    cardStyleConfig = cardStyleConfig
                )
            }
        }
    }
}

@Composable
private fun CardArchitectureSection(
    cardStyleConfig: CardStyleConfig,
    onCardStyleConfigChange: (CardStyleConfig) -> Unit
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
                text = "Card Architecture",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )

            // Shape
            Text("Shape", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                CardShape.entries.forEach { shape ->
                    FilterChip(
                        selected = cardStyleConfig.shape == shape,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(shape = shape))
                        },
                        label = { Text(shape.name) }
                    )
                }
            }

            // Border Type
            Text("Border Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                BorderType.entries.forEach { type ->
                    FilterChip(
                        selected = cardStyleConfig.borderType == type,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(borderType = type))
                        },
                        label = { Text(type.name) }
                    )
                }
            }

            // Border Width
            if (cardStyleConfig.borderType != BorderType.NONE) {
                SliderWithLabel(
                    label = "Border Width (${cardStyleConfig.borderWidth.value.toInt()}dp)",
                    value = cardStyleConfig.borderWidth.value,
                    onValueChange = {
                        onCardStyleConfigChange(cardStyleConfig.copy(borderWidth = it.dp))
                    },
                    valueRange = 1f..10f,
                    steps = 8
                )
            }

            // Shadow Type
            Text("Shadow Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ShadowType.entries.forEach { type ->
                    FilterChip(
                        selected = cardStyleConfig.shadowType == type,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(shadowType = type))
                        },
                        label = { Text(type.name) }
                    )
                }
            }

            // Elevation
            SliderWithLabel(
                label = "Elevation (${cardStyleConfig.elevation.value.toInt()}dp)",
                value = cardStyleConfig.elevation.value,
                onValueChange = {
                    onCardStyleConfigChange(cardStyleConfig.copy(elevation = it.dp))
                },
                valueRange = 0f..24f,
                steps = 23
            )

            // Surface Type
            Text("Surface Type", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                CardSurface.entries.forEach { surface ->
                    FilterChip(
                        selected = cardStyleConfig.surfaceType == surface,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(surfaceType = surface))
                        },
                        label = { Text(surface.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentLayoutSection(
    cardStyleConfig: CardStyleConfig,
    onCardStyleConfigChange: (CardStyleConfig) -> Unit
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
                text = "Content Layout",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            // Image Position
            Text("Image Position", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ContentPosition.entries.forEach { pos ->
                    FilterChip(
                        selected = cardStyleConfig.imagePosition == pos,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(imagePosition = pos))
                        },
                        label = { Text(pos.name) }
                    )
                }
            }

            // Title Position
            Text("Title Position", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ContentPosition.entries.forEach { pos ->
                    FilterChip(
                        selected = cardStyleConfig.titlePosition == pos,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(titlePosition = pos))
                        },
                        label = { Text(pos.name) }
                    )
                }
            }

            // Subtitle Position
            Text("Subtitle Position", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ContentPosition.entries.forEach { pos ->
                    FilterChip(
                        selected = cardStyleConfig.subtitlePosition == pos,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(subtitlePosition = pos))
                        },
                        label = { Text(pos.name) }
                    )
                }
            }

            // Action Buttons Position
            Text("Action Buttons Position", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ActionPosition.entries.forEach { pos ->
                    FilterChip(
                        selected = cardStyleConfig.actionButtonsPosition == pos,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(actionButtonsPosition = pos))
                        },
                        label = { Text(pos.name) }
                    )
                }
            }

            // Metadata Display
            Text("Metadata Display", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                MetadataDisplay.entries.forEach { display ->
                    FilterChip(
                        selected = cardStyleConfig.metadataDisplay == display,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(metadataDisplay = display))
                        },
                        label = { Text(display.name.replace("_", " ")) }
                    )
                }
            }

            // Content Padding
            Text("Content Padding", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                ContentPadding.entries.forEach { padding ->
                    FilterChip(
                        selected = cardStyleConfig.contentPadding == padding,
                        onClick = {
                            onCardStyleConfigChange(cardStyleConfig.copy(contentPadding = padding))
                        },
                        label = { Text(padding.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun InteractiveStatesSection(
    cardStyleConfig: CardStyleConfig,
    onCardStyleConfigChange: (CardStyleConfig) -> Unit
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
                text = "Interactive States",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            // State configuration accordion
            val states = listOf(
                "default" to "Default",
                "hover" to "Hover",
                "pressed" to "Pressed",
                "selected" to "Selected",
                "disabled" to "Disabled",
                "focused" to "Focused"
            )

            states.forEach { (key, label) ->
                StateConfigRow(
                    label = label,
                    config = when (key) {
                        "default" -> cardStyleConfig.interactiveStates.defaultConfig
                        "hover" -> cardStyleConfig.interactiveStates.hoverConfig
                        "pressed" -> cardStyleConfig.interactiveStates.pressedConfig
                        "selected" -> cardStyleConfig.interactiveStates.selectedConfig
                        "disabled" -> cardStyleConfig.interactiveStates.disabledConfig
                        "focused" -> cardStyleConfig.interactiveStates.focusedConfig
                        else -> StateConfig()
                    },
                    onConfigChange = { newConfig ->
                        val newStates = cardStyleConfig.interactiveStates.copy(
                            defaultConfig = if (key == "default") newConfig else cardStyleConfig.interactiveStates.defaultConfig,
                            hoverConfig = if (key == "hover") newConfig else cardStyleConfig.interactiveStates.hoverConfig,
                            pressedConfig = if (key == "pressed") newConfig else cardStyleConfig.interactiveStates.pressedConfig,
                            selectedConfig = if (key == "selected") newConfig else cardStyleConfig.interactiveStates.selectedConfig,
                            disabledConfig = if (key == "disabled") newConfig else cardStyleConfig.interactiveStates.disabledConfig,
                            focusedConfig = if (key == "focused") newConfig else cardStyleConfig.interactiveStates.focusedConfig
                        )
                        onCardStyleConfigChange(cardStyleConfig.copy(interactiveStates = newStates))
                    }
                )
            }
        }
    }
}

@Composable
private fun StateConfigRow(
    label: String,
    config: StateConfig,
    onConfigChange: (StateConfig) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
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
                text = label,
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Scale: ${config.scale}",
                    style = MaterialTheme.typography.bodySmall
                )
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
    }

    if (expanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            SliderWithLabel(
                label = "Scale (${config.scale})",
                value = config.scale,
                onValueChange = { onConfigChange(config.copy(scale = it)) },
                valueRange = 0.8f..1.2f,
                steps = 7
            )
            SliderWithLabel(
                label = "Rotation (${config.rotation}°)",
                value = config.rotation,
                onValueChange = { onConfigChange(config.copy(rotation = it)) },
                valueRange = -15f..15f,
                steps = 29
            )
            SliderWithLabel(
                label = "Alpha (${config.alpha})",
                value = config.alpha,
                onValueChange = { onConfigChange(config.copy(alpha = it)) },
                valueRange = 0.5f..1f,
                steps = 9
            )
        }
    }
}

@Composable
private fun CardPreviewSection(
    cardStyleConfig: CardStyleConfig
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
                text = "Preview",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            // Simple card preview
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                shape = when (cardStyleConfig.shape) {
                    CardShape.RECTANGLE -> RoundedCornerShape(0.dp)
                    CardShape.ROUNDED -> RoundedCornerShape(SugarDimens.Radius.lg)
                    CardShape.CIRCLE -> RoundedCornerShape(75.dp)
                    CardShape.SQUIRCLE -> RoundedCornerShape(percent = 25)
                    else -> RoundedCornerShape(SugarDimens.Radius.md)
                },
                shadowElevation = cardStyleConfig.elevation,
                color = Color(android.graphics.Color.parseColor(cardStyleConfig.surfaceColor))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(SugarDimens.Spacing.lg),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Card Preview")
                }
            }
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
