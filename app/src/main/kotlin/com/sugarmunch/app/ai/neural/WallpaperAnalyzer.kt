package com.sugarmunch.app.ai.neural

import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlin.math.*

/**
 * Wallpaper Mood types
 */
enum class WallpaperMood {
    ENERGETIC,
    CALM,
    MELANCHOLIC,
    NEUTRAL,
    ROMANTIC,
    MYSTERIOUS
}

/**
 * Color palette extracted from wallpaper
 */
data class WallpaperColorPalette(
    val dominantColors: List<Int> = emptyList(),
    val accentColors: List<Int> = emptyList(),
    val vibrantColors: List<Int> = emptyList(),
    val mutedColors: List<Int> = emptyList(),
    val lightColors: List<Int> = emptyList(),
    val darkColors: List<Int> = emptyList()
) {
    val primaryColor: Int get() = dominantColors.firstOrNull() ?: Color.BLACK
    val secondaryColor: Int get() = dominantColors.getOrElse(1) { primaryColor }
    val tertiaryColor: Int get() = dominantColors.getOrElse(2) { secondaryColor }

    fun toHexList(): List<String> {
        return dominantColors.map { ColorUtils.colorToHex(it) }
    }
}

/**
 * Wallpaper Analyzer - Extracts colors and analyzes mood from images
 */
class WallpaperAnalyzer {

    /**
     * Extract color palette from bitmap
     */
    fun extractColors(bitmap: Bitmap): WallpaperColorPalette {
        val palette = Palette.from(bitmap)
            .maximumColorCount(64)
            .resizeBitmapArea(192)
            .clearFilters()
            .generate()

        val allSwatches = palette.swatches

        // Extract dominant colors (most prominent)
        val dominantColors = listOfNotNull(
            palette.dominantSwatch?.rgb,
            palette.lightVibrantSwatch?.rgb,
            palette.darkVibrantSwatch?.rgb
        ).distinct()

        // Extract accent colors
        val accentColors = listOfNotNull(
            palette.vibrantSwatch?.rgb,
            palette.mutedSwatch?.rgb,
            palette.lightMutedSwatch?.rgb,
            palette.darkMutedSwatch?.rgb
        ).distinct()

        // Categorize by vibrancy
        val vibrantColors = allSwatches
            .filter { it.saturation > 0.5f }
            .map { it.rgb }
            .distinct()
            .take(5)

        val mutedColors = allSwatches
            .filter { it.saturation <= 0.5f }
            .map { it.rgb }
            .distinct()
            .take(5)

        // Categorize by lightness
        val lightColors = allSwatches
            .filter { it.hsl[2] > 0.7f }
            .map { it.rgb }
            .distinct()
            .take(5)

        val darkColors = allSwatches
            .filter { it.hsl[2] <= 0.3f }
            .map { it.rgb }
            .distinct()
            .take(5)

        return WallpaperColorPalette(
            dominantColors = dominantColors,
            accentColors = accentColors,
            vibrantColors = vibrantColors,
            mutedColors = mutedColors,
            lightColors = lightColors,
            darkColors = darkColors
        )
    }

    /**
     * Analyze mood from color palette
     */
    fun analyzeMood(colors: WallpaperColorPalette): WallpaperMood {
        if (colors.dominantColors.isEmpty()) {
            return WallpaperMood.NEUTRAL
        }

        val avgBrightness = colors.dominantColors.map { getBrightness(it) }.average()
        val avgSaturation = colors.dominantColors.map { getSaturation(it) }.average()
        val avgHue = colors.dominantColors.map { getHue(it) }.average()

        // Determine mood based on color characteristics
        return when {
            // Energetic: High saturation, medium-high brightness
            avgSaturation > 0.6 && avgBrightness > 0.5 -> WallpaperMood.ENERGETIC

            // Calm: Low saturation, high brightness (pastels)
            avgSaturation < 0.3 && avgBrightness > 0.7 -> WallpaperMood.CALM

            // Melancholic: Low brightness, low-medium saturation
            avgBrightness < 0.4 && avgSaturation < 0.5 -> WallpaperMood.MELANCHOLIC

            // Romantic: Pink/red hues, medium brightness
            avgHue in 300.0..360.0 || avgHue in 0.0..30.0 -> WallpaperMood.ROMANTIC

            // Mysterious: Dark colors, any saturation
            avgBrightness < 0.3 -> WallpaperMood.MYSTERIOUS

            else -> WallpaperMood.NEUTRAL
        }
    }

    /**
     * Analyze color temperature
     */
    fun analyzeColorTemperature(colors: WallpaperColorPalette): ColorTemperature {
        if (colors.dominantColors.isEmpty()) {
            return ColorTemperature.NEUTRAL
        }

        val avgHue = colors.dominantColors.map { getHue(it) }.average()

        return when {
            avgHue in 0.0..60.0 || avgHue in 300.0..360.0 -> ColorTemperature.WARM
            avgHue in 180.0..300.0 -> ColorTemperature.COOL
            else -> ColorTemperature.NEUTRAL
        }
    }

    /**
     * Analyze color harmony
     */
    fun analyzeColorHarmony(colors: WallpaperColorPalette): ColorHarmony {
        if (colors.dominantColors.size < 2) {
            return ColorHarmony.MONOCHROMATIC
        }

        val hues = colors.dominantColors.map { getHue(it) }.sorted()
        val hueDifferences = hues.zipWithNext { a, b -> abs(b - a) }
        val maxHueDiff = hueDifferences.maxOrNull() ?: 0.0

        return when {
            maxHueDiff < 30 -> ColorHarmony.MONOCHROMATIC
            maxHueDiff in 30.0..60.0 -> ColorHarmony.ANALOGOUS
            maxHueDiff in 120.0..150.0 -> ColorHarmony.TRIADIC
            maxHueDiff > 150 -> ColorHarmony.COMPLEMENTARY
            else -> ColorHarmony.SPLIT_COMPLEMENTARY
        }
    }

    /**
     * Generate complementary color
     */
    fun generateComplementaryColor(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        // Rotate hue by 180 degrees
        hsv[0] = (hsv[0] + 180) % 360

        return Color.HSVToColor(hsv)
    }

    /**
     * Generate analogous colors
     */
    fun generateAnalogousColors(color: Int, count: Int = 2): List<Int> {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        return (1..count).map { i ->
            val newHsv = hsv.clone()
            newHsv[0] = (newHsv[0] + (30 * i)) % 360
            Color.HSVToColor(newHsv)
        }
    }

    /**
     * Generate triadic colors
     */
    fun generateTriadicColors(color: Int): List<Int> {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        return listOf(120, 240).map { offset ->
            val newHsv = hsv.clone()
            newHsv[0] = (newHsv[0] + offset) % 360
            Color.HSVToColor(newHsv)
        }
    }

    /**
     * Adjust color lightness
     */
    fun adjustLightness(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)

        return Color.HSVToColor(hsv)
    }

    /**
     * Adjust color saturation
     */
    fun adjustSaturation(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)

        hsv[1] = (hsv[1] * factor).coerceIn(0f, 1f)

        return Color.HSVToColor(hsv)
    }

    /**
     * Generate gradient colors between two colors
     */
    fun generateGradientColors(start: Int, end: Int, steps: Int): List<Int> {
        return (0..steps).map { t ->
            val ratio = t.toFloat() / steps
            lerpColor(start, end, ratio)
        }
    }

    /**
     * Get color contrast ratio
     */
    fun getContrastRatio(color1: Int, color2: Int): Double {
        return ColorUtils.calculateContrast(color1, color2)
    }

    /**
     * Check if color is accessible on background
     */
    fun isAccessible(foreground: Int, background: Int, minRatio: Float = 4.5f): Boolean {
        return getContrastRatio(foreground, background) >= minRatio
    }

    /**
     * Find best text color for background
     */
    fun findBestTextColor(background: Int): Int {
        val whiteContrast = getContrastRatio(Color.WHITE, background)
        val blackContrast = getContrastRatio(Color.BLACK, background)

        return if (whiteContrast > blackContrast) Color.WHITE else Color.BLACK
    }

    // ========== PRIVATE UTILITIES ==========

    private fun getBrightness(color: Int): Double {
        return ColorUtils.calculateLuminance(color)
    }

    private fun getSaturation(color: Int): Float {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[1]
    }

    private fun getHue(color: Int): Double {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[0].toDouble()
    }

    private fun lerpColor(color1: Int, color2: Int, t: Float): Int {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)
        val a1 = Color.alpha(color1)

        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)
        val a2 = Color.alpha(color2)

        val r = (r1 + (r2 - r1) * t).toInt().coerceIn(0, 255)
        val g = (g1 + (g2 - g1) * t).toInt().coerceIn(0, 255)
        val b = (b1 + (b2 - b1) * t).toInt().coerceIn(0, 255)
        val a = (a1 + (a2 - a1) * t).toInt().coerceIn(0, 255)

        return Color.argb(a, r, g, b)
    }

    private fun <T> List<T>.zipWithNext(transform: (Pair<T, T>) -> T): List<T> {
        if (size < 2) return emptyList()
        return indices.dropLast(1).map { transform(this[it] to this[it + 1]) }
    }
}

/**
 * Color temperature classification
 */
enum class ColorTemperature {
    WARM,
    COOL,
    NEUTRAL
}

/**
 * Color harmony types
 */
enum class ColorHarmony {
    MONOCHROMATIC,
    ANALOGOUS,
    COMPLEMENTARY,
    SPLIT_COMPLEMENTARY,
    TRIADIC,
    TETRADIC
}
