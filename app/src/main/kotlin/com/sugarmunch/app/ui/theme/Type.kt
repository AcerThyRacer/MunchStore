package com.sugarmunch.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val baseDisplayLarge = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 57.sp,
    lineHeight = 64.sp
)
private val baseHeadlineLarge = TextStyle(
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp,
    lineHeight = 40.sp
)
private val baseTitleLarge = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 22.sp,
    lineHeight = 28.sp
)
private val baseBodyLarge = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 24.sp
)
private val baseBodyMedium = TextStyle(
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 20.sp
)
private val baseLabelLarge = TextStyle(
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp,
    lineHeight = 20.sp
)

/** Base typography (intensity 1f). */
val SugarTypography = Typography(
    displayLarge = baseDisplayLarge,
    headlineLarge = baseHeadlineLarge,
    titleLarge = baseTitleLarge,
    bodyLarge = baseBodyLarge,
    bodyMedium = baseBodyMedium,
    labelLarge = baseLabelLarge
)

/**
 * Typography scaled by theme intensity (0.9 + 0.2 * intensity so 1f => 1.1 scale at max).
 * Higher intensity = slightly larger, bolder feel.
 */
fun sugarTypographyForIntensity(intensity: Float): Typography {
    val scale = (0.9f + 0.2f * intensity.coerceIn(0f, 2f))
    return Typography(
        displayLarge = baseDisplayLarge.scale(scale),
        headlineLarge = baseHeadlineLarge.scale(scale),
        titleLarge = baseTitleLarge.scale(scale),
        bodyLarge = baseBodyLarge.scale(scale),
        bodyMedium = baseBodyMedium.scale(scale),
        labelLarge = baseLabelLarge.scale(scale)
    )
}

private fun TextStyle.scale(scale: Float): TextStyle = copy(
    fontSize = (fontSize.value * scale).sp,
    lineHeight = (lineHeight.value * scale).sp
)
