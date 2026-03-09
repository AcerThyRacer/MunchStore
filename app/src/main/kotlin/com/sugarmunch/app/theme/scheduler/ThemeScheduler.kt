package com.sugarmunch.app.theme.scheduler

import android.content.Context
import com.sugarmunch.app.theme.model.CandyTheme
import com.sugarmunch.app.util.SecureLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

/**
 * Theme Scheduler - Time-based automatic theme switching
 * 
 * Features:
 * - Schedule themes by time of day
 * - Sunrise/sunset integration
 * - Day/night automatic switching
 * - Custom time-based rules
 * - Smart theme recommendations
 */
class ThemeScheduler(private val context: Context) {
    private val logger = SecureLogger.create("ThemeScheduler")
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // Scheduled themes
    private val _scheduledThemes = MutableStateFlow<List<ScheduledTheme>>(emptyList())
    val scheduledThemes: StateFlow<List<ScheduledTheme>> = _scheduledThemes.asStateFlow()
    
    // Current active scheduled theme
    private val _activeScheduledTheme = MutableStateFlow<ScheduledTheme?>(null)
    val activeScheduledTheme: StateFlow<ScheduledTheme?> = _activeScheduledTheme.asStateFlow()
    
    // Auto-switch enabled
    private val _autoSwitchEnabled = MutableStateFlow(false)
    val autoSwitchEnabled: StateFlow<Boolean> = _autoSwitchEnabled.asStateFlow()
    
    // Location for sunrise/sunset
    private var latitude: Double? = null
    private var longitude: Double? = null
    
    init {
        loadScheduledThemes()
        startScheduler()
    }
    
    /**
     * Load saved scheduled themes
     */
    private fun loadScheduledThemes() {
        // Load from DataStore/SharedPreferences
        _scheduledThemes.value = getDefaultScheduledThemes()
    }
    
    /**
     * Get default scheduled themes
     */
    private fun getDefaultScheduledThemes(): List<ScheduledTheme> {
        return listOf(
            ScheduledTheme(
                id = "morning",
                name = "Morning Energy",
                themeId = "theme_sugarrush",
                startTime = TimeRange(6, 0), // 6:00 AM
                endTime = TimeRange(12, 0),  // 12:00 PM
                daysOfWeek = setOf(1, 2, 3, 4, 5), // Weekdays
                enabled = true
            ),
            ScheduledTheme(
                id = "afternoon",
                name = "Afternoon Focus",
                themeId = "theme_focus",
                startTime = TimeRange(12, 0),
                endTime = TimeRange(17, 0),
                daysOfWeek = setOf(1, 2, 3, 4, 5),
                enabled = true
            ),
            ScheduledTheme(
                id = "evening",
                name = "Evening Relax",
                themeId = "theme_relax",
                startTime = TimeRange(17, 0),
                endTime = TimeRange(22, 0),
                daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7),
                enabled = true
            ),
            ScheduledTheme(
                id = "night",
                name = "Night Mode",
                themeId = "theme_dark",
                startTime = TimeRange(22, 0),
                endTime = TimeRange(6, 0),
                daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7),
                enabled = true
            )
        )
    }
    
    /**
     * Start the theme scheduler
     */
    private fun startScheduler() {
        scope.launch {
            while (isActive) {
                if (_autoSwitchEnabled.value) {
                    checkAndSwitchTheme()
                }
                delay(60 * 1000) // Check every minute
            }
        }
    }
    
    /**
     * Check if theme should be switched
     */
    private fun checkAndSwitchTheme() {
        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK)
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMinute = now.get(Calendar.MINUTE)
        
        val currentTime = TimeRange(currentHour, currentMinute)
        
        // Find matching scheduled theme
        val matchingTheme = _scheduledThemes.value.find { scheduled ->
            scheduled.enabled &&
            scheduled.daysOfWeek.contains(currentDay) &&
            isTimeInRange(currentTime, scheduled.startTime, scheduled.endTime)
        }
        
        // Switch if different from current
        if (matchingTheme != null && matchingTheme.id != _activeScheduledTheme.value?.id) {
            _activeScheduledTheme.value = matchingTheme
            logger.d("Switched to scheduled theme: ${matchingTheme.name}")
        }
    }
    
    /**
     * Check if current time is in range
     */
    private fun isTimeInRange(
        current: TimeRange,
        start: TimeRange,
        end: TimeRange
    ): Boolean {
        return if (start <= end) {
            current in start..end
        } else {
            // Overnight range (e.g., 22:00 - 06:00)
            current >= start || current <= end
        }
    }
    
    /**
     * Add scheduled theme
     */
    fun addScheduledTheme(theme: ScheduledTheme) {
        _scheduledThemes.value = _scheduledThemes.value + theme
        saveScheduledThemes()
    }
    
    /**
     * Remove scheduled theme
     */
    fun removeScheduledTheme(themeId: String) {
        _scheduledThemes.value = _scheduledThemes.value.filter { it.id != themeId }
        saveScheduledThemes()
    }
    
    /**
     * Update scheduled theme
     */
    fun updateScheduledTheme(theme: ScheduledTheme) {
        _scheduledThemes.value = _scheduledThemes.value.map {
            if (it.id == theme.id) theme else it
        }
        saveScheduledThemes()
    }
    
    /**
     * Enable/disable auto-switch
     */
    fun setAutoSwitchEnabled(enabled: Boolean) {
        _autoSwitchEnabled.value = enabled
        logger.d("Auto-switch ${if (enabled) "enabled" else "disabled"}")
    }
    
    /**
     * Set location for sunrise/sunset
     */
    fun setLocation(lat: Double, lng: Double) {
        latitude = lat
        longitude = lng
    }
    
    /**
     * Save scheduled themes
     */
    private fun saveScheduledThemes() {
        // Save to DataStore/SharedPreferences
    }
    
    /**
     * Get theme recommendation based on time
     */
    fun getRecommendedTheme(): ThemeRecommendation {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        return when (hour) {
            in 5..11 -> ThemeRecommendation(
                type = RecommendationType.MORNING,
                reason = "Good morning! Start your day with energy.",
                suggestedThemeId = "theme_sugarrush"
            )
            in 12..16 -> ThemeRecommendation(
                type = RecommendationType.AFTERNOON,
                reason = "Stay focused and productive!",
                suggestedThemeId = "theme_focus"
            )
            in 17..21 -> ThemeRecommendation(
                type = RecommendationType.EVENING,
                reason = "Wind down and relax.",
                suggestedThemeId = "theme_relax"
            )
            else -> ThemeRecommendation(
                type = RecommendationType.NIGHT,
                reason = "Time to rest. Dark theme activated.",
                suggestedThemeId = "theme_dark"
            )
        }
    }
}

/**
 * Scheduled theme configuration
 */
data class ScheduledTheme(
    val id: String,
    val name: String,
    val themeId: String,
    val startTime: TimeRange,
    val endTime: TimeRange,
    val daysOfWeek: Set<Int>, // 1=Sunday, 7=Saturday
    val enabled: Boolean = true
)

/**
 * Time range
 */
data class TimeRange(
    val hour: Int,
    val minute: Int = 0
) : Comparable<TimeRange> {
    override fun compareTo(other: TimeRange): Int {
        return when {
            hour < other.hour -> -1
            hour > other.hour -> 1
            minute < other.minute -> -1
            minute > other.minute -> 1
            else -> 0
        }
    }
}

/**
 * Theme recommendation
 */
data class ThemeRecommendation(
    val type: RecommendationType,
    val reason: String,
    val suggestedThemeId: String
)

/**
 * Recommendation types
 */
enum class RecommendationType {
    MORNING, AFTERNOON, EVENING, NIGHT, WEEKEND, SPECIAL
}

/**
 * Sunrise/Sunset calculator
 */
class SunriseSunsetCalculator {
    
    /**
     * Calculate sunrise time for given location and date
     */
    fun calculateSunrise(latitude: Double, longitude: Double, date: Date): TimeRange {
        // Simplified calculation
        // In production, use proper astronomical calculations
        return TimeRange(6, 30) // Default 6:30 AM
    }
    
    /**
     * Calculate sunset time for given location and date
     */
    fun calculateSunset(latitude: Double, longitude: Double, date: Date): TimeRange {
        // Simplified calculation
        return TimeRange(18, 30) // Default 6:30 PM
    }
    
    /**
     * Check if currently between sunrise and sunset
     */
    fun isDaytime(
        latitude: Double,
        longitude: Double,
        date: Date = Date()
    ): Boolean {
        val sunrise = calculateSunrise(latitude, longitude, date)
        val sunset = calculateSunset(latitude, longitude, date)
        val now = Calendar.getInstance()
        val currentTime = TimeRange(now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))
        
        return currentTime in sunrise..sunset
    }
}
