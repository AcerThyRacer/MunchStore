package com.sugarmunch.app.ai.sentient

import android.content.Context
import com.sugarmunch.app.hub.UnifiedAppInfo
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * App Predictor - ML-based app usage prediction
 * 
 * Features:
 * - Neural network prediction
 * - Time-series analysis
 * - Pattern recognition
 * - Usage statistics
 */
class AppPredictor(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Usage history
    private val usageHistory = mutableListOf<UsageEntry>()
    private val _usageStats = MutableStateFlow(UsageStats())
    val usageStats: StateFlow<UsageStats> = _usageStats.asStateFlow()

    // Prediction model weights (simplified neural network)
    private var modelWeights = PredictionWeights()

    // Prediction configuration
    var predictionConfig = PredictionConfig(
        historyWindow = 168, // 7 days in hours
        minDataPoints = 50,
        learningRate = 0.01f,
        confidenceThreshold = 0.5f
    )

    /**
     * Record app usage
     */
    fun recordAppUsage(app: UnifiedAppInfo) {
        val entry = UsageEntry(
            appId = app.packageName,
            appName = app.name,
            category = app.category,
            timestamp = System.currentTimeMillis(),
            hourOfDay = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
            dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK),
            launchCount = app.launchCount,
            sessionDuration = 0 // Would track actual session duration
        )

        usageHistory.add(entry)

        // Trim old entries
        val cutoff = System.currentTimeMillis() - (predictionConfig.historyWindow * 60 * 60 * 1000)
        while (usageHistory.isNotEmpty() && usageHistory.first().timestamp < cutoff) {
            usageHistory.removeAt(0)
        }

        // Update stats
        updateUsageStats()

        // Train model
        trainModel()
    }

    /**
     * Predict next apps
     */
    fun predictNextApps(
        context: ContextData,
        count: Int = 5,
        minConfidence: Float = 0.5f
    ): List<AppPrediction> {
        if (usageHistory.size < predictionConfig.minDataPoints) {
            return getFallbackPredictions(count)
        }

        val predictions = mutableListOf<AppPrediction>()
        val appScores = mutableMapOf<String, Float>()

        // Calculate scores for each app based on multiple factors
        usageHistory
            .map { it.appId }
            .distinct()
            .forEach { appId ->
                val score = calculateAppScore(appId, context)
                if (score >= minConfidence) {
                    appScores[appId] = score
                }
            }

        // Sort by score and take top N
        val sortedApps = appScores.toSortedMap(compareByDescending { it.value })
            .take(count)

        sortedApps.forEach { (appId, score) ->
            val app = getAppById(appId)
            if (app != null) {
                predictions.add(
                    AppPrediction(
                        app = app,
                        confidence = score,
                        predictedHour = context.hour,
                        reason = generatePredictionReason(appId, context)
                    )
                )
            }
        }

        return predictions
    }

    /**
     * Calculate app score using neural network-inspired approach
     */
    private fun calculateAppScore(appId: String, context: ContextData): Float {
        val appHistory = usageHistory.filter { it.appId == appId }

        if (appHistory.isEmpty()) return 0f

        // Time-based score
        val timeScore = calculateTimeScore(appHistory, context.hour, context.dayOfWeek)

        // Recency score
        val recencyScore = calculateRecencyScore(appHistory)

        // Frequency score
        val frequencyScore = calculateFrequencyScore(appHistory)

        // Pattern score
        val patternScore = calculatePatternScore(appHistory, context)

        // Combine scores with learned weights
        val combinedScore = (
            timeScore * modelWeights.timeWeight +
            recencyScore * modelWeights.recencyWeight +
            frequencyScore * modelWeights.frequencyWeight +
            patternScore * modelWeights.patternWeight
        ) / (modelWeights.timeWeight + modelWeights.recencyWeight +
             modelWeights.frequencyWeight + modelWeights.patternWeight)

        return combinedScore.coerceIn(0f, 1f)
    }

    private fun calculateTimeScore(
        appHistory: List<UsageEntry>,
        hour: Int,
        dayOfWeek: Int
    ): Float {
        val sameHourUsage = appHistory.filter { it.hourOfDay == hour }
        val sameDayUsage = appHistory.filter { it.dayOfWeek == dayOfWeek }

        val hourRatio = sameHourUsage.size.toFloat() / appHistory.size.coerceAtLeast(1)
        val dayRatio = sameDayUsage.size.toFloat() / appHistory.size.coerceAtLeast(1)

        return (hourRatio * 0.7f + dayRatio * 0.3f).coerceIn(0f, 1f)
    }

    private fun calculateRecencyScore(appHistory: List<UsageEntry>): Float {
        if (appHistory.isEmpty()) return 0f

        val now = System.currentTimeMillis()
        val latestUsage = appHistory.maxByOrNull { it.timestamp } ?: return 0f

        val hoursSinceLastUse = (now - latestUsage.timestamp) / (1000 * 60 * 60)

        // Exponential decay
        return kotlin.math.exp(-hoursSinceLastUse.toFloat() / 24).coerceIn(0f, 1f)
    }

    private fun calculateFrequencyScore(appHistory: List<UsageEntry>): Float {
        if (appHistory.isEmpty()) return 0f

        val now = System.currentTimeMillis()
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)

        val recentUsage = appHistory.filter { it.timestamp > weekAgo }.size
        val totalUsage = appHistory.size

        return (recentUsage.toFloat() / totalUsage).coerceIn(0f, 1f)
    }

    private fun calculatePatternScore(
        appHistory: List<UsageEntry>,
        context: ContextData
    ): Float {
        // Check for consistent patterns
        val hourCounts = appHistory.groupingBy { it.hourOfDay }.eachCount()
        val dayCounts = appHistory.groupingBy { it.dayOfWeek }.eachCount()

        val maxHourCount = hourCounts.values.maxOrNull() ?: 0
        val maxDayCount = dayCounts.values.maxOrNull() ?: 0

        val hourPatternStrength = maxHourCount.toFloat() / appHistory.size.coerceAtLeast(1)
        val dayPatternStrength = maxDayCount.toFloat() / appHistory.size.coerceAtLeast(1)

        return (hourPatternStrength * 0.6f + dayPatternStrength * 0.4f).coerceIn(0f, 1f)
    }

    /**
     * Train prediction model
     */
    private fun trainModel() {
        if (usageHistory.size < predictionConfig.minDataPoints) return

        // Simple gradient descent to optimize weights
        val predictions = makeTestPredictions()
        val actuals = getTestActuals()

        var totalError = 0f

        predictions.forEachIndexed { index, predicted ->
            val actual = actuals.getOrElse(index) { 0f }
            val error = actual - predicted
            totalError += kotlin.math.abs(error)

            // Update weights based on error
            modelWeights.timeWeight += predictionConfig.learningRate * error
            modelWeights.recencyWeight += predictionConfig.learningRate * error
            modelWeights.frequencyWeight += predictionConfig.learningRate * error
            modelWeights.patternWeight += predictionConfig.learningRate * error
        }

        // Normalize weights
        val totalWeight = modelWeights.timeWeight + modelWeights.recencyWeight +
                         modelWeights.frequencyWeight + modelWeights.patternWeight

        modelWeights = PredictionWeights(
            timeWeight = modelWeights.timeWeight / totalWeight,
            recencyWeight = modelWeights.recencyWeight / totalWeight,
            frequencyWeight = modelWeights.frequencyWeight / totalWeight,
            patternWeight = modelWeights.patternWeight / totalWeight
        )
    }

    private fun makeTestPredictions(): List<Float> {
        // Use last 20% of data for testing
        val testSize = (usageHistory.size * 0.2).toInt().coerceAtLeast(10)
        val testData = usageHistory.takeLast(testSize)

        return testData.map { entry ->
            val context = ContextData(
                hour = entry.hourOfDay,
                dayOfWeek = entry.dayOfWeek,
                location = null,
                isMoving = false,
                isConnectedToWifi = true,
                batteryLevel = 0.8f,
                isCharging = false
            )
            calculateAppScore(entry.appId, context)
        }
    }

    private fun getTestActuals(): List<Float> {
        val testSize = (usageHistory.size * 0.2).toInt().coerceAtLeast(10)
        return List(testSize) { 1f } // Binary: app was used
    }

    /**
     * Calculate usage statistics
     */
    fun calculateUsageStats(): UsageStats {
        val now = System.currentTimeMillis()
        val dayAgo = now - (24 * 60 * 60 * 1000)
        val weekAgo = now - (7 * 24 * 60 * 60 * 1000)

        val dailyUsage = usageHistory.filter { it.timestamp > dayAgo }
        val weeklyUsage = usageHistory.filter { it.timestamp > weekAgo }

        val appCounts = weeklyUsage.groupingBy { it.appId }.eachCount()
        val categoryCounts = weeklyUsage.groupingBy { it.category }.eachCount()

        val topApps = appCounts.toSortedMap(compareByDescending { it.value }).take(10)
        val topCategories = categoryCounts.toSortedMap(compareByDescending { it.value }).take(5)

        return UsageStats(
            averageDailyLaunches = dailyUsage.size.toFloat(),
            weeklyLaunches = weeklyUsage.size,
            uniqueAppsUsed = appCounts.size,
            topApps = topApps.map { AppUsage(it.key, it.value) },
            topCategories = topCategories.map { CategoryUsage(it.key ?: "Unknown", it.value) },
            peakHour = findPeakHour(),
            peakDay = findPeakDay()
        )
    }

    private fun updateUsageStats() {
        _usageStats.value = calculateUsageStats()
    }

    private fun findPeakHour(): Int {
        val hourCounts = usageHistory.groupingBy { it.hourOfDay }.eachCount()
        return hourCounts.maxByOrNull { it.value }?.key ?: 12
    }

    private fun findPeakDay(): Int {
        val dayCounts = usageHistory.groupingBy { it.dayOfWeek }.eachCount()
        return dayCounts.maxByOrNull { it.value }?.key ?: 1
    }

    private fun getFallbackPredictions(count: Int): List<AppPrediction> {
        // Return most frequently used apps
        val appCounts = usageHistory.groupingBy { it.appId }.eachCount()
        val topApps = appCounts.toSortedMap(compareByDescending { it.value }).take(count)

        return topApps.map { (appId, count) ->
            AppPrediction(
                app = getAppById(appId) ?: createUnknownApp(appId),
                confidence = 0.3f,
                predictedHour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY),
                reason = "Frequently used"
            )
        }
    }

    private fun getAppById(appId: String): UnifiedAppInfo? {
        // Would fetch from AllAppsManager
        return null
    }

    private fun createUnknownApp(appId: String): UnifiedAppInfo {
        return UnifiedAppInfo(
            id = appId,
            name = appId,
            packageName = appId,
            isSugarMunchApp = false,
            isSystemApp = false,
            isInstalled = true,
            icon = null
        )
    }

    private fun generatePredictionReason(appId: String, context: ContextData): String {
        val appHistory = usageHistory.filter { it.appId == appId }

        return when {
            appHistory.any { it.hourOfDay == context.hour } -> "Usually used at this time"
            appHistory.any { it.dayOfWeek == context.dayOfWeek } -> "Usually used on this day"
            else -> "Based on your usage patterns"
        }
    }
}

/**
 * Prediction weights for neural network
 */
data class PredictionWeights(
    var timeWeight: Float = 0.4f,
    var recencyWeight: Float = 0.3f,
    var frequencyWeight: Float = 0.2f,
    var patternWeight: Float = 0.1f
)

/**
 * Usage entry
 */
data class UsageEntry(
    val appId: String,
    val appName: String,
    val category: String?,
    val timestamp: Long,
    val hourOfDay: Int,
    val dayOfWeek: Int,
    val launchCount: Int,
    val sessionDuration: Long
)

/**
 * Usage statistics
 */
data class UsageStats(
    val averageDailyLaunches: Float = 0f,
    val weeklyLaunches: Int = 0,
    val uniqueAppsUsed: Int = 0,
    val topApps: List<AppUsage> = emptyList(),
    val topCategories: List<CategoryUsage> = emptyList(),
    val peakHour: Int = 0,
    val peakDay: Int = 0
)

/**
 * App usage
 */
data class AppUsage(
    val appId: String,
    val count: Int
)

/**
 * Category usage
 */
data class CategoryUsage(
    val category: String,
    val count: Int
)

/**
 * Prediction configuration
 */
data class PredictionConfig(
    val historyWindow: Int = 168,
    val minDataPoints: Int = 50,
    val learningRate: Float = 0.01f,
    val confidenceThreshold: Float = 0.5f
)
