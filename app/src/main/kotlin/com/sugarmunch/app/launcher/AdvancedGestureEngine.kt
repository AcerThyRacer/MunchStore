package com.sugarmunch.app.launcher

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.sugarmunch.app.ui.feedback.HapticPattern
import com.sugarmunch.app.ui.feedback.HapticIntensity
import com.sugarmunch.app.ui.feedback.ThemeAwareHapticEngine
import com.sugarmunch.app.theme.model.CandyTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Gesture feedback configuration
 */
data class GestureFeedbackConfig(
    val showVisualFeedback: Boolean = true,
    val showHapticFeedback: Boolean = true,
    val feedbackIntensity: Float = 0.5f,
    val feedbackStyle: FeedbackStyle = FeedbackStyle.RIPPLE,
    val soundEnabled: Boolean = false,
    val customSound: SoundType? = null
)

/**
 * Visual feedback styles for gestures
 */
enum class FeedbackStyle {
    RIPPLE,           // Default Material ripple
    BURST,            // Particle burst
    GLOW,             // Glowing circle
    SPARKLE,          // Sparkle effect
    SHOCKWAVE,        // Expanding ring
    CONFETTI          // Confetti explosion
}

/**
 * Sound types for gesture feedback
 */
enum class SoundType {
    NONE,
    CLICK,
    POP,
    SWOOSH,
    CHIME,
    SUCCESS,
    ERROR
}

/**
 * Visual feedback effect data
 */
data class VisualFeedbackEffect(
    val style: FeedbackStyle,
    val position: Offset,
    val color: Color,
    val size: Float,
    val duration: Long,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Enhanced Gesture Engine with advanced feedback
 * Extends the base GestureEngine with visual and haptic feedback
 */
class AdvancedGestureEngine(
    context: Context,
    private val baseEngine: GestureEngine = GestureEngine.getInstance(context)
) {
    private val hapticEngine = ThemeAwareHapticEngine.getInstance(context)

    private var feedbackConfig = GestureFeedbackConfig()
    private val _activeEffects = MutableStateFlow<List<VisualFeedbackEffect>>(emptyList())
    val activeEffects: StateFlow<List<VisualFeedbackEffect>> = _activeEffects.asStateFlow()

    /**
     * Update feedback configuration
     */
    fun updateFeedbackConfig(config: GestureFeedbackConfig) {
        feedbackConfig = config
    }

    /**
     * Perform gesture feedback with haptic and visual effects
     */
    fun performGestureFeedback(
        gestureType: GestureType,
        position: Offset,
        themeIntensity: Float = 1.0f
    ) {
        // Haptic feedback
        if (feedbackConfig.showHapticFeedback) {
            performHapticFeedback(gestureType, themeIntensity)
        }

        // Visual feedback
        if (feedbackConfig.showVisualFeedback) {
            performVisualFeedback(gestureType, position)
        }
    }

    /**
     * Perform haptic feedback based on gesture type
     */
    private fun performHapticFeedback(
        gestureType: GestureType,
        themeIntensity: Float
    ) {
        val hapticPattern = when (gestureType) {
            GestureType.TAP -> HapticPattern.TAP
            GestureType.DOUBLE_TAP -> HapticPattern.DOUBLE_TAP
            GestureType.LONG_PRESS -> HapticPattern.LONG_PRESS
            GestureType.SWIPE_UP,
            GestureType.SWIPE_DOWN,
            GestureType.SWIPE_LEFT,
            GestureType.SWIPE_RIGHT,
            GestureType.SWIPE_UP_LEFT,
            GestureType.SWIPE_UP_RIGHT,
            GestureType.SWIPE_DOWN_LEFT,
            GestureType.SWIPE_DOWN_RIGHT -> HapticPattern.TAP
            GestureType.PINCH_IN,
            GestureType.PINCH_OUT -> HapticPattern.WARNING
            GestureType.TWO_FINGER_SWIPE_UP,
            GestureType.TWO_FINGER_SWIPE_DOWN,
            GestureType.TWO_FINGER_TAP -> HapticPattern.TAP
            GestureType.CUSTOM_GESTURE -> HapticPattern.SUCCESS
        }

        hapticEngine.performPattern(hapticPattern, themeIntensity, CandyTheme.DEFAULT)
    }

    /**
     * Perform visual feedback effect
     */
    private fun performVisualFeedback(
        gestureType: GestureType,
        position: Offset
    ) {
        val effect = VisualFeedbackEffect(
            style = feedbackConfig.feedbackStyle,
            position = position,
            color = getGestureColor(gestureType),
            size = getGestureSize(gestureType),
            duration = getGestureDuration(gestureType)
        )

        _activeEffects.value = _activeEffects.value + effect

        // Remove effect after duration
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            kotlinx.coroutines.delay(effect.duration)
            _activeEffects.value = _activeEffects.value.filter { it != effect }
        }
    }

    /**
     * Get feedback color for gesture type
     */
    private fun getGestureColor(gestureType: GestureType): Color {
        return when (gestureType) {
            GestureType.TAP -> Color.Blue
            GestureType.DOUBLE_TAP -> Color.Green
            GestureType.LONG_PRESS -> Color.Yellow
            GestureType.SWIPE_UP -> Color.Cyan
            GestureType.SWIPE_DOWN -> Color.Magenta
            GestureType.SWIPE_LEFT,
            GestureType.SWIPE_RIGHT -> Color.Blue
            GestureType.SWIPE_UP_LEFT,
            GestureType.SWIPE_UP_RIGHT,
            GestureType.SWIPE_DOWN_LEFT,
            GestureType.SWIPE_DOWN_RIGHT -> Color.Blue
            GestureType.PINCH_IN,
            GestureType.PINCH_OUT -> Color.Red
            GestureType.TWO_FINGER_SWIPE_UP,
            GestureType.TWO_FINGER_SWIPE_DOWN,
            GestureType.TWO_FINGER_TAP -> Color.Green
            GestureType.CUSTOM_GESTURE -> Color.Magenta
        }
    }

    /**
     * Get feedback size for gesture type
     */
    private fun getGestureSize(gestureType: GestureType): Float {
        return when (gestureType) {
            GestureType.TAP -> 50f
            GestureType.DOUBLE_TAP -> 60f
            GestureType.LONG_PRESS -> 80f
            GestureType.SWIPE_UP,
            GestureType.SWIPE_DOWN,
            GestureType.SWIPE_LEFT,
            GestureType.SWIPE_RIGHT -> 100f
            GestureType.SWIPE_UP_LEFT,
            GestureType.SWIPE_UP_RIGHT,
            GestureType.SWIPE_DOWN_LEFT,
            GestureType.SWIPE_DOWN_RIGHT -> 100f
            GestureType.PINCH_IN,
            GestureType.PINCH_OUT -> 120f
            GestureType.TWO_FINGER_SWIPE_UP,
            GestureType.TWO_FINGER_SWIPE_DOWN,
            GestureType.TWO_FINGER_TAP -> 80f
            GestureType.CUSTOM_GESTURE -> 150f
        }
    }

    /**
     * Get feedback duration for gesture type
     */
    private fun getGestureDuration(gestureType: GestureType): Long {
        return when (gestureType) {
            GestureType.TAP -> 200L
            GestureType.DOUBLE_TAP -> 300L
            GestureType.LONG_PRESS -> 500L
            GestureType.SWIPE_UP,
            GestureType.SWIPE_DOWN,
            GestureType.SWIPE_LEFT,
            GestureType.SWIPE_RIGHT -> 400L
            GestureType.SWIPE_UP_LEFT,
            GestureType.SWIPE_UP_RIGHT,
            GestureType.SWIPE_DOWN_LEFT,
            GestureType.SWIPE_DOWN_RIGHT -> 400L
            GestureType.PINCH_IN,
            GestureType.PINCH_OUT -> 500L
            GestureType.TWO_FINGER_SWIPE_UP,
            GestureType.TWO_FINGER_SWIPE_DOWN,
            GestureType.TWO_FINGER_TAP -> 400L
            GestureType.CUSTOM_GESTURE -> 600L
        }
    }

    /**
     * Clear all active effects
     */
    fun clearEffects() {
        _activeEffects.value = emptyList()
    }

    companion object {
        @Volatile
        private var instance: AdvancedGestureEngine? = null

        fun getInstance(context: Context): AdvancedGestureEngine {
            return instance ?: synchronized(this) {
                instance ?: AdvancedGestureEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Composable to remember the advanced gesture engine
 */
@Composable
fun rememberAdvancedGestureEngine(
    context: Context = LocalContext.current
): AdvancedGestureEngine {
    return remember {
        AdvancedGestureEngine.getInstance(context)
    }
}

/**
 * Touch visualization overlay composable
 * Shows visual feedback for touch events
 */
@Composable
fun TouchVisualizationOverlay(
    gestureEngine: AdvancedGestureEngine,
    config: GestureFeedbackConfig,
    modifier: Modifier = Modifier
) {
    val activeEffects by gestureEngine.activeEffects.collectAsState()

    Box(modifier = modifier) {
        activeEffects.forEach { effect ->
            VisualFeedbackEffect(
                effect = effect,
                intensity = config.feedbackIntensity
            )
        }
    }
}

/**
 * Per-gesture haptic configuration
 */
data class GestureHapticConfig(
    val tapPattern: HapticPattern = HapticPattern.TAP,
    val swipePattern: HapticPattern = HapticPattern.TAP,
    val longPressPattern: HapticPattern = HapticPattern.LONG_PRESS,
    val pinchPattern: HapticPattern = HapticPattern.WARNING,
    val multiTouchPattern: HapticPattern = HapticPattern.TAP,
    val customPattern: HapticPattern = HapticPattern.SUCCESS,
    val intensity: Float = 0.5f,
    val enabled: Boolean = true
)
