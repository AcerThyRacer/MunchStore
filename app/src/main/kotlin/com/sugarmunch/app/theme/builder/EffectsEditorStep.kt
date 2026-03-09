package com.sugarmunch.app.theme.builder

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Step 3: Effects Editor - Allows users to configure particle effects and animations.
 *
 * Provides toggles for particle effects, animated backgrounds, and blur effects,
 * plus controls for particle type, density, and speed.
 *
 * @param state Current theme builder state
 * @param onStateChange Callback to update the state
 */
@Composable
fun EffectsEditorStep(
    state: ThemeBuilderState,
    onStateChange: (ThemeBuilderState) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Particle effects toggle
        item {
            EffectToggleCard(
                title = "Particle Effects",
                description = "Add animated particles to your theme",
                enabled = state.enableParticles,
                onEnabledChange = { onStateChange(state.copy(enableParticles = it)) }
            )
        }

        if (state.enableParticles) {
            // Particle type selector
            item {
                ParticleTypeSelector(
                    selectedType = state.particleType,
                    onTypeSelected = { onStateChange(state.copy(particleType = it)) }
                )
            }

            // Particle density slider
            item {
                ParticleDensitySlider(
                    density = state.particleDensity,
                    onDensityChange = { onStateChange(state.copy(particleDensity = it)) }
                )
            }

            // Particle speed slider
            item {
                ParticleSpeedSlider(
                    speed = state.particleSpeed,
                    onSpeedChange = { onStateChange(state.copy(particleSpeed = it)) }
                )
            }
        }

        // Animation effects toggle
        item {
            EffectToggleCard(
                title = "Animated Background",
                description = "Add subtle background animations",
                enabled = state.enableAnimation,
                onEnabledChange = { onStateChange(state.copy(enableAnimation = it)) }
            )
        }

        // Blur effect toggle
        item {
            EffectToggleCard(
                title = "Blur Effect",
                description = "Add blur to background elements",
                enabled = state.enableBlur,
                onEnabledChange = { onStateChange(state.copy(enableBlur = it)) }
            )
        }
    }
}

/**
 * A card with a toggle switch for enabling/disabling an effect.
 *
 * @param title The title of the effect
 * @param description Description of what the effect does
 * @param enabled Whether the effect is currently enabled
 * @param onEnabledChange Callback when the toggle changes
 */
@Composable
private fun EffectToggleCard(
    title: String,
    description: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
    }
}

/**
 * Selector for particle type (circles, squares, stars, etc.).
 *
 * @param selectedType Currently selected particle type
 * @param onTypeSelected Callback when a type is selected
 */
@Composable
private fun ParticleTypeSelector(
    selectedType: ParticleType,
    onTypeSelected: (ParticleType) -> Unit
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
            Text(
                text = "Particle Type",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(ParticleType.values()) { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { onTypeSelected(type) },
                        label = { Text(type.displayName) }
                    )
                }
            }
        }
    }
}

/**
 * Slider for controlling particle density.
 *
 * @param density Current particle density (0-100)
 * @param onDensityChange Callback when density changes
 */
@Composable
private fun ParticleDensitySlider(
    density: Int,
    onDensityChange: (Int) -> Unit
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
                    text = "Particle Density",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "$density particles",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = density.toFloat(),
                onValueChange = { onDensityChange(it.toInt()) },
                valueRange = 0f..100f,
                steps = 99
            )
        }
    }
}

/**
 * Slider for controlling particle animation speed.
 *
 * @param speed Current particle speed (0.0-3.0)
 * @param onSpeedChange Callback when speed changes
 */
@Composable
private fun ParticleSpeedSlider(
    speed: Float,
    onSpeedChange: (Float) -> Unit
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
                    text = "Particle Speed",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "%.1fx".format(speed),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Slider(
                value = speed,
                onSpeedChange = onSpeedChange,
                valueRange = 0f..3f,
                steps = 29
            )
        }
    }
}
