package com.sugarmunch.app.plugin

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.v2.model.EffectCategory
import com.sugarmunch.app.effects.v2.model.EffectSetting
import com.sugarmunch.app.effects.v2.model.EffectType
import kotlinx.coroutines.*
import kotlin.math.*
import kotlin.random.Random

/**
 * SugarMunch Effect Plugin - Base class for custom effect plugins
 * 
 * Plugin developers extend this class to create custom visual effects,
 * animations, haptic feedback patterns, and audio effects.
 * 
 * Example:
 * ```kotlin
 * class RainbowWaveEffect : EffectPlugin(
 *     id = "rainbow_wave",
 *     name = "Rainbow Wave",
 *     description = "A flowing rainbow wave across the screen"
 * ) {
 *     override fun onDraw(canvas: Canvas, width: Float, height: Float, deltaTime: Float) {
 *         // Custom drawing code
 *     }
 * }
 * ```
 */
abstract class EffectPlugin(
    override val id: String,
    override val name: String,
    override val description: String,
    override val category: EffectCategory = EffectCategory.VISUAL_OVERLAY,
    override val type: EffectType = EffectType.OVERLAY,
    override val minIntensity: Float = 0.2f,
    override val maxIntensity: Float = 2.0f,
    override val defaultIntensity: Float = 1.0f,
    override val hasVisual: Boolean = true,
    override val hasHaptic: Boolean = false,
    override val hasAudio: Boolean = false,
    override val settings: List<EffectSetting> = emptyList()
) : com.sugarmunch.app.effects.v2.model.EffectV2 {
    
    // Effect state
    private var _isActive = false
    private var _currentIntensity = defaultIntensity
    private var _settings: Map<String, Any> = emptyMap()
    
    // Android context and window manager
    protected lateinit var context: Context
    protected lateinit var windowManager: WindowManager
    
    // Effect scope for coroutines
    protected val effectScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    // Canvas overlay view (for custom drawing)
    private var overlayView: EffectOverlayView? = null
    
    // Animation state
    protected var animationTime = 0f
    protected var frameCount = 0
    
    // Particle system (if using built-in particles)
    protected val particles = mutableListOf<Particle>()
    
    // Shader program (if using custom shaders)
    protected var shaderProgram: ShaderProgram? = null
    
    // Haptic feedback
    protected val vibrator by lazy {
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    // ========== LIFECYCLE METHODS ==========
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.context = context
        this.windowManager = windowManager
        _currentIntensity = intensity.coerceIn(minIntensity, maxIntensity)
        _isActive = true
        
        // Call plugin-specific enable logic
        onEnable()
        
        // Start animation loop if visual
        if (hasVisual) {
            startVisualEffect()
        }
        
        // Start haptic feedback if enabled
        if (hasHaptic) {
            startHapticEffect()
        }
        
        // Start audio if enabled
        if (hasAudio) {
            startAudioEffect()
        }
    }
    
    override fun disable() {
        _isActive = false
        
        // Stop all effects
        stopVisualEffect()
        stopHapticEffect()
        stopAudioEffect()
        
        // Cancel coroutines
        effectScope.cancel()
        
        // Call plugin-specific disable logic
        onDisable()
    }
    
    override fun isActive(): Boolean = _isActive
    
    override fun setIntensity(intensity: Float) {
        _currentIntensity = intensity.coerceIn(minIntensity, maxIntensity)
        onIntensityChanged(_currentIntensity)
    }
    
    override fun getCurrentIntensity(): Float = _currentIntensity
    
    // ========== ABSTRACT METHODS (TO IMPLEMENT) ==========
    
    /**
     * Called when the effect is enabled
     * Override to perform initialization
     */
    abstract fun onEnable()
    
    /**
     * Called when the effect is disabled
     * Override to clean up resources
     */
    abstract fun onDisable()
    
    /**
     * Called when intensity changes
     * @param intensity The new intensity value (0.0 - 2.0)
     */
    abstract fun onIntensityChanged(intensity: Float)
    
    // ========== OPTIONAL OVERRIDE METHODS ==========
    
    /**
     * Custom drawing on Canvas
     * Override this to draw custom visuals
     * @param canvas The canvas to draw on
     * @param width Canvas width
     * @param height Canvas height
     * @param deltaTime Time since last frame in seconds
     */
    open fun onDraw(canvas: Canvas, width: Float, height: Float, deltaTime: Float) {
        // Default: no drawing
    }
    
    /**
     * Compose-based preview
     * Override to provide a custom preview
     */
    @Composable
    override fun Preview(intensity: Float) {
        DefaultPreview(intensity)
    }
    
    /**
     * Haptic pattern
     * Override to provide custom haptic feedback
     */
    open fun getHapticPattern(): VibrationEffect? {
        return VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
    }
    
    /**
     * Called on each animation frame
     * @param deltaTime Time since last frame in seconds
     */
    open fun onUpdate(deltaTime: Float) {
        // Default: no update logic
    }
    
    // ========== PROTECTED METHODS ==========
    
    /**
     * Trigger a haptic feedback
     */
    protected fun performHaptic(feedbackConstant: Int = HapticFeedbackConstants.LONG_PRESS) {
        if (hasHaptic && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    }
    
    /**
     * Create a custom vibration pattern
     */
    protected fun vibratePattern(timings: LongArray, amplitudes: IntArray? = null) {
        if (hasHaptic && vibrator.hasVibrator()) {
            val effect = if (amplitudes != null) {
                VibrationEffect.createWaveform(timings, amplitudes, -1)
            } else {
                VibrationEffect.createWaveform(timings, -1)
            }
            vibrator.vibrate(effect)
        }
    }
    
    /**
     * Spawn a particle
     */
    protected fun spawnParticle(particle: Particle) {
        particles.add(particle)
    }
    
    /**
     * Update all particles
     */
    protected fun updateParticles(deltaTime: Float) {
        particles.removeAll { particle ->
            particle.update(deltaTime)
            particle.life <= 0f
        }
    }
    
    /**
     * Draw all particles
     */
    protected fun drawParticles(canvas: Canvas, paint: Paint) {
        particles.forEach { it.draw(canvas, paint) }
    }
    
    /**
     * Get a setting value
     */
    protected fun <T> getSetting(key: String, defaultValue: T): T {
        @Suppress("UNCHECKED_CAST")
        return _settings[key] as? T ?: defaultValue
    }
    
    /**
     * Update settings
     */
    protected fun updateSettings(settings: Map<String, Any>) {
        _settings = settings
        onSettingsChanged(settings)
    }
    
    /**
     * Called when settings change
     */
    open fun onSettingsChanged(settings: Map<String, Any>) {
        // Override to handle setting changes
    }
    
    // ========== PRIVATE METHODS ==========
    
    private fun startVisualEffect() {
        // Start animation loop
        effectScope.launch {
            var lastTime = System.nanoTime()
            while (_isActive) {
                val currentTime = System.nanoTime()
                val deltaTime = (currentTime - lastTime) / 1_000_000_000f
                lastTime = currentTime
                
                animationTime += deltaTime
                frameCount++
                
                onUpdate(deltaTime)
                overlayView?.invalidate()
                
                delay(16) // ~60 FPS
            }
        }
    }
    
    private fun stopVisualEffect() {
        overlayView?.let { view ->
            try {
                windowManager.removeView(view)
            } catch (e: Exception) {
                // View might already be removed
            }
        }
        overlayView = null
    }
    
    private fun startHapticEffect() {
        effectScope.launch {
            while (_isActive) {
                getHapticPattern()?.let { vibrator.vibrate(it) }
                delay(1000) // Default interval
            }
        }
    }
    
    private fun stopHapticEffect() {
        if (hasHaptic) {
            vibrator.cancel()
        }
    }
    
    private fun startAudioEffect() {
        // Audio implementation depends on plugin
    }
    
    private fun stopAudioEffect() {
        // Audio cleanup
    }
    
    @Composable
    private fun DefaultPreview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "preview")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
        
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Default preview drawing
                drawCircle(
                    color = Color.Cyan,
                    radius = 20.dp.toPx() * scale * intensity,
                    center = center
                )
            }
        }
    }
}

/**
 * Particle data class for particle effects
 */
data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float = 1f,
    var size: Float = 10f,
    var color: Int = android.graphics.Color.CYAN,
    var alpha: Float = 1f,
    val decay: Float = 0.01f,
    val gravity: Float = 0f
) {
    fun update(deltaTime: Float) {
        x += vx * deltaTime
        y += vy * deltaTime
        vy += gravity * deltaTime
        life -= decay * deltaTime
        alpha = life.coerceIn(0f, 1f)
    }
    
    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        paint.alpha = (alpha * 255).toInt()
        canvas.drawCircle(x, y, size, paint)
    }
}

/**
 * Custom overlay view for effect drawing
 */
private class EffectOverlayView(
    context: Context,
    private val effect: EffectPlugin
) : View(context) {
    
    private var lastFrameTime = System.nanoTime()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastFrameTime) / 1_000_000_000f
        lastFrameTime = currentTime
        
        effect.onDraw(canvas, width.toFloat(), height.toFloat(), deltaTime)
    }
}

/**
 * Shader program placeholder for custom shaders
 */
class ShaderProgram {
    // Implementation would include OpenGL/AGSL shader support
    fun use() {}
    fun setUniform(name: String, value: Float) {}
    fun setUniform(name: String, value: FloatArray) {}
}

// ========== EXAMPLE EFFECT IMPLEMENTATIONS ==========

/**
 * Custom particle effect example - Floating sparkles
 */
class CustomParticleEffect : EffectPlugin(
    id = "floating_sparkles",
    name = "Floating Sparkles",
    description = "Magical sparkles that float across the screen",
    category = EffectCategory.PARTICLES,
    hasVisual = true,
    defaultIntensity = 0.8f
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var spawnTimer = 0f
    private val spawnRate = 0.1f
    
    override fun onEnable() {
        // Initialize
    }
    
    override fun onDisable() {
        particles.clear()
    }
    
    override fun onIntensityChanged(intensity: Float) {
        // Adjust particle count based on intensity
    }
    
    override fun onUpdate(deltaTime: Float) {
        spawnTimer += deltaTime
        
        // Spawn new particles
        if (spawnTimer > spawnRate / _currentIntensity) {
            spawnTimer = 0f
            spawnParticle(createSparkle())
        }
        
        updateParticles(deltaTime)
    }
    
    override fun onDraw(canvas: Canvas, width: Float, height: Float, deltaTime: Float) {
        drawParticles(canvas, paint)
    }
    
    private fun createSparkle(): Particle {
        return Particle(
            x = Random.nextFloat() * 1000,
            y = 1200f,
            vx = (Random.nextFloat() - 0.5f) * 50,
            vy = -Random.nextFloat() * 100 - 50,
            life = 3f,
            size = 5f + Random.nextFloat() * 10,
            color = listOf(
                android.graphics.Color.YELLOW,
                android.graphics.Color.CYAN,
                android.graphics.Color.MAGENTA,
                android.graphics.Color.WHITE
            ).random(),
            decay = 0.2f + Random.nextFloat() * 0.3f
        )
    }
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing)
            ),
            label = "offset"
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw sparkle preview
            repeat(5) { i ->
                val x = size.width * (0.2f + i * 0.15f)
                val y = size.height * (0.3f + (offset + i * 0.2f) % 1f * 0.4f)
                
                drawCircle(
                    color = Color.Yellow.copy(alpha = 0.7f),
                    radius = (5f + i * 2f) * intensity,
                    center = Offset(x, y)
                )
            }
        }
    }
}

/**
 * Custom shader effect example - Rainbow wave
 */
class CustomShaderEffect : EffectPlugin(
    id = "rainbow_wave",
    name = "Rainbow Wave",
    description = "Flowing rainbow waves across the screen",
    category = EffectCategory.VISUAL_OVERLAY,
    hasVisual = true,
    defaultIntensity = 1f,
    settings = listOf(
        EffectSetting.Slider("speed", "Wave Speed", 0.5f, 3f, 1f, 0.1f),
        EffectSetting.Slider("amplitude", "Wave Height", 10f, 100f, 50f, 5f),
        EffectSetting.ColorPicker("base_color", "Base Color", Color.White)
    )
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    override fun onEnable() {}
    override fun onDisable() {}
    
    override fun onIntensityChanged(intensity: Float) {}
    
    override fun onDraw(canvas: Canvas, width: Float, height: Float, deltaTime: Float) {
        val speed = getSetting("speed", 1f) * _currentIntensity
        val amplitude = getSetting("amplitude", 50f) * _currentIntensity
        
        // Draw rainbow wave
        for (i in 0..10) {
            val hue = ((animationTime * 60 * speed + i * 36) % 360).toFloat()
            paint.color = android.graphics.Color.HSVToColor(
                (50 * _currentIntensity).toInt().coerceIn(10, 100),
                floatArrayOf(hue, 1f, 1f)
            )
            
            val path = android.graphics.Path()
            path.moveTo(0f, height / 2)
            
            for (x in 0..width.toInt() step 20) {
                val y = height / 2 + 
                    sin((x + animationTime * 100 * speed) * 0.01f + i * 0.5f) * amplitude
                path.lineTo(x.toFloat(), y)
            }
            
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()
            
            canvas.drawPath(path, paint)
        }
    }
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "wave")
        val phase by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing)
            ),
            label = "phase"
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            for (i in 0..3) {
                val hue = (i * 90).toFloat()
                val color = androidx.compose.ui.graphics.Color.hsv(hue, 1f, 1f, 0.5f)
                
                val points = (0..10).map { x ->
                    val xPos = size.width * x / 10
                    val yPos = size.height / 2 + 
                        sin(phase + x * 0.5f + i * 0.5f) * 20f * intensity
                    Offset(xPos, yPos)
                }
                
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height)
                        points.forEach { lineTo(it.x, it.y) }
                        lineTo(size.width, size.height)
                        close()
                    },
                    color = color
                )
            }
        }
    }
}

/**
 * Custom pulse effect - Rhythmic pulsing overlay
 */
class CustomPulseEffect : EffectPlugin(
    id = "rhythm_pulse",
    name = "Rhythm Pulse",
    description = "Pulses to a rhythmic beat",
    category = EffectCategory.VISUAL_OVERLAY,
    hasVisual = true,
    hasHaptic = true,
    defaultIntensity = 1f
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var pulsePhase = 0f
    
    override fun onEnable() {
        startPulsePattern()
    }
    
    override fun onDisable() {}
    
    override fun onIntensityChanged(intensity: Float) {}
    
    override fun onUpdate(deltaTime: Float) {
        pulsePhase += deltaTime * 2f // 2 Hz pulse
        if (pulsePhase > 1f) {
            pulsePhase = 0f
            performHaptic()
        }
    }
    
    override fun onDraw(canvas: Canvas, width: Float, height: Float, deltaTime: Float) {
        val pulseScale = 0.5f + 0.5f * sin(pulsePhase * PI.toFloat() * 2)
        val alpha = (pulseScale * 50 * _currentIntensity).toInt().coerceIn(0, 100)
        
        paint.color = android.graphics.Color.argb(
            alpha,
            255,
            (100 + 155 * pulseScale).toInt(),
            200
        )
        
        canvas.drawRect(0f, 0f, width, height, paint)
    }
    
    private fun startPulsePattern() {
        vibratePattern(longArrayOf(0, 100, 400, 100), intArrayOf(0, 100, 0, 100))
    }
    
    override fun getHapticPattern(): VibrationEffect? {
        return VibrationEffect.createWaveform(
            longArrayOf(0, 100, 400, 100),
            intArrayOf(0, 100, 0, 100),
            0
        )
    }
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                color = Color.Cyan.copy(alpha = scale * 0.3f * intensity),
                size = size
            )
        }
    }
}
