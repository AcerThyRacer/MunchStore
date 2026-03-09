package com.sugarmunch.app.plugin.examples

import android.graphics.Canvas
import android.graphics.Paint
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.v2.model.EffectCategory
import com.sugarmunch.app.effects.v2.model.EffectSetting
import com.sugarmunch.app.effects.v2.model.EffectType
import com.sugarmunch.app.plugin.EffectPlugin
import com.sugarmunch.app.plugin.Particle
import com.sugarmunch.app.plugin.model.PluginContext
import com.sugarmunch.app.plugin.model.PluginManifest
import com.sugarmunch.app.plugin.model.PluginCategory
import com.sugarmunch.app.plugin.model.PluginPermission
import kotlin.math.*
import kotlin.random.Random

/**
 * ============================================
 * SUGARMUNCH EFFECT PLUGIN EXAMPLES
 * ============================================
 * 
 * This file contains example effect plugins that demonstrate
 * how to create custom effects for SugarMunch.
 * 
 * Each plugin showcases different capabilities:
 * - Canvas drawing
 * - Particle systems
 * - Haptic feedback
 * - Custom settings
 * - Compose previews
 */

/**
 * Example 1: MATRIX RAIN EFFECT
 * 
 * Classic Matrix-style falling code effect.
 * Demonstrates custom canvas drawing and animation.
 */
class MatrixRainEffect : EffectPlugin(
    id = "matrix_rain",
    name = "Matrix Rain",
    description = "The classic Matrix digital rain effect with falling code",
    category = EffectCategory.VISUAL_OVERLAY,
    type = EffectType.OVERLAY,
    minIntensity = 0.3f,
    maxIntensity = 2.0f,
    defaultIntensity = 1.0f,
    hasVisual = true,
    hasHaptic = false,
    settings = listOf(
        EffectSetting.Slider("speed", "Rain Speed", 0.5f, 3f, 1f, 0.1f),
        EffectSetting.Slider("density", "Code Density", 0.3f, 1f, 0.6f, 0.1f),
        EffectSetting.ColorPicker("base_color", "Base Color", Color(0xFF00FF00)),
        EffectSetting.Toggle("glitch_mode", "Glitch Mode", false)
    )
) {
    // Matrix characters
    private val matrixChars = "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン0123456789"
    
    // Columns of falling characters
    private val columns = mutableListOf<MatrixColumn>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = 24f
        typeface = android.graphics.Typeface.MONOSPACE
    }
    
    data class MatrixColumn(
        var x: Float,
        var y: Float,
        var speed: Float,
        var chars: List<Char>,
        var brightness: Float
    )
    
    override fun onEnable() {
        initializeColumns()
    }
    
    override fun onDisable() {
        columns.clear()
    }
    
    override fun onIntensityChanged(intensity: Float) {
        // Adjust column count based on intensity
        val targetCount = (20 * intensity).toInt()
        while (columns.size < targetCount) {
            addRandomColumn()
        }
        while (columns.size > targetCount) {
            columns.removeLastOrNull()
        }
    }
    
    override fun onDraw(canvas: Canvas, width: Float, height: Float, deltaTime: Float) {
        val speed = getSetting("speed", 1f) * _currentIntensity
        val baseColor = getSetting("base_color", Color(0xFF00FF00))
        val glitchMode = getSetting("glitch_mode", false)
        
        // Semi-transparent black for trail effect
        canvas.drawColor(android.graphics.Color.argb(40, 0, 0, 0))
        
        columns.forEach { column ->
            // Update position
            column.y += column.speed * speed * 50 * deltaTime
            
            // Reset if off screen
            if (column.y - column.chars.size * 30 > height) {
                column.y = -column.chars.size * 30f
                column.chars = generateRandomChars((5 + Random.nextInt(10)))
            }
            
            // Draw characters
            column.chars.forEachIndexed { index, char ->
                val charY = column.y - index * 30
                if (charY in -30f..height) {
                    // Head of the column is brighter
                    val alpha = if (index == 0) 255 else (200 - index * 20).coerceIn(50, 200)
                    
                    // Glitch effect
                    val glitchOffset = if (glitchMode && Random.nextFloat() < 0.05f) {
                        (Random.nextFloat() - 0.5f) * 10
                    } else 0f
                    
                    paint.color = android.graphics.Color.argb(
                        alpha,
                        (baseColor.red * 255).toInt(),
                        (baseColor.green * 255).toInt(),
                        (baseColor.blue * 255).toInt()
                    )
                    
                    canvas.drawText(
                        char.toString(),
                        column.x + glitchOffset,
                        charY,
                        paint
                    )
                }
            }
        }
    }
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "matrix")
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing)
            ),
            label = "offset"
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw simplified matrix preview
            drawRect(Color.Black)
            
            repeat(5) { col ->
                val x = size.width * (0.1f + col * 0.2f)
                val chars = "10ア"
                chars.forEachIndexed { i, char ->
                    val y = (offset + i * 20 + col * 15) % size.height
                    drawCircle(
                        color = Color(0xFF00FF00).copy(alpha = 0.7f - i * 0.2f),
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
    
    private fun initializeColumns() {
        columns.clear()
        val count = (20 * _currentIntensity).toInt()
        repeat(count) {
            addRandomColumn()
        }
    }
    
    private fun addRandomColumn() {
        val colWidth = 1000f / 20  // Assume 1000px width, 20 columns
        columns.add(
            MatrixColumn(
                x = Random.nextFloat() * 1000,
                y = Random.nextFloat() * -500,
                speed = 0.5f + Random.nextFloat(),
                chars = generateRandomChars(5 + Random.nextInt(10)),
                brightness = 0.5f + Random.nextFloat() * 0.5f
            )
        )
    }
    
    private fun generateRandomChars(count: Int): List<Char> {
        return List(count) { matrixChars.random() }
    }
}

/**
 * Example 2: HEARTBEAT PULSE EFFECT
 * 
 * Rhythmic pulsing overlay with haptic feedback.
 * Demonstrates haptic integration and rhythmic animations.
 */
class HeartbeatPulseEffect : EffectPlugin(
    id = "heartbeat_pulse",
    name = "Heartbeat Pulse",
    description = "A calming heartbeat rhythm with gentle pulses and haptics",
    category = EffectCategory.ANIMATIONS,
    type = EffectType.OVERLAY,
    hasVisual = true,
    hasHaptic = true,
    defaultIntensity = 0.7f,
    settings = listOf(
        EffectSetting.Slider("bpm", "Beats Per Minute", 40f, 120f, 60f, 5f),
        EffectSetting.ColorPicker("pulse_color", "Pulse Color", Color(0xFFFF69B4)),
        EffectSetting.Toggle("haptic_enabled", "Enable Haptics", true),
        EffectSetting.Slider("pulse_size", "Pulse Size", 0.3f, 1f, 0.6f, 0.1f)
    )
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var phase = 0f
    private var lastBeatTime = 0L
    
    override fun onEnable() {
        phase = 0f
    }
    
    override fun onDisable() {}
    
    override fun onIntensityChanged(intensity: Float) {}
    
    override fun onUpdate(deltaTime: Float) {
        val bpm = getSetting("bpm", 60f)
        val beatDuration = 60f / bpm
        
        phase += deltaTime / beatDuration
        if (phase >= 1f) {
            phase = 0f
            performHeartbeat()
        }
    }
    
    override fun onDraw(canvas: Canvas, width: Float, height: Float, deltaTime: Float) {
        val pulseColor = getSetting("pulse_color", Color(0xFFFF69B4))
        val pulseSize = getSetting("pulse_size", 0.6f)
        
        // Heartbeat pattern: lub-dub
        val intensity1 = if (phase < 0.15f) {
            sin(phase / 0.15f * PI.toFloat() / 2)
        } else 0f
        
        val intensity2 = if (phase in 0.25f..0.4f) {
            sin((phase - 0.25f) / 0.15f * PI.toFloat() / 2)
        } else 0f
        
        val totalIntensity = (intensity1 + intensity2 * 0.7f) * _currentIntensity
        
        if (totalIntensity > 0.01f) {
            val alpha = (totalIntensity * 80).toInt().coerceIn(0, 100)
            paint.color = android.graphics.Color.argb(
                alpha,
                (pulseColor.red * 255).toInt(),
                (pulseColor.green * 255).toInt(),
                (pulseColor.blue * 255).toInt()
            )
            
            // Draw expanding circle from center
            val maxRadius = minOf(width, height) * 0.8f * pulseSize
            val radius = maxRadius * totalIntensity
            
            canvas.drawCircle(width / 2, height / 2, radius, paint)
        }
    }
    
    private fun performHeartbeat() {
        if (getSetting("haptic_enabled", true)) {
            // lub
            performHaptic()
            
            // dub (delayed)
            effectScope.launch {
                kotlinx.coroutines.delay(250)
                performHaptic()
            }
        }
    }
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = keyframes {
                    durationMillis = 1000
                    0f at 0
                    0.8f at 150
                    0f at 300
                    0.6f at 400
                    0f at 550
                    0f at 1000
                }
            ),
            label = "pulse"
        )
        
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = Color(0xFFFF69B4).copy(alpha = 0.3f * scale * intensity),
                    radius = size.minDimension * 0.4f * scale,
                    center = center
                )
            }
            
            Icon(
                Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFFF69B4).copy(alpha = 0.5f + scale * 0.5f),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

/**
 * Example 3: CONFETTI CELEBRATION EFFECT
 * 
 * Colorful confetti particles that burst from the screen.
 * Demonstrates particle system usage.
 */
class ConfettiEffect : EffectPlugin(
    id = "confetti_celebration",
    name = "Confetti Celebration",
    description = "Colorful confetti bursts for celebrations and achievements",
    category = EffectCategory.PARTICLES,
    type = EffectType.TRIGGERED,
    hasVisual = true,
    hasHaptic = true,
    defaultIntensity = 1.2f,
    settings = listOf(
        EffectSetting.Slider("burst_count", "Particles Per Burst", 20f, 200f, 50f, 10f),
        EffectSetting.Toggle("auto_burst", "Auto Burst", false),
        EffectSetting.Slider("burst_interval", "Burst Interval (sec)", 1f, 10f, 3f, 1f),
        EffectSetting.Toggle("gravity", "Enable Gravity", true)
    )
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val confettiColors = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFE66D),
        Color(0xFF95E1D3), Color(0xFFF38181), Color(0xFFAA96DA),
        Color(0xFFFFD93D), Color(0xFF6BCB77)
    )
    private var burstTimer = 0f
    private var autoBurstInterval = 3f
    
    data class ConfettiParticle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var rotation: Float,
        var rotationSpeed: Float,
        var color: Color,
        var size: Float,
        var life: Float
    ) {
        fun update(deltaTime: Float, gravity: Boolean) {
            x += vx * deltaTime * 100
            y += vy * deltaTime * 100
            rotation += rotationSpeed * deltaTime * 100
            
            if (gravity) {
                vy += 2f * deltaTime // Gravity
            }
            
            life -= deltaTime
        }
    }
    
    private val confettiParticles = mutableListOf<ConfettiParticle>()
    
    override fun onEnable() {
        // Initial burst
        burst()
    }
    
    override fun onDisable() {
        confettiParticles.clear()
    }
    
    override fun onIntensityChanged(intensity: Float) {}
    
    override fun onUpdate(deltaTime: Float) {
        val gravity = getSetting("gravity", true)
        
        // Auto burst
        if (getSetting("auto_burst", false)) {
            burstTimer += deltaTime
            autoBurstInterval = getSetting("burst_interval", 3f)
            if (burstTimer >= autoBurstInterval) {
                burstTimer = 0f
                burst()
            }
        }
        
        // Update particles
        confettiParticles.removeAll { particle ->
            particle.update(deltaTime, gravity)
            particle.life <= 0f || particle.y > 2000 // Remove if off screen
        }
    }
    
    override fun onDraw(canvas: Canvas, width: Float, height: Float, deltaTime: Float) {
        confettiParticles.forEach { particle ->
            val androidColor = android.graphics.Color.argb(
                (particle.life * 255).toInt().coerceIn(0, 255),
                (particle.color.red * 255).toInt(),
                (particle.color.green * 255).toInt(),
                (particle.color.blue * 255).toInt()
            )
            
            paint.color = androidColor
            
            canvas.save()
            canvas.rotate(particle.rotation, particle.x, particle.y)
            
            // Draw confetti as small rectangles
            canvas.drawRect(
                particle.x - particle.size / 2,
                particle.y - particle.size / 4,
                particle.x + particle.size / 2,
                particle.y + particle.size / 4,
                paint
            )
            
            canvas.restore()
        }
    }
    
    fun burst(atX: Float = 500f, atY: Float = 500f) {
        val count = (getSetting("burst_count", 50f) * _currentIntensity).toInt()
        
        repeat(count) {
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val speed = 3f + Random.nextFloat() * 7f
            
            confettiParticles.add(
                ConfettiParticle(
                    x = atX,
                    y = atY,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed - 5f, // Initial upward velocity
                    rotation = Random.nextFloat() * 360f,
                    rotationSpeed = (Random.nextFloat() - 0.5f) * 10f,
                    color = confettiColors.random(),
                    size = 10f + Random.nextFloat() * 20f,
                    life = 2f + Random.nextFloat() * 2f
                )
            )
        }
        
        // Haptic feedback
        vibratePattern(longArrayOf(0, 50, 50, 50))
    }
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "confetti")
        val progress by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, easing = LinearEasing)
            ),
            label = "progress"
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            repeat(20) { i ->
                val startOffset = i * 0.05f
                if (progress >= startOffset && progress <= startOffset + 0.5f) {
                    val localProgress = (progress - startOffset) / 0.5f
                    val x = size.width * (0.2f + (i % 5) * 0.15f)
                    val y = size.height * 0.3f + localProgress * size.height * 0.5f
                    
                    drawRect(
                        color = confettiColors[i % confettiColors.size].copy(
                            alpha = (1f - localProgress) * intensity
                        ),
                        topLeft = Offset(x, y),
                        size = Size(12f, 8f)
                    )
                }
            }
        }
    }
}

/**
 * Example 4: NORTHERN LIGHTS EFFECT
 * 
 * Flowing aurora borealis effect with smooth gradients.
 * Demonstrates advanced canvas drawing with gradients.
 */
class NorthernLightsEffect : EffectPlugin(
    id = "northern_lights",
    name = "Northern Lights",
    description = "Mesmerizing aurora borealis flowing across your screen",
    category = EffectCategory.VISUAL_OVERLAY,
    type = EffectType.BACKGROUND,
    hasVisual = true,
    defaultIntensity = 0.9f,
    settings = listOf(
        EffectSetting.Slider("flow_speed", "Flow Speed", 0.2f, 2f, 0.8f, 0.1f),
        EffectSetting.Slider("wave_height", "Wave Height", 50f, 300f, 150f, 25f),
        EffectSetting.Toggle("stars", "Show Stars", true)
    )
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val stars = mutableListOf<Star>()
    
    data class Star(
        var x: Float,
        var y: Float,
        var size: Float,
        var twinklePhase: Float
    )
    
    override fun onEnable() {
        // Generate stars
        stars.clear()
        repeat(100) {
            stars.add(
                Star(
                    x = Random.nextFloat() * 2000,
                    y = Random.nextFloat() * 1000,
                    size = 1f + Random.nextFloat() * 2f,
                    twinklePhase = Random.nextFloat() * PI.toFloat() * 2
                )
            )
        }
    }
    
    override fun onDisable() {}
    
    override fun onIntensityChanged(intensity: Float) {}
    
    override fun onDraw(canvas: Canvas, width: Float, height: Float, deltaTime: Float) {
        val flowSpeed = getSetting("flow_speed", 0.8f) * _currentIntensity
        val waveHeight = getSetting("wave_height", 150f) * _currentIntensity
        val showStars = getSetting("stars", true)
        
        // Clear with dark night sky
        canvas.drawColor(android.graphics.Color.argb(255, 5, 5, 20))
        
        // Draw stars
        if (showStars) {
            stars.forEach { star ->
                star.twinklePhase += deltaTime
                val twinkle = (sin(star.twinklePhase * 2) + 1f) / 2f
                val alpha = (50 + twinkle * 200).toInt()
                
                paint.color = android.graphics.Color.argb(alpha, 255, 255, 255)
                canvas.drawCircle(star.x, star.y, star.size, paint)
            }
        }
        
        // Draw aurora waves
        val colors = listOf(
            Triple(0, 255, 128),   // Green
            Triple(0, 200, 255),   // Cyan
            Triple(128, 0, 255),   // Purple
            Triple(255, 0, 128)    // Pink
        )
        
        colors.forEachIndexed { index, (r, g, b) ->
            val path = android.graphics.Path()
            val yOffset = height * 0.3f + index * 100
            
            path.moveTo(0f, height)
            
            for (x in 0..width.toInt() step 10) {
                val wave1 = sin((x * 0.003f + animationTime * flowSpeed + index) * PI.toFloat())
                val wave2 = sin((x * 0.007f + animationTime * flowSpeed * 0.5f) * PI.toFloat())
                val y = yOffset + (wave1 + wave2) * waveHeight
                
                if (x == 0) {
                    path.moveTo(x.toFloat(), y)
                } else {
                    path.lineTo(x.toFloat(), y)
                }
            }
            
            path.lineTo(width, height)
            path.close()
            
            val alpha = (40 + index * 15).coerceIn(20, 80)
            paint.color = android.graphics.Color.argb(alpha, r, g, b)
            canvas.drawPath(path, paint)
        }
    }
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "aurora")
        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 2 * PI.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(5000, easing = LinearEasing)
            ),
            label = "phase"
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Dark background
            drawRect(Color(0xFF050514))
            
            // Aurora waves
            val waveColors = listOf(
                Color(0xFF00FF80),
                Color(0xFF00C8FF),
                Color(0xFF8000FF)
            )
            
            waveColors.forEachIndexed { index, color ->
                val path = androidx.compose.ui.graphics.Path()
                val yOffset = size.height * 0.3f + index * 30
                
                for (x in 0..size.width.toInt() step 20) {
                    val wave1 = sin((x * 0.01f + phase + index) * 0.5f)
                    val wave2 = sin((x * 0.02f + phase * 0.7f) * 0.5f)
                    val y = yOffset + (wave1 + wave2) * 30f * intensity
                    
                    if (x == 0) {
                        path.moveTo(x.toFloat(), y)
                    } else {
                        path.lineTo(x.toFloat(), y)
                    }
                }
                
                path.lineTo(size.width, size.height)
                path.lineTo(0f, size.height)
                path.close()
                
                drawPath(
                    path = path,
                    color = color.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * ============================================
 * PLUGIN MANIFEST EXAMPLE
 * ============================================
 * 
 * This is how you would define the plugin manifest for packaging.
 */
object SampleEffectPluginManifest {
    
    fun createManifest(): PluginManifest {
        return PluginManifest(
            id = "com.example.awesomeeffects",
            name = "Awesome Effects Pack",
            description = "A collection of amazing visual effects for SugarMunch",
            version = "1.0.0",
            author = "Example Developer",
            authorId = "example_dev",
            category = PluginCategory.EFFECTS,
            minApiLevel = 26,
            targetApiLevel = 34,
            minSugarMunchVersion = "1.0.0",
            maxSugarMunchVersion = null,
            permissions = setOf(
                PluginPermission.OVERLAY,
                PluginPermission.NOTIFICATIONS
            ),
            dependencies = emptyList(),
            tags = listOf("visual", "particles", "effects", "animation"),
            autoEnable = false,
            entryPoint = "com.example.awesomeeffects.AwesomeEffectsPlugin",
            resources = com.sugarmunch.app.plugin.model.PluginResources(
                icon = "icon.png",
                screenshots = listOf("screenshot1.jpg", "screenshot2.jpg"),
                banner = "banner.jpg"
            ),
            settings = emptyList()
        )
    }
}

/**
 * ============================================
 * HOW TO CREATE YOUR OWN EFFECT PLUGIN
 * ============================================
 * 
 * 1. Create a new class extending EffectPlugin
 * 2. Define your effect's metadata in the constructor
 * 3. Implement required methods: onEnable, onDisable, onIntensityChanged
 * 4. Override onDraw() for custom canvas drawing
 * 5. Override onUpdate() for per-frame logic
 * 6. Add a Preview() composable for the UI
 * 7. Package with a manifest.json file
 * 
 * For more examples and documentation, visit:
 * https://docs.sugarmunch.app/plugin-development
 */
