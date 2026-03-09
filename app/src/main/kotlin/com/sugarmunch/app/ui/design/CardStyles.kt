package com.sugarmunch.app.ui.design

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

enum class CardStyle {
    FLAT,
    ELEVATED,
    OUTLINED,
    GLASSMORPHIC,
    NEON,
    HOLOGRAPHIC,
    LIQUID,
    CRYSTAL,
    SUGARRUSH,
    MAX,
    // NEW V2 Candy Styles
    CANDY_WRAPPER,
    LOLLIPOP,
    GUMMY,
    CHOCOLATE_BAR,
    CARAMEL_DRIZZLE,
    SPRINKLES,
    HARD_CANDY,
    MARSHMALLOW,
    CANDY_CANE,
    SUGAR_GLASS
}

@Composable
fun SugarCard(
    modifier: Modifier = Modifier,
    style: CardStyle = CardStyle.ELEVATED,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    when (style) {
        CardStyle.FLAT -> FlatCard(modifier, onClick, content)
        CardStyle.ELEVATED -> ElevatedCard(modifier, onClick, content)
        CardStyle.OUTLINED -> OutlinedCard(modifier, onClick, content)
        CardStyle.GLASSMORPHIC -> GlassmorphicCard(modifier, onClick, content)
        CardStyle.NEON -> NeonCard(modifier, onClick, content)
        CardStyle.HOLOGRAPHIC -> HolographicCard(modifier, onClick, content)
        CardStyle.LIQUID -> LiquidCard(modifier, onClick, content)
        CardStyle.CRYSTAL -> CrystalCard(modifier, onClick, content)
        CardStyle.SUGARRUSH -> SugarRushCard(modifier, onClick, content)
        CardStyle.MAX -> MaxCard(modifier, onClick, content)
        // V2 Candy Styles - use SugarCardV2 from CardStylesV2.kt instead
        CardStyle.CANDY_WRAPPER,
        CardStyle.LOLLIPOP,
        CardStyle.GUMMY,
        CardStyle.CHOCOLATE_BAR,
        CardStyle.CARAMEL_DRIZZLE,
        CardStyle.SPRINKLES,
        CardStyle.HARD_CANDY,
        CardStyle.MARSHMALLOW,
        CardStyle.CANDY_CANE,
        CardStyle.SUGAR_GLASS -> {
            // Fallback to ELEVATED for V2 styles when using SugarCard
            // Use SugarCardV2 directly for these styles
            ElevatedCard(modifier, onClick, content)
        }
    }
}

@Composable
private fun pressScaleModifier(
    onClick: (() -> Unit)?,
    baseModifier: Modifier
): Pair<Modifier, MutableInteractionSource> {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.97f else 1f

    var mod = baseModifier.scale(scale)
    if (onClick != null) {
        mod = mod.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    }
    return mod to interactionSource
}

@Composable
private fun FlatCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.sm)
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    Card(
        modifier = clickMod,
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.none),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
            content()
        }
    }
}

@Composable
private fun ElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.md)
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    Card(
        modifier = clickMod
            .shadow(
                elevation = SugarDimens.Elevation.low,
                shape = shape,
                ambientColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            ),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.low),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
            content()
        }
    }
}

@Composable
private fun OutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.md)
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    Card(
        modifier = clickMod,
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.none),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
            content()
        }
    }
}

@Composable
private fun GlassmorphicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.md)
    val surfaceColor = MaterialTheme.colorScheme.surface
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    Card(
        modifier = clickMod,
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.none),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .background(surfaceColor.copy(alpha = 0.6f))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                )
                .padding(SugarDimens.Spacing.md)
        ) {
            content()
        }
    }
}

@Composable
private fun NeonCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.md)
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    val infiniteTransition = rememberInfiniteTransition(label = "neon_border")
    val animatedColor by infiniteTransition.animateColor(
        initialValue = primaryColor,
        targetValue = tertiaryColor,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_color"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "neon_glow"
    )

    val radiusPx = SugarDimens.Radius.md
    Card(
        modifier = clickMod
            .shadow(
                elevation = SugarDimens.Elevation.medium,
                shape = shape,
                ambientColor = primaryColor.copy(alpha = glowAlpha * 0.5f),
                spotColor = primaryColor.copy(alpha = glowAlpha)
            )
            .clip(shape)
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val cornerRadiusPx = radiusPx.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(animatedColor, tertiaryColor, animatedColor),
                        start = Offset.Zero,
                        end = Offset(size.width, size.height)
                    ),
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = strokeWidth)
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.md)) {
            content()
        }
    }
}

@Composable
private fun HolographicCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.lg)
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    val infiniteTransition = rememberInfiniteTransition(label = "holo_gradient")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "holo_angle"
    )

    Card(
        modifier = clickMod
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color(0xFFFF0080),
                            Color(0xFF00FFFF),
                            Color(0xFF80FF00),
                            Color(0xFFFF0080)
                        ),
                        center = Offset(size.width / 2, size.height / 2)
                    ),
                    alpha = 0.3f
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.high),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A2E).copy(alpha = 0.9f)
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

@Composable
private fun LiquidCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.xl)
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    Card(
        modifier = clickMod
            .drawBehind {
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00C9FF).copy(alpha = 0.8f),
                            Color(0xFF92FE9D).copy(alpha = 0.6f)
                        ),
                        center = Offset(size.width / 2, size.height / 2),
                        radius = size.maxDimension / 2
                    )
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.medium),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

@Composable
private fun CrystalCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.lg)
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    Card(
        modifier = clickMod
            .shadow(
                elevation = SugarDimens.Elevation.high,
                shape = shape,
                ambientColor = Color(0xFFE0C3FC).copy(alpha = 0.3f),
                spotColor = Color(0xFF8EC5FC).copy(alpha = 0.5f)
            ),
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.high),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA).copy(alpha = 0.7f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFE0C3FC).copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color(0xFF8EC5FC).copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(SugarDimens.Spacing.lg)
        ) {
            content()
        }
    }
}

@Composable
private fun SugarRushCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.lg)
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    val infiniteTransition = rememberInfiniteTransition(label = "sugarrush")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient_offset"
    )

    Card(
        modifier = clickMod
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            SugarDimens.Brand.hotPink,
                            SugarDimens.Brand.mint,
                            SugarDimens.Brand.yellow,
                            SugarDimens.Brand.candyOrange
                        ),
                        start = Offset(gradientOffset, 0f),
                        end = Offset(gradientOffset + size.width, size.height)
                    )
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.high),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.lg)) {
            content()
        }
    }
}

@Composable
private fun MaxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(SugarDimens.Radius.xl)
    val (clickMod, _) = pressScaleModifier(onClick, modifier)

    val infiniteTransition = rememberInfiniteTransition(label = "max_effects")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "max_scale"
    )
    val hue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "max_hue"
    )

    Card(
        modifier = clickMod
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                rotationZ = (hue % 360 - 180) * 0.02f
            }
            .shadow(
                elevation = SugarDimens.Elevation.highest,
                shape = shape,
                ambientColor = Color.Hsv(hue % 360, 0.8f, 0.6f).copy(alpha = 0.5f),
                spotColor = Color.Hsv(hue % 360, 1f, 0.8f).copy(alpha = 0.8f)
            )
            .clip(shape)
            .drawBehind {
                drawRoundRect(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Hsv(hue % 360, 1f, 0.8f),
                            Color.Hsv((hue + 60) % 360, 1f, 0.8f),
                            Color.Hsv((hue + 120) % 360, 1f, 0.8f),
                            Color.Hsv(hue % 360, 1f, 0.8f)
                        )
                    ),
                    alpha = 0.5f
                )
            },
        shape = shape,
        elevation = CardDefaults.cardElevation(defaultElevation = SugarDimens.Elevation.highest),
        colors = CardDefaults.cardColors(
            containerColor = SugarDimens.Brand.deepPurple
        )
    ) {
        Column(modifier = Modifier.padding(SugarDimens.Spacing.xl)) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SugarCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SugarDimens.Spacing.md)
        ) {
            CardStyle.entries.forEach { style ->
                SugarCard(
                    modifier = Modifier.fillMaxWidth(),
                    style = style,
                    onClick = {}
                ) {
                    Text(
                        text = style.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(SugarDimens.Spacing.xs))
                    Text(
                        text = "Sample card content",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(modifier = Modifier.height(SugarDimens.Spacing.sm))
            }
        }
    }
}
