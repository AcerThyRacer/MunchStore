package com.sugarmunch.app.theme.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.engine.ThemeRuntimeSnapshot
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.*
import com.sugarmunch.app.theme.presets.ThemePresets
import com.sugarmunch.app.theme.profile.ThemeProfile
import com.sugarmunch.app.theme.profile.toCandyTheme
import com.sugarmunch.app.theme.profile.toThemeProfile
import com.sugarmunch.app.ui.typography.toDynamicTypographyConfig
import dagger.hilt.android.EntryPointAccessors
import com.sugarmunch.app.ai.AiAdaptersEntryPoint
import com.sugarmunch.app.theme.engine.ThemeManagerEntryPoint
import kotlin.math.*
import kotlin.random.Random

/**
 * Reads the optional "Reduce motion" preference. When true, animations are reduced or disabled.
 * Uses Hilt entry point to get the injected PreferencesRepository singleton.
 */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    val entryPoint = remember(context.applicationContext) {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            ThemeManagerEntryPoint::class.java
        )
    }
    val prefs = entryPoint.preferencesRepository()
    return prefs.reduceMotion.collectAsState(initial = false).value
}

/**
 * Animated Theme Background - Responds to intensity settings and optional reduce motion.
 * When [previewTheme] and [previewBackgroundIntensity] are provided, uses them for preview (e.g. Theme Studio).
 */
@Composable
fun AnimatedThemeBackground(
    modifier: Modifier = Modifier,
    appId: String? = null,
    previewProfile: ThemeProfile? = null,
    previewTheme: CandyTheme? = null,
    previewBackgroundIntensity: Float? = null,
    previewParticleIntensity: Float? = null
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val reduceMotion = rememberReduceMotion()
    
    val appContext = context.applicationContext
    val aiEntryPoint = remember(appContext) {
        EntryPointAccessors.fromApplication(appContext, AiAdaptersEntryPoint::class.java)
    }
    val acousticAdapter = remember { aiEntryPoint.acousticAmbientAdapter() }
    val bpmThrob by acousticAdapter.bpmThrob.collectAsState()

    val fallbackTheme = remember { ThemePresets.getDefault() }
    val runtime by themeManager.observeThemeRuntime(appId).collectAsState(
        initial = ThemeRuntimeSnapshot(
            profile = fallbackTheme.toThemeProfile(),
            theme = fallbackTheme,
            colors = fallbackTheme.getColorsForIntensity(1f),
            typography = fallbackTheme.toThemeProfile().typography.toDynamicTypographyConfig(context, emptyList()),
            themeIntensity = 1f,
            backgroundIntensity = 1f,
            particleIntensity = 1f,
            animationIntensity = 1f
        )
    )

    val effectiveProfile = previewProfile ?: runtime.profile
    val theme = previewTheme ?: previewProfile?.toCandyTheme() ?: runtime.theme
    val bgInt = previewBackgroundIntensity ?: runtime.backgroundIntensity
    val particleInt = previewParticleIntensity ?: runtime.particleIntensity
    val config = previewProfile?.particles ?: previewTheme?.particleConfig ?: runtime.theme.particleConfig
    
    Box(modifier = modifier.fillMaxSize()) {
        ThemeGradientBackground(
            theme = theme,
            intensity = bgInt,
            reduceMotion = reduceMotion
        )

        LayeredThemeRenderer(
            profile = effectiveProfile,
            backgroundIntensity = bgInt,
            particleIntensity = particleInt,
            reduceMotion = reduceMotion
        )

        if (!reduceMotion && config.enabled && particleInt > 0.1f && effectiveProfile.layers.none { it.kind == com.sugarmunch.app.theme.profile.ThemeLayerKind.PARTICLE_SYSTEM }) {
            ThemeParticleOverlay(config = config, intensity = particleInt)
        }

        if (!reduceMotion && theme.backgroundStyle is BackgroundStyle.AnimatedMesh && effectiveProfile.layers.none { it.kind == com.sugarmunch.app.theme.profile.ThemeLayerKind.MESH_GRADIENT }) {
            MeshGradientOverlay(style = theme.backgroundStyle, intensity = bgInt, bpmThrob = bpmThrob)
        }
    }
}

@Composable
private fun ThemeGradientBackground(
    theme: CandyTheme,
    intensity: Float,
    reduceMotion: Boolean = false
) {
// ... unchanged
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")

    
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (reduceMotion) Int.MAX_VALUE else (15000 / max(intensity, 0.5f)).toInt()
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient_offset"
    )
    
    val (colors, angleDegrees) = when (val style = theme.backgroundStyle) {
        is BackgroundStyle.Gradient -> {
            val c = if (intensity >= 1.5f && style.intenseColors != null) {
                style.intenseColors
            } else {
                style.colors
            }
            Pair(c, style.angleDegrees)
        }
        is BackgroundStyle.AnimatedMesh -> Pair(style.baseColors, 90f)
        is BackgroundStyle.Solid -> Pair(listOf(style.color, style.color), 90f)
    }
    
    val adjustedColors = colors.map { it.copy(alpha = it.alpha * (0.5f + intensity * 0.5f)) }
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val w = with(density) { maxWidth.toPx() }
        val h = with(density) { maxHeight.toPx() }
        val angleRad = angleDegrees * PI.toFloat() / 180f
        val start = Offset(0f, 0f)
        val end = Offset(w * kotlin.math.cos(angleRad), h * kotlin.math.sin(angleRad))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = adjustedColors,
                        start = start,
                        end = end
                    )
                )
        )
    }
}

@Composable
private fun MeshGradientOverlay(
    style: BackgroundStyle.AnimatedMesh,
    intensity: Float,
    bpmThrob: Float = 1f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")
    val reduceMotion = rememberReduceMotion()
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (reduceMotion) Int.MAX_VALUE else (20000 / style.animationSpeed / max(intensity, 0.3f)).toInt()
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "mesh_time"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        // Draw animated mesh gradient circles
        style.baseColors.forEachIndexed { index, color ->
            val phase = (index * 2f * PI / style.baseColors.size).toFloat()
            // Throbbing effect added based on AcousticAmbientAdapter
            val x = width * (0.3f + 0.4f * cos((time * bpmThrob) + phase))
            val y = height * (0.3f + 0.4f * sin((time * bpmThrob) * 0.7f + phase))
            
            drawCircle(
                color = color.copy(alpha = color.alpha * intensity * 0.5f),
                center = Offset(x, y),
                radius = width * 0.4f * bpmThrob
            )
        }
    }
}

@Composable
private fun ThemeParticleOverlay(
    config: ParticleConfig,
    intensity: Float
) {
    val particleCount = (config.count.first + 
        (config.count.last - config.count.first) * intensity).toInt()
    
    val particles = remember(particleCount, config.type) {
        List(particleCount) { index ->
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                size = config.size.min + Random.nextFloat() * (config.size.max - config.size.min),
                speedX = (Random.nextFloat() - 0.5f) * 2f,
                speedY = when (config.type) {
                    ParticleType.RISING -> -Random.nextFloat() * 2f - 0.5f
                    ParticleType.RAINING -> Random.nextFloat() * 2f + 0.5f
                    else -> (Random.nextFloat() - 0.5f) * 2f
                },
                colorIndex = index % config.colors.size,
                alpha = 0.3f + Random.nextFloat() * 0.4f,
                phase = Random.nextFloat() * 2f * PI.toFloat()
            )
        }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(100000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_time"
    )
    
    val animationSpeed = config.speed.min + 
        (config.speed.max - config.speed.min) * intensity
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        
        particles.forEach { particle ->
            // Calculate position based on particle type
            val (x, y) = calculateParticlePosition(
                particle = particle,
                time = time,
                width = width,
                height = height,
                speed = animationSpeed,
                type = config.type
            )
            
            // Draw particle
            val color = config.colors[particle.colorIndex]
            val finalAlpha = particle.alpha * (0.5f + intensity * 0.5f)
            val particleSize = particle.size * (0.8f + intensity * 0.4f)
            
            // Draw different shapes based on particle type
            when (config.type) {
                ParticleType.CANDY_CANE -> {
                    // Draw candy cane shape (hook)
                    drawCandyCane(
                        center = Offset(x, y),
                        size = particleSize,
                        color = color.copy(alpha = finalAlpha),
                        rotation = time * 0.002f + particle.phase
                    )
                }
                ParticleType.GUMMY_BEAR -> {
                    // Draw gummy bear silhouette
                    drawGummyBear(
                        center = Offset(x, y),
                        size = particleSize,
                        color = color.copy(alpha = finalAlpha)
                    )
                }
                ParticleType.SPRINKLES -> {
                    // Draw small rod/sprinkle shape
                    drawSprinkle(
                        center = Offset(x, y),
                        length = particleSize * 2f,
                        width = particleSize * 0.3f,
                        color = color.copy(alpha = finalAlpha),
                        rotation = time * 0.005f + particle.phase
                    )
                }
                ParticleType.LOLLIPOP_SPIN -> {
                    // Draw circular lollipop with spiral
                    drawLollipop(
                        center = Offset(x, y),
                        size = particleSize,
                        color = color.copy(alpha = finalAlpha),
                        rotation = time * 0.003f + particle.phase
                    )
                }
                ParticleType.SUGAR_CRYSTAL -> {
                    // Draw diamond/crystal shape
                    drawSugarCrystal(
                        center = Offset(x, y),
                        size = particleSize,
                        color = color.copy(alpha = finalAlpha)
                    )
                }
                else -> {
                    // Default: draw circle for other particle types
                    drawCircle(
                        color = color.copy(alpha = finalAlpha),
                        center = Offset(x, y),
                        radius = particleSize
                    )
                }
            }
        }
    }
}

private fun calculateParticlePosition(
    particle: Particle,
    time: Float,
    width: Float,
    height: Float,
    speed: Float,
    type: ParticleType
): Pair<Float, Float> {
    val t = time * 0.001f * speed
    
    return when (type) {
        ParticleType.FLOATING -> {
            val x = ((particle.x + particle.speedX * t * 0.1f) % 1f) * width
            val y = ((particle.y + particle.speedY * t * 0.1f) % 1f) * height
            Pair(
                if (x < 0) x + width else x,
                if (y < 0) y + height else y
            )
        }
        ParticleType.RISING -> {
            val y = ((particle.y - particle.speedY * t * 0.05f) % 1f) * height
            Pair(particle.x * width, if (y < 0) y + height else y)
        }
        ParticleType.RAINING -> {
            val y = ((particle.y - particle.speedY * t * 0.05f) % 1f) * height
            Pair(particle.x * width, if (y > height) y - height else y)
        }
        ParticleType.EXPLODING -> {
            val angle = particle.phase + t * 0.5f
            val distance = (t * 0.1f) % 1f
            val x = width / 2 + cos(angle) * width * distance * 0.5f
            val y = height / 2 + sin(angle) * height * distance * 0.5f
            Pair(x, y)
        }
        ParticleType.SWIRLING -> {
            val centerX = width / 2
            val centerY = height / 2
            val baseAngle = atan2(
                particle.y * height - centerY,
                particle.x * width - centerX
            )
            val angle = baseAngle + t * 0.3f
            val radius = hypot(
                particle.x * width - centerX,
                particle.y * height - centerY
            ) * (1f + sin(t + particle.phase) * 0.1f)
            Pair(
                centerX + cos(angle) * radius,
                centerY + sin(angle) * radius
            )
        }
        ParticleType.CHAOTIC -> {
            val x = particle.x * width + sin(t * 2f + particle.phase) * width * 0.1f
            val y = particle.y * height + cos(t * 1.5f + particle.phase) * height * 0.1f
            Pair(
                (x % width + width) % width,
                (y % height + height) % height
            )
        }
        ParticleType.BUBBLES -> {
            val y = ((particle.y - t * 0.02f) % 1f) * height
            val wobble = sin(t * 0.5f + particle.phase) * width * 0.05f
            Pair((particle.x * width + wobble + width) % width, if (y < 0) y + height else y)
        }
        ParticleType.SPARKLE -> {
            val x = ((particle.x + sin(t * 3f + particle.phase) * 0.02f) % 1f) * width
            val y = ((particle.y + cos(t * 2.5f + particle.phase) * 0.02f) % 1f) * height
            Pair(
                if (x < 0) x + width else x,
                if (y < 0) y + height else y
            )
        }
        // NEW: Candy-specific particle types
        ParticleType.CANDY_CANE -> {
            // Rotating striped candy canes - spiral motion
            val angle = t * 0.5f + particle.phase
            val radius = 0.2f + sin(t * 0.3f) * 0.1f
            val centerX = width * (0.3f + particle.x * 0.4f)
            val centerY = height * (0.5f + particle.y * 0.3f)
            Pair(
                centerX + cos(angle) * width * radius,
                centerY + sin(angle) * height * radius
            )
        }
        ParticleType.GUMMY_BEAR -> {
            // Bouncing gummy bears with gravity
            val bounceFreq = 2f
            val bounceHeight = 0.15f
            val x = ((particle.x + t * 0.05f) % 1f) * width
            val y = (particle.y + abs(sin(t * bounceFreq + particle.phase)) * bounceHeight) * height
            Pair(x, if (y > height * 0.9f) height * 0.9f else y)
        }
        ParticleType.SPRINKLES -> {
            // Tiny confetti rods falling with rotation
            val x = ((particle.x + sin(t * 2f + particle.phase) * 0.01f) % 1f) * width
            val y = ((particle.y + t * 0.08f) % 1f) * height
            Pair(
                if (x < 0) x + width else x,
                if (y < 0) y + height else y
            )
        }
        ParticleType.LOLLIPOP_SPIN -> {
            // Rotating circular lollipops around center
            val centerX = width / 2
            val centerY = height / 2
            val angle = t * 0.4f + particle.phase
            val radius = hypot(
                particle.x * width - centerX,
                particle.y * height - centerY
            ) * 0.5f
            Pair(
                centerX + cos(angle) * radius,
                centerY + sin(angle) * radius
            )
        }
        ParticleType.SUGAR_CRYSTAL -> {
            // Sparkling diamond crystals with quick movements
            val x = particle.x * width + sin(t * 4f + particle.phase) * width * 0.05f
            val y = particle.y * height + cos(t * 3f + particle.phase) * height * 0.05f
            Pair(
                (x % width + width) % width,
                (y % height + height) % height
            )
        }
    }
}

private data class Particle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speedX: Float,
    val speedY: Float,
    val colorIndex: Int,
    val alpha: Float,
    val phase: Float
)

// ═════════════════════════════════════════════════════════════════
// CANDY PARTICLE DRAWING HELPERS
// ═════════════════════════════════════════════════════════════════

private fun DrawScope.drawCandyCane(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float
) {
    rotate(rotation) {
        // Candy cane hook shape
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(center.x, center.y - size)
            lineTo(center.x, center.y + size * 0.5f)
            quadraticBezierTo(
                center.x + size * 0.8f, center.y + size * 0.5f,
                center.x + size * 0.8f, center.y - size * 0.2f
            )
            lineTo(center.x + size * 0.5f, center.y - size * 0.2f)
            quadraticBezierTo(
                center.x + size * 0.5f, center.y + size * 0.3f,
                center.x, center.y + size * 0.3f
            )
            lineTo(center.x, center.y - size * 0.7f)
            close()
        }
        drawPath(path, color)
        
        // Striped pattern
        repeat(3) { i ->
            val stripeY = center.y - size * 0.5f + i * size * 0.4f
            drawRect(
                color = Color.White.copy(alpha = color.alpha * 0.5f),
                topLeft = Offset(center.x - size * 0.3f, stripeY),
                size = androidx.compose.ui.geometry.Size(size * 0.6f, size * 0.15f)
            )
        }
    }
}

private fun DrawScope.drawGummyBear(
    center: Offset,
    size: Float,
    color: Color
) {
    // Simplified gummy bear silhouette
    val path = androidx.compose.ui.graphics.Path().apply {
        // Head
        addOval(
            oval = androidx.compose.ui.geometry.Rect(
                topLeft = Offset(center.x - size * 0.4f, center.y - size * 0.8f),
                size = androidx.compose.ui.geometry.Size(size * 0.8f, size * 0.6f)
            )
        )
        // Body
        addOval(
            oval = androidx.compose.ui.geometry.Rect(
                topLeft = Offset(center.x - size * 0.5f, center.y - size * 0.3f),
                size = androidx.compose.ui.geometry.Size(size, size * 0.7f)
            )
        )
        // Ears
        addOval(
            oval = androidx.compose.ui.geometry.Rect(
                topLeft = Offset(center.x - size * 0.5f, center.y - size * 0.9f),
                size = androidx.compose.ui.geometry.Size(size * 0.25f, size * 0.25f)
            )
        )
        addOval(
            oval = androidx.compose.ui.geometry.Rect(
                topLeft = Offset(center.x + size * 0.25f, center.y - size * 0.9f),
                size = androidx.compose.ui.geometry.Size(size * 0.25f, size * 0.25f)
            )
        )
    }
    drawPath(path, color)
    
    // Glossy highlight
    drawCircle(
        color = Color.White.copy(alpha = color.alpha * 0.4f),
        radius = size * 0.2f,
        center = Offset(center.x - size * 0.2f, center.y - size * 0.4f)
    )
}

private fun DrawScope.drawSprinkle(
    center: Offset,
    length: Float,
    width: Float,
    color: Color,
    rotation: Float
) {
    rotate(rotation) {
        drawRoundRect(
            color = color,
            topLeft = Offset(center.x - length / 2, center.y - width / 2),
            size = androidx.compose.ui.geometry.Size(length, width),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 2)
        )
    }
}

private fun DrawScope.drawLollipop(
    center: Offset,
    size: Float,
    color: Color,
    rotation: Float
) {
    rotate(rotation) {
        // Circular candy part
        drawCircle(
            color = color,
            radius = size,
            center = center
        )
        
        // Spiral pattern
        repeat(6) { i ->
            val angle = i * 60f * Math.PI / 180f
            drawLine(
                color = Color.White.copy(alpha = color.alpha * 0.5f),
                start = center,
                end = Offset(
                    center.x + cos(angle).toFloat() * size,
                    center.y + sin(angle).toFloat() * size
                ),
                strokeWidth = 2f
            )
        }
        
        // Stick
        drawRect(
            color = Color.White.copy(alpha = 0.7f),
            topLeft = Offset(center.x - 2f, center.y + size * 0.5f),
            size = androidx.compose.ui.geometry.Size(4f, size * 0.8f)
        )
    }
}

private fun DrawScope.drawSugarCrystal(
    center: Offset,
    size: Float,
    color: Color
) {
    // Diamond/crystal shape
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y - size)  // Top point
        lineTo(center.x + size * 0.7f, center.y)  // Right point
        lineTo(center.x, center.y + size)  // Bottom point
        lineTo(center.x - size * 0.7f, center.y)  // Left point
        close()
    }
    drawPath(path, color)
    
    // Facet lines (crystal sparkle)
    drawLine(
        color = Color.White.copy(alpha = color.alpha * 0.6f),
        start = Offset(center.x - size * 0.3f, center.y - size * 0.5f),
        end = Offset(center.x + size * 0.3f, center.y + size * 0.3f),
        strokeWidth = 1.5f
    )
    drawLine(
        color = Color.White.copy(alpha = color.alpha * 0.6f),
        start = Offset(center.x + size * 0.3f, center.y - size * 0.5f),
        end = Offset(center.x - size * 0.3f, center.y + size * 0.3f),
        strokeWidth = 1.5f
    )
    
    // Sparkle effect
    val sparkleSize = size * 0.15f
    drawCircle(
        color = Color.White.copy(alpha = color.alpha),
        radius = sparkleSize,
        center = Offset(center.x - size * 0.3f, center.y - size * 0.3f)
    )
}

/**
 * Theme-aware card with pulse animation based on intensity. Pulse is disabled when reduce motion is on.
 */
@Composable
fun ThemeAwareCard(
    modifier: Modifier = Modifier,
    intensity: Float = 1f,
    pulseEnabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val reduceMotion = rememberReduceMotion()
    val animationIntensity by themeManager.animationIntensity.collectAsState()
    
    val scale by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = if (!reduceMotion && pulseEnabled && intensity > 0.5f) {
            1f + 0.02f * intensity * animationIntensity
        } else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (reduceMotion) Int.MAX_VALUE else (1500 / max(intensity * animationIntensity, 0.5f)).toInt()
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "card_pulse"
    )
    
    Box(modifier = modifier.scale(scale)) {
        content()
    }
}

/**
 * Theme transition animation wrapper
 */
@Composable
fun ThemeTransition(
    targetTheme: CandyTheme,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    
    val transition = updateTransition(targetState = targetTheme, label = "theme")
    
    // Animate theme change
    content()
}
