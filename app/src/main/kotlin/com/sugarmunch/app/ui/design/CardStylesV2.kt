package com.sugarmunch.app.ui.design

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Sugar Card Styles V2 - 10 New Candy-Inspired Card Styles
 * Extreme candy-themed visual treatments for maximum delight
 */

enum class CardStyleV2 {
    CANDY_WRAPPER,    // Pinched rectangle with twisted ends
    LOLLIPOP,         // Circular gradient with stick shadow
    GUMMY,            // Translucent jelly with subsurface scattering
    CHOCOLATE_BAR,    // Segmented rectangle with emboss
    CARAMEL_DRIZZLE,  // Dripping caramel effect
    SPRINKLES,        // Confetti particle overlay
    HARD_CANDY,       // Glassy with refraction
    MARSHMALLOW,      // Soft puffy edges with gradient
    CANDY_CANE,       // Diagonal stripe pattern
    SUGAR_GLASS       // Ultra-transparent frosted glass
}

@Composable
fun SugarCardV2(
    modifier: Modifier = Modifier,
    style: CardStyleV2 = CardStyleV2.CANDY_WRAPPER,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    when (style) {
        CardStyleV2.CANDY_WRAPPER -> CandyWrapperCard(modifier, onClick, content)
        CardStyleV2.LOLLIPOP -> LollipopCard(modifier, onClick, content)
        CardStyleV2.GUMMY -> GummyCard(modifier, onClick, content)
        CardStyleV2.CHOCOLATE_BAR -> ChocolateBarCard(modifier, onClick, content)
        CardStyleV2.CARAMEL_DRIZZLE -> CaramelDrizzleCard(modifier, onClick, content)
        CardStyleV2.SPRINKLES -> SprinklesCard(modifier, onClick, content)
        CardStyleV2.HARD_CANDY -> HardCandyCard(modifier, onClick, content)
        CardStyleV2.MARSHMALLOW -> MarshmallowCard(modifier, onClick, content)
        CardStyleV2.CANDY_CANE -> CandyCaneCard(modifier, onClick, content)
        CardStyleV2.SUGAR_GLASS -> SugarGlassCard(modifier, onClick, content)
    }
}

/**
 * CANDY_WRAPPER - Pinched rectangle with twisted ends like wrapped candy
 */
@Composable
private fun CandyWrapperCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f
    
    val infiniteTransition = rememberInfiniteTransition(label = "wrapper_shimmer")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    
    val shape = RoundedCornerShape(
        topStart = SugarDimens.Radius.pill,
        topEnd = SugarDimens.Radius.pill,
        bottomStart = SugarDimens.Radius.pill,
        bottomEnd = SugarDimens.Radius.pill
    )
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // Wrapper gradient
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            SugarDimens.Brand.hotPink,
                            SugarDimens.Brand.mint,
                            SugarDimens.Brand.yellow
                        )
                    )
                )
                
                // Shimmer overlay
                drawRect(
                    color = Color.White.copy(alpha = shimmerAlpha),
                    blendMode = BlendMode.Overlay
                )
                
                // Twisted ends effect
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(0f, size.height * 0.2f)
                        lineTo(size.width * 0.15f, size.height * 0.3f)
                        lineTo(0f, size.height * 0.4f)
                        close()
                    },
                    color = SugarDimens.Brand.hotPink.copy(alpha = 0.5f)
                )
                drawPath(
                    path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width, size.height * 0.2f)
                        lineTo(size.width * 0.85f, size.height * 0.3f)
                        lineTo(size.width, size.height * 0.4f)
                        close()
                    },
                    color = SugarDimens.Brand.mint.copy(alpha = 0.5f)
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .padding(SugarDimens.Spacing.lg)
                .padding(horizontal = SugarDimens.Spacing.xl)
        ) {
            content()
        }
    }
}

/**
 * LOLLIPOP - Circular gradient with spiral pattern
 */
@Composable
private fun LollipopCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f
    
    val infiniteTransition = rememberInfiniteTransition(label = "lollipop_spin")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin"
    )
    
    val shape = RoundedCornerShape(SugarDimens.Radius.xl)
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // Spiral gradient background
                drawRect(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            SugarDimens.Brand.hotPink,
                            SugarDimens.Brand.bubblegumBlue,
                            SugarDimens.Brand.mint,
                            SugarDimens.Brand.yellow,
                            SugarDimens.Brand.candyOrange,
                            SugarDimens.Brand.hotPink
                        ),
                        center = Offset(size.width / 2, size.height / 2)
                    )
                )
                
                // Rotating spiral lines
                rotate(angle) {
                    repeat(8) { i ->
                        drawCircle(
                            color = Color.White.copy(alpha = 0.3f),
                            radius = size.minDimension * 0.4f,
                            style = Stroke(width = 3f)
                        )
                    }
                }
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.high),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.2f))
                .padding(SugarDimens.Spacing.lg)
        ) {
            content()
        }
    }
}

/**
 * GUMMY - Translucent jelly with soft edges
 */
@Composable
private fun GummyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f
    
    val infiniteTransition = rememberInfiniteTransition(label = "gummy_wobble")
    val wobble by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wobble"
    )
    
    val shape = RoundedCornerShape(SugarDimens.Radius.xl)
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // Translucent gummy base
                drawRect(
                    color = SugarDimens.Brand.hotPink.copy(alpha = 0.4f + wobble * 0.2f)
                )
                
                // Subsurface scattering effect (inner glow)
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            SugarDimens.Brand.hotPink.copy(alpha = 0.3f),
                            SugarDimens.Brand.hotPink.copy(alpha = 0.6f)
                        ),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.maxDimension * 0.7f
                    )
                )
                
                // Glossy highlight
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.8f),
                            Color.White.copy(alpha = 0f)
                        )
                    ),
                    topLeft = Offset(size.width * 0.1f, size.height * 0.1f),
                    size = Size(size.width * 0.4f, size.height * 0.3f)
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

/**
 * CHOCOLATE_BAR - Segmented with embossed squares
 */
@Composable
private fun ChocolateBarCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f
    
    val shape = RoundedCornerShape(SugarDimens.Radius.md)
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // Dark chocolate base
                drawRect(Color(0xFF3E2723))
                
                // Embossed squares (3x4 grid)
                val squareWidth = size.width / 4f
                val squareHeight = size.height / 3f
                
                for (row in 0 until 3) {
                    for (col in 0 until 4) {
                        val left = col * squareWidth + 4f
                        val top = row * squareHeight + 4f
                        val right = (col + 1) * squareWidth - 4f
                        val bottom = (row + 1) * squareHeight - 4f
                        
                        // Shadow (bottom-right)
                        drawRect(
                            color = Color(0xFF1A1A1A),
                            topLeft = Offset(right - 6f, bottom - 6f),
                            size = Size(6f, 6f)
                        )
                        
                        // Highlight (top-left)
                        drawRect(
                            color = Color(0xFF5D4037),
                            topLeft = Offset(left, top),
                            size = Size(6f, 6f)
                        )
                        
                        // Square body
                        drawRect(
                            color = Color(0xFF4E342E),
                            topLeft = Offset(left, top),
                            size = Size(squareWidth - 8f, squareHeight - 8f)
                        )
                    }
                }
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(Color(0xFF3E2723).copy(alpha = 0.9f))
                .padding(SugarDimens.Spacing.lg)
        ) {
            content()
        }
    }
}

/**
 * CARAMEL_DRIZZLE - Dripping caramel from top
 */
@Composable
private fun CaramelDrizzleCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f
    
    val infiniteTransition = rememberInfiniteTransition(label = "drip_animation")
    val dripOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drip"
    )
    
    val shape = RoundedCornerShape(SugarDimens.Radius.lg)
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // Cream base
                drawRect(Color(0xFFFFF8E1))
                
                // Caramel drips from top
                val dripPositions = listOf(0.1f, 0.25f, 0.4f, 0.55f, 0.7f, 0.85f)
                dripPositions.forEachIndexed { index, xPos ->
                    val dripLength = (15f + index * 5f + dripOffset).coerceAtMost(size.height * 0.4f)
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(size.width * xPos - 10f, 0f)
                        quadraticBezierTo(
                            size.width * xPos, 0f,
                            size.width * xPos, dripLength
                        )
                        quadraticBezierTo(
                            size.width * xPos + 10f, 0f,
                            size.width * xPos + 20f, 0f
                        )
                        close()
                    }
                    drawPath(
                        path,
                        color = Color(0xFFC68E59).copy(alpha = 0.7f)
                    )
                }
                
                // Caramel pool at bottom (subtle)
                drawRect(
                    color = Color(0xFFC68E59).copy(alpha = 0.2f),
                    topLeft = Offset(0f, size.height - 20f),
                    size = Size(size.width, 20f)
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.low),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

/**
 * SPRINKLES - Confetti particle overlay
 */
@Composable
private fun SprinklesCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f
    
    val infiniteTransition = rememberInfiniteTransition(label = "sprinkles_twinkle")
    val twinkle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )
    
    val shape = RoundedCornerShape(SugarDimens.Radius.lg)
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    // Generate random sprinkle positions (deterministic based on index)
    val sprinkles = remember {
        List(50) { i ->
            Triple(
                (i * 17 % 100) / 100f,  // x position
                (i * 23 % 100) / 100f,  // y position
                i % 5                    // color index
            )
        }
    }
    
    val sprinkleColors = listOf(
        SugarDimens.Brand.hotPink,
        SugarDimens.Brand.mint,
        SugarDimens.Brand.yellow,
        SugarDimens.Brand.bubblegumBlue,
        SugarDimens.Brand.candyOrange
    )
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // White frosting base
                drawRect(Color(0xFFFFF8F0))
                
                // Sprinkles
                sprinkles.forEach { (x, y, colorIdx) ->
                    val sprinkleX = x * size.width
                    val sprinkleY = y * size.height
                    val sprinkleWidth = 8f + twinkle * 2f
                    val sprinkleHeight = 3f
                    
                    drawRoundRect(
                        color = sprinkleColors[colorIdx],
                        topLeft = Offset(sprinkleX - sprinkleWidth / 2, sprinkleY - sprinkleHeight / 2),
                        size = Size(sprinkleWidth, sprinkleHeight),
                        cornerRadius = CornerRadius(sprinkleHeight / 2)
                    )
                }
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

/**
 * HARD_CANDY - Glassy with refraction effect
 */
@Composable
private fun HardCandyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f
    
    val shape = RoundedCornerShape(SugarDimens.Radius.xl)
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // Clear glass base
                drawRect(Color(0xFFE0F7FA).copy(alpha = 0.3f))
                
                // Refraction gradient (simulates light bending through glass)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.6f),
                            Color(0xFFB2EBF2).copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.4f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                
                // Strong highlight (top-left)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0f)
                        )
                    ),
                    radius = size.minDimension * 0.3f,
                    center = Offset(size.width * 0.3f, size.height * 0.3f)
                )
                
                // Edge highlight
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.5f),
                    topLeft = Offset(5f, 5f),
                    size = Size(size.width - 10f, size.height - 10f),
                    cornerRadius = CornerRadius((SugarDimens.Radius.xl.toPx() - 5f)),
                    style = Stroke(width = 2f)
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.high),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

/**
 * MARSHMALLOW - Soft puffy edges with gradient
 */
@Composable
private fun MarshmallowCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.94f else 1f  // Extra squish
    
    val shape = RoundedCornerShape(SugarDimens.Radius.xxl)
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // Fluffy white-pink gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF0F5),  // Lavender blush
                            Color(0xFFFFE4E1),  // Misty rose
                            Color(0xFFFFF0F5)
                        )
                    )
                )
                
                // Soft shadow (simulates fluffy edges)
                drawRoundRect(
                    color = Color(0xFFFFC0CB).copy(alpha = 0.3f),
                    topLeft = Offset(10f, 10f),
                    size = Size(size.width - 20f, size.height - 20f),
                    cornerRadius = CornerRadius(SugarDimens.Radius.xxl.toPx() - 10f),
                    style = Stroke(width = 20f)
                )
                
                // Powdery texture (subtle noise simulation with dots)
                repeat(30) { i ->
                    val x = (i * 37 % 100) / 100f * size.width
                    val y = (i * 41 % 100) / 100f * size.height
                    drawCircle(
                        color = Color.White.copy(alpha = 0.5f),
                        radius = 2f,
                        center = Offset(x, y)
                    )
                }
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.xl)) {
            content()
        }
    }
}

/**
 * CANDY_CANE - Diagonal red-white stripes
 */
@Composable
private fun CandyCaneCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f
    
    val infiniteTransition = rememberInfiniteTransition(label = "cane_shimmer")
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )
    
    val shape = RoundedCornerShape(SugarDimens.Radius.lg)
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // White base
                drawRect(Color.White)
                
                // Diagonal red stripes
                val stripeWidth = 30f
                val numStripes = ((size.width + size.height) / stripeWidth).toInt() + 1
                
                for (i in 0 until numStripes) {
                    val offset = i * stripeWidth - size.height
                    drawPath(
                        path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(offset, 0f)
                            lineTo(offset + stripeWidth * 1.5f, 0f)
                            lineTo(offset + stripeWidth * 0.5f, size.height)
                            lineTo(offset - stripeWidth * 0.5f, size.height)
                            close()
                        },
                        color = SugarDimens.Brand.hotPink.copy(alpha = 0.8f + shimmer * 0.2f)
                    )
                }
                
                // Glossy overlay
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.White.copy(alpha = 0.1f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

/**
 * SUGAR_GLASS - Ultra-transparent frosted glass
 */
@Composable
private fun SugarGlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.96f else 1f
    
    val shape = RoundedCornerShape(SugarDimens.Radius.lg)
    
    var mod = modifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    
    Card(
        modifier = mod
            .clip(shape)
            .drawBehind {
                // Ultra-transparent base
                drawRect(Color(0xFFF0F0F0).copy(alpha = 0.15f))
                
                // Frosted effect (simulated with subtle gradient)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.2f),
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.15f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height)
                    )
                )
                
                // Thin white border
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.3f),
                    topLeft = Offset(1f, 1f),
                    size = Size(size.width - 2f, size.height - 2f),
                    cornerRadius = CornerRadius(SugarDimens.Radius.lg.toPx() - 1f),
                    style = Stroke(width = 1f)
                )
                
                // Subtle inner shadow
                drawRoundRect(
                    color = Color(0xFF1A1A1A).copy(alpha = 0.05f),
                    topLeft = Offset(2f, 2f),
                    size = Size(size.width - 4f, size.height - 4f),
                    cornerRadius = CornerRadius(SugarDimens.Radius.lg.toPx() - 2f),
                    style = Stroke(width = 2f)
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.low),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

/**
 * Combined preview showing all CardStyleV2 options
 */
@Preview(showBackground = true, heightDp = 2000)
@Composable
fun CardStyleV2Preview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(SugarDimens.Spacing.md)
    ) {
        CardStyleV2.entries.forEach { style ->
            Text(
                text = style.name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(vertical = SugarDimens.Spacing.xs)
            )
            SugarCardV2(
                modifier = Modifier.fillMaxWidth(),
                style = style,
                onClick = {}
            ) {
                Text(
                    text = "${style.name} Card",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
                Text(
                    text = "Sample card content for ${style.name}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(SugarDimens.Spacing.md))
        }
    }
}
