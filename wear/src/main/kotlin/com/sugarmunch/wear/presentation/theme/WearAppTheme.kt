package com.sugarmunch.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

// SugarMunch Wear OS Color Palette
private val SugarPink = Color(0xFFFFB6C1)
private val SugarMint = Color(0xFF98FF98)
private val SugarLemon = Color(0xFFFFFACD)
private val SugarBlue = Color(0xFFB5DEFF)
private val SugarCoral = Color(0xFFFF6B6B)
private val SugarPurple = Color(0xFFE8C5FF)

private val DarkBackground = Color(0xFF1A1A1A)
private val DarkSurface = Color(0xFF2D2D2D)

// Light color scheme (default for Wear OS)
private val LightColors = Colors(
    primary = Color(0xFFFF1493),
    primaryVariant = Color(0xFFFF69B4),
    secondary = Color(0xFF00CED1),
    secondaryVariant = Color(0xFF48D1CC),
    background = Color(0xFF000000),
    surface = Color(0xFF1A1A1A),
    error = Color(0xFFFF6B6B),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0),
    onError = Color.White
)

// Candy-themed color scheme
private val CandyColors = Colors(
    primary = SugarPink,
    primaryVariant = Color(0xFFFF69B4),
    secondary = SugarMint,
    secondaryVariant = Color(0xFF7FFFD4),
    background = DarkBackground,
    surface = DarkSurface,
    error = SugarCoral,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0B0),
    onError = Color.White
)

// High contrast color scheme for accessibility
private val HighContrastColors = Colors(
    primary = Color(0xFFFFFF00),
    primaryVariant = Color(0xFFFFD700),
    secondary = Color(0xFF00FFFF),
    secondaryVariant = Color(0xFF87CEEB),
    background = Color.Black,
    surface = Color(0xFF0A0A0A),
    error = Color(0xFFFF0000),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFCCCCCC),
    onError = Color.White
)

/**
 * SugarMunch Wear OS Theme
 */
@Composable
fun WearAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = LightColors,
        typography = MaterialTheme.typography,
        content = content
    )
}

/**
 * Candy variant theme
 */
@Composable
fun CandyWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = CandyColors,
        typography = MaterialTheme.typography,
        content = content
    )
}

/**
 * High contrast variant for accessibility
 */
@Composable
fun HighContrastWearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = HighContrastColors,
        typography = MaterialTheme.typography,
        content = content
    )
}

/**
 * Get colors for a specific theme ID
 */
fun getThemeColors(themeId: String?): Colors {
    return when (themeId) {
        "sugarrush_classic", "sugarrush_nuclear", "sugarrush_volcano" -> CandyColors
        "trippy_rainbow", "trippy_acid" -> HighContrastColors
        else -> LightColors
    }
}
