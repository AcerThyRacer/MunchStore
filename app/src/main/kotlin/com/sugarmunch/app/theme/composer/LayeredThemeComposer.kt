package com.sugarmunch.app.theme.composer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draggablescrollable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.sugarmunch.app.theme.model.*
import kotlinx.coroutines.launch

/**
 * Layered Theme Composer - Photoshop-like interface for building themes
 *
 * Features:
 * - Layer list with drag-and-drop reordering
 * - Layer visibility toggle
 * - Opacity sliders per layer
 * - Blend mode selection
 * - Layer properties editor
 * - Real-time preview
 * - Add/remove/duplicate layers
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayeredThemeComposer(
    layeredTheme: LayeredThemeConfig,
    onThemeChange: (LayeredThemeConfig) -> Unit,
    onNavigateBack: () -> Unit,
    onSaveTheme: (LayeredThemeConfig) -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedLayerId by remember { mutableStateOf<String?>(null) }
    var showAddLayerDialog by remember { mutableStateOf(false) }
    var showBlendModeDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val selectedLayer = selectedLayerId?.let { id ->
        layeredTheme.layers.find { it.id == id }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Layered Theme Composer")
                        Text(
                            text = "${layeredTheme.layers.count { it.isEnabled }}/${layeredTheme.layers.size} layers active",
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
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Outlined.FileUpload, contentDescription = "Export")
                    }
                    IconButton(onClick = { onSaveTheme(layeredTheme) }) {
                        Icon(Icons.Default.Check, contentDescription = "Save")
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
            // Left Panel - Layer List
            LayerListPanel(
                layers = layeredTheme.layers,
                selectedLayerId = selectedLayerId,
                onLayerSelected = { selectedLayerId = it },
                onLayerVisibilityToggled = { layerId ->
                    onThemeChange(layeredTheme.updateLayer(layerId) { toggle() })
                },
                onLayerOpacityChanged = { layerId, opacity ->
                    onThemeChange(layeredTheme.updateLayer(layerId) { withOpacity(opacity) })
                },
                onLayerReordered = { layerId, newIndex ->
                    onThemeChange(layeredTheme.reorderLayer(layerId, newIndex))
                },
                onLayerDeleted = { layerId ->
                    onThemeChange(layeredTheme.removeLayer(layerId))
                    if (selectedLayerId == layerId) selectedLayerId = null
                },
                onLayerDuplicated = { layerId ->
                    onThemeChange(layeredTheme.duplicateLayer(layerId))
                },
                onAddLayerClick = { showAddLayerDialog = true },
                modifier = Modifier.weight(1f)
            )

            // Right Panel - Layer Properties
            if (selectedLayer != null) {
                LayerPropertiesPanel(
                    layer = selectedLayer,
                    onBlendModeClick = { showBlendModeDialog = true },
                    onLayerChange = { updatedLayer ->
                        onThemeChange(
                            layeredTheme.updateLayer(selectedLayer.id) { updatedLayer }
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            } else {
                EmptyStatePanel(
                    title = "No Layer Selected",
                    description = "Select a layer from the list to view and edit its properties",
                    icon = Icons.Outlined.Layers,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

    // Add Layer Dialog
    if (showAddLayerDialog) {
        AddLayerDialog(
            onDismiss = { showAddLayerDialog = false },
            onLayerAdded = { newLayer ->
                onThemeChange(layeredTheme.addLayer(newLayer))
                showAddLayerDialog = false
            }
        )
    }

    // Blend Mode Dialog
    if (showBlendModeDialog && selectedLayer != null) {
        BlendModeDialog(
            currentBlendMode = selectedLayer.blendMode,
            onDismiss = { showBlendModeDialog = false },
            onBlendModeSelected = { blendMode ->
                onThemeChange(
                    layeredTheme.updateLayer(selectedLayer.id) { withBlendMode(blendMode) }
                )
                showBlendModeDialog = false
            }
        )
    }

    // Export Dialog
    if (showExportDialog) {
        ExportThemeDialog(
            layeredTheme = layeredTheme,
            onDismiss = { showExportDialog = false }
        )
    }
}

// ═════════════════════════════════════════════════════════════
// LAYER LIST PANEL
// ═════════════════════════════════════════════════════════════

@Composable
private fun LayerListPanel(
    layers: List<ThemeLayer>,
    selectedLayerId: String?,
    onLayerSelected: (String) -> Unit,
    onLayerVisibilityToggled: (String) -> Unit,
    onLayerOpacityChanged: (String, Float) -> Unit,
    onLayerReordered: (String, Int) -> Unit,
    onLayerDeleted: (String) -> Unit,
    onLayerDuplicated: (String) -> Unit,
    onAddLayerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    var expandedLayerId by remember { mutableStateOf<String?>(null) }

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
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "LAYERS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            IconButton(
                onClick = onAddLayerClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Layer", tint = MaterialTheme.colorScheme.primary)
            }
        }

        // Layer List
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Render layers in reverse order (top layer first)
            items(layers.asReversed(), key = { it.id }) { layer ->
                LayerListItem(
                    layer = layer,
                    isSelected = layer.id == selectedLayerId,
                    isExpanded = layer.id == expandedLayerId,
                    onClick = { onLayerSelected(layer.id) },
                    onVisibilityToggled = { onLayerVisibilityToggled(layer.id) },
                    onOpacityChanged = { onLayerOpacityChanged(layer.id, it) },
                    onExpandedChange = { expandedLayerId = if (it) layer.id else null },
                    onDuplicate = { onLayerDuplicated(layer.id) },
                    onDelete = { onLayerDeleted(layer.id) }
                )
            }
        }

        // Global Controls
        GlobalLayerControls(
            globalOpacity = layers.globalOpacity,
            globalBlendMode = layers.globalBlendMode,
            onOpacityChanged = { layers.copy(globalOpacity = it) },
            onBlendModeClick = { }
        )
    }
}

@Composable
private fun LayerListItem(
    layer: ThemeLayer,
    isSelected: Boolean,
    isExpanded: Boolean,
    onClick: () -> Unit,
    onVisibilityToggled: () -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showContextMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else {
                    Modifier
                }
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Main Layer Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Visibility Toggle
                IconButton(
                    onClick = onVisibilityToggled,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (layer.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = "Toggle Visibility",
                        tint = if (layer.isEnabled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Layer Type Icon
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(getLayerColor(layer.type).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = layer.type.icon,
                        fontSize = 16.sp
                    )
                }

                // Layer Info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = layer.type.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                    Text(
                        text = layer.id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                // Expand/Collapse
                IconButton(
                    onClick = { onExpandedChange(!isExpanded) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand",
                        modifier = Modifier.rotate(if (isExpanded) 180f else 0f)
                    )
                }

                // Context Menu
                Box {
                    IconButton(
                        onClick = { showContextMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }

                    DropdownMenu(
                        expanded = showContextMenu,
                        onDismissRequest = { showContextMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Duplicate") },
                            onClick = {
                                onDuplicate()
                                showContextMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDelete()
                                showContextMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            },
                            colors = MenuItemDefaults.colors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }

            // Expanded Controls
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Opacity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Opacity,
                            contentDescription = "Opacity",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Slider(
                            value = layer.opacity,
                            onValueChange = onOpacityChanged,
                            valueRange = 0f..1f,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${(layer.opacity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(40.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    // Blend Mode Chip
                    AssistChip(
                        onClick = { },
                        label = { Text(layer.blendMode.name) },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.BlurOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun GlobalLayerControls(
    globalOpacity: Float,
    globalBlendMode: BlendMode,
    onOpacityChanged: (Float) -> Unit,
    onBlendModeClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "GLOBAL SETTINGS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Global Opacity
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Opacity",
                    style = MaterialTheme.typography.bodySmall
                )
                Slider(
                    value = globalOpacity,
                    onValueChange = onOpacityChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${(globalOpacity * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.width(40.dp),
                    textAlign = TextAlign.End
                )
            }

            // Global Blend Mode
            OutlinedButton(
                onClick = onBlendModeClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Outlined.BlurOn, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Blend: ${globalBlendMode.name}")
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// LAYER PROPERTIES PANEL
// ═════════════════════════════════════════════════════════════

@Composable
private fun LayerPropertiesPanel(
    layer: ThemeLayer,
    onBlendModeClick: () -> Unit,
    onLayerChange: (ThemeLayer) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxHeight()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
            .horizontalScroll(scrollState)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(getLayerColor(layer.type).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = layer.type.icon, fontSize = 20.sp)
                }
                Column {
                    Text(
                        text = layer.type.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = layer.id,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Properties based on layer type
        when (layer) {
            is ThemeLayer.BackgroundLayer -> BackgroundLayerProperties(layer, onLayerChange)
            is ThemeLayer.ParticleLayer -> ParticleLayerProperties(layer, onLayerChange)
            is ThemeLayer.ColorOverlay -> ColorOverlayProperties(layer, onLayerChange)
            is ThemeLayer.TextureLayer -> TextureLayerProperties(layer, onLayerChange)
            is ThemeLayer.LightEffects -> LightEffectsProperties(layer, onLayerChange)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Blend Mode Section
        Text(
            text = "BLEND MODE",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = onBlendModeClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Outlined.BlurOn, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(layer.blendMode.name)
        }
    }
}

@Composable
private fun BackgroundLayerProperties(
    layer: ThemeLayer.BackgroundLayer,
    onLayerChange: (ThemeLayer) -> Unit
) {
    Text(
        text = "BACKGROUND SETTINGS",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Animation Speed
    SliderWithLabel(
        label = "Animation Speed",
        value = layer.animationSpeed,
        valueRange = 0f..3f,
        onValueChange = { onLayerChange(layer.copy(animationSpeed = it)) },
        valueSuffix = "x"
    )
}

@Composable
private fun ParticleLayerProperties(
    layer: ThemeLayer.ParticleLayer,
    onLayerChange: (ThemeLayer) -> Unit
) {
    Text(
        text = "PARTICLE SETTINGS",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Density
    SliderWithLabel(
        label = "Density",
        value = layer.density.last.toFloat(),
        valueRange = 10f..500f,
        onValueChange = { onLayerChange(layer.copy(density = layer.density.first..it.toInt())) },
        valueSuffix = " particles"
    )

    // Speed
    SliderWithLabel(
        label = "Speed",
        value = layer.speed.max,
        valueRange = 0.1f..10f,
        onValueChange = { onLayerChange(layer.copy(speed = layer.speed.min..it)) },
        valueSuffix = "x"
    )

    // Size
    SliderWithLabel(
        label = "Size",
        value = layer.size.max,
        valueRange = 1f..50f,
        onValueChange = { onLayerChange(layer.copy(size = layer.size.min..it)) },
        valueSuffix = "dp"
    )

    // Physics Toggle
    SwitchWithLabel(
        label = "Physics Enabled",
        checked = layer.physicsEnabled,
        onCheckedChange = { onLayerChange(layer.copy(physicsEnabled = it)) }
    )
}

@Composable
private fun ColorOverlayProperties(
    layer: ThemeLayer.ColorOverlay,
    onLayerChange: (ThemeLayer) -> Unit
) {
    Text(
        text = "COLOR OVERLAY SETTINGS",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Color Picker Placeholder
    ColorPickerRow(
        label = "Overlay Color",
        color = layer.color,
        onColorSelected = { onLayerChange(layer.copy(color = it)) }
    )

    // Animation Speed
    SliderWithLabel(
        label = "Animation Speed",
        value = layer.animationSpeed,
        valueRange = 0f..3f,
        onValueChange = { onLayerChange(layer.copy(animationSpeed = it)) },
        valueSuffix = "x"
    )
}

@Composable
private fun TextureLayerProperties(
    layer: ThemeLayer.TextureLayer,
    onLayerChange: (ThemeLayer) -> Unit
) {
    Text(
        text = "TEXTURE SETTINGS",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Texture Type Selector
    Text(
        text = "Texture Type",
        style = MaterialTheme.typography.bodyMedium
    )

    Spacer(modifier = Modifier.height(4.dp))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TextureType.entries) { type ->
            FilterChip(
                selected = layer.textureType == type,
                onClick = { onLayerChange(layer.copy(textureType = type)) },
                label = { Text(type.displayName) }
            )
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Scale
    SliderWithLabel(
        label = "Scale",
        value = layer.scale,
        valueRange = 0.1f..5f,
        onValueChange = { onLayerChange(layer.copy(scale = it)) },
        valueSuffix = "x"
    )

    // Rotation
    SliderWithLabel(
        label = "Rotation",
        value = layer.rotation,
        valueRange = 0f..360f,
        onValueChange = { onLayerChange(layer.copy(rotation = it)) },
        valueSuffix = "°"
    )
}

@Composable
private fun LightEffectsProperties(
    layer: ThemeLayer.LightEffects,
    onLayerChange: (ThemeLayer) -> Unit
) {
    Text(
        text = "LIGHT EFFECTS SETTINGS",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(8.dp))

    // Bloom Strength
    SliderWithLabel(
        label = "Bloom Strength",
        value = layer.bloomStrength,
        valueRange = 0f..2f,
        onValueChange = { onLayerChange(layer.copy(bloomStrength = it)) },
        valueSuffix = "x"
    )

    // Glow Radius
    SliderWithLabel(
        label = "Glow Radius",
        value = layer.glowRadius,
        valueRange = 10f..200f,
        onValueChange = { onLayerChange(layer.copy(glowRadius = it)) },
        valueSuffix = "dp"
    )

    // Highlight Intensity
    SliderWithLabel(
        label = "Highlight Intensity",
        value = layer.highlightIntensity,
        valueRange = 0f..2f,
        onValueChange = { onLayerChange(layer.copy(highlightIntensity = it)) },
        valueSuffix = "x"
    )

    // Glow Color
    ColorPickerRow(
        label = "Glow Color",
        color = layer.glowColor,
        onColorSelected = { onLayerChange(layer.copy(glowColor = it)) }
    )
}

// ═════════════════════════════════════════════════════════════
// ADD LAYER DIALOG
// ═════════════════════════════════════════════════════════════

@Composable
private fun AddLayerDialog(
    onDismiss: () -> Unit,
    onLayerAdded: (ThemeLayer) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Layer") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Select a layer type to add:",
                    style = MaterialTheme.typography.bodyMedium
                )

                ThemeLayer.LayerType.entries.forEach { type ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLayerAdded(createDefaultLayer(type))
                            },
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
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(getLayerColor(type).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = type.icon, fontSize = 20.sp)
                            }
                            Column {
                                Text(
                                    text = type.displayName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = getLayerDescription(type),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
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
private fun BlendModeDialog(
    currentBlendMode: BlendMode,
    onDismiss: () -> Unit,
    onBlendModeSelected: (BlendMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Blend Mode") },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(BlendModePresets.OVERLAY_BLEND_MODES) { blendMode ->
                    FilterChip(
                        selected = blendMode == currentBlendMode,
                        onClick = { onBlendModeSelected(blendMode) },
                        label = { Text(blendMode.name) },
                        modifier = Modifier.fillMaxWidth()
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
private fun ExportThemeDialog(
    layeredTheme: LayeredThemeConfig,
    onDismiss: () -> Unit
) {
    val json = kotlinx.serialization.json.Json { prettyPrint = true }
    val exportedJson = remember { json.encodeToString(layeredTheme) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Layered Theme") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Your layered theme has been exported to JSON. You can save this code and import it later.",
                    style = MaterialTheme.typography.bodyMedium
                )

                OutlinedTextField(
                    value = exportedJson,
                    onValueChange = { },
                    label = { Text("Theme JSON") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    readOnly = true,
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

// ═════════════════════════════════════════════════════════════
// HELPER COMPOSABLES
// ═════════════════════════════════════════════════════════════

@Composable
private fun SliderWithLabel(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    valueSuffix: String = ""
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "${String.format("%.1f", value)}$valueSuffix",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
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
            style = MaterialTheme.typography.bodyMedium
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun ColorPickerRow(
    label: String,
    color: Color,
    onColorSelected: (Color) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color)
                .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
                .clickable { /* Open color picker */ },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Colorize,
                contentDescription = "Pick Color",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun EmptyStatePanel(
    title: String,
    description: String,
    icon: ImageVector,
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
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ═════════════════════════════════════════════════════════════
// UTILITIES
// ═════════════════════════════════════════════════════════════

private fun getLayerColor(type: ThemeLayer.LayerType): Color {
    return when (type) {
        ThemeLayer.LayerType.BACKGROUND -> Color(0xFF4CAF50)
        ThemeLayer.LayerType.PARTICLES -> Color(0xFFFF9800)
        ThemeLayer.LayerType.COLOR_OVERLAY -> Color(0xFF2196F3)
        ThemeLayer.LayerType.TEXTURE -> Color(0xFF9C27B0)
        ThemeLayer.LayerType.LIGHT_EFFECTS -> Color(0xFFFFEB3B)
    }
}

private fun createDefaultLayer(type: ThemeLayer.LayerType): ThemeLayer {
    val id = "${type.name.lowercase()}_${System.currentTimeMillis()}"
    return when (type) {
        ThemeLayer.LayerType.BACKGROUND -> ThemeLayer.BackgroundLayer(
            id = id,
            isEnabled = true,
            opacity = 1f,
            blendMode = BlendMode.Normal,
            gradient = GradientConfig.DEFAULT,
            animationSpeed = 1f
        )
        ThemeLayer.LayerType.PARTICLES -> ThemeLayer.ParticleLayer(
            id = id,
            isEnabled = true,
            opacity = 1f,
            blendMode = BlendMode.Screen,
            particleType = ParticleType.CIRCLES,
            density = 50..100,
            speed = 1f..3f,
            size = 4f..12f,
            physicsEnabled = true
        )
        ThemeLayer.LayerType.COLOR_OVERLAY -> ThemeLayer.ColorOverlay(
            id = id,
            isEnabled = false,
            opacity = 0.2f,
            blendMode = BlendMode.Overlay,
            color = Color.White,
            animationSpeed = 0f
        )
        ThemeLayer.LayerType.TEXTURE -> ThemeLayer.TextureLayer(
            id = id,
            isEnabled = false,
            opacity = 0.5f,
            blendMode = BlendMode.Normal,
            textureType = TextureType.NOISE,
            scale = 1f,
            rotation = 0f
        )
        ThemeLayer.LayerType.LIGHT_EFFECTS -> ThemeLayer.LightEffects(
            id = id,
            isEnabled = true,
            opacity = 1f,
            blendMode = BlendMode.Screen,
            bloomStrength = 1f,
            glowColor = Color.White,
            glowRadius = 50f,
            highlightIntensity = 1f
        )
    }
}

private fun getLayerDescription(type: ThemeLayer.LayerType): String {
    return when (type) {
        ThemeLayer.LayerType.BACKGROUND -> "Gradient or solid color backgrounds"
        ThemeLayer.LayerType.PARTICLES -> "Animated particle systems"
        ThemeLayer.LayerType.COLOR_OVERLAY -> "Color tints with blend modes"
        ThemeLayer.LayerType.TEXTURE -> "Image or pattern overlays"
        ThemeLayer.LayerType.LIGHT_EFFECTS -> "Bloom, glow, and highlights"
    }
}
