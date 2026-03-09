package com.sugarmunch.app.ui.customization.utils

import android.graphics.Color
import androidx.compose.ui.graphics
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * EXTREME Color Utilities for SugarMunch
 * Advanced color manipulation, conversion, and harmony generation
 */
object ColorUtils {

    // ═══════════════════════════════════════════════════════════════
    // COLOR CONVERSION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Convert HEX color to RGB components
     */
    fun hexToRgb(hex: String): Triple<Int, Int, Int> {
        val cleanHex = hex.removePrefix("#")
        val r = cleanHex.substring(0, 2).toInt(16)
        val g = cleanHex.substring(2, 4).toInt(16)
        val b = cleanHex.substring(4, 6).toInt(16)
        return Triple(r, g, b)
    }

    /**
     * Convert RGB to HEX color
     */
    fun rgbToHex(r: Int, g: Int, b: Int): String {
        return "#%02X%02X%02X".format(r, g, b)
    }

    /**
     * Convert RGB to HSV
     */
    fun rgbToHsv(r: Int, g: Int, b: Int): Triple<Float, Float, Float> {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f

        val max = max(rf, max(gf, bf))
        val min = min(rf, min(gf, bf))
        val delta = max - min

        val h = when {
            delta == 0f -> 0f
            max == rf -> 60f * ((gf - bf) / delta % 6)
            max == gf -> 60f * ((bf - rf) / delta + 2)
            else -> 60f * ((rf - gf) / delta + 4)
        }

        val s = if (max == 0f) 0f else delta / max
        val v = max

        return Triple(
            (h + 360) % 360,
            s,
            v
        )
    }

    /**
     * Convert HSV to RGB
     */
    fun hsvToRgb(h: Float, s: Float, v: Float): Triple<Int, Int, Int> {
        val c = v * s
        val x = c * (1 - abs((h / 60) % 2 - 1))
        val m = v - c

        val (rf, gf, bf) = when {
            h < 60 -> Triple(c, x, 0f)
            h < 120 -> Triple(x, c, 0f)
            h < 180 -> Triple(0f, c, x)
            h < 240 -> Triple(0f, x, c)
            h < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Triple(
            ((rf + m) * 255).roundToInt(),
            ((gf + m) * 255).roundToInt(),
            ((bf + m) * 255).roundToInt()
        )
    }

    /**
     * Convert RGB to HSL
     */
    fun rgbToHsl(r: Int, g: Int, b: Int): Triple<Float, Float, Float> {
        val rf = r / 255f
        val gf = g / 255f
        val bf = b / 255f

        val max = max(rf, max(gf, bf))
        val min = min(rf, min(gf, bf))
        val delta = max - min

        val l = (max + min) / 2

        val s = if (delta == 0f) 0f
        else delta / (1 - abs(2 * l - 1))

        val h = when {
            delta == 0f -> 0f
            max == rf -> 60f * ((gf - bf) / delta % 6)
            max == gf -> 60f * ((bf - rf) / delta + 2)
            else -> 60f * ((rf - gf) / delta + 4)
        }

        return Triple(
            (h + 360) % 360,
            s,
            l
        )
    }

    /**
     * Convert HSL to RGB
     */
    fun hslToRgb(h: Float, s: Float, l: Float): Triple<Int, Int, Int> {
        val c = (1 - abs(2 * l - 1)) * s
        val x = c * (1 - abs((h / 60) % 2 - 1))
        val m = l - c / 2

        val (rf, gf, bf) = when {
            h < 60 -> Triple(c, x, 0f)
            h < 120 -> Triple(x, c, 0f)
            h < 180 -> Triple(0f, c, x)
            h < 240 -> Triple(0f, x, c)
            h < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }

        return Triple(
            ((rf + m) * 255).roundToInt(),
            ((gf + m) * 255).roundToInt(),
            ((bf + m) * 255).roundToInt()
        )
    }

    // ═══════════════════════════════════════════════════════════════
    // COLOR HARMONIES
    // ═══════════════════════════════════════════════════════════════

    /**
     * Generate complementary color (180° opposite on color wheel)
     */
    fun getComplementary(hex: String): String {
        val (h, s, v) = rgbToHsv(*hexToRgb(hex).toArray())
        val newH = (h + 180) % 360
        val (r, g, b) = hsvToRgb(newH, s, v)
        return rgbToHex(r, g, b)
    }

    /**
     * Generate analogous colors (30° apart on color wheel)
     */
    fun getAnalogous(hex: String): List<String> {
        val (h, s, v) = rgbToHsv(*hexToRgb(hex).toArray())
        return listOf(
            hex,
            generateColorAtAngle((h + 30) % 360, s, v),
            generateColorAtAngle((h + 60) % 360, s, v),
            generateColorAtAngle((h - 30 + 360) % 360, s, v),
            generateColorAtAngle((h - 60 + 360) % 360, s, v)
        )
    }

    /**
     * Generate triadic colors (120° apart on color wheel)
     */
    fun getTriadic(hex: String): List<String> {
        val (h, s, v) = rgbToHsv(*hexToRgb(hex).toArray())
        return listOf(
            hex,
            generateColorAtAngle((h + 120) % 360, s, v),
            generateColorAtAngle((h + 240) % 360, s, v)
        )
    }

    /**
     * Generate tetradic colors (90° apart on color wheel)
     */
    fun getTetradic(hex: String): List<String> {
        val (h, s, v) = rgbToHsv(*hexToRgb(hex).toArray())
        return listOf(
            hex,
            generateColorAtAngle((h + 90) % 360, s, v),
            generateColorAtAngle((h + 180) % 360, s, v),
            generateColorAtAngle((h + 270) % 360, s, v)
        )
    }

    /**
     * Generate split-complementary colors
     */
    fun getSplitComplementary(hex: String): List<String> {
        val (h, s, v) = rgbToHsv(*hexToRgb(hex).toArray())
        return listOf(
            hex,
            generateColorAtAngle((h + 150) % 360, s, v),
            generateColorAtAngle((h + 210) % 360, s, v)
        )
    }

    /**
     * Generate monochromatic colors (same hue, different saturation/lightness)
     */
    fun getMonochromatic(hex: String): List<String> {
        val (h, s, v) = rgbToHsv(*hexToRgb(hex).toArray())
        return listOf(
            hex,
            generateColorAtAngle(h, s * 0.75f, v),
            generateColorAtAngle(h, s * 0.5f, v),
            generateColorAtAngle(h, s, min(1f, v * 1.2f)),
            generateColorAtAngle(h, s, max(0.3f, v * 0.7f))
        )
    }

    private fun generateColorAtAngle(h: Float, s: Float, v: Float): String {
        val (r, g, b) = hsvToRgb(h, s, v)
        return rgbToHex(r, g, b)
    }

    private fun Triple<Int, Int, Int>.toArray(): IntArray = intArray(first, second, third)

    private fun intArray(a: Int, b: Int, c: Int): IntArray = intArrayOf(a, b, c)

    // ═══════════════════════════════════════════════════════════════
    // COLOR MANIPULATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Adjust color temperature (warm to cool)
     * @param kelvin Temperature in Kelvin (2700-6500)
     */
    fun adjustColorTemperature(hex: String, kelvin: Float): String {
        val (r, g, b) = hexToRgb(hex)

        // Normalize kelvin (2700K - 6500K)
        val temp = (kelvin - 2700) / (6500 - 2700)

        // Apply temperature adjustment
        val rf = r * (1 + temp * 0.2f)
        val gf = g * (1 + (temp - 0.5f) * 0.1f)
        val bf = b * (1 + (1 - temp) * 0.2f)

        return rgbToHex(
            rf.toInt().coerceIn(0, 255),
            gf.toInt().coerceIn(0, 255),
            bf.toInt().coerceIn(0, 255)
        )
    }

    /**
     * Adjust saturation of a color
     * @param factor Saturation factor (0 = grayscale, 1 = original, 2 = double)
     */
    fun adjustSaturation(hex: String, factor: Float): String {
        val (h, s, v) = rgbToHsv(*hexToRgb(hex).toArray())
        val newS = (s * factor).coerceIn(0f, 1f)
        val (r, g, b) = hsvToRgb(h, newS, v)
        return rgbToHex(r, g, b)
    }

    /**
     * Adjust brightness of a color
     * @param factor Brightness factor (0 = black, 1 = original, 2 = double)
     */
    fun adjustBrightness(hex: String, factor: Float): String {
        val (h, s, v) = rgbToHsv(*hexToRgb(hex).toArray())
        val newV = (v * factor).coerceIn(0f, 1f)
        val (r, g, b) = hsvToRgb(h, s, newV)
        return rgbToHex(r, g, b)
    }

    /**
     * Adjust contrast of a color relative to a background
     */
    fun adjustContrast(hex: String, backgroundHex: String, factor: Float): String {
        val (r, g, b) = hexToRgb(hex)
        val (br, bg, bb) = hexToRgb(backgroundHex)

        // Calculate luminance difference
        val luminance1 = (0.299 * r + 0.587 * g + 0.114 * b) / 255
        val luminance2 = (0.299 * br + 0.587 * bg + 0.114 * bb) / 255

        val contrast = abs(luminance1 - luminance2)
        val targetContrast = (contrast * factor).coerceIn(0f, 1f)

        // Adjust color to achieve target contrast
        val adjustment = (targetContrast - luminance1) * 255

        return rgbToHex(
            (r + adjustment).toInt().coerceIn(0, 255),
            (g + adjustment).toInt().coerceIn(0, 255),
            (b + adjustment).toInt().coerceIn(0, 255)
        )
    }

    /**
     * Blend two colors together
     * @param factor Blend factor (0 = first color, 1 = second color)
     */
    fun blendColors(color1: String, color2: String, factor: Float): String {
        val (r1, g1, b1) = hexToRgb(color1)
        val (r2, g2, b2) = hexToRgb(color2)

        return rgbToHex(
            (r1 + (r2 - r1) * factor).toInt().coerceIn(0, 255),
            (g1 + (g2 - g1) * factor).toInt().coerceIn(0, 255),
            (b1 + (b2 - b1) * factor).toInt().coerceIn(0, 255)
        )
    }

    // ═══════════════════════════════════════════════════════════════
    // COLOR ANALYSIS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Calculate the perceived brightness of a color
     */
    fun getPerceivedBrightness(hex: String): Float {
        val (r, g, b) = hexToRgb(hex)
        return (0.299 * r + 0.587 * g + 0.114 * b) / 255
    }

    /**
     * Determine if a color is light or dark
     */
    fun isColorLight(hex: String): Boolean {
        return getPerceivedBrightness(hex) > 0.5f
    }

    /**
     * Get the dominant hue of a color
     */
    fun getDominantHue(hex: String): Float {
        val (h, s, v) = rgbToHsv(*hexToRgb(hex).toArray())
        return h
    }

    /**
     * Calculate color distance (Euclidean in RGB space)
     */
    fun colorDistance(color1: String, color2: String): Float {
        val (r1, g1, b1) = hexToRgb(color1)
        val (r2, g2, b2) = hexToRgb(color2)

        return kotlin.math.sqrt(
            (r1 - r2).toDouble().pow(2) +
            (g1 - g2).toDouble().pow(2) +
            (b1 - b2).toDouble().pow(2)
        ).toFloat()
    }

    private fun Double.pow(exponent: Int): Double = Math.pow(this, exponent.toDouble())

    // ═══════════════════════════════════════════════════════════════
    // COLOR PALETTE GENERATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Generate a color palette from a base color
     */
    fun generatePalette(
        baseColor: String,
        paletteType: PaletteType,
        steps: Int = 5
    ): List<String> {
        return when (paletteType) {
            PaletteType.MONOCHROMATIC -> generateMonochromaticPalette(baseColor, steps)
            PaletteType.GRADIENT -> generateGradientPalette(baseColor, steps)
            PaletteType.SHADES -> generateShadesPalette(baseColor, steps)
            PaletteType.TINTS -> generateTintsPalette(baseColor, steps)
        }
    }

    private fun generateMonochromaticPalette(baseColor: String, steps: Int): List<String> {
        val (h, s, v) = rgbToHsv(*hexToRgb(baseColor).toArray())
        return List(steps) { i ->
            val newS = s * (0.5f + i.toFloat() / steps * 0.5f)
            val newV = v * (0.5f + i.toFloat() / steps * 0.5f)
            val (r, g, b) = hsvToRgb(h, newS.coerceIn(0f, 1f), newV.coerceIn(0f, 1f))
            rgbToHex(r, g, b)
        }
    }

    private fun generateGradientPalette(baseColor: String, steps: Int): List<String> {
        val (h, s, v) = rgbToHsv(*hexToRgb(baseColor).toArray())
        return List(steps) { i ->
            val newH = (h + i * 360 / steps) % 360
            val (r, g, b) = hsvToRgb(newH, s, v)
            rgbToHex(r, g, b)
        }
    }

    private fun generateShadesPalette(baseColor: String, steps: Int): List<String> {
        val (r, g, b) = hexToRgb(baseColor)
        return List(steps) { i ->
            val factor = 1 - i.toFloat() / steps * 0.8f
            rgbToHex(
                (r * factor).toInt().coerceIn(0, 255),
                (g * factor).toInt().coerceIn(0, 255),
                (b * factor).toInt().coerceIn(0, 255)
            )
        }
    }

    private fun generateTintsPalette(baseColor: String, steps: Int): List<String> {
        val (r, g, b) = hexToRgb(baseColor)
        return List(steps) { i ->
            val factor = i.toFloat() / steps * 0.8f
            rgbToHex(
                (r + (255 - r) * factor).toInt().coerceIn(0, 255),
                (g + (255 - g) * factor).toInt().coerceIn(0, 255),
                (b + (255 - b) * factor).toInt().coerceIn(0, 255)
            )
        }
    }

    enum class PaletteType {
        MONOCHROMATIC,
        GRADIENT,
        SHADES,
        TINTS
    }

    // ═══════════════════════════════════════════════════════════════
    // COMPOSE COLOR CONVERSION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Convert HEX to Compose Color
     */
    fun hexToComposeColor(hex: String): androidx.compose.ui.graphics.Color {
        val color = Color.parseColor(hex)
        return androidx.compose.ui.graphics.Color(color)
    }

    /**
     * Convert Compose Color to HEX
     */
    fun composeColorToHex(color: androidx.compose.ui.graphics.Color): String {
        val androidColor = android.graphics.Color.argb(
            (color.alpha * 255).toInt(),
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        return "#%08X".format(androidColor)
    }
}
