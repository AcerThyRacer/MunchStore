package com.sugarmunch.app.effects.v2.presets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.v2.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ═════════════════════════════════════════════════════════════════
// CANDY CONFETTI
// ═════════════════════════════════════════════════════════════════

class CandyConfettiEffect : EffectV2 {
    override val id = "candy_confetti"
    override val name = "Candy Confetti"
    override val description = "Colorful candy pieces floating around"
    override val category = EffectCategory.PARTICLES
    override val type = EffectType.OVERLAY
    override val icon = "🎊"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 1f
    
    override val hasVisual = true
    override val hasHaptic = false
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("count", "Particle Count", 20f, 150f, 50f, 10f),
        EffectSetting.Slider("speed", "Speed", 0.5f, 3f, 1f, 0.1f)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 1f
    
    private val colors = listOf(
        0xFFFFB6C1.toInt(), 0xFF98FF98.toInt(), 0xFFFFFACD.toInt(),
        0xFFDEB887.toInt(), 0xFFB5DEFF.toInt(), 0xFFE6B3FF.toInt()
    )
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val particleCount = (50 * intensity).toInt().coerceIn(20, 150)
        
        val view = object : View(context) {
            private var particles: List<ConfettiParticle> = emptyList()
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            private var time = 0f
            
            init {
                generateParticles(particleCount)
            }
            
            private fun generateParticles(count: Int) {
                particles = List(count) {
                    ConfettiParticle(
                        x = Random.nextFloat() * 1000,
                        y = Random.nextFloat() * 2000,
                        size = 4f + Random.nextFloat() * 8f * intensity,
                        color = colors.random(),
                        speedY = 1f + Random.nextFloat() * 2f * intensity,
                        speedX = (Random.nextFloat() - 0.5f) * 2f,
                        rotation = Random.nextFloat() * 360f,
                        rotationSpeed = (Random.nextFloat() - 0.5f) * 10f
                    )
                }
            }
            
            override fun onDraw(canvas: Canvas) {
                if (width == 0 || height == 0) return
                
                time += 0.016f
                
                particles.forEach { particle ->
                    particle.y += particle.speedY * intensity
                    particle.x += particle.speedX * sin(time + particle.y * 0.01f)
                    particle.rotation += particle.rotationSpeed
                    
                    // Wrap around
                    if (particle.y > height) particle.y = -50f
                    if (particle.x > width) particle.x = 0f
                    if (particle.x < 0) particle.x = width.toFloat()
                    
                    paint.color = particle.color
                    paint.alpha = (150 * intensity).toInt().coerceIn(30, 255)
                    
                    canvas.save()
                    canvas.rotate(particle.rotation, particle.x, particle.y)
                    canvas.drawRect(
                        particle.x - particle.size / 2,
                        particle.y - particle.size / 2,
                        particle.x + particle.size / 2,
                        particle.y + particle.size / 2,
                        paint
                    )
                    canvas.restore()
                }
                
                postInvalidateDelayed((50 / intensity).toLong().coerceAtLeast(16))
            }
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else 
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(view, params)
        overlayView = view
    }
    
    override fun disable() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        windowManager = null
    }
    
    override fun isActive() = overlayView != null
    override fun setIntensity(intensity: Float) { currentIntensity = intensity }
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val particleCount = (8 * intensity).toInt().coerceIn(3, 15)
                repeat(particleCount) {
                    val x = Random.nextFloat() * size.width
                    val y = Random.nextFloat() * size.height
                    val particleSize = 3f + Random.nextFloat() * 5f * intensity
                    drawCircle(
                        color = Color(colors.random()).copy(alpha = 0.6f),
                        radius = particleSize,
                        center = Offset(x, y)
                    )
                }
            }
            Text(icon)
        }
    }
    
    private data class ConfettiParticle(
        var x: Float,
        var y: Float,
        val size: Float,
        val color: Int,
        val speedY: Float,
        val speedX: Float,
        var rotation: Float,
        val rotationSpeed: Float
    )
}

// ═════════════════════════════════════════════════════════════════
// CHOCOLATE RAIN
// ═════════════════════════════════════════════════════════════════

class ChocolateRainEffect : EffectV2 {
    override val id = "chocolate_rain"
    override val name = "Chocolate Rain"
    override val description = "Cocoa particles falling from above"
    override val category = EffectCategory.PARTICLES
    override val type = EffectType.OVERLAY
    override val icon = "🍫🌧️"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 0.8f
    
    override val hasVisual = true
    override val hasHaptic = false
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("density", "Density", 20f, 100f, 40f, 10f)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 0.8f
    
    private val brownColors = listOf(
        0xFF3C281E.toInt(), 0xFF5D4037.toInt(), 
        0xFF8D6E63.toInt(), 0xFFA1887F.toInt()
    )
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val view = object : View(context) {
            private var drops: List<ChocolateDrop> = emptyList()
            private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            
            init {
                val count = (40 * intensity).toInt().coerceIn(20, 100)
                drops = List(count) {
                    ChocolateDrop(
                        x = Random.nextFloat() * 1000,
                        y = Random.nextFloat() * 2000,
                        length = 10f + Random.nextFloat() * 20f * intensity,
                        speed = 5f + Random.nextFloat() * 10f * intensity,
                        thickness = 2f + Random.nextFloat() * 3f,
                        color = brownColors.random()
                    )
                }
            }
            
            override fun onDraw(canvas: Canvas) {
                if (width == 0 || height == 0) return
                
                drops.forEach { drop ->
                    drop.y += drop.speed
                    if (drop.y > height) {
                        drop.y = -drop.length
                        drop.x = Random.nextFloat() * width
                    }
                    
                    paint.color = drop.color
                    paint.alpha = (180 * intensity).toInt().coerceIn(50, 255)
                    paint.strokeWidth = drop.thickness
                    
                    canvas.drawLine(drop.x, drop.y, drop.x, drop.y + drop.length, paint)
                }
                
                postInvalidateDelayed((30 / intensity).toLong().coerceAtLeast(16))
            }
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else 
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(view, params)
        overlayView = view
    }
    
    override fun disable() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        windowManager = null
    }
    
    override fun isActive() = overlayView != null
    override fun setIntensity(intensity: Float) { currentIntensity = intensity }
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF3C281E)),
            contentAlignment = Alignment.Center
        ) {
            val dropCount = (5 * intensity).toInt().coerceIn(2, 10)
            Canvas(modifier = Modifier.fillMaxSize()) {
                repeat(dropCount) {
                    val x = Random.nextFloat() * size.width
                    val y = Random.nextFloat() * size.height
                    drawLine(
                        color = Color(0xFF8D6E63).copy(alpha = 0.7f),
                        start = Offset(x, y),
                        end = Offset(x, y + 8.dp.toPx() * intensity),
                        strokeWidth = 2f
                    )
                }
            }
            Text("🍫")
        }
    }
    
    private data class ChocolateDrop(
        var x: Float,
        var y: Float,
        val length: Float,
        val speed: Float,
        val thickness: Float,
        val color: Int
    )
}

// ═════════════════════════════════════════════════════════════════
// GUMMY WIGGLE
// ═════════════════════════════════════════════════════════════════

class GummyWiggleEffect : EffectV2 {
    override val id = "gummy_wiggle"
    override val name = "Gummy Wiggle"
    override val description = "Screen subtly wobbles like gummy candy"
    override val category = EffectCategory.PARTICLES
    override val type = EffectType.OVERLAY
    override val icon = "🍬"
    
    override val minIntensity = 0.2f
    override val maxIntensity = 1.5f
    override val defaultIntensity = 0.6f
    
    override val hasVisual = true
    override val hasHaptic = true
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("wobble_amount", "Wobble", 0.5f, 2f, 1f, 0.1f)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 0.6f
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        // Gummy wiggle would use SurfaceFlinger or shader effects
        // For now, subtle overlay
        val view = View(context).apply {
            alpha = 0f
        }
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else 
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(view, params)
        overlayView = view
    }
    
    override fun disable() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        windowManager = null
    }
    
    override fun isActive() = overlayView != null
    override fun setIntensity(intensity: Float) { currentIntensity = intensity }
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF69B4).copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// POP ROCKS
// ═════════════════════════════════════════════════════════════════

class PopRocksEffect : EffectV2 {
    override val id = "pop_rocks"
    override val name = "Pop Rocks"
    override val description = "Screen sparkles on touch and randomly"
    override val category = EffectCategory.PARTICLES
    override val type = EffectType.INTERACTIVE
    override val icon = "✨"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 1f
    
    override val hasVisual = true
    override val hasHaptic = true
    override val hasAudio = true
    
    override val settings = listOf(
        EffectSetting.Slider("frequency", "Pop Frequency", 0.5f, 3f, 1f, 0.1f),
        EffectSetting.Toggle("sound_enabled", "Sound", true)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 1f
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val view = View(context)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else 
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(view, params)
        overlayView = view
        
        // Auto-pop timer
        startAutoPop(intensity)
    }
    
    private fun startAutoPop(intensity: Float) {
        // Implementation would schedule random pop animations
    }
    
    override fun disable() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        windowManager = null
    }
    
    override fun isActive() = overlayView != null
    override fun setIntensity(intensity: Float) { currentIntensity = intensity }
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFD700).copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            val sparkles = (5 * intensity).toInt().coerceIn(2, 12)
            Canvas(modifier = Modifier.fillMaxSize()) {
                repeat(sparkles) {
                    val x = Random.nextFloat() * size.width
                    val y = Random.nextFloat() * size.height
                    drawCircle(
                        color = Color.Yellow.copy(alpha = 0.8f),
                        radius = 2f + Random.nextFloat() * 3f,
                        center = Offset(x, y)
                    )
                }
            }
            Text(icon)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// CANDY FIREWORKS
// ═════════════════════════════════════════════════════════════════

class CandyFireworksEffect : EffectV2 {
    override val id = "candy_fireworks"
    override val name = "Candy Fireworks"
    override val description = "Explosive particle bursts periodically"
    override val category = EffectCategory.PARTICLES
    override val type = EffectType.BACKGROUND
    override val icon = "🎆"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 1f
    
    override val hasVisual = true
    override val hasHaptic = true
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("frequency", "Burst Frequency", 0.3f, 2f, 1f, 0.1f),
        EffectSetting.Slider("size", "Explosion Size", 0.5f, 2f, 1f, 0.1f)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 1f
    
    private val candyColors = listOf(
        0xFFFFB6C1, 0xFF98FF98, 0xFFFFFACD,
        0xFFDEB887, 0xFFB5DEFF, 0xFFE6B3FF
    )
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val view = View(context)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else 
                @Suppress("DEPRECATION") WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )
        
        windowManager.addView(view, params)
        overlayView = view
    }
    
    override fun disable() {
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        windowManager = null
    }
    
    override fun isActive() = overlayView != null
    override fun setIntensity(intensity: Float) { currentIntensity = intensity }
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF1A1A2E)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Draw burst
                val center = Offset(size.width / 2, size.height / 2)
                val particleCount = (12 * intensity).toInt().coerceIn(6, 24)
                repeat(particleCount) { i ->
                    val angle = (i / particleCount.toFloat()) * 2f * PI.toFloat()
                    val distance = 15f + 10f * intensity
                    val x = center.x + cos(angle) * distance
                    val y = center.y + sin(angle) * distance
                    drawCircle(
                        color = Color(candyColors.random()).copy(alpha = 0.9f),
                        radius = 3f + 2f * intensity,
                        center = Offset(x, y)
                    )
                }
            }
            Text("🎆")
        }
    }
}
