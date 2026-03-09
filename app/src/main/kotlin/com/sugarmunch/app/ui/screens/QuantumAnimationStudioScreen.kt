package com.sugarmunch.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.physics.quantum.*
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager

/**
 * Quantum Animation Studio Screen
 * Physics-based animation customization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantumAnimationStudioScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val animationEngine = remember { QuantumAnimationEngine.getInstance() }

    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)

    val animationState by animationEngine.animationState.collectAsState()
    val activeAnimations by animationEngine.activeAnimations.collectAsState()
    val performanceConfig by remember { mutableStateOf(animationEngine.performanceConfig) }

    var selectedPreset by remember { mutableStateOf<AnimationPreset>(AnimationPreset.BALANCED) }
    var showPhysicsSettings by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationEngine.start()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quantum Animation Studio") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showPhysicsSettings = true }) {
                        Icon(Icons.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedThemeBackground()

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Animation Status
                item {
                    AnimationStatusCard(
                        isRunning = animationState == AnimationState.RUNNING,
                        activeCount = activeAnimations.size,
                        fps = if (performanceConfig.lowPowerMode) 30 else 60
                    )
                }

                // Performance Presets
                item {
                    PerformancePresetsCard(
                        selectedPreset = selectedPreset,
                        onPresetSelected = { preset ->
                            selectedPreset = preset
                            animationEngine.performanceConfig = when (preset) {
                                AnimationPreset.GENTLE -> AnimationPresets.GENTLE
                                AnimationPreset.BALANCED -> AnimationPresets.BALANCED
                                AnimationPreset.EXTREME -> AnimationPresets.EXTREME
                            }
                        }
                    )
                }

                // Animation Previews
                item {
                    Text(
                        text = "Animation Previews",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Bounce Preview
                item {
                    AnimationPreviewCard(
                        name = "Bounce",
                        icon = Icons.Default.VerticalAlignBottom,
                        previewContent = {
                            var offset by remember { mutableStateOf(0f) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    animate(
                                        initialValue = 0f,
                                        targetValue = 20f,
                                        animationSpec = spring(
                                            stiffness = 200f,
                                            dampingRatio = 0.1f
                                        )
                                    ) { offset = it }
                                    animate(
                                        initialValue = 20f,
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            stiffness = 200f,
                                            dampingRatio = 0.1f
                                        )
                                    ) { offset = it }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .offset(y = offset.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(colors.primary, colors.secondary)
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                            )
                        },
                        onPlay = {
                            // Play bounce animation
                        }
                    )
                }

                // Shake Preview
                item {
                    AnimationPreviewCard(
                        name = "Shake",
                        icon = Icons.Default.ScreenRotation,
                        previewContent = {
                            var rotation by remember { mutableStateOf(0f) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    animate(
                                        initialValue = 0f,
                                        targetValue = 10f,
                                        animationSpec = tween(50)
                                    ) { rotation = it }
                                    animate(
                                        initialValue = 10f,
                                        targetValue = -10f,
                                        animationSpec = tween(100)
                                    ) { rotation = it }
                                    animate(
                                        initialValue = -10f,
                                        targetValue = 0f,
                                        animationSpec = tween(50)
                                    ) { rotation = it }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .rotate(rotation)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(colors.tertiary, colors.accent)
                                        ),
                                        RoundedCornerShape(12.dp)
                                    )
                            )
                        },
                        onPlay = {
                            // Play shake animation
                        }
                    )
                }

                // Pulse Preview
                item {
                    AnimationPreviewCard(
                        name = "Pulse",
                        icon = Icons.Favorite,
                        previewContent = {
                            var scale by remember { mutableStateOf(1f) }
                            LaunchedEffect(Unit) {
                                while (true) {
                                    animate(
                                        initialValue = 1f,
                                        targetValue = 1.2f,
                                        animationSpec = spring(stiffness = 150f)
                                    ) { scale = it }
                                    animate(
                                        initialValue = 1.2f,
                                        targetValue = 1f,
                                        animationSpec = spring(stiffness = 150f)
                                    ) { scale = it }
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .scale(scale)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(colors.primary, colors.secondary)
                                        ),
                                        RoundedCornerShape(50)
                                    )
                            )
                        },
                        onPlay = {
                            // Play pulse animation
                        }
                    )
                }

                // Animation Effects
                item {
                    Text(
                        text = "Quick Effects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    QuickEffectsRow(
                        onExplosion = {
                            animationEngine.playExplosion(
                                Offset(500f, 1000f),
                                50,
                                listOf(Color.Red, Color.Orange, Color.Yellow)
                            )
                        },
                        onRipple = {
                            animationEngine.playRipple(Offset(500f, 1000f), Color.Blue)
                        },
                        onWave = {
                            // Play wave effect
                        }
                    )
                }

                // Spring Configurations
                item {
                    SpringConfigurationsCard(
                        onSpringSelected = { config ->
                            // Apply spring configuration
                        }
                    )
                }

                // Active Animations
                if (activeAnimations.isNotEmpty()) {
                    item {
                        Text(
                            text = "Active Animations",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    items(activeAnimations) { animation ->
                        ActiveAnimationItem(animation = animation)
                    }
                }
            }
        }
    }
}

@Composable
private fun AnimationStatusCard(
    isRunning: Boolean,
    activeCount: Int,
    fps: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) Color.Green.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = if (isRunning) Color.Green else Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "Running" else "Stopped",
                    fontWeight = FontWeight.Bold
                )
            }

            Row {
                Text("Active: $activeCount")
                Spacer(modifier = Modifier.width(16.dp))
                Text("FPS: $fps")
            }
        }
    }
}

@Composable
private fun PerformancePresetsCard(
    selectedPreset: AnimationPreset,
    onPresetSelected: (AnimationPreset) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Performance Preset",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AnimationPreset.entries) { preset ->
                    FilterChip(
                        selected = selectedPreset == preset,
                        onClick = { onPresetSelected(preset) },
                        label = { Text(preset.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimationPreviewCard(
    name: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    previewContent: @Composable () -> Unit,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = name,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                previewContent()
            }

            IconButton(onClick = onPlay) {
                Icon(Icons.PlayArrow, contentDescription = "Play")
            }
        }
    }
}

@Composable
private fun QuickEffectsRow(
    onExplosion: () -> Unit,
    onRipple: () -> Unit,
    onWave: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onExplosion,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Explosion, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Explosion")
        }

        Button(
            onClick = onRipple,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.WaterDrop, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Ripple")
        }

        Button(
            onClick = onWave,
            modifier = Modifier.weight(1f)
        ) {
            Icon(Icons.Waves, contentDescription = null)
            Spacer(modifier = Modifier.width(4.dp))
            Text("Wave")
        }
    }
}

@Composable
private fun SpringConfigurationsCard(
    onSpringSelected: (SpringPreset) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Spring Presets",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    listOf(
                        SpringPreset.BOUNCY,
                        SpringPreset.GENTLE,
                        SpringPreset.SNAPPY,
                        SpringPreset.WOBBLY,
                        SpringPreset.JELLY
                    )
                ) { preset ->
                    AssistChip(
                        onClick = { onSpringSelected(preset) },
                        label = { Text(preset.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveAnimationItem(animation: ActiveAnimation) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = animation.type.name,
                style = MaterialTheme.typography.labelLarge
            )
            Text(
                text = "${(System.currentTimeMillis() - animation.startTime) / 1000f}s",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

enum class AnimationPreset {
    GENTLE, BALANCED, EXTREME
}

enum class SpringPreset {
    BOUNCY, GENTLE, SNAPPY, WOBBLY, JELLY
}
