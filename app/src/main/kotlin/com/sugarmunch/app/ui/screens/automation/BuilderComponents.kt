package com.sugarmunch.app.ui.screens.automation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.automation.AutomationAction
import com.sugarmunch.app.theme.model.AdjustedColors

/**
 * A card for displaying and selecting triggers in the trigger selection step.
 *
 * @param name The display name of the trigger
 * @param description A brief description of the trigger
 * @param isSelected Whether this trigger is currently selected
 * @param colors The theme colors to use for styling
 * @param onClick Callback when the card is clicked
 */
@Composable
fun TriggerCard(
    name: String,
    description: String,
    isSelected: Boolean,
    colors: AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                colors.primary.copy(alpha = 0.15f)
            else
                colors.surface.copy(alpha = 0.95f)
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, colors.primary)
        } else null,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) colors.primary else colors.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = colors.primary
                )
            }
        }
    }
}

/**
 * A card for displaying an action in the action picker dialog.
 *
 * @param name The action name to display
 * @param description The action description to display
 * @param colors The theme colors to use for styling
 * @param onClick Callback when the card is clicked
 */
@Composable
fun ActionPickerCard(
    name: String,
    description: String,
    colors: AdjustedColors,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = colors.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface.copy(alpha = 0.6f)
                )
            }

            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = colors.primary
            )
        }
    }
}

/**
 * A quick action chip for adding common conditions.
 *
 * @param label The text to display on the chip
 * @param onClick Callback when the chip is clicked
 * @param colors The theme colors to use for styling
 */
@Composable
fun QuickConditionChip(
    label: String,
    onClick: () -> Unit,
    colors: AdjustedColors
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = colors.primary.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = colors.primary
            )
        }
    }
}

/**
 * A flow row layout that wraps children to the next line when they exceed
 * the available width.
 *
 * @param modifier Optional modifier for the layout
 * @param horizontalArrangement The horizontal arrangement of the row
 * @param verticalArrangement The vertical arrangement of the rows
 * @param content The content to display in the flow row
 */
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val hGapPx = 8.dp.roundToPx()
        val vGapPx = 8.dp.roundToPx()

        val rows = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        val rowWidths = mutableListOf<Int>()
        val rowHeights = mutableListOf<Int>()

        var currentRow = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentRowWidth = 0
        var currentRowHeight = 0

        measurables.forEach { measurable ->
            val placeable = measurable.measure(constraints)

            if (currentRow.isNotEmpty() &&
                currentRowWidth + hGapPx + placeable.width > constraints.maxWidth
            ) {
                rows.add(currentRow)
                rowWidths.add(currentRowWidth)
                rowHeights.add(currentRowHeight)
                currentRow = mutableListOf()
                currentRowWidth = 0
                currentRowHeight = 0
            }

            currentRow.add(placeable)
            currentRowWidth += if (currentRow.size > 1) hGapPx else 0
            currentRowWidth += placeable.width
            currentRowHeight = maxOf(currentRowHeight, placeable.height)
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
            rowWidths.add(currentRowWidth)
            rowHeights.add(currentRowHeight)
        }

        val height = rowHeights.sum() + (rowHeights.size - 1).coerceAtLeast(0) * vGapPx

        layout(constraints.maxWidth, height) {
            var y = 0
            rows.forEachIndexed { rowIndex, row ->
                var x = when (horizontalArrangement) {
                    Arrangement.End -> constraints.maxWidth - rowWidths[rowIndex]
                    Arrangement.Center -> (constraints.maxWidth - rowWidths[rowIndex]) / 2
                    else -> 0
                }

                row.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + hGapPx
                }

                y += rowHeights[rowIndex] + vGapPx
            }
        }
    }
}
