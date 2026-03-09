package com.sugarmunch.app.automation

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.sqrt

/**
 * Sensor-based trigger evaluation functions.
 * Handles Shake, Orientation, and Proximity triggers.
 */
internal class SensorTriggerEvaluator(private val context: Context) {

    /**
     * Evaluates shake triggers that fire when device is shaken.
     */
    fun evaluateShakeTrigger(trigger: AutomationTrigger.ShakeTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

            if (accelerometer == null) {
                Log.w(TAG, "Accelerometer sensor not available")
                close()
                return@callbackFlow
            }

            val threshold = when (trigger.sensitivity) {
                AutomationTrigger.ShakeTrigger.ShakeSensitivity.LOW -> 15f
                AutomationTrigger.ShakeTrigger.ShakeSensitivity.MEDIUM -> 12f
                AutomationTrigger.ShakeTrigger.ShakeSensitivity.HIGH -> 8f
            }

            var shakeCount = 0
            var lastShakeTime = 0L

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]

                    val acceleration = sqrt(x * x + y * y + z * z)

                    if (acceleration > threshold) {
                        val now = System.currentTimeMillis()
                        if (now - lastShakeTime > 500) { // Debounce
                            shakeCount++
                            lastShakeTime = now

                            if (shakeCount >= trigger.minShakes) {
                                trySend(TriggerEvent.ShakeEvent(shakeCount))
                                shakeCount = 0
                            }
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)
            awaitClose { sensorManager.unregisterListener(listener) }
        }

    /**
     * Evaluates orientation triggers that fire on device orientation changes.
     */
    fun evaluateOrientationTrigger(trigger: AutomationTrigger.OrientationTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            val magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

            if (accelerometer == null || magnetometer == null) {
                Log.w(TAG, "Required sensors not available")
                close()
                return@callbackFlow
            }

            var gravity: FloatArray? = null
            var geomagnetic: FloatArray? = null
            var lastOrientation: AutomationTrigger.OrientationTrigger.DeviceOrientation? = null

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    when (event.sensor.type) {
                        Sensor.TYPE_ACCELEROMETER -> gravity = event.values.clone()
                        Sensor.TYPE_MAGNETIC_FIELD -> geomagnetic = event.values.clone()
                    }

                    if (gravity != null && geomagnetic != null) {
                        val rotationMatrix = FloatArray(9)
                        val inclinationMatrix = FloatArray(9)

                        if (SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix,
                                                              gravity, geomagnetic)) {
                            val orientation = FloatArray(3)
                            SensorManager.getOrientation(rotationMatrix, orientation)

                            val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
                            val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()

                            val currentOrientation = when (trigger.orientation) {
                                AutomationTrigger.OrientationTrigger.DeviceOrientation.PORTRAIT ->
                                    if (kotlin.math.abs(pitch) < 45)
                                        AutomationTrigger.OrientationTrigger.DeviceOrientation.PORTRAIT else null
                                AutomationTrigger.OrientationTrigger.DeviceOrientation.LANDSCAPE ->
                                    if (kotlin.math.abs(roll) > 45)
                                        AutomationTrigger.OrientationTrigger.DeviceOrientation.LANDSCAPE else null
                                AutomationTrigger.OrientationTrigger.DeviceOrientation.FACE_UP ->
                                    if (pitch > 45)
                                        AutomationTrigger.OrientationTrigger.DeviceOrientation.FACE_UP else null
                                AutomationTrigger.OrientationTrigger.DeviceOrientation.FACE_DOWN ->
                                    if (pitch < -45)
                                        AutomationTrigger.OrientationTrigger.DeviceOrientation.FACE_DOWN else null
                            }

                            if (currentOrientation == trigger.orientation &&
                                currentOrientation != lastOrientation) {
                                trySend(TriggerEvent.OrientationEvent(currentOrientation))
                            }
                            lastOrientation = currentOrientation
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            sensorManager.registerListener(listener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL)

            awaitClose {
                sensorManager.unregisterListener(listener)
            }
        }

    /**
     * Evaluates proximity triggers that fire when objects are near/far from sensor.
     */
    fun evaluateProximityTrigger(trigger: AutomationTrigger.ProximityTrigger): Flow<TriggerEvent> =
        callbackFlow {
            val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            val proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

            if (proximity == null) {
                Log.w(TAG, "Proximity sensor not available")
                close()
                return@callbackFlow
            }

            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    val distance = event.values[0]
                    val maxRange = proximity.maximumRange

                    val isNear = distance < maxRange
                    val currentState = if (isNear)
                        AutomationTrigger.ProximityTrigger.ProximityState.NEAR
                    else
                        AutomationTrigger.ProximityTrigger.ProximityState.FAR

                    if (currentState == trigger.state) {
                        trySend(TriggerEvent.ProximityEvent(currentState))
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            sensorManager.registerListener(listener, proximity, SensorManager.SENSOR_DELAY_NORMAL)
            awaitClose { sensorManager.unregisterListener(listener) }
        }

    companion object {
        private const val TAG = "SensorTriggerEvaluator"
    }
}
