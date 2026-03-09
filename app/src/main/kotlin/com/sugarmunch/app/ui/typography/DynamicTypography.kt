package com.sugarmunch.app.ui.typography

import android.graphics.Typeface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.sugarmunch.app.theme.profile.ThemeFontAxisValue

data class DynamicTypographyConfig(
    val headingFont: SugarFontFamily = SugarFontFamily.SYSTEM_DEFAULT,
    val bodyFont: SugarFontFamily = SugarFontFamily.SYSTEM_DEFAULT,
    val captionFont: SugarFontFamily = SugarFontFamily.SYSTEM_DEFAULT,
    val headingAxes: List<ThemeFontAxisValue> = emptyList(),
    val bodyAxes: List<ThemeFontAxisValue> = emptyList(),
    val captionAxes: List<ThemeFontAxisValue> = emptyList(),
    val headingTypeface: Typeface? = null,
    val bodyTypeface: Typeface? = null,
    val captionTypeface: Typeface? = null,
    val scale: TypeScale = TypeScale.DEFAULT,
    val letterSpacingMultiplier: Float = 1f,
    val lineHeightMultiplier: Float = 1f,
    val fontWeightBoost: Boolean = false
)

val LocalSugarTypography = staticCompositionLocalOf { DynamicTypographyConfig() }

@Composable
fun ProvideSugarTypography(
    config: DynamicTypographyConfig,
    content: @Composable () -> Unit
) {
    val typography = config.toTypography()
    CompositionLocalProvider(LocalSugarTypography provides config) {
        MaterialTheme(
            typography = typography,
            colorScheme = MaterialTheme.colorScheme,
            shapes = MaterialTheme.shapes,
            content = content
        )
    }
}

fun DynamicTypographyConfig.toTypography(): Typography {
    val baseConfig = scale.toConfig()
    val headingFamily = headingTypeface?.let(::FontFamily) ?: headingFont.toComposeFontFamily(headingAxes)
    val bodyFamily = bodyTypeface?.let(::FontFamily) ?: bodyFont.toComposeFontFamily(bodyAxes)
    val captionFamily = captionTypeface?.let(::FontFamily) ?: captionFont.toComposeFontFamily(captionAxes)

    fun TextStyle.applyHeading(): TextStyle = copy(
        fontFamily = headingFamily,
        letterSpacing = letterSpacing * letterSpacingMultiplier,
        lineHeight = (lineHeight.value * lineHeightMultiplier).sp,
        fontWeight = if (fontWeightBoost) bumpWeight(fontWeight) else fontWeight
    )

    fun TextStyle.applyBody(): TextStyle = copy(
        fontFamily = bodyFamily,
        letterSpacing = letterSpacing * letterSpacingMultiplier,
        lineHeight = (lineHeight.value * lineHeightMultiplier).sp,
        fontWeight = if (fontWeightBoost) bumpWeight(fontWeight) else fontWeight
    )

    fun TextStyle.applyCaption(): TextStyle = copy(
        fontFamily = captionFamily,
        letterSpacing = letterSpacing * letterSpacingMultiplier,
        lineHeight = (lineHeight.value * lineHeightMultiplier).sp,
        fontWeight = if (fontWeightBoost) bumpWeight(fontWeight) else fontWeight
    )

    return Typography(
        displayLarge = baseConfig.displayLarge.applyHeading(),
        displayMedium = baseConfig.displayMedium.applyHeading(),
        displaySmall = baseConfig.displaySmall.applyHeading(),
        headlineLarge = baseConfig.headlineLarge.applyHeading(),
        headlineMedium = baseConfig.headlineMedium.applyHeading(),
        headlineSmall = baseConfig.headlineSmall.applyHeading(),
        titleLarge = baseConfig.titleLarge.applyHeading(),
        titleMedium = baseConfig.titleMedium.applyBody(),
        titleSmall = baseConfig.titleSmall.applyBody(),
        bodyLarge = baseConfig.bodyLarge.applyBody(),
        bodyMedium = baseConfig.bodyMedium.applyBody(),
        bodySmall = baseConfig.bodySmall.applyCaption(),
        labelLarge = baseConfig.labelLarge.applyCaption(),
        labelMedium = baseConfig.labelMedium.applyCaption(),
        labelSmall = baseConfig.labelSmall.applyCaption()
    )
}

private fun bumpWeight(weight: FontWeight?): FontWeight = when (weight) {
    FontWeight.Thin -> FontWeight.ExtraLight
    FontWeight.ExtraLight -> FontWeight.Light
    FontWeight.Light -> FontWeight.Normal
    FontWeight.Normal -> FontWeight.Medium
    FontWeight.Medium -> FontWeight.SemiBold
    FontWeight.SemiBold -> FontWeight.Bold
    FontWeight.Bold -> FontWeight.ExtraBold
    FontWeight.ExtraBold -> FontWeight.Black
    FontWeight.Black -> FontWeight.Black
    else -> FontWeight.Medium
}
