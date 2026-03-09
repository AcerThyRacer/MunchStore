package com.sugarmunch.app.ui.shaders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ── Data models ─────────────────────────────────────────────────────────────────

data class ShaderLayer(
    val config: ShaderConfig,
    val opacity: Float = 1f,
    val blendMode: BlendMode = BlendMode.SrcOver
)

data class ShaderMix(
    val layers: List<ShaderLayer> = emptyList()
) {
    fun addLayer(config: ShaderConfig, opacity: Float = 1f): ShaderMix =
        copy(layers = layers + ShaderLayer(config, opacity))

    fun removeLayer(index: Int): ShaderMix =
        copy(layers = layers.filterIndexed { i, _ -> i != index })

    fun updateLayer(index: Int, config: ShaderConfig): ShaderMix =
        copy(layers = layers.mapIndexed { i, layer ->
            if (i == index) layer.copy(config = config) else layer
        })

    fun setOpacity(index: Int, opacity: Float): ShaderMix =
        copy(layers = layers.mapIndexed { i, layer ->
            if (i == index) layer.copy(opacity = opacity.coerceIn(0f, 1f)) else layer
        })
}

// ── Composable overlay ──────────────────────────────────────────────────────────

@Composable
fun MixedShaderOverlay(
    mix: ShaderMix,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        for (layer in mix.layers) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = layer.opacity
                    }
            ) {
                ShaderEffectOverlay(
                    config = layer.config,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ── Presets ──────────────────────────────────────────────────────────────────────

object ShaderMixPresets {

    val RetroVibes = ShaderMix(
        layers = listOf(
            ShaderLayer(
                config = ShaderConfig(effect = ShaderEffect.CRT_SCANLINES, intensity = 1f),
                opacity = 0.9f
            ),
            ShaderLayer(
                config = ShaderConfig(effect = ShaderEffect.CHROMATIC_ABERRATION, intensity = 0.4f),
                opacity = 0.5f
            )
        )
    )

    val Cyberpunk = ShaderMix(
        layers = listOf(
            ShaderLayer(
                config = ShaderConfig(effect = ShaderEffect.MATRIX_RAIN, intensity = 0.8f, speed = 1.2f),
                opacity = 0.7f
            ),
            ShaderLayer(
                config = ShaderConfig(effect = ShaderEffect.HOLOGRAPHIC_SWEEP, intensity = 0.9f),
                opacity = 0.6f
            ),
            ShaderLayer(
                config = ShaderConfig(effect = ShaderEffect.GLITCH, intensity = 0.3f),
                opacity = 0.4f
            )
        )
    )

    val Dreamy = ShaderMix(
        layers = listOf(
            ShaderLayer(
                config = ShaderConfig(
                    effect = ShaderEffect.FROSTED_GLASS,
                    intensity = 0.7f,
                    color = Color(0xFFE1BEE7)
                ),
                opacity = 0.8f
            ),
            ShaderLayer(
                config = ShaderConfig(effect = ShaderEffect.WATER_RIPPLE, intensity = 0.3f, speed = 0.5f),
                opacity = 0.4f
            )
        )
    )

    val Vaporwave = ShaderMix(
        layers = listOf(
            ShaderLayer(
                config = ShaderConfig(effect = ShaderEffect.HOLOGRAPHIC_SWEEP, intensity = 1.2f),
                opacity = 0.7f
            ),
            ShaderLayer(
                config = ShaderConfig(
                    effect = ShaderEffect.CRT_SCANLINES,
                    intensity = 0.6f,
                    color = Color(0xFFFF4081)
                ),
                opacity = 0.5f
            ),
            ShaderLayer(
                config = ShaderConfig(effect = ShaderEffect.CHROMATIC_ABERRATION, intensity = 0.6f),
                opacity = 0.5f
            )
        )
    )

    val Underwater = ShaderMix(
        layers = listOf(
            ShaderLayer(
                config = ShaderConfig(
                    effect = ShaderEffect.WATER_RIPPLE,
                    intensity = 0.8f,
                    speed = 0.7f,
                    color = Color(0xFF0288D1)
                ),
                opacity = 0.7f
            ),
            ShaderLayer(
                config = ShaderConfig(
                    effect = ShaderEffect.HEAT_HAZE,
                    intensity = 0.5f,
                    speed = 0.4f,
                    color = Color(0xFF4FC3F7)
                ),
                opacity = 0.6f
            ),
            ShaderLayer(
                config = ShaderConfig(
                    effect = ShaderEffect.FROSTED_GLASS,
                    intensity = 0.3f,
                    color = Color(0xFFB3E5FC)
                ),
                opacity = 0.35f
            )
        )
    )

    val all: List<Pair<String, ShaderMix>> = listOf(
        "Retro Vibes" to RetroVibes,
        "Cyberpunk" to Cyberpunk,
        "Dreamy" to Dreamy,
        "Vaporwave" to Vaporwave,
        "Underwater" to Underwater
    )
}

// ── Mixer settings panel ────────────────────────────────────────────────────────

private fun ShaderEffect.displayName(): String = when (this) {
    ShaderEffect.WATER_RIPPLE -> "Water Ripple"
    ShaderEffect.CHROMATIC_ABERRATION -> "Chromatic Aberration"
    ShaderEffect.GLITCH -> "Glitch"
    ShaderEffect.HEAT_HAZE -> "Heat Haze"
    ShaderEffect.FROSTED_GLASS -> "Frosted Glass"
    ShaderEffect.HOLOGRAPHIC_SWEEP -> "Holographic Sweep"
    ShaderEffect.CRT_SCANLINES -> "CRT Scanlines"
    ShaderEffect.MATRIX_RAIN -> "Matrix Rain"
    ShaderEffect.LIQUID_CHROME -> "Liquid Chrome"
}

@Composable
fun ShaderMixerPanel(
    mix: ShaderMix,
    onMixChanged: (ShaderMix) -> Unit,
    modifier: Modifier = Modifier
) {
    var showEffectDropdown by remember { mutableStateOf(false) }

    Column(modifier = modifier.padding(12.dp)) {
        // Preset chips
        Text(
            text = "Presets",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            for ((name, preset) in ShaderMixPresets.all) {
                FilterChip(
                    selected = false,
                    onClick = { onMixChanged(preset) },
                    label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Layer list
        Text(
            text = "Layers (${mix.layers.size})",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            itemsIndexed(mix.layers) { index, layer ->
                ShaderLayerCard(
                    index = index,
                    layer = layer,
                    onIntensityChanged = { intensity ->
                        onMixChanged(
                            mix.updateLayer(index, layer.config.copy(intensity = intensity))
                        )
                    },
                    onOpacityChanged = { opacity ->
                        onMixChanged(mix.setOpacity(index, opacity))
                    },
                    onRemove = { onMixChanged(mix.removeLayer(index)) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Add layer button
        Box {
            FilledTonalButton(
                onClick = { showEffectDropdown = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Layer")
            }

            DropdownMenu(
                expanded = showEffectDropdown,
                onDismissRequest = { showEffectDropdown = false }
            ) {
                ShaderEffect.entries.forEach { effect ->
                    DropdownMenuItem(
                        text = { Text(effect.displayName()) },
                        onClick = {
                            onMixChanged(mix.addLayer(ShaderConfig(effect = effect)))
                            showEffectDropdown = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ShaderLayerCard(
    index: Int,
    layer: ShaderLayer,
    onIntensityChanged: (Float) -> Unit,
    onOpacityChanged: (Float) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${index + 1}. ${layer.config.effect.displayName()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Remove layer",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Intensity slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Intensity",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(64.dp)
                )
                Slider(
                    value = layer.config.intensity,
                    onValueChange = onIntensityChanged,
                    valueRange = 0f..2f,
                    modifier = Modifier.weight(1f)
                )
            }

            // Opacity slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Opacity",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.width(64.dp)
                )
                Slider(
                    value = layer.opacity,
                    onValueChange = onOpacityChanged,
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
