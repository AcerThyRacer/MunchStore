package com.sugarmunch.app.theme.builder

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kotlin.math.max

/**
 * Step 4: Preview - Shows a live preview of the theme being built.
 *
 * Displays a mock UI with the selected colors, gradient, and effects,
 * along with a theme summary and name input for final creation.
 *
 * @param state Current theme builder state
 * @param onThemeCreated Callback when the user confirms theme creation
 */
@Composable
fun PreviewStep(
    state: ThemeBuilderState,
    onThemeCreated: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme preview card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = state.backgroundColorAsColor()
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Gradient background
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val brush = when (state.gradientType) {
                            GradientType.LINEAR -> Brush.linearGradient(
                                colors = state.gradientColorsAsColors(),
                                start = Offset(0f, 0f),
                                end = Offset(size.width, size.height)
                            )
                            GradientType.RADIAL -> Brush.radialGradient(
                                colors = state.gradientColorsAsColors(),
                                center = Offset(size.width / 2, size.height / 2),
                                radius = max(size.width, size.height) / 2
                            )
                            GradientType.SWEEP -> Brush.sweepGradient(
                                colors = state.gradientColorsAsColors(),
                                center = Offset(size.width / 2, size.height / 2)
                            )
                        }
                        drawRect(brush = brush, alpha = 0.3f)
                    }

                    // Preview content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Sample app bar
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            color = state.primaryColorAsColor(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(100.dp, 12.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.3f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sample cards
                        repeat(3) { index ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                                color = state.surfaceColorAsColor(),
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(state.secondaryColorAsColor())
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.7f)
                                                .height(12.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                        )
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f)
                                                .height(12.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Animated particles
                    if (state.enableParticles) {
                        repeat(state.particleDensity / 10) { index ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .offset {
                                        Offset(
                                            (animatedOffset + index * 37) % 100,
                                            (animatedOffset * 0.7f + index * 23) % 100
                                        ).let {
                                            IntOffset(
                                                it.x.toInt().dp.roundToPx(),
                                                it.y.toInt().dp.roundToPx()
                                            )
                                        }
                                    }
                                    .clip(CircleShape)
                                    .background(state.primaryColorAsColor().copy(alpha = 0.5f))
                            )
                        }
                    }
                }
            }
        }

        // Theme info card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Theme Summary",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    InfoRow("Primary", state.primaryColorAsColor().toHex())
                    InfoRow("Secondary", state.secondaryColorAsColor().toHex())
                    InfoRow("Background", state.backgroundColorAsColor().toHex())
                    InfoRow("Gradient", "${state.gradientType.name} (${state.gradientColors.size} colors)")

                    if (state.enableParticles) {
                        InfoRow("Particles", "${state.particleType.displayName}, ${state.particleDensity} particles")
                    }

                    if (state.enableAnimation) {
                        InfoRow("Animation", "Enabled")
                    }
                }
            }
        }

        // Name input
        item {
            var themeName by remember { mutableStateOf("") }

            OutlinedTextField(
                value = themeName,
                onValueChange = { themeName = it },
                label = { Text("Theme Name") },
                placeholder = { Text("My Custom Theme") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onThemeCreated,
                modifier = Modifier.fillMaxWidth(),
                enabled = themeName.isNotBlank()
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Theme")
            }
        }
    }
}

/**
 * A row displaying a label-value pair for theme information.
 *
 * @param label The label text
 * @param value The value text
 */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
