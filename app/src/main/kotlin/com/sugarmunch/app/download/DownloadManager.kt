package com.sugarmunch.app.download

import android.app.DownloadManager as SystemDownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.sugarmunch.app.MainActivity
import com.sugarmunch.app.R
import com.sugarmunch.app.crash.GlobalExceptionHandler
import com.sugarmunch.app.crash.Severity
import com.sugarmunch.app.security.ApkSignatureVerifier
import com.sugarmunch.app.security.VerificationResult
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Smart Download Manager - Advanced APK downloading with notifications
 * Features: Parallel downloads, resume support, queue management, progress tracking
 * 
 * Note for Hilt migration: Use @ApplicationContext for the Context parameter
 */
class SmartDownloadManager constructor(private val context: Context) {

    private val systemDownloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as SystemDownloadManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val downloads = ConcurrentHashMap<Long, DownloadTask>()
    private val _downloadState = MutableStateFlow<List<DownloadTask>>(emptyList())
    val downloadState: StateFlow<List<DownloadTask>> = _downloadState.asStateFlow()

    private val maxParallelDownloads = 3
    private val downloadQueue = mutableListOf<DownloadRequest>()
    private var activeDownloadCount = 0

    init {
        createNotificationChannels()
        registerDownloadReceiver()
    }

    // ═════════════════════════════════════════════════════════════
    // NOTIFICATION CHANNELS
    // ═════════════════════════════════════════════════════════════

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channels = listOf(
                NotificationChannel(
                    CHANNEL_DOWNLOADS,
                    "Downloads",
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = "Download progress notifications"
                    setShowBadge(false)
                },
                NotificationChannel(
                    CHANNEL_DOWNLOAD_COMPLETE,
                    "Download Complete",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for completed downloads"
                },
                NotificationChannel(
                    CHANNEL_DOWNLOAD_FAILED,
                    "Download Failed",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for failed downloads"
                }
            )
            notificationManager.createNotificationChannels(channels)
        }
    }

    // ═════════════════════════════════════════════════════════════
    // DOWNLOAD REQUEST
    // ═════════════════════════════════════════════════════════════

    data class DownloadRequest(
        val id: String,
        val url: String,
        val fileName: String,
        val appName: String,
        val appIcon: String? = null,
        val wifiOnly: Boolean = false,
        val autoInstall: Boolean = true,
        val priority: DownloadPriority = DownloadPriority.NORMAL
    )

    enum class DownloadPriority {
        LOW, NORMAL, HIGH, URGENT
    }

    // ═════════════════════════════════════════════════════════════
    // QUEUE MANAGEMENT
    // ═════════════════════════════════════════════════════════════

    fun enqueue(request: DownloadRequest): String {
        // Check if already downloading
        if (downloads.values.any { it.request.id == request.id && it.status.isActive() }) {
            return request.id
        }

        // Add to queue
        downloadQueue.add(request)
        downloadQueue.sortBy { it.priority.ordinal }

        // Try to start download
        processQueue()
        
        return request.id
    }

    fun enqueueMultiple(requests: List<DownloadRequest>): List<String> {
        return requests.map { enqueue(it) }
    }

    private fun processQueue() {
        scope.launch {
            while (activeDownloadCount < maxParallelDownloads && downloadQueue.isNotEmpty()) {
                val request = downloadQueue.removeFirstOrNull() ?: break
                startDownload(request)
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // DOWNLOAD EXECUTION
    // ═════════════════════════════════════════════════════════════

    private suspend fun startDownload(request: DownloadRequest) {
        activeDownloadCount++
        
        val downloadDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "sugarmunch")
        downloadDir.mkdirs()
        
        val file = File(downloadDir, request.fileName)
        
        // Create download task
        val task = DownloadTask(
            request = request,
            localFile = file,
            status = DownloadStatus.DOWNLOADING,
            progress = 0f,
            bytesDownloaded = 0,
            totalBytes = 0,
            speed = 0,
            startTime = System.currentTimeMillis()
        )

        // Build download request
        val downloadRequest = SystemDownloadManager.Request(Uri.parse(request.url))
            .setTitle("Downloading ${request.appName}")
            .setDescription("SugarMunch")
            .setDestinationUri(Uri.fromFile(file))
            .setNotificationVisibility(SystemDownloadManager.Request.VISIBILITY_HIDDEN)
            .setAllowedNetworkTypes(
                if (request.wifiOnly) SystemDownloadManager.Request.NETWORK_WIFI
                else SystemDownloadManager.Request.NETWORK_WIFI or SystemDownloadManager.Request.NETWORK_MOBILE
            )

        // Start system download
        val downloadId = systemDownloadManager.enqueue(downloadRequest)
        
        task.copy(downloadId = downloadId).let {
            downloads[downloadId] = it
            updateDownloadState()
        }

        // Start progress monitoring
        monitorDownloadProgress(downloadId)
    }

    private fun monitorDownloadProgress(downloadId: Long) {
        scope.launch {
            while (isActive) {
                val task = downloads[downloadId] ?: break
                if (!task.status.isActive()) break

                val query = SystemDownloadManager.Query().setFilterById(downloadId)
                val cursor = systemDownloadManager.query(query)
                
                cursor.use {
                    if (it.moveToFirst()) {
                        val statusIdx = it.getColumnIndex(SystemDownloadManager.COLUMN_STATUS)
                        val bytesIdx = it.getColumnIndex(SystemDownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                        val totalIdx = it.getColumnIndex(SystemDownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                        
                        val status = it.getInt(statusIdx)
                        val bytesDownloaded = it.getLong(bytesIdx)
                        val totalBytes = it.getLong(totalIdx)
                        
                        when (status) {
                            SystemDownloadManager.STATUS_RUNNING -> {
                                val progress = if (totalBytes > 0) {
                                    bytesDownloaded.toFloat() / totalBytes
                                } else 0f
                                
                                updateTask(downloadId) { task ->
                                    task.copy(
                                        progress = progress,
                                        bytesDownloaded = bytesDownloaded,
                                        totalBytes = totalBytes,
                                        speed = calculateSpeed(task, bytesDownloaded)
                                    )
                                }
                                showProgressNotification(task, progress)
                            }
                            SystemDownloadManager.STATUS_SUCCESSFUL -> {
                                onDownloadComplete(downloadId)
                                break
                            }
                            SystemDownloadManager.STATUS_FAILED -> {
                                onDownloadFailed(downloadId)
                                break
                            }
                        }
                    }
                }
                
                delay(500) // Update every 500ms
            }
        }
    }

    private fun calculateSpeed(task: DownloadTask, currentBytes: Long): Long {
        val elapsed = System.currentTimeMillis() - task.startTime
        return if (elapsed > 0) (currentBytes * 1000) / elapsed else 0
    }

    private fun onDownloadComplete(downloadId: Long) {
        val task = downloads[downloadId] ?: return

        // Verify APK signature before marking as complete
        val signatureVerifier = ApkSignatureVerifier.getInstance(context)
        val verificationResult = signatureVerifier.verifyApk(task.localFile)
        
        when (verificationResult) {
            is VerificationResult.Success -> {
                // Log verification for analytics
                GlobalExceptionHandler.getInstance().reportError(
                    message = "APK verified: ${task.request.appName} (trusted: ${verificationResult.isTrusted})",
                    severity = Severity.INFO
                )
                
                updateTask(downloadId) { it.copy(
                    status = DownloadStatus.COMPLETED,
                    progress = 1f,
                    endTime = System.currentTimeMillis(),
                    signatureVerified = verificationResult.isTrusted
                )}

                showCompleteNotification(task)

                if (task.request.autoInstall) {
                    installApk(task.localFile)
                }
            }
            is VerificationResult.Error -> {
                // Signature verification failed
                GlobalExceptionHandler.getInstance().reportError(
                    message = "APK signature verification failed: ${verificationResult.message}",
                    severity = Severity.WARNING
                )
                
                updateTask(downloadId) { it.copy(
                    status = DownloadStatus.FAILED,
                    endTime = System.currentTimeMillis(),
                    errorMessage = verificationResult.message
                )}
                
                showFailedNotification(task, verificationResult.message)
            }
        }

        activeDownloadCount--
        processQueue()
    }

    private fun onDownloadFailed(downloadId: Long) {
        val task = downloads[downloadId] ?: return
        
        updateTask(downloadId) { it.copy(
            status = DownloadStatus.FAILED,
            endTime = System.currentTimeMillis()
        )}

        showFailedNotification(task)
        
        activeDownloadCount--
        processQueue()
    }

    // ═════════════════════════════════════════════════════════════
    // ACTIONS
    // ═════════════════════════════════════════════════════════════

    fun pauseDownload(downloadId: Long) {
        // System DownloadManager doesn't support pause directly
        // Would need custom implementation for true pause/resume
        updateTask(downloadId) { it.copy(status = DownloadStatus.PAUSED) }
    }

    fun resumeDownload(downloadId: Long) {
        updateTask(downloadId) { it.copy(status = DownloadStatus.DOWNLOADING) }
    }

    fun cancelDownload(downloadId: Long) {
        systemDownloadManager.remove(downloadId)
        downloads.remove(downloadId)
        updateDownloadState()
        activeDownloadCount--
        processQueue()
        notificationManager.cancel(downloadId.toInt())
    }

    fun retryDownload(downloadId: Long) {
        val task = downloads[downloadId] ?: return
        cancelDownload(downloadId)
        enqueue(task.request)
    }

    fun clearCompleted() {
        downloads.values.removeAll { it.status == DownloadStatus.COMPLETED || it.status == DownloadStatus.FAILED }
        updateDownloadState()
    }

    // ═════════════════════════════════════════════════════════════
    // INSTALLATION
    // ═════════════════════════════════════════════════════════════

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }
        
        context.startActivity(intent)
    }

    // ═════════════════════════════════════════════════════════════
    // NOTIFICATIONS
    // ═════════════════════════════════════════════════════════════

    private fun showProgressNotification(task: DownloadTask, progress: Float) {
        val notification = NotificationCompat.Builder(context, CHANNEL_DOWNLOADS)
            .setContentTitle("Downloading ${task.request.appName}")
            .setContentText("${(progress * 100).toInt()}% - ${formatBytes(task.speed)}/s")
            .setSmallIcon(R.drawable.ic_fab_candy)
            .setProgress(100, (progress * 100).toInt(), false)
            .setOngoing(true)
            .setSilent(true)
            .addAction(
                R.drawable.ic_fab_candy,
                "Cancel",
                createPendingIntent(ACTION_CANCEL, task.downloadId ?: 0)
            )
            .build()

        notificationManager.notify(task.downloadId?.toInt() ?: 0, notification)
    }

    private fun showCompleteNotification(task: DownloadTask) {
        val installIntent = createPendingIntent(ACTION_INSTALL, task.downloadId ?: 0)
        
        val notification = NotificationCompat.Builder(context, CHANNEL_DOWNLOAD_COMPLETE)
            .setContentTitle("Download Complete")
            .setContentText("${task.request.appName} is ready to install")
            .setSmallIcon(R.drawable.ic_fab_candy)
            .setAutoCancel(true)
            .addAction(R.drawable.ic_fab_candy, "Install", installIntent)
            .addAction(R.drawable.ic_fab_candy, "Dismiss", createPendingIntent(ACTION_DISMISS, task.downloadId ?: 0))
            .build()

        notificationManager.notify(task.downloadId?.toInt() ?: 0, notification)
    }

    private fun showFailedNotification(task: DownloadTask, errorMessage: String? = null) {
        val retryIntent = createPendingIntent(ACTION_RETRY, task.downloadId ?: 0)

        val contentText = errorMessage 
            ?: "Failed to download ${task.request.appName}"

        val notification = NotificationCompat.Builder(context, CHANNEL_DOWNLOAD_FAILED)
            .setContentTitle("Download Failed")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_fab_candy)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setAutoCancel(true)
            .addAction(R.drawable.ic_fab_candy, "Retry", retryIntent)
            .addAction(R.drawable.ic_fab_candy, "Dismiss", createPendingIntent(ACTION_DISMISS, task.downloadId ?: 0))
            .build()

        notificationManager.notify(task.downloadId?.toInt() ?: 0, notification)
    }

    private fun createPendingIntent(action: String, downloadId: Long): PendingIntent {
        val intent = Intent(context, DownloadBroadcastReceiver::class.java).apply {
            this.action = action
            putExtra(EXTRA_DOWNLOAD_ID, downloadId)
        }
        return PendingIntent.getBroadcast(
            context,
            downloadId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ═════════════════════════════════════════════════════════════
    // RECEIVER
    // ═════════════════════════════════════════════════════════════

    private fun registerDownloadReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_CANCEL)
            addAction(ACTION_RETRY)
            addAction(ACTION_INSTALL)
            addAction(ACTION_DISMISS)
        }
        context.registerReceiver(DownloadBroadcastReceiver(), filter)
    }

    class DownloadBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val downloadId = intent.getLongExtra(EXTRA_DOWNLOAD_ID, -1)
            if (downloadId == -1L) return

            // Note: For Hilt migration, inject SmartDownloadManager or use a service locator pattern
            val manager = SmartDownloadManager(context.applicationContext)

            when (intent.action) {
                ACTION_CANCEL -> manager.cancelDownload(downloadId)
                ACTION_RETRY -> manager.retryDownload(downloadId)
                ACTION_DISMISS -> manager.notificationManager.cancel(downloadId.toInt())
                ACTION_INSTALL -> {
                    manager.downloads[downloadId]?.let {
                        manager.installApk(it.localFile)
                    }
                }
            }
        }
    }

    // ═════════════════════════════════════════════════════════════
    // HELPERS
    // ═════════════════════════════════════════════════════════════

    private fun updateTask(downloadId: Long, update: (DownloadTask) -> DownloadTask) {
        downloads[downloadId]?.let { current ->
            downloads[downloadId] = update(current)
            updateDownloadState()
        }
    }

    private fun updateDownloadState() {
        _downloadState.value = downloads.values.toList()
    }

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> "%.2f GB".format(bytes / 1_000_000_000f)
            bytes >= 1_000_000 -> "%.2f MB".format(bytes / 1_000_000f)
            bytes >= 1_000 -> "%.2f KB".format(bytes / 1_000f)
            else -> "$bytes B"
        }
    }

    private fun DownloadStatus.isActive(): Boolean {
        return this == DownloadStatus.PENDING || this == DownloadStatus.DOWNLOADING
    }

    // ═════════════════════════════════════════════════════════════
    // CONSTANTS
    // ═════════════════════════════════════════════════════════════

    companion object {
        const val CHANNEL_DOWNLOADS = "downloads"
        const val CHANNEL_DOWNLOAD_COMPLETE = "download_complete"
        const val CHANNEL_DOWNLOAD_FAILED = "download_failed"

        const val ACTION_CANCEL = "com.sugarmunch.download.CANCEL"
        const val ACTION_RETRY = "com.sugarmunch.download.RETRY"
        const val ACTION_INSTALL = "com.sugarmunch.download.INSTALL"
        const val ACTION_DISMISS = "com.sugarmunch.download.DISMISS"
        const val EXTRA_DOWNLOAD_ID = "download_id"
    }
}

// ═════════════════════════════════════════════════════════════
// DATA CLASSES
// ═════════════════════════════════════════════════════════════

data class DownloadTask(
    val request: SmartDownloadManager.DownloadRequest,
    val localFile: File,
    val downloadId: Long? = null,
    val status: DownloadStatus,
    val progress: Float,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val speed: Long,
    val startTime: Long,
    val endTime: Long? = null,
    val signatureVerified: Boolean = false,
    val errorMessage: String? = null
) {
    val estimatedTimeRemaining: Long
        get() {
            if (speed <= 0 || totalBytes <= 0) return 0
            val remaining = totalBytes - bytesDownloaded
            return (remaining * 1000) / speed
        }
}

enum class DownloadStatus {
    PENDING, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
}
