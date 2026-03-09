package com.sugarmunch.app.work

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import androidx.work.*
import com.sugarmunch.app.ai.SmartCacheManager
import com.sugarmunch.app.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * CacheWorker - Background cache management
 * 
 * Features:
 * - Cache cleanup and maintenance
 * - Predictive cache warming
 * - Battery-aware operation
 * - Runs during charging + WiFi
 * - Smart cache size management
 */
class CacheWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val cacheManager = SmartCacheManager.getInstance(context)
    private val database = AppDatabase.getDatabase(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val operation = inputData.getString(KEY_OPERATION) ?: OPERATION_MAINTENANCE
        
        when (operation) {
            OPERATION_MAINTENANCE -> performMaintenance()
            OPERATION_WARMING -> performCacheWarming()
            OPERATION_CLEANUP -> performCleanup()
            else -> performMaintenance()
        }
    }

    /**
     * Perform routine cache maintenance
     */
    private suspend fun performMaintenance(): Result {
        val results = mutableMapOf<String, Any>()
        
        // 1. Clean up old cache entries
        val clearedCount = cacheManager.clearOldCache()
        results["cleared_count"] = clearedCount
        
        // 2. Get current cache stats
        val stats = cacheManager.getCacheStats()
        results["cache_size_mb"] = stats.totalSizeMB
        results["cache_utilization"] = stats.utilizationPercent
        results["cached_apps"] = stats.appCount
        
        // 3. If cache is getting full, be more aggressive
        if (stats.utilizationRatio > HIGH_UTILIZATION_THRESHOLD) {
            val aggressiveCleared = cacheManager.clearOldCache(
                (cacheManager.cacheState.value.stats.totalSizeBytes * 0.6).toLong()
            )
            results["aggressive_cleared"] = aggressiveCleared
        }
        
        // 4. Check if we should trigger predictive warming
        if (shouldPerformWarming()) {
            val warmingResult = cacheManager.performSmartCacheWarming()
            results["warming_success"] = warmingResult.success
            results["warming_apps"] = warmingResult.appsCached
        }
        
        return Result.success(workDataOf(
            KEY_RESULT_TYPE to "maintenance",
            KEY_CLEARED_COUNT to clearedCount,
            KEY_CACHE_SIZE_MB to stats.totalSizeMB.toDouble(),
            KEY_CACHED_APPS to stats.appCount
        ))
    }

    /**
     * Perform predictive cache warming
     */
    private suspend fun performCacheWarming(): Result {
        // Check if conditions are optimal
        if (!isOptimalWarmingConditions()) {
            return Result.retry()
        }
        
        val result = cacheManager.performSmartCacheWarming()
        
        return if (result.success) {
            Result.success(workDataOf(
                KEY_RESULT_TYPE to "warming",
                KEY_WARMED_APPS to result.appsCached,
                KEY_DURATION_MS to result.durationMs
            ))
        } else {
            // No high-confidence predictions or other issue - not a failure
            Result.success(workDataOf(
                KEY_RESULT_TYPE to "warming",
                KEY_WARMED_APPS to 0,
                KEY_REASON to result.reason
            ))
        }
    }

    /**
     * Perform aggressive cache cleanup
     */
    private suspend fun performCleanup(): Result {
        val targetRatio = inputData.getFloat(KEY_TARGET_RATIO, DEFAULT_CLEANUP_TARGET_RATIO)
        val currentStats = cacheManager.getCacheStats()
        val targetSize = (currentStats.totalSizeBytes * targetRatio).toLong()
        
        val clearedCount = cacheManager.clearOldCache(targetSize)
        
        return Result.success(workDataOf(
            KEY_RESULT_TYPE to "cleanup",
            KEY_CLEARED_COUNT to clearedCount,
            KEY_TARGET_RATIO to targetRatio
        ))
    }

    /**
     * Check if we should perform cache warming now
     */
    private fun shouldPerformWarming(): Boolean {
        // Only warm cache if:
        // 1. On WiFi
        // 2. Battery level good or charging
        // 3. Not in power save mode
        return isWifiConnected() && (isCharging() || getBatteryLevel() > 50)
    }

    /**
     * Check for optimal warming conditions (more strict)
     */
    private fun isOptimalWarmingConditions(): Boolean {
        return isWifiConnected() && 
               isCharging() && 
               getBatteryLevel() > MIN_BATTERY_FOR_WARMING &&
               !isPowerSaveMode()
    }

    private fun isWifiConnected(): Boolean {
        val cm = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    private fun isCharging(): Boolean {
        val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.isCharging
    }

    private fun getBatteryLevel(): Int {
        val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun isPowerSaveMode(): Boolean {
        val powerManager = applicationContext.getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager.isPowerSaveMode
        } else {
            false
        }
    }

    companion object {
        // Operations
        const val OPERATION_MAINTENANCE = "maintenance"
        const val OPERATION_WARMING = "warming"
        const val OPERATION_CLEANUP = "cleanup"
        
        // Input keys
        const val KEY_OPERATION = "operation"
        const val KEY_TARGET_RATIO = "target_ratio"
        
        // Output keys
        const val KEY_RESULT_TYPE = "result_type"
        const val KEY_CLEARED_COUNT = "cleared_count"
        const val KEY_CACHE_SIZE_MB = "cache_size_mb"
        const val KEY_CACHED_APPS = "cached_apps"
        const val KEY_WARMED_APPS = "warmed_apps"
        const val KEY_DURATION_MS = "duration_ms"
        const val KEY_REASON = "reason"
        
        // Constants
        private const val HIGH_UTILIZATION_THRESHOLD = 0.85f
        private const val DEFAULT_CLEANUP_TARGET_RATIO = 0.7f
        private const val MIN_BATTERY_FOR_WARMING = 60
        private const val TAG = "CacheWorker"

        /**
         * Schedule periodic cache maintenance
         */
        fun scheduleMaintenance(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build()

            val maintenanceWork = PeriodicWorkRequestBuilder<CacheWorker>(
                MAINTENANCE_INTERVAL_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_OPERATION to OPERATION_MAINTENANCE))
                .addTag(TAG)
                .addTag(OPERATION_MAINTENANCE)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                MAINTENANCE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                maintenanceWork
            )
        }

        /**
         * Schedule cache warming during optimal conditions
         */
        fun scheduleCacheWarming(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED) // WiFi only
                .setRequiresCharging(true)
                .setRequiresBatteryNotLow(true)
                .build()

            val warmingWork = OneTimeWorkRequestBuilder<CacheWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_OPERATION to OPERATION_WARMING))
                .addTag(TAG)
                .addTag(OPERATION_WARMING)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WARMING_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                warmingWork
            )
        }

        /**
         * Schedule periodic cache warming (runs when constraints are met)
         */
        fun schedulePeriodicWarming(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresCharging(true)
                .build()

            val warmingWork = PeriodicWorkRequestBuilder<CacheWorker>(
                WARMING_INTERVAL_HOURS, TimeUnit.HOURS,
                WARMING_FLEX_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_OPERATION to OPERATION_WARMING))
                .addTag(TAG)
                .addTag(OPERATION_WARMING)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WARMING_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                warmingWork
            )
        }

        /**
         * Trigger immediate cache cleanup
         */
        fun cleanupNow(context: Context, targetRatio: Float = DEFAULT_CLEANUP_TARGET_RATIO) {
            val cleanupWork = OneTimeWorkRequestBuilder<CacheWorker>()
                .setInputData(workDataOf(
                    KEY_OPERATION to OPERATION_CLEANUP,
                    KEY_TARGET_RATIO to targetRatio
                ))
                .addTag(TAG)
                .addTag(OPERATION_CLEANUP)
                .build()

            WorkManager.getInstance(context).enqueue(cleanupWork)
        }

        /**
         * Cancel all cache work
         */
        fun cancelAll(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(MAINTENANCE_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(WARMING_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WARMING_WORK_NAME)
        }

        /**
         * Cancel only warming work
         */
        fun cancelWarming(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WARMING_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WARMING_WORK_NAME)
        }

        private const val MAINTENANCE_INTERVAL_HOURS = 12L
        private const val WARMING_INTERVAL_HOURS = 6L
        private const val WARMING_FLEX_HOURS = 2L
        
        private const val MAINTENANCE_WORK_NAME = "cache_maintenance"
        private const val WARMING_WORK_NAME = "cache_warming"
        private const val PERIODIC_WARMING_WORK_NAME = "periodic_cache_warming"
    }
}

/**
 * Worker for analyzing usage patterns and updating ML models
 */
class AnalyticsSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val cacheManager = SmartCacheManager.getInstance(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Clean up old analytics data
            val cleanupBefore = System.currentTimeMillis() - ANALYTICS_RETENTION_MS
            database.installHistoryDao().deleteOldHistory(cleanupBefore)
            
            // Update predictions based on new patterns
            val availableApps = database.appDao().getAllApps().first()
                .map { it.toAppEntry() }
            
            if (availableApps.isNotEmpty()) {
                cacheManager.predictNextDownloads(availableApps)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val ANALYTICS_RETENTION_MS = 90L * 24 * 60 * 60 * 1000 // 90 days

        fun schedule(context: Context) {
            val work = PeriodicWorkRequestBuilder<AnalyticsSyncWorker>(
                24, TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "analytics_sync",
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
        }
    }
}

/**
 * Extension function for AppEntity conversion
 */
private fun com.sugarmunch.app.data.local.AppEntity.toAppEntry() = com.sugarmunch.app.data.AppEntry(
    id = id,
    name = name,
    packageName = packageName,
    description = description,
    iconUrl = iconUrl,
    downloadUrl = downloadUrl,
    version = version,
    source = source,
    category = category,
    accentColor = accentColor,
    badge = badge,
    featured = featured,
    sortOrder = sortOrder
)
