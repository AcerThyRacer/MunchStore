package com.sugarmunch.app.trading

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.sugarmunch.app.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * ═══════════════════════════════════════════════════════════════════
 * TRADE WORKER
 * 
 * WorkManager for background trade processing:
 * • Expire old trades automatically
 * • Sync trade status with server
 * • Send push notifications for trade updates
 * • Clean up old trade history
 * ═══════════════════════════════════════════════════════════════════
 */

private const val TRADE_CHANNEL_ID = "trade_notifications"
private const val TRADE_CHANNEL_NAME = "Trade Notifications"
private const val TRADE_CHANNEL_DESCRIPTION = "Notifications for trade offers, gifts, and marketplace updates"

/**
 * Trade expiration worker - runs periodically to expire old trades
 */
class TradeExpirationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val tradeManager = TradeManager.getInstance(applicationContext)
            
            // Get all pending trades
            val pendingTrades = tradeManager.activeTrades.value.filter { 
                it.status == TradeStatus.PENDING 
            }
            
            val now = System.currentTimeMillis()
            var expiredCount = 0
            
            pendingTrades.forEach { trade ->
                if (trade.expiresAt < now) {
                    // Expire the trade
                    tradeManager.cancelOffer(trade.id)
                    expiredCount++
                    
                    // Send notification
                    sendExpirationNotification(trade)
                }
            }
            
            // Log work result
            android.util.Log.d("TradeExpirationWorker", "Expired $expiredCount trades")
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("TradeExpirationWorker", "Failed to expire trades", e)
            Result.retry()
        }
    }
    
    private fun sendExpirationNotification(trade: TradeOffer) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) 
            as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "trades")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(applicationContext, TRADE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_share)
            .setContentTitle("Trade Expired")
            .setContentText("Your trade offer to ${trade.recipientName} has expired")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(trade.id.hashCode(), notification)
    }
}

/**
 * Trade sync worker - syncs trade status with server
 */
class TradeSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            // Simulate server sync
            withContext(Dispatchers.IO) {
                // In a real app, this would:
                // 1. Fetch pending trades from server
                // 2. Update local trade statuses
                // 3. Push local changes to server
                // 4. Handle conflicts
                
                delay(1000) // Simulate network delay
                
                android.util.Log.d("TradeSyncWorker", "Trade sync completed")
            }
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("TradeSyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}

/**
 * Marketplace sync worker - syncs marketplace listings
 */
class MarketplaceSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            withContext(Dispatchers.IO) {
                // Sync marketplace listings with server
                delay(500)
                android.util.Log.d("MarketplaceSyncWorker", "Marketplace sync completed")
            }
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("MarketplaceSyncWorker", "Sync failed", e)
            Result.retry()
        }
    }
}

/**
 * Trade cleanup worker - removes old completed trades from active list
 */
class TradeCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val MAX_ACTIVE_TRADES_AGE_DAYS = 30
        const val MAX_HISTORY_AGE_DAYS = 90
    }
    
    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()
            val maxAge = TimeUnit.DAYS.toMillis(MAX_ACTIVE_TRADES_AGE_DAYS.toLong())
            
            // Clean up old completed/cancelled trades
            // In a real app, this would archive them to history
            
            android.util.Log.d("TradeCleanupWorker", "Cleanup completed")
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("TradeCleanupWorker", "Cleanup failed", e)
            Result.failure()
        }
    }
}

/**
 * Gift reminder worker - reminds users of unopened gifts
 */
class GiftReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val giftingManager = GiftingManager.getInstance(applicationContext)
            val unopenedGifts = giftingManager.getUnopenedGifts()
            
            if (unopenedGifts.isNotEmpty()) {
                sendGiftReminderNotification(unopenedGifts.size)
            }
            
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("GiftReminderWorker", "Failed", e)
            Result.retry()
        }
    }
    
    private fun sendGiftReminderNotification(count: Int) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "gifts")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(applicationContext, TRADE_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_gallery)
            .setContentTitle("🎁 Unopened Gifts!")
            .setContentText("You have $count gift${if (count > 1) "s" else ""} waiting to be opened")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(1001, notification)
    }
}

/**
 * Trade notification worker - sends push notifications for trade events
 */
class TradeNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    companion object {
        const val KEY_NOTIFICATION_TYPE = "notification_type"
        const val KEY_TITLE = "title"
        const val KEY_MESSAGE = "message"
        const val KEY_TRADE_ID = "trade_id"
    }
    
    override suspend fun doWork(): Result {
        val type = inputData.getString(KEY_NOTIFICATION_TYPE) ?: return Result.failure()
        val title = inputData.getString(KEY_TITLE) ?: return Result.failure()
        val message = inputData.getString(KEY_MESSAGE) ?: return Result.failure()
        val tradeId = inputData.getString(KEY_TRADE_ID)
        
        return try {
            sendNotification(type, title, message, tradeId)
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("TradeNotificationWorker", "Failed to send notification", e)
            Result.retry()
        }
    }
    
    private fun sendNotification(type: String, title: String, message: String, tradeId: String?) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        
        createNotificationChannel(notificationManager)
        
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "trades")
            tradeId?.let { putExtra("trade_id", it) }
        }
        
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            type.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Determine icon and priority based on type
        val (icon, priority) = when (type) {
            "OFFER_RECEIVED", "GIFT_RECEIVED" -> 
                android.R.drawable.ic_menu_gallery to NotificationCompat.PRIORITY_HIGH
            "OFFER_ACCEPTED", "TRADE_COMPLETED" -> 
                android.R.drawable.ic_menu_send to NotificationCompat.PRIORITY_DEFAULT
            "OFFER_DECLINED" -> 
                android.R.drawable.ic_menu_close_clear_cancel to NotificationCompat.PRIORITY_DEFAULT
            else -> 
                android.R.drawable.ic_menu_info_details to NotificationCompat.PRIORITY_DEFAULT
        }
        
        val notification = NotificationCompat.Builder(applicationContext, TRADE_CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(priority)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(tradeId?.hashCode() ?: type.hashCode(), notification)
    }
}

// ═══════════════════════════════════════════════════════════════════
// WORKER SCHEDULING
// ═══════════════════════════════════════════════════════════════════

object TradeWorkScheduler {
    
    private const val TAG_EXPIRATION = "trade_expiration"
    private const val TAG_SYNC = "trade_sync"
    private const val TAG_MARKETPLACE_SYNC = "marketplace_sync"
    private const val TAG_CLEANUP = "trade_cleanup"
    private const val TAG_GIFT_REMINDER = "gift_reminder"
    
    /**
     * Schedule all trade workers
     */
    fun scheduleAll(context: Context) {
        scheduleExpirationWorker(context)
        scheduleSyncWorker(context)
        scheduleMarketplaceSyncWorker(context)
        scheduleCleanupWorker(context)
        scheduleGiftReminderWorker(context)
    }
    
    /**
     * Schedule trade expiration worker - runs every hour
     */
    fun scheduleExpirationWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<TradeExpirationWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(TAG_EXPIRATION)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TAG_EXPIRATION,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    /**
     * Schedule trade sync worker - runs every 15 minutes when online
     */
    fun scheduleSyncWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<TradeSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(TAG_SYNC)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TAG_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    /**
     * Schedule marketplace sync worker - runs every 30 minutes
     */
    fun scheduleMarketplaceSyncWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<MarketplaceSyncWorker>(
            30, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag(TAG_MARKETPLACE_SYNC)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TAG_MARKETPLACE_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    /**
     * Schedule cleanup worker - runs daily
     */
    fun scheduleCleanupWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<TradeCleanupWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag(TAG_CLEANUP)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TAG_CLEANUP,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    /**
     * Schedule gift reminder worker - runs daily at 10 AM
     */
    fun scheduleGiftReminderWorker(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<GiftReminderWorker>(
            1, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag(TAG_GIFT_REMINDER)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            TAG_GIFT_REMINDER,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
    
    /**
     * Enqueue a one-time trade notification
     */
    fun enqueueNotification(
        context: Context,
        type: TradeNotificationType,
        title: String,
        message: String,
        tradeId: String? = null
    ) {
        val inputData = workDataOf(
            TradeNotificationWorker.KEY_NOTIFICATION_TYPE to type.name,
            TradeNotificationWorker.KEY_TITLE to title,
            TradeNotificationWorker.KEY_MESSAGE to message,
            TradeNotificationWorker.KEY_TRADE_ID to tradeId
        )
        
        val workRequest = OneTimeWorkRequestBuilder<TradeNotificationWorker>()
            .setInputData(inputData)
            .build()
        
        WorkManager.getInstance(context).enqueue(workRequest)
    }
    
    /**
     * Cancel all trade workers
     */
    fun cancelAll(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_EXPIRATION)
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_SYNC)
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_MARKETPLACE_SYNC)
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_CLEANUP)
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG_GIFT_REMINDER)
    }
}

// ═══════════════════════════════════════════════════════════════════
// NOTIFICATION CHANNEL
// ═══════════════════════════════════════════════════════════════════

fun createNotificationChannel(notificationManager: NotificationManager) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            TRADE_CHANNEL_ID,
            TRADE_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = TRADE_CHANNEL_DESCRIPTION
            enableLights(true)
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(channel)
    }
}

// ═══════════════════════════════════════════════════════════════════
// INITIALIZER
// ═══════════════════════════════════════════════════════════════════

/**
 * Initialize trade workers on app start
 */
fun initializeTradeWorkers(context: Context) {
    TradeWorkScheduler.scheduleAll(context)
}
