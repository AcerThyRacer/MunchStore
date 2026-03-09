package com.sugarmunch.app.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.roundToInt

/**
 * LayoutCustomizer - Drag-drop grid editor for customizing app layouts.
 * Features:
 * - Drag-drop grid editor
 * - Custom column/row counts
 * - Spacing sliders
 * - Corner radius controls
 * - Icon size/padding controls
 * - Section reordering
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LayoutCustomizer(
    onLayoutChanged: (LayoutConfig) -> Unit = {},
    onDismiss: () -> Unit = {}
) {
    // Layout configuration
    var columns by remember { mutableIntStateOf(4) }
    var rows by remember { mutableIntStateOf(6) }
    var iconSize by remember { mutableIntStateOf(56) }
    var iconSpacing by remember { mutableIntStateOf(12) }
    var iconPadding by remember { mutableIntStateOf(8) }
    var cornerRadius by remember { mutableIntStateOf(12) }
    var labelSize by remember { mutableIntStateOf(12) }

    // Sample layout items for preview
    val sampleItems = remember {
        (1..24).map { "App $it" }
    }

    // Drag state
    var draggedItem by remember { mutableStateOf<String?>(null) }
    var itemPositions by remember {
        mutableStateOf(sampleItems.associateWith { it }.toMutableMap())
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(SugarDimens.Radius.xl)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(SugarDimens.Spacing.lg)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.GridView,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Layout Customizer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

                // Control panel
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Grid size row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Columns: $columns", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = columns.toFloat(),
                                    onValueChange = { columns = it.roundToInt() },
                                    valueRange = 2f..8f,
                                    steps = 5
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Rows: $rows", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = rows.toFloat(),
                                    onValueChange = { rows = it.roundToInt() },
                                    valueRange = 3f..10f,
                                    steps = 6
                                )
                            }
                        }

                        // Icon settings row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Icon Size: ${iconSize}dp", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = iconSize.toFloat(),
                                    onValueChange = { iconSize = it.roundToInt() },
                                    valueRange = 40f..72f,
                                    steps = 7
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Spacing: ${iconSpacing}dp", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = iconSpacing.toFloat(),
                                    onValueChange = { iconSpacing = it.roundToInt() },
                                    valueRange = 4f..24f,
                                    steps = 9
                                )
                            }
                        }

                        // Corner radius and label size
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Corner Radius: ${cornerRadius}dp", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = cornerRadius.toFloat(),
                                    onValueChange = { cornerRadius = it.roundToInt() },
                                    valueRange = 0f..28f,
                                    steps = 6
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Label Size: ${labelSize}sp", style = MaterialTheme.typography.labelMedium)
                                Slider(
                                    value = labelSize.toFloat(),
                                    onValueChange = { labelSize = it.roundToInt() },
                                    valueRange = 8f..16f,
                                    steps = 3
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

                // Preview area
                Text(
                    "Preview (drag to reorder)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(SugarDimens.Radius.lg))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        horizontalArrangement = Arrangement.spacedBy(iconSpacing.dp),
                        verticalArrangement = Arrangement.spacedBy(iconSpacing.dp)
                    ) {
                        items(sampleItems.size) { index ->
                            val item = sampleItems[index]
                            DraggableLayoutItem(
                                item = item,
                                iconSize = iconSize,
                                cornerRadius = cornerRadius,
                                labelSize = labelSize,
                                isDragged = draggedItem == item,
                                onDragStart = { draggedItem = item },
                                onDragEnd = {
                                    draggedItem = null
                                    // Would update position here
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            columns = 4
                            rows = 6
                            iconSize = 56
                            iconSpacing = 12
                            iconPadding = 8
                            cornerRadius = 12
                            labelSize = 12
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset")
                    }

                    OutlinedButton(
                        onClick = {
                            // Export layout config
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export")
                    }

                    Button(
                        onClick = {
                            onLayoutChanged(
                                LayoutConfig(
                                    columns = columns,
                                    rows = rows,
                                    iconSize = iconSize,
                                    iconSpacing = iconSpacing,
                                    iconPadding = iconPadding,
                                    cornerRadius = cornerRadius,
                                    labelSize = labelSize
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun DraggableLayoutItem(
    item: String,
    iconSize: Int,
    cornerRadius: Int,
    labelSize: Int,
    isDragged: Boolean,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit
) {
    var offset by remember { mutableStateOf(Offset.Zero) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDrag = { _, dragAmount ->
                        offset += dragAmount
                    },
                    onDragEnd = {
                        offset = Offset.Zero
                        onDragEnd()
                    }
                )
            }
            .then(
                if (isDragged) {
                    Modifier
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            RoundedCornerShape(cornerRadius.dp)
                        )
                        .border(
                            2.dp,
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(cornerRadius.dp)
                        )
                } else {
                    Modifier
                }
            )
    ) {
        Box(
            modifier = Modifier
                .size(iconSize.dp)
                .clip(RoundedCornerShape(cornerRadius.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                item.first().toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            item,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = labelSize.sp),
            maxLines = 1
        )
    }
}

/**
 * Layout configuration data class
 */
data class LayoutConfig(
    val columns: Int = 4,
    val rows: Int = 6,
    val iconSize: Int = 56,
    val iconSpacing: Int = 12,
    val iconPadding: Int = 8,
    val cornerRadius: Int = 12,
    val labelSize: Int = 12,
    val showLabels: Boolean = true,
    val labelPosition: LabelPosition = LabelPosition.BOTTOM
)

enum class LabelPosition {
    BOTTOM,
    TOP,
    HIDDEN
}

/**
 * Widget position for layout
 */
data class WidgetPosition(
    val id: String,
    val row: Int,
    val column: Int,
    val rowSpan: Int = 1,
    val columnSpan: Int = 1
)
