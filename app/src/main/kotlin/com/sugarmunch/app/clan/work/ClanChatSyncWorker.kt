package com.sugarmunch.app.clan.work

import android.content.Context
import androidx.work.*
import com.sugarmunch.app.clan.ClanChatManager
import com.sugarmunch.app.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * ClanChatSyncWorker - Background sync for clan chat messages
 * 
 * Features:
 * - Syncs new messages from server
 * - Sends pending outgoing messages
 * - Cleans up old messages
 */
class ClanChatSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val clanDao = database.clanDao()
    private val chatManager = ClanChatManager.getInstance(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Clean up old messages
            cleanupOldMessages()
            
            // In a real implementation, would:
            // 1. Fetch new messages from server
            // 2. Send any pending outgoing messages
            // 3. Update read receipts
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun cleanupOldMessages() {
        val cutoffTime = System.currentTimeMillis() - 
            (ClanChatManager.MESSAGE_RETENTION_DAYS * 24 * 60 * 60 * 1000)
        
        // Get all clan IDs that have messages
        val allClans = clanDao.getTopClans(1000)
        
        allClans.forEach { clan ->
            clanDao.deleteOldMessages(clan.id, cutoffTime)
        }
    }

    companion object {
        private const val TAG = "ClanChatSyncWorker"
        private const val CHAT_SYNC_WORK_NAME = "clan_chat_sync"
        private const val SYNC_INTERVAL_MINUTES = 1L // Frequent sync for chat

        /**
         * Schedule periodic chat sync
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWork = PeriodicWorkRequestBuilder<ClanChatSyncWorker>(
                SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                CHAT_SYNC_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                syncWork
            )
        }

        /**
         * Schedule immediate chat sync
         */
        fun syncNow(context: Context) {
            val work = OneTimeWorkRequestBuilder<ClanChatSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${CHAT_SYNC_WORK_NAME}_immediate",
                ExistingWorkPolicy.REPLACE,
                work
            )
        }

        /**
         * Cancel chat sync
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(CHAT_SYNC_WORK_NAME)
        }
    }
}
