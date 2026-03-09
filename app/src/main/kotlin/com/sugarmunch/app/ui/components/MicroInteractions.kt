package com.sugarmunch.app.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.animation.core.*
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.engine.ThemeManager
import kotlinx.coroutines.delay

/**
 * Haptic feedback utilities
 */
object Haptics {
    
    fun performClick(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(10)
        }
    }
    
    fun performHeavyClick(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(20)
        }
    }
    
    fun performTick(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        }
    }
    
    fun performSuccess(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(
                longArrayOf(0, 50, 50, 50),
                intArrayOf(0, 128, 0, 255),
                -1
            ))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 50, 50, 50), -1)
        }
    }
    
    fun performError(context: Context) {
        val vibrator = getVibrator(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(
                longArrayOf(0, 100, 50, 100),
                intArrayOf(0, 255, 0, 255),
                -1
            ))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 100, 50, 100), -1)
        }
    }
    
    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}

/**
 * Candy-themed spring animation specs
 */
object CandySprings {
    val Bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val Soft = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val Snappy = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    val Gummy = spring<Float>(
        dampingRatio = 0.4f,
        stiffness = 200f
    )
}

/**
 * Modifier with candy-themed haptic feedback on click
 */
fun Modifier.candyClickable(
    onClick: () -> Unit,
    hapticType: HapticType = HapticType.CLICK
) = composed {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val hapticsEnabled by themeManager.currentTheme.collectAsState()
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = CandySprings.Bouncy,
        label = "scale"
    )
    
    this
        .scale(scale)
        .clickable(
            interactionSource = interactionSource,
            indication = rememberRipple(
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.2f)
            ),
            onClick = {
                when (hapticType) {
                    HapticType.CLICK -> Haptics.performClick(context)
                    HapticType.HEAVY -> Haptics.performHeavyClick(context)
                    HapticType.SUCCESS -> Haptics.performSuccess(context)
                    HapticType.ERROR -> Haptics.performError(context)
                }
                onClick()
            }
        )
}

enum class HapticType {
    CLICK, HEAVY, SUCCESS, ERROR
}

/**
 * Modifier with spring animation on press
 */
fun Modifier.springPressable(
    enabled: Boolean = true
) = composed {
    if (!enabled) return@composed this
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = CandySprings.Gummy,
        label = "spring_press"
    )
    
    this.scale(scale)
}

/**
 * Elastic list item animation
 */
@Composable
fun ElasticListItem(
    index: Int,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val animIntensity by themeManager.animationIntensity.collectAsState()
    
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay((index * 50 / animIntensity).toLong().coerceAtLeast(20))
        isVisible = true
    }
    
    val offsetY by animateFloatAsState(
        targetValue = if (isVisible) 0f else 50f * animIntensity,
        animationSpec = CandySprings.Soft,
        label = "elastic_offset"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween((300 / animIntensity).toInt().coerceAtLeast(100)),
        label = "elastic_alpha"
    )
    
    androidx.compose.ui.platform.LocalView.current
    
    Box(
        modifier = Modifier
            .graphicsLayer {
                translationY = offsetY
                this.alpha = alpha
            }
    ) {
        content()
    }
}

private fun Modifier.graphicsLayer(block: androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit): Modifier {
    return this.then(
        Modifier.drawBehind { }
    )
}
