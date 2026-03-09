package com.sugarmunch.wear

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.Wearable
import com.sugarmunch.wear.complications.updateComplicationData
import com.sugarmunch.wear.data.WearDataLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * SugarMunch Wear OS Application
 * 
 * Handles lifecycle events, data layer initialization, and complication updates.
 */
class SugarMunchWearApplication : Application(), DefaultLifecycleObserver {

    companion object {
        private const val TAG = "SugarMunchWearApp"
        lateinit var instance: SugarMunchWearApplication
            private set
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    lateinit var wearDataLayer: WearDataLayer
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Data Layer
        wearDataLayer = WearDataLayer(this)
        
        // Observe app lifecycle
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        
        // Start observing data changes
        observeDataChanges()
        
        Log.d(TAG, "Wear application initialized")
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d(TAG, "App in foreground - requesting sync")
        
        // Request sync from phone when app comes to foreground
        applicationScope.launch {
            wearDataLayer.checkPhoneConnection()
            wearDataLayer.requestSync()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d(TAG, "App in background")
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
        Log.d(TAG, "Application terminated")
    }

    /**
     * Observe data layer changes and update complications
     */
    private fun observeDataChanges() {
        // Listen for effect state changes to update complication
        wearDataLayer.effectStates
            .onEach { states ->
                val activeCount = states.count { it.value.isActive }
                val boostMode = wearDataLayer.boostMode.value
                updateComplicationData(this, activeCount, boostMode)
            }
            .launchIn(applicationScope)
        
        // Listen for data events from phone
        applicationScope.launch {
            wearDataLayer.dataEvents().collect { dataEvents ->
                dataEvents.forEach { event ->
                    if (event.type == DataEvent.TYPE_CHANGED) {
                        wearDataLayer.processDataItem(event.dataItem)
                    }
                }
            }
        }
        
        // Listen for capability changes
        applicationScope.launch {
            wearDataLayer.capabilityChanges().collect { capabilityInfo ->
                Log.d(TAG, "Capability changed: ${capabilityInfo.name}")
                wearDataLayer.checkPhoneConnection()
            }
        }
    }

    /**
     * Public method to trigger manual sync
     */
    fun requestSync() {
        applicationScope.launch {
            wearDataLayer.requestSync()
        }
    }
}
