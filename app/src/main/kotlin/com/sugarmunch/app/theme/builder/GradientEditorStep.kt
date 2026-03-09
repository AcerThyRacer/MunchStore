package com.sugarmunch.app.theme.builder

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin

/**
 * Step 2: Gradient Editor - Allows users to configure gradient backgrounds.
 *
 * Provides controls for gradient type (linear, radial, sweep), angle adjustment,
 * color selection, and a live preview.
 *
 * @param state Current theme builder state
 * @param onStateChange Callback to update the state
 */
@Composable
fun GradientEditorStep(
    state: ThemeBuilderState,
    onStateChange: (ThemeBuilderState) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Gradient type selector
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Gradient Type",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = state.gradientType == GradientType.LINEAR,
                            onClick = { onStateChange(state.copy(gradientType = GradientType.LINEAR)) },
                            label = { Text("Linear") },
                            leadingIcon = if (state.gradientType == GradientType.LINEAR) {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = state.gradientType == GradientType.RADIAL,
                            onClick = { onStateChange(state.copy(gradientType = GradientType.RADIAL)) },
                            label = { Text("Radial") },
                            leadingIcon = if (state.gradientType == GradientType.RADIAL) {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                            } else null
                        )
                        FilterChip(
                            selected = state.gradientType == GradientType.SWEEP,
                            onClick = { onStateChange(state.copy(gradientType = GradientType.SWEEP)) },
                            label = { Text("Sweep") },
                            leadingIcon = if (state.gradientType == GradientType.SWEEP) {
                                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // Gradient angle (for linear)
        if (state.gradientType == GradientType.LINEAR) {
            item {
                GradientAnglePicker(
                    angle = state.gradientAngle,
                    onAngleChange = { onStateChange(state.copy(gradientAngle = it)) }
                )
            }
        }

        // Gradient colors
        item {
            GradientColorPicker(
                gradientColors = state.gradientColorsAsColors(),
                onGradientColorsChange = { colors ->
                    onStateChange(state.copy(gradientColors = colors.map { it.value }))
                }
            )
        }

        // Gradient preview
        item {
            GradientPreview(
                gradientType = state.gradientType,
                gradientColors = state.gradientColorsAsColors(),
                gradientAngle = state.gradientAngle
            )
        }
    }
}

/**
 * Angle picker slider for linear gradients.
 *
 * @param angle Current angle in degrees
 * @param onAngleChange Callback when angle changes
 */
@Composable
private fun GradientAnglePicker(
    angle: Float,
    onAngleChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Angle",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "%.0f°".format(angle),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = angle,
                onValueChange = onAngleChange,
                valueRange = 0f..360f,
                steps = 35
            )
        }
    }
}

/**
 * Color picker for gradient stops.
 *
 * @param gradientColors List of colors in the gradient
 * @param onGradientColorsChange Callback when colors change
 */
@Composable
private fun GradientColorPicker(
    gradientColors: List<Color>,
    onGradientColorsChange: (List<Color>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Gradient Colors",
                    style = MaterialTheme.typography.titleMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (gradientColors.size < 5) {
                                onGradientColorsChange(gradientColors + Color.White)
                            }
                        },
                        enabled = gradientColors.size < 5
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add color")
                    }
                    IconButton(
                        onClick = {
                            if (gradientColors.size > 2) {
                                onGradientColorsChange(gradientColors.dropLast(1))
                            }
                        },
                        enabled = gradientColors.size > 2
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Remove color")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Color stops
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                gradientColors.forEachIndexed { index, color ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color)
                            .border(2.dp, MaterialTheme.colorScheme.onSurfaceVariant, RoundedCornerShape(8.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${gradientColors.size} colors (2-5 allowed)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Live preview of the gradient configuration.
 *
 * @param gradientType Type of gradient (linear, radial, sweep)
 * @param gradientColors Colors in the gradient
 * @param gradientAngle Angle for linear gradients
 */
@Composable
private fun GradientPreview(
    gradientType: GradientType,
    gradientColors: List<Color>,
    gradientAngle: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Draw gradient preview
            androidx.compose.foundation.Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val brush = when (gradientType) {
                    GradientType.LINEAR -> {
                        val radians = Math.toRadians(gradientAngle.toDouble()).toFloat()
                        val startX = size.width / 2 + cos(radians) * size.width / 2
                        val startY = size.height / 2 + sin(radians) * size.height / 2
                        val endX = size.width / 2 - cos(radians) * size.width / 2
                        val endY = size.height / 2 - sin(radians) * size.height / 2
                        Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(startX, startY),
                            end = Offset(endX, endY)
                        )
                    }
                    GradientType.RADIAL -> {
                        Brush.radialGradient(
                            colors = gradientColors,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = max(size.width, size.height) / 2
                        )
                    }
                    GradientType.SWEEP -> {
                        Brush.sweepGradient(
                            colors = gradientColors,
                            center = Offset(size.width / 2, size.height / 2)
                        )
                    }
                }
                drawRect(brush = brush)
            }

            // Overlay text
            Text(
                text = "Gradient Preview",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.Center)
                    .shadow(4.dp)
            )
        }
    }
}
