package com.sugarmunch.app.ai.neural

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.ThemeCategory
import com.sugarmunch.app.theme.model.ThemeColors
import com.sugarmunch.app.theme.model.GradientSpec
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.math.*

/**
 * Neural Theme Engine - AI-Powered Adaptive Theming
 * 
 * Features:
 * - Auto-generate themes from wallpaper
 * - Time-based theme transitions
 * - Weather-reactive colors
 * - Mood-based theme suggestions
 * - Theme DNA for sharing/mixing
 * - Text-to-theme generation
 */
class NeuralThemeEngine(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    private val wallpaperAnalyzer = WallpaperAnalyzer()
    private val moodDetector = MoodDetector(context)
    private val colorPsychology = ColorPsychology()
    private val weatherReactiveTheme = WeatherReactiveTheme(context)

    // Current state
    private val _currentWallpaperColors = MutableStateFlow<WallpaperColorPalette>(WallpaperColorPalette())
    val currentWallpaperColors: StateFlow<WallpaperColorPalette> = _currentWallpaperColors.asStateFlow()

    private val _currentTimeTheme = MutableStateFlow<CandyTheme?>(null)
    val currentTimeTheme: StateFlow<CandyTheme?> = _currentTimeTheme.asStateFlow()

    private val _currentWeatherTheme = MutableStateFlow<CandyTheme?>(null)
    val currentWeatherTheme: StateFlow<CandyTheme?> = _currentWeatherTheme.asStateFlow()

    private val _detectedMood = MutableStateFlow<UserMood>(UserMood.NEUTRAL)
    val detectedMood: StateFlow<UserMood> = _detectedMood.asStateFlow()

    private val _suggestedThemes = MutableStateFlow<List<ThemeSuggestion>>(emptyList())
    val suggestedThemes: StateFlow<List<ThemeSuggestion>> = _suggestedThemes.asStateFlow()

    private val _themeDNA = MutableStateFlow<ThemeDNA?>(null)
    val themeDNA: StateFlow<ThemeDNA?> = _themeDNA.asStateFlow()

    private val _isGeneratingTheme = MutableStateFlow(false)
    val isGeneratingTheme: StateFlow<Boolean> = _isGeneratingTheme.asStateFlow()

    private val _generationProgress = MutableStateFlow(0f)
    val generationProgress: StateFlow<Float> = _generationProgress.asStateFlow()

    // Theme history for learning
    private val themeHistory = mutableListOf<ThemeHistoryEntry>()
    private val themePreferences = mutableMapOf<ThemeCategory, Float>()

    init {
        startTimeBasedThemeGeneration()
        startWeatherReactiveTheme()
        startMoodDetection()
    }

    // ========== WALLPAPER ANALYSIS ==========

    /**
     * Analyze wallpaper and generate theme
     */
    suspend fun analyzeWallpaper(bitmap: Bitmap): ThemeGenerationResult = withContext(Dispatchers.Default) {
        _isGeneratingTheme.value = true
        _generationProgress.value = 0f

        try {
            // Step 1: Extract colors
            _generationProgress.value = 0.2f
            val colorPalette = wallpaperAnalyzer.extractColors(bitmap)
            _currentWallpaperColors.value = colorPalette

            // Step 2: Analyze mood/style
            _generationProgress.value = 0.4f
            val wallpaperMood = wallpaperAnalyzer.analyzeMood(colorPalette)

            // Step 3: Generate theme
            _generationProgress.value = 0.6f
            val theme = generateThemeFromColors(
                colors = colorPalette,
                mood = wallpaperMood,
                category = suggestCategoryFromColors(colorPalette)
            )

            // Step 4: Generate Theme DNA
            _generationProgress.value = 0.8f
            val dna = ThemeDNA.fromTheme(theme, colorPalette)
            _themeDNA.value = dna

            // Step 5: Generate suggestions
            _generationProgress.value = 0.9f
            generateThemeSuggestions(colorPalette, wallpaperMood)

            _generationProgress.value = 1f
            delay(500)

            ThemeGenerationResult(
                success = true,
                theme = theme,
                colorPalette = colorPalette,
                themeDNA = dna,
                mood = wallpaperMood
            )
        } catch (e: Exception) {
            ThemeGenerationResult(
                success = false,
                error = e.message ?: "Unknown error"
            )
        } finally {
            _isGeneratingTheme.value = false
        }
    }

    /**
     * Generate theme from color palette
     */
    private fun generateThemeFromColors(
        colors: WallpaperColorPalette,
        mood: WallpaperMood,
        category: ThemeCategory
    ): CandyTheme {
        val primaryColor = colors.dominantColors.firstOrNull() ?: Color.BLACK
        val secondaryColor = colors.accentColors.firstOrNull() ?: primaryColor
        val tertiaryColor = colors.vibrantColors.firstOrNull() ?: secondaryColor

        // Apply color psychology adjustments
        val adjustedColors = colorPsychology.adjustColorsForMood(
            primary = primaryColor,
            secondary = secondaryColor,
            tertiary = tertiaryColor,
            mood = mood
        )

        // Generate gradient specs
        val backgroundGradient = GradientSpec(
            colors = listOf(
                adjustedColors.primary,
                adjustedColors.secondary,
                adjustedColors.tertiary
            ).map { ColorUtils.colorToHex(it) },
            startOffset = listOf(0f, 0.5f, 1f),
            direction = GradientDirection.VERTICAL
        )

        return CandyTheme(
            id = "neural_${System.currentTimeMillis()}",
            name = generateThemeName(mood, category),
            description = "AI-generated from your wallpaper",
            category = category,
            isDark = isDarkTheme(adjustedColors.primary),
            colors = ThemeColors(
                primary = adjustedColors.primary,
                onPrimary = getContrastingColor(adjustedColors.primary),
                secondary = adjustedColors.secondary,
                onSecondary = getContrastingColor(adjustedColors.secondary),
                tertiary = adjustedColors.tertiary,
                onTertiary = getContrastingColor(adjustedColors.tertiary),
                background = adjustedColors.primary,
                onBackground = getContrastingColor(adjustedColors.primary),
                surface = ColorUtils.setAlphaComponent(adjustedColors.secondary, 128),
                onSurface = getContrastingColor(adjustedColors.secondary),
                error = Color.RED,
                onError = Color.WHITE
            ),
            themeGradient = backgroundGradient,
            particleConfig = ParticleConfig(
                count = when (mood) {
                    WallpaperMood.ENERGETIC -> 50..100
                    WallpaperMood.CALM -> 10..30
                    WallpaperMood.MELANCHOLIC -> 20..50
                    else -> 30..60
                },
                speed = FloatRange(
                    min = when (mood) {
                        WallpaperMood.ENERGETIC -> 2f
                        WallpaperMood.CALM -> 0.5f
                        else -> 1f
                    },
                    max = when (mood) {
                        WallpaperMood.ENERGETIC -> 5f
                        WallpaperMood.CALM -> 1.5f
                        else -> 3f
                    }
                ),
                colors = colors.accentColors.map { ColorUtils.colorToHex(it) }
            )
        )
    }

    // ========== TIME-BASED THEMING ==========

    private fun startTimeBasedThemeGeneration() {
        scope.launch {
            while (isActive) {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val timeTheme = generateTimeBasedTheme(hour)
                _currentTimeTheme.value = timeTheme
                delay(60000) // Update every minute
            }
        }
    }

    /**
     * Generate theme based on time of day
     */
    fun generateTimeBasedTheme(hour: Int): CandyTheme {
        return when (hour) {
            in 5..8 -> createSunriseTheme()
            in 9..11 -> createMorningTheme()
            in 12..14 -> createNoonTheme()
            in 15..17 -> createAfternoonTheme()
            in 18..20 -> createSunsetTheme()
            in 21..22 -> createEveningTheme()
            else -> createNightTheme()
        }
    }

    private fun createSunriseTheme(): CandyTheme {
        return CandyTheme(
            id = "time_sunrise",
            name = "Sunrise Serenity",
            description = "Warm dawn colors to start your day",
            category = ThemeCategory.NATURE,
            isDark = false,
            colors = ThemeColors(
                primary = Color(0xFFFFD7A0),
                onPrimary = Color(0xFF3D2914),
                secondary = Color(0xFFFFAB76),
                onSecondary = Color(0xFF3D2914),
                tertiary = Color(0xFFFF8C69),
                onTertiary = Color.WHITE,
                background = Color(0xFFFFF5E6),
                onBackground = Color(0xFF3D2914),
                surface = Color(0xFFFFE4C4),
                onSurface = Color(0xFF3D2914),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            themeGradient = GradientSpec(
                colors = listOf("#FFF5E6", "#FFD7A0", "#FFAB76"),
                startOffset = listOf(0f, 0.5f, 1f),
                direction = GradientDirection.VERTICAL
            )
        )
    }

    private fun createNightTheme(): CandyTheme {
        return CandyTheme(
            id = "time_night",
            name = "Midnight Dreams",
            description = "Deep night colors for late hours",
            category = ThemeCategory.DARK,
            isDark = true,
            colors = ThemeColors(
                primary = Color(0xFF1A1A2E),
                onPrimary = Color(0xFFEAEAEA),
                secondary = Color(0xFF16213E),
                onSecondary = Color(0xFFEAEAEA),
                tertiary = Color(0xFF0F3460),
                onTertiary = Color(0xFFEAEAEA),
                background = Color(0xFF0D0D1A),
                onBackground = Color(0xFFEAEAEA),
                surface = Color(0xFF1F1F3D),
                onSurface = Color(0xFFEAEAEA),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            themeGradient = GradientSpec(
                colors = listOf("#0D0D1A", "#1A1A2E", "#16213E"),
                startOffset = listOf(0f, 0.5f, 1f),
                direction = GradientDirection.VERTICAL
            ),
            particleConfig = ParticleConfig(
                count = 20..40,
                speed = FloatRange(0.3f, 0.8f),
                colors = listOf("#EAEAEA", "#FFD700", "#C0C0C0")
            )
        )
    }

    // ========== WEATHER REACTIVE THEMING ==========

    private fun startWeatherReactiveTheme() {
        scope.launch {
            weatherReactiveTheme.weatherData.collect { weather ->
                val weatherTheme = weatherReactiveTheme.generateThemeForWeather(weather)
                _currentWeatherTheme.value = weatherTheme
            }
        }
    }

    // ========== MOOD DETECTION ==========

    private fun startMoodDetection() {
        scope.launch {
            moodDetector.moodFlow.collect { mood ->
                _detectedMood.value = mood
                generateMoodBasedSuggestions(mood)
            }
        }
    }

    /**
     * Manually set mood for theme suggestions
     */
    fun setMood(mood: UserMood) {
        _detectedMood.value = mood
        generateMoodBasedSuggestions(mood)
    }

    private fun generateMoodBasedSuggestions(mood: UserMood) {
        val suggestions = when (mood) {
            UserMood.HAPPY -> listOf(
                ThemeSuggestion("Energetic Sunrise", ThemeCategory.SUGARRUSH, "Bright and uplifting"),
                ThemeSuggestion("Joyful Colors", ThemeCategory.COLORFUL, "Vibrant and cheerful"),
                ThemeSuggestion("Golden Hour", ThemeCategory.NATURE, "Warm and happy")
            )
            UserMood.CALM -> listOf(
                ThemeSuggestion("Zen Garden", ThemeCategory.NATURE, "Peaceful and serene"),
                ThemeSuggestion("Ocean Breeze", ThemeCategory.NATURE, "Cool and calming"),
                ThemeSuggestion("Lavender Dreams", ThemeCategory.CHILL, "Soft and relaxing")
            )
            UserMood.FOCUSED -> listOf(
                ThemeSuggestion("Deep Work", ThemeCategory.MINIMAL, "Distraction-free"),
                ThemeSuggestion("Monochrome", ThemeCategory.MINIMAL, "Clean and focused"),
                ThemeSuggestion("Forest Study", ThemeCategory.NATURE, "Natural concentration")
            )
            UserMood.ENERGETIC -> listOf(
                ThemeSuggestion("Neon Rush", ThemeCategory.SUGARRUSH, "High energy"),
                ThemeSuggestion("Electric Blue", ThemeCategory.SUGARRUSH, "Powerful and bold"),
                ThemeSuggestion("Fire Storm", ThemeCategory.SUGARRUSH, "Intense and dynamic")
            )
            UserMood.MELANCHOLIC -> listOf(
                ThemeSuggestion("Rainy Day", ThemeCategory.CHILL, "Comforting and soft"),
                ThemeSuggestion("Moonlight", ThemeCategory.DARK, "Gentle darkness"),
                ThemeSuggestion("Autumn Leaves", ThemeCategory.NATURE, "Warm nostalgia")
            )
            UserMood.NEUTRAL -> listOf(
                ThemeSuggestion("Balanced Harmony", ThemeCategory.MINIMAL, "Perfect equilibrium"),
                ThemeSuggestion("Classic Elegance", ThemeCategory.MINIMAL, "Timeless beauty"),
                ThemeSuggestion("Modern Simplicity", ThemeCategory.MINIMAL, "Clean aesthetics")
            )
        }
        _suggestedThemes.value = suggestions
    }

    // ========== THEME DNA ==========

    /**
     * Mix two theme DNAs to create a new theme
     */
    fun mixThemeDNA(dna1: ThemeDNA, dna2: ThemeDNA, mixRatio: Float = 0.5f): ThemeDNA {
        return ThemeDNA(
            id = "mix_${System.currentTimeMillis()}",
            colorGenes = mixColorGenes(dna1.colorGenes, dna2.colorGenes, mixRatio),
            styleGenes = mixStyleGenes(dna1.styleGenes, dna2.styleGenes, mixRatio),
            particleGenes = mixParticleGenes(dna1.particleGenes, dna2.particleGenes, mixRatio),
            gradientGenes = mixGradientGenes(dna1.gradientGenes, dna2.gradientGenes, mixRatio),
            metadata = ThemeDNAMetadata(
                name = "${dna1.metadata.name} × ${dna2.metadata.name}",
                createdAt = System.currentTimeMillis(),
                parentDNA = listOf(dna1.id, dna2.id),
                generation = max(dna1.metadata.generation, dna2.metadata.generation) + 1
            )
        )
    }

    private fun mixColorGenes(genes1: List<ColorGene>, genes2: List<ColorGene>, ratio: Float): List<ColorGene> {
        return genes1.zip(genes2).map { (g1, g2) ->
            ColorGene(
                hue = lerp(g1.hue, g2.hue, ratio),
                saturation = lerp(g1.saturation, g2.saturation, ratio),
                lightness = lerp(g1.lightness, g2.lightness, ratio),
                weight = lerp(g1.weight, g2.weight, ratio)
            )
        }
    }

    private fun mixStyleGenes(genes1: List<StyleGene>, genes2: List<StyleGene>, ratio: Float): List<StyleGene> {
        return genes1.zip(genes2).map { (g1, g2) ->
            StyleGene(
                type = if (ratio > 0.5f) g1.type else g2.type,
                intensity = lerp(g1.intensity, g2.intensity, ratio),
                variation = lerp(g1.variation, g2.variation, ratio)
            )
        }
    }

    private fun mixParticleGenes(genes1: List<ParticleGene>, genes2: List<ParticleGene>, ratio: Float): List<ParticleGene> {
        return genes1.zip(genes2).map { (g1, g2) ->
            ParticleGene(
                shape = if (ratio > 0.5f) g1.shape else g2.shape,
                size = lerp(g1.size, g2.size, ratio),
                speed = lerp(g1.speed, g2.speed, ratio),
                density = lerp(g1.density, g2.density, ratio)
            )
        }
    }

    private fun mixGradientGenes(genes1: List<GradientGene>, genes2: List<GradientGene>, ratio: Float): List<GradientGene> {
        return genes1.zip(genes2).map { (g1, g2) ->
            GradientGene(
                angle = lerp(g1.angle, g2.angle, ratio),
                colorStops = g1.colorStops.zip(g2.colorStops).map { (c1, c2) ->
                    lerpColor(c1, c2, ratio)
                },
                animationSpeed = lerp(g1.animationSpeed, g2.animationSpeed, ratio)
            )
        }
    }

    /**
     * Export theme DNA as shareable string
     */
    fun exportThemeDNA(dna: ThemeDNA): String {
        return dna.toBase64()
    }

    /**
     * Import theme DNA from string
     */
    fun importThemeDNA(base64: String): ThemeDNA? {
        return try {
            ThemeDNA.fromBase64(base64)
        } catch (e: Exception) {
            null
        }
    }

    // ========== TEXT-TO-THEME ==========

    /**
     * Generate theme from text description
     */
    suspend fun generateThemeFromText(description: String): ThemeGenerationResult = withContext(Dispatchers.Default) {
        _isGeneratingTheme.value = true
        _generationProgress.value = 0f

        try {
            // Parse text for keywords
            val keywords = parseTextDescription(description)
            _generationProgress.value = 0.3f

            // Determine category from keywords
            val category = determineCategoryFromKeywords(keywords)
            _generationProgress.value = 0.5f

            // Generate colors from keywords
            val colors = generateColorsFromKeywords(keywords)
            _generationProgress.value = 0.7f

            // Create theme
            val theme = generateThemeFromColors(
                colors = colors,
                mood = keywordsToMood(keywords),
                category = category
            )
            _generationProgress.value = 0.9f

            // Generate DNA
            val dna = ThemeDNA.fromTheme(theme, colors)
            _themeDNA.value = dna

            _generationProgress.value = 1f
            _isGeneratingTheme.value = false

            ThemeGenerationResult(
                success = true,
                theme = theme,
                colorPalette = colors,
                themeDNA = dna,
                mood = keywordsToMood(keywords)
            )
        } catch (e: Exception) {
            _isGeneratingTheme.value = false
            ThemeGenerationResult(
                success = false,
                error = e.message ?: "Failed to generate theme"
            )
        }
    }

    private fun parseTextDescription(description: String): List<String> {
        val commonWords = setOf("a", "an", "the", "with", "and", "or", "for", "in", "on", "at", "to", "of")
        return description
            .lowercase()
            .split(Regex("[\\s,]+"))
            .filter { it.length > 2 && it !in commonWords }
    }

    private fun determineCategoryFromKeywords(keywords: List<String>): ThemeCategory {
        val natureWords = setOf("nature", "forest", "ocean", "sky", "mountain", "river", "flower", "tree", "green", "blue")
        val darkWords = setOf("dark", "night", "black", "shadow", "midnight", "gothic", "vampire")
        val colorfulWords = setOf("colorful", "rainbow", "bright", "vibrant", "neon", "electric")
        val minimalWords = setOf("minimal", "simple", "clean", "white", "plain", "basic")
        val sugarrushWords = setOf("energy", "rush", "extreme", "intense", "power", "fire", "explosion")

        return when {
            keywords.any { it in natureWords } -> ThemeCategory.NATURE
            keywords.any { it in darkWords } -> ThemeCategory.DARK
            keywords.any { it in colorfulWords } -> ThemeCategory.COLORFUL
            keywords.any { it in sugarrushWords } -> ThemeCategory.SUGARRUSH
            keywords.any { it in minimalWords } -> ThemeCategory.MINIMAL
            else -> ThemeCategory.CUSTOM
        }
    }

    private fun generateColorsFromKeywords(keywords: List<String>): WallpaperColorPalette {
        val colorMap = mapOf(
            "red" to Color.RED,
            "blue" to Color.BLUE,
            "green" to Color.GREEN,
            "yellow" to Color.YELLOW,
            "purple" to Color(0xFF800080),
            "orange" to Color(0xFFFFA500),
            "pink" to Color(0xFFFFC0CB),
            "cyan" to Color(0xFF00FFFF),
            "magenta" to Color(0xFFFF00FF),
            "gold" to Color(0xFFFFD700),
            "silver" to Color(0xFFC0C0C0),
            "bronze" to Color(0xFFCD7F32),
            "white" to Color.WHITE,
            "black" to Color.BLACK,
            "gray" to Color.GRAY,
            "neon" to Color(0xFF39FF14),
            "lavender" to Color(0xFFE6E6FA),
            "mint" to Color(0xFF98FF98),
            "peach" to Color(0xFFFFDAB9),
            "coral" to Color(0xFFFF7F50)
        )

        val foundColors = keywords.mapNotNull { colorMap[it] }

        return if (foundColors.isNotEmpty()) {
            WallpaperColorPalette(
                dominantColors = foundColors.take(3),
                accentColors = foundColors.drop(3).take(3),
                vibrantColors = foundColors.filter { isVibrant(it) },
                mutedColors = foundColors.filter { !isVibrant(it) }
            )
        } else {
            WallpaperColorPalette()
        }
    }

    private fun keywordsToMood(keywords: List<String>): WallpaperMood {
        val energeticWords = setOf("energy", "power", "fast", "quick", "dynamic", "intense")
        val calmWords = setOf("calm", "peaceful", "quiet", "gentle", "soft", "relax")
        val melancholicWords = setOf("sad", "melancholy", "gloomy", "somber", "mournful")

        return when {
            keywords.any { it in energeticWords } -> WallpaperMood.ENERGETIC
            keywords.any { it in calmWords } -> WallpaperMood.CALM
            keywords.any { it in melancholicWords } -> WallpaperMood.MELANCHOLIC
            else -> WallpaperMood.NEUTRAL
        }
    }

    // ========== UTILITIES ==========

    private fun generateThemeName(mood: WallpaperMood, category: ThemeCategory): String {
        val adjectives = mapOf(
            WallpaperMood.ENERGETIC to listOf("Dynamic", "Electric", "Vibrant", "Powerful"),
            WallpaperMood.CALM to listOf("Serene", "Peaceful", "Gentle", "Tranquil"),
            WallpaperMood.MELANCHOLIC to listOf("Dreamy", "Ethereal", "Soft", "Subtle"),
            WallpaperMood.NEUTRAL to listOf("Balanced", "Harmonious", "Classic", "Timeless")
        )

        val nouns = mapOf(
            ThemeCategory.NATURE to listOf("Garden", "Forest", "Ocean", "Sky"),
            ThemeCategory.DARK to listOf("Night", "Shadow", "Midnight", "Abyss"),
            ThemeCategory.COLORFUL to listOf("Spectrum", "Prism", "Kaleidoscope", "Rainbow"),
            ThemeCategory.SUGARRUSH to listOf("Rush", "Explosion", "Storm", "Surge"),
            ThemeCategory.MINIMAL to listOf("Essence", "Purity", "Zen", "Void"),
            ThemeCategory.CUSTOM to listOf("Creation", "Vision", "Dream", "Fantasy")
        )

        val adj = adjectives[mood]?.random() ?: "Custom"
        val noun = nouns[category]?.random() ?: "Theme"

        return "$adj $noun"
    }

    private fun suggestCategoryFromColors(colors: WallpaperColorPalette): ThemeCategory {
        val avgBrightness = colors.dominantColors.map { getBrightness(it) }.average()
        val avgSaturation = colors.dominantColors.map { getSaturation(it) }.average()

        return when {
            avgBrightness < 0.3 -> ThemeCategory.DARK
            avgSaturation > 0.7 -> ThemeCategory.SUGARRUSH
            avgSaturation < 0.3 -> ThemeCategory.MINIMAL
            avgBrightness > 0.7 -> ThemeCategory.NATURE
            else -> ThemeCategory.CUSTOM
        }
    }

    private fun isDarkTheme(color: Int): Boolean {
        return getBrightness(color) < 0.5
    }

    private fun getBrightness(color: Int): Double {
        return ColorUtils.calculateLuminance(color)
    }

    private fun getSaturation(color: Int): Float {
        val hsv = FloatArray(3)
        Color.colorToHSV(color, hsv)
        return hsv[1]
    }

    private fun getContrastingColor(color: Int): Int {
        return if (getBrightness(color) > 0.5) Color.BLACK else Color.WHITE
    }

    private fun isVibrant(color: Int): Boolean {
        return getSaturation(color) > 0.5
    }

    private fun lerp(a: Float, b: Float, t: Float): Float {
        return a + (b - a) * t.coerceIn(0f, 1f)
    }

    private fun lerpColor(color1: Int, color2: Int, t: Float): String {
        val r1 = Color.red(color1)
        val g1 = Color.green(color1)
        val b1 = Color.blue(color1)

        val r2 = Color.red(color2)
        val g2 = Color.green(color2)
        val b2 = Color.blue(color2)

        val r = (r1 + (r2 - r1) * t).toInt().coerceIn(0, 255)
        val g = (g1 + (g2 - g1) * t).toInt().coerceIn(0, 255)
        val b = (b1 + (b2 - b1) * t).toInt().coerceIn(0, 255)

        return String.format("#%02X%02X%02X", r, g, b)
    }

    private fun generateThemeSuggestions(colors: WallpaperColorPalette, mood: WallpaperMood) {
        val suggestions = mutableListOf<ThemeSuggestion>()

        // Suggest based on dominant color
        colors.dominantColors.firstOrNull()?.let { dominant ->
            val hsv = FloatArray(3)
            Color.colorToHSV(dominant, hsv)
            val hue = hsv[0]

            when {
                hue < 30 -> suggestions.add(ThemeSuggestion("Warm Sunset", ThemeCategory.NATURE, "Orange and red tones"))
                hue < 90 -> suggestions.add(ThemeSuggestion("Spring Garden", ThemeCategory.NATURE, "Fresh green tones"))
                hue < 150 -> suggestions.add(ThemeSuggestion("Ocean Deep", ThemeCategory.NATURE, "Cyan and blue tones"))
                hue < 210 -> suggestions.add(ThemeSuggestion("Royal Purple", ThemeCategory.DARK, "Deep purple tones"))
                hue < 270 -> suggestions.add(ThemeSuggestion("Mystic Violet", ThemeCategory.DARK, "Violet mystery"))
                else -> suggestions.add(ThemeSuggestion("Rose Garden", ThemeCategory.COLORFUL, "Pink and magenta"))
            }
        }

        // Suggest based on mood
        when (mood) {
            WallpaperMood.ENERGETIC -> suggestions.add(ThemeSuggestion("Energy Boost", ThemeCategory.SUGARRUSH, "High octane colors"))
            WallpaperMood.CALM -> suggestions.add(ThemeSuggestion("Zen Mode", ThemeCategory.MINIMAL, "Peaceful palette"))
            WallpaperMood.MELANCHOLIC -> suggestions.add(ThemeSuggestion("Comfort Zone", ThemeCategory.CHILL, "Soothing tones"))
            else -> suggestions.add(ThemeSuggestion("Balanced Life", ThemeCategory.MINIMAL, "Harmonious blend"))
        }

        _suggestedThemes.value = suggestions.distinctBy { it.name }
    }

    /**
     * Record theme selection for learning
     */
    fun recordThemeSelection(theme: CandyTheme, context: ThemeSelectionContext) {
        themeHistory.add(
            ThemeHistoryEntry(
                themeId = theme.id,
                category = theme.category,
                timestamp = System.currentTimeMillis(),
                context = context
            )
        )

        // Update preferences
        val currentPref = themePreferences[theme.category] ?: 0.5f
        themePreferences[theme.category] = (currentPref + 0.1f).coerceIn(0f, 1f)

        // Keep history manageable
        if (themeHistory.size > 1000) {
            themeHistory.removeAt(0)
        }
    }

    companion object {
        @Volatile
        private var instance: NeuralThemeEngine? = null

        fun getInstance(context: Context): NeuralThemeEngine {
            return instance ?: synchronized(this) {
                instance ?: NeuralThemeEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Result of theme generation
 */
data class ThemeGenerationResult(
    val success: Boolean,
    val theme: CandyTheme? = null,
    val colorPalette: WallpaperColorPalette? = null,
    val themeDNA: ThemeDNA? = null,
    val mood: WallpaperMood? = null,
    val error: String? = null
)

/**
 * Theme suggestion for user
 */
data class ThemeSuggestion(
    val name: String,
    val category: ThemeCategory,
    val description: String
)

/**
 * User mood states
 */
enum class UserMood {
    HAPPY,
    CALM,
    FOCUSED,
    ENERGETIC,
    MELANCHOLIC,
    NEUTRAL
}

/**
 * Theme selection context for learning
 */
enum class ThemeSelectionContext {
    MANUAL_SELECT,
    AUTO_GENERATED,
    TIME_BASED,
    WEATHER_BASED,
    MOOD_BASED,
    TEXT_GENERATED,
    DNA_MIXED
}
