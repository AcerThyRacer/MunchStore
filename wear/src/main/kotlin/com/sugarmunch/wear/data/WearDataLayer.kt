package com.sugarmunch.wear.data

import android.content.Context
import android.util.Log
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Wearable Data Layer - Communication bridge between watch and phone
 */
class WearDataLayer(private val context: Context) {

    companion object {
        private const val TAG = "WearDataLayer"
        
        // Data paths
        const val PATH_EFFECT_STATE = "/sugarmunch/effect_state"
        const val PATH_THEME_STATE = "/sugarmunch/theme_state"
        const val PATH_COMMAND = "/sugarmunch/command"
        const val PATH_PRESET = "/sugarmunch/preset"
        
        // Capabilities
        const val CAPABILITY_PHONE_APP = "sugarmunch_phone_app"
        const val CAPABILITY_WEAR_APP = "sugarmunch_wear_app"
        
        // Command keys
        const val KEY_COMMAND_TYPE = "command_type"
        const val KEY_EFFECT_ID = "effect_id"
        const val KEY_THEME_ID = "theme_id"
        const val KEY_ENABLED = "enabled"
        const val KEY_INTENSITY = "intensity"
        const val KEY_PRESET_NAME = "preset_name"
        const val KEY_ACTIVE_EFFECT_COUNT = "active_effect_count"
        const val KEY_BOOST_MODE = "boost_mode"
        
        // Command types
        const val CMD_TOGGLE_EFFECT = "toggle_effect"
        const val CMD_SET_THEME = "set_theme"
        const val CMD_APPLY_PRESET = "apply_preset"
        const val CMD_SET_BOOST_MODE = "set_boost_mode"
        const val CMD_ALL_OFF = "all_off"
        const val CMD_SYNC_REQUEST = "sync_request"
    }

    private val dataClient: DataClient = Wearable.getDataClient(context)
    private val capabilityClient: CapabilityClient = Wearable.getCapabilityClient(context)

    // Connection state
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    // Effect states synced from phone
    private val _effectStates = MutableStateFlow<Map<String, EffectState>>(emptyMap())
    val effectStates: StateFlow<Map<String, EffectState>> = _effectStates.asStateFlow()

    // Current theme synced from phone
    private val _currentTheme = MutableStateFlow<String?>(null)
    val currentTheme: StateFlow<String?> = _currentTheme.asStateFlow()

    // Active effect count
    private val _activeEffectCount = MutableStateFlow(0)
    val activeEffectCount: StateFlow<Int> = _activeEffectCount.asStateFlow()

    // Boost mode state
    private val _boostMode = MutableStateFlow(false)
    val boostMode: StateFlow<Boolean> = _boostMode.asStateFlow()

    /**
     * Check if phone app is installed and reachable
     */
    suspend fun checkPhoneConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val capabilityInfo = capabilityClient
                .getCapability(CAPABILITY_PHONE_APP, CapabilityClient.FILTER_ALL)
                .await()
            
            val connected = capabilityInfo.nodes.isNotEmpty()
            _connectionState.value = if (connected) {
                ConnectionState.Connected(capabilityInfo.nodes.firstOrNull()?.displayName ?: "Phone")
            } else {
                ConnectionState.Disconnected
            }
            connected
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Log.e(TAG, "Error checking phone connection", e)
            _connectionState.value = ConnectionState.Error(e.message ?: "Unknown error")
            false
        }
    }

    /**
     * Send command to toggle an effect
     */
    suspend fun sendToggleEffect(effectId: String, enabled: Boolean, intensity: Float = 1f) {
        sendCommand(CMD_TOGGLE_EFFECT) {
            putString(KEY_EFFECT_ID, effectId)
            putBoolean(KEY_ENABLED, enabled)
            putFloat(KEY_INTENSITY, intensity)
        }
    }

    /**
     * Send command to change theme
     */
    suspend fun sendSetTheme(themeId: String) {
        sendCommand(CMD_SET_THEME) {
            putString(KEY_THEME_ID, themeId)
        }
    }

    /**
     * Send command to apply a preset
     */
    suspend fun sendApplyPreset(presetName: String) {
        sendCommand(CMD_APPLY_PRESET) {
            putString(KEY_PRESET_NAME, presetName)
        }
    }

    /**
     * Send command to toggle boost mode
     */
    suspend fun sendSetBoostMode(enabled: Boolean) {
        sendCommand(CMD_SET_BOOST_MODE) {
            putBoolean(KEY_BOOST_MODE, enabled)
        }
    }

    /**
     * Send command to turn all effects off
     */
    suspend fun sendAllOff() {
        sendCommand(CMD_ALL_OFF)
    }

    /**
     * Request sync from phone
     */
    suspend fun requestSync() {
        sendCommand(CMD_SYNC_REQUEST)
    }

    /**
     * Send a generic command to the phone
     */
    private suspend fun sendCommand(commandType: String, builder: (PutDataMapRequest.() -> Unit)? = null) {
        withContext(Dispatchers.IO) {
            try {
                val putDataReq = PutDataMapRequest.create(PATH_COMMAND).apply {
                    dataMap.putString(KEY_COMMAND_TYPE, commandType)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                    builder?.invoke(this)
                }.asPutDataRequest()
                    .setUrgent()

                dataClient.putDataItem(putDataReq).await()
                Log.d(TAG, "Command sent: $commandType")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error sending command: $commandType", e)
                throw e
            }
        }
    }

    /**
     * Flow of data changes from the phone
     */
    fun dataEvents(): Flow<DataEventBuffer> = callbackFlow {
        val listener = DataClient.OnDataChangedListener { events ->
            trySend(events)
        }
        
        dataClient.addListener(listener)
        
        awaitClose {
            dataClient.removeListener(listener)
        }
    }

    /**
     * Process incoming data items
     */
    fun processDataItem(dataItem: DataItem) {
        when (dataItem.uri.path) {
            PATH_EFFECT_STATE -> processEffectState(dataItem)
            PATH_THEME_STATE -> processThemeState(dataItem)
        }
    }

    private fun processEffectState(dataItem: DataItem) {
        try {
            val dataMap = dataItem.data?.let { 
                com.google.android.gms.wearable.DataMap.fromByteArray(it) 
            } ?: return

            val effectStatesMap = mutableMapOf<String, EffectState>()
            val effectIds = dataMap.getStringArrayList("effect_ids") ?: emptyList()
            
            effectIds.forEach { effectId ->
                val isActive = dataMap.getBoolean("${effectId}_active", false)
                val intensity = dataMap.getFloat("${effectId}_intensity", 1f)
                effectStatesMap[effectId] = EffectState(
                    effectId = effectId,
                    isActive = isActive,
                    intensity = intensity
                )
            }

            _effectStates.value = effectStatesMap
            _activeEffectCount.value = effectStatesMap.count { it.value.isActive }
            
            Log.d(TAG, "Effect states updated: ${_activeEffectCount.value} active")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing effect state", e)
        }
    }

    private fun processThemeState(dataItem: DataItem) {
        try {
            val dataMap = dataItem.data?.let { 
                com.google.android.gms.wearable.DataMap.fromByteArray(it) 
            } ?: return

            _currentTheme.value = dataMap.getString(KEY_THEME_ID)
            _boostMode.value = dataMap.getBoolean(KEY_BOOST_MODE, false)
            
            Log.d(TAG, "Theme updated: ${_currentTheme.value}")
        } catch (e: Exception) {
            Log.e(TAG, "Error processing theme state", e)
        }
    }

    /**
     * Observe capability changes for phone app availability
     */
    fun capabilityChanges(): Flow<CapabilityInfo> = callbackFlow {
        val listener = CapabilityClient.OnCapabilityChangedListener { info ->
            trySend(info)
        }
        
        capabilityClient.addListener(listener, CAPABILITY_PHONE_APP)
        
        awaitClose {
            capabilityClient.removeListener(listener)
        }
    }
}

/**
 * Connection state sealed class
 */
sealed class ConnectionState {
    data object Disconnected : ConnectionState()
    data class Connected(val deviceName: String) : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

/**
 * Effect state data class
 */
data class EffectState(
    val effectId: String,
    val isActive: Boolean,
    val intensity: Float
)
