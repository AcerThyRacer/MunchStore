package com.sugarmunch.app.tiles

import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.Icon
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import com.sugarmunch.app.MainActivity
import com.sugarmunch.app.data.ManifestRepository
import com.sugarmunch.app.download.SmartDownloadManager
import com.sugarmunch.app.download.DownloadStatus
import com.sugarmunch.app.download.DownloadTask
import com.sugarmunch.app.util.SecureLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

/**
 * Quick Settings tile for quick app installation
 *
 * Features:
 * - Shows suggested app to install (from manifest)
 * - Tap to start download
 * - Progress indicator during download
 * - Updates based on download state
 * - Shows completion status
 * - Rotates suggestions based on install history
 */
class QuickInstallTileService : TileService() {

    private val logger = SecureLogger.create("QuickInstallTile")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var downloadManager: SmartDownloadManager? = null
    private var manifestRepository: ManifestRepository? = null
    private var downloadJob: Job? = null
    private var suggestionJob: Job? = null

    // SharedPreferences for storing suggestion state
    private val prefs: SharedPreferences by lazy {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
    }

    // Currently suggested app
    private var suggestedApp: SuggestedApp? = null

    // Track active download
    private var activeDownloadId: String? = null

    companion object {
        private const val PREFS_NAME = "quick_install_tile"
        private const val KEY_LAST_SUGGESTION_ID = "last_suggestion_id"
        private const val KEY_SUGGESTION_TIMESTAMP = "suggestion_timestamp"
        private const val KEY_SUGGESTION_ROTATION_COUNT = "rotation_count"
        
        const val EXTRA_OPEN_CATALOG = "extra_open_catalog"
        const val EXTRA_HIGHLIGHT_APP = "extra_highlight_app"
        
        // Suggestion rotates every 6 hours
        private const val SUGGESTION_ROTATION_MS = 6 * 60 * 60 * 1000L
    }

    data class SuggestedApp(
        val id: String,
        val name: String,
        val downloadUrl: String,
        val fileName: String
    )

    override fun onCreate() {
        super.onCreate()
        downloadManager = SmartDownloadManager.getInstance(this)
        manifestRepository = ManifestRepository()
    }

    override fun onStartListening() {
        super.onStartListening()
        updateTileState()
        startCollectingDownloadState()
        loadSuggestedApp()
    }

    override fun onStopListening() {
        super.onStopListening()
        downloadJob?.cancel()
        suggestionJob?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadJob?.cancel()
        suggestionJob?.cancel()
        scope.cancel()
    }

    override fun onClick() {
        super.onClick()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isLocked) {
                unlockAndRun { handleTileClick() }
                return
            }
        }
        
        handleTileClick()
    }

    override fun onLongClick() {
        super.onLongClick()
        openCatalog()
    }

    /**
     * Handle tile click based on current state
     */
    private fun handleTileClick() {
        val downloadMgr = downloadManager ?: return
        
        // Check if there's an active download for this app
        val currentDownload = suggestedApp?.id?.let { appId ->
            downloadMgr.downloadState.value.find { 
                it.request.id == appId && it.status.isActive()
            }
        }
        
        when {
            currentDownload != null -> {
                // Download in progress - open catalog to see progress
                openCatalog()
            }
            suggestedApp != null -> {
                // Start new download
                startDownload()
            }
            else -> {
                // No suggestion loaded yet - open catalog
                openCatalog()
            }
        }
    }

    /**
     * Start downloading the suggested app
     */
    private fun startDownload() {
        val app = suggestedApp ?: return
        val downloadMgr = downloadManager ?: return
        
        // Check if already downloading
        val existingDownload = downloadMgr.downloadState.value.find { 
            it.request.id == app.id && it.status.isActive() 
        }
        
        if (existingDownload != null) {
            showToast("${app.name} is already downloading")
            return
        }
        
        // Create download request
        val request = SmartDownloadManager.DownloadRequest(
            id = app.id,
            url = app.downloadUrl,
            fileName = app.fileName,
            appName = app.name,
            priority = SmartDownloadManager.DownloadPriority.HIGH,
            autoInstall = true
        )
        
        // Enqueue download
        downloadMgr.enqueue(request)
        activeDownloadId = app.id
        
        showToast("⬇️ Downloading ${app.name}...")
        updateTileToDownloadingState()
    }

    /**
     * Load a suggested app from manifest.
     * Rotates suggestions based on:
     * - Install history (don't suggest already installed apps)
     * - Featured apps priority
     * - Time-based rotation (new suggestion every 6 hours)
     */
    private fun loadSuggestedApp() {
        suggestionJob?.cancel()
        suggestionJob = scope.launch {
            try {
                val repo = manifestRepository ?: return@launch
                val manifest = repo.getManifest()
                
                if (manifest == null || manifest.apps.isEmpty()) {
                    logger.w("No manifest or empty apps list")
                    suggestedApp = getDefaultSuggestion()
                    withContext(Dispatchers.Main) {
                        updateTileForSuggestion()
                    }
                    return@launch
                }

                // Get last suggestion info
                val lastSuggestionId = prefs.getString(KEY_LAST_SUGGESTION_ID, null)
                val lastTimestamp = prefs.getLong(KEY_SUGGESTION_TIMESTAMP, 0)
                val now = System.currentTimeMillis()
                
                // Check if we should rotate suggestion
                val shouldRotate = lastSuggestionId == null || 
                    (now - lastTimestamp) > SUGGESTION_ROTATION_MS

                logger.d("Loading suggestion. Last: $lastSuggestionId, Should rotate: $shouldRotate")

                // Filter out already installed apps using PackageManager
                val pm = packageManager
                val availableApps = manifest.apps.filter { app ->
                    try {
                        // Check if app is installed
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            pm.getPackageInfo(app.packageName, PackageManager.PackageInfoFlags.of(0))
                        } else {
                            @Suppress("DEPRECATION")
                            pm.getPackageInfo(app.packageName, 0)
                        }
                        // Package found = app is installed, exclude from suggestions
                        logger.d("App ${app.name} is already installed, excluding from suggestions")
                        false
                    } catch (e: PackageManager.NameNotFoundException) {
                        // Package not found = app not installed, include in suggestions
                        true
                    } catch (e: Exception) {
                        // On any other error, include the app to be safe
                        Log.w("QuickInstallTile", "Error checking if ${app.packageName} is installed", e)
                        true
                    }
                }

                logger.d("Found ${availableApps.size} available apps out of ${manifest.apps.size} total")

                // Prioritize featured apps
                val featuredApps = availableApps.filter { it.featured }
                
                // Select suggestion
                val selectedApp = if (shouldRotate && featuredApps.isNotEmpty()) {
                    // Pick a random featured app (different from last)
                    val candidates = if (lastSuggestionId != null) {
                        featuredApps.filter { it.id != lastSuggestionId }
                    } else {
                        featuredApps
                    }
                    candidates.randomOrNull() ?: featuredApps.random()
                } else if (featuredApps.isNotEmpty()) {
                    // Keep current featured suggestion
                    featuredApps.find { it.id == lastSuggestionId } ?: featuredApps.first()
                } else {
                    // Fallback to any available app
                    availableApps.randomOrNull()
                }

                if (selectedApp != null) {
                    suggestedApp = SuggestedApp(
                        id = selectedApp.id,
                        name = selectedApp.name,
                        downloadUrl = selectedApp.downloadUrl,
                        fileName = "${selectedApp.packageName}_${selectedApp.version}.apk"
                    )
                    
                    // Save suggestion state
                    prefs.edit().apply {
                        putString(KEY_LAST_SUGGESTION_ID, selectedApp.id)
                        putLong(KEY_SUGGESTION_TIMESTAMP, now)
                        apply()
                    }
                    
                    logger.d("Selected suggestion: ${selectedApp.name} (featured: ${selectedApp.featured})")
                } else {
                    logger.w("No apps available for suggestion")
                    suggestedApp = getDefaultSuggestion()
                }

                withContext(Dispatchers.Main) {
                    updateTileForSuggestion()
                }
            } catch (e: Exception) {
                logger.e("Failed to load suggestion", e)
                suggestedApp = null
                withContext(Dispatchers.Main) {
                    updateTileNoSuggestion()
                }
            }
        }
    }

    /**
     * Get default app suggestion as fallback.
     * Returns SugarTube as the default featured app.
     */
    private fun getDefaultSuggestion(): SuggestedApp? {
        return SuggestedApp(
            id = "sugartube",
            name = "SugarTube",
            downloadUrl = "https://github.com/sugarmunch/SugarMunch/releases/download/sugartube-v1.0.0/sugartube.apk",
            fileName = "sugartube.apk"
        )
    }

    /**
     * Start collecting download state changes
     */
    private fun startCollectingDownloadState() {
        downloadJob?.cancel()
        downloadJob = scope.launch {
            downloadManager?.downloadState?.collectLatest { downloads ->
                val appId = suggestedApp?.id
                if (appId != null) {
                    val appDownload = downloads.find { it.request.id == appId }
                    updateTileForDownloadState(appDownload)
                }
            }
        }
    }

    /**
     * Update tile based on download state
     */
    private fun updateTileForDownloadState(downloadTask: DownloadTask?) {
        qsTile?.let { tile ->
            when {
                downloadTask == null -> {
                    // No active download - show suggestion
                    updateTileForSuggestion()
                }
                downloadTask.status == DownloadStatus.DOWNLOADING -> {
                    // Download in progress
                    updateTileToProgressState(downloadTask.progress)
                }
                downloadTask.status == DownloadStatus.COMPLETED -> {
                    // Download complete
                    updateTileToCompletedState()
                }
                downloadTask.status == DownloadStatus.FAILED -> {
                    // Download failed
                    updateTileToFailedState()
                }
                else -> {
                    updateTileForSuggestion()
                }
            }
        }
    }

    /**
     * Update tile to show suggestion
     */
    private fun updateTileForSuggestion() {
        qsTile?.let { tile ->
            val app = suggestedApp
            if (app != null) {
                tile.state = Tile.STATE_ACTIVE
                tile.label = "⬇️ ${app.name}"
                tile.subtitle = "Tap to install"
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    tile.contentDescription = "Suggested app: ${app.name}. Tap to download."
                    tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_download)
                }
            } else {
                updateTileNoSuggestion()
            }
            tile.updateTile()
        }
    }

    /**
     * Update tile when no suggestion available
     */
    private fun updateTileNoSuggestion() {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Quick Install"
            tile.subtitle = "Browse apps"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.contentDescription = "No app suggestion. Long press to browse catalog."
                tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_search)
            }
            tile.updateTile()
        }
    }

    /**
     * Update tile to downloading state
     */
    private fun updateTileToDownloadingState() {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.label = suggestedApp?.name ?: "Downloading"
            tile.subtitle = "Starting..."
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_upload)
            }
            tile.updateTile()
        }
    }

    /**
     * Update tile with download progress
     */
    private fun updateTileToProgressState(progress: Float) {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.label = suggestedApp?.name ?: "Downloading"
            tile.subtitle = "${(progress * 100).toInt()}%"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.contentDescription = "Downloading ${suggestedApp?.name}: ${(progress * 100).toInt()}% complete"
                tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_upload)
            }
            tile.updateTile()
        }
    }

    /**
     * Update tile when download completed
     */
    private fun updateTileToCompletedState() {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_ACTIVE
            tile.label = "✓ ${suggestedApp?.name ?: "Done"}"
            tile.subtitle = "Ready to install"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.contentDescription = "Download complete. Ready to install."
                tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_save)
            }
            tile.updateTile()
            
            // Reset after a delay to show next suggestion
            scope.launch {
                delay(5000)
                loadSuggestedApp()
            }
        }
    }

    /**
     * Update tile when download failed
     */
    private fun updateTileToFailedState() {
        qsTile?.let { tile ->
            tile.state = Tile.STATE_UNAVAILABLE
            tile.label = "⚠️ Failed"
            tile.subtitle = "Tap to retry"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tile.contentDescription = "Download failed. Tap to retry."
                tile.icon = Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel)
            }
            tile.updateTile()
        }
    }

    /**
     * Open main app to catalog
     */
    private fun openCatalog() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_CATALOG, true)
            suggestedApp?.let { app ->
                putExtra(EXTRA_HIGHLIGHT_APP, app.id)
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pendingIntent = PendingIntent.getActivity(
                this,
                4,
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
     * Update general tile state
     */
    private fun updateTileState() {
        val downloads = downloadManager?.downloadState?.value
        val appId = suggestedApp?.id
        
        val appDownload = if (appId != null) {
            downloads?.find { it.request.id == appId }
        } else null
        
        updateTileForDownloadState(appDownload)
    }

    /**
     * Show a toast message
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Extension to check if download status is active
     */
    private fun DownloadStatus.isActive(): Boolean {
        return this == DownloadStatus.PENDING || this == DownloadStatus.DOWNLOADING
    }
}
