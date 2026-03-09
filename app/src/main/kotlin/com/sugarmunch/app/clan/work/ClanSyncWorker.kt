package com.sugarmunch.app.clan.work

import android.content.Context
import androidx.work.*
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * ClanSyncWorker - Periodic synchronization of clan data
 * 
 * Features:
 * - Syncs clan data with server
 * - Updates member statuses
 * - Syncs messages
 * - Checks for war updates
 */
class ClanSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val clanDao = database.clanDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // In a real implementation, this would:
            // 1. Fetch latest clan data from server
            // 2. Sync member list and statuses
            // 3. Check for pending invitations
            // 4. Update join requests status
            // 5. Sync any pending local changes
            
            // For now, we'll just clean up expired invitations
            cleanupExpiredInvitations()
            
            // Check for war status updates
            checkWarStatus()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun cleanupExpiredInvitations() {
        val currentTime = System.currentTimeMillis()
        clanDao.deleteExpiredInvitations(currentTime, InvitationStatus.PENDING)
    }

    private suspend fun checkWarStatus() {
        val activeWars = clanDao.getAllWarsByStatus(WarStatus.ACTIVE)
        val currentTime = System.currentTimeMillis()
        
        activeWars.forEach { war ->
            if (war.endsAt <= currentTime) {
                // War has ended, update status
                val updatedWar = war.copy(status = WarStatus.ENDED)
                clanDao.updateWar(updatedWar)
                
                // Trigger end war processing
                // This would typically be done by a server, but for local:
                // ClanWarsManager.getInstance(applicationContext).endWar(war.id)
            }
        }
    }

    companion object {
        private const val TAG = "ClanSyncWorker"
        private const val SYNC_WORK_NAME = "clan_sync"
        private const val SYNC_INTERVAL_MINUTES = 15L

        /**
         * Schedule periodic clan sync
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWork = PeriodicWorkRequestBuilder<ClanSyncWorker>(
                SYNC_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.MINUTES
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
         * Schedule immediate sync
         */
        fun scheduleImmediate(context: Context) {
            val work = OneTimeWorkRequestBuilder<ClanSyncWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "${SYNC_WORK_NAME}_immediate",
                ExistingWorkPolicy.REPLACE,
                work
            )
        }

        /**
         * Cancel sync
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_WORK_NAME)
        }
    }
}
