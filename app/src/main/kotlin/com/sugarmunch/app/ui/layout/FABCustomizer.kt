package com.sugarmunch.app.ui.layout

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.ui.design.HeartShape
import com.sugarmunch.app.ui.design.HexagonShape
import com.sugarmunch.app.ui.design.StarShape
import com.sugarmunch.app.ui.design.SugarDimens

enum class FABShape(val displayName: String) {
    CIRCLE("Circle"),
    ROUNDED_SQUARE("Rounded Square"),
    CANDY("Candy"),
    STAR("Star"),
    HEART("Heart"),
    HEXAGON("Hexagon")
}

enum class FABSize(val sizeDp: Int, val displayName: String) {
    SMALL(40, "Small"),
    MEDIUM(56, "Medium"),
    LARGE(72, "Large")
}

enum class FABAnimation(val displayName: String) {
    NONE("None"),
    PULSE("Pulse"),
    BOUNCE("Bounce"),
    ROTATE("Rotate"),
    BREATHE("Breathe"),
    WIGGLE("Wiggle")
}

data class FABConfig(
    val shape: FABShape = FABShape.CIRCLE,
    val size: FABSize = FABSize.MEDIUM,
    val animation: FABAnimation = FABAnimation.PULSE,
    val primaryColor: Color = Color(0xFFFF69B4),
    val iconColor: Color = Color.White,
    val shadowEnabled: Boolean = true,
    val hapticEnabled: Boolean = true
)

private fun FABShape.toComposeShape(): Shape = when (this) {
    FABShape.CIRCLE -> CircleShape
    FABShape.ROUNDED_SQUARE -> RoundedCornerShape(SugarDimens.Radius.lg)
    FABShape.CANDY -> RoundedCornerShape(topStart = SugarDimens.Radius.xl, topEnd = SugarDimens.Radius.sm, bottomStart = SugarDimens.Radius.sm, bottomEnd = SugarDimens.Radius.xl)
    FABShape.STAR -> StarShape(points = 5, innerRadius = 0.45f)
    FABShape.HEART -> HeartShape()
    FABShape.HEXAGON -> HexagonShape()
}

@Composable
fun CustomFAB(
    config: FABConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit = { Icon(Icons.Default.Add, null, tint = config.iconColor) }
) {
    val sizeDp = config.size.sizeDp.dp
    val shape = config.shape.toComposeShape()

    val infiniteTransition = rememberInfiniteTransition(label = "fab_anim")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pulse"
    )
    val bounceY by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = -4f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "bounce"
    )
    val rotateDeg by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart), label = "rotate"
    )
    val breatheAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse), label = "breathe"
    )
    val wiggleRot by infiniteTransition.animateFloat(
        initialValue = -3f, targetValue = 3f,
        animationSpec = infiniteRepeatable(tween(300), RepeatMode.Reverse), label = "wiggle"
    )

    val animModifier = when (config.animation) {
        FABAnimation.NONE -> Modifier
        FABAnimation.PULSE -> Modifier.scale(pulseScale)
        FABAnimation.BOUNCE -> Modifier.offset { IntOffset(0, bounceY.dp.roundToPx()) }
        FABAnimation.ROTATE -> Modifier.rotate(rotateDeg)
        FABAnimation.BREATHE -> Modifier.graphicsLayer { alpha = breatheAlpha }
        FABAnimation.WIGGLE -> Modifier.rotate(wiggleRot)
    }

    val shadowModifier = if (config.shadowEnabled) {
        Modifier.shadow(SugarDimens.Elevation.high, shape, ambientColor = config.primaryColor.copy(alpha = 0.3f), spotColor = config.primaryColor.copy(alpha = 0.5f))
    } else {
        Modifier
    }

    Surface(
        modifier = modifier
            .size(sizeDp)
            .then(animModifier)
            .then(shadowModifier)
            .clickable { onClick() },
        shape = shape,
        color = config.primaryColor,
        tonalElevation = if (config.shadowEnabled) SugarDimens.Elevation.medium else SugarDimens.Elevation.none
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(sizeDp)) {
            icon()
        }
    }
}

@Composable
fun FABCustomizerPanel(
    config: FABConfig,
    onConfigChanged: (FABConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(SugarDimens.Spacing.md)
    ) {
        Text("FAB Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(SugarDimens.Spacing.md))

        // Live preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(SugarDimens.Radius.md)),
            contentAlignment = Alignment.Center
        ) {
            CustomFAB(config = config, onClick = {})
        }

        Spacer(Modifier.height(SugarDimens.Spacing.lg))

        // Shape
        SectionHeader("Shape")
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            FABShape.entries.forEach { shape ->
                val isSelected = shape == config.shape
                val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .border(2.dp, borderColor, RoundedCornerShape(SugarDimens.Radius.sm))
                        .clickable { onConfigChanged(config.copy(shape = shape)) },
                    shape = RoundedCornerShape(SugarDimens.Radius.sm),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Box(
                            Modifier
                                .size(28.dp)
                                .background(config.primaryColor.copy(alpha = 0.7f), shape.toComposeShape())
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(SugarDimens.Spacing.md))

        // Size
        SectionHeader("Size")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            FABSize.entries.forEach { size ->
                val isSelected = size == config.size
                val scale by animateFloatAsState(
                    if (isSelected) 1f else 0.9f,
                    animationSpec = spring(), label = "size_scale"
                )
                val bgColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .scale(scale)
                        .background(bgColor, RoundedCornerShape(SugarDimens.Radius.sm))
                        .clickable { onConfigChanged(config.copy(size = size)) }
                        .padding(vertical = SugarDimens.Spacing.sm),
                    contentAlignment = Alignment.Center
                ) {
                    Text(size.displayName, color = textColor, style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        Spacer(Modifier.height(SugarDimens.Spacing.md))

        // Animation
        SectionHeader("Animation")
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            FABAnimation.entries.forEach { anim ->
                val isSelected = anim == config.animation
                val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                Column(
                    modifier = Modifier
                        .width(64.dp)
                        .border(2.dp, borderColor, RoundedCornerShape(SugarDimens.Radius.sm))
                        .clickable { onConfigChanged(config.copy(animation = anim)) }
                        .padding(SugarDimens.Spacing.xs),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CustomFAB(
                        config = FABConfig(
                            shape = FABShape.CIRCLE,
                            size = FABSize.SMALL,
                            animation = anim,
                            primaryColor = config.primaryColor,
                            iconColor = config.iconColor,
                            shadowEnabled = false
                        ),
                        onClick = {}
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(anim.displayName, style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(SugarDimens.Spacing.md))

        // Color
        SectionHeader("Color")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(SugarDimens.Spacing.sm)
        ) {
            val presetColors = listOf(
                Color(0xFFFF69B4), Color(0xFF6C63FF), Color(0xFF00BFA5),
                Color(0xFFFF6D00), Color(0xFFE91E63), Color(0xFF2196F3)
            )
            presetColors.forEach { color ->
                val isSelected = color == config.primaryColor
                val borderColor = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent
                Box(
                    Modifier
                        .size(36.dp)
                        .border(2.dp, borderColor, CircleShape)
                        .padding(2.dp)
                        .background(color, CircleShape)
                        .clickable { onConfigChanged(config.copy(primaryColor = color)) }
                )
            }
        }

        Spacer(Modifier.height(SugarDimens.Spacing.md))

        // Toggles
        ToggleOption("Shadow", config.shadowEnabled) {
            onConfigChanged(config.copy(shadowEnabled = it))
        }
        ToggleOption("Haptic Feedback", config.hapticEnabled) {
            onConfigChanged(config.copy(hapticEnabled = it))
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = SugarDimens.Spacing.xs)
    )
}

@Composable
private fun ToggleOption(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = SugarDimens.Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
