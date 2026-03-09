package com.sugarmunch.app.ai.neural

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*

private val Context.moodDataStore: DataStore<Preferences> by preferencesDataStore(name = "mood_preferences")

/**
 * Mood Detector - Detects user mood through various signals
 * 
 * Signals used:
 * - Time of day
 * - Usage patterns
 * - Device handling (accelerometer)
 * - Typing speed (if available)
 * - Manual mood input
 */
class MoodDetector(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _moodFlow = MutableStateFlow<UserMood>(UserMood.NEUTRAL)
    val moodFlow: StateFlow<UserMood> = _moodFlow.asStateFlow()

    private val _confidenceFlow = MutableStateFlow(0.5f)
    val confidenceFlow: StateFlow<Float> = _confidenceFlow.asStateFlow()

    private val moodSignals = mutableListOf<MoodSignal>()
    private var accelerometerSensor: Sensor? = null
    private var accelerometerListener: SensorEventListener? = null

    // Mood history for pattern detection
    private val moodHistory = mutableListOf<MoodHistoryEntry>()
    private val moodPatterns = mutableMapOf<Calendar.HOUR_OF_DAY, List<UserMood>>()

    init {
        initializeSensors()
        startTimeBasedMoodDetection()
        loadMoodHistory()
    }

    /**
     * Initialize sensors for mood detection
     */
    private fun initializeSensors() {
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        if (accelerometerSensor != null) {
            accelerometerListener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    analyzeDeviceHandling(event.values)
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }
        }
    }

    /**
     * Start listening for mood signals
     */
    fun startMoodDetection() {
        accelerometerListener?.let { listener ->
            accelerometerSensor?.let { sensor ->
                sensorManager.registerListener(
                    listener,
                    sensor,
                    SensorManager.SENSOR_DELAY_UI
                )
            }
        }
    }

    /**
     * Stop mood detection
     */
    fun stopMoodDetection() {
        accelerometerListener?.let { listener ->
            sensorManager.unregisterListener(listener)
        }
    }

    /**
     * Analyze device handling for mood clues
     */
    private fun analyzeDeviceHandling(values: FloatArray) {
        val x = values[0]
        val y = values[1]
        val z = values[2]

        // Calculate movement intensity
        val movementIntensity = kotlin.math.sqrt(x * x + y * y + z * z - 9.8f * 9.8f)

        // Add signal based on movement
        val signal = when {
            movementIntensity > 15f -> MoodSignal(MoodSignalType.HIGH_MOVEMENT, 0.3f)
            movementIntensity < 2f -> MoodSignal(MoodSignalType.LOW_MOVEMENT, 0.2f)
            else -> MoodSignal(MoodSignalType.NORMAL_MOVEMENT, 0.1f)
        }

        addSignal(signal)
    }

    /**
     * Start time-based mood pattern detection
     */
    private fun startTimeBasedMoodDetection() {
        scope.launch {
            while (isActive) {
                val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                val timeBasedMood = detectMoodFromTime(currentHour)
                
                // Combine with other signals
                val combinedMood = combineMoodSignals(timeBasedMood)
                _moodFlow.value = combinedMood

                // Save to history
                saveMoodEntry(combinedMood, MoodEntrySource.TIME_PATTERN)

                delay(60000) // Update every minute
            }
        }
    }

    /**
     * Detect mood from time of day
     */
    private fun detectMoodFromTime(hour: Int): UserMood {
        return when (hour) {
            in 5..8 -> UserMood.NEUTRAL // Morning grogginess
            in 9..11 -> UserMood.ENERGETIC // Morning energy peak
            in 12..14 -> UserMood.HAPPY // Lunch time
            in 15..17 -> UserMood.FOCUSED // Afternoon focus
            in 18..20 -> UserMood.CALM // Evening wind down
            in 21..23 -> UserMood.MELANCHOLIC // Night reflection
            else -> UserMood.CALM // Late night calm
        }
    }

    /**
     * Add a mood signal
     */
    fun addSignal(signal: MoodSignal) {
        moodSignals.add(signal)

        // Keep only recent signals
        if (moodSignals.size > 100) {
            moodSignals.removeAt(0)
        }

        // Recalculate mood
        val combinedMood = combineMoodSignals(_moodFlow.value)
        _moodFlow.value = combinedMood
        _confidenceFlow.value = calculateConfidence()
    }

    /**
     * Manually set mood (user input)
     */
    fun setManualMood(mood: UserMood, confidence: Float = 1.0f) {
        _moodFlow.value = mood
        _confidenceFlow.value = confidence

        scope.launch {
            saveMoodEntry(mood, MoodEntrySource.MANUAL)
        }
    }

    /**
     * Record usage pattern for mood inference
     */
    fun recordUsagePattern(pattern: UsagePattern) {
        val signal = when (pattern) {
            UsagePattern.RAPID_APP_SWITCHING -> MoodSignal(MoodSignalType.STRESSED, 0.4f)
            UsagePattern.LONG_SESSION_ONE_APP -> MoodSignal(MoodSignalType.FOCUSED, 0.5f)
            UsagePattern.FREQUENCY_HIGH -> MoodSignal(MoodSignalType.ENGAGED, 0.3f)
            UsagePattern.FREQUENCY_LOW -> MoodSignal(MoodSignalType.DISINTERESTED, 0.2f)
            UsagePattern.TYPING_FAST -> MoodSignal(MoodSignalType.ENERGETIC, 0.4f)
            UsagePattern.TYPING_SLOW -> MoodSignal(MoodSignalType.TIRED, 0.3f)
        }

        addSignal(signal)
    }

    /**
     * Combine all mood signals
     */
    private fun combineMoodSignals(baseMood: UserMood): UserMood {
        if (moodSignals.isEmpty()) {
            return baseMood
        }

        // Weight recent signals more heavily
        val weightedSignals = moodSignals.mapIndexed { index, signal ->
            val weight = (index + 1).toFloat() / moodSignals.size
            signal.copy(strength = signal.strength * weight)
        }

        // Group by mood type
        val moodScores = mutableMapOf<UserMood, Float>()

        weightedSignals.forEach { signal ->
            val mood = signal.toMood()
            moodScores[mood] = (moodScores[mood] ?: 0f) + signal.strength
        }

        // Find dominant mood
        val dominantMood = moodScores.maxByOrNull { it.value }?.key ?: baseMood

        // Apply base mood influence
        return if (moodScores[dominantMood] ?: 0f > 0.5f) {
            dominantMood
        } else {
            baseMood
        }
    }

    /**
     * Calculate confidence in mood detection
     */
    private fun calculateConfidence(): Float {
        if (moodSignals.isEmpty()) {
            return 0.3f
        }

        val recentSignals = moodSignals.takeLast(20)
        val signalStrength = recentSignals.map { it.strength }.average()

        return signalStrength.toFloat().coerceIn(0.1f, 1.0f)
    }

    /**
     * Save mood entry to history
     */
    private suspend fun saveMoodEntry(mood: UserMood, source: MoodEntrySource) {
        moodHistory.add(
            MoodHistoryEntry(
                mood = mood,
                timestamp = System.currentTimeMillis(),
                source = source,
                hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            )
        )

        // Keep history manageable
        if (moodHistory.size > 1000) {
            moodHistory.removeAt(0)
        }

        // Save to DataStore
        context.moodDataStore.edit { prefs ->
            prefs[stringPreferencesKey("last_mood")] = mood.name
            prefs[intPreferencesKey("mood_history_size")] = moodHistory.size
        }
    }

    /**
     * Load mood history from DataStore
     */
    private suspend fun loadMoodHistory() {
        context.moodDataStore.data.first().let { prefs ->
            val lastMood = prefs[stringPreferencesKey("last_mood")]?.let {
                try {
                    UserMood.valueOf(it)
                } catch (e: Exception) {
                    UserMood.NEUTRAL
                }
            } ?: UserMood.NEUTRAL

            _moodFlow.value = lastMood
        }
    }

    /**
     * Get mood patterns by hour
     */
    fun getMoodPatterns(): Map<Int, List<UserMood>> {
        return moodHistory.groupBy { it.hourOfDay }.mapValues { entry ->
            entry.value.map { it.mood }
        }
    }

    /**
     * Predict mood for a given hour
     */
    fun predictMoodForHour(hour: Int): UserMood {
        val patterns = getMoodPatterns()
        val moodsForHour = patterns[hour] ?: listOf(UserMood.NEUTRAL)

        // Find most common mood for this hour
        return moodsForHour.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key ?: UserMood.NEUTRAL
    }

    /**
     * Get mood trend over time
     */
    fun getMoodTrend(hours: Int = 24): MoodTrend {
        val cutoff = System.currentTimeMillis() - (hours * 60 * 60 * 1000)
        val recentHistory = moodHistory.filter { it.timestamp > cutoff }

        if (recentHistory.isEmpty()) {
            return MoodTrend.STABLE
        }

        val moodCounts = recentHistory.groupingBy { it.mood }.eachCount()
        val dominantMood = moodCounts.maxByOrNull { it.value }?.key ?: UserMood.NEUTRAL

        // Analyze trend
        val firstHalf = recentHistory.take(recentHistory.size / 2)
        val secondHalf = recentHistory.drop(recentHistory.size / 2)

        val firstAvg = firstHalf.map { moodToNumber(it.mood) }.average()
        val secondAvg = secondHalf.map { moodToNumber(it.mood) }.average()

        return when {
            secondAvg > firstAvg + 0.5 -> MoodTrend.IMPROVING
            secondAvg < firstAvg - 0.5 -> MoodTrend.DECLINING
            else -> MoodTrend.STABLE
        }
    }

    /**
     * Get mood improvement suggestions
     */
    fun getMoodImprovementSuggestions(): List<String> {
        val currentMood = _moodFlow.value
        val trend = getMoodTrend()

        return when {
            currentMood == UserMood.MELANCHOLIC -> listOf(
                "Try a brighter theme to lift your spirits",
                "Consider calming nature sounds",
                "Take a short break and stretch"
            )
            currentMood == UserMood.ENERGETIC && trend == MoodTrend.DECLINING -> listOf(
                "Channel your energy into a creative task",
                "Try an upbeat theme to maintain momentum"
            )
            trend == MoodTrend.DECLINING -> listOf(
                "Consider a theme change for a fresh perspective",
                "Take regular breaks throughout the day",
                "Try some deep breathing exercises"
            )
            else -> listOf(
                "Great mood! Keep it up with positive themes",
                "Your mood is stable - perfect for focused work"
            )
        }
    }

    private fun moodToNumber(mood: UserMood): Float {
        return when (mood) {
            UserMood.HAPPY -> 1.0f
            UserMood.ENERGETIC -> 0.8f
            UserMood.FOCUSED -> 0.5f
            UserMood.CALM -> 0.3f
            UserMood.NEUTRAL -> 0.0f
            UserMood.MELANCHOLIC -> -0.5f
        }
    }

    companion object {
        @Volatile
        private var instance: MoodDetector? = null

        fun getInstance(context: Context): MoodDetector {
            return instance ?: synchronized(this) {
                instance ?: MoodDetector(context.applicationContext).also { instance = it }
            }
        }
    }
}

/**
 * Mood signal for detection
 */
data class MoodSignal(
    val type: MoodSignalType,
    val strength: Float
) {
    fun toMood(): UserMood {
        return when (type) {
            MoodSignalType.HIGH_MOVEMENT -> UserMood.ENERGETIC
            MoodSignalType.LOW_MOVEMENT -> UserMood.CALM
            MoodSignalType.NORMAL_MOVEMENT -> UserMood.NEUTRAL
            MoodSignalType.STRESSED -> UserMood.MELANCHOLIC
            MoodSignalType.FOCUSED -> UserMood.FOCUSED
            MoodSignalType.ENGAGED -> UserMood.HAPPY
            MoodSignalType.DISINTERESTED -> UserMood.NEUTRAL
            MoodSignalType.ENERGETIC -> UserMood.ENERGETIC
            MoodSignalType.TIRED -> UserMood.MELANCHOLIC
        }
    }
}

/**
 * Types of mood signals
 */
enum class MoodSignalType {
    HIGH_MOVEMENT,
    LOW_MOVEMENT,
    NORMAL_MOVEMENT,
    STRESSED,
    FOCUSED,
    ENGAGED,
    DISINTERESTED,
    ENERGETIC,
    TIRED
}

/**
 * Usage patterns for mood inference
 */
enum class UsagePattern {
    RAPID_APP_SWITCHING,
    LONG_SESSION_ONE_APP,
    FREQUENCY_HIGH,
    FREQUENCY_LOW,
    TYPING_FAST,
    TYPING_SLOW
}

/**
 * Mood history entry
 */
data class MoodHistoryEntry(
    val mood: UserMood,
    val timestamp: Long,
    val source: MoodEntrySource,
    val hourOfDay: Int
)

/**
 * Source of mood entry
 */
enum class MoodEntrySource {
    MANUAL,
    TIME_PATTERN,
    DEVICE_HANDLING,
    USAGE_PATTERN,
    INFERRED
}

/**
 * Mood trend direction
 */
enum class MoodTrend {
    IMPROVING,
    STABLE,
    DECLINING
}
