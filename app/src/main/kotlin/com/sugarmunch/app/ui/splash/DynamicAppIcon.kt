package com.sugarmunch.app.ui.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.SugarDimens

enum class IconShape { CIRCLE, ROUNDED_SQUARE, SQUIRCLE }

enum class AppIconVariant(
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val iconShape: IconShape
) {
    CLASSIC("Classic", "Original candy pink", Color(0xFFFFB6C1), Color(0xFFFF69B4), IconShape.CIRCLE),
    MINT_FRESH("Mint Fresh", "Cool mint green", Color(0xFF98FF98), Color(0xFF00E676), IconShape.CIRCLE),
    OCEAN_BLUE("Ocean Blue", "Deep blue ocean", Color(0xFFB5DEFF), Color(0xFF2196F3), IconShape.CIRCLE),
    BERRY_PURPLE("Berry Purple", "Rich purple berry", Color(0xFFE6B3FF), Color(0xFF9C27B0), IconShape.CIRCLE),
    SUNSET_ORANGE("Sunset Orange", "Warm sunset", Color(0xFFFFCC80), Color(0xFFFF9800), IconShape.CIRCLE),
    CARAMEL_GOLD("Caramel Gold", "Golden caramel", Color(0xFFDEB887), Color(0xFFFFC107), IconShape.CIRCLE),
    NEON_PINK("Neon Pink", "Glowing neon", Color(0xFFFF1493), Color(0xFFFF00FF), IconShape.SQUIRCLE),
    NEON_GREEN("Neon Green", "Cyber green", Color(0xFF00FF41), Color(0xFF39FF14), IconShape.SQUIRCLE),
    DARK_MODE("Dark Mode", "Sleek and dark", Color(0xFF2D1B2E), Color(0xFF6A1B9A), IconShape.ROUNDED_SQUARE),
    MINIMAL_WHITE("Minimal White", "Clean and simple", Color(0xFFFFFBF7), Color(0xFFFFB6C1), IconShape.CIRCLE),
    RETRO("Retro", "Nostalgic vibes", Color(0xFFFF6B6B), Color(0xFFFECA57), IconShape.ROUNDED_SQUARE),
    HOLOGRAPHIC("Holographic", "Iridescent shimmer", Color(0xFFE0C3FC), Color(0xFF8EC5FC), IconShape.SQUIRCLE)
}

@Composable
fun AppIconPreview(
    variant: AppIconVariant,
    isSelected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )

    val borderShape = when (variant.iconShape) {
        IconShape.CIRCLE -> CircleShape
        IconShape.ROUNDED_SQUARE -> RoundedCornerShape(SugarDimens.Radius.lg)
        IconShape.SQUIRCLE -> RoundedCornerShape(SugarDimens.Radius.xl)
    }

    Box(
        modifier = modifier
            .size(SugarDimens.IconSize.hero)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isSelected) {
                    Modifier
                        .shadow(
                            elevation = SugarDimens.Elevation.high,
                            shape = borderShape,
                            ambientColor = variant.secondaryColor,
                            spotColor = variant.secondaryColor
                        )
                        .border(2.dp, variant.secondaryColor, borderShape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(SugarDimens.IconSize.hero)) {
            val iconSize = size.minDimension
            val gradient = Brush.linearGradient(
                colors = listOf(variant.primaryColor, variant.secondaryColor),
                start = Offset.Zero,
                end = Offset(iconSize, iconSize)
            )

            drawIconShape(variant.iconShape, gradient, iconSize)
            drawCandySymbol(center, iconSize * 0.18f, variant.secondaryColor)

            if (isSelected) {
                drawCheckBadge(iconSize)
            }
        }
    }
}

private fun DrawScope.drawIconShape(shape: IconShape, gradient: Brush, iconSize: Float) {
    when (shape) {
        IconShape.CIRCLE -> {
            drawCircle(brush = gradient, radius = iconSize / 2f)
        }
        IconShape.ROUNDED_SQUARE -> {
            val cornerRadius = iconSize * 0.2f
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 0f, top = 0f, right = iconSize, bottom = iconSize,
                        cornerRadius = CornerRadius(cornerRadius)
                    )
                )
            }
            drawPath(path, brush = gradient)
        }
        IconShape.SQUIRCLE -> {
            val cornerRadius = iconSize * 0.3f
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 0f, top = 0f, right = iconSize, bottom = iconSize,
                        cornerRadius = CornerRadius(cornerRadius)
                    )
                )
            }
            drawPath(path, brush = gradient)
        }
    }
}

private fun DrawScope.drawCandySymbol(center: Offset, radius: Float, accentColor: Color) {
    val candyColor = Color.White.copy(alpha = 0.9f)
    drawCircle(color = candyColor, radius = radius, center = center)

    // Left wrapper
    val leftPath = Path().apply {
        moveTo(center.x - radius, center.y - radius * 0.5f)
        lineTo(center.x - radius * 1.7f, center.y)
        lineTo(center.x - radius, center.y + radius * 0.5f)
        close()
    }
    drawPath(leftPath, color = accentColor.copy(alpha = 0.8f))

    // Right wrapper
    val rightPath = Path().apply {
        moveTo(center.x + radius, center.y - radius * 0.5f)
        lineTo(center.x + radius * 1.7f, center.y)
        lineTo(center.x + radius, center.y + radius * 0.5f)
        close()
    }
    drawPath(rightPath, color = accentColor.copy(alpha = 0.8f))
}

private fun DrawScope.drawCheckBadge(iconSize: Float) {
    val badgeRadius = iconSize * 0.15f
    val badgeCenter = Offset(iconSize * 0.82f, iconSize * 0.18f)
    drawCircle(color = Color(0xFF4CAF50), radius = badgeRadius, center = badgeCenter)
    drawCircle(color = Color.White, radius = badgeRadius, center = badgeCenter, style = Stroke(width = 1.5f))

    val checkPath = Path().apply {
        moveTo(badgeCenter.x - badgeRadius * 0.4f, badgeCenter.y)
        lineTo(badgeCenter.x - badgeRadius * 0.05f, badgeCenter.y + badgeRadius * 0.35f)
        lineTo(badgeCenter.x + badgeRadius * 0.45f, badgeCenter.y - badgeRadius * 0.3f)
    }
    drawPath(checkPath, color = Color.White, style = Stroke(width = 2f))
}

@Composable
fun AppIconSelector(
    currentVariant: AppIconVariant,
    onVariantSelected: (AppIconVariant) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(SugarDimens.Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.md)
    ) {
        items(AppIconVariant.entries.toList()) { variant ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = SugarDimens.Spacing.xxs)
            ) {
                AppIconPreview(
                    variant = variant,
                    isSelected = variant == currentVariant,
                    onClick = { onVariantSelected(variant) }
                )

                Text(
                    text = variant.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = SugarDimens.Spacing.xxs)
                )
            }
        }
    }
}
