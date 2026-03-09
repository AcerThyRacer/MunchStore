package com.sugarmunch.app.ui.loading

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens
import kotlin.math.cos
import kotlin.math.sin

enum class SugarTransition {
    SLIDE_HORIZONTAL,
    SLIDE_VERTICAL,
    FADE,
    SCALE_FADE,
    SHARED_AXIS_X,
    SHARED_AXIS_Y,
    CANDY_POP
}

object SugarTransitions {

    private const val STANDARD_MS = 300
    private const val QUICK_MS = 200
    private const val DRAMATIC_MS = 400

    // -- Enter transitions ------------------------------------------------

    fun enterTransition(type: SugarTransition): EnterTransition = when (type) {
        SugarTransition.SLIDE_HORIZONTAL ->
            slideInHorizontally(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialOffsetX = { it }
            ) + fadeIn(tween(STANDARD_MS))

        SugarTransition.SLIDE_VERTICAL ->
            slideInVertically(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialOffsetY = { it }
            ) + fadeIn(tween(STANDARD_MS))

        SugarTransition.FADE ->
            fadeIn(tween(QUICK_MS, easing = LinearEasing))

        SugarTransition.SCALE_FADE ->
            scaleIn(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialScale = 0.92f
            ) + fadeIn(tween(STANDARD_MS))

        SugarTransition.SHARED_AXIS_X ->
            slideInHorizontally(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialOffsetX = { it / 5 }
            ) + fadeIn(tween(STANDARD_MS, delayMillis = 50))

        SugarTransition.SHARED_AXIS_Y ->
            slideInVertically(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialOffsetY = { it / 5 }
            ) + fadeIn(tween(STANDARD_MS, delayMillis = 50))

        SugarTransition.CANDY_POP ->
            scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialScale = 0f
            ) + fadeIn(tween(DRAMATIC_MS))
    }

    // -- Exit transitions -------------------------------------------------

    fun exitTransition(type: SugarTransition): ExitTransition = when (type) {
        SugarTransition.SLIDE_HORIZONTAL ->
            slideOutHorizontally(
                animationSpec = tween(STANDARD_MS, easing = EaseInCubic),
                targetOffsetX = { -it / 3 }
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.SLIDE_VERTICAL ->
            slideOutVertically(
                animationSpec = tween(STANDARD_MS, easing = EaseInCubic),
                targetOffsetY = { -it / 3 }
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.FADE ->
            fadeOut(tween(QUICK_MS, easing = LinearEasing))

        SugarTransition.SCALE_FADE ->
            scaleOut(
                animationSpec = tween(QUICK_MS, easing = EaseInCubic),
                targetScale = 0.92f
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.SHARED_AXIS_X ->
            slideOutHorizontally(
                animationSpec = tween(STANDARD_MS, easing = EaseInCubic),
                targetOffsetX = { -it / 5 }
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.SHARED_AXIS_Y ->
            slideOutVertically(
                animationSpec = tween(STANDARD_MS, easing = EaseInCubic),
                targetOffsetY = { -it / 5 }
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.CANDY_POP ->
            scaleOut(
                animationSpec = tween(QUICK_MS, easing = EaseInCubic),
                targetScale = 0.6f
            ) + fadeOut(tween(QUICK_MS))
    }

    // -- Pop enter transitions (reverse navigation) -----------------------

    fun popEnterTransition(type: SugarTransition): EnterTransition = when (type) {
        SugarTransition.SLIDE_HORIZONTAL ->
            slideInHorizontally(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialOffsetX = { -it / 3 }
            ) + fadeIn(tween(STANDARD_MS))

        SugarTransition.SLIDE_VERTICAL ->
            slideInVertically(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialOffsetY = { -it / 3 }
            ) + fadeIn(tween(STANDARD_MS))

        SugarTransition.FADE ->
            fadeIn(tween(QUICK_MS, easing = LinearEasing))

        SugarTransition.SCALE_FADE ->
            scaleIn(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialScale = 0.92f
            ) + fadeIn(tween(STANDARD_MS))

        SugarTransition.SHARED_AXIS_X ->
            slideInHorizontally(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialOffsetX = { -it / 5 }
            ) + fadeIn(tween(STANDARD_MS, delayMillis = 50))

        SugarTransition.SHARED_AXIS_Y ->
            slideInVertically(
                animationSpec = tween(STANDARD_MS, easing = EaseOutCubic),
                initialOffsetY = { -it / 5 }
            ) + fadeIn(tween(STANDARD_MS, delayMillis = 50))

        SugarTransition.CANDY_POP ->
            scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialScale = 0.6f
            ) + fadeIn(tween(STANDARD_MS))
    }

    // -- Pop exit transitions (reverse navigation) ------------------------

    fun popExitTransition(type: SugarTransition): ExitTransition = when (type) {
        SugarTransition.SLIDE_HORIZONTAL ->
            slideOutHorizontally(
                animationSpec = tween(STANDARD_MS, easing = EaseInCubic),
                targetOffsetX = { it }
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.SLIDE_VERTICAL ->
            slideOutVertically(
                animationSpec = tween(STANDARD_MS, easing = EaseInCubic),
                targetOffsetY = { it }
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.FADE ->
            fadeOut(tween(QUICK_MS, easing = LinearEasing))

        SugarTransition.SCALE_FADE ->
            scaleOut(
                animationSpec = tween(QUICK_MS, easing = EaseInCubic),
                targetScale = 1.05f
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.SHARED_AXIS_X ->
            slideOutHorizontally(
                animationSpec = tween(STANDARD_MS, easing = EaseInCubic),
                targetOffsetX = { it / 5 }
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.SHARED_AXIS_Y ->
            slideOutVertically(
                animationSpec = tween(STANDARD_MS, easing = EaseInCubic),
                targetOffsetY = { it / 5 }
            ) + fadeOut(tween(QUICK_MS))

        SugarTransition.CANDY_POP ->
            scaleOut(
                animationSpec = tween(STANDARD_MS, easing = EaseInCubic),
                targetScale = 0f
            ) + fadeOut(tween(QUICK_MS))
    }
}

// ---------------------------------------------------------------------------
// Pull-to-refresh candy animation
// ---------------------------------------------------------------------------

@Composable
fun CandyPullRefreshIndicator(
    isRefreshing: Boolean,
    pullProgress: Float,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surface

    // Spin while refreshing
    val infiniteTransition = rememberInfiniteTransition(label = "candy_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )

    // Smooth progress for unwrap
    val animatedProgress by animateFloatAsState(
        targetValue = pullProgress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "pull_progress"
    )

    val progress = if (isRefreshing) 1f else animatedProgress
    val rotation = if (isRefreshing) spinAngle else 0f

    Canvas(
        modifier = modifier.size(SugarDimens.IconSize.xxl)
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        rotate(degrees = rotation, pivot = Offset(cx, cy)) {
            drawCandyUnwrap(cx, cy, w, h, progress, primary, secondary, tertiary, surface)
        }
    }
}

private fun DrawScope.drawCandyUnwrap(
    cx: Float, cy: Float, w: Float, h: Float,
    progress: Float,
    primary: Color, secondary: Color, tertiary: Color, surface: Color
) {
    val candyRadius = w * 0.18f
    val wrapExtend = w * 0.22f

    // Wrapper spread based on progress: 0 = closed, 1 = fully open
    val wrapAngle = progress * 55f

    // Left wrapper flap
    rotate(degrees = -wrapAngle, pivot = Offset(cx - candyRadius, cy)) {
        val leftWrap = Path().apply {
            moveTo(cx - candyRadius, cy - candyRadius * 0.5f)
            lineTo(cx - candyRadius - wrapExtend, cy - candyRadius * 0.8f)
            lineTo(cx - candyRadius - wrapExtend, cy + candyRadius * 0.8f)
            lineTo(cx - candyRadius, cy + candyRadius * 0.5f)
            close()
        }
        drawPath(path = leftWrap, color = tertiary.copy(alpha = 0.4f))
        drawPath(
            path = leftWrap,
            color = secondary.copy(alpha = 0.3f),
            style = Stroke(width = 1.5f)
        )
        // Crinkle lines
        drawLine(
            color = surface.copy(alpha = 0.2f),
            start = Offset(cx - candyRadius - wrapExtend * 0.3f, cy - candyRadius * 0.3f),
            end = Offset(cx - candyRadius - wrapExtend * 0.6f, cy + candyRadius * 0.3f),
            strokeWidth = 1f
        )
    }

    // Right wrapper flap
    rotate(degrees = wrapAngle, pivot = Offset(cx + candyRadius, cy)) {
        val rightWrap = Path().apply {
            moveTo(cx + candyRadius, cy - candyRadius * 0.5f)
            lineTo(cx + candyRadius + wrapExtend, cy - candyRadius * 0.8f)
            lineTo(cx + candyRadius + wrapExtend, cy + candyRadius * 0.8f)
            lineTo(cx + candyRadius, cy + candyRadius * 0.5f)
            close()
        }
        drawPath(path = rightWrap, color = tertiary.copy(alpha = 0.4f))
        drawPath(
            path = rightWrap,
            color = secondary.copy(alpha = 0.3f),
            style = Stroke(width = 1.5f)
        )
        drawLine(
            color = surface.copy(alpha = 0.2f),
            start = Offset(cx + candyRadius + wrapExtend * 0.3f, cy - candyRadius * 0.3f),
            end = Offset(cx + candyRadius + wrapExtend * 0.6f, cy + candyRadius * 0.3f),
            strokeWidth = 1f
        )
    }

    // Candy body (always visible, revealed as wrapper opens)
    drawCircle(
        color = primary.copy(alpha = 0.6f + progress * 0.4f),
        radius = candyRadius,
        center = Offset(cx, cy)
    )

    // Candy swirl pattern
    val swirlRadius = candyRadius * 0.6f
    drawArc(
        color = secondary.copy(alpha = 0.3f + progress * 0.3f),
        startAngle = 0f,
        sweepAngle = 270f,
        useCenter = false,
        topLeft = Offset(cx - swirlRadius, cy - swirlRadius),
        size = Size(swirlRadius * 2f, swirlRadius * 2f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )

    // Highlight dot
    drawCircle(
        color = Color.White.copy(alpha = 0.3f * progress),
        radius = candyRadius * 0.15f,
        center = Offset(cx - candyRadius * 0.3f, cy - candyRadius * 0.3f)
    )
}

// Easing helpers available in Compose 1.5+; redeclared for compatibility.
private val EaseOutCubic: Easing = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)
private val EaseInCubic: Easing = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)
