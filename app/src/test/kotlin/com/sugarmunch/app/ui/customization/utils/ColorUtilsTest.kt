package com.sugarmunch.app.ui.customization.utils

import com.sugarmunch.app.ui.customization.utils.ColorUtils
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ColorUtils
 */
class ColorUtilsTest {

    @Test
    fun hexToRgb_conversion_isCorrect() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val (r, g, b) = ColorUtils.hexToRgb(hex)

        // Then
        assertEquals(255, r)
        assertEquals(105, g)
        assertEquals(180, b)
    }

    @Test
    fun rgbToHex_conversion_isCorrect() {
        // Given
        val r = 255
        val g = 105
        val b = 180

        // When
        val hex = ColorUtils.rgbToHex(r, g, b)

        // Then
        assertEquals("#FFFF69B4", hex)
    }

    @Test
    fun hexToHsv_conversion_isCorrect() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val (h, s, v) = ColorUtils.rgbToHsv(*ColorUtils.hexToRgb(hex).toArray())

        // Then
        assertTrue(h in 0f..360f)
        assertTrue(s in 0f..1f)
        assertTrue(v in 0f..1f)
    }

    @Test
    fun hsvToRgb_conversion_isCorrect() {
        // Given
        val h = 330f
        val s = 0.59f
        val v = 1f

        // When
        val (r, g, b) = ColorUtils.hsvToRgb(h, s, v)

        // Then
        assertTrue(r in 0..255)
        assertTrue(g in 0..255)
        assertTrue(b in 0..255)
    }

    @Test
    fun getComplementary_color_isCorrect() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val complementary = ColorUtils.getComplementary(hex)

        // Then
        assertNotNull(complementary)
        assertTrue(complementary.startsWith("#"))
    }

    @Test
    fun getAnalogous_colors_count_isCorrect() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val analogous = ColorUtils.getAnalogous(hex)

        // Then
        assertEquals(5, analogous.size)
        analogous.forEach { assertTrue(it.startsWith("#")) }
    }

    @Test
    fun getTriadic_colors_count_isCorrect() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val triadic = ColorUtils.getTriadic(hex)

        // Then
        assertEquals(3, triadic.size)
        triadic.forEach { assertTrue(it.startsWith("#")) }
    }

    @Test
    fun getTetradic_colors_count_isCorrect() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val tetradic = ColorUtils.getTetradic(hex)

        // Then
        assertEquals(4, tetradic.size)
        tetradic.forEach { assertTrue(it.startsWith("#")) }
    }

    @Test
    fun getMonochromatic_colors_count_isCorrect() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val monochromatic = ColorUtils.getMonochromatic(hex)

        // Then
        assertEquals(5, monochromatic.size)
        monochromatic.forEach { assertTrue(it.startsWith("#")) }
    }

    @Test
    fun adjustSaturation_factor_zero_returnsGrayscale() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val grayscale = ColorUtils.adjustSaturation(hex, 0f)

        // Then
        assertNotNull(grayscale)
    }

    @Test
    fun adjustBrightness_factor_zero_returnsBlack() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val black = ColorUtils.adjustBrightness(hex, 0f)

        // Then
        assertEquals("#FF000000", black)
    }

    @Test
    fun blendColors_midpoint_isCorrect() {
        // Given
        val color1 = "#FF000000" // Black
        val color2 = "#FFFFFFFF" // White

        // When
        val blended = ColorUtils.blendColors(color1, color2, 0.5f)

        // Then
        assertNotNull(blended)
    }

    @Test
    fun getPerceivedBrightness_white_isBright() {
        // Given
        val white = "#FFFFFFFF"

        // When
        val brightness = ColorUtils.getPerceivedBrightness(white)

        // Then
        assertTrue(brightness > 0.8f)
    }

    @Test
    fun getPerceivedBrightness_black_isDark() {
        // Given
        val black = "#FF000000"

        // When
        val brightness = ColorUtils.getPerceivedBrightness(black)

        // Then
        assertTrue(brightness < 0.2f)
    }

    @Test
    fun isColorLight_white_isTrue() {
        // Given
        val white = "#FFFFFFFF"

        // When
        val isLight = ColorUtils.isColorLight(white)

        // Then
        assertTrue(isLight)
    }

    @Test
    fun isColorLight_black_isFalse() {
        // Given
        val black = "#FF000000"

        // When
        val isLight = ColorUtils.isColorLight(black)

        // Then
        assertFalse(isLight)
    }

    @Test
    fun colorDistance_sameColor_isZero() {
        // Given
        val color = "#FFFF69B4"

        // When
        val distance = ColorUtils.colorDistance(color, color)

        // Then
        assertEquals(0f, distance, 0.001f)
    }

    @Test
    fun colorDistance_differentColors_isPositive() {
        // Given
        val color1 = "#FF000000"
        val color2 = "#FFFFFFFF"

        // When
        val distance = ColorUtils.colorDistance(color1, color2)

        // Then
        assertTrue(distance > 0f)
    }

    @Test
    fun generatePalette_monochromatic_count_isCorrect() {
        // Given
        val baseColor = "#FFFF69B4"

        // When
        val palette = ColorUtils.generatePalette(baseColor, ColorUtils.PaletteType.MONOCHROMATIC, 5)

        // Then
        assertEquals(5, palette.size)
        palette.forEach { assertTrue(it.startsWith("#")) }
    }

    @Test
    fun generatePalette_gradient_count_isCorrect() {
        // Given
        val baseColor = "#FFFF69B4"

        // When
        val palette = ColorUtils.generatePalette(baseColor, ColorUtils.PaletteType.GRADIENT, 5)

        // Then
        assertEquals(5, palette.size)
        palette.forEach { assertTrue(it.startsWith("#")) }
    }

    @Test
    fun hexToComposeColor_conversion_isCorrect() {
        // Given
        val hex = "#FFFF69B4"

        // When
        val color = ColorUtils.hexToComposeColor(hex)

        // Then
        assertNotNull(color)
        assertEquals(1f, color.alpha, 0.001f)
    }

    @Test
    fun composeColorToHex_conversion_isCorrect() {
        // Given
        val color = androidx.compose.ui.graphics.Color(255, 105, 180)

        // When
        val hex = ColorUtils.composeColorToHex(color)

        // Then
        assertTrue(hex.startsWith("#"))
    }

    // Helper extension
    private fun Triple<Int, Int, Int>.toArray(): IntArray = intArrayOf(first, second, third)
}
