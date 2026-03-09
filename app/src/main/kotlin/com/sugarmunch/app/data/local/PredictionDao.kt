package com.sugarmunch.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionDao {
    
    @Query("SELECT * FROM predictions WHERE expiresAt > :currentTime ORDER BY confidenceScore DESC")
    fun getActivePredictions(currentTime: Long = System.currentTimeMillis()): Flow<List<PredictionEntity>>
    
    @Query("SELECT * FROM predictions WHERE expiresAt > :currentTime ORDER BY confidenceScore DESC LIMIT :limit")
    suspend fun getTopPredictions(limit: Int, currentTime: Long = System.currentTimeMillis()): List<PredictionEntity>
    
    @Query("SELECT * FROM predictions WHERE appId = :appId")
    suspend fun getPredictionForApp(appId: String): PredictionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPredictions(predictions: List<PredictionEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrediction(prediction: PredictionEntity)
    
    @Query("DELETE FROM predictions WHERE expiresAt < :currentTime")
    suspend fun deleteExpiredPredictions(currentTime: Long = System.currentTimeMillis()): Int
    
    @Query("DELETE FROM predictions")
    suspend fun deleteAllPredictions()
    
    @Query("SELECT AVG(confidenceScore) FROM predictions WHERE predictionType = :type")
    suspend fun getAverageConfidenceForType(type: String): Float?
}

@Dao
interface AppUsageDao {
    
    @Query("SELECT * FROM app_usage ORDER BY updatedAt DESC")
    fun getAllUsage(): Flow<List<AppUsageEntity>>
    
    @Query("SELECT * FROM app_usage WHERE appId = :appId")
    suspend fun getUsageForApp(appId: String): AppUsageEntity?
    
    @Query("SELECT * FROM app_usage WHERE category = :category ORDER BY viewCount DESC")
    suspend fun getUsageByCategory(category: String): List<AppUsageEntity>
    
    @Query("SELECT * FROM app_usage ORDER BY viewCount DESC LIMIT :limit")
    suspend fun getMostViewedApps(limit: Int): List<AppUsageEntity>
    
    @Query("SELECT * FROM app_usage WHERE installCount > 0 ORDER BY installCount DESC LIMIT :limit")
    suspend fun getMostInstalledApps(limit: Int): List<AppUsageEntity>
    
    @Query("SELECT * FROM app_usage WHERE isFavorite = 1")
    suspend fun getFavoriteApps(): List<AppUsageEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: AppUsageEntity)
    
    @Query("UPDATE app_usage SET viewCount = viewCount + 1, lastViewedAt = :timestamp, totalViewTime = totalViewTime + :timeSpent, updatedAt = :timestamp WHERE appId = :appId")
    suspend fun incrementViewCount(appId: String, timestamp: Long, timeSpent: Long = 0)
    
    @Query("UPDATE app_usage SET installCount = installCount + 1, lastInstalledAt = :timestamp, isCurrentlyInstalled = 1, updatedAt = :timestamp WHERE appId = :appId")
    suspend fun incrementInstallCount(appId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE app_usage SET isCurrentlyInstalled = 0 WHERE appId = :appId")
    suspend fun markAsUninstalled(appId: String)
    
    @Query("UPDATE app_usage SET isFavorite = :isFavorite, updatedAt = :timestamp WHERE appId = :appId")
    suspend fun setFavorite(appId: String, isFavorite: Boolean, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT category, COUNT(*) as count FROM app_usage WHERE installCount > 0 GROUP BY category ORDER BY count DESC")
    suspend fun getCategoryInstallCounts(): List<CategoryCount>
    
    @Query("DELETE FROM app_usage WHERE appId = :appId")
    suspend fun deleteUsage(appId: String)
}

data class CategoryCount(
    val category: String?,
    val count: Int
)

@Dao
interface CachedAppDao {
    
    @Query("SELECT * FROM cached_apps ORDER BY cachePriority DESC, lastAccessedAt DESC")
    fun getAllCachedApps(): Flow<List<CachedAppEntity>>
    
    @Query("SELECT * FROM cached_apps WHERE isApkCached = 1 ORDER BY cachePriority DESC, lastAccessedAt DESC")
    suspend fun getAppsWithCachedApk(): List<CachedAppEntity>
    
    @Query("SELECT * FROM cached_apps WHERE appId = :appId")
    suspend fun getCachedApp(appId: String): CachedAppEntity?
    
    @Query("SELECT * FROM cached_apps WHERE isPredictiveCache = 1 AND isApkCached = 0 ORDER BY predictionConfidence DESC")
    suspend fun getPendingPredictiveCaches(): List<CachedAppEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedApp(cachedApp: CachedAppEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedApps(cachedApps: List<CachedAppEntity>)
    
    @Query("UPDATE cached_apps SET lastAccessedAt = :timestamp, accessCount = accessCount + 1 WHERE appId = :appId")
    suspend fun updateLastAccessed(appId: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE cached_apps SET isApkCached = :isCached, cachedApkPath = :path, cachedApkSize = :size WHERE appId = :appId")
    suspend fun updateApkCacheStatus(appId: String, isCached: Boolean, path: String?, size: Long)
    
    @Query("UPDATE cached_apps SET cachePriority = :priority WHERE appId = :appId")
    suspend fun updateCachePriority(appId: String, priority: Float)
    
    @Query("SELECT SUM(cachedApkSize) FROM cached_apps WHERE isApkCached = 1")
    suspend fun getTotalCacheSize(): Long?
    
    @Query("SELECT * FROM cached_apps WHERE isApkCached = 1 ORDER BY cachePriority ASC, lastAccessedAt ASC LIMIT :limit")
    suspend fun getLeastValuableCachedApps(limit: Int): List<CachedAppEntity>
    
    @Query("DELETE FROM cached_apps WHERE appId = :appId")
    suspend fun deleteCachedApp(appId: String)
    
    @Query("DELETE FROM cached_apps WHERE isApkCached = 0 AND cachedAt < :timestamp")
    suspend fun deleteOldUncachedApps(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM cached_apps WHERE isApkCached = 1")
    suspend fun getCachedApkCount(): Int
}

@Dao
interface InstallHistoryDao {
    
    @Query("SELECT * FROM install_history ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentInstalls(limit: Int): List<InstallHistoryEntity>
    
    @Query("SELECT * FROM install_history WHERE timestamp > :since ORDER BY timestamp DESC")
    suspend fun getInstallsSince(since: Long): List<InstallHistoryEntity>
    
    @Query("SELECT * FROM install_history WHERE appId = :appId ORDER BY timestamp DESC")
    suspend fun getInstallHistoryForApp(appId: String): List<InstallHistoryEntity>
    
    @Query("SELECT hourOfDay, COUNT(*) as count FROM install_history WHERE timestamp > :since GROUP BY hourOfDay ORDER BY count DESC")
    suspend fun getInstallPatternsByHour(since: Long): List<HourlyPattern>
    
    @Query("SELECT dayOfWeek, COUNT(*) as count FROM install_history WHERE timestamp > :since GROUP BY dayOfWeek ORDER BY count DESC")
    suspend fun getInstallPatternsByDay(since: Long): List<DailyPattern>
    
    @Query("SELECT category, COUNT(*) as count FROM install_history WHERE timestamp > :since GROUP BY category ORDER BY count DESC")
    suspend fun getInstallPatternsByCategory(since: Long): List<CategoryPattern>
    
    @Insert
    suspend fun insertInstallHistory(history: InstallHistoryEntity)
    
    @Query("DELETE FROM install_history WHERE timestamp < :before")
    suspend fun deleteOldHistory(before: Long)
}

data class HourlyPattern(
    val hourOfDay: Int,
    val count: Int
)

data class DailyPattern(
    val dayOfWeek: Int,
    val count: Int
)

data class CategoryPattern(
    val category: String?,
    val count: Int
)

@Dao
interface SyncStateDao {
    
    @Query("SELECT * FROM sync_state WHERE id = 1")
    suspend fun getSyncState(): SyncStateEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSyncState(state: SyncStateEntity)
    
    @Query("UPDATE sync_state SET lastSuccessfulSync = :timestamp, lastSyncError = NULL WHERE id = 1")
    suspend fun markSyncSuccess(timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE sync_state SET lastAttemptedSync = :timestamp, syncAttemptCount = syncAttemptCount + 1 WHERE id = 1")
    suspend fun recordSyncAttempt(timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE sync_state SET lastSyncError = :error, syncAttemptCount = 0 WHERE id = 1")
    suspend fun recordSyncError(error: String)
}
