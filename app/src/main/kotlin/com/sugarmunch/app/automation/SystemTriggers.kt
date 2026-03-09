package com.sugarmunch.app.automation

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.debounce

/**
 * System-based trigger evaluation functions.
 * Handles Battery, Charging, WiFi, Bluetooth, Screen, and Geofence triggers.
 */
internal class SystemTriggerEvaluator(private val context: Context) {

    /**
     * Evaluates battery level triggers that fire when battery reaches specified thresholds.
     */
    fun evaluateBatteryTrigger(trigger: AutomationTrigger.BatteryLevelTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val batteryPct = (level * 100 / scale.toFloat()).toInt()

                    val shouldTrigger = when (trigger.operator) {
                        AutomationTrigger.BatteryLevelTrigger.ComparisonOperator.LESS_THAN ->
                            batteryPct < trigger.level
                        AutomationTrigger.BatteryLevelTrigger.ComparisonOperator.LESS_THAN_OR_EQUAL ->
                            batteryPct <= trigger.level
                        AutomationTrigger.BatteryLevelTrigger.ComparisonOperator.EQUAL ->
                            batteryPct == trigger.level
                        AutomationTrigger.BatteryLevelTrigger.ComparisonOperator.GREATER_THAN ->
                            batteryPct > trigger.level
                        AutomationTrigger.BatteryLevelTrigger.ComparisonOperator.GREATER_THAN_OR_EQUAL ->
                            batteryPct >= trigger.level
                    }

                    if (shouldTrigger) {
                        trySend(TriggerEvent.BatteryEvent(batteryPct))
                    }
                }
            }

            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            awaitClose { context.unregisterReceiver(receiver) }
        }.debounce(5000) // Debounce to prevent rapid triggers

    /**
     * Evaluates charging state triggers that fire on charging state changes.
     */
    fun evaluateChargingTrigger(trigger: AutomationTrigger.ChargingTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                    status == BatteryManager.BATTERY_STATUS_FULL
                    val isFastCharging = isCharging && (chargePlug == BatteryManager.BATTERY_PLUGGED_AC)
                    val isWireless = chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS

                    val shouldTrigger = when (trigger.state) {
                        AutomationTrigger.ChargingTrigger.ChargingState.PLUGGED_IN -> isCharging
                        AutomationTrigger.ChargingTrigger.ChargingState.UNPLUGGED -> !isCharging
                        AutomationTrigger.ChargingTrigger.ChargingState.FAST_CHARGING -> isFastCharging
                        AutomationTrigger.ChargingTrigger.ChargingState.WIRELESS_CHARGING -> isWireless
                    }

                    if (shouldTrigger) {
                        trySend(TriggerEvent.ChargingEvent(trigger.state))
                    }
                }
            }

            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            awaitClose { context.unregisterReceiver(receiver) }
        }

    /**
     * Evaluates WiFi connection triggers that fire when connecting to networks.
     */
    fun evaluateWifiTrigger(trigger: AutomationTrigger.WifiConnectedTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val connectionInfo = wifiManager.connectionInfo

                    if (connectionInfo.networkId != -1) {
                        val ssid = connectionInfo.ssid?.removePrefix("\"")?.removeSuffix("\"")
                        val shouldTrigger = trigger.anyWifi ||
                                          (trigger.ssid != null && ssid == trigger.ssid)

                        if (shouldTrigger) {
                            trySend(TriggerEvent.WifiEvent(ssid ?: "unknown"))
                        }
                    }
                }
            }

            context.registerReceiver(receiver, IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION))
            awaitClose { context.unregisterReceiver(receiver) }
        }

    /**
     * Evaluates Bluetooth connection triggers that fire when devices connect.
     */
    fun evaluateBluetoothTrigger(trigger: AutomationTrigger.BluetoothConnectedTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == BluetoothDevice.ACTION_ACL_CONNECTED) {
                        val device: BluetoothDevice? =
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)

                        device?.let {
                            val shouldTrigger = when {
                                trigger.deviceName != null ->
                                    it.name?.contains(trigger.deviceName, ignoreCase = true) == true
                                trigger.deviceType != null ->
                                    matchesDeviceType(it, trigger.deviceType)
                                else -> true
                            }

                            if (shouldTrigger) {
                                trySend(TriggerEvent.BluetoothEvent(it.name ?: "Unknown", it.address))
                            }
                        }
                    }
                }
            }

            context.registerReceiver(receiver, IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
            awaitClose { context.unregisterReceiver(receiver) }
        }

    /**
     * Evaluates screen state triggers that fire on screen on/off/unlock.
     */
    fun evaluateScreenTrigger(trigger: AutomationTrigger.ScreenStateTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val shouldTrigger = when (trigger.state) {
                        AutomationTrigger.ScreenStateTrigger.ScreenState.ON ->
                            intent.action == Intent.ACTION_SCREEN_ON
                        AutomationTrigger.ScreenStateTrigger.ScreenState.OFF ->
                            intent.action == Intent.ACTION_SCREEN_OFF
                        AutomationTrigger.ScreenStateTrigger.ScreenState.UNLOCKED ->
                            intent.action == Intent.ACTION_USER_PRESENT
                    }

                    if (shouldTrigger) {
                        trySend(TriggerEvent.ScreenEvent(trigger.state))
                    }
                }
            }

            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            context.registerReceiver(receiver, filter)
            awaitClose { context.unregisterReceiver(receiver) }
        }

    /**
     * Evaluates geofence triggers that fire when entering/exiting geographic areas.
     */
    fun evaluateGeofenceTrigger(trigger: AutomationTrigger.GeofenceTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            var lastInside = false

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val geofenceLocation = Location("").apply {
                        latitude = trigger.latitude
                        longitude = trigger.longitude
                    }

                    val distance = location.distanceTo(geofenceLocation)
                    val isInside = distance <= trigger.radiusMeters

                    val shouldTrigger = when (trigger.transition) {
                        AutomationTrigger.GeofenceTrigger.GeofenceTransition.ENTER ->
                            isInside && !lastInside
                        AutomationTrigger.GeofenceTrigger.GeofenceTransition.EXIT ->
                            !isInside && lastInside
                        AutomationTrigger.GeofenceTrigger.GeofenceTransition.DWELL ->
                            isInside // Trigger every update while inside
                    }

                    if (shouldTrigger) {
                        trySend(TriggerEvent.GeofenceEvent(trigger.transition, distance))
                    }

                    lastInside = isInside
                }

                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            }

            try {
                locationManager.requestLocationUpdates(
                    LocationManager.FUSED,
                    60000, // 1 minute
                    50f,   // 50 meters
                    listener,
                    Looper.getMainLooper()
                )
            } catch (e: SecurityException) {
                Log.e(TAG, "Location permission not granted", e)
            }

            awaitClose {
                locationManager.removeUpdates(listener)
            }
        }

    /**
     * Checks if a Bluetooth device matches the specified type.
     */
    private fun matchesDeviceType(device: BluetoothDevice, type: AutomationTrigger.BluetoothConnectedTrigger.DeviceType): Boolean {
        val bluetoothClass = device.bluetoothClass
        return when (type) {
            AutomationTrigger.BluetoothConnectedTrigger.DeviceType.HEADPHONES ->
                bluetoothClass?.deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES ||
                bluetoothClass?.deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
            AutomationTrigger.BluetoothConnectedTrigger.DeviceType.SPEAKER ->
                bluetoothClass?.deviceClass == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER
            AutomationTrigger.BluetoothConnectedTrigger.DeviceType.WATCH ->
                bluetoothClass?.deviceClass == BluetoothClass.Device.WEARABLE_WRIST_WATCH
            AutomationTrigger.BluetoothConnectedTrigger.DeviceType.CAR ->
                bluetoothClass?.majorDeviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO
            else -> true
        }
    }

    companion object {
        private const val TAG = "SystemTriggerEvaluator"
    }
}
