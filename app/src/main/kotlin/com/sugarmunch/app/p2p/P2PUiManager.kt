package com.sugarmunch.app.p2p

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.flow.*
import java.util.*

/**
 * P2P UI Manager - UI state management and helper functions for P2P sharing
 * 
 * Features:
 * - Nearby users list state management
 * - Transfer progress tracking
 * - QR code generation for quick pairing
 * - Share history tracking
 * - UI event handling
 */
class P2PUiManager private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "p2p_share_history"
        private const val MAX_HISTORY_ITEMS = 50
        private const val QR_CODE_SIZE = 512

        @Volatile
        private var instance: P2PUiManager? = null

        fun getInstance(context: Context): P2PUiManager {
            return instance ?: synchronized(this) {
                instance ?: P2PUiManager(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ═════════════════════════════════════════════════════════════
    // UI STATE
    // ═════════════════════════════════════════════════════════════

    // Discovery state
    private val _discoveryMode = MutableStateFlow<DiscoveryMode>(DiscoveryMode.NEARBY)
    val discoveryMode: StateFlow<DiscoveryMode> = _discoveryMode.asStateFlow()

    // Nearby devices list
    private val _nearbyDevices = MutableStateFlow<List<NearbyDevice>>(emptyList())
    val nearbyDevices: StateFlow<List<NearbyDevice>> = _nearbyDevices.asStateFlow()

    // Selected device for sharing
    private val _selectedDevice = MutableStateFlow<NearbyDevice?>(null)
    val selectedDevice: StateFlow<NearbyDevice?> = _selectedDevice.asStateFlow()

    // Transfer state
    private val _currentTransfer = MutableStateFlow<TransferUiState?>(null)
    val currentTransfer: StateFlow<TransferUiState?> = _currentTransfer.asStateFlow()

    // Share history
    private val _shareHistory = MutableStateFlow<List<ShareHistoryItem>>(emptyList())
    val shareHistory: StateFlow<List<ShareHistoryItem>> = _shareHistory.asStateFlow()

    // QR code for pairing
    private val _pairingQrCode = MutableStateFlow<Bitmap?>(null)
    val pairingQrCode: StateFlow<Bitmap?> = _pairingQrCode.asStateFlow()

    // UI Events
    private val _uiEvents = MutableSharedFlow<P2PUiEvent>()
    val uiEvents: SharedFlow<P2PUiEvent> = _uiEvents.asSharedFlow()

    // ═════════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═════════════════════════════════════════════════════════════

    enum class DiscoveryMode {
        NEARBY,      // Google Nearby Connections
        LOCAL_WIFI,  // mDNS/Bonjour
        QR_CODE      // QR code pairing
    }

    data class NearbyDevice(
        val id: String,
        val name: String,
        val connectionType: ConnectionType,
        val status: DeviceStatus,
        val signalStrength: Int = 0, // 0-100
        val lastSeen: Long = System.currentTimeMillis(),
        val endpointId: String? = null, // For Nearby Connections
        val ipAddress: String? = null,  // For mDNS
        val port: Int = 0
    ) {
        fun getStatusText(): String = when (status) {
            DeviceStatus.AVAILABLE -> "Available"
            DeviceStatus.CONNECTING -> "Connecting..."
            DeviceStatus.CONNECTED -> "Connected"
            DeviceStatus.TRANSFERRING -> "Transferring..."
            DeviceStatus.BUSY -> "Busy"
            DeviceStatus.OFFLINE -> "Offline"
        }

        fun getSignalIcon(): String = when {
            signalStrength >= 75 -> "📶"
            signalStrength >= 50 -> "📶"
            signalStrength >= 25 -> "📶"
            signalStrength > 0 -> "📶"
            else -> "📵"
        }
    }

    enum class ConnectionType {
        WIFI_DIRECT,
        BLUETOOTH,
        LOCAL_NETWORK
    }

    enum class DeviceStatus {
        AVAILABLE,
        CONNECTING,
        CONNECTED,
        TRANSFERRING,
        BUSY,
        OFFLINE
    }

    data class TransferUiState(
        val transferId: String,
        val fileName: String,
        val appName: String?,
        val direction: TransferDirection,
        val status: TransferStatus,
        val progress: Float,
        val bytesTransferred: Long,
        val totalBytes: Long,
        val speed: Long, // bytes per second
        val eta: Long, // milliseconds
        val fromDevice: String,
        val toDevice: String,
        val startTime: Long = System.currentTimeMillis(),
        val errorMessage: String? = null
    ) {
        fun getProgressPercent(): Int = (progress * 100).toInt()
        
        fun getFormattedSpeed(): String {
            return when {
                speed >= 1_000_000 -> "%.1f MB/s".format(speed / 1_000_000f)
                speed >= 1_000 -> "%.1f KB/s".format(speed / 1_000f)
                else -> "$speed B/s"
            }
        }
        
        fun getFormattedEta(): String {
            if (eta <= 0) return "Calculating..."
            val seconds = eta / 1000
            return when {
                seconds < 60 -> "$seconds sec"
                seconds < 3600 -> "${seconds / 60} min ${seconds % 60} sec"
                else -> "${seconds / 3600} hr ${(seconds % 3600) / 60} min"
            }
        }
        
        fun getFormattedSize(): String {
            return when {
                totalBytes >= 1_000_000_000 -> "%.2f GB".format(totalBytes / 1_000_000_000f)
                totalBytes >= 1_000_000 -> "%.2f MB".format(totalBytes / 1_000_000f)
                totalBytes >= 1_000 -> "%.2f KB".format(totalBytes / 1_000f)
                else -> "$totalBytes B"
            }
        }
    }

    enum class TransferDirection {
        SENDING,
        RECEIVING
    }

    enum class TransferStatus {
        PENDING,
        IN_PROGRESS,
        PAUSED,
        COMPLETED,
        FAILED,
        CANCELLED,
        VERIFYING
    }

    data class ShareHistoryItem(
        val id: String,
        val appName: String,
        val appVersion: String?,
        val fileSize: Long,
        val deviceName: String,
        val direction: TransferDirection,
        val timestamp: Long,
        val status: TransferStatus,
        val filePath: String? = null
    ) {
        fun getFormattedTime(): String {
            val date = Date(timestamp)
            val now = System.currentTimeMillis()
            val diff = now - timestamp
            
            return when {
                diff < 60_000 -> "Just now"
                diff < 3_600_000 -> "${diff / 60_000} min ago"
                diff < 86_400_000 -> "${diff / 3_600_000} hours ago"
                else -> SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()).format(date)
            }
        }
    }

    sealed class P2PUiEvent {
        data class ShowTransferRequest(val fromDevice: String, val appName: String) : P2PUiEvent()
        data class TransferComplete(val filePath: String) : P2PUiEvent()
        data class TransferFailed(val error: String) : P2PUiEvent()
        data class DeviceConnected(val deviceName: String) : P2PUiEvent()
        data class DeviceDisconnected(val deviceName: String) : P2PUiEvent()
        data class ShowError(val message: String) : P2PUiEvent()
    }

    // ═════════════════════════════════════════════════════════════
    // DISCOVERY MODE
    // ═════════════════════════════════════════════════════════════

    fun setDiscoveryMode(mode: DiscoveryMode) {
        _discoveryMode.value = mode
        when (mode) {
            DiscoveryMode.QR_CODE -> generatePairingQrCode()
            else -> _pairingQrCode.value = null
        }
    }

    // ═════════════════════════════════════════════════════════════
    // NEARBY DEVICES
    // ═════════════════════════════════════════════════════════════

    fun updateNearbyDevices(devices: List<NearbyDevice>) {
        _nearbyDevices.value = devices.sortedByDescending { it.signalStrength }
    }

    fun addOrUpdateDevice(device: NearbyDevice) {
        _nearbyDevices.update { devices ->
            val filtered = devices.filter { it.id != device.id }
            (filtered + device).sortedByDescending { it.signalStrength }
        }
    }

    fun removeDevice(deviceId: String) {
        _nearbyDevices.update { devices ->
            devices.filter { it.id != deviceId }
        }
    }

    fun selectDevice(device: NearbyDevice?) {
        _selectedDevice.value = device
    }

    fun clearDevices() {
        _nearbyDevices.value = emptyList()
    }

    // ═════════════════════════════════════════════════════════════
    // TRANSFER STATE
    // ═════════════════════════════════════════════════════════════

    fun startTransfer(
        transferId: String,
        fileName: String,
        appName: String?,
        direction: TransferDirection,
        totalBytes: Long,
        deviceName: String
    ) {
        _currentTransfer.value = TransferUiState(
            transferId = transferId,
            fileName = fileName,
            appName = appName,
            direction = direction,
            status = TransferStatus.PENDING,
            progress = 0f,
            bytesTransferred = 0,
            totalBytes = totalBytes,
            speed = 0,
            eta = 0,
            fromDevice = if (direction == TransferDirection.RECEIVING) deviceName else "You",
            toDevice = if (direction == TransferDirection.SENDING) deviceName else "You"
        )
    }

    fun updateTransferProgress(
        bytesTransferred: Long,
        speed: Long
    ) {
        _currentTransfer.update { state ->
            state?.let {
                val progress = if (it.totalBytes > 0) {
                    bytesTransferred.toFloat() / it.totalBytes
                } else 0f
                
                val remaining = it.totalBytes - bytesTransferred
                val eta = if (speed > 0) (remaining * 1000) / speed else 0
                
                it.copy(
                    bytesTransferred = bytesTransferred,
                    progress = progress.coerceIn(0f, 1f),
                    speed = speed,
                    eta = eta,
                    status = TransferStatus.IN_PROGRESS
                )
            }
        }
    }

    fun setTransferStatus(status: TransferStatus, errorMessage: String? = null) {
        _currentTransfer.update { state ->
            state?.copy(status = status, errorMessage = errorMessage)
        }
    }

    fun completeTransfer(filePath: String? = null) {
        _currentTransfer.update { state ->
            state?.copy(
                status = TransferStatus.COMPLETED,
                progress = 1f
            )
        }
        
        // Add to history
        _currentTransfer.value?.let { transfer ->
            addToHistory(transfer, filePath)
        }
        
        // Clear after delay
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(3000)
            _currentTransfer.value = null
        }
    }

    fun cancelTransfer() {
        _currentTransfer.update { state ->
            state?.copy(status = TransferStatus.CANCELLED)
        }
        kotlinx.coroutines.GlobalScope.launch {
            kotlinx.coroutines.delay(1000)
            _currentTransfer.value = null
        }
    }

    fun clearTransfer() {
        _currentTransfer.value = null
    }

    // ═════════════════════════════════════════════════════════════
    // QR CODE GENERATION
    // ═════════════════════════════════════════════════════════════

    fun generatePairingQrCode() {
        val deviceId = getDeviceId()
        val deviceName = android.os.Build.MODEL
        val localIp = LocalNetworkDiscovery.getInstance(context).getLocalIpAddress()
        
        val qrData = """
            {
                "type": "sugarmunch_p2p",
                "deviceId": "$deviceId",
                "deviceName": "$deviceName",
                "ip": "$localIp",
                "port": 8888
            }
        """.trimIndent()

        try {
            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.MARGIN, 2)
                put(EncodeHintType.ERROR_CORRECTION, com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M)
            }

            val writer = QRCodeWriter()
            val bitMatrix: BitMatrix = writer.encode(qrData, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hints)
            
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix.get(x, y)) AndroidColor.BLACK else AndroidColor.WHITE)
                }
            }
            
            _pairingQrCode.value = bmp
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate pairing QR code", e)
            _pairingQrCode.value = null
        }
    }

    fun parseQrCodeData(data: String): QrCodeData? {
        return try {
            // Simple JSON parsing - in production use Gson
            val map = data.trim().removeSurrounding("{", "}")
                .split(",\n")
                .associate { line ->
                    val parts = line.trim().split(": ", limit = 2)
                    if (parts.size == 2) {
                        parts[0].trim('"') to parts[1].trim().trim('"')
                    } else {
                        "" to ""
                    }
                }
            
            if (map["type"] == "sugarmunch_p2p") {
                QrCodeData(
                    deviceId = map["deviceId"] ?: "",
                    deviceName = map["deviceName"] ?: "Unknown",
                    ipAddress = map["ip"] ?: "",
                    port = map["port"]?.toIntOrNull() ?: 8888
                )
            } else null
        } catch (e: Exception) {
            null
        }
    }

    data class QrCodeData(
        val deviceId: String,
        val deviceName: String,
        val ipAddress: String,
        val port: Int
    )

    // ═════════════════════════════════════════════════════════════
    // SHARE HISTORY
    // ═════════════════════════════════════════════════════════════

    private fun addToHistory(transfer: TransferUiState, filePath: String?) {
        val item = ShareHistoryItem(
            id = transfer.transferId,
            appName = transfer.appName ?: transfer.fileName,
            appVersion = null,
            fileSize = transfer.totalBytes,
            deviceName = if (transfer.direction == TransferDirection.SENDING) transfer.toDevice else transfer.fromDevice,
            direction = transfer.direction,
            timestamp = System.currentTimeMillis(),
            status = transfer.status,
            filePath = filePath
        )

        _shareHistory.update { history ->
            val newHistory = listOf(item) + history
            if (newHistory.size > MAX_HISTORY_ITEMS) {
                newHistory.take(MAX_HISTORY_ITEMS)
            } else {
                newHistory
            }
        }

        saveHistory()
    }

    fun loadHistory() {
        val historyJson = prefs.getString("history", null)
        // Parse and load history
        // In production, use Gson to deserialize
    }

    private fun saveHistory() {
        // Save history to prefs
        // In production, use Gson to serialize
    }

    fun clearHistory() {
        _shareHistory.value = emptyList()
        prefs.edit().remove("history").apply()
    }

    fun removeHistoryItem(itemId: String) {
        _shareHistory.update { history ->
            history.filter { it.id != itemId }
        }
        saveHistory()
    }

    // ═════════════════════════════════════════════════════════════
    // UI EVENTS
    // ═════════════════════════════════════════════════════════════

    suspend fun emitEvent(event: P2PUiEvent) {
        _uiEvents.emit(event)
    }

    // ═════════════════════════════════════════════════════════════
    // UTILITY
    // ═════════════════════════════════════════════════════════════

    private fun getDeviceId(): String {
        return prefs.getString("device_id", null) ?: run {
            val newId = UUID.randomUUID().toString().take(8)
            prefs.edit().putString("device_id", newId).apply()
            newId
        }
    }

    fun getLocalDeviceInfo(): NearbyDevice {
        return NearbyDevice(
            id = getDeviceId(),
            name = android.os.Build.MODEL,
            connectionType = ConnectionType.WIFI_DIRECT,
            status = DeviceStatus.AVAILABLE,
            signalStrength = 100
        )
    }

    fun cleanup() {
        _pairingQrCode.value?.recycle()
        _pairingQrCode.value = null
    }
}
