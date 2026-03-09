package com.sugarmunch.app.ai

import android.content.Context
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.local.AppDatabase
import com.sugarmunch.app.data.local.toAppEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Calendar

/**
 * Stable fallback recommendation engine.
 *
 * This keeps the recommendation UI functional without relying on the unfinished
 * TensorFlow/analytics pipeline. Recommendations are derived from the local app
 * catalog and simple heuristics such as featured status, category variety, and
 * time-of-day context.
 */
class RecommendationEngine private constructor(
    private val context: Context
) {
    private val database = AppDatabase.getDatabase(context)
    private val appDao = database.appDao()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _cachedRecommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val cachedRecommendations: StateFlow<List<Recommendation>> = _cachedRecommendations.asStateFlow()

    private val _trendingApps = MutableStateFlow<List<TrendingApp>>(emptyList())
    val trendingApps: StateFlow<List<TrendingApp>> = _trendingApps.asStateFlow()

    suspend fun getRecommendations(
        limit: Int = 10,
        contextType: ContextType? = null
    ): List<Recommendation> = withContext(Dispatchers.IO) {
        val apps = loadApps()
        val currentContext = contextType ?: detectCurrentContext()
        val recommendations = scoreForContext(apps, currentContext)
            .take(limit)

        _cachedRecommendations.value = recommendations
        if (_userProfile.value == null) {
            _userProfile.value = buildUserProfile(apps)
        }
        recommendations
    }

    suspend fun getSimilarApps(
        appId: String,
        limit: Int = 5
    ): List<SimilarApp> = withContext(Dispatchers.IO) {
        val apps = loadApps()
        val reference = apps.firstOrNull { it.id == appId } ?: return@withContext emptyList()
        apps.asSequence()
            .filter { it.id != appId }
            .map { app ->
                val categoryScore = if (app.category == reference.category && app.category != null) 1f else 0.4f
                val featuredBoost = if (app.featured == true) 0.1f else 0f
                SimilarApp(
                    app = app,
                    similarityScore = (categoryScore + featuredBoost).coerceAtMost(1f),
                    matchingFeatures = buildList {
                        if (app.category == reference.category && app.category != null) add("Shared category")
                        if (app.featured == true) add("Featured pick")
                    }
                )
            }
            .sortedByDescending { it.similarityScore }
            .take(limit)
            .toList()
    }

    suspend fun getTrendingApps(
        limit: Int = 10,
        timeWindow: Int = 24
    ): List<TrendingApp> = withContext(Dispatchers.IO) {
        val apps = loadApps()
        val trending = apps
            .sortedWith(
                compareByDescending<AppEntry> { it.featured == true }
                    .thenBy { it.sortOrder ?: Int.MAX_VALUE }
                    .thenBy { it.name }
            )
            .take(limit)
            .mapIndexed { index, app ->
                val baseScore = (1f - (index * 0.08f)).coerceAtLeast(0.3f)
                TrendingApp(
                    app = app,
                    installCount = (timeWindow - index).coerceAtLeast(1),
                    trendScore = baseScore,
                    momentum = (baseScore * 0.9f).coerceAtLeast(0.2f)
                )
            }

        _trendingApps.value = trending
        trending
    }

    suspend fun getPersonalizedFeed(): PersonalizedFeed = withContext(Dispatchers.IO) {
        val contextType = detectCurrentContext()
        val recommendations = getRecommendations(limit = 6, contextType = contextType)
        val trending = getTrendingApps(limit = 5)
        val similarSeed = recommendations.firstOrNull()?.app?.id
        val similar = similarSeed?.let { seed ->
            getSimilarApps(seed, 5).map {
                Recommendation(
                    app = it.app,
                    confidence = it.similarityScore,
                    reason = RecommendationReason.Similarity,
                    factors = listOf(RecommendationFactor.FeatureMatch("Shared category"))
                )
            }
        }.orEmpty()
        val discover = loadApps()
            .filter { it.category !in recommendations.mapNotNull { rec -> rec.app.category }.toSet() }
            .take(5)
            .mapIndexed { index, app ->
                Recommendation(
                    app = app,
                    confidence = (0.7f - index * 0.08f).coerceAtLeast(0.3f),
                    reason = RecommendationReason.Discovery,
                    factors = listOf(RecommendationFactor.NewCategory)
                )
            }

        PersonalizedFeed(
            contextHeader = generateContextHeader(contextType),
            sections = listOf(
                FeedSection(
                    type = FeedSectionType.RECOMMENDED_FOR_YOU,
                    title = "Recommended for You",
                    subtitle = "A curated mix from your local catalog",
                    apps = recommendations
                ),
                FeedSection(
                    type = FeedSectionType.BECAUSE_YOU_INSTALLED,
                    title = "Related Picks",
                    subtitle = "Similar categories and featured apps",
                    apps = similar
                ),
                FeedSection(
                    type = FeedSectionType.TRENDING_NOW,
                    title = "Trending Now",
                    subtitle = "Popular and featured catalog highlights",
                    apps = trending.map {
                        Recommendation(
                            app = it.app,
                            confidence = it.trendScore,
                            reason = RecommendationReason.Trending,
                            factors = listOf(RecommendationFactor.TrendingScore(it.trendScore))
                        )
                    }
                ),
                FeedSection(
                    type = FeedSectionType.CONTEXTUAL,
                    title = getContextualTitle(contextType),
                    subtitle = "Adjusted for the current moment",
                    apps = recommendations.map { it.copy(reason = RecommendationReason.Context) }
                ),
                FeedSection(
                    type = FeedSectionType.DISCOVER,
                    title = "Discover Something New",
                    subtitle = "Explore beyond your usual categories",
                    apps = discover
                )
            )
        )
    }

    suspend fun getUserProfile(): UserProfile = withContext(Dispatchers.IO) {
        _userProfile.value ?: buildUserProfile(loadApps()).also { _userProfile.value = it }
    }

    private suspend fun loadApps(): List<AppEntry> {
        return appDao.getAllApps().first().map { it.toAppEntry() }
    }

    private fun buildUserProfile(apps: List<AppEntry>): UserProfile {
        val categoryCounts = apps.mapNotNull { it.category }
            .groupingBy { it }
            .eachCount()
        val total = categoryCounts.values.sum().coerceAtLeast(1)
        return UserProfile(
            preferredCategories = categoryCounts.mapValues { (_, count) -> count.toFloat() / total.toFloat() },
            favoriteCategory = categoryCounts.maxByOrNull { it.value }?.key,
            installedAppCount = apps.size
        )
    }

    private fun scoreForContext(apps: List<AppEntry>, contextType: ContextType): List<Recommendation> {
        return apps.mapIndexed { index, app ->
            val contextScore = calculateContextScore(app, contextType)
            val featuredBoost = if (app.featured == true) 0.15f else 0f
            val orderBoost = if ((app.sortOrder ?: Int.MAX_VALUE) <= 10) 0.1f else 0f
            Recommendation(
                app = app,
                confidence = (contextScore + featuredBoost + orderBoost - index * 0.01f).coerceIn(0.2f, 0.98f),
                reason = when {
                    app.featured == true -> RecommendationReason.Featured
                    contextScore >= 0.75f -> RecommendationReason.Context
                    app.category != null -> RecommendationReason.ContentBased
                    else -> RecommendationReason.Collaborative
                },
                factors = listOf(
                    RecommendationFactor.ContextMatch(contextScore, contextType),
                    RecommendationFactor.TrendingScore((0.8f - index * 0.03f).coerceAtLeast(0.2f))
                )
            )
        }.sortedByDescending { it.confidence }
    }

    private fun detectCurrentContext(): ContextType {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val day = calendar.get(Calendar.DAY_OF_WEEK)
        return when {
            hour in 6..10 -> ContextType.MORNING
            hour in 18..22 -> ContextType.EVENING
            hour >= 23 || hour in 0..5 -> ContextType.LATE_NIGHT
            day == Calendar.SATURDAY || day == Calendar.SUNDAY -> ContextType.WEEKEND
            hour in 14..17 -> ContextType.GAMING
            hour in 9..17 -> ContextType.WORK
            else -> ContextType.GENERAL
        }
    }

    private fun calculateContextScore(app: AppEntry, contextType: ContextType): Float {
        val category = app.category.orEmpty().lowercase()
        val matchingCategories = when (contextType) {
            ContextType.MORNING -> setOf("productivity", "utilities", "news", "tools")
            ContextType.EVENING -> setOf("entertainment", "media", "social", "streaming")
            ContextType.GAMING -> setOf("games", "gaming-tools", "game-mods")
            ContextType.WORK -> setOf("productivity", "office", "tools")
            ContextType.WEEKEND -> setOf("games", "social", "entertainment")
            ContextType.LATE_NIGHT -> setOf("media", "relaxation", "music", "entertainment")
            ContextType.GENERAL -> emptySet()
        }
        return when {
            matchingCategories.isEmpty() -> 0.6f
            category in matchingCategories -> 0.85f
            app.featured == true -> 0.7f
            else -> 0.45f
        }
    }

    private fun generateContextHeader(context: ContextType): String {
        return when (context) {
            ContextType.MORNING -> "Good morning. Here are fresh picks."
            ContextType.EVENING -> "Evening mode: softer, more relaxing suggestions."
            ContextType.GAMING -> "Game on. Fast, bright, and playful picks."
            ContextType.WORK -> "Focus-friendly recommendations for productive sessions."
            ContextType.WEEKEND -> "Weekend discovery mode is active."
            ContextType.LATE_NIGHT -> "Late night browsing with lighter recommendations."
            ContextType.GENERAL -> "Hand-picked catalog suggestions."
        }
    }

    private fun getContextualTitle(context: ContextType): String {
        return when (context) {
            ContextType.MORNING -> "Start Your Day"
            ContextType.EVENING -> "Wind Down"
            ContextType.GAMING -> "Gaming Picks"
            ContextType.WORK -> "Focus Picks"
            ContextType.WEEKEND -> "Weekend Vibes"
            ContextType.LATE_NIGHT -> "Night Mode Picks"
            ContextType.GENERAL -> "Right Now"
        }
    }

    companion object {
        @Volatile
        private var instance: RecommendationEngine? = null

        fun getInstance(context: Context): RecommendationEngine {
            return instance ?: synchronized(this) {
                instance ?: RecommendationEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

data class Recommendation(
    val app: AppEntry,
    val confidence: Float,
    val reason: RecommendationReason,
    val factors: List<RecommendationFactor> = emptyList()
)

data class SimilarApp(
    val app: AppEntry,
    val similarityScore: Float,
    val matchingFeatures: List<String>
)

data class TrendingApp(
    val app: AppEntry,
    val installCount: Int,
    val trendScore: Float,
    val momentum: Float
)

data class PersonalizedFeed(
    val contextHeader: String,
    val sections: List<FeedSection>
)

data class FeedSection(
    val type: FeedSectionType,
    val title: String,
    val subtitle: String,
    val apps: List<Recommendation>
)

enum class FeedSectionType {
    RECOMMENDED_FOR_YOU,
    BECAUSE_YOU_INSTALLED,
    TRENDING_NOW,
    CONTEXTUAL,
    DISCOVER
}

enum class RecommendationReason {
    Collaborative,
    ContentBased,
    Context,
    Trending,
    Similarity,
    Discovery,
    Featured
}

sealed class RecommendationFactor {
    data class ContextMatch(val score: Float, val context: ContextType) : RecommendationFactor()
    data class TrendingScore(val score: Float) : RecommendationFactor()
    data class FeatureMatch(val label: String) : RecommendationFactor()
    object NewCategory : RecommendationFactor()
}

data class UserProfile(
    val preferredCategories: Map<String, Float>,
    val favoriteCategory: String?,
    val installedAppCount: Int
)

enum class ContextType {
    MORNING,
    EVENING,
    GAMING,
    WORK,
    WEEKEND,
    LATE_NIGHT,
    GENERAL
}
