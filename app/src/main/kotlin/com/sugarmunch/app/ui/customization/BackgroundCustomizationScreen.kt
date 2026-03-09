package com.sugarmunch.app.ui.customization

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Background Customization Screen
 * Comprehensive control over all background types and effects
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackgroundCustomizationScreen(
    onNavigateBack: () -> Unit,
    config: BackgroundConfig,
    onConfigChange: (BackgroundConfig) -> Unit
) {
    var selectedType by remember { mutableStateOf(config.type) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Background Customization") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Background Type Selector
            BackgroundTypeSelector(
                selectedType = selectedType,
                onTypeSelected = { 
                    selectedType = it
                    onConfigChange(config.copy(type = it))
                }
            )
            
            HorizontalDivider()
            
            // Configuration based on type
            when (selectedType) {
                BackgroundType.STATIC -> StaticBackgroundConfigPanel(
                    staticConfig = config.staticConfig,
                    onConfigChange = { onConfigChange(config.copy(staticConfig = it)) }
                )
                BackgroundType.ANIMATED -> AnimatedBackgroundConfigPanel(
                    animatedConfig = config.animatedConfig,
                    onConfigChange = { onConfigChange(config.copy(animatedConfig = it)) }
                )
                BackgroundType.REACTIVE -> ReactiveBackgroundConfigPanel(
                    reactiveConfig = config.reactiveConfig,
                    onConfigChange = { onConfigChange(config.copy(reactiveConfig = it)) }
                )
                BackgroundType.INTERACTIVE -> InteractiveBackgroundConfigPanel(
                    interactiveConfig = config.interactiveConfig,
                    onConfigChange = { onConfigChange(config.copy(interactiveConfig = it)) }
                )
            }
        }
    }
}

@Composable
private fun BackgroundTypeSelector(
    selectedType: BackgroundType,
    onTypeSelected: (BackgroundType) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(SugarDimens.Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
    ) {
        items(BackgroundType.entries) { type ->
            FilterChip(
                selected = type == selectedType,
                onClick = { onTypeSelected(type) },
                label = { Text(type.name.replace("_", " ").lowercase().capitalize()) },
                leadingIcon = if (type == selectedType) {
                    {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Filled.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else null
            )
        }
    }
}

@Composable
private fun StaticBackgroundConfigPanel(
    staticConfig: StaticBackgroundConfig,
    onConfigChange: (StaticBackgroundConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SugarDimens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xl)
    ) {
        // Solid Color Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text(
                    text = "Solid Color",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.hotPink
                )
                
                ColorPickerRow(
                    color = Color(android.graphics.Color.parseColor(staticConfig.solidColor)),
                    onColorSelected = { 
                        onConfigChange(staticConfig.copy(solidColor = "#${it.value.toString(16).uppercase()}"))
                    }
                )
                
                // Color Presets
                ColorPresetGrid(
                    presets = listOf(
                        "#FF1A1A2E", "#FF2D2D44", "#FF3D3D5C",
                        "#FFFF69B4", "#FF00FFA3", "#FFFFD700",
                        "#FFFFA500", "#FF00BFFF", "#FF1A237E"
                    ),
                    selectedColor = staticConfig.solidColor,
                    onColorSelected = { onConfigChange(staticConfig.copy(solidColor = it)) }
                )
            }
        }
        
        // Gradient Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text(
                    text = "Gradient",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.mint
                )
                
                // Gradient Type
                OutlinedDropdownMenu(
                    label = "Gradient Type",
                    selected = staticConfig.gradientType.name,
                    options = GradientType.entries.map { it.name },
                    onOptionSelected = { 
                        onConfigChange(staticConfig.copy(gradientType = GradientType.valueOf(it)))
                    }
                )
                
                // Gradient Colors
                Text("Gradient Colors", style = MaterialTheme.typography.labelLarge)
                staticConfig.gradientColors.forEachIndexed { index, color ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ColorPickerRow(
                            color = Color(android.graphics.Color.parseColor(color)),
                            onColorSelected = {
                                val newColors = staticConfig.gradientColors.toMutableList()
                                newColors[index] = "#${it.value.toString(16).uppercase()}"
                                onConfigChange(staticConfig.copy(gradientColors = newColors))
                            }
                        )
                        
                        if (staticConfig.gradientColors.size > 2) {
                            IconButton(onClick = {
                                val newColors = staticConfig.gradientColors.toMutableList()
                                newColors.removeAt(index)
                                onConfigChange(staticConfig.copy(gradientColors = newColors))
                            }) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                                    contentDescription = "Remove color"
                                )
                            }
                        }
                    }
                }
                
                // Add Color Button
                if (staticConfig.gradientColors.size < 5) {
                    OutlinedButton(
                        onClick = {
                            onConfigChange(
                                staticConfig.copy(
                                    gradientColors = staticConfig.gradientColors + "#FFFFFFFF"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add Color Stop")
                    }
                }
                
                // Gradient Angle
                if (staticConfig.gradientType == GradientType.DIAGONAL) {
                    SliderWithLabel(
                        label = "Gradient Angle",
                        value = staticConfig.gradientAngle,
                        onValueChange = { onConfigChange(staticConfig.copy(gradientAngle = it)) },
                        valueRange = 0f..360f,
                        steps = 359
                    )
                }
                
                // Gradient Spread
                SliderWithLabel(
                    label = "Gradient Spread",
                    value = staticConfig.gradientSpread,
                    onValueChange = { onConfigChange(staticConfig.copy(gradientSpread = it)) },
                    valueRange = 0.5f..2f,
                    steps = 14
                )
            }
        }
        
        // Pattern Library
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text(
                    text = "Pattern Library",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.candyOrange
                )
                
                Text(
                    text = "20+ candy-themed patterns available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                
                // Pattern Grid (simplified for brevity)
                PatternGrid(
                    selectedPatternId = staticConfig.patternId,
                    onPatternSelected = { onConfigChange(staticConfig.copy(patternId = it)) }
                )
            }
        }
    }
}

@Composable
private fun AnimatedBackgroundConfigPanel(
    animatedConfig: AnimatedBackgroundConfig,
    onConfigChange: (AnimatedBackgroundConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SugarDimens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xl)
    ) {
        // Particle Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text(
                    text = "Particle Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.hotPink
                )
                
                SliderWithLabel(
                    label = "Particle Density (${animatedConfig.particleDensity})",
                    value = animatedConfig.particleDensity.toFloat(),
                    onValueChange = { onConfigChange(animatedConfig.copy(particleDensity = it.toInt())) },
                    valueRange = 0f..500f,
                    steps = 499
                )
                
                SliderWithLabel(
                    label = "Particle Speed (${animatedConfig.particleSpeed}x)",
                    value = animatedConfig.particleSpeed,
                    onValueChange = { onConfigChange(animatedConfig.copy(particleSpeed = it)) },
                    valueRange = 0.1f..5f,
                    steps = 48
                )
                
                SliderWithLabel(
                    label = "Particle Size (${animatedConfig.particleSize}px)",
                    value = animatedConfig.particleSize,
                    onValueChange = { onConfigChange(animatedConfig.copy(particleSize = it)) },
                    valueRange = 2f..20f,
                    steps = 17
                )
                
                // Particle Type
                OutlinedDropdownMenu(
                    label = "Particle Type",
                    selected = animatedConfig.particleType.name,
                    options = ParticleType.entries.map { it.name.lowercase().capitalize() },
                    onOptionSelected = { 
                        onConfigChange(animatedConfig.copy(particleType = ParticleType.valueOf(it.uppercase())))
                    }
                )
            }
        }
        
        // Animation Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text(
                    text = "Animation Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.mint
                )
                
                SliderWithLabel(
                    label = "Animation Speed (${animatedConfig.animationSpeed}x)",
                    value = animatedConfig.animationSpeed,
                    onValueChange = { onConfigChange(animatedConfig.copy(animationSpeed = it)) },
                    valueRange = 0f..2f,
                    steps = 19
                )
                
                // Mesh Complexity
                OutlinedDropdownMenu(
                    label = "Mesh Complexity",
                    selected = animatedConfig.meshComplexity.name,
                    options = MeshComplexity.entries.map { it.name },
                    onOptionSelected = { 
                        onConfigChange(animatedConfig.copy(meshComplexity = MeshComplexity.valueOf(it)))
                    }
                )
            }
        }
        
        // Wave Settings
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text(
                    text = "Wave Settings",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.yellow
                )
                
                SliderWithLabel(
                    label = "Wave Amplitude (${animatedConfig.waveAmplitude}px)",
                    value = animatedConfig.waveAmplitude,
                    onValueChange = { onConfigChange(animatedConfig.copy(waveAmplitude = it)) },
                    valueRange = 0f..100f,
                    steps = 99
                )
                
                SliderWithLabel(
                    label = "Wave Frequency (${animatedConfig.waveFrequency})",
                    value = animatedConfig.waveFrequency,
                    onValueChange = { onConfigChange(animatedConfig.copy(waveFrequency = it)) },
                    valueRange = 0.001f..0.1f
                )
            }
        }
    }
}

@Composable
private fun ReactiveBackgroundConfigPanel(
    reactiveConfig: ReactiveBackgroundConfig,
    onConfigChange: (ReactiveBackgroundConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SugarDimens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xl)
    ) {
        // Weather Reactive
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Weather Reactive",
                        style = MaterialTheme.typography.titleMedium,
                        color = SugarDimens.Brand.bubblegumBlue
                    )
                    Switch(
                        checked = reactiveConfig.enableWeatherReactive,
                        onCheckedChange = { 
                            onConfigChange(reactiveConfig.copy(enableWeatherReactive = it))
                        }
                    )
                }
                
                if (reactiveConfig.enableWeatherReactive) {
                    SliderWithLabel(
                        label = "Weather Sensitivity (${(reactiveConfig.weatherSensitivity * 100).toInt()}%)",
                        value = reactiveConfig.weatherSensitivity,
                        onValueChange = { onConfigChange(reactiveConfig.copy(weatherSensitivity = it)) },
                        valueRange = 0f..1f
                    )
                }
            }
        }
        
        // Music Reactive
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Music Reactive",
                        style = MaterialTheme.typography.titleMedium,
                        color = SugarDimens.Brand.candyOrange
                    )
                    Switch(
                        checked = reactiveConfig.enableMusicReactive,
                        onCheckedChange = { 
                            onConfigChange(reactiveConfig.copy(enableMusicReactive = it))
                        }
                    )
                }
                
                if (reactiveConfig.enableMusicReactive) {
                    SliderWithLabel(
                        label = "Beat Threshold (${(reactiveConfig.musicBeatThreshold * 100).toInt()}%)",
                        value = reactiveConfig.musicBeatThreshold,
                        onValueChange = { onConfigChange(reactiveConfig.copy(musicBeatThreshold = it)) },
                        valueRange = 0f..1f
                    )
                }
            }
        }
        
        // Time Reactive
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Time Reactive",
                        style = MaterialTheme.typography.titleMedium,
                        color = SugarDimens.Brand.deepPurple
                    )
                    Switch(
                        checked = reactiveConfig.enableTimeReactive,
                        onCheckedChange = { 
                            onConfigChange(reactiveConfig.copy(enableTimeReactive = it))
                        }
                    )
                }
                
                if (reactiveConfig.enableTimeReactive) {
                    SliderWithLabel(
                        label = "Transition Speed (${reactiveConfig.timeTransitionSpeed}x)",
                        value = reactiveConfig.timeTransitionSpeed,
                        onValueChange = { onConfigChange(reactiveConfig.copy(timeTransitionSpeed = it)) },
                        valueRange = 0.1f..5f
                    )
                }
            }
        }
        
        // Battery Triggers
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Text(
                    text = "Battery Triggers",
                    style = MaterialTheme.typography.titleMedium,
                    color = SugarDimens.Brand.mint
                )
                
                Text(
                    text = "Effects activate at: ${reactiveConfig.batteryTriggers.joinToString("%, ") { it.toString() }}%",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
                ) {
                    listOf(20, 50, 80).forEach { level ->
                        FilterChip(
                            selected = level in reactiveConfig.batteryTriggers,
                            onClick = {
                                val triggers = reactiveConfig.batteryTriggers.toMutableList()
                                if (level in triggers) {
                                    triggers.remove(level)
                                } else {
                                    triggers.add(level)
                                }
                                onConfigChange(reactiveConfig.copy(batteryTriggers = triggers))
                            },
                            label = { Text("$level%") }
                        )
                    }
                }
                
                SwitchWithLabel(
                    label = "Charging State Effects",
                    checked = reactiveConfig.chargingStateEffects,
                    onCheckedChange = { 
                        onConfigChange(reactiveConfig.copy(chargingStateEffects = it))
                    }
                )
            }
        }
    }
}

@Composable
private fun InteractiveBackgroundConfigPanel(
    interactiveConfig: InteractiveBackgroundConfig,
    onConfigChange: (InteractiveBackgroundConfig) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(SugarDimens.Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.xl)
    ) {
        // Touch Ripple
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Touch Ripple",
                        style = MaterialTheme.typography.titleMedium,
                        color = SugarDimens.Brand.hotPink
                    )
                    Switch(
                        checked = interactiveConfig.enableTouchRipple,
                        onCheckedChange = { 
                            onConfigChange(interactiveConfig.copy(enableTouchRipple = it))
                        }
                    )
                }
                
                if (interactiveConfig.enableTouchRipple) {
                    SliderWithLabel(
                        label = "Ripple Size (${interactiveConfig.touchRippleSize}px)",
                        value = interactiveConfig.touchRippleSize,
                        onValueChange = { onConfigChange(interactiveConfig.copy(touchRippleSize = it)) },
                        valueRange = 50f..200f
                    )
                    
                    SliderWithLabel(
                        label = "Ripple Intensity (${(interactiveConfig.touchRippleIntensity * 100).toInt()}%)",
                        value = interactiveConfig.touchRippleIntensity,
                        onValueChange = { onConfigChange(interactiveConfig.copy(touchRippleIntensity = it)) },
                        valueRange = 0f..1f
                    )
                }
            }
        }
        
        // Gyroscope
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Gyroscope",
                        style = MaterialTheme.typography.titleMedium,
                        color = SugarDimens.Brand.mint
                    )
                    Switch(
                        checked = interactiveConfig.enableGyroscope,
                        onCheckedChange = { 
                            onConfigChange(interactiveConfig.copy(enableGyroscope = it))
                        }
                    )
                }
                
                if (interactiveConfig.enableGyroscope) {
                    SliderWithLabel(
                        label = "Sensitivity (${(interactiveConfig.gyroscopeSensitivity * 100).toInt()}%)",
                        value = interactiveConfig.gyroscopeSensitivity,
                        onValueChange = { onConfigChange(interactiveConfig.copy(gyroscopeSensitivity = it)) },
                        valueRange = 0f..1f
                    )
                }
            }
        }
        
        // Tilt & Pinch
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(SugarDimens.Radius.lg)
        ) {
            Column(
                modifier = Modifier.padding(SugarDimens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                SwitchWithLabel(
                    label = "Tilt Responsiveness",
                    checked = interactiveConfig.enableTilt,
                    onCheckedChange = { 
                        onConfigChange(interactiveConfig.copy(enableTilt = it))
                    }
                )
                
                if (interactiveConfig.enableTilt) {
                    SliderWithLabel(
                        label = "Tilt Sensitivity",
                        value = interactiveConfig.tiltResponsiveness,
                        onValueChange = { onConfigChange(interactiveConfig.copy(tiltResponsiveness = it)) },
                        valueRange = 0f..1f
                    )
                }
                
                SwitchWithLabel(
                    label = "Pinch to Zoom",
                    checked = interactiveConfig.pinchToZoom,
                    onCheckedChange = { 
                        onConfigChange(interactiveConfig.copy(pinchToZoom = it))
                    }
                )
            }
        }
    }
}

// Helper Composables

@Composable
private fun ColorPickerRow(
    color: Color,
    onColorSelected: (Color) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color, RoundedCornerShape(8.dp))
                .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        )
        
        Text(
            text = "#${color.value.toString(16).uppercase().drop(2)}",
            style = MaterialTheme.typography.labelMedium
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // In a real implementation, this would open a color picker dialog
        Button(onClick = { /* Open color picker */ }) {
            Text("Pick Color")
        }
    }
}

@Composable
private fun ColorPresetGrid(
    presets: List<String>,
    selectedColor: String,
    onColorSelected: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
    ) {
        items(presets) { color ->
            val isSelected = color == selectedColor
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(android.graphics.Color.parseColor(color)),
                        RoundedCornerShape(8.dp)
                    )
                    .then(
                        if (isSelected) {
                            Modifier.border(3.dp, Color.White, RoundedCornerShape(8.dp))
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onColorSelected(color) }
            )
        }
    }
}

@Composable
private fun PatternGrid(
    selectedPatternId: String?,
    onPatternSelected: (String) -> Unit
) {
    // Simplified pattern grid - in real implementation would show actual patterns
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
    ) {
        items(10) { index ->
            val patternId = "pattern_$index"
            val isSelected = patternId == selectedPatternId
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        SugarDimens.Brand.hotPink.copy(alpha = 0.3f),
                        RoundedCornerShape(8.dp)
                    )
                    .then(
                        if (isSelected) {
                            Modifier.border(3.dp, SugarDimens.Brand.hotPink, RoundedCornerShape(8.dp))
                        } else {
                            Modifier
                        }
                    )
                    .clickable { onPatternSelected(patternId) }
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
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(bottom = SugarDimens.Spacing.xxs)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}

@Composable
private fun OutlinedDropdownMenu(
    label: String,
    selected: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
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
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
