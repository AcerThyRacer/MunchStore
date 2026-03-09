package com.sugarmunch.app.ui.theme.materialyou

import android.app.WallpaperColors
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sugarmunch.app.theme.model.BaseColors

/**
 * PHASE 13: MATERIAL YOU INTEGRATION
 * Dynamic color extraction and theming
 */

/**
 * Get dynamic colors from wallpaper (Android 12+)
 */
@Composable
fun getDynamicColors(context: Context): BaseColors {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val wallpaperColors = WallpaperManager.getInstance(context).wallpaperColors
        val primaryColor = wallpaperColors?.primaryColor?.toArgb() ?: 0xFFFF69B4

        BaseColors(
            primary = primaryColor,
            secondary = wallpaperColors?.secondaryColor?.toArgb() ?: 0xFF9370DB,
            tertiary = wallpaperColors?.tertiaryColor?.toArgb() ?: 0xFFFFB6C1,
            background = 0xFF1A1A2E,
            onPrimary = Color.WHITE,
            onSecondary = Color.WHITE,
            onBackground = Color.WHITE
        )
    } else {
        // Fallback for older Android versions
        BaseColors(
            primary = 0xFFFF69B4,
            secondary = 0xFF9370DB,
            tertiary = 0xFFFFB6C1,
            background = 0xFF1A1A2E,
            onPrimary = Color.WHITE,
            onSecondary = Color.WHITE,
            onBackground = Color.WHITE
        )
    }
}

/**
 * Extract dominant colors from image
 */
fun extractColorsFromImage(bitmap: Bitmap): List<Int> {
    val colors = mutableListOf<Int>()
    val width = bitmap.width
    val height = bitmap.height
    val pixels = IntArray(width * height)
    
    bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
    
    // Simple color extraction (in production, use Palette API)
    val colorCount = mutableMapOf<Int, Int>()
    pixels.forEach { pixel ->
        // Quantize colors to reduce variations
        val r = (Color.red(pixel) / 32) * 32
        val g = (Color.green(pixel) / 32) * 32
        val b = (Color.blue(pixel) / 32) * 32
        val quantized = Color.rgb(r, g, b)
        colorCount[quantized] = (colorCount[quantized] ?: 0) + 1
    }
    
    // Get top 5 colors
    val sortedColors = colorCount.entries.sortedByDescending { it.value }.take(5)
    sortedColors.forEach { colors.add(it.key) }
    
    return colors
}

/**
 * Generate color scheme from seed color
 */
fun generateColorScheme(seedColor: Int): MaterialYouColorScheme {
    val hsl = FloatArray(3)
    Color.colorToHSV(seedColor, hsl)
    
    val hue = hsl[0]
    val saturation = hsl[1]
    val lightness = hsl[2]
    
    // Generate harmonious colors
    val complementary = Color.HSLToColor(floatArrayOf((hue + 180) % 360, saturation, lightness))
    val analogous1 = Color.HSLToColor(floatArrayOf((hue + 30) % 360, saturation, lightness))
    val analogous2 = Color.HSLToColor(floatArrayOf((hue - 30 + 360) % 360, saturation, lightness))
    val triadic1 = Color.HSLToColor(floatArrayOf((hue + 120) % 360, saturation, lightness))
    val triadic2 = Color.HSLToColor(floatArrayOf((hue + 240) % 360, saturation, lightness))
    
    return MaterialYouColorScheme(
        primary = seedColor,
        secondary = complementary,
        tertiary = analogous1,
        quaternary = analogous2,
        accent1 = triadic1,
        accent2 = triadic2
    )
}

/**
 * Material You color scheme
 */
data class MaterialYouColorScheme(
    val primary: Int,
    val secondary: Int,
    val tertiary: Int,
    val quaternary: Int,
    val accent1: Int,
    val accent2: Int
) {
    fun toBaseColors(): BaseColors {
        return BaseColors(
            primary = primary,
            secondary = secondary,
            tertiary = tertiary,
            background = 0xFF1A1A2E,
            onPrimary = Color.WHITE,
            onSecondary = Color.WHITE,
            onBackground = Color.WHITE
        )
    }
}

/**
 * Get tonal palette from seed color
 */
fun getTonalPalette(seedColor: Int): TonalPalette {
    val hsl = FloatArray(3)
    Color.colorToHSV(seedColor, hsl)
    
    return TonalPalette(
        hue = hsl[0],
        chroma = hsl[1]
    )
}

data class TonalPalette(
    val hue: Float,
    val chroma: Float
) {
    fun getColor(tone: Int): Int {
        val lightness = tone / 100f
        return Color.HSLToColor(floatArrayOf(hue, chroma, lightness))
    }
}

/**
 * Dynamic theme preview
 */
@Composable
fun DynamicThemePreview(
    seedColor: Int,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier
) {
    val colorScheme = remember(seedColor) { generateColorScheme(seedColor) }
    
    androidx.compose.foundation.layout.Column(
        modifier = modifier
    ) {
        // Preview circles
        androidx.compose.foundation.layout.Row {
            listOf(
                colorScheme.primary,
                colorScheme.secondary,
                colorScheme.tertiary,
                colorScheme.quaternary,
                colorScheme.accent1,
                colorScheme.accent2
            ).forEach { color ->
                androidx.compose.foundation.Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(40.dp)
                        .padding(4.dp)
                        .background(
                            androidx.compose.ui.graphics.Color(color),
                            androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Icon theming based on current theme
 */
@Composable
fun ThemedIcon(
    icon: String,
    colors: BaseColors,
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    size: androidx.compose.ui.unit.Dp = 24.dp
) {
    androidx.compose.material3.Icon(
        modifier = modifier.size(size),
        contentDescription = icon,
        tint = androidx.compose.ui.graphics.Color(colors.primary),
        // In production, use actual icon implementation
        androidx.compose.material.icons.Icons.Default.Favorite
    )
}

/**
 * Shape theming
 */
object ShapeTheming {
    val extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(4.dp)
    val small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    val medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    val large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    val extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
    val circular = androidx.compose.foundation.shape.CircleShape
}

/**
 * Typography theming
 * Note: These properties must be accessed within a @Composable context.
 * Use LocalTypography.current or MaterialTheme.typography directly in composables.
 */
object TypographyTheming {
    @Composable
    fun getDisplayLarge() = androidx.compose.material3.MaterialTheme.typography.displayLarge
    @Composable
    fun getDisplayMedium() = androidx.compose.material3.MaterialTheme.typography.displayMedium
    @Composable
    fun getDisplaySmall() = androidx.compose.material3.MaterialTheme.typography.displaySmall
    @Composable
    fun getHeadlineLarge() = androidx.compose.material3.MaterialTheme.typography.headlineLarge
    @Composable
    fun getHeadlineMedium() = androidx.compose.material3.MaterialTheme.typography.headlineMedium
    @Composable
    fun getHeadlineSmall() = androidx.compose.material3.MaterialTheme.typography.headlineSmall
    @Composable
    fun getTitleLarge() = androidx.compose.material3.MaterialTheme.typography.titleLarge
    @Composable
    fun getTitleMedium() = androidx.compose.material3.MaterialTheme.typography.titleMedium
    @Composable
    fun getTitleSmall() = androidx.compose.material3.MaterialTheme.typography.titleSmall
    @Composable
    fun getBodyLarge() = androidx.compose.material3.MaterialTheme.typography.bodyLarge
    @Composable
    fun getBodyMedium() = androidx.compose.material3.MaterialTheme.typography.bodyMedium
    @Composable
    fun getBodySmall() = androidx.compose.material3.MaterialTheme.typography.bodySmall
    @Composable
    fun getLabelLarge() = androidx.compose.material3.MaterialTheme.typography.labelLarge
    @Composable
    fun getLabelMedium() = androidx.compose.material3.MaterialTheme.typography.labelMedium
    @Composable
    fun getLabelSmall() = androidx.compose.material3.MaterialTheme.typography.labelSmall
}
