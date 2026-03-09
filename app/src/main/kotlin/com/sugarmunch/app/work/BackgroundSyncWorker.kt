package com.sugarmunch.app.work

import android.content.Context
import androidx.work.*
import com.sugarmunch.app.ai.SmartCacheManager
import com.sugarmunch.app.data.local.AppDatabase
import com.sugarmunch.app.data.local.SyncStateEntity
import com.sugarmunch.app.repository.SmartManifestRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * BackgroundSyncWorker - Periodic manifest synchronization
 * 
 * Features:
 * - Periodic sync of app manifest
 * - Battery-aware scheduling
 * - Smart conflict resolution
 * - Retry with exponential backoff
 */
class BackgroundSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val syncStateDao = database.syncStateDao()
    private val repository = SmartManifestRepository(context)
    private val cacheManager = SmartCacheManager.getInstance(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        try {
            // Record sync attempt
            syncStateDao.recordSyncAttempt()
            
            // Check if we should skip this sync (backoff)
            val syncState = syncStateDao.getSyncState()
            if (shouldSkipSync(syncState)) {
                return@withContext Result.retry()
            }
            
            // Perform sync
            val syncResult = repository.sync()
            
            if (syncResult.isSuccess) {
                // Sync successful
                syncStateDao.markSyncSuccess()
                
                // Trigger cache warming for new predictions
                val apps = repository.getAppsWithCache().first()
                cacheManager.predictNextDownloads(apps)
                
                // Schedule cache warming if conditions are good
                if (shouldTriggerCacheWarming()) {
                    CacheWorker.scheduleCacheWarming(applicationContext)
                }
                
                Result.success(
                    workDataOf(
                        KEY_SYNC_DURATION to System.currentTimeMillis() - startTime,
                        KEY_APPS_SYNCED to (apps.size)
                    )
                )
            } else {
                // Sync failed
                val error = syncResult.exceptionOrNull()?.message ?: "Unknown error"
                syncStateDao.recordSyncError(error)
                
                // Check if we should retry
                if (shouldRetry(syncState)) {
                    Result.retry()
                } else {
                    Result.failure(
                        workDataOf(KEY_ERROR to error)
                    )
                }
            }
        } catch (e: Exception) {
            syncStateDao.recordSyncError(e.message ?: "Unknown error")
            
            if (shouldRetry(syncStateDao.getSyncState())) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf(KEY_ERROR to (e.message ?: "Unknown error"))
                )
            }
        }
    }

    private fun shouldSkipSync(syncState: SyncStateEntity?): Boolean {
        syncState ?: return false
        
        // Skip if we've had too many recent failures
        if (syncState.syncAttemptCount >= MAX_RETRY_ATTEMPTS) {
            val lastAttempt = syncState.lastAttemptedSync ?: 0
            val backoffTime = calculateBackoffTime(syncState.syncAttemptCount)
            return System.currentTimeMillis() - lastAttempt < backoffTime
        }
        
        return false
    }

    private fun shouldRetry(syncState: SyncStateEntity?): Boolean {
        syncState ?: return true
        return syncState.syncAttemptCount < MAX_RETRY_ATTEMPTS
    }

    private fun calculateBackoffTime(attemptCount: Int): Long {
        // Exponential backoff: 15min, 30min, 1hr, 2hr, 4hr
        return (15 * 60 * 1000L * (1 shl attemptCount.coerceAtMost(4)))
    }

    private fun shouldTriggerCacheWarming(): Boolean {
        // Only trigger cache warming during optimal conditions
        // The CacheWorker will check battery/WiFi status
        return true
    }

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 5
        private const val TAG = "BackgroundSyncWorker"
        
        // Output keys
        const val KEY_SYNC_DURATION = "sync_duration"
        const val KEY_APPS_SYNCED = "apps_synced"
        const val KEY_ERROR = "error"

        /**
         * Schedule periodic background sync
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val syncWork = PeriodicWorkRequestBuilder<BackgroundSyncWorker>(
                SYNC_INTERVAL_HOURS, TimeUnit.HOURS,
                SYNC_FLEX_HOURS, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    BACKOFF_DELAY_MINUTES,
                    TimeUnit.MINUTES
                )
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncWork
            )
        }

        /**
         * Schedule immediate one-time sync
         */
        fun syncNow(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWork = OneTimeWorkRequestBuilder<BackgroundSyncWorker>()
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                SYNC_NOW_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncWork
            )
        }

        /**
         * Cancel all sync work
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_NOW_WORK_NAME)
        }

        /**
         * Check if sync is scheduled
         */
        fun isScheduled(context: Context): Boolean {
            val workManager = WorkManager.getInstance(context)
            val workInfos = workManager.getWorkInfosForUniqueWork(SYNC_WORK_NAME).get()
            return workInfos?.any { 
                it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING 
            } ?: false
        }

        private const val SYNC_INTERVAL_HOURS = 6L
        private const val SYNC_FLEX_HOURS = 1L
        private const val BACKOFF_DELAY_MINUTES = 15L
        private const val SYNC_WORK_NAME = "manifest_sync"
        private const val SYNC_NOW_WORK_NAME = "manifest_sync_now"
    }
}

/**
 * Worker for handling immediate refresh requests from UI
 */
class ImmediateSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val repository = SmartManifestRepository(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val result = repository.forceRefresh()
            
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        fun schedule(context: Context) {
            val work = OneTimeWorkRequestBuilder<ImmediateSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueue(work)
        }
    }
}
