package com.sugarmunch.app.tiles

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.sugarmunch.app.MainActivity
import com.sugarmunch.app.effects.v2.engine.EffectEngineV2
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Dedicated Quick Settings tile for SugarRush effect
 * 
 * Features:
 * - One-tap toggle for SugarRush effect
 * - Shows ON/OFF state in subtitle
 * - Visual feedback with icon changes
 * - Long press to open effect settings
 */
class SugarRushTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var effectEngine: EffectEngineV2? = null
    private var collectionJob: Job? = null

    companion object {
        const val SUGARRUSH_EFFECT_ID = "sugarrush"
        const val EXTRA_OPEN_SUGARRUSH_SETTINGS = "extra_open_sugarrush_settings"
    }

    override fun onCreate() {
        super.onCreate()
        effectEngine = EffectEngineV2.getInstance(this)
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
        startCollectingEffectState()
    }

    override fun onStopListening() {
        super.onStopListening()
        collectionJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        collectionJob?.cancel()
        scope.cancel()
    }

    override fun onClick() {
        super.onClick()
        
        // Unlock device if needed
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isLocked) {
                unlockAndRun { toggleSugarRush() }
                return
            }
        }
        
        toggleSugarRush()
    }

    override fun onLongClick() {
        super.onLongClick()
        openSugarRushSettings()
    }

    /**
     * Toggle SugarRush effect on/off
     */
    private fun toggleSugarRush() {
        val engine = effectEngine ?: return
        
        try {
            // Check if SugarRush effect exists
            val isActive = engine.activeEffects.value.containsKey(SUGARRUSH_EFFECT_ID)
            
            if (isActive) {
                // Turn off SugarRush
                engine.disableEffect(SUGARRUSH_EFFECT_ID)
                showToast("SugarRush OFF")
            } else {
                // Turn on SugarRush with default intensity
                engine.enableEffect(SUGARRUSH_EFFECT_ID, 1.0f)
                showToast("🚀 SugarRush ON!")
            }
            
            // Tile will update via flow collection
        } catch (e: Exception) {
            showToast("Failed to toggle SugarRush")
        }
    }

    /**
     * Open main app to SugarRush settings
     */
    private fun openSugarRushSettings() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_SUGARRUSH_SETTINGS, true)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                2,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            startActivityAndCollapse(pendingIntent)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            @Suppress("DEPRECATION")
            startActivityAndCollapse(intent)
        } else {
            startActivity(intent)
        }
    }

    /**
     * Start collecting effect state changes
     */
    private fun startCollectingEffectState() {
        collectionJob?.cancel()
        collectionJob = scope.launch {
            effectEngine?.activeEffects?.collectLatest { activeEffects ->
                val isActive = activeEffects.containsKey(SUGARRUSH_EFFECT_ID)
                updateTileVisualState(isActive)
            }
        }
    }

    /**
     * Update tile state
     */
    private fun updateTileState() {
        val engine = effectEngine ?: return
        val isActive = engine.activeEffects.value.containsKey(SUGARRUSH_EFFECT_ID)
        updateTileVisualState(isActive)
    }

    /**
     * Update tile visual appearance
     */
    private fun updateTileVisualState(isActive: Boolean) {
        qsTile?.let { tile ->
            if (isActive) {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "🚀 SugarRush"
                tile.subtitle = "ON"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.contentDescription = "SugarRush effect is active"
                    // Use a vibrant icon when active
                    tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_send)
                }
            } else {
                tile.state = Tile.STATE_INACTIVE
                tile.label = "SugarRush"
                tile.subtitle = "OFF"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.contentDescription = "SugarRush effect is inactive. Tap to enable."
                    tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_add)
                }
            }
            
            tile.updateTile()
        }
    }

    /**
     * Show a toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
