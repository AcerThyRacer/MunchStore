package com.sugarmunch.app.ai

import android.content.Context
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.*

/**
 * UsagePredictor - Time-series analysis and ML-based prediction engine
 *
 * Predicts user behavior patterns including:
 * - When user is likely to install apps (time of day, day of week)
 * - Which categories user prefers
 * - Which specific apps user might install next
 *
 * ## Hilt Migration Note
 * This class has been migrated from a singleton pattern to Hilt dependency injection.
 * Inject this class using:
 * ```
 * @Inject lateinit var usagePredictor: UsagePredictor
 * ```
 * In a Hilt module, provide it with [@ApplicationContext](file:///home/ace/Downloads/SugarMunch/app/src/main/kotlin/com/sugarmunch/app/ui/theme/Color.kt#L5-L5):
 * ```
 * @Provides
 * fun provideUsagePredictor(@ApplicationContext context: Context): UsagePredictor {
 *     return UsagePredictor(context)
 * }
 * ```
 */
class UsagePredictor constructor(
    private val context: Context
) {
    private val database = AppDatabase.getDatabase(context)
    private val usageDao = database.appUsageDao()
    private val installHistoryDao = database.installHistoryDao()
    private val appDao = database.appDao()
    
    private val scope = kotlinx.coroutines.CoroutineScope(Dispatchers.IO)

    // ═════════════════════════════════════════════════════════════
    // TIME PREDICTION
    // ═════════════════════════════════════════════════════════════

    /**
     * Predicts the optimal time for pre-loading content based on user's
     * historical install patterns.
     * 
     * @return Predicted timestamp for next likely install session
     */
    suspend fun predictNextInstallTime(): Long = withContext(Dispatchers.IO) {
        val patterns = installHistoryDao.getInstallPatternsByHour(
            since = System.currentTimeMillis() - ANALYSIS_WINDOW_MS
        )
        
        if (patterns.isEmpty()) {
            // Default: predict next hour if no data
            return@withContext System.currentTimeMillis() + DEFAULT_PREDICTION_HORIZON_MS
        }

        // Find peak hours
        val peakHours = patterns.sortedByDescending { it.count }.take(3).map { it.hourOfDay }
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // Find next peak hour
        val nextPeakHour = peakHours
            .sorted()
            .firstOrNull { it > currentHour } ?: peakHours.firstOrNull() ?: currentHour + 1
        
        val calendar = Calendar.getInstance()
        if (nextPeakHour <= currentHour) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        calendar.set(Calendar.HOUR_OF_DAY, nextPeakHour)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        
        calendar.timeInMillis
    }

    /**
     * Determines if content should be preloaded now based on current context
     * and predicted usage patterns.
     * 
     * @param confidenceThreshold Minimum confidence level (0.0 - 1.0)
     * @return true if preloading is recommended
     */
    suspend fun shouldPreload(confidenceThreshold: Float = 0.6f): Boolean = withContext(Dispatchers.IO) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        
        // Get install patterns
        val hourlyPatterns = installHistoryDao.getInstallPatternsByHour(
            since = System.currentTimeMillis() - ANALYSIS_WINDOW_MS
        )
        val dailyPatterns = installHistoryDao.getInstallPatternsByDay(
            since = System.currentTimeMillis() - ANALYSIS_WINDOW_MS
        )
        
        if (hourlyPatterns.isEmpty() || dailyPatterns.isEmpty()) {
            return@withContext false
        }

        // Calculate confidence score
        val totalInstalls = hourlyPatterns.sumOf { it.count.toDouble() }
        val currentHourLikelihood = hourlyPatterns
            .find { it.hourOfDay == currentHour }
            ?.let { it.count / totalInstalls }
            ?: 0.0
            
        val totalDailyInstalls = dailyPatterns.sumOf { it.count.toDouble() }
        val currentDayLikelihood = dailyPatterns
            .find { it.dayOfWeek == currentDay }
            ?.let { it.count / totalDailyInstalls }
            ?: 0.0
        
        // Combined score with time decay (higher confidence closer to predicted time)
        val confidence = (currentHourLikelihood * 0.6 + currentDayLikelihood * 0.4).toFloat()
        
        confidence >= confidenceThreshold
    }

    /**
     * Get the current time window's likelihood score for installs
     */
    suspend fun getCurrentTimeWindowScore(): Float = withContext(Dispatchers.IO) {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        val patterns = installHistoryDao.getInstallPatternsByHour(
            since = System.currentTimeMillis() - ANALYSIS_WINDOW_MS
        )
        
        if (patterns.isEmpty()) return@withContext 0.5f
        
        val total = patterns.sumOf { it.count }
        val current = patterns.find { it.hourOfDay == currentHour }?.count ?: 0
        
        (current.toFloat() / total).coerceIn(0f, 1f)
    }

    // ═════════════════════════════════════════════════════════════
    // APP RECOMMENDATIONS
    // ═════════════════════════════════════════════════════════════

    /**
     * Get recommended apps based on user's install history and preferences
     * 
     * @param limit Maximum number of recommendations
     * @return List of app IDs with confidence scores
     */
    suspend fun getRecommendedApps(limit: Int = 10): List<AppRecommendation> = withContext(Dispatchers.IO) {
        val recommendations = mutableListOf<AppRecommendation>()
        
        // Get user's favorite categories
        val categoryScores = calculateCategoryAffinity()
        
        // Get recently viewed but not installed apps
        val viewedApps = usageDao.getMostViewedApps(limit * 2)
            .filter { it.installCount == 0 && !it.isCurrentlyInstalled }
        
        // Score each app
        viewedApps.forEach { usage ->
            val categoryScore = categoryScores[usage.category] ?: 0f
            val viewScore = calculateViewScore(usage)
            val timeScore = calculateRecencyScore(usage.lastViewedAt)
            
            val totalScore = (categoryScore * 0.4f + viewScore * 0.35f + timeScore * 0.25f)
                .coerceIn(0f, 1f)
            
            if (totalScore > 0.3f) {
                recommendations.add(
                    AppRecommendation(
                        appId = usage.appId,
                        confidence = totalScore,
                        factors = listOf(
                            RecommendationFactor.CategoryAffinity(categoryScore),
                            RecommendationFactor.ViewPattern(viewScore),
                            RecommendationFactor.Recency(timeScore)
                        )
                    )
                )
            }
        }
        
        recommendations
            .sortedByDescending { it.confidence }
            .take(limit)
    }

    /**
     * Predict which apps user is most likely to install next
     * based on comprehensive behavior analysis
     */
    suspend fun predictNextInstalls(
        availableApps: List<AppEntry>,
        limit: Int = 5
    ): List<PredictionResult> = withContext(Dispatchers.IO) {
        val predictions = mutableListOf<PredictionResult>()
        
        // Get user patterns
        val categoryAffinity = calculateCategoryAffinity()
        val installedApps = usageDao.getMostInstalledApps(50)
        val viewedApps = usageDao.getMostViewedApps(50)
        val favoriteApps = usageDao.getFavoriteApps()
        
        // Calculate time context
        val timeContext = getTimeContext()
        
        availableApps.forEach { app ->
            var score = 0f
            val factors = mutableListOf<PredictionFactor>()
            
            // Category affinity score
            val catScore = categoryAffinity[app.category] ?: 0f
            if (catScore > 0) {
                score += catScore * CATEGORY_WEIGHT
                factors.add(PredictionFactor.CategoryMatch(catScore))
            }
            
            // Already installed check (negative factor)
            val isInstalled = installedApps.any { it.appId == app.id && it.isCurrentlyInstalled }
            if (isInstalled) {
                score -= 0.5f
                factors.add(PredictionFactor.AlreadyInstalled)
            }
            
            // Previously viewed but not installed (strong signal)
            val viewed = viewedApps.find { it.appId == app.id }
            if (viewed != null && viewed.installCount == 0) {
                val viewScore = (viewed.viewCount / 10f).coerceAtMost(0.3f)
                score += viewScore
                factors.add(PredictionFactor.PreviouslyViewed(viewScore))
            }
            
            // Favorite status
            if (favoriteApps.any { it.appId == app.id }) {
                score += FAVORITE_BONUS
                factors.add(PredictionFactor.InFavorites)
            }
            
            // Featured app bonus
            if (app.featured == true) {
                score += FEATURED_BONUS
                factors.add(PredictionFactor.FeaturedApp)
            }
            
            // Time context match
            val timeScore = calculateTimeContextMatch(app.category, timeContext)
            if (timeScore > 0) {
                score += timeScore * TIME_CONTEXT_WEIGHT
                factors.add(PredictionFactor.TimeContextMatch(timeScore))
            }
            
            // Similarity to installed apps
            val similarityScore = calculateSimilarityScore(app, installedApps)
            if (similarityScore > 0) {
                score += similarityScore * SIMILARITY_WEIGHT
                factors.add(PredictionFactor.AppSimilarity(similarityScore))
            }
            
            // Normalize score
            score = score.coerceIn(0f, 1f)
            
            if (score > PREDICTION_THRESHOLD) {
                predictions.add(
                    PredictionResult(
                        app = app,
                        confidence = score,
                        predictedInstallTime = estimateInstallTime(score, timeContext),
                        factors = factors
                    )
                )
            }
        }
        
        predictions
            .sortedByDescending { it.confidence }
            .take(limit)
    }

    /**
     * Get similarity-based recommendations (apps similar to what user likes)
     */
    suspend fun getSimilarityRecommendations(
        referenceAppId: String,
        availableApps: List<AppEntry>,
        limit: Int = 5
    ): List<AppEntry> = withContext(Dispatchers.IO) {
        val referenceApp = availableApps.find { it.id == referenceAppId } ?: return@withContext emptyList()
        val referenceCategory = referenceApp.category
        
        availableApps
            .filter { it.id != referenceAppId && it.category == referenceCategory }
            .sortedByDescending { it.featured == true }
            .take(limit)
    }

    // ═════════════════════════════════════════════════════════════
    // SCORING HELPERS
    // ═════════════════════════════════════════════════════════════

    private suspend fun calculateCategoryAffinity(): Map<String?, Float> {
        val categoryInstalls = installHistoryDao.getInstallPatternsByCategory(
            since = System.currentTimeMillis() - ANALYSIS_WINDOW_MS
        )
        
        if (categoryInstalls.isEmpty()) return emptyMap()
        
        val total = categoryInstalls.sumOf { it.count.toFloat() }
        return categoryInstalls.associate { 
            it.category to (it.count / total).coerceIn(0f, 1f)
        }
    }

    private fun calculateViewScore(usage: AppUsageEntity): Float {
        val viewWeight = (usage.viewCount * 0.1f).coerceAtMost(0.5f)
        val timeWeight = (usage.totalViewTime / 60000f).coerceAtMost(0.3f) // Per minute
        return (viewWeight + timeWeight).coerceIn(0f, 1f)
    }

    private fun calculateRecencyScore(lastViewedAt: Long?): Float {
        if (lastViewedAt == null) return 0f
        
        val daysSince = (System.currentTimeMillis() - lastViewedAt) / (24 * 60 * 60 * 1000f)
        return exp(-daysSince / 7f).coerceIn(0f, 1f) // Exponential decay over a week
    }

    private suspend fun calculateTimeContextMatch(category: String?, context: TimeContext): Float {
        // Check if this category is commonly installed in current time context
        val recentInstalls = installHistoryDao.getInstallsSince(
            System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000
        )
        
        val matchingInstalls = recentInstalls.count { 
            it.category == category && 
            it.hourOfDay == context.hour &&
            it.dayOfWeek == context.dayOfWeek
        }
        
        return (matchingInstalls / 5f).coerceAtMost(1f)
    }

    private suspend fun calculateSimilarityScore(
        app: AppEntry, 
        installedApps: List<AppUsageEntity>
    ): Float {
        // Simple category-based similarity
        val sameCategory = installedApps.count { it.category == app.category }
        return (sameCategory / 10f).coerceAtMost(0.5f)
    }

    private fun getTimeContext(): TimeContext {
        val calendar = Calendar.getInstance()
        return TimeContext(
            hour = calendar.get(Calendar.HOUR_OF_DAY),
            dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
            isWeekend = calendar.get(Calendar.DAY_OF_WEEK) in listOf(Calendar.SATURDAY, Calendar.SUNDAY),
            isEvening = calendar.get(Calendar.HOUR_OF_DAY) >= 18
        )
    }

    private fun estimateInstallTime(confidence: Float, context: TimeContext): Long {
        // Higher confidence = sooner install
        val baseDelay = when {
            confidence > 0.8 -> 30 * 60 * 1000 // 30 minutes
            confidence > 0.6 -> 2 * 60 * 60 * 1000 // 2 hours
            confidence > 0.4 -> 6 * 60 * 60 * 1000 // 6 hours
            else -> 24 * 60 * 60 * 1000 // 24 hours
        }
        
        return System.currentTimeMillis() + baseDelay
    }

    // ═════════════════════════════════════════════════════════════
    // ANALYTICS & INSIGHTS
    // ═════════════════════════════════════════════════════════════

    /**
     * Get user's install pattern insights
     */
    suspend fun getUserPatternInsights(): UserPatternInsights = withContext(Dispatchers.IO) {
        val since = System.currentTimeMillis() - ANALYSIS_WINDOW_MS
        
        val hourlyPatterns = installHistoryDao.getInstallPatternsByHour(since)
        val dailyPatterns = installHistoryDao.getInstallPatternsByDay(since)
        val categoryPatterns = installHistoryDao.getInstallPatternsByCategory(since)
        
        val peakHour = hourlyPatterns.maxByOrNull { it.count }?.hourOfDay
        val peakDay = dailyPatterns.maxByOrNull { it.count }?.dayOfWeek
        val topCategory = categoryPatterns.maxByOrNull { it.count }?.category
        
        UserPatternInsights(
            peakInstallHour = peakHour,
            peakInstallDay = peakDay?.let { dayNumberToName(it) },
            favoriteCategory = topCategory,
            totalInstallsLastMonth = hourlyPatterns.sumOf { it.count },
            installConsistency = calculateInstallConsistency(hourlyPatterns)
        )
    }

    private fun calculateInstallConsistency(patterns: List<HourlyPattern>): Float {
        if (patterns.isEmpty()) return 0f
        val counts = patterns.map { it.count }
        val mean = counts.average()
        val variance = counts.map { (it - mean).pow(2) }.average()
        val stdDev = sqrt(variance)
        return (1 - (stdDev / (mean + 1))).toFloat().coerceIn(0f, 1f)
    }

    private fun dayNumberToName(day: Int): String = when (day) {
        Calendar.SUNDAY -> "Sunday"
        Calendar.MONDAY -> "Monday"
        Calendar.TUESDAY -> "Tuesday"
        Calendar.WEDNESDAY -> "Wednesday"
        Calendar.THURSDAY -> "Thursday"
        Calendar.FRIDAY -> "Friday"
        Calendar.SATURDAY -> "Saturday"
        else -> "Unknown"
    }

    // ═════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═════════════════════════════════════════════════════════════

    companion object {
        private const val ANALYSIS_WINDOW_MS = 30L * 24 * 60 * 60 * 1000 // 30 days
        private const val DEFAULT_PREDICTION_HORIZON_MS = 60 * 60 * 1000 // 1 hour

        // Weights for prediction scoring
        private const val CATEGORY_WEIGHT = 0.35f
        private const val TIME_CONTEXT_WEIGHT = 0.15f
        private const val SIMILARITY_WEIGHT = 0.20f
        private const val FAVORITE_BONUS = 0.15f
        private const val FEATURED_BONUS = 0.05f

        private const val PREDICTION_THRESHOLD = 0.25f
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

data class AppRecommendation(
    val appId: String,
    val confidence: Float,
    val factors: List<RecommendationFactor>
)

sealed class RecommendationFactor {
    abstract val score: Float
    
    data class CategoryAffinity(override val score: Float) : RecommendationFactor()
    data class ViewPattern(override val score: Float) : RecommendationFactor()
    data class Recency(override val score: Float) : RecommendationFactor()
    data class Similarity(override val score: Float) : RecommendationFactor()
    data class Trending(override val score: Float) : RecommendationFactor()
}

data class PredictionResult(
    val app: AppEntry,
    val confidence: Float,
    val predictedInstallTime: Long,
    val factors: List<PredictionFactor>
)

sealed class PredictionFactor {
    data class CategoryMatch(val score: Float) : PredictionFactor()
    data class PreviouslyViewed(val score: Float) : PredictionFactor()
    data class TimeContextMatch(val score: Float) : PredictionFactor()
    data class AppSimilarity(val score: Float) : PredictionFactor()
    object AlreadyInstalled : PredictionFactor()
    object InFavorites : PredictionFactor()
    object FeaturedApp : PredictionFactor()
}

data class TimeContext(
    val hour: Int,
    val dayOfWeek: Int,
    val isWeekend: Boolean,
    val isEvening: Boolean
)

data class UserPatternInsights(
    val peakInstallHour: Int?,
    val peakInstallDay: String?,
    val favoriteCategory: String?,
    val totalInstallsLastMonth: Int,
    val installConsistency: Float // 0.0 - 1.0, higher = more consistent patterns
)
