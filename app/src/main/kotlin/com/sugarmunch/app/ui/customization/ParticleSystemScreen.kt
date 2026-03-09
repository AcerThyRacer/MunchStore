package com.sugarmunch.app.ui.customization

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

/**
 * EXTREME Particle System Deep Control Screen
 * Particle physics, appearance, and behavior controls
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParticleSystemScreen(
    onNavigateBack: () -> Unit,
    particleConfig: ParticleConfig,
    onParticleConfigChange: (ParticleConfig) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Particle System") },
                onNavigateBack = onNavigateBack
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.lg)
        ) {
            // Particle Physics
            item {
                ParticlePhysicsSection(
                    particleConfig = particleConfig,
                    onParticleConfigChange = onParticleConfigChange
                )
            }

            // Particle Appearance
            item {
                ParticleAppearanceSection(
                    particleConfig = particleConfig,
                    onParticleConfigChange = onParticleConfigChange
                )
            }

            // Particle Behavior
            item {
                ParticleBehaviorSection(
                    particleConfig = particleConfig,
                    onParticleConfigChange = onParticleConfigChange
                )
            }

            // Lifetime Curves
            item {
                LifetimeCurvesSection(
                    particleConfig = particleConfig,
                    onParticleConfigChange = onParticleConfigChange
                )
            }
        }
    }
}

@Composable
private fun ParticlePhysicsSection(
    particleConfig: ParticleConfig,
    onParticleConfigChange: (ParticleConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Particle Physics",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.hotPink
            )

            // Gravity
            SliderWithLabel(
                label = "Gravity (${particleConfig.gravity}G)",
                value = particleConfig.gravity,
                onValueChange = { onParticleConfigChange(particleConfig.copy(gravity = it)) },
                valueRange = 0f..2f,
                steps = 19
            )

            // Gravity Direction
            SliderWithLabel(
                label = "Gravity Direction (${particleConfig.gravityDirection}°)",
                value = particleConfig.gravityDirection,
                onValueChange = { onParticleConfigChange(particleConfig.copy(gravityDirection = it)) },
                valueRange = 0f..360f,
                steps = 359
            )

            // Wind
            SliderWithLabel(
                label = "Wind (${(particleConfig.wind * 100).toInt()}%)",
                value = particleConfig.wind,
                onValueChange = { onParticleConfigChange(particleConfig.copy(wind = it)) },
                valueRange = 0f..1f,
                steps = 19
            )

            // Friction
            SliderWithLabel(
                label = "Friction (${(particleConfig.friction * 100).toInt()}%)",
                value = particleConfig.friction,
                onValueChange = { onParticleConfigChange(particleConfig.copy(friction = it)) },
                valueRange = 0f..1f,
                steps = 19
            )

            // Bounce
            SliderWithLabel(
                label = "Bounce (${(particleConfig.bounce * 100).toInt()}%)",
                value = particleConfig.bounce,
                onValueChange = { onParticleConfigChange(particleConfig.copy(bounce = it)) },
                valueRange = 0f..1f,
                steps = 19
            )

            // Collision Detection
            SwitchWithLabel(
                label = "Collision Detection",
                checked = particleConfig.collisionDetection,
                onCheckedChange = {
                    onParticleConfigChange(particleConfig.copy(collisionDetection = it))
                }
            )

            // Particle Lifetime
            SliderWithLabel(
                label = "Particle Lifetime (${particleConfig.particleLifetime}s)",
                value = particleConfig.particleLifetime,
                onValueChange = { onParticleConfigChange(particleConfig.copy(particleLifetime = it)) },
                valueRange = 1f..60f,
                steps = 58
            )

            // Spawn Rate
            SliderWithLabel(
                label = "Spawn Rate (${particleConfig.spawnRate}/sec)",
                value = particleConfig.spawnRate.toFloat(),
                onValueChange = {
                    onParticleConfigChange(particleConfig.copy(spawnRate = it.toInt()))
                },
                valueRange = 1f..1000f,
                steps = 998
            )
        }
    }
}

@Composable
private fun ParticleAppearanceSection(
    particleConfig: ParticleConfig,
    onParticleConfigChange: (ParticleConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Particle Appearance",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.mint
            )

            // Size Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Min Size", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = particleConfig.sizeMin,
                        onValueChange = { onParticleConfigChange(particleConfig.copy(sizeMin = it)) },
                        valueRange = 1f..20f,
                        steps = 18
                    )
                    Text("${particleConfig.sizeMin}px", style = MaterialTheme.typography.bodySmall)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Max Size", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = particleConfig.sizeMax,
                        onValueChange = { onParticleConfigChange(particleConfig.copy(sizeMax = it)) },
                        valueRange = 5f..50f,
                        steps = 44
                    )
                    Text("${particleConfig.sizeMax}px", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Color Start
            ColorSelectorRow(
                label = "Start Color",
                colorHex = particleConfig.colorStart,
                onColorSelected = { onParticleConfigChange(particleConfig.copy(colorStart = it)) }
            )

            // Color End
            ColorSelectorRow(
                label = "End Color",
                colorHex = particleConfig.colorEnd,
                onColorSelected = { onParticleConfigChange(particleConfig.copy(colorEnd = it)) }
            )

            // Spin Speed Range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Min Spin Speed", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = particleConfig.spinSpeedMin,
                        onValueChange = { onParticleConfigChange(particleConfig.copy(spinSpeedMin = it)) },
                        valueRange = -100f..0f,
                        steps = 19
                    )
                    Text("${particleConfig.spinSpeedMin}°/s", style = MaterialTheme.typography.bodySmall)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Max Spin Speed", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = particleConfig.spinSpeedMax,
                        onValueChange = { onParticleConfigChange(particleConfig.copy(spinSpeedMax = it)) },
                        valueRange = 0f..100f,
                        steps = 19
                    )
                    Text("${particleConfig.spinSpeedMax}°/s", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun ParticleBehaviorSection(
    particleConfig: ParticleConfig,
    onParticleConfigChange: (ParticleConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Particle Behavior",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.candyOrange
            )

            // Spawn Position
            Text("Spawn Position", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
            ) {
                SpawnPosition.entries.forEach { pos ->
                    FilterChip(
                        selected = particleConfig.spawnPosition == pos,
                        onClick = {
                            onParticleConfigChange(particleConfig.copy(spawnPosition = pos))
                        },
                        label = { Text(pos.name) }
                    )
                }
            }

            // Initial Velocity
            SliderWithLabel(
                label = "Initial Velocity (${particleConfig.initialVelocity})",
                value = particleConfig.initialVelocity,
                onValueChange = { onParticleConfigChange(particleConfig.copy(initialVelocity = it)) },
                valueRange = 0f..200f,
                steps = 39
            )

            // Spawn Spread
            SliderWithLabel(
                label = "Spawn Spread (${particleConfig.spawnSpread}°)",
                value = particleConfig.spawnSpread,
                onValueChange = { onParticleConfigChange(particleConfig.copy(spawnSpread = it)) },
                valueRange = 0f..360f,
                steps = 359
            )

            // Follow Touch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Follow Touch",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (particleConfig.followTouch) {
                        Text(
                            text = "Strength: ${(particleConfig.followTouchStrength * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Switch(
                    checked = particleConfig.followTouch,
                    onCheckedChange = {
                        onParticleConfigChange(particleConfig.copy(followTouch = it))
                    }
                )
            }

            if (particleConfig.followTouch) {
                SliderWithLabel(
                    label = "Follow Strength",
                    value = particleConfig.followTouchStrength,
                    onValueChange = {
                        onParticleConfigChange(particleConfig.copy(followTouchStrength = it))
                    },
                    valueRange = 0f..1f,
                    steps = 19
                )
            }

            // Follow Music
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Follow Music",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (particleConfig.followMusic) {
                        Text(
                            text = "Sensitivity: ${(particleConfig.followMusicSensitivity * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Switch(
                    checked = particleConfig.followMusic,
                    onCheckedChange = {
                        onParticleConfigChange(particleConfig.copy(followMusic = it))
                    }
                )
            }

            // Attract to Points
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Attract to Points",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Switch(
                    checked = particleConfig.attractToPoints,
                    onCheckedChange = {
                        onParticleConfigChange(particleConfig.copy(attractToPoints = it))
                    }
                )
            }
        }
    }
}

@Composable
private fun LifetimeCurvesSection(
    particleConfig: ParticleConfig,
    onParticleConfigChange: (ParticleConfig) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SugarDimens.Radius.lg)
    ) {
        Column(
            modifier = Modifier.padding(SugarDimens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
        ) {
            Text(
                text = "Lifetime Curves",
                style = MaterialTheme.typography.titleMedium,
                color = SugarDimens.Brand.bubblegumBlue
            )

            Text(
                text = "Configure how particles change over their lifetime",
                style = MaterialTheme.typography.bodySmall
            )

            // Opacity over lifetime
            Text("Opacity Over Lifetime", style = MaterialTheme.typography.labelLarge)
            CurveEditor(
                values = particleConfig.opacityOverLifetime,
                onValuesChange = { onParticleConfigChange(particleConfig.copy(opacityOverLifetime = it)) }
            )

            // Size over lifetime
            Text("Size Over Lifetime", style = MaterialTheme.typography.labelLarge)
            CurveEditor(
                values = particleConfig.sizeOverLifetime,
                onValuesChange = { onParticleConfigChange(particleConfig.copy(sizeOverLifetime = it)) }
            )

            // Rotation over lifetime
            Text("Rotation Over Lifetime", style = MaterialTheme.typography.labelLarge)
            CurveEditor(
                values = particleConfig.rotationOverLifetime,
                onValuesChange = { onParticleConfigChange(particleConfig.copy(rotationOverLifetime = it)) }
            )
        }
    }
}

@Composable
private fun CurveEditor(
    values: List<Float>,
    onValuesChange: (List<Float>) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
    ) {
        values.forEachIndexed { index, value ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Slider(
                    value = value,
                    onValueChange = {
                        val newValues = values.toMutableList()
                        newValues[index] = it
                        onValuesChange(newValues)
                    },
                    valueRange = 0f..1f,
                    steps = 9,
                    modifier = Modifier
                        .width(40.dp)
                        .height(100.dp)
                )
                Text(
                    text = "${(value * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Composable
private fun ColorSelectorRow(
    label: String,
    colorHex: String,
    onColorSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Row(
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        Color(android.graphics.Color.parseColor(colorHex)),
                        RoundedCornerShape(8.dp)
                    )
            )
            TextButton(onClick = { /* Open color picker */ }) {
                Text("Change")
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
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
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
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps
        )
    }
}
