package com.sugarmunch.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.theme.CandyPink
import com.sugarmunch.app.ui.theme.CottonCandyBlue
import com.sugarmunch.app.ui.theme.CandyMint
import com.sugarmunch.app.ui.theme.CandyPurple
import com.sugarmunch.app.ui.theme.CandyYellow

data class CustomEffectConfig(
    val name: String = "My Custom Effect",
    val primaryColor: Color = CandyPink,
    val secondaryColor: Color = CottonCandyBlue,
    val opacity: Float = 0.5f,
    val intensity: Float = 0.7f,
    val hasVisual: Boolean = true,
    val hasHaptic: Boolean = false,
    val hapticIntensity: Float = 0.5f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomEffectBuilderScreen(
    onBack: () -> Unit,
    onSave: (CustomEffectConfig) -> Unit = {}
) {
    var effectName by remember { mutableStateOf("My Candy Effect") }
    var selectedPrimaryColor by remember { mutableStateOf(CandyPink) }
    var selectedSecondaryColor by remember { mutableStateOf(CottonCandyBlue) }
    var opacity by remember { mutableFloatStateOf(0.5f) }
    var intensity by remember { mutableFloatStateOf(0.7f) }
    var hasHaptic by remember { mutableStateOf(false) }
    var hapticIntensity by remember { mutableFloatStateOf(0.5f) }
    
    val colorOptions = listOf(
        CandyPink to "Pink",
        CottonCandyBlue to "Blue",
        CandyMint to "Mint",
        CandyPurple to "Purple",
        CandyYellow to "Yellow",
        Color(0xFFFF6B6B) to "Coral"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build Custom Effect") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        onSave(
                            CustomEffectConfig(
                                name = effectName,
                                primaryColor = selectedPrimaryColor,
                                secondaryColor = selectedSecondaryColor,
                                opacity = opacity,
                                intensity = intensity,
                                hasHaptic = hasHaptic,
                                hapticIntensity = hapticIntensity
                            )
                        )
                    }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            selectedPrimaryColor.copy(alpha = 0.15f),
                            selectedSecondaryColor.copy(alpha = 0.1f)
                        )
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Preview",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Effect preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        selectedPrimaryColor.copy(alpha = opacity),
                                        selectedSecondaryColor.copy(alpha = opacity * 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "\uD83C\uDF6C",
                            style = MaterialTheme.typography.displayLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* Preview effect */ },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = selectedPrimaryColor
                            )
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview")
                        }
                    }
                }
            }
            
            // Effect Name
            OutlinedTextField(
                value = effectName,
                onValueChange = { effectName = it },
                label = { Text("Effect Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            // Primary Color Selection
            ColorSelectionCard(
                title = "Primary Color",
                selectedColor = selectedPrimaryColor,
                colorOptions = colorOptions,
                onColorSelected = { selectedPrimaryColor = it }
            )
            
            // Secondary Color Selection
            ColorSelectionCard(
                title = "Secondary Color",
                selectedColor = selectedSecondaryColor,
                colorOptions = colorOptions,
                onColorSelected = { selectedSecondaryColor = it }
            )
            
            // Opacity Slider
            SliderCard(
                title = "Opacity",
                value = opacity,
                onValueChange = { opacity = it },
                valueRange = 0.1f..1f,
                color = selectedPrimaryColor
            )
            
            // Intensity Slider
            SliderCard(
                title = "Intensity",
                value = intensity,
                onValueChange = { intensity = it },
                valueRange = 0.1f..1f,
                color = selectedSecondaryColor
            )
            
            // Haptic Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Haptic Feedback",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Add vibration to your effect",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = hasHaptic,
                        onCheckedChange = { hasHaptic = it },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
                            checkedThumbColor = selectedPrimaryColor
                        )
                    )
                }
            }
            
            // Haptic Intensity (if enabled)
            if (hasHaptic) {
                SliderCard(
                    title = "Haptic Intensity",
                    value = hapticIntensity,
                    onValueChange = { hapticIntensity = it },
                    valueRange = 0.1f..1f,
                    color = Color(0xFFFF6B6B)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ColorSelectionCard(
    title: String,
    selectedColor: Color,
    colorOptions: List<Pair<Color, String>>,
    onColorSelected: (Color) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colorOptions.forEach { (color, name) ->
                    val isSelected = color == selectedColor
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.2f else 1f,
                        animationSpec = tween(200),
                        label = "colorScale"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) {
                                        Modifier.background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.3f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            name,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SliderCard(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
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
                    title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = color
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                colors = SliderDefaults.colors(
                    thumbColor = color,
                    activeTrackColor = color,
                    inactiveTrackColor = color.copy(alpha = 0.3f)
                )
            )
        }
    }
}