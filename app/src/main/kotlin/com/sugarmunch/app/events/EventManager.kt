package com.sugarmunch.app.events

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.work.*
import com.sugarmunch.app.MainActivity
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

/**
 * SugarMunch Event Manager
 * 
 * Manages the complete lifecycle of seasonal events:
 * - Automatic event detection based on calendar
 * - Event scheduling with WorkManager
 * - Active event tracking
 * - Event notifications and reminders
 * - Event state persistence
 */

enum class EventNotificationType {
    EVENT_STARTED,
    EVENT_ENDING_SOON,
    EVENT_ENDED,
    DAILY_REMINDER,
    CHALLENGE_COMPLETED,
    REWARD_UNLOCKED
}

data class EventState(
    val eventId: String,
    val status: EventStatus,
    val startDate: Long,
    val endDate: Long,
    val totalPoints: Int = 0,
    val completedChallenges: Int = 0,
    val claimedRewards: List<String> = emptyList(),
    val lastActiveAt: Long = System.currentTimeMillis(),
    val notificationSent: Map<EventNotificationType, Boolean> = emptyMap()
)

data class EventSummary(
    val activeEvents: List<SeasonalEvent>,
    val upcomingEvents: List<SeasonalEvent>,
    val recentlyEnded: List<SeasonalEvent>,
    val totalActivePoints: Int = 0
)

private val Context.eventDataStore: DataStore<Preferences> by preferencesDataStore(name = "event_manager")

class EventManager(private val context: Context) {
    private val dataStore = context.eventDataStore
    private val challengeManager = ChallengeProgressManager.getInstance(context)
    private val rewardsManager = EventRewardsManager.getInstance(context)
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    companion object {
        @Volatile
        private var instance: EventManager? = null
        
        const val EVENT_NOTIFICATION_CHANNEL_ID = "event_notifications"
        const val EVENT_WORK_TAG = "event_scheduler"
        const val EVENT_CHECK_WORK_NAME = "event_check"
        
        fun getInstance(context: Context): EventManager {
            return instance ?: synchronized(this) {
                instance ?: EventManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
    
    init {
        createNotificationChannel()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                EVENT_NOTIFICATION_CHANNEL_ID,
                "Seasonal Events",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for seasonal events, challenges, and rewards"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Event Lifecycle Management
    // ═════════════════════════════════════════════════════════════════
    
    fun getActiveEvents(date: LocalDate = LocalDate.now()): Flow<List<SeasonalEvent>> {
        return flow {
            val activeEvents = SeasonalEvents.ALL_EVENTS.filter { it.isActiveOn(date) }
            
            // Check for anniversary
            val anniversary = getAnniversaryEvent()
            if (anniversary?.isActiveOn(date) == true) {
                emit(activeEvents + anniversary)
            } else {
                emit(activeEvents)
            }
        }
    }
    
    fun getUpcomingEvents(date: LocalDate = LocalDate.now(), daysAhead: Long = 30): Flow<List<SeasonalEvent>> {
        return flow {
            val upcoming = SeasonalEvents.ALL_EVENTS
                .filter { !it.isActiveOn(date) && it.daysUntilStart(date) in 0..daysAhead }
                .sortedBy { it.daysUntilStart(date) }
            emit(upcoming)
        }
    }
    
    suspend fun getEventState(eventId: String): EventState? {
        return dataStore.data.map { prefs ->
            val exists = prefs[booleanPreferencesKey("event_${eventId}_initialized")] ?: false
            if (!exists) return@map null
            
            EventState(
                eventId = eventId,
                status = prefs[stringPreferencesKey("event_${eventId}_status")]
                    ?.let { EventStatus.valueOf(it) } ?: EventStatus.UPCOMING,
                startDate = prefs[longPreferencesKey("event_${eventId}_start")] ?: 0,
                endDate = prefs[longPreferencesKey("event_${eventId}_end")] ?: 0,
                totalPoints = prefs[intPreferencesKey("event_${eventId}_points")] ?: 0,
                completedChallenges = prefs[intPreferencesKey("event_${eventId}_challenges")] ?: 0,
                claimedRewards = prefs[stringPreferencesKey("event_${eventId}_rewards")]
                    ?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
                lastActiveAt = prefs[longPreferencesKey("event_${eventId}_active")] ?: System.currentTimeMillis()
            )
        }.first()
    }
    
    suspend fun startEvent(event: SeasonalEvent) {
        val now = System.currentTimeMillis()
        
        dataStore.edit { prefs ->
            prefs[booleanPreferencesKey("event_${event.id}_initialized")] = true
            prefs[stringPreferencesKey("event_${event.id}_status")] = EventStatus.ACTIVE.name
            prefs[longPreferencesKey("event_${event.id}_start")] = event.getStartDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            prefs[longPreferencesKey("event_${event.id}_end")] = event.getEndDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            prefs[longPreferencesKey("event_${event.id}_active")] = now
        }
        
        // Reset challenges for this event
        challengeManager.resetEventChallenges(event.id)
        
        // Send notification
        sendEventNotification(
            EventNotificationType.EVENT_STARTED,
            "🎉 ${event.name} is Here!",
            event.shortDescription,
            event.accentColor.hashCode()
        )
    }
    
    suspend fun endEvent(eventId: String) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("event_${eventId}_status")] = EventStatus.ENDED.name
        }
        
        val event = SeasonalEvents.getEventById(eventId)
        if (event != null) {
            sendEventNotification(
                EventNotificationType.EVENT_ENDED,
                "👋 ${event.name} Has Ended",
                "Thanks for participating! See you next year!",
                event.accentColor.hashCode()
            )
        }
    }
    
    suspend fun checkEventStatus() {
        val today = LocalDate.now()
        
        // Check all events
        SeasonalEvents.ALL_EVENTS.forEach { event ->
            val state = getEventState(event.id)
            val currentStatus = event.getStatus(today)
            
            when {
                // Event just started
                currentStatus == EventStatus.ACTIVE && state == null -> {
                    startEvent(event)
                }
                // Event ending soon
                currentStatus == EventStatus.ENDING_SOON && state?.status == EventStatus.ACTIVE -> {
                    markEventEndingSoon(event)
                }
                // Event ended
                currentStatus == EventStatus.ENDED && state?.status != EventStatus.ENDED -> {
                    endEvent(event.id)
                }
            }
        }
        
        // Check for daily reset
        if (challengeManager.shouldResetDaily()) {
            challengeManager.resetDailyChallenges()
            challengeManager.recordDailyReset()
        }
    }
    
    private suspend fun markEventEndingSoon(event: SeasonalEvent) {
        dataStore.edit { prefs ->
            prefs[stringPreferencesKey("event_${event.id}_status")] = EventStatus.ENDING_SOON.name
        }
        
        sendEventNotification(
            EventNotificationType.EVENT_ENDING_SOON,
            "⏰ ${event.name} Ends Soon!",
            "Only ${event.daysRemaining()} days left to claim your rewards!",
            event.accentColor.hashCode()
        )
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Notifications
    // ═════════════════════════════════════════════════════════════════
    
    fun sendEventNotification(
        type: EventNotificationType,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to_events", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, EVENT_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
    
    suspend fun notifyChallengeCompleted(challengeName: String, points: Int) {
        sendEventNotification(
            EventNotificationType.CHALLENGE_COMPLETED,
            "✅ Challenge Complete!",
            "You completed '$challengeName' and earned $points points!",
            challengeName.hashCode()
        )
    }
    
    suspend fun notifyRewardUnlocked(rewardName: String) {
        sendEventNotification(
            EventNotificationType.REWARD_UNLOCKED,
            "🎁 Reward Unlocked!",
            "'$rewardName' is now available to claim!",
            rewardName.hashCode()
        )
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Event Scheduling with WorkManager
    // ═════════════════════════════════════════════════════════════════
    
    fun scheduleEventChecks() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(false)
            .build()
        
        // Daily check at midnight
        val dailyCheck = PeriodicWorkRequestBuilder<EventCheckWorker>(24, TimeUnit.HOURS)
            .setConstraints(constraints)
            .addTag(EVENT_WORK_TAG)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EVENT_CHECK_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyCheck
        )
        
        // Immediate check
        val immediateCheck = OneTimeWorkRequestBuilder<EventCheckWorker>()
            .addTag(EVENT_WORK_TAG)
            .build()
        
        WorkManager.getInstance(context).enqueue(immediateCheck)
    }
    
    fun cancelEventChecks() {
        WorkManager.getInstance(context).cancelAllWorkByTag(EVENT_WORK_TAG)
    }
    
    fun scheduleEventReminder(event: SeasonalEvent, daysBefore: Long = 1) {
        val reminderDate = event.getStartDate().minusDays(daysBefore)
        val delayMillis = ChronoUnit.MILLIS.between(
            LocalDate.now().atStartOfDay(),
            reminderDate.atStartOfDay()
        )
        
        if (delayMillis > 0) {
            val reminderWork = OneTimeWorkRequestBuilder<EventReminderWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf(
                    "event_id" to event.id,
                    "event_name" to event.name,
                    "event_description" to event.shortDescription
                ))
                .addTag(EVENT_WORK_TAG)
                .build()
            
            WorkManager.getInstance(context).enqueueUniqueWork(
                "reminder_${event.id}",
                ExistingWorkPolicy.REPLACE,
                reminderWork
            )
        }
    }
    
    // ═════════════════════════════════════════════════════════════════
    // Event Summary & Stats
    // ═════════════════════════════════════════════════════════════════
    
    suspend fun getEventSummary(): EventSummary {
        val today = LocalDate.now()
        val activeEvents = getActiveEvents(today).first()
        val upcomingEvents = getUpcomingEvents(today).first()
        
        val totalPoints = activeEvents.sumOf { event ->
            challengeManager.getTotalEventPoints(event.id)
        }
        
        return EventSummary(
            activeEvents = activeEvents,
            upcomingEvents = upcomingEvents,
            recentlyEnded = emptyList(), // Would track from state
            totalActivePoints = totalPoints
        )
    }
    
    suspend fun updateEventActivity(eventId: String) {
        dataStore.edit { prefs ->
            prefs[longPreferencesKey("event_${eventId}_active")] = System.currentTimeMillis()
        }
    }
    
    suspend fun getEventParticipationDays(eventId: String): Int {
        val state = getEventState(eventId) ?: return 0
        val startDate = LocalDate.ofEpochDay(state.startDate / (24 * 60 * 60 * 1000))
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(startDate, today).toInt().coerceAtLeast(0)
    }
    
    private fun getAnniversaryEvent(): SeasonalEvent? {
        // Get app install date - this would come from actual app data
        val appBirthday = LocalDate.of(2023, 6, 15) // Example date
        return SeasonalEvents.getAnniversaryEvent(appBirthday)
    }
}

/**
 * Worker for checking event status periodically
 */
class EventCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val eventManager = EventManager.getInstance(applicationContext)
            eventManager.checkEventStatus()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

/**
 * Worker for sending event reminders
 */
class EventReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val eventName = inputData.getString("event_name") ?: return Result.failure()
        val eventDescription = inputData.getString("event_description") ?: ""
        
        val eventManager = EventManager.getInstance(applicationContext)
        eventManager.sendEventNotification(
            EventNotificationType.DAILY_REMINDER,
            "🎊 $eventName Starts Tomorrow!",
            "Get ready for $eventDescription",
            eventName.hashCode()
        )
        
        return Result.success()
    }
}
