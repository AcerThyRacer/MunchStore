package com.sugarmunch.app.ui.loading

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

enum class EmptyStateType {
    NO_APPS,
    NO_EFFECTS,
    NO_REWARDS,
    SEARCH_EMPTY,
    OFFLINE,
    NO_FAVORITES,
    NO_DOWNLOADS,
    ERROR,
    COMING_SOON,
    EMPTY_COLLECTION,
    /** Catalog failed to load (network or manifest). */
    CATALOG_LOAD_FAILED,
    /** No clan joined. */
    NO_CLAN,
    /** No trades in marketplace. */
    NO_TRADES
}

@Composable
fun EmptyStateView(
    type: EmptyStateType,
    title: String? = null,
    message: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val defaults = defaultContent(type)
    val displayTitle = title ?: defaults.first
    val displayMessage = message ?: defaults.second

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = SugarDimens.Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EmptyStateIllustration(type = type)

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xl))

        Text(
            text = displayTitle,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))

        Text(
            text = displayMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.xl))
            FilledTonalButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@Composable
private fun EmptyStateIllustration(type: EmptyStateType) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurface = MaterialTheme.colorScheme.onSurface

    val infiniteTransition = rememberInfiniteTransition(label = "empty_state")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    Canvas(
        modifier = Modifier.size(120.dp)
    ) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (type) {
            EmptyStateType.NO_APPS -> drawNoApps(
                cx, cy, w, h, primary, secondary, surfaceVariant, shimmerAlpha
            )
            EmptyStateType.NO_EFFECTS -> drawNoEffects(
                cx, cy, w, h, onSurface, surfaceVariant, tertiary, shimmerAlpha
            )
            EmptyStateType.NO_REWARDS -> drawNoRewards(
                cx, cy, w, h, primary, secondary, surfaceVariant, onSurface
            )
            EmptyStateType.SEARCH_EMPTY -> drawSearchEmpty(
                cx, cy, w, h, primary, surfaceVariant, onSurface, tertiary
            )
            EmptyStateType.OFFLINE -> drawOffline(
                cx, cy, w, h, primary, secondary, onSurface
            )
            EmptyStateType.NO_FAVORITES -> drawNoFavorites(
                cx, cy, w, h, surfaceVariant, primary, secondary, floatOffset
            )
            EmptyStateType.NO_DOWNLOADS -> drawNoDownloads(
                cx, cy, w, h, surfaceVariant, primary, onSurface
            )
            EmptyStateType.ERROR -> drawError(
                cx, cy, w, h, primary, secondary, onSurface, floatOffset
            )
            EmptyStateType.COMING_SOON -> drawComingSoon(
                cx, cy, w, h, primary, tertiary, secondary, surfaceVariant, shimmerAlpha
            )
            EmptyStateType.EMPTY_COLLECTION -> drawEmptyCollection(
                cx, cy, w, h, surfaceVariant, onSurface, tertiary, shimmerAlpha
            )
            EmptyStateType.CATALOG_LOAD_FAILED -> drawError(
                cx, cy, w, h, primary, secondary, onSurface, floatOffset
            )
            EmptyStateType.NO_CLAN -> drawEmptyCollection(
                cx, cy, w, h, surfaceVariant, onSurface, tertiary, shimmerAlpha
            )
            EmptyStateType.NO_TRADES -> drawEmptyCollection(
                cx, cy, w, h, surfaceVariant, onSurface, tertiary, shimmerAlpha
            )
        }
    }
}

// Empty candy jar
private fun DrawScope.drawNoApps(
    cx: Float, cy: Float, w: Float, h: Float,
    primary: Color, secondary: Color, surfaceVariant: Color, shimmerAlpha: Float
) {
    val jarWidth = w * 0.5f
    val jarHeight = h * 0.6f
    val jarLeft = cx - jarWidth / 2f
    val jarTop = cy - jarHeight / 2f + h * 0.08f

    // Jar body
    drawRoundRect(
        color = surfaceVariant.copy(alpha = 0.5f),
        topLeft = Offset(jarLeft, jarTop),
        size = Size(jarWidth, jarHeight),
        cornerRadius = CornerRadius(jarWidth * 0.15f)
    )
    drawRoundRect(
        color = primary.copy(alpha = 0.3f),
        topLeft = Offset(jarLeft, jarTop),
        size = Size(jarWidth, jarHeight),
        cornerRadius = CornerRadius(jarWidth * 0.15f),
        style = Stroke(width = 3f)
    )

    // Jar lid
    val lidWidth = jarWidth * 1.15f
    val lidHeight = h * 0.08f
    drawRoundRect(
        color = secondary.copy(alpha = 0.6f),
        topLeft = Offset(cx - lidWidth / 2f, jarTop - lidHeight),
        size = Size(lidWidth, lidHeight),
        cornerRadius = CornerRadius(lidHeight * 0.4f)
    )

    // Lid knob
    drawCircle(
        color = secondary.copy(alpha = 0.8f),
        radius = lidHeight * 0.5f,
        center = Offset(cx, jarTop - lidHeight)
    )

    // Shimmer highlight inside jar
    drawRoundRect(
        color = primary.copy(alpha = shimmerAlpha * 0.15f),
        topLeft = Offset(jarLeft + jarWidth * 0.15f, jarTop + jarHeight * 0.2f),
        size = Size(jarWidth * 0.3f, jarHeight * 0.6f),
        cornerRadius = CornerRadius(jarWidth * 0.1f)
    )

    // Glass reflection line
    drawLine(
        color = Color.White.copy(alpha = 0.3f),
        start = Offset(jarLeft + jarWidth * 0.2f, jarTop + jarHeight * 0.15f),
        end = Offset(jarLeft + jarWidth * 0.2f, jarTop + jarHeight * 0.75f),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )
}

// Gray magic wand with dim sparkles
private fun DrawScope.drawNoEffects(
    cx: Float, cy: Float, w: Float, h: Float,
    onSurface: Color, surfaceVariant: Color, tertiary: Color, shimmerAlpha: Float
) {
    val wandColor = onSurface.copy(alpha = 0.25f)

    // Wand stick (diagonal)
    val wandStart = Offset(cx - w * 0.22f, cy + h * 0.22f)
    val wandEnd = Offset(cx + w * 0.22f, cy - h * 0.22f)
    drawLine(
        color = wandColor,
        start = wandStart,
        end = wandEnd,
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )

    // Wand tip
    drawCircle(
        color = surfaceVariant.copy(alpha = 0.6f),
        radius = w * 0.05f,
        center = wandEnd
    )

    // Dim sparkles around tip
    val sparkleColor = tertiary.copy(alpha = shimmerAlpha * 0.5f)
    val sparkleOffsets = listOf(
        Offset(wandEnd.x - w * 0.08f, wandEnd.y - h * 0.1f),
        Offset(wandEnd.x + w * 0.1f, wandEnd.y - h * 0.06f),
        Offset(wandEnd.x + w * 0.04f, wandEnd.y - h * 0.14f),
        Offset(wandEnd.x - w * 0.12f, wandEnd.y + h * 0.02f),
        Offset(wandEnd.x + w * 0.14f, wandEnd.y + h * 0.04f)
    )

    sparkleOffsets.forEachIndexed { i, offset ->
        val sparkSize = if (i % 2 == 0) w * 0.06f else w * 0.04f
        // Vertical line of star
        drawLine(
            color = sparkleColor,
            start = Offset(offset.x, offset.y - sparkSize / 2f),
            end = Offset(offset.x, offset.y + sparkSize / 2f),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
        // Horizontal line of star
        drawLine(
            color = sparkleColor,
            start = Offset(offset.x - sparkSize / 2f, offset.y),
            end = Offset(offset.x + sparkSize / 2f, offset.y),
            strokeWidth = 1.5f,
            cap = StrokeCap.Round
        )
    }
}

// Unwrapped empty candy wrapper
private fun DrawScope.drawNoRewards(
    cx: Float, cy: Float, w: Float, h: Float,
    primary: Color, secondary: Color, surfaceVariant: Color, onSurface: Color
) {
    val wrapperColor = surfaceVariant.copy(alpha = 0.5f)
    val stripeColor = primary.copy(alpha = 0.2f)

    // Wrapper body (flat rectangle in center)
    val bodyWidth = w * 0.3f
    val bodyHeight = h * 0.2f
    drawRoundRect(
        color = wrapperColor,
        topLeft = Offset(cx - bodyWidth / 2f, cy - bodyHeight / 2f),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(4f)
    )

    // Wrapper stripes
    for (i in 0..2) {
        val x = cx - bodyWidth / 2f + bodyWidth * (i + 1) / 4f
        drawLine(
            color = stripeColor,
            start = Offset(x, cy - bodyHeight / 2f),
            end = Offset(x, cy + bodyHeight / 2f),
            strokeWidth = 1.5f
        )
    }

    // Left twist
    val twistPath = Path().apply {
        moveTo(cx - bodyWidth / 2f, cy - bodyHeight * 0.3f)
        lineTo(cx - bodyWidth / 2f - w * 0.18f, cy - bodyHeight * 0.6f)
        lineTo(cx - bodyWidth / 2f - w * 0.18f, cy + bodyHeight * 0.6f)
        lineTo(cx - bodyWidth / 2f, cy + bodyHeight * 0.3f)
        close()
    }
    drawPath(path = twistPath, color = secondary.copy(alpha = 0.25f))
    drawPath(path = twistPath, color = onSurface.copy(alpha = 0.15f), style = Stroke(1.5f))

    // Right twist
    val rightTwist = Path().apply {
        moveTo(cx + bodyWidth / 2f, cy - bodyHeight * 0.3f)
        lineTo(cx + bodyWidth / 2f + w * 0.18f, cy - bodyHeight * 0.6f)
        lineTo(cx + bodyWidth / 2f + w * 0.18f, cy + bodyHeight * 0.6f)
        lineTo(cx + bodyWidth / 2f, cy + bodyHeight * 0.3f)
        close()
    }
    drawPath(path = rightTwist, color = secondary.copy(alpha = 0.25f))
    drawPath(path = rightTwist, color = onSurface.copy(alpha = 0.15f), style = Stroke(1.5f))

    // Crinkle lines on twists
    drawLine(
        color = onSurface.copy(alpha = 0.1f),
        start = Offset(cx - bodyWidth / 2f - w * 0.06f, cy - bodyHeight * 0.2f),
        end = Offset(cx - bodyWidth / 2f - w * 0.12f, cy + bodyHeight * 0.2f),
        strokeWidth = 1f
    )
    drawLine(
        color = onSurface.copy(alpha = 0.1f),
        start = Offset(cx + bodyWidth / 2f + w * 0.06f, cy - bodyHeight * 0.2f),
        end = Offset(cx + bodyWidth / 2f + w * 0.12f, cy + bodyHeight * 0.2f),
        strokeWidth = 1f
    )
}

// Magnifying glass over scattered candy crumbs
private fun DrawScope.drawSearchEmpty(
    cx: Float, cy: Float, w: Float, h: Float,
    primary: Color, surfaceVariant: Color, onSurface: Color, tertiary: Color
) {
    // Candy crumbs (small dots scattered below)
    val crumbColor = tertiary.copy(alpha = 0.3f)
    val crumbs = listOf(
        Offset(cx - w * 0.2f, cy + h * 0.15f) to w * 0.02f,
        Offset(cx + w * 0.05f, cy + h * 0.2f) to w * 0.025f,
        Offset(cx + w * 0.2f, cy + h * 0.12f) to w * 0.015f,
        Offset(cx - w * 0.08f, cy + h * 0.25f) to w * 0.018f,
        Offset(cx + w * 0.15f, cy + h * 0.28f) to w * 0.02f,
        Offset(cx - w * 0.15f, cy + h * 0.3f) to w * 0.012f
    )
    crumbs.forEach { (offset, radius) ->
        drawCircle(color = crumbColor, radius = radius, center = offset)
    }

    // Magnifying glass lens
    val lensCenter = Offset(cx - w * 0.04f, cy - h * 0.06f)
    val lensRadius = w * 0.2f
    drawCircle(
        color = surfaceVariant.copy(alpha = 0.3f),
        radius = lensRadius,
        center = lensCenter
    )
    drawCircle(
        color = primary.copy(alpha = 0.5f),
        radius = lensRadius,
        center = lensCenter,
        style = Stroke(width = 4f)
    )

    // Glass reflection
    drawArc(
        color = Color.White.copy(alpha = 0.2f),
        startAngle = 200f,
        sweepAngle = 60f,
        useCenter = false,
        topLeft = Offset(lensCenter.x - lensRadius * 0.7f, lensCenter.y - lensRadius * 0.7f),
        size = Size(lensRadius * 1.4f, lensRadius * 1.4f),
        style = Stroke(width = 2f, cap = StrokeCap.Round)
    )

    // Handle
    val handleStart = Offset(
        lensCenter.x + lensRadius * 0.65f,
        lensCenter.y + lensRadius * 0.65f
    )
    val handleEnd = Offset(
        handleStart.x + w * 0.15f,
        handleStart.y + h * 0.15f
    )
    drawLine(
        color = onSurface.copy(alpha = 0.35f),
        start = handleStart,
        end = handleEnd,
        strokeWidth = 6f,
        cap = StrokeCap.Round
    )
}

// Broken candy cane
private fun DrawScope.drawOffline(
    cx: Float, cy: Float, w: Float, h: Float,
    primary: Color, secondary: Color, onSurface: Color
) {
    val caneWidth = w * 0.08f
    val breakGap = w * 0.04f

    // Left piece (top with hook + upper part of shaft)
    val hookPath = Path().apply {
        // Hook curve
        moveTo(cx - caneWidth / 2f, cy - h * 0.06f)
        lineTo(cx - caneWidth / 2f, cy - h * 0.2f)
        cubicTo(
            cx - caneWidth / 2f, cy - h * 0.35f,
            cx + w * 0.12f, cy - h * 0.35f,
            cx + w * 0.12f, cy - h * 0.22f
        )
        // Outer edge of hook
        cubicTo(
            cx + w * 0.12f, cy - h * 0.18f,
            cx + w * 0.06f, cy - h * 0.18f,
            cx + caneWidth / 2f, cy - h * 0.2f
        )
        lineTo(cx + caneWidth / 2f, cy - h * 0.06f)
        // Jagged break edge
        lineTo(cx + caneWidth / 2f - breakGap * 0.3f, cy - h * 0.04f)
        lineTo(cx, cy - h * 0.02f)
        lineTo(cx - caneWidth / 2f + breakGap * 0.3f, cy - h * 0.05f)
        close()
    }
    drawPath(path = hookPath, color = primary.copy(alpha = 0.6f))

    // Stripes on left piece
    for (y in 0..2) {
        val yPos = cy - h * 0.2f + y * caneWidth * 1.5f
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(cx - caneWidth / 2f, yPos),
            end = Offset(cx + caneWidth / 2f, yPos),
            strokeWidth = 2f
        )
    }

    // Right piece (lower shaft), offset slightly right and down
    val offsetX = w * 0.06f
    val offsetY = h * 0.04f
    val lowerPath = Path().apply {
        // Jagged top edge
        moveTo(cx - caneWidth / 2f + offsetX + breakGap * 0.3f, cy + offsetY)
        lineTo(cx + offsetX, cy + h * 0.02f + offsetY)
        lineTo(cx + caneWidth / 2f + offsetX - breakGap * 0.3f, cy - h * 0.01f + offsetY)
        lineTo(cx + caneWidth / 2f + offsetX, cy + offsetY)
        // Shaft down
        lineTo(cx + caneWidth / 2f + offsetX, cy + h * 0.25f + offsetY)
        // Rounded bottom
        lineTo(cx - caneWidth / 2f + offsetX, cy + h * 0.25f + offsetY)
        lineTo(cx - caneWidth / 2f + offsetX, cy + offsetY)
        close()
    }
    drawPath(path = lowerPath, color = secondary.copy(alpha = 0.5f))

    // Stripes on lower piece
    for (y in 0..1) {
        val yPos = cy + h * 0.06f + offsetY + y * caneWidth * 1.5f
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = Offset(cx - caneWidth / 2f + offsetX, yPos),
            end = Offset(cx + caneWidth / 2f + offsetX, yPos),
            strokeWidth = 2f
        )
    }

    // Small crack lines
    drawLine(
        color = onSurface.copy(alpha = 0.15f),
        start = Offset(cx - breakGap, cy),
        end = Offset(cx + breakGap, cy + h * 0.01f),
        strokeWidth = 1f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(3f, 3f))
    )
}

// Empty star outline with a small heart
private fun DrawScope.drawNoFavorites(
    cx: Float, cy: Float, w: Float, h: Float,
    surfaceVariant: Color, primary: Color, secondary: Color, floatOffset: Float
) {
    val starRadius = w * 0.28f
    val innerRadius = starRadius * 0.4f
    val adjustedCy = cy + floatOffset

    // 5-pointed star path
    val starPath = Path().apply {
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) starRadius else innerRadius
            val angle = Math.toRadians((i * 36.0 - 90.0)).toFloat()
            val px = cx + r * kotlin.math.cos(angle)
            val py = adjustedCy + r * kotlin.math.sin(angle)
            if (i == 0) moveTo(px, py) else lineTo(px, py)
        }
        close()
    }

    drawPath(
        path = starPath,
        color = surfaceVariant.copy(alpha = 0.3f)
    )
    drawPath(
        path = starPath,
        color = primary.copy(alpha = 0.4f),
        style = Stroke(width = 2.5f, join = StrokeJoin.Round)
    )

    // Small heart near bottom-right of star
    val heartCx = cx + w * 0.12f
    val heartCy = adjustedCy + h * 0.1f
    val heartSize = w * 0.06f
    val heartPath = Path().apply {
        moveTo(heartCx, heartCy + heartSize * 0.3f)
        cubicTo(
            heartCx - heartSize, heartCy - heartSize * 0.5f,
            heartCx - heartSize * 0.5f, heartCy - heartSize,
            heartCx, heartCy - heartSize * 0.3f
        )
        cubicTo(
            heartCx + heartSize * 0.5f, heartCy - heartSize,
            heartCx + heartSize, heartCy - heartSize * 0.5f,
            heartCx, heartCy + heartSize * 0.3f
        )
    }
    drawPath(path = heartPath, color = secondary.copy(alpha = 0.6f))
}

// Arrow pointing down into empty box
private fun DrawScope.drawNoDownloads(
    cx: Float, cy: Float, w: Float, h: Float,
    surfaceVariant: Color, primary: Color, onSurface: Color
) {
    val boxWidth = w * 0.45f
    val boxHeight = h * 0.25f
    val boxTop = cy + h * 0.05f
    val boxLeft = cx - boxWidth / 2f

    // Open box (3-sided rect, no top)
    val boxPath = Path().apply {
        moveTo(boxLeft, boxTop)
        lineTo(boxLeft, boxTop + boxHeight)
        lineTo(boxLeft + boxWidth, boxTop + boxHeight)
        lineTo(boxLeft + boxWidth, boxTop)
    }
    drawPath(
        path = boxPath,
        color = surfaceVariant.copy(alpha = 0.5f),
        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Box flaps (small angled lines at top opening)
    drawLine(
        color = surfaceVariant.copy(alpha = 0.4f),
        start = Offset(boxLeft, boxTop),
        end = Offset(boxLeft - w * 0.04f, boxTop - h * 0.04f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = surfaceVariant.copy(alpha = 0.4f),
        start = Offset(boxLeft + boxWidth, boxTop),
        end = Offset(boxLeft + boxWidth + w * 0.04f, boxTop - h * 0.04f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )

    // Down arrow shaft
    val arrowTop = cy - h * 0.25f
    val arrowBottom = boxTop - h * 0.03f
    drawLine(
        color = primary.copy(alpha = 0.4f),
        start = Offset(cx, arrowTop),
        end = Offset(cx, arrowBottom),
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // Arrow head
    val arrowHeadSize = w * 0.06f
    val arrowHeadPath = Path().apply {
        moveTo(cx, arrowBottom)
        lineTo(cx - arrowHeadSize, arrowBottom - arrowHeadSize)
        moveTo(cx, arrowBottom)
        lineTo(cx + arrowHeadSize, arrowBottom - arrowHeadSize)
    }
    drawPath(
        path = arrowHeadPath,
        color = primary.copy(alpha = 0.4f),
        style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Dashed line inside box (empty indicator)
    drawLine(
        color = onSurface.copy(alpha = 0.08f),
        start = Offset(boxLeft + boxWidth * 0.2f, boxTop + boxHeight * 0.6f),
        end = Offset(boxLeft + boxWidth * 0.8f, boxTop + boxHeight * 0.6f),
        strokeWidth = 1.5f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f))
    )
}

// Melted candy with worried face
private fun DrawScope.drawError(
    cx: Float, cy: Float, w: Float, h: Float,
    primary: Color, secondary: Color, onSurface: Color, floatOffset: Float
) {
    val adjustedCy = cy + floatOffset * 0.5f

    // Melted candy body (droopy oval)
    val candyPath = Path().apply {
        moveTo(cx - w * 0.2f, adjustedCy - h * 0.1f)
        // Top curve
        cubicTo(
            cx - w * 0.2f, adjustedCy - h * 0.22f,
            cx + w * 0.2f, adjustedCy - h * 0.22f,
            cx + w * 0.2f, adjustedCy - h * 0.1f
        )
        // Right drip
        cubicTo(
            cx + w * 0.22f, adjustedCy + h * 0.05f,
            cx + w * 0.15f, adjustedCy + h * 0.12f,
            cx + w * 0.18f, adjustedCy + h * 0.2f
        )
        // Bottom drips
        cubicTo(
            cx + w * 0.16f, adjustedCy + h * 0.25f,
            cx + w * 0.08f, adjustedCy + h * 0.15f,
            cx, adjustedCy + h * 0.18f
        )
        cubicTo(
            cx - w * 0.08f, adjustedCy + h * 0.2f,
            cx - w * 0.16f, adjustedCy + h * 0.25f,
            cx - w * 0.18f, adjustedCy + h * 0.18f
        )
        // Left side back up
        cubicTo(
            cx - w * 0.22f, adjustedCy + h * 0.08f,
            cx - w * 0.22f, adjustedCy + h * 0.02f,
            cx - w * 0.2f, adjustedCy - h * 0.1f
        )
        close()
    }
    drawPath(path = candyPath, color = primary.copy(alpha = 0.35f))
    drawPath(
        path = candyPath,
        color = secondary.copy(alpha = 0.3f),
        style = Stroke(width = 2f)
    )

    // Worried eyes (dots)
    val eyeY = adjustedCy - h * 0.1f
    drawCircle(
        color = onSurface.copy(alpha = 0.5f),
        radius = w * 0.025f,
        center = Offset(cx - w * 0.07f, eyeY)
    )
    drawCircle(
        color = onSurface.copy(alpha = 0.5f),
        radius = w * 0.025f,
        center = Offset(cx + w * 0.07f, eyeY)
    )

    // Worried mouth (small wavy line)
    val mouthPath = Path().apply {
        moveTo(cx - w * 0.06f, adjustedCy + h * 0.02f)
        cubicTo(
            cx - w * 0.03f, adjustedCy + h * 0.05f,
            cx + w * 0.03f, adjustedCy - h * 0.01f,
            cx + w * 0.06f, adjustedCy + h * 0.02f
        )
    }
    drawPath(
        path = mouthPath,
        color = onSurface.copy(alpha = 0.4f),
        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
    )
}

// Gift box with question mark ribbon
private fun DrawScope.drawComingSoon(
    cx: Float, cy: Float, w: Float, h: Float,
    primary: Color, tertiary: Color, secondary: Color, surfaceVariant: Color,
    shimmerAlpha: Float
) {
    val boxWidth = w * 0.45f
    val boxHeight = h * 0.3f
    val boxLeft = cx - boxWidth / 2f
    val boxTop = cy - h * 0.02f

    // Box body
    drawRoundRect(
        color = primary.copy(alpha = 0.2f),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(w * 0.02f)
    )
    drawRoundRect(
        color = primary.copy(alpha = 0.4f),
        topLeft = Offset(boxLeft, boxTop),
        size = Size(boxWidth, boxHeight),
        cornerRadius = CornerRadius(w * 0.02f),
        style = Stroke(width = 2f)
    )

    // Box lid
    val lidHeight = h * 0.06f
    drawRoundRect(
        color = primary.copy(alpha = 0.3f),
        topLeft = Offset(boxLeft - w * 0.02f, boxTop - lidHeight),
        size = Size(boxWidth + w * 0.04f, lidHeight),
        cornerRadius = CornerRadius(w * 0.015f)
    )

    // Vertical ribbon
    val ribbonWidth = boxWidth * 0.15f
    drawRect(
        color = tertiary.copy(alpha = shimmerAlpha * 0.6f),
        topLeft = Offset(cx - ribbonWidth / 2f, boxTop - lidHeight),
        size = Size(ribbonWidth, boxHeight + lidHeight)
    )

    // Horizontal ribbon
    drawRect(
        color = tertiary.copy(alpha = shimmerAlpha * 0.6f),
        topLeft = Offset(boxLeft, boxTop + boxHeight * 0.4f),
        size = Size(boxWidth, ribbonWidth * 0.8f)
    )

    // Bow on top (two loops)
    val bowY = boxTop - lidHeight
    val bowRadius = w * 0.06f
    drawArc(
        color = tertiary.copy(alpha = 0.5f),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx - bowRadius * 2.2f, bowY - bowRadius),
        size = Size(bowRadius * 2.2f, bowRadius * 1.5f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
    drawArc(
        color = tertiary.copy(alpha = 0.5f),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(cx, bowY - bowRadius),
        size = Size(bowRadius * 2.2f, bowRadius * 1.5f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )

    // Question mark in center of box
    val qPath = Path().apply {
        moveTo(cx - w * 0.04f, boxTop + boxHeight * 0.25f)
        cubicTo(
            cx - w * 0.04f, boxTop + boxHeight * 0.15f,
            cx + w * 0.04f, boxTop + boxHeight * 0.15f,
            cx + w * 0.04f, boxTop + boxHeight * 0.35f
        )
        cubicTo(
            cx + w * 0.04f, boxTop + boxHeight * 0.45f,
            cx, boxTop + boxHeight * 0.45f,
            cx, boxTop + boxHeight * 0.55f
        )
    }
    drawPath(
        path = qPath,
        color = secondary.copy(alpha = 0.5f),
        style = Stroke(width = 2.5f, cap = StrokeCap.Round)
    )
    drawCircle(
        color = secondary.copy(alpha = 0.5f),
        radius = 2f,
        center = Offset(cx, boxTop + boxHeight * 0.7f)
    )
}

// Empty shelf with dust particles
private fun DrawScope.drawEmptyCollection(
    cx: Float, cy: Float, w: Float, h: Float,
    surfaceVariant: Color, onSurface: Color, tertiary: Color, shimmerAlpha: Float
) {
    val shelfColor = surfaceVariant.copy(alpha = 0.5f)
    val shelfWidth = w * 0.7f
    val shelfLeft = cx - shelfWidth / 2f

    // Three shelf boards
    for (i in 0..2) {
        val shelfY = cy - h * 0.15f + i * h * 0.18f

        // Shelf surface
        drawRoundRect(
            color = shelfColor,
            topLeft = Offset(shelfLeft, shelfY),
            size = Size(shelfWidth, h * 0.03f),
            cornerRadius = CornerRadius(2f)
        )

        // Shelf bracket left
        drawLine(
            color = onSurface.copy(alpha = 0.15f),
            start = Offset(shelfLeft + w * 0.04f, shelfY + h * 0.03f),
            end = Offset(shelfLeft + w * 0.02f, shelfY + h * 0.08f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        // Shelf bracket right
        drawLine(
            color = onSurface.copy(alpha = 0.15f),
            start = Offset(shelfLeft + shelfWidth - w * 0.04f, shelfY + h * 0.03f),
            end = Offset(shelfLeft + shelfWidth - w * 0.02f, shelfY + h * 0.08f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )
    }

    // Dust particles (floating dots)
    val dustParticles = listOf(
        Offset(cx - w * 0.15f, cy - h * 0.08f),
        Offset(cx + w * 0.2f, cy + h * 0.04f),
        Offset(cx - w * 0.08f, cy + h * 0.12f),
        Offset(cx + w * 0.1f, cy - h * 0.02f),
        Offset(cx + w * 0.25f, cy + h * 0.15f),
        Offset(cx - w * 0.22f, cy + h * 0.08f),
        Offset(cx, cy + h * 0.2f)
    )
    dustParticles.forEachIndexed { i, offset ->
        val alpha = if (i % 2 == 0) shimmerAlpha * 0.3f else (1f - shimmerAlpha) * 0.3f
        val radius = if (i % 3 == 0) 2f else 1.5f
        drawCircle(
            color = tertiary.copy(alpha = alpha),
            radius = radius,
            center = offset
        )
    }
}

private fun defaultContent(type: EmptyStateType): Pair<String, String> = when (type) {
    EmptyStateType.NO_APPS -> "No Apps Found" to
            "Your candy jar is empty. Browse the catalog to find sweet apps!"
    EmptyStateType.NO_EFFECTS -> "No Active Effects" to
            "Tap an effect to add some sugar to your experience!"
    EmptyStateType.NO_REWARDS -> "No Rewards Yet" to
            "Check back later for sweet surprises!"
    EmptyStateType.SEARCH_EMPTY -> "Nothing Found" to
            "Try different keywords or browse categories"
    EmptyStateType.OFFLINE -> "You're Offline" to
            "Check your connection and try again"
    EmptyStateType.NO_FAVORITES -> "No Favorites" to
            "Star your favorite apps to find them here"
    EmptyStateType.NO_DOWNLOADS -> "No Downloads" to
            "Your download queue is empty"
    EmptyStateType.ERROR -> "Oops!" to
            "Something went wrong. Please try again."
    EmptyStateType.COMING_SOON -> "Coming Soon!" to
            "This sweet feature is still being baked"
    EmptyStateType.EMPTY_COLLECTION -> "Empty Collection" to
            "Start collecting to fill your shelf!"
    EmptyStateType.CATALOG_LOAD_FAILED -> "Catalog Unavailable" to
            "We couldn't load the app list. Check your connection and try again."
    EmptyStateType.NO_CLAN -> "No Clan" to
            "Join or create a clan to team up with other candy fans!"
    EmptyStateType.NO_TRADES -> "No Trades Yet" to
            "Be the first to list a trade or check back later."
}
