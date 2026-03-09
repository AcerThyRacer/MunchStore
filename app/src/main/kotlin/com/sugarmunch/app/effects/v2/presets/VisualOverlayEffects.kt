package com.sugarmunch.app.effects.v2.presets

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.effects.v2.model.*
import kotlin.math.PI
import kotlin.math.sin

// ═════════════════════════════════════════════════════════════════
// SUGARRUSH OVERLAY
// ═════════════════════════════════════════════════════════════════

class SugarrushOverlayEffect : EffectV2 {
    override val id = "sugarrush_overlay"
    override val name = "SugarRush"
    override val description = "The classic gradient overlay with haptic pulse"
    override val category = EffectCategory.VISUAL_OVERLAY
    override val type = EffectType.OVERLAY
    override val icon = "🚀"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 1f
    
    override val hasVisual = true
    override val hasHaptic = true
    override val hasAudio = false
    
    override val settings = listOf<EffectSetting>(
        EffectSetting.ColorPicker("primary_color", "Primary Color", Color(0xFFFF1493)),
        EffectSetting.ColorPicker("secondary_color", "Secondary Color", Color(0xFF00CED1)),
        EffectSetting.Slider("pulse_speed", "Pulse Speed", 0.5f, 3f, 1f, 0.1f)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 1f
    private var hapticThread: Thread? = null
    private var isRunning = false
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val alpha = (0.2f + intensity * 0.3f).coerceIn(0f, 0.8f)
        
        val view = View(context).apply {
            setBackgroundColor(Color(0xFFFF1493).copy(alpha = alpha).toArgb())
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
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        
        windowManager.addView(view, params)
        overlayView = view
        isRunning = true
        
        // Start haptic pulse if enabled
        if (intensity > 0.5f) {
            startHapticPulse(context, intensity)
        }
    }
    
    override fun disable() {
        isRunning = false
        hapticThread?.interrupt()
        overlayView?.let { windowManager?.removeView(it) }
        overlayView = null
        windowManager = null
    }
    
    override fun isActive() = overlayView != null
    
    override fun setIntensity(intensity: Float) {
        currentIntensity = intensity
        val alpha = (0.2f + intensity * 0.3f).coerceIn(0f, 0.8f)
        overlayView?.alpha = alpha
    }
    
    override fun getCurrentIntensity() = currentIntensity
    
    private fun startHapticPulse(context: Context, intensity: Float) {
        // Implementation would use Vibrator service
    }
    
    @Composable
    override fun Preview(intensity: Float) {
        val alpha = (0.2f + intensity * 0.3f).coerceIn(0f, 0.8f)
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFFF1493).copy(alpha = alpha)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// RAINBOW TINT
// ═════════════════════════════════════════════════════════════════

class RainbowTintEffect : EffectV2 {
    override val id = "rainbow_tint"
    override val name = "Rainbow Tint"
    override val description = "Soft rainbow color filter overlay"
    override val category = EffectCategory.VISUAL_OVERLAY
    override val type = EffectType.OVERLAY
    override val icon = "🌈"
    
    override val minIntensity = 0.2f
    override val maxIntensity = 1.5f
    override val defaultIntensity = 0.6f
    
    override val hasVisual = true
    override val hasHaptic = false
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("saturation", "Saturation", 0f, 1f, 0.4f)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 0.6f
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val view = View(context).apply {
            setBackgroundColor(android.graphics.Color.argb(
                (50 * intensity).toInt().coerceIn(10, 120),
                255, 100, 200
            ))
            alpha = 0.3f * intensity
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
    
    override fun setIntensity(intensity: Float) {
        currentIntensity = intensity
        overlayView?.alpha = 0.3f * intensity
    }
    
    override fun getCurrentIntensity() = currentIntensity
    
    @Composable
    override fun Preview(intensity: Float) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Red.copy(alpha = 0.3f * intensity),
                            Color.Yellow.copy(alpha = 0.3f * intensity),
                            Color.Green.copy(alpha = 0.3f * intensity),
                            Color.Blue.copy(alpha = 0.3f * intensity)
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
// MINT WASH
// ═════════════════════════════════════════════════════════════════

class MintWashEffect : EffectV2 {
    override val id = "mint_wash"
    override val name = "Mint Wash"
    override val description = "Refreshing mint green tint"
    override val category = EffectCategory.VISUAL_OVERLAY
    override val type = EffectType.OVERLAY
    override val icon = "🌿"
    
    override val minIntensity = 0.2f
    override val maxIntensity = 1.5f
    override val defaultIntensity = 0.7f
    
    override val hasVisual = true
    override val hasHaptic = false
    override val hasAudio = false
    
    override val settings = emptyList<EffectSetting>()
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 0.7f
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val view = View(context).apply {
            setBackgroundColor(android.graphics.Color.argb(
                (60 * intensity).toInt().coerceIn(10, 120),
                152, 255, 152
            ))
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
                .background(Color(0xFF98FF98).copy(alpha = 0.3f * intensity)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// CARAMEL DIM
// ═════════════════════════════════════════════════════════════════

class CaramelDimEffect : EffectV2 {
    override val id = "caramel_dim"
    override val name = "Caramel Dim"
    override val description = "Warm caramel comfort filter"
    override val category = EffectCategory.VISUAL_OVERLAY
    override val type = EffectType.OVERLAY
    override val icon = "🍮"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 1.2f
    override val defaultIntensity = 0.6f
    
    override val hasVisual = true
    override val hasHaptic = false
    override val hasAudio = false
    
    override val settings = emptyList<EffectSetting>()
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 0.6f
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val view = View(context).apply {
            setBackgroundColor(android.graphics.Color.argb(
                (80 * intensity).toInt().coerceIn(20, 150),
                222, 184, 135
            ))
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
                .background(Color(0xFFDEB887).copy(alpha = 0.4f * intensity)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// CHOCOLATE DARKNESS
// ═════════════════════════════════════════════════════════════════

class ChocolateDarknessEffect : EffectV2 {
    override val id = "chocolate_darkness"
    override val name = "Chocolate Darkness"
    override val description = "Deep rich chocolate dimming"
    override val category = EffectCategory.VISUAL_OVERLAY
    override val type = EffectType.OVERLAY
    override val icon = "🍫"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 1.5f
    override val defaultIntensity = 0.8f
    
    override val hasVisual = true
    override val hasHaptic = false
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.Slider("warmth", "Warmth", 0f, 1f, 0.7f)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 0.8f
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val view = View(context).apply {
            setBackgroundColor(android.graphics.Color.argb(
                (100 * intensity).toInt().coerceIn(30, 180),
                60, 40, 30
            ))
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
                .background(Color(0xFF3C281E).copy(alpha = 0.5f * intensity)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// LOLLIPOP GLOW
// ═════════════════════════════════════════════════════════════════

class LollipopGlowEffect : EffectV2 {
    override val id = "lollipop_glow"
    override val name = "Lollipop Glow"
    override val description = "Soft pulsing glow around screen edges"
    override val category = EffectCategory.VISUAL_OVERLAY
    override val type = EffectType.OVERLAY
    override val icon = "🍭"
    
    override val minIntensity = 0.3f
    override val maxIntensity = 2f
    override val defaultIntensity = 1f
    
    override val hasVisual = true
    override val hasHaptic = true
    override val hasAudio = false
    
    override val settings = listOf(
        EffectSetting.ColorPicker("glow_color", "Glow Color", Color(0xFFFF69B4)),
        EffectSetting.Slider("pulse_speed", "Pulse Speed", 0.5f, 3f, 1f)
    )
    
    private var overlayView: View? = null
    private var windowManager: WindowManager? = null
    private var currentIntensity = 1f
    
    override fun enable(context: Context, windowManager: WindowManager, intensity: Float) {
        this.windowManager = windowManager
        this.currentIntensity = intensity
        
        val view = View(context).apply {
            // Gradient edge glow would be implemented with custom drawable
            setBackgroundColor(Color(0xFFFF69B4).copy(alpha = 0.1f * intensity).toArgb())
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
                .drawBehind {
                    drawCircle(
                        color = Color(0xFFFF69B4).copy(alpha = 0.2f * intensity),
                        radius = size.maxDimension / 2
                    )
                    drawCircle(
                        color = Color(0xFFFF69B4).copy(alpha = 0.4f * intensity),
                        radius = size.maxDimension / 3
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }
    }
}
