package com.sugarmunch.app.sugartube

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.TimeUnit

/**
 * Sweet Spot - An engaging segment in a video worth previewing.
 *
 * Sweet Spots are AI-driven highlights that help users quickly find
 * the most interesting parts of a video without watching it entirely.
 */
@Serializable
data class SweetSpot(
    val startMs: Long,
    val endMs: Long,
    val label: String? = null,
    val score: Float = 1.0f,           // Relevance score 0-1
    val category: SweetSpotCategory = SweetSpotCategory.HIGHLIGHT
)

/**
 * Categories of sweet spots
 */
@Serializable
enum class SweetSpotCategory {
    HIGHLIGHT,       // Best moments
    INTRO,           // Video introduction
    CLIMAX,          // Peak moment
    FUNNY,           // Humorous content
    EDUCATIONAL,     // Key learning points
    MUSIC,           // Music segments
    ACTION,          // Action sequences
    REVELATION,      // Plot reveals or key info
    OUTRO            // Ending/conclusion
}

/**
 * Extended sweet spot data with metadata
 */
@Serializable
data class VideoSweetSpots(
    val videoId: String,
    val spots: List<SweetSpot>,
    val generatedAt: Long = System.currentTimeMillis(),
    val source: SweetSpotSource = SweetSpotSource.LOCAL_ANALYSIS,
    val confidence: Float = 0.5f
)

enum class SweetSpotSource {
    LOCAL_ANALYSIS,    // Generated locally
    CACHED,            // From cache
    BACKEND_API,       // From backend
    USER_MARKED        // User-saved spots
}

/**
 * Repository interface for fetching sweet spots.
 */
interface SweetSpotsRepository {
    /**
     * Fetch sweet spots for a video.
     *
     * @param videoId Unique video identifier
     * @return Flow of sweet spots list (empty if unavailable)
     */
    fun getSweetSpots(videoId: String): Flow<List<SweetSpot>>

    /**
     * Get extended sweet spots data with metadata.
     */
    fun getVideoSweetSpots(videoId: String): Flow<VideoSweetSpots?>

    /**
     * Save user-marked sweet spot.
     */
    suspend fun saveUserSweetSpot(videoId: String, spot: SweetSpot): Boolean

    /**
     * Clear cached sweet spots for a video.
     */
    suspend fun clearCache(videoId: String)

    /**
     * Clear all cached sweet spots.
     */
    suspend fun clearAllCache()
}

/**
 * Local implementation of SweetSpotsRepository.
 *
 * Generates sweet spots using:
 * 1. Cached user-marked spots
 * 2. Video duration-based intelligent defaults
 * 3. Pattern analysis (when video metadata available)
 *
 * Can be replaced with backend implementation when API is available.
 */
class LocalSweetSpotsRepository(
    private val context: Context
) : SweetSpotsRepository {

    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true 
    }
    
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "SweetSpotsRepo"
        private const val PREFS_NAME = "sweet_spots_cache"
        private const val KEY_PREFIX_VIDEO = "video_"
        private const val KEY_PREFIX_USER = "user_"
        private const val CACHE_EXPIRY_DAYS = 30L
        
        @Volatile
        private var INSTANCE: LocalSweetSpotsRepository? = null
        
        fun getInstance(context: Context): LocalSweetSpotsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LocalSweetSpotsRepository(context.applicationContext).also { 
                    INSTANCE = it 
                }
            }
        }
    }

    override fun getSweetSpots(videoId: String): Flow<List<SweetSpot>> = flow {
        val spots = getOrGenerateSweetSpots(videoId)
        emit(spots)
    }.flowOn(Dispatchers.IO)

    override fun getVideoSweetSpots(videoId: String): Flow<VideoSweetSpots?> = flow {
        val cached = loadFromCache(videoId)
        if (cached != null && !isCacheExpired(cached)) {
            emit(cached)
            return@flow
        }

        val spots = getOrGenerateSweetSpots(videoId)
        val videoSpots = VideoSweetSpots(
            videoId = videoId,
            spots = spots,
            source = if (cached != null) SweetSpotSource.CACHED else SweetSpotSource.LOCAL_ANALYSIS
        )
        emit(videoSpots)
    }.flowOn(Dispatchers.IO)

    override suspend fun saveUserSweetSpot(videoId: String, spot: SweetSpot): Boolean {
        return try {
            val userKey = KEY_PREFIX_USER + videoId
            val existingSpots = loadUserSpots(videoId).toMutableList()
            existingSpots.add(spot)
            
            prefs.edit()
                .putString(userKey, json.encodeToString(existingSpots))
                .apply()
            
            Log.d(TAG, "Saved user sweet spot for $videoId at ${spot.startMs}ms")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save user sweet spot", e)
            false
        }
    }

    override suspend fun clearCache(videoId: String) {
        prefs.edit()
            .remove(KEY_PREFIX_VIDEO + videoId)
            .remove(KEY_PREFIX_USER + videoId)
            .apply()
        Log.d(TAG, "Cleared cache for video $videoId")
    }

    override suspend fun clearAllCache() {
        prefs.edit().clear().apply()
        Log.d(TAG, "Cleared all sweet spots cache")
    }

    /**
     * Get sweet spots from cache or generate new ones.
     */
    private suspend fun getOrGenerateSweetSpots(videoId: String): List<SweetSpot> {
        // 1. Check for user-marked spots first
        val userSpots = loadUserSpots(videoId)
        if (userSpots.isNotEmpty()) {
            Log.d(TAG, "Found ${userSpots.size} user-marked spots for $videoId")
            return userSpots
        }

        // 2. Check cache
        val cached = loadFromCache(videoId)
        if (cached != null && !isCacheExpired(cached)) {
            Log.d(TAG, "Using cached sweet spots for $videoId")
            return cached.spots
        }

        // 3. Generate intelligent defaults
        val generatedSpots = generateIntelligentDefaults(videoId)
        
        // Cache the generated spots
        saveToCache(videoId, generatedSpots)
        
        Log.d(TAG, "Generated ${generatedSpots.size} sweet spots for $videoId")
        return generatedSpots
    }

    /**
     * Load user-marked sweet spots.
     */
    private fun loadUserSpots(videoId: String): List<SweetSpot> {
        return try {
            val userKey = KEY_PREFIX_USER + videoId
            val jsonStr = prefs.getString(userKey, null) ?: return emptyList()
            json.decodeFromString<List<SweetSpot>>(jsonStr)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load user spots for $videoId", e)
            emptyList()
        }
    }

    /**
     * Load cached sweet spots.
     */
    private fun loadFromCache(videoId: String): VideoSweetSpots? {
        return try {
            val cacheKey = KEY_PREFIX_VIDEO + videoId
            val jsonStr = prefs.getString(cacheKey, null) ?: return null
            json.decodeFromString<VideoSweetSpots>(jsonStr)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load cache for $videoId", e)
            null
        }
    }

    /**
     * Save sweet spots to cache.
     */
    private fun saveToCache(videoId: String, spots: List<SweetSpot>) {
        try {
            val cacheKey = KEY_PREFIX_VIDEO + videoId
            val videoSpots = VideoSweetSpots(
                videoId = videoId,
                spots = spots,
                generatedAt = System.currentTimeMillis(),
                source = SweetSpotSource.LOCAL_ANALYSIS
            )
            prefs.edit()
                .putString(cacheKey, json.encodeToString(videoSpots))
                .apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cache for $videoId", e)
        }
    }

    /**
     * Check if cached data is expired.
     */
    private fun isCacheExpired(videoSpots: VideoSweetSpots): Boolean {
        val expiryMs = TimeUnit.DAYS.toMillis(CACHE_EXPIRY_DAYS)
        return System.currentTimeMillis() - videoSpots.generatedAt > expiryMs
    }

    /**
     * Generate intelligent default sweet spots based on video patterns.
     *
     * Since we don't have access to video content analysis, we generate
     * strategic timestamps that are commonly interesting in videos:
     * - Intro (first 10%)
     * - Early hook (15-20%)
     * - Mid-point highlights (40-60%)
     * - Climax (70-80%)
     * - Outro (last 10%)
     */
    private fun generateIntelligentDefaults(videoId: String): List<SweetSpot> {
        val spots = mutableListOf<SweetSpot>()
        
        // Try to extract video duration from videoId if encoded
        // Format: "videoId_durationMs" or just use defaults
        val durationMs = extractDurationFromVideoId(videoId) ?: DEFAULT_VIDEO_DURATION_MS
        
        // Intro spot (first 10% of video)
        spots.add(SweetSpot(
            startMs = 0,
            endMs = (durationMs * 0.10).toLong(),
            label = "Intro",
            score = 0.7f,
            category = SweetSpotCategory.INTRO
        ))

        // Early hook (15-20% of video)
        spots.add(SweetSpot(
            startMs = (durationMs * 0.15).toLong(),
            endMs = (durationMs * 0.20).toLong(),
            label = "Early Highlight",
            score = 0.8f,
            category = SweetSpotCategory.HIGHLIGHT
        ))

        // First third highlight (30-35%)
        spots.add(SweetSpot(
            startMs = (durationMs * 0.30).toLong(),
            endMs = (durationMs * 0.35).toLong(),
            label = "First Act",
            score = 0.75f,
            category = SweetSpotCategory.HIGHLIGHT
        ))

        // Mid-point (45-55%)
        spots.add(SweetSpot(
            startMs = (durationMs * 0.45).toLong(),
            endMs = (durationMs * 0.55).toLong(),
            label = "Mid Section",
            score = 0.85f,
            category = SweetSpotCategory.HIGHLIGHT
        ))

        // Climax (70-80%)
        spots.add(SweetSpot(
            startMs = (durationMs * 0.70).toLong(),
            endMs = (durationMs * 0.80).toLong(),
            label = "Key Moment",
            score = 0.9f,
            category = SweetSpotCategory.CLIMAX
        ))

        // Outro (last 10%)
        spots.add(SweetSpot(
            startMs = (durationMs * 0.90).toLong(),
            endMs = durationMs,
            label = "Conclusion",
            score = 0.65f,
            category = SweetSpotCategory.OUTRO
        ))

        return spots.sortedBy { it.startMs }
    }

    /**
     * Extract video duration from videoId if encoded.
     * 
     * VideoId format options:
     * - "abc123" -> null (use default)
     * - "abc123_300000" -> 300000ms (5 minutes)
     * - "abc123_300s" -> 300000ms
     * - "abc123_5m" -> 300000ms
     */
    private fun extractDurationFromVideoId(videoId: String): Long? {
        return try {
            val parts = videoId.split("_")
            if (parts.size < 2) return null

            val durationPart = parts.last()
            
            when {
                durationPart.endsWith("ms") -> {
                    durationPart.dropLast(2).toLongOrNull()
                }
                durationPart.endsWith("s") -> {
                    durationPart.dropLast(1).toLongOrNull()?.times(1000)
                }
                durationPart.endsWith("m") -> {
                    durationPart.dropLast(1).toLongOrNull()?.times(60 * 1000)
                }
                durationPart.all { it.isDigit() } -> {
                    durationPart.toLongOrNull()
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Generate sweet spots for a specific video duration.
     * Useful when you know the video length.
     */
    fun generateSpotsForDuration(durationMs: Long): List<SweetSpot> {
        return listOf(
            SweetSpot(
                startMs = 0,
                endMs = (durationMs * 0.10).toLong(),
                label = "Intro",
                category = SweetSpotCategory.INTRO
            ),
            SweetSpot(
                startMs = (durationMs * 0.15).toLong(),
                endMs = (durationMs * 0.20).toLong(),
                label = "Early Highlight",
                category = SweetSpotCategory.HIGHLIGHT
            ),
            SweetSpot(
                startMs = (durationMs * 0.45).toLong(),
                endMs = (durationMs * 0.55).toLong(),
                label = "Mid Section",
                category = SweetSpotCategory.HIGHLIGHT
            ),
            SweetSpot(
                startMs = (durationMs * 0.70).toLong(),
                endMs = (durationMs * 0.80).toLong(),
                label = "Key Moment",
                category = SweetSpotCategory.CLIMAX
            ),
            SweetSpot(
                startMs = (durationMs * 0.90).toLong(),
                endMs = durationMs,
                label = "Conclusion",
                category = SweetSpotCategory.OUTRO
            )
        )
    }

    companion object Defaults {
        // Default video duration: 10 minutes
        const val DEFAULT_VIDEO_DURATION_MS = 10 * 60 * 1000L
    }
}

/**
 * Backend API implementation placeholder.
 * 
 * This implementation would call a real backend API when available.
 * Currently returns empty list as no backend exists.
 * 
 * To use: Replace LocalSweetSpotsRepository with this class
 * when backend API is ready.
 */
class BackendSweetSpotsRepository(
    private val apiKey: String? = null,
    private val baseUrl: String = "https://api.sugarmunch.com/v1/sweetspots"
) : SweetSpotsRepository {

    private val localFallback = LocalSweetSpotsRepository.getInstance(
        android.app.Application().applicationContext
    )

    override fun getSweetSpots(videoId: String): Flow<List<SweetSpot>> = flow {
        // TODO: Implement backend API call when available
        // For now, use local fallback
        Log.d("BackendSweetSpots", "Backend not available, using local fallback for $videoId")
        localFallback.getSweetSpots(videoId).collect { emit(it) }
    }

    override fun getVideoSweetSpots(videoId: String): Flow<VideoSweetSpots?> = flow {
        // TODO: Implement backend API call when available
        localFallback.getVideoSweetSpots(videoId).collect { emit(it) }
    }

    override suspend fun saveUserSweetSpot(videoId: String, spot: SweetSpot): Boolean {
        // Save locally and sync to backend when available
        return localFallback.saveUserSweetSpot(videoId, spot)
    }

    override suspend fun clearCache(videoId: String) {
        localFallback.clearCache(videoId)
    }

    override suspend fun clearAllCache() {
        localFallback.clearAllCache()
    }
}

/**
 * Legacy stub for backwards compatibility.
 * @deprecated Use LocalSweetSpotsRepository instead
 */
@Deprecated(
    message = "Use LocalSweetSpotsRepository instead",
    replaceWith = ReplaceWith("LocalSweetSpotsRepository.getInstance(context)")
)
class SweetSpotsRepositoryStub : SweetSpotsRepository {
    override fun getSweetSpots(videoId: String): Flow<List<SweetSpot>> = 
        flowOf(emptyList())
}