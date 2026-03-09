package com.sugarmunch.app.ai.neural

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import com.sugarmunch.app.theme.model.ThemeColors

/**
 * Color Psychology Engine
 * Maps colors to psychological effects and adjusts them for mood
 */
class ColorPsychology {

    /**
     * Color meanings and associations
     */
    data class ColorMeaning(
        val emotions: List<String>,
        val associations: List<String>,
        val effects: List<String>,
        val bestFor: List<String>,
        val avoidFor: List<String>
    )

    private val colorMeanings = mapOf(
        Color.RED to ColorMeaning(
            emotions = listOf("passion", "energy", "excitement", "anger", "love"),
            associations = listOf("fire", "heart", "danger", "power"),
            effects = listOf("increases heart rate", "creates urgency", "stimulates appetite"),
            bestFor = listOf("action buttons", "warnings", "sales"),
            avoidFor = listOf("relaxation", "trust building")
        ),
        Color.BLUE to ColorMeaning(
            emotions = listOf("calm", "trust", "security", "sadness"),
            associations = listOf("sky", "ocean", "stability", "professionalism"),
            effects = listOf("calming effect", "increases productivity", "suppresses appetite"),
            bestFor = listOf("corporate", "healthcare", "technology"),
            avoidFor = listOf("food apps", "energetic themes")
        ),
        Color.GREEN to ColorMeaning(
            emotions = listOf("growth", "harmony", "freshness", "safety"),
            associations = listOf("nature", "money", "health", "environment"),
            effects = listOf("reduces stress", "improves reading ability", "encourages action"),
            bestFor = listOf("finance", "health", "environmental"),
            avoidFor = listOf("luxury products")
        ),
        Color.YELLOW to ColorMeaning(
            emotions = listOf("happiness", "optimism", "caution", "anxiety"),
            associations = listOf("sun", "warmth", "warning", "creativity"),
            effects = listOf("stimulates mental activity", "creates optimism", "can cause eye strain"),
            bestFor = listOf("children", "creative", "food"),
            avoidFor = listOf("luxury", "professional services")
        ),
        Color.parseColor("#800080") to ColorMeaning(
            emotions = listOf("luxury", "creativity", "wisdom", "mystery"),
            associations = listOf("royalty", "magic", "spirituality", "sophistication"),
            effects = listOf("encourages creativity", "calms nerves", "can seem artificial"),
            bestFor = listOf("beauty", "luxury", "spiritual"),
            avoidFor = listOf("budget products")
        ),
        Color.parseColor("#FFA500") to ColorMeaning(
            emotions = listOf("enthusiasm", "creativity", "warmth", "caution"),
            associations = listOf("sunset", "citrus", "energy", "affordability"),
            effects = listOf("increases energy", "stimulates activity", "attracts attention"),
            bestFor = listOf("call-to-action", "food", "children"),
            avoidFor = listOf("luxury", "professional")
        ),
        Color.parseColor("#FFC0CB") to ColorMeaning(
            emotions = listOf("love", "sweetness", "calm", "nostalgia"),
            associations = listOf("romance", "childhood", "femininity", "spring"),
            effects = listOf("calming effect", "reduces aggression", "can seem immature"),
            bestFor = listOf("beauty", "romance", "children"),
            avoidFor = listOf("professional", "masculine products")
        ),
        Color.BLACK to ColorMeaning(
            emotions = listOf("power", "elegance", "mystery", "sadness"),
            associations = listOf("luxury", "death", "sophistication", "formality"),
            effects = listOf("creates sophistication", "can be intimidating", "hides details"),
            bestFor = listOf("luxury", "fashion", "professional"),
            avoidFor = listOf("children", "healthcare")
        ),
        Color.WHITE to ColorMeaning(
            emotions = listOf("purity", "cleanliness", "simplicity", "emptiness"),
            associations = listOf("snow", "clouds", "wedding", "minimalism"),
            effects = listOf("creates space", "improves clarity", "can seem sterile"),
            bestFor = listOf("minimalist", "healthcare", "technology"),
            avoidFor = listOf("children", "food")
        ),
        Color.GRAY to ColorMeaning(
            emotions = listOf("neutrality", "balance", "boredom", "sophistication"),
            associations = listOf("concrete", "clouds", "technology", "age"),
            effects = listOf("creates neutrality", "can be depressing", "professional appearance"),
            bestFor = listOf("corporate", "technology", "professional"),
            avoidFor = listOf("children", "energetic themes")
        )
    )

    /**
     * Adjust colors based on mood
     */
    fun adjustColorsForMood(
        primary: Int,
        secondary: Int,
        tertiary: Int,
        mood: WallpaperMood
    ): AdjustedColors {
        return when (mood) {
            WallpaperMood.ENERGETIC -> adjustForEnergetic(primary, secondary, tertiary)
            WallpaperMood.CALM -> adjustForCalm(primary, secondary, tertiary)
            WallpaperMood.MELANCHOLIC -> adjustForMelancholic(primary, secondary, tertiary)
            WallpaperMood.ROMANTIC -> adjustForRomantic(primary, secondary, tertiary)
            WallpaperMood.MYSTERIOUS -> adjustForMysterious(primary, secondary, tertiary)
            WallpaperMood.NEUTRAL -> AdjustedColors(primary, secondary, tertiary)
        }
    }

    private fun adjustForEnergetic(primary: Int, secondary: Int, tertiary: Int): AdjustedColors {
        // Increase saturation and brightness for energy
        return AdjustedColors(
            primary = increaseVibrancy(primary, 1.3f),
            secondary = increaseVibrancy(secondary, 1.2f),
            tertiary = increaseVibrancy(tertiary, 1.4f)
        )
    }

    private fun adjustForCalm(primary: Int, secondary: Int, tertiary: Int): AdjustedColors {
        // Decrease saturation, increase lightness for calm
        return AdjustedColors(
            primary = decreaseSaturation(primary, 0.7f),
            secondary = decreaseSaturation(secondary, 0.6f),
            tertiary = decreaseSaturation(tertiary, 0.5f)
        )
    }

    private fun adjustForMelancholic(primary: Int, secondary: Int, tertiary: Int): AdjustedColors {
        // Decrease brightness and saturation for melancholy
        return AdjustedColors(
            primary = adjustLightnessAndSaturation(primary, 0.7f, 0.6f),
            secondary = adjustLightnessAndSaturation(secondary, 0.6f, 0.5f),
            tertiary = adjustLightnessAndSaturation(tertiary, 0.5f, 0.4f)
        )
    }

    private fun adjustForRomantic(primary: Int, secondary: Int, tertiary: Int): AdjustedColors {
        // Shift towards pink/red, soften
        return AdjustedColors(
            primary = shiftTowardsPink(primary),
            secondary = shiftTowardsPink(secondary),
            tertiary = shiftTowardsPink(tertiary)
        )
    }

    private fun adjustForMysterious(primary: Int, secondary: Int, tertiary: Int): AdjustedColors {
        // Darken and add purple/blue tones
        return AdjustedColors(
            primary = darkenAndShift(primary, 0.6f, 240),
            secondary = darkenAndShift(secondary, 0.5f, 270),
            tertiary = darkenAndShift(tertiary, 0.4f, 300)
        )
    }

    /**
     * Get psychological meaning of a color
     */
    fun getColorMeaning(color: Int): ColorMeaning? {
        // Find closest color in our map
        return colorMeanings.entries.minByOrNull { entry ->
            colorDistance(color, entry.key)
        }?.value
    }

    /**
     * Suggest colors for specific purposes
     */
    fun suggestColorsForPurpose(purpose: ColorPurpose): List<Int> {
        return when (purpose) {
            ColorPurpose.TRUST -> listOf(
                Color.parseColor("#1E88E5"), // Blue
                Color.parseColor("#42A5F5"), // Light Blue
                Color.parseColor("#64B5F6")  // Lighter Blue
            )
            ColorPurpose.ENERGY -> listOf(
                Color.parseColor("#FF5722"), // Orange
                Color.parseColor("#FFC107"), // Amber
                Color.parseColor("#FFEB3B")  // Yellow
            )
            ColorPurpose.CALM -> listOf(
                Color.parseColor("#81C784"), // Green
                Color.parseColor("#A5D6A7"), // Light Green
                Color.parseColor("#C8E6C9")  // Lighter Green
            )
            ColorPurpose.LUXURY -> listOf(
                Color.parseColor("#7B1FA2"), // Purple
                Color.parseColor("#FFD700"), // Gold
                Color.BLACK
            )
            ColorPurpose.NATURE -> listOf(
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#8BC34A"), // Light Green
                Color.parseColor("#795548")  // Brown
            )
            ColorPurpose.CREATIVITY -> listOf(
                Color.parseColor("#E91E63"), // Pink
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#FF9800")  // Orange
            )
        }
    }

    /**
     * Generate accessible color pair
     */
    fun generateAccessiblePair(background: Int): Pair<Int, Int> {
        val textColor = if (ColorUtils.calculateLuminance(background) > 0.5) {
            Color.BLACK
        } else {
            Color.WHITE
        }

        // Ensure minimum contrast ratio of 4.5:1 for WCAG AA
        val contrast = ColorUtils.calculateContrast(textColor, background)

        return if (contrast >= 4.5) {
            Pair(textColor, background)
        } else {
            // Adjust background to ensure contrast
            val adjustedBg = if (ColorUtils.calculateLuminance(background) > 0.5) {
                adjustLightness(background, 1.2f)
            } else {
                adjustLightness(background, 0.8f)
            }
            Pair(textColor, adjustedBg)
        }
    }

    /**
     * Create harmonious color scheme
     */
    fun createHarmoniousScheme(baseColor: Int, harmony: ColorHarmony): List<Int> {
        return when (harmony) {
            ColorHarmony.MONOCHROMATIC -> createMonochromaticScheme(baseColor)
            ColorHarmony.ANALOGOUS -> createAnalogousScheme(baseColor)
            ColorHarmony.COMPLEMENTARY -> createComplementaryScheme(baseColor)
            ColorHarmony.SPLIT_COMPLEMENTARY -> createSplitComplementaryScheme(baseColor)
            ColorHarmony.TRIADIC -> createTriadicScheme(baseColor)
            ColorHarmony.TETRADIC -> createTetradicScheme(baseColor)
        }
    }

    private fun createMonochromaticScheme(baseColor: Int): List<Int> {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)

        return listOf(
            baseColor,
            adjustLightness(baseColor, 1.3f),
            adjustLightness(baseColor, 0.7f),
            adjustSaturation(baseColor, 0.5f),
            adjustSaturation(baseColor, 1.3f)
        )
    }

    private fun createAnalogousScheme(baseColor: Int): List<Int> {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)

        return listOf(
            baseColor,
            Color.HSVToColor(floatArrayOf((hsv[0] + 30) % 360, hsv[1], hsv[2])),
            Color.HSVToColor(floatArrayOf((hsv[0] + 330) % 360, hsv[1], hsv[2])),
            Color.HSVToColor(floatArrayOf((hsv[0] + 60) % 360, hsv[1], hsv[2])),
            Color.HSVToColor(floatArrayOf((hsv[0] + 300) % 360, hsv[1], hsv[2]))
        )
    }

    private fun createComplementaryScheme(baseColor: Int): List<Int> {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)
        val complement = Color.HSVToColor(floatArrayOf((hsv[0] + 180) % 360, hsv[1], hsv[2]))

        return listOf(baseColor, complement, adjustLightness(baseColor, 0.7f), adjustLightness(complement, 0.7f))
    }

    private fun createSplitComplementaryScheme(baseColor: Int): List<Int> {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)

        return listOf(
            baseColor,
            Color.HSVToColor(floatArrayOf((hsv[0] + 150) % 360, hsv[1], hsv[2])),
            Color.HSVToColor(floatArrayOf((hsv[0] + 210) % 360, hsv[1], hsv[2]))
        )
    }

    private fun createTriadicScheme(baseColor: Int): List<Int> {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)

        return listOf(
            baseColor,
            Color.HSVToColor(floatArrayOf((hsv[0] + 120) % 360, hsv[1], hsv[2])),
            Color.HSVToColor(floatArrayOf((hsv[0] + 240) % 360, hsv[1], hsv[2]))
        )
    }

    private fun createTetradicScheme(baseColor: Int): List<Int> {
        val hsv = FloatArray(3)
        Color.colorToHSV(baseColor, hsv)

        return listOf(
            baseColor,
            Color.HSVToColor(floatArrayOf((hsv[0] + 90) % 360, hsv[1], hsv[2])),
            Color.HSVToColor(floatArrayOf((hsv[0] + 180) % 360, hsv[1], hsv[2])),
            Color.HSVToColor(floatArrayOf((hsv[0] + 270) % 360, hsv[1], hsv[2]))
        )
    }

    // ========== PRIVATE UTILITIES ==========

    private fun increaseVibrancy(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = (hsv[1] * factor).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * 1.1f).coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }

    private fun decreaseSaturation(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = (hsv[1] * factor).coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }

    private fun adjustLightnessAndSaturation(color: Int, lightnessFactor: Float, saturationFactor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[1] = (hsv[1] * saturationFactor).coerceIn(0f, 1f)
        hsv[2] = (hsv[2] * lightnessFactor).coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }

    private fun shiftTowardsPink(color: Int): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        // Shift hue towards pink (330-360 degrees)
        val currentHue = hsv[0]
        hsv[0] = if (currentHue < 180) 330f else (currentHue + 330) % 360
        hsv[1] = (hsv[1] * 0.8f).coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }

    private fun darkenAndShift(color: Int, lightnessFactor: Float, targetHue: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[0] = targetHue
        hsv[2] = (hsv[2] * lightnessFactor).coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }

    private fun adjustLightness(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
        return Color.HSVToColor(hsv)
    }

    private fun colorDistance(color1: Int, color2: Int): Float {
        val r1 = Color.red(color1).toFloat()
        val g1 = Color.green(color1).toFloat()
        val b1 = Color.blue(color1).toFloat()

        val r2 = Color.red(color2).toFloat()
        val g2 = Color.green(color2).toFloat()
        val b2 = Color.blue(color2).toFloat()

        return kotlin.math.sqrt(
            (r1 - r2) * (r1 - r2) +
            (g1 - g2) * (g1 - g2) +
            (b1 - b2) * (b1 - b2)
        )
    }
}

/**
 * Adjusted colors after mood modification
 */
data class AdjustedColors(
    val primary: Int,
    val secondary: Int,
    val tertiary: Int
)

/**
 * Color purposes for suggestions
 */
enum class ColorPurpose {
    TRUST,
    ENERGY,
    CALM,
    LUXURY,
    NATURE,
    CREATIVITY
}
