package com.sugarmunch.app.ai.neural

import android.content.Context
import android.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.theme.model.ThemeCategory
import com.sugarmunch.app.theme.model.ThemeColors
import com.sugarmunch.app.theme.model.GradientSpec
import com.sugarmunch.app.theme.model.GradientDirection
import com.sugarmunch.app.theme.model.ParticleConfig
import com.sugarmunch.app.theme.model.FloatRange
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.serialization.json.*

private val Context.weatherDataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_preferences")

/**
 * Weather data model
 */
data class WeatherData(
    val temperature: Float,
    val condition: WeatherCondition,
    val humidity: Float,
    val windSpeed: Float,
    val isDaytime: Boolean,
    val location: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Weather conditions
 */
enum class WeatherCondition {
    SUNNY,
    CLEAR,
    PARTLY_CLOUDY,
    CLOUDY,
    OVERCAST,
    RAIN,
    HEAVY_RAIN,
    STORM,
    SNOW,
    HEAVY_SNOW,
    FOG,
    MIST,
    WINDY,
    EXTREME
}

/**
 * Weather Reactive Theme - Generates themes based on weather conditions
 */
class WeatherReactiveTheme(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _weatherData = MutableStateFlow<WeatherData?>(null)
    val weatherData: StateFlow<WeatherData?> = _weatherData.asStateFlow()

    private val _currentWeatherTheme = MutableStateFlow<CandyTheme?>(null)
    val currentWeatherTheme: StateFlow<CandyTheme?> = _currentWeatherTheme.asStateFlow()

    private var apiKey: String? = null
    private var cityId: String? = null

    init {
        loadWeatherPreferences()
    }

    /**
     * Set location for weather updates
     */
    fun setLocation(cityId: String, apiKey: String) {
        this.cityId = cityId
        this.apiKey = apiKey
        fetchWeather()
    }

    /**
     * Manually set weather data
     */
    fun setWeatherData(weather: WeatherData) {
        _weatherData.value = weather
        val theme = generateThemeForWeather(weather)
        _currentWeatherTheme.value = theme
    }

    /**
     * Fetch weather from API
     */
    private fun fetchWeather() {
        if (apiKey == null || cityId == null) return

        scope.launch {
            try {
                val weather = fetchWeatherFromAPI()
                _weatherData.value = weather
                _currentWeatherTheme.value = generateThemeForWeather(weather)

                // Save to preferences
                context.weatherDataStore.edit { prefs ->
                    prefs[stringPreferencesKey("last_condition")] = weather.condition.name
                }
            } catch (e: Exception) {
                // Use default weather
                val defaultWeather = WeatherData(
                    temperature = 20f,
                    condition = WeatherCondition.PARTLY_CLOUDY,
                    humidity = 50f,
                    windSpeed = 10f,
                    isDaytime = true,
                    location = "Unknown"
                )
                _weatherData.value = defaultWeather
            }
        }
    }

    /**
     * Generate theme for weather condition
     */
    fun generateThemeForWeather(weather: WeatherData): CandyTheme {
        val (colors, category, name, description) = when (weather.condition) {
            WeatherCondition.SUNNY, WeatherCondition.CLEAR -> {
                if (weather.isDaytime) {
                    createSunnyDayTheme(weather)
                } else {
                    createClearNightTheme(weather)
                }
            }
            WeatherCondition.PARTLY_CLOUDY -> createPartlyCloudyTheme(weather)
            WeatherCondition.CLOUDY, WeatherCondition.OVERCAST -> createCloudyTheme(weather)
            WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> createRainyTheme(weather)
            WeatherCondition.STORM -> createStormyTheme(weather)
            WeatherCondition.SNOW, WeatherCondition.HEAVY_SNOW -> createSnowyTheme(weather)
            WeatherCondition.FOG, WeatherCondition.MIST -> createFoggyTheme(weather)
            WeatherCondition.WINDY -> createWindyTheme(weather)
            WeatherCondition.EXTREME -> createExtremeWeatherTheme(weather)
        }

        return CandyTheme(
            id = "weather_${weather.condition.name.lowercase()}_${System.currentTimeMillis()}",
            name = name,
            description = description,
            category = category,
            isDark = !weather.isDaytime || weather.condition in listOf(
                WeatherCondition.STORM,
                WeatherCondition.OVERCAST,
                WeatherCondition.HEAVY_RAIN
            ),
            colors = colors,
            themeGradient = GradientSpec(
                colors = listOf(
                    ColorUtils.colorToHex(colors.primary),
                    ColorUtils.colorToHex(colors.secondary),
                    ColorUtils.colorToHex(colors.tertiary)
                ),
                startOffset = listOf(0f, 0.5f, 1f),
                direction = if (weather.isDaytime) GradientDirection.VERTICAL else GradientDirection.DIAGONAL_TOP_LEFT
            ),
            particleConfig = createParticleConfigForWeather(weather)
        )
    }

    private fun createSunnyDayTheme(weather: WeatherData): ThemeData {
        val warmth = ((weather.temperature - 15) / 20).coerceIn(0f, 1f)

        return ThemeData(
            colors = ThemeColors(
                primary = lerpColor(Color(0xFFFFD54F), Color(0xFFFF8F00), warmth),
                onPrimary = Color(0xFF3D2914),
                secondary = lerpColor(Color(0xFFFFECB3), Color(0xFFFFD54F), warmth),
                onSecondary = Color(0xFF3D2914),
                tertiary = Color(0xFF4FC3F7),
                onTertiary = Color(0xFF0D47A1),
                background = Color(0xFFFFFBF0),
                onBackground = Color(0xFF3D2914),
                surface = Color(0xFFFFF8E1),
                onSurface = Color(0xFF3D2914),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            category = ThemeCategory.NATURE,
            name = "Sunny Day",
            description = "Bright and warm like a sunny day"
        )
    }

    private fun createClearNightTheme(weather: WeatherData): ThemeData {
        return ThemeData(
            colors = ThemeColors(
                primary = Color(0xFF1A237E),
                onPrimary = Color(0xFFE8EAF6),
                secondary = Color(0xFF283593),
                onSecondary = Color(0xFFE8EAF6),
                tertiary = Color(0xFF3949AB),
                onTertiary = Color(0xFFE8EAF6),
                background = Color(0xFF0D142E),
                onBackground = Color(0xFFE8EAF6),
                surface = Color(0xFF1A237E),
                onSurface = Color(0xFFE8EAF6),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            category = ThemeCategory.DARK,
            name = "Clear Night",
            description = "Deep night sky with stars"
        )
    }

    private fun createPartlyCloudyTheme(weather: WeatherData): ThemeData {
        return ThemeData(
            colors = ThemeColors(
                primary = Color(0xFF64B5F6),
                onPrimary = Color.WHITE,
                secondary = Color(0xFF90CAF9),
                onSecondary = Color(0xFF0D47A1),
                tertiary = Color(0xFFECEFF1),
                onTertiary = Color(0xFF37474F),
                background = Color(0xFFE3F2FD),
                onBackground = Color(0xFF0D47A1),
                surface = Color(0xFFBBDEFB),
                onSurface = Color(0xFF0D47A1),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            category = ThemeCategory.NATURE,
            name = "Partly Cloudy",
            description = "Blue skies with fluffy clouds"
        )
    }

    private fun createCloudyTheme(weather: WeatherData): ThemeData {
        return ThemeData(
            colors = ThemeColors(
                primary = Color(0xFF90A4AE),
                onPrimary = Color(0xFF263238),
                secondary = Color(0xFFB0BEC5),
                onSecondary = Color(0xFF263238),
                tertiary = Color(0xFFCFD8DC),
                onTertiary = Color(0xFF263238),
                background = Color(0xFFECEFF1),
                onBackground = Color(0xFF263238),
                surface = Color(0xFFCFD8DC),
                onSurface = Color(0xFF263238),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            category = ThemeCategory.MINIMAL,
            name = "Cloudy Day",
            description = "Soft gray clouds"
        )
    }

    private fun createRainyTheme(weather: WeatherData): ThemeData {
        return ThemeData(
            colors = ThemeColors(
                primary = Color(0xFF546E7A),
                onPrimary = Color.WHITE,
                secondary = Color(0xFF78909C),
                onSecondary = Color(0xFF263238),
                tertiary = Color(0xFF90A4AE),
                onTertiary = Color(0xFF263238),
                background = Color(0xFFECEFF1),
                onBackground = Color(0xFF37474F),
                surface = Color(0xFFCFD8DC),
                onSurface = Color(0xFF37474F),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            category = ThemeCategory.CHILL,
            name = "Rainy Day",
            description = "Calming rain atmosphere"
        )
    }

    private fun createStormyTheme(weather: WeatherData): ThemeData {
        return ThemeData(
            colors = ThemeColors(
                primary = Color(0xFF263238),
                onPrimary = Color(0xFFECEFF1),
                secondary = Color(0xFF37474F),
                onSecondary = Color(0xFFECEFF1),
                tertiary = Color(0xFF546E7A),
                onTertiary = Color(0xFFECEFF1),
                background = Color(0xFF1A237E),
                onBackground = Color(0xFFE8EAF6),
                surface = Color(0xFF283593),
                onSurface = Color(0xFFE8EAF6),
                error = Color(0xFFFFEB3B),
                onError = Color(0xFF000000)
            ),
            category = ThemeCategory.DARK,
            name = "Thunderstorm",
            description = "Dark and dramatic storm"
        )
    }

    private fun createSnowyTheme(weather: WeatherData): ThemeData {
        return ThemeData(
            colors = ThemeColors(
                primary = Color(0xFFE3F2FD),
                onPrimary = Color(0xFF0D47A1),
                secondary = Color(0xFFBBDEFB),
                onSecondary = Color(0xFF0D47A1),
                tertiary = Color(0xFF90CAF9),
                onTertiary = Color(0xFF0D47A1),
                background = Color(0xFFF5F5F5),
                onBackground = Color(0xFF37474F),
                surface = Color(0xFFEEEEEE),
                onSurface = Color(0xFF37474F),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            category = ThemeCategory.NATURE,
            name = "Snowy Day",
            description = "Pure white snow"
        )
    }

    private fun createFoggyTheme(weather: WeatherData): ThemeData {
        return ThemeData(
            colors = ThemeColors(
                primary = Color(0xFFBDBDBD),
                onPrimary = Color(0xFF212121),
                secondary = Color(0xFFE0E0E0),
                onSecondary = Color(0xFF212121),
                tertiary = Color(0xFFF5F5F5),
                onTertiary = Color(0xFF212121),
                background = Color(0xFFEEEEEE),
                onBackground = Color(0xFF212121),
                surface = Color(0xFFE0E0E0),
                onSurface = Color(0xFF212121),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            category = ThemeCategory.MINIMAL,
            name = "Foggy Morning",
            description = "Misty and mysterious"
        )
    }

    private fun createWindyTheme(weather: WeatherData): ThemeData {
        return ThemeData(
            colors = ThemeColors(
                primary = Color(0xFF80CBC4),
                onPrimary = Color(0xFF004D40),
                secondary = Color(0xFFA5D6A7),
                onSecondary = Color(0xFF004D40),
                tertiary = Color(0xFFC8E6C9),
                onTertiary = Color(0xFF004D40),
                background = Color(0xFFE8F5E9),
                onBackground = Color(0xFF1B5E20),
                surface = Color(0xFFC8E6C9),
                onSurface = Color(0xFF1B5E20),
                error = Color(0xFFE57373),
                onError = Color.WHITE
            ),
            category = ThemeCategory.NATURE,
            name = "Windy Day",
            description = "Fresh breeze"
        )
    }

    private fun createExtremeWeatherTheme(weather: WeatherData): ThemeData {
        return ThemeData(
            colors = ThemeColors(
                primary = Color(0xFFD32F2F),
                onPrimary = Color.WHITE,
                secondary = Color(0xFFF44336),
                onSecondary = Color.WHITE,
                tertiary = Color(0xFFFF5722),
                onTertiary = Color.WHITE,
                background = Color(0xFF212121),
                onBackground = Color(0xFFEEEEEE),
                surface = Color(0xFF424242),
                onSurface = Color(0xFFEEEEEE),
                error = Color(0xFFFFEB3B),
                onError = Color(0xFF000000)
            ),
            category = ThemeCategory.SUGARRUSH,
            name = "Extreme Weather",
            description = "Intense weather alert"
        )
    }

    private fun createParticleConfigForWeather(weather: WeatherData): ParticleConfig {
        return when (weather.condition) {
            WeatherCondition.SUNNY, WeatherCondition.CLEAR -> ParticleConfig(
                count = 30..60,
                speed = FloatRange(1f, 3f),
                colors = listOf("#FFD54F", "#FFECB3", "#FFFFFF")
            )
            WeatherCondition.RAIN, WeatherCondition.HEAVY_RAIN -> ParticleConfig(
                count = 100..200,
                speed = FloatRange(5f, 10f),
                colors = listOf("#90CAF9", "#64B5F6", "#42A5F5")
            )
            WeatherCondition.SNOW, WeatherCondition.HEAVY_SNOW -> ParticleConfig(
                count = 50..100,
                speed = FloatRange(0.5f, 2f),
                colors = listOf("#FFFFFF", "#E3F2FD", "#BBDEFB")
            )
            WeatherCondition.STORM -> ParticleConfig(
                count = 20..40,
                speed = FloatRange(3f, 8f),
                colors = listOf("#FFEB3B", "#FFC107", "#90A4AE")
            )
            else -> ParticleConfig(
                count = 20..50,
                speed = FloatRange(0.5f, 2f),
                colors = listOf("#B0BEC5", "#CFD8DC", "#ECEFF1")
            )
        }
    }

    private suspend fun fetchWeatherFromAPI(): WeatherData {
        // Mock implementation - in production, call actual weather API
        return WeatherData(
            temperature = 22f,
            condition = WeatherCondition.PARTLY_CLOUDY,
            humidity = 60f,
            windSpeed = 15f,
            isDaytime = true,
            location = "Default Location"
        )
    }

    private fun loadWeatherPreferences() {
        scope.launch {
            context.weatherDataStore.data.collect { prefs ->
                // Load saved preferences if needed
            }
        }
    }
}

/**
 * Theme data for weather themes
 */
data class ThemeData(
    val colors: ThemeColors,
    val category: ThemeCategory,
    val name: String,
    val description: String
)

/**
 * Utility for color operations
 */
object ColorUtils {
    fun colorToHex(color: Int): String {
        return String.format("#%06X", 0xFFFFFF and color)
    }

    fun colorToHSL(color: Int): FloatArray {
        val hsl = FloatArray(3)
        androidx.core.graphics.ColorUtils.colorToHSL(color, hsl)
        return hsl
    }

    fun setAlphaComponent(color: Int, alpha: Int): Int {
        return androidx.core.graphics.ColorUtils.setAlphaComponent(color, alpha)
    }

    fun calculateLuminance(color: Int): Double {
        return androidx.core.graphics.ColorUtils.calculateLuminance(color)
    }
}

/**
 * Linear interpolation between colors
 */
fun lerpColor(color1: Int, color2: Int, fraction: Float): Int {
    val r1 = Color.red(color1)
    val g1 = Color.green(color1)
    val b1 = Color.blue(color1)

    val r2 = Color.red(color2)
    val g2 = Color.green(color2)
    val b2 = Color.blue(color2)

    val r = (r1 + (r2 - r1) * fraction).toInt().coerceIn(0, 255)
    val g = (g1 + (g2 - g1) * fraction).toInt().coerceIn(0, 255)
    val b = (b1 + (b2 - b1) * fraction).toInt().coerceIn(0, 255)

    return Color.argb(255, r, g, b)
}
