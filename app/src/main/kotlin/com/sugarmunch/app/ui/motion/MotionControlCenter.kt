package com.sugarmunch.app.ui.motion

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.loading.SugarTransition
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════════════════════
// MotionControlCenter — main user-facing settings screen
// ═══════════════════════════════════════════════════════════════════════════

@Composable
fun MotionControlCenter(
    config: MotionConfig,
    onConfigChanged: (MotionConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // — 1. Live preview ————————————————————————————————————————
        SectionLabel("Live Preview")
        AnimatedDemoBox(
            config = config,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        // — 2. Preset selection ————————————————————————————————————
        SectionLabel("Preset")
        PresetRow(
            selected = config.preset,
            onPresetSelected = { preset ->
                onConfigChanged(preset.toDetailedConfig())
            }
        )

        // — 3. Speed control ——————————————————————————————————————
        SectionLabel("Speed  ×${"%.1f".format(config.speedMultiplier)}")
        SpeedSlider(
            value = config.speedMultiplier,
            onValueChange = { onConfigChanged(config.copy(speedMultiplier = it.clampSpeed())) }
        )

        // — 4. Screen transition ——————————————————————————————————
        SectionLabel("Screen Transition")
        TransitionChipRow(
            selected = config.screenTransition,
            onSelected = { onConfigChanged(config.copy(screenTransition = it)) }
        )

        // — 5. Particle density ——————————————————————————————————
        SectionLabel("Particle Density  ${"%.1f".format(config.particleDensity)}")
        Slider(
            value = config.particleDensity,
            onValueChange = { onConfigChanged(config.copy(particleDensity = it.clampDensity())) },
            valueRange = 0f..2f,
            steps = 7,
            modifier = Modifier.fillMaxWidth()
        )

        // — 6. Accessibility ——————————————————————————————————————
        AccessibilitySection(
            reduceMotion = config.reduceMotion,
            respectSystem = config.respectSystemReduceMotion,
            onReduceMotionChanged = { onConfigChanged(config.copy(reduceMotion = it)) },
            onRespectSystemChanged = { onConfigChanged(config.copy(respectSystemReduceMotion = it)) }
        )

        Spacer(Modifier.height(32.dp))
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Sub-composables
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

// ── Preset row ──────────────────────────────────────────────────────────

@Composable
private fun PresetRow(
    selected: MotionPreset,
    onPresetSelected: (MotionPreset) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AllMotionPresets.forEach { preset ->
            val isSelected = preset == selected
            PresetCard(
                preset = preset,
                isSelected = isSelected,
                onClick = { onPresetSelected(preset) }
            )
        }
    }
}

@Composable
private fun PresetCard(
    preset: MotionPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "preset_scale"
    )

    Card(
        modifier = Modifier
            .width(120.dp)
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(preset.emoji, style = MaterialTheme.typography.headlineMedium)
            Text(
                preset.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                preset.description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
        }
    }
}

// ── Speed slider ────────────────────────────────────────────────────────

@Composable
private fun SpeedSlider(value: Float, onValueChange: (Float) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0.5f..3f,
            steps = 4,   // markers at 0.5, 1.0, 1.5, 2.0, 2.5, 3.0
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("0.5×", "1×", "1.5×", "2×", "3×").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

// ── Transition chips ────────────────────────────────────────────────────

@Composable
private fun TransitionChipRow(
    selected: SugarTransition,
    onSelected: (SugarTransition) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SugarTransition.entries.forEach { transition ->
            val isActive = transition == selected
            FilterChip(
                selected = isActive,
                onClick = { onSelected(transition) },
                label = {
                    Text(
                        transition.name
                            .replace('_', ' ')
                            .lowercase()
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                leadingIcon = if (isActive) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
                } else null
            )
        }
    }
}

// ── Accessibility ───────────────────────────────────────────────────────

@Composable
private fun AccessibilitySection(
    reduceMotion: Boolean,
    respectSystem: Boolean,
    onReduceMotionChanged: (Boolean) -> Unit,
    onRespectSystemChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Accessibility",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Reduce Motion", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = reduceMotion, onCheckedChange = onReduceMotionChanged)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Follow System Setting", style = MaterialTheme.typography.bodyMedium)
                Switch(checked = respectSystem, onCheckedChange = onRespectSystemChanged)
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Reduce Motion disables most animations including transitions, " +
                        "particles, and parallax effects for a calmer experience.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// AnimatedDemoBox — live preview sandbox
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun AnimatedDemoBox(
    config: MotionConfig,
    modifier: Modifier = Modifier
) {
    val isReducedOrOff = config.reduceMotion || config.speedMultiplier <= 0f
    val cycleDurationMs = 4000

    // Cycle counter that increments every N seconds to restart the demo
    var cycle by remember { mutableIntStateOf(0) }
    LaunchedEffect(config) {
        cycle = 0 // reset on config change
    }
    LaunchedEffect(cycle, isReducedOrOff) {
        if (isReducedOrOff) return@LaunchedEffect
        kotlinx.coroutines.delay(cycleDurationMs.toLong())
        cycle++
    }

    // Visibility toggles on each cycle
    val visible = remember(cycle) { mutableStateOf(true) }
    LaunchedEffect(cycle) {
        visible.value = false
        kotlinx.coroutines.delay(200)
        visible.value = true
    }

    val tweenMs = if (isReducedOrOff) 0
    else (config.baseDurationMs / config.speedMultiplier.coerceAtLeast(0.1f)).roundToInt()

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        // Particle layer
        if (config.particleDensity > 0f && !isReducedOrOff) {
            DemoParticles(
                density = config.particleDensity,
                modifier = Modifier.matchParentSize()
            )
        }

        // Animated card
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn(tween(tweenMs)) + scaleIn(
                tween(tweenMs),
                initialScale = 0.85f
            ),
            exit = fadeOut(tween(tweenMs.coerceAtMost(300))) + scaleOut(
                tween(tweenMs.coerceAtMost(300)),
                targetScale = 0.85f
            )
        ) {
            DemoCard(config = config)
        }
    }
}

@Composable
private fun DemoCard(config: MotionConfig) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) config.buttonPressScale else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "btn_press"
    )

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.padding(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                config.preset.emoji,
                style = MaterialTheme.typography.headlineLarge
            )
            Text(
                config.preset.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {},
                interactionSource = interactionSource,
                modifier = Modifier.scale(pressScale)
            ) {
                Text("Tap me")
            }
        }
    }
}

// ── Lightweight demo particles ──────────────────────────────────────────

@Composable
private fun DemoParticles(
    density: Float,
    modifier: Modifier = Modifier
) {
    data class Dot(var x: Float, var y: Float, val radius: Float, val speed: Float, val color: Color)

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val palette = remember(primary, secondary, tertiary) { listOf(primary, secondary, tertiary) }

    val count = (15 * density).roundToInt().coerceIn(1, 60)
    val dots = remember(count, palette) {
        List(count) {
            Dot(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 4f + 2f,
                speed = Random.nextFloat() * 0.4f + 0.1f,
                color = palette[it % palette.size].copy(alpha = 0.45f)
            )
        }
    }

    var time by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val start = withFrameNanos { it }
        while (true) {
            withFrameNanos { nanos ->
                time = (nanos - start) / 1_000_000_000f
            }
        }
    }

    Canvas(modifier = modifier) {
        dots.forEach { d ->
            val px = ((d.x + sin(time * d.speed * 2f + d.radius).toFloat() * 0.06f) % 1f) * size.width
            val py = ((d.y + time * d.speed * 0.04f) % 1f) * size.height
            drawCircle(color = d.color, radius = d.radius.dp.toPx(), center = Offset(px, py))
        }
    }
}
