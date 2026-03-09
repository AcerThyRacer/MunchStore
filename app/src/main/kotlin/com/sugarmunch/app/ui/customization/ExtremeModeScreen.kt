package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.model.IntensityLevels

/**
 * Extreme Mode Screen
 * Configure Sugar Rush limits and extreme customization options
 */

@Composable
fun ExtremeModeScreen(
    onNavigateBack: () -> Unit
) {
    var sugarRushEnabled by remember { mutableStateOf(false) }
    var themeIntensity by remember { mutableStateOf(1.5f) }
    var particleIntensity by remember { mutableStateOf(1.0f) }
    var animationIntensity by remember { mutableStateOf(1.0f) }
    var effectIntensity by remember { mutableStateOf(1.0f) }
    var nuclearOverloadMode by remember { mutableStateOf(false) }
    var maxParticles by remember { mutableStateOf(100) }
    var performanceMode by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Extreme Mode") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Warning banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFFFF69B4), Color(0xFFFFD700))
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = "Extreme Mode Warning",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "High intensity settings may increase battery usage and reduce performance on older devices.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Sugar Rush toggle
            item {
                ExtremeModeSwitch(
                    title = "Sugar Rush Mode",
                    description = "Enable maximum intensity for all visual effects",
                    checked = sugarRushEnabled,
                    onCheckedChange = { sugarRushEnabled = it }
                )
            }
            
            // Nuclear Overload toggle
            item {
                ExtremeModeSwitch(
                    title = "Nuclear Overload",
                    description = "Unleash the full power of SugarMunch Extreme (2.0x intensity)",
                    checked = nuclearOverloadMode,
                    onCheckedChange = { nuclearOverloadMode = it },
                    enabled = sugarRushEnabled
                )
            }
            
            // Theme intensity
            item {
                IntensitySlider(
                    title = "Theme Intensity",
                    currentValue = themeIntensity,
                    onValueChange = { themeIntensity = it },
                    maxValue = if (nuclearOverloadMode) 2.0f else 1.5f,
                    icon = "🎨"
                )
            }
            
            // Particle intensity
            item {
                IntensitySlider(
                    title = "Particle Intensity",
                    currentValue = particleIntensity,
                    onValueChange = { particleIntensity = it },
                    maxValue = if (nuclearOverloadMode) 2.0f else 1.5f,
                    icon = "✨"
                )
            }
            
            // Animation intensity
            item {
                IntensitySlider(
                    title = "Animation Intensity",
                    currentValue = animationIntensity,
                    onValueChange = { animationIntensity = it },
                    maxValue = if (nuclearOverloadMode) 2.0f else 1.5f,
                    icon = "🎬"
                )
            }
            
            // Effect intensity
            item {
                IntensitySlider(
                    title = "Effect Intensity",
                    currentValue = effectIntensity,
                    onValueChange = { effectIntensity = it },
                    maxValue = if (nuclearOverloadMode) 2.0f else 1.5f,
                    icon = "⚡"
                )
            }
            
            // Max particles slider
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌟",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Max Particles",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Text(
                            text = maxParticles.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Slider(
                        value = maxParticles.toFloat(),
                        onValueChange = { maxParticles = it.toInt() },
                        valueRange = 50f..500f,
                        steps = 9
                    )
                    
                    Text(
                        text = when {
                            maxParticles < 100 -> "Low (Better Performance)"
                            maxParticles < 200 -> "Medium (Balanced)"
                            maxParticles < 300 -> "High (More Visuals)"
                            else -> "Maximum (Extreme!)"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Performance mode toggle
            item {
                ExtremeModeSwitch(
                    title = "Performance Mode",
                    description = "Automatically reduce effects when battery is low",
                    checked = performanceMode,
                    onCheckedChange = { performanceMode = it }
                )
            }
            
            // Preset buttons
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Quick Presets",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    themeIntensity = IntensityLevels.CHILL
                                    particleIntensity = IntensityLevels.CHILL
                                    animationIntensity = IntensityLevels.CHILL
                                    effectIntensity = IntensityLevels.CHILL
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Chill")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    themeIntensity = IntensityLevels.NORMAL
                                    particleIntensity = IntensityLevels.NORMAL
                                    animationIntensity = IntensityLevels.NORMAL
                                    effectIntensity = IntensityLevels.NORMAL
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Normal")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    themeIntensity = IntensityLevels.SWEET
                                    particleIntensity = IntensityLevels.SWEET
                                    animationIntensity = IntensityLevels.SWEET
                                    effectIntensity = IntensityLevels.SWEET
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Sweet")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    themeIntensity = IntensityLevels.SUGARRUSH
                                    particleIntensity = IntensityLevels.SUGARRUSH
                                    animationIntensity = IntensityLevels.SUGARRUSH
                                    effectIntensity = IntensityLevels.SUGARRUSH
                                    sugarRushEnabled = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Sugar Rush")
                            }
                            
                            Button(
                                onClick = {
                                    themeIntensity = IntensityLevels.MAXIMUM
                                    particleIntensity = IntensityLevels.MAXIMUM
                                    animationIntensity = IntensityLevels.MAXIMUM
                                    effectIntensity = IntensityLevels.MAXIMUM
                                    sugarRushEnabled = true
                                    nuclearOverloadMode = true
                                },
                                enabled = sugarRushEnabled,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF1493)
                                )
                            ) {
                                Text("MAX")
                            }
                        }
                    }
                }
            }
            
            // Reset button
            item {
                OutlinedButton(
                    onClick = {
                        sugarRushEnabled = false
                        nuclearOverloadMode = false
                        themeIntensity = 1.0f
                        particleIntensity = 1.0f
                        animationIntensity = 1.0f
                        effectIntensity = 1.0f
                        maxParticles = 100
                        performanceMode = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reset to Defaults")
                }
            }
        }
    }
}

@Composable
private fun ExtremeModeSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        colors = CardDefaults.cardColors(
            containerColor = if (!enabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    }
                )
            }
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled
            )
        }
    }
}

@Composable
private fun IntensitySlider(
    title: String,
    currentValue: Float,
    onValueChange: (Float) -> Unit,
    maxValue: Float = 2.0f,
    icon: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Text(
                text = "${currentValue}x",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = currentValue,
            onValueChange = onValueChange,
            valueRange = 0f..maxValue,
            steps = (maxValue * 10).toInt() - 1
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "0x",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${maxValue}x",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
