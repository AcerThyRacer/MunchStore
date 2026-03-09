package com.sugarmunch.app.p2p

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Local Network Discovery using mDNS/Bonjour
 * 
 * Features:
 * - Works on same WiFi network without internet
 * - No Google Play Services required
 * - Cross-platform compatible
 * - Automatic service registration and discovery
 */
class LocalNetworkDiscovery private constructor(private val context: Context) {

    companion object {
        private const val TAG = "LocalNetworkDiscovery"
        private const val SERVICE_TYPE = "_sugarmunch._tcp"
        private const val SERVICE_NAME_PREFIX = "SugarMunch-"
        private const val DISCOVERY_TIMEOUT_MS = 30000L

        @Volatile
        private var instance: LocalNetworkDiscovery? = null

        fun getInstance(context: Context): LocalNetworkDiscovery {
            return instance ?: synchronized(this) {
                instance ?: LocalNetworkDiscovery(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Discovery state
    private val _discoveredPeers = MutableStateFlow<List<DiscoveredPeer>>(emptyList())
    val discoveredPeers: StateFlow<List<DiscoveredPeer>> = _discoveredPeers.asStateFlow()

    private val _discoveryState = MutableStateFlow<DiscoveryState>(DiscoveryState.Idle)
    val discoveryState: StateFlow<DiscoveryState> = _discoveryState.asStateFlow()

    // Active discovery listeners
    private val discoveryListeners = ConcurrentHashMap<String, NsdManager.DiscoveryListener>()
    private val registeredServices = ConcurrentHashMap<String, NsdManager.RegistrationListener>()
    private val resolveQueue = ConcurrentHashMap<String, DiscoveredPeer>()

    // My service info
    private var myServiceInfo: NsdServiceInfo? = null
    private var deviceId: String = generateDeviceId()

    // ═════════════════════════════════════════════════════════════
    // DEVICE INFO
    // ═════════════════════════════════════════════════════════════

    data class DiscoveredPeer(
        val serviceName: String,
        val hostAddress: InetAddress?,
        val port: Int,
        val deviceId: String,
        val deviceName: String,
        val timestamp: Long = System.currentTimeMillis(),
        val attributes: Map<String, String> = emptyMap()
    ) {
        val isOnline: Boolean
            get() = System.currentTimeMillis() - timestamp < 60000 // 60 second timeout

        fun getDisplayName(): String = deviceName.takeIf { it.isNotBlank() } 
            ?: serviceName.removePrefix(SERVICE_NAME_PREFIX)
    }

    sealed class DiscoveryState {
        object Idle : DiscoveryState()
        object Advertising : DiscoveryState()
        object Discovering : DiscoveryState()
        object Both : DiscoveryState()
        data class Error(val message: String) : DiscoveryState()
    }

    // ═════════════════════════════════════════════════════════════
    // SERVICE ADVERTISING (Make myself discoverable)
    // ═════════════════════════════════════════════════════════════

    /**
     * Start advertising this device on the local network
     */
    fun startAdvertising(
        deviceName: String = android.os.Build.MODEL,
        port: Int = 0,
        attributes: Map<String, String> = emptyMap()
    ): Boolean {
        if (!isWifiAvailable()) {
            _discoveryState.value = DiscoveryState.Error("WiFi not available")
            return false
        }

        val serviceName = "$SERVICE_NAME_PREFIX${deviceName.replace(" ", "_")}-$deviceId"
        
        val serviceInfo = NsdServiceInfo().apply {
            this.serviceName = serviceName
            this.serviceType = SERVICE_TYPE
            this.port = port
            
            // Add attributes
            setAttribute("deviceId", deviceId)
            setAttribute("deviceName", deviceName)
            setAttribute("version", "1.0")
            attributes.forEach { (key, value) ->
                setAttribute(key, value)
            }
        }

        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(info: NsdServiceInfo) {
                Log.d(TAG, "Service registered: ${info.serviceName}")
                myServiceInfo = info
                updateDiscoveryState()
            }

            override fun onRegistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Registration failed: $errorCode")
                _discoveryState.value = DiscoveryState.Error("Registration failed: $errorCode")
            }

            override fun onServiceUnregistered(info: NsdServiceInfo) {
                Log.d(TAG, "Service unregistered: ${info.serviceName}")
                myServiceInfo = null
                updateDiscoveryState()
            }

            override fun onUnregistrationFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Unregistration failed: $errorCode")
            }
        }

        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)
            registeredServices[serviceName] = listener
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register service", e)
            _discoveryState.value = DiscoveryState.Error(e.message ?: "Unknown error")
            return false
        }
    }

    /**
     * Stop advertising this device
     */
    fun stopAdvertising() {
        registeredServices.forEach { (name, listener) ->
            try {
                nsdManager.unregisterService(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering service", e)
            }
        }
        registeredServices.clear()
        myServiceInfo = null
        updateDiscoveryState()
    }

    // ═════════════════════════════════════════════════════════════
    // SERVICE DISCOVERY (Find other devices)
    // ═════════════════════════════════════════════════════════════

    /**
     * Start discovering other SugarMunch devices on the network
     */
    fun startDiscovery() {
        if (!isWifiAvailable()) {
            _discoveryState.value = DiscoveryState.Error("WiFi not available")
            return
        }

        // Stop existing discovery
        stopDiscovery()

        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.d(TAG, "Discovery started for $regType")
                updateDiscoveryState()
            }

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service found: ${serviceInfo.serviceName}")
                
                // Skip our own service
                if (serviceInfo.serviceName.contains(deviceId)) {
                    return
                }

                // Resolve the service to get full info
                resolveService(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) {
                Log.d(TAG, "Service lost: ${serviceInfo.serviceName}")
                
                // Remove from discovered peers
                _discoveredPeers.update { peers ->
                    peers.filter { it.serviceName != serviceInfo.serviceName }
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.d(TAG, "Discovery stopped: $serviceType")
                updateDiscoveryState()
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery start failed: $errorCode")
                _discoveryState.value = DiscoveryState.Error("Discovery failed: $errorCode")
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery stop failed: $errorCode")
            }
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
            discoveryListeners[SERVICE_TYPE] = listener
            
            // Auto-stop after timeout
            scope.launch {
                delay(DISCOVERY_TIMEOUT_MS)
                if (discoveryListeners.isNotEmpty()) {
                    refreshDiscovery()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start discovery", e)
            _discoveryState.value = DiscoveryState.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Stop discovering devices
     */
    fun stopDiscovery() {
        discoveryListeners.forEach { (type, listener) ->
            try {
                nsdManager.stopServiceDiscovery(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping discovery", e)
            }
        }
        discoveryListeners.clear()
        updateDiscoveryState()
    }

    /**
     * Refresh discovery - stop and restart
     */
    fun refreshDiscovery() {
        stopDiscovery()
        startDiscovery()
    }

    /**
     * Resolve a discovered service to get connection details
     */
    private fun resolveService(serviceInfo: NsdServiceInfo) {
        val resolveListener = object : NsdManager.ResolveListener {
            override fun onResolveFailed(info: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Resolve failed: ${info.serviceName}, error: $errorCode")
                
                // Retry once after delay
                if (!resolveQueue.containsKey(info.serviceName)) {
                    scope.launch {
                        delay(1000)
                        resolveQueue[info.serviceName] = DiscoveredPeer(
                            serviceName = info.serviceName,
                            hostAddress = null,
                            port = info.port,
                            deviceId = "",
                            deviceName = info.serviceName
                        )
                        try {
                            nsdManager.resolveService(info, this)
                        } catch (e: Exception) {
                            resolveQueue.remove(info.serviceName)
                        }
                    }
                }
            }

            override fun onServiceResolved(info: NsdServiceInfo) {
                Log.d(TAG, "Service resolved: ${info.serviceName} at ${info.host}:${info.port}")
                resolveQueue.remove(info.serviceName)

                val attributes = mutableMapOf<String, String>()
                info.attributes?.forEach { (key, value) ->
                    attributes[key] = String(value, Charsets.UTF_8)
                }

                val peer = DiscoveredPeer(
                    serviceName = info.serviceName,
                    hostAddress = info.host,
                    port = info.port,
                    deviceId = attributes["deviceId"] ?: "",
                    deviceName = attributes["deviceName"] ?: info.serviceName.removePrefix(SERVICE_NAME_PREFIX),
                    timestamp = System.currentTimeMillis(),
                    attributes = attributes
                )

                // Add or update peer
                _discoveredPeers.update { peers ->
                    (peers.filter { it.serviceName != peer.serviceName } + peer)
                        .sortedBy { it.getDisplayName() }
                }
            }
        }

        try {
            nsdManager.resolveService(serviceInfo, resolveListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving service", e)
        }
    }

    // ═════════════════════════════════════════════════════════════
    // WIFI DIRECT ALTERNATIVE
    // ═════════════════════════════════════════════════════════════

    /**
     * Get local IP address for direct connection
     */
    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                
                // Skip loopback and disabled interfaces
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    // Prefer IPv4
                    if (!address.isLoopbackAddress && address.hostAddress.indexOf(':') < 0) {
                        return address.hostAddress
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting local IP", e)
        }
        return null
    }

    /**
     * Get WiFi network info
     */
    fun getWifiInfo(): WifiInfo? {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val connectionInfo = wifiManager?.connectionInfo
        
        return connectionInfo?.let {
            WifiInfo(
                ssid = it.ssid?.removeSurrounding("\"") ?: "Unknown",
                ipAddress = formatIpAddress(it.ipAddress),
                macAddress = it.macAddress,
                linkSpeed = it.linkSpeed
            )
        }
    }

    data class WifiInfo(
        val ssid: String,
        val ipAddress: String,
        val macAddress: String,
        val linkSpeed: Int // Mbps
    )

    // ═════════════════════════════════════════════════════════════
    // UTILITY METHODS
    // ═════════════════════════════════════════════════════════════

    /**
     * Check if WiFi is available and connected
     */
    fun isWifiAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Check if any network is available
     */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Generate a unique device ID
     */
    private fun generateDeviceId(): String {
        return UUID.randomUUID().toString().take(8)
    }

    /**
     * Update discovery state based on current operations
     */
    private fun updateDiscoveryState() {
        val isAdvertising = registeredServices.isNotEmpty()
        val isDiscovering = discoveryListeners.isNotEmpty()

        _discoveryState.value = when {
            isAdvertising && isDiscovering -> DiscoveryState.Both
            isAdvertising -> DiscoveryState.Advertising
            isDiscovering -> DiscoveryState.Discovering
            else -> DiscoveryState.Idle
        }
    }

    /**
     * Format IP address from integer
     */
    private fun formatIpAddress(ip: Int): String {
        return String.format(
            "%d.%d.%d.%d",
            ip and 0xff,
            ip shr 8 and 0xff,
            ip shr 16 and 0xff,
            ip shr 24 and 0xff
        )
    }

    /**
     * Clean up all resources
     */
    fun cleanup() {
        stopAdvertising()
        stopDiscovery()
        scope.cancel()
    }

    /**
     * Find a specific peer by device ID
     */
    fun findPeer(deviceId: String): DiscoveredPeer? {
        return _discoveredPeers.value.find { it.deviceId == deviceId }
    }

    /**
     * Get count of discovered peers
     */
    fun getPeerCount(): Int = _discoveredPeers.value.size

    /**
     * Clear discovered peers list
     */
    fun clearPeers() {
        _discoveredPeers.value = emptyList()
    }
}
