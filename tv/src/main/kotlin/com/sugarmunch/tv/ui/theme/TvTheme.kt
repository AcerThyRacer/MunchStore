package com.sugarmunch.tv.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme
import com.sugarmunch.app.ui.theme.CandyPink
import com.sugarmunch.app.ui.theme.CandyMint
import com.sugarmunch.app.ui.theme.CandyYellow
import com.sugarmunch.app.ui.theme.CandyCaramel
import com.sugarmunch.app.ui.theme.CottonCandyBlue
import com.sugarmunch.app.ui.theme.CandyPurple
import com.sugarmunch.app.ui.theme.SugarWhite
import com.sugarmunch.app.ui.theme.SugarDark
import com.sugarmunch.app.ui.theme.TextOnLight
import com.sugarmunch.app.ui.theme.TextOnDark
import com.sugarmunch.app.ui.theme.SurfaceLight
import com.sugarmunch.app.ui.theme.SurfaceDark

/**
 * SugarMunch TV Theme
 * Optimized color scheme for Android TV with:
 * - Higher contrast for TV viewing distances
 * - Dark theme optimized for living room viewing
 * - TV-specific typography scale
 * - Accent colors from the candy palette
 */

// TV-optimized dark color scheme (primary for TV)
@OptIn(ExperimentalTvMaterial3Api::class)
private val TvDarkColorScheme = darkColorScheme(
    primary = CandyPink,
    onPrimary = SugarDark,
    primaryContainer = CandyPink.copy(alpha = 0.7f),
    onPrimaryContainer = SugarWhite,
    
    secondary = CandyMint,
    onSecondary = SugarDark,
    secondaryContainer = CandyMint.copy(alpha = 0.7f),
    onSecondaryContainer = SugarDark,
    
    tertiary = CottonCandyBlue,
    onTertiary = SugarDark,
    tertiaryContainer = CottonCandyBlue.copy(alpha = 0.7f),
    onTertiaryContainer = SugarDark,
    
    background = SugarDark,
    onBackground = TextOnDark,
    
    surface = SurfaceDark,
    onSurface = TextOnDark,
    surfaceVariant = SurfaceDark.copy(lightness = 1.1f),
    onSurfaceVariant = TextOnDark.copy(alpha = 0.7f),
    
    error = androidx.compose.ui.graphics.Color(0xFFFF6B6B),
    onError = SugarWhite,
    errorContainer = androidx.compose.ui.graphics.Color(0xFFFF6B6B).copy(alpha = 0.3f),
    onErrorContainer = SugarWhite,
    
    border = TextOnDark.copy(alpha = 0.2f),
    outline = TextOnDark.copy(alpha = 0.3f),
    outlineVariant = TextOnDark.copy(alpha = 0.1f)
)

// TV light color scheme (less common for TV)
@OptIn(ExperimentalTvMaterial3Api::class)
private val TvLightColorScheme = lightColorScheme(
    primary = CandyPink,
    onPrimary = SugarWhite,
    primaryContainer = CandyPink.copy(alpha = 0.2f),
    onPrimaryContainer = SugarDark,
    
    secondary = CandyMint,
    onSecondary = SugarDark,
    secondaryContainer = CandyMint.copy(alpha = 0.2f),
    onSecondaryContainer = SugarDark,
    
    tertiary = CottonCandyBlue,
    onTertiary = SugarDark,
    tertiaryContainer = CottonCandyBlue.copy(alpha = 0.2f),
    onTertiaryContainer = SugarDark,
    
    background = SugarWhite,
    onBackground = TextOnLight,
    
    surface = SurfaceLight,
    onSurface = TextOnLight,
    surfaceVariant = SurfaceLight.copy(lightness = 0.95f),
    onSurfaceVariant = TextOnLight.copy(alpha = 0.7f),
    
    error = androidx.compose.ui.graphics.Color(0xFFFF6B6B),
    onError = SugarWhite,
    errorContainer = androidx.compose.ui.graphics.Color(0xFFFF6B6B).copy(alpha = 0.2f),
    onErrorContainer = SugarDark,
    
    border = TextOnLight.copy(alpha = 0.2f),
    outline = TextOnLight.copy(alpha = 0.3f),
    outlineVariant = TextOnLight.copy(alpha = 0.1f)
)

/**
 * Main TV Theme composable
 * Uses dark theme by default for optimal TV viewing
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvTheme(
    darkTheme: Boolean = true, // Default to dark for TV
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) TvDarkColorScheme else TvLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = TvTypography,
        content = content
    )
}

// Extension to adjust color lightness
private fun androidx.compose.ui.graphics.Color.copy(
    lightness: Float
): androidx.compose.ui.graphics.Color {
    val hsl = FloatArray(3)
    android.graphics.Color.RGBToHSV(
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt(),
        hsl
    )
    hsl[2] = hsl[2] * lightness
    val newColor = android.graphics.Color.HSVToColor((alpha * 255).toInt(), hsl)
    return androidx.compose.ui.graphics.Color(newColor)
}
