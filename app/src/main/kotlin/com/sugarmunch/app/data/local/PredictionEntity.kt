package com.sugarmunch.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing ML predictions about app installs
 */
@Entity(tableName = "predictions")
data class PredictionEntity(
    @PrimaryKey val appId: String,
    /** Confidence score 0.0 - 1.0 */
    val confidenceScore: Float,
    /** Predicted time of install (timestamp) */
    val predictedInstallTime: Long,
    /** Category of prediction: "time_based", "category_based", "trending", "similarity" */
    val predictionType: String,
    /** Factors that contributed to this prediction */
    val factors: String, // JSON string of prediction factors
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + PREDICTION_TTL_MS
) {
    companion object {
        const val PREDICTION_TTL_MS = 24 * 60 * 60 * 1000 // 24 hours
        
        const val TYPE_TIME_BASED = "time_based"
        const val TYPE_CATEGORY_BASED = "category_based"
        const val TYPE_TRENDING = "trending"
        const val TYPE_SIMILARITY = "similarity"
        const val TYPE_SEASONAL = "seasonal"
    }
}

/**
 * Entity for tracking detailed app usage patterns
 */
@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey val appId: String,
    /** How many times user viewed this app detail page */
    val viewCount: Int = 0,
    /** How many times user initiated download */
    val installCount: Int = 0,
    /** Last time user viewed this app */
    val lastViewedAt: Long? = null,
    /** Last time user installed this app */
    val lastInstalledAt: Long? = null,
    /** Average time spent on detail page (ms) */
    val avgTimeOnPage: Long = 0,
    /** Category of the app */
    val category: String? = null,
    /** Whether this app is currently installed on device */
    val isCurrentlyInstalled: Boolean = false,
    /** User rating (1-5) or null if not rated */
    val userRating: Int? = null,
    /** Whether app is in favorites */
    val isFavorite: Boolean = false,
    /** Total time this app was viewed (ms) */
    val totalViewTime: Long = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Entity for caching app metadata for offline access
 */
@Entity(tableName = "cached_apps")
data class CachedAppEntity(
    @PrimaryKey val appId: String,
    val name: String,
    val packageName: String,
    val description: String,
    val iconUrl: String?,
    val downloadUrl: String,
    val version: String,
    val source: String?,
    val category: String?,
    val accentColor: String?,
    val badge: String?,
    val featured: Boolean?,
    val sortOrder: Int?,
    /** Whether the full APK is cached locally */
    val isApkCached: Boolean = false,
    /** Local path to cached APK */
    val cachedApkPath: String? = null,
    /** Size of cached APK in bytes */
    val cachedApkSize: Long = 0,
    /** Cache priority score (higher = keep longer) */
    val cachePriority: Float = 0f,
    /** When this app was cached */
    val cachedAt: Long = System.currentTimeMillis(),
    /** When this cache entry was last accessed */
    val lastAccessedAt: Long = System.currentTimeMillis(),
    /** Number of times this cached app was accessed */
    val accessCount: Int = 0,
    /** Whether this is a predictive cache (pre-downloaded before user asked) */
    val isPredictiveCache: Boolean = false,
    /** Prediction confidence that led to caching */
    val predictionConfidence: Float = 0f
)

/**
 * Entity for time-series install data
 */
@Entity(tableName = "install_history")
data class InstallHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val appId: String,
    val appName: String,
    val category: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val dayOfWeek: Int, // 1 = Sunday, 7 = Saturday
    val hourOfDay: Int, // 0-23
    val installSource: String = "manual", // "manual", "predictive_cache", "background_sync"
    val success: Boolean = true
)

/**
 * Entity for sync state management
 */
@Entity(tableName = "sync_state")
data class SyncStateEntity(
    @PrimaryKey val id: Int = 1,
    val lastSuccessfulSync: Long? = null,
    val lastAttemptedSync: Long? = null,
    val syncAttemptCount: Int = 0,
    val lastSyncError: String? = null,
    val pendingChangesCount: Int = 0,
    val isOfflineMode: Boolean = false,
    val etag: String? = null,
    val serverTimestamp: Long? = null
)
