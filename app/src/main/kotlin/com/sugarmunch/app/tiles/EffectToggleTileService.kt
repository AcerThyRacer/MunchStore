package com.sugarmunch.app.tiles

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.sugarmunch.app.MainActivity
import com.sugarmunch.app.effects.v2.engine.EffectEngineV2
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Quick Settings tile for toggling SugarMunch effects
 * 
 * Features:
 * - Shows current active effect count in subtitle
 * - Tap to open effect panel in main app
 * - Long press to open main app
 * - Dynamic tile state based on active effects
 * - Visual feedback when effects are active
 */
class EffectToggleTileService : TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var effectEngine: EffectEngineV2? = null
    private var collectionJob: Job? = null

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
                unlockAndRun { openEffectPanel() }
                return
            }
        }
        
        openEffectPanel()
    }

    override fun onLongClick() {
        super.onLongClick()
        openMainApp()
    }

    /**
     * Open the main app to the effects panel
     */
    private fun openEffectPanel() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_EFFECTS, true)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ - use startActivityAndCollapse with PendingIntent
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
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
     * Open the main app
     */
    private fun openMainApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                1,
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
     * Start collecting effect state changes to update tile
     */
    private fun startCollectingEffectState() {
        collectionJob?.cancel()
        collectionJob = scope.launch {
            effectEngine?.activeEffects?.collectLatest { activeEffects ->
                updateTileWithEffectCount(activeEffects.size)
            }
        }
    }

    /**
     * Update tile state based on current effects
     */
    private fun updateTileState() {
        val engine = effectEngine ?: return
        val activeCount = engine.getActiveEffectCount()
        updateTileWithEffectCount(activeCount)
    }

    /**
     * Update tile visual state based on effect count
     */
    private fun updateTileWithEffectCount(count: Int) {
        qsTile?.let { tile ->
            if (count > 0) {
                // Active state - effects are running
                tile.state = Tile.STATE_ACTIVE
                tile.label = "SugarMunch Effects"
                tile.subtitle = "$count active"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.contentDescription = "$count SugarMunch effects are active"
                }
            } else {
                // Inactive state - no effects
                tile.state = Tile.STATE_INACTIVE
                tile.label = "SugarMunch"
                tile.subtitle = "Tap for effects"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.contentDescription = "No effects active. Tap to open effects panel."
                }
            }
            
            // Update tile icon based on state
            updateTileIcon(tile, count > 0)
            
            tile.updateTile()
        }
    }

    /**
     * Update tile icon - could use different icons for active/inactive
     */
    private fun updateTileIcon(tile: Tile, isActive: Boolean) {
        // Use default tile icon or set a custom one
        // For now, we use the app's icon
        try {
            val iconRes = if (isActive) {
                android.R.drawable.ic_media_play
            } else {
                android.R.drawable.ic_media_pause
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.icon = Icon.createWithResource(this, iconRes)
            }
        } catch (e: Exception) {
            // Fallback to default icon
        }
    }

    companion object {
        const val EXTRA_OPEN_EFFECTS = "extra_open_effects"
    }
}
