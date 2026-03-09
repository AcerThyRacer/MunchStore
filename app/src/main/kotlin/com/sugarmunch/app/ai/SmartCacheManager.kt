package com.sugarmunch.app.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.StatFs
import com.sugarmunch.app.data.AppEntry
import com.sugarmunch.app.data.local.*
import com.sugarmunch.app.download.SmartDownloadManager
import com.sugarmunch.app.util.SecureLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.*

/**
 * SmartCacheManager - AI-powered predictive caching system
 *
 * Features:
 * - Predictive caching based on ML models
 * - Smart eviction policy (LRU + frequency + predicted value)
 * - Cache warming based on time patterns
 * - Battery and storage aware operations
 * - Offline-first architecture
 * - Hit rate tracking for cache effectiveness
 *
 * ## Hilt Migration Note
 * This class has been migrated from a singleton pattern to Hilt dependency injection.
 * Inject this class using:
 * ```
 * @Inject lateinit var smartCacheManager: SmartCacheManager
 * ```
 * In a Hilt module, provide it with [@ApplicationContext](file:///home/ace/Downloads/SugarMunch/app/src/main/kotlin/com/sugarmunch/app/ui/theme/Color.kt#L5-L5):
 * ```
 * @Provides
 * fun provideSmartCacheManager(@ApplicationContext context: Context): SmartCacheManager {
 *     return SmartCacheManager(context)
 * }
 * ```
 */
class SmartCacheManager constructor(
    private val context: Context,
    private val predictor: UsagePredictor,
    private val downloadManager: SmartDownloadManager
) {
    private val logger = SecureLogger.create("SmartCacheManager")
    private val database = AppDatabase.getDatabase(context)
    private val cachedAppDao = database.cachedAppDao()
    private val predictionDao = database.predictionDao()
    private val usageDao = database.appUsageDao()
    private val installHistoryDao = database.installHistoryDao()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Cache configuration
    private val config = CacheConfig()

    // State flows
    private val _cacheState = MutableStateFlow(CacheState())
    val cacheState: StateFlow<CacheState> = _cacheState.asStateFlow()

    private val _activePredictions = MutableStateFlow<List<PredictionEntity>>(emptyList())
    val activePredictions: StateFlow<List<PredictionEntity>> = _activePredictions.asStateFlow()

    // ═════════════════════════════════════════════════════════════
    // CACHE WARMING STATE TRACKING
    // ═════════════════════════════════════════════════════════════

    /**
     * Tracks the current cache warming operation state.
     */
    private val _cacheWarmingState = MutableStateFlow<CacheWarmingState>(CacheWarmingState.Idle)
    val cacheWarmingState: StateFlow<CacheWarmingState> = _cacheWarmingState.asStateFlow()

    /**
     * Cache warming operation details.
     */
    private var currentWarmingOperation: CacheWarmingOperation? = null

    // ═════════════════════════════════════════════════════════════
    // HIT RATE TRACKING
    // ═════════════════════════════════════════════════════════════

    /**
     * Tracks cache hits and misses for hit rate calculation.
     * Uses a sliding window of recent accesses.
     */
    private val cacheAccessTracker = ConcurrentHashMap<String, CacheAccessRecord>()
    private val hitRateWindow = object {
        private val windowSize = 100
        private val accesses = ArrayDeque<Boolean>(windowSize)
        
        fun recordHit(hit: Boolean) {
            if (accesses.size >= windowSize) {
                accesses.removeFirst()
            }
            accesses.addLast(hit)
        }
        
        fun getHitRate(): Float {
            if (accesses.isEmpty()) return 0f
            return accesses.count { it }.toFloat() / accesses.size
        }
        
        fun reset() {
            accesses.clear()
        }
    }

    init {
        logger.d("Initializing SmartCacheManager")
        // Start monitoring cache state
        scope.launch {
            while (isActive) {
                updateCacheState()
                delay(CACHE_STATE_UPDATE_INTERVAL_MS)
            }
        }

        // Monitor predictions
        scope.launch {
            predictionDao.getActivePredictions()
                .collect { _activePredictions.value = it }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // PREDICTION & CACHE WARMING
    // ═════════════════════════════════════════════════════════════

    /**
     * Predict which apps the user will download next and cache them proactively.
     * This is the main entry point for AI-powered predictive caching.
     * 
     * @param availableApps List of all available apps from manifest
     * @param maxPredictions Maximum number of apps to predict
     * @return List of predictions with confidence scores
     */
    suspend fun predictNextDownloads(
        availableApps: List<AppEntry>,
        maxPredictions: Int = DEFAULT_MAX_PREDICTIONS
    ): List<PredictionResult> = withContext(Dispatchers.IO) {
        // Clean up expired predictions first
        cleanupExpiredPredictions()
        
        // Get predictions from ML model
        val predictions = predictor.predictNextInstalls(availableApps, maxPredictions * 2)
        
        // Store predictions in database
        val predictionEntities = predictions.map { result ->
            PredictionEntity(
                appId = result.app.id,
                confidenceScore = result.confidence,
                predictedInstallTime = result.predictedInstallTime,
                predictionType = determinePredictionType(result.factors),
                factors = serializeFactors(result.factors)
            )
        }
        predictionDao.insertPredictions(predictionEntities)
        
        // Update cache priorities based on predictions
        predictions.forEach { result ->
            updateCachePriority(result.app, result.confidence)
        }
        
        predictions.take(maxPredictions)
    }

    /**
     * Warm the cache by pre-downloading predicted apps.
     * Only runs when conditions are favorable (WiFi, charging, enough storage).
     * 
     * @param predictions List of predictions to cache
     * @param aggressive If true, caches more aggressively (higher bandwidth/storage use)
     * @return Number of apps queued for caching
     */
    suspend fun warmCache(
        predictions: List<PredictionResult>? = null,
        aggressive: Boolean = false
    ): Int = withContext(Dispatchers.IO) {
        // Check if conditions are right for cache warming
        if (!shouldPerformCacheWarming(aggressive)) {
            return@withContext 0
        }
        
        val appsToCache = predictions 
            ?: predictor.predictNextInstalls(getAvailableApps(), DEFAULT_MAX_PREDICTIONS)
        
        var queuedCount = 0
        var totalSizeNeeded = 0L
        
        appsToCache.forEach { prediction ->
            val app = prediction.app
            
            // Check if already cached
            val cached = cachedAppDao.getCachedApp(app.id)
            if (cached?.isApkCached == true) {
                return@forEach
            }
            
            // Estimate size (conservative estimate)
            val estimatedSize = estimateAppSize(app)
            
            // Check storage constraints
            if (!hasEnoughStorage(estimatedSize)) {
                // Try to make room
                if (!evictToMakeRoom(estimatedSize)) {
                    return@forEach
                }
            }
            
            // Queue for download
            queueCacheDownload(app, prediction.confidence)
            queuedCount++
            totalSizeNeeded += estimatedSize
            
            // Update predictive cache flag
            cachedAppDao.insertCachedApp(
                CachedAppEntity(
                    appId = app.id,
                    name = app.name,
                    packageName = app.packageName,
                    description = app.description,
                    iconUrl = app.iconUrl,
                    downloadUrl = app.downloadUrl,
                    version = app.version,
                    source = app.source,
                    category = app.category,
                    accentColor = app.accentColor,
                    badge = app.badge,
                    featured = app.featured,
                    sortOrder = app.sortOrder,
                    isPredictiveCache = true,
                    predictionConfidence = prediction.confidence,
                    cachePriority = prediction.confidence
                )
            )
        }
        
        queuedCount
    }

    /**
     * Smart cache warming strategy based on time, battery, and usage patterns.
     * Should be called periodically (e.g., from WorkManager).
     */
    suspend fun performSmartCacheWarming(): CacheWarmingResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        // Check if user is likely to use app soon
        val shouldPreload = predictor.shouldPreload(PRELOAD_CONFIDENCE_THRESHOLD)
        
        if (!shouldPreload && !isCharging()) {
            return@withContext CacheWarmingResult(
                success = false,
                reason = "Preload conditions not met",
                appsCached = 0,
                durationMs = System.currentTimeMillis() - startTime
            )
        }
        
        // Get predictions and warm cache
        val predictions = predictor.predictNextInstalls(getAvailableApps(), DEFAULT_MAX_PREDICTIONS)
        val highConfidencePredictions = predictions.filter { it.confidence >= MIN_CACHE_CONFIDENCE }
        
        if (highConfidencePredictions.isEmpty()) {
            return@withContext CacheWarmingResult(
                success = true,
                reason = "No high-confidence predictions",
                appsCached = 0,
                durationMs = System.currentTimeMillis() - startTime
            )
        }
        
        val cached = warmCache(highConfidencePredictions, aggressive = isCharging() && isWifiConnected())
        
        CacheWarmingResult(
            success = cached > 0,
            reason = if (cached > 0) "Successfully cached $cached apps" else "No apps cached",
            appsCached = cached,
            durationMs = System.currentTimeMillis() - startTime
        )
    }

    // ═════════════════════════════════════════════════════════════
    // CACHE MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    /**
     * Get all cached apps with their metadata
     */
    fun getCachedApps(): Flow<List<CachedAppEntity>> {
        return cachedAppDao.getAllCachedApps()
    }

    /**
     * Get apps that have their APKs fully cached locally
     */
    suspend fun getFullyCachedApps(): List<CachedAppEntity> = withContext(Dispatchers.IO) {
        cachedAppDao.getAppsWithCachedApk()
    }

    /**
     * Mark an app as accessed (updates LRU tracking)
     */
    suspend fun markAppAccessed(appId: String) = withContext(Dispatchers.IO) {
        cachedAppDao.updateLastAccessed(appId)
        usageDao.incrementViewCount(appId, System.currentTimeMillis())
    }

    /**
     * Record an app installation
     */
    suspend fun recordAppInstalled(appId: String, appName: String, category: String?) = withContext(Dispatchers.IO) {
        usageDao.incrementInstallCount(appId)
        
        val calendar = Calendar.getInstance()
        installHistoryDao.insertInstallHistory(
            InstallHistoryEntity(
                appId = appId,
                appName = appName,
                category = category,
                dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK),
                hourOfDay = calendar.get(Calendar.HOUR_OF_DAY),
                installSource = "manual"
            )
        )
        
        // Update cache priority (installed apps get higher priority temporarily)
        cachedAppDao.updateCachePriority(appId, 1.0f)
    }

    /**
     * Clear old cache entries based on smart eviction policy
     * 
     * @param targetSizeBytes Target cache size after cleanup
     * @return Number of apps removed from cache
     */
    suspend fun clearOldCache(targetSizeBytes: Long? = null): Int = withContext(Dispatchers.IO) {
        val target = targetSizeBytes ?: (config.maxCacheSizeBytes * CACHE_MAINTENANCE_RATIO).toLong()
        val currentSize = cachedAppDao.getTotalCacheSize() ?: 0L
        
        if (currentSize <= target) {
            return@withContext 0
        }
        
        var freedSize = 0L
        var removedCount = 0
        val bytesToFree = currentSize - target
        
        // Get apps sorted by eviction score (lowest first = evict first)
        val candidates = cachedAppDao.getLeastValuableCachedApps(100)
        
        for (app in candidates) {
            if (freedSize >= bytesToFree) break
            
            val evictionScore = calculateEvictionScore(app)
            
            // Only evict if score is below threshold
            if (evictionScore < EVICTION_THRESHOLD) {
                deleteCachedApk(app)
                freedSize += app.cachedApkSize
                removedCount++
            }
        }
        
        // Also clean up old database entries
        val oldThreshold = System.currentTimeMillis() - OLD_CACHE_THRESHOLD_MS
        cachedAppDao.deleteOldUncachedApps(oldThreshold)
        
        removedCount
    }

    /**
     * Force clear all cached APKs
     */
    suspend fun clearAllCache(): Int = withContext(Dispatchers.IO) {
        val cachedApps = cachedAppDao.getAppsWithCachedApk()
        
        cachedApps.forEach { app ->
            deleteCachedApk(app)
        }
        
        cachedApps.size
    }

    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): CacheStats = withContext(Dispatchers.IO) {
        val totalSize = cachedAppDao.getTotalCacheSize() ?: 0L
        val appCount = cachedAppDao.getCachedApkCount()
        val totalCapacity = config.maxCacheSizeBytes
        
        CacheStats(
            totalSizeBytes = totalSize,
            appCount = appCount,
            utilizationRatio = totalSize.toFloat() / totalCapacity,
            availableSpaceBytes = totalCapacity - totalSize,
            hitRate = calculateHitRate(),
            predictedAppsCount = predictionDao.getTopPredictions(100).size
        )
    }

    // ═════════════════════════════════════════════════════════════
    // SMART EVICTION POLICY
    // ═════════════════════════════════════════════════════════════

    /**
     * Calculate eviction score for an app (lower = more likely to evict)
     * Combines LRU, frequency, and predicted value
     */
    private suspend fun calculateEvictionScore(app: CachedAppEntity): Float {
        // LRU component: time since last access
        val timeSinceAccess = System.currentTimeMillis() - app.lastAccessedAt
        val lruScore = 1 - exp(-timeSinceAccess.toFloat() / LRU_DECAY_MS)
        
        // Frequency component: access count (normalized)
        val freqScore = (app.accessCount / 10f).coerceAtMost(1f)
        
        // Predicted value component
        val prediction = predictionDao.getPredictionForApp(app.appId)
        val predictionScore = prediction?.confidenceScore ?: 0f
        
        // Combined weighted score
        return (lruScore * LRU_WEIGHT + 
                freqScore * FREQUENCY_WEIGHT + 
                predictionScore * PREDICTION_WEIGHT)
            .coerceIn(0f, 1f)
    }

    /**
     * Evict cache entries to make room for a new download
     */
    private suspend fun evictToMakeRoom(requiredBytes: Long): Boolean {
        val currentSize = cachedAppDao.getTotalCacheSize() ?: 0L
        val maxSize = config.maxCacheSizeBytes
        
        if (currentSize + requiredBytes > maxSize * 1.1) { // Allow 10% overflow temporarily
            val targetSize = (maxSize - requiredBytes).coerceAtLeast(maxSize / 2)
            clearOldCache(targetSize)
        }
        
        val newSize = cachedAppDao.getTotalCacheSize() ?: 0L
        return (newSize + requiredBytes) <= maxSize
    }

    // ═════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═════════════════════════════════════════════════════════════

    /**
     * Update cache state with current statistics and warming status.
     */
    private suspend fun updateCacheState() {
        val stats = getCacheStats()
        _cacheState.value = CacheState(
            isReady = true,
            stats = stats,
            isCacheWarming = _cacheWarmingState.value is CacheWarmingState.Warming,
            hitRate = calculateHitRate()
        )
    }

    /**
     * Start a cache warming operation.
     * @param appsToWarm List of apps to cache
     * @param aggressive Whether to ignore battery/network constraints
     */
    private suspend fun startCacheWarming(
        appsToWarm: List<AppEntry>,
        aggressive: Boolean
    ): CacheWarmingResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        // Update warming state
        val operation = CacheWarmingOperation(
            startTime = startTime,
            totalApps = appsToWarm.size,
            aggressive = aggressive
        )
        currentWarmingOperation = operation
        _cacheWarmingState.value = CacheWarmingState.Warming(
            progress = 0f,
            appsProcessed = 0,
            totalApps = appsToWarm.size,
            estimatedTimeRemainingMs = -1L
        )

        logger.d("Starting cache warming: ${appsToWarm.size} apps, aggressive=$aggressive")

        var successCount = 0
        var failedCount = 0

        appsToWarm.forEachIndexed { index, app ->
            try {
                // Update progress
                val progress = (index + 1).toFloat() / appsToWarm.size
                val elapsed = System.currentTimeMillis() - startTime
                val estimatedTotal = if (progress > 0) elapsed / progress else -1L
                val eta = if (estimatedTotal > 0) estimatedTotal - elapsed else -1L
                
                _cacheWarmingState.value = CacheWarmingState.Warming(
                    progress = progress,
                    appsProcessed = index,
                    totalApps = appsToWarm.size,
                    estimatedTimeRemainingMs = eta
                )

                // Cache the app
                queueCacheDownload(app, confidence = 0.7f)
                successCount++
            } catch (e: Exception) {
                logger.e("Failed to cache app: ${app.id}", e)
                failedCount++
            }
        }

        val duration = System.currentTimeMillis() - startTime
        val success = failedCount == 0 || (successCount.toFloat() / appsToWarm.size) > 0.8f

        // Complete warming state
        _cacheWarmingState.value = CacheWarmingState.Complete(
            appsCached = successCount,
            appsFailed = failedCount,
            durationMs = duration
        )
        currentWarmingOperation = null

        logger.d("Cache warming complete: $successCount succeeded, $failedCount failed in ${duration}ms")

        CacheWarmingResult(
            success = success,
            reason = if (success) "Success" else "Too many failures",
            appsCached = successCount,
            durationMs = duration
        )
    }

    /**
     * Record a cache hit for hit rate tracking.
     */
    fun recordCacheHit(appId: String) {
        cacheAccessTracker[appId] = CacheAccessRecord(
            appId = appId,
            lastAccessed = System.currentTimeMillis(),
            accessCount = (cacheAccessTracker[appId]?.accessCount ?: 0) + 1
        )
        hitRateWindow.recordHit(hit = true)
        logger.d("Cache hit recorded for: $appId")
    }

    /**
     * Record a cache miss for hit rate tracking.
     */
    fun recordCacheMiss(appId: String) {
        hitRateWindow.recordHit(hit = false)
        logger.d("Cache miss recorded for: $appId")
    }

    /**
     * Calculate actual cache hit rate from recent accesses.
     */
    private fun calculateHitRate(): Float {
        val hitRate = hitRateWindow.getHitRate()
        logger.d("Current cache hit rate: ${hitRate}")
        return hitRate
    }

    /**
     * Reset hit rate tracking statistics.
     */
    fun resetHitRateTracking() {
        hitRateWindow.reset()
        cacheAccessTracker.clear()
        logger.d("Hit rate tracking reset")
    }

    private suspend fun updateCachePriority(app: AppEntry, confidence: Float) {
        val existing = cachedAppDao.getCachedApp(app.id)
        if (existing != null) {
            val newPriority = (existing.cachePriority + confidence) / 2
            cachedAppDao.updateCachePriority(app.id, newPriority)
        }
    }

    private suspend fun queueCacheDownload(app: AppEntry, confidence: Float) {
        // Use download manager with low priority for background caching
        val request = SmartDownloadManager.DownloadRequest(
            id = "cache_${app.id}",
            url = app.downloadUrl,
            fileName = "${app.packageName}_${app.version}.apk",
            appName = app.name,
            appIcon = app.iconUrl,
            wifiOnly = true, // Only cache on WiFi
            autoInstall = false, // Don't auto-install cached apps
            priority = if (confidence > 0.7) 
                SmartDownloadManager.DownloadPriority.NORMAL 
            else 
                SmartDownloadManager.DownloadPriority.LOW
        )
        
        downloadManager.enqueue(request)
    }

    private suspend fun deleteCachedApk(app: CachedAppEntity) {
        app.cachedApkPath?.let { path ->
            File(path).delete()
        }
        cachedAppDao.updateApkCacheStatus(app.appId, false, null, 0)
    }

    private fun shouldPerformCacheWarming(aggressive: Boolean): Boolean {
        if (!isWifiConnected() && !aggressive) return false
        if (getBatteryLevel() < MIN_BATTERY_LEVEL && !isCharging()) return false
        if (!hasEnoughStorage(MIN_FREE_STORAGE_BYTES)) return false
        return true
    }

    private fun hasEnoughStorage(requiredBytes: Long): Boolean {
        val stat = StatFs(context.filesDir.path)
        val available = stat.availableBytes
        return available > requiredBytes + MIN_FREE_STORAGE_BYTES
    }

    private fun estimateAppSize(app: AppEntry): Long {
        // Conservative estimate - most APKs are between 10MB and 100MB
        return 50 * 1024 * 1024 // 50MB default estimate
    }

    private fun isWifiConnected(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun isCharging(): Boolean {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.isCharging
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private suspend fun getAvailableApps(): List<AppEntry> {
        // Get from database cache
        return database.appDao().getAllApps().firstOrNull()?.map { it.toAppEntry() } ?: emptyList()
    }

    private fun determinePredictionType(factors: List<PredictionFactor>): String {
        return when {
            factors.any { it is PredictionFactor.CategoryMatch } -> PredictionEntity.TYPE_CATEGORY_BASED
            factors.any { it is PredictionFactor.TimeContextMatch } -> PredictionEntity.TYPE_TIME_BASED
            factors.any { it is PredictionFactor.AppSimilarity } -> PredictionEntity.TYPE_SIMILARITY
            factors.any { it is PredictionFactor.FeaturedApp } -> PredictionEntity.TYPE_TRENDING
            else -> PredictionEntity.TYPE_CATEGORY_BASED
        }
    }

    private fun serializeFactors(factors: List<PredictionFactor>): String {
        return factors.joinToString(",") { factor ->
            when (factor) {
                is PredictionFactor.CategoryMatch -> "cat:${factor.score}"
                is PredictionFactor.PreviouslyViewed -> "viewed:${factor.score}"
                is PredictionFactor.TimeContextMatch -> "time:${factor.score}"
                is PredictionFactor.AppSimilarity -> "similar:${factor.score}"
                PredictionFactor.AlreadyInstalled -> "installed"
                PredictionFactor.InFavorites -> "favorite"
                PredictionFactor.FeaturedApp -> "featured"
            }
        }
    }

    private suspend fun cleanupExpiredPredictions() {
        predictionDao.deleteExpiredPredictions()
    }

    // ═════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═════════════════════════════════════════════════════════════

    companion object {
        private const val DEFAULT_MAX_PREDICTIONS = 5
        private const val MIN_CACHE_CONFIDENCE = 0.6f
        private const val PRELOAD_CONFIDENCE_THRESHOLD = 0.5f
        private const val MIN_BATTERY_LEVEL = 30
        private const val MIN_FREE_STORAGE_BYTES = 500 * 1024 * 1024L // 500MB
        private const val CACHE_STATE_UPDATE_INTERVAL_MS = 30000L // 30 seconds
        private const val CACHE_MAINTENANCE_RATIO = 0.8f // Target 80% capacity
        private const val OLD_CACHE_THRESHOLD_MS = 7L * 24 * 60 * 60 * 1000 // 7 days
        private const val LRU_DECAY_MS = 24 * 60 * 60 * 1000f // 1 day
        private const val EVICTION_THRESHOLD = 0.3f

        // Eviction weights
        private const val LRU_WEIGHT = 0.4f
        private const val FREQUENCY_WEIGHT = 0.3f
        private const val PREDICTION_WEIGHT = 0.3f
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES & CONFIG
// ═════════════════════════════════════════════════════════════

data class CacheConfig(
    val maxCacheSizeBytes: Long = 2L * 1024 * 1024 * 1024, // 2GB default
    val maxPredictiveCacheItems: Int = 10,
    val minConfidenceForCache: Float = 0.5f,
    val enableAggressiveCaching: Boolean = false,
    val respectDataSaver: Boolean = true,
    val cacheMetadataExpiryMs: Long = 24 * 60 * 60 * 1000 // 24 hours
)

data class CacheState(
    val isReady: Boolean = false,
    val stats: CacheStats = CacheStats(),
    val isCacheWarming: Boolean = false,
    val hitRate: Float = 0f
)

data class CacheStats(
    val totalSizeBytes: Long = 0,
    val appCount: Int = 0,
    val utilizationRatio: Float = 0f,
    val availableSpaceBytes: Long = 0,
    val hitRate: Float = 0f,
    val predictedAppsCount: Int = 0
) {
    val totalSizeMB: Float get() = totalSizeBytes / (1024f * 1024f)
    val utilizationPercent: Float get() = utilizationRatio * 100
}

data class CacheWarmingResult(
    val success: Boolean,
    val reason: String,
    val appsCached: Int,
    val durationMs: Long
)

// ═════════════════════════════════════════════════════════════
// CACHE WARMING STATE CLASSES
// ═════════════════════════════════════════════════════════════

/**
 * Represents the current state of a cache warming operation.
 */
sealed class CacheWarmingState {
    /**
     * No cache warming operation in progress.
     */
    object Idle : CacheWarmingState()

    /**
     * Cache warming is in progress.
     * @param progress Progress from 0.0 to 1.0
     * @param appsProcessed Number of apps processed so far
     * @param totalApps Total number of apps to warm
     * @param estimatedTimeRemainingMs Estimated time remaining in milliseconds (-1 if unknown)
     */
    data class Warming(
        val progress: Float,
        val appsProcessed: Int,
        val totalApps: Int,
        val estimatedTimeRemainingMs: Long
    ) : CacheWarmingState() {
        val progressPercent: Int get() = (progress * 100).toInt()
    }

    /**
     * Cache warming operation completed.
     * @param appsCached Number of apps successfully cached
     * @param appsFailed Number of apps that failed to cache
     * @param durationMs Total duration of the operation
     */
    data class Complete(
        val appsCached: Int,
        val appsFailed: Int,
        val durationMs: Long
    ) : CacheWarmingState() {
        val successRate: Float get() = if (appsCached + appsFailed > 0) {
            appsCached.toFloat() / (appsCached + appsFailed)
        } else 0f
    }
}

/**
 * Details about the current cache warming operation.
 */
data class CacheWarmingOperation(
    val startTime: Long,
    val totalApps: Int,
    val aggressive: Boolean
)

/**
 * Records cache access for hit rate tracking.
 */
data class CacheAccessRecord(
    val appId: String,
    val lastAccessed: Long,
    val accessCount: Int
)
