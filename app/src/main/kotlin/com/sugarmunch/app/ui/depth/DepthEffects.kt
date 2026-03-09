package com.sugarmunch.app.ui.depth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


// ─── Neumorphism ─────────────────────────────────────────────────────────────

enum class NeumorphStyle { RAISED, PRESSED, FLAT }

fun Modifier.neumorphic(
    style: NeumorphStyle = NeumorphStyle.RAISED,
    cornerRadius: Dp = 16.dp,
    lightColor: Color = Color.White.copy(alpha = 0.7f),
    darkColor: Color = Color.Black.copy(alpha = 0.15f),
    elevation: Dp = 6.dp
): Modifier = composed {
    this.drawBehind {
        val cornerPx = cornerRadius.toPx()
        val elevPx = elevation.toPx()

        when (style) {
            NeumorphStyle.RAISED -> {
                drawIntoCanvas { canvas ->
                    val lightPaint = Paint().also { p ->
                        p.asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = lightColor.toArgb()
                            setShadowLayer(elevPx, -elevPx / 2, -elevPx / 2, lightColor.toArgb())
                        }
                    }
                    canvas.drawRoundRect(
                        0f, 0f, size.width, size.height,
                        cornerPx, cornerPx, lightPaint
                    )

                    val darkPaint = Paint().also { p ->
                        p.asFrameworkPaint().apply {
                            isAntiAlias = true
                            color = android.graphics.Color.TRANSPARENT
                            setShadowLayer(elevPx, elevPx / 2, elevPx / 2, darkColor.toArgb())
                        }
                    }
                    canvas.drawRoundRect(
                        0f, 0f, size.width, size.height,
                        cornerPx, cornerPx, darkPaint
                    )
                }
            }

            NeumorphStyle.PRESSED -> {
                // Inner shadow approximation using gradient borders
                val borderWidth = elevPx
                // Top-left inner shadow (dark)
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(darkColor, Color.Transparent),
                        start = Offset.Zero,
                        end = Offset(borderWidth * 2, borderWidth * 2)
                    ),
                    cornerRadius = CornerRadius(cornerPx),
                    size = size
                )
                // Bottom-right inner shadow (light)
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color.Transparent, lightColor),
                        start = Offset(size.width - borderWidth * 2, size.height - borderWidth * 2),
                        end = Offset(size.width, size.height)
                    ),
                    cornerRadius = CornerRadius(cornerPx),
                    size = size
                )
            }

            NeumorphStyle.FLAT -> {
                drawRoundRect(
                    color = Color.Gray.copy(alpha = 0.05f),
                    cornerRadius = CornerRadius(cornerPx),
                    size = size
                )
            }
        }
    }
}

@Composable
fun NeumorphicSurface(
    style: NeumorphStyle = NeumorphStyle.RAISED,
    cornerRadius: Dp = 16.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.neumorphic(style = style, cornerRadius = cornerRadius)
    ) {
        content()
    }
}

// ─── Depth Blur ──────────────────────────────────────────────────────────────

fun Modifier.depthBlur(
    scrollState: ScrollState,
    maxBlur: Float = 10f
): Modifier = composed {
    val scrollFraction = (scrollState.value / 600f).coerceIn(0f, 1f)
    val overlayAlpha = scrollFraction * (maxBlur / 10f) * 0.6f

    this
        .graphicsLayer { alpha = 1f - (overlayAlpha * 0.3f).coerceIn(0f, 0.5f) }
        .drawBehind {
            drawRect(color = Color.White.copy(alpha = overlayAlpha.coerceIn(0f, 0.8f)))
        }
}

@Composable
fun DepthBlurHeader(
    scrollState: ScrollState,
    headerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollFraction = (scrollState.value / 400f).coerceIn(0f, 1f)
    val headerAlpha by animateFloatAsState(
        targetValue = 1f - scrollFraction * 0.7f,
        animationSpec = tween(100),
        label = "header_alpha"
    )
    val frostAlpha by animateFloatAsState(
        targetValue = scrollFraction * 0.6f,
        animationSpec = tween(100),
        label = "frost_alpha"
    )

    Box(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.graphicsLayer { alpha = headerAlpha }) {
            headerContent()
        }
        // Frosted overlay
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = frostAlpha))
        )
    }
}

// ─── Dynamic Elevation ───────────────────────────────────────────────────────

fun Modifier.dynamicElevation(
    defaultElevation: Dp = 2.dp,
    pressedElevation: Dp = 8.dp,
    shadowColor: Color = Color.Black
): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (pressed) pressedElevation else defaultElevation,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "dyn_elev"
    )
    this
        .shadow(
            elevation = elevation,
            shape = RoundedCornerShape(12.dp),
            ambientColor = shadowColor,
            spotColor = shadowColor
        )
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    pressed = true
                    tryAwaitRelease()
                    pressed = false
                }
            )
        }
}

fun Modifier.coloredShadow(
    color: Color,
    alpha: Float = 0.3f,
    borderRadius: Dp = 12.dp,
    shadowRadius: Dp = 8.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 4.dp
): Modifier = composed {
    this.drawBehind {
        drawIntoCanvas { canvas ->
            val paint = Paint().also { p ->
                p.asFrameworkPaint().apply {
                    isAntiAlias = true
                    this.color = android.graphics.Color.TRANSPARENT
                    setShadowLayer(
                        shadowRadius.toPx(),
                        offsetX.toPx(),
                        offsetY.toPx(),
                        color.copy(alpha = alpha).toArgb()
                    )
                }
            }
            canvas.drawRoundRect(
                0f,
                0f,
                size.width,
                size.height,
                borderRadius.toPx(),
                borderRadius.toPx(),
                paint
            )
        }
    }
}

// ─── Enhanced 3D Card ────────────────────────────────────────────────────────

@Composable
fun ThreeDCard(
    modifier: Modifier = Modifier,
    maxRotation: Float = 15f,
    content: @Composable () -> Unit
) {
    var rotationX by remember { mutableFloatStateOf(0f) }
    var rotationY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    val animRotX by animateFloatAsState(
        targetValue = if (isDragging) rotationX else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "3d_rotX"
    )
    val animRotY by animateFloatAsState(
        targetValue = if (isDragging) rotationY else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "3d_rotY"
    )

    // Light reflection gradient shifts based on rotation
    val lightOffsetX = animRotY / maxRotation
    val lightOffsetY = -animRotX / maxRotation

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false }
                ) { change, _ ->
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val px = change.position.x
                    val py = change.position.y

                    rotationX = ((centerY - py) / centerY * maxRotation).coerceIn(-maxRotation, maxRotation)
                    rotationY = ((px - centerX) / centerX * maxRotation).coerceIn(-maxRotation, maxRotation)
                }
            }
            .graphicsLayer {
                this.rotationX = animRotX
                this.rotationY = animRotY
                cameraDistance = 12f * density
            }
    ) {
        content()

        // Edge light reflection overlay
        val lightIntensity = (lightOffsetX * lightOffsetX + lightOffsetY * lightOffsetY).coerceAtMost(1f)
        Box(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f * lightIntensity),
                                Color.Transparent
                            ),
                            center = Offset(
                                x = size.width * (0.5f + lightOffsetX * 0.3f),
                                y = size.height * (0.5f + lightOffsetY * 0.3f)
                            ),
                            radius = size.minDimension * 0.8f
                        )
                    )
                }
        )
    }
}

@Composable
fun FloatingCard(
    modifier: Modifier = Modifier,
    floatAmount: Dp = 4.dp,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )
    val shadowElevation by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_shadow"
    )

    Box(
        modifier = modifier
            .shadow(shadowElevation.dp, RoundedCornerShape(12.dp))
            .graphicsLayer {
                translationY = floatOffset * floatAmount.toPx()
            }
    ) {
        content()
    }
}
