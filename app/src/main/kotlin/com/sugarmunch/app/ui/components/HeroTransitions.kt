package com.sugarmunch.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.engine.ThemeManager

/**
 * Shared element key for hero transitions
 */
object HeroKeys {
    fun appIcon(appId: String) = "app_icon_$appId"
    fun appCard(appId: String) = "app_card_$appId"
    fun appName(appId: String) = "app_name_$appId"
}

/**
 * Modifier for card press animation
 */
fun Modifier.pressAnimation(
    enabled: Boolean = true
) = composed {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val animIntensity by themeManager.animationIntensity.collectAsState()
    
    if (!enabled) return@composed this
    
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "press"
    )
    
    this
        .scale(scale)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    isPressed = event.changes.any { it.pressed }
                }
            }
        }
}

/**
 * Bouncy scale animation modifier
 */
fun Modifier.bouncyScale(
    targetScale: Float = 1.05f,
    animationDuration: Int = 1500
) = composed {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val animIntensity by themeManager.animationIntensity.collectAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "bouncy")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f + ((targetScale - 1f) * animIntensity),
        animationSpec = infiniteRepeatable(
            animation = tween((animationDuration / animIntensity).toInt().coerceAtLeast(500)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bouncy_scale"
    )
    
    this.scale(scale)
}

/**
 * Elastic scroll animation modifier
 */
fun Modifier.elasticScroll() = composed {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val animIntensity by themeManager.animationIntensity.collectAsState()
    
    if (animIntensity < 0.5f) return@composed this
    
    val scrollState = rememberScrollState()
    val offset by remember {
        derivedStateOf {
            val maxOffset = 50f * animIntensity
            when {
                scrollState.value < 0 -> (-scrollState.value).coerceAtMost(maxOffset.toInt())
                scrollState.value > scrollState.maxValue -> 
                    (scrollState.value - scrollState.maxValue).coerceAtMost(maxOffset.toInt())
                else -> 0
            }.toFloat()
        }
    }
    
    this.graphicsLayer {
        translationY = offset * 0.5f
    }
}

/**
 * Staggered item animation
 */
@Composable
fun <T> StaggeredListAnimation(
    items: List<T>,
    key: ((T) -> Any)? = null,
    delayMillis: Int = 50,
    content: @Composable (T) -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val animIntensity by themeManager.animationIntensity.collectAsState()
    
    val actualDelay = (delayMillis / animIntensity).toInt().coerceAtLeast(20)
    
    items.forEachIndexed { index, item ->
        val visible = remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(index * actualDelay.toLong())
            visible.value = true
        }
        
        AnimatedVisibility(
            visible = visible.value,
            enter = fadeIn(
                animationSpec = tween((300 / animIntensity).toInt().coerceAtLeast(100))
            ) + slideInVertically(
                initialOffsetY = { (it * 0.2f).toInt() },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        ) {
            content(item)
        }
    }
}

/**
 * Shimmer loading effect with theme colors
 */
@Composable
fun ThemedShimmerItem(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val shimmerColors = listOf(
        colors.surfaceVariant.copy(alpha = 0.3f),
        colors.surfaceVariant.copy(alpha = 0.6f),
        colors.surfaceVariant.copy(alpha = 0.3f)
    )
    
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )
    
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.linearGradient(
                    colors = shimmerColors,
                    start = androidx.compose.ui.geometry.Offset(
                        translateAnim - 200f, 
                        translateAnim - 200f
                    ),
                    end = androidx.compose.ui.geometry.Offset(
                        translateAnim, 
                        translateAnim
                    )
                )
            )
    )
}

/**
 * Pulsing glow effect
 */
@Composable
fun PulsingGlow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val currentTheme by themeManager.currentTheme.collectAsState()
    val themeIntensity by themeManager.themeIntensity.collectAsState()
    val animIntensity by themeManager.animationIntensity.collectAsState()
    val colors = currentTheme.getColorsForIntensity(themeIntensity)
    
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f * animIntensity,
        animationSpec = infiniteRepeatable(
            animation = tween((1500 / animIntensity).toInt().coerceAtLeast(500)),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    Box(
        modifier = modifier
            .border(
                width = 2.dp,
                brush = Brush.radialGradient(
                    listOf(
                        colors.primary.copy(alpha = glowAlpha),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        content()
    }
}

private fun Modifier.border(width: androidx.compose.ui.unit.Dp, brush: Brush, shape: RoundedCornerShape): Modifier {
    return this.then(
        Modifier.drawBehind {
            drawRoundRect(
                brush = brush,
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(
                    shape.topStart.toPx(size, density),
                    shape.topStart.toPx(size, density)
                ),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width.toPx())
            )
        }
    )
}
