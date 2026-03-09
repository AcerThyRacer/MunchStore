package com.sugarmunch.app.ai.sentient

import android.content.Context
import com.sugarmunch.app.hub.UnifiedAppInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Routine Detector - Detect and learn user routines
 * 
 * Features:
 * - Automatic routine detection
 * - Pattern recognition
 * - Routine suggestions
 * - Habit tracking
 */
class RoutineDetector(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Detected routines
    private val _routines = MutableStateFlow<List<DetectedRoutine>>(emptyList())
    val routines: StateFlow<List<DetectedRoutine>> = _routines.asStateFlow()

    // Usage history for pattern detection
    private val usageHistory = mutableListOf<UsageSession>()

    // Routine configuration
    var routineConfig = RoutineConfig(
        minOccurrences = 5,
        timeWindowMinutes = 30,
        confidenceThreshold = 0.7f,
        maxRoutines = 20
    )

    init {
        startRoutineDetection()
    }

    private fun startRoutineDetection() {
        scope.launch {
            while (isActive) {
                detectRoutines()
                delay(60000) // Check every minute
            }
        }
    }

    /**
     * Record app usage session
     */
    fun recordAppUsage(app: UnifiedAppInfo) {
        val session = UsageSession(
            appId = app.packageName,
            appName = app.name,
            category = app.category,
            startTime = System.currentTimeMillis(),
            hourOfDay = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
            dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK),
            location = getCurrentLocation(),
            isCharging = isCharging(),
            isConnectedToWifi = isConnectedToWifi()
        )

        usageHistory.add(session)

        // Trim old sessions
        val cutoff = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) // 7 days
        while (usageHistory.isNotEmpty() && usageHistory.first().startTime < cutoff) {
            usageHistory.removeAt(0)
        }
    }

    /**
     * Detect routines from usage history
     */
    private fun detectRoutines() {
        if (usageHistory.size < routineConfig.minOccurrences * 3) return

        val newRoutines = mutableListOf<DetectedRoutine>()

        // Group sessions by app and time window
        val appTimeGroups = usageHistory.groupBy { session ->
            "${session.appId}_${getRoundedHour(session.hourOfDay)}_${session.dayOfWeek}"
        }

        // Find patterns that meet threshold
        appTimeGroups.forEach { (key, sessions) ->
            if (sessions.size >= routineConfig.minOccurrences) {
                val parts = key.split("_")
                val appId = parts[0]
                val hour = parts[1].toInt()
                val dayOfWeek = parts[2].toInt()

                val confidence = calculateRoutineConfidence(sessions)

                if (confidence >= routineConfig.confidenceThreshold) {
                    newRoutines.add(
                        DetectedRoutine(
                            id = "routine_${System.currentTimeMillis()}",
                            name = generateRoutineName(appId, hour, dayOfWeek),
                            appId = appId,
                            hourOfDay = hour,
                            dayOfWeek = dayOfWeek,
                            confidence = confidence,
                            occurrences = sessions.size,
                            lastTriggered = sessions.maxByOrNull { it.startTime }?.startTime ?: 0,
                            type = RoutineType.APP_LAUNCH,
                            isEnabled = true
                        )
                    )
                }
            }
        }

        // Merge with existing routines
        val mergedRoutines = mergeRoutines(_routines.value, newRoutines)
        _routines.value = mergedRoutines.take(routineConfig.maxRoutines)
    }

    private fun getRoundedHour(hour: Int): Int {
        return (hour / 1) * 1 // Round to nearest hour
    }

    private fun calculateRoutineConfidence(sessions: List<UsageSession>): Float {
        if (sessions.size < routineConfig.minOccurrences) return 0f

        // Calculate consistency score
        val hourVariance = calculateVariance(sessions.map { it.hourOfDay })
        val dayVariance = calculateVariance(sessions.map { it.dayOfWeek.toFloat() })

        val hourConsistency = 1f - (hourVariance / 24f).coerceIn(0f, 1f)
        val dayConsistency = 1f - (dayVariance / 7f).coerceIn(0f, 1f)

        // Calculate recency score
        val now = System.currentTimeMillis()
        val latestSession = sessions.maxByOrNull { it.startTime } ?: return 0f
        val daysSinceLastUse = (now - latestSession.startTime) / (24 * 60 * 60 * 1000)
        val recencyScore = kotlin.math.exp(-daysSinceLastUse.toFloat() / 7).coerceIn(0f, 1f)

        // Calculate frequency score
        val frequencyScore = (sessions.size.toFloat() / 30).coerceIn(0f, 1f) // Max score at 30 occurrences

        // Combined confidence
        return (hourConsistency * 0.3f + dayConsistency * 0.3f + recencyScore * 0.2f + frequencyScore * 0.2f)
            .coerceIn(0f, 1f)
    }

    private fun calculateVariance(values: List<Float>): Float {
        if (values.isEmpty()) return 0f
        val mean = values.average()
        return values.map { (it - mean) * (it - mean) }.average().toFloat()
    }

    private fun mergeRoutines(
        existing: List<DetectedRoutine>,
        new: List<DetectedRoutine>
    ): List<DetectedRoutine> {
        val merged = existing.toMutableList()

        new.forEach { newRoutine ->
            val existingIndex = merged.indexOfFirst { existing ->
                existing.appId == newRoutine.appId &&
                existing.hourOfDay == newRoutine.hourOfDay &&
                existing.dayOfWeek == newRoutine.dayOfWeek
            }

            if (existingIndex != -1) {
                // Update existing routine
                val existingRoutine = merged[existingIndex]
                merged[existingIndex] = existingRoutine.copy(
                    confidence = (existingRoutine.confidence + newRoutine.confidence) / 2,
                    occurrences = existingRoutine.occurrences + newRoutine.occurrences,
                    lastTriggered = maxOf(existingRoutine.lastTriggered, newRoutine.lastTriggered)
                )
            } else {
                // Add new routine
                merged.add(newRoutine)
            }
        }

        return merged.sortedByDescending { it.confidence }
    }

    private fun generateRoutineName(appId: String, hour: Int, dayOfWeek: Int): String {
        val timeOfDay = when (hour) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            in 17..20 -> "Evening"
            else -> "Night"
        }

        val dayName = when (dayOfWeek) {
            1 -> "Sunday"
            2 -> "Monday"
            3 -> "Tuesday"
            4 -> "Wednesday"
            5 -> "Thursday"
            6 -> "Friday"
            7 -> "Saturday"
            else -> "Weekday"
        }

        return "$timeOfDay $dayName Routine"
    }

    // ========== ROUTINE OPPORTUNITIES ==========

    fun detectRoutineOpportunities(): List<Suggestion> {
        val suggestions = mutableListOf<Suggestion>()
        val now = System.currentTimeMillis()
        val calendar = java.util.Calendar.getInstance()
        val currentHour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val currentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK)

        _routines.value.forEach { routine ->
            if (!routine.isEnabled) return@forEach

            // Check if routine time is approaching (within 15 minutes)
            val timeDiff = abs(routine.hourOfDay - currentHour)
            if (timeDiff <= 1 && routine.dayOfWeek == currentDay) {
                suggestions.add(
                    Suggestion(
                        id = "routine_${routine.id}",
                        type = SuggestionType.ROUTINE,
                        title = "Time for ${routine.name}",
                        description = "You usually use ${getAppName(routine.appId)} around this time",
                        priority = Priority.MEDIUM,
                        action = "launch_app_${routine.appId}"
                    )
                )
            }
        }

        return suggestions
    }

    // ========== ROUTINE MANAGEMENT ==========

    fun enableRoutine(routineId: String) {
        _routines.value = _routines.value.map { routine ->
            if (routine.id == routineId) routine.copy(isEnabled = true) else routine
        }
    }

    fun disableRoutine(routineId: String) {
        _routines.value = _routines.value.map { routine ->
            if (routine.id == routineId) routine.copy(isEnabled = false) else routine
        }
    }

    fun deleteRoutine(routineId: String) {
        _routines.value = _routines.value.filter { it.id != routineId }
    }

    fun createManualRoutine(
        name: String,
        appId: String,
        hourOfDay: Int,
        dayOfWeek: Int
    ): DetectedRoutine {
        val routine = DetectedRoutine(
            id = "manual_${System.currentTimeMillis()}",
            name = name,
            appId = appId,
            hourOfDay = hourOfDay,
            dayOfWeek = dayOfWeek,
            confidence = 1.0f,
            occurrences = 0,
            lastTriggered = 0,
            type = RoutineType.MANUAL,
            isEnabled = true
        )

        _routines.value = _routines.value + routine
        return routine
    }

    // ========== HELPER METHODS ==========

    private fun getCurrentLocation(): String? {
        // Would get actual location
        return null
    }

    private fun isCharging(): Boolean {
        // Would check charging status
        return false
    }

    private fun isConnectedToWifi(): Boolean {
        // Would check WiFi status
        return false
    }

    private fun getAppName(appId: String): String {
        // Would get app name from package manager
        return appId
    }
}

/**
 * Detected routine
 */
data class DetectedRoutine(
    val id: String,
    val name: String,
    val appId: String,
    val hourOfDay: Int,
    val dayOfWeek: Int,
    val confidence: Float,
    val occurrences: Int,
    val lastTriggered: Long,
    val type: RoutineType,
    val isEnabled: Boolean,
    val apps: List<String> = emptyList(), // For multi-app routines
    val location: String? = null,
    val conditions: Map<String, Any> = emptyMap()
)

/**
 * Routine types
 */
enum class RoutineType {
    APP_LAUNCH,
    MULTI_APP,
    LOCATION_BASED,
    TIME_BASED,
    MANUAL,
    SMART
}

/**
 * Usage session
 */
data class UsageSession(
    val appId: String,
    val appName: String,
    val category: String?,
    val startTime: Long,
    val hourOfDay: Int,
    val dayOfWeek: Int,
    val location: String?,
    val isCharging: Boolean,
    val isConnectedToWifi: Boolean,
    val endTime: Long = 0,
    val duration: Long = 0
)

/**
 * Routine configuration
 */
data class RoutineConfig(
    val minOccurrences: Int = 5,
    val timeWindowMinutes: Int = 30,
    val confidenceThreshold: Float = 0.7f,
    val maxRoutines: Int = 20
)
