package com.sugarmunch.app.effects.v2.presets

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.v2.model.*
import kotlinx.coroutines.*

// ═════════════════════════════════════════════════════════════════
// UNICORN SWIRL
// ═════════════════════════════════════════════════════════════════

class UnicornSwirlEffect : EffectV2 {
    override val id = "unicorn_swirl"
    override val name = "Unicorn Swirl"
    override val description = "Animated gradient mesh background"
    override val category = EffectCategory.ANIMATIONS
    override val type = EffectType.BACKGROUND
    override val icon = "🦄"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 1f
    
    override val hasVisual = true
    override val hasHaptic = false
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("speed", "Animation Speed", 0.3f, 2f, 1f, 0.1f),
        EffectSetting.Slider("complexity", "Mesh Complexity", 3f, 8f, 5f, 1f)
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
        val infiniteTransition = rememberInfiniteTransition(label = "swirl")
        val angle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween((10000 / intensity).toInt(), easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotation"
        )
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFF0080).copy(alpha = 0.3f),
                            Color(0xFF00FF80).copy(alpha = 0.3f),
                            Color(0xFF8000FF).copy(alpha = 0.3f),
                            Color(0xFFFF0080).copy(alpha = 0.3f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                rotate(angle, pivot = center) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f * intensity),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension / 3
                    )
                }
            }
            Text(icon)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// ICE CRYSTAL
// ═════════════════════════════════════════════════════════════════

class IceCrystalEffect : EffectV2 {
    override val id = "ice_crystal"
    override val name = "Ice Crystal"
    override val description = "Frost overlay with touch-melt interaction"
    override val category = EffectCategory.ANIMATIONS
    override val type = EffectType.INTERACTIVE
    override val icon = "🧊"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 1.5f
    override val defaultIntensity = 0.7f
    
    override val hasVisual = true
    override val hasHaptic = true
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("frost_amount", "Frost Amount", 0.3f, 1f, 0.7f),
        EffectSetting.Toggle("melt_on_touch", "Melt on Touch", true)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 0.7f
    
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
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE0F7FA).copy(alpha = 0.7f * intensity),
                            Color(0xFF00BCD4).copy(alpha = 0.3f * intensity),
                            Color(0xFF006064).copy(alpha = 0.1f * intensity)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// CINNAMON FIRE
// ═════════════════════════════════════════════════════════════════

class CinnamonFireEffect : EffectV2 {
    override val id = "cinnamon_fire"
    override val name = "Cinnamon Fire"
    override val description = "Warm orange flicker effect"
    override val category = EffectCategory.ANIMATIONS
    override val type = EffectType.OVERLAY
    override val icon = "🔥"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 1f
    
    override val hasVisual = true
    override val hasHaptic = false
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("flicker_speed", "Flicker Speed", 0.5f, 3f, 1f, 0.1f),
        EffectSetting.Slider("warmth", "Warmth", 0.5f, 1f, 0.8f)
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
        val infiniteTransition = rememberInfiniteTransition(label = "fire")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.1f,
            targetValue = 0.3f * intensity,
            animationSpec = infiniteRepeatable(
                animation = tween((500 / intensity).toInt().coerceAtLeast(100)),
                repeatMode = RepeatMode.Reverse
            ),
            label = "flicker"
        )
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF5722).copy(alpha = alpha)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// HAPTIC EFFECTS
// ═════════════════════════════════════════════════════════════════

class HeartbeatHapticEffect : EffectV2 {
    override val id = "heartbeat_haptic"
    override val name = "Heartbeat"
    override val description = "Rhythmic heartbeat vibration pattern"
    override val category = EffectCategory.HAPTIC
    override val type = EffectType.BACKGROUND
    override val icon = "💓"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 0.8f
    
    override val hasVisual = false
    override val hasHaptic = true
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("bpm", "Beats Per Minute", 40f, 120f, 60f, 5f)
    )
    
    private var isActive = false
    private var currentIntensity = 0.8f
    private var hapticJob: Job? = null
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.currentIntensity = intensity
        isActive = true
        
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        
        hapticJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val duration = (80 * intensity).toLong()
                val pause = (500 / intensity).toLong()
                
                vibrator?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(duration)
                    }
                }
                
                delay(duration + pause)
                
                vibrator?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createOneShot((duration * 0.7).toLong(), VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate((duration * 0.7).toLong())
                    }
                }
                
                delay((duration * 0.7).toLong() + (pause * 2))
            }
        }
    }
    
    override fun disable() {
        isActive = false
        hapticJob?.cancel()
        hapticJob = null
    }
    
    override fun isActive() = isActive
    
    override fun setIntensity(intensity: Float) {
        currentIntensity = intensity
    }
    
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1f + (0.2f * intensity),
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFCDD2)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size((40 * scale).dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE91E63))
            )
            Text(icon)
        }
    }
}

class GummyBounceEffect : EffectV2 {
    override val id = "gummy_bounce"
    override val name = "Gummy Bounce"
    override val description = "Rhythmic bouncy vibrations"
    override val category = EffectCategory.HAPTIC
    override val type = EffectType.BACKGROUND
    override val icon = "🍬"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 1f
    
    override val hasVisual = false
    override val hasHaptic = true
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("bounce_rate", "Bounce Rate", 0.5f, 2f, 1f, 0.1f)
    )
    
    private var isActive = false
    private var currentIntensity = 1f
    private var hapticJob: Job? = null
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.currentIntensity = intensity
        isActive = true
        
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        
        hapticJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                val pattern = longArrayOf(0, 50, 30, 50, 30, 50)
                val amps = intArrayOf(0, 255, 128, 255, 128, 255)
                
                vibrator?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createWaveform(pattern, amps, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(pattern, -1)
                    }
                }
                
                delay((600 / intensity).toLong())
            }
        }
    }
    
    override fun disable() {
        isActive = false
        hapticJob?.cancel()
        hapticJob = null
    }
    
    override fun isActive() = isActive
    override fun setIntensity(intensity: Float) { currentIntensity = intensity }
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTranslation(label = "bounce")
        val offset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -10f * intensity,
            animationSpec = infiniteRepeatable(
                animation = tween(300, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bounce"
        )
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF69B4)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, modifier = Modifier.offset(y = offset.dp))
        }
    }
}

class CrunchHapticEffect : EffectV2 {
    override val id = "crunch_haptic"
    override val name = "Crunch"
    override val description = "Sharp crackling vibration patterns"
    override val category = EffectCategory.HAPTIC
    override val type = EffectType.BACKGROUND
    override val icon = "🍪"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 0.7f
    
    override val hasVisual = false
    override val hasHaptic = true
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("crunchiness", "Crunchiness", 0.5f, 2f, 1f, 0.1f)
    )
    
    private var isActive = false
    private var currentIntensity = 0.7f
    private var hapticJob: Job? = null
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.currentIntensity = intensity
        isActive = true
        
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        
        hapticJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                // Random crunch pattern
                val pattern = List(10) { (10 + kotlin.random.Random.nextInt(30)).toLong() }.toLongArray()
                
                vibrator?.let {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        it.vibrate(VibrationEffect.createWaveform(pattern, -1))
                    } else {
                        @Suppress("DEPRECATION")
                        it.vibrate(pattern, -1)
                    }
                }
                
                delay((800 / intensity).toLong() + kotlin.random.Random.nextLong(500))
            }
        }
    }
    
    override fun disable() {
        isActive = false
        hapticJob?.cancel()
        hapticJob = null
    }
    
    override fun isActive() = isActive
    override fun setIntensity(intensity: Float) { currentIntensity = intensity }
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFD7CCC8)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }
    }
}

class FizzySodaEffect : EffectV2 {
    override val id = "fizzy_soda"
    override val name = "Fizzy Soda"
    override val description = "Rapid micro-vibrations like carbonation"
    override val category = EffectCategory.HAPTIC
    override val type = EffectType.BACKGROUND
    override val icon = "🥤"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 1f
    
    override val hasVisual = false
    override val hasHaptic = true
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("fizz_level", "Fizz Level", 0.5f, 2f, 1f, 0.1f)
    )
    
    private var isActive = false
    private var currentIntensity = 1f
    private var hapticJob: Job? = null
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.currentIntensity = intensity
        isActive = true
        
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        
        hapticJob = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                // Very rapid short pulses
                repeat(20) {
                    vibrator?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            it.vibrate(VibrationEffect.createOneShot(5, 128))
                        } else {
                            @Suppress("DEPRECATION")
                            it.vibrate(5)
                        }
                    }
                    delay((20 / intensity).toLong())
                }
                
                delay((200 / intensity).toLong())
            }
        }
    }
    
    override fun disable() {
        isActive = false
        hapticJob?.cancel()
        hapticJob = null
    }
    
    override fun isActive() = isActive
    override fun setIntensity(intensity: Float) { currentIntensity = intensity }
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        val infiniteTransition = rememberInfiniteTransition(label = "fizz")
        val alphas = List(5) { index ->
            infiniteTransition.animateFloat(
                initialValue = 0.2f,
                targetValue = 0.8f,
                animationSpec = infiniteRepeatable(
                    animation = tween((100 + index * 20) / intensity.toInt().coerceAtLeast(1)),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bubble$index"
            )
        }
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFF03A9F4)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                alphas.forEachIndexed { index, alphaAnim ->
                    val y = size.height - (index + 1) * 12.dp.toPx()
                    drawCircle(
                        color = Color.White.copy(alpha = alphaAnim.value),
                        radius = 3f + index,
                        center = Offset(size.width / 2, y)
                    )
                }
            }
            Text(icon)
        }
    }
}

@Composable
private fun rememberInfiniteTranslation(label: String) = rememberInfiniteTransition(label = label)
