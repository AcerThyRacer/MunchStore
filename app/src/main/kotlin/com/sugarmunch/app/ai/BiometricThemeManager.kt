package com.sugarmunch.app.ai

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.presets.ThemePresets
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricThemeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val themeManager: ThemeManager
) : DataClient.OnDataChangedListener {

    private val TAG = "BiometricThemeManager"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Heart rate thresholds
    private val HR_HIGH_THRESHOLD = 120f
    private val HR_LOW_THRESHOLD = 70f
    
    // Track previous state to avoid constant theme switching
    private var lastMode: Mode = Mode.NORMAL

    enum class Mode {
        CHILL, NORMAL, RUSH
    }

    fun start() {
        Wearable.getDataClient(context).addListener(this)
        Log.d(TAG, "BiometricThemeManager started listening to Wear OS data")
    }

    fun stop() {
        Wearable.getDataClient(context).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val path = event.dataItem.uri.path
                if (path == "/biometrics/heart_rate") {
                    val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                    val heartRate = dataMap.getFloat("heart_rate")
                    Log.d(TAG, "Received heart rate: $heartRate")
                    processHeartRate(heartRate)
                }
            }
        }
    }
    
    private fun processHeartRate(hr: Float) {
        scope.launch {
            val newMode = when {
                hr >= HR_HIGH_THRESHOLD -> Mode.RUSH
                hr <= HR_LOW_THRESHOLD -> Mode.CHILL
                else -> Mode.NORMAL
            }
            
            if (newMode != lastMode) {
                lastMode = newMode
                Log.d(TAG, "Heart rate changed mode to $newMode. Applying theme...")
                when (newMode) {
                    Mode.RUSH -> themeManager.setTheme(ThemePresets.SUGARRUSH_NUCLEAR)
                    Mode.CHILL -> themeManager.setTheme(ThemePresets.CHILL_MINT)
                    Mode.NORMAL -> themeManager.setTheme(ThemePresets.CLASSIC_CANDY)
                }
            }
        }
    }
}
