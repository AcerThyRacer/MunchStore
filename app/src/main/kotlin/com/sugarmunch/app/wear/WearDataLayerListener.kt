package com.sugarmunch.app.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.sugarmunch.app.effects.v2.engine.EffectEngineV2
import com.sugarmunch.app.effects.v2.presets.EffectRegistry
import com.sugarmunch.app.theme.engine.ThemeManager
import com.sugarmunch.app.theme.presets.ThemePresets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Wear Data Layer Listener - Handles communication from Wear OS app
 *
 * This class runs on the phone and listens for commands from the connected
 * smartwatch to control effects and themes.
 */
class WearDataLayerListener(
    private val context: Context,
    private val effectEngine: EffectEngineV2,
    private val themeManager: ThemeManager
) {
    companion object {
        private const val TAG = "WearDataLayerListener"

        // Data paths (must match wear app)
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
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var isListening = false

    /**
     * Start listening for data layer events
     */
    fun startListening() {
        if (isListening) return
        isListening = true

        // Add capability
        scope.launch {
            try {
                capabilityClient.addLocalCapability(CAPABILITY_PHONE_APP).await()
                Log.d(TAG, "Added phone capability")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error adding capability", e)
            }
        }

        // Listen for data changes
        dataClient.addListener(dataListener)

        // Send initial sync
        syncStateToWatch()

        Log.d(TAG, "Wear Data Layer Listener started")
    }

    /**
     * Stop listening for events
     */
    fun stopListening() {
        isListening = false
        dataClient.removeListener(dataListener)
        scope.cancel()
        Log.d(TAG, "Wear Data Layer Listener stopped")
    }

    /**
     * Data listener for incoming commands from watch
     */
    private val dataListener = DataClient.OnDataChangedListener { dataEvents ->
        dataEvents.forEach { event ->
            if (event.type == DataEvent.TYPE_CHANGED) {
                handleDataItem(event.dataItem)
            }
        }
    }

    /**
     * Handle incoming data item from watch
     */
    private fun handleDataItem(dataItem: DataItem) {
        when (dataItem.uri.path) {
            PATH_COMMAND -> handleCommand(dataItem)
        }
    }

    /**
     * Handle command from watch
     */
    private fun handleCommand(dataItem: DataItem) {
        try {
            val dataMap = dataItem.data?.let {
                com.google.android.gms.wearable.DataMap.fromByteArray(it)
            } ?: return

            val commandType = dataMap.getString(KEY_COMMAND_TYPE) ?: return

            Log.d(TAG, "Received command: $commandType")

            when (commandType) {
                CMD_TOGGLE_EFFECT -> {
                    val effectId = dataMap.getString(KEY_EFFECT_ID) ?: return
                    val enabled = dataMap.getBoolean(KEY_ENABLED, false)
                    val intensity = dataMap.getFloat(KEY_INTENSITY, 1f)
                    handleToggleEffect(effectId, enabled, intensity)
                }
                CMD_SET_THEME -> {
                    val themeId = dataMap.getString(KEY_THEME_ID) ?: return
                    handleSetTheme(themeId)
                }
                CMD_APPLY_PRESET -> {
                    val presetName = dataMap.getString(KEY_PRESET_NAME) ?: return
                    handleApplyPreset(presetName)
                }
                CMD_SET_BOOST_MODE -> {
                    val enabled = dataMap.getBoolean(KEY_BOOST_MODE, false)
                    handleSetBoostMode(enabled)
                }
                CMD_ALL_OFF -> {
                    handleAllOff()
                }
                CMD_SYNC_REQUEST -> {
                    syncStateToWatch()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling command", e)
        }
    }

    /**
     * Handle toggle effect command
     */
    private fun handleToggleEffect(effectId: String, enabled: Boolean, intensity: Float) {
        scope.launch {
            try {
                if (enabled) {
                    effectEngine.enableEffect(effectId, intensity)
                } else {
                    effectEngine.disableEffect(effectId)
                }
                syncStateToWatch()
                Log.d(TAG, "Toggled effect $effectId to $enabled")
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling effect", e)
            }
        }
    }

    /**
     * Handle set theme command
     */
    private fun handleSetTheme(themeId: String) {
        scope.launch {
            try {
                themeManager.setThemeById(themeId)
                syncStateToWatch()
                Log.d(TAG, "Applied theme $themeId")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting theme", e)
            }
        }
    }

    /**
     * Handle apply preset command
     */
    private fun handleApplyPreset(presetName: String) {
        scope.launch {
            try {
                when (presetName.lowercase()) {
                    "chill" -> applyChillPreset()
                    "focus" -> applyFocusPreset()
                    "party" -> applyPartyPreset()
                    "gaming" -> applyGamingPreset()
                    "favorites" -> applyFavoritesPreset()
                    "random_theme" -> applyRandomTheme()
                }
                syncStateToWatch()
                Log.d(TAG, "Applied preset $presetName")
            } catch (e: Exception) {
                Log.e(TAG, "Error applying preset", e)
            }
        }
    }

    /**
     * Handle boost mode toggle
     */
    private fun handleSetBoostMode(enabled: Boolean) {
        scope.launch {
            try {
                // Apply boost intensity to all active effects
                effectEngine.activeEffects.value.keys.forEach { effectId ->
                    val newIntensity = if (enabled) 2f else 1f
                    effectEngine.setEffectIntensity(effectId, newIntensity)
                }
                syncStateToWatch()
                Log.d(TAG, "Boost mode set to $enabled")
            } catch (e: Exception) {
                Log.e(TAG, "Error setting boost mode", e)
            }
        }
    }

    /**
     * Handle all off command
     */
    private fun handleAllOff() {
        scope.launch {
            try {
                // Disable all active effects
                effectEngine.activeEffects.value.keys.toList().forEach { effectId ->
                    effectEngine.disableEffect(effectId)
                }
                syncStateToWatch()
                Log.d(TAG, "All effects turned off")
            } catch (e: Exception) {
                Log.e(TAG, "Error turning off effects", e)
            }
        }
    }

    /**
     * Sync current state to watch
     */
    fun syncStateToWatch() {
        scope.launch {
            try {
                // Build effect state data
                val activeEffects = effectEngine.activeEffects.value
                val effectIds = EffectRegistry.ALL_EFFECTS.map { it.id }

                val effectDataReq = PutDataMapRequest.create(PATH_EFFECT_STATE).apply {
                    dataMap.putStringArrayList("effect_ids", ArrayList(effectIds))
                    effectIds.forEach { id ->
                        val state = activeEffects[id]
                        dataMap.putBoolean("${id}_active", state != null)
                        dataMap.putFloat("${id}_intensity", state?.currentIntensity ?: 1f)
                    }
                }.asPutDataRequest().setUrgent()

                dataClient.putDataItem(effectDataReq).await()

                // Build theme state data
                val currentThemeId = themeManager.currentTheme.value?.id
                val boostMode = activeEffects.values.any { it.currentIntensity >= 1.5f }

                val themeDataReq = PutDataMapRequest.create(PATH_THEME_STATE).apply {
                    dataMap.putString(KEY_THEME_ID, currentThemeId ?: "")
                    dataMap.putBoolean(KEY_BOOST_MODE, boostMode)
                    dataMap.putLong("timestamp", System.currentTimeMillis())
                }.asPutDataRequest().setUrgent()

                dataClient.putDataItem(themeDataReq).await()

                Log.d(TAG, "State synced to watch: ${activeEffects.size} active effects")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Log.e(TAG, "Error syncing state to watch", e)
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════
    // PRESETS
    // ═════════════════════════════════════════════════════════════════

    private fun applyChillPreset() {
        effectEngine.clearAllEffects()
        themeManager.setThemeById("chill_mint")
        effectEngine.enableEffect("chill_mint", 0.5f)
    }

    private fun applyFocusPreset() {
        effectEngine.clearAllEffects()
        themeManager.setThemeById("caramel_dim")
        // No effects for focus mode - minimal distractions
    }

    private fun applyPartyPreset() {
        effectEngine.clearAllEffects()
        themeManager.setThemeById("sugarrush_classic")
        effectEngine.enableEffect("candy_confetti", 1.5f)
        effectEngine.enableEffect("rainbow_tint", 1.2f)
        effectEngine.enableEffect("heartbeat_haptic", 1f)
    }

    private fun applyGamingPreset() {
        effectEngine.clearAllEffects()
        themeManager.setThemeById("sugarrush_nuclear")
        effectEngine.enableEffect("sugarrush_overlay", 1.8f)
        effectEngine.enableEffect("gummy_bounce", 1.5f)
    }

    private fun applyFavoritesPreset() {
        // Apply user's favorite effects - simplified version
        effectEngine.enableEffect("candy_confetti", 1f)
        effectEngine.enableEffect("lollipop_glow", 0.8f)
    }

    private fun applyRandomTheme() {
        val randomTheme = ThemePresets.getRandom()
        themeManager.setTheme(randomTheme)
    }
}