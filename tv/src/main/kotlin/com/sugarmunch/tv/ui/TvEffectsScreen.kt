package com.sugarmunch.tv.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Button
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.OutlinedButton
import androidx.tv.material3.Text
import com.sugarmunch.app.effects.v2.model.EffectV2
import com.sugarmunch.tv.presentation.TvMainViewModel

/**
 * TV Effects Screen
 * Effect control interface for TV with:
 * - Toggle effects on/off
 * - Preset buttons (Focus, Celebration, Chill, Maximum)
 * - Visual preview
 * - D-pad friendly grid layout
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvEffectsScreen(
    viewModel: TvMainViewModel,
    modifier: Modifier = Modifier
) {
    val effectsState by viewModel.effectsState.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Effects",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Customize your SugarMunch experience with visual effects",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Presets Section
        item {
            PresetsSection(
                onPresetSelected = { preset ->
                    viewModel.applyEffectPreset(preset)
                }
            )
        }

        // Active Effects Summary
        item {
            ActiveEffectsSection(
                activeCount = effectsState.activeEffects.size,
                onClearAll = { viewModel.applyEffectPreset("none") }
            )
        }

        // Effects Grid
        item {
            EffectsGridSection(
                effects = effectsState.availableEffects,
                activeEffects = effectsState.activeEffects,
                onEffectToggle = { effectId ->
                    viewModel.toggleEffect(effectId)
                }
            )
        }

        // Preview Section
        item {
            EffectPreviewSection()
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PresetsSection(onPresetSelected: (String) -> Unit) {
    Column {
        Text(
            text = "Quick Presets",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PresetButton(
                label = "None",
                description = "Turn off all effects",
                icon = Icons.Default.Clear,
                onClick = { onPresetSelected("none") }
            )

            PresetButton(
                label = "Focus",
                description = "Minimal glow for concentration",
                icon = Icons.Default.Check,
                onClick = { onPresetSelected("focus") }
            )

            PresetButton(
                label = "Celebration",
                description = "Confetti and sparkles",
                icon = Icons.Default.Star,
                onClick = { onPresetSelected("celebration") }
            )

            PresetButton(
                label = "Chill",
                description = "Gentle calming effects",
                icon = Icons.Default.MoreVert,
                onClick = { onPresetSelected("chill") }
            )

            PresetButton(
                label = "Maximum",
                description = "All effects enabled",
                icon = Icons.Default.Star,
                onClick = { onPresetSelected("maximum") }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PresetButton(
    label: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1f)
    val elevation by animateDpAsState(if (isFocused) 8.dp else 2.dp)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(180.dp)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused },
        border = CardDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = if (isFocused) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isFocused) MaterialTheme.colorScheme.primary 
                       else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ActiveEffectsSection(
    activeCount: Int,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Active Effects",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(
                modifier = Modifier
                    .background(
                        if (activeCount > 0) MaterialTheme.colorScheme.primary 
                        else MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = activeCount.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (activeCount > 0) MaterialTheme.colorScheme.onPrimary 
                           else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (activeCount > 0) {
            OutlinedButton(onClick = onClearAll) {
                Icon(Icons.Default.Clear, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Clear All")
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EffectsGridSection(
    effects: List<EffectV2>,
    activeEffects: List<String>,
    onEffectToggle: (String) -> Unit
) {
    Column {
        Text(
            text = "All Effects",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.height(400.dp)
        ) {
            items(effects) { effect ->
                val isActive = activeEffects.contains(effect.id)
                EffectCard(
                    effect = effect,
                    isActive = isActive,
                    onToggle = { onEffectToggle(effect.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EffectCard(
    effect: EffectV2,
    isActive: Boolean,
    onToggle: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.05f else 1f)

    Card(
        onClick = onToggle,
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .onFocusChanged { isFocused = it.isFocused },
        colors = CardDefaults.colors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Active indicator
            if (isActive) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(24.dp)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Effect Icon/Preview
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = effect.icon ?: Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = if (isActive) MaterialTheme.colorScheme.onPrimary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = effect.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = effect.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun EffectPreviewSection() {
    Column {
        Text(
            text = "Preview",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            onClick = { },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Effect Preview Area",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Effects will be visible here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

// Extension to get icon from effect - this would need to be in the actual EffectV2 model
private val EffectV2.icon: ImageVector?
    get() = when (category.name.lowercase()) {
        "visual" -> Icons.Default.Star
        "animation" -> Icons.Default.MoreVert
        "haptic" -> Icons.Default.Check
        "audio" -> Icons.Default.Star
        else -> Icons.Default.Star
    }
