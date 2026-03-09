package com.sugarmunch.app.ui.typography

import android.graphics.Paint as NativePaint
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.theme.CandyMint
import com.sugarmunch.app.ui.theme.CandyPink
import com.sugarmunch.app.ui.theme.CandyPurple
import com.sugarmunch.app.ui.theme.CottonCandyBlue
import kotlinx.coroutines.delay

@Composable
fun GradientText(
    text: String,
    gradient: Brush,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = style.copy(brush = gradient),
        modifier = modifier
    )
}

@Composable
fun GlowText(
    text: String,
    color: Color = MaterialTheme.colorScheme.primary,
    glowRadius: Dp = 8.dp,
    glowColor: Color = color.copy(alpha = 0.5f),
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    modifier: Modifier = Modifier
) {
    val offsets = listOf(
        Offset(-1f, -1f), Offset(1f, -1f),
        Offset(-1f, 1f), Offset(1f, 1f),
        Offset(0f, -1.5f), Offset(0f, 1.5f),
        Offset(-1.5f, 0f), Offset(1.5f, 0f)
    )
    val glowPx = with(LocalDensity.current) { glowRadius.toPx() }

    Box(modifier = modifier) {
        // Glow layers: semi-transparent copies offset in each direction
        offsets.forEach { offset ->
            Text(
                text = text,
                style = style.copy(
                    color = glowColor,
                    shadow = Shadow(
                        color = glowColor,
                        offset = Offset(offset.x * glowPx * 0.5f, offset.y * glowPx * 0.5f),
                        blurRadius = glowPx
                    )
                )
            )
        }
        // Sharp foreground text
        Text(
            text = text,
            style = style.copy(color = color)
        )
    }
}

@Composable
fun OutlinedText(
    text: String,
    fillColor: Color = MaterialTheme.colorScheme.onSurface,
    strokeColor: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Float = 2f,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textLayoutResult = remember(text, style) {
        textMeasurer.measure(text, style)
    }

    val widthDp = with(density) { textLayoutResult.size.width.toDp() }
    val heightDp = with(density) { textLayoutResult.size.height.toDp() }

    Canvas(modifier = modifier.size(width = widthDp, height = heightDp)) {
        drawIntoCanvas { canvas ->
            val nativeCanvas = canvas.nativeCanvas
            val fontSizePx = with(density) { style.fontSize.toPx() }

            val paint = NativePaint().apply {
                isAntiAlias = true
                textSize = fontSizePx
                style.fontWeight?.let { weight ->
                    isFakeBoldText = weight.weight >= 600
                }
            }

            val baseline = textLayoutResult.firstBaseline

            // Stroke pass
            paint.style = NativePaint.Style.STROKE
            paint.color = strokeColor.toArgb()
            paint.strokeWidth = strokeWidth
            nativeCanvas.drawText(text, 0f, baseline, paint)

            // Fill pass
            paint.style = NativePaint.Style.FILL
            paint.color = fillColor.toArgb()
            paint.strokeWidth = 0f
            nativeCanvas.drawText(text, 0f, baseline, paint)
        }
    }
}

@Composable
fun ShadowText(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    shadowColor: Color = Color.Black.copy(alpha = 0.3f),
    shadowOffset: Offset = Offset(2f, 2f),
    shadowBlur: Float = 4f,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = style.copy(
            color = color,
            shadow = Shadow(
                color = shadowColor,
                offset = shadowOffset,
                blurRadius = shadowBlur
            )
        ),
        modifier = modifier
    )
}

@Composable
fun EmbossedText(
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Highlight shadow (top-left, light)
        Text(
            text = text,
            style = style.copy(
                color = color,
                shadow = Shadow(
                    color = Color.White.copy(alpha = 0.6f),
                    offset = Offset(-1f, -1f),
                    blurRadius = 1f
                )
            )
        )
        // Lowlight shadow (bottom-right, dark)
        Text(
            text = text,
            style = style.copy(
                color = color,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.4f),
                    offset = Offset(1f, 1f),
                    blurRadius = 1f
                )
            )
        )
    }
}

@Composable
fun AnimatedGradientText(
    text: String,
    colors: List<Color> = listOf(CandyPink, CandyPurple, CottonCandyBlue, CandyMint),
    style: TextStyle = MaterialTheme.typography.headlineMedium,
    animationDuration: Int = 3000,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradientTextAnim")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradientOffset"
    )

    // Double the colors to create a seamless loop
    val extendedColors = colors + colors
    val textMeasurer = rememberTextMeasurer()
    val textLayoutResult = remember(text, style) {
        textMeasurer.measure(text, style)
    }
    val textWidth = textLayoutResult.size.width.toFloat()

    val animatedBrush = Brush.linearGradient(
        colors = extendedColors,
        start = Offset(x = -textWidth + (offset * textWidth * 2), y = 0f),
        end = Offset(x = offset * textWidth * 2, y = 0f)
    )

    Text(
        text = text,
        style = style.copy(brush = animatedBrush),
        modifier = modifier
    )
}

@Composable
fun TypewriterText(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    charDelay: Long = 50L,
    modifier: Modifier = Modifier
) {
    var visibleCount by remember(text) { mutableStateOf(0) }

    LaunchedEffect(text) {
        visibleCount = 0
        for (i in 1..text.length) {
            delay(charDelay)
            visibleCount = i
        }
    }

    Text(
        text = text.take(visibleCount),
        style = style,
        modifier = modifier
    )
}
