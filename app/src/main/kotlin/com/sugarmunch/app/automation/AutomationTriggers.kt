package com.sugarmunch.app.automation

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

/**
 * Main trigger evaluator that delegates to category-specific evaluators.
 * Provides a unified interface for evaluating all automation triggers.
 */
class TriggerEvaluator(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val timeEvaluator = TimeTriggerEvaluator()
    private val appEvaluator = AppTriggerEvaluator(context)
    private val systemEvaluator = SystemTriggerEvaluator(context)
    private val sensorEvaluator = SensorTriggerEvaluator(context)
    private val userEvaluator = UserTriggerEvaluator()

    /**
     * Creates a Flow that emits when the trigger condition is met.
     * Delegates to the appropriate category evaluator based on trigger type.
     */
    fun evaluateTrigger(trigger: AutomationTrigger): Flow<TriggerEvent> = when (trigger) {
        // Time-based triggers
        is AutomationTrigger.TimeTrigger -> timeEvaluator.evaluateTimeTrigger(context, trigger)
        is AutomationTrigger.IntervalTrigger -> timeEvaluator.evaluateIntervalTrigger(context, trigger)
        is AutomationTrigger.SunriseSunsetTrigger -> timeEvaluator.evaluateSunriseSunsetTrigger(context, trigger)
        
        // App-based triggers
        is AutomationTrigger.AppOpenedTrigger -> appEvaluator.evaluateAppOpenedTrigger(trigger)
        is AutomationTrigger.AppClosedTrigger -> appEvaluator.evaluateAppClosedTrigger(trigger)
        is AutomationTrigger.AppInstalledTrigger -> appEvaluator.evaluateAppInstalledTrigger(trigger)
        
        // System triggers
        is AutomationTrigger.BatteryLevelTrigger -> systemEvaluator.evaluateBatteryTrigger(trigger)
        is AutomationTrigger.ChargingTrigger -> systemEvaluator.evaluateChargingTrigger(trigger)
        is AutomationTrigger.WifiConnectedTrigger -> systemEvaluator.evaluateWifiTrigger(trigger)
        is AutomationTrigger.BluetoothConnectedTrigger -> systemEvaluator.evaluateBluetoothTrigger(trigger)
        is AutomationTrigger.ScreenStateTrigger -> systemEvaluator.evaluateScreenTrigger(trigger)
        is AutomationTrigger.GeofenceTrigger -> systemEvaluator.evaluateGeofenceTrigger(trigger)
        
        // Sensor triggers
        is AutomationTrigger.ShakeTrigger -> sensorEvaluator.evaluateShakeTrigger(trigger)
        is AutomationTrigger.OrientationTrigger -> sensorEvaluator.evaluateOrientationTrigger(trigger)
        is AutomationTrigger.ProximityTrigger -> sensorEvaluator.evaluateProximityTrigger(trigger)
        
        // User interaction triggers
        is AutomationTrigger.EffectToggledTrigger -> userEvaluator.evaluateEffectToggledTrigger(trigger)
        is AutomationTrigger.ThemeChangedTrigger -> userEvaluator.evaluateThemeChangedTrigger(trigger)
        is AutomationTrigger.RewardClaimedTrigger -> userEvaluator.evaluateRewardClaimedTrigger(trigger)
        is AutomationTrigger.ManualTrigger -> userEvaluator.evaluateManualTrigger(trigger)
    }.catch { e ->
        Log.e(TAG, "Error evaluating trigger: ${trigger.triggerId}", e)
    }

    companion object {
        private const val TAG = "TriggerEvaluator"
    }
}
