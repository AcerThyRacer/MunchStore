package com.sugarmunch.app.ui.icons

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class IconStyle {
    FILLED,
    OUTLINED,
    ROUNDED,
    TWO_TONE
}

data class ThemedIconConfig(
    val style: IconStyle = IconStyle.FILLED,
    val tintColor: Color? = null,
    val size: Dp = 24.dp,
    val animateOnAppear: Boolean = false
)

@Composable
fun ThemedIcon(
    icon: ImageVector,
    config: ThemedIconConfig = ThemedIconConfig(),
    modifier: Modifier = Modifier
) {
    val tint = config.tintColor ?: MaterialTheme.colorScheme.onSurface

    val scale = if (config.animateOnAppear) {
        val animatable = remember { Animatable(0f) }
        LaunchedEffect(Unit) {
            animatable.animateTo(1f, animationSpec = spring())
        }
        animatable.value
    } else {
        1f
    }

    val styledIcon = getIconForStyle(icon, config.style)

    Icon(
        imageVector = styledIcon,
        contentDescription = null,
        tint = tint,
        modifier = modifier
            .size(config.size)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    )
}

@Composable
fun CategoryIcon(
    category: String,
    config: ThemedIconConfig = ThemedIconConfig(),
    modifier: Modifier = Modifier
) {
    val icon = when (category.lowercase()) {
        "video", "music", "video_music" -> CandyIcons.VideoMusic
        "tools", "utilities" -> CandyIcons.Tools
        "social", "communication" -> CandyIcons.Social
        "games", "gaming" -> CandyIcons.Games
        "productivity", "work" -> CandyIcons.Productivity
        else -> CandyIcons.Other
    }
    ThemedIcon(icon, config, modifier)
}

@Composable
fun EffectIcon(
    effectId: String,
    config: ThemedIconConfig = ThemedIconConfig(),
    modifier: Modifier = Modifier
) {
    val icon = when (effectId.lowercase()) {
        "sugar_rush" -> CandyIcons.SugarRush
        "rainbow" -> CandyIcons.Rainbow
        "mint_wash" -> CandyIcons.MintWash
        "caramel_dim" -> CandyIcons.CaramelDim
        "confetti" -> CandyIcons.Confetti
        "heartbeat" -> CandyIcons.Heartbeat
        "sparkle" -> CandyIcons.Sparkle
        "magic" -> CandyIcons.Magic
        else -> CandyIcons.Effects
    }
    ThemedIcon(icon, config, modifier)
}

fun getIconForStyle(baseIcon: ImageVector, style: IconStyle): ImageVector {
    return when (style) {
        IconStyle.FILLED -> baseIcon

        IconStyle.OUTLINED -> ImageVector.Builder(
            name = "${baseIcon.name}_outlined",
            defaultWidth = baseIcon.defaultWidth,
            defaultHeight = baseIcon.defaultHeight,
            viewportWidth = baseIcon.viewportWidth,
            viewportHeight = baseIcon.viewportHeight
        ).apply {
            baseIcon.root.forEach { node ->
                if (node is VectorPath) {
                    addPath(
                        pathData = node.pathData,
                        pathFillType = node.pathFillType,
                        fill = null,
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 1.5f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round
                    )
                }
            }
        }.build()

        IconStyle.ROUNDED -> ImageVector.Builder(
            name = "${baseIcon.name}_rounded",
            defaultWidth = baseIcon.defaultWidth,
            defaultHeight = baseIcon.defaultHeight,
            viewportWidth = baseIcon.viewportWidth,
            viewportHeight = baseIcon.viewportHeight
        ).apply {
            baseIcon.root.forEach { node ->
                if (node is VectorPath) {
                    addPath(
                        pathData = node.pathData,
                        pathFillType = node.pathFillType,
                        fill = null,
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 2f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round
                    )
                }
            }
        }.build()

        IconStyle.TWO_TONE -> ImageVector.Builder(
            name = "${baseIcon.name}_two_tone",
            defaultWidth = baseIcon.defaultWidth,
            defaultHeight = baseIcon.defaultHeight,
            viewportWidth = baseIcon.viewportWidth,
            viewportHeight = baseIcon.viewportHeight
        ).apply {
            baseIcon.root.forEach { node ->
                if (node is VectorPath) {
                    addPath(
                        pathData = node.pathData,
                        pathFillType = node.pathFillType,
                        fill = SolidColor(Color.Black.copy(alpha = 0.3f)),
                        stroke = null
                    )
                    addPath(
                        pathData = node.pathData,
                        pathFillType = node.pathFillType,
                        fill = null,
                        stroke = SolidColor(Color.Black),
                        strokeLineWidth = 1.5f,
                        strokeLineCap = StrokeCap.Round,
                        strokeLineJoin = StrokeJoin.Round
                    )
                }
            }
        }.build()
    }
}
