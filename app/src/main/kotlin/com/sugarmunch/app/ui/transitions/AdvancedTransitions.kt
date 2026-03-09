package com.sugarmunch.app.ui.transitions

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.sqrt

// ---------------------------------------------------------------------------
// Phase 3.3 – Advanced Screen Transitions
// ---------------------------------------------------------------------------

/**
 * Circular reveal that expands a clipping circle from [revealCenter].
 * If [revealCenter] is [Offset.Unspecified] the centre of the composable is used.
 */
@Composable
fun CircularRevealTransition(
    visible: Boolean,
    revealCenter: Offset = Offset.Unspecified,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "circularRevealProgress"
    )

    if (progress > 0f) {
        BoxWithConstraints(modifier = modifier) {
            val widthPx = constraints.maxWidth.toFloat()
            val heightPx = constraints.maxHeight.toFloat()
            val center = if (revealCenter == Offset.Unspecified) {
                Offset(widthPx / 2f, heightPx / 2f)
            } else {
                revealCenter
            }
            val maxRadius = max(
                sqrt(
                    max(center.x, widthPx - center.x).let { it * it } +
                        max(center.y, heightPx - center.y).let { it * it }
                ),
                1f
            ) * 1.5f

            val shape = remember(center, maxRadius, progress) {
                CircleRevealShape(center, maxRadius * progress)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
            ) {
                content()
            }
        }
    }
}

private class CircleRevealShape(
    private val center: Offset,
    private val radius: Float
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            addOval(
                Rect(
                    center.x - radius,
                    center.y - radius,
                    center.x + radius,
                    center.y + radius
                )
            )
        }
        return Outline.Generic(path)
    }
}

// ---------------------------------------------------------------------------
// MorphTransition – card morph to fullscreen
// ---------------------------------------------------------------------------

/**
 * Scales content from 0.85→1.0, fades in, and animates corner radius from 24 dp→0 dp.
 */
@Composable
fun MorphTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "morphProgress"
    )

    if (progress > 0f) {
        val density = LocalDensity.current
        val cornerRadiusPx = with(density) { 24.dp.toPx() } * (1f - progress)
        val scale = 0.85f + 0.15f * progress

        val shape = remember(cornerRadiusPx) {
            RoundedCornerShape(cornerRadiusPx)
        }

        Box(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    alpha = progress
                    clip = true
                    this.shape = shape
                }
        ) {
            content()
        }
    }
}

private class RoundedCornerShape(private val radius: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    radiusX = radius,
                    radiusY = radius
                )
            )
        }
        return Outline.Generic(path)
    }
}

// ---------------------------------------------------------------------------
// LiquidSwipeTransition – fluid curve wipe
// ---------------------------------------------------------------------------

/**
 * Clips content with a fluid cubic-bezier curve that sweeps across the screen.
 */
@Composable
fun LiquidSwipeTransition(
    visible: Boolean,
    fromLeft: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "liquidSwipeProgress"
    )

    if (progress > 0f) {
        val shape = remember(progress, fromLeft) {
            LiquidSwipeShape(progress, fromLeft)
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .clip(shape)
        ) {
            content()
        }
    }
}

private class LiquidSwipeShape(
    private val progress: Float,
    private val fromLeft: Boolean
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height

        // The edge x-position sweeps from off-screen to fully covering
        val edgeX = if (fromLeft) w * progress else w * (1f - progress)
        // Bulge amount for the fluid effect – peaks at mid-transition
        val bulge = w * 0.35f * progress * (1f - progress) * 4f

        val path = Path().apply {
            if (fromLeft) {
                moveTo(0f, 0f)
                lineTo(edgeX, 0f)
                cubicTo(
                    edgeX + bulge, h * 0.25f,
                    edgeX - bulge * 0.5f, h * 0.75f,
                    edgeX, h
                )
                lineTo(0f, h)
                close()
            } else {
                moveTo(w, 0f)
                lineTo(edgeX, 0f)
                cubicTo(
                    edgeX - bulge, h * 0.25f,
                    edgeX + bulge * 0.5f, h * 0.75f,
                    edgeX, h
                )
                lineTo(w, h)
                close()
            }
        }
        return Outline.Generic(path)
    }
}

// ---------------------------------------------------------------------------
// ParallaxTransition – staggered horizontal strip entrance
// ---------------------------------------------------------------------------

/**
 * Splits content into [layers] horizontal strips that slide in from the right
 * with staggered delays, producing a parallax entrance effect.
 */
@Composable
fun ParallaxTransition(
    visible: Boolean,
    layers: Int = 3,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val effectiveLayers = layers.coerceIn(1, 8)

    BoxWithConstraints(modifier = modifier) {
        val widthPx = constraints.maxWidth.toFloat()
        val totalHeightPx = constraints.maxHeight.toFloat()
        val stripHeight = totalHeightPx / effectiveLayers
        val density = LocalDensity.current

        for (i in 0 until effectiveLayers) {
            val delayMs = i * 60
            val stripProgress by animateFloatAsState(
                targetValue = if (visible) 0f else 1f,
                animationSpec = tween(
                    durationMillis = 400,
                    delayMillis = delayMs,
                    easing = FastOutSlowInEasing
                ),
                label = "parallaxStrip$i"
            )

            val translationX = widthPx * stripProgress
            val stripHeightDp = with(density) { stripHeight.toDp() }
            val offsetY = with(density) { (stripHeight * i).toDp() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(stripHeightDp)
                    .offset { IntOffset(0, (stripHeight * i).toInt()) }
                    .clipToBounds()
                    .graphicsLayer {
                        this.translationX = translationX
                        alpha = 1f - stripProgress
                    }
            ) {
                // Render the full content shifted so this strip shows only its portion
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(0, -(stripHeight * i).toInt()) }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        content()
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// FlipBookTransition – 3-D page flip around Y axis
// ---------------------------------------------------------------------------

/**
 * 3-D page flip: content rotates around the Y axis from ±90° to 0°.
 * A shadow overlay fades proportionally to the rotation angle.
 */
@Composable
fun FlipBookTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "flipBookProgress"
    )

    if (progress > 0f) {
        val rotationY = -90f * (1f - progress) // -90 → 0 on enter
        val shadowAlpha = 0.4f * (1f - progress)

        Box(
            modifier = modifier
                .graphicsLayer {
                    this.rotationY = rotationY
                    cameraDistance = 12f * density
                    alpha = progress
                }
        ) {
            content()

            // Shadow overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = shadowAlpha))
            )
        }
    }
}

// ---------------------------------------------------------------------------
// DissolveTransition – grid-based pseudo-noise dissolve
// ---------------------------------------------------------------------------

private const val DISSOLVE_GRID_COLS = 10
private const val DISSOLVE_GRID_ROWS = 10
private const val DISSOLVE_CELL_COUNT = DISSOLVE_GRID_COLS * DISSOLVE_GRID_ROWS

/**
 * Dissolve that fades individual cells of a 10×10 grid at random times,
 * approximating a noise-based pattern.
 */
@Composable
fun DissolveTransition(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Generate stable random thresholds per cell (each 0..1)
    val thresholds = remember {
        val rng = kotlin.random.Random(42)
        FloatArray(DISSOLVE_CELL_COUNT) { rng.nextFloat() }
    }

    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 700, easing = LinearEasing),
        label = "dissolveProgress"
    )

    if (progress > 0f) {
        BoxWithConstraints(modifier = modifier) {
            val cellWidth = constraints.maxWidth.toFloat() / DISSOLVE_GRID_COLS
            val cellHeight = constraints.maxHeight.toFloat() / DISSOLVE_GRID_ROWS

            // Render full content behind the mask so it appears through revealed cells
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }

            // Overlay mask: each cell is an opaque rectangle that fades out as
            // the global progress passes its random threshold.
            val maskShape = remember(progress, thresholds) {
                DissolveMaskShape(progress, thresholds, DISSOLVE_GRID_COLS, DISSOLVE_GRID_ROWS)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(maskShape)
            )
        }
    }
}

/**
 * Shape that produces the inverse mask – it covers only the cells whose
 * threshold has NOT yet been reached, thereby gradually revealing content.
 */
private class DissolveMaskShape(
    private val progress: Float,
    private val thresholds: FloatArray,
    private val cols: Int,
    private val rows: Int
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val cellW = size.width / cols
        val cellH = size.height / rows
        val path = Path()
        for (i in thresholds.indices) {
            // Cell is "revealed" when progress >= threshold; we draw the UN-revealed cells
            if (progress < thresholds[i]) {
                val col = i % cols
                val row = i / cols
                path.addRect(
                    Rect(
                        left = col * cellW,
                        top = row * cellH,
                        right = (col + 1) * cellW,
                        bottom = (row + 1) * cellH
                    )
                )
            }
        }
        // If all cells revealed the path is empty – that's fine, nothing is masked.
        return Outline.Generic(path)
    }
}
