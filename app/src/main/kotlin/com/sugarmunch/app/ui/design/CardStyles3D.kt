package com.sugarmunch.app.ui.design

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Enhanced 3D Card Styles for Phase 2
 * Includes:
 * - 3D flip animations
 * - Parallax tilt on gyroscope
 * - 3D perspective transforms
 * - Depth shadows with dynamic lighting
 * - Refraction/glass bending effects
 */

/**
 * 3D Card configuration
 */
data class Card3DConfig(
    val enableParallax: Boolean = true,
    val enableGyroscope: Boolean = true,
    val maxTiltDegrees: Float = 15f,
    val perspectiveDistance: Float = 8f,
    val shadowDepth: Float = 16f,
    val enableDynamicLighting: Boolean = true,
    val enableRefraction: Boolean = false,
    val flipEnabled: Boolean = false,
    val flipOnTap: Boolean = true,
    val flipDurationMs: Int = 600
)

/**
 * Extended card styles with 3D effects
 */
enum class CardStyle3D {
    FLIP_3D,
    PARALLAX_TILT,
    DEPTH_SHADOW,
    REFRACTION_GLASS,
    PERSPECTIVE_CUBE,
    HOLOGRAPHIC_3D,
    FLOATING_CARD,
    MORPHING_CARD
}

/**
 * Main 3D card composable with configuration
 */
@Composable
fun SugarCard3D(
    modifier: Modifier = Modifier,
    style: CardStyle3D = CardStyle3D.PARALLAX_TILT,
    config: Card3DConfig = Card3DConfig(),
    onClick: (() -> Unit)? = null,
    frontContent: @Composable ColumnScope.() -> Unit,
    backContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    when (style) {
        CardStyle3D.FLIP_3D -> FlipCard3D(
            modifier = modifier,
            config = config,
            onClick = onClick,
            frontContent = frontContent,
            backContent = backContent
        )
        CardStyle3D.PARALLAX_TILT -> ParallaxTiltCard(
            modifier = modifier,
            config = config,
            onClick = onClick,
            content = frontContent
        )
        CardStyle3D.DEPTH_SHADOW -> DepthShadowCard(
            modifier = modifier,
            config = config,
            onClick = onClick,
            content = frontContent
        )
        CardStyle3D.REFRACTION_GLASS -> RefractionGlassCard(
            modifier = modifier,
            config = config,
            onClick = onClick,
            content = frontContent
        )
        CardStyle3D.PERSPECTIVE_CUBE -> PerspectiveCubeCard(
            modifier = modifier,
            config = config,
            onClick = onClick,
            content = frontContent
        )
        CardStyle3D.HOLOGRAPHIC_3D -> Holographic3DCard(
            modifier = modifier,
            config = config,
            onClick = onClick,
            content = frontContent
        )
        CardStyle3D.FLOATING_CARD -> FloatingCard(
            modifier = modifier,
            config = config,
            onClick = onClick,
            content = frontContent
        )
        CardStyle3D.MORPHING_CARD -> MorphingCard(
            modifier = modifier,
            config = config,
            onClick = onClick,
            content = frontContent
        )
    }
}

/**
 * 3D Flip Card - Flips on tap with front and back content
 */
@Composable
private fun FlipCard3D(
    modifier: Modifier = Modifier,
    config: Card3DConfig,
    onClick: (() -> Unit)?,
    frontContent: @Composable ColumnScope.() -> Unit,
    backContent: (@Composable ColumnScope.() -> Unit)?
) {
    var isFlipped by remember { mutableStateOf(false) }
    val rotationY by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffnessness = Spring.StiffnessLow
        ),
        label = "flip_rotation"
    )

    val shape = RoundedCornerShape(SugarDimens.Radius.lg)

    Box(
        modifier = modifier
            .graphicsLayer {
                this.rotationY = rotationY
                this.cameraDistance = config.perspectiveDistance * density
            }
            .pointerInput(Unit) {
                if (config.flipOnTap && backContent != null) {
                    detectTapGestures {
                        isFlipped = !isFlipped
                    }
                }
            }
    ) {
        // Show front or back based on rotation
        if (rotationY < 90f) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = SugarDimens.Elevation.medium
                )
            ) {
                Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
                    frontContent()
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        this.rotationY = 180f
                    },
                shape = shape,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = SugarDimens.Elevation.medium
                )
            ) {
                Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
                    backContent?.invoke(this)
                }
            }
        }
    }
}

/**
 * Parallax Tilt Card - Tilts based on touch/gesture position
 */
@Composable
private fun ParallaxTiltCard(
    modifier: Modifier = Modifier,
    config: Card3DConfig,
    onClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    var tiltX by remember { mutableFloatStateOf(0f) }
    var tiltY by remember { mutableFloatStateOf(0f) }
    var isPressed by remember { mutableStateOf(false) }

    val animatedTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = spring(stiffnessness = Spring.StiffnessLow),
        label = "tilt_x"
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = spring(stiffnessness = Spring.StiffnessLow),
        label = "tilt_y"
    )

    val shape = RoundedCornerShape(SugarDimens.Radius.lg)

    val interactionSource = remember { MutableInteractionSource() }
    val pressState by interactionSource.collectIsPressedAsState()
    isPressed = pressState

    Card(
        modifier = modifier
            .graphicsLayer {
                rotationX = animatedTiltX
                rotationY = animatedTiltY
                cameraDistance = config.perspectiveDistance * density
                // Add scale when pressed
                scaleX = if (isPressed) 1.02f else 1f
                scaleY = if (isPressed) 1.02f else 1f
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tiltX = 0f
                        tiltY = 0f
                        tryAwaitRelease()
                        isPressed = false
                        tiltX = 0f
                        tiltY = 0f
                    },
                    onTap = { onClick?.invoke() }
                )
            }
            .drawBehind {
                // Dynamic lighting based on tilt
                if (config.enableDynamicLighting) {
                    val lightOffsetX = size.width / 2 - (animatedTiltY / config.maxTiltDegrees) * size.width / 4
                    val lightOffsetY = size.height / 2 - (animatedTiltX / config.maxTiltDegrees) * size.height / 4

                    drawRoundRect(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            center = Offset(lightOffsetX, lightOffsetY),
                            radius = size.maxDimension
                        )
                    )
                }
            }
            .shadow(
                elevation = if (isPressed) 8.dp else config.shadowDepth.dp,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
            content()
        }
    }
}

/**
 * Depth Shadow Card - Creates realistic depth with layered shadows
 */
@Composable
private fun DepthShadowCard(
    modifier: Modifier = Modifier,
    config: Card3DConfig,
    onClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else config.shadowDepth.dp,
        animationSpec = spring(stiffnessness = Spring.StiffnessMedium),
        label = "elevation"
    )

    val shape = RoundedCornerShape(SugarDimens.Radius.lg)

    Card(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick?.invoke() }
            )
            .graphicsLayer {
                translationZ = if (isPressed) -2f else 0f
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .drawBehind {
                    // Multi-layer shadow effect
                    for (i in 1..3) {
                        drawRoundRect(
                            color = Color.Black.copy(alpha = 0.02f * i),
                            topLeft = Offset(i * 2.dp.toPx(), i * 2.dp.toPx()),
                            size = Size(size.width - i * 4.dp.toPx(), size.height - i * 4.dp.toPx()),
                            cornerRadius = CornerRadius(SugarDimens.Radius.lg.toPx())
                        )
                    }
                }
                .padding(SugarDimens.Spacing.md)
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * Refraction Glass Card - Glass bending effect with light refraction
 */
@Composable
private fun RefractionGlassCard(
    modifier: Modifier = Modifier,
    config: Card3DConfig,
    onClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.xl)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "refraction")
    val shimmerOffset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    Card(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick?.invoke() }
            )
            .graphicsLayer {
                shape = shape
                shadowElevation = if (isPressed) 8f else 16f
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        )
                    )
                )
                .drawBehind {
                    // Refraction light streak
                    val shimmerX = shimmerOffset * size.width
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            ),
                            start = Offset(shimmerX - size.width / 2, 0f),
                            end = Offset(shimmerX + size.width / 2, size.height)
                        )
                    )

                    // Glass edge highlight
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        topLeft = Offset.Zero,
                        size = Size(size.width, 2.dp.toPx()),
                        cornerRadius = CornerRadius(SugarDimens.Radius.xl.toPx())
                    )
                }
                .padding(SugarDimens.Spacing.lg)
        ) {
            Column {
                content()
            }
        }
    }
}

/**
 * Perspective Cube Card - 3D cube-like perspective
 */
@Composable
private fun PerspectiveCubeCard(
    modifier: Modifier = Modifier,
    config: Card3DConfig,
    onClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = spring(stiffnessness = Spring.StiffnessLow),
        label = "cube_rotation"
    )

    val shape = RoundedCornerShape(SugarDimens.Radius.md)

    Card(
        modifier = modifier
            .clickable(onClick = { onClick?.invoke() })
            .graphicsLayer {
                rotationX = 10f
                rotationY = animatedRotation
                cameraDistance = 12f * density
                // Add perspective distortion
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        rotation = if (rotation == 0f) 15f else 0f
                    }
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = SugarDimens.Elevation.high
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
            content()
        }
    }
}

/**
 * Holographic 3D Card - Holographic effect with 3D depth
 */
@Composable
private fun Holographic3DCard(
    modifier: Modifier = Modifier,
    config: Card3DConfig,
    onClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.lg)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "holo3d")
    val hueShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hue"
    )
    val depth by animateFloatAsState(
        targetValue = if (isPressed) 8f else 16f,
        animationSpec = spring(stiffnessness = Spring.StiffnessLow),
        label = "depth"
    )

    Card(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick?.invoke() }
            )
            .graphicsLayer {
                shadowElevation = depth
                translationZ = if (isPressed) -4f else 0f
            }
            .drawBehind {
                // Holographic rainbow gradient
                drawRoundRect(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFF0080).copy(alpha = 0.6f),
                            Color(0xFF8000FF).copy(alpha = 0.6f),
                            Color(0xFF0080FF).copy(alpha = 0.6f),
                            Color(0xFF00FF80).copy(alpha = 0.6f),
                            Color(0xFFFFFF00).copy(alpha = 0.6f),
                            Color(0xFFFF0080).copy(alpha = 0.6f)
                        )
                    ),
                    alpha = 0.4f + sin(hueShift * PI / 180).toFloat() * 0.2f
                )
            },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

/**
 * Floating Card - Floats with gentle animation
 */
@Composable
private fun FloatingCard(
    modifier: Modifier = Modifier,
    config: Card3DConfig,
    onClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.lg)

    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    val shadowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shadow_alpha"
    )

    Card(
        modifier = modifier
            .clickable(onClick = { onClick?.invoke() })
            .graphicsLayer {
                translationY = floatOffset
            }
            .shadow(
                elevation = 12.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = shadowAlpha),
                spotColor = Color.Black.copy(alpha = shadowAlpha * 2)
            ),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
            content()
        }
    }
}

/**
 * Morphing Card - Shape morphs on interaction
 */
@Composable
private fun MorphingCard(
    modifier: Modifier = Modifier,
    config: Card3DConfig,
    onClick: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) SugarDimens.Radius.xs else SugarDimens.Radius.lg,
        animationSpec = spring(stiffnessness = Spring.StiffnessLow),
        label = "corner_radius"
    )

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffnessness = Spring.StiffnessMedium),
        label = "scale"
    )

    val shape = RoundedCornerShape(cornerRadius)

    Card(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = { onClick?.invoke() }
            )
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isPressed) 4.dp else SugarDimens.Elevation.medium
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
            content()
        }
    }
}

/**
 * Helper extension to create HSV color
 */
fun Color.Companion.Hsv(hue: Float, saturation: Float, value: Float): Color {
    val c = value * saturation
    val x = c * (1 - abs((hue / 60) % 2 - 1))
    val m = value - c

    val (r, g, b) = when {
        hue < 60 -> Triple(c, x, 0f)
        hue < 120 -> Triple(x, c, 0f)
        hue < 180 -> Triple(0f, c, x)
        hue < 240 -> Triple(0f, x, c)
        hue < 300 -> Triple(x, 0f, c)
        else -> Triple(c, 0f, x)
    }

    return Color(
        red = (r + m),
        green = (g + m),
        blue = (b + m)
    )
}
