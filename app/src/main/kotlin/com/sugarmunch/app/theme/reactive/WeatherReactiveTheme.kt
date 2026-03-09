package com.sugarmunch.app.theme.reactive

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Weather-Reactive Theme Engine
 * Changes theme based on current weather conditions
 */
@Singleton
class WeatherReactiveTheme @Inject constructor(
    private val context: Context
) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    
    private val _weatherState = MutableStateFlow(WeatherState.Unknown)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()
    
    private val _temperature = MutableStateFlow(20f)
    val temperature: StateFlow<Float> = _temperature.asStateFlow()
    
    private val _weatherCondition = MutableStateFlow(WeatherCondition.Clear)
    val weatherCondition: StateFlow<WeatherCondition> = _weatherCondition.asStateFlow()
    
    private val _isDaytime = MutableStateFlow(true)
    val isDaytime: StateFlow<Boolean> = _isDaytime.asStateFlow()
    
    private var locationListener: LocationListener? = null
    
    companion object {
        private const val LOCATION_UPDATE_INTERVAL = 10 * 60 * 1000L // 10 minutes
        private const val LOCATION_UPDATE_MIN_DISTANCE = 1000f // 1km
    }
    
    /**
     * Start weather tracking
     */
    fun startWeatherTracking() {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            _weatherState.value = WeatherState.PermissionRequired
            return
        }
        
        try {
            locationListener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    fetchWeatherForLocation(location.latitude, location.longitude)
                }
                
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                
                @Suppress("DEPRECATION")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }
            
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                LOCATION_UPDATE_INTERVAL,
                LOCATION_UPDATE_MIN_DISTANCE,
                locationListener!!
            )
            
            // Get last known location
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            
            lastLocation?.let {
                fetchWeatherForLocation(it.latitude, it.longitude)
            }
            
        } catch (e: SecurityException) {
            _weatherState.value = WeatherState.PermissionRequired
        }
    }
    
    /**
     * Stop weather tracking
     */
    fun stopWeatherTracking() {
        locationListener?.let {
            locationManager.removeUpdates(it)
        }
        locationListener = null
    }
    
    /**
     * Fetch weather for location (simulated - would use weather API in production)
     */
    private fun fetchWeatherForLocation(latitude: Double, longitude: Double) {
        _weatherState.value = WeatherState.Loading
        
        // Simulate weather API call
        // In production, use OpenWeatherMap, WeatherAPI, etc.
        _weatherState.value = WeatherState.Available
        
        // Simulate weather based on coordinates (for demo)
        val condition = when {
            latitude > 60 || latitude < -60 -> WeatherCondition.Snow
            latitude > 45 || latitude < -45 -> WeatherCondition.Cloudy
            latitude > 30 || latitude < -30 -> WeatherCondition.Rain
            else -> WeatherCondition.Clear
        }
        
        _weatherCondition.value = condition
        _temperature.value = 25f - (kotlin.math.abs(latitude) * 0.3f)
        
        // Determine if daytime
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        _isDaytime.value = hour in 6..18
    }
    
    /**
     * Set weather manually (for testing or user override)
     */
    fun setWeatherManually(condition: WeatherCondition, temp: Float) {
        _weatherCondition.value = condition
        _temperature.value = temp
        _weatherState.value = WeatherState.Available
    }
    
    /**
     * Get theme colors based on weather
     */
    fun getWeatherThemeColors(): WeatherThemeColors {
        return when (_weatherCondition.value) {
            WeatherCondition.Clear -> {
                if (_isDaytime.value) {
                    WeatherThemeColors(
                        primary = 0xFFFFD700, // Gold
                        secondary = 0xFF87CEEB, // Sky blue
                        background = 0xFF87CEEB,
                        particleColor = 0xFFFFFFFF
                    )
                } else {
                    WeatherThemeColors(
                        primary = 0xFFFFD700, // Moon gold
                        secondary = 0xFF191970, // Midnight blue
                        background = 0xFF000033,
                        particleColor = 0xFFFFFFFF
                    )
                }
            }
            WeatherCondition.Cloudy -> WeatherThemeColors(
                primary = 0xFFA9A9A9, // Dark gray
                secondary = 0xFFD3D3D3, // Light gray
                background = 0xFF2F4F4F,
                particleColor = 0xFFC0C0C0
            )
            WeatherCondition.Rain -> WeatherThemeColors(
                primary = 0xFF4682B4, // Steel blue
                secondary = 0xFF708090, // Slate gray
                background = 0xFF191970,
                particleColor = 0xFFADD8E6
            )
            WeatherCondition.Snow -> WeatherThemeColors(
                primary = 0xFFB0E0E6, // Powder blue
                secondary = 0xFFE0FFFF, // Light cyan
                background = 0xFF191970,
                particleColor = 0xFFFFFFFF
            )
            WeatherCondition.Storm -> WeatherThemeColors(
                primary = 0xFF4B0082, // Indigo
                secondary = 0xFFFFD700, // Lightning gold
                background = 0xFF000033,
                particleColor = 0xFFFFFF00
            )
            WeatherCondition.Fog -> WeatherThemeColors(
                primary = 0xFFD3D3D3, // Light gray
                secondary = 0xFFA9A9A9, // Dark gray
                background = 0xFF2F4F4F,
                particleColor = 0xFFC0C0C0
            )
        }
    }
    
    /**
     * Get particle type based on weather
     */
    fun getWeatherParticleType(): String {
        return when (_weatherCondition.value) {
            WeatherCondition.Clear -> if (_isDaytime.value) "sun_rays" else "stars"
            WeatherCondition.Cloudy -> "clouds"
            WeatherCondition.Rain -> "rain"
            WeatherCondition.Snow -> "snow"
            WeatherCondition.Storm -> "lightning"
            WeatherCondition.Fog -> "mist"
        }
    }
    
    /**
     * Get particle intensity based on weather severity
     */
    fun getParticleIntensity(): Float {
        return when (_weatherCondition.value) {
            WeatherCondition.Clear -> 0.5f
            WeatherCondition.Cloudy -> 0.7f
            WeatherCondition.Rain -> 1.0f
            WeatherCondition.Snow -> 0.8f
            WeatherCondition.Storm -> 1.5f
            WeatherCondition.Fog -> 0.6f
        }
    }
}

/**
 * Weather state sealed class
 */
sealed class WeatherState {
    object Unknown : WeatherState()
    object Loading : WeatherState()
    object Available : WeatherState()
    object PermissionRequired : WeatherState()
    object Error : WeatherState()
}

/**
 * Weather conditions
 */
enum class WeatherCondition {
    Clear,
    Cloudy,
    Rain,
    Snow,
    Storm,
    Fog
}

/**
 * Weather theme colors
 */
data class WeatherThemeColors(
    val primary: Int,
    val secondary: Int,
    val background: Int,
    val particleColor: Int
)
