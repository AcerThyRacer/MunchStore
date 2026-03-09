package com.sugarmunch.app.ui.typography

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

enum class TypeScale {
    COMPACT,
    DEFAULT,
    COMFORTABLE,
    LARGE,
    ACCESSIBILITY
}

data class TypeScaleConfig(
    val displayLarge: TextStyle,
    val displayMedium: TextStyle,
    val displaySmall: TextStyle,
    val headlineLarge: TextStyle,
    val headlineMedium: TextStyle,
    val headlineSmall: TextStyle,
    val titleLarge: TextStyle,
    val titleMedium: TextStyle,
    val titleSmall: TextStyle,
    val bodyLarge: TextStyle,
    val bodyMedium: TextStyle,
    val bodySmall: TextStyle,
    val labelLarge: TextStyle,
    val labelMedium: TextStyle,
    val labelSmall: TextStyle
)

fun TypeScale.toConfig(): TypeScaleConfig = when (this) {
    TypeScale.COMPACT -> TypeScaleConfig(
        displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 48.sp),
        displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
        displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
        headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 26.sp),
        titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 18.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 11.sp, lineHeight = 16.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp)
    )

    TypeScale.DEFAULT -> TypeScaleConfig(
        displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 57.sp, lineHeight = 64.sp),
        displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 45.sp, lineHeight = 52.sp),
        displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
        headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
        titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp)
    )

    TypeScale.COMFORTABLE -> TypeScaleConfig(
        displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 64.sp, lineHeight = 72.sp),
        displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 52.sp, lineHeight = 60.sp),
        displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 44.sp, lineHeight = 52.sp),
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
        headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 36.sp),
        titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 28.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp)
    )

    TypeScale.LARGE -> TypeScaleConfig(
        displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 72.sp, lineHeight = 80.sp),
        displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 60.sp, lineHeight = 68.sp),
        displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 56.sp),
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 48.sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 44.sp),
        headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 40.sp),
        titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 36.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 32.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 28.sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 30.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 28.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 24.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp)
    )

    TypeScale.ACCESSIBILITY -> TypeScaleConfig(
        displayLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 72.sp, lineHeight = 115.sp),
        displayMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 60.sp, lineHeight = 96.sp),
        displaySmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 48.sp, lineHeight = 77.sp),
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 40.sp, lineHeight = 64.sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = 36.sp, lineHeight = 58.sp),
        headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 51.sp),
        titleLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 28.sp, lineHeight = 45.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 24.sp, lineHeight = 38.sp),
        titleSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 32.sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 22.sp, lineHeight = 35.sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = 20.sp, lineHeight = 32.sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = 18.sp, lineHeight = 29.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 32.sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 29.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 26.sp)
    )
}

fun TypeScaleConfig.toTypography(): Typography = Typography(
    displayLarge = displayLarge,
    displayMedium = displayMedium,
    displaySmall = displaySmall,
    headlineLarge = headlineLarge,
    headlineMedium = headlineMedium,
    headlineSmall = headlineSmall,
    titleLarge = titleLarge,
    titleMedium = titleMedium,
    titleSmall = titleSmall,
    bodyLarge = bodyLarge,
    bodyMedium = bodyMedium,
    bodySmall = bodySmall,
    labelLarge = labelLarge,
    labelMedium = labelMedium,
    labelSmall = labelSmall
)
