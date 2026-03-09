package com.sugarmunch.app.theme.reactive

import android.content.Context
import com.sugarmunch.app.BuildConfig
import com.sugarmunch.app.util.SecureLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

/**
 * Weather Provider for Weather-Reactive Themes
 * 
 * Features:
 * - Current weather data
 * - Weather-based theme suggestions
 * - Automatic theme switching
 */
class WeatherProvider(private val context: Context) {
    private val logger = SecureLogger.create("WeatherProvider")
    private val json = Json { ignoreUnknownKeys = true }
    
    // Weather API (using OpenWeatherMap - free tier)
    private val weatherApiUrl = "https://api.openweathermap.org/data/2.5/weather"
    private val apiKey: String? = BuildConfig.OPENWEATHER_API_KEY.takeIf { it.isNotBlank() }
    
    // Current weather state
    private var currentWeather: WeatherData? = null
    private var lastUpdateTime = 0L
    private val updateInterval = 30 * 60 * 1000L // 30 minutes
    
    /**
     * Get current weather data
     */
    suspend fun getCurrentWeather(location: Location): Result<WeatherData> {
        return withContext(Dispatchers.IO) {
            try {
                // Check cache
                val now = System.currentTimeMillis()
                if (currentWeather != null && now - lastUpdateTime < updateInterval) {
                    return@withContext Result.success(currentWeather!!)
                }
                
                // Check API key
                val key = apiKey ?: return@withContext Result.failure(
                    WeatherException("API key not configured")
                )
                
                // Fetch weather data
                val url = URL(
                    "$weatherApiUrl?lat=${location.latitude}&lon=${location.longitude}" +
                    "&appid=$key&units=metric"
                )
                
                val response = url.readText()
                val weatherData = parseWeatherResponse(response)
                
                currentWeather = weatherData
                lastUpdateTime = now
                
                Result.success(weatherData)
            } catch (e: Exception) {
                logger.e("Failed to fetch weather data", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * Get weather-based theme suggestion
     */
    fun getSuggestedTheme(): WeatherTheme {
        return when (currentWeather?.condition) {
            WeatherCondition.SUNNY -> WeatherTheme.BRIGHT_CHEERFUL
            WeatherCondition.CLOUDY -> WeatherTheme.COZY_GRAY
            WeatherCondition.RAINY -> WeatherCondition.RAINY_MOOD
            WeatherCondition.STORMY -> WeatherTheme.DRAMATIC_DARK
            WeatherCondition.SNOWY -> WeatherTheme.WINTER_WONDERLAND
            WeatherCondition.FOGGY -> WeatherTheme.MYSTICAL_MIST
            else -> WeatherTheme.NEUTRAL
        }
    }
    
    /**
     * Parse weather API response
     */
    private fun parseWeatherResponse(jsonString: String): WeatherData {
        // Simplified parsing - in production use proper Retrofit + Moshi/Gson
        return try {
            val json = Json.parseToJsonElement(jsonString).jsonObject
            val main = json["main"]?.jsonObject
            val weather = json["weather"]?.jsonArray?.get(0)?.jsonObject
            
            WeatherData(
                temperature = main?.get("temp")?.jsonPrimitive?.float ?: 20f,
                condition = parseWeatherCondition(
                    weather?.get("main")?.jsonPrimitive?.content ?: "Clear"
                ),
                humidity = main?.get("humidity")?.jsonPrimitive?.int ?: 50,
                description = weather?.get("description")?.jsonPrimitive?.content ?: "Clear"
            )
        } catch (e: Exception) {
            WeatherData(20f, WeatherCondition.CLEAR, 50, "Clear")
        }
    }
    
    /**
     * Parse weather condition string to enum
     */
    private fun parseWeatherCondition(condition: String): WeatherCondition {
        return when (condition.lowercase()) {
            "clear" -> WeatherCondition.SUNNY
            "clouds" -> WeatherCondition.CLOUDY
            "rain", "drizzle" -> WeatherCondition.RAINY
            "thunderstorm" -> WeatherCondition.STORMY
            "snow" -> WeatherCondition.SNOWY
            "mist", "haze", "fog" -> WeatherCondition.FOGGY
            else -> WeatherCondition.CLEAR
        }
    }
    
    /**
     * Check if weather-based themes should be active
     */
    fun shouldUpdateTheme(): Boolean {
        val now = System.currentTimeMillis()
        return currentWeather == null || now - lastUpdateTime > updateInterval
    }
}

/**
 * Weather data
 */
@Serializable
data class WeatherData(
    val temperature: Float,      // Celsius
    val condition: WeatherCondition,
    val humidity: Int,           // Percentage
    val description: String
) {
    /**
     * Get temperature in Fahrenheit
     */
    fun getTemperatureF(): Float = temperature * 9/5 + 32
    
    /**
     * Get comfort level
     */
    fun getComfortLevel(): ComfortLevel {
        return when {
            temperature < 0 -> ComfortLevel.FREEZING
            temperature < 10 -> ComfortLevel.COLD
            temperature < 20 -> ComfortLevel.COOL
            temperature < 28 -> ComfortLevel.COMFORTABLE
            temperature < 35 -> ComfortLevel.WARM
            else -> ComfortLevel.HOT
        }
    }
}

/**
 * Weather conditions
 */
enum class WeatherCondition {
    SUNNY, CLEAR, CLOUDY, RAINY, STORMY, SNOWY, FOGGY
}

/**
 * Comfort levels
 */
enum class ComfortLevel {
    FREEZING, COLD, COOL, COMFORTABLE, WARM, HOT
}

/**
 * Location data
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val name: String = ""
)

/**
 * Weather-based theme suggestions
 */
enum class WeatherTheme(val displayName: String) {
    BRIGHT_CHEERFUL("Bright & Cheerful"),
    COZY_GRAY("Cozy Gray"),
    RAINY_MOOD("Rainy Mood"),
    DRAMATIC_DARK("Dramatic Dark"),
    WINTER_WONDERLAND("Winter Wonderland"),
    MYSTICAL_MIST("Mystical Mist"),
    NEUTRAL("Neutral")
}

/**
 * Weather exception
 */
class WeatherException(message: String) : Exception(message)
