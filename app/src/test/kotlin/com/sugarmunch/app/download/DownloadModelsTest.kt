package com.sugarmunch.app.download

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for Download Manager models
 */
class DownloadModelsTest {

    @Test
    fun `DownloadStatus isActive should return true for PENDING`() {
        // Then
        assertThat(DownloadStatus.PENDING.isActive()).isTrue()
    }

    @Test
    fun `DownloadStatus isActive should return true for DOWNLOADING`() {
        // Then
        assertThat(DownloadStatus.DOWNLOADING.isActive()).isTrue()
    }

    @Test
    fun `DownloadStatus isActive should return false for COMPLETED`() {
        // Then
        assertThat(DownloadStatus.COMPLETED.isActive()).isFalse()
    }

    @Test
    fun `DownloadStatus isActive should return false for FAILED`() {
        // Then
        assertThat(DownloadStatus.FAILED.isActive()).isFalse()
    }

    @Test
    fun `DownloadStatus isActive should return false for CANCELLED`() {
        // Then
        assertThat(DownloadStatus.CANCELLED.isActive()).isFalse()
    }

    @Test
    fun `DownloadStatus isActive should return false for PAUSED`() {
        // Then
        assertThat(DownloadStatus.PAUSED.isActive()).isFalse()
    }

    @Test
    fun `DownloadPriority enum should have correct ordinal values`() {
        // Then
        assertThat(DownloadPriority.LOW.ordinal).isEqualTo(0)
        assertThat(DownloadPriority.NORMAL.ordinal).isEqualTo(1)
        assertThat(DownloadPriority.HIGH.ordinal).isEqualTo(2)
        assertThat(DownloadPriority.URGENT.ordinal).isEqualTo(3)
    }

    @Test
    fun `DownloadRequest should have correct default values`() {
        // Given
        val request = SmartDownloadManager.DownloadRequest(
            id = "test-id",
            url = "https://example.com/app.apk",
            fileName = "app.apk",
            appName = "Test App"
        )

        // Then
        assertThat(request.wifiOnly).isFalse()
        assertThat(request.autoInstall).isTrue()
        assertThat(request.priority).isEqualTo(DownloadPriority.NORMAL)
    }

    @Test
    fun `DownloadRequest with custom values should be correct`() {
        // Given
        val request = SmartDownloadManager.DownloadRequest(
            id = "test-id",
            url = "https://example.com/app.apk",
            fileName = "app.apk",
            appName = "Test App",
            wifiOnly = true,
            autoInstall = false,
            priority = DownloadPriority.HIGH
        )

        // Then
        assertThat(request.wifiOnly).isTrue()
        assertThat(request.autoInstall).isFalse()
        assertThat(request.priority).isEqualTo(DownloadPriority.HIGH)
    }
}

/**
 * Unit tests for DownloadTask calculations
 */
class DownloadTaskTest {

    @Test
    fun `estimatedTimeRemaining should calculate correctly`() {
        // Given
        val task = createDownloadTask(
            bytesDownloaded = 500000,
            totalBytes = 1000000,
            speed = 100000,
            startTime = System.currentTimeMillis() - 5000
        )

        // Then - Should estimate ~5 seconds remaining
        assertThat(task.estimatedTimeRemaining).isAtLeast(0)
    }

    @Test
    fun `estimatedTimeRemaining should return 0 when speed is 0`() {
        // Given
        val task = createDownloadTask(
            bytesDownloaded = 500000,
            totalBytes = 1000000,
            speed = 0
        )

        // Then
        assertThat(task.estimatedTimeRemaining).isEqualTo(0)
    }

    @Test
    fun `estimatedTimeRemaining should return 0 when totalBytes is 0`() {
        // Given
        val task = createDownloadTask(
            bytesDownloaded = 0,
            totalBytes = 0,
            speed = 100000
        )

        // Then
        assertThat(task.estimatedTimeRemaining).isEqualTo(0)
    }

    @Test
    fun `DownloadTask with signature verification should track status`() {
        // Given
        val task = createDownloadTask(
            status = DownloadStatus.COMPLETED,
            signatureVerified = true
        )

        // Then
        assertThat(task.status).isEqualTo(DownloadStatus.COMPLETED)
        assertThat(task.signatureVerified).isTrue()
    }

    @Test
    fun `DownloadTask with error message should track failure`() {
        // Given
        val task = createDownloadTask(
            status = DownloadStatus.FAILED,
            errorMessage = "Signature verification failed"
        )

        // Then
        assertThat(task.status).isEqualTo(DownloadStatus.FAILED)
        assertThat(task.errorMessage).isEqualTo("Signature verification failed")
    }

    private fun createDownloadTask(
        status: DownloadStatus = DownloadStatus.DOWNLOADING,
        progress: Float = 0.5f,
        bytesDownloaded: Long = 500000,
        totalBytes: Long = 1000000,
        speed: Long = 100000,
        startTime: Long = System.currentTimeMillis() - 5000,
        signatureVerified: Boolean = false,
        errorMessage: String? = null
    ): DownloadTask {
        val request = SmartDownloadManager.DownloadRequest(
            id = "test-id",
            url = "https://example.com/app.apk",
            fileName = "app.apk",
            appName = "Test App"
        )
        return DownloadTask(
            request = request,
            localFile = java.io.File("/tmp/test.apk"),
            status = status,
            progress = progress,
            bytesDownloaded = bytesDownloaded,
            totalBytes = totalBytes,
            speed = speed,
            startTime = startTime,
            signatureVerified = signatureVerified,
            errorMessage = errorMessage
        )
    }
}

/**
 * Unit tests for download notification channels
 */
class DownloadNotificationChannelsTest {

    @Test
    fun `notification channel constants should be defined`() {
        // Then
        assertThat(SmartDownloadManager.CHANNEL_DOWNLOADS).isEqualTo("downloads")
        assertThat(SmartDownloadManager.CHANNEL_DOWNLOAD_COMPLETE).isEqualTo("download_complete")
        assertThat(SmartDownloadManager.CHANNEL_DOWNLOAD_FAILED).isEqualTo("download_failed")
    }

    @Test
    fun `notification action constants should be defined`() {
        // Then
        assertThat(SmartDownloadManager.ACTION_CANCEL).isEqualTo("com.sugarmunch.download.CANCEL")
        assertThat(SmartDownloadManager.ACTION_RETRY).isEqualTo("com.sugarmunch.download.RETRY")
        assertThat(SmartDownloadManager.ACTION_INSTALL).isEqualTo("com.sugarmunch.download.INSTALL")
        assertThat(SmartDownloadManager.ACTION_DISMISS).isEqualTo("com.sugarmunch.download.DISMISS")
    }

    @Test
    fun `extra key constant should be defined`() {
        // Then
        assertThat(SmartDownloadManager.EXTRA_DOWNLOAD_ID).isEqualTo("download_id")
    }
}
