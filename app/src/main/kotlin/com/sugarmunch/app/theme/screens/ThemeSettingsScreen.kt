package com.sugarmunch.app.theme.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.theme.components.AnimatedThemeBackground
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.*
import com.sugarmunch.app.ui.components.Haptics
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    appId: String? = null,
    onBack: () -> Unit
) {
    PhaseOneThemeStudioScreen(
        appId = appId,
        onBack = onBack
    )
}

@Composable
private fun CurrentThemeCard(
    theme: CandyTheme,
    colors: AdjustedColors,
    intensity: Float,
    onIntensityClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                colors.primary.copy(alpha = 0.3f * intensity),
                                colors.secondary.copy(alpha = 0.2f * intensity),
                                colors.tertiary.copy(alpha = 0.1f * intensity)
                            )
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = theme.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = colors.onSurface
                        )
                        Text(
                            text = theme.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    
                    // Category badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.primary.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = theme.category.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.primary
                        )
                    }
                }
                
                // Intensity indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Intensity: ${(intensity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = colors.onSurface
                        )
                        LinearProgressIndicator(
                            progress = { intensity / 2f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = colors.primary,
                            trackColor = colors.surfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        onClick = onIntensityClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adjust")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickIntensityPresets(
    currentIntensity: Float,
    onPresetSelected: (ThemeManager.IntensityPreset) -> Unit
) {
    Column {
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
            IntensityPresetChip(
                label = "🧊 Chill",
                intensity = IntensityLevels.CHILL,
                isSelected = currentIntensity == IntensityLevels.CHILL,
                onClick = { onPresetSelected(ThemeManager.IntensityPreset.CHILL) },
                modifier = Modifier.weight(1f)
            )
            IntensityPresetChip(
                label = "🍬 Normal",
                intensity = IntensityLevels.NORMAL,
                isSelected = currentIntensity == IntensityLevels.NORMAL,
                onClick = { onPresetSelected(ThemeManager.IntensityPreset.NORMAL) },
                modifier = Modifier.weight(1f)
            )
            IntensityPresetChip(
                label = "🚀 Rush",
                intensity = IntensityLevels.SUGARRUSH,
                isSelected = currentIntensity == IntensityLevels.SUGARRUSH,
                onClick = { onPresetSelected(ThemeManager.IntensityPreset.SUGARRUSH) },
                modifier = Modifier.weight(1f)
            )
            IntensityPresetChip(
                label = "🔥 MAX",
                intensity = IntensityLevels.MAXIMUM,
                isSelected = currentIntensity == IntensityLevels.MAXIMUM,
                onClick = { onPresetSelected(ThemeManager.IntensityPreset.MAXIMUM) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun IntensityPresetChip(
    label: String,
    intensity: Float,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryFilterChips(
    selectedCategory: ThemeCategory?,
    onCategorySelected: (ThemeCategory?) -> Unit,
    categories: List<ThemeCategory>
) {
    Column {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" chip
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { Text("All") },
                leadingIcon = if (selectedCategory == null) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null
            )
            
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category.name.replace("_", " ")) },
                    leadingIcon = if (selectedCategory == category) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun ThemeCard(
    theme: CandyTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = theme.getColorsForIntensity(1f)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = colors.primary,
                        shape = RoundedCornerShape(16.dp)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Color preview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ColorPreviewDot(colors.primary)
                ColorPreviewDot(colors.secondary)
                ColorPreviewDot(colors.tertiary)
                ColorPreviewDot(colors.accent)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = theme.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = colors.onSurface
            )
            
            Text(
                text = theme.description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurface.copy(alpha = 0.6f),
                maxLines = 2
            )
            
            if (isSelected) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Active",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPreviewDot(color: Color) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .shadow(2.dp, CircleShape)
            .background(color, CircleShape)
    )
}

@Composable
private fun IntensityControlPanel(
    themeIntensity: Float,
    bgIntensity: Float,
    particleIntensity: Float,
    animationIntensity: Float,
    onThemeIntensityChange: (Float) -> Unit,
    onBgIntensityChange: (Float) -> Unit,
    onParticleIntensityChange: (Float) -> Unit,
    onAnimationIntensityChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    colors: AdjustedColors
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface.copy(alpha = 0.98f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Handle bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            colors.onSurface.copy(alpha = 0.3f),
                            RoundedCornerShape(2.dp)
                        )
                        .clickable { onDismiss() }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SugarRush Intensity",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Fine-tune your candy experience",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurface.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Theme Colors Intensity
            IntensitySlider(
                label = "🎨 Color Intensity",
                value = themeIntensity,
                onValueChange = onThemeIntensityChange,
                description = "Saturation and brightness of theme colors",
                colors = colors
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Background Intensity
            IntensitySlider(
                label = "🌈 Background",
                value = bgIntensity,
                onValueChange = onBgIntensityChange,
                description = "Gradient intensity and animation speed",
                colors = colors
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Particle Intensity
            IntensitySlider(
                label = "✨ Particles",
                value = particleIntensity,
                onValueChange = onParticleIntensityChange,
                description = "Number and speed of background particles",
                colors = colors
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Animation Intensity
            IntensitySlider(
                label = "🎬 Animations",
                value = animationIntensity,
                onValueChange = onAnimationIntensityChange,
                description = "Animation speed and effects strength",
                colors = colors
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Master control
            Button(
                onClick = {
                    val masterValue = 1f
                    onThemeIntensityChange(masterValue)
                    onBgIntensityChange(masterValue)
                    onParticleIntensityChange(masterValue)
                    onAnimationIntensityChange(masterValue)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Reset All to Default")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedButton(
                onClick = {
                    onThemeIntensityChange(2f)
                    onBgIntensityChange(2f)
                    onParticleIntensityChange(2f)
                    onAnimationIntensityChange(2f)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = colors.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Bolt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("MAXIMUM OVERDRIVE 🔥")
            }
        }
    }
}

@Composable
private fun IntensitySlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    description: String,
    colors: AdjustedColors
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelLarge,
                color = colors.primary,
                fontWeight = FontWeight.Bold
            )
        }
        
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = colors.onSurface.copy(alpha = 0.6f)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..2f,
            steps = 19,
            colors = SliderDefaults.colors(
                thumbColor = colors.primary,
                activeTrackColor = colors.primary,
                inactiveTrackColor = colors.surfaceVariant
            ),
            thumb = {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shadow(4.dp, CircleShape)
                        .background(colors.primary, CircleShape)
                        .border(2.dp, colors.surface, CircleShape)
                )
            }
        )
        
        // Intensity level indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("🧊", fontSize = 12.sp)
            Text("🍬", fontSize = 12.sp)
            Text("🍭", fontSize = 12.sp)
            Text("🚀", fontSize = 12.sp)
            Text("🔥", fontSize = 12.sp)
        }
    }
}

