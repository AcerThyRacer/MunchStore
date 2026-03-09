package com.sugarmunch.app.automation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.work.*
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * TaskScheduler - Handles background scheduling for automation tasks
 * Integrates with WorkManager, AlarmManager, and Geofencing
 */
class TaskScheduler private constructor(private val context: Context) {
    
    private val TAG = "TaskScheduler"
    
    private val workManager = WorkManager.getInstance(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val geofencingClient = LocationServices.getGeofencingClient(context)
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // ═════════════════════════════════════════════════════════════
    // WORKER CLASSES
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Worker for executing automation tasks
     */
    class AutomationWorker(
        context: Context,
        params: WorkerParameters
    ) : CoroutineWorker(context, params) {
        
        override suspend fun doWork(): Result {
            val taskId = inputData.getString(KEY_TASK_ID) ?: return Result.failure()
            val triggeredBy = inputData.getString(KEY_TRIGGERED_BY) ?: "worker"
            
            return try {
                val engine = AutomationEngine.getInstance(applicationContext)
                val record = engine.runTask(taskId, triggeredBy)
                
                if (record.status == ExecutionRecord.ExecutionStatus.SUCCESS) {
                    Result.success()
                } else {
                    Result.retry()
                }
            } catch (e: Exception) {
                Log.e("AutomationWorker", "Error executing task", e)
                Result.retry()
            }
        }
        
        companion object {
            const val KEY_TASK_ID = "task_id"
            const val KEY_TRIGGERED_BY = "triggered_by"
            
            fun buildRequest(taskId: String, triggeredBy: String): OneTimeWorkRequest {
                val data = workDataOf(
                    KEY_TASK_ID to taskId,
                    KEY_TRIGGERED_BY to triggeredBy
                )
                
                return OneTimeWorkRequestBuilder<AutomationWorker>()
                    .setInputData(data)
                    .addTag("automation_$taskId")
                    .build()
            }
        }
    }
    
    /**
     * Worker for periodic automation checks
     */
    class AutomationPeriodicWorker(
        context: Context,
        params: WorkerParameters
    ) : CoroutineWorker(context, params) {
        
        override suspend fun doWork(): Result {
            // Check and trigger any pending automations
            // This is used for interval-based triggers
            return Result.success()
        }
    }
    
    // ═════════════════════════════════════════════════════════════
    // SCHEDULING METHODS
    // ═════════════════════════════════════════════════════════════
    
    /**
     * Schedule a task using the appropriate mechanism based on trigger type
     */
    fun schedule(task: AutomationTask, triggerTime: Long? = null) {
        when (val trigger = task.trigger) {
            is AutomationTrigger.TimeTrigger -> scheduleTimeTrigger(task, trigger)
            is AutomationTrigger.IntervalTrigger -> scheduleIntervalTrigger(task, trigger)
            is AutomationTrigger.GeofenceTrigger -> scheduleGeofenceTrigger(task, trigger)
            else -> scheduleWithWorkManager(task)
        }
    }
    
    /**
     * Cancel all scheduled executions for a task
     */
    fun cancel(taskId: String) {
        // Cancel WorkManager work
        workManager.cancelAllWorkByTag("automation_$taskId")
        
        // Cancel AlarmManager alarms
        val intent = Intent(context, AutomationAlarmReceiver::class.java).apply {
            putExtra(AutomationAlarmReceiver.EXTRA_TASK_ID, taskId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        
        // Cancel geofences
        geofencingClient.removeGeofences(listOf(taskId))
        
        Log.d(TAG, "Cancelled all scheduling for task: $taskId")
    }
    
    /**
     * Reschedule a task (useful after device reboot)
     */
    fun reschedule(task: AutomationTask) {
        cancel(task.id)
        schedule(task)
    }
    
    /**
     * Schedule a one-time execution at a specific time using AlarmManager
     */
    fun scheduleOneTime(taskId: String, executionTime: Long) {
        val intent = Intent(context, AutomationAlarmReceiver::class.java).apply {
            putExtra(AutomationAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AutomationAlarmReceiver.EXTRA_TRIGGER_TYPE, "alarm")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    executionTime,
                    pendingIntent
                )
            } else {
                // Fall back to inexact alarm
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    executionTime,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                executionTime,
                pendingIntent
            )
        }
        
        Log.d(TAG, "Scheduled one-time alarm for task: $taskId at $executionTime")
    }
    
    /**
     * Schedule a daily repeating task
     */
    fun scheduleDaily(taskId: String, hour: Int, minute: Int) {
        val intent = Intent(context, AutomationAlarmReceiver::class.java).apply {
            putExtra(AutomationAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AutomationAlarmReceiver.EXTRA_TRIGGER_TYPE, "daily")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Calculate first trigger time
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            
            if (timeInMillis < System.currentTimeMillis()) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
        
        Log.d(TAG, "Scheduled daily alarm for task: $taskId at $hour:$minute")
    }
    
    /**
     * Schedule with WorkManager (flexible timing, battery-friendly)
     */
    fun scheduleWithWorkManager(task: AutomationTask) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<AutomationPeriodicWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("automation_${task.id}")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "periodic_${task.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        
        Log.d(TAG, "Scheduled with WorkManager: ${task.id}")
    }
    
    /**
     * Execute a task immediately using WorkManager
     */
    fun executeNow(taskId: String, triggeredBy: String = "manual") {
        val workRequest = AutomationWorker.buildRequest(taskId, triggeredBy)
        workManager.enqueueUniqueWork(
            "immediate_$taskId",
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        Log.d(TAG, "Enqueued immediate execution: $taskId")
    }
    
    // ═════════════════════════════════════════════════════════════
    // PRIVATE SCHEDULING IMPLEMENTATIONS
    // ═════════════════════════════════════════════════════════════
    
    private fun scheduleTimeTrigger(task: AutomationTask, trigger: AutomationTrigger.TimeTrigger) {
        if (trigger.repeatDays.isEmpty()) {
            // One-time schedule
            val calendar = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, trigger.hour)
                set(java.util.Calendar.MINUTE, trigger.minute)
                set(java.util.Calendar.SECOND, 0)
                
                if (timeInMillis < System.currentTimeMillis()) {
                    add(java.util.Calendar.DAY_OF_YEAR, 1)
                }
            }
            scheduleOneTime(task.id, calendar.timeInMillis)
        } else {
            // Schedule for each day
            scheduleDaily(task.id, trigger.hour, trigger.minute)
        }
    }
    
    private fun scheduleIntervalTrigger(task: AutomationTask, trigger: AutomationTrigger.IntervalTrigger) {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<AutomationPeriodicWorker>(
            trigger.intervalMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .addTag("automation_${task.id}")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "interval_${task.id}",
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
        
        Log.d(TAG, "Scheduled interval trigger for task: ${task.id}")
    }
    
    private fun scheduleGeofenceTrigger(
        task: AutomationTask, 
        trigger: AutomationTrigger.GeofenceTrigger
    ) {
        val geofence = Geofence.Builder()
            .setRequestId(task.id)
            .setCircularRegion(
                trigger.latitude,
                trigger.longitude,
                trigger.radiusMeters
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                when (trigger.transition) {
                    AutomationTrigger.GeofenceTrigger.GeofenceTransition.ENTER -> 
                        Geofence.GEOFENCE_TRANSITION_ENTER
                    AutomationTrigger.GeofenceTrigger.GeofenceTransition.EXIT -> 
                        Geofence.GEOFENCE_TRANSITION_EXIT
                    AutomationTrigger.GeofenceTrigger.GeofenceTransition.DWELL -> 
                        Geofence.GEOFENCE_TRANSITION_DWELL
                }
            )
            .build()
        
        val request = GeofencingRequest.Builder()
            .addGeofence(geofence)
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .build()
        
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            putExtra(GeofenceBroadcastReceiver.EXTRA_TASK_ID, task.id)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        
        try {
            geofencingClient.addGeofences(request, pendingIntent)
                .addOnSuccessListener {
                    Log.d(TAG, "Geofence added for task: ${task.id}")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Failed to add geofence for task: ${task.id}", e)
                }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission not granted", e)
        }
    }
    
    // ═════════════════════════════════════════════════════════════
    // BOOT RECEIVER
    // ═════════════════════════════════════════════════════════════
    
    class BootReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                Log.d(TAG, "Device booted, rescheduling automations")
                
                val scheduler = getInstance(context)
                val repository = AutomationRepository.getInstance(context)
                
                scheduler.scope.launch {
                    val tasks = repository.getEnabledTasks()
                    tasks.forEach { task ->
                        scheduler.reschedule(task)
                    }
                    Log.d(TAG, "Rescheduled ${tasks.size} tasks after boot")
                }
            }
        }
    }
    
    // ═════════════════════════════════════════════════════════════
    // BROADCAST RECEIVERS
    // ═════════════════════════════════════════════════════════════
    
    class AutomationAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
            val triggerType = intent.getStringExtra(EXTRA_TRIGGER_TYPE) ?: "alarm"
            
            Log.d(TAG, "Alarm received for task: $taskId")
            
            val scheduler = getInstance(context)
            scheduler.executeNow(taskId, triggerType)
        }
        
        companion object {
            const val EXTRA_TASK_ID = "task_id"
            const val EXTRA_TRIGGER_TYPE = "trigger_type"
        }
    }
    
    class GeofenceBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return
            
            if (geofencingEvent.hasError()) {
                Log.e(TAG, "Geofence error: ${geofencingEvent.errorCode}")
                return
            }
            
            val geofenceTransition = geofencingEvent.geofenceTransition
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            
            triggeringGeofences?.forEach { geofence ->
                val taskId = geofence.requestId
                val transitionType = when (geofenceTransition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> "geofence_enter"
                    Geofence.GEOFENCE_TRANSITION_EXIT -> "geofence_exit"
                    Geofence.GEOFENCE_TRANSITION_DWELL -> "geofence_dwell"
                    else -> "geofence"
                }
                
                Log.d(TAG, "Geofence triggered: $taskId - $transitionType")
                
                val scheduler = getInstance(context)
                scheduler.executeNow(taskId, transitionType)
            }
        }
        
        companion object {
            const val EXTRA_TASK_ID = "task_id"
        }
    }
    
    // ═════════════════════════════════════════════════════════════
    // COMPANION OBJECT
    // ═════════════════════════════════════════════════════════════
    
    companion object {
        private const val TAG = "TaskScheduler"
        
        @Volatile
        private var instance: TaskScheduler? = null
        
        val scope: CoroutineScope
            get() = instance?.scope ?: CoroutineScope(SupervisorJob() + Dispatchers.Default)
        
        fun getInstance(context: Context): TaskScheduler {
            return instance ?: synchronized(this) {
                instance ?: TaskScheduler(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════
// EXTENSION FUNCTIONS FOR WORK MANAGER
// ═════════════════════════════════════════════════════════════

fun Constraints.Builder.setRequiresBatteryNotLow(requires: Boolean): Constraints.Builder {
    return if (requires) {
        this.setRequiresBatteryNotLow(true)
    } else {
        this
    }
}
