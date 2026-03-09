package com.sugarmunch.app.theme.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.sugarmunch.app.theme.profile.ThemeLayerKind
import com.sugarmunch.app.theme.profile.ThemeLayerSpec
import com.sugarmunch.app.theme.profile.ThemeMeshSpec
import com.sugarmunch.app.theme.profile.ThemeParticleSpec
import com.sugarmunch.app.theme.profile.ThemeProfile
import com.sugarmunch.app.theme.profile.toComposeColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LayeredThemeRenderer(
    profile: ThemeProfile,
    backgroundIntensity: Float,
    particleIntensity: Float,
    reduceMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val activeLayers = remember(profile.layers) { profile.layers.filter { it.enabled } }
    Box(modifier = modifier.fillMaxSize()) {
        activeLayers.forEach { layer ->
            when (layer.kind) {
                ThemeLayerKind.BACKGROUND_GRADIENT -> GradientLayer(layer, backgroundIntensity)
                ThemeLayerKind.MESH_GRADIENT -> MeshLayer(layer.mesh, layer.opacity, backgroundIntensity, reduceMotion, layer.blendMode.toComposeBlendMode())
                ThemeLayerKind.PARTICLE_SYSTEM -> {
                    if (!reduceMotion) {
                        ParticleLayer(
                            particle = layer.particle ?: profile.particles,
                            opacity = layer.opacity,
                            intensity = particleIntensity
                        )
                    }
                }
                ThemeLayerKind.COLOR_OVERLAY -> ColorOverlayLayer(layer)
                ThemeLayerKind.TEXTURE -> TextureLayer(layer)
                ThemeLayerKind.LIGHT_EFFECTS -> LightEffectsLayer(layer)
                ThemeLayerKind.UI_ELEMENTS -> UiElementsLayer(layer)
            }
        }
    }
}

@Composable
private fun GradientLayer(layer: ThemeLayerSpec, intensity: Float) {
    val colors = layer.gradientStops.ifEmpty {
        listOfNotNull(layer.overlayHex?.let { com.sugarmunch.app.theme.profile.ThemeGradientStopSpec(it, 0f) })
    }.map { it.colorHex.toComposeColor().copy(alpha = layer.opacity * (0.4f + intensity * 0.35f)) }
    if (colors.isEmpty()) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(colors)
            )
    )
}

@Composable
private fun MeshLayer(
    mesh: ThemeMeshSpec?,
    opacity: Float,
    intensity: Float,
    reduceMotion: Boolean,
    blendMode: BlendMode
) {
    if (mesh == null || mesh.points.isEmpty()) return
    val transition = rememberInfiniteTransition(label = "layer-mesh")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (reduceMotion) Int.MAX_VALUE else (16_000 / mesh.animationSpeed.coerceAtLeast(0.25f)).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "layer-mesh-phase"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        mesh.points.forEachIndexed { index, point ->
            val driftScale = if (reduceMotion) 0f else mesh.amplitude * size.minDimension
            val x = point.xFraction * size.width + cos(phase + index) * driftScale * point.driftX
            val y = point.yFraction * size.height + sin(phase + index) * driftScale * point.driftY
            drawCircle(
                color = point.colorHex.toComposeColor().copy(alpha = opacity * (0.25f + intensity * 0.2f)),
                center = Offset(x.toFloat(), y.toFloat()),
                radius = size.minDimension * (0.22f + point.influence * 0.12f),
                blendMode = blendMode
            )
        }
    }
}

@Composable
private fun ParticleLayer(
    particle: ThemeParticleSpec,
    opacity: Float,
    intensity: Float
) {
    if (!particle.enabled || particle.colors.isEmpty()) return
    val cappedCount = (particle.countMin + ((particle.countMax - particle.countMin) * intensity)).toInt().coerceAtMost(72)
    val particles = remember(cappedCount, particle.colors) {
        List(cappedCount) {
            Triple(Random.nextFloat(), Random.nextFloat(), particle.colors[it % particle.colors.size].toComposeColor())
        }
    }
    val transition = rememberInfiniteTransition(label = "layer-particles")
    val travel by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(40_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "layer-particles-travel"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        particles.forEachIndexed { index, particleSeed ->
            val offsetX = ((particleSeed.first + travel * (0.03f + index * 0.0007f)) % 1f) * size.width
            val offsetY = ((particleSeed.second + sin(travel * PI.toFloat() + index) * 0.08f + travel) % 1f) * size.height
            drawCircle(
                color = particleSeed.third.copy(alpha = opacity * 0.55f),
                center = Offset(offsetX, offsetY),
                radius = (particle.sizeMin + particle.sizeMax) * 0.5f
            )
        }
    }
}

@Composable
private fun ColorOverlayLayer(layer: ThemeLayerSpec) {
    val overlay = layer.overlayHex?.toComposeColor() ?: return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(overlay.copy(alpha = layer.opacity * 0.4f))
    )
}

@Composable
private fun TextureLayer(layer: ThemeLayerSpec) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val color = (layer.overlayHex?.toComposeColor() ?: Color.White).copy(alpha = layer.textureOpacity * layer.opacity)
        val gap = size.minDimension / 12f
        var x = -size.height
        while (x < size.width + size.height) {
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x + size.height, size.height),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            x += gap
        }
    }
}

@Composable
private fun LightEffectsLayer(layer: ThemeLayerSpec) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val glow = (layer.glowHex?.toComposeColor() ?: Color.White).copy(alpha = layer.opacity * 0.45f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow, Color.Transparent),
                center = Offset(size.width * 0.25f, size.height * 0.2f),
                radius = size.minDimension * (0.2f + layer.glowIntensity * 0.14f)
            ),
            radius = size.minDimension * (0.2f + layer.glowIntensity * 0.14f),
            center = Offset(size.width * 0.25f, size.height * 0.2f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(glow.copy(alpha = glow.alpha * 0.8f), Color.Transparent),
                center = Offset(size.width * 0.82f, size.height * 0.72f),
                radius = size.minDimension * (0.18f + layer.glowIntensity * 0.1f)
            ),
            radius = size.minDimension * (0.18f + layer.glowIntensity * 0.1f),
            center = Offset(size.width * 0.82f, size.height * 0.72f)
        )
    }
}

@Composable
private fun UiElementsLayer(layer: ThemeLayerSpec) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val accent = (layer.accentHex?.toComposeColor() ?: Color.White).copy(alpha = layer.opacity * 0.35f)
        drawRoundRect(
            color = accent,
            topLeft = Offset(size.width * 0.08f, size.height * 0.08f),
            size = Size(size.width * 0.34f, size.height * 0.12f)
        )
        drawRoundRect(
            color = accent.copy(alpha = accent.alpha * 0.7f),
            topLeft = Offset(size.width * 0.58f, size.height * 0.78f),
            size = Size(size.width * 0.24f, size.height * 0.08f)
        )
        drawRoundRect(
            color = accent.copy(alpha = accent.alpha * 0.9f),
            topLeft = Offset(size.width * 0.12f, size.height * 0.64f),
            size = Size(size.width * 0.22f, size.height * 0.06f),
            style = Stroke(width = 3f)
        )
    }
}

private fun com.sugarmunch.app.theme.layers.BlendMode.toComposeBlendMode(): BlendMode = when (this) {
    com.sugarmunch.app.theme.layers.BlendMode.NORMAL -> BlendMode.SrcOver
    com.sugarmunch.app.theme.layers.BlendMode.MULTIPLY -> BlendMode.Multiply
    com.sugarmunch.app.theme.layers.BlendMode.SCREEN -> BlendMode.Screen
    com.sugarmunch.app.theme.layers.BlendMode.OVERLAY -> BlendMode.Overlay
    com.sugarmunch.app.theme.layers.BlendMode.SOFT_LIGHT -> BlendMode.Softlight
    com.sugarmunch.app.theme.layers.BlendMode.HARD_LIGHT -> BlendMode.Hardlight
    com.sugarmunch.app.theme.layers.BlendMode.COLOR_DODGE -> BlendMode.ColorDodge
    com.sugarmunch.app.theme.layers.BlendMode.COLOR_BURN -> BlendMode.ColorBurn
    com.sugarmunch.app.theme.layers.BlendMode.DIFFERENCE -> BlendMode.Difference
    com.sugarmunch.app.theme.layers.BlendMode.EXCLUSION -> BlendMode.Exclusion
    com.sugarmunch.app.theme.layers.BlendMode.ADD -> BlendMode.Plus
    com.sugarmunch.app.theme.layers.BlendMode.HUE,
    com.sugarmunch.app.theme.layers.BlendMode.SATURATION,
    com.sugarmunch.app.theme.layers.BlendMode.COLOR,
    com.sugarmunch.app.theme.layers.BlendMode.LUMINOSITY,
    com.sugarmunch.app.theme.layers.BlendMode.SUBTRACT,
    com.sugarmunch.app.theme.layers.BlendMode.DIVIDE,
    com.sugarmunch.app.theme.layers.BlendMode.AVERAGE,
    com.sugarmunch.app.theme.layers.BlendMode.NEGATION,
    com.sugarmunch.app.theme.layers.BlendMode.REFLECT,
    com.sugarmunch.app.theme.layers.BlendMode.GLOW,
    com.sugarmunch.app.theme.layers.BlendMode.PHOENIX -> BlendMode.SrcOver
}
