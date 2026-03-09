package com.sugarmunch.app.clan.work

import android.content.Context
import androidx.work.*
import com.sugarmunch.app.clan.ClanWarsManager
import com.sugarmunch.app.clan.model.*
import com.sugarmunch.app.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * ClanWarWorker - Handles war-related background tasks
 * 
 * Features:
 * - War end detection and processing
 * - Reward distribution scheduling
 * - War reminder notifications
 */
class ClanWarWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val clanDao = database.clanDao()
    private val warManager = ClanWarsManager.getInstance(context)

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentTime = System.currentTimeMillis()
            
            // Check for wars ending soon
            checkEndingWars(currentTime)
            
            // Process ended wars
            processEndedWars()
            
            // Update war scores
            updateWarScores()
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private suspend fun checkEndingWars(currentTime: Long) {
        val activeWars = clanDao.getAllWarsByStatus(WarStatus.ACTIVE)
        
        activeWars.forEach { war ->
            val timeRemaining = war.endsAt - currentTime
            
            // Send notification 1 hour before war ends
            if (timeRemaining in (59 * 60 * 1000)..(61 * 60 * 1000)) {
                sendWarEndingNotification(war, "1 hour")
            }
            
            // Send notification 24 hours before war ends
            if (timeRemaining in (23 * 60 * 60 * 1000)..(25 * 60 * 60 * 1000)) {
                sendWarEndingNotification(war, "24 hours")
            }
        }
    }

    private suspend fun processEndedWars() {
        val currentTime = System.currentTimeMillis()
        val endedWars = clanDao.getAllWarsByStatus(WarStatus.ENDED)
        
        endedWars.forEach { war ->
            // Calculate final results
            val homeScore = warManager.calculateClanScore(war.homeClanId)
            
            // Update war with final scores
            val finalWar = war.copy(
                homeScore = homeScore,
                status = WarStatus.COMPLETED
            )
            
            clanDao.updateWar(finalWar)
            
            // Distribute rewards
            warManager.endWar(war.id)
        }
    }

    private suspend fun updateWarScores() {
        val activeWars = clanDao.getAllWarsByStatus(WarStatus.ACTIVE)
        
        activeWars.forEach { war ->
            // Recalculate scores based on current member contributions
            val homeScore = warManager.calculateClanScore(war.homeClanId)
            
            // In a real implementation, would also get enemy score from server
            // For now, simulate enemy score progression
            val updatedWar = war.copy(homeScore = homeScore)
            clanDao.updateWar(updatedWar)
        }
    }

    private fun sendWarEndingNotification(war: ClanWar, timeRemaining: String) {
        // In a real implementation, would send a notification
        // NotificationHelper.sendWarEndingNotification(applicationContext, war, timeRemaining)
    }

    companion object {
        private const val TAG = "ClanWarWorker"
        private const val WAR_WORK_NAME = "clan_war_processor"
        private const val CHECK_INTERVAL_MINUTES = 5L

        /**
         * Schedule periodic war checks
         */
        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val warWork = PeriodicWorkRequestBuilder<ClanWarWorker>(
                CHECK_INTERVAL_MINUTES, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WAR_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                warWork
            )
        }

        /**
         * Schedule war end processing
         */
        fun scheduleWarEnd(context: Context, warId: String, delayMillis: Long) {
            val inputData = workDataOf("war_id" to warId)
            
            val warEndWork = OneTimeWorkRequestBuilder<ClanWarEndWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("${TAG}_end_$warId")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "war_end_$warId",
                ExistingWorkPolicy.REPLACE,
                warEndWork
            )
        }

        /**
         * Cancel all war workers
         */
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WAR_WORK_NAME)
        }
    }
}

/**
 * Worker specifically for handling war end processing
 */
class ClanWarEndWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val warManager = ClanWarsManager.getInstance(context)

    override suspend fun doWork(): Result {
        val warId = inputData.getString("war_id") ?: return Result.failure()
        
        return try {
            warManager.endWar(warId)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
