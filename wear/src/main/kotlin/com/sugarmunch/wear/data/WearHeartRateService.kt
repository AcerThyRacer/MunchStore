package com.sugarmunch.wear.data

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import android.util.Log
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WearHeartRateService : Service(), SensorEventListener {

    private val TAG = "WearHeartRateService"
    private lateinit var sensorManager: SensorManager
    private var heartRateSensor: Sensor? = null
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val dataClient by lazy { Wearable.getDataClient(this) }
    
    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d(TAG, "Heart rate sensor registered")
        } ?: run {
            Log.e(TAG, "No heart rate sensor found")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_HEART_RATE) {
            val heartRate = event.values[0]
            if (heartRate > 0) {
                Log.d(TAG, "Heart rate: $heartRate")
                sendHeartRateToPhone(heartRate)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not used
    }
    
    private fun sendHeartRateToPhone(heartRate: Float) {
        scope.launch {
            try {
                val putDataMapReq = PutDataMapRequest.create("/biometrics/heart_rate").apply {
                    dataMap.putFloat("heart_rate", heartRate)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }
                val putDataReq = putDataMapReq.asPutDataRequest()
                putDataReq.setUrgent()
                dataClient.putDataItem(putDataReq)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send heart rate", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}
