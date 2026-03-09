package com.sugarmunch.wear.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Typography

/**
 * Wear OS Theme
 */
@Composable
fun WearTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = androidx.wear.compose.material.ColorScheme(
            primary = Color(0xFFFF69B4),
            secondary = Color(0xFF9370DB),
            tertiary = Color(0xFFFFB6C1),
            background = Color(0xFF1A1A2E),
            surface = Color(0xFF2D2D44),
            error = Color(0xFFCF6679),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White,
            onError = Color.Black
        ),
        typography = Typography(),
        content = content
    )
}
