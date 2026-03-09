package com.sugarmunch.app.automation

import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import java.util.*

/**
 * Time-based trigger evaluation functions.
 * Handles TimeTrigger, IntervalTrigger, and SunriseSunsetTrigger.
 */
internal class TimeTriggerEvaluator {

    /**
     * Evaluates time-based triggers that fire at specific times.
     */
    fun evaluateTimeTrigger(context: Context, trigger: AutomationTrigger.TimeTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val timer = Timer()
            val calendar = Calendar.getInstance()

            val task = object : TimerTask() {
                override fun run() {
                    val now = Calendar.getInstance()
                    val currentHour = now.get(Calendar.HOUR_OF_DAY)
                    val currentMinute = now.get(Calendar.MINUTE)
                    val currentDay = now.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday

                    if (currentHour == trigger.hour && currentMinute == trigger.minute) {
                        if (trigger.repeatDays.isEmpty() || currentDay in trigger.repeatDays) {
                            trySend(TriggerEvent.TimeEvent(trigger.hour, trigger.minute))
                        }
                    }
                }
            }

            // Check every minute
            timer.scheduleAtFixedRate(task, 0, 60000)

            awaitClose { timer.cancel() }
        }

    /**
     * Evaluates interval-based triggers that fire at regular intervals.
     */
    fun evaluateIntervalTrigger(context: Context, trigger: AutomationTrigger.IntervalTrigger): Flow<TriggerEvent> =
        flow {
            while (currentCoroutineContext().isActive) {
                val now = System.currentTimeMillis()
                val shouldTrigger = when {
                    trigger.startTime != null && now < trigger.startTime -> false
                    trigger.endTime != null && now > trigger.endTime -> false
                    else -> true
                }

                if (shouldTrigger) {
                    emit(TriggerEvent.IntervalEvent(trigger.intervalMinutes))
                }
                delay(trigger.intervalMinutes * 60 * 1000)
            }
        }

    /**
     * Evaluates sunrise/sunset triggers that fire at solar events.
     */
    fun evaluateSunriseSunsetTrigger(context: Context, trigger: AutomationTrigger.SunriseSunsetTrigger): Flow<TriggerEvent> =
        flow {
            // Simplified sun calculation - in production, use a proper library
            while (currentCoroutineContext().isActive) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)

                val eventHour = when (trigger.event) {
                    AutomationTrigger.SunriseSunsetTrigger.SunEvent.SUNRISE -> 6
                    AutomationTrigger.SunriseSunsetTrigger.SunEvent.SUNSET -> 18
                }

                val adjustedHour = (eventHour + trigger.offsetMinutes / 60).coerceIn(0, 23)

                if (hour == adjustedHour) {
                    emit(TriggerEvent.SunEvent(trigger.event))
                }

                delay(60 * 60 * 1000) // Check every hour
            }
        }

    companion object {
        private const val TAG = "TimeTriggerEvaluator"
    }
}
