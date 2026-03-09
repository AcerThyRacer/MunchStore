package com.sugarmunch.app.automation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Events emitted when triggers are evaluated and fired.
 * Each event corresponds to a specific trigger type and carries relevant data.
 */
sealed class TriggerEvent {
    /** Time-based trigger event */
    data class TimeEvent(val hour: Int, val minute: Int) : TriggerEvent()
    
    /** Interval-based trigger event */
    data class IntervalEvent(val intervalMinutes: Long) : TriggerEvent()
    
    /** Sunrise/sunset trigger event */
    data class SunEvent(val event: AutomationTrigger.SunriseSunsetTrigger.SunEvent) : TriggerEvent()
    
    /** App opened trigger event */
    data class AppOpenedEvent(val packageName: String) : TriggerEvent()
    
    /** App closed trigger event */
    data class AppClosedEvent(val packageName: String) : TriggerEvent()
    
    /** App installed trigger event */
    data class AppInstalledEvent(val packageName: String) : TriggerEvent()
    
    /** Effect toggled trigger event */
    data class EffectToggledEvent(val effectId: String, val enabled: Boolean) : TriggerEvent()
    
    /** Theme changed trigger event */
    data class ThemeChangedEvent(val themeId: String) : TriggerEvent()
    
    /** Reward claimed trigger event */
    data class RewardClaimedEvent(val rewardType: String?) : TriggerEvent()
    
    /** Battery level trigger event */
    data class BatteryEvent(val level: Int) : TriggerEvent()
    
    /** Charging state trigger event */
    data class ChargingEvent(val state: AutomationTrigger.ChargingTrigger.ChargingState) : TriggerEvent()
    
    /** WiFi connection trigger event */
    data class WifiEvent(val ssid: String) : TriggerEvent()
    
    /** Bluetooth connection trigger event */
    data class BluetoothEvent(val deviceName: String, val address: String) : TriggerEvent()
    
    /** Screen state trigger event */
    data class ScreenEvent(val state: AutomationTrigger.ScreenStateTrigger.ScreenState) : TriggerEvent()
    
    /** Geofence transition trigger event */
    data class GeofenceEvent(
        val transition: AutomationTrigger.GeofenceTrigger.GeofenceTransition,
        val distance: Float
    ) : TriggerEvent()
    
    /** Shake sensor trigger event */
    data class ShakeEvent(val shakeCount: Int) : TriggerEvent()
    
    /** Device orientation trigger event */
    data class OrientationEvent(
        val orientation: AutomationTrigger.OrientationTrigger.DeviceOrientation
    ) : TriggerEvent()
    
    /** Proximity sensor trigger event */
    data class ProximityEvent(val state: AutomationTrigger.ProximityTrigger.ProximityState) : TriggerEvent()
    
    /** Manual trigger event */
    data class ManualEvent(val triggerId: String, val shortcutName: String?) : TriggerEvent()
}

/**
 * Internal event bus for propagating application events that can trigger automations.
 * Use the [emit] methods to fire events that user-triggered automations can respond to.
 */
object AutomationEventBus {
    private val _effectToggledEvents = MutableSharedFlow<EffectToggleEvent>(extraBufferCapacity = 64)
    val effectToggledEvents: SharedFlow<EffectToggleEvent> = _effectToggledEvents.asSharedFlow()

    private val _themeChangedEvents = MutableSharedFlow<ThemeChangeEvent>(extraBufferCapacity = 64)
    val themeChangedEvents: SharedFlow<ThemeChangeEvent> = _themeChangedEvents.asSharedFlow()

    private val _rewardClaimedEvents = MutableSharedFlow<RewardClaimEvent>(extraBufferCapacity = 64)
    val rewardClaimedEvents: SharedFlow<RewardClaimEvent> = _rewardClaimedEvents.asSharedFlow()

    private val _manualTriggerEvents = MutableSharedFlow<ManualTriggerEvent>(extraBufferCapacity = 64)
    val manualTriggerEvents: SharedFlow<ManualTriggerEvent> = _manualTriggerEvents.asSharedFlow()

    /** Emit when an effect is toggled on/off */
    fun emitEffectToggled(effectId: String, enabled: Boolean) {
        _effectToggledEvents.tryEmit(EffectToggleEvent(effectId, enabled))
    }

    /** Emit when a theme is changed */
    fun emitThemeChanged(themeId: String) {
        _themeChangedEvents.tryEmit(ThemeChangeEvent(themeId))
    }

    /** Emit when a reward is claimed */
    fun emitRewardClaimed(rewardType: String?) {
        _rewardClaimedEvents.tryEmit(RewardClaimEvent(rewardType))
    }

    /** Emit when a manual trigger is activated */
    fun emitManualTrigger(triggerId: String) {
        _manualTriggerEvents.tryEmit(ManualTriggerEvent(triggerId))
    }

    /** Effect toggle event data */
    data class EffectToggleEvent(val effectId: String, val enabled: Boolean)
    
    /** Theme change event data */
    data class ThemeChangeEvent(val themeId: String)
    
    /** Reward claim event data */
    data class RewardClaimEvent(val rewardType: String?)
    
    /** Manual trigger event data */
    data class ManualTriggerEvent(val triggerId: String)
}
