package com.sugarmunch.app.plugin.examples

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.plugin.ThemePlugin
import com.sugarmunch.app.theme.model.*
import kotlin.math.*

/**
 * ============================================
 * SUGARMUNCH THEME PLUGIN EXAMPLES
 * ============================================
 * 
 * This file contains example theme plugins that demonstrate
 * how to create custom themes for SugarMunch.
 * 
 * Each plugin showcases different capabilities:
 * - Dynamic color generation
 * - Gradient backgrounds
 * - Animated backgrounds
 * - Particle configurations
 * - Animation definitions
 */

/**
 * Example 1: OCEAN BREEZE THEME
 * 
 * A calming ocean-inspired theme with flowing waves.
 * Demonstrates custom animated backgrounds and dynamic colors.
 */
class OceanBreezeTheme : ThemePlugin(
    id = "ocean_breeze",
    name = "Ocean Breeze",
    description = "Calming ocean waves and refreshing aqua tones",
    category = ThemeCategory.CHILL,
    isDark = false,
    defaultIntensity = 0.8f
) {
    override fun generateColors(intensity: Float): BaseColors {
        val saturation = 0.6f + intensity * 0.2f
        val lightness = 0.5f + (intensity - 1f) * 0.1f
        
        return BaseColors(
            primary = hsl(190f, saturation, lightness),
            secondary = hsl(170f, saturation * 0.9f, lightness),
            tertiary = hsl(210f, saturation * 0.8f, lightness + 0.1f),
            accent = hsl(160f, saturation, lightness + 0.15f),
            surface = hsl(200f, 0.1f, 0.95f),
            surfaceVariant = hsl(195f, 0.15f, 0.90f),
            background = hsl(200f, 0.05f, 0.98f),
            onPrimary = Color.White,
            onSurface = hsl(200f, 0.3f, 0.2f),
            onBackground = hsl(200f, 0.2f, 0.25f),
            error = Color(0xFFE53935),
            success = Color(0xFF43A047)
        )
    }
    
    override fun generateBackground(intensity: Float): BackgroundStyle {
        return BackgroundStyle.Gradient(
            colors = listOf(
                hsl(190f, 0.4f, 0.95f),
                hsl(195f, 0.5f, 0.90f),
                hsl(200f, 0.6f, 0.85f)
            ),
            intenseColors = listOf(
                hsl(185f, 0.6f, 0.90f),
                hsl(190f, 0.7f, 0.80f),
                hsl(200f, 0.8f, 0.70f)
            )
        )
    }
    
    override fun generateParticles(intensity: Float): ParticleConfig {
        return ParticleConfig(
            enabled = true,
            colors = listOf(
                hsl(190f, 0.7f, 0.8f, 0.5f),
                hsl(170f, 0.6f, 0.85f, 0.4f),
                hsl(210f, 0.5f, 0.75f, 0.4f)
            ),
            count = (15 * intensity).toInt()..(40 * intensity).toInt(),
            speed = FloatRange(0.3f, 1f * intensity),
            size = FloatRange(2f, 8f),
            type = ParticleType.FLOATING,
            intensityMultiplier = intensity
        )
    }
    
    override fun generateAnimations(intensity: Float): AnimationConfig {
        return AnimationConfig(
            cardPulseEnabled = false,
            cardPulseSpeed = 0.5f,
            backgroundAnimationEnabled = true,
            transitionDuration = (500 / intensity.coerceAtLeast(0.5f)).toInt(),
            staggerDelay = (80 / intensity.coerceAtLeast(0.5f)).toInt()
        )
    }
    
    @Composable
    override fun AnimatedBackground(modifier: Modifier, intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "ocean")
        
        // Multiple wave phases for layered effect
        val wave1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(6000, easing = LinearEasing)
            ),
            label = "wave1"
        )
        
        val wave2 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing)
            ),
            label = "wave2"
        )
        
        val wave3 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(10000, easing = LinearEasing)
            ),
            label = "wave3"
        )
        
        Canvas(modifier = modifier.fillMaxSize()) {
            // Base gradient
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE0F7FA),
                        Color(0xFFB2EBF2),
                        Color(0xFF80DEEA)
                    )
                )
            )
            
            // Draw wave layers
            val waveColors = listOf(
                Color(0xFF4DD0E1).copy(alpha = 0.2f * intensity),
                Color(0xFF26C6DA).copy(alpha = 0.25f * intensity),
                Color(0xFF00BCD4).copy(alpha = 0.3f * intensity)
            )
            
            val waves = listOf(wave1, wave2, wave3)
            
            waveColors.forEachIndexed { index, color ->
                drawWave(
                    color = color,
                    phase = waves[index],
                    amplitude = 30f + index * 15f,
                    frequency = 0.003f + index * 0.001f,
                    yOffset = size.height * (0.6f + index * 0.15f)
                )
            }
            
            // Draw floating bubbles
            repeat((10 * intensity).toInt()) { i ->
                val bubbleX = (i * 100 + wave1 * 10) % size.width
                val bubbleY = (size.height * 0.5f + sin(wave1 + i) * 50f) % size.height
                val bubbleSize = 3f + (i % 5)
                
                drawCircle(
                    color = Color.White.copy(alpha = 0.3f * intensity),
                    radius = bubbleSize,
                    center = Offset(bubbleX, bubbleY)
                )
            }
        }
    }
    
    private fun DrawScope.drawWave(
        color: Color,
        phase: Float,
        amplitude: Float,
        frequency: Float,
        yOffset: Float
    ) {
        val path = androidx.compose.ui.graphics.Path()
        path.moveTo(0f, size.height)
        
        for (x in 0..size.width.toInt() step 10) {
            val y = yOffset + sin(x * frequency + phase) * amplitude
            if (x == 0) {
                path.moveTo(x.toFloat(), y)
            } else {
                path.lineTo(x.toFloat(), y)
            }
        }
        
        path.lineTo(size.width, size.height)
        path.lineTo(0f, size.height)
        path.close()
        
        drawPath(path = path, color = color)
    }
}

/**
 * Example 2: GALAXY DARK THEME
 * 
 * Deep space theme with stars and nebulae.
 * Demonstrates particle systems and complex animations.
 */
class GalaxyDarkTheme : ThemePlugin(
    id = "galaxy_dark",
    name = "Galaxy",
    description = "Deep space exploration with stars and cosmic colors",
    category = ThemeCategory.DARK,
    isDark = true,
    defaultIntensity = 1.1f
) {
    override fun generateColors(intensity: Float): BaseColors {
        val saturation = 0.8f + intensity * 0.1f
        val lightness = 0.5f + (intensity - 1f) * 0.1f
        
        return BaseColors(
            primary = hsl(270f, saturation, lightness),
            secondary = hsl(300f, saturation * 0.9f, lightness),
            tertiary = hsl(240f, saturation * 0.8f, lightness + 0.05f),
            accent = hsl(180f, saturation, lightness + 0.1f),
            surface = hsl(260f, 0.3f, 0.12f),
            surfaceVariant = hsl(255f, 0.25f, 0.15f),
            background = hsl(260f, 0.4f, 0.05f),
            onPrimary = Color.White,
            onSurface = hsl(260f, 0.1f, 0.95f),
            onBackground = hsl(260f, 0.15f, 0.90f),
            error = Color(0xFFFF5252),
            success = Color(0xFF69F0AE)
        )
    }
    
    override fun generateBackground(intensity: Float): BackgroundStyle {
        return BackgroundStyle.AnimatedMesh(
            baseColors = listOf(
                hsl(260f, 0.5f, 0.05f),
                hsl(270f, 0.6f, 0.08f),
                hsl(280f, 0.4f, 0.06f),
                hsl(240f, 0.5f, 0.07f)
            ),
            animationSpeed = 0.3f * intensity,
            complexity = 4
        )
    }
    
    override fun generateParticles(intensity: Float): ParticleConfig {
        return ParticleConfig(
            enabled = true,
            colors = listOf(
                Color.White.copy(alpha = 0.8f),
                hsl(60f, 1f, 0.8f, 0.7f),  // Yellow stars
                hsl(200f, 0.8f, 0.8f, 0.6f) // Blue stars
            ),
            count = (30 * intensity).toInt()..(80 * intensity).toInt(),
            speed = FloatRange(0f, 0.2f),
            size = FloatRange(1f, 3f),
            type = ParticleType.SWIRLING,
            intensityMultiplier = intensity
        )
    }
    
    override fun generateAnimations(intensity: Float): AnimationConfig {
        return AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.4f * intensity,
            backgroundAnimationEnabled = true,
            transitionDuration = 600,
            staggerDelay = 80
        )
    }
    
    @Composable
    override fun AnimatedBackground(modifier: Modifier, intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "galaxy")
        
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(120000, easing = LinearEasing)
            ),
            label = "rotation"
        )
        
        val nebulaPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing)
            ),
            label = "nebula"
        )
        
        Canvas(modifier = modifier.fillMaxSize()) {
            // Deep space background
            drawRect(Color(0xFF0A0014))
            
            // Draw nebula clouds
            val nebulaColors = listOf(
                Color(0xFF4A148C).copy(alpha = 0.4f * intensity),
                Color(0xFF311B92).copy(alpha = 0.3f * intensity),
                Color(0xFF1A237E).copy(alpha = 0.35f * intensity)
            )
            
            nebulaColors.forEachIndexed { index, color ->
                val offsetX = size.width * (0.3f + sin(nebulaPhase + index) * 0.2f)
                val offsetY = size.height * (0.4f + cos(nebulaPhase * 0.7f + index) * 0.2f)
                
                drawCircle(
                    color = color,
                    radius = size.minDimension * (0.4f + index * 0.1f),
                    center = Offset(offsetX, offsetY)
                )
            }
            
            // Draw stars with rotation
            rotate(rotation, center) {
                repeat((100 * intensity).toInt()) { i ->
                    val angle = i * 137.5f * (PI.toFloat() / 180f)
                    val radius = sqrt(i.toFloat()) * 20f
                    val x = center.x + cos(angle) * radius
                    val y = center.y + sin(angle) * radius
                    
                    if (x in 0f..size.width && y in 0f..size.height) {
                        val starSize = 1f + (i % 3)
                        val twinkle = sin(nebulaPhase + i * 0.5f) * 0.3f + 0.7f
                        
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f * twinkle * intensity),
                            radius = starSize,
                            center = Offset(x, y)
                        )
                    }
                }
            }
            
            // Draw shooting stars occasionally
            if (sin(nebulaPhase * 3) > 0.8f) {
                val shootX = (nebulaPhase * 100) % size.width
                val shootY = size.height * 0.3f + sin(nebulaPhase) * 100
                
                drawLine(
                    color = Color.White.copy(alpha = 0.6f * intensity),
                    start = Offset(shootX, shootY),
                    end = Offset(shootX - 50, shootY + 30),
                    strokeWidth = 2f
                )
            }
        }
    }
}

/**
 * Example 3: CHERRY BLOSSOM THEME
 * 
 * Japanese spring theme with falling sakura petals.
 * Demonstrates seasonal themes and falling particles.
 */
class CherryBlossomTheme : ThemePlugin(
    id = "cherry_blossom",
    name = "Cherry Blossom",
    description = "Japanese spring with falling sakura petals",
    category = ThemeCategory.SEASONAL,
    isDark = false,
    defaultIntensity = 0.9f
) {
    override fun generateColors(intensity: Float): BaseColors {
        return BaseColors(
            primary = hsl(340f, 0.7f, 0.75f),
            secondary = hsl(10f, 0.6f, 0.85f),
            tertiary = hsl(120f, 0.3f, 0.75f),
            accent = hsl(50f, 0.9f, 0.85f),
            surface = hsl(340f, 0.1f, 0.97f),
            surfaceVariant = hsl(340f, 0.15f, 0.93f),
            background = hsl(340f, 0.05f, 0.98f),
            onPrimary = Color.White,
            onSurface = hsl(340f, 0.3f, 0.2f),
            onBackground = hsl(340f, 0.25f, 0.25f),
            error = Color(0xFFE57373),
            success = Color(0xFF81C784)
        )
    }
    
    override fun generateBackground(intensity: Float): BackgroundStyle {
        return BackgroundStyle.Gradient(
            colors = listOf(
                hsl(340f, 0.15f, 0.96f),
                hsl(30f, 0.2f, 0.94f),
                hsl(60f, 0.15f, 0.92f)
            ),
            intenseColors = listOf(
                hsl(340f, 0.25f, 0.92f),
                hsl(20f, 0.3f, 0.88f),
                hsl(50f, 0.2f, 0.85f)
            )
        )
    }
    
    override fun generateParticles(intensity: Float): ParticleConfig {
        return ParticleConfig(
            enabled = true,
            colors = listOf(
                hsl(340f, 0.6f, 0.85f, 0.7f),
                hsl(350f, 0.5f, 0.90f, 0.6f),
                hsl(330f, 0.4f, 0.88f, 0.6f)
            ),
            count = (20 * intensity).toInt()..(50 * intensity).toInt(),
            speed = FloatRange(0.5f, 1.5f * intensity),
            size = FloatRange(4f, 10f),
            type = ParticleType.RAINING,
            intensityMultiplier = intensity
        )
    }
    
    override fun generateAnimations(intensity: Float): AnimationConfig {
        return AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 0.6f * intensity,
            backgroundAnimationEnabled = true,
            transitionDuration = 500,
            staggerDelay = 60
        )
    }
    
    @Composable
    override fun AnimatedBackground(modifier: Modifier, intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "sakura")
        
        val windPhase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = LinearEasing)
            ),
            label = "wind"
        )
        
        Canvas(modifier = modifier.fillMaxSize())
        {
            // Soft gradient background
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF0F5),
                        Color(0xFFFFE4E1),
                        Color(0xFFFDF5E6)
                    )
                )
            )
            
            // Draw sakura petals
            val petalColors = listOf(
                Color(0xFFFFB7C5).copy(alpha = 0.6f * intensity),
                Color(0xFFFFC0CB).copy(alpha = 0.5f * intensity),
                Color(0xFFFFD1DC).copy(alpha = 0.4f * intensity)
            )
            
            repeat((25 * intensity).toInt()) { i ->
                val petalPhase = (i * 0.5f + windPhase * (0.5f + i * 0.05f)) % (2 * PI.toFloat())
                val x = (i * 50f + sin(petalPhase) * 100f) % size.width
                val y = (petalPhase / (2 * PI.toFloat())) * size.height
                val rotation = petalPhase * 50f
                val scale = 0.5f + (i % 3) * 0.3f
                
                drawPetals(
                    color = petalColors[i % petalColors.size],
                    center = Offset(x, y),
                    radius = 8f * scale * intensity,
                    rotation = rotation
                )
            }
        }
    }
    
    private fun DrawScope.drawPetals(
        color: Color,
        center: Offset,
        radius: Float,
        rotation: Float
    ) {
        rotate(rotation, center) {
            // Draw 5 petals
            repeat(5) { i ->
                val angle = i * 72f * (PI.toFloat() / 180f)
                val petalX = center.x + cos(angle) * radius * 0.6f
                val petalY = center.y + sin(angle) * radius * 0.6f
                
                drawCircle(
                    color = color,
                    radius = radius * 0.4f,
                    center = Offset(petalX, petalY)
                )
            }
            
            // Center
            drawCircle(
                color = Color(0xFFFFF0F0).copy(alpha = color.alpha),
                radius = radius * 0.25f,
                center = center
            )
        }
    }
}

/**
 * Example 4: RETRO ARCADE THEME
 * 
 * 80s retro arcade aesthetic with scanlines and neon.
 * Demonstrates retro styling and unique visual effects.
 */
class RetroArcadeTheme : ThemePlugin(
    id = "retro_arcade",
    name = "Retro Arcade",
    description = "80s neon arcade with scanlines and retro vibes",
    category = ThemeCategory.SUGARRUSH,
    isDark = true,
    defaultIntensity = 1.3f
) {
    override fun generateColors(intensity: Float): BaseColors {
        return BaseColors(
            primary = Color(0xFFFF00FF),  // Magenta
            secondary = Color(0xFF00FFFF), // Cyan
            tertiary = Color(0xFFFFFF00),  // Yellow
            accent = Color(0xFF00FF00),    // Lime
            surface = Color(0xFF1A0A1A),
            surfaceVariant = Color(0xFF2A152A),
            background = Color(0xFF0D050D),
            onPrimary = Color.Black,
            onSurface = Color(0xFFFFE0FF),
            onBackground = Color(0xFFFFD0FF),
            error = Color(0xFFFF0040),
            success = Color(0xFF00FF80)
        )
    }
    
    override fun generateBackground(intensity: Float): BackgroundStyle {
        return BackgroundStyle.Solid(Color(0xFF0D050D))
    }
    
    override fun generateParticles(intensity: Float): ParticleConfig {
        return ParticleConfig(
            enabled = true,
            colors = listOf(
                Color(0xFFFF00FF).copy(alpha = 0.7f),
                Color(0xFF00FFFF).copy(alpha = 0.7f),
                Color(0xFFFFFF00).copy(alpha = 0.7f)
            ),
            count = (10 * intensity).toInt()..(25 * intensity).toInt(),
            speed = FloatRange(1f, 3f * intensity),
            size = FloatRange(2f, 5f),
            type = ParticleType.CHAOTIC,
            intensityMultiplier = intensity
        )
    }
    
    override fun generateAnimations(intensity: Float): AnimationConfig {
        return AnimationConfig(
            cardPulseEnabled = true,
            cardPulseSpeed = 2f * intensity,
            backgroundAnimationEnabled = true,
            transitionDuration = 150,
            staggerDelay = 30
        )
    }
    
    @Composable
    override fun AnimatedBackground(modifier: Modifier, intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "retro")
        
        val scanlineOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 4f,
            animationSpec = infiniteRepeatable(
                animation = tween(100, easing = LinearEasing)
            ),
            label = "scanline"
        )
        
        val glowPulse by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glow"
        )
        
        Canvas(modifier = modifier.fillMaxSize()) {
            // Dark background
            drawRect(Color(0xFF0D050D))
            
            // Draw grid
            val gridColor = Color(0xFF330033).copy(alpha = 0.3f * intensity)
            val gridSpacing = 50f
            
            for (i in 0..(size.width / gridSpacing).toInt()) {
                val x = i * gridSpacing
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
            }
            
            // Horizon grid (perspective effect)
            val horizonY = size.height * 0.6f
            for (i in 0..10) {
                val y = horizonY + i * (size.height - horizonY) / 10
                drawLine(
                    color = Color(0xFFFF00FF).copy(alpha = 0.1f * intensity),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
            
            // Scanlines
            val scanlineAlpha = 0.1f * intensity
            for (i in 0..(size.height / 4).toInt()) {
                val y = i * 4f + scanlineOffset
                drawLine(
                    color = Color.Black.copy(alpha = scanlineAlpha),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 2f
                )
            }
            
            // Neon glow orbs
            listOf(
                Triple(0.2f, 0.3f, Color(0xFFFF00FF)),
                Triple(0.8f, 0.4f, Color(0xFF00FFFF)),
                Triple(0.5f, 0.7f, Color(0xFFFFFF00))
            ).forEach { (rx, ry, color) ->
                drawCircle(
                    color = color.copy(alpha = 0.2f * glowPulse * intensity),
                    radius = 100f,
                    center = Offset(size.width * rx, size.height * ry)
                )
            }
            
            // Retro sun
            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFF00FF).copy(alpha = 0.4f * intensity),
                        Color(0xFF8800FF).copy(alpha = 0.2f * intensity),
                        Color.Transparent
                    )
                ),
                radius = size.minDimension * 0.3f,
                center = Offset(size.width * 0.5f, size.height * 0.4f)
            )
        }
    }
}

// ========== HELPER FUNCTIONS ==========

/**
 * Create a color from HSL values
 */
private fun hsl(hue: Float, saturation: Float, lightness: Float, alpha: Float = 1f): Color {
    val c = (1f - kotlin.math.abs(2f * lightness - 1f)) * saturation
    val x = c * (1f - kotlin.math.abs((hue / 60f) % 2f - 1f))
    val m = lightness - c / 2f
    
    val (r, g, b) = when {
        hue < 60f -> Triple(c, x, 0f)
        hue < 120f -> Triple(x, c, 0f)
        hue < 180f -> Triple(0f, c, x)
        hue < 240f -> Triple(0f, x, c)
        hue < 300f -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }
    
    return Color(
        red = (r + m).coerceIn(0f, 1f),
        green = (g + m).coerceIn(0f, 1f),
        blue = (b + m).coerceIn(0f, 1f),
        alpha = alpha
    )
}

/**
 * ============================================
 * HOW TO CREATE YOUR OWN THEME PLUGIN
 * ============================================
 * 
 * 1. Create a new class extending ThemePlugin
 * 2. Define your theme's metadata in the constructor
 * 3. Implement generateColors() to return BaseColors
 * 4. Implement generateBackground() for background style
 * 5. Implement generateParticles() for particle config
 * 6. Implement generateAnimations() for animation config
 * 7. Override AnimatedBackground() for custom animations
 * 8. Package with a manifest.json file
 * 
 * Tips:
 * - Use hsl() helper for easy color manipulation
 * - Intensity (0.0-2.0) controls effect strength
 * - Test at different intensity levels
 * - Keep animations subtle for low intensity
 * 
 * For more examples and documentation, visit:
 * https://docs.sugarmunch.app/theme-development
 */
