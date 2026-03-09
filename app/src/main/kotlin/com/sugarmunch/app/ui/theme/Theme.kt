package com.sugarmunch.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.sugarmunch.app.theme.engine.ThemeRuntimeSnapshot
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.model.AdjustedColors
import com.sugarmunch.app.theme.presets.ThemePresets
import com.sugarmunch.app.theme.profile.toThemeProfile
import com.sugarmunch.app.ui.theme.sugarTypographyForIntensity
import com.sugarmunch.app.ui.typography.toDynamicTypographyConfig

/**
 * SugarMunch Dynamic Theme System
 * Supports SugarRush intensity levels and custom candy themes
 * 
 * Color definitions moved to Color.kt to avoid duplication.
 */

/**
 * Convert AdjustedColors to Material3 ColorScheme
 */
fun AdjustedColors.toLightColorScheme(): androidx.compose.material3.ColorScheme {
    return lightColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.8f),
        onPrimaryContainer = onPrimary,
        secondary = secondary,
        onSecondary = onSurface,
        secondaryContainer = secondary.copy(alpha = 0.8f),
        onSecondaryContainer = onSurface,
        tertiary = tertiary,
        onTertiary = onSurface,
        tertiaryContainer = tertiary.copy(alpha = 0.8f),
        onTertiaryContainer = onSurface,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurface.copy(alpha = 0.7f),
        surfaceTint = primary,
        inverseSurface = onSurface,
        inverseOnSurface = surface,
        error = error,
        onError = androidx.compose.ui.graphics.Color.White,
        errorContainer = error.copy(alpha = 0.8f),
        onErrorContainer = androidx.compose.ui.graphics.Color.White,
        outline = onSurface.copy(alpha = 0.3f),
        outlineVariant = onSurface.copy(alpha = 0.1f),
        scrim = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.5f)
    )
}

fun AdjustedColors.toDarkColorScheme(): androidx.compose.material3.ColorScheme {
    return darkColorScheme(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primary.copy(alpha = 0.6f),
        onPrimaryContainer = onPrimary,
        secondary = secondary,
        onSecondary = onSurface,
        secondaryContainer = secondary.copy(alpha = 0.6f),
        onSecondaryContainer = onSurface,
        tertiary = tertiary,
        onTertiary = onSurface,
        tertiaryContainer = tertiary.copy(alpha = 0.6f),
        onTertiaryContainer = onSurface,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurface.copy(alpha = 0.7f),
        surfaceTint = primary,
        inverseSurface = onSurface,
        inverseOnSurface = surface,
        error = error,
        onError = androidx.compose.ui.graphics.Color.White,
        errorContainer = error.copy(alpha = 0.6f),
        onErrorContainer = androidx.compose.ui.graphics.Color.White,
        outline = onSurface.copy(alpha = 0.3f),
        outlineVariant = onSurface.copy(alpha = 0.1f),
        scrim = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.7f)
    )
}

/**
 * Main SugarMunch Theme composable with dynamic theming support
 */
@Composable
fun SugarMunchTheme(
    appId: String? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disable dynamic color by default for candy themes
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }

    val fallbackTheme = remember { ThemePresets.getDefault() }
    val runtime by themeManager.observeThemeRuntime(appId).collectAsState(
        initial = ThemeRuntimeSnapshot(
            profile = fallbackTheme.toThemeProfile(),
            theme = fallbackTheme,
            colors = fallbackTheme.getColorsForIntensity(1f),
            typography = fallbackTheme.toThemeProfile().typography.toDynamicTypographyConfig(context, emptyList()),
            themeIntensity = 1f,
            backgroundIntensity = 1f,
            particleIntensity = 1f,
            animationIntensity = 1f
        )
    )

    val currentTheme = runtime.theme
    val adjustedColors = runtime.colors
    
    // Determine if we should use dark theme
    val useDarkTheme = currentTheme.isDark || (darkTheme && currentTheme.id == "classic_candy")
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDarkTheme) dynamicDarkColorScheme(context) 
            else dynamicLightColorScheme(context)
        }
        useDarkTheme -> adjustedColors.toDarkColorScheme()
        else -> adjustedColors.toLightColorScheme()
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = adjustedColors.background.toArgb()
            window.navigationBarColor = adjustedColors.surface.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !useDarkTheme
                isAppearanceLightNavigationBars = !useDarkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = runtime.typography.toTypography(),
        content = content
    )
}

/**
 * Alternative theme entry point that forces a specific theme
 */
@Composable
fun SugarMunchThemeWithId(
    themeId: String,
    intensity: Float = 1f,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    
    // Temporarily set theme
    SideEffect {
        themeManager.setThemeById(themeId)
        themeManager.setMasterIntensity(intensity)
    }
    
    SugarMunchTheme {
        content()
    }
}

@Composable
fun ScopedSugarMunchTheme(
    appId: String,
    content: @Composable () -> Unit
) {
    SugarMunchTheme(appId = appId, content = content)
}

/**
 * Get current theme colors for use in composables
 */
@Composable
fun currentThemeColors(appId: String? = null): AdjustedColors {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val fallbackTheme = remember { ThemePresets.getDefault() }
    val runtime by themeManager.observeThemeRuntime(appId).collectAsState(
        initial = ThemeRuntimeSnapshot(
            profile = fallbackTheme.toThemeProfile(),
            theme = fallbackTheme,
            colors = fallbackTheme.getColorsForIntensity(1f),
            typography = fallbackTheme.toThemeProfile().typography.toDynamicTypographyConfig(context, emptyList()),
            themeIntensity = 1f,
            backgroundIntensity = 1f,
            particleIntensity = 1f,
            animationIntensity = 1f
        )
    )
    
    return runtime.colors
}

/**
 * Get current background brush
 */
@Composable
fun currentBackgroundBrush(appId: String? = null): androidx.compose.ui.graphics.Brush {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager.getInstance(context) }
    val fallbackTheme = remember { ThemePresets.getDefault() }
    val runtime by themeManager.observeThemeRuntime(appId).collectAsState(
        initial = ThemeRuntimeSnapshot(
            profile = fallbackTheme.toThemeProfile(),
            theme = fallbackTheme,
            colors = fallbackTheme.getColorsForIntensity(1f),
            typography = fallbackTheme.toThemeProfile().typography.toDynamicTypographyConfig(context, emptyList()),
            themeIntensity = 1f,
            backgroundIntensity = 1f,
            particleIntensity = 1f,
            animationIntensity = 1f
        )
    )
    
    return runtime.theme.getBackgroundGradient(runtime.backgroundIntensity)
}
